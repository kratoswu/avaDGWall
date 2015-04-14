package com.avadesign.ha.camera;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityCameraEdit extends BaseActivity 
{
	private Button tab_add;
	
	private EditText edit_ip,edit_port,edit_suburl,edit_name,edit_acc,edit_pwd;
	
	private Boolean EditMode;
	
	private HashMap<String,String> cam_map;
	
	private GetCamTask mGetCamTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_edit);
	
		FineView();
		
		Setlistener();
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
	}
		
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
			callProgress();
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(String... params) 
		{
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
				map.put("username", cp.getControllerAcc());
				map.put("userpwd",  cp.getControllerPwd());
			}
			
			//Log.v(TAG,"aaa="+params[1]+params[2]+params[3]+params[4]+params[5]+params[6]+params[7]+params[8]+params[9]+params[10]);
			
			success = SendHttpCommand.send(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
					map, 
					cp.getControllerAcc(), 
					cp.getControllerPwd(), 
					cp.isLocalUsed());
			
			Log.v(TAG,"success="+success);
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetCamTask = null;
			cancelProgress();
			
			if (success)
				finish();
		}
	}
}
