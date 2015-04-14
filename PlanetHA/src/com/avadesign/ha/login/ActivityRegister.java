package com.avadesign.ha.login;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityRegister extends BaseActivity 
{
	private EditText edit_acc,edit_pwd,edit_fn,edit_ln;
	private Button bt_register;
	private CheckBox cb;
	
	private HttpCommandTask mHttpCommandTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		FindView();
		
		Setlistener();
		
		//String url=String.format(getString(R.string.server_url_format) , getString(R.string.server_ip) , getString(R.string.server_port) )+"cloud_reg.cgi";
		
		//Log.v(TAG,"strurl="+url);
	}
	private void FindView()
	{
		edit_acc = (EditText)this.findViewById(R.id.edit_acc);
		edit_pwd = (EditText)this.findViewById(R.id.edit_pwd);
		edit_fn  = (EditText)this.findViewById(R.id.edit_fname);
		edit_ln  = (EditText)this.findViewById(R.id.edit_lname);
		
		bt_register = (Button)this.findViewById(R.id.button2);
		
		cb = (CheckBox)this.findViewById(R.id.checkBox1);
	}
	
	private void Setlistener()
	{
		bt_register.setOnClickListener(button_down);
		cb.setOnCheckedChangeListener(cb_down);
	}
	
	private CheckBox.OnCheckedChangeListener cb_down = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
		{
			//check=isChecked;
			//Log.v(TAG,"cb="+isChecked);
		}
	};
	
	private Button.OnClickListener button_down = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			if (edit_acc.getText()==null || edit_pwd.getText()==null || edit_fn.getText()==null || edit_ln.getText()==null)
			{
				new AlertDialog.Builder(ActivityRegister.this)
				.setTitle(getString(R.string.alert_title_error))
				.setMessage(getString(R.string.alert_message_text_empty))
				.setPositiveButton(R.string.alert_button_ok,new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						
					}
				}).show();
			}
			else
			{	
				RegisterCommand();
			}
		}
	};
	
	private void RegisterCommand()
	{
		if(mHttpCommandTask!=null)
			return;
		
		mHttpCommandTask = new HttpCommandTask();
		mHttpCommandTask.execute(new String[]{"cloud_reg.cgi","acc",edit_acc.getText().toString(),"pwd",edit_pwd.getText().toString(),"firstName",edit_fn.getText().toString(),"lastName",edit_ln.getText().toString(),"newsCheck",cb.isChecked() ? "true" : "false"});
	}
	
	private class HttpCommandTask extends AsyncTask<String, Void, Boolean> 
	{		
		String result;
		String fun="";
		//boolean success=false;
		
		@Override
		protected void onPreExecute() 
		{
			callProgress();
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(String... params) 
		{
			Map<String, String> map = new HashMap<String,String>();
			
			fun=params[2];
			
			for(int i=2;i<params.length;i+=2)
				map.put(params[i-1], params[i]);
			
			/*
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getControllerAcc());
				map.put("userpwd",  cp.getControllerPwd());
			}
			*/		
			result = SendHttpCommand.getString(String.format(getString(R.string.server_url_format) , getString(R.string.server_ip) , getString(R.string.server_port) )+params[0],
					map/*, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed()*/);
			
			//Log.v(TAG,"result="+result);
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mHttpCommandTask = null;
			
			cancelProgress(); 
			
			JSONParser parser = new JSONParser();

			try 
			{
				Map<?, ?> data = (Map<?, ?>)parser.parse(result);
				
				Log.v(TAG,"map="+data);
				
				if (data.get("result").toString().equalsIgnoreCase("ok"))
				{
					new AlertDialog.Builder(ActivityRegister.this)
					.setTitle(getString(R.string.alert_title_success))
					.setMessage(getString(R.string.alert_message_register_done))
					.setPositiveButton(R.string.alert_button_ok,new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							setResult(0,null);
							
							cp.setUserAccount (edit_acc.getText().toString());
							cp.setUserPassword(edit_pwd.getText().toString());
							
							finish();
						}
					}).show();
				}
				else
				{
					Toast.makeText(ActivityRegister.this,data.get("msg").toString(), Toast.LENGTH_SHORT).show();
				}
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
