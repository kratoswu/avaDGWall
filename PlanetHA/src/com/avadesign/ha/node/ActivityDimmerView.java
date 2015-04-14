package com.avadesign.ha.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;

public class ActivityDimmerView extends BaseActivity 
{
	private TextView title,value;
	private SeekBar seekbar;
	private ImageView image_on,image_off;
	private ImageButton bt_close;
	private ZWaveNode znode;
	private ZWaveNodeValue value_zvalue = null,image_zvalue = null;
	
	private SendCommandTask mSendCommandTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitydimmerview);
		
		FindView();
		
		Setlistener();
		
		refreshView();
	}
	
	private void FindView()
	{
		title = (TextView) this.findViewById(R.id.TextView_title);
		value = (TextView) this.findViewById(R.id.TextView_value);
		
		seekbar = (SeekBar) this.findViewById(R.id.SeekBar);
		
		image_on = (ImageView) this.findViewById(R.id.ImageView_on);
		image_off = (ImageView) this.findViewById(R.id.ImageView_off);
		
		bt_close = (ImageButton) this.findViewById(R.id.button_close);
	}
	
	private void Setlistener()
	{
		bt_close.setOnClickListener(button_down);
		
		seekbar.setOnSeekBarChangeListener(Seek_down);
	}
	
	private Button.OnClickListener button_down = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			finish();
		}
	};
	

	@SuppressLint("DefaultLocale")
	private void refreshView()
	{
		znode = ((SharedClassApp)(ActivityDimmerView.this.getApplication())).getZWaveNode();
		
		title.setText(znode.name_fix);
		//Log.v(TAG,""+znode.id);
		//Log.v(TAG,""+znode.name_fix);
		for(ZWaveNodeValue zvalue : znode.value)
		{
			if (zvalue.class_c.toLowerCase().equals("switch multilevel") && zvalue.label.toLowerCase().equals("level"))
			{
				value_zvalue=zvalue;
			}
			else if (zvalue.class_c.toLowerCase().equals("switch multilevel") && zvalue.units.toLowerCase().equals("icon"))
			{
				image_zvalue=zvalue;
			}
		}
		
		//String status=value_zvalue.current.toLowerCase().equals("0") ? "off" : "on";
		
		if (value_zvalue!=null && image_zvalue!=null)
		{
			String path_on=label_to_path_label(image_zvalue.current,"on");
			String path_off=label_to_path_label(image_zvalue.current,"off");
			
			Bitmap bitmap_on = BitmapFactory.decodeFile(path_on);
			Bitmap bitmap_off = BitmapFactory.decodeFile(path_off);
			
			if (bitmap_on!=null)
				image_on.setImageBitmap(bitmap_on);
			else
				image_on.setImageResource(R.drawable.dimmer_light_on);
			
			if (bitmap_off!=null)
				image_off.setImageBitmap(bitmap_off);
			else
				image_on.setImageResource(R.drawable.dimmer_light_off);
			
			int point=Integer.valueOf(value_zvalue.current);
			
			float fpoint=point/100;
			
			image_on.setAlpha(fpoint);
		}
	}
	
	private String label_to_path_label (String label , String status)
	{
		ArrayList<HashMap<String,String>> image_array=(ArrayList<HashMap<String,String>>)cp.getIcon_Image();
		
		for(HashMap<String,String> map : image_array)
	    {
	    	if (map.get("type").equalsIgnoreCase(label))
	    	{
	    		String url=null;
	    		
	    		if (status.equalsIgnoreCase("normal"))
	    			url=map.get("Normal");
	    		else if (status.equalsIgnoreCase("on"))
	    			url=map.get("On");
	    		else if (status.equalsIgnoreCase("off"))
	    			url=map.get("Off");
	    		
	    		String [] ary=url.split("/");
	    		
	    		if (ary.length>=3)
	    		{
	    			String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.avadesign.ha"+"/"+ary[0]+"/"+ary[1]+"/"+status;
	            	
	    			return path+"/"+ary[2];
	    		}
	    	}
	    }
		return "";
	}
	
	private SeekBar.OnSeekBarChangeListener Seek_down=new SeekBar.OnSeekBarChangeListener()
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
		{
			String SeekStr=String.valueOf(progress);
			
			value.setText(SeekStr+"%");
			
			int point=Integer.valueOf(SeekStr);
			
			float fpoint=point/100;
			
			image_on.setAlpha(fpoint);
			
			Log.v(TAG,"change touch");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) 
		{
			Log.v(TAG,"Start touch");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) 
		{	
			final String vid = znode.id+"-"+value_zvalue.class_c+"-"+value_zvalue.genre+"-"+value_zvalue.type+"-"+value_zvalue.instance+"-"+value_zvalue.index;
			sendCommand(vid,String.valueOf(seekBar.getProgress()),znode.id);

			Log.v(TAG,"post="+vid+String.valueOf(seekBar.getProgress()));
			
			Log.v(TAG,"Stop touch");
		}
	};
	
	private void sendCommand(final String vid , final String action , final String id)
	{
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"valuepost.html",vid,action});
	}
	
	private class SendCommandTask extends AsyncTask<String, Void, Boolean> 
	{
		@Override
		protected void onPreExecute() 
		{
			callProgress();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... args) 
		{
			boolean success = false;
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(args[1], args[2]);
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getControllerAcc());
				map.put("userpwd", cp.getControllerPwd());
				//map.put("tunnelid", "0");
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
			if (success) {
				Toast.makeText(ActivityDimmerView.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ActivityDimmerView.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
			}
			
			cancelProgress();
			
			mSendCommandTask = null;
		}

		@Override
		protected void onCancelled() 
		{
			cancelProgress();
			super.onCancelled();
		}
	}
}
