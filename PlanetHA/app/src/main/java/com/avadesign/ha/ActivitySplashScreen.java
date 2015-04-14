package com.avadesign.ha;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.avadesign.ha.frame.GCMReceiver;
import com.avadesign.ha.frame.avacontrol_service;
import com.google.android.gcm.GCMessaging;

public class ActivitySplashScreen extends Activity 
{
	//private final String TAG = this.getClass().getSimpleName();
	private Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitysplashscreen);

		Intent intent_startService = new Intent(ActivitySplashScreen.this, avacontrol_service.class);
		startService(intent_startService);
		
		GCMessaging.register(this, GCMReceiver.SENDER_ID);
		
		//Log.v("C2DMReceiver","action="+intent_startService.getAction());
		
		timer= new Timer();
		timer.schedule(mTimerTask, 1500);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	
	@Override
	public void onBackPressed() 
	{
		
	}
	
	private TimerTask mTimerTask=new TimerTask()
	{
		@Override
		public void run() 
		{
			timer.cancel();
			
			Intent intent = new Intent();
			intent.setClass(ActivitySplashScreen.this ,ActivityMenuView.class);
			startActivity(intent);
			
			finish();
		}
	};
}
