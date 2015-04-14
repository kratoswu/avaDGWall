package com.avadesign.ha;

import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class EventAlert extends Activity 
{
private static final String TAG = EventAlert.class.getCanonicalName();
	
	private PowerManager.WakeLock mWakeLock;
	@SuppressWarnings("deprecation")
	private KeyguardLock Keylock;
	
	private TextView alertContent_txt;
	private TextView alertTime_txt;
	
	private Button ok_btn,cancel_btn;
	
	private Handler mHandler = new Handler();
	
	private final int alertTime = 10000;
	
	public static final String MSG_KEY = "MSG_KEY";
	public static final String TYPE_KEY = "TYPE_KEY";

	private String dialogMsg = "";
	
	private CountDownTimer cTimer;
	
	//private final KeyguardManager keyguardManager = avaha.getKeyguardManager();

	private MediaPlayer media;
	private Vibrator vibrate;
	
	private Bundle bundle;
	private boolean hasAlarm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.eventalert);
        
        Log.v(TAG,"eventalert");
        
        alertContent_txt = (TextView)this.findViewById(R.id.screen_mycam_alert_content_txt);
        alertTime_txt = (TextView)this.findViewById(R.id.screen_mycam_alert_time_txt);
        
        ok_btn = (Button)this.findViewById(R.id.screen_mycam_alert_ok_btn);

        ok_btn.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View arg0) 
        	{
        		Boolean hasService=false;
        		
        		ActivityManager am = (ActivityManager)EventAlert.this.getSystemService(Context.ACTIVITY_SERVICE);
        	    List<RunningServiceInfo> list = am.getRunningServices(100);   
        	    for (RunningServiceInfo info : list) 
        	    {
        	    	//Log.v(TAG,"service="+info.service.getClassName());
        	    	
        	    	if (info.service.getClassName().equalsIgnoreCase("com.avadesign.ha.avacontrol_service"))
        	    		hasService=true;
        	    }
        	    
        	    if (!hasService)
        	    {
        	    	Intent i = new Intent(EventAlert.this, ActivitySplashScreen.class);
        	    	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	    	startActivity(i);
        	    }

				//cTimer.cancel();
        		//mHandler.removeCallbacks(reenableKG);
        		//mute();
        		
        	    onStop();
        	    finish();
        	}
        });
        
        cTimer = new CountDownTimer(alertTime,1000)
        {
            @Override
            public void onFinish() {
				
				reenableKG();
				mute();
            }

            @Override
            public void onTick(long millisUntilFinished) 
            {
            	alertTime_txt.setText(String.valueOf((int)(millisUntilFinished/1000)));
            }
        };
        
        bundle = getIntent().getExtras();
        hasAlarm = false;
    }
	
	@Override
	protected void onNewIntent(Intent intent) 
	{
		super.onNewIntent(intent);
		bundle = intent.getExtras();
		hasAlarm = false;
	}
	
	private void setView()
	{
		
		//mute_btn.setVisibility(View.VISIBLE);
		alertTime_txt.setVisibility(View.VISIBLE);
		
        alertContent_txt.setText(dialogMsg);
    }

	private Runnable reenableKG = new Runnable()
	{
		@Override
		public void run() {
			reenableKG();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		
		if(hasAlarm)
			return;
			
		String jsonMsg = bundle.getString(MSG_KEY);
		
		Log.v(TAG,"push="+jsonMsg);
		
		try{
			JSONParser parser = new JSONParser();
			Map json = (Map)parser.parse(jsonMsg);
			Map object = (Map)parser.parse(json.get("object").toString());
			
			//String time =  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(Long.parseLong(object.get("time").toString())*1000);
			//String mac = object.get("mac").toString();
			String Name = object.get("device_name").toString();
			//String type = object.get("type").toString();
			
	        dialogMsg = String.format(Name+" is alert");

			cTimer.start();
			mHandler.postDelayed(reenableKG, alertTime);
			
			alarm();
			disableKG();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		setView();
	}
	
	@Override
	protected void onStop() {
		cTimer.cancel();
		mHandler.removeCallbacks(reenableKG);
		
		mute();
		reenableKG();
	
		hasAlarm = true;
		
		super.onStop();
	}

	private void alarm()
	{
		mute();
		
		media = new MediaPlayer();
		media=MediaPlayer.create(this, R.raw.a010);
		if (media != null)
        	media.stop();
		
		try {
			media.prepare();
			media.setLooping(true);
 			media.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//play sound
        
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);  
   	 	vibrate.vibrate(new long[]{0,250,125,250,125,250,500}, 0);
	}
		
	private void mute() {
		if(media!=null){
			media.stop();
			media.release();
			media=null;
		}
		
		if(vibrate!=null){
			vibrate.cancel();
			vibrate=null;
		}
		
		hasAlarm = true;
	}
	
	private void disableKG()
	{
		reenableKG();
		
		KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
		if(keyguardManager != null){
			if(Keylock == null){
				Keylock = keyguardManager.newKeyguardLock(TAG);
			}
		
			Keylock.disableKeyguard();
		}
		
		PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		if(powerManager != null && mWakeLock == null)
		{
			mWakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
			if(mWakeLock != null)
			{
				mWakeLock.acquire();
			}
		}

	}
	
	private void reenableKG()
	{
		if(mWakeLock != null && mWakeLock.isHeld())
		{
			mWakeLock.release();
			mWakeLock=null;
		}
		
		if(Keylock!=null)
		{
			Keylock.reenableKeyguard();
			Keylock=null;
		}
	}

	@Override
	public void onBackPressed() {
		//cancel the event
	}
}
