package com.avadesign.ha;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityDownloadView extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();

	private TextView lab_text;
	private ProgressBar progressBar;
	private ImageView imageView;
	private int count;
	private ArrayList<HashMap<String,String>> image_array;
	private GetImageTask mGetImageTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
		CusPreference cp = new CusPreference(ActivityDownloadView.this);
		
		Bundle bundle=this.getIntent().getExtras();
		image_array=(ArrayList<HashMap<String,String>>) bundle.getSerializable("image_array");
		
		lab_text=(TextView)findViewById(R.id.lab_text);
		lab_text.setText(R.string.DOWNLOAD_ICON);
		
		progressBar=(ProgressBar)findViewById(R.id.progressBar1);
		progressBar.setMax(image_array.size());
		progressBar.setProgress(0);
		
		imageView=(ImageView)findViewById(R.id.imageView1);
		
		count=0;
		download(count);
	}
	
	private void download(int i)
	{
		//final CusPreference cp = new CusPreference(ActivityDownloadView.this);
		
		HashMap<String, String> map=image_array.get(i);
		
		String normal_path=(String) map.get("Normal");
		String on_path=(String) map.get("On");
		String off_path=(String) map.get("Off");
		String group=(String) map.get("group");
		
		if(mGetImageTask!=null)
			return;
		
		mGetImageTask = new GetImageTask();
		mGetImageTask.execute(new String[]{normal_path,on_path,off_path,group});
	}
	
	private void detede_download (int i)
	{
		if (count<image_array.size())
		{
			download(count);
		}
		else
		{
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finish();
		}
	}
	
	@Override
	public void onBackPressed() 
	{
		
	}
	
	private class GetImageTask extends AsyncTask<String, Void, Boolean> 
	{		
		private Bitmap bm_normal=null,bm_on=null,bm_off=null;
		private String group=null;
		private String normal_path=null;
		private String on_path=null;
		private String off_path=null;
		
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(String... params) 
		{
			CusPreference cp = new CusPreference(ActivityDownloadView.this);
			normal_path=params[0];
			on_path=params[1];
			off_path=params[2];
			group=params[3];
			
			bm_normal=SendHttpCommand.GetCamImage(
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0]+(cp.isLocalUsed()?"":"?mac="+cp.getControllerMAC()+"&username="+cp.getUserName()+"&userpwd="+cp.getUserPwd()+"&tunnelid=0"),
					cp.getUserName(),
					cp.getUserPwd(),
					params[3].equalsIgnoreCase("location") ? 120 : 120);
			
			if (!params[3].equalsIgnoreCase("location"))
			{
				bm_on=SendHttpCommand.GetCamImage(
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[1]+(cp.isLocalUsed()?"":"?mac="+cp.getControllerMAC()+"&username="+cp.getUserName()+"&userpwd="+cp.getUserPwd()+"&tunnelid=0"),
						cp.getUserName(),
						cp.getUserPwd(),
						50);
				
				bm_off=SendHttpCommand.GetCamImage(
						String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[2]+(cp.isLocalUsed()?"":"?mac="+cp.getControllerMAC()+"&username="+cp.getUserName()+"&userpwd="+cp.getUserPwd()+"&tunnelid=0"),
						cp.getUserName(),
						cp.getUserPwd(),
						50);
				
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetImageTask = null;
			
			if (bm_normal!=null)
			{
				String [] ary=normal_path.split("/");
				
				if (ary.length>=3)
				{
					String path= Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+ary[0]+"/"+ary[1]+(group.equalsIgnoreCase("location") ? "" : "/normal");
					
					File file = new File(path, ary[2]);
					
					if(file.exists())
						file.delete();
					
					new File(path).mkdirs();
					
					try
					{
						FileOutputStream out = new FileOutputStream(file);
						bm_normal.compress(Bitmap.CompressFormat.PNG, 100, out);
						
						out.flush();
				    	out.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				
				imageView.setImageBitmap(bm_normal);
			}
			
			if(!group.equalsIgnoreCase("location"))
			{
				if (bm_on!=null)
				{
					String [] ary=on_path.split("/");
					
					if (ary.length>=3)
					{
						String path= Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+ary[0]+"/"+ary[1]+(group.equalsIgnoreCase("location") ? "" : "/on");
						
						File file = new File(path, ary[2]);
						
						if(file.exists())
							file.delete();
						
						new File(path).mkdirs();
						
						try
						{
							FileOutputStream out = new FileOutputStream(file);
							bm_on.compress(Bitmap.CompressFormat.PNG, 100, out);
							
							out.flush();
					    	out.close();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					
					imageView.setImageBitmap(bm_on);
				}
				if (bm_off!=null)
				{
					String [] ary=off_path.split("/");
					
					if (ary.length>=3)
					{
						String path= Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+ary[0]+"/"+ary[1]+(group.equalsIgnoreCase("location") ? "" : "/off");
						
						File file = new File(path, ary[2]);
						
						if(file.exists())
							file.delete();
						
						new File(path).mkdirs();
						
						try
						{
							FileOutputStream out = new FileOutputStream(file);
							bm_off.compress(Bitmap.CompressFormat.PNG, 100, out);
							
							out.flush();
					    	out.close();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					
					imageView.setImageBitmap(bm_off);
				}
			}
			
			count++;
			
			detede_download(count);
			
			progressBar.setProgress(count);
		}
	}
}
