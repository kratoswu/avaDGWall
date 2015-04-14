package com.avadesign;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

import com.avadesign.comp.CamListDialog;
import com.avadesign.comp.DeviceListDialog;
import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;

public class NVRScnSettingActivity extends SearchDeviceActivity implements OnDismissListener {
    
    private ImageView camImgView_00, camImgView_01, camImgView_02, camImgView_03, camImgView_04, camImgView_05;

    private List<JSONObject> camItemList;

    private AvaPref appPref;

    private Timer timer00;

    private Timer timer01;

    private Timer timer02;

    private Timer timer03;

    private Timer timer04;

    private Timer timer05;
    
    private int currentKey = -1;
    
    public void exitActivity(View v) {
        finish();
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        
        setContentView(R.layout.activity_nvr);
        
        initCamData();

        initCamImgView00();
        initCamImgView01();
        initCamImgView02();
        initCamImgView03();
        initCamImgView04();
        initCamImgView05();
    }

    public void displaySearchResult(final Map<String, Map<String, String>> deviceDataMap) {
        runOnUiThread(new Runnable() {
            
            public void run() {
                DeviceListDialog dialog = new DeviceListDialog(NVRScnSettingActivity.this, deviceDataMap);
                dialog.show();
            }
        });
    }

    public void updateIPAddrField(Map<String, String> data) {
        if (currentKey != -1) {
            getAppPref().setValue(getString(currentKey), data.get("mac"));
        }
    }
    
    protected void onDestroy() {
        timer00.cancel();
        timer01.cancel();
        timer02.cancel();
        timer03.cancel();
        timer04.cancel();
        timer05.cancel();
        
        super.onDestroy();
    }
    
    private AvaPref getAppPref() {
        if (appPref == null) {
            appPref = ((SharedClassApp) getApplication()).getAppPref();
        }

        return appPref;
    }
    
    private JSONObject getCamViewData(String mac) throws Exception {
        for (JSONObject obj : camItemList) {
            if (obj.getString(getString(R.string.key_cam_mac)).equals(mac)) {
                return obj;
            }
        }

        return null;
    }

    private void initCamImgView05() {
        camImgView_05 = (ImageView) findViewById(R.id.camImgView_05);
        timer05 = new Timer();
        initCamImgView(R.string.key_cam_05, camImgView_05, timer05);
    }

    private void initCamImgView04() {
        camImgView_04 = (ImageView) findViewById(R.id.camImgView_04);
        timer04 = new Timer();
        initCamImgView(R.string.key_cam_04, camImgView_04, timer04);
    }

    private void initCamImgView03() {
        camImgView_03 = (ImageView) findViewById(R.id.camImgView_03);
        timer03 = new Timer();
        initCamImgView(R.string.key_cam_03, camImgView_03, timer03);
    }

    private void initCamImgView02() {
        camImgView_02 = (ImageView) findViewById(R.id.camImgView_02);
        timer02 = new Timer();
        initCamImgView(R.string.key_cam_02, camImgView_02, timer02);
    }

    private void initCamImgView01() {
        camImgView_01 = (ImageView) findViewById(R.id.camImgView_01);
        timer01 = new Timer();
        initCamImgView(R.string.key_cam_01, camImgView_01, timer01);
    }

    private void initCamImgView00() {
        camImgView_00 = (ImageView) findViewById(R.id.camImgView_00);
        timer00 = new Timer();
        initCamImgView(R.string.key_cam_00, camImgView_00, timer00);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initCamImgView(final int keyId, ImageView imgView, Timer timer) {
        imgView.setOnLongClickListener(new OnLongClickListener() {
            
            public boolean onLongClick(View v) {
                currentKey = keyId;
                CamListDialog dialog = new CamListDialog(NVRScnSettingActivity.this, keyId);
                dialog.setOnDismissListener(NVRScnSettingActivity.this);
                dialog.show();
                
                return true;
            }
        });
        
        String mac = getAppPref().getValue(getString(keyId));
        String url = "";

        try {
            final JSONObject camData = getCamViewData(mac);
            final String ipAddr = camData.getString(getString(R.string.key_cam_ip));
            url = "http://" + ipAddr + "/image.cgi";
        } catch (Exception e) {
            Log.e("", e.getMessage(), e);
        }

        timer.schedule(new ImgTimerTask(url, imgView), 0, 1000);
    }

    private void initCamData() {
        Set<String> camStrList = getAppPref().getValueSet(getString(R.string.key_camlist));
        camItemList = new ArrayList<JSONObject>();

        try {
            if (camStrList != null) {
                for (String dataStr : camStrList) {
                    JSONObject data = new JSONObject(dataStr);
                    camItemList.add(data);
                }
            }
        } catch (Exception e) {
            Log.e("Initialize cam data failed", e.getMessage(), e);
            camItemList.clear();
        }

        Log.v("data size:", camItemList.size() + "");
    }
    
    private class ImgTimerTask extends TimerTask {

        private String urlStr;
        private ImageView imgView;

        public ImgTimerTask(String url, ImageView imgView) {
            this.urlStr = url;
            this.imgView = imgView;
        }

        public void run() {
            if (!StringUtil.isEmptyString(urlStr)) {
                new DownloadImgTask(imgView).execute(new String[] { urlStr });
            }
        }

    }

    private class DownloadImgTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imgView;

        public DownloadImgTask(ImageView imgView) {
            this.imgView = imgView;
        }

        protected Bitmap doInBackground(String... params) {
            String urlStr = params[0];
            Bitmap bm = null;

            try {
                java.net.URL url = new java.net.URL(urlStr);
                url.openConnection().setReadTimeout(500);
                InputStream is = url.openStream();
                bm = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                Log.e("", e.getMessage(), e);
            }

            return bm;
        }

        protected void onPostExecute(Bitmap result) {
            imgView.setImageBitmap(result);
        }

    }

    public void onDismiss(DialogInterface dialog) {
        timer00.cancel();
        timer01.cancel();
        timer02.cancel();
        timer03.cancel();
        timer04.cancel();
        timer05.cancel();
        
        initCamData();

        initCamImgView00();
        initCamImgView01();
        initCamImgView02();
        initCamImgView03();
        initCamImgView04();
        initCamImgView05();
    }

}
