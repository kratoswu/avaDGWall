package com.avadesign.task;

import org.json.JSONObject;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphonePreferences.AccountBuilder;

import com.avadesign.R;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class SignInSIPServerTask extends AsyncTask<JSONObject, Void, Void> {
    
    private Activity activity;
    
    public SignInSIPServerTask(Activity activity) {
        this.activity = activity;
    }

    protected Void doInBackground(JSONObject... params) {
        JSONObject camData = params[0];
        
        try {
            String sipId = camData.getString(activity.getString(R.string.key_sipid));
            String sipPwd = camData.getString(activity.getString(R.string.key_sipPwd));
            String sipDomain = camData.getString(activity.getString(R.string.key_sipserver));
            String camName = camData.getString(activity.getString(R.string.key_cameraName));
            String proxy = activity.getString(R.string.setup_forced_proxy);
            
            AccountBuilder ab = new AccountBuilder(LinphoneManager.getLcIfManagerNotDestroyedOrNull());
            ab.setUsername(sipId).setUserId(sipId).setDomain(sipDomain).setPassword(sipPwd);
            
            if (!TextUtils.isEmpty(proxy)) {
                ab.setProxy(proxy).setOutboundProxyEnabled(true);
            }
            
            if (activity.getResources().getBoolean(R.bool.enable_push_id)) {
                LinphonePreferences linPref = LinphonePreferences.instance();
                String regId = linPref.getPushNotificationRegistrationID();
                String appId = activity.getString(R.string.push_sender_id);
                
                if (regId != null && linPref.isPushNotificationEnabled()) {
                    String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
                    ab.setContactParameters(contactInfos);
                }
            }
            
            ab.saveNewAccount();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
        
        return null;
    }

}
