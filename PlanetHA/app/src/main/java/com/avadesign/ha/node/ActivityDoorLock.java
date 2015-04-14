package com.avadesign.ha.node;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;

public class ActivityDoorLock extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private Button bt1,bt2,bt3,bt4,bt5,bt6,bt7,bt8,bt9,bt0,bt_cancel,bt_ok,bt_back;
	private TextView textview_pass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doorlick);
		
		FindView();
		
		Setlistener();
	}
	
	private void FindView()
	{
		bt1=(Button)findViewById(R.id.button1);
		bt2=(Button)findViewById(R.id.button2);
		bt3=(Button)findViewById(R.id.button3);
		bt4=(Button)findViewById(R.id.button4);
		bt5=(Button)findViewById(R.id.button5);
		bt6=(Button)findViewById(R.id.button6);
		bt7=(Button)findViewById(R.id.button7);
		bt8=(Button)findViewById(R.id.button8);
		bt9=(Button)findViewById(R.id.button9);
		bt0=(Button)findViewById(R.id.button0);
		
		bt_cancel=(Button)findViewById(R.id.button10);
		bt_ok=(Button)findViewById(R.id.button11);
		
		bt_back=(Button)findViewById(R.id.button_back);
		
		textview_pass=(TextView)findViewById(R.id.textview_pass);
	}
	
	private void Setlistener()
	{
		bt1.setTag(1);
		bt2.setTag(2);
		bt3.setTag(3);
		bt4.setTag(4);
		bt5.setTag(5);
		bt6.setTag(6);
		bt7.setTag(7);
		bt8.setTag(8);
		bt9.setTag(9);
		bt0.setTag(0);
		
		bt_cancel.setTag(10);
		bt_ok.setTag(11);
		bt_back.setTag(12);
		
		bt1.setOnClickListener(button_down);
		bt2.setOnClickListener(button_down);
		bt3.setOnClickListener(button_down);
		bt4.setOnClickListener(button_down);
		bt5.setOnClickListener(button_down);
		bt6.setOnClickListener(button_down);
		bt7.setOnClickListener(button_down);
		bt8.setOnClickListener(button_down);
		bt9.setOnClickListener(button_down);
		bt0.setOnClickListener(button_down);
		
		bt_cancel.setOnClickListener(button_down);
		bt_ok.setOnClickListener(button_down);
		bt_back.setOnClickListener(button_down);
		
		bt_back.setVisibility(View.INVISIBLE);
		bt_back.setText("<<");
	}
	
	private void check_pass()
	{
		ZWaveNode znode = ((SharedClassApp)(ActivityDoorLock.this.getApplication())).getZWaveNode();
		
		boolean has_code=false;
		
		for(ZWaveNodeValue zvalue : znode.value)
		{
			if (zvalue.class_c.equalsIgnoreCase("user code"))
			{
				if (zvalue.type.equalsIgnoreCase("raw"))
				{
					String [] ary=zvalue.current.split(" ");
					
					String default_code="";
					
					for(String str : ary)
					{
						int code=Integer.valueOf(str.replace("0x",""))-30;
						
						default_code+=String.valueOf(code);
					}
					Log.v(TAG,"defaule_code="+default_code);
					
					if (default_code.equals(textview_pass.getText().toString()))
						has_code=true;
				}
			}
		}
		
		if (has_code)
		{
			for(ZWaveNodeValue zvalue : znode.value)
			{
				if (zvalue.class_c.equalsIgnoreCase("door lock"))
				{
					if (zvalue.label.equalsIgnoreCase("mode"))
					{
						final String vid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;						
    					sendCommand(vid,value_ToChange(zvalue.current.toLowerCase()),znode.id);
                    }
                }
            }
            
            Log.v(TAG,"Oper Door");
		}
		else
		{
			AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityDoorLock.this);
				delAlertDialog.setTitle(R.string.device_lock_pass_title);
			   delAlertDialog.setMessage(getString(R.string.device_lock_pass_message));
			   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
			   {
				   public void onClick(DialogInterface arg0, int arg1) 
				   {
					   
				   }
			   });
			   delAlertDialog.show();
	        
			Log.v(TAG,"Oper Error");
		}
	}
	
	private Button.OnClickListener button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			PlayButtonSound.play(ActivityDoorLock.this);
			
			switch ((Integer) v.getTag())
			{
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
				case 8:
				case 9:
				{
					String tag=String.valueOf((Integer) v.getTag());
					
					Log.v(TAG,""+textview_pass.getText().toString()+tag);
					
					textview_pass.setText(textview_pass.getText().toString()+tag);
					
					if (textview_pass.getText().toString().length()>0)
						bt_back.setVisibility(View.VISIBLE);
					
					break;
				}
				case 10:
				{
					finish();
					break;
				}
				case 11:
					check_pass();
					break;
				case 12:
				{
					String str=textview_pass.getText().toString();
					
					String str_1=str.substring(0, str.length()-1);
					
					textview_pass.setText(str_1);
					
					if (str_1.length()==0)
						bt_back.setVisibility(View.INVISIBLE);
					
					break;
				}
				default:
					break;
			}
		}
	};
	
	private String value_ToChange(final String value)
	{
		if (value.indexOf("unsecured")!=-1)
			return "Secured";
		else if (value.indexOf("secured")!=-1)
			return "Unsecured";
		else if (value.equalsIgnoreCase("true"))
			return "False";
		else if (value.equalsIgnoreCase("false"))
			return "True";
		else if (value.equalsIgnoreCase("0"))
			return "99";
		else if (value.equalsIgnoreCase("99"))
			return "0";
		
		return "0";
	}
	
	private void sendCommand(final String vid , final String action , final String id)
	{
		Log.v(TAG,"vid "+vid+"="+action);
		new Thread()
		{
			public void run()
			{
				boolean success = false;
				CusPreference cp = new CusPreference(ActivityDoorLock.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put(vid , action);
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", "admin");
					map.put("userpwd", "admin");
				}
				
				success = SendHttpCommand.send(String.format(cp.isLocalUsed()?getString(R.string.local_url_syntax):getString(R.string.server_url_syntax),cp.getControllerIP(),String.valueOf(cp.getControllerPort()))+"valuepost.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
				
			}
		}.start();
		
		Toast.makeText(ActivityDoorLock.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
		
		finish();
	}
}
