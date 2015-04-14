package com.avadesign.ha.gateway;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
import com.avadesign.ha.frame.BaseActivity;

//Find Ava zwave Controller
public class ActivityGatewaySearch extends BaseActivity 
{
	private DatagramSocket mDSocket;
	
	private ReceiveCommandTask mReceiveCommandTask = null;
	
	private Handler mHandler = new Handler();
	
	private SimpleAdapter mAdapter;
	
	private ArrayList<HashMap<String,Object>> mControllers;
	
	private ArrayList<HashMap<String,Object>> history_array;
	
	private final int finderTimeot = 5000;
	
	private TextView title;
	
	private ListView listview;
	
	private Button search,back,history;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gateway_search);
			
		title=(TextView)findViewById(R.id.text_title);
		listview = (ListView)this.findViewById(R.id.listview);
		search= (Button)this.findViewById(R.id.tab_search);
		back= (Button)this.findViewById(R.id.tab_back);
		history= (Button)this.findViewById(R.id.tab_his);
		
		mControllers = new ArrayList<HashMap<String,Object>>();
		
		mAdapter = new SimpleAdapter(this,mControllers,android.R.layout.simple_expandable_list_item_2, new String[] {"MAC", "IP"}, new int[] { android.R.id.text1,android.R.id.text2} );
		
		history_array=new ArrayList<HashMap<String,Object>>();
		if (cp.getGatewayHistory()!=null)
			history_array.addAll(cp.getGatewayHistory());
		
		
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(listview_down);
		
		title.setText(getResources().getString(R.string.search_title));
		
		search.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		history.setOnClickListener(tab_button_down);
		search.setTag(1);
		back.setTag(2);
		history.setTag(3);
		
		back.setVisibility(View.GONE);
		
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(ActivityGatewaySearch.this);
		builder.setTitle(getString(R.string.gws_alert_title));
		builder.setMessage(getString(R.string.gws_alert_message));
		builder.setPositiveButton(getString(R.string.alert_button_ok), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				findController();
			}
		})
		.show();
	}
	
	private ListView.OnItemClickListener listview_down=new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			if (arg2==mControllers.size()-1)
			{
				Log.v(TAG,"¤â°Ê¿é¤J");
				
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
						String gw_mac=uname_edit.getText().toString();
						
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString("gw_mac",gw_mac);
						intent.putExtras(bundle);
						setResult(0,intent);

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
				//HashMap<String ,String> gw_map = new HashMap<String ,String>();
				
				String gw_mac=mControllers.get(arg2).get("MAC").toString();
				
				Intent intent = new Intent();
				
				Bundle bundle = new Bundle();
				bundle.putString("gw_mac",gw_mac);
				intent.putExtras(bundle);
				setResult(0,intent);
				
				Log.v(TAG,"gw_mac="+gw_mac);
				
				finish();
				//goNodeListPage(mControllers.get(arg2).get("ip").toString(), mControllers.get(arg2).get("mac").toString(), Integer.parseInt(mControllers.get(arg2).get("port").toString()), mControllers.get(arg2).get("version").toString(),true);
			}
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
			callProgress();
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
    					/*
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
    					*/
    					boolean isDuplicate = false;
    					
    					String [] receive=re.split("#");
    					
    					String [] info=receive[1].split("&");
    					
    					Log.v(TAG,"info="+info);
    					
    					HashMap<String,Object> map = new HashMap<String,Object>();
    					map.put("IP", receiveDP.getAddress().toString().substring(1));
    					map.put("PORT", String.valueOf(receiveDP.getPort()));
    					
    					for(String str : info)
    					{
    						str=str.replace("\r", "").replace("\n", "");
    						
    						String keyValue[]=str.split("=");
    						
    						map.put(keyValue[0].toUpperCase(),keyValue[1]);
    					}

    					for(HashMap<String,Object> mController : tmps)
    					{
    						if(mController.get("IP").toString().equals(receiveDP.getAddress().toString().substring(1)))
    						{
    							isDuplicate = true;
    							break;
    						}
    					}
    					
    					if(!isDuplicate)
    					{
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
			cancelProgress();
			
			mControllers.clear();
			
			for(HashMap<String,Object> tmp:tmps)
			{
				mControllers.add(tmp);
			}
			
			search_add_to_history();
			
			
			HashMap<String,Object> mu_map=new HashMap<String,Object>();
			mu_map.put("IP", "");
			mu_map.put("MAC", getString(R.string.manually_enter));
			mu_map.put("PORT", "");
			mu_map.put("VERSION", "");
			
			mControllers.add(mu_map);
			
			mAdapter.notifyDataSetChanged();
			
			
			if(mControllers.size()<1)
			{
				Toast.makeText(ActivityGatewaySearch.this, R.string.search_no_found, Toast.LENGTH_SHORT);
			}
		}
	}
	
	private void search_add_to_history()
	{
		for(HashMap<String,Object> map :mControllers)
	    {
	        Boolean have=false;
	        
	        Log.v(TAG,""+map);
	        
	        for(HashMap<String,Object> hic : history_array)
	        {
	            if (hic.get("MAC").equals(map.get("MAC")))
	            {
	                have=true;
	            }
	        }
	        if (!have)
	        	history_array.add(map);
	    }
	    
	    cp.setGatewayHistory(history_array);
	}
	
	private void goNodeListPage(String ip, String mac, int port, String version, boolean isLocal)
	{
		//00092C101a67
		cp.setControllerIP(ip);
		cp.setControllerMAC(mac);
		cp.setControllerPort(port);
		cp.setControllerVersion(version);
		cp.setIsLocalUsed(isLocal);
		cp.setStopPolling(true);
		
		if (cp.getControllerAcc().equalsIgnoreCase("") || cp.getControllerPwd().equalsIgnoreCase(""))
		{
			View view= View.inflate(ActivityGatewaySearch.this,R.layout.dialog_set_auth,null);
			
			final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
			final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
			
			uname_edit.setText(cp.getControllerAcc());
			upwd_edit.setText(cp.getControllerPwd());
			
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
					cp.setControllerAcc(uname_edit.getText().toString());
					cp.setControllerPwd(upwd_edit.getText().toString());
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
				case 3:
				{
					mControllers.clear();
					mControllers.addAll(history_array);
					
					mAdapter.notifyDataSetChanged();
					
					break;
				}
				default:
					break;
			}
		}
	};
}
