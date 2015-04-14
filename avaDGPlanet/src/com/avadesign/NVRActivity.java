package com.avadesign;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;

public class NVRActivity extends Activity {

    /**
     * Screen size: 420 x 315 (L), 328 x 248 (S)
     * */
    private ImageView camImgView_00, camImgView_01, camImgView_02, camImgView_03, camImgView_04, camImgView_05;

    private List<JSONObject> camItemList;

    private AvaPref appPref;

    private Timer timer00;

    private Timer timer01;

    private Timer timer02;

    private Timer timer03;

    private Timer timer04;

    private Timer timer05;

    private String urlStr;

    private GestureDetector gd;

    private int currentKeyId;

    private Map<Integer, Integer> errTimerMap;

    private class DoubleClickListener extends GestureDetector.SimpleOnGestureListener {

        public boolean onDoubleTap(MotionEvent e) {
            if (errTimerMap.containsKey(currentKeyId)) {
                Toast.makeText(NVRActivity.this, R.string.invalid_camera, Toast.LENGTH_SHORT).show();
            } else {
                Intent it = new Intent(NVRActivity.this, CamVideoActivity.class);
                it.putExtra("url", urlStr);
                startActivity(it);
            }

            return true;
        }

    }

    private class ImgTimerTask extends TimerTask {

        private String urlStr;
        private ImageView imgView;
        private Timer parent;
        private int keyId;

        public ImgTimerTask(String url, ImageView imgView, Timer parent, int keyId) {
            this.urlStr = url;
            this.imgView = imgView;
            this.parent = parent;
            this.keyId = keyId;
        }

        public void run() {
            if (!StringUtil.isEmptyString(urlStr)) {
                AsyncTask<String, Void, Bitmap> dit = new DownloadImgTask(imgView).execute(new String[] { urlStr });

                try {
                    if (dit.get() == null) {
                        parent.cancel();
                        errTimerMap.put(keyId, keyId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    parent.cancel();
                    errTimerMap.put(keyId, keyId);
                }
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
            if (result != null) {
                imgView.setImageBitmap(result);
            } else {
                Bitmap errIcon = BitmapFactory.decodeResource(getResources(), R.drawable.disconnect);
                imgView.setImageBitmap(errIcon);
            }
        }

    }

    public void exitActivity(View v) {
        finish();
    }

    /**
     * Activity 結束前將所有視訊串流都清除掉.
     * */
    protected void onDestroy() {
        timer00.cancel();
        timer01.cancel();
        timer02.cancel();
        timer03.cancel();
        timer04.cancel();
        timer05.cancel();

        errTimerMap.clear();

        super.onDestroy();
    }

    private AvaPref getAppPref() {
        if (appPref == null) {
            appPref = ((SharedClassApp) getApplication()).getAppPref();
        }

        return appPref;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nvr);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        gd = new GestureDetector(this, new DoubleClickListener());

        errTimerMap = new HashMap<Integer, Integer>();

        // Just for test, please remove it after developing.
//        initTestData();

        initCamData();
    }

    protected void onResume() {
        super.onResume();

        initCamImgView00();
        initCamImgView01();
        initCamImgView02();
        initCamImgView03();
        initCamImgView04();
        initCamImgView05();
    }

    protected void onPause() {
        timer00.cancel();
        timer01.cancel();
        timer02.cancel();
        timer03.cancel();
        timer04.cancel();
        timer05.cancel();

        super.onPause();
    }

    private void initTestData() {
        getAppPref().setValue(getString(R.string.key_cam_00), "00134BE14008");
        getAppPref().setValue(getString(R.string.key_cam_01), "0002AC5588A8");
        getAppPref().setValue(getString(R.string.key_cam_02), "00134BE14008");
        getAppPref().setValue(getString(R.string.key_cam_03), "0002AC5588A8");
        getAppPref().setValue(getString(R.string.key_cam_04), "0002AC5588A8");
        getAppPref().setValue(getString(R.string.key_cam_05), "0002AC5588A8");
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
        String mac = getAppPref().getValue(getString(keyId));
        String url = "";

        try {
            /*
             * 2015-04-10, edited by Phoenix.
             * User 使用的監視器 URL 格式不一, 因此儲存的設定值從 IP address 改成 URL.
             * */
            final JSONObject camData = getCamViewData(mac);
            final String ipAddr = camData.getString(getString(R.string.key_cam_ip));
            url = ipAddr;

            imgView.setOnTouchListener(new OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    try {
//                        urlStr = "http://" + ipAddr + "/image.cgi";
                        urlStr = ipAddr;
                        currentKeyId = keyId;
                        gd.onTouchEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            });
        } catch (Exception e) {
            Log.e("", e.getMessage(), e);
        }

        if (errTimerMap.containsKey(keyId)) {
            return;
        } else {
            timer.schedule(new ImgTimerTask(url, imgView, timer, keyId), 0, 1000);
        }
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

}
