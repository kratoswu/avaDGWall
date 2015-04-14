package com.avadesign.ha.camera;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.HttpClientHelper;

public class ActivityCameraWatch extends Activity 
{
	private String TAG = this.getClass().getSimpleName();
	
	private TextView text_name,text_fps;
	
	private ImageView image;
	
	private String ip,port,sub_url,tunnel_id,acc,pwd,name;
	
	private Bitmap bitmap =null;
	
	private int fps,stop_flag;
	
	private Timer timer;
	
	private Handler handler = new MyHandler(this);
	
	//private MyOrientationEventListener myOrientationEventListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_watch);
		
		Bundle bundle=this.getIntent().getExtras();
		ip=bundle.getString("ip");
		port=bundle.getString("port");
		sub_url=bundle.getString("sub_url");
		tunnel_id=bundle.getString("tunnel_id");
		acc=bundle.getString("acc");
		pwd=bundle.getString("pwd");
		name=bundle.getString("name");
		
		/*
		myOrientationEventListener = new MyOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);

		if (myOrientationEventListener.canDetectOrientation())
		{
			myOrientationEventListener.enable();
		}
		*/
		FineView();
		
		Setlistener();
		
		//Log.v(TAG,"result="+ip+"-"+port+"-"+sub_url+"-"+tunnel_id+"-"+acc+"-"+pwd);
	}
	
	private void FineView()
	{
		text_name=(TextView)findViewById(R.id.textView1);
		text_fps=(TextView)findViewById(R.id.textView2);
		image=(ImageView)findViewById(R.id.imageView1);
	}
	
	private void Setlistener()
	{
		text_name.setText(name);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		stop_flag=1;
		fps=0;
		connecting();
		
		timer= new Timer();
		timer.schedule(mTimerTask, 1000, 1000);
	}

	@Override
	protected void onPause() 
	{
		timer.cancel();
		stop_flag=0;
		super.onPause();
	}
	
	private TimerTask mTimerTask=new TimerTask()
	{
		@Override
		public void run() 
		{
			Message message = handler.obtainMessage(3,null);
			handler.sendMessage(message);
		}
	};
	
	private void connecting()
	{
		CusPreference cp = new CusPreference(ActivityCameraWatch.this);
		
		HashMap<String,String>map = new HashMap<String, String>();
		
		if(!cp.isLocalUsed()){
			map.put("mac", cp.getControllerMAC());
			map.put("username", acc);
			map.put("userpwd" , pwd);
			map.put("tunnelid", tunnel_id);
		}
		
		GetCamImage(String.format(cp.isLocalUsed()?getString(R.string.cam_url_syntax):getString(R.string.server_url_syntax),cp.isLocalUsed() ? ip : getString(R.string.server_ip),cp.isLocalUsed() ? port : getString(R.string.server_port)
				)+sub_url,map,acc,pwd,cp.isLocalUsed(),tunnel_id);
	}
	
	private void GetCamImage(final String syntax, final Map<String, String> param, final String username, final String userpwd, final boolean isLocal, final String tunnel_id)
	{
		new Thread()
		{
			public void run()
			{				
        		try 
        		{
        			//Log.v(TAG,"URL="+syntax);
        			
        			URL url = new URL(syntax);
        			
        			DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
        			
        			httpClient.getCredentialsProvider().setCredentials(
        	                new AuthScope(url.getHost(), url.getPort()), 
        	                new UsernamePasswordCredentials(username, userpwd));   
                    
        			HttpPost httpRequest = new HttpPost(syntax);
        			
        			Iterator<Entry<String, String>> iter = param.entrySet().iterator();
        			if(isLocal)
        			{
        				
        				/*
        				//without url encode
        				StringBuilder params = new StringBuilder("");
        				while (iter.hasNext()) 
        				{
        				    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
        				    params.append((String)entry.getKey());
        					params.append("=");
        					params.append((String)entry.getValue());
        					params.append("&");
        				}
        				
        				if(params.toString().endsWith("&"))
        				{
        					params.delete(params.length()-1, params.length());
        				}
        				
        				ByteArrayEntity reqEntity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
        				httpRequest.addHeader(BasicScheme.authenticate( new UsernamePasswordCredentials(username, userpwd),"UTF-8", false));
        				httpRequest.setEntity(reqEntity);
        				*/
        			}
        			else
        			{
        				//need url encode
        				
        				List<NameValuePair> params = new ArrayList<NameValuePair>();
        				
        				while (iter.hasNext()) 
        				{
        				    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
        				    params.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
        				} 
        				
        				//Log.v(TAG,"params="+params);
        				//httpRequest.addHeader(BasicScheme.authenticate( new UsernamePasswordCredentials(username, userpwd),"UTF-8", false));
        				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			}
        			
        			BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
        			int code = httpResponse.getStatusLine().getStatusCode();
        			
        			if (code==200)
        			{
        				byte[] bytearray=EntityUtils.toByteArray(httpResponse.getEntity());
        				
        				bitmap=BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
            			/*
            			int width = bm.getWidth();

            			int height = bm.getHeight();

            			int newWidth = 200;

            			float scaleWidth = ((float) newWidth) / width;
            			
            			Matrix matrix = new Matrix();

            			matrix.postScale(scaleWidth, scaleWidth);
            			
            			bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,true);
            			*/

        				//Log.v(TAG,"code="+code);
        						
    	    			Message message = handler.obtainMessage(stop_flag,null);
        				handler.sendMessage(message);
        			}
        			else if (code==401)
        			{
        				Message message = handler.obtainMessage(2,null);
        				handler.sendMessage(message);
        				
        				//Log.v(TAG,"code="+code);
        			}
				} 
        		catch (MalformedURLException e) 
        		{
					//Log.v(TAG,"Error");
					e.printStackTrace();
				} 
        		catch (IOException e) 
        		{
        			//Log.v(TAG,"Error");
					e.printStackTrace();
				}
        		
			}
		}.start();
	}
	
	private static class MyHandler extends Handler
	{
		private final WeakReference<Activity> mActivity;
		
	    public MyHandler(Activity activity) 
	    {
	        mActivity = new WeakReference<Activity>(activity);
	    }
	    
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			ActivityCameraWatch activity = (ActivityCameraWatch) mActivity.get();
			
			if (msg.what==2)
			{
				activity.auth_show();
			}
			else if (msg.what==1)
			{
				activity.fps++;
				activity.image.setImageBitmap(activity.bitmap);
				activity.connecting();
			}
			else if (msg.what==3)
			{
				activity.text_fps.setText("FPS : "+String.valueOf(activity.fps));
				activity.fps=0;
			}
		}
	}
	
	private void auth_show()
	{
		View view= View.inflate(ActivityCameraWatch.this,R.layout.dialog_set_auth,null);
	
		final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
		final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
				
		uname_edit.setHint(R.string.dialog_account);
		upwd_edit.setHint(R.string.dialog_password);
		
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(ActivityCameraWatch.this);
		
		builder.setTitle(ActivityCameraWatch.this.getText(R.string.dialog_cam_title_user));
		builder.setView(view);
		builder.setMessage(ActivityCameraWatch.this.getText(R.string.dialog_cam_message_user));
		builder.setCancelable(false);
		builder.setPositiveButton(ActivityCameraWatch.this.getText(R.string.alert_button_ok), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				connecting();
			}
		});
		builder.setNegativeButton(ActivityCameraWatch.this.getText(R.string.alert_button_cancel), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				// TODO Auto-generated method stub
			}
		});
		
		builder.create().show();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		int ori =newConfig.orientation;

		Log.v(TAG,""+newConfig.orientation);

	    switch (ori) 
	    {
	    	case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT: 
	        {
	        	text_name.setVisibility(View.VISIBLE);
	   			text_fps.setVisibility(View.VISIBLE);
	        	break;
	        }
	        default :
	        {
	        	text_name.setVisibility(View.GONE);
   				text_fps.setVisibility(View.GONE);
	    		break;
	        }
	    } 
	    super.onConfigurationChanged(newConfig);
	}
	
	/*
	class MyOrientationEventListener extends OrientationEventListener
	{
		public MyOrientationEventListener(Context context, int rate)
		{
			super(context, rate);
			//TODO Auto-generated constructor stub
		}
		 
		@Override
		public void onOrientationChanged(int arg0) 
		{
			DisplayMetrics dm = getResources().getDisplayMetrics();
		
			int ScreenWidth = dm.widthPixels;
			int ScreenHeight = dm.heightPixels;
		    
			//Log.v(TAG,"Screen Width="+ScreenWidth);
			//Log.v(TAG,"Screen Height="+ScreenHeight);
			
			if (ScreenHeight>ScreenWidth)
			{
				text_name.setVisibility(View.VISIBLE);
	   			text_fps.setVisibility(View.VISIBLE);
			}
			else
			{
				text_name.setVisibility(View.GONE);
   				text_fps.setVisibility(View.GONE);
			}
		}
	}
	*/
}


