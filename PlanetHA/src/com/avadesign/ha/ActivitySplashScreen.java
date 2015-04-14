package com.avadesign.ha;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;

import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.GCMReceiver;
import com.avadesign.ha.frame.avacontrol_service;
import com.avadesign.ha.login.ActivityLogin;
import com.google.android.gcm.GCMessaging;

public class ActivitySplashScreen extends BaseActivity 
{
	private Timer timer;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitysplashscreen);

		Intent intent_startService = new Intent(ActivitySplashScreen.this, avacontrol_service.class);
		startService(intent_startService);
		
		GCMessaging.register(this, GCMReceiver.SENDER_ID);
		
		timer= new Timer();
		timer.schedule(mTimerTask, 1500);
		
		//
		//CheckMyId.Check_and_Set_PushId(ActivitySplashScreen.this);
		
	}
	
	private TimerTask mTimerTask=new TimerTask()
	{
		@Override
		public void run() 
		{
			//timer.cancel();
			
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putBoolean("auto_login", true);
			intent.putExtras(bundle);
			intent.setClass(ActivitySplashScreen.this ,ActivityLogin.class);
			startActivity(intent);
			
			finish();
		}
	};
}
