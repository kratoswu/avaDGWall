package com.avadesign.task;

import java.util.HashMap;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.avadesign.R;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;

public class SendCmdTask extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private AvaPref appPref;

    private String getString(int resId) {
        return context.getString(resId);
    }

    public SendCmdTask(Context context, AvaPref appPref) {
        this.context = context;
        this.appPref = appPref;
    }

    protected void onPreExecute() {
//        if (context instanceof Activity) {
//            ((Activity) context).runOnUiThread(new Runnable() {
//
//                public void run() {
//                    Toast.makeText(context, "Send command...", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }

    protected Boolean doInBackground(String... params) {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put(params[1], params[2]);

        String hostAddr = appPref.getValue(getString(R.string.key_gateway_ip));
        String hostPort = appPref.getValue(getString(R.string.key_gateway_port));
        String acc = appPref.getValue(getString(R.string.key_acc));
        String pwd = appPref.getValue(getString(R.string.key_pwd));

        // String urlStr = "http://192.168.1.31:5000/" + params[0];
        String urlStr = String.format(getString(R.string.local_url_pattern), hostAddr, hostPort) + "/" + params[0];
        
        Log.v(getClass().getSimpleName(), "Send command");
        boolean result = HttpCommunicator.send(urlStr, paramMap, acc, pwd, true);
        
        return result;
    }

    protected void onPostExecute(final Boolean result) {
//        if (context instanceof Activity) {
//            ((Activity) context).runOnUiThread(new Runnable() {
//
//                public void run() {
//                    String msg = result ? "Command sent successfully!" : "Command sent failed!";
//                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }

}
