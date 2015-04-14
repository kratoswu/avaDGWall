package com.avadesign;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class CamQuarterScnActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_quarter_scn);
        
        if (getActionBar() != null) {
            getActionBar().hide();
        }
    }
    
    public void exitActivity(View v) {
        finish();
    }

}
