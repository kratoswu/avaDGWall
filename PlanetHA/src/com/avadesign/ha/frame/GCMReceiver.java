package com.avadesign.ha.frame;


import java.io.IOException;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.avadesign.ha.EventAlert;
import com.google.android.gcm.GCMBaseReceiver;

public class GCMReceiver extends GCMBaseReceiver 
{
	public static final String SENDER_ID = "345578763707"; // project id
	public static final String MESSAGE_KEY_MSG = "KEY_broadcast_msg";		
	private final String TAG = this.getClass().getSimpleName();

	public GCMReceiver() 
	{
		super(SENDER_ID);
	}

	public GCMReceiver(String senderId) 
	{
		super(senderId);
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("NewApi")
	@Override
	protected void onMessage(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG, "GCMReceiver message");
		Bundle extras = intent.getExtras();
		if (extras != null) 
		{
			Iterator<?> iter = extras.keySet().iterator();
			while (iter.hasNext()) 
			{
				String key = (String) iter.next();
				String val = (String) extras.get(key);
				Log.v(TAG, "The received msg : " + key + " = " + val);
			}
			
			try{
				
				String msg =/*EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "",*/(String) extras.get(MESSAGE_KEY_MSG)/*)*/;
				//Log.v(TAG,"msg="+msg);
				//JSONParser parser = new JSONParser();
				//Log.v(TAG,"new json");
				//Map json = (Map)parser.parse(msg);
				//Map object = (Map)parser.parse(json.get("object").toString());
				//Log.v(TAG,"parser json");
				//String device_name = object.get("device_name").toString();
				//Log.v(TAG,"device_name"+device_name);
				
				
				
				Intent i = new Intent(context, EventAlert.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra(EventAlert.MSG_KEY, msg);
				context.startActivity(i);
				
				
				Log.v(TAG,"try");
			}
			catch(Exception e){
				e.printStackTrace();
				Log.v(TAG,"catch");
			}
		}
	}

	@Override
	public void onError(Context context, String errorId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "C2DMReceiver error");
	}

	@Override
	public void onRegistered(Context context, String registrationId)
			throws IOException {
		// TODO Auto-generated method stub
		super.onRegistered(context, registrationId);
		Log.v(TAG, "C2DMReceiver Register");
		
		Log.v(TAG,registrationId);
	}

	@Override
	public void onUnregistered(Context context) {
		// TODO Auto-generated method stub
		super.onUnregistered(context);
		Log.v(TAG, "C2DMReceiver UnRegister");
	}
}
