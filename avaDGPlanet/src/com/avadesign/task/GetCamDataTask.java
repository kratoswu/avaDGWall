package com.avadesign.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphonePreferences.AccountBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.avadesign.R;
import com.avadesign.SharedClassApp;
import com.avadesign.util.AvaDecrypt;
import com.avadesign.util.HttpClientHelper;
import com.avadesign.util.StringUtil;

public class GetCamDataTask extends AsyncTask<Void, Void, JSONObject> {
    
    private Activity activity;
    
    public GetCamDataTask (Activity activity) {
        this.activity = activity;
    }

    protected JSONObject doInBackground(Void... params) {
        WifiManager wm = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        String macAddr = wi.getMacAddress();
        
        if (StringUtil.isEmptyString(macAddr)) {
            Log.e(getClass().getSimpleName(), "Can't get MAC address!");
            return null;
        } else {
            macAddr = macAddr.replace(":", "");
            
            try {
                PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                String version = pi.versionName;
                
                // Save cam data
               return getCamData(macAddr, version, "WP101", "0");
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), e.getMessage(), e);
                return null;
            }
        }
    }
    
    protected void onPostExecute(JSONObject camData) {
        if (camData != null) {
            Log.v("result", camData.toString());
            saveCamData(camData);
            
            // TODO Start sign task
            SignInSIPServerTask task = new SignInSIPServerTask(activity);
            task.execute(camData);
        }
    }

    private void saveCamData(JSONObject camData) {
        ((SharedClassApp) activity.getApplication()).getAppPref().setValue(activity.getString(R.string.key_cam_data), camData.toString());
    }

    private JSONObject getCamData(String mac, String fw, String model, String upnp) throws Exception {
        String urlStr = activity.getString(R.string.qualia_url) + "GetCamData";
        
        HttpPost httpReq = new HttpPost(urlStr);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("mac", mac));
        pairs.add(new BasicNameValuePair("fw", fw));
        pairs.add(new BasicNameValuePair("model", model));
        pairs.add(new BasicNameValuePair("upnp", upnp));
        httpReq.setEntity(new UrlEncodedFormEntity(pairs));
        
        HttpClient httpClient = HttpClientHelper.getHttpClient();
        HttpResponse response = httpClient.execute(httpReq);
        
        if (response.getStatusLine().getStatusCode() == 200) {
            String source = EntityUtils.toString(response.getEntity());
            source = source.replace("\r", "").replace("\n", "").replace("%", "");
            source = AvaDecrypt.decode(source);
            
            if (source != null) {
                source = source.substring(0, source.lastIndexOf("}") + 1);
            }
            
            Log.w("result", source + "");
            JSONObject result = new JSONObject(source);
            return result;
        }
        
        return null;
    }

}
