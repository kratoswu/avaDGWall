package com.avadesign.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.avadesign.R;
import com.avadesign.util.AES;
import com.avadesign.util.HttpClientHelper;
import com.avadesign.util.StringUtil;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.SSLCertificateSocketFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 請求 Qualia server 發出 push 的 task
 * */
public class SendCallNotificationTask extends AsyncTask<String, Void, Void> {
    
    private Activity activity;
    
    public SendCallNotificationTask (Activity activity) {
        this.activity = activity;
    }

    protected Void doInBackground(String... params) {
        WifiManager wm = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        String macAddr = wi.getMacAddress();
        
        if (StringUtil.isEmptyString(macAddr)) {
            Log.e(getClass().getSimpleName(), "Can't get MAC address!");
            return null;
        } else {
            macAddr = macAddr.replace(":", "");
            
            try {
                String data = "<ava_alert><type>status_call</type><mac>" + macAddr + "</mac></ava_alert>";
                String sipID = params[0];
                String urlStr = activity.getString(R.string.qualia_url) + "alarmFromWP";
                
                Log.v("Call Push", urlStr);
                
                HttpPost httpReq = new HttpPost(urlStr);
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("data", data));
                pairs.add(new BasicNameValuePair("sipid", sipID));
                httpReq.setEntity(new UrlEncodedFormEntity(pairs));
                
                Log.v(getClass().getSimpleName(), "Push...");
                HttpClient httpClient = HttpClientHelper.getHttpClient();
                HttpResponse response = httpClient.execute(httpReq);
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    Log.e("Error", response.getStatusLine().getStatusCode() + "");
                }
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        
        return null;
    }

}
