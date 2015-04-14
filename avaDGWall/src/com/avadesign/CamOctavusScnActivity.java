package com.avadesign;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class CamOctavusScnActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_octavus);
    }
    
    public void exitActivity(View v) {
        finish();
    }

}
