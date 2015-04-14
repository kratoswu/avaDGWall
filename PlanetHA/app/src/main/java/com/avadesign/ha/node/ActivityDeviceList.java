package com.avadesign.ha.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;

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

		StartService();
		
		mDialog_SPINNER_status = new ProgressDialog(this);
		mDialog_SPINNER_status.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		Bundle bundle=this.getIntent().getExtras();
		page=bundle.getInt("page");
		
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
    	edit.setVisibility(cp.getUserName().equals("admin") ? View.VISIBLE : View.GONE);
    	
		adapter.notifyDataSetChanged();

		if (!init_scroll)
			initScroll();
		
		RegisterBroadcast();
		
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		UnRegisterBroadcast();
	}
	
	@Override
	protected void onDestroy()
	{
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
		init_scroll=true;
		
		room_bt_ary.clear();
		
		for(int i=0;i<location.size();i++)
		{
			String loc=location.get(i).get("location");
			
			LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
			
			View view= inflater.inflate(R.layout.room_frame,null);
			
			ImageButton imgbt=(ImageButton)view.findViewById(R.id.room_frame_image);
			TextView name_text=(TextView)view.findViewById(R.id.room_frame_name);
			
			imgbt.setTag(i);
			imgbt.setOnClickListener(room_button_down);
			
			if (page==i)
				imgbt.setSelected(true);
			
			ArrayList<HashMap<String,String>> image_array=(ArrayList<HashMap<String,String>>)cp.getIcon_Image();
			
			String path = Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+"image"+"/"+"location";
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
		
		name_text.setText(getString(R.string.room_no_room));
		imgbt.setImageDrawable(getResources().getDrawable(R.drawable.room));
		imgbt.setOnClickListener(room_button_down);
		imgbt.setTag(location.size());
		room_bt_ary.add(imgbt);
		scrolliew_room_layout.addView(view);
	}
	
	private Button.OnClickListener room_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			page=(Integer) v.getTag();
			
			Log.v(TAG,"room.tag="+v.getTag());

			for(ImageButton ibt : room_bt_ary)
			{
				if (ibt.getTag()==v.getTag())
					ibt.setSelected(true);
				else
					ibt.setSelected(false);
			}
			
			RrefreshView();
		}
	};
	
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
		{
			if (Edit)
			{
				if (!mNodes.get(page).get(position).get("gtype").equals("title"))
				{
					ZWaveNode znode = new ZWaveNode(mNodes.get(page).get(position));
					
					((SharedClassApp)(ActivityDeviceList.this.getApplication())).setZWaveNode(znode);
					
					Log.v(TAG,"id=="+znode.id);
					
					Intent intent = new Intent();
					
					Bundle bundle= new Bundle();
	        		bundle.putSerializable("location", location);
	        		intent.putExtras(bundle);
	        		
					intent.setClass(ActivityDeviceList.this, ActivityNodeEdit.class);
					startActivity(intent);
				}			
			}
			else
			{
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
				*/
			}
		}
	};
		
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
			mDialog_SPINNER_status.setMessage(getString(R.string.dialog_message_wait));
			mDialog_SPINNER_status.show();
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
				Toast.makeText(ActivityDeviceList.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ActivityDeviceList.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
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
		
		Log.v(TAG,""+nodes);
		
		
		for (HashMap<String,Object> node : nodes)
		{
			ZWaveNode znode = new ZWaveNode(node);
			
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
		
		adapter.notifyDataSetChanged();
	}
	
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
        	LinearLayout 	switchlayout_dead_layout;
        	
        	//Dimmer
        	RelativeLayout 	dimmerlayout;
        	ImageView		dimmerlayout_icon_image;
        	TextView		dimmerlayout_nickname_text;
        	TextView		dimmerlayout_seek_text;
        	SeekBar  		dimmerlayout_seekbar;
        	LinearLayout 	dimmerlayout_power_layout;
        	LinearLayout 	dimmerlayout_dead_layout;

        	//Sensor
        	RelativeLayout 	sensorlayout;
        	ImageView 		sensorlayout_icon_image;
        	TextView 		sensorlayout_nickname_text;
        	TextView 		sensorlayout_status_text;
        	LinearLayout 	sensorlayout_power_layout;
        	LinearLayout 	sensorlayout_dead_layout;

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
        		viewHolder.switchlayout_dead_layout		= (LinearLayout) convertView.findViewById(R.id.switchlayout_dead_layout);
        		
        		viewHolder.dimmerlayout    				= (RelativeLayout) convertView.findViewById(R.id.dimmerlayout);
        		viewHolder.dimmerlayout_icon_image     	= (ImageView) convertView.findViewById(R.id.dimmerlayout_icon_image);
        		viewHolder.dimmerlayout_nickname_text  	= (TextView)  convertView.findViewById(R.id.dimmerlayout_nickname_text);
        		viewHolder.dimmerlayout_seek_text  		= (TextView)  convertView.findViewById(R.id.dimmerlayout_seek_text);
        		viewHolder.dimmerlayout_seekbar			= (SeekBar) convertView.findViewById(R.id.dimmerlayout_seekbar);
        		viewHolder.dimmerlayout_power_layout	= (LinearLayout) convertView.findViewById(R.id.dimmerlayout_power_layout);
        		viewHolder.dimmerlayout_dead_layout		= (LinearLayout) convertView.findViewById(R.id.dimmerlayout_dead_layout);
        		
        		viewHolder.sensorlayout    				= (RelativeLayout) convertView.findViewById(R.id.sensorlayout);
        		viewHolder.sensorlayout_icon_image		= (ImageView) convertView.findViewById(R.id.sensorlayout_icon_image);
        		viewHolder.sensorlayout_nickname_text  	= (TextView)  convertView.findViewById(R.id.sensorlayout_nickname_text);
        		viewHolder.sensorlayout_status_text   	= (TextView) convertView.findViewById(R.id.sensorlayout_status_text);
        		viewHolder.sensorlayout_power_layout	= (LinearLayout) convertView.findViewById(R.id.sensorlayout_power_layout);
        		viewHolder.sensorlayout_dead_layout		= (LinearLayout) convertView.findViewById(R.id.sensorlayout_dead_layout);
        		
        		viewHolder.coverlayout    				= (RelativeLayout) convertView.findViewById(R.id.coverlayout);
        		viewHolder.coverlayout_up				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_up);
        		viewHolder.coverlayout_down				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_down);
        		viewHolder.coverlayout_stop				= (ImageButton) convertView.findViewById(R.id.coverlayout_control_stop);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
            
            HashMap<String,Object> node=mNode_section.get(position);
            
            if (node!=null)
            {
            	ZWaveNode znode = new ZWaveNode(node);
                
            	ZWaveNodeValue zvalue=znode.value.get(0);
            
                if (zvalue.class_c.toLowerCase().equalsIgnoreCase("switch binary"))
                {
                	
                }
            }
            
            
            
            
            
            
            
            
            
            
            
            return convertView;
    	}
    }

//--------------Button down-------------------//
	private RadioGroup.OnCheckedChangeListener radio_down = new RadioGroup.OnCheckedChangeListener() 
	{
		public void onCheckedChanged(RadioGroup group, int checkedId) 
		{
		
			int p = group.indexOfChild((RadioButton) findViewById(checkedId));
	
			int count = group.getChildCount();
		
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
			
			PlayButtonSound.play(ActivityDeviceList.this);
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
	
	private String label_to_path_label (String label , String status)
	{
		CusPreference cp = new CusPreference(ActivityDeviceList.this);
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
