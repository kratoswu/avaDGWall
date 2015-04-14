package com.avadesign;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;
import com.avadesign.v4.PanelSettingMainActivity_New;

public class SettingMainScreenActivity extends Activity {

    public static SettingMainScreenActivity instance;
    private AvaPref appPref;
    private long previousMillis = 0L;
    private int fireCount = 0;
    private File sdCardRoot = Environment.getExternalStorageDirectory();
    private ProgressDialog waitPop;

    public void upgradeApp(View v) {
        File apk = new File(sdCardRoot, "avaDGWall.apk");

        if (apk.exists()) {
            apk.delete();
        }

        new UpgradeAppTask().execute(apk);
    }

    private class UpgradeAppTask extends AsyncTask<File, Void, Void> {

        private File apk;

        protected Void doInBackground(File... params) {
            apk = params[0];

            try {
                URL target = new URL("http://220.135.186.178/phoenix/avaDGWall.apk");
                HttpURLConnection urlConn = (HttpURLConnection) target.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.setDoOutput(true);
                urlConn.connect();

                InputStream inStream = urlConn.getInputStream();
                FileOutputStream fos = new FileOutputStream(apk);
                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                while ((bufferLength = inStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, bufferLength);
                }

                fos.close();
            } catch (Exception e) {
                Log.e("Upgrading app error", e.getMessage(), e);
            }

            return null;
        }

        protected void onPreExecute() {
            waitPop = new ProgressDialog(SettingMainScreenActivity.this);
            waitPop.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitPop.setMessage("Downloading package...");

            runOnUiThread(new Runnable() {

                public void run() {
                    waitPop.show();
                }
            });
        }

        protected void onPostExecute(Void result) {
            if (waitPop != null) {
                runOnUiThread(new Runnable() {

                    public void run() {
                        waitPop.dismiss();
                        waitPop = null;
                    }
                });
            }

            if (apk != null && apk.exists()) {
                // Install
                Intent it = new Intent(Intent.ACTION_VIEW);
                it.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
                startActivity(it);
            }
        }

    }

    public void fireSettingEvent(View v) {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();

        if ((timeInMillis - previousMillis) < 500) {
            fireCount++;
        } else {
            fireCount = 0;
        }

        previousMillis = timeInMillis;

        Log.v("fireCount", fireCount + "");

        if (fireCount == 4) {
            // Go to settings of android
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
            startActivity(intent);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.door, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean invalidateGatewaySettings() {
        if (appPref != null) {
            boolean result = false;

            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_gateway_ip)));
            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_gateway_port)));
            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_acc)));
            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_pwd)));

            return result;
        }

        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }

        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_main);

        if (getActionBar() != null) {
            getActionBar().setTitle("");
        }

        appPref = ((SharedClassApp) getApplication()).getAppPref();

        instance = this;
    }

    public void goToFuncDisplay(View v) {
        startActivity(new Intent(this, MainScnBtnSettingActivity.class));
    }

    public void goToMicGain(View v) {
        startActivity(new Intent(this, MICGainSettingActivity.class));
    }

    public void goToActivateCode(View v) {
        startActivity(new Intent(this, ActivateCodeActivity.class));
    }

    public void goToSceneSetting(View v) {
        if (invalidateGatewaySettings()) {
            Toast.makeText(this, R.string.empty_pref, Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(this, SceneSettingActivity.class));
        }
    }

    public void goToZWDSetting(View v) {
        if (invalidateGatewaySettings()) {
            Toast.makeText(this, R.string.empty_pref, Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(this, ZWaveDeviceSettingActivity.class));
        }
    }

    public void goToDPSetting(View v) {
        startActivity(new Intent(this, DPSettingActivity.class));
    }

    public void goToCtrlPnlSetting(View v) {
        if (invalidateGatewaySettings()) {
            Toast.makeText(this, R.string.empty_pref, Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(this, PanelSettingMainActivity_New.class));
        }
    }

    public void goToSystemSetting(View v) {
        startActivity(new Intent(this, SystemSettingActivity.class));
    }

    public void goToTestSetting(View v) {
//        startActivity(new Intent(this, CamOctavusScnActivity.class));

//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
//        startActivity(intent);

//        startActivity(new Intent(this, RTSPTestActivity.class));

        // TODO 在呼叫 VLC 之前, 先 kill
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processList = am.getRunningAppProcesses();

        try {
            for (RunningAppProcessInfo process : processList) {
                Log.v(process.processName, process.pid + "");

                if (process.processName.contains("vlc")) {
                    Log.e("kill process", process.processName);

                    Process.killProcess(process.pid);
                    Process.sendSignal(process.pid, Process.SIGNAL_KILL);
                    am.killBackgroundProcesses(process.processName);
                }
            }
        } catch (Exception e) {
            Log.e("", e.getMessage(), e);
        }

        try {
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("rtsp://admin:admin@192.168.3.33:80/live/h264/VGA"));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("rtsp://192.168.1.122/cam1/h264"));
            startActivity(intent);
        } catch (Exception e) {
            Log.e("", e.getMessage(), e);
        }
    }

    public void goToSecuritySetting(View v) {
        startActivity(new Intent(this, SecurityCallSettingActivity.class));
    }

    public void goToNVRSetting(View v) {
        startActivity(new Intent(this, CamListActivity.class));
    }

    public void goToTriggerSetting(View v) {
        if (invalidateGatewaySettings()) {
            Toast.makeText(this, R.string.empty_pref, Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(this, TriggerSettingActivity.class));
        }
    }

}
