package com.avadesign.v4.frag;

import com.avadesign.model.bean.PanelDetailBean;

public abstract class AbstractPanelFrag extends AbstractFrag {

    protected PanelDetailBean pnlConfig;

    public AbstractPanelFrag(PanelDetailBean bean) {
        pnlConfig = bean;
    }

    public abstract int getPanelTypeID();

    public abstract void updateUI();
    
    public String getFragTitle() {
        String label = pnlConfig == null ? "Empty" : pnlConfig.label;
        return label;
    }

}
