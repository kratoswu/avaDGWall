package com.avadesign.task;

import java.util.Map;


import android.os.AsyncTask;
import android.util.Log;

import com.avadesign.R;
import com.avadesign.SharedClassApp;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;

@SuppressWarnings("rawtypes")
public class SavePanelDetailTask extends AsyncTask<Map, Void, Void> {

    private AvaPref appPref;
    private SharedClassApp app;

    public SavePanelDetailTask(SharedClassApp avaApp) {
        appPref = avaApp.getAppPref();
        app = avaApp;
    }

    private String getValue(String key) {
        return appPref.getValue(key);
    }

    private String getString(int resId) {
        return app.getString(resId);
    }

    @SuppressWarnings("unchecked")
    protected Void doInBackground(Map... params) {
        if (params != null && params.length > 0) {
            String urlStr = String.format(getString(R.string.local_url_pattern), getValue(getString(R.string.key_gateway_ip)),
                    getValue(getString(R.string.key_gateway_port))) + getString(R.string.panel_list);
            String acc = getValue(getString(R.string.key_acc));
            String pwd = getValue(getString(R.string.key_pwd));

            Log.i(getTag(), "URL: [" + urlStr + "]");
            Log.i(getTag(), "acc: [" + acc + "]");
            Log.i(getTag(), "pwd: [" + pwd + "]");

            for (Map paramMap : params) {
                HttpCommunicator.send(urlStr, ((Map<String, String>)paramMap), acc, pwd, true);
            }
        }

        return null;
    }

    private String getTag() {
        return getClass().getSimpleName();
    }

}
