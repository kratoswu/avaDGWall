package com.avadesign.frag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.avadesign.R;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.ZWaveNodeValue;
import com.avadesign.model.bean.PanelSettingBean;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.util.StringUtil;

public class DeviceDetailFrag extends Fragment {

    private static final String NONE = "None";
    private static final String EMPTY_VALUE = "Empty Value";
    private TextView nameLbl;
    private EditText nameField;
    private TextView deviceLbl;
    private Spinner deviceCombo;
    private TextView instanceLbl;
    private Spinner instanceCombo;
    private LayoutInflater inflater;
    private Map<String, ZWaveNode> deviceNodeMap;
    private Map<String, SceneBean> sceneMap;
    private Map<String, String> currentItem;
    private List<Map<String, String>> tempData = new ArrayList<Map<String, String>>();
    private String panelID;

    private Map<String, String> getParamTemplate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("action", "update_info");
        map.put("id", panelID);
        map.put("fun_name", currentItem.get("type"));
        map.put("fun_id", currentItem.get("id"));

        return map;
    }

    public List<Map<String, String>> getTempData() {
        return tempData;
    }

    public void onDestroy() {
        tempData = new ArrayList<Map<String, String>>();
        System.gc();
        super.onDestroy();
    }

    private class InstanceAdapter extends ArrayAdapter<ZWaveNodeValue> {
        private List<ZWaveNodeValue> nodeValList;

        public InstanceAdapter(Context context, int resource, int textViewResourceId, List<ZWaveNodeValue> objects) {
            super(context, resource, textViewResourceId, objects);
            nodeValList = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.instance_row, null);
                TextView label = (TextView) convertView.findViewById(R.id.instLbl);
                label.setText(nodeValList.get(position).instance);
            }

            return convertView;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

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

    private boolean isMultiSwitch(ZWaveNode node) {
        for (ZWaveNodeValue value : node.value) {
            if (value.class_c.equalsIgnoreCase("SWITCH MULTILEVEL")) {
                return true;
            }
        }

        return false;
    }

    private boolean isMultiSwitchInstance(ZWaveNodeValue val) {
        return val.class_c.equalsIgnoreCase("SWITCH MULTILEVEL") && val.label.equalsIgnoreCase("LEVEL");
    }

    private boolean isBinSwitchInstance(ZWaveNodeValue val) {
        if (!val.class_c.equalsIgnoreCase("SWITCH BINARY")) {
            return false;
        }

        if (!val.label.equalsIgnoreCase("SWITCH")) {
            return false;
        }

        return true;
    }

    private void displayInstance(ZWaveNode node, int type) {
        int selectedIdx = 0;
        List<ZWaveNodeValue> instances = null;

        if (type == PanelSettingBean.TYPE_SWITCH) {
            if (isMultiSwitch(node)) {
                instances = getMultiSwitchInstances(node);
            } else {
                instances = getBinarySwitchInstances(node);
            }
        } else if (type == PanelSettingBean.TYPE_TEMPERATURE) {
            // Temperature handling
            instances = getTempInstances(node);
        }

        // Update selected index for instance
        if (!StringUtil.isEmptyString(currentItem.get("instance"))) {
            for (int i = 0; i < instances.size(); i++) {
                if (currentItem.get("instance").equals(instances.get(i).instance)) {
                    selectedIdx = i;
                    break;
                }
            }
        }

        InstanceAdapter adapter = new InstanceAdapter(getActivity(), R.layout.instance_row, R.id.instLbl, instances);
        instanceCombo.setAdapter(adapter);
        instanceCombo.setSelection(selectedIdx);
        instanceCombo.invalidate();
        setInstanceVisible(View.VISIBLE);
    }

    private List<ZWaveNodeValue> getTempInstances(ZWaveNode node) {
        ArrayList<ZWaveNodeValue> values = new ArrayList<ZWaveNodeValue>();

        for (ZWaveNodeValue value : node.value) {
            if (isTempInstance(value)) {
                values.add(value);
            }
        }

        return values;
    }

    private List<ZWaveNodeValue> getMultiSwitchInstances(ZWaveNode node) {
        ArrayList<ZWaveNodeValue> values = new ArrayList<ZWaveNodeValue>();

        for (ZWaveNodeValue value : node.value) {
            if (isMultiSwitchInstance(value)) {
                values.add(value);
            }
        }

        return values;
    }

    private List<ZWaveNodeValue> getBinarySwitchInstances(ZWaveNode node) {
        ArrayList<ZWaveNodeValue> values = new ArrayList<ZWaveNodeValue>();

        for (ZWaveNodeValue val : node.value) {
            if (isBinSwitchInstance(val)) {
                values.add(val);
            }
        }

        if (values.size() > 0) {
            ZWaveNodeValue emptyValue = new ZWaveNodeValue(new HashMap<String, Object>());
            emptyValue.instance = NONE;
            emptyValue.class_c = EMPTY_VALUE;
            values.add(0, emptyValue);
        }

        return values;
    }

    private class DeviceArrayAdapter extends ArrayAdapter<PanelSettingBean> {

        private List<PanelSettingBean> beans;

        public DeviceArrayAdapter(Context context, int textViewResourceId, List<PanelSettingBean> beans) {
            super(context, textViewResourceId, beans);
            this.beans = beans;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
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

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_device_detail, container, false);
        nameLbl = (TextView) rootView.findViewById(R.id.nameLbl);
        nameField = (EditText) rootView.findViewById(R.id.nameField);
        deviceLbl = (TextView) rootView.findViewById(R.id.deviceLbl);
        initDeviceCombo(rootView);
        instanceLbl = (TextView) rootView.findViewById(R.id.instanceLbl);
        initInstanceCombo(rootView);

        this.inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return rootView;
    }

    private void initInstanceCombo(View rootView) {
        instanceCombo = (Spinner) rootView.findViewById(R.id.instanceCombo);
        instanceCombo.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentItem != null) {
                    try {
                        ZWaveNodeValue nodeValue = (ZWaveNodeValue) parent.getAdapter().getItem(position);
                        if (!nodeValue.class_c.equals(EMPTY_VALUE)) {

                            // Save Instance info
                            Map<String, String> classMap = getParamTemplate();
                            classMap.put("param_name", "class");
                            classMap.put("param_value", nodeValue.class_c);
                            tempData.add(classMap);

                            Map<String, String> instanceMap = getParamTemplate();
                            instanceMap.put("param_name", "instance");
                            instanceMap.put("param_value", nodeValue.instance);
                            tempData.add(instanceMap);

                            Map<String, String> indexMap = getParamTemplate();
                            indexMap.put("param_name", "index");
                            indexMap.put("param_value", nodeValue.index);
                            tempData.add(indexMap);
                        }
                    } catch (Exception e) {
                        Log.e(DeviceDetailFrag.class.getSimpleName(), e.getMessage(), e);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    private void initDeviceCombo(View rootView) {
        deviceCombo = (Spinner) rootView.findViewById(R.id.deviceCombo);
        deviceCombo.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentItem != null) {
                    try {
                        /*
                         * 取得被選取的 item. 包在 try/catch 裡面是為了防止
                         * NullPointerException 或是其它 Exception 造成程式掛掉.
                         */
                        PanelSettingBean b = (PanelSettingBean) parent.getAdapter().getItem(position);

                        if (!b.getId().equals("-1")) {
                            if (b.getType() == PanelSettingBean.TYPE_SWITCH || b.getType() == PanelSettingBean.TYPE_TEMPERATURE) {
                                // Switch setting
                                ZWaveNode node = deviceNodeMap.get(b.getId());
                                displayInstance(node, b.getType());

                                // Save selected value
                                Map<String, String> paramMap = getParamTemplate();
                                paramMap.put("param_name", "node_id");
                                paramMap.put("param_value", node.id);
                                tempData.add(paramMap);
                            } else if (b.getType() == PanelSettingBean.TYPE_SCENE) {
                                // Scene setting
                                SceneBean sb = sceneMap.get(b.getId());

                                Map<String, String> paramMap = getParamTemplate();
                                paramMap.put("param_name", "scene_id");
                                paramMap.put("param_value", sb.id);
                                tempData.add(paramMap);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(DeviceDetailFrag.this.getClass().getSimpleName(), e.getMessage(), e);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    private void setMainAreaVisible(int visibility) {
        nameLbl.setVisibility(visibility);
        nameField.setVisibility(visibility);
        deviceLbl.setVisibility(visibility);
        deviceCombo.setVisibility(visibility);
    }

    private void setInstanceVisible(int visibility) {
        instanceLbl.setVisibility(visibility);
        instanceCombo.setVisibility(visibility);
    }

    private List<PanelSettingBean> getTemperatureUIBeans(HashMap<String, ZWaveNode> deviceMap) {
        ArrayList<PanelSettingBean> beans = new ArrayList<PanelSettingBean>();

        if (deviceMap != null) {
            for (ZWaveNode node : deviceMap.values()) {
                if (isTemperatureSensor(node)) {
                    PanelSettingBean bean = new PanelSettingBean();
                    bean.setType(PanelSettingBean.TYPE_TEMPERATURE);
                    bean.setId(node.id);
                    bean.setLabel(node.name);

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

    private List<PanelSettingBean> getSwitchUIBeans(Map<String, ZWaveNode> deviceMap) {
        ArrayList<PanelSettingBean> beans = new ArrayList<PanelSettingBean>();

        if (deviceMap != null) {
            for (ZWaveNode node : deviceMap.values()) {
                if (node.gtype.matches(StringUtil.getSwitchTypePattern())) {
                    PanelSettingBean bean = new PanelSettingBean();
                    bean.setType(PanelSettingBean.TYPE_SWITCH);
                    bean.setId(node.id);
                    bean.setLabel(node.name);

                    beans.add(bean);
                }
            }
        }

        if (beans.size() > 0) {
            PanelSettingBean emptyBean = new PanelSettingBean();
            emptyBean.setId("-1");
            emptyBean.setLabel(NONE);
            emptyBean.setType(PanelSettingBean.TYPE_SWITCH);
            beans.add(0, emptyBean);
        }

        return beans;
    }

    public void loadData(String panelID, HashMap<String, String> item, HashMap<String, ZWaveNode> deviceNodeMap, Map<String, SceneBean> sceneMap) {
        this.deviceNodeMap = deviceNodeMap;
        this.sceneMap = sceneMap;
        this.panelID = panelID;

        nameField.setText(item.get("type") + item.get("id"));
        nameField.setEnabled(false);

        this.deviceNodeMap = deviceNodeMap;
        this.sceneMap = sceneMap;
        currentItem = item;

        DeviceArrayAdapter dadaptr = null;
        int selectedIdx = 0;

        if (item.get("type").matches(StringUtil.getSwitchTypePattern())) {
            List<PanelSettingBean> deviceUIBeans = getSwitchUIBeans(deviceNodeMap);
            dadaptr = new DeviceArrayAdapter(getActivity(), R.layout.device_row, deviceUIBeans);

            if (!StringUtil.isEmptyString(item.get("node_id"))) {
                for (int i = 0; i < deviceUIBeans.size(); i++) {
                    if (item.get("node_id").equals(deviceUIBeans.get(i).getId())) {
                        selectedIdx = i;
                        break;
                    }
                }
            }
        } else if (item.get("type").matches(StringUtil.getSceneTypePattern())) {
            List<PanelSettingBean> sceneUIBeans = getSceneUIBeans(sceneMap);
            dadaptr = new DeviceArrayAdapter(getActivity(), R.layout.device_row, sceneUIBeans);
            setInstanceVisible(View.INVISIBLE);

            if(!StringUtil.isEmptyString(item.get("scene_id"))) {
                for (int i = 0; i < sceneUIBeans.size(); i++) {
                    if (item.get("scene_id").equals(sceneUIBeans.get(i).getId())) {
                        selectedIdx = i;
                        break;
                    }
                }
            }
        } else if (item.get("type").equalsIgnoreCase("Temperature")) {
            List<PanelSettingBean> sensorUIBeans = getTemperatureUIBeans(deviceNodeMap);
            dadaptr = new DeviceArrayAdapter(getActivity(), R.layout.device_row, sensorUIBeans);

            if (!StringUtil.isEmptyString(item.get("node_id"))) {
                for (int i = 0; i < sensorUIBeans.size(); i++) {
                    if (item.get("node_id").equals(sensorUIBeans.get(i).getId())) {
                        selectedIdx = i;
                        break;
                    }
                }
            }
        }

        dadaptr.setDropDownViewResource(R.layout.device_row);
        deviceCombo.setAdapter(dadaptr);
        deviceCombo.setSelection(selectedIdx);
        deviceCombo.invalidate();

        setMainAreaVisible(View.VISIBLE);
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

        return beans;
    }

}
