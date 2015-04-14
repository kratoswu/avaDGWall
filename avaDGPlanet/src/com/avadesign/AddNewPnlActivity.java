package com.avadesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.model.bean.PanelTempItem;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;
import com.avadesign.util.StringUtil;

public class AddNewPnlActivity extends Activity {

    private ListView pnlTemplateList;
    private ImageView previewImg;
    private TextView descriptionTxt;
    private List<PanelTempItem> pnlItems;
    private LayoutInflater inflater;
    private PanelItemListAdapter listAdapter;
    private int selectedTypeId;
    private LinearLayout funBtnsLayout;
    private Dialog pnlNameInputDialog;
    private ProgressDialog waitPop;
    private AvaPref appPref;
    private String acc;
    private String pwd;

    private class AddPanelTask extends AsyncTask<Void, Void, Void> {
        private int typeId;
        private String label;

        public AddPanelTask(String label, int typeId) {
            this.typeId = typeId;
            this.label = label;
        }

        protected Void doInBackground(Void... params) {
            if (selectedTypeId > 0) {
                // Show waiting popup.
                runOnUiThread(new Runnable() {

                    public void run() {
                       waitPop = new ProgressDialog(AddNewPnlActivity.this);
                       waitPop.setTitle("Adding panel...");
                       waitPop.show();
                    }
                });

                // Add panel
                String urlStr = String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip),
                        getAppPrefVal(R.string.key_gateway_port)) + getString(R.string.panel_list);

                Map<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("action", "create");
                paramMap.put("label", label);
                paramMap.put("type_id", typeId + "");

                try {
                    HttpCommunicator.sendCmd(urlStr, paramMap, acc, pwd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            runOnUiThread(new Runnable() {

                public void run() {
                    if (waitPop != null) {
                        waitPop.dismiss();
                    }

//                    startActivity(new Intent(AddNewPnlActivity.this, PanelSettingMainActivity.class));
                    SettingMainScreenActivity.instance.goToCtrlPnlSetting(null);
                    finish();
                }
            });
        }

    }

    private String getAppPrefVal(int resId) {
        return appPref.getValue(getString(resId));
    }

    private void showMainArea() {
        setMainAreaVisible(View.VISIBLE);
    }

    private void hideMainArea() {
        setMainAreaVisible(View.INVISIBLE);
    }

    private void setMainAreaVisible(int visibility) {
        previewImg.setVisibility(visibility);
        descriptionTxt.setVisibility(visibility);
        funBtnsLayout.setVisibility(visibility);
    }

    private List<PanelTempItem> getData() {
        List<PanelTempItem> data = new ArrayList<PanelTempItem>();

        PanelTempItem item0 = new PanelTempItem();
        item0.label = "Panel 01";
        item0.description = "4 個開關, 4 個情境按鈕";
        item0.imgRes = R.drawable.scr_pnl_1;
        item0.typeId = 1;
        data.add(item0);

        return data;
    }

    private class PanelItemListAdapter extends ArrayAdapter<PanelTempItem> {

        private int selectedIdx = -1;

        public PanelItemListAdapter(Context context, int resource, List<PanelTempItem> objects) {
            super(context, resource, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.pnl_template_row, null);
            }

            TextView lbl = (TextView) convertView.findViewById(R.id.pnlItemLbl);
            lbl.setText(pnlItems.get(position).label);

            int bgRes = position == selectedIdx ? R.drawable.cmpt_item_1 : R.drawable.cmpt_item_0;
            convertView.setBackgroundResource(bgRes);

            return convertView;
        }

        public void setSelectedIdx(int selectedIdx) {
            this.selectedIdx = selectedIdx;
            this.notifyDataSetInvalidated();
        }

    }

    private SharedClassApp getAvaApp() {
        return (SharedClassApp) getApplication();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pnl);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pnlItems = getData();

        appPref = getAvaApp().getAppPref();
        acc = appPref.getValue(getString(R.string.key_acc));
        pwd = appPref.getValue(getString(R.string.key_pwd));

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        funBtnsLayout = (LinearLayout) findViewById(R.id.funBtnsLayout);
        initBtnList();
        previewImg = (ImageView) findViewById(R.id.previewImg);
        descriptionTxt = (TextView) findViewById(R.id.descriptionTxt);

        /*
         * 2014-08-22, edited by Phoenix
         * 一定要加下面這行, 不然會無法 scroll
         * */
        descriptionTxt.setMovementMethod(new ScrollingMovementMethod());

        hideMainArea();
    }

    private void initBtnList() {
        pnlTemplateList = (ListView) findViewById(R.id.pnlTemplateList);
        listAdapter = new PanelItemListAdapter(this, R.layout.pnl_template_row, pnlItems);
        pnlTemplateList.setAdapter(listAdapter);
        pnlTemplateList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listAdapter.setSelectedIdx(position);

                PanelTempItem item = pnlItems.get(position);
                previewImg.setImageResource(item.imgRes);
                descriptionTxt.setText(item.description);
                selectedTypeId = item.typeId;

                showMainArea();
            }
        });
    }

    public void cancelAdd(View view) {
        finish();
    }

    public void addPnl(View view) {
        openNameInputDialog();
    }

    private void openNameInputDialog() {
        pnlNameInputDialog = new Dialog(this);
        pnlNameInputDialog.setContentView(R.layout.pnl_name_dialog);
        pnlNameInputDialog.setTitle("Input Panel Name");
        final EditText nameField = (EditText) pnlNameInputDialog.findViewById(R.id.pnlNameField);

        Button cancelBtn = (Button) pnlNameInputDialog.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                pnlNameInputDialog.dismiss();
            }
        });

        Button okBtn = (Button) pnlNameInputDialog.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String label = nameField.getText().toString();

                if (!StringUtil.isEmptyString(label)) {
                    // Run AsyncTask of adding new panel.
                    AddPanelTask task = new AddPanelTask(label, selectedTypeId);
                    task.execute(new Void[0]);
                    pnlNameInputDialog.dismiss();
                }
            }
        });

        pnlNameInputDialog.show();
    }

}
