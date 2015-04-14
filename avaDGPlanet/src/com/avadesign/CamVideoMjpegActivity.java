package com.avadesign;

import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.avadesign.util.StringUtil;
import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;

public class CamVideoMjpegActivity extends Activity {
    private MjpegView camVideo;
    private GestureDetector gd;
    private String urlStr;
    private String acc = "admin";
    private String pwd = "admin";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camvideo);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        initCamVideo();

        gd = new GestureDetector(this, new DoubleClickListener());
        urlStr = getIntent().getStringExtra("url");
        acc = getIntent().getStringExtra("acc");
    }

    protected void onResume() {
        super.onResume();

        resumeVideo();
    }

    private void resumeVideo() {
        if (camVideo != null && !camVideo.isStreaming()) {
            if (!StringUtil.isEmptyString(urlStr) && !StringUtil.isEmptyString(acc) && !StringUtil.isEmptyString(pwd)) {
                new LoadMjpegTask().execute(new String[0]);
            }
        }
    }

    protected void onDestroy() {
        pauseVideo(camVideo);
        freeCamMemory(camVideo);
        super.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class DoubleClickListener extends GestureDetector.SimpleOnGestureListener {

        public boolean onDoubleTap(MotionEvent e) {
            finish();
            return true;
        }

    }

    private void initCamVideo() {
        camVideo = (MjpegView) findViewById(R.id.camVideo);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        camVideo.setResolution(size.x, size.y);
    }

    private class LoadMjpegTask extends AsyncTask<String, Void, MjpegInputStream> {

        private boolean isOK;

        protected MjpegInputStream doInBackground(String... params) {
            try {
                URL url = new URL(urlStr);
                HttpResponse res = null;
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpParams httpParams = httpclient.getParams();

                HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
                HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);

                httpclient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),
                        new UsernamePasswordCredentials(acc, pwd));

                HttpGet req = new HttpGet(urlStr);
                res = httpclient.execute(req);

                if (res.getStatusLine().getStatusCode() == 200) {
                    isOK = true;
                    return new MjpegInputStream(res.getEntity().getContent());
                } else {
                    isOK = false;
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                isOK = false;
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            if (isOK) {
                try {
                    camVideo.setSource(result);

                    if (result != null) {
                        result.setSkip(1);
                    }

                    camVideo.setDisplayMode(MjpegView.SIZE_BEST_FIT);
                    camVideo.showFps(false);
                } catch (Exception e) {
                    Log.e("Display error", e.getMessage(), e);

                    runOnUiThread(new Runnable() {

                        public void run() {
                            pauseVideo(camVideo);
                            freeCamMemory(camVideo);
                        }
                    });
                }
            } else {
                disconnect();
            }
        }

        private void disconnect() {
            runOnUiThread(new Runnable() {

                public void run() {
                    pauseVideo(camVideo);
                    freeCamMemory(camVideo);
                    camVideo.setBackgroundResource(R.drawable.disconnect);
                }
            });
        }

    }

    private void pauseVideo(MjpegView camView) {
        if (camView != null && camView.isStreaming()) {
            camView.stopPlayback();
        }
    }

    private void freeCamMemory(MjpegView camView) {
        if (camView != null) {
            camView.freeCameraMemory();
        }
    }
}
