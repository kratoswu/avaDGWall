package com.avadesign.v4.frag;

import java.util.Map;

import android.view.View;
import android.widget.Button;

import com.avadesign.R;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.SceneBean;

public class SwhPnlTemplate_02_Frag extends AbstractPanelTemplateFrag {
    
    private Button swhBtn01;
    private Button swhBtn02;
    private Button swhBtn03;
    private Button swhBtn04;
    private Button swhBtn05;
    private Button swhBtn06;

    protected void update(Map<String, ZWaveNode> nodeMap, Map<String, SceneBean> sceneMap) {
        // TODO Auto-generated method stub
        
    }

    public int getLayoutResId() {
        return R.layout.frag_ctrl_02;
    }

    public void initView(View rootView) {
        initSwhBtn01(rootView);
        initSwhBtn02(rootView);
        initSwhBtn03(rootView);
        initSwhBtn04(rootView);
        initSwhBtn05(rootView);
        initSwhBtn06(rootView);
    }

    private void initSwhBtn06(View rootView) {
        swhBtn06 = (Button) rootView.findViewById(R.id.swhBtn06);
        swhBtn06.setText("茅廁燈 on");
        swhBtn06.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_switch_on, 0, 0, 0);
    }

    private void initSwhBtn05(View rootView) {
        swhBtn05 = (Button) rootView.findViewById(R.id.swhBtn05);
        swhBtn05.setText("冰箱燈 off");
        swhBtn05.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_switch_off, 0, 0, 0);
    }

    private void initSwhBtn04(View rootView) {
        swhBtn04 = (Button) rootView.findViewById(R.id.swhBtn04);
        swhBtn04.setText("沒有燈 on");
        swhBtn04.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_switch_on, 0, 0, 0);
    }

    private void initSwhBtn03(View rootView) {
        swhBtn03 = (Button) rootView.findViewById(R.id.swhBtn03);
        swhBtn03.setText("哪個燈 on");
        swhBtn03.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_switch_on, 0, 0, 0);
    }

    private void initSwhBtn02(View rootView) {
        swhBtn02 = (Button) rootView.findViewById(R.id.swhBtn02);
        swhBtn02.setText("浴室燈 off");
        swhBtn02.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_switch_off, 0, 0, 0);
    }

    private void initSwhBtn01(View rootView) {
        swhBtn01 = (Button) rootView.findViewById(R.id.swhBtn01);
        swhBtn01.setText("調光燈 80 %");
        swhBtn01.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_switch_on, 0, 0, 0);
    }

}
