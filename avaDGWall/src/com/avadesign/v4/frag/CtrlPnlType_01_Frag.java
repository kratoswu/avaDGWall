package com.avadesign.v4.frag;

import java.util.Map;


import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.avadesign.R;
import com.avadesign.comp.CustomSlider;
import com.avadesign.comp.SceneSettingDialog;
import com.avadesign.comp.SwitchSettingDialog;
import com.avadesign.comp.TempSettingDialog;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.model.bean.SceneItem;
import com.avadesign.model.bean.SwitchItem;
import com.avadesign.util.StringUtil;

public class CtrlPnlType_01_Frag extends AbstractPanelTemplateFrag {

    // Sliders
    private CustomSlider mySlider0;
    private CustomSlider mySlider1;
    private CustomSlider mySlider2;
    private CustomSlider mySlider3;

    // Scene buttons
    private Button sceneBtn0;
    private Button sceneBtn1;
    private Button sceneBtn2;
    private Button sceneBtn3;

    // Fan button
    private Button fanBtn;

    // Temperature
    private TextView tempInfo;

    private SwitchSettingDialog switchDialog;
    private SceneSettingDialog sceneDialog;
    private TempSettingDialog temperatureDialog;

    private boolean hasInit;

    public int getLayoutResId() {
        return R.layout.frag_other;
    }

    public void initView(View rootView) {
        initSliders(rootView);
        initSceneBtns(rootView);
        initFanBtn(rootView);
        initTempInfo(rootView);
        hasInit = true;
    }

    private void initTempInfo(View rootView) {
        tempInfo = (TextView) rootView.findViewById(R.id.tempInfo);
        tempInfo.setOnClickListener(new TempAreaClickListener());
    }

    private void initFanBtn(View rootView) {
        fanBtn = (Button) rootView.findViewById(R.id.fanBtn);
        fanBtn.setOnClickListener(new FanSwitchClickListener());
    }

    private class TempAreaClickListener implements OnClickListener {

        public void onClick(View v) {
            if (nodeMap != null && nodeMap.size() > 0) {
                Map<String, String> currentItem = data.allCmptMap.get("Temperature1");
                temperatureDialog = new TempSettingDialog(getActivity(), nodeMap, currentItem, data);
                temperatureDialog.show();
            }
        }

    }

    private class FanSwitchClickListener implements OnClickListener {

        public void onClick(View v) {
            Map<String, String> currentItem = data.allCmptMap.get("FanSwitch1");
            switchDialog = new SwitchSettingDialog(getActivity(), nodeMap, currentItem, data);
            switchDialog.show();
        }

    }

    private void initSceneBtns(View rootView) {
        initSceneBtn0(rootView);
        initSceneBtn1(rootView);
        initSceneBtn2(rootView);
        initSceneBtn3(rootView);
    }

    private void initSceneBtn3(View rootView) {
        sceneBtn3 = (Button) rootView.findViewById(R.id.sceneBtn3);
        sceneBtn3.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                sceneDialog = new SceneSettingDialog(getActivity(), sceneMap, data.allCmptMap.get("SceneButton4"), data);
                sceneDialog.show();
            }
        });
    }

    private void initSceneBtn2(View rootView) {
        sceneBtn2 = (Button) rootView.findViewById(R.id.sceneBtn2);
        sceneBtn2.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                sceneDialog = new SceneSettingDialog(getActivity(), sceneMap, data.allCmptMap.get("SceneButton3"), data);
                sceneDialog.show();
            }
        });
    }

    private void initSceneBtn1(View rootView) {
        sceneBtn1 = (Button) rootView.findViewById(R.id.sceneBtn1);
        sceneBtn1.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                sceneDialog = new SceneSettingDialog(getActivity(), sceneMap, data.allCmptMap.get("SceneButton2"), data);
                sceneDialog.show();
            }
        });
    }

    private void initSceneBtn0(View rootView) {
        sceneBtn0 = (Button) rootView.findViewById(R.id.sceneBtn0);
        sceneBtn0.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                sceneDialog = new SceneSettingDialog(getActivity(), sceneMap, data.allCmptMap.get("SceneButton1"), data);
                sceneDialog.show();
            }
        });
    }

    private void initSliders(View rootView) {
        initMySlider0(rootView);
        initMySlider1(rootView);
        initMySlider2(rootView);
        initMySlider3(rootView);
    }

    private void initMySlider3(View rootView) {
        mySlider3 = (CustomSlider) rootView.findViewById(R.id.mySlider3);
        mySlider3.setDemoStatus(true);
        SwitchClickListener lsnr = new SwitchClickListener("4");
        mySlider3.setOnClickListener(lsnr);
        mySlider3.setSwitchBtnClickListener(lsnr);
    }

    private void initMySlider2(View rootView) {
        mySlider2 = (CustomSlider) rootView.findViewById(R.id.mySlider2);
        mySlider2.setDemoStatus(true);
        SwitchClickListener lsnr = new SwitchClickListener("3");
        mySlider2.setOnClickListener(lsnr);
        mySlider2.setSwitchBtnClickListener(lsnr);
    }

    private void initMySlider1(View rootView) {
        mySlider1 = (CustomSlider) rootView.findViewById(R.id.mySlider1);
        mySlider1.setDemoStatus(true);
        SwitchClickListener lsnr = new SwitchClickListener("2");
        mySlider1.setOnClickListener(lsnr);
        mySlider1.setSwitchBtnClickListener(lsnr);
    }

    private void initMySlider0(View rootView) {
        mySlider0 = (CustomSlider) rootView.findViewById(R.id.mySlider0);
        mySlider0.setDemoStatus(true);
        SwitchClickListener lsnr = new SwitchClickListener("1");
        mySlider0.setOnClickListener(lsnr);
        mySlider0.setSwitchBtnClickListener(lsnr);
    }

    private class SwitchClickListener implements OnClickListener {
        private String switchId;

        public SwitchClickListener (String switchId) {
            this.switchId = switchId;
        }

        public void onClick(View v) {
            Map<String, String> currentItem = data.allCmptMap.get("LightingSwitch" + switchId);
            switchDialog = new SwitchSettingDialog(getActivity(), nodeMap, currentItem, data);
            switchDialog.show();
        }

    }

    private boolean isStatusOK() {
//        return hasInit && data != null && nodeMap != null && sceneMap != null && nodeMap.size() > 0 && sceneMap.size() > 0;
        return data != null && nodeMap != null && sceneMap != null && nodeMap.size() > 0 && sceneMap.size() > 0;
    }

    private void updateSlider(CustomSlider slider, String sliderId, Map<String, ZWaveNode> nodeMap) {
        SwitchItem switchItem = data.switchItemMap.get(sliderId);

        if (switchItem != null) {
            String nodeId = switchItem.nodeId;
            if (nodeMap.containsKey(nodeId)) {
                /*
                 * 2014-11-17, edited by Phoenix.
                 * 剛新增的裝置可能沒有 name, 所以要以 id + _ + gtype 的方式做為裝置的
                 * label.
                 * */
                ZWaveNode node = nodeMap.get(nodeId);
                String label = StringUtil.isEmptyString(node.name) ? node.id + "_" + node.gtype : node.name;
                slider.setTitle(label);
            }
        } else {
            slider.setTitle("");
        }
    }

    private void updateSceneBtn(Button button, String btnId, Map<String, SceneBean> sceneMap) {
        SceneItem sceneItem = data.sceneItemMap.get(btnId);

        if (sceneItem != null) {
            String sceneId = sceneItem.sceneId;
            if (sceneMap.containsKey(sceneId)) {
                button.setText(sceneMap.get(sceneId).label);
            }
        } else {
            button.setText("");
        }
    }

    public void update(Map<String, ZWaveNode> nodeMap, Map<String, SceneBean> sceneMap) {
        if (isStatusOK()) {
            updateSlider(mySlider0, "1", nodeMap);
            updateSlider(mySlider1, "2", nodeMap);
            updateSlider(mySlider2, "3", nodeMap);
            updateSlider(mySlider3, "4", nodeMap);

            updateSceneBtn(sceneBtn0, "1", sceneMap);
            updateSceneBtn(sceneBtn1, "2", sceneMap);
            updateSceneBtn(sceneBtn2, "3", sceneMap);
            updateSceneBtn(sceneBtn3, "4", sceneMap);
        }
    }

}
