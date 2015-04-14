package com.avadesign.ha.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.avadesign.ha.ActivityMenuView;
import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityLogin extends BaseActivity 
{
	private EditText edit_acc,edit_pwd;
	private Button bt_register,bt_login;
	private CheckBox cb;
	private Boolean real_auto;
	
	private HttpCommandTask mHttpCommandTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		/*
		Resources res = getResources();
		Configuration conf = res.getConfiguration();
		//以下擇一
		//conf.locale = Locale.CHINA;//簡中
		conf.locale = Locale.ENGLISH;//英文
		//conf.locale = Locale.TRADITIONAL_CHINESE;//繁中

		DisplayMetrics dm = res.getDisplayMetrics();
		res.updateConfiguration(conf, dm);
		*/
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		Bundle bundle=this.getIntent().getExtras();
		real_auto=bundle.getBoolean("auto_login");
		
		FindView();
		
		Setlistener();
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		edit_acc.setText(cp.getUserAccount());
		edit_pwd.setText(cp.getUserPassword());
		
		cb.setChecked(cp.getAutoLogin());
		
		if (real_auto)
			if (cp.getAutoLogin())
				LoginCommand();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		real_auto=false;
	}
	
	private void FindView()
	{
		edit_acc = (EditText)this.findViewById(R.id.editText_acc);
		edit_pwd = (EditText)this.findViewById(R.id.editText_pwd);
		
		bt_login    = (Button)this.findViewById(R.id.button1);
		bt_register = (Button)this.findViewById(R.id.button2);
		
		cb = (CheckBox)this.findViewById(R.id.checkBox1);
	}
	
	private void Setlistener()
	{
		bt_login.setTag(1);
		bt_register.setTag(2);
		
		bt_login.setOnClickListener(button_down);
		bt_register.setOnClickListener(button_down);
		
		cb.setOnCheckedChangeListener(cb_down);
	}
	
	private CheckBox.OnCheckedChangeListener cb_down = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
		{
			cp.setAutoLogin(isChecked);
		}
	};
	private Button.OnClickListener button_down = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			if ((Integer)v.getTag()==1)
			{
				if (edit_acc.getText()==null || edit_pwd.getText()==null)
				{
					new AlertDialog.Builder(ActivityLogin.this)
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
					LoginCommand();
				}
			}
			else
			{
				Intent intent = new Intent();
				intent.setClass(ActivityLogin.this, ActivityRegister.class);
				startActivityForResult(intent,0);
			}
		}
	};
	
	private void LoginCommand()
	{
		if(mHttpCommandTask!=null)
			return;
		
		mHttpCommandTask = new HttpCommandTask();
		mHttpCommandTask.execute(new String[]{"cloud_login.cgi","acc",edit_acc.getText().toString(),"pwd",edit_pwd.getText().toString()});
	}
	
	private void RepassCommand()
	{
		if(mHttpCommandTask!=null)
			return;
		
		mHttpCommandTask = new HttpCommandTask();
		mHttpCommandTask.execute(new String[]{"cloud_send_pwd.cgi","acc",edit_acc.getText().toString()});
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
			
			fun=params[0];
			
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
				
				if (fun.equalsIgnoreCase("cloud_send_pwd.cgi"))
				{
					if (data.get("result").toString().equalsIgnoreCase("ok"))
					{
						new AlertDialog.Builder(ActivityLogin.this)
						.setTitle(getString(R.string.alert_title_success))
						.setMessage(getString(R.string.alert_message_resend_done))
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
						Toast.makeText(ActivityLogin.this,data.get("msg").toString(), Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					if (data.get("result").toString().equalsIgnoreCase("ok"))
					{
						cp.setUserAccount (edit_acc.getText().toString());
						cp.setUserPassword(edit_pwd.getText().toString());
						
						@SuppressWarnings("unchecked")
						ArrayList<HashMap<String,String>> acclist=(ArrayList<HashMap<String,String>>)data.get("acclist");
						
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putSerializable("gateway_list", acclist);
						bundle.putBoolean("new", true);
						intent.putExtras(bundle);
						if (cp.getControllerMAC().equalsIgnoreCase(""))
							intent.setClass(ActivityLogin.this ,ActivityGatewayList.class);
						else
							intent.setClass(ActivityLogin.this ,ActivityMenuView.class);
						startActivity(intent);
						
						finish();
					}
					else
					{
						new AlertDialog.Builder(ActivityLogin.this)
						.setTitle(getString(R.string.alert_title_error))
						.setMessage(getString(R.string.alert_message_error_401))
						.setPositiveButton(R.string.alert_button_ok,new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								
							}
						})
						.setNegativeButton(R.string.alert_button_repass, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								RepassCommand();
							}
						})
						.show();
					}
				}
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
