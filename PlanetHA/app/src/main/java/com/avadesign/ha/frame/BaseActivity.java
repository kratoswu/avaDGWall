package com.avadesign.ha.frame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.avadesign.ha.R;

public class BaseActivity extends Activity 
{
	protected final String TAG = this.getClass().getSimpleName();
	
	protected double screenInches;
	
	protected ProgressDialog mDialog_SPINNER;
	
	protected CusPreference cp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//§PÂ_¿Ã¹õ¤j¤p		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		double x = Math.pow(dm.widthPixels/dm.xdpi,2);
		double y = Math.pow(dm.heightPixels/dm.ydpi,2);
		screenInches = Math.sqrt(x+y);
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
		
		cp = new CusPreference(this);
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	protected void RegisterBroadcast()
	{
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
	}
		
	protected void UnRegisterBroadcast()
	{
		unregisterReceiver(mBroadcastReceiver);
	}
	
	protected void StartService()
	{
		cp.setStopPolling(false);
		
		Intent intent_startService = new Intent(this, ServicePolling.class);
		startService(intent_startService);
	}
	
	protected void StopService()
	{
		cp.setStopPolling(true);
		
		Intent intent_stopService = new Intent(this, ServicePolling.class);
		stopService(intent_stopService);
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if(intent.getAction().equals(ServicePolling.HTTP_401))
			{
				call401();
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				call404();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
				callBroadcastdone();
			}
		}
	};
	
	protected void callBroadcastdone()
	{
		
	}
	
	protected void callProgress() 
	{
		mDialog_SPINNER.show();	
	}
	
	protected void cancelProgress() 
	{
		mDialog_SPINNER.dismiss();
	}
	
	protected void call401()
	{
		DialogSetAuth.show(this);
	}
	
	protected void call404()
	{
		CusPreference cp = new CusPreference(this);
		if(!cp.getControllerIP().equals(""))
			Toast.makeText(this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
	}
}
