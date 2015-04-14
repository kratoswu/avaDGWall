package com.avadesign.comp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.linphone.mediastream.Log;

import com.avadesign.R;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.ZWaveNodeValue;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.PanelSettingBean;
import com.avadesign.util.StringUtil;
import com.avadesign.v4.PanelSettingMainActivity_New;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class TempSettingDialog extends Dialog {
    private static final String NONE = "None";
    private static final String EMPTY_VALUE = "Empty Value";
    private Context myContext;
    private Map<String, ZWaveNode> deviceMap;
    private Map<String, String> currentItem;
    private PanelDetailBean panelInfo;
    private List<Map<String, String>> tempData;
    private Button okBtn;
    private Button cancelBtn;
    private LayoutInflater inflater;
    private Spinner deviceCombo;
    private Spinner instanceCombo;

    public TempSettingDialog(Context context, Map<String, ZWaveNode> deviceMap, Map<String, String> currentItem, PanelDetailBean panelInfo) {
        super(context);
        myContext = context;
        this.deviceMap = deviceMap;
        this.currentItem = currentItem;
        this.panelInfo = panelInfo;

        tempData = new ArrayList<Map<String,String>>();
    }

    protected void onStart() {
        super.onStart();
        List<PanelSettingBean> deviceUIBeans = getTemperatureUIBeans(deviceMap);
        deviceCombo.setAdapter(new SensorListAdapter(myContext, R.layout.device_row, deviceUIBeans));

        int selectedIdx = 0;

        if (!StringUtil.isEmptyString(currentItem.get("node_id"))) {
            for (int i = 0; i < deviceUIBeans.size(); i++) {
                if (currentItem.get("node_id").equals(deviceUIBeans.get(i).getId())) {
                    selectedIdx = i;
                    break;
                }
            }
        }

        deviceCombo.setSelection(selectedIdx);
        deviceCombo.invalidate();
    }

    private boolean isTemperatureSensor(ZWaveNode node) {
        for (ZWaveNodeValue value : node.value) {
            if (isTempInstance(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTempInstance(ZWaveNodeValue value) {
        return value.class_c.equalsIgnoreCase("SENSOR MULTILEVEL") && value.label.equalsIgnoreCase("Temperature");
    }

    private List<PanelSettingBean> getTemperatureUIBeans(Map<String, ZWaveNode> deviceMap) {
        ArrayList<PanelSettingBean> beans = new ArrayList<PanelSettingBean>();

        if (deviceMap != null) {
            for (ZWaveNode node : deviceMap.values()) {
                if (isTemperatureSensor(node)) {
                    PanelSettingBean bean = new PanelSettingBean();
                    bean.setType(PanelSettingBean.TYPE_TEMPERATURE);
                    bean.setId(node.id);

                    /*
                     * 2014-11-17, edited by Phoenix.
                     * 剛新增的裝置可能沒有 name, 所以要以 gtype + _ + id 的方式做為裝置的
                     * label.
                     * */
                    String label = StringUtil.isEmptyString(node.name) ? node.id + "_" + node.gtype : node.name;
                    bean.setLabel(label);

                    beans.add(bean);
                }
            }
        }

        if (beans.size() > 0) {
            PanelSettingBean emptyBean = new PanelSettingBean();
            emptyBean.setId("-1");
            emptyBean.setLabel(NONE);
            emptyBean.setType(PanelSettingBean.TYPE_TEMPERATURE);
            beans.add(0, emptyBean);
        }

        return beans;
    }

    private class SensorListAdapter extends ArrayAdapter<PanelSettingBean> {
        private List<PanelSettingBean> beans;

        public SensorListAdapter(Context context, int resource, List<PanelSettingBean> beans) {
            super(context, resource, beans);
            this.beans = beans;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.device_row, null);
            }

            TextView itemLbl = (TextView) convertView.findViewById(R.id.itemLbl);

            PanelSettingBean bean =beans.get(position);
            itemLbl.setText(bean.getLabel());

            return convertView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    private class InstanceAdapter extends ArrayAdapter<ZWaveNodeValue> {
        private List<ZWaveNodeValue> data;

        public InstanceAdapter(Context context, int resource, List<ZWaveNodeValue> objects) {
            super(context, resource, objects);
            data = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.instance_row, null);
            }

            TextView label = (TextView) convertView.findViewById(R.id.instLbl);
            label.setText(data.get(position).instance);

            return convertView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_swh);
        inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initDeviceCombo();
        initInstanceCombo();

        initOkBtn();
        initCancelBtn();
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

    private void initInstanceCombo() {
        instanceCombo = (Spinner) findViewById(R.id.instanceCombo);
        instanceCombo.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    ZWaveNodeValue value = (ZWaveNodeValue) parent.getAdapter().getItem(position);

                    if (!value.class_c.equals(EMPTY_VALUE)) {
                        appendTempData("class", value.class_c);
                        appendTempData("instance", value.instance);
                        appendTempData("index", value.index);
                    }
                } catch (Exception e) {
                    Log.e(SwitchSettingDialog.class.getSimpleName(), e.getMessage());
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void displayInstance(ZWaveNode node) {
        int selectedIdx = 0;
        List<ZWaveNodeValue> nodeValues = node.value;
        List<ZWaveNodeValue> tempComboData = new ArrayList<ZWaveNodeValue>();

        if (nodeValues != null && nodeValues.size() > 0) {
            for (ZWaveNodeValue value : nodeValues) {
                if (isTempInstance(value)) {
                    tempComboData.add(value);
                }
            }

            if (!StringUtil.isEmptyString(currentItem.get("instance"))) {
                for (int i = 0; i < tempComboData.size(); i++) {
                    if (currentItem.get("instance").equals(tempComboData.get(i).instance)) {
                        selectedIdx = i;
                        break;
                    }
                }
            }
        }

        InstanceAdapter adapter = new InstanceAdapter(myContext, R.layout.instance_row, tempComboData);
        instanceCombo.setAdapter(adapter);
        instanceCombo.setSelection(selectedIdx);
        instanceCombo.invalidate();
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

    private void initDeviceCombo() {
        deviceCombo = (Spinner) findViewById(R.id.deviceCombo);
        deviceCombo.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentItem != null) {
                    try {
                        PanelSettingBean b = (PanelSettingBean) parent.getAdapter().getItem(position);

                        if (!b.getId().equals("-1")) {
                            ZWaveNode node = deviceMap.get(b.getId());
                            displayInstance(node);
                            appendTempData("node_id", node.id);

                            /*
                             * 2014-11-17, edited by Phoenix.
                             * 剛新增的裝置可能沒有 name, 所以要以 gtype + _ + id 的方式做為裝置的
                             * label.
                             * */
                            String label = StringUtil.isEmptyString(node.name) ? node.id + "_" + node.gtype : node.name;
                            appendTempData("label", label);
                        }
                    } catch (Exception e) {
                        Log.e(TempSettingDialog.class.getSimpleName(), e.getMessage(), e);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
