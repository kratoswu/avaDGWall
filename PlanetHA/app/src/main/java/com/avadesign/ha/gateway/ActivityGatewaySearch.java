package com.avadesign.ha.gateway;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.PlayButtonSound;

//Find Ava zwave Controller
public class ActivityGatewaySearch extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private ProgressDialog mDialog_SPINNER;
	private DatagramSocket mDSocket;
	
	private ReceiveCommandTask mReceiveCommandTask = null;
	
	private Handler mHandler = new Handler();
	
	private SimpleAdapter mAdapter;
	
	private ArrayList<HashMap<String,Object>> mControllers;
	
	private final int finderTimeot = 5000;
	
	private TextView title;
	
	private ListView listview;
	
	private Button search,back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gateway_search);
			
		title=(TextView)findViewById(R.id.text_title);
		listview = (ListView)this.findViewById(R.id.listview);
		search= (Button)this.findViewById(R.id.tab_search);
		back= (Button)this.findViewById(R.id.tab_back);
		
		mControllers = new ArrayList<HashMap<String,Object>>();
		
		mAdapter = new SimpleAdapter(this,mControllers,android.R.layout.simple_expandable_list_item_2, new String[] {"mac", "ip"}, new int[] { android.R.id.text1,android.R.id.text2} );
		
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(listview_down);
		
		title.setText(getResources().getString(R.string.search_title));
		
		search.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		
		search.setTag(1);
		back.setTag(2);
		
		back.setVisibility(View.GONE);
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
		

		findController();
	}
	
	private ListView.OnItemClickListener listview_down=new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			if (arg2==mControllers.size()-1)
			{
				final CusPreference cp = new CusPreference(ActivityGatewaySearch.this);
				
				View view= View.inflate(ActivityGatewaySearch.this,R.layout.dialog_set_auth,null);
				
				final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
				final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
				
				uname_edit.setHint(getString(R.string.dialog_message_set_mac));
				upwd_edit.setVisibility(View.GONE);
				
				AlertDialog.Builder builder;
				builder = new AlertDialog.Builder(ActivityGatewaySearch.this);
				
				builder.setTitle(getString(R.string.dialog_title_set_auth));
				builder.setView(view);
				builder.setMessage(getString(R.string.dialog_message_set_mac));
				builder.setCancelable(false);
				builder.setPositiveButton(getString(R.string.alert_button_ok), new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1) 
					{
						cp.setControllerIP("UnKnown");
						cp.setControllerMAC(uname_edit.getText().toString());
						cp.setControllerPort(5000);
						cp.setControllerVersion("UnKnown");
						cp.setIsLocalUsed(false);
						cp.setStopPolling(true);
						
						finish();
					}
				});
				builder.setNegativeButton(getString(R.string.alert_button_cancel), new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1) 
					{
						// TODO Auto-generated method stub
					}
				});
				
				builder.create().show();
			}
			else
				goNodeListPage(mControllers.get(arg2).get("ip").toString(), mControllers.get(arg2).get("mac").toString(), Integer.parseInt(mControllers.get(arg2).get("port").toString()), mControllers.get(arg2).get("version").toString(),true);
			
			PlayButtonSound.play(ActivityGatewaySearch.this);
		}
	};
	
	private void findController()
	{
		try
		{
			mDSocket = new DatagramSocket(10000);
			
			mReceiveCommandTask = new ReceiveCommandTask();
			mReceiveCommandTask.execute();
			mHandler.postDelayed(timeoutRunnable, finderTimeot);
			
			new Thread(broCommandRunnable).start();
		}
		catch(Exception e){
			e.printStackTrace();
			Log.d(TAG,"Error:"+e.toString());
		}
	}
	
	private Runnable broCommandRunnable = new Runnable()
	{
		@Override
		public void run() 
		{
			try
			{
				byte[] msg = new String("WHOIS_AVA_ZWAVE#").getBytes();
				DatagramPacket mDP = new DatagramPacket(msg, msg.length,InetAddress.getByName("255.255.255.255"), 10000);
				
				int i=0;
				while(i<3)
				{
					mDSocket.send(mDP);
					Thread.sleep(500);
					i++;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d(TAG,"Error:"+e.toString());
			}
		}
	};
	
	private Runnable timeoutRunnable = new Runnable()
	{
		@Override
		public void run() 
		{
			try
			{
//				if(mReceiveCommandTask!=null){
//					mReceiveCommandTask.cancel(true);
//				}
				if(mDSocket!=null)
				{
					mDSocket.close();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d(TAG,"Error:"+e.toString());
			}
		}
	};
	
	private class ReceiveCommandTask extends AsyncTask<String, Void, String> 
	{
		private ArrayList<HashMap<String,Object>> tmps = new ArrayList<HashMap<String,Object>>();
		@Override
		protected void onPreExecute() 
		{
			mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
			mDialog_SPINNER.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... args) 
		{
			MulticastLock mlock = null;
			try
			{
				WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    			mlock = wifi.createMulticastLock(TAG); 
    			mlock.acquire(); 
    			
    			while(!mDSocket.isClosed())
    			{
    				DatagramPacket receiveDP = new DatagramPacket(new byte[1024], 1024);
    				mDSocket.receive(receiveDP);
    				String re = new String(receiveDP.getData(), 0, receiveDP.getLength());
    				if(re.startsWith("RE_WHOIS_AVA_ZWAVE#"))
    				{
    					//RE_WHOIS_AVA_ZWAVE#version=sw-win32-0.2.0-config-1.1-20130&mac=485B3914F40D
    					boolean isDuplicate = false;
    					
    					String mac = re.substring(re.indexOf("mac=")+4).replace(":", "").replace("\r","").replace("\n","");
    					String port = "5000";
    					String ip = receiveDP.getAddress().toString().substring(1);
    					String version = re.substring(re.indexOf("version=")+8,re.indexOf("&mac="));

    					for(HashMap<String,Object> mController : tmps)
    					{
    						if(mController.get("mac").toString().equals(mac))
    						{
    							isDuplicate = true;
    							break;
    						}
    					}
    					
    					if(!isDuplicate)
    					{
    						HashMap<String,Object> map = new HashMap<String,Object>();
        					map.put("ip", ip);
        					map.put("mac", mac);
        					map.put("port", port);
        					map.put("version", version);
        					tmps.add(map);
    					}
    				}
				}
				
    			mDSocket.close();	
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d(TAG,"Error:"+e.toString());
			}
			
			finally
			{
				if(mlock!=null)
				{
					mlock.release();
					mlock = null;
				}
				if(mDSocket!=null)
				{
					mDSocket.close();
					mDSocket = null;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String re) 
		{
			mDialog_SPINNER.dismiss();
			
			mControllers.clear();
			
			for(HashMap<String,Object> tmp:tmps)
			{
				mControllers.add(tmp);
			}
			
			HashMap<String,Object> mu_map=new HashMap<String,Object>();
			mu_map.put("ip", "");
			mu_map.put("mac", getString(R.string.manually_enter));
			mu_map.put("port", "");
			mu_map.put("version", "");
			
			mControllers.add(mu_map);
			
			mAdapter.notifyDataSetChanged();
			
			if(mControllers.size()<1)
			{
				Toast.makeText(ActivityGatewaySearch.this, R.string.search_no_found, Toast.LENGTH_SHORT);
				
				/*
				CusPreference cp = new CusPreference(ActivityControFinder.this);
				
				cp.setControllerIP("192.168.1.171");
				cp.setControllerMAC("");
				cp.setControllerPort(5000);
				cp.setControllerVersion("");
				cp.setIsLocalUsed(false);
				cp.setStopPolling(false);
				*/
				/*
				Intent intent = new Intent();
				intent.setClass(ActivityControFinder.this, ActivityNodeList.class);
				startActivity(intent);
				*/
				
				//finish();
			}
			else if(mControllers.size()==1)
			{
				//goNodeListPage(mControllers.get(0).get("ip").toString(), mControllers.get(0).get("mac").toString(), Integer.parseInt(mControllers.get(0).get("port").toString()), mControllers.get(0).get("version").toString(),true);
			}
		}
	}
	
	private void goNodeListPage(String ip, String mac, int port, String version, boolean isLocal)
	{
		final CusPreference cp = new CusPreference(ActivityGatewaySearch.this);
		
		//00092C101a67
		cp.setControllerIP(ip);
		cp.setControllerMAC(mac);
		cp.setControllerPort(port);
		cp.setControllerVersion(version);
		cp.setIsLocalUsed(isLocal);
		cp.setStopPolling(true);
		
		if (cp.getUserName().equalsIgnoreCase("") || cp.getUserPwd().equalsIgnoreCase(""))
		{
			View view= View.inflate(ActivityGatewaySearch.this,R.layout.dialog_set_auth,null);
			
			final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
			final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
			
			uname_edit.setText(cp.getUserName());
			upwd_edit.setText(cp.getUserPwd());
			
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(ActivityGatewaySearch.this);
			
			builder.setTitle(getString(R.string.dialog_title_set_auth));
			builder.setView(view);
			builder.setMessage(getString(R.string.dialog_message_set_auth));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.alert_button_ok), new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
					cp.setUserName(uname_edit.getText().toString());
					cp.setUserPwd(upwd_edit.getText().toString());
					cp.setStopPolling(true);
					
					finish();
				}
			});
			builder.setNegativeButton(getString(R.string.alert_button_cancel), new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
					// TODO Auto-generated method stub
				}
			});
			
			builder.create().show();
		}
		else
		{
			finish();
		}
		//cp.setUserName("");
		//cp.setUserPwd("");
		/*
		Intent intent = new Intent();
		intent.setClass(ActivityControFinder.this, ActivityNodeList.class);
		startActivity(intent);
		*/
	}
	
	private Button.OnClickListener tab_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer) v.getTag())
			{			
				case 1:
				{
					findController();
					break;
				}
				case 2:
				{
					finish();
					break;
				}
				
				default:
					break;
			}
			
			PlayButtonSound.play(ActivityGatewaySearch.this);
		}
	};
}
