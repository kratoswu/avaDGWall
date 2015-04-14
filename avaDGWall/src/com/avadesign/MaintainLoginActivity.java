package com.avadesign;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.avadesign.util.StringUtil;

public class MaintainLoginActivity extends Activity {

    private EditText userNameField;

    private EditText pwdField;
    
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
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }

        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintain_login);
        
        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.maintain_title);
        }

        userNameField = (EditText) findViewById(R.id.userNameField);
        pwdField = (EditText) findViewById(R.id.pwdField);
    }
    
    public void login(View view) {
        String userName = userNameField.getText().toString();
        String pwd = pwdField.getText().toString();
        
        // TODO
        startActivity(new Intent(this, SettingMainScreenActivity.class));
        finish();
        
//        if (StringUtil.isEmptyString(userName) || StringUtil.isEmptyString(pwd)) {
//            showErrDialog();
//        } else if (!checkUserName(userName) || !checkPwd(pwd)) {
//            showErrDialog();
//        } else {
//            startActivity(new Intent(this, SettingMainScreenActivity.class));
//            finish();
//        }
    }
    
    private boolean checkPwd(String pwd) {
        return pwd.equals(getString(R.string.maintain_pwd));
    }

    private boolean checkUserName(String userName) {
        return userName.equals(getString(R.string.maintain_user));
    }

    private void showErrDialog() {
        AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
        dBuilder.setMessage(R.string.login_err);
        dBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                userNameField.setText("");
                pwdField.setText("");
            }
        });
        
        AlertDialog d = dBuilder.create();
        d.show();
    }

}