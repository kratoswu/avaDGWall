package com.avadesign.v4.frag;

import java.util.Map;

import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.SceneBean;

public abstract class AbstractPanelTemplateFrag extends AbstractFrag {

    protected PanelDetailBean data;
    protected Map<String, ZWaveNode> nodeMap;
    protected Map<String, SceneBean> sceneMap;

    public PanelDetailBean getData() {
        return data;
    }

    public void setData(PanelDetailBean data) {
        this.data = data;
    }

    public String getTitle() {
        return data.label;
    }

    public void refresh(Map<String, ZWaveNode> nodeMap, Map<String, SceneBean> sceneMap) {
        this.nodeMap = nodeMap;
        this.sceneMap = sceneMap;
        update(nodeMap, sceneMap);
    }

    protected abstract void update(Map<String, ZWaveNode> nodeMap, Map<String, SceneBean> sceneMap);

}
