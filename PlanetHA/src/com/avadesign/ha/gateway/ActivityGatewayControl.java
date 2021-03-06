package com.avadesign.ha.gateway;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.SharedClassApp;

public class ActivityGatewayControl extends BaseActivity 
{
	private Integer action=0;
	private Button bt1;
	private Button bt2;
	private Button back;
	private SendCommandTask mSendCommandTask;
	private Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gateway_control);
		
		RegisterBroadcast();
		
		StartService();
		
		bt1=(Button)findViewById(R.id.button1);
		bt2=(Button)findViewById(R.id.button2);
		back=(Button)findViewById(R.id.tab_back);
		
		bt1.setOnClickListener(button_down);
		bt2.setOnClickListener(button_down);
		back.setOnClickListener(button_down);
		
		back.setVisibility(View.GONE);
		
		//callProgress();
	}
	
	@Override
	protected void onDestroy() 
	{
		UnRegisterBroadcast();
		
		StopService();
		
		super.onDestroy();
	}
	
	@Override
	protected void callBroadcastdone()
	{
		attemptGetData();
	};
	
	private void attemptGetData()
	{
		Log.v(TAG,"isActive="+((SharedClassApp)(ActivityGatewayControl.this.getApplication())).isActive());
		
		if (!((SharedClassApp)(ActivityGatewayControl.this.getApplication())).isActive())
		{
			cancelProgress();
			
			Log.v(TAG,"action="+action);
			
			if (action!=0)
			{
				Builder builder = new AlertDialog.Builder(ActivityGatewayControl.this);
				
				if (action==1)
				{
					builder.setTitle(getString(R.string.CM_BUTTON_IN));
					
					builder.setMessage(getString(R.string.CM_BUTTON_IN_OK));
				}
				else
				{
					builder.setTitle(getString(R.string.CM_BUTTON_EX));
					
					builder.setMessage(getString(R.string.CM_BUTTON_EX_OK));
				}

				builder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						//Toast.makeText(ActivityNodeEdit.this, "�z���UOK���s", Toast.LENGTH_SHORT).show();
					}
				});

				builder.create();
				builder.show();
				action=0;
			}
		}
		else
		{
			String State=((SharedClassApp)(ActivityGatewayControl.this.getApplication())).getControllerState();
			
			String[] array = State.split(":");
			
			if (array.length >=2)
			{
				Log.v(TAG,array[0]);
				Log.v(TAG,array[1]);
			}
			String title =array[0];//array[0].replaceAll("(", "").replaceAll(")", "").replaceAll("1", "").replaceAll("2", "").replaceAll("3", "").replaceAll("4", "").replaceAll("5", "").replaceAll("6", "").replaceAll("7", "").replaceAll("8", "").replaceAll("9", "");
			
			
			String msg   =array[1];//array[1].replaceAll("(", "").replaceAll(")", "").replaceAll("1", "").replaceAll("2", "").replaceAll("3", "").replaceAll("4", "").replaceAll("5", "").replaceAll("6", "").replaceAll("7", "").replaceAll("8", "").replaceAll("9", "");
			
			//Log.v(TAG,title);
			
			mDialog_SPINNER.setTitle(title);
			mDialog_SPINNER.setMessage(msg);
			mDialog_SPINNER.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.alert_button_cancel),process_down);
			mDialog_SPINNER.show();
		}
	}
	
	private DialogInterface.OnClickListener  process_down=new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
			action=0;
			sendCancelCommand();
		}
	};
	
	private void sendAddCommand()
	{
		if(mSendCommandTask!=null)
			return;
		
		if(((SharedClassApp)(ActivityGatewayControl.this.getApplication())).isActive())
			sendCancelCommand();
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"admpost.html","fun","addd"});
		
	}
	
	private void sendRemoveCommand()
	{
		if(mSendCommandTask!=null)
			return;
		
		if(((SharedClassApp)(ActivityGatewayControl.this.getApplication())).isActive())
			sendCancelCommand();
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"admpost.html","fun","remd"});
	}
	
	private void sendCancelCommand()
	{
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"admpost.html","fun","cancel"});
	}
	
	private class SendCommandTask extends AsyncTask<String, Void, Boolean> 
	{
		String fun;
		@Override
		protected void onPreExecute() 
		{
			//mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
			//mDialog_SPINNER.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... args) 
		{
			boolean success = false;
			fun=args[2];
			Map<String, String> map = new HashMap<String,String>();
			map.put(args[1], args[2]);
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getControllerAcc());
				map.put("userpwd", cp.getControllerPwd());
				map.put("tunnelid", "0");
			}
			
			success = SendHttpCommand.send(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+args[0],
					map, 
					cp.getControllerAcc(), 
					cp.getControllerPwd(), 
					cp.isLocalUsed());
			Log.v(TAG,"success="+success);
			return success;
		}

		@Override
		protected void onPostExecute(final Boolean success) 
		{
			//mDialog_SPINNER.dismiss();
			
			if (success) {
				//Toast.makeText(ActivityCtrlModeView.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			} else {
				//Toast.makeText(ActivityCtrlModeView.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
			}
			
			mSendCommandTask = null;
			
			if (fun.equalsIgnoreCase("addd"))
			{
				mHandler.postDelayed(actionSet1Runnable, 1000);
			}
			else
			{
				if (fun.equalsIgnoreCase("cancel"))
					mHandler.postDelayed(actionSet0Runnable, 1000);
				else
					mHandler.postDelayed(actionSet2Runnable, 1000);
			}
		}

		@Override
		protected void onCancelled() 
		{
			//mDialog_SPINNER.dismiss();
			super.onCancelled();
		}
	}
	
	private Runnable actionSet1Runnable = new Runnable()
	{
		@Override
		public void run() 
		{
			action=1;
		}
	};
	
	private Runnable actionSet2Runnable = new Runnable()
	{
		@Override
		public void run() 
		{
			action=2;
		}
	};
	
	private Runnable actionSet0Runnable = new Runnable()
	{
		@Override
		public void run() 
		{
			action=0;
		}
	};
	
	private Button.OnClickListener button_down=new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			if (v==bt1)
			{
				sendAddCommand();
				Log.v(TAG,"BT1");
			}
			else if (v==bt2)
			{
				sendRemoveCommand();
				Log.v(TAG,"BT2");
			}
			else
			{
				finish();
			}
		}
	};
}
