package com.avadesign.v4.frag;

import java.util.Map;

import android.view.View;
import android.widget.Button;

import com.avadesign.R;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.SceneBean;

public class ScenePnlType_01_Frag extends AbstractPanelTemplateFrag {
    
    private Button sceneBtn01;
    private Button sceneBtn02;
    private Button sceneBtn03;
    private Button sceneBtn04;
    private Button sceneBtn05;
    private Button sceneBtn06;

    protected void update(Map<String, ZWaveNode> nodeMap, Map<String, SceneBean> sceneMap) {
        // TODO Auto-generated method stub
        
    }

    public int getLayoutResId() {
        return R.layout.frag_scene_01;
    }

    public void initView(View rootView) {
        // TODO Auto-generated method stub
        
    }

}
