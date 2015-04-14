package com.avadesign.ha.frame;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class avacontrol_service extends Service 
{
	private final String TAG = this.getClass().getSimpleName();
	
	@Override
	public void onCreate() 
	{
		Log.d(TAG,"avacontrol_service start");
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		Log.d(TAG,"avacontrol_service stop");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
