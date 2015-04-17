package com.avadesign.ha.login;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.ActivityMenuView;
import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.gateway.ActivityGatewaySearch;

public class ActivityGatewayList extends BaseActivity
{
	private DatagramSocket mDSocket;
	private ReceiveCommandTask mReceiveCommandTask = null;
	private final int finderTimeot = 2000;

	private ArrayList<HashMap<String,String>> gateway_list;

	private ArrayList<HashMap<String,String>> gw_list_sort;

	private HttpCommandTask mHttpCommandTask;

	private ListView listview;

	private GWAdapter gwAdapter;

	private Boolean new_flag;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gatewaylist);

		Bundle bundle=this.getIntent().getExtras();
		gateway_list=(ArrayList<HashMap<String, String>>) bundle.getSerializable("gateway_list");
		new_flag=bundle.getBoolean("new");
		FindView();

		Setlistener();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (gateway_list.size()==0)
			LoginCommand();
		else
		{
			RefreshAry();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (data!=null)
		{
			Bundle bundle=data.getExtras();

			String gw_mac=bundle.getString("gw_mac");

			AddCommand(gw_mac);
		}
	}

	private void FindView()
	{
		listview = (ListView)this.findViewById(R.id.listView1);

		gw_list_sort = new ArrayList<HashMap<String,String>>();

		gwAdapter = new GWAdapter(this, R.layout.item_user,gw_list_sort);
	}

	private void Setlistener()
	{
		listview.setAdapter(gwAdapter);
		listview.setOnItemClickListener(list_down);
	}

	private void RefreshAry()
	{
		gw_list_sort.clear();

		HashMap<String,String> title_map = new HashMap<String,String>();
		title_map.put("title", "1");
		title_map.put("msg",getString(R.string.section1));

		gw_list_sort.add(title_map);

		for(HashMap<String,String> map : gateway_list)
		{
			if (map.get("acc").toString().equalsIgnoreCase("admin"))
			{
				map.put("title", "0");

				gw_list_sort.add(map);
			}
		}

		title_map = new HashMap<String,String>();
		title_map.put("title", "1");
		title_map.put("msg",getString(R.string.section2));
		gw_list_sort.add(title_map);

		for(HashMap<String,String> map : gateway_list)
		{
			if (!map.get("acc").toString().equalsIgnoreCase("admin"))
			{
				map.put("title", "0");

				gw_list_sort.add(map);
			}
		}

		title_map = new HashMap<String,String>();
		title_map.put("title", "1");
		title_map.put("msg",getString(R.string.section3));
		gw_list_sort.add(title_map);

		gwAdapter.notifyDataSetChanged();

		//Log.v(TAG,"gw_list_sort="+gw_list_sort);
	}

	private ListView.OnItemClickListener list_down = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id)
		{
			HashMap<String,String> map=gw_list_sort.get(position);

            String hasTitle=map.get("title").toString();

			if (hasTitle.equalsIgnoreCase("1"))
			{
				if (map.get("msg").toString().equalsIgnoreCase(getString(R.string.section3)))
				{
					Intent intent = new Intent();
					intent.setClass(ActivityGatewayList.this, ActivityGatewaySearch.class);
					startActivityForResult(intent,0);
				}
			}
			else
			{
				//Log.v(TAG,"map="+map);
				String macc=map.get("mac").toString();
				String accc=map.get("acc").toString();
				String pwdc=map.get("pwd").toString();

				cp.setControllerMAC(macc);
				cp.setControllerPort(8080);
				cp.setControllerAcc(accc);
				cp.setControllerPwd(pwdc);

				cp.setIsLocalUsed(false);

				//Log.v(TAG,"new_flag="+new_flag);

				cp.setIsLocalUsed(false);
				findController();
			}
		}

	};

	private void LoginCommand()
	{
		if(mHttpCommandTask!=null)
			return;

		mHttpCommandTask = new HttpCommandTask();
		mHttpCommandTask.execute(new String[]{"cloud_login.cgi","acc",cp.getUserAccount(),"pwd",cp.getUserPassword()});
	}

	private void AddCommand(String mac)
	{
		if(mHttpCommandTask!=null)
			return;

		mHttpCommandTask = new HttpCommandTask();

		/*
		 * 2015-04-17, edited by Phoenix
		 * User 輸入的 MAC 位址可能是小寫的, 強制轉成全大寫.
		 * */
		mHttpCommandTask.execute(new String[]{"cloud_add_gateway.cgi","acc",cp.getUserAccount(),"pwd",cp.getUserPassword(),"mac",mac.toUpperCase()});
	}

	private void DeleteCommand(String mac)
	{
		if(mHttpCommandTask!=null)
			return;

		mHttpCommandTask = new HttpCommandTask();

		/*
         * 2015-04-17, edited by Phoenix
         * User 輸入的 MAC 位址可能是小寫的, 強制轉成全大寫.
         * */
		mHttpCommandTask.execute(new String[]{"cloud_rm_gateway.cgi","acc",cp.getUserAccount(),"pwd",cp.getUserPassword(),"mac",mac.toUpperCase()});
	}

	private class HttpCommandTask extends AsyncTask<String, Void, Boolean>
	{
		String result;
		String fun="";

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

			fun=params[0];

			for(int i=2;i<params.length;i+=2)
				map.put(params[i-1], params[i]);

			result = SendHttpCommand.getString(String.format(getString(R.string.server_url_format) , getString(R.string.server_ip) , getString(R.string.server_port) )+params[0],
					map);

			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(final Boolean success)
		{
			mHttpCommandTask = null;

			cancelProgress();

			JSONParser parser = new JSONParser();

			try
			{
				Map<?, ?> data = (Map<?, ?>)parser.parse(result);

				if (fun.equalsIgnoreCase("cloud_login.cgi"))
				{
					if (data.get("result").toString().equalsIgnoreCase("ok"))
					{
						gateway_list=(ArrayList<HashMap<String,String>>)data.get("acclist");

						RefreshAry();
					}
				}
				else //if (fun.equalsIgnoreCase("cloud_add_gateway.cgi"))
				{
					if (data.get("result").toString().equalsIgnoreCase("ok"))
					{
						LoginCommand();
					}
					else
					{
						Toast.makeText(ActivityGatewayList.this,data.get("msg").toString(), Toast.LENGTH_SHORT).show();
					}
					Log.v(TAG,"add delete done="+data);
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
	}

	public class GWAdapter extends ArrayAdapter<HashMap<String, String>>
    {
    	private ViewHolder viewHolder;
        private LayoutInflater inflater;
        private ArrayList<HashMap<String,String>> gw_list;

    	public GWAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, String>> arrayListItems)
    	{
    		super(context, textViewResourceId, arrayListItems);
    		inflater = LayoutInflater.from(context);

    		gw_list=arrayListItems;
        }

		private class ViewHolder
        {
            ImageButton edit,del;
            TextView name,title;
            LinearLayout layout1;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null)
            {
            	convertView = inflater.inflate(R.layout.item_user,null);

        		viewHolder = new ViewHolder();

        		viewHolder.edit  = (ImageButton) convertView.findViewById(R.id.imageButton2);
        		viewHolder.del   = (ImageButton) convertView.findViewById(R.id.imageButton1);
        		viewHolder.name  = (TextView)    convertView.findViewById(R.id.textView1);
        		viewHolder.title  = (TextView)    convertView.findViewById(R.id.textView2);
        		viewHolder.layout1 = (LinearLayout)convertView.findViewById(R.id.layout1);
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();

            viewHolder.edit.setVisibility(View.INVISIBLE);

            final HashMap<String,String> map=gw_list.get(position);

            String hasTitle=map.get("title").toString();

            if (hasTitle.equalsIgnoreCase("1"))
            {
            	viewHolder.title  .setVisibility(View.VISIBLE);
            	viewHolder.layout1.setVisibility(View.GONE);

            	viewHolder.title.setText(map.get("msg").toString());

            	if (map.get("msg").toString().equalsIgnoreCase(getString(R.string.section3)))
            		viewHolder.title.setBackgroundResource(R.color.clear);
            	else
            		viewHolder.title.setBackgroundResource(R.color.label_back);
            }
            else
            {
            	viewHolder.layout1.setVisibility(View.VISIBLE);
            	viewHolder.title  .setVisibility(View.GONE);

            	if (!map.get("acc").toString().equalsIgnoreCase("admin"))
            		viewHolder.del.setVisibility(View.INVISIBLE);

            	viewHolder.name.setText(map.get("mac").toString());

            	viewHolder.del.setOnClickListener(new Button.OnClickListener()
            	{
					@Override
					public void onClick(View v)
					{
						new AlertDialog.Builder(ActivityGatewayList.this)
						.setTitle(getString(R.string.delete_title))
						.setMessage(getString(R.string.delete_message))
						.setPositiveButton(R.string.alert_button_ok,new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								String macc=map.get("mac").toString();
								DeleteCommand(macc);
							}
						})
						.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{

							}
						})
						.show();
					}
            	});
            }

            return convertView;
    	}
    }

	private void findController()
	{
		callProgress();
		try
		{
			mDSocket = new DatagramSocket(10000);

			mReceiveCommandTask = new ReceiveCommandTask();
			mReceiveCommandTask.execute();

			Handler mHandler=new Handler();
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
				if(mDSocket!=null)
				{
					mDSocket.close();

					cancelProgress();

					if (new_flag)
					{
						Intent intent = new Intent();
						intent.setClass(ActivityGatewayList.this, ActivityMenuView.class);
						startActivity(intent);
					}
					finish();
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
		@Override
		protected void onPreExecute()
		{
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
    					String [] receive=re.split("#");

    					String [] info=receive[1].split("&");


    					HashMap<String,Object> map = new HashMap<String,Object>();
    					map.put("IP", receiveDP.getAddress().toString().substring(1));
    					map.put("PORT", String.valueOf(receiveDP.getPort()));

    					for(String str : info)
    					{
    						str=str.replace("\r", "").replace("\n", "");

    						String keyValue[]=str.split("=");

    						map.put(keyValue[0].toUpperCase(),keyValue[1]);
    					}

    					/*
    					String mac = re.substring(re.indexOf("mac=")+4).replace(":", "").replace("\r","").replace("\n","");
    					String ip = receiveDP.getAddress().toString().substring(1);
    					String version = re.substring(re.indexOf("version=")+8,re.indexOf("&mac="));
    					*/

    					String mac=map.get("MAC").toString();
    					String mac1=cp.getControllerMAC();

    					Log.v(TAG,"mac0="+mac+", len="+mac.length());
    					Log.v(TAG,"mac1="+mac1+", len="+mac1.length());

    					if(mac.equals(mac1))
						{
    						Log.v(TAG,"local...");
    						cp.setIsLocalUsed(true);
							cp.setControllerIP(map.get("IP").toString());
							cp.setControllerVersion(map.get("VERSION").toString());
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

		}
	}
}
