package com.avadesign;

import org.linphone.LinphonePreferences;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MICGainSettingActivity extends Activity {
    
    private float currentMicGain;
    
    private TextView micGainLbl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_mic_gain_setting);
        micGainLbl = (TextView) findViewById(R.id.micGainLbl);
        
        updateValue();
    }

    private void updateValue() {
        currentMicGain = LinphonePreferences.instance().getMicGain();
        micGainLbl.setText(currentMicGain + "");
    }
    
    public void micGainUp(View v) {
        if (currentMicGain >= 10f) {
            return;
        }
        
        currentMicGain += 1;
        LinphonePreferences.instance().setMicGain(currentMicGain);
        updateValue();
    }
    
    public void micGainDown(View v) {
        if (currentMicGain <= 0f) {
            return;
        }
        
        currentMicGain -= 1;
        LinphonePreferences.instance().setMicGain(currentMicGain);
        updateValue();
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
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    
}
