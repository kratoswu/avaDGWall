package com.avadesign.ha;

import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
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
import com.avadesign.ha.frame.CheckMyId;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.avacontrol_service;
import com.avadesign.ha.gateway.ActivityGatewayView;
import com.avadesign.ha.login.ActivityLogin;
import com.avadesign.ha.report.ActivityReport;
import com.avadesign.ha.room.ActivityRoomList;
import com.avadesign.ha.scene.ActivitySceneView;
import com.avadesign.ha.schedule.ActivityScheduleView;
import com.avadesign.ha.trigger.ActivityTriggerView;

public class ActivityMenuView extends BaseActivity
{
	private DatagramSocket mDSocket;
	private ReceiveCommandTask mReceiveCommandTask = null;
	private final int finderTimeot = 2000;

	private Button button_control,button_scene,button_camera,button_trigger,button_schedule,button_setting,button_history,button_report;
	private LinearLayout layout3,layout5;

	private long currentTime;

	private Handler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_view);

		FindView();

		Setlistener();

		CheckIconFile();

		CheckMyId.Check_and_Set_PushId(ActivityMenuView.this);

		findController();

		//boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

		//Log.v(TAG,"sdCardExist="+sdCardExist);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		//SetView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		Log.v(TAG,"resultCode="+resultCode);

		if (resultCode==888)
		{
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putBoolean("auto_login", false);
			intent.putExtras(bundle);
			intent.setClass(ActivityMenuView.this ,ActivityLogin.class);
			startActivity(intent);

			cp.setControllerMAC("");

			finish();
		}
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
				//CusPreference cp = new CusPreference(ActivityMenuView.this);

				String xml_str = SendHttpCommand.get_histiry(
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"image/image_list.xml"+(cp.isLocalUsed()?"":"?mac="+cp.getControllerMAC()+"&username="+cp.getControllerAcc()+"&userpwd="+cp.getControllerPwd()/*+"&tunnelid=0"*/),
						cp.getControllerAcc(),
						cp.getControllerPwd());
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
	private static class MyHandler extends Handler
	{
		private final WeakReference<ActivityMenuView> mActivity;

	    public MyHandler(ActivityMenuView activity)
	    {
	        mActivity = new WeakReference<ActivityMenuView>(activity);
	    }

	    @SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);

			ActivityMenuView MyActivity = mActivity.get();

			if (msg.what==1)
			{
				ArrayList<HashMap<String,String>> image_array = (ArrayList<HashMap<String,String>>)msg.obj;

				if (image_array!=null)
				{
					ArrayList<HashMap<String,String>> old_array=MyActivity.cp.getIcon_Image();

					if (old_array!=null)
					{
						Log.v(MyActivity.TAG,""+old_array.size());

						if (old_array.size()<image_array.size() && image_array.size()!=0)
						{
							MyActivity.cp.setUpdateTime(MyActivity.currentTime);

							//Log.v(TAG,"download view");

							Intent intent = new Intent();
							Bundle bundle= new Bundle();
							bundle.putSerializable("image_array", image_array);
			        		intent.putExtras(bundle);
		    				intent.setClass(MyActivity, ActivityDownloadView.class);
		    				MyActivity.startActivity(intent);
						}
					}
					else
					{
						Log.v(MyActivity.TAG,""+image_array.size());
						MyActivity.cp.setUpdateTime(MyActivity.currentTime);

						//Log.v(TAG,"download view");

						Intent intent = new Intent();
						Bundle bundle= new Bundle();
						bundle.putSerializable("image_array", image_array);
		        		intent.putExtras(bundle);
	    				intent.setClass(MyActivity, ActivityDownloadView.class);
	    				MyActivity.startActivity(intent);
					}

					MyActivity.cp.setIcon_Image(image_array);
				}
				else
				{
					Log.v(MyActivity.TAG,"image=null");

					image_array=new ArrayList<HashMap<String,String>>();

					MyActivity.cp.setIcon_Image(image_array);
				}
			}
		}
	}

	private void SetView()
	{
		CusPreference cp = new CusPreference(ActivityMenuView.this);

    	if (cp.getControllerAcc().equals("admin"))
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
					Intent intent = new Intent();
        			intent.setClass(ActivityMenuView.this, ActivityTriggerView.class);
        			startActivity(intent);
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
    				startActivityForResult(intent,0);
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

	private void findController()
	{
		callProgress();
		try
		{
			mDSocket = new DatagramSocket(10000);

			mReceiveCommandTask = new ReceiveCommandTask();
			mReceiveCommandTask.execute();

			Handler mHandler=new Handler();
			mHandler.postDelayed(timeoutRunnable, finderTimeot);

			new Thread(broCommandRunnable).start();
		}
		catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"Error:"+e.toString());
		}
	}

	private Runnable broCommandRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				byte[] msg = new String("WHOIS_AVA_ZWAVE#").getBytes();
				DatagramPacket mDP = new DatagramPacket(msg, msg.length,InetAddress.getByName("255.255.255.255"), 10000);

				int i=0;
				while(i<3)
				{
					mDSocket.send(mDP);
					Thread.sleep(500);
					i++;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d(TAG,"Error:"+e.toString());
			}
		}
	};

	private Runnable timeoutRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				if(mDSocket!=null)
				{
					mDSocket.close();

					cancelProgress();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d(TAG,"Error:"+e.toString());
			}
		}
	};

	private class ReceiveCommandTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... args)
		{
			MulticastLock mlock = null;
			try
			{
				WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    			mlock = wifi.createMulticastLock(TAG);
    			mlock.acquire();

    			while(!mDSocket.isClosed())
    			{
    				DatagramPacket receiveDP = new DatagramPacket(new byte[1024], 1024);
    				mDSocket.receive(receiveDP);

    				String re = new String(receiveDP.getData(), 0, receiveDP.getLength());

    				if(re.startsWith("RE_WHOIS_AVA_ZWAVE#"))
    				{
    					String [] receive=re.split("#");

    					String [] info=receive[1].split("&");


    					HashMap<String,Object> map = new HashMap<String,Object>();
    					map.put("IP", receiveDP.getAddress().toString().substring(1));
    					map.put("PORT", String.valueOf(receiveDP.getPort()));

    					for(String str : info)
    					{
    						str=str.replace("\r", "").replace("\n", "");

    						String keyValue[]=str.split("=");

    						map.put(keyValue[0].toUpperCase(),keyValue[1]);
    					}

    					/*
    					String mac = re.substring(re.indexOf("mac=")+4).replace(":", "").replace("\r","").replace("\n","");
    					String ip = receiveDP.getAddress().toString().substring(1);
    					String version = re.substring(re.indexOf("version=")+8,re.indexOf("&mac="));
    					*/

    					String mac=map.get("MAC").toString();
    					String mac1=cp.getControllerMAC();

    					Log.v(TAG,"mac0="+mac+", len="+mac.length());
    					Log.v(TAG,"mac1="+mac1+", len="+mac1.length());



    					if(mac.equals(mac1))
						{
    						Log.v(TAG,"local...");
    						cp.setIsLocalUsed(true);
							cp.setControllerIP(map.get("IP").toString());
							cp.setControllerVersion(map.get("VERSION").toString());
						}
    				}
				}

    			mDSocket.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d(TAG,"Error:"+e.toString());
			}

			finally
			{
				if(mlock!=null)
				{
					mlock.release();
					mlock = null;
				}
				if(mDSocket!=null)
				{
					mDSocket.close();
					mDSocket = null;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String re)
		{

		}
	}
}
