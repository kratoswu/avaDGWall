package com.avadesign.v4.frag;

import com.avadesign.model.bean.PanelDetailBean;

public abstract class AbstractPanelFrag extends AbstractFrag {

    protected PanelDetailBean pnlConfig;

    public AbstractPanelFrag(PanelDetailBean bean) {
        pnlConfig = bean;
    }

    public abstract int getPanelTypeID();

    public abstract void updateUI();

}
