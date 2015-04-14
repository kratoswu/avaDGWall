package com.avadesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.mediastream.Log;

import android.app.Application;

import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.util.AvaPref;

public class SharedClassApp extends Application {
    private static final int NORMAL_MODE = 0;
    private static final int SIMPLE_MODE = 1;
    private static final int DOOR_MODE = 2;

    private ZWaveNode mZWaveNode;
    private ArrayList<HashMap<String,Object>> mNodes;
    private boolean isActive = false; //controller is in active
    private String controllerState = "";
    private AvaPref appPref;
    private int waitingTime;
    private List<PanelDetailBean> pnlDetails = new ArrayList<PanelDetailBean>();
    private String weatherCode;
    private String temperature;
    
    public Map<String, JSONObject> getDpMap() {
        Map<String, JSONObject> result = new HashMap<String, JSONObject>();
        Set<String> dpVals = appPref.getValueSet(getString(R.string.key_dplist));
        
        if (dpVals != null && dpVals.size() > 0) {
            try {
                for (String s : dpVals) {
                    JSONObject obj = new JSONObject(s);
                    result.put(obj.getString(getString(R.string.key_dp_sip)), obj);
                }
            } catch (JSONException e) {
               result.clear();
            }
        }
        
        return result;
    }

    public void setPnlDetails(List<PanelDetailBean> pnlDetails) {
        this.pnlDetails = pnlDetails;
    }

    public List<PanelDetailBean> getPnlDetails() {
        return pnlDetails;
    }

    public void onCreate() {
        super.onCreate();

        initPref();
    }

    private void initPref() {
        appPref = new AvaPref(this, "AVA_WALLPAD");

        // 以下為開發時測試用的 code
//        appPref.setValue(getString(R.string.key_gateway_ip), "192.168.1.45");
//        appPref.setValue(getString(R.string.key_gateway_port), "5000");
//        appPref.setValue(getString(R.string.key_acc), "admin");
//        appPref.setValue(getString(R.string.key_pwd), "admin");
//
//        try {
//            Set<String> dpSet = new LinkedHashSet<String>();
//
//            JSONObject dpObj = new JSONObject();
//            dpObj.put(getString(R.string.key_dp_ip), "192.168.1.104");
//            dpObj.put(getString(R.string.key_dp_label), "DEMO 01");
//            dpObj.put(getString(R.string.key_acc), "admin");
//            dpObj.put(getString(R.string.key_dp_pwd), "admin");
//            dpObj.put(getString(R.string.key_dp_sip), "2004");
//            dpSet.add(dpObj.toString());
//
//            appPref.setValueSet(getString(R.string.key_dplist), dpSet);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    public AvaPref getAppPref() {
        return appPref;
    }

    public void setIsActive(Boolean value){
        isActive = value;
    }

    public boolean isActive(){
        return isActive;
    }

    public void setControllerState(String value){
        controllerState = value;
    }

    public String getControllerState()
    {
        return controllerState;
    }

    public void setZWaveNode(ZWaveNode mZWaveNode){
        if(mZWaveNode == null)
            this.mZWaveNode = null;
        else
            this.mZWaveNode = new ZWaveNode(mZWaveNode);
    }

    public ZWaveNode getZWaveNode(){
        if(mZWaveNode == null)
            return null;
        else
            return new ZWaveNode(mZWaveNode);
    }

    public void setNodesList(ArrayList<HashMap<String,Object>> mNodes)
    {
        this.mNodes = null;
        this.mNodes = new ArrayList<HashMap<String,Object>>(mNodes);
    }

    public void refreshNodesList(ArrayList<HashMap<String,Object>> container){
        container.clear();
        for(HashMap<String,Object> map : mNodes){
            container.add(map);
        }
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }
}
