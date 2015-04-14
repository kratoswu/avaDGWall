package com.avadesign.ha.frame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ZWaveNode  implements Serializable{
	private static final long serialVersionUID = 1L;
	
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
	/*
	public String type = "";
	public String detail = "";
	public String mode = "";
	public Integer select_int = -1;
	public String battery_text = "";
	*/
	public long time = 0;
	public boolean routing = false;
	public boolean beam = false;
	public boolean security = false;
	public boolean listening = false;
	public boolean frequent = false;
	public ArrayList<ZWaveNodeValue> value;
	
	private HashMap<String, Object> ori_data; //the original data, is only to used for create a new instance.
	
	public ZWaveNode(ZWaveNode zwn){ //copy
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
		this.sort_id=zwn.sort_id;
		
		this.name_fix = zwn.name_fix;
		
		/*
		this.type = zwn.type;
		this.detail = zwn.detail;
		this.mode =zwn.mode;
		this.select_int=zwn.select_int;
		this.battery_text= zwn.battery_text;
		*/
	}
	
	public ZWaveNode(HashMap<String, Object> nodeData){
		this.ori_data = nodeData;
		parserData();
	}
	
	private void parserData(){
		value = new ArrayList<ZWaveNodeValue>();

		id =  getValueFromMap("id","");
		btype = getValueFromMap("btype","");
		gtype = getValueFromMap("gtype","");
		name = getValueFromMap("name","");
		location = getValueFromMap("location","");
		manufacturer = getValueFromMap("manufacturer","");
		product = getValueFromMap("product","");
		status = getValueFromMap("status","");
		icon = getValueFromMap("icon","");
		time = Long.parseLong(getValueFromMap("time","0"));
		routing = getValueFromMap("routing","false").equalsIgnoreCase("true");
		security = getValueFromMap("security","false").equalsIgnoreCase("true");
		listening = getValueFromMap("listening","false").equalsIgnoreCase("true");
		frequent = getValueFromMap("frequent","false").equalsIgnoreCase("true");
		beam = getValueFromMap("beam","false").equals("true");		
		
		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, Object>> tmp = (ArrayList<HashMap<String, Object>>)ori_data.get("value");
		for(HashMap<String, Object>map:tmp){
			value.add(new ZWaveNodeValue(map));
		}
		
		sort_id=getValueFromMap("id","");
		name_fix=getName();
		//setBattery();
		//getCurrent();
	}
	
	private String getValueFromMap(String key, String defaultValue){
		return ori_data.get(key) == null ? defaultValue : ori_data.get(key).toString();
	}
	/*
	private void setBattery()
	{
		Boolean battery=false;                 
        
		String b_text="";
		
        for(int i=0; i<value.size(); i++)
    	{
			ZWaveNodeValue znode_value = value.get(i);
			
			if(znode_value.class_c.equalsIgnoreCase("BATTERY"))
			{
				battery=true;
				b_text=znode_value.current;	
			}
    	}
        
        if (battery)
        	battery_text=b_text;
        else
        	battery_text="";
	}
	*/
	private String getName()
	{
		if (name.equals("") || name.equals(" "))
        {
        	if (product.equals(""))
        		return gtype;
        	else
        		return product;
        }
        else
        	return name;
	}
	/*
	private void getCurrent()
	{
		if (type.equals("sensor"))
		{
			for(int i=0; i<value.size(); i++)
        	{
    			ZWaveNodeValue znode_value =value.get(i);

    			if(znode_value.class_c.equalsIgnoreCase("ALARM"))
    			{
    				if(znode_value.label.equalsIgnoreCase("Alarm Level"))
        			{
    					select_int=i;
        			}
    				else if(znode_value.label.equalsIgnoreCase("Mode"))
    				{
    					mode=znode_value.current;
    				}
    			}
    			else if(znode_value.class_c.equalsIgnoreCase("SENSOR MULTILEVEL"))
    			{
    				select_int=i;
    				detail=znode_value.label+" : "+znode_value.current+"\u00B0"+znode_value.units;
    			}
            }
		}
		else if (type.equals("dimmer"))
        {
        	for(int i=0; i<value.size(); i++)
        	{
    			ZWaveNodeValue znode_value = value.get(i);
    			
    			if(znode_value.class_c.equalsIgnoreCase("SWITCH MULTILEVEL"))
    			{
    				if(znode_value.label.equalsIgnoreCase("Level"))
        			{
    					select_int=i;
        			}
    			}
        	}
        }
		else if (type.equals("cover"))
        {
            for(int i=0; i<value.size(); i++)
        	{
    			ZWaveNodeValue znode_value = value.get(i);
    			
    			if(znode_value.class_c.equalsIgnoreCase("SWITCH MULTILEVEL"))
    			{
    				if(znode_value.label.equalsIgnoreCase("Level"))
        			{        	
    					select_int=i;
        			}
    			}
        	}
        }
		else if (type.equals("switch") || type.equals("lock"))
        {
        	if (product.equals("In-Wall Switch, 2 Relays"))
        	{
                for(int i=0; i<value.size(); i++)
            	{
        			ZWaveNodeValue znode_value = value.get(i);
        			
        			if(znode_value.class_c.equalsIgnoreCase("SWITCH BINARY"))
        			{
        				if (znode_value.instance.equalsIgnoreCase("1"))
        				{
        					select_int=i*10;
        				}
        				else if (znode_value.instance.equalsIgnoreCase("3"))
        				{
        					select_int+=i;
        				}
        			}
            	}
        	}
        	else
        	{
        		if (type.equals("switch"))
                {
                	for(int i=0; i<value.size(); i++)
                	{
            			ZWaveNodeValue znode_value = value.get(i);

            			if(znode_value.class_c.equalsIgnoreCase("SWITCH BINARY"))
            			{
            				select_int=i;
            			}
                	}
                }
                else if (type.equals("lock"))
                {
                	for(int i=0; i<value.size(); i++)
                	{
            			ZWaveNodeValue znode_value = value.get(i);
            			
            			if(znode_value.class_c.equalsIgnoreCase("DOOR LOCK"))
            			{
            				if(znode_value.label.equalsIgnoreCase("Mode"))
            				{
            					select_int=i;
            				}
            			}
                	}
                }
        	}
        }
	}
	*/
	/*
	private String getType()
	{
		String type="unknow";
		
		if (product.equals("Lamp Dimmer Module(Power Plug In Type)"))
        	type="dimmer";
        else if (product.equals("Window Cover"))
        	type="cover";
        else if (product.equals("Wireless Siren and Strobe Alarm Battery Power") || product.equals("Hidden Type On/Off Module") || product.equals("Appliance On/Off Module(Power Plug In Type)") || product.equals("In-Wall Switch, 2 Relays") )
        	type="switch";
        else if (product.equals("Wireless PIR Motion Sensor(Temperature Sensor Built-In)") || product.equals("Wireless Door/Window Sensor") || product.equals("Wireless CO Detector") || product.equals("Wireless Smoke Detector") || product.equals("Wireless Shock Sensor"))
        	type="sensor";
        else if (product.equals("Wireless Electronic Deadbolt Door Lock(Real Time Version)"))
        	type="lock";
        else if (product.indexOf("PC Controller") != -1)
        	type="controller";
        else
        {
        	if (gtype.indexOf("Sensor") != -1)
        		type="sensor";
        	else if (gtype.indexOf("Door Lock") != -1)
        		type="lock";
        	else if (gtype.indexOf("PC Controller") != -1)
        		type="controller";
        	else if (gtype.indexOf("Switch") != -1)
        	{
        		for(int i=0; i<value.size(); i++)
            	{
        			ZWaveNodeValue mZWaveNodeValue = value.get(i);
        			
        			if(mZWaveNodeValue.class_c.equalsIgnoreCase("SWITCH BINARY"))
        			{
        				type="switch";
        			}
        			else if(mZWaveNodeValue.class_c.equalsIgnoreCase("SWITCH MULTILEVEL"))
        			{
        				type="dimmer";
        			}
            	}
        	}
        }
		return type;
	}
	*/
	
}
