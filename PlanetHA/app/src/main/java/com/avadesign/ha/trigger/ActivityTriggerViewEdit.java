package com.avadesign.ha.trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.ServicePolling;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;

public class ActivityTriggerViewEdit extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();

	private Button tab_next,tab_previous;
	
	private ArrayList<HashMap<String,String>> scene_array;
	
	private EditText edit_name,edit_delay_sec;
	private Spinner spinner_node,spinner_scene,spinner_delay_scene,spinner_mode;
	
	private ImageView node_icon;
	
	private ArrayList<HashMap<String,Object>> mNodes;
	
	private Handler handler;
	
	private Boolean EditMode;
	
	private Switch sw;
	
	private ScrollView status_scroll1,status_scroll2;
	private LinearLayout layout_name,layout_notify,layout3,layout2,layout1;
	
	private HashMap<String, String> trigger_map;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trigger_view_edit);
		
		FindView();
		
		Setlistener();
	}
	
	private void FindView()
	{
		edit_delay_sec=(EditText)findViewById(R.id.item_trigger_second_edit);
		
		edit_name=(EditText)findViewById(R.id.trigger_name_edit);
		
		spinner_node=(Spinner)findViewById(R.id.item_trigger_spinner);
		
		spinner_scene=(Spinner)findViewById(R.id.item_trigger_scene_spinner);
		
		spinner_delay_scene=(Spinner)findViewById(R.id.item_trigger_scene_delay_spinner);
		
		spinner_mode=(Spinner)findViewById(R.id.item_trigger_mode_spinner);
		
		node_icon=(ImageView)findViewById(R.id.item_trigger_image);
		
		sw=(Switch)findViewById(R.id.item_trigger_notify_sw);
		
		tab_next= (Button)findViewById(R.id.tab_next);
		tab_previous= (Button)findViewById(R.id.tab_previous);
		
		status_scroll1= (ScrollView)findViewById(R.id.status_scroll1);
		status_scroll2= (ScrollView)findViewById(R.id.status_scroll2);
		layout_name= (LinearLayout)findViewById(R.id.layout_name);
		layout_notify= (LinearLayout)findViewById(R.id.layout_notify);
		layout3= (LinearLayout)findViewById(R.id.layout3);
		layout2= (LinearLayout)findViewById(R.id.layout2);
		layout1= (LinearLayout)findViewById(R.id.layout1);
	}
	
	private void Setlistener()
	{
		mNodes = new ArrayList<HashMap<String,Object>>();
		scene_array = new ArrayList<HashMap<String,String>>();
		
		tab_next.setOnClickListener(admin_button_down);
		tab_previous.setOnClickListener(admin_button_down);
		
		tab_next.setTag(0);
		tab_previous.setTag(1);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		CusPreference cp = new CusPreference(ActivityTriggerViewEdit.this);
		cp.setStopPolling(false);
		
		edit_name.clearFocus();
		edit_delay_sec.clearFocus();

		handler =  new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message msg) 
			{
				String MsgString = (String)msg.obj;
					 
				if (MsgString.equals("scene_ok"))
					GetNode();
				else if (MsgString.equals("node_ok"))
					SetLayout();
				else if (MsgString.equals("layout_ok"))
					ChangeLayout();
				return false;
			}
		});
		
		EditMode=false;
		
		Bundle bundle=this.getIntent().getExtras();
		trigger_map=(HashMap<String, String>) bundle.getSerializable("map");
		
		if (trigger_map.size()>0)
		{
			EditMode=true;
			/*
			Iterator iter = trigger_map.entrySet().iterator(); 

			while (iter.hasNext()) 
			{ 
				Map.Entry entry = (Map.Entry) iter.next(); 
				Object key = entry.getKey(); 
				Object val = entry.getValue(); 
				Log.v(TAG,"key="+key+", value="+val);
			}
			*/ 
			tab_next.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_update), null, null);
			tab_next.setText(R.string.tab_button_update);
		}
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
		
		handler.postDelayed(GetSceneRunnable, 500);
	}
	
	private Runnable GetSceneRunnable = new Runnable()
	{
		@Override
		public void run() 
		{
			GetSceneCommand();
		}
	};
	
	@Override
	protected void onPause() 
	{
		CusPreference cp = new CusPreference(ActivityTriggerViewEdit.this);
		cp.setStopPolling(true);
		
		unregisterReceiver(mBroadcastReceiver);
		super.onPause();
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if(intent.getAction().equals(ServicePolling.HTTP_401))
			{
				DialogSetAuth.show(ActivityTriggerViewEdit.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivityTriggerViewEdit.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivityTriggerViewEdit.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{

			}
		}
	};
	
	private Button.OnClickListener admin_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			if ((Integer) v.getTag()==0)
			{
				if (tab_next.getText().toString().equalsIgnoreCase(getString(R.string.tab_button_next)))
				{
					status_scroll2.setVisibility(View.VISIBLE);
					status_scroll1.setVisibility(View.GONE);
					tab_next.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_save), null, null);
					tab_next.setText(R.string.tab_button_save);
					tab_previous.setVisibility(View.VISIBLE);
				}
				else
				{
					if (EditMode)
					{
						int j=spinner_mode.getSelectedItemPosition();
						String mode="Arm";
						
						switch (j)
						{
							case 0:
								mode="Arm";
								break;
							case 1:
								mode="Bypass";
								break;
							case 2:
								mode="Normal";
								break;
						}
						SaveTriggerCommand(edit_name.getText().equals("") ? "New Trigger" : edit_name.getText().toString(),(String)trigger_map.get("id"),mode,"","","",sw.isChecked() ? "true" : "false");
					}
					else
					{
						int i=spinner_node.getSelectedItemPosition();
						ZWaveNode mZWaveNode=new ZWaveNode(mNodes.get(i));
						
						int j=spinner_mode.getSelectedItemPosition();
						String mode="Arm";
						
						switch (j)
						{
							case 0:
								mode="Arm";
								break;
							case 1:
								mode="Bypass";
								break;
							case 2:
								mode="Normal";
								break;
						}
						
						int k=spinner_scene.getSelectedItemPosition();
						
						HashMap<String, String> map=scene_array.get(k);
						
						int x=spinner_delay_scene.getSelectedItemPosition();
						
						String delay_scene="";
						if (x!=0)
						{
							map=scene_array.get(x-1);
							delay_scene=(String) map.get("id");
						}
						
						SaveTriggerCommand(edit_name.getText().equals("") ? "New Trigger" : edit_name.getText().toString(),mZWaveNode.id,mode,(String)map.get("id"),edit_delay_sec.getText().toString(),delay_scene,sw.isChecked() ? "true" : "false");
					}
				}
			}
			else
			{
				status_scroll1.setVisibility(View.VISIBLE);
				status_scroll2.setVisibility(View.GONE);
				tab_next.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_next), null, null);
				tab_next.setText(R.string.tab_button_next);
				tab_previous.setVisibility(View.GONE);
			}
			
			PlayButtonSound.play(ActivityTriggerViewEdit.this);
		}
	};
	
	private void ChangeLayout()
	{
		if (EditMode)
		{
			status_scroll1.setVisibility(View.VISIBLE);
			status_scroll2.setVisibility(View.GONE);
			tab_next.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_next), null, null);
			tab_next.setText(R.string.tab_button_next);
			tab_previous.setVisibility(View.GONE);
			
			/*
			status_scroll1.setVisibility(View.GONE);
			status_scroll2.setVisibility(View.GONE);
			tab_previous.setVisibility(View.GONE);
			
			layout1.removeView(layout_name);
			layout2.removeView(layout_notify);
			
			layout3.addView(layout_name);
			layout3.addView(layout_notify);
			*/
			
			edit_delay_sec.setFocusable(false);
			
			spinner_node.setEnabled(false);
			spinner_scene.setEnabled(false);
			edit_delay_sec.setEnabled(false);
			spinner_delay_scene.setEnabled(false);
			
			String name=(String) trigger_map.get("label");
			String node_id=(String) trigger_map.get("node");
			String mode=(String) trigger_map.get("mode");
			String scene=Scene_id_to_name((String) trigger_map.get("scene"));
			String time=(String) trigger_map.get("delay_sec");
			String delay_scene=(String) trigger_map.get("delay_scene");
			String notification=(String )trigger_map.get("notification");
			
			edit_name.setText(name);
			
			for(int i=0;i<mNodes.size();i++)
			{		
				ZWaveNode mnode = new ZWaveNode(mNodes.get(i));
				
				if (mnode.id.equals(node_id))
					spinner_node.setSelection(i);
			}
			
			if (mode.equalsIgnoreCase("arm"))
				spinner_mode.setSelection(0);
			else if (mode.equalsIgnoreCase("bypass"))
				spinner_mode.setSelection(1);
			else
				spinner_mode.setSelection(2);
			
			
			for(int i=0;i<scene_array.size();i++)
			{
				HashMap<String, String> map=scene_array.get(i);
				
				if (map.get("label").equalsIgnoreCase(scene))
					spinner_scene.setSelection(i);
			}
			
			if (time!=null)
				edit_delay_sec.setText(time);
		
			if (delay_scene!=null)
			{
				String delay_s=Scene_id_to_name(delay_scene);
				
				for(int i=0;i<scene_array.size();i++)
				{
					HashMap<String, String> map=scene_array.get(i);
					
					if (map.get("label").equalsIgnoreCase(delay_s))
						spinner_delay_scene.setSelection(i+1);
				}
			}
			
			sw.setChecked(notification.equals("true") ? true : false);
		}
		else
		{
			status_scroll1.setVisibility(View.VISIBLE);
			status_scroll2.setVisibility(View.GONE);
			tab_next.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_next), null, null);
			tab_next.setText(R.string.tab_button_next);
			tab_previous.setVisibility(View.GONE);
		}
	}
	
	private void SetLayout()
	{
		
		String [] string_mode=new String [3];
		string_mode[0]="Arm";
		string_mode[1]="Bypass";
		string_mode[2]="Normal";
		
		ArrayAdapter<String> adapter_mode=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_mode);
		adapter_mode.setDropDownViewResource(R.layout.custom_spinner);
		spinner_mode.setAdapter(adapter_mode);
		spinner_mode.setOnItemSelectedListener(Spinner_select);
		
		
		String [] string_scene=new String [scene_array.size()];
		for(int i=0;i<scene_array.size();i++)
		{
			HashMap<String, String> map=scene_array.get(i);
			string_scene[i]=map.get("label");
		}
		
		ArrayAdapter<String> adapter_scene=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_scene);
		adapter_scene.setDropDownViewResource(R.layout.custom_spinner);
		spinner_scene.setAdapter(adapter_scene);
		spinner_scene.setOnItemSelectedListener(Spinner_select);
		
		String [] string_delay_scene=new String [scene_array.size()+1];
		string_delay_scene[0]=getString(R.string.trigger_scene_hint);
		for(int i=0;i<scene_array.size();i++)
		{
			HashMap<String, String> map=scene_array.get(i);
			string_delay_scene[i+1]=map.get("label");
		}
		
		ArrayAdapter<String> adapter_delay_scene=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_delay_scene);
		adapter_delay_scene.setDropDownViewResource(R.layout.custom_spinner);
		spinner_delay_scene.setAdapter(adapter_delay_scene);
		spinner_delay_scene.setOnItemSelectedListener(Spinner_select);
		
		String [] string_node=new String [mNodes.size()];
		for(int i=0;i<mNodes.size();i++)
		{
			ZWaveNode mZWaveNode= new ZWaveNode(mNodes.get(i));
			string_node[i]=getZWaveNode_name(mZWaveNode);
		}
		
		ArrayAdapter<String> adapter_node=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_node);
		adapter_node.setDropDownViewResource(R.layout.custom_spinner);
		spinner_node.setAdapter(adapter_node);
		spinner_node.setOnItemSelectedListener(Spinner_select);
		
		string_mode=null;
		string_scene=null;
		string_delay_scene=null;
		string_node=null;
		
		Message message;
		String obj = "layout_ok";
		message = handler.obtainMessage(1,obj);
		handler.sendMessage(message);
	}
	
	private Spinner.OnItemSelectedListener Spinner_select=new Spinner.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			//Log.v(TAG,"position"+spinner_mode.getSelectedItemPosition());
			
			if (arg0.getId()==R.id.item_trigger_spinner)
			{
				ZWaveNode mZWaveNode=new ZWaveNode(mNodes.get(arg2));
				
				//Log.v(TAG,"id="+mZWaveNode.id);
				
				SetZWaveNode_image(mZWaveNode.id);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			
		}
	};
	
	private void GetSceneCommand()
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityTriggerViewEdit.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("fun","load");
				
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					map.put("tunnelid", "0");
				}
				scene_array.clear();
				scene_array = SendHttpCommand.getlist(
						
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"scenepost.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed(),"scene");
				
				/*
				for (HashMap<String, String> mapp : scene)
				{
					Log.v(TAG,"id="+mapp.get("id")+"label="+mapp.get("label"));
				}
				*/
				Message message;
				String obj = "scene_ok";
				message = handler.obtainMessage(1,obj);
				handler.sendMessage(message);
			}
		}.start();
	}
	
	private void GetNode()
	{
		((SharedClassApp)(ActivityTriggerViewEdit.this.getApplication())).refreshNodesList(mNodes);
		
		ArrayList<HashMap<String,Object>> mNodes1 = new ArrayList<HashMap<String,Object>>();
		
		for(int i=0;i<mNodes.size();i++)
		{
			ZWaveNode znode = new ZWaveNode(mNodes.get(i));
			
			for(ZWaveNodeValue zvalue : znode.value)
			{
				if (zvalue.class_c.equalsIgnoreCase("alarm") && zvalue.label.equalsIgnoreCase("mode"))
				{
					mNodes1.add(mNodes.get(i));
					break;
				}
			}
			/*
			if (znode.gtype.toLowerCase().indexOf("sensor")!=-1)
			{
				mNodes1.add(mNodes.get(i));
			}
			*/
		}
		
		mNodes.clear();
		
		//sensor
		for(int i=0;i<mNodes1.size();i++)
			mNodes.add(mNodes1.get(i));

		mNodes1=null;

		Message message;
		String obj = "node_ok";
		message = handler.obtainMessage(1,obj);
		handler.sendMessage(message);
	}
	
	/*
	private ZWaveNode getZWaveNode(final String node_id)
	{
		for(HashMap<String,Object> map : mNodes)
		{		
			ZWaveNode mnode = new ZWaveNode(map);
			
			if (mnode.id.equals(node_id))
				return mnode;
		}
		return null;
	}
	*/
	
	private String getZWaveNode_name(final ZWaveNode mZWaveNode)
	{
		if (mZWaveNode.name.equals("") || mZWaveNode.name.equals(" "))
        {
        	if (mZWaveNode.product.equals(""))
        		return mZWaveNode.gtype;
        	else
        		return mZWaveNode.product;
        }
        else
        	return mZWaveNode.name;
	}
	
	private void SetZWaveNode_image(final String node_id)
	{
		Log.v(TAG,"node_id="+node_id);
		
		for(HashMap<String,Object> map : mNodes)
		{		
			ZWaveNode znode = new ZWaveNode(map);
			
			if (znode.id.equals(node_id))
			{
				int default_image=-1;
				
				if(znode.gtype.toLowerCase().indexOf("sensor")!=-1)
				{
					if (znode.name.toLowerCase().indexOf("pir")!=-1 || znode.product.toLowerCase().indexOf("pir")!=-1)
						default_image=R.drawable.iroff;
		        	else if (znode.name.toLowerCase().indexOf("door")!=-1 || znode.product.toLowerCase().indexOf("door")!=-1)
		        		default_image=R.drawable.dooroff;
		        	else if (znode.name.toLowerCase().indexOf("window")!=-1 || znode.product.toLowerCase().indexOf("window")!=-1)
		        		default_image=R.drawable.windowoff;
		        	else if (znode.name.toLowerCase().indexOf("co")!=-1 || znode.product.toLowerCase().indexOf("co")!=-1)
		        		default_image=R.drawable.co_off;
		        	else if (znode.name.toLowerCase().indexOf("shock")!=-1 || znode.product.toLowerCase().indexOf("shcok")!=-1)
		        		default_image=R.drawable.shock_off;
		        	else if (znode.name.toLowerCase().indexOf("smoke")!=-1 || znode.product.toLowerCase().indexOf("smoke")!=-1)
		        		default_image=R.drawable.smork_off;
		        	else if (znode.name.toLowerCase().indexOf("water")!=-1 || znode.product.toLowerCase().indexOf("water")!=-1)
		        		default_image=R.drawable.water_off;
				}
				else
				{
					default_image=R.drawable.dimmer_light_off;
				}
				
				String path=label_to_path_label(znode.icon,"normal");
    			
    			Bitmap bitmap = BitmapFactory.decodeFile(path);
    			
    			if (bitmap!=null)
    				node_icon.setImageBitmap(bitmap);
    			else
    				node_icon.setImageResource(default_image);
				
				/*
    			for(ZWaveNodeValue zvalue : znode.value)
    			{
    				if (zvalue.class_c.equalsIgnoreCase("alarm"))
    				{
    					if (zvalue.label.toLowerCase().indexOf("icon")!=-1)
        				{
    						if (!zvalue.current.equalsIgnoreCase("") && !zvalue.current.equalsIgnoreCase("0"))
    						{
    							path=label_to_path_label(zvalue.current,"normal");
        		    			
        		    			bitmap = BitmapFactory.decodeFile(path);
        		    			
        		    			if (bitmap!=null)
        		    				node_icon.setImageBitmap(bitmap);
    						}
        				}
    				}
    			}
    			*/
				break;
			}
		}
	}
	
	private String Scene_id_to_name(String scene_id)
	{
	    for(HashMap<String,String> map : scene_array )
	    {
	        if (map.get("id").equals(scene_id))
	            return map.get("label");
	    }
	    return "";
	}

	private String label_to_path_label (String label , String status)
	{
		CusPreference cp = new CusPreference(ActivityTriggerViewEdit.this);
		ArrayList<HashMap<String,String>> image_array=(ArrayList<HashMap<String,String>>)cp.getIcon_Image();
		
		for(HashMap<String,String> map : image_array)
	    {
	    	if (map.get("type").equalsIgnoreCase(label))
	    	{
	    		String url=null;
	    		
	    		if (status.equalsIgnoreCase("normal"))
	    			url=map.get("Normal");
	    		else if (status.equalsIgnoreCase("on"))
	    			url=map.get("On");
	    		else if (status.equalsIgnoreCase("off"))
	    			url=map.get("Off");
	    		
	    		String [] ary=url.split("/");
	    		
	    		if (ary.length>=3)
	    		{
	    			String path = Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+ary[0]+"/"+ary[1]+"/"+status;
	            	
	    			return path+"/"+ary[2];
	    		}
	    	}
	    }
		return "";
	}
	/*
	private String Scene_name_to_id(String scene_name)
	{
		for(HashMap<String,String> map : scene_array )
	    {
			if (map.get("label").equals(scene_name))
	            return map.get("id");
	    }
	    return "";
	}
	*/
	
	private void SaveTriggerCommand(final String label , final String node_id , final String mode , final String scene , final String delay_sec , final String delay_scene , final String notify)
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityTriggerViewEdit.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("action",EditMode ? "update" : "add");
				
				if (!EditMode)
				{
					map.put("label", label);
					map.put("node", node_id);
					map.put("mode", mode);
					map.put("scene", scene);
					map.put("delay_sec", delay_sec);
					map.put("delay_scene", delay_scene);
					map.put("notification", notify);
				}
				else
				{
					map.put("label", label);
					map.put("id", node_id);
					map.put("mode", mode);
					map.put("notification", notify);
				}
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					map.put("tunnelid", "0");
				}
				
				SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"scene_trigger.cgi",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
			}
		}.start();
		finish();
	}
}
