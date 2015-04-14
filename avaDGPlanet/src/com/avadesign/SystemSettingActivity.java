package com.avadesign;

import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;

import com.avadesign.comp.DeviceListDialog;
import com.avadesign.util.AvaPref;
import com.avadesign.util.DeviceSearcher;
import com.avadesign.util.StringUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SystemSettingActivity extends SearchDeviceActivity {

    private Dialog errDialog;
    private EditText ipField;
    private EditText portField;
    private EditText accField;
    private EditText pwdField;
    private AvaPref avaPref;
    private Button searchBtn;
    
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
    
    public void displaySearchResult(final Map<String, Map<String, String>> deviceDataMap) {
        /*
         * 不確定會被誰呼叫, 使用 runOnUiThread() 運行 UI 層的程式較保險.
         * */
        runOnUiThread(new Runnable() {
            
            public void run() {
                DeviceListDialog dialog = new DeviceListDialog(SystemSettingActivity.this, deviceDataMap);
                dialog.show();
            }
        });
    }

    public void updateIPAddrField(Map<String, String> data) {
        // 有可能是針對原有的進行編輯, 設完 IP address 後得把其它欄位的值清掉.
        ipField.setText(data.get("ip"));
        portField.setText("");
        accField.setText("");
        pwdField.setText("");
    }

    private boolean isIPAddrFormat(EditText field) {
        return InetAddressUtils.isIPv4Address(field.getText().toString());
    }

    private boolean isEmptyString(EditText field) {
        return StringUtil.isEmptyString(field.getText().toString());
    }

    private void showErrDialog(int msgResId) {
        errDialog = new Dialog(this);
        errDialog.setContentView(R.layout.err_msg_dialog);
        errDialog.setTitle("Error");
        TextView msgView = (TextView) errDialog.findViewById(R.id.errMsg);
        msgView.setText(getString(msgResId));

        Button okBtn = (Button) errDialog.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                errDialog.dismiss();
            }
        });

        errDialog.show();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);
        avaPref = ((SharedClassApp) getApplication()).getAppPref();

        if (getActionBar() != null) {
            getActionBar().setTitle("System settings");
        }

        initIpField();
        initPortField();
        initAccField();
        initPwdField();
        initSearchBtn();
    }

    private void initSearchBtn() {
        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                DeviceSearcher searcher = new DeviceSearcher(SystemSettingActivity.this);
                searcher.findDevice(DeviceSearcher.CMD_CONTROLLER);
            }
        });
    }

    private void initPwdField() {
        pwdField = (EditText) findViewById(R.id.pwdField);
        pwdField.setText(avaPref.getValue(getString(R.string.key_pwd)));
    }

    private void initAccField() {
        accField = (EditText) findViewById(R.id.accField);
        accField.setText(avaPref.getValue(getString(R.string.key_acc)));
    }

    private void initPortField() {
        portField = (EditText) findViewById(R.id.portField);
        portField.setText(avaPref.getValue(getString(R.string.key_gateway_port)));
    }

    private void initIpField() {
        ipField = (EditText) findViewById(R.id.ipField);
        ipField.setText(avaPref.getValue(getString(R.string.key_gateway_ip)));
    }

    public void cancelEdit(View v) {
        goToMainActivity();
    }

    public void saveEdit(View v) {
        if (validateFields()) {
            String gatewayIPAddr = ipField.getText().toString();
            String gatewayAcc = accField.getText().toString();
            String gatewayPwd = pwdField.getText().toString();
            String gatewayPort = portField.getText().toString();

            avaPref.setValue(getString(R.string.key_gateway_ip), gatewayIPAddr);
            avaPref.setValue(getString(R.string.key_gateway_port), gatewayPort);
            avaPref.setValue(getString(R.string.key_acc), gatewayAcc);
            avaPref.setValue(getString(R.string.key_pwd), gatewayPwd);

            goToMainActivity();
        }
    }

    private boolean validateFields() {
        if (isEmptyString(ipField)) {
            showErrDialog(R.string.empty_ip);
            return false;
        } else if (!isIPAddrFormat(ipField)) {
            showErrDialog(R.string.incorrect_ip_format);
            return false;
        }

        if (isEmptyString(portField)) {
            showErrDialog(R.string.empty_port);
            return false;
        } else if (!isDigitalNumber(portField)) {
            showErrDialog(R.string.incorrect_port_format);
            return false;
        }

        if (isEmptyString(accField)) {
            showErrDialog(R.string.empty_acc);
            return false;
        }

        if (isEmptyString(pwdField)) {
            showErrDialog(R.string.empty_pwd);
            return false;
        }

        return true;
    }

    private boolean isDigitalNumber(EditText field) {
        String value = field.getText().toString();

        try {
            return Integer.parseInt(value) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainScreenActivity.class));
    }

}
