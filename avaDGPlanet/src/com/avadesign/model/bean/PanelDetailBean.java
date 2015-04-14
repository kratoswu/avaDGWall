package com.avadesign.model.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PanelDetailBean {
    public String id;

    public String label;

    public String typeId;

    public TemperatureItem tempItem;

    public FanItem fanItem;

    public Map<String, SceneItem> sceneItemMap;

    public Map<String, SwitchItem> switchItemMap;

    public ArrayList<HashMap<String, String>> allCmptList;

    public Map<String, Map<String, String>> allCmptMap;
}
