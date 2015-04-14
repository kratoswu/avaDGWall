package com.avadesign.ha.camera;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.ServicePolling;
import com.avadesign.ha.room.ActivityRoomList;
import com.avadesign.ha.scene.ActivitySceneView;

public class ActivityCameraView extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private LinearLayout admintoollayout;
	
	private Button control,scene,cam,back,tab_add,tab_edit,tab_del;
	
	private GridView gridview;
	
	private ArrayList<HashMap<String,String>> cam_list;
	
	private GetCamTask mGetCamTask;
	private ProgressDialog mDialog_SPINNER;
	
	private boolean edit,delete;
	
	private MyCustomAdapter adapter;
	
	private Bitmap bit1,bit2,bit3,bit4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);
		
		FineView();
		
		Setlistener();
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
	}
	
	private void FineView()
	{
		admintoollayout= (LinearLayout)findViewById(R.id.admintoolLayout);
		tab_add= (Button)findViewById(R.id.tab_add);
		tab_edit= (Button)findViewById(R.id.tab_edit);
		tab_del= (Button)findViewById(R.id.tab_del);
		
		gridview = (GridView)this.findViewById(R.id.gridview);
		
		control= (Button)findViewById(R.id.tab_control);
		scene  = (Button)findViewById(R.id.tab_scene);
		cam    = (Button)findViewById(R.id.tab_cam);
		back   = (Button)findViewById(R.id.tab_back);
	}
	
	private void Setlistener()
	{
		tab_add.setOnClickListener(admin_button_down);
		tab_edit.setOnClickListener(admin_button_down);
		tab_del.setOnClickListener(admin_button_down);
		tab_add.setTag(1);
		tab_edit.setTag(2);
		tab_del.setTag(3);
		
		control.setOnClickListener(tab_button_down);
		scene.setOnClickListener(tab_button_down);
		cam.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		control.setTag(1);
		scene.setTag(2);
		cam.setTag(3);
		back.setTag(4);
		
		back.setVisibility(View.GONE);
		
		cam_list = new ArrayList<HashMap<String,String>>();
		
		adapter = new MyCustomAdapter(this, R.layout.item_cam, cam_list);
		
		gridview.setAdapter(adapter);
		
		gridview.setOnItemClickListener(listItem_down);
	}
	
	@Override
	protected void onResume() 
	{
		edit=delete=false;
		
		bit1=bit2=bit3=bit4=null;
		
		tab_edit.setSelected(edit);
		tab_del.setSelected(delete);
		
		CusPreference cp = new CusPreference(ActivityCameraView.this);
		admintoollayout.setVisibility(cp.getUserName().equals("admin") ? View.VISIBLE : View.GONE);
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		registerReceiver(mBroadcastReceiver, mFilter);

		GetCamCommand();
		
		cam.setSelected(true);
		super.onResume();
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
				DialogSetAuth.show(ActivityCameraView.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivityCameraView.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivityCameraView.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
			}
		}
	};
	
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			
			Log.v(TAG,"Handler");
			
			adapter.notifyDataSetChanged();
			
			if (msg.what==cam_list.size())
			{
				adapter.notifyDataSetChanged();
				//Log.v(TAG,"Handler notify");
			}
		}
	};
	
	private Button.OnClickListener admin_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer) v.getTag())
			{			
				case 1:
				{
					HashMap<Object, Object> map=new HashMap<Object, Object>();
					Intent intent = new Intent();
					intent.setClass(ActivityCameraView.this, ActivityCameraEdit.class);
					Bundle bundle=new Bundle();
					bundle.putSerializable("map", map);
					intent.putExtras(bundle);
					startActivity(intent);
					
					break;
				}
				case 2:
				{
					edit=!edit;
					tab_edit.setSelected(edit);
					
					delete=false;
					tab_del.setSelected(false);
					
					adapter.notifyDataSetChanged();
					break;
				}
				case 3:
				{
					delete=!delete;
					tab_del.setSelected(delete);
					
					edit=false;
					tab_edit.setSelected(false);
					
					adapter.notifyDataSetChanged();
					break;
				}
				default:
					break;
			}
			
			PlayButtonSound.play(ActivityCameraView.this);
		}
	};
	
	private Button.OnClickListener tab_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer) v.getTag())
			{
				case 1:
				{
					control.setSelected(true);
					//scene.setSelected(false);
					//cam.setSelected(false);
					
					Intent intent = new Intent();
					intent.setClass(ActivityCameraView.this, ActivityRoomList.class);
	    			startActivity(intent);
					
	    			finish();
					break;
				}
				case 2:
				{
					//control.setSelected(false);
					scene.setSelected(true);
					//cam.setSelected(false);
					
					Intent intent = new Intent();
					intent.setClass(ActivityCameraView.this, ActivitySceneView.class);
	    			startActivity(intent);
					
	    			finish();
					break;
				}
				case 3:
				{
					//control.setSelected(false);
					//scene.setSelected(false);
					cam.setSelected(true);
					
            		Intent intent = new Intent();
            		intent.setClass(ActivityCameraView.this, ActivityCameraView.class);
    				startActivity(intent);
    				finish();
					break;
				}
				case 4:
				{
					finish();
					break;
				}
				default:
					break;
			}
			
			PlayButtonSound.play(ActivityCameraView.this);
		}
	};
	
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) 
		{
			final HashMap<String, String> map=cam_list.get(position);
			
			String tunnel=map.get("specific_id");
			
			final Integer tunnel_id=Integer.valueOf(tunnel)/10000;
			
			final String sub=map.get("sub_url");
			
			String active=map.get("active");
			
			final String cam_id=map.get("id");
			
			if (!edit && !delete)
			{
				if (active.equals("true"))
				{
					final CusPreference cp = new CusPreference(ActivityCameraView.this);
					
					if(!cp.isLocalUsed())
					{
						mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
						mDialog_SPINNER.show();
						
						new Thread()
						{
							public void run()
							{
								Map<String, String> map_http = new HashMap<String,String>();
								map_http.put("action", "execute");
								map_http.put("id", cam_id);
								
								if(!cp.isLocalUsed()){
									map_http.put("mac", cp.getControllerMAC());
									map_http.put("username", cp.getUserName());
									map_http.put("userpwd", cp.getUserPwd());
									map_http.put("tunnelid", "0");
								}
								
								Boolean success=SendHttpCommand.send(
										
										String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"camera_list.cgi",
										map_http, 
										cp.getUserName(), 
										cp.getUserPwd(), 
										cp.isLocalUsed());
								
								//Log.v(TAG,String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"camera_list.cgi");
								//Log.v(TAG,"map="+map_http);
								//Log.v(TAG,"success="+success);
								
								if (success)
								{
									Intent intent = new Intent();
									
									Bundle bundle= new Bundle();
						    		bundle.putString("ip", map.get("ip"));
						    		bundle.putString("port", map.get("port"));
						    		bundle.putString("sub_url", sub.substring(1));
						    		bundle.putString("acc", map.get("account")==null ? "" : map.get("account"));
						    		bundle.putString("pwd", map.get("pwd")==null ? "" : map.get("pwd"));
						    		bundle.putString("tunnel_id", String.valueOf(tunnel_id-1));
						    		bundle.putString("name", map.get("name"));
						    		intent.putExtras(bundle);
						    		
						    		intent.setClass(ActivityCameraView.this, ActivityCameraWatch.class);
									startActivity(intent);
									
									mDialog_SPINNER.dismiss();
								}
							}
						}.start();
					}
					else    
					{
						Intent intent = new Intent();
						
						Bundle bundle= new Bundle();
			    		bundle.putString("ip", map.get("ip"));
			    		bundle.putString("port", map.get("port"));
			    		bundle.putString("sub_url", sub.substring(1));
			    		bundle.putString("acc", map.get("account")==null ? "" : map.get("account"));
			    		bundle.putString("pwd", map.get("pwd")==null ? "" : map.get("pwd"));
			    		bundle.putString("tunnel_id", String.valueOf(tunnel_id-1));
			    		bundle.putString("name", map.get("name"));
			    		intent.putExtras(bundle);
			    		
			    		intent.setClass(ActivityCameraView.this, ActivityCameraWatch.class);
						startActivity(intent);
					}
				}
			}
			else if (edit)
			{
				Intent intent = new Intent();
				intent.setClass(ActivityCameraView.this, ActivityCameraEdit.class);
				Bundle bundle=new Bundle();
				bundle.putSerializable("map", map);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			else if (delete)
			{
				AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityCameraView.this);
				   delAlertDialog.setTitle(R.string.cam_delete_title);
				   delAlertDialog.setMessage(getString(R.string.cam_delete_message) + map.get("name"));
				   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface arg0, int arg1) 
					   {
						   RemoveCamCommand(cam_id);

						   Log.v(TAG,"²¾°£");
					   }
				   });
				   delAlertDialog.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface arg0, int arg1) 
					   {
						   Log.v(TAG,"¨ú®ø");
					   }
				   });
				   delAlertDialog.show();
			}
			
			PlayButtonSound.play(ActivityCameraView.this);
		}
	};
	
	private void GetCamImage(final String syntax, final Map<String, String> param, final String username, final String userpwd, final boolean isLocal, final String tunnel_id)
	{
		new Thread()
		{
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
        				
        				Log.v(TAG,"params="+params);
        				//httpRequest.addHeader(BasicScheme.authenticate( new UsernamePasswordCredentials(username, userpwd),"UTF-8", false));
        				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        			}
        			
        			BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
        			int code = httpResponse.getStatusLine().getStatusCode();
        			
        			if (code==200)
        			{
        				byte[] bytearray=EntityUtils.toByteArray(httpResponse.getEntity());
        				
        				Bitmap bm=BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
            			
            			int width = bm.getWidth();

            			int height = bm.getHeight();

            			Log.v(TAG,"width="+width);
            			Log.v(TAG,"height="+height);
            			
            			DisplayMetrics metrics = new DisplayMetrics();  
            		    getWindowManager().getDefaultDisplay().getMetrics(metrics);

            		    //metrics.widthPixels
            		    //metrics.heightPixels
            			
            			int newWidth = metrics.widthPixels/2-(metrics.widthPixels/10);

            			float scaleWidth = ((float) newWidth) / width;
            			
            			//Log.v(TAG,"scaleWidth="+scaleWidth);
            			
            			Matrix matrix = new Matrix();

            			matrix.postScale(scaleWidth, scaleWidth);
            			
            			bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,true);
            			
            			//Log.v(TAG,"width="+bitmap.getWidth());
            			//Log.v(TAG,"height="+bitmap.getHeight());
            			
            			//Log.v(TAG,"matrix="+matrix);
            			
            			if (tunnel_id.equals("1"))
            				bit1=bitmap;
            			else if (tunnel_id.equals("2"))
            				bit2=bitmap;
            			else if (tunnel_id.equals("3"))
            				bit3=bitmap;
            			else if (tunnel_id.equals("4"))
            				bit4=bitmap;
            			
            			Message message = handler.obtainMessage(Integer.valueOf(tunnel_id),null);
        				handler.sendMessage(message);
            			
    	    			Log.v(TAG,"Success="+tunnel_id);
        			}
        			else
        			{
        				Message message = handler.obtainMessage(Integer.valueOf(tunnel_id),null);
        				handler.sendMessage(message);
        				
        				Log.v(TAG,code+"="+tunnel_id);
        			}
				} 
        		catch (MalformedURLException e) 
        		{
					Log.v(TAG,"Error");
					e.printStackTrace();
				} 
        		catch (IOException e) 
        		{
        			Log.v(TAG,"Error");
					e.printStackTrace();
				}
        		
			}
		}.start();
	}
	
	private void GetCamCommand()
	{
		if(mGetCamTask!=null)
			return;
		
		mGetCamTask = new GetCamTask();
		mGetCamTask.execute(new String[]{"camera_list.cgi","action","load"});
	}
		
	private void RemoveCamCommand(String cam_id)
	{
		if(mGetCamTask!=null)
			return;
		
		mGetCamTask = new GetCamTask();
		mGetCamTask.execute(new String[]{"camera_list.cgi","action","remove","id",cam_id});
	}
	
	private class GetCamTask extends AsyncTask<String, Void, Boolean> 
	{		
		ArrayList<HashMap<String,String>> list;
		
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
			CusPreference cp = new CusPreference(ActivityCameraView.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(params[1], params[2]);

			if (params[2].equals("remove"))
				map.put(params[3], params[4]);
			
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getUserName());
				map.put("userpwd",  cp.getUserPwd());
				map.put("tunnelid", "0");
			}
			
			list = SendHttpCommand.getlist(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
					map, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed(),"cam");
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetCamTask = null;
			mDialog_SPINNER.dismiss();
			
			if (list!=null)
			{
				cam_list.clear();
				for (int i=0;i<list.size();i++) 
				{
					HashMap<String,String> map=list.get(i);
					cam_list.add(map);
						
					
					//Get Cam Image--------------------------------------
					CusPreference cp = new CusPreference(ActivityCameraView.this);
					
					String tunnel=map.get("specific_id");
					
					Integer tunnel_id=Integer.valueOf(tunnel)/10000;
					
					Map<String, String> map1 = new HashMap<String,String>();
					if(!cp.isLocalUsed()){
						map1.put("mac", cp.getControllerMAC());
						map1.put("username", map.get("account"));
						map1.put("userpwd" , map.get("pwd"));
						map1.put("tunnelid", String.valueOf(tunnel_id-1));
					}
					
					String sub=map.get("sub_url");
					
					if (!sub.equals("") && sub!=null)
						GetCamImage(String.format(cp.isLocalUsed()?getString(R.string.cam_url_syntax):getString(R.string.server_url_syntax),cp.isLocalUsed()?map.get("ip"):getString(R.string.server_ip),cp.isLocalUsed()?map.get("port"):getString(R.string.server_port)
							)+sub.substring(1),map1,map.get("account")==null ? "" : map.get("account"),map.get("pwd")==null ? "" : map.get("pwd"),cp.isLocalUsed(),String.valueOf(tunnel_id-1));
					//--------------------------------------
				}
				
				adapter.notifyDataSetChanged();
			}
			else
			{
				cam_list.clear();
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	public class MyCustomAdapter extends ArrayAdapter<HashMap<String, String>>
    { 
    	ViewHolder viewHolder;
    	private LayoutInflater inflater;
    	
    	public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, String>> arrayListItems) 
    	{
    		super(context, textViewResourceId, arrayListItems);
    		inflater = LayoutInflater.from(context);
        }
        // class for caching the views in a row
    	private class ViewHolder 
        {
            ImageView item_trigger_detail,icon;
            TextView item_trigger_name;
            RelativeLayout relativeLayout;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
    		if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_cam,null);

        		viewHolder = new ViewHolder();

        		viewHolder.icon= (ImageView) convertView.findViewById(R.id.imageView2);
        		viewHolder.item_trigger_detail = (ImageView) convertView.findViewById(R.id.imageView1);
        		viewHolder.item_trigger_name   = (TextView) convertView.findViewById(R.id.textView1);
        		viewHolder.relativeLayout =(RelativeLayout)convertView.findViewById(R.id.RelativeLayout1);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
    		if (delete || edit)
            {
            	viewHolder.relativeLayout.setSelected(true);
            	viewHolder.item_trigger_detail.setVisibility(View.VISIBLE);
            	
            	if (edit)
            		viewHolder.item_trigger_detail.setImageResource(R.drawable.edit);
            	else
            		viewHolder.item_trigger_detail.setImageResource(R.drawable.remove);
            }
            else
            {
            	viewHolder.relativeLayout.setSelected(false);
            	viewHolder.item_trigger_detail.setVisibility(View.INVISIBLE);
            }
    		
    		String tunnel=cam_list.get(position).get("specific_id").toString();
			
    		Integer tunnel_id=(Integer.valueOf(tunnel)/10000)-1;

    		viewHolder.icon.setImageDrawable(getResources().getDrawable(R.drawable.menu_cam));
    		Bitmap loadingIcon = BitmapFactory.decodeResource(getResources(),R.drawable.menu_cam);
    		
    		switch (tunnel_id)
    		{
    			case 1:
    			{
    				if (bit1!=null)
    				{
    					/*
    					LayoutParams para;
    			        para = viewHolder.icon.getLayoutParams();
    			        
    			        Log.d(TAG, "layout height0: " + para.height);
    			        Log.d(TAG, "layout width0: " + para.width);
    			        
    			        para.height = bit1.getHeight();
    			        para.width = bit1.getWidth();
    			        viewHolder.icon.setLayoutParams(para);
    			        */
    					viewHolder.icon.setImageBitmap(bit1);
    				}
    				else
    					viewHolder.icon.setImageBitmap(loadingIcon);
    				break;
    			}
    			case 2:
    			{
    				if (bit2!=null)
    					viewHolder.icon.setImageBitmap(bit2);
    				else
    					viewHolder.icon.setImageBitmap(loadingIcon);
    				break;
    			}
    			case 3:
    			{
    				if (bit3!=null)
    					viewHolder.icon.setImageBitmap(bit3);
    				else
    					viewHolder.icon.setImageBitmap(loadingIcon);
    				break;
    			}
    			case 4:
    			{
    				if (bit4!=null)
    					viewHolder.icon.setImageBitmap(bit4);
    				else
    					viewHolder.icon.setImageBitmap(loadingIcon);
    				break;
    			}
    		}
            	
    		String name=cam_list.get(position).get("name").toString();
            
            viewHolder.item_trigger_name.setText(name.equals("") ? "New Camera" : name);
            
            return convertView;
    	}
    }
}
