package com.avadesign.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.avadesign.R;
import com.avadesign.SharedClassApp;
import com.avadesign.model.ZWaveNode;
import com.avadesign.util.AvaPref;

public class PollingService extends Service {

    private AvaPref appPref;
    private final String tag = PollingService.class.getSimpleName();
    private ArrayList<HashMap<String, Object>> mNodes;
    private final int pooling_period_local = 1000;
    private Timer timer = null;

    public final static String REFRESH_NODE_DATA = "REFRESH_NODE_DATA";
    public final static String HTTP_401 = "HTTP_401";
    public final static String HTTP_404 = "HTTP_404";

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(tag, "PollingService start...");
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateData() {
        getAvaApp().setNodesList(mNodes);

        ZWaveNode node = getAvaApp().getZWaveNode();

        if (node != null) {
            String targetId = node.id;

            for (HashMap<String, Object> map : mNodes) {
                ZWaveNode n = new ZWaveNode(map);

                if (n.id.equals(targetId)) {
                    getAvaApp().setZWaveNode(n);
                    break;
                } else {
                    getAvaApp().setZWaveNode(null);
                }
            }
        }

        Intent intent = new Intent(REFRESH_NODE_DATA);
        sendBroadcast(intent);

        mNodes = null;
    }

    private SharedClassApp getAvaApp() {
        return (SharedClassApp) getApplication();
    }

    private String getPrefVal(int resId) {
        return appPref.getValue(getString(resId));
    }

    private TimerTask mTimerTask = new TimerTask() {

        public void run() {
            int waitTime = getAvaApp().getWaitingTime();

            if (waitTime > 0) {
                waitTime--;
                getAvaApp().setWaitingTime(waitTime);
            } else {
                mNodes = new ArrayList<HashMap<String, Object>>();

                try {
                    String urlStr = String.format(getString(R.string.local_url_pattern), getPrefVal(R.string.key_gateway_ip),
                            getPrefVal(R.string.key_gateway_port)) + "/poll2.xml";
                    URL url = new URL(urlStr);

                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
                    HttpConnectionParams.setSoTimeout(httpParameters, 5000);
                    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
                    httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),
                            new UsernamePasswordCredentials("admin", "admin"));

                    HttpGet request = new HttpGet(urlStr);

                    BasicHttpResponse response = (BasicHttpResponse) httpClient.execute(request);
                    int code = response.getStatusLine().getStatusCode();

                    if (code == 401) {
                        broad401Error();
                    } else if (code == 404) {
                        // Do nothing now...
                    } else if (code == 200) {
                        String xml = EntityUtils.toString(response.getEntity());

                        if (parseXML(new String(xml.getBytes("ISO-8859-1"), "UTF-8"))) {
                            updateData();
                        }

                        System.gc();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    broad404Error();
                }
            }
        }

        private boolean parseXML(String xml) {
            try {
                Document document = DocumentHelper.parseText(xml);
                Iterator<?> it = document.selectNodes("/poll/admin").iterator();
                while (it.hasNext()) {
                    Element ele = (Element) it.next();
                    getAvaApp().setIsActive(ele.attribute("active").getValue().equalsIgnoreCase("true"));
                    getAvaApp().setControllerState(ele.getStringValue());
                }

                it = document.selectNodes("/poll/node").iterator();
                while (it.hasNext()) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    Element ele = (Element) it.next();
                    map.putAll(getAttr(ele));
                    map.put("value", getVals(ele));
                    mNodes.add(map);
                }
                document = null;
                it = null;

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // Get element attributes
        private HashMap<String, String> getAttr(Element ele) {
            HashMap<String, String> map = new HashMap<String, String>();
            try {
                List<?> attributes = ele.attributes();
                for (int i = 0; i < attributes.size(); i++) {
                    Attribute a = ((Attribute) attributes.get(i));
                    map.put(a.getName(), a.getValue());
                }
                map.put("sort_id", map.get("id"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return map;
        }

        // Get <value>
        private List<HashMap<String, Object>> getVals(Element element) {
            List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
            try {
                Iterator<?> it = element.elementIterator();
                while (it.hasNext()) {
                    Element ele = (Element) it.next();
                    if (ele.getQualifiedName().equalsIgnoreCase("value")) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.putAll(getAttr(ele));

                        List<String> items = getItems(ele);
                        if (items.size() > 0)
                            /*
                             * if it has <item> element, the attribute will has
                             * "current", we will get this attr with
                             * getAttr(ele), so no need to put this key
                             * ourselves. <value .. current="1"> <item>..</item>
                             * </value>
                             */
                            map.put("item", items);
                        else {
                            String current = ele.getTextTrim();

                            /*
                             * without <item>, add the current value ourselves.
                             */
                            map.put("current", current);
                        }

                        String help = getHelp(ele);
                        if (help.length() > 0)
                            map.put("help", help);

                        list.add(map);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        // Get <item>
        private List<String> getItems(Element element) {
            List<String> list = new ArrayList<String>();
            try {
                Iterator<?> it = element.elementIterator();
                while (it.hasNext()) {
                    Element ele = (Element) it.next();
                    if (ele.getQualifiedName().equals("item")) {
                        list.add(ele.getStringValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        // Get <help>
        private String getHelp(Element element) {
            String help = "";
            try {
                Iterator<?> it = element.elementIterator();
                while (it.hasNext()) {
                    Element ele = (Element) it.next();
                    if (ele.getQualifiedName().equals("help")) {
                        help = ele.getStringValue();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return help;
        }
    };

    public IBinder onBind(Intent intent) {
        Log.i(getClass().getSimpleName(), "onBind() had been invoked.");
        return null;
    }

    protected void broad401Error() {
        Intent i = new Intent(HTTP_401);
        sendBroadcast(i);
    }

    protected void broad404Error() {
        Intent i = new Intent(HTTP_404);
        sendBroadcast(i);
    }

    public void onCreate() {
        appPref = ((SharedClassApp) getApplication()).getAppPref();
        timer = new Timer();
        timer.schedule(mTimerTask, 1000, pooling_period_local);
        super.onCreate();
    }

    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }

        Log.i(tag, "PollingService stop...");
        super.onDestroy();
    }

}
