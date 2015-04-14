package com.avadesign.ha.camera;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.ServicePolling;

public class ActivityCameraEdit extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private Button tab_add;
	
	private EditText edit_ip,edit_port,edit_suburl,edit_name,edit_acc,edit_pwd;
	
	private Boolean EditMode;
	
	private HashMap<String,String> cam_map;
	
	private GetCamTask mGetCamTask;
	
	private ProgressDialog mDialog_SPINNER;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_edit);
	
		FineView();
		
		Setlistener();

		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
	}
	
	private void FineView()
	{
		tab_add= (Button)findViewById(R.id.tab_add);
		
		edit_name=(EditText)findViewById(R.id.edit_name);
		edit_ip=(EditText)findViewById(R.id.edit_ip);
		edit_port=(EditText)findViewById(R.id.edit_port);
		edit_suburl=(EditText)findViewById(R.id.edit_url);
		
		edit_acc=(EditText)findViewById(R.id.edit_acc);
		edit_pwd=(EditText)findViewById(R.id.edit_pwd);
	}
	
	private void Setlistener()
	{
		tab_add.setOnClickListener(admin_button_down);
		
		tab_add.setTag(1);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		edit_name.clearFocus();
		edit_ip.clearFocus();
		edit_port.clearFocus();
		edit_suburl.clearFocus();
		edit_acc.clearFocus();
		edit_pwd.clearFocus();
		
		EditMode=false;
		
		Bundle bundle=this.getIntent().getExtras();
		cam_map=(HashMap<String, String>) bundle.getSerializable("map");
		
		if (cam_map.size()>0)
		{
			EditMode=true;
 
			edit_name.setText(cam_map.get("name"));
			edit_ip.setText(cam_map.get("ip"));
			edit_port.setText(cam_map.get("port"));
			edit_suburl.setText(cam_map.get("sub_url"));
			edit_acc.setText(cam_map.get("account"));
			edit_pwd.setText(cam_map.get("pwd"));
			
			tab_add.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_update), null, null);
			tab_add.setText(R.string.tab_button_update);
		}
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
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
				DialogSetAuth.show(ActivityCameraEdit.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivityCameraEdit.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivityCameraEdit.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
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
				UpdateCamCommand();
			}
			else
			{
				AddCamCommand();
			}
			
			PlayButtonSound.play(ActivityCameraEdit.this);
		}
	};
	
	private void AddCamCommand()
	{
		if(mGetCamTask!=null)
			return;
		
		mGetCamTask = new GetCamTask();
		mGetCamTask.execute(new String[]{"camera_list.cgi","action","add","name",edit_name.getText().toString(),"ip",edit_ip.getText().toString(),"port",edit_port.getText().toString(),"sub_url",edit_suburl.getText().toString(),"account",edit_acc.getText().toString(),"pwd",edit_pwd.getText().toString()});
	}
	
	private void UpdateCamCommand()
	{
		if(mGetCamTask!=null)
			return;
		
		mGetCamTask = new GetCamTask();
		mGetCamTask.execute(new String[]{"camera_list.cgi","action","update","id",cam_map.get("id"),"active","true","name",edit_name.getText().toString(),"ip",edit_ip.getText().toString(),"port",edit_port.getText().toString(),"sub_url",edit_suburl.getText().toString(),"account",edit_acc.getText().toString(),"pwd",edit_pwd.getText().toString()});
	}
	
	private class GetCamTask extends AsyncTask<String, Void, Boolean> 
	{	
		private boolean success=false;
		
		@Override
		protected void onPreExecute() 
		{
			mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
			mDialog_SPINNER.show();
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(String... params) 
		{
			CusPreference cp = new CusPreference(ActivityCameraEdit.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(params[1], params[2]);
			map.put(params[3], params[4]);
			map.put(params[5], params[6]);
			map.put(params[7], params[8]);
			map.put(params[9], params[10]);
			map.put(params[9], params[10]);
			map.put(params[11], params[12]);
			map.put(params[13], params[14]);
			if (params[2].equals("update"))
			{
				map.put(params[15], params[16]);
				map.put(params[17], params[18]);
			}
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getUserName());
				map.put("userpwd",  cp.getUserPwd());
			}
			
			//Log.v(TAG,"aaa="+params[1]+params[2]+params[3]+params[4]+params[5]+params[6]+params[7]+params[8]+params[9]+params[10]);
			
			success = SendHttpCommand.send(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
					map, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed());
			
			Log.v(TAG,"success="+success);
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetCamTask = null;
			mDialog_SPINNER.dismiss();
			
			if (success)
				finish();
		}
	}
}
