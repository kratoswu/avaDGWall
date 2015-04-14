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

import com.avadesign.R;
import com.avadesign.util.AvaDecrypt;
import com.avadesign.util.HttpClientHelper;
import com.avadesign.util.StringUtil;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public abstract class LoadContactInfoTask extends AsyncTask<String, Void, List<JSONObject>> {
    
    protected Activity activity;
    
    public LoadContactInfoTask(Activity activity) {
        this.activity = activity;
    }

    protected List<JSONObject> doInBackground(String... params) {
        List<JSONObject> contactList = new ArrayList<JSONObject>();
        
        try {
            for (String sipNo : params) {
                String acc = getAccBySipId(sipNo);
                
                if (!StringUtil.isEmptyString(acc)) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("sipID", sipNo);
                    jsonObj.put("acc", acc);
                    
                    contactList.add(jsonObj);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
            contactList.clear();
        }
        
        return contactList;
    }
    
    protected void onPostExecute(List<JSONObject> result) {
        handleResult(result);
    }
    
    protected abstract void handleResult(List<JSONObject> result);

    private String getAccBySipId(String dialNum) throws Exception {
        int sipNo = Integer.parseInt(dialNum);
        sipNo %= 1000000;
        String urlStr = activity.getString(R.string.qualia_url) + "getUserAcc";
        HttpPost httpReq = new HttpPost(urlStr);
        
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("sipId", sipNo + ""));
        httpReq.setEntity(new UrlEncodedFormEntity(pairs));
        
        HttpClient httpClient = HttpClientHelper.getHttpClient();
        HttpResponse response = httpClient.execute(httpReq);
        
        if (response.getStatusLine().getStatusCode() == 200) {
            String source = EntityUtils.toString(response.getEntity());
            source = source.replace("\r", "").replace("\n", "").replace("%", "");
            source = AvaDecrypt.decode(source);
            
            if (source != null) {
                source = source.substring(1, source.lastIndexOf("]"));
                
                if (!source.startsWith("ERROR")) {
                    return source;
                }
            }
        }
        
        return null;
    }

}
