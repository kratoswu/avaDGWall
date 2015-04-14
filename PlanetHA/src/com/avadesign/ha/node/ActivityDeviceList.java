package com.avadesign.ha.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;

@SuppressLint("NewApi")
public class ActivityDeviceList extends BaseActivity 
{
	private ListView mNode_list;
	
	private MyCustomAdapter adapter;
	
	private ArrayList<HashMap<String,Object>> mNodes_ready;
	
	private ArrayList<ArrayList<HashMap<String,Object>>> mNodes;
	
	private ArrayList<HashMap<String,Object>> mNode_section;
	
	private Button back,edit;
	
	private ProgressDialog mDialog_SPINNER_status;
	
	private RadioGroup radioGroup;

	//private SendValueTask mSendValueTask;
	private SendCommandTask mSendCommandTask;
	
	private Boolean Edit=false;
	
	private int page;
	
	private ArrayList<HashMap<String,String>> location;
	
	private HorizontalScrollView scrolliew_room;
	private LinearLayout scrolliew_room_layout;
	
	private ArrayList<ImageButton> room_bt_ary;
	
	private TextView seekbar_text;
	
	private boolean init_scroll;
	
	private Integer radio_count;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_list);

		RegisterBroadcast();
		
		StartService();
		
		mDialog_SPINNER_status = new ProgressDialog(this);
		mDialog_SPINNER_status.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		Bundle bundle=this.getIntent().getExtras();
		page=bundle.getInt("page");
		
		//Log.v(TAG,"page="+page);
		
		location=(ArrayList<HashMap<String, String>>) bundle.getSerializable("location");
		
		init_scroll=false;
		
		radio_count=0;
		
		FindView();
		
		Setlistener();	
	}
	
	@Override
	protected void onResume() 
	{
		((SharedClassApp)(ActivityDeviceList.this.getApplication())).setZWaveNode(null);
		
		cp.setStopPolling(false);
		
		Edit=false;	
		edit.setSelected(false);
    	edit.setVisibility(cp.getControllerAcc().equals("admin") ? View.VISIBLE : View.GONE);
    	
		adapter.notifyDataSetChanged();

		if (!init_scroll)
			initScroll();
		
		callProgress();
		mDialog_SPINNER.setCancelable(false);
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		UnRegisterBroadcast();
		
		StopService();
		
		super.onDestroy();
	}
	
	private void FindView()
	{
		mNode_list = (ListView)findViewById(R.id.listview);
		
		back= (Button)this.findViewById(R.id.tab_back);
		edit= (Button)this.findViewById(R.id.tab_edit);
		
		scrolliew_room=(HorizontalScrollView)this.findViewById(R.id.scrollview_room);
		scrolliew_room_layout=(LinearLayout)this.findViewById(R.id.scrollview_room_layout);
		
		scrolliew_room.setVerticalScrollBarEnabled(false);
		scrolliew_room.setHorizontalScrollBarEnabled(false);
		
		seekbar_text=(TextView)findViewById(R.id.seekbar_text);
		
		radioGroup=(RadioGroup)this.findViewById(R.id.radioGroup1);
	}
	
	private void Setlistener()
	{
		mNodes_ready = new ArrayList<HashMap<String,Object>>();
		mNode_section = new ArrayList<HashMap<String,Object>>();
		mNodes = new ArrayList<ArrayList<HashMap<String,Object>>>();
		
		room_bt_ary= new ArrayList<ImageButton>();
		
		adapter = new MyCustomAdapter(this, R.layout.item_node,mNode_section);
		mNode_list.setAdapter(adapter);

		mNode_list.setOnItemClickListener(listItem_down);
		
		back.setOnClickListener(tab_button_down);
		edit.setOnClickListener(tab_button_down);
		
		back.setTag(1);
		edit.setTag(2);
		
		back.setVisibility(View.GONE);
		
		radioGroup.setOnCheckedChangeListener(radio_down);
	}
	
	private void initScroll()
	{
		room_bt_ary.clear();
		
		for(int i=0;i<location.size();i++)
		{
			String loc=location.get(i).get("location");
			
			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
			
			View view= inflater.inflate(R.layout.room_frame,null);
			
			ImageButton imgbt=(ImageButton)view.findViewById(R.id.room_frame_image);
			TextView name_text=(TextView)view.findViewById(R.id.room_frame_name);
			name_text.setGravity(Gravity.CENTER);
			
			imgbt.setTag(i);
			imgbt.setOnClickListener(room_button_down);
			
			ArrayList<HashMap<String,String>> image_array=(ArrayList<HashMap<String,String>>)cp.getIcon_Image();
			
			String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.avadesign.ha"+"/"+"image"+"/"+"location";
        	String file_name=null;
        	
        	for(HashMap<String,String> map : image_array)
        	{
        		String lab=location.get(i).get("image");
        		
        		if (lab.equalsIgnoreCase(map.get("type")))
        		{
        			file_name=map.get("Normal");
        			break;
        		}
        	}
        	
        	if (file_name!=null)
        	{
        		String [] name=file_name.split("/");
        	
        		if (name.length>=3)
            	{
            		//Log.v(TAG,"=="+path+"/"+name[2]);
            		Bitmap bitmap = BitmapFactory.decodeFile(path+"/"+name[2]);
            		imgbt.setImageBitmap(bitmap);
            	}
        	}
			name_text.setText(loc);
			room_bt_ary.add(imgbt);
			scrolliew_room_layout.addView(view);
		}
		
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		
		View view= inflater.inflate(R.layout.room_frame,null);
		
		ImageButton imgbt=(ImageButton)view.findViewById(R.id.room_frame_image);
		TextView name_text=(TextView)view.findViewById(R.id.room_frame_name);
		name_text.setGravity(Gravity.CENTER);
		
		name_text.setText(getString(R.string.room_no_room));
		imgbt.setImageDrawable(getResources().getDrawable(R.drawable.room));
		imgbt.setOnClickListener(room_button_down);
		imgbt.setTag(location.size());
		room_bt_ary.add(imgbt);
		scrolliew_room_layout.addView(view);
	}
		
	@Override
	protected void callBroadcastdone()
	{
		GetGatewayStatus();
		
		attemptGetData();
	}
	
	@Override
	protected void call404()
	{
		super.call404();
		
		clearList();
	}
	
	private void GetGatewayStatus()
	{
		//±µ¦¬ gateway ª¬ºA
		
		if (!((SharedClassApp)(ActivityDeviceList.this.getApplication())).isActive())
		{
			mDialog_SPINNER_status.dismiss();
		}
		else
		{
			String State=((SharedClassApp)(ActivityDeviceList.this.getApplication())).getControllerState();
			
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
			callProgress();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... args) 
		{
			boolean success = false;
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(args[1], args[2]);
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getControllerAcc());
				map.put("userpwd", cp.getControllerPwd());
				//map.put("tunnelid", "0");
			}
			
			success = SendHttpCommand.send(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+args[0],
					map, 
					cp.getControllerAcc(), 
					cp.getControllerPwd(), 
					cp.isLocalUsed());
			
			Log.v(TAG,"success="+success);
			
			return success;
		}

		@Override
		protected void onPostExecute(final Boolean success) 
		{
			if (success) {
				Toast.makeText(ActivityDeviceList.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ActivityDeviceList.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
			}
			
			cancelProgress();
			
			mSendCommandTask = null;
		}

		@Override
		protected void onCancelled() 
		{
			cancelProgress();
			super.onCancelled();
		}
	}
	
	private void clearList()
	{
		mNodes.clear();
		adapter.notifyDataSetChanged();
	}
	
	@SuppressWarnings("unchecked")
	private void attemptGetData()
	{
		cancelProgress();
		
		ArrayList<HashMap<String,Object>> nodes_ready = new ArrayList<HashMap<String,Object>>();
		
		((SharedClassApp)(ActivityDeviceList.this.getApplication())).refreshNodesList(nodes_ready);
				
		mNodes_ready.clear();
		
		for(HashMap<String, Object> nodes : nodes_ready)
		{
			HashMap<String , Object> map=new HashMap<String , Object>();
			
			map.putAll(nodes);
			
			ArrayList<HashMap<String , Object>> value=(ArrayList<HashMap<String, Object>>) nodes.get("value");
			ArrayList<HashMap<String , Object>> new_value= new ArrayList<HashMap<String , Object>>();
			
			for(HashMap<String, Object> node_value : value)
			{
				new_value.add(node_value);
			}
			
			map.put("value", new_value);
			
			mNodes_ready.add(map);
		}
		
		mNodes.clear();
		
		for(int i=0;i<location.size()+1;i++)
		{
			ArrayList<HashMap<String,Object>> ary1= new ArrayList<HashMap<String,Object>>();
			
			/*
			//add title
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (i==location.size())
				map.put("name",getString(R.string.room_no_room));
			else
				map.put("name",location.get(i).get("location"));
			
			map.put("gtype","title");
			
			ary1.add(map);
			map=null;
			*/
			
			//add znode
			for(int j=0;j<mNodes_ready.size();j++)
			{
				ZWaveNode znode = new ZWaveNode(mNodes_ready.get(j));
				
				if (i==location.size())
				{
					boolean have=false;
					for(HashMap<String,String> loc_map : location)
					{
						if (znode.location.equals(loc_map.get("location")))
						{
							have=true;
						}
					}
					
					if (znode.location.equals("") || znode.location.equals(" "))
						ary1.add(mNodes_ready.get(j));
					else if (!have)
						ary1.add(mNodes_ready.get(j));
				}
				else
				{
					if (znode.location.equals(location.get(i).get("location")))
						ary1.add(mNodes_ready.get(j));
				}
			}
			mNodes.add(ary1);
			
			//Log.v(TAG,"ary size="+ary1.size());
			
			ary1=null;
		}
		
		RrefreshView();
		//mNode_section.addAll(mNodes.get(page));
	}
	
	@SuppressWarnings("unchecked")
	private void RrefreshView()
	{
		mNode_section.clear();
		
		ArrayList<HashMap<String,Object>> nodes=mNodes.get(page);
		
		//Log.v(TAG,""+nodes);
		
		
		for (HashMap<String,Object> node : nodes)
		{
			ZWaveNode znode = new ZWaveNode(node);
			
			if (!znode.gtype.toLowerCase().equals("static pc controller"))
			{
				Boolean hasAlarm=false;
				
				for(ZWaveNodeValue zvalue : znode.value)
				{
					if (zvalue.genre.toLowerCase().equals("user") && zvalue.class_c.toLowerCase().equals("alarm") && zvalue.label.toLowerCase().equals("alarm level"))
					{
						hasAlarm=true;
					}
				}
		
				for(HashMap<String,Object> value : (ArrayList<HashMap<String,Object>>)node.get("value"))
		        {
					HashMap<String , Object> new_node=new HashMap<String , Object>();
					
					new_node.putAll(node);
					
					ArrayList<HashMap<String,Object>> new_values = new ArrayList<HashMap<String,Object>>();
					
					ZWaveNodeValue zvalue = new ZWaveNodeValue(value);
					
					if (Search_node(znode,zvalue,hasAlarm))
					{
						new_values.add(value);
						
						new_node.put("value", new_values);
						
						mNode_section.add(new_node);
					}
		        }
			}
		}
		cancelProgress();
		
		if (!init_scroll)
		{
			ImageButton ibtt=null;
			for(ImageButton ibt : room_bt_ary)
			{
				if (page==(Integer)ibt.getTag())
				{
					ibt.setSelected(true);
					ibtt=ibt;
				}
				else
					ibt.setSelected(false);
			}
			
			ibtt.callOnClick();
			
			init_scroll=true;
		}
		
		adapter.notifyDataSetChanged();
	}
	
	@SuppressLint("DefaultLocale")
	private Boolean Search_node(ZWaveNode znode,ZWaveNodeValue zvalue, Boolean has)
	{
		if (radio_count==0)
		{
			if (zvalue.genre.toLowerCase().equals("user") && zvalue.class_c.toLowerCase().equals("switch binary") && zvalue.label.toLowerCase().equals("switch"))
			{
				return true;
			}
			else if (zvalue.genre.toLowerCase().equals("user") && zvalue.class_c.toLowerCase().equals("switch multilevel") && zvalue.label.toLowerCase().equals("level"))
			{
				return true;
			}
			else if (zvalue.genre.toLowerCase().equals("user") && zvalue.class_c.toLowerCase().equals("door lock") && zvalue.label.toLowerCase().equals("mode"))
			{
				return true;
			}
			else if (zvalue.class_c.toLowerCase().equals("basic") && znode.gtype.toLowerCase().indexOf("motor")!=-1)
			{
				return true;
			}
		}
		else if (radio_count==1)
		{
			if (zvalue.genre.toLowerCase().equals("user") && zvalue.class_c.toLowerCase().equals("sensor binary") && zvalue.label.toLowerCase().equals("sensorbinary icon"))
			{
				return !has;
	        }
			else if (zvalue.genre.toLowerCase().equals("user") && zvalue.class_c.toLowerCase().equals("alarm") && zvalue.label.toLowerCase().equals("alarm level"))
			{
				return true;
			}
			else if (zvalue.genre.toLowerCase().equals("user") && zvalue.class_c.toLowerCase().equals("sensor multilevel"))
			{
				return true;
			}
		}
		else if (radio_count==2)
		{
			if (zvalue.genre.toLowerCase().equals("user"))
	        {
	            if ((zvalue.class_c.toLowerCase().equals("meter") && zvalue.label.toLowerCase().equals("energy")) || 
	            		(zvalue.class_c.toLowerCase().equals("meter") && zvalue.label.toLowerCase().equals("voltage")) || 
	            		(zvalue.class_c.toLowerCase().equals("meter") && zvalue.label.toLowerCase().equals("power")) ||
	            		(zvalue.class_c.toLowerCase().equals("meter") && zvalue.label.toLowerCase().equals("current")) ||
	            		(zvalue.class_c.toLowerCase().equals("meter") && zvalue.label.toLowerCase().equals("power factor")) )
	            {
	                return true;
	            }
	        }
		}
		return false;
	}
	
	
	@SuppressLint("DefaultLocale")
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
        	LinearLayout 	switchlayout_power_layout;
        	RelativeLayout 	switchlayout_dead_layout;
        	ImageView		switchlayout_power_image;
        	TextView 		switchlayout_power_text;
        	
        	//Dimmer
        	RelativeLayout 	dimmerlayout;
        	ImageView		dimmerlayout_icon_image;
        	TextView		dimmerlayout_nickname_text;
        	TextView		dimmerlayout_seek_text;
        	SeekBar  		dimmerlayout_seekbar;
        	LinearLayout 	dimmerlayout_power_layout;
        	RelativeLayout 	dimmerlayout_dead_layout;
        	ImageView		dimmerlayout_power_image;
        	TextView 		dimmerlayout_power_text;

        	//Sensor
        	RelativeLayout 	sensorlayout;
        	ImageView 		sensorlayout_icon_image;
        	TextView 		sensorlayout_nickname_text;
        	TextView 		sensorlayout_status_text;
        	LinearLayout 	sensorlayout_power_layout;
        	RelativeLayout 	sensorlayout_dead_layout;
        	ImageView		sensorlayout_power_image;
        	TextView 		sensorlayout_power_text;

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
        		viewHolder.switchlayout_icon_image     	= (ImageView) convertView.findViewById(R.id.switchlayout_icon_image);
        		viewHolder.switchlayout_nickname_text 	= (TextView)  convertView.findViewById(R.id.switchlayout_nickname_text);
        		viewHolder.switchlayout_status_text		= (TextView) convertView.findViewById(R.id.switchlayout_status_text);
        		viewHolder.switchlayout_power_layout	= (LinearLayout) convertView.findViewById(R.id.switchlayout_power_layout);
        		viewHolder.switchlayout_dead_layout		= (RelativeLayout) convertView.findViewById(R.id.switchlayout_dead_layout);
        		viewHolder.switchlayout_power_image    	= (ImageView) convertView.findViewById(R.id.switchlayout_power_image);
        		viewHolder.switchlayout_power_text		= (TextView) convertView.findViewById(R.id.switchlayout_power_text);
        		
        		
        		viewHolder.dimmerlayout    				= (RelativeLayout) convertView.findViewById(R.id.dimmerlayout);
        		viewHolder.dimmerlayout_icon_image     	= (ImageView) convertView.findViewById(R.id.dimmerlayout_icon_image);
        		viewHolder.dimmerlayout_nickname_text  	= (TextView)  convertView.findViewById(R.id.dimmerlayout_nickname_text);
        		viewHolder.dimmerlayout_seek_text  		= (TextView)  convertView.findViewById(R.id.dimmerlayout_seek_text);
        		viewHolder.dimmerlayout_seekbar			= (SeekBar) convertView.findViewById(R.id.dimmerlayout_seekbar);
        		viewHolder.dimmerlayout_power_layout	= (LinearLayout) convertView.findViewById(R.id.dimmerlayout_power_layout);
        		viewHolder.dimmerlayout_dead_layout		= (RelativeLayout) convertView.findViewById(R.id.dimmerlayout_dead_layout);
        		viewHolder.dimmerlayout_power_image    	= (ImageView) convertView.findViewById(R.id.dimmerlayout_power_image);
        		viewHolder.dimmerlayout_power_text		= (TextView) convertView.findViewById(R.id.dimmerlayout_power_text);
        		
        		
        		viewHolder.sensorlayout    				= (RelativeLayout) convertView.findViewById(R.id.sensorlayout);
        		viewHolder.sensorlayout_icon_image		= (ImageView) convertView.findViewById(R.id.sensorlayout_icon_image);
        		viewHolder.sensorlayout_nickname_text  	= (TextView)  convertView.findViewById(R.id.sensorlayout_nickname_text);
        		viewHolder.sensorlayout_status_text   	= (TextView) convertView.findViewById(R.id.sensorlayout_status_text);
        		viewHolder.sensorlayout_power_layout	= (LinearLayout) convertView.findViewById(R.id.sensorlayout_power_layout);
        		viewHolder.sensorlayout_dead_layout		= (RelativeLayout) convertView.findViewById(R.id.sensorlayout_dead_layout);
        		viewHolder.sensorlayout_power_image    	= (ImageView) convertView.findViewById(R.id.sensorlayout_power_image);
        		viewHolder.sensorlayout_power_text		= (TextView) convertView.findViewById(R.id.sensorlayout_power_text);
        		
        		
        		viewHolder.coverlayout    				= (RelativeLayout) convertView.findViewById(R.id.coverlayout);
        		viewHolder.coverlayout_up				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_up);
        		viewHolder.coverlayout_down				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_down);
        		viewHolder.coverlayout_stop				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_stop);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
            
            HashMap<String,Object> node=mNode_section.get(position);
            
            //Log.v(TAG,"node="+node);
            
            if (node!=null)
            {
            	ZWaveNode znode = new ZWaveNode(node);
                
            	ZWaveNodeValue zvalue=znode.value.get(0);
            
                if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch binary"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.VISIBLE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.switchlayout.setSelected(Edit);
                	
                	if (znode.status.toLowerCase().equals("dead"))
                		viewHolder.switchlayout_dead_layout.setVisibility(View.VISIBLE);
                	else
                		viewHolder.switchlayout_dead_layout.setVisibility(View.GONE);
                	
                	HashMap<String,Object> o_node=Node_id_to_node(znode.id);
                	
                	ZWaveNodeValue batteryValue=GetBatteryLevel_O_node(o_node);
                	
                	if (batteryValue!=null)
                    {
                		viewHolder.switchlayout_power_layout.setVisibility(View.VISIBLE);
                		viewHolder.switchlayout_power_text.setText(batteryValue.current+batteryValue.units);
                		
                		String color=Battery_to_color(Integer.valueOf(batteryValue.current));
                		Drawable power_icon=Battery_to_image(Integer.valueOf(batteryValue.current));
                		viewHolder.switchlayout_power_text.setTextColor(color.equals("black") ? Color.BLACK : Color.RED);
                		viewHolder.switchlayout_power_image.setImageDrawable(power_icon);
                    }
                	else
                		viewHolder.switchlayout_power_layout.setVisibility(View.GONE);
                	
                	if (GetNodeMulti(znode,o_node))
                		viewHolder.switchlayout_nickname_text.setText(znode.name_fix+" ["+zvalue.instance+"]");
                    else
                    	viewHolder.switchlayout_nickname_text.setText(znode.name_fix);
                	
                	String status=zvalue.current.equalsIgnoreCase("false") ? "off" : "on";
                	String sts_text=getString(R.string.device_status)+" : " + (status.equals("off") ? getString(R.string.device_off) : getString(R.string.device_on));
                	viewHolder.switchlayout_status_text.setText(sts_text);
                	
                	Bitmap image_set=GetIconImage(znode,o_node);
                    
                    if (image_set!=null)
                    	viewHolder.switchlayout_icon_image.setImageBitmap(image_set);
                    else
                    	viewHolder.switchlayout_icon_image.setImageResource(Get_Sw_Default_Icon(znode));
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch multilevel"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.VISIBLE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.dimmerlayout.setSelected(Edit);
                	
                	if (znode.status.toLowerCase().equals("dead"))
                		viewHolder.dimmerlayout_dead_layout.setVisibility(View.VISIBLE);
                	else
                		viewHolder.dimmerlayout_dead_layout.setVisibility(View.GONE);
                	
                	HashMap<String,Object> o_node=Node_id_to_node(znode.id);
                	
                	ZWaveNodeValue batteryValue=GetBatteryLevel_O_node(o_node);
                	
                	if (batteryValue!=null)
                    {
                		viewHolder.dimmerlayout_power_layout.setVisibility(View.VISIBLE);
                		viewHolder.dimmerlayout_power_text.setText(batteryValue.current+batteryValue.units);
                		
                		String color=Battery_to_color(Integer.valueOf(batteryValue.current));
                		Drawable power_icon=Battery_to_image(Integer.valueOf(batteryValue.current));
                		viewHolder.dimmerlayout_power_text.setTextColor(color.equals("black") ? Color.BLACK : Color.RED);
                		viewHolder.dimmerlayout_power_image.setImageDrawable(power_icon);
                    }
                	else
                		viewHolder.dimmerlayout_power_layout.setVisibility(View.GONE);
                	
                	if (GetNodeMulti(znode,o_node))
                		viewHolder.dimmerlayout_nickname_text.setText(znode.name_fix+" ["+zvalue.instance+"]");
                    else
                    	viewHolder.dimmerlayout_nickname_text.setText(znode.name_fix);
                	
                	Bitmap image_set=GetIconImage(znode,o_node);
                    
                	int default_image=zvalue.current.equalsIgnoreCase("0")? R.drawable.dimmer_light_off : R.drawable.dimmer_light_on;
                    
                    if (image_set!=null)
                    	viewHolder.dimmerlayout_icon_image.setImageBitmap(image_set);
                    else
                    	viewHolder.dimmerlayout_icon_image.setImageResource(default_image);
                    
                    Integer Value=Integer.valueOf(zvalue.current);
					viewHolder.dimmerlayout_seekbar.setProgress(Value);
					viewHolder.dimmerlayout_seek_text.setText(zvalue.current+"%");

	            	viewHolder.dimmerlayout_seekbar.setTag(position);
	            	viewHolder.dimmerlayout_seekbar.setEnabled(false);
	                //viewHolder.dimmerlayout_seekbar.setOnSeekBarChangeListener(Seek_down);
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("door lock"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.VISIBLE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.switchlayout.setSelected(Edit);
                	
                	if (znode.status.toLowerCase().equals("dead"))
                		viewHolder.switchlayout_dead_layout.setVisibility(View.VISIBLE);
                	else
                		viewHolder.switchlayout_dead_layout.setVisibility(View.GONE);
                	
                	HashMap<String,Object> o_node=Node_id_to_node(znode.id);
                	
                	ZWaveNodeValue batteryValue=GetBatteryLevel_O_node(o_node);
                	
                	if (batteryValue!=null)
                    {
                		viewHolder.switchlayout_power_layout.setVisibility(View.VISIBLE);
                		viewHolder.switchlayout_power_text.setText(batteryValue.current+batteryValue.units);
                		
                		String color=Battery_to_color(Integer.valueOf(batteryValue.current));
                		Drawable power_icon=Battery_to_image(Integer.valueOf(batteryValue.current));
                		viewHolder.switchlayout_power_text.setTextColor(color.equals("black") ? Color.BLACK : Color.RED);
                		viewHolder.switchlayout_power_image.setImageDrawable(power_icon);
                    }
                	else
                		viewHolder.switchlayout_power_layout.setVisibility(View.GONE);
                	
                	if (GetNodeMulti(znode,o_node))
                		viewHolder.switchlayout_nickname_text.setText(znode.name_fix+" ["+zvalue.instance+"]");
                    else
                    	viewHolder.switchlayout_nickname_text.setText(znode.name_fix);
                	
                	String status=zvalue.current.toLowerCase().indexOf("unsecured")!=-1 ? "off" : "on";
                	String sts_text=getString(R.string.device_status)+" : " + (status.equals("off") ? getString(R.string.device_unlock) : getString(R.string.device_lock));
                	viewHolder.switchlayout_status_text.setText(sts_text);
                	
                	int default_image=zvalue.current.toLowerCase().indexOf("unsecured")!=-1 ? R.drawable.doorkeypad_off : R.drawable.doorkeypad_on;
                	
                	Bitmap image_set=GetIconImage(znode,o_node);
                    
                    if (image_set!=null)
                    	viewHolder.switchlayout_icon_image.setImageBitmap(image_set);
                    else
                    	viewHolder.switchlayout_icon_image.setImageResource(default_image);
                }
                else if (zvalue.class_c.toLowerCase().equalsIgnoreCase("basic"))
                {
                	viewHolder.sensorlayout.setVisibility(View.GONE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.VISIBLE);
                	
                	viewHolder.coverlayout.setSelected(Edit);
                	
                	viewHolder.coverlayout_up.setTag(position);
	                viewHolder.coverlayout_down.setTag(position);
	                viewHolder.coverlayout_stop.setTag(position);
	                viewHolder.coverlayout_down.setOnClickListener(cover_button_down);
	                viewHolder.coverlayout_stop.setOnClickListener(cover_button_down);
	                viewHolder.coverlayout_up.setOnClickListener(cover_button_down);
                }
                else  if (zvalue.class_c.toLowerCase().equals("sensor binary") || (zvalue.class_c.toLowerCase().equals("alarm") && zvalue.label.toLowerCase().equals("alarm level")))
                {
                	viewHolder.sensorlayout.setVisibility(View.VISIBLE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout.setSelected(Edit);
                	
                	if (znode.status.toLowerCase().equals("dead"))
                		viewHolder.sensorlayout_dead_layout.setVisibility(View.VISIBLE);
                	else
                		viewHolder.sensorlayout_dead_layout.setVisibility(View.GONE);
                	
                	HashMap<String,Object> o_node=Node_id_to_node(znode.id);
                	
                	ZWaveNodeValue batteryValue=GetBatteryLevel_O_node(o_node);
                	
                	if (batteryValue!=null)
                    {
                		viewHolder.sensorlayout_power_layout.setVisibility(View.VISIBLE);
                		viewHolder.sensorlayout_power_text.setText(batteryValue.current+batteryValue.units);
                		
                		String color=Battery_to_color(Integer.valueOf(batteryValue.current));
                		Drawable power_icon=Battery_to_image(Integer.valueOf(batteryValue.current));
                		viewHolder.sensorlayout_power_text.setTextColor(color.equals("black") ? Color.BLACK : Color.RED);
                		viewHolder.sensorlayout_power_image.setImageDrawable(power_icon);
                    }
                	else
                		viewHolder.sensorlayout_power_layout.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout_nickname_text.setText(znode.name_fix);
                	
                	String sts_text=getString(R.string.device_status)+" : " + ( (zvalue.current.equalsIgnoreCase("false") || zvalue.current.equalsIgnoreCase("0")) ? getString(R.string.device_off) : getString(R.string.device_on));
                	
                	viewHolder.sensorlayout_status_text.setText(sts_text);
                	
                	Bitmap image_set=GetIconImage(znode,o_node);
                    
                    if (image_set!=null)
                    	viewHolder.sensorlayout_icon_image.setImageBitmap(image_set);
                    else
                    	viewHolder.sensorlayout_icon_image.setImageResource(Get_Sensor_Default_Icon(znode));
                }
                else  if (zvalue.class_c.toLowerCase().equals("sensor multilevel") || zvalue.class_c.toLowerCase().equals("meter"))
                {
                	viewHolder.sensorlayout.setVisibility(View.VISIBLE);
                	viewHolder.switchlayout.setVisibility(View.GONE);
                	viewHolder.dimmerlayout.setVisibility(View.GONE);
                	viewHolder.coverlayout.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout.setSelected(Edit);
                	
                	if (znode.status.toLowerCase().equals("dead"))
                		viewHolder.sensorlayout_dead_layout.setVisibility(View.VISIBLE);
                	else
                		viewHolder.sensorlayout_dead_layout.setVisibility(View.GONE);
                	
                	HashMap<String,Object> o_node=Node_id_to_node(znode.id);
                	
                	ZWaveNodeValue batteryValue=GetBatteryLevel_O_node(o_node);
                	
                	if (batteryValue!=null)
                    {
                		viewHolder.sensorlayout_power_layout.setVisibility(View.VISIBLE);
                		viewHolder.sensorlayout_power_text.setText(batteryValue.current+batteryValue.units);
                		
                		String color=Battery_to_color(Integer.valueOf(batteryValue.current));
                		Drawable power_icon=Battery_to_image(Integer.valueOf(batteryValue.current));
                		viewHolder.sensorlayout_power_text.setTextColor(color.equals("black") ? Color.BLACK : Color.RED);
                		viewHolder.sensorlayout_power_image.setImageDrawable(power_icon);
                    }
                	else
                		viewHolder.sensorlayout_power_layout.setVisibility(View.GONE);
                	
                	viewHolder.sensorlayout_nickname_text.setText(znode.name_fix);
                	
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
                }
            }
            
            return convertView;
    	}
    }

//--------------Button down-------------------//
	
	private Button.OnClickListener cover_button_down=new Button.OnClickListener()
	{
		@Override
		public void onClick(View view) 
		{
			HashMap<String,Object> node=mNode_section.get((Integer)view.getTag());
			
			HashMap<String,Object> o_node=Node_id_to_node((String)node.get("id"));
			
			ZWaveNode znode = new ZWaveNode(o_node);
			
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
	
	/*
	private SeekBar.OnSeekBarChangeListener Seek_down=new SeekBar.OnSeekBarChangeListener()
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
		{
			String SeekStr=String.valueOf(progress);
			seekbar_text.setText(SeekStr+"%");
			
			Log.v(TAG,"change touch");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) 
		{
			seekbar_text.setVisibility(View.VISIBLE);
			
			Log.v(TAG,"Start touch");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) 
		{	
			HashMap<String,Object> node=mNode_section.get((Integer)seekBar.getTag());
			
			ZWaveNode znode = new ZWaveNode(node);
			
			ZWaveNodeValue zvalue=znode.value.get(0);
			
			if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch multilevel"))
			{
				if (zvalue.label.toLowerCase().equalsIgnoreCase("level"))
				{
					final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
					sendCommand(vid,String.valueOf(seekBar.getProgress()),znode.id);
					seekbar_text.setVisibility(View.GONE);
				}
			}
			
			Log.v(TAG,"Stop touch");
		}
	};
	*/
	
	private RadioGroup.OnCheckedChangeListener radio_down = new RadioGroup.OnCheckedChangeListener() 
	{
		public void onCheckedChanged(RadioGroup group, int checkedId) 
		{
		
			//int p = group.indexOfChild((RadioButton) findViewById(checkedId));
	
			//int count = group.getChildCount();
		
			switch (checkedId) 
			{
				case R.id.radio0 :
				{
					radio_count=0;
					break;
				}
				case R.id.radio1 :
				{
					radio_count=1;
					break;
				}
				case R.id.radio2 :
				{
					radio_count=2;
					break;
				}
			}
			
			RrefreshView();
		}
	};
	
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
					adapter.notifyDataSetChanged();
					break;
				}
				default:
					break;
			}
		}
	};
	
	private Button.OnClickListener room_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			page=(Integer) v.getTag();
			
			Log.v(TAG,"room.tag="+v.getTag());

			//scrolliew_room.smoothScrollTo(100, 0);
			int scroll_width=scrolliew_room.getWidth();
			int bt_width=0;
			
			for(ImageButton ibt : room_bt_ary)
			{
				if (ibt.getTag()==v.getTag())
					ibt.setSelected(true);
				else
					ibt.setSelected(false);
				
				//Log.v(TAG,"ibt.getWidth()="+ibt.getWidth());
				bt_width=ibt.getWidth();
			}
			
			int all_width=page*bt_width+100;
			
			if (all_width>scroll_width/2)
			{
				int scrollto=all_width-(scroll_width/2);
				scrolliew_room.smoothScrollTo(scrollto, 0);
			}
			else
			{
				scrolliew_room.smoothScrollTo(0, 0);
			}

			if (init_scroll)
			{
				RrefreshView();
			}
		}
	};
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
		{
			if (Edit)
			{
				ZWaveNode znode = new ZWaveNode(mNode_section.get(position));
				
				HashMap<String,Object> o_node=Node_id_to_node(znode.id);
	        	
	        	ZWaveNode znode_o = new ZWaveNode(o_node);
				
				((SharedClassApp)(ActivityDeviceList.this.getApplication())).setZWaveNode(znode_o);
				
				Log.v(TAG,"id=="+znode.id);
				
				Intent intent = new Intent();
				
				Bundle bundle= new Bundle();
        		bundle.putSerializable("location", location);
        		intent.putExtras(bundle);
        		
				intent.setClass(ActivityDeviceList.this, ActivityNodeEdit.class);
				startActivity(intent);			
			}
			else
			{
				if (radio_count==0)
				{
					ZWaveNode znode = new ZWaveNode(mNode_section.get(position));
					ZWaveNodeValue zvalue = znode.value.get(0);
					
					@SuppressWarnings("unused")
					Boolean have_dimmer=false;

					if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch binary"))
			        {
			            if (zvalue.label.toLowerCase().equalsIgnoreCase("switch"))
			            {
			            	final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;						
							sendCommand(vid,value_ToChange(zvalue.current.toLowerCase()),znode.id);
			            	
							/*
			            	HashMap<String,Object> o_node=Node_id_to_node(znode.id);
				        	
				        	ZWaveNode znode_o = new ZWaveNode(o_node);
							
							((SharedClassApp)(ActivityDeviceList.this.getApplication())).setZWaveNode(znode_o);
							
				        	Intent intent = new Intent();
							
							intent.setClass(ActivityDeviceList.this, ActivityDimmerView.class);
							startActivity(intent);
							*/
			            }
			        }
			        else if (zvalue.class_c.equalsIgnoreCase("switch multilevel"))
					{
						if (zvalue.label.equalsIgnoreCase("level"))
						{
							HashMap<String,Object> o_node=Node_id_to_node(znode.id);
				        	
				        	ZWaveNode znode_o = new ZWaveNode(o_node);
							
							((SharedClassApp)(ActivityDeviceList.this.getApplication())).setZWaveNode(znode_o);
							
				        	Intent intent = new Intent();
							
							intent.setClass(ActivityDeviceList.this, ActivityDimmerView.class);
							startActivity(intent);
							
							//blues
							
							/*
							final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;						
	    					sendCommand(vid,value_ToChange(zvalue.current.toLowerCase()),znode.id);
	    					
	    					have_dimmer=true;
	    					*/
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
			        	HashMap<String,Object> o_node=Node_id_to_node(znode.id);
			        	
			        	ZWaveNode znode_o = new ZWaveNode(o_node);
						
						((SharedClassApp)(ActivityDeviceList.this.getApplication())).setZWaveNode(znode_o);
						
			        	Intent intent = new Intent();
						
						intent.setClass(ActivityDeviceList.this, ActivityDoorLock.class);
						startActivity(intent);
					}
				}
				/*
				if (!mNodes.get(page).get(position).get("gtype").equals("title"))
				{
					ZWaveNode znode = new ZWaveNode(mNodes.get(page).get(position));
					
					((SharedClassApp)(ActivityDeviceList.this.getApplication())).setZWaveNode(znode);
					
					Log.v(TAG,"id=="+znode.id);
					
					Intent intent = new Intent();
					
					Bundle bundle= new Bundle();
	        		bundle.putSerializable("location", location);
	        		bundle.putSerializable("room_node_array", mNodes.get(page));
	        		bundle.putInt("page", position);
	        		bundle.putBoolean("SceneMode", false);
	        		intent.putExtras(bundle);
	        		
					intent.setClass(ActivityDeviceList.this, ActivityNodeList.class);
					startActivity(intent);
				}
				
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
				*/
			}
		}
	};
	
//--------------Other Function-------------------//
	
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
	
	@SuppressWarnings("unchecked")
	private  ZWaveNodeValue GetBatteryLevel_O_node(HashMap<String,Object> o_node)
	{
	    for (HashMap<String,Object> value : (ArrayList<HashMap<String,Object>>)o_node.get("value"))
	    {
	        ZWaveNodeValue zvalue = new ZWaveNodeValue(value);
	        
	        if (zvalue.class_c.toLowerCase().equals("battery"))
	        {
	            if (zvalue.label.toLowerCase().equals("battery level"))
	            {
	                return zvalue;
	            }
	        }
	    }
	    return null;
	}
	
	@SuppressWarnings("unchecked")
	private Boolean GetNodeMulti(ZWaveNode znode,HashMap<String,Object> o_node)
	{
	    int have_multi=0;
	    
	    ZWaveNodeValue zvalue=znode.value.get(0);
	   
	    for (HashMap<String,Object> node_value : (ArrayList<HashMap<String,Object>>)o_node.get("value"))
	    {
	    	ZWaveNodeValue zvalue_i = new ZWaveNodeValue(node_value);
	    	
	    	if (zvalue_i.class_c.toLowerCase().equals(zvalue.class_c.toLowerCase()))
	    	{
	    		if (zvalue_i.label.toLowerCase().equals(zvalue.label.toLowerCase()))
	    		{
	    			have_multi++;
	    		}
	    	}
	    }
	    
	    if (have_multi>1)
	        return true;
	    else
	        return false;
	}
	
	private int Get_Sw_Default_Icon(ZWaveNode znode)
	{
		ZWaveNodeValue zvalue=znode.value.get(0);
	    
	    int default_image;
	    
		if (znode.name_fix.toLowerCase().indexOf("siren")!=-1 || znode.product.toLowerCase().indexOf("siren")!=-1)
    		default_image=zvalue.current.toLowerCase().equals("true") ? R.drawable.alerton : R.drawable.alertoff;
    	else
    		default_image=zvalue.current.toLowerCase().equals("true") ? R.drawable.dimmer_light_on : R.drawable.dimmer_light_off;
	    
	    return default_image;
	}
	
	private int Get_Sensor_Default_Icon(ZWaveNode znode)
	{
		ZWaveNodeValue zvalue=znode.value.get(0);
	    
		String status;
	    
	    if (zvalue.class_c.toLowerCase().equals("sensor binary"))
	        status=zvalue.current.toLowerCase().equals("false") ? "off" : "on";
	    else
	        status=zvalue.current.toLowerCase().equals("0") ? "off" : "on";
	    
	    int default_image;
	    
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
	    
		return default_image;
	}
	
	@SuppressWarnings("unchecked")
	private Bitmap GetIconImage(ZWaveNode znode, HashMap<String,Object> o_node)
	{
		ZWaveNodeValue zvalue=znode.value.get(0);
	    
	    String status;
	    
	    if (zvalue.class_c.toLowerCase().equals("alarm"))
	        status=zvalue.current.toLowerCase().equals("0") ? "off" : "on";
	    else
	    	status=zvalue.current.toLowerCase().equals("false") ? "off" : "on";
	    
	    if (zvalue.class_c.toLowerCase().equals("door lock"))
		    status=zvalue.current.toLowerCase().indexOf("unsecured")!=-1 ? "off" : "on";
	    
	    for (HashMap<String,Object> node_value : (ArrayList<HashMap<String,Object>>)o_node.get("value"))
	    {
	    	ZWaveNodeValue zvalue_i = new ZWaveNodeValue(node_value);
	    	
	    	if (zvalue_i.units.toLowerCase().equals("icon") && zvalue_i.instance.equals(zvalue.instance) && zvalue_i.class_c.equals(zvalue.class_c))
	        {
	    		if (!zvalue.class_c.toLowerCase().equals("switch binary"))
	            {
	    			String path=label_to_path_label(zvalue_i.current,status);
	    			Bitmap bitmap = BitmapFactory.decodeFile(path);
	    			if (bitmap!=null)
        				return bitmap;
	            }
	    		else
	    		{
	    			if (znode.gtype.toLowerCase().indexOf("motor")==-1)
	                {
	    				String path=label_to_path_label(zvalue_i.current,status);
		    			Bitmap bitmap = BitmapFactory.decodeFile(path);
		    			if (bitmap!=null)
	        				return bitmap;
	                }
	    		}
	        }
	    }
		return null;
	}
	
	private HashMap<String,Object> Node_id_to_node(final String node_id)
	{
		ArrayList<HashMap<String,Object>> nodes= new ArrayList<HashMap<String,Object>>();
		((SharedClassApp)(ActivityDeviceList.this.getApplication())).refreshNodesList(nodes);
		
		for (HashMap<String,Object> node : nodes)
		{
			ZWaveNode znode=new ZWaveNode(node);
			
			if (znode.id.equals(node_id))
				return node;
		}
		
		return null;
	}
	
	private void sendCommand(final String vid , final String action , final String id)
	{
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"valuepost.html",vid,action});
		
		/*
		
		//mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
		//mDialog_SPINNER.show();
		Log.v(TAG,"vid "+vid+"="+action);
		new Thread()
		{
			public void run()
			{
				boolean success = false;
				
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
						cp.getControllerAcc(), 
						cp.getControllerPwd(), 
						cp.isLocalUsed());
				
				if (success)
				{
					//Refresh(id);
				}
				Touch=false;
			}
		}.start();
		*/
	}
	
	private void Refresh(final String id)
	{
		try 
		{
			Thread.sleep(3000);
			
			Log.v(TAG,"Refresh");
			
			new Thread()
			{
				public void run()
				{
					Map<String, String> map = new HashMap<String,String>();
					
					map.put("fun","racp");
					map.put("node",id);
					
					if(!cp.isLocalUsed())
					{
						map.put("mac", cp.getControllerMAC());
						map.put("username", cp.getControllerAcc());
						map.put("userpwd", cp.getControllerPwd());
						//map.put("tunnelid", "0");
					}
					
					SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"refreshpost.html",
							map, 
							cp.getControllerAcc(), 
							cp.getControllerPwd(), 
							cp.isLocalUsed());
				}
			}.start();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
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
	
	private String label_to_path_label (String label , String status)
	{
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
	    			String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.avadesign.ha"+"/"+ary[0]+"/"+ary[1]+"/"+status;
	            	
	    			return path+"/"+ary[2];
	    		}
	    	}
	    }
		return "";
	}
}
