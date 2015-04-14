package com.avadesign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.avadesign.util.AvaPref;

/**
 * 設定主畫面功能 button 顯示的 activity.
 * 
 * @author Phoenix Wu
 * 
 * @version
 * <pre>
 * 2015-03-23 First version.
 * </pre>
 * */
public class MainScnBtnSettingActivity extends Activity {
    
    private CheckBox janitorCheck;
    
    private CheckBox intercomCheck;
    
    private CheckBox cameraCheck;
    
    private CheckBox contactCheck;
    
    private CheckBox ctrlCheck;
    
    private AvaPref appPref;
    
    private void setCheck(int resId, boolean isChecked) {
        appPref.setValue(getString(resId), isChecked + "");
    }
    
    private boolean isCheck(int resId) {
        return appPref.getBooleanVal(getString(resId));
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_scn_setting);
        
        appPref = ((SharedClassApp) getApplication()).getAppPref();
        
        initJanitorCheck();
        initIntercomCheck();
        initCamCheck();
        initContactCheck();
        initCtrlCheck();
    }

    private void initCtrlCheck() {
        ctrlCheck = (CheckBox) findViewById(R.id.ctrlCheck);
        ctrlCheck.setChecked(isCheck(R.string.key_func_ctrl));
        ctrlCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(R.string.key_func_ctrl, isChecked);                
            }
        });
    }

    private void initContactCheck() {
        contactCheck = (CheckBox) findViewById(R.id.contactCheck);
        contactCheck.setChecked(isCheck(R.string.key_func_call));
        contactCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(R.string.key_func_call, isChecked);
            }
        });
    }

    private void initCamCheck() {
        cameraCheck = (CheckBox) findViewById(R.id.cameraCheck);
        cameraCheck.setChecked(isCheck(R.string.key_func_nvr));
        cameraCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(R.string.key_func_nvr, isChecked);
            }
        });
    }

    private void initIntercomCheck() {
        intercomCheck = (CheckBox) findViewById(R.id.intercomCheck);
        intercomCheck.setChecked(isCheck(R.string.key_func_door));
        intercomCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(R.string.key_func_door, isChecked);
            }
        });
    }

    private void initJanitorCheck() {
        janitorCheck = (CheckBox) findViewById(R.id.janitorCheck);
        janitorCheck.setChecked(isCheck(R.string.key_func_janitor));
        janitorCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheck(R.string.key_func_janitor, isChecked);
            }
        });
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
