package com.avadesign.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ZWaveNode implements Serializable {

    private static final long serialVersionUID = 299753900375834004L;

    public String id = "";
    public String sort_id = "";
    public String btype = "";
    public String gtype = "";
    public String icon = "";
    public String name = "";
    public String name_fix = "";
    public String location = "";
    public String manufacturer = "";
    public String product = "";
    public String status = "";
    public long time = 0;
    public boolean routing = false;
    public boolean beam = false;
    public boolean security = false;
    public boolean listening = false;
    public boolean frequent = false;
    public ArrayList<ZWaveNodeValue> value;

    private HashMap<String, Object> ori_data; // the original data, is only to
                                              // used for create a new instance.

    public ZWaveNode(ZWaveNode zwn) { // copy
        this.id = zwn.id;
        this.btype = zwn.btype;
        this.gtype = zwn.gtype;
        this.name = zwn.name;
        this.location = zwn.location;
        this.manufacturer = zwn.manufacturer;
        this.product = zwn.product;
        this.status = zwn.status;
        this.time = zwn.time;
        this.routing = zwn.routing;
        this.beam = zwn.beam;
        this.security = zwn.security;
        this.listening = zwn.listening;
        this.frequent = zwn.frequent;
        this.value = zwn.value;
        this.icon = zwn.icon;
        this.sort_id = zwn.sort_id;

        this.name_fix = zwn.name_fix;

        /*
         * this.type = zwn.type; this.detail = zwn.detail; this.mode =zwn.mode;
         * this.select_int=zwn.select_int; this.battery_text= zwn.battery_text;
         */
    }

    public ZWaveNode(HashMap<String, Object> nodeData) {
        this.ori_data = nodeData;
        parserData();
    }

    private void parserData() {
        value = new ArrayList<ZWaveNodeValue>();

        id = getValueFromMap("id", "");
        btype = getValueFromMap("btype", "");
        gtype = getValueFromMap("gtype", "");
        name = getValueFromMap("name", "");
        location = getValueFromMap("location", "");
        manufacturer = getValueFromMap("manufacturer", "");
        product = getValueFromMap("product", "");
        status = getValueFromMap("status", "");
        icon = getValueFromMap("icon", "");
        time = Long.parseLong(getValueFromMap("time", "0"));
        routing = getValueFromMap("routing", "false").equalsIgnoreCase("true");
        security = getValueFromMap("security", "false").equalsIgnoreCase("true");
        listening = getValueFromMap("listening", "false").equalsIgnoreCase("true");
        frequent = getValueFromMap("frequent", "false").equalsIgnoreCase("true");
        beam = getValueFromMap("beam", "false").equals("true");

        @SuppressWarnings("unchecked")
        ArrayList<HashMap<String, Object>> tmp = (ArrayList<HashMap<String, Object>>) ori_data.get("value");
        for (HashMap<String, Object> map : tmp) {
            value.add(new ZWaveNodeValue(map));
        }

        sort_id = getValueFromMap("id", "");
        name_fix = getName();
        // setBattery();
        // getCurrent();
    }

    private String getValueFromMap(String key, String defaultValue) {
        return ori_data.get(key) == null ? defaultValue : ori_data.get(key).toString();
    }

    private String getName() {
        if (name.equals("") || name.equals(" ")) {
            if (product.equals(""))
                return gtype;
            else
                return product;
        } else
            return name;
    }

}
