package com.avadesign.ha.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
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

public class ActivityNodeList extends Activity 
{	
	private ListView mNode_list;
	
	private TextView seekbar_text;
	
	private MyCustomAdapter adapter;
	
	private ArrayList<ArrayList<HashMap<String,Object>>> mNodes;
	
	private ArrayList<HashMap<String,Object>> room_node_array,node_user_value;
	
	private Button back,edit;
	
	private ProgressDialog mDialog_SPINNER;
	private ProgressDialog mDialog_SPINNER_scene;
	private ProgressDialog mDialog_SPINNER_status;

	//private SendValueTask mSendValueTask;
	private SendCommandTask mSendCommandTask;
	
	private Boolean Edit=false,Touch=false;
	
	private String SeekStr="",scene_id;;
	
	private int page;
	
	private boolean SceneMode; 
	
	private HashMap<String,Object> node;
	
	private ArrayList<HashMap<String,String>> location;
	
	private final String TAG = this.getClass().getSimpleName();
	
	private HorizontalScrollView scrolliew_room;
	private LinearLayout scrolliew_room_layout;
	
	private ArrayList<ImageButton> device_bt_ary;
	
	private boolean init_scroll;
	
	private ImageView img_icon,img_power;
	private TextView txt_name,txt_loc,txt_power;
	private LinearLayout layout_power;
	
	private boolean hasAlarm;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_node_list);
		
		FindView();
		
		Setlistener();
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
		
		mDialog_SPINNER_status = new ProgressDialog(this);
		mDialog_SPINNER_status.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER_status.setCancelable(false);
		
		mDialog_SPINNER_scene = new ProgressDialog(this);
		mDialog_SPINNER_scene.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		
		mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
		mDialog_SPINNER.show();
		
		Bundle bundle=this.getIntent().getExtras();
		page=bundle.getInt("page");
		page=page-1;
		SceneMode=bundle.getBoolean("SceneMode");
		
		
		Log.v(TAG,"Scene_Mode="+SceneMode);
		
		if (!SceneMode)
			location=(ArrayList<HashMap<String, String>>) bundle.getSerializable("location");
		else
			scene_id=bundle.getString("scene_id");
			
		room_node_array=(ArrayList<HashMap<String,Object>>) bundle.getSerializable("room_node_array");
		room_node_array.remove(0);
		
		init_scroll=false;
		
		Log.v(TAG,"page="+page);
	}

	private void FindView()
	{
		img_icon=(ImageView)findViewById(R.id.item_image);
		img_power=(ImageView)findViewById(R.id.power_image);
		txt_name=(TextView)findViewById(R.id.item_name);
		txt_loc=(TextView)findViewById(R.id.item_location);
		txt_power=(TextView)findViewById(R.id.power_text);
		layout_power=(LinearLayout)findViewById(R.id.layout_power);
		
		mNode_list = (ListView)findViewById(R.id.listview);
		
		seekbar_text=(TextView)findViewById(R.id.seekbar_text);
		
		back= (Button)this.findViewById(R.id.tab_back);
		edit= (Button)this.findViewById(R.id.tab_edit);
		
		scrolliew_room=(HorizontalScrollView)this.findViewById(R.id.scrollview_room);
		scrolliew_room_layout=(LinearLayout)this.findViewById(R.id.scrollview_room_layout);
		
		scrolliew_room.setVerticalScrollBarEnabled(false);
		scrolliew_room.setHorizontalScrollBarEnabled(false);
	}
	
	private void Setlistener()
	{
		node_user_value = new ArrayList<HashMap<String,Object>>();
		mNodes = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		device_bt_ary= new ArrayList<ImageButton>();
		
		adapter = new MyCustomAdapter(this, R.layout.item_node,node_user_value);
		mNode_list.setAdapter(adapter);

		mNode_list.setOnItemClickListener(listItem_down);
		
		back.setOnClickListener(tab_button_down);
		edit.setOnClickListener(tab_button_down);
		
		back.setTag(1);
		edit.setTag(2);
		
		back.setVisibility(View.GONE);
	}
	
	@Override
	protected void onResume() 
	{
		((SharedClassApp)(ActivityNodeList.this.getApplication())).setZWaveNode(null);
		
		CusPreference cp = new CusPreference(ActivityNodeList.this);
		
		cp.setStopPolling(false);

		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		registerReceiver(mBroadcastReceiver, mFilter);
		
		hasAlarm=false;
		Edit=false;	
		edit.setSelected(false);
		
		if (!cp.getUserName().equals("admin"))
			edit.setVisibility(View.GONE);
		else
		{
			if (SceneMode)
				edit.setVisibility(View.GONE);
		}
    	
    	
		adapter.notifyDataSetChanged();

		if (!init_scroll)
			initScroll();
		
		super.onResume();
	}

	@Override
	protected void onPause() 
	{
		CusPreference cp = new CusPreference(ActivityNodeList.this);
		cp.setStopPolling(true);
		
		unregisterReceiver(mBroadcastReceiver);
		super.onPause();
	}	
	
	private void initScroll()
	{
		CusPreference cp = new CusPreference(ActivityNodeList.this);
		
		init_scroll=true;
		
		device_bt_ary.clear();
		
		for(int i=0;i<room_node_array.size();i++)
		{
			ZWaveNode znode = new ZWaveNode(room_node_array.get(i));
			
			String zname=znode.name_fix;
			
			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
			
			View view= inflater.inflate(R.layout.room_frame,null);
			
			ImageButton imgbt=(ImageButton)view.findViewById(R.id.room_frame_image);
			TextView name_text=(TextView)view.findViewById(R.id.room_frame_name);
			
			imgbt.setTag(i);
			imgbt.setOnClickListener(device_button_down);
			
			if (page==i)
				imgbt.setSelected(true);

            if (znode.gtype.toLowerCase().equalsIgnoreCase("static pc controller") || 
            		znode.gtype.toLowerCase().indexOf("sensor") !=-1 || 
            		znode.gtype.toLowerCase().indexOf("repeater") !=-1 )
            {
                if (znode.gtype.toLowerCase().equalsIgnoreCase("static pc controller") || znode.gtype.toLowerCase().indexOf("repeater") !=-1)
                {
                	imgbt.setImageResource(R.drawable.gw);
                	
                	if (!znode.icon.equalsIgnoreCase(""))
                    {
                		String path=label_to_path_label(znode.icon,"normal");
            			
            			Bitmap bitmap = BitmapFactory.decodeFile(path);
            			
            			if (bitmap!=null)
            				imgbt.setImageBitmap(bitmap);
                	}
                }
                else
                {
                	int default_image=-1;
    				
    				if (znode.name_fix.toLowerCase().indexOf("pir")!=-1 || znode.product.toLowerCase().indexOf("pir")!=-1)
						default_image=R.drawable.iroff;
    				else if (znode.name_fix.toLowerCase().indexOf("motion")!=-1 || znode.product.toLowerCase().indexOf("motion")!=-1)
						default_image=R.drawable.iroff;
					else if (znode.name_fix.toLowerCase().indexOf("door")!=-1 || znode.product.toLowerCase().indexOf("door")!=-1)
						default_image=R.drawable.dooroff;
					else if (znode.name_fix.toLowerCase().indexOf("window")!=-1 || znode.product.toLowerCase().indexOf("window")!=-1)
						default_image=R.drawable.windowoff;
					else if (znode.name_fix.toLowerCase().indexOf("co")!=-1 || znode.product.toLowerCase().indexOf("co")!=-1)
						default_image=R.drawable.co_off;
					else if (znode.name_fix.toLowerCase().indexOf("smoke")!=-1 || znode.product.toLowerCase().indexOf("smoke")!=-1)
						default_image=R.drawable.smork_off;
					else if (znode.name_fix.toLowerCase().indexOf("shock")!=-1 || znode.product.toLowerCase().indexOf("shock")!=-1)
						default_image=R.drawable.shock_off;
					else if (znode.name_fix.toLowerCase().indexOf("water")!=-1 || znode.product.toLowerCase().indexOf("water")!=-1)
						default_image=R.drawable.water_off;
					else
						default_image=R.drawable.shock_off;
    				
    				String path=label_to_path_label(znode.icon,"off");
            		Bitmap bitmap = BitmapFactory.decodeFile(path);
            			
            		if (bitmap!=null)
            			imgbt.setImageBitmap(bitmap);
            		else
            			imgbt.setImageResource(default_image); 
                }
            }
            else
            {
            	int default_image=-1;
            	
            	if (znode.name_fix.toLowerCase().indexOf("siren")!=-1 || znode.product.toLowerCase().indexOf("siren")!=-1)
            		default_image=R.drawable.alertoff;
            	else if (znode.name_fix.toLowerCase().indexOf("door lock")!=-1 || znode.product.toLowerCase().indexOf("door lock")!=-1)
            		default_image=R.drawable.doorkeypad_off;
            	else
            		default_image=R.drawable.dimmer_light_off;
            	
            	String path=label_to_path_label(znode.icon,"off");
    			Bitmap bitmap = BitmapFactory.decodeFile(path);
    			
    			if (bitmap!=null)
    				imgbt.setImageBitmap(bitmap);
    			else
    				imgbt.setImageResource(default_image);
            }
			
			name_text.setText(zname);
			device_bt_ary.add(imgbt);
			scrolliew_room_layout.addView(view);
		}
	}
	
	private Button.OnClickListener device_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			page=(Integer) v.getTag();
			//adapter.notifyDataSetChanged();
			Log.v(TAG,"room.tag="+v.getTag());

			for(ImageButton ibt : device_bt_ary)
			{
				if (ibt.getTag()==v.getTag())
					ibt.setSelected(true);
				else
					ibt.setSelected(false);
			}		
			
			PlayButtonSound.play(ActivityNodeList.this);
		}
	};
	
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
		{
			PlayButtonSound.play(ActivityNodeList.this);
			
			ZWaveNode znode = new ZWaveNode(node);
			
			HashMap<String,Object> values=node_user_value.get(position);
			ZWaveNodeValue zvalue=new ZWaveNodeValue(values);
			
			
			if (SceneMode)
		    {
				String vid = null;
				String value = null;
		        
				if (zvalue.genre.toLowerCase().equalsIgnoreCase("user"))
		        {
					if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch binary"))
        			{
    					if (zvalue.label.toLowerCase().equalsIgnoreCase("switch"))
    					{
    						vid=znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
    						value=zvalue.current;
    					}
        			}
    				else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch multilevel"))
        			{
    					if (zvalue.label.toLowerCase().equalsIgnoreCase("level"))
    					{
    						vid=znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
    						value=zvalue.current;
    					}
        			}
    				else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("alarm"))
    				{
    					if (zvalue.label.toLowerCase().equalsIgnoreCase("mode"))
    					{
    						vid=znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
    						value=zvalue.current;
    					}
    				}
    				else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("door lock"))
    				{
    					if (zvalue.label.toLowerCase().equalsIgnoreCase("mode"))
    					{
    						vid=znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
    						value=zvalue.current;
    					}
    				}
		        }
		        
				if (vid!=null)
				{
					mDialog_SPINNER_scene.setMessage(getString(R.string.dialog_message_wait));
					mDialog_SPINNER_scene.show();
					
					SetSceneValueCommand("addvalue",scene_id,vid,value);
					
					PlayButtonSound.play(ActivityNodeList.this);
				}
				
		    }
			else
			{
				boolean have_dimmer=false;
		        
		        if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch binary"))
		        {
		            if (zvalue.label.toLowerCase().equalsIgnoreCase("switch"))
		            {
		            	final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;						
						sendCommand(vid,value_ToChange(zvalue.current.toLowerCase()),znode.id);
		            }
		        }
		        else if (zvalue.class_c.equalsIgnoreCase("switch multilevel"))
				{
					if (zvalue.label.equalsIgnoreCase("level"))
					{
						final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;						
    					sendCommand(vid,value_ToChange(zvalue.current.toLowerCase()),znode.id);
    					
    					have_dimmer=true;
					}
				}
		        else if (zvalue.class_c.equalsIgnoreCase("alarm"))
				{
					if (zvalue.label.equalsIgnoreCase("mode"))
					{
						String value_str=zvalue.current.toLowerCase().equalsIgnoreCase("arm") ? "Bypass" : "Arm";
						
						final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;						
    					sendCommand(vid,value_str,znode.id);
		            }
		        }        
		        else if (znode.gtype.toLowerCase().indexOf("lock")!=-1)
				{
		        	ZWaveNode znode_o = new ZWaveNode(room_node_array.get(page));
					
					((SharedClassApp)(ActivityNodeList.this.getApplication())).setZWaveNode(znode_o);
					
		        	Intent intent = new Intent();
					
					intent.setClass(ActivityNodeList.this, ActivityDoorLock.class);
					startActivity(intent);
				}
			}
		}
	};
		
	private String value_ToChange(final String value)
	{
		if (value.indexOf("unsecured")!=-1)
			return "Secured";
		else if (value.indexOf("secured")!=-1)
			return "Unsecured";
		else if (value.equalsIgnoreCase("true"))
			return "False";
		else if (value.equalsIgnoreCase("false"))
			return "True";
		else if (value.equalsIgnoreCase("0"))
			return "99";
		else if (value.equalsIgnoreCase("99"))
			return "0";
		
		return "0";
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if(intent.getAction().equals(ServicePolling.HTTP_401))
			{
				DialogSetAuth.show(ActivityNodeList.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivityNodeList.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivityNodeList.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
				clearList();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
				GetGatewayStatus();
				
				if (!Touch)
					attemptGetData();
			}
		}
	};

	private void GetGatewayStatus()
	{
		//±µ¦¬ gateway ª¬ºA
		
		if (!((SharedClassApp)(ActivityNodeList.this.getApplication())).isActive())
		{
			mDialog_SPINNER_status.dismiss();
		}
		else
		{
			String State=((SharedClassApp)(ActivityNodeList.this.getApplication())).getControllerState();
			
			String[] array = State.split(":");
			
			if (array.length >=2)
			{
				Log.v(TAG,array[0]);
				Log.v(TAG,array[1]);
			}
			String title =array[0];//array[0].replaceAll("(", "").replaceAll(")", "").replaceAll("1", "").replaceAll("2", "").replaceAll("3", "").replaceAll("4", "").replaceAll("5", "").replaceAll("6", "").replaceAll("7", "").replaceAll("8", "").replaceAll("9", "");

			String msg   =array[1];//array[1].replaceAll("(", "").replaceAll(")", "").replaceAll("1", "").replaceAll("2", "").replaceAll("3", "").replaceAll("4", "").replaceAll("5", "").replaceAll("6", "").replaceAll("7", "").replaceAll("8", "").replaceAll("9", "");
			
			//Log.v(TAG,title);
			
			mDialog_SPINNER_status.setTitle(title);
			mDialog_SPINNER_status.setMessage(msg);
			mDialog_SPINNER_status.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.alert_button_cancel),process_down);
			mDialog_SPINNER_status.show();
		}
	}
	
	private DialogInterface.OnClickListener  process_down = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
			sendCancelCommand();
		}
	};
	
	private void sendCancelCommand()
	{
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"admpost.html","fun","cancel"});
	}
	
	private class SendCommandTask extends AsyncTask<String, Void, Boolean> 
	{
		@Override
		protected void onPreExecute() 
		{
			mDialog_SPINNER_status.setMessage(getString(R.string.dialog_message_wait));
			mDialog_SPINNER_status.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... args) 
		{
			boolean success = false;
			CusPreference cp = new CusPreference(ActivityNodeList.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(args[1], args[2]);
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getUserName());
				map.put("userpwd", cp.getUserPwd());
				//map.put("tunnelid", "0");
			}
			
			success = SendHttpCommand.send(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+args[0],
					map, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed());
			Log.v(TAG,"success="+success);
			return success;
		}

		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mDialog_SPINNER_status.dismiss();
			
			if (success) {
				Toast.makeText(ActivityNodeList.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ActivityNodeList.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
			}
			
			mSendCommandTask = null;
		}

		@Override
		protected void onCancelled() 
		{
			mDialog_SPINNER_status.dismiss();
			super.onCancelled();
		}
	}
	
	private void clearList()
	{
		mNodes.clear();
		adapter.notifyDataSetChanged();
	}
	
	private void attemptGetData()
	{
		mDialog_SPINNER.dismiss();
				
		ZWaveNode znode_o = new ZWaveNode(room_node_array.get(page));
		
		node=Node_id_to_node(znode_o.id);
		
		ZWaveNode znode = new ZWaveNode(node);
		
		node_user_value.clear();
		
		ArrayList<HashMap<String,Object>> node_values=(ArrayList<HashMap<String, Object>>) node.get("value");
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.genre.toLowerCase().equalsIgnoreCase("user") && zvalue.class_c.toLowerCase().equalsIgnoreCase("switch binary") && zvalue.label.toLowerCase().equalsIgnoreCase("switch"))
                node_user_value.add(value);
		}
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.genre.toLowerCase().equalsIgnoreCase("user") && zvalue.class_c.toLowerCase().equalsIgnoreCase("switch multilevel") && zvalue.label.toLowerCase().equalsIgnoreCase("level"))
                node_user_value.add(value);
		}
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.genre.toLowerCase().equalsIgnoreCase("user") && zvalue.class_c.toLowerCase().equalsIgnoreCase("door lock") && zvalue.label.toLowerCase().equalsIgnoreCase("mode"))
                node_user_value.add(value);
		}
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.class_c.toLowerCase().equalsIgnoreCase("basic") && znode.gtype.toLowerCase().indexOf("motor")!=-1)
                node_user_value.add(value);
		}
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.genre.toLowerCase().equalsIgnoreCase("user") && zvalue.class_c.toLowerCase().equalsIgnoreCase("alarm") && zvalue.label.toLowerCase().equalsIgnoreCase("mode"))
                node_user_value.add(value);
		}
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.genre.toLowerCase().equalsIgnoreCase("user") && zvalue.class_c.toLowerCase().equalsIgnoreCase("alarm") && zvalue.label.toLowerCase().equalsIgnoreCase("alarm level"))
			{
                node_user_value.add(value);
                hasAlarm=true;
			}
		}
		
		if (!hasAlarm)
		{
			for(HashMap<String,Object> value : node_values)
			{
				ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
				if (zvalue.genre.toLowerCase().equalsIgnoreCase("user") && zvalue.class_c.toLowerCase().equalsIgnoreCase("sensor binary") )
	                node_user_value.add(value);
			}
		}
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.genre.toLowerCase().equalsIgnoreCase("user") && zvalue.class_c.toLowerCase().equalsIgnoreCase("sensor multilevel") )
                node_user_value.add(value);
		}
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			if (zvalue.genre.toLowerCase().equalsIgnoreCase("user"))
			{
				if ( 	(zvalue.class_c.toLowerCase().equalsIgnoreCase("meter") && zvalue.label.toLowerCase().equalsIgnoreCase("energy")) ||
						(zvalue.class_c.toLowerCase().equalsIgnoreCase("meter") && zvalue.label.toLowerCase().equalsIgnoreCase("voltage")) ||
						(zvalue.class_c.toLowerCase().equalsIgnoreCase("meter") && zvalue.label.toLowerCase().equalsIgnoreCase("power")) ||
						(zvalue.class_c.toLowerCase().equalsIgnoreCase("meter") && zvalue.label.toLowerCase().equalsIgnoreCase("current")) ||
						(zvalue.class_c.toLowerCase().equalsIgnoreCase("meter") && zvalue.label.toLowerCase().equalsIgnoreCase("power factor")) )
				{
					node_user_value.add(value);
				}
			}
		}
		
		
		txt_name.setText(znode.name_fix);
		txt_loc.setText(znode.location);
		
		layout_power.setVisibility(View.INVISIBLE);
		
		for(HashMap<String,Object> value : node_values)
		{
			ZWaveNodeValue zvalue= new ZWaveNodeValue(value);
			
			if (zvalue.class_c.toLowerCase().equalsIgnoreCase("battery"))
			{
				if (zvalue.label.toLowerCase().equalsIgnoreCase("battery level"))
				{
					layout_power.setVisibility(View.VISIBLE);
					
					int bty=Integer.parseInt(zvalue.current);
            		
					img_power.setImageDrawable(Battery_to_image(bty));
					txt_power.setTextColor(Battery_to_color(bty).equals("black") ? Color.BLACK : Color.RED);
					txt_power.setText(zvalue.current+zvalue.units);
				}
			}
		}
		
		if (znode.gtype.toLowerCase().equalsIgnoreCase("static pc controller") || znode.gtype.toLowerCase().indexOf("sensor") !=-1 || znode.gtype.toLowerCase().indexOf("repeater") !=-1 )
        {
			if (znode.gtype.equalsIgnoreCase("static pc controller") || znode.gtype.toLowerCase().indexOf("repeater") !=-1)
        	{
        		img_icon.setImageResource(R.drawable.gw);
        		
        		if (!znode.icon.equalsIgnoreCase(""))
                {
        			String path=label_to_path_label(znode.icon,"normal");
        			
        			Bitmap bitmap = BitmapFactory.decodeFile(path);
        			
        			if (bitmap!=null)
        				img_icon.setImageBitmap(bitmap);
                }
        	}
			else
        	{
        		int default_image=-1;
				
				if (znode.name_fix.toLowerCase().indexOf("pir")!=-1 || znode.product.toLowerCase().indexOf("pir")!=-1)
					default_image=R.drawable.iroff;
				else if (znode.name_fix.toLowerCase().indexOf("motion")!=-1 || znode.product.toLowerCase().indexOf("motion")!=-1)
					default_image=R.drawable.iroff;
				else if (znode.name_fix.toLowerCase().indexOf("door")!=-1 || znode.product.toLowerCase().indexOf("door")!=-1)
					default_image=R.drawable.dooroff;
				else if (znode.name_fix.toLowerCase().indexOf("window")!=-1 || znode.product.toLowerCase().indexOf("window")!=-1)
					default_image=R.drawable.windowoff;
				else if (znode.name_fix.toLowerCase().indexOf("co")!=-1 || znode.product.toLowerCase().indexOf("co")!=-1)
					default_image=R.drawable.co_off;
				else if (znode.name_fix.toLowerCase().indexOf("smoke")!=-1 || znode.product.toLowerCase().indexOf("smoke")!=-1)
					default_image=R.drawable.smork_off;
				else if (znode.name_fix.toLowerCase().indexOf("shock")!=-1 || znode.product.toLowerCase().indexOf("shock")!=-1)
					default_image=R.drawable.shock_off;
				else if (znode.name_fix.toLowerCase().indexOf("water")!=-1 || znode.product.toLowerCase().indexOf("water")!=-1)
					default_image=R.drawable.water_off;
				else
					default_image=R.drawable.shock_off;
				
				String path=label_to_path_label(znode.icon,"off");
        		Bitmap bitmap = BitmapFactory.decodeFile(path);
        			
        		if (bitmap!=null)
        			img_icon.setImageBitmap(bitmap);
        		else
        			img_icon.setImageResource(default_image);   
        	}
        }
		else
    	{
        	int default_image=-1;
        	
        	if (znode.name_fix.toLowerCase().indexOf("siren")!=-1 || znode.product.toLowerCase().indexOf("siren")!=-1)
        		default_image=R.drawable.alertoff;
        	else if (znode.name_fix.toLowerCase().indexOf("door lock")!=-1 || znode.product.toLowerCase().indexOf("door lock")!=-1)
        		default_image=R.drawable.doorkeypad_off;
        	else
        		default_image=R.drawable.dimmer_light_off;
        	
        	String path=label_to_path_label(znode.icon,"off");
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			
			if (bitmap!=null)
				img_icon.setImageBitmap(bitmap);
			else
				img_icon.setImageResource(default_image);
    	}

		adapter.notifyDataSetChanged();
	}
	
	public class MyCustomAdapter extends ArrayAdapter<HashMap<String,Object>>
    { 
    	ViewHolder viewHolder;
    	 private LayoutInflater inflater; 

    	public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String,Object>> arrayListItems) 
    	{
    		super(context, textViewResourceId, arrayListItems);
    		 inflater = LayoutInflater.from(context);
        }
        
        private class ViewHolder 
        {
        	//Switch
        	RelativeLayout 	switchlayout;
        	ImageView 		switchlayout_icon_image;
        	TextView 		switchlayout_nickname_text;
        	TextView 		switchlayout_status_text;
        	
        	//Dimmer
        	RelativeLayout 	dimmerlayout;
        	ImageView		dimmerlayout_icon_image;
        	TextView		dimmerlayout_nickname_text;
        	TextView		dimmerlayout_seek_text;
        	SeekBar  		dimmerlayout_seekbar;

        	//Sensor
        	RelativeLayout 	sensorlayout;
        	ImageView 		sensorlayout_icon_image;
        	TextView 		sensorlayout_nickname_text;
        	TextView 		sensorlayout_status_text;

           	//cover
           	RelativeLayout 	coverlayout;
        	ImageButton		coverlayout_up;
        	ImageButton		coverlayout_down;
        	ImageButton		coverlayout_stop;
        }

    	@Override 
    	public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_node,null);
            	
        		viewHolder = new ViewHolder();

        		viewHolder.switchlayout    				= (RelativeLayout) convertView.findViewById(R.id.switchlayout);
        		viewHolder.switchlayout_icon_image     = (ImageView) convertView.findViewById(R.id.switchlayout_icon_image);
        		viewHolder.switchlayout_nickname_text 	= (TextView)  convertView.findViewById(R.id.switchlayout_nickname_text);
        		viewHolder.switchlayout_status_text		= (TextView) convertView.findViewById(R.id.switchlayout_status_text);
        		
        		viewHolder.dimmerlayout    				= (RelativeLayout) convertView.findViewById(R.id.dimmerlayout);
        		viewHolder.dimmerlayout_icon_image     	= (ImageView) convertView.findViewById(R.id.dimmerlayout_icon_image);
        		viewHolder.dimmerlayout_nickname_text  	= (TextView)  convertView.findViewById(R.id.dimmerlayout_nickname_text);
        		viewHolder.dimmerlayout_seek_text  		= (TextView)  convertView.findViewById(R.id.dimmerlayout_seek_text);
        		viewHolder.dimmerlayout_seekbar			= (SeekBar) convertView.findViewById(R.id.dimmerlayout_seekbar);
        		
        		viewHolder.sensorlayout    				= (RelativeLayout) convertView.findViewById(R.id.sensorlayout);
        		viewHolder.sensorlayout_icon_image		= (ImageView) convertView.findViewById(R.id.sensorlayout_icon_image);
        		viewHolder.sensorlayout_nickname_text  	= (TextView)  convertView.findViewById(R.id.sensorlayout_nickname_text);
        		viewHolder.sensorlayout_status_text   	= (TextView) convertView.findViewById(R.id.sensorlayout_status_text);
        		
        		viewHolder.coverlayout    				= (RelativeLayout) convertView.findViewById(R.id.coverlayout);
        		viewHolder.coverlayout_up				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_up);
        		viewHolder.coverlayout_down				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_down);
        		viewHolder.coverlayout_stop				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_stop);
	
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();

            if (node!=null)
            {
            	ZWaveNode znode = new ZWaveNode(node);
                HashMap<String,Object> node_value=node_user_value.get(position);            
                ZWaveNodeValue zvalue=new ZWaveNodeValue(node_value);
                
                if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch binary"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.VISIBLE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	String status=zvalue.current.equalsIgnoreCase("false") ? "off" : "on";
                    
                	int default_image=-1;
                	
                	if (znode.name_fix.toLowerCase().indexOf("siren")!=-1 || znode.product.toLowerCase().indexOf("siren")!=-1)
                		default_image=status.equalsIgnoreCase("on") ? R.drawable.alerton : R.drawable.alertoff;
                	else
                		default_image=status.equalsIgnoreCase("on") ? R.drawable.dimmer_light_on : R.drawable.dimmer_light_off;
                	
                	String sts_text=getString(R.string.device_status)+" : " + (status.equals("off") ? getString(R.string.device_off) : getString(R.string.device_on));
                	
                	viewHolder.switchlayout_status_text.setText(sts_text);
                	
                	viewHolder.switchlayout_icon_image.setImageResource(default_image);
                	
                	int have_multi=0;
        			
                	for(ZWaveNodeValue zvalue_o : znode.value)
                	{
                		if (zvalue_o.units.toLowerCase().equalsIgnoreCase("icon")  && zvalue_o.class_c.equalsIgnoreCase(zvalue.class_c) && zvalue_o.instance.equalsIgnoreCase(zvalue.instance))
                		{
                			if (znode.gtype.toLowerCase().indexOf("motor")==-1)
                			{
                				String path=label_to_path_label(zvalue_o.current,status);
                    			Bitmap bitmap = BitmapFactory.decodeFile(path);

                    			if (bitmap!=null)
                    				viewHolder.switchlayout_icon_image.setImageBitmap(bitmap);
                			}
                		}
        				
        				if (zvalue_o.class_c.toLowerCase().equalsIgnoreCase("switch binary"))
                        {
                            if (zvalue_o.label.toLowerCase().equalsIgnoreCase("switch"))
                            {
                                have_multi++;
                            }
                        }
                	}
        			
        			if (have_multi>1)
                    {
                        int aa=Integer.valueOf(zvalue.instance);
                        viewHolder.switchlayout_nickname_text.setText(getString(R.string.node_switch)+"["+String.valueOf(aa)+"]");
                    }
        			else 
        				viewHolder.switchlayout_nickname_text.setVisibility(View.GONE);
        			
        			return convertView;
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch multilevel"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.VISIBLE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	String status=zvalue.current.equalsIgnoreCase("0") ? "off" : "on";
                    
                    int default_image=zvalue.current.equalsIgnoreCase("0")? R.drawable.dimmer_light_off : R.drawable.dimmer_light_on;
                    
                    viewHolder.dimmerlayout_icon_image.setImageResource(default_image);
                    
                    int have_multi=0;
        			
                	for(ZWaveNodeValue zvalue_o : znode.value)
                	{
        				if (zvalue_o.units.toLowerCase().equalsIgnoreCase("icon")  && zvalue_o.class_c.equalsIgnoreCase(zvalue.class_c) && zvalue_o.instance.equalsIgnoreCase(zvalue.instance))
                		{
                			if (znode.gtype.toLowerCase().indexOf("motor")==-1)
                			{
                				String path=label_to_path_label(zvalue_o.current,status);
                    			Bitmap bitmap = BitmapFactory.decodeFile(path);

                    			if (bitmap!=null)
                    				viewHolder.dimmerlayout_icon_image.setImageBitmap(bitmap);
                			}
                		}
        				
        				if (zvalue_o.class_c.toLowerCase().equalsIgnoreCase("switch multilevel"))
                        {
                            if (zvalue_o.label.toLowerCase().equalsIgnoreCase("level"))
                            {
                                have_multi++;
                            }
                        }
                	}
        			
        			if (have_multi>1)
                    {
                        int aa=Integer.valueOf(zvalue.instance);
                        viewHolder.dimmerlayout_nickname_text.setText(getString(R.string.node_dimmer)+"["+String.valueOf(aa)+"]");
                    }
        			else 
        				viewHolder.dimmerlayout_nickname_text.setVisibility(View.GONE);
        			
        			if (!Touch)
					{
						Integer Value=Integer.valueOf(zvalue.current);
						viewHolder.dimmerlayout_seekbar.setProgress(Value);
						viewHolder.dimmerlayout_seek_text.setText(zvalue.current+"%");
					}

	            	viewHolder.dimmerlayout_seekbar.setTag(position);
	                viewHolder.dimmerlayout_seekbar.setOnSeekBarChangeListener(Seek_down);
	                
	                return convertView;
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("sensor binary") || (zvalue.class_c.toLowerCase().equalsIgnoreCase("alarm") && zvalue.label.toLowerCase().equalsIgnoreCase("alarm level")))
                {
                	viewHolder.sensorlayout.setVisibility(View.VISIBLE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout.setBackgroundResource(R.drawable.selector_node_item_sensor);
                	
                	String status;
                	
                	if (hasAlarm)
                		status=zvalue.current.equalsIgnoreCase("0") ? "off" : "on";
                	else
                		status=zvalue.current.equalsIgnoreCase("false") ? "off" : "on";
                    
                	int default_image=-1;
                	
                	if (znode.name_fix.toLowerCase().indexOf("pir")!=-1 || znode.product.toLowerCase().indexOf("pir")!=-1)
                		default_image=status.equals("off") ? R.drawable.iroff : R.drawable.iron;
    				else if (znode.name_fix.toLowerCase().indexOf("motion")!=-1 || znode.product.toLowerCase().indexOf("motion")!=-1)
    					default_image=status.equals("off") ? R.drawable.iroff : R.drawable.iron;
					else if (znode.name_fix.toLowerCase().indexOf("door")!=-1 || znode.product.toLowerCase().indexOf("door")!=-1)
						default_image=status.equals("off") ? R.drawable.dooroff : R.drawable.dooron;
					else if (znode.name_fix.toLowerCase().indexOf("window")!=-1 || znode.product.toLowerCase().indexOf("window")!=-1)
						default_image=status.equals("off") ? R.drawable.windowoff : R.drawable.windowon;
					else if (znode.name_fix.toLowerCase().indexOf("co")!=-1 || znode.product.toLowerCase().indexOf("co")!=-1)
						default_image=status.equals("off") ? R.drawable.co_off : R.drawable.co_on;
					else if (znode.name_fix.toLowerCase().indexOf("smoke")!=-1 || znode.product.toLowerCase().indexOf("smoke")!=-1)
						default_image=status.equals("off") ? R.drawable.smork_off : R.drawable.smork_on;
					else if (znode.name_fix.toLowerCase().indexOf("shock")!=-1 || znode.product.toLowerCase().indexOf("shock")!=-1)
						default_image=status.equals("off") ? R.drawable.shock_off : R.drawable.shock_on;
					else if (znode.name_fix.toLowerCase().indexOf("water")!=-1 || znode.product.toLowerCase().indexOf("water")!=-1)
						default_image=status.equals("off") ? R.drawable.water_off : R.drawable.water_on;
					else
						default_image=status.equals("off") ? R.drawable.shock_off : R.drawable.shock_on;
                	
                	String sts_text=getString(R.string.device_status)+" : " + (status.equals("off") ? getString(R.string.device_off) : getString(R.string.device_on));
                	
                	viewHolder.sensorlayout_status_text.setText(sts_text);
                	
                	viewHolder.sensorlayout_icon_image.setImageResource(default_image);
                	
                	viewHolder.sensorlayout_nickname_text.setVisibility(View.VISIBLE);
                	
                	viewHolder.sensorlayout_nickname_text.setText(zvalue.label);
                	
                	for(ZWaveNodeValue zvalue_o : znode.value)
                	{
        				if (zvalue_o.units.toLowerCase().equalsIgnoreCase("icon")  && zvalue_o.class_c.equalsIgnoreCase(zvalue.class_c) && zvalue_o.instance.equalsIgnoreCase(zvalue.instance))
                		{
                			if (znode.gtype.toLowerCase().indexOf("motor")==-1)
                			{
                				String path=label_to_path_label(zvalue_o.current,status);
                    			Bitmap bitmap = BitmapFactory.decodeFile(path);

                    			if (bitmap!=null)
                    				viewHolder.sensorlayout_icon_image.setImageBitmap(bitmap);
                			}
                		}
                	}
                	
                	return convertView;
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("sensor multilevel") || zvalue.class_c.toLowerCase().equalsIgnoreCase("meter"))
                {
                	viewHolder.sensorlayout.setVisibility(View.VISIBLE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout.setBackgroundResource(R.drawable.selector_node_item_sensor);
                	
                	viewHolder.sensorlayout_nickname_text.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout_status_text.setText(zvalue.label+" : "+zvalue.current+zvalue.units);
    				
    				if (zvalue.label.toLowerCase().indexOf("temp")!=-1)
    				{
    					viewHolder.sensorlayout_status_text.setText(zvalue.label+" : "+zvalue.current+"\u00B0"+zvalue.units);
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.temp);
    				}
    				else if (zvalue.label.toLowerCase().indexOf("luminance")!=-1)
    				{
    					viewHolder.sensorlayout_status_text.setText(zvalue.label+" : "+zvalue.current+"\u00B0"+zvalue.units);
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.brightness);
    				}
    				else if (zvalue.label.toLowerCase().indexOf("humidity")!=-1)
    				{
    					viewHolder.sensorlayout_status_text.setText(zvalue.label+" : "+zvalue.current+"\u00B0"+zvalue.units);
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.humidity);
    				}
    				else if (zvalue.label.toLowerCase().indexOf("power factor")!=-1)
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.power_factor);
    				else if (zvalue.label.toLowerCase().indexOf("energy")!=-1)
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.power);
    				else if (zvalue.label.toLowerCase().indexOf("power")!=-1)
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.wattage);
    				else if (zvalue.label.toLowerCase().indexOf("voltage")!=-1)
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.voltage);
    				else if (zvalue.label.toLowerCase().indexOf("current")!=-1)
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.current);
    				else
    					viewHolder.sensorlayout_icon_image.setImageResource(R.drawable.shock_off);
    				
                	return convertView;
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("alarm") && zvalue.label.toLowerCase().equalsIgnoreCase("mode"))
                {
                	viewHolder.sensorlayout.setVisibility(View.VISIBLE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout.setBackgroundResource(R.drawable.selector_node_item_arm);
                	
                	viewHolder.sensorlayout_nickname_text.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout_status_text.setText(zvalue.label+" : "+zvalue.current+zvalue.units);
                	
                	String status=zvalue.current.equalsIgnoreCase("arm") ? "on" : "off";
                	
                	int default_image=status.equals("off") ? R.drawable.unsafe : R.drawable.safe;
                	
                	viewHolder.sensorlayout_icon_image.setImageResource(default_image);
                	
                	return convertView;
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("door lock"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.VISIBLE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	String status=zvalue.current.toLowerCase().indexOf("unsecured")!=-1 ? "off" : "on";
                	int default_image=zvalue.current.toLowerCase().indexOf("unsecured")!=-1 ? R.drawable.doorkeypad_off : R.drawable.doorkeypad_on;
        			
                	String sts_text=getString(R.string.device_status)+" : " + (status.equals("off") ? getString(R.string.device_unlock) : getString(R.string.device_lock));
                	viewHolder.switchlayout_status_text.setText(sts_text);
                	
                	viewHolder.switchlayout_icon_image.setImageResource(default_image);
                	
                	for(ZWaveNodeValue zvalue_o : znode.value)
                	{
        				if (zvalue_o.units.toLowerCase().equalsIgnoreCase("icon")  && zvalue_o.class_c.equalsIgnoreCase(zvalue.class_c) && zvalue_o.instance.equalsIgnoreCase(zvalue.instance))
                		{
        					String path=label_to_path_label(zvalue_o.current,status);
                			Bitmap bitmap = BitmapFactory.decodeFile(path);

                			if (bitmap!=null)
                				viewHolder.switchlayout_icon_image.setImageBitmap(bitmap);
                		}
                	}
                	
                	viewHolder.switchlayout_nickname_text.setVisibility(View.GONE);
                	
                	return convertView;
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("basic"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.VISIBLE);
                	
                	viewHolder.coverlayout_up.setTag(position);
	                viewHolder.coverlayout_down.setTag(position);
	                viewHolder.coverlayout_stop.setTag(position);
	                viewHolder.coverlayout_down.setOnClickListener(cover_button_down);
	                viewHolder.coverlayout_stop.setOnClickListener(cover_button_down);
	                viewHolder.coverlayout_up.setOnClickListener(cover_button_down);
	                
	                return convertView;
                }
            }
            
            viewHolder.sensorlayout.setVisibility(View.GONE);
        	viewHolder.switchlayout.setVisibility(View.GONE);
        	viewHolder.dimmerlayout.setVisibility(View.GONE);
        	viewHolder.coverlayout.setVisibility(View.GONE);
        	
            return convertView;
    	}
    }

//--------------Button down-------------------//
	
	private Button.OnClickListener tab_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer) v.getTag())
			{
				case 1:
				{
					finish();
					break;
				}
				case 2:
				{
					Edit=!Edit;
					edit.setSelected(Edit);

					ZWaveNode znode_o = new ZWaveNode(room_node_array.get(page));
					
					((SharedClassApp)(ActivityNodeList.this.getApplication())).setZWaveNode(znode_o);
					
					Log.v(TAG,"id=="+znode_o.id);
					
					Intent intent = new Intent();
					
					Bundle bundle= new Bundle();
	        		bundle.putSerializable("location", location);
	        		intent.putExtras(bundle);
	        		
					intent.setClass(ActivityNodeList.this, ActivityNodeEdit.class);
					startActivity(intent);
					
					break;
				}

				default:
					break;
			}
			
			PlayButtonSound.play(ActivityNodeList.this);
		}
	};
	
	private Button.OnClickListener cover_button_down=new Button.OnClickListener()
	{
		@Override
		public void onClick(View view) 
		{
			ZWaveNode znode = new ZWaveNode(node);
			
			for(int i=0; i<znode.value.size(); i++)
        	{
    			ZWaveNodeValue znode_value = znode.value.get(i);
    			
    			if (view.getId()==R.id.coverlayout_control_stop)
    			{
    				if (znode_value.class_c.equalsIgnoreCase("switch multilevel"))
        			{
        				if (znode_value.label.equalsIgnoreCase("stopchange"))
    	    			{
        					final String vid = znode.id+"-"+znode_value.class_c+"-"+znode_value.genre+"-"+znode_value.type+"-"+znode_value.instance+"-"+znode_value.index;
        					sendCommand(vid,"True",znode.id);
        					break;
    	    			}
        			}
    			}
    			else
    			{
    				if (znode_value.class_c.equalsIgnoreCase("basic"))
        			{
        				if (znode_value.label.equalsIgnoreCase("basic"))
    	    			{
        					final String vid = znode.id+"-"+znode_value.class_c+"-"+znode_value.genre+"-"+znode_value.type+"-"+znode_value.instance+"-"+znode_value.index;
        					
        					if (view.getId()==R.id.coverlayout_control_down)
        						sendCommand(vid,"0",znode.id);
        					else if (view.getId()==R.id.coverlayout_control_up)
        						sendCommand(vid,"255",znode.id);
        					break;
    	    			}
        			}
    			}
        	}
		}
	};
	
	private SeekBar.OnSeekBarChangeListener Seek_down=new SeekBar.OnSeekBarChangeListener()
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
		{
			SeekStr=String.valueOf(progress);
			seekbar_text.setText(SeekStr+"%");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) 
		{
			Touch=true;
			seekbar_text.setVisibility(View.VISIBLE);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) 
		{	
			ZWaveNode znode = new ZWaveNode(node);
			
			HashMap<String,Object> value=node_user_value.get((Integer)seekBar.getTag());
			
			ZWaveNodeValue zvalue=new ZWaveNodeValue(value);
			
			if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch multilevel"))
			{
				if (zvalue.label.toLowerCase().equalsIgnoreCase("level"))
				{
					final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
					sendCommand(vid,String.valueOf(seekBar.getProgress()),znode.id);
					seekbar_text.setVisibility(View.GONE);
				}
			}
		}
	};
	
//--------------Other Function-------------------//
	private Drawable Battery_to_image(Integer bty)
	{
		Drawable draw;
		if (bty>80)
			draw = getResources().getDrawable(R.drawable.battery_5);
    	else if (bty>60)
    		draw = getResources().getDrawable(R.drawable.battery_4);
    	else if (bty>40)
    		draw = getResources().getDrawable(R.drawable.battery_3);
    	else if (bty>20)
    		draw = getResources().getDrawable(R.drawable.battery_2);
    	else 
    		draw = getResources().getDrawable(R.drawable.battery_1);
		
		return draw;
	}
	
	private String Battery_to_color(Integer bty)
	{
		String color;
		
		if (bty>80)
			color="black";
    	else if (bty>60)
    		color="black";
    	else if (bty>40)
    		color="black";
    	else if (bty>20)
    		color="red";
    	else 
    		color="red";
		
		return color;
	}
	
	private HashMap<String,Object> Node_id_to_node(final String node_id)
	{
		ArrayList<HashMap<String,Object>> nodes= new ArrayList<HashMap<String,Object>>();
		((SharedClassApp)(ActivityNodeList.this.getApplication())).refreshNodesList(nodes);
		
		for (HashMap<String,Object> node : nodes)
		{
			ZWaveNode znode=new ZWaveNode(node);
			
			if (znode.id.equals(node_id))
				return node;
		}
		
		return null;
	}
	
//--------------Http Send Command-------------------//	
	
	private void sendCommand(final String vid , final String action , final String id)
	{
		
		//mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
		//mDialog_SPINNER.show();
		Log.v(TAG,"vid "+vid+"="+action);
		new Thread()
		{
			public void run()
			{
				boolean success = false;
				CusPreference cp = new CusPreference(ActivityNodeList.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put(vid , action);
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", "admin");
					map.put("userpwd", "admin");
				}
				
				success = SendHttpCommand.send(String.format(cp.isLocalUsed()?getString(R.string.local_url_syntax):getString(R.string.server_url_syntax),cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"valuepost.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
				
				if (success)
				{
					Refresh(id);
				}
				Touch=false;
				//mDialog_SPINNER.dismiss();
			}
		}.start();
		
		Toast.makeText(ActivityNodeList.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
		
		/*
		if(mSendValueTask!=null){
			return;
		}
		
		mSendValueTask = new SendValueTask();
		mSendValueTask.execute(new String[]{"valuepost.html",vid,action,id});
		*/
	}
	
	/*
	private class SendValueTask extends AsyncTask<String, Void, Boolean> 
	{
		@Override
		protected void onPreExecute() 
		{
			//mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
			//mDialog_SPINNER.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... args) 
		{
			boolean success = false;
			CusPreference cp = new CusPreference(ActivityNodeList.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(args[1], args[2]);
			if(!cp.isLocalUsed())
			{
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getUserName());
				map.put("userpwd", cp.getUserPwd());
				map.put("tunnelid", "0");
			}
			
			//Log.v(TAG,String.format(cp.isLocalUsed()?getString(R.string.local_url_syntax):getString(R.string.server_url_syntax),cp.isLocalUsed()?cp.getControllerIP():getString(R.string.server_ip),cp.isLocalUsed()?String.valueOf(cp.getControllerPort()):getString(R.string.server_port))+args[0]);
			//Log.v(TAG,"map="+map);
			
			success = SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+args[0],
					map, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed());
			
			if (success)
				Refresh(args[3]);
			Log.v(TAG,"success="+success);
			return success;
		}

		@Override
		protected void onPostExecute(final Boolean success) 
		{
			//mDialog_SPINNER.dismiss();
			
			if (!success)
				//Toast.makeText(ActivityNodeList.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			//else
				Toast.makeText(ActivityNodeList.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
			
			Touch=false;
			mSendValueTask = null;
		}
	}
	*/
	private void SetSceneValueCommand(final String fun , final String id , final String vid , final String value)
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityNodeList.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("fun",fun);
				map.put("id",id);
				map.put("vid",vid);
				map.put("value",value);
				
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
				}
				
				SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"scenepost.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
				
				//Message message = handler.obtainMessage(1,null);
				//handler.sendMessage(message);
				
				mDialog_SPINNER_scene.dismiss();
				finish();
			}
		}.start();
	}

	private void Refresh(final String id)
	{
		try 
		{
			Thread.sleep(4000);
			
			Log.v(TAG,"Refresh");
			
			new Thread()
			{
				public void run()
				{
					CusPreference cp = new CusPreference(ActivityNodeList.this);
					
					Map<String, String> map = new HashMap<String,String>();
					
					map.put("fun","racp");
					map.put("node",id);
					
					if(!cp.isLocalUsed())
					{
						map.put("mac", cp.getControllerMAC());
						map.put("username", cp.getUserName());
						map.put("userpwd", cp.getUserPwd());
						//map.put("tunnelid", "0");
					}
					
					SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"refreshpost.html",
							map, 
							cp.getUserName(), 
							cp.getUserPwd(), 
							cp.isLocalUsed());
				}
			}.start();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	private String label_to_path_label (String label , String status)
	{
		CusPreference cp = new CusPreference(ActivityNodeList.this);
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
}
