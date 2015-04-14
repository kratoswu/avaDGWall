package com.avadesign.v4.frag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.avadesign.PanelSettingMainActivity;
import com.avadesign.R;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.ZWaveNodeValue;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.PanelSettingBean;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.util.StringUtil;

public class PanelDetailFrag extends Fragment {

    private static final String NONE = "None";
    private static final String EMPTY_VALUE = "Empty Value";

    private ArrayList<HashMap<String, String>> componentTempData = new ArrayList<HashMap<String, String>>();
    private ListView cmptList;
    private PanelDetailBean bean;
    private LayoutInflater inflater;
    private ComponentListAdapter listAdapter;
    private Map<String, ZWaveNode> deviceNodeMap;
    private Spinner deviceCombo;
    private TextView instanceLbl;
    private Spinner instanceCombo;
    private Map<String, SceneBean> sceneMap;
    private Map<String, String> currentItem;
    private PanelSettingMainActivity parentAct;
    private Button rmPnlBtn;
    private TextView nameLbl;
    private TextView deviceLbl;
    private TextView nameValLbl;

    private void showMainArea() {
        setMainAreaVisible(View.VISIBLE);
    }

    private void hideMainArea() {
        setMainAreaVisible(View.INVISIBLE);
    }

    private void setMainAreaVisible(int visibility) {
        nameLbl.setVisibility(visibility);
        nameValLbl.setVisibility(visibility);
        deviceLbl.setVisibility(visibility);
        deviceCombo.setVisibility(visibility);
    }

    public void updateSceneMap(Map<String, SceneBean> sceneMap) {
        this.sceneMap = sceneMap;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Device drop list 的 adapter.
     * */
    private class DeviceArrayAdapter extends ArrayAdapter<PanelSettingBean> {

        private List<PanelSettingBean> beans;

        public DeviceArrayAdapter(Context context, int resource, List<PanelSettingBean> beans) {
            super(context, resource, beans);
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

    private void hideInstanceArea() {
        setInstanceAreaVisible(View.INVISIBLE);
    }

    private void displayInstanceArea() {
        setInstanceAreaVisible(View.VISIBLE);
    }

    private void setInstanceAreaVisible(int visibility) {
        instanceLbl.setVisibility(visibility);
        instanceCombo.setVisibility(visibility);
    }

    public PanelDetailFrag() {
    }

    public PanelDetailFrag(PanelDetailBean bean) {
        this.bean = bean;
        componentTempData = new ArrayList<HashMap<String, String>>();
        componentTempData.addAll(this.bean.allCmptList);
    }

    /**
     * 左側 button list 的 adapter.
     * */
    private class ComponentListAdapter extends ArrayAdapter<HashMap<String, String>> {

        private int selectedIdx = -1;

        public ComponentListAdapter(Activity activity, int deviceRow, List<HashMap<String, String>> objects) {
            super(activity, deviceRow, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.cmpt_row, null);
            }

            TextView cmptName = (TextView) convertView.findViewById(R.id.cmptLbl);
            Map<String, String> item = componentTempData.get(position);
            cmptName.setText(item.get("type") + " " + item.get("id"));

            int bgRes = position == selectedIdx ? R.drawable.cmpt_item_1 : R.drawable.cmpt_item_0;
            convertView.setBackgroundResource(bgRes);

            return convertView;
        }

        public void setSelectedIdx(int selectedIdx) {
            this.selectedIdx = selectedIdx;
            this.notifyDataSetInvalidated();
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_pnl_detail, container, false);
        this.inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        parentAct = (PanelSettingMainActivity) getActivity();

        // Initialize
        listAdapter = new ComponentListAdapter(getActivity(), R.layout.cmpt_row, componentTempData);
        cmptList = (ListView) rootView.findViewById(R.id.cmptList);
        cmptList.setAdapter(listAdapter);
        cmptList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listAdapter.setSelectedIdx(position);
                showMainArea();

                if (deviceNodeMap != null && deviceNodeMap.size() > 0) {
                    // Update UI
                    currentItem = componentTempData.get(position);
                    Log.e("Debug", currentItem.toString());
                    nameValLbl.setText(currentItem.get("label"));

                    DeviceArrayAdapter dadaptr = null;
                    int selectedIdx = 0;

                    if (currentItem.get("type").matches(StringUtil.getSwitchTypePattern())) {
                        List<PanelSettingBean> deviceUIBeans = getSwitchUIBeans(deviceNodeMap);
                        dadaptr = new DeviceArrayAdapter(getActivity(), R.layout.device_row, deviceUIBeans);

                        if (!StringUtil.isEmptyString(currentItem.get("node_id"))) {
                            for (int i = 0; i < deviceUIBeans.size(); i++) {
                                if (currentItem.get("node_id").equals(deviceUIBeans.get(i).getId())) {
                                    selectedIdx = i;
                                    break;
                                }
                            }
                        }
                    } else if (currentItem.get("type").matches(StringUtil.getSceneTypePattern())) {
                        List<PanelSettingBean> sceneUIBeans = getSceneUIBeans(sceneMap);
                        dadaptr = new DeviceArrayAdapter(getActivity(), R.layout.device_row, sceneUIBeans);
                        hideInstanceArea();

                        if (!StringUtil.isEmptyString(currentItem.get("scene_id"))) {
                            for (int i = 0; i < sceneUIBeans.size(); i++) {
                                if (currentItem.get("scene_id").equals(sceneUIBeans.get(i).getId())) {
                                    selectedIdx = i;
                                    break;
                                }
                            }
                        }
                    } else if (currentItem.get("type").equalsIgnoreCase("Temperature")) {
                        List<PanelSettingBean> sensorUIBeans = getTemperatureUIBeans(deviceNodeMap);
                        dadaptr = new DeviceArrayAdapter(getActivity(), R.layout.device_row, sensorUIBeans);

                        if (!StringUtil.isEmptyString(currentItem.get("node_id"))) {
                            for (int i = 0; i < sensorUIBeans.size(); i++) {
                                if (currentItem.get("node_id").equals(sensorUIBeans.get(i).getId())) {
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
                }
            }
        });

        nameLbl = (TextView) rootView.findViewById(R.id.nameLbl);
        initNameField(rootView);
        deviceLbl = (TextView) rootView.findViewById(R.id.deviceLbl);
        initDeviceCombo(rootView);
        initInstanceCombo(rootView);
        initRmPnlBtn(rootView);

        return rootView;
    }

    private void initNameField(View rootView) {
        nameValLbl = (TextView) rootView.findViewById(R.id.nameValLbl);
    }

    private void initRmPnlBtn(View rootView) {
        rmPnlBtn = (Button) rootView.findViewById(R.id.rmPnlBtn);
        rmPnlBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                ((PanelSettingMainActivity) getActivity()).rmPanel(bean.id);
            }
        });
    }

    private void initInstanceCombo(View rootView) {
        instanceLbl = (TextView) rootView.findViewById(R.id.instanceLbl);
        instanceCombo = (Spinner) rootView.findViewById(R.id.instanceCombo);
        instanceCombo.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentItem != null) {
                    try {
                        ZWaveNodeValue value = (ZWaveNodeValue) parent.getAdapter().getItem(position);

                        if (!value.class_c.equals(EMPTY_VALUE)) {
                            appendTempData("class", value.class_c);
                            appendTempData("instance", value.instance);
                            appendTempData("index", value.index);
                        }
                    } catch (Exception e) {
                        Log.e(PanelDetailFrag.class.getSimpleName(), e.getMessage(), e);
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
                        PanelSettingBean b = (PanelSettingBean) parent.getAdapter().getItem(position);

                        if (!b.getId().equals("-1")) {
                            if (b.getType() == PanelSettingBean.TYPE_SWITCH || b.getType() == PanelSettingBean.TYPE_TEMPERATURE) {
                                // Switch setting
                                ZWaveNode node = deviceNodeMap.get(b.getId());
                                displayInstance(node, b.getType());
                                appendTempData("node_id", node.id);
                            } else if (b.getType() == PanelSettingBean.TYPE_SCENE) {
                                // Scene setting.
                                appendTempData("scene_id", sceneMap.get(b.getId()).id);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(PanelDetailFrag.class.getSimpleName(), e.getMessage(), e);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void appendTempData(String paramName, String paramValue) {
        Map<String, String> paramMap = getParamTemplate();
        paramMap.put("param_name", paramName);
        paramMap.put("param_value", paramValue);

        parentAct.appendTempData(paramMap);
    }

    private Map<String, String> getParamTemplate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("action", "update_info");
        map.put("id", bean.id);
        map.put("fun_name", currentItem.get("type"));
        map.put("fun_id", currentItem.get("id"));

        return map;
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
            instances = getTempInstances(node);
        }

        if (!StringUtil.isEmptyString(currentItem.get("instance"))) {
            for (int i = 0; i < instances.size(); i++) {
                if (currentItem.get("instance").equals(instances.get(i).instance)) {
                    selectedIdx = i;
                    break;
                }
            }
        }

        // Instance adapter
        InstanceAdapter adapter = new InstanceAdapter(getActivity(), R.layout.instance_row, R.id.instLbl, instances);
        instanceCombo.setAdapter(adapter);
        instanceCombo.setSelection(selectedIdx);
        instanceCombo.invalidate();
        displayInstanceArea();
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

    private boolean isBinSwitchInstance(ZWaveNodeValue val) {
        if (!val.class_c.equalsIgnoreCase("SWITCH BINARY")) {
            return false;
        }

        if (!val.label.equalsIgnoreCase("SWITCH")) {
            return false;
        }

        return true;
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

    private boolean isMultiSwitchInstance(ZWaveNodeValue value) {
        return value.class_c.equalsIgnoreCase("SWITCH MULTILEVEL") && value.label.equalsIgnoreCase("LEVEL");
    }

    private boolean isMultiSwitch(ZWaveNode node) {
        for (ZWaveNodeValue value : node.value) {
            if (value.class_c.equalsIgnoreCase("SWITCH MULTILEVEL")) {
                return true;
            }
        }

        return false;
    }

    private List<PanelSettingBean> getTemperatureUIBeans(Map<String, ZWaveNode> deviceMap) {
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

    public void updateDeviceList(Map<String, ZWaveNode> deviceNodeMap) {
        this.deviceNodeMap = deviceNodeMap;
    }

}
