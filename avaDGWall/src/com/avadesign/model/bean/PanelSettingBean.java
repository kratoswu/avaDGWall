package com.avadesign.model.bean;

import java.io.Serializable;

public class PanelSettingBean implements Serializable {

    private static final long serialVersionUID = -6795478792799394576L;

    public static final int TYPE_SWITCH = 0;
    public static final int TYPE_SCENE = 1;
    public static final int TYPE_TEMPERATURE = 2;

    private String id;
    private String label;
    private int type;

    public String toString() {
        StringBuffer buff = new StringBuffer("id: " + id + ", label: " + label + ", type: ");

        switch (type) {
        case TYPE_SWITCH:
            buff.append("Switch");
            break;

        case TYPE_SCENE:
            buff.append("Scene");
            break;

        case TYPE_TEMPERATURE:
            buff.append("Sensor");
            break;

        default:
            buff.append("Unknown");
            break;
        }

        return buff.toString();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
