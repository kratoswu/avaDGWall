package com.avadesign.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.util.Log;

public class HttpCommunicator {

    public static String update(String syntax) {
        try {
            // URL url = new URL(syntax);

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpGet httpRequest = new HttpGet(syntax);

            BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient.execute(httpRequest);
            int code = httpResponse.getStatusLine().getStatusCode();

            if (code == 200) {
                String result = EntityUtils.toString(httpResponse.getEntity());

                return result;
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static boolean send(String syntax, Map<String, String> param, String username, String userpwd, boolean isLocal) {
        boolean success = false;
        try {
            URL url = new URL(syntax);

            HttpParams httpParameters = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);

            HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

            httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(username, userpwd));

            HttpPost httpRequest = new HttpPost(syntax);

            Iterator<Entry<String, String>> iter = param.entrySet().iterator();

            if (isLocal) {
                // without url encode
                StringBuilder params = new StringBuilder("");
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                    params.append((String) entry.getKey());
                    params.append("=");
                    params.append((String) entry.getValue());
                    params.append("&");
                }

                if (params.toString().endsWith("&")) {
                    params.delete(params.length() - 1, params.length());
                }

                ByteArrayEntity reqEntity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
                httpRequest.setEntity(reqEntity);
            } else {
                // need url encode

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                    params.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
                }

                httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }

            BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient.execute(httpRequest);
            int code = httpResponse.getStatusLine().getStatusCode();

            if (code == 200) {
                // System.out.println(EntityUtils.toString(httpResponse.getEntity()));
                success = true;
            } else {
                success = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return success;
    }

    public static String sendCmd(String urlStr, Map<String, String> param, String acc, String pwd) {
        try {
            URL url = new URL(urlStr);
            HttpParams httpParams = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 5000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            httpClient.getCredentialsProvider()
                    .setCredentials(new AuthScope(url.getHost(), url.getPort()), new UsernamePasswordCredentials(acc, pwd));

            HttpPost httpReq = new HttpPost(urlStr);
            StringBuffer paramBuff = new StringBuffer();

            for(Entry<String, String> entry : param.entrySet()) {
                paramBuff.append(entry.getKey() + "=" + entry.getValue() + "&");
            }

            if (paramBuff.toString().endsWith("&")) {
                paramBuff.delete(paramBuff.length() - 1, paramBuff.length());
            }

            Log.i(HttpCommunicator.class.getSimpleName(), "params: [" + paramBuff + "]");

            ByteArrayEntity bae = new ByteArrayEntity(paramBuff.toString().getBytes("UTF-8"));
            httpReq.setEntity(bae);

            HttpResponse httpRes = httpClient.execute(httpReq);

            if (httpRes.getStatusLine().getStatusCode() == 200) {
                return new String(EntityUtils.toString(httpRes.getEntity()).getBytes("ISO-8859-1"), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Document getPanelDetail(String syntax, Map<String, String> param, String username, String userpwd, boolean isLocal) {
        try {
            URL url = new URL(syntax);
            HttpParams httpParameters = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(username, userpwd));
            HttpPost httpRequest = new HttpPost(syntax);

            Iterator<Entry<String, String>> iter = param.entrySet().iterator();

            if (isLocal) {
                // without url encode
                StringBuilder params = new StringBuilder("");
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                    params.append((String) entry.getKey());
                    params.append("=");
                    params.append((String) entry.getValue());
                    params.append("&");
                }

                if (params.toString().endsWith("&")) {
                    params.delete(params.length() - 1, params.length());
                }

                Log.v("TAG", "" + params);

                ByteArrayEntity reqEntity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
                httpRequest.setEntity(reqEntity);
            } else {
                // need url encode

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                    params.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
                }

                httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }

            HttpResponse httpResponse = httpClient.execute(httpRequest);
            int code = httpResponse.getStatusLine().getStatusCode();

            if (code == 200) {
                String xml = EntityUtils.toString(httpResponse.getEntity());

                return DocumentHelper.parseText(new String(xml.getBytes("ISO-8859-1"), "UTF-8"));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<HashMap<String, String>> getSceneList(String syntax, Map<String, String> param, String username, String userpwd,
            boolean isLocal) {
        try {
            URL url = new URL(syntax);

            HttpParams httpParameters = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);

            HttpConnectionParams.setSoTimeout(httpParameters, 5000);

            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

            httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(username, userpwd));

            HttpPost httpRequest = new HttpPost(syntax);

            Iterator<Entry<String, String>> iter = param.entrySet().iterator();

            if (isLocal) {
                // without url encode
                StringBuilder params = new StringBuilder("");
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                    params.append((String) entry.getKey());
                    params.append("=");
                    params.append((String) entry.getValue());
                    params.append("&");
                }

                if (params.toString().endsWith("&")) {
                    params.delete(params.length() - 1, params.length());
                }

                Log.v("TAG", "" + params);

                ByteArrayEntity reqEntity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
                httpRequest.setEntity(reqEntity);
            } else {
                // need url encode

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                    params.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
                }

                httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }

            HttpResponse httpResponse = (HttpResponse) httpClient.execute(httpRequest);
            int code = httpResponse.getStatusLine().getStatusCode();

            if (code == 200) {
                // System.out.println(EntityUtils.toString(httpResponse.getEntity()));

                String xml = EntityUtils.toString(httpResponse.getEntity());

                // Log.v("TAG",xml);

                ArrayList<HashMap<String, String>> location = getSceneData(new String(xml.getBytes("ISO-8859-1"), "UTF-8"));
                xml = null;

                return location;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private static ArrayList<HashMap<String, String>> getSceneData(String xml) {
        try {
            Document document = DocumentHelper.parseText(xml);
            ArrayList<HashMap<String, String>> loc = new ArrayList<HashMap<String, String>>();

            for (Object o : document.selectNodes("scenes/sceneid")) {
                Element ele = (Element) o;
                loc.add(getAttr(ele));
            }

            return loc;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(HttpCommunicator.class.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    // Get element attributes
    private static HashMap<String, String> getAttr(Element ele) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            List<?> attributes = ele.attributes();
            for (int i = 0; i < attributes.size(); i++) {
                Attribute a = ((Attribute) attributes.get(i));
                map.put(a.getName(), a.getValue());
            }
            String current = ele.getTextTrim();
            map.put("current", current);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
