package com.avadesign;

import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class SecurityCallSettingActivity extends Activity {
    
    private EditText sipField;
    private AvaPref pref;
    
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
        setContentView(R.layout.activity_security_call_setting);
        pref = ((SharedClassApp) getApplication()).getAppPref();
        
        sipField = (EditText) findViewById(R.id.sipField);
        String value = pref.getValue(getString(R.string.key_security_sip));
        
        if (!StringUtil.isEmptyString(value)) {
            sipField.setText(value);
        }
    }
    
    public void cancel(View v) {
        finish();
    }
    
    public void save(View v) {
        String sipId = sipField.getText().toString();
        
        if (!StringUtil.isEmptyString(sipId)) {
            pref.setValue(getString(R.string.key_security_sip), sipId);
            finish();
        } else {
            AlertDialog.Builder builder = new Builder(this);
            builder.setMessage(R.string.empty_sip);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            
            builder.create().show();
        }
    }

}
