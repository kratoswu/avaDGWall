package com.avadesign.ha.gateway;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.avadesign.ha.ActivityAbout;
import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.ServicePolling;

public class ActivityGatewayView extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
		
	//private Handler handler_update;//,handler;
	
	private TextView ip;
	private TextView mac;
	private TextView version;
	
	private Button setting,search,back,about,firmupdate,user_list,notify_list,change_user;
	
	private ToggleButton sw;
	
	private ProgressDialog mDialog_SPINNER;
	
	private String ver,md5;
	
	private LinearLayout Gateway_version_layout,button_layout2;
	
	//private FrameLayout user_list_layout,notify_list_layout,swap_layout,firm_layout;
	
	private boolean play=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gateway_view);
				
		FindView();
		
		Setlistener();
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
	}
	
	private void FindView()
	{
		ip=(TextView)findViewById(R.id.textView6);
		mac=(TextView)findViewById(R.id.textView4);
		version=(TextView)findViewById(R.id.textView2);
		
		sw=(ToggleButton)this.findViewById(R.id.ToggleButton01);
		
		change_user= (Button)this.findViewById(R.id.button_change);
		user_list= (Button)this.findViewById(R.id.button_acc);
		notify_list= (Button)this.findViewById(R.id.button_notify);
		firmupdate= (Button)this.findViewById(R.id.button_firm);

		
		setting= (Button)this.findViewById(R.id.tab_setting);
		search= (Button)this.findViewById(R.id.tab_search);
		back= (Button)this.findViewById(R.id.tab_back);
		about= (Button)this.findViewById(R.id.tab_about);
		
		Gateway_version_layout=(LinearLayout)findViewById(R.id.Gateway_version_layout);
		button_layout2=(LinearLayout)findViewById(R.id.button_layout2);
		
		//user_list_layout=(FrameLayout)findViewById(R.id.frame_layout_user);
		//notify_list_layout=(FrameLayout)findViewById(R.id.frame_layout_notify);
		//swap_layout=(FrameLayout)findViewById(R.id.frame_layout_swap);
		//firm_layout=(FrameLayout)findViewById(R.id.frame_layout_firm);
	}
	
	private void Setlistener()
	{
		
		change_user.setOnClickListener(button_down);
		user_list.setOnClickListener(button_down);
		notify_list.setOnClickListener(button_down);
		firmupdate.setOnClickListener(button_down);
		
		//change_user.setOnTouchListener(touch_down);
		//user_list.setOnTouchListener(touch_down);
		//notify_list.setOnTouchListener(touch_down);
		//firmupdate.setOnTouchListener(touch_down);
		
		sw.setOnCheckedChangeListener(sw_change);
		
		setting.setOnClickListener(tab_button_down);
		search.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		about.setOnClickListener(tab_button_down);
		
		setting.setTag(1);
		search.setTag(2);
		back.setTag(3);
		about.setTag(4);
		
		back.setVisibility(View.GONE);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
		
		CusPreference cp = new CusPreference(ActivityGatewayView.this);
		
		ip.setText(cp.getControllerIP());
		mac.setText(cp.getControllerMAC());
		version.setText(cp.getControllerVersion());
		
    	if (cp.getUserName().equals("admin"))
    	{
    		Gateway_version_layout.setVisibility(View.VISIBLE);
    		button_layout2.setVisibility(View.VISIBLE);
    		user_list.setVisibility(View.VISIBLE);
    		setting.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		Gateway_version_layout.setVisibility(View.GONE);
    		button_layout2.setVisibility(View.GONE);
    		user_list.setVisibility(View.INVISIBLE);
    		setting.setVisibility(View.GONE);
    	}
		
		sw.setChecked(!cp.isLocalUsed());
		
		play=true;
		//tab3.setPressed(true);
	}
	
	@Override
	protected void onPause() 
	{
		unregisterReceiver(mBroadcastReceiver);
		handler_update.removeCallbacksAndMessages(null);

		super.onPause();
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if(intent.getAction().equals(ServicePolling.HTTP_401))
			{
				DialogSetAuth.show(ActivityGatewayView.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivityGatewayView.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivityGatewayView.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{

			}
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler handler_update = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			if (msg.what==1)
			{
				String result = (String)msg.obj;
				
				ver=result;
				
				Log.v(TAG,"Ver="+ver);
				
				if (result.equals(version.getText().toString()))
				{
					AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityGatewayView.this);
					   delAlertDialog.setTitle(R.string.dialog_title_firm_cant);
					   delAlertDialog.setMessage(R.string.dialog_message_firm_cant);
					   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
					   {
						   public void onClick(DialogInterface arg0, int arg1) 
						   {
							   Log.v(TAG,"取消"); 
						   }
					   });
					   delAlertDialog.show();
				}
				else
				{
					AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityGatewayView.this);
					   delAlertDialog.setTitle(R.string.dialog_title_firm_can);
					   delAlertDialog.setMessage(getString(R.string.dialog_message_firm_can)+result);
					   delAlertDialog.setPositiveButton(R.string.alert_button_update, new DialogInterface.OnClickListener() 
					   {
						   public void onClick(DialogInterface arg0, int arg1) 
						   {
							   GetTheMD5();
						   }
					   });
					   
					   delAlertDialog.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() 
					   {
						   public void onClick(DialogInterface arg0, int arg1) 
						   {
							   Log.v(TAG,"取消");
						   }
					   });
					   delAlertDialog.show();
				}
			}
			else if (msg.what==2)
			{
				String result = (String)msg.obj;
				
				md5=result.substring(0, 32);
				
				Log.v(TAG,"MD5="+md5);
				
				SendUpdate();
			}
			else
			{
				AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityGatewayView.this);
				   delAlertDialog.setTitle(R.string.dialog_title_firm_done);
				   delAlertDialog.setMessage(R.string.dialog_message_firm_done);
				   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface arg0, int arg1) 
					   {
						   
					   }
				   });
			}
		}
	};
	
	/*
	private Button.OnTouchListener touch_down = new Button.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event) 
		{
			if (v==change_user)
			{
				if (event.getAction()==MotionEvent.ACTION_DOWN)
					swap_layout.setPressed(true);
				else if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
					swap_layout.setPressed(false);
			}
			else if (v==user_list)
			{
				if (event.getAction()==MotionEvent.ACTION_DOWN)
					user_list_layout.setPressed(true);
				else if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
					user_list_layout.setPressed(false);
			}
			else if (v==notify_list)
			{
				if (event.getAction()==MotionEvent.ACTION_DOWN)
					notify_list_layout.setPressed(true);
				else if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
					notify_list_layout.setPressed(false);
			}
			else if (v==firmupdate)
			{
				if (event.getAction()==MotionEvent.ACTION_DOWN)
					firm_layout.setPressed(true);
				else if (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
					firm_layout.setPressed(false);
			}
			
			if (event.getAction()==MotionEvent.ACTION_UP)
			{
				if (v==firmupdate)
				{
					GetTheVer();
				}
				else if (v==user_list)
				{
					Intent intent = new Intent();
					intent.setClass(ActivityGatewayView.this, ActivityUserList.class);
					startActivity(intent);
				}
				else if (v==notify_list)
				{
					Intent intent = new Intent();
					intent.setClass(ActivityGatewayView.this, ActivityNotifyList.class);
					startActivity(intent);
				}
				else if (v==change_user)
				{
					acc_pwd_show();
				}
			}
			
			return false;
		}
	};
	*/
	
	private ToggleButton.OnCheckedChangeListener sw_change = new ToggleButton.OnCheckedChangeListener() 
	{ 
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
		{
			CusPreference cp = new CusPreference(ActivityGatewayView.this);
			
			cp.setStopPolling(true);
			cp.setIsLocalUsed(!arg1);
			
			if (play)
				PlayButtonSound.play(ActivityGatewayView.this);
		};
	};
	
	
	private Button.OnClickListener button_down = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			if (v==firmupdate)
			{
				GetTheVer();
			}
			else if (v==user_list)
			{
				Intent intent = new Intent();
				intent.setClass(ActivityGatewayView.this, ActivityUserList.class);
				startActivity(intent);
			}
			else if (v==notify_list)
			{
				Intent intent = new Intent();
				intent.setClass(ActivityGatewayView.this, ActivityNotifyList.class);
				startActivity(intent);
			}
			else if (v==change_user)
			{
				acc_pwd_show();
			}
			
			PlayButtonSound.play(ActivityGatewayView.this);
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
					Intent intent = new Intent();
					intent.setClass(ActivityGatewayView.this, ActivityGatewayControl.class);
					startActivity(intent);
					break;
				}
				case 2:
				{
					Intent intent = new Intent();
					intent.setClass(ActivityGatewayView.this, ActivityGatewaySearch.class);
					startActivity(intent);
					break;
				}
				case 3:
				{
					finish();
					break;
				}
				case 4:
				{
					Intent intent = new Intent();
					intent.setClass(ActivityGatewayView.this, ActivityAbout.class);
					startActivity(intent);
					break;
				}
				default:
					break;
			}
			
			PlayButtonSound.play(ActivityGatewayView.this);
		}
	};
	
	private void acc_pwd_show()
	{
		final CusPreference cp = new CusPreference(ActivityGatewayView.this);
		
		View view= View.inflate(ActivityGatewayView.this,R.layout.dialog_set_auth,null);
		
		final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
		final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
		
		uname_edit.setText(cp.getUserName());
		upwd_edit.setText(cp.getUserPwd());
		
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(ActivityGatewayView.this);
		
		builder.setTitle(ActivityGatewayView.this.getText(R.string.dialog_title_change_user));
		builder.setView(view);
		builder.setMessage(ActivityGatewayView.this.getText(R.string.dialog_message_change_user));
		builder.setCancelable(false);
		builder.setPositiveButton(ActivityGatewayView.this.getText(R.string.alert_button_ok), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				CusPreference cp = new CusPreference(ActivityGatewayView.this);
				cp.setUserName(uname_edit.getText().toString());
				cp.setUserPwd(upwd_edit.getText().toString());
				
				if (cp.getUserName().equals("admin"))
		    	{
		    		Gateway_version_layout.setVisibility(View.VISIBLE);
		    		button_layout2.setVisibility(View.VISIBLE);
		    		user_list.setVisibility(View.VISIBLE);
		    		setting.setVisibility(View.VISIBLE);
		    	}
		    	else
		    	{
		    		Gateway_version_layout.setVisibility(View.GONE);
		    		button_layout2.setVisibility(View.GONE);
		    		user_list.setVisibility(View.INVISIBLE);
		    		setting.setVisibility(View.GONE);
		    	}
				//mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
				//mDialog_SPINNER.show();
				//SaveAcc_PwdCommand(uname_edit.getText().toString(),upwd_edit.getText().toString());
			}
		});
		builder.setNegativeButton(ActivityGatewayView.this.getText(R.string.alert_button_cancel), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				//return false;
			}
		});
		
		builder.create().show();
	}

	/*
	private void SaveAcc_PwdCommand(final String acc, final String pwd)
	{
		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				
				String[] result = (String[])msg.obj;
				
				Log.v(TAG,result[0]);
				Log.v(TAG,result[1]);
				Log.v(TAG,result[2]);
				
				if (result[0].equals("true"))
				{
					CusPreference cp = new CusPreference(ActivityGatewayView.this);
					cp.setUserName(result[1]);
					cp.setUserPwd(result[2]);
					cp.setStopPolling(false);
					
					mDialog_SPINNER.dismiss();
				}
			}
		};
		
		new Thread()
		{
			public void run()
			{
				boolean result;
				CusPreference cp = new CusPreference(ActivityGatewayView.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("account",acc);
				map.put("password",pwd);
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", "admin");
					map.put("userpwd", "admin");
				}
				cp.setStopPolling(true);
				result=SendHttpCommand.send(String.format(cp.isLocalUsed()?getString(R.string.local_url_syntax):getString(R.string.server_url_syntax),cp.getControllerIP(),String.valueOf(cp.getControllerPort()))+"authpost.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
				
				String [] string_array={String.valueOf(result),acc,pwd};
				
				Message message = handler.obtainMessage(1,string_array);
				handler.sendMessage(message);
			}
		}.start();
	}
	*/
	
	private void GetTheVer()
	{	
		new Thread()
		{
			public void run()
			{
				String result;
	
				result=SendHttpCommand.update("http://220.135.186.178/zwave/update/version.txt");
				
				Message message = handler_update.obtainMessage(1,result);
				handler_update.sendMessage(message);
				
				//Log.v(TAG,result);
			}
		}.start();
	}
	
	private void GetTheMD5()
	{	
		new Thread()
		{
			public void run()
			{
				String result;
	
				result=SendHttpCommand.update("http://220.135.186.178/zwave/update/md5.txt");
				
				Message message = handler_update.obtainMessage(2,result);
				handler_update.sendMessage(message);
				
				//Log.v(TAG,result);
			}
		}.start();
	}
	
	private void SendUpdate()
	{	
		new Thread()
		{
			public void run()
			{
				boolean result;
	
				//result=SendHttpCommand.update("http://220.135.186.178/zwave/update/md5.txt");

				CusPreference cp = new CusPreference(ActivityGatewayView.this);
				Map<String, String> map = new HashMap<String,String>();
				map.put("url","http://220.135.186.178/zwave/update/update.pkg");
				map.put("md5",md5);
				
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					map.put("tunnelid", "0");
				}
				cp.setStopPolling(true);
				
				result=SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"update.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
				
				
				if (result)
				{
					Message message = handler_update.obtainMessage(3,"ok");
					handler_update.sendMessage(message);
				}
			}
		}.start();
	}
}
