package com.avadesign;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avadesign.comp.DeviceListDialog;
import com.avadesign.util.AvaPref;
import com.avadesign.util.DeviceSearcher;
import com.avadesign.util.StringUtil;

public class EditCamActivity extends SearchDeviceActivity {
    private EditText labelField, ipField, accField, pwdField, macField;
    private Dialog errDialog;
    private JSONObject oldCamData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_cam);
        labelField = (EditText) findViewById(R.id.labelField);
        ipField = (EditText) findViewById(R.id.ipField);
        accField = (EditText) findViewById(R.id.accField);
        pwdField = (EditText) findViewById(R.id.pwdField);
        macField = (EditText) findViewById(R.id.macField);

        String oldData = getIntent().getStringExtra("oldCamData");

        if (!StringUtil.isEmptyString(oldData)) {
            try {
                oldCamData = new JSONObject(oldData);
                labelField.setText(oldCamData.getString(getString(R.string.key_cam_label)));
                ipField.setText(oldCamData.getString(getString(R.string.key_cam_ip)));
                accField.setText(oldCamData.getString(getString(R.string.key_cam_acc)));
                pwdField.setText(oldCamData.getString(getString(R.string.key_cam_pwd)));
                macField.setText(oldCamData.getString(getString(R.string.key_cam_mac)));
            } catch (Exception e) {
                Log.e("Data parsing error", e.getMessage(), e);
                showErrDialog(R.string.data_loading_err);
                finish();
                return;
            }
        }
    }

    public void doSave(View v) {
        if (validateFields()) {
            // Save data.
            try {
                JSONObject data = new JSONObject();
                data.put(getString(R.string.key_cam_label), labelField.getText().toString());
                data.put(getString(R.string.key_cam_ip), ipField.getText().toString());
                data.put(getString(R.string.key_cam_acc), accField.getText().toString());
                data.put(getString(R.string.key_cam_pwd), pwdField.getText().toString());
                data.put(getString(R.string.key_cam_mac), macField.getText().toString());

                if (oldCamData == null) {
                    saveData(data);
                } else {
                    updateData(data);
                }
            } catch (Exception e) {
                showErrDialog(R.string.edit_err);
                Log.e("ERROR", e.getMessage(), e);
            } finally {
                finish();
            }
        }
    }

    private void updateData(JSONObject data) throws Exception {
        AvaPref appPref = getAppPref();
        Set<String> camDataStrSet = appPref.getValueSet(getString(R.string.key_camlist));
        camDataStrSet = camDataStrSet == null ? new LinkedHashSet<String>() : camDataStrSet;
        Set<String> newDataSet = new LinkedHashSet<String>();
        String oldDataStr = oldCamData.toString();

        for (String s : camDataStrSet) {
            if (s.equals(oldDataStr)) {
                newDataSet.add(data.toString());
            } else {
                newDataSet.add(s);
            }
        }

        appPref.setValueSet(getString(R.string.key_camlist), newDataSet);

        String key_cam_mac = getString(R.string.key_cam_mac);
        updateNVRScnData(R.string.key_cam_00, oldCamData.getString(key_cam_mac), data.getString(key_cam_mac));
        updateNVRScnData(R.string.key_cam_01, oldCamData.getString(key_cam_mac), data.getString(key_cam_mac));
        updateNVRScnData(R.string.key_cam_02, oldCamData.getString(key_cam_mac), data.getString(key_cam_mac));
        updateNVRScnData(R.string.key_cam_03, oldCamData.getString(key_cam_mac), data.getString(key_cam_mac));
        updateNVRScnData(R.string.key_cam_04, oldCamData.getString(key_cam_mac), data.getString(key_cam_mac));
        updateNVRScnData(R.string.key_cam_05, oldCamData.getString(key_cam_mac), data.getString(key_cam_mac));
    }

    private void updateNVRScnData(int key, String oldMacVal, String newMacVal) {
        AvaPref appPref = getAppPref();

        if (appPref.getValue(getString(key)).equals(oldMacVal)) {
            appPref.setValue(getString(key), newMacVal);
        }
    }

    private void saveData(JSONObject data) throws Exception {
        AvaPref appPref = getAppPref();
        Set<String> camDataStrSet = appPref.getValueSet(getString(R.string.key_camlist));
        ArrayList<String> values = camDataStrSet == null ? new ArrayList<String>() : new ArrayList<String>(camDataStrSet);
        values.add(data.toString());

        camDataStrSet = new LinkedHashSet<String>();

        for (String value : values) {
            camDataStrSet.add(value);
        }

        appPref.setValueSet(getString(R.string.key_camlist), camDataStrSet);
    }

    private AvaPref getAppPref() {
        return ((SharedClassApp) getApplication()).getAppPref();
    }

    public void searchCam(View v) {
        DeviceSearcher searcher = new DeviceSearcher(this);
        searcher.findDevice(DeviceSearcher.CMD_CAM);
    }

    public void doCancel(View v) {
        finish();
    }

    private boolean isEmptyString(EditText field) {
        return StringUtil.isEmptyString(field.getText().toString());
    }

    private boolean isIPAddrFormat(EditText field) {
        /*
         * 2015-04-10, edited by Phoenix.
         * 改成輸入 URL, 因為監視器不一定是公司的.
         * */
//        return InetAddressUtils.isIPv4Address(field.getText().toString());
        return Patterns.WEB_URL.matcher(field.getText().toString()).matches();
    }

    private boolean validateFields() {
        if (isEmptyString(labelField)) {
            showErrDialog(R.string.empty_label);
            return false;
        }

        if (isEmptyString(ipField)) {
            showErrDialog(R.string.empty_ip);
            return false;
        } else if (!isIPAddrFormat(ipField)) {
            showErrDialog(R.string.incorrect_ip_format);
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

        if (isEmptyString(macField)) {
            showErrDialog(R.string.empty_sip);
            return false;
        }

        return true;
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

    public void displaySearchResult(final Map<String, Map<String, String>> deviceDataMap) {
        runOnUiThread(new Runnable() {

            public void run() {
                DeviceListDialog dialog = new DeviceListDialog(EditCamActivity.this, deviceDataMap);
                dialog.show();
            }
        });
    }

    public void updateIPAddrField(Map<String, String> data) {
        /*
         * 2015-04-10, edited by Phoenix.
         * 改成 URL. 因為 user 可能用的是自己的 camera. ipField 欄位改成輸入 URL.
         * */
        ipField.setText("http://" + data.get("ip") + "/image.cgi");
        macField.setText(data.get("mac"));
    }

}
