package com.avadesign.v4.frag;

import java.util.Map;

import android.view.View;
import android.widget.Button;

import com.avadesign.CtrlPnlTestActivity;
import com.avadesign.R;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.SceneBean;

public class ScenePnlTemplate_01_Frag extends AbstractPanelTemplateFrag {
    
    private Button sceneBtn01;
    private Button sceneBtn02;
    private Button sceneBtn03;
    private Button sceneBtn04;
    private Button sceneBtn05;
    private Button sceneBtn06;

    protected void update(Map<String, ZWaveNode> nodeMap, Map<String, SceneBean> sceneMap) {
        if (getActivity() instanceof CtrlPnlTestActivity) {
            return;
        } else {
            // TODO
        }
    }

    public int getLayoutResId() {
        return R.layout.frag_scene_01;
    }

    public void initView(View rootView) {
        sceneBtn01 = (Button) rootView.findViewById(R.id.sceneBtn01);
        sceneBtn02 = (Button) rootView.findViewById(R.id.sceneBtn02);
        sceneBtn03 = (Button) rootView.findViewById(R.id.sceneBtn03);
        sceneBtn04 = (Button) rootView.findViewById(R.id.sceneBtn04);
        sceneBtn05 = (Button) rootView.findViewById(R.id.sceneBtn05);
        sceneBtn06 = (Button) rootView.findViewById(R.id.sceneBtn06);
    }

}
