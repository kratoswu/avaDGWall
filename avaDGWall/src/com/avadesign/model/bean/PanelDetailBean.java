package com.avadesign.model.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PanelDetailBean {
    public String id;

    public String label;

    public String typeId;

    /**
     * @deprecated
     * 初版的面板只有一個溫度顯示元件, 但後來會有多個的可能性, 建議改用 {@code TemperatureItem.tempItemMap}
     * */
    @Deprecated
    public TemperatureItem tempItem;

    public FanItem fanItem;

    public Map<String, SceneItem> sceneItemMap;

    public Map<String, SwitchItem> switchItemMap;

    public ArrayList<HashMap<String, String>> allCmptList;

    public Map<String, Map<String, String>> allCmptMap;
    
    public Map<String, TemperatureItem> tempItemMap;
    
    public Map<String, SensorItem> sensorItemMap;
}
