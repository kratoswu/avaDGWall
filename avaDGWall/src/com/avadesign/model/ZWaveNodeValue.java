package com.avadesign.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ZWaveNodeValue implements Serializable {

    private static final long serialVersionUID = 7251457033595202521L;

    public String genre;
    public String index;
    public String polled;
    public String current;
    public String class_c;
    public String label;
    public String type;
    public String instance;
    public boolean readonly;
    public String units;
    public String help;
    public String[] item;

    private HashMap<String, Object> ori_data; // the original data, is only to
                                              // used for create a new instance.
    private int position; // the position of (ArrayList<ZWaveNodeValue>)

    public ZWaveNodeValue(HashMap<String, Object> data) {
        this.ori_data = data;
        parserData();
    }

    private void parserData() {
        genre = getValueFromMap("genre", "");
        index = getValueFromMap("index", "");
        polled = getValueFromMap("polled", "");
        current = getValueFromMap("current", "");
        class_c = getValueFromMap("class", "");
        label = getValueFromMap("label", "");
        type = getValueFromMap("type", "");
        instance = getValueFromMap("instance", "");
        units = getValueFromMap("units", "");
        help = getValueFromMap("help", "");
        readonly = getValueFromMap("readonly", "true").equalsIgnoreCase("true");

        @SuppressWarnings("unchecked")
        ArrayList<String> tmp = (ArrayList<String>) ori_data.get("item");
        if (tmp != null) {
            item = new String[tmp.size()];
            tmp.toArray(item);
        } else {
            item = new String[0];
        }

    }

    private String getValueFromMap(String key, String defaultValue) {
        return ori_data.get(key) == null ? defaultValue : ori_data.get(key).toString();
    }

    public void setPosition(int p) {
        position = p;
    }

    public int getPosition() {
        return position;
    }

}
