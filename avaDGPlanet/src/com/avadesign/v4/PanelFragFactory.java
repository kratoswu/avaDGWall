package com.avadesign.v4;

import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.v4.frag.AbstractPanelFrag;
import com.avadesign.v4.frag.OtherFrag;

public abstract class PanelFragFactory {

    public static final AbstractPanelFrag getFrag(PanelDetailBean bean) {
        if (bean.typeId != null) {
            int typeID = Integer.parseInt(bean.typeId);

            if (typeID == 1) {
                return new OtherFrag(bean);
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

}
