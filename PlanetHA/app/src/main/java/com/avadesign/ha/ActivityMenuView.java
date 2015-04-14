package com.avadesign.ha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.avadesign.ha.camera.ActivityCameraView;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.GCMReceiver;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.avacontrol_service;
import com.avadesign.ha.gateway.ActivityGatewaySearch;
import com.avadesign.ha.gateway.ActivityGatewayView;
import com.avadesign.ha.room.ActivityRoomList;
import com.avadesign.ha.scene.ActivitySceneView;
import com.avadesign.ha.schedule.ActivityScheduleView;
import com.avadesign.ha.trigger.ActivityTriggerView;
import com.google.android.gcm.GCMessaging;

public class ActivityMenuView extends BaseActivity
{
	private Button button_control,button_scene,button_camera,button_trigger,button_schedule,button_setting,button_history,button_report;
	private LinearLayout layout3,layout5;
	
	private long currentTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_view);
		
		FindView();
		
		Setlistener();
		
		CheckFirstStart();
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		cp.setStopPolling(true);
		
		SetView();
		
		CheckIconFile();
		
		/*
		String path = Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+"image"+"/"+"location";
		
		File location_path = new File(path);

		File[] location_dir = location_path.listFiles();

		//CharSequence[] list = new CharSequence[location_dir.length];
		
		for (int i = 0; i < location_dir.length; i++) 
		{
			//list[i] = location_dir[i].getName();
			Log.v(TAG,path+"="+location_dir[i].getName());
			
			//String file_path=location_dir[i].getAbsolutePath();
			
			//Bitmap bitmap = BitmapFactory.decodeFile(file_path);
		}
		*/
	}
	
	@Override
	protected void onDestroy() 
	{		
		Intent intent_stopService = new Intent(this, avacontrol_service.class);
		stopService(intent_stopService);
		
		super.onDestroy();
	}
	
	private void FindView()
	{
		//gridview = (GridView)findViewById(R.id.gridview );
		
		button_control	=(Button)findViewById(R.id.button_control);
		button_scene	=(Button)findViewById(R.id.button_scene);
		button_camera	=(Button)findViewById(R.id.button_camera);
		button_trigger	=(Button)findViewById(R.id.button_trigger);
		button_schedule	=(Button)findViewById(R.id.button_schedule);
		button_setting	=(Button)findViewById(R.id.button_setting);
		//button_notify	=(Button)findViewById(R.id.button_notify);
		button_history	=(Button)findViewById(R.id.button_history);
		button_report	=(Button)findViewById(R.id.button_report);
		
		layout3				=(LinearLayout)findViewById(R.id.layout3);
		layout5				=(LinearLayout)findViewById(R.id.layout5);
	}
	
	private void Setlistener()
	{
		button_control.setTag(0);
		button_scene.setTag(1);
		button_camera.setTag(2);
		button_trigger.setTag(3);
		button_schedule.setTag(4);
		button_setting.setTag(5);
		//button_notify.setTag(6);
		button_history.setTag(7);
		button_report.setTag(8);
		
		button_control.setOnClickListener(button_down);
		button_scene.setOnClickListener(button_down);
		button_camera.setOnClickListener(button_down);
		button_trigger.setOnClickListener(button_down);
		button_schedule.setOnClickListener(button_down);
		button_setting.setOnClickListener(button_down);
		//button_notify.setOnClickListener(button_down);
		button_history.setOnClickListener(button_down);
		button_report.setOnClickListener(button_down);
		
		Log.v(TAG,"width="+button_control.getHeight());
	}
	
	private void CheckFirstStart()
	{
		if(cp.getControllerIP().equals(""))
		{
			cp.setUserName("");
			cp.setUserPwd("");
			Intent intent = new Intent();
			intent.setClass(ActivityMenuView.this, ActivityGatewaySearch.class);
			startActivity(intent);
		}
	}
	
	private void CheckIconFile()
	{
		if(!cp.getControllerIP().equals(""))
		{
			currentTime= System.currentTimeMillis();
			long LastUpdateTime=cp.getUpdateTime();
			
			Log.v(TAG,"c_time="+currentTime);
			Log.v(TAG,"l_time="+LastUpdateTime);
			Log.v(TAG,"l_time="+(currentTime-LastUpdateTime));
			
			if ((currentTime-LastUpdateTime-(3600000*24))>0)
				getIconList();
		}
	}

	private void getIconList()
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityMenuView.this);

				String xml_str = SendHttpCommand.get_histiry(
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"image/image_list.xml"+(cp.isLocalUsed()?"":"?mac="+cp.getControllerMAC()+"&username="+cp.getUserName()+"&userpwd="+cp.getUserPwd()/*+"&tunnelid=0"*/), 
						cp.getUserName(), 
						cp.getUserPwd());
				ArrayList<HashMap<String,String>> image_array=SendHttpCommand.parserXML(xml_str,"image"); 
								
				Message message = handler.obtainMessage(1,image_array);
				handler.sendMessage(message);
			}
		}.start();
	}
	
	/*
	@Override
	public void onBackPressed() 
	{
		Builder builder = new AlertDialog.Builder(ActivityMenuView.this);

		builder.setTitle(R.string.menu_exit_alert_title);

		builder.setMessage(R.string.menu_exit_alert_message);
		builder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				Intent intent_stopService = new Intent(ActivityMenuView.this, ServicePolling.class);
				stopService(intent_stopService);
				finish();
			}
		});
		
		builder.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				
			}
		});
		builder.show();
	}
	*/
	
	@SuppressLint("HandlerLeak")
	@SuppressWarnings("unchecked")
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			if (msg.what==1)
			{
				ArrayList<HashMap<String,String>> image_array = (ArrayList<HashMap<String,String>>)msg.obj;
				
				if (image_array!=null)
				{
					ArrayList<HashMap<String,String>> old_array=cp.getIcon_Image();
					
					if (old_array!=null)
					{
						Log.v(TAG,""+old_array.size());
						
						if (old_array.size()<image_array.size() && image_array.size()!=0)
						{
							cp.setUpdateTime(currentTime);
							
							//Log.v(TAG,"download view");
							
							Intent intent = new Intent();
							Bundle bundle= new Bundle();
							bundle.putSerializable("image_array", image_array);
			        		intent.putExtras(bundle);
		    				intent.setClass(ActivityMenuView.this, ActivityDownloadView.class);
		    				startActivity(intent);
						}
					}
					else
					{
						Log.v(TAG,""+image_array.size());
						cp.setUpdateTime(currentTime);
						
						//Log.v(TAG,"download view");
						
						Intent intent = new Intent();
						Bundle bundle= new Bundle();
						bundle.putSerializable("image_array", image_array);
		        		intent.putExtras(bundle);
	    				intent.setClass(ActivityMenuView.this, ActivityDownloadView.class);
	    				startActivity(intent);
					}
					
					cp.setIcon_Image(image_array);
				}
				else
				{
					Log.v(TAG,"image=null");
					
					image_array=new ArrayList<HashMap<String,String>>();
					
					cp.setIcon_Image(image_array);
				}
			}
		}
	};
	
	private void SetView()
	{
		CusPreference cp = new CusPreference(ActivityMenuView.this);
    	if (cp.getUserName().equals("admin"))
    	{
    		button_trigger.setBackgroundResource(R.drawable.selector_menu_trigger);
    		button_trigger.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.menu_trigger), null, null);
    		button_trigger.setText(R.string.menu_trigger);
    		
    		layout3.setVisibility(View.VISIBLE);
    		layout5.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		button_trigger.setBackgroundResource(R.drawable.selector_menu_setting);
    		button_trigger.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.menu_setting), null, null);
    		button_trigger.setText(R.string.menu_setting);
    		
    		layout3.setVisibility(View.GONE);
    		layout5.setVisibility(View.GONE);
    	}
    	    	
    	/*
		Boolean ispush=cp.isPush();
		if (ispush)
		{
			button_notify.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.menu_notify_on),null, null, null);
			button_notify.setText(R.string.menu_notify_on);
		}
		else
		{
			button_notify.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.menu_notify_off),null, null, null);
			button_notify.setText(R.string.menu_notify_off);
		}
		*/
	}
	
	private Button.OnClickListener button_down = new OnClickListener()
	{
		@Override
		public void onClick(View arg0) 
		{
			switch((Integer)arg0.getTag())
			{
				case 0:
				{
					//Control
            		Intent intent = new Intent();
    				intent.setClass(ActivityMenuView.this, ActivityRoomList.class);
    				startActivity(intent);
					break;
				}
				case 1:
				{
					//Scene
            		Intent intent = new Intent();
    				intent.setClass(ActivityMenuView.this, ActivitySceneView.class);
    				startActivity(intent);
					break;
				}
				case 2:
				{
					//Camera
            		Intent intent = new Intent();
    				intent.setClass(ActivityMenuView.this, ActivityCameraView.class);
    				startActivity(intent);
					break;
				}
				case 3:
				{
					if (cp.getUserName().equals("admin"))
                	{
                		//trigger
                		Intent intent = new Intent();
        				intent.setClass(ActivityMenuView.this, ActivityTriggerView.class);
        				startActivity(intent);
                	}
                	else
                	{
                		//setting
                		Intent intent = new Intent();
        				intent.setClass(ActivityMenuView.this, ActivityGatewayView.class);
        				startActivity(intent);
                	}
					break;
				}
				case 4:
				{
					//schedule
            		Intent intent = new Intent();
    				intent.setClass(ActivityMenuView.this, ActivityScheduleView.class);
    				startActivity(intent);
					break;
				}
				case 5:
				{
					Intent intent = new Intent();
    				intent.setClass(ActivityMenuView.this, ActivityGatewayView.class);
    				startActivity(intent);
					break;
				}
				case 6:
				{
					/*
					CusPreference cp = new CusPreference(ActivityMenuView.this);
            		cp.setIsPush(!cp.isPush());                		
            		SetView();
            		getNotifyList();
            		*/
					break;
				}
				case 7:
				{
					Intent intent = new Intent();
        			intent.setClass(ActivityMenuView.this, ActivityNotifyHistory.class);
        			startActivity(intent);
					break;
				}
				case 8:
				{
					Intent intent = new Intent();
        			intent.setClass(ActivityMenuView.this, ActivityReport.class);
        			startActivity(intent);
					break;
				}
				default:
					break;
			}
		}
	};
	
	/*
	private GridView.OnItemClickListener grid_down = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			Log.v(TAG,"Button="+arg2);
			
			switch (arg2)
			{
				case 0:
				{
					//Control
            		Intent intent = new Intent();

            		Bundle bundle= new Bundle();
            		bundle.putString("status", gw_status_text.getText().toString());
            		intent.putExtras(bundle);
            		
    				intent.setClass(ActivityMenuView.this, ActivityRoomList.class);
    				startActivity(intent);
					break;
				}
				case 1:
				{
					//Scene
            		Intent intent = new Intent();

            		Bundle bundle= new Bundle();
            		bundle.putString("status", gw_status_text.getText().toString());
            		intent.putExtras(bundle);
            		
    				intent.setClass(ActivityMenuView.this, ActivitySceneView.class);
    				startActivity(intent);
					break;
				}
				case 2:
				{
					//Camera
            		Intent intent = new Intent();

            		Bundle bundle= new Bundle();
            		bundle.putString("status", gw_status_text.getText().toString());
            		intent.putExtras(bundle);
            		
    				intent.setClass(ActivityMenuView.this, ActivityCameraView.class);
    				startActivity(intent);
					break;
				}
				case 3:
				{
					CusPreference cp = new CusPreference(ActivityMenuView.this);
                	if (cp.getUserName().equals("admin"))
                	{
                		//trigger
                		Intent intent = new Intent();

                		Bundle bundle= new Bundle();
                		bundle.putString("status", gw_status_text.getText().toString());
                		intent.putExtras(bundle);
                		
        				intent.setClass(ActivityMenuView.this, ActivityTriggerView.class);
        				startActivity(intent);
                	}
                	else
                	{
                		//setting
                		Intent intent = new Intent();

                		Bundle bundle= new Bundle();
                		bundle.putString("status", gw_status_text.getText().toString());
                		intent.putExtras(bundle);
                		
        				intent.setClass(ActivityMenuView.this, ActivityGatewayView.class);
        				startActivity(intent);
                	}
					break;
				}
				case 4:
				{
					CusPreference cp = new CusPreference(ActivityMenuView.this);
                	if (cp.getUserName().equals("admin"))
                	{
                		//schedule
                		Intent intent = new Intent();

                		Bundle bundle= new Bundle();
                		bundle.putString("status", gw_status_text.getText().toString());
                		intent.putExtras(bundle);
                		
        				intent.setClass(ActivityMenuView.this, ActivityScheduleView.class);
        				startActivity(intent);
                	}
                	else
                	{                		
                		cp.setIsPush(!cp.isPush());   
                		adapter.notifyDataSetChanged();
                		getNotifyList();
                	}
					break;
				}
				case 5:
				{
               		Intent intent = new Intent();
               		
               		Bundle bundle= new Bundle();
            		bundle.putString("status", gw_status_text.getText().toString());
            		intent.putExtras(bundle);
            		
        			intent.setClass(ActivityMenuView.this, ActivityGatewayView.class);
        			startActivity(intent);
                	
					break;
				}
				case 6:
				{
					CusPreference cp = new CusPreference(ActivityMenuView.this);
            		cp.setIsPush(!cp.isPush());                		
            		adapter.notifyDataSetChanged();
            		getNotifyList();
					break;
				}
				case 7:
				{
					Intent intent = new Intent();
               		
               		Bundle bundle= new Bundle();
            		bundle.putString("status", gw_status_text.getText().toString());
            		intent.putExtras(bundle);
            		
        			intent.setClass(ActivityMenuView.this, ActivityNotifyHistory.class);
        			startActivity(intent);
        			
					break;
				}
				default:
					break;
			}
		}
	};
	*/
	
	/*
	public class PictureListAdapter extends BaseAdapter
	{
		ViewHolder viewHolder;
        private LayoutInflater mInflater;      

        
        public PictureListAdapter(Context context, GridView gridView, List<String> items, List<String> paths)
        {
            mInflater = LayoutInflater.from(context);
        }
        
        private class ViewHolder 
        {
            ImageView menu_image;
            TextView text;
            RelativeLayout relativelayout;
        }
        
        @Override
        public int getCount() 
        {
        	CusPreference cp = new CusPreference(ActivityMenuView.this);
        	if (cp.getUserName().equals("admin"))
        		return 8;
        	else
        		return 5;
        }

        @Override
        public Object getItem(int position) 
        {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) 
        {
            // TODO Auto-generated method stub
            return position;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            // TODO Auto-generated method stub

            if (convertView == null) 
            {
            	convertView = mInflater.inflate(R.layout.item_menu,null);
            	
            	viewHolder = new ViewHolder();

        		viewHolder.menu_image= (ImageView) convertView.findViewById(R.id.imageView1);
        		viewHolder.text		 = (TextView)  convertView.findViewById(R.id.textView1);
        		viewHolder.relativelayout = (RelativeLayout) convertView.findViewById(R.id.relativelayout);
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
            
            switch (position)
            {
            	case 0:
            	{
            		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_control));
            		viewHolder.text.setText(R.string.menu_control);
            		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_control);
                    break;
            	}
            	case 1:
            	{
            		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_scene));
            		viewHolder.text.setText(R.string.menu_scene);
            		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_scene);
                    break;
            	}
            	case 2:
            	{
            		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_cam));
            		viewHolder.text.setText(R.string.menu_camera);
            		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_cam);
                    break;
            	}
            	case 3:
            	{
            		CusPreference cp = new CusPreference(ActivityMenuView.this);
                	if (cp.getUserName().equals("admin"))
                	{
                		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_trigger));
                		viewHolder.text.setText(R.string.menu_trigger);
                		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_trigger);
                	}
                	else
                	{
                		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_setting));
                		viewHolder.text.setText(R.string.menu_setting);
                		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_setting);
                	}
                    break;
            	}
            	case 4:
            	{
            		CusPreference cp = new CusPreference(ActivityMenuView.this);
                	if (cp.getUserName().equals("admin"))
                	{
                		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_schedule));
                		viewHolder.text.setText(R.string.menu_schedule);
                		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_schedule);
                	}
                	else
                	{
                		Boolean ispush=cp.isPush();
                		if (ispush)
                		{
                			viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_notify_on));                	
                    		viewHolder.text.setText(R.string.menu_notify_on);
                		}
                		else
                		{
                			viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_notify_off));
                    		viewHolder.text.setText(R.string.menu_notify_off);
                		}  
                		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_notify);
                	}
                    break;
            	}
            	case 5:
            	{
            		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_setting));
            		viewHolder.text.setText(R.string.menu_setting);
            		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_setting);
                    break;
            	}
            	case 6:
            	{
            		CusPreference cp = new CusPreference(ActivityMenuView.this);
            		Boolean ispush=cp.isPush();
            		if (ispush)
            		{
            			viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_notify_on));
                		viewHolder.text.setText(R.string.menu_notify_on);
            		}
            		else
            		{
            			viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_notify_off));
                		viewHolder.text.setText(R.string.menu_notify_off);
            		}
            		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_notify);
                    break;
            	}
            	case 7:
            	{
            		viewHolder.menu_image.setImageDrawable(getResources().getDrawable(R.drawable.menu_notify_history));
            		viewHolder.text.setText(R.string.menu_notify_history);
            		viewHolder.relativelayout.setBackgroundResource(R.drawable.selector_menu_notify_history);
            		break;
            	}
            	default :
            		break;
            }
            
            
            return convertView;   
        }
    }
	*/
	

	@SuppressWarnings("unused")
	private void getNotifyList()
	{
		new Thread()
		{
			public void run()
			{
				ArrayList<HashMap<String,String>> list,notify_list;
				
				notify_list= new ArrayList<HashMap<String,String>>();
				
				CusPreference cp = new CusPreference(ActivityMenuView.this);
				
				Map<String, String> map = new HashMap<String,String>();
				
				map.put("action","load");
				
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					//map.put("tunnelid", "0");
				}
				
				list = SendHttpCommand.getlist(
						
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"notify_list.cgi",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed(),"notify");
				
				
				if (list!=null)
				{
					for (HashMap<String,String> map1 : list) 
					{
						notify_list.add(map1);
					}
					
					Boolean have=false;
					String notify_id = null;
					
					for (HashMap<String,String> map1 : notify_list) 
					{
						//Log.v(TAG,"get id="+map1.get("device_id"));
						//Log.v(TAG,"id="+GCMessaging.getRegistrationId(ActivityMenuView.this));
						
						String GetID=map1.get("device_id");
						String MyID=GCMessaging.getRegistrationId(ActivityMenuView.this);
						
						if (GetID.equals(MyID))
						{
							have=true;
							notify_id=map1.get("id");
						}
					}
					
            		Boolean ispush=cp.isPush();
            		
            		if (have)
					{
            			if (ispush)
    					{
            				SWNotify_id("true" , notify_id);
            				Log.v(TAG,"SW on");
    					}
    					else
    					{
    						SWNotify_id("false" , notify_id);
    						Log.v(TAG,"SW off");
    					}
					}
					else
					{
						if (ispush)
    					{
							AddNotify_id(GCMessaging.getRegistrationId(ActivityMenuView.this));
							Log.v(TAG,"add SW");
    					}
    					else
    					{
    						if (cp.isPush())
    							GCMessaging.register(ActivityMenuView.this, GCMReceiver.SENDER_ID);
                    		else
                    			GCMessaging.unregister(ActivityMenuView.this);
    						
    						Log.v(TAG,"register SW");
    					}
					}
				}
			}
		}.start();
	}
	
	private void AddNotify_id(final String notify_id)
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityMenuView.this);
				
				Map<String, String> map = new HashMap<String,String>();
				
				map.put("action","add");
				map.put("name",cp.getUserName());
				map.put("device_type","android");
				map.put("device_id",notify_id);
				
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					//map.put("tunnelid", "0");
				}
				
				Boolean success=SendHttpCommand.send(
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"notify_list.cgi",
				map, 
				cp.getUserName(), 
				cp.getUserPwd(), 
				cp.isLocalUsed());
				
				//Log.v(TAG,notify_id);
				
				if (!success)
					AddNotify_id(notify_id);
				Log.v(TAG,"adding...");
			}
		}.start();
	}
	
	private void SWNotify_id(final String active , final String id)
	{
		Log.v(TAG,"id="+id+","+"active="+active);
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityMenuView.this);
				
				Map<String, String> map = new HashMap<String,String>();
				
				map.put("action","update");
				map.put("id",id);
				map.put("active",active);
				
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					//map.put("tunnelid", "0");
				}
				
				SendHttpCommand.send(
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"notify_list.cgi",
				map, 
				cp.getUserName(), 
				cp.getUserPwd(), 
				cp.isLocalUsed());
			}
		}.start();
	}
}
