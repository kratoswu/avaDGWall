package com.avadesign.ha.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.ServicePolling;


public class ActivityScheduleViewEdit extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();

	private Button tab_save;
	
	private ArrayList<HashMap<String,String>> scene_array;
	
	private EditText edit_name,edit_time,edit_date;
	private Spinner spinner_period,spinner_day,spinner_scene;
	
	private Handler handler;
	
	private Boolean EditMode;
	
	private HashMap<String, String> schedule_map;
	
	private RelativeLayout relativeLayout;
	
	private LinearLayout linearLayout;
	
	private TextView weekday_delaytime;
	
	private int hour;
	private int min;
	private int timezone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule_view_edit);
		
		FindView();
		
		Setlistener();

		final Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		min = c.get(Calendar.MINUTE);
		
		timezone=TimeZone.getDefault().getRawOffset()/3600000;
		
		//Log.v(TAG,"aaa="+timezone/3600000);
		setBtnText();
    }
	
	private void FindView()
	{
		edit_name=(EditText)findViewById(R.id.editText1);
		edit_time=(EditText)findViewById(R.id.editText3);
		edit_date=(EditText)findViewById(R.id.editText2);
		edit_date.setOnFocusChangeListener(TextFocusChange);
		
		spinner_period=(Spinner)findViewById(R.id.spinner2);
		spinner_day=(Spinner)findViewById(R.id.spinner3);
		spinner_scene=(Spinner)findViewById(R.id.spinner5);
		
		relativeLayout=(RelativeLayout)findViewById(R.id.RelativeLayout01); 
		
		linearLayout=(LinearLayout)findViewById(R.id.LinearLayout01);
		
		weekday_delaytime=(TextView)findViewById(R.id.textView3);
		
		tab_save= (Button)findViewById(R.id.tab_save);
	}
	
	private void Setlistener()
	{
		scene_array = new ArrayList<HashMap<String,String>>();
		tab_save.setOnClickListener(admin_button_down);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		edit_name.clearFocus();
		edit_time.clearFocus();

		handler =  new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message msg) 
			{
				String MsgString = (String)msg.obj;
					 
				if (MsgString.equals("scene_ok"))
					SetLayout();
				else if (MsgString.equals("layout_ok"))
					ChangeLayout();
				return false;
			}
		});
		
		EditMode=false;
		
		Bundle bundle=this.getIntent().getExtras();
		schedule_map=(HashMap<String, String>) bundle.getSerializable("map");
		
		if (schedule_map.size()>0)
		{
			EditMode=true;
			tab_save.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_update), null, null);
			tab_save.setText(R.string.tab_button_update);
		}
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
		
		GetSceneCommand();
	}
	
	@Override
	protected void onPause() 
	{
		unregisterReceiver(mBroadcastReceiver);
		super.onPause();
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if(intent.getAction().equals(ServicePolling.HTTP_401))
			{
				DialogSetAuth.show(ActivityScheduleViewEdit.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivityScheduleViewEdit.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivityScheduleViewEdit.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
				
			}
		}
	};
	
	private Button.OnClickListener admin_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			if (EditMode)
			{
				SaveScheduleCommand(edit_name.getText().toString(),"","","","",schedule_map.get("id"));
			}
			else
			{
				HashMap<String,String> map=scene_array.get(spinner_scene.getSelectedItemPosition());
				
				String period=spinner_period.getSelectedItemPosition()==0 ? "week" : spinner_period.getSelectedItemPosition()==1 ? "day" : "none";
				
				String scene_id=map.get("id");
				
				String weekday="";
				if (period.equalsIgnoreCase("none"))
					weekday=edit_time.getText().toString();
				else
					weekday=String.valueOf(spinner_day.getSelectedItemPosition());
				
				String time=edit_date.getText().toString();
				
				
				String[] array = time.split(":");
						
				int HOUR=0;
				int MINUTE=0;
				
				if (array.length>0)
				{
					HOUR=Integer.valueOf(array[0]);
					
					if (HOUR-timezone<0)
						HOUR=HOUR-timezone+24;
					else
						HOUR=HOUR-timezone;
					
					MINUTE=Integer.valueOf(array[1]);
				}
				
				/*
				Log.v(TAG,edit_name.getText().toString());
				Log.v(TAG,period);
				Log.v(TAG,weekday);
				Log.v(TAG,String.valueOf(HOUR));
				Log.v(TAG,String.valueOf(MINUTE));
				Log.v(TAG,scene_id);
				*/
				SaveScheduleCommand(edit_name.getText().toString(),period,weekday,String.valueOf(HOUR),String.valueOf(MINUTE),scene_id);
			}
			PlayButtonSound.play(ActivityScheduleViewEdit.this);
		}
	};
	
	private EditText.OnFocusChangeListener TextFocusChange =new OnFocusChangeListener()
	{
		@SuppressWarnings("deprecation")
		@Override
		public void onFocusChange(View arg0, boolean arg1) 
		{
			//Log.v(TAG,"Edit="+arg0+", focus="+arg1);
			if (arg1)
			{
				if (arg0==edit_date)
				{
					arg0.clearFocus();
					showDialog(0);
				}
			}
			
		}
		
	};
	
	private void GetSceneCommand()
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityScheduleViewEdit.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("fun","load");
				
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					map.put("tunnelid", "0");
				}
				scene_array.clear();
				scene_array = SendHttpCommand.getlist(
						
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"scenepost.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed(),"scene");
				
				/*
				for (HashMap<String, String> mapp : scene_array)
				{
					Log.v(TAG,"id="+mapp.get("id")+"label="+mapp.get("label"));
				}
				*/
				Message message;
				String obj = "scene_ok";
				message = handler.obtainMessage(1,obj);
				handler.sendMessage(message);
			}
		}.start();
	}
	
	private void SetLayout()
	{
		String [] string_period=new String [3];
		string_period[0]=getString(R.string.schedule_period_week);//"Week";
		string_period[1]=getString(R.string.schedule_period_day);//"Day";
		string_period[2]=getString(R.string.schedule_period_once);//"Once";
		
		ArrayAdapter<String> adapter_period=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_period);
		adapter_period.setDropDownViewResource(R.layout.custom_spinner);
		spinner_period.setAdapter(adapter_period);
		spinner_period.setOnItemSelectedListener(Spinner_select);
		
		
		
		String [] string_scene=new String [scene_array.size()];
		for(int i=0;i<scene_array.size();i++)
		{
			HashMap<String, String> map=scene_array.get(i);
			string_scene[i]=map.get("label");
		}
		
		ArrayAdapter<String> adapter_scene=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_scene);
		adapter_scene.setDropDownViewResource(R.layout.custom_spinner);
		spinner_scene.setAdapter(adapter_scene);
		spinner_scene.setOnItemSelectedListener(Spinner_select);

		
		String [] string_day=new String [7];
		string_day[0]=getString(R.string.schedule_day_sum);//"Sum";
		string_day[1]=getString(R.string.schedule_day_mon);//"Mon";
		string_day[2]=getString(R.string.schedule_day_tue);//"Tue";
		string_day[3]=getString(R.string.schedule_day_wed);//"Wed";
		string_day[4]=getString(R.string.schedule_day_thu);//"Thu";
		string_day[5]=getString(R.string.schedule_day_fri);//"Fri";
		string_day[6]=getString(R.string.schedule_day_sat);//"Sat";
		
		ArrayAdapter<String> adapter_day=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_day);
		adapter_day.setDropDownViewResource(R.layout.custom_spinner);
		spinner_day.setAdapter(adapter_day);
		spinner_day.setOnItemSelectedListener(Spinner_select);
				
		Message message;
		String obj = "layout_ok";
		message = handler.obtainMessage(1,obj);
		handler.sendMessage(message);
	}
	
	private void ChangeLayout()
	{
		if (EditMode)
		{
			edit_time.setEnabled(false);
			spinner_period.setEnabled(false);
			spinner_day.setEnabled(false);
			spinner_scene.setEnabled(false);
			edit_date.setEnabled(false);
			
			edit_name.setText((String)schedule_map.get("label"));
			
			if (schedule_map.get("period").equalsIgnoreCase("week"))
			{
				spinner_period.setSelection(0);
				
				spinner_day.setSelection(Integer.valueOf(schedule_map.get("weekday")));
				
			}
			else if (schedule_map.get("period").equalsIgnoreCase("day"))
			{
				spinner_period.setSelection(1);
			}
			else
			{
				spinner_period.setSelection(2);
			}
			
			String scene=Scene_id_to_name((String) schedule_map.get("scene"));
			
			for(int i=0;i<scene_array.size();i++)
			{
				HashMap<String, String> map=scene_array.get(i);
				
				if (map.get("label").equalsIgnoreCase(scene))
					spinner_scene.setSelection(i);
			}
			
			String HOUR=schedule_map.get("hour");
			String MINUTE=schedule_map.get("minute");
			
			if (Integer.valueOf(HOUR)+timezone>=24)
				hour=Integer.valueOf(HOUR)+timezone-24;
	        else
	            hour=Integer.valueOf(HOUR)+timezone;
	        
			min=Integer.valueOf(MINUTE);
			
			setBtnText();
		}
	}
	
	private Spinner.OnItemSelectedListener Spinner_select=new Spinner.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			//Log.v(TAG,"position"+spinner_mode.getSelectedItemPosition());
			
			if (arg0.getId()==R.id.spinner2)
			{
				if (arg2==0)
				{
					relativeLayout.setVisibility(View.VISIBLE);
					edit_time.setVisibility(View.INVISIBLE);
					spinner_day.setVisibility(View.VISIBLE);
					
					weekday_delaytime.setVisibility(View.VISIBLE);
					weekday_delaytime.setText(getString(R.string.schedule_day));
					
					linearLayout.setVisibility(View.VISIBLE);
				}
				else if (arg2==1)
				{
					relativeLayout.setVisibility(View.GONE);
					weekday_delaytime.setVisibility(View.GONE);
					
					linearLayout.setVisibility(View.VISIBLE);
				}
				else
				{
					relativeLayout.setVisibility(View.VISIBLE);
					edit_time.setVisibility(View.VISIBLE);
					spinner_day.setVisibility(View.INVISIBLE);
					
					weekday_delaytime.setVisibility(View.VISIBLE);
					weekday_delaytime.setText(getString(R.string.schedule_delay));
					
					linearLayout.setVisibility(View.GONE);
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			
		}
	};
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) 
		{
			case 0:
				return new TimePickerDialog(this, timePickerListener, hour, min, true);
		}
		return null;
	}
	
	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() 
	{
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
		{
			hour = hourOfDay;
			min = minute;
			setBtnText();
		}
	};

	private void setBtnText() 
	{	
		//time_button.setText(String.format("%02d", hour)+":"+String.format("%02d", min));
		edit_date.setText(String.format("%02d", hour)+":"+String.format("%02d", min));
	}
	
	private String Scene_id_to_name(String scene_id)
	{
	    for(HashMap<String,String> map : scene_array )
	    {
	        if (map.get("id").equals(scene_id))
	            return map.get("label");
	    }
	    return "";
	}
	
	private void SaveScheduleCommand(final String label , final String period , final String weekday , final String hour , final String minute , final String scene_id)
	{
		new Thread()
		{
			public void run()
			{
				Log.v(TAG,label+"-"+period+"-"+weekday+"-"+hour+"-"+minute+"-"+scene_id);
				
				CusPreference cp = new CusPreference(ActivityScheduleViewEdit.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("action",EditMode ? "update" : "add");
				
				if (!EditMode)
				{
					//action=%@&label=%@&period=%@&weekday=%@&hour=%@&minute=%@&id=%@&active=%@"
					if (period.equalsIgnoreCase("week"))
					{
						map.put("label", label);
						map.put("period", period);
						map.put("weekday", weekday);
						map.put("hour", hour);
						map.put("minute", minute);
						map.put("id", scene_id);
						map.put("active", "true");
					}
					else if (period.equalsIgnoreCase("day"))
					{
						map.put("label", label);
						map.put("period", period);
						map.put("hour", hour);
						map.put("minute", minute);
						map.put("id", scene_id);
						map.put("active", "true");
					}
					else
					{
						map.put("label", label);
						map.put("period", period);
						map.put("delay", weekday);
						map.put("id", scene_id);
						map.put("active", "true");
					}
				}
				else
				{
					map.put("label", label);
					map.put("id", scene_id);
					map.put("active", "true");
				}
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					map.put("tunnelid", "0");
				}
				
				SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"scene_schedule.cgi",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
			}
		}.start();
		finish();
	}
}
