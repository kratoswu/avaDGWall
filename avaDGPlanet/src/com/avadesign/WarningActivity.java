package com.avadesign;

import com.avadesign.util.StringUtil;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class WarningActivity extends Activity {

    private MediaPlayer mPlayer;
    private TextView warnMsgLbl;

    private void startRing() {
        Log.e("Start Ring", "");
        mPlayer = MediaPlayer.create(this, R.raw.planet);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnCompletionListener(new OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                mPlayer.start();
            }
        });

        mPlayer.setLooping(false);
        mPlayer.start();
    }

    private void stopRing() {
        Log.e("Stop Ring", "");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        String msg = getIntent().getStringExtra("errMsg");

        if (!StringUtil.isEmptyString(msg)) {
            warnMsgLbl = (TextView) findViewById(R.id.warnMsgLbl);
            warnMsgLbl.setText(msg);
        }

        startRing();
    }

    public void exitActivity(View v) {
        stopRing();
        finish();
    }

}
