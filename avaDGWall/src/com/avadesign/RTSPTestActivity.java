package com.avadesign;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.MediaController;
import android.widget.VideoView;

public class RTSPTestActivity extends Activity {

//    private VideoView testVideo;
    private SurfaceView testSurface;
    private MediaPlayer player;
    private SurfaceHolder surHolder;

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
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rtsp_test);

//        testVideo = (VideoView) findViewById(R.id.testVideo);
//
//        try {
//            testVideo.setMediaController(new MediaController(this));
//            testVideo.setVideoURI(Uri.parse("rtsp://192.168.1.122/cam1/mjpeg"));
//            testVideo.requestFocus();
//            testVideo.start();
//        } catch (Exception e) {
//            Log.e("", e.getMessage(), e);
//        }
    }

    protected void onStart() {
        super.onStart();

        testSurface = (SurfaceView) findViewById(R.id.testSurface);
        initPlayer();
        initSurHolder();

        try {
            play();
        } catch (Exception e) {
            Log.e("", e.getMessage(), e);
        }
    }

    private void play() throws Exception {
        if (!player.isPlaying()) {
            player.setDataSource("rtsp://192.168.1.122/cam1/mjpeg");
            player.start();
        }
    }

    private void initSurHolder() {
        surHolder = testSurface.getHolder();
        surHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surHolder.addCallback(new Callback() {

            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            public void surfaceCreated(SurfaceHolder holder) {
               player.reset();
               player.setDisplay(surHolder);
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });
    }

    private void initPlayer() {
        player = new MediaPlayer();
        player.setOnErrorListener(new OnErrorListener() {

            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("RTSP ERROR CODE", what + "");
                return false;
            }
        });

        player.setOnCompletionListener(new OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                try {
                    player.seekTo(0);
                } catch (Exception e) {
                    Log.e("", e.getMessage(), e);
                }
            }
        });
    }

}
