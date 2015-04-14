package com.avadesign.ha.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityNotifyList extends BaseActivity 
{
	private Boolean Edit;
	private Button edit,back;
	private ListView listview;
	private ArrayList<HashMap<String,String>> notifylist;
	private MyCustomAdapter adapter;
	private GetListTask mGetListTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitynotifylist);
		
		listview = (ListView)this.findViewById(R.id.listview);
		
		edit=(Button)findViewById(R.id.tab_edit);
		back=(Button)findViewById(R.id.tab_back);
		edit.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		edit.setTag(1);
		back.setTag(2);
		
		back.setVisibility(View.GONE);
		
		notifylist=new ArrayList<HashMap<String,String>>();
		adapter = new MyCustomAdapter(this, R.layout.item_notify,notifylist);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(listItem_down);
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		Edit=false;
		GetListCommand();
	}

	private void GetListCommand()
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"notify_list.cgi","action","load"});
	}
	
	private void RemoveLocCommand(final String id)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"notify_list.cgi","action","remove","id",id});
	}
	
	private void UpdateListCommand(final String id , final String active)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"notify_list.cgi","action","update","id",id,"active",active});
	}
	
	@SuppressWarnings("unused")
	private void AddListCommand(final String name , final String device_type , final String device_id)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"notify_list.cgi","action","add","name",name,"device_type",device_type,"device_id",device_id});
	}
	
	private class GetListTask extends AsyncTask<String, Void, Boolean> 
	{		
		ArrayList<HashMap<String,String>> list;
		
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
			if (params[2].equals("add"))
			{
				map.put(params[3], params[4]);
				map.put(params[5], params[6]);
				map.put(params[7], params[8]);
			}
			else if (params[2].equals("update"))
			{
				map.put(params[3], params[4]);
				map.put(params[5], params[6]);
			}
			else if (params[2].equals("remove"))
			{
				map.put(params[3], params[4]);
			}
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getControllerAcc());
				map.put("userpwd", cp.getControllerPwd());
				map.put("tunnelid", "0");
			}
			
			list = SendHttpCommand.getlist(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
					map, 
					cp.getControllerAcc(), 
					cp.getControllerPwd(), 
					cp.isLocalUsed(),"notify");
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetListTask = null;
			cancelProgress();
			
			if (list!=null)
			{
				notifylist.clear();
				for (HashMap<String,String> map : list) 
				{
					Log.v(TAG,"done");
					notifylist.add(map);
				}
				
				adapter.notifyDataSetChanged();
			}
			else
			{
				Log.v(TAG,"error");
				notifylist.clear();
				adapter.notifyDataSetChanged();
			}
		}
	}
		
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
		{
			if (Edit)
			{
				HashMap<String,String> map=notifylist.get(position);
				
				final String id=map.get("id");
				
				AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityNotifyList.this);
				   delAlertDialog.setTitle("Delete This Notification User?");
				   delAlertDialog.setMessage(map.get("name") +" will be delete?");
				   delAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface arg0, int arg1) 
					   {
						   RemoveLocCommand(id);
						   Log.v(TAG,"移除");
					   }
				   });
				   delAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface arg0, int arg1) 
					   {
						   Log.v(TAG,"取消");
					   }
				   });
				   delAlertDialog.show();
			}
			else
			{
				HashMap<String, String> map=notifylist.get(position);
				UpdateListCommand((String)map.get("id") , map.get("active").equalsIgnoreCase("false") ? "true" : "false");
			}
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
					Edit=!Edit;
					edit.setSelected(Edit);
					adapter.notifyDataSetChanged();
					
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
		}
	};
	
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
            TextView name;
            Switch sw;
            ImageButton del;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_notify,null);
            	
        		viewHolder = new ViewHolder();

        		viewHolder.name       = (TextView)convertView.findViewById(R.id.item_schedule_name);
        		viewHolder.sw		  = (Switch)  convertView.findViewById(R.id.item_schedule_sw);
        		viewHolder.del		  = (ImageButton)  convertView.findViewById(R.id.item_schedule_del);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
            if (Edit)
            {
            	viewHolder.sw.setVisibility(View.INVISIBLE);
            	viewHolder.del.setVisibility(View.VISIBLE);
            }
            else
            {
            	viewHolder.del.setVisibility(View.INVISIBLE);
            	viewHolder.sw.setVisibility(View.VISIBLE);
            }
            
            viewHolder.sw.setOnClickListener(sw_change);
            viewHolder.del.setOnClickListener(del_button_down);
            
            String name=notifylist.get(position).get("device_id").toString();
            
            String sw=notifylist.get(position).get("active").toString();
            
            viewHolder.del.setTag(position);
            
            viewHolder.sw.setTag(position);
            
            if (sw.equalsIgnoreCase("true"))
            	viewHolder.sw.setChecked(true);
            else
            	viewHolder.sw.setChecked(false);
            
            viewHolder.name.setText(name);
            	
            return convertView;
    	}
    }
	
	private Switch.OnClickListener sw_change = new Switch.OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			HashMap<String, String> map=notifylist.get((Integer) v.getTag());
			
			UpdateListCommand((String)map.get("id") , (boolean)((CompoundButton) v).isChecked() ? "true" : "false");
		};
	};
	
	private Button.OnClickListener del_button_down = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			HashMap<String,String> map=notifylist.get((Integer) v.getTag());
			
			final String id=map.get("id");
			
			AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityNotifyList.this);
			   delAlertDialog.setTitle("Delete This Notification User?");
			   delAlertDialog.setMessage(map.get("name") +" will be delete?");
			   delAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() 
			   {
				   public void onClick(DialogInterface arg0, int arg1) 
				   {
					   RemoveLocCommand(id);
					   Log.v(TAG,"移除");
				   }
			   });
			   delAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			   {
				   public void onClick(DialogInterface arg0, int arg1) 
				   {
					   Log.v(TAG,"取消");
				   }
			   });
			   delAlertDialog.show();
		}
	};
}
