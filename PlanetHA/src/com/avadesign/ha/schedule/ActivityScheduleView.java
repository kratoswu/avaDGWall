package com.avadesign.ha.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.scene.ActivitySceneView;
import com.avadesign.ha.trigger.ActivityTriggerView;

public class ActivityScheduleView extends BaseActivity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private LinearLayout admintoollayout;
	
	private Button tab_scene,tab_trigger,tab_schedule,tab_back,tab_add,tab_edit,tab_del;
	
	private GridView gridview;
	
	private ArrayList<HashMap<String,String>> schedule_list;
	
	private GetScheduleTask mGetScheduleTask;
		
	private boolean edit=false,delete=false;
	
	private MyCustomAdapter adapter;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule_view);
		
		FindView();
		
		Setlistener();		
	}
	
	private void FindView()
	{
		admintoollayout= (LinearLayout)findViewById(R.id.admintoolLayout);
		tab_add= (Button)findViewById(R.id.tab_add);
		tab_edit= (Button)findViewById(R.id.tab_edit);
		tab_del= (Button)findViewById(R.id.tab_del);
		
		gridview = (GridView)this.findViewById(R.id.gridview);
		
		tab_scene= (Button)findViewById(R.id.tab_scene);
		tab_trigger= (Button)findViewById(R.id.tab_trigger);
		tab_schedule  = (Button)findViewById(R.id.tab_schedule);
		tab_back   = (Button)findViewById(R.id.tab_back);
	}
	
	private void Setlistener()
	{
		tab_add.setOnClickListener(admin_button_down);
		tab_edit.setOnClickListener(admin_button_down);
		tab_del.setOnClickListener(admin_button_down);
		tab_add.setTag(1);
		tab_edit.setTag(2);
		tab_del.setTag(3);
		
		tab_scene.setOnClickListener(tab_button_down);
		tab_trigger.setOnClickListener(tab_button_down);
		tab_schedule.setOnClickListener(tab_button_down);
		tab_back.setOnClickListener(tab_button_down);
		tab_scene.setTag(1);
		tab_trigger.setTag(2);
		tab_schedule.setTag(3);
		tab_back.setTag(4);
		
		tab_back.setVisibility(View.GONE);
		
		schedule_list = new ArrayList<HashMap<String,String>>();
		
		adapter = new MyCustomAdapter(this, R.layout.item_schedule,schedule_list);
		
		gridview.setAdapter(adapter);
		
		gridview.setOnItemClickListener(listItem_down);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		edit=delete=false;
		
		tab_edit.setSelected(edit);
		tab_del.setSelected(delete);
		
		admintoollayout.setVisibility(cp.getControllerAcc().equals("admin") ? View.VISIBLE : View.GONE);
		
		GetScheduleCommand();
		
		tab_schedule.setSelected(true);
	}
	
	private Button.OnClickListener admin_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch((Integer)v.getTag())
			{
				case 1:
				{
					HashMap<Object, Object> map=new HashMap<Object, Object>();
					
					Intent intent = new Intent();
					intent.setClass(ActivityScheduleView.this, ActivityScheduleViewEdit.class);
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
					tab_scene.setSelected(true);
				
					Intent intent = new Intent();
					intent.setClass(ActivityScheduleView.this, ActivitySceneView.class);
	    			startActivity(intent);
					
	    			finish();
					break;
				}
				case 2:
				{
					tab_trigger.setSelected(true);
				
					Intent intent = new Intent();
					intent.setClass(ActivityScheduleView.this, ActivityTriggerView.class);
	    			startActivity(intent);
					
	    			finish();
					break;
				}
				case 3:
				{
					tab_schedule.setSelected(true);
				
					Intent intent = new Intent();
					intent.setClass(ActivityScheduleView.this, ActivityScheduleView.class);
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
		}
	};
	
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
		{
			HashMap<String,String> map=schedule_list.get(position);
			
			if (cp.getControllerAcc().equals("admin"))
			{
				if (!edit && !delete)
				{
					UpdateScheduleCommand((String)map.get("id") , map.get("active").equalsIgnoreCase("false") ? "true" : "false");
				}
				else if (edit)
				{
					Intent intent = new Intent();
					intent.setClass(ActivityScheduleView.this, ActivityScheduleViewEdit.class);
					Bundle bundle=new Bundle();
					bundle.putSerializable("map", map);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				else if (delete)
				{
					final String id=map.get("id");
					
					AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityScheduleView.this);
					   delAlertDialog.setTitle(R.string.schedule_delete_title);
					   delAlertDialog.setMessage(getString(R.string.schedule_delete_message)+(map.get("label").equals("") ? "New Schedule" : map.get("label") ));
					   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
					   {
						   public void onClick(DialogInterface arg0, int arg1) 
						   {
							   RemoveScheduleCommand(id);
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
			}
			else
			{
				Intent intent = new Intent();
				intent.setClass(ActivityScheduleView.this, ActivityScheduleViewEdit.class);
				Bundle bundle=new Bundle();
				bundle.putSerializable("map", map);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}
	};
	
	private void GetScheduleCommand()
	{
		if(mGetScheduleTask!=null)
			return;
		
		mGetScheduleTask = new GetScheduleTask();
		mGetScheduleTask.execute(new String[]{"scene_schedule.cgi","action","load"});
	}
	
	private void RemoveScheduleCommand(String scene_id)
	{
		if(mGetScheduleTask!=null)
			return;
		
		mGetScheduleTask = new GetScheduleTask();
		mGetScheduleTask.execute(new String[]{"scene_schedule.cgi","action","remove","id",scene_id});
	}
	
	private void UpdateScheduleCommand(final String id , final String value)
	{
		if(mGetScheduleTask!=null)
			return;
		
		mGetScheduleTask = new GetScheduleTask();
		mGetScheduleTask.execute(new String[]{"scene_schedule.cgi","action","update","id",id,"active",value});
	}
	
	private class GetScheduleTask extends AsyncTask<String, Void, Boolean> 
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
			
			if (params[2].equals("remove"))
				map.put(params[3], params[4]);
			else if (params[2].equals("update"))
			{
				map.put(params[3], params[4]);
				map.put(params[5], params[6]);
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
					cp.isLocalUsed(),"schedule");
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetScheduleTask = null;
			cancelProgress();
			
			if (list!=null)
			{
				schedule_list.clear();
				for (HashMap<String,String> map : list) 
				{
					schedule_list.add(map);
					
					Log.v(TAG,"id="+map.get("id")+" label="+map.get("label"));
				}
				
				adapter.notifyDataSetChanged();
			}
			else
			{
				schedule_list.clear();
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
        	ImageView item_schedule_detail;
            TextView item_schedule_name;
            Switch item_schedule_sw;
            RelativeLayout relativeLayout;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_schedule,null);
            	
        		viewHolder = new ViewHolder();

        		viewHolder.item_schedule_detail = (ImageView) convertView.findViewById(R.id.item_schedule_detail);
        		viewHolder.item_schedule_name   = (TextView) convertView.findViewById(R.id.item_schedule_name);
        		viewHolder.item_schedule_sw	   = (Switch) convertView.findViewById(R.id.item_schedule_sw);
        		viewHolder.relativeLayout =(RelativeLayout)convertView.findViewById(R.id.RelativeLayout1);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
            if (delete || edit)
            {
            	viewHolder.relativeLayout.setSelected(true);
            	viewHolder.item_schedule_detail.setVisibility(View.VISIBLE);
            	viewHolder.item_schedule_sw.setVisibility(View.INVISIBLE);
            	
            	if (edit)
            		viewHolder.item_schedule_detail.setImageResource(R.drawable.edit);
            	else
            		viewHolder.item_schedule_detail.setImageResource(R.drawable.remove);
            }
            else
            {
            	viewHolder.relativeLayout.setSelected(false);
            	viewHolder.item_schedule_detail.setVisibility(View.INVISIBLE);
            	viewHolder.item_schedule_sw.setVisibility(View.VISIBLE);
            }
            
            viewHolder.item_schedule_sw.setTag(position);
            viewHolder.item_schedule_sw.setOnClickListener(sw_change);
            
            String name=schedule_list.get(position).get("label").toString();
            
            String sw=schedule_list.get(position).get("active").toString();
            
            viewHolder.item_schedule_sw.setChecked(sw.equalsIgnoreCase("true") ? true : false);
            
            viewHolder.item_schedule_name.setText(name.equals("") ? "New Schedule" : name);
            	
            return convertView;
    	}
    }
	
	private Switch.OnClickListener sw_change = new Switch.OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			HashMap<String, String> map=schedule_list.get((Integer) v.getTag());
			
			UpdateScheduleCommand((String)map.get("id") , (boolean)((CompoundButton) v).isChecked() ? "true" : "false");
		};
	};
}
