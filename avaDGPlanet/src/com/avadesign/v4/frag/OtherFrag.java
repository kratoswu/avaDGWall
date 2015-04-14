package com.avadesign.v4.frag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.avadesign.R;
import com.avadesign.comp.CustomSlider;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.ZWaveNodeValue;
import com.avadesign.model.bean.FanItem;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.model.bean.SceneItem;
import com.avadesign.model.bean.SwitchItem;
import com.avadesign.model.bean.TemperatureItem;
import com.avadesign.task.SendCmdTask;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;
import com.avadesign.util.StringUtil;

public class OtherFrag extends AbstractPanelFrag {

    public OtherFrag(PanelDetailBean bean) {
        super(bean);
    }

    private boolean hasInitialized;
    private String acc;
    private String pwd;
    // private AvaPref pref;
    private AvaPref appPref;
    private Map<String, SceneBean> sceneNodeMap = new HashMap<String, SceneBean>();
    private Map<String, ZWaveNode> deviceNodeMap;
    private ArrayList<String> switchIdList = new ArrayList<String>();

    // Switches
    private CustomSlider slider0;
    private CustomSlider slider1;
    private CustomSlider slider2;
    private CustomSlider slider3;

    // All on / off
    private Button allOnBtn;
    private Button allOffBtn;

    // Scene buttons
    private Button sceneBtn0;
    private Button sceneBtn1;
    private Button sceneBtn2;
    private Button sceneBtn3;

    // Temp info
    private TextView tempInfo;

    // Fan button
    private Button fanBtn;

    private HashMap<String, Timer> timerQueue = new HashMap<String, Timer>();

    private void pauseService() {
        if (getAvaApp().getWaitingTime() == 0) {
            getAvaApp().setWaitingTime(5);
        }
    }

    private String getGatewayPath() {
        return String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip), getAppPrefVal(R.string.key_gateway_port));
    }

    private void stopTimer(String nodeId) {
        if (timerQueue.containsKey(nodeId)) {
            Log.i(getLogTag(), "Stop timer");
            timerQueue.get(nodeId).cancel();

            synchronized (timerQueue) {
                timerQueue.remove(nodeId);
            }
        }
    }

    private String getLogTag() {
        return getClass().getSimpleName();
    }

//    private class RefreshTask extends AsyncTask<String, Void, Boolean> {
//
//        protected Boolean doInBackground(String... params) {
//            String nodeId = params[0];
//            Log.i(getLogTag(), "Start Refreshing, ID: [" + nodeId + "]...");
//            String urlStr = getGatewayPath() + "/" + getString(R.string.refreshpost);
//            HashMap<String, String> paramMap = new HashMap<String, String>();
//            paramMap.put("node", nodeId);
//
//            return HttpCommunicator.send(urlStr, paramMap, acc, pwd, true);
//        }
//
//    }

//    private class RefreshMultiSwitchTask extends TimerTask {
//        private String nodeId;
//        private int refreshTime;
//
//        public RefreshMultiSwitchTask(String nodeId) {
//            this.nodeId = nodeId;
//        }
//
//        public void run() {
//            if (refreshTime >= 5) {
//                stopTimer(nodeId);
//            } else {
//                refreshTime++;
//                RefreshTask task = new RefreshTask();
//                task.execute(nodeId);
//            }
//        }
//    }

    private class ExecSceneTask extends AsyncTask<String, Void, Boolean> {

        public void onPreExecute() {
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }

        protected Boolean doInBackground(String... params) {
            String urlStr = getGatewayPath() + "/" + getString(R.string.scenepost);
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("fun", "execute");
            paramMap.put("id", params[0]);

            return HttpCommunicator.send(urlStr, paramMap, acc, pwd, true);
        }

    }

    private class LoadSceneTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
            String urlStr = getGatewayPath() + "/" + getString(R.string.scenepost);
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("fun", "load");

            List<HashMap<String, String>> result = HttpCommunicator.getSceneList(urlStr, paramMap, acc, pwd, true);
            sceneNodeMap.clear();

            if (result != null) {
                for (HashMap<String, String> element : result) {
                    SceneBean scene = new SceneBean();
                    scene.id = element.get("id");
                    scene.label = element.get("label");

                    sceneNodeMap.put(scene.id, scene);
                }
            }

            return true;
        }

        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (sceneNodeMap.size() > 0) {
                getActivity().runOnUiThread(new Runnable() {

                    public void run() {
                        // 從 Prefrence 取值的地方要改掉
                        Map<String, SceneItem> sceneMap = pnlConfig.sceneItemMap;

                        if (sceneMap != null) {
                            updateSceneBtn(sceneBtn0, sceneMap, "1");
                            updateSceneBtn(sceneBtn1, sceneMap, "2");
                            updateSceneBtn(sceneBtn2, sceneMap, "3");
                            updateSceneBtn(sceneBtn3, sceneMap, "4");
                        }
                    }
                });

            }
        }

    }

    private void updateSceneBtn(Button btn, Map<String, SceneItem> sceneItemMap, String key) {
        if (sceneItemMap.containsKey(key)) {
            updateSceneBtn(btn, sceneItemMap.get(key).sceneId);
        }
    }

    private void updateSceneBtn(Button btn, final String sceneId) {
        if (sceneNodeMap.containsKey(sceneId)) {
            btn.setText(sceneNodeMap.get(sceneId).label);
            btn.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    executeScene(sceneId);
                }
            });
        }
    }

    private void initSceneBtns(View rootView) {
        sceneBtn0 = (Button) rootView.findViewById(R.id.sceneBtn0);
        sceneBtn1 = (Button) rootView.findViewById(R.id.sceneBtn1);
        sceneBtn2 = (Button) rootView.findViewById(R.id.sceneBtn2);
        sceneBtn3 = (Button) rootView.findViewById(R.id.sceneBtn3);
    }

    protected void executeScene(String nodeID) {
        ExecSceneTask task = new ExecSceneTask();
        task.execute(nodeID);

//        refreshAll();
    }

//    private void refreshAll() {
//        for (String key : switchIdList) {
//            String[] values = key.split("-");
//            refresh(values[0], values[1]);
//        }
//    }

//    private void refresh(String nodeID, String instance) {
//        if (!timerQueue.containsKey(nodeID)) {
//            Timer t = new Timer();
//            t.schedule(new RefreshMultiSwitchTask(nodeID), 500, 4000);
//            timerQueue.put(nodeID, t);
//        }
//    }

    private void sendCmd(String vid, String value) {
        SendCmdTask task = new SendCmdTask(getActivity(), getAvaApp().getAppPref());
        task.execute(getString(R.string.valuepost), vid, value);
    }

    private void updateSwitch(CustomSlider swh, String deviceId, String instance, String label) {
        if (swh.getLockTime() > 0) {
            swh.setLockTime(swh.getLockTime() - 1);
            return;
        } else {
            swh.setDemoStatus(false);

            if (!StringUtil.isEmptyString(deviceId) && !StringUtil.isEmptyString(instance) && deviceNodeMap.containsKey(deviceId)) {
                ZWaveNode node = getNode(deviceId);
                swh.setTitle(label);
                swh.setMultiple(isMultiSwitch(node, instance));
                if (swh.isMultiple()) {
                    updateMultiSwh(swh, deviceId, instance);
                } else {
                    updateBinSwh(swh, deviceId, instance);
                }
            }
        }
    }

    private void updateMultiSwh(CustomSlider swh, String deviceId, String instance) {
        ZWaveNode node = getNode(deviceId);

        if (node == null) {
            Log.e("Empty node ID", deviceId);
            return;
        }

        for (ZWaveNodeValue value : node.value) {
            if (value.class_c.equalsIgnoreCase("SWITCH MULTILEVEL") && value.instance.equals(instance) && value.label.equalsIgnoreCase("LEVEL")) {
                try {
                    swh.updateProgress(Integer.parseInt(value.current.trim()));
                    swh.updateProgInfo(swh.getCurrentProgress());
                } catch (Exception e) {
                }
            }
        }
    }

    private void updateBinSwh(CustomSlider swh, String deviceId, String instance) {
        ZWaveNode node = getNode(deviceId);

        if (node == null) {
            Log.e("Empty node ID", deviceId);
            return;
        }

        for (ZWaveNodeValue value : node.value) {
            if (value.class_c.equalsIgnoreCase("SWITCH BINARY") && value.instance.equals(instance) && value.label.equalsIgnoreCase("switch")) {
                if (value.current.equalsIgnoreCase("True")) {
                    swh.turnOn();
                } else if (value.current.equalsIgnoreCase("False")) {
                    swh.turnOff();
                }
            }
        }
    }

    private boolean isMultiSwitch(ZWaveNode node, String instance) {
        boolean isMutiple = false;

        for (ZWaveNodeValue val : node.value) {
            if (val.class_c.equalsIgnoreCase("SWITCH MULTILEVEL") && val.instance.equals(instance) && val.label.equalsIgnoreCase("LEVEL")) {
                return true;
            }
        }

        return isMutiple;
    }

    private void updateTempInfo() {
        // 取得溫度
        TemperatureItem tempItem = pnlConfig.tempItem;

        if (!StringUtil.isEmptyString(tempItem.nodeId) && !StringUtil.isEmptyString(tempItem.instance) && deviceNodeMap.containsKey(tempItem.nodeId)) {
            ZWaveNode node = deviceNodeMap.get(tempItem.nodeId);

            if (node != null) {
//                Log.e(getLogTag(), tempItem.toString());
                ZWaveNodeValue value = getMatchTempInstance(node, tempItem);

                if (value != null) {
                    tempInfo.setText(value.current + '\u00B0' + value.units);
                }
            }
        }
    }

    private ZWaveNodeValue getMatchTempInstance(ZWaveNode node, SwitchItem item) {
        for (ZWaveNodeValue value : node.value) {
            if (value.class_c.equalsIgnoreCase(item.clazz) && value.label.equalsIgnoreCase("Temperature")
                    && value.instance.equalsIgnoreCase(item.instance)) {
                return value;
            }
        }

        return null;
    }

    private String getResString(int stringId) {
        return getActivity().getString(stringId);
    }

    private void loadScenes() {
        if (sceneNodeMap.size() == 0) {
            LoadSceneTask task = new LoadSceneTask();
            task.execute(new String[0]);
        }
    }

    private void turnOffSwitch(String id, String instance) {
        if (!StringUtil.isEmptyString(id) && !StringUtil.isEmptyString(instance) && getNode(id) != null) {
            if (isMultiSwitch(getNode(id), instance)) {
                switchMulti(id, instance, 0);
            } else {
                switchBinary(id, instance, false);
            }
        } else if (getNode(id) == null) {
            Log.e("Empty node ID", id);
        }
    }

    private void turnOnSwitch(String id, String instance) {
        ZWaveNode node = getNode(id);

        if (StringUtil.isEmptyString(id) || StringUtil.isEmptyString(instance) || node == null) {
            if (node == null) {
                Log.e("Empty node ID", id);
            }

            return;
        }

        if (isMultiSwitch(node, instance)) {
            switchMulti(id, instance, 99);
        } else {
            switchBinary(id, instance, true);
        }
    }

    private ZWaveNode getNode(String id) {
        return deviceNodeMap.get(id);
    }

    private void initAllOffBtn(View rootView) {
        allOffBtn = (Button) rootView.findViewById(R.id.allOffBtn);
        allOffBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // 從 Prefrence 取值的地方要改掉
                Map<String, SwitchItem> switchItemMap = pnlConfig.switchItemMap;
                updateSlider(slider0, switchItemMap, 0, "1");
                updateSlider(slider1, switchItemMap, 0, "2");
                updateSlider(slider2, switchItemMap, 0, "3");
                updateSlider(slider3, switchItemMap, 0, "4");

                pauseService();

                turnOffSwitch(switchItemMap.get("1"));
                turnOffSwitch(switchItemMap.get("2"));
                turnOffSwitch(switchItemMap.get("3"));
                turnOffSwitch(switchItemMap.get("4"));

//                refreshAll();
            }
        });
    }

    private void turnOffSwitch(SwitchItem item) {
        if (item != null) {
            turnOffSwitch(item.nodeId, item.instance);
        }
    }

    private void updateSlider(CustomSlider slider, Map<String, SwitchItem> switchItemMap, int value, String key) {
        if (switchItemMap.containsKey(key) && getNode(switchItemMap.get(key).nodeId) != null) {
            Log.e("", "key: " + key + ", node ID: " + switchItemMap.get(key).nodeId);
            updateSlider(slider, value, switchItemMap.get(key).nodeId);
        }
    }

    private void updateSlider(CustomSlider slider, int value, String nodeId) {
        if (!StringUtil.isEmptyString(nodeId)) {
            slider.updateProgress(value);
            slider.updateProgInfo(value);
        }
    }

    private void initAllOnBtn(View rootView) {
        allOnBtn = (Button) rootView.findViewById(R.id.allOnBtn);
        allOnBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // 從 Preference 取值的地方要改掉.
                Map<String, SwitchItem> switchItemMap = pnlConfig.switchItemMap;
                updateSlider(slider0, switchItemMap, 100, "1");
                updateSlider(slider1, switchItemMap, 100, "2");
                updateSlider(slider2, switchItemMap, 100, "3");
                updateSlider(slider3, switchItemMap, 100, "4");

                pauseService();

                turnOnSwitch(switchItemMap.get("1"));
                turnOnSwitch(switchItemMap.get("2"));
                turnOnSwitch(switchItemMap.get("3"));
                turnOnSwitch(switchItemMap.get("4"));

//                refreshAll();
            }
        });
    }

    private void turnOnSwitch(SwitchItem item) {
        if (item != null) {
            turnOnSwitch(item.nodeId, item.instance);
        }
    }

    private String getVID(ZWaveNode znode, String class_c, String label, String instance) {
        String vid = null;

        for (ZWaveNodeValue zvalue : znode.value) {
            if (zvalue.class_c.equalsIgnoreCase(class_c) && zvalue.label.equalsIgnoreCase(label) && zvalue.instance.equals(instance)) {
                vid = znode.id + "-" + zvalue.class_c + "-" + zvalue.genre + "-" + zvalue.type + "-" + zvalue.instance + "-" + zvalue.index;
            }
        }

        return vid;
    }

    private void initFanBtn(View rootView) {
        fanBtn = (Button) rootView.findViewById(R.id.fanBtn);
        fanBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                FanItem item = pnlConfig.fanItem;
                ZWaveNode node = deviceNodeMap.get(item.nodeId);

                if (node != null) {
                    boolean isOn = false;
                    for (ZWaveNodeValue value : node.value) {
                        if (value.class_c.equalsIgnoreCase("SWITCH BINARY") && value.instance.equals(item.instance)
                                && value.label.equalsIgnoreCase("switch")) {
                            isOn = value.current.equalsIgnoreCase("True");
                        }
                    }

                    switchBinary(item.nodeId, item.instance, !isOn);
                }
            }
        });
    }

    /**
     * @param view
     */
    private void initSliders(View view) {
        slider0 = (CustomSlider) view.findViewById(R.id.mySlider0);
        slider1 = (CustomSlider) view.findViewById(R.id.mySlider1);
        slider2 = (CustomSlider) view.findViewById(R.id.mySlider2);
        slider3 = (CustomSlider) view.findViewById(R.id.mySlider3);

        // 從 Preference 取值的地方要改掉
        Map<String, SwitchItem> switchItemMap = pnlConfig.switchItemMap;
        initSlider(slider0, switchItemMap.get("1"));
        initSlider(slider1, switchItemMap.get("2"));
        initSlider(slider2, switchItemMap.get("3"));
        initSlider(slider3, switchItemMap.get("4"));
    }

    private void initSlider(CustomSlider slider, SwitchItem item) {
        if (item != null) {
            initSlider(slider, item.nodeId, item.instance);
        }
    }

    private class SwitchBtnClickListener implements OnClickListener {
        private String nodeId;
        private String instance;
        private CustomSlider slider;

        public SwitchBtnClickListener(String nodeId, String instance, CustomSlider slider) {
            this.nodeId = nodeId;
            this.instance = instance;
            this.slider = slider;
        }

        public void onClick(View v) {
            if (slider.getLockTime() > 0) {
                return;
            }

            ZWaveNode deviceNode = deviceNodeMap.get(nodeId);

            if (deviceNode != null) {
                if (isMultiSwitch(deviceNode, instance)) {
                    int value = slider.isOn() ? 0 : 99;
                    slider.updateProgress(value);
                    slider.updateProgInfo(value);

                    switchMulti(nodeId, instance, value);
                } else {
                    boolean onOff = !slider.isOn();
                    int value = onOff ? 100 : 0;
                    slider.updateProgInfo(value);
                    slider.updateProgress(value);

                    switchBinary(nodeId, instance, onOff);
                }

                // pauseService();

                /*
                 * 2014-11-04, edited by Phoenix. 改以 lock 特定 slider 的方式等待資料的同步.
                 */
                slider.setDemoStatus(true);
                slider.setLockTime(5);
            }
        }

    }

    private class SlideListener implements OnSeekBarChangeListener {
        private String nodeId;
        private String instance;
        private CustomSlider slider;

        public SlideListener(String nodeId, String instance, CustomSlider slider) {
            this.nodeId = nodeId;
            this.instance = instance;
            this.slider = slider;
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (deviceNodeMap.containsKey(nodeId)) {
                slider.updateProgInfo(progress);
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (slider.getLockTime() > 0) {
                return;
            }

            ZWaveNode deviceNode = deviceNodeMap.get(nodeId);

            if (deviceNode != null) {
                if (isMultiSwitch(deviceNode, instance)) {
                    int progress = seekBar.getProgress();
                    progress = progress == 100 ? 99 : progress;

                    slider.updateProgress(progress);
                    slider.updateProgInfo(progress);

                    switchMulti(nodeId, instance, progress);
                    pauseService();
                } else {
                    boolean needTurnOn = seekBar.getProgress() > slider.getOldProgress();
                    int progress = needTurnOn ? 100 : 0;

                    slider.updateProgress(progress);
                    slider.updateProgInfo(progress);

                    switchBinary(nodeId, instance, needTurnOn);
                }

                // pauseService();

                /*
                 * 2014-11-04, edited by Phoenix. 改以 lock 特定 slider 的方式等待資料的同步.
                 */
                slider.setDemoStatus(true);
                slider.setLockTime(5);
            }

        }

    }

    private void initSlider(CustomSlider slider, String nodeId, String instance) {
        if (!StringUtil.isEmptyString(nodeId) && !StringUtil.isEmptyString(instance)) {
            switchIdList.add(nodeId + "-" + instance);
            slider.setSwitchBtnClickListener(new SwitchBtnClickListener(nodeId, instance, slider));
            slider.setSeekListener(new SlideListener(nodeId, instance, slider));
        }
    }

    private void switchMulti(String id, String instance, int value) {
        ZWaveNode node = getNode(id);

        if (node == null) {
            Log.e("Empty node ID", id);
            return;
        }

        String vid = getVID(node, "switch multilevel", "level", instance);

        if (vid != null) {
            sendCmd(vid, value + "");
//            refresh(id, instance);
        }
    }

    private void switchBinary(String id, String instance, boolean onOff) {
        ZWaveNode znode = getNode(id);

        if (znode == null) {
            Log.e("Empty node ID", id);
            return;
        }

        String vid = getVID(znode, "switch binary", "switch", instance);

        if (vid != null) {
            sendCmd(vid, onOff ? "True" : "False");
        }
    }

    public int getLayoutResId() {
        return R.layout.frag_other;
    }

    public void initView(View rootView) {
        // Get preferences
        appPref = getAvaApp().getAppPref();
        acc = appPref.getValue(getResString(R.string.key_acc));
        pwd = appPref.getValue(getResString(R.string.key_pwd));

        // Inititlize UI components.
        tempInfo = (TextView) rootView.findViewById(R.id.tempInfo);
        initSliders(rootView);
        initAllOnBtn(rootView);
        initAllOffBtn(rootView);
        initSceneBtns(rootView);
        initFanBtn(rootView);
        loadScenes();

        hasInitialized = true;
    }

    public int getPanelTypeID() {
        return 1;
    }

    private void updateSwitch(CustomSlider slider, SwitchItem item) {
        if (item != null) {
            updateSwitch(slider, item.nodeId, item.instance, item.label);
        }
    }

    public void updateUI() {
        // 改寫原先資料取得的方式.
        if (hasInitialized) {
            ArrayList<HashMap<String, Object>> nodeMapList = new ArrayList<HashMap<String, Object>>();
            getAvaApp().refreshNodesList(nodeMapList);
            deviceNodeMap = new HashMap<String, ZWaveNode>();
            for (HashMap<String, Object> nodeMap : nodeMapList) {

                ZWaveNode zNode = new ZWaveNode(nodeMap);
                deviceNodeMap.put(zNode.id, zNode);
            }

            // Switches
            Map<String, SwitchItem> switchItemMap = pnlConfig.switchItemMap;
            updateSwitch(slider0, switchItemMap.get("1"));
            updateSwitch(slider1, switchItemMap.get("2"));
            updateSwitch(slider2, switchItemMap.get("3"));
            updateSwitch(slider3, switchItemMap.get("4"));

            Map<String, SceneItem> sceneMap = pnlConfig.sceneItemMap;

            if (sceneMap != null) {
                updateSceneBtn(sceneBtn0, sceneMap, "1");
                updateSceneBtn(sceneBtn1, sceneMap, "2");
                updateSceneBtn(sceneBtn2, sceneMap, "3");
                updateSceneBtn(sceneBtn3, sceneMap, "4");
            }

            // Temperature
            updateTempInfo();
        }
    }

}
