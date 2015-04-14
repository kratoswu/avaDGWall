package com.avadesign;

import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.avadesign.util.StringUtil;

public class CamVideoActivity extends Activity {
    
    private ImageView camVideo;
    private GestureDetector gd;
    private String urlStr;
    private Timer timer;
    private boolean isRefreshing;
    
    private class ImgTimerTask extends TimerTask {
        private String urlStr;
        private ImageView imgView;
        private Timer parent;
        
        public ImgTimerTask(String url, ImageView imgView, Timer parent) {
            this.urlStr = url;
            this.imgView = imgView;
            this.parent = parent;
        }
        
        public void run() {
            if (!StringUtil.isEmptyString(urlStr) && !isRefreshing) {
//                Log.e("", "load img...");
                AsyncTask<String, Void, Bitmap> dit = new DownloadImgTask(imgView).execute(new String[] { urlStr });
                
                try {
                    if (dit.get() == null) {
                        parent.cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    parent.cancel();
                }
            }
        }
    }
    
    private class DownloadImgTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        
        public DownloadImgTask(ImageView imgView) {
            this.imgView = imgView;
        }
        
        public Bitmap doInBackground(String... params) {
            isRefreshing = true;
            String urlStr = params[0];
            Bitmap bm = null;
            
            try {
                URL url = new URL(urlStr);
                url.openConnection().setReadTimeout(500);
                InputStream is = url.openStream();
                bm = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return bm;
        }
        
        public void onPostExecute(Bitmap result) {
            if (result != null) {
                imgView.setImageBitmap(result);
            } else {
                Bitmap errIcon = BitmapFactory.decodeResource(getResources(), R.drawable.disconnect);
                imgView.setImageBitmap(errIcon);
            }
            
            isRefreshing = false;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camvideo);
        
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        
        camVideo = (ImageView) findViewById(R.id.camVideo);

        gd = new GestureDetector(this, new DoubleClickListener());
        urlStr = getIntent().getStringExtra("url");
    }
    
    protected void onResume() {
        super.onResume();
        
        resumeVideo();
    }

    private void resumeVideo() {
        if (camVideo != null) {
            timer = new Timer();
            timer.schedule(new ImgTimerTask(urlStr, camVideo, timer), 0, 10);
        }
    }
    
    protected void onDestroy() {
        pauseVideo();
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
    
    private void pauseVideo() {
        if (timer != null) {
            timer.cancel();
        }
    }

}
