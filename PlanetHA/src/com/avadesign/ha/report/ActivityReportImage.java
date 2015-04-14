package com.avadesign.ha.report;

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

import org.apache.http.NameValuePair;
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
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avadesign.ha.R;



public class ActivityReportImage extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private ImageView imageview_graph;
	
	private TextView text_nodata;
	
	private Bitmap btm;
	
	private ProgressDialog mDialog_SPINNER;
	
	private Handler handler = new MyHandler(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report_image);
		
		FindView();
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}
	
	private void FindView()
	{
		imageview_graph	=(ImageView)findViewById(R.id.imageview_graph);
		
		text_nodata =(TextView)findViewById(R.id.textView1);
	}
	
	@SuppressWarnings("unchecked")
	protected void onResume() 
	{
		super.onResume();
		
		text_nodata.setVisibility(View.INVISIBLE);
		imageview_graph.setVisibility(View.INVISIBLE);
		
		Bundle bundle=this.getIntent().getExtras();
		HashMap<String, String> map=(HashMap<String, String>) bundle.getSerializable("map");
		
		mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
		mDialog_SPINNER.show();
		GetCamImage("http://54.215.15.185:8080/avaControl/powerchartimg.png",map);
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
			
			ActivityReportImage activity = (ActivityReportImage) mActivity.get();
			
			Log.v(activity.TAG,"Handler");
			
			Log.v(activity.TAG,"msg="+msg.what);
			
			if (msg.what==0)
			{
				activity.text_nodata.setVisibility(View.INVISIBLE);
				activity.imageview_graph.setVisibility(View.VISIBLE);
				activity.imageview_graph.setImageBitmap(activity.btm);
			}
			else
			{
				activity.text_nodata.setVisibility(View.VISIBLE);
				activity.imageview_graph.setVisibility(View.INVISIBLE);
			}
			activity.mDialog_SPINNER.dismiss();
		}
	}
	
	private void GetCamImage(final String syntax, final Map<String, String> param)
	{
		new Thread()
		{
			@SuppressWarnings("unused")
			public void run()
			{				
        		Bitmap bitmap =null;
        		
        		try 
        		{
        			Log.v(TAG,"URL="+syntax);
        			
        			URL url = new URL(syntax);
        			
        			HttpParams httpParameters = new BasicHttpParams();
        			
        			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
        			
        			HttpConnectionParams.setSoTimeout(httpParameters, 5000);
        			
        			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        			/*
        			httpClient.getCredentialsProvider().setCredentials(
        	                new AuthScope(url.getHost(), url.getPort()), 
        	                new UsernamePasswordCredentials(username, userpwd));   
                    */
        			HttpPost httpRequest = new HttpPost(syntax);
        			
        			Iterator<Entry<String, String>> iter = param.entrySet().iterator();
        				
        			List<NameValuePair> params = new ArrayList<NameValuePair>();
    				
    				while (iter.hasNext()) 
    				{
    				    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
    				    params.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
    				} 
    				
    				Log.v(TAG,"params="+params);
    				
    				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			
        			BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
        			int code = httpResponse.getStatusLine().getStatusCode();
        			
        			if (code==200)
        			{
        				byte[] bytearray=EntityUtils.toByteArray(httpResponse.getEntity());
        				
        				if (bytearray.length==0)
        				{
        					Message message = handler.obtainMessage(1,null);
            				handler.sendMessage(message);
        				}
        				else
        				{
        					Bitmap bm=BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
                			
                			int width = bm.getWidth();

                			int height = bm.getHeight();

                			Log.v(TAG,"width="+width);
                			Log.v(TAG,"height="+height);
                			
                			btm=bm;
                			
                			Message message = handler.obtainMessage(0,null);
            				handler.sendMessage(message);
        				}
        			}
        			else
        			{
        				Message message = handler.obtainMessage(1,null);
        				handler.sendMessage(message);
        			}
				} 
        		catch (MalformedURLException e) 
        		{
					Log.v(TAG,"Error");
					e.printStackTrace();
					
					Message message = handler.obtainMessage(1,null);
    				handler.sendMessage(message);
				} 
        		catch (IOException e) 
        		{
        			Log.v(TAG,"Error");
					e.printStackTrace();
					Message message = handler.obtainMessage(1,null);
    				handler.sendMessage(message);
				}
        		
			}
		}.start();
	}
}