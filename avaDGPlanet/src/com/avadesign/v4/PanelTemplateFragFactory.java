package com.avadesign.v4;

import java.util.HashMap;
import java.util.Map;

import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.v4.frag.AbstractPanelTemplateFrag;
import com.avadesign.v4.frag.CtrlPnlType_01_Frag;

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
}
