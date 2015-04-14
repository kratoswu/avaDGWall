package com.avadesign.v4;

import java.util.HashMap;
import java.util.Map;

import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.v4.frag.AbstractPanelTemplateFrag;
import com.avadesign.v4.frag.CtrlPnlType_01_Frag;
import com.avadesign.v4.frag.ScenePnlTemplate_01_Frag;
import com.avadesign.v4.frag.SwhPnlTemplate_02_Frag;

public class PanelTemplateFragFactory {

    private static PanelTemplateFragFactory instance;
    private Map<String, TemplateGenerater> generaterMap = new HashMap<String, TemplateGenerater>();

    public static PanelTemplateFragFactory getInstance() {
        if (instance == null) {
            instance = new PanelTemplateFragFactory();
        }

        return instance;
    }

    private PanelTemplateFragFactory() {
        generaterMap.put("1", new Generater01());
        generaterMap.put("2", new Generater02());
        generaterMap.put("3", new Generater03());
    }

    public AbstractPanelTemplateFrag getTemplateFrag(PanelDetailBean bean) {
        return generaterMap.get(bean.typeId).getTemplateFrag(bean);
    }

    private interface TemplateGenerater {
        public AbstractPanelTemplateFrag getTemplateFrag(PanelDetailBean bean);
    }

    private class Generater01 implements TemplateGenerater {

        public AbstractPanelTemplateFrag getTemplateFrag(PanelDetailBean bean) {
            CtrlPnlType_01_Frag frag = new CtrlPnlType_01_Frag();
            frag.setData(bean);
            return frag;
        }

    }
    
    private class Generater02 implements TemplateGenerater {

        public AbstractPanelTemplateFrag getTemplateFrag(PanelDetailBean bean) {
            ScenePnlTemplate_01_Frag frag = new ScenePnlTemplate_01_Frag();
            frag.setData(bean);
            return frag;
        }
        
    }
    
    private class Generater03 implements TemplateGenerater {

        public AbstractPanelTemplateFrag getTemplateFrag(PanelDetailBean bean) {
            SwhPnlTemplate_02_Frag frag = new SwhPnlTemplate_02_Frag();
            frag.setData(bean);
            return frag;
        }
        
    }
}
