package com.avadesign;

import com.avadesign.util.StringUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class RTSPVideoSettingActivity extends Activity {

    private EditText urlField;

    public void doSave(View v) {
        if (!validateURL()) {
            // TODO show dialog
        } else {
            // TODO save
        }
    }

    public void doCancel(View v) {
        backSettingMain();
    }

    private boolean validateURL() {
        String url = urlField.getText().toString();

        return !StringUtil.isEmptyString(url);
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
            backSettingMain();
        }

        return super.onOptionsItemSelected(item);
    }

    private void backSettingMain() {
        startActivity(new Intent(this, SettingMainScreenActivity.class));
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp_video_setting);

        /*
         * rtsp://admin:admin@192.168.3.33:80/live/h264/VGA
         * rtsp://192.168.1.122/cam1/h264
         * */
        urlField = (EditText) findViewById(R.id.urlField);
        urlField.setText("rtsp://192.168.1.122/cam1/h264");
    }

}
