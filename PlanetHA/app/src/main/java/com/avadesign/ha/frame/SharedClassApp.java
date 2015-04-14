package com.avadesign.ha.frame;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;

public class SharedClassApp extends Application {
	private ZWaveNode mZWaveNode;
	//private ArrayList<String> Location;
	private ArrayList<HashMap<String,Object>> mNodes; 
	private boolean isActive = false; //controller is in active
	private String controllerState = "";
	//private final String TAG = this.getClass().getSimpleName();
	
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
		for(HashMap<String,Object> map:mNodes){
			container.add(map);
		}
	}
}
