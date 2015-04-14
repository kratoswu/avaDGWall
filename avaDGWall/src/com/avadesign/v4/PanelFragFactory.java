package com.avadesign.v4;

import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.v4.frag.AbstractPanelFrag;
import com.avadesign.v4.frag.OtherFrag;
import com.avadesign.v4.frag.SceneType_01_Frag;
import com.avadesign.v4.frag.SwhType_02_Frag;

public abstract class PanelFragFactory {

    public static final AbstractPanelFrag getFrag(PanelDetailBean bean) {
        if (bean.typeId != null) {
            int typeID = Integer.parseInt(bean.typeId);

            if (typeID == 1) {
                return new OtherFrag(bean);
            } else if (typeID == 2) {
                return new SceneType_01_Frag(bean);
            } else if (typeID == 3) {
                return new SwhType_02_Frag(bean);
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

}
