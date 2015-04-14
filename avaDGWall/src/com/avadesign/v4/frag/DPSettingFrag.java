package com.avadesign.v4.frag;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avadesign.DPSettingActivity;
import com.avadesign.R;
import com.avadesign.util.StringUtil;

public class DPSettingFrag extends AbstractFrag {
    private JSONObject data;
    private Button rmBtn;
    private Button saveBtn, cancelBtn;
    private EditText labelField, ipField, accField, pwdField, sipIdField;
    private int sequence;
    private Dialog errDialog;
    private Button searchBtn;
    
    public String getLabel() {
        if (data == null) {
            return "new dp";
        } else {
            String label = "Non";
            
            try {
                label = data.getString(getString(R.string.key_dp_label));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return label;
        }
    }
    
    private boolean isIPAddrFormat(EditText field) {
        return InetAddressUtils.isIPv4Address(field.getText().toString());
    }

    private void showErrDialog(int msgResId) {
        errDialog = new Dialog(getActivity());
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

    private boolean isEmptyString(EditText field) {
        return StringUtil.isEmptyString(field.getText().toString());
    }

    private void saveData() {
        try {
            JSONObject data = new JSONObject();
            data.put(getString(R.string.key_dp_label), labelField.getText().toString());
            data.put(getString(R.string.key_dp_ip), ipField.getText().toString());
            data.put(getString(R.string.key_dp_acc), accField.getText().toString());
            data.put(getString(R.string.key_dp_pwd), pwdField.getText().toString());
            data.put(getString(R.string.key_dp_sip), sipIdField.getText().toString());

            this.data = data;
            ((DPSettingActivity)getActivity()).saveDpData(getSequence(), data);
        } catch (Exception e) {
            showErrDialog(R.string.edit_err);
        }
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

        if (isEmptyString(sipIdField)) {
            showErrDialog(R.string.empty_sip);
            return false;
        }

        return true;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public int getLayoutResId() {
        return R.layout.frag_dp_setting;
    }

    public void initView(View rootView) {
        initRmBtn(rootView);
        initSaveBtn(rootView);
        initCancelBtn(rootView);

        initFields(rootView);
        
        initSearchBtn(rootView);
    }

    private void initSearchBtn(View rootView) {
        searchBtn = (Button) rootView.findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                ((DPSettingActivity) getActivity()).scanCam();
            }
        });
    }

    private void initFields(View rootView) {
        labelField = (EditText) rootView.findViewById(R.id.labelField);
        ipField = (EditText) rootView.findViewById(R.id.ipField);
        accField = (EditText) rootView.findViewById(R.id.accField);
        pwdField = (EditText) rootView.findViewById(R.id.pwdField);
        sipIdField = (EditText) rootView.findViewById(R.id.macField);

        displayData();
    }

    private void displayData() {
        if (data != null) {
            try {
                labelField.setText(data.getString(getString(R.string.key_dp_label)));
                ipField.setText(data.getString(getString(R.string.key_dp_ip)));
                accField.setText(data.getString(getString(R.string.key_dp_acc)));
                pwdField.setText(data.getString(getString(R.string.key_dp_pwd)));
                sipIdField.setText(data.getString(getString(R.string.key_dp_sip)));
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        } else {
            labelField.setText("");
            ipField.setText("");
            accField.setText("");
            pwdField.setText("");
            sipIdField.setText("");
        }
    }

    private void initCancelBtn(View rootView) {
        cancelBtn = (Button) rootView.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (data != null) {
                    displayData();
                } else {
                    rmDP();
                }
            }
        });
    }

    private void initSaveBtn(View rootView) {
        saveBtn = (Button) rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (validateFields()) {
                    saveData();
                }
            }
        });
    }

    private void initRmBtn(View rootView) {
        rmBtn = (Button) rootView.findViewById(R.id.rmBtn);
        rmBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());
                confirmBuilder.setMessage(R.string.rm_dp_confirm);
                confirmBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        rmDP();
                        dialog.cancel();
                    }
                });
                
                confirmBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                
                AlertDialog confirm = confirmBuilder.create();
                confirm.show();
            }
        });
    }

    public void rmDP() {
        DPSettingActivity parent = (DPSettingActivity) getActivity();
        parent.rmDPFrag();
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void updateIPAddr(String ipaddr) {
        ipField.setText(ipaddr);
        accField.setText("");
        pwdField.setText("");
        sipIdField.setText("");
    }

}
