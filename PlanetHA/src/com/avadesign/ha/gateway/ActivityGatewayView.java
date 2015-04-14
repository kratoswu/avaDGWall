package com.avadesign.ha.gateway;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.avadesign.ha.ActivityAbout;
import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.CheckMyId;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.login.ActivityGatewayList;

public class ActivityGatewayView extends BaseActivity 
{
	private TextView ip;
	private TextView mac;
	private TextView version;
	
	private TextView lan,wan;
	
	private Switch sw;
	
	private Button back,about,setting,firmupdate,user_list,notify_list,change_user,button_notify;
	
	private String ver,md5;
	
	//private LinearLayout Gateway_version_layout,button_layout2;
	
	private Handler handler_update = new MyHandler(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gateway_view);
				
		FindView();
		
		Setlistener();
		
		CheckMyId.Check_and_Set_PushId(ActivityGatewayView.this);
	}
	
	private void FindView()
	{
		ip=(TextView)findViewById(R.id.textView6);
		mac=(TextView)findViewById(R.id.textView4);
		version=(TextView)findViewById(R.id.textView2);
		
		lan=(TextView)findViewById(R.id.TextView03);
		wan=(TextView)findViewById(R.id.TextView01);
		
		sw=(Switch)this.findViewById(R.id.switch1);
		
		change_user= (Button)this.findViewById(R.id.button_change);
		user_list= (Button)this.findViewById(R.id.button_acc);
		notify_list= (Button)this.findViewById(R.id.button_notify);
		firmupdate= (Button)this.findViewById(R.id.button_firm);
		setting= (Button)this.findViewById(R.id.button_setting);
		button_notify= (Button)this.findViewById(R.id.button_notification);
		
		
		back= (Button)this.findViewById(R.id.tab_back);
		about= (Button)this.findViewById(R.id.tab_about);
		
		//Gateway_version_layout=(LinearLayout)findViewById(R.id.Gateway_version_layout);
		//button_layout2=(LinearLayout)findViewById(R.id.button_layout2);
		
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
		button_notify.setOnClickListener(button_down);
		setting.setOnClickListener(button_down);
		
		sw.setOnCheckedChangeListener(sw_change);
		
		back.setOnClickListener(tab_button_down);
		about.setOnClickListener(tab_button_down);
		
		about.setTag(1);
		back.setTag(2);
		
		back.setVisibility(View.GONE);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		ip.setText(cp.getControllerIP());
		mac.setText(cp.getControllerMAC());
		version.setText(cp.getControllerVersion());
		
		Log.v(TAG,"cp.getControllerAcc()="+cp.getControllerAcc());
		
    	if (cp.getControllerAcc().equals("admin"))
    	{
    		setting.setEnabled(true);
    		firmupdate.setEnabled(true);
    		user_list.setEnabled(true);
    		notify_list.setEnabled(true);
    		
    		setting.getBackground().setAlpha(255);
    		firmupdate.getBackground().setAlpha(255);
    		notify_list.getBackground().setAlpha(255);
    		user_list.getBackground().setAlpha(255);
    		
//    		Drawable [] setting_d		=setting.getCompoundDrawables();
//    		Drawable [] firmupdate_d	=firmupdate.getCompoundDrawables();
//    		Drawable [] notify_list_d	=notify_list.getCompoundDrawables();
//    		Drawable [] user_list_d		=user_list.getCompoundDrawables();
//    		
//    		setting_d[1].setAlpha(255);
//    		firmupdate_d[1].setAlpha(255);
//    		notify_list_d[1].setAlpha(255);
//    		user_list_d[1].setAlpha(255);
    	}
    	else
    	{
    		setting.setEnabled(false);
    		firmupdate.setEnabled(false);
    		user_list.setEnabled(false);
    		notify_list.setEnabled(false);
    		
    		setting.getBackground().setAlpha(150);
    		firmupdate.getBackground().setAlpha(150);
    		notify_list.getBackground().setAlpha(150);
    		user_list.getBackground().setAlpha(150);
    		
//    		Drawable [] setting_d		=setting.getCompoundDrawables();
//    		Drawable [] firmupdate_d	=firmupdate.getCompoundDrawables();
//    		Drawable [] notify_list_d	=notify_list.getCompoundDrawables();
//    		Drawable [] user_list_d		=user_list.getCompoundDrawables();
//    		
//    		setting_d[1].setAlpha(150);
//    		firmupdate_d[1].setAlpha(150);
//    		notify_list_d[1].setAlpha(150);
//    		user_list_d[1].setAlpha(150);

    	}
		
    	if (cp.isLocalUsed())
    	{
    		lan.setTextColor(getResources().getColor(R.color.lan_wan_color));
    		wan.setTextColor(getResources().getColor(R.color.encode_view));
    	}
    	else
    	{
    		lan.setTextColor(getResources().getColor(R.color.encode_view));
    		wan.setTextColor(getResources().getColor(R.color.lan_wan_color));
    	}
    	
    	sw.setChecked(!cp.isLocalUsed());
    	
		
		refresh_notification();
	}
		
	private void refresh_notification()
	{
		if (cp.isPush())
		{
//			button_notify.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.menu_notify_on), null, null);
		    button_notify.setBackgroundResource(R.drawable.menu_notify_on);
			button_notify.setText(R.string.menu_notify_on);
		}
		else
		{
//			button_notify.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.menu_notify_off), null, null);
		    button_notify.setBackgroundResource(R.drawable.menu_notify_off);
			button_notify.setText(R.string.menu_notify_off);
		}
	}

	private static class MyHandler extends Handler
	{
		private final WeakReference<Activity> mActivity;
		
	    public MyHandler(Activity activity) 
	    {
	        mActivity = new WeakReference<Activity>(activity);
	    }
	    
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			final ActivityGatewayView activity = (ActivityGatewayView) mActivity.get();
			
			if (msg.what==1)
			{
				String result = (String)msg.obj;
				
				activity.ver=result;
				
				Log.v(activity.TAG,"�̷s����="+result);
				
				Log.v(activity.TAG,"�ثe����="+activity.cp.getControllerVersion());

				if (result.equalsIgnoreCase(activity.version.getText().toString()))
				{
					AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(activity);
					   delAlertDialog.setTitle(R.string.dialog_title_firm_cant);
					   delAlertDialog.setMessage(R.string.dialog_message_firm_cant);
					   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
					   {
						   public void onClick(DialogInterface arg0, int arg1) 
						   {
							   Log.v(activity.TAG,"���"); 
						   }
					   });
					   delAlertDialog.show();
				}
				else
				{
					AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(activity);
					   delAlertDialog.setTitle(R.string.dialog_title_firm_can);
					   delAlertDialog.setMessage(activity.getString(R.string.dialog_message_firm_can)+result);
					   delAlertDialog.setPositiveButton(R.string.alert_button_update, new DialogInterface.OnClickListener() 
					   {
						   public void onClick(DialogInterface arg0, int arg1) 
						   {
							   activity.GetTheMD5();
						   }
					   });
					   
					   delAlertDialog.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() 
					   {
						   public void onClick(DialogInterface arg0, int arg1) 
						   {
							   Log.v(activity.TAG,"���");
						   }
					   });
					   delAlertDialog.show();
				}
			}
			else if (msg.what==2)
			{
				String result = (String)msg.obj;
				
				activity.md5=result.substring(0, 32);
				
				Log.v(activity.TAG,"MD5="+activity.md5);
				
				activity.SendUpdate();
			}
			else
			{
				AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(activity);
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
	}

	
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
	
	private Switch.OnCheckedChangeListener sw_change = new Switch.OnCheckedChangeListener() 
	{ 
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
		{
			cp.setIsLocalUsed(!arg1);
			
			if (cp.isLocalUsed())
			{
				lan.setTextColor(getResources().getColor(R.color.lan_wan_color));
				wan.setTextColor(getResources().getColor(R.color.encode_view));
			}
			else
			{
				lan.setTextColor(getResources().getColor(R.color.encode_view));
				wan.setTextColor(getResources().getColor(R.color.lan_wan_color));
			}
			
			Log.v(TAG,"islocal="+cp.isLocalUsed());
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
				/*
				Intent intent = new Intent();
				intent.setClass(ActivityGatewayView.this, ActivityNotifyList.class);
				startActivity(intent);
				*/
				ArrayList<HashMap<String,String>> acclist = new ArrayList<HashMap<String,String>>();
				
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("gateway_list", acclist);
				bundle.putBoolean("new", false);
				intent.putExtras(bundle);
				intent.setClass(ActivityGatewayView.this ,ActivityGatewayList.class);
				startActivity(intent);
			}
			else if (v==change_user)
			{
				//acc_pwd_show();
				setResult(888,null);
				finish();
			}
			else if (v==button_notify)
			{
				cp.setIsPush(!cp.isPush());
				refresh_notification();
				CheckMyId.Check_and_Set_PushId(ActivityGatewayView.this);
			}
			else if (v==setting)
			{
				Intent intent = new Intent();
				intent.setClass(ActivityGatewayView.this, ActivityGatewayControl.class);
				startActivity(intent);
			}
		}
	};
	
	private Button.OnClickListener tab_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer) v.getTag())
			{			
				/*
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
				*/
				case 2:
				{
					finish();
					break;
				}
				case 1:
				{
					Intent intent = new Intent();
					intent.setClass(ActivityGatewayView.this, ActivityAbout.class);
					startActivity(intent);
					break;
				}
				default:
					break;
			}
			
			
		}
	};
	
	/*
	private void acc_pwd_show()
	{
		View view= View.inflate(ActivityGatewayView.this,R.layout.dialog_set_auth,null);
		
		final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
		final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
		
		uname_edit.setText(cp.getControllerAcc());
		upwd_edit.setText(cp.getControllerPwd());
		
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
				cp.setControllerAcc(uname_edit.getText().toString());
				cp.setControllerPwd(upwd_edit.getText().toString());
				
				if (cp.getControllerAcc().equals("admin"))
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
*/
	
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
						cp.getControllerAcc(), 
						cp.getControllerPwd(), 
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
					map.put("username", cp.getControllerAcc());
					map.put("userpwd", cp.getControllerPwd());
					map.put("tunnelid", "0");
				}
				cp.setStopPolling(true);
				
				result=SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"update.html",
						map, 
						cp.getControllerAcc(), 
						cp.getControllerPwd(), 
						cp.isLocalUsed());
				
				Log.v(TAG,"result="+result);
				
				if (result)
				{
					Message message = handler_update.obtainMessage(3,"ok");
					handler_update.sendMessage(message);
				}
			}
		}.start();
	}
}
