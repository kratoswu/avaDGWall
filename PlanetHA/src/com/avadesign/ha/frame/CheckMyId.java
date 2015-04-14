package com.avadesign.ha.frame;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.util.Log;

import com.avadesign.ha.R;
import com.google.android.gcm.GCMessaging;

public class CheckMyId
{
	private final static String TAG = "CheckMyId";
	
	private static CusPreference cp;
	private static Context context;
	
	public static void Check_and_Set_PushId(Context _context)
	{
		context=_context;
		cp = new CusPreference(_context);
		
		Log.v(TAG,"check");
		
		//getNotifyList();
		
		String push_id=GCMessaging.getRegistrationId(context);
		
		SetPushCommand(push_id);
	}
	
	private static void SetPushCommand(final String push_id)
	{
		new Thread()
		{
			public void run()
			{
				Map<String, String> map = new HashMap<String,String>();
				
				map.put("acc",cp.getUserAccount());
				map.put("pwd",cp.getUserPassword());
				map.put("pushid",push_id);
				map.put("needpush",cp.isPush() ? "true" : "false");
				
				String result = SendHttpCommand.getString(String.format(context.getString(R.string.server_url_format) , context.getString(R.string.server_ip) , context.getString(R.string.server_port) )+"editpushid.cgi",
						map);
				
				
				JSONParser parser = new JSONParser();

				try 
				{
					Map<?, ?> data = (Map<?, ?>)parser.parse(result);
					
					Log.v(TAG,"map="+data);
					
					if (data.get("result").toString().equalsIgnoreCase("ok"))
					{
					
					}
					else
					{
						String push_id=GCMessaging.getRegistrationId(context);
						
						SetPushCommand(push_id);
					}
				} 
				catch (ParseException e) 
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
}
