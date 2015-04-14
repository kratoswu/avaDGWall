package com.avadesign.comp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import com.avadesign.R;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.PanelSettingBean;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.v4.PanelSettingMainActivity_New;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class SceneSettingDialog extends Dialog {

    private static final String NONE = "None";
    private static final String EMPTY_VALUE = "Empty Value";
    private Map<String, SceneBean> sceneMap;
    private Map<String, String> currentItem;
    private Button okBtn;
    private Button cancelBtn;
    private LayoutInflater inflater;
    private Spinner sceneCombo;
    private Context myContext;
    private PanelDetailBean panelInfo;
    private List<Map<String, String>> tempData;

    public SceneSettingDialog(Context context, Map<String, SceneBean> sceneMap, Map<String, String> currentItem, PanelDetailBean panelInfo) {
        super(context);
        myContext = context;
        this.sceneMap = sceneMap;
        this.currentItem = currentItem;
        this.panelInfo = panelInfo;
        tempData = new ArrayList<Map<String,String>>();
    }

    private class SceneComboAdapter extends ArrayAdapter<PanelSettingBean> {

        private List<PanelSettingBean> beans;

        public SceneComboAdapter(Context context, int res, List<PanelSettingBean> beans) {
            super(context, res, beans);
            this.beans = beans;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.device_row, null);
            }

            TextView itemLbl = (TextView) convertView.findViewById(R.id.itemLbl);
            PanelSettingBean bean = beans.get(position);
            itemLbl.setText(bean.getLabel());

            return convertView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    private List<PanelSettingBean> getSceneUIBeans(Map<String, SceneBean> sceneMap) {
        ArrayList<PanelSettingBean> beans = new ArrayList<PanelSettingBean>();

        for (SceneBean sb : sceneMap.values()) {
            PanelSettingBean bean = new PanelSettingBean();
            bean.setId(sb.id);
            bean.setType(PanelSettingBean.TYPE_SCENE);
            bean.setLabel(sb.label);

            beans.add(bean);
        }

        if (beans.size() > 0) {
            PanelSettingBean emptBean = new PanelSettingBean();
            emptBean.setId("-1");
            emptBean.setLabel(NONE);
            emptBean.setType(PanelSettingBean.TYPE_SCENE);
            beans.add(0, emptBean);
        }

        return beans;
    }

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_scene);
        inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initSceneCombo();
        initOkBtn();
        initCancelBtn();
    }

    private void appendTempData(String paramName, String paramValue) {
        Map<String, String> paramMap = getParamTemplate();
        paramMap.put("param_name", paramName);
        paramMap.put("param_value", paramValue);

        // Save to memory
        tempData.add(paramMap);
    }

    private Map<String, String> getParamTemplate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("action", "update_info");
        map.put("id", panelInfo.id);
        map.put("fun_name", currentItem.get("type"));
        map.put("fun_id", currentItem.get("id"));

        return map;
    }

    private void initSceneCombo() {
        sceneCombo = (Spinner) findViewById(R.id.sceneCombo);
        sceneCombo.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentItem != null) {
                    try {
                        PanelSettingBean b = (PanelSettingBean) parent.getAdapter().getItem(position);

                        if (!b.getId().equals("-1")) {
                            appendTempData("scene_id", sceneMap.get(b.getId()).id);
                        }
                    } catch (Exception e) {
                        Log.e(SceneSettingDialog.class.getSimpleName(), e.getMessage(), e);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initCancelBtn() {
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initOkBtn() {
        okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Save editing.
                if (tempData.size() > 0) {
                    PanelSettingMainActivity_New activity = (PanelSettingMainActivity_New) myContext;

                    for (Map<String, String> data : tempData) {
                        activity.appendTempData(data);
                    }

                    activity.savePanelInfo();
                }

                dismiss();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        List<PanelSettingBean> sceneUIBeans = getSceneUIBeans(sceneMap);
        sceneCombo.setAdapter(new SceneComboAdapter(myContext, R.layout.device_row, sceneUIBeans));

        int selectedIdx = 0;

        for (int i = 0; i < sceneUIBeans.size(); i++) {
            if (currentItem.get("scene_id").equals(sceneUIBeans.get(i).getId())) {
                selectedIdx = i;
                break;
            }
        }

        sceneCombo.setSelection(selectedIdx);
        sceneCombo.invalidate();
    }
}
