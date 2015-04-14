package com.avadesign.ha.scene;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.ServicePolling;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.node.ActivityNodeList;

public class ActivitySceneEditValue extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private ListView listview;
	
	private MyCustomAdapter adapter;
	
	private ArrayList<HashMap<String,Object>> mNodes;
	
	private String scene_id;
	
	private ZWaveNode znode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scene_edit_value);
		
		FindView();
		
		Setlistener();
	}
	
	private void FindView()
	{
		listview = (ListView)this.findViewById(R.id.activity_scene_edit_value_list);
	}
	
	private void Setlistener()
	{
		mNodes = new ArrayList<HashMap<String,Object>>();
		
		adapter = new MyCustomAdapter(this, R.layout.item_scene_edit_value,mNodes);
		
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(listItem_down);
	}
	
	@Override
	protected void onResume() 
	{
		((SharedClassApp)(ActivitySceneEditValue.this.getApplication())).setZWaveNode(null);
		CusPreference cp = new CusPreference(ActivitySceneEditValue.this);
		cp.setStopPolling(false);
		
		Bundle bundle=this.getIntent().getExtras();
		scene_id=bundle.getString("scene_id");
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
		
		adapter.notifyDataSetChanged();
		
		super.onResume();
	}
	
	@Override
	protected void onPause() 
	{
		CusPreference cp = new CusPreference(ActivitySceneEditValue.this);
		cp.setStopPolling(true);
		
		unregisterReceiver(mBroadcastReceiver);
		super.onPause();
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			CusPreference cp = new CusPreference(ActivitySceneEditValue.this);
			
			if(intent.getAction().equals(ServicePolling.HTTP_401))
			{
				DialogSetAuth.show(ActivitySceneEditValue.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
			
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivitySceneEditValue.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
				
				cp.setStopPolling(false);
				clearList();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
				if (mNodes.size()==0)
				{
					cp.setStopPolling(true);
					attemptGetData();
				}
			}
		}
	};
	
	private void clearList()
	{
		
		mNodes.clear();
		adapter.notifyDataSetChanged();
	}
	
	private void attemptGetData()
	{
		ArrayList<HashMap<String,Object>> nodes_ready = new ArrayList<HashMap<String,Object>>();
		
		((SharedClassApp)(ActivitySceneEditValue.this.getApplication())).refreshNodesList(nodes_ready);
		
		mNodes.clear();
		
		for(HashMap<String, Object> nodes : nodes_ready)
		{
			HashMap<String , Object> map=new HashMap<String , Object>();
			
			map.putAll(nodes);
			
			ArrayList<HashMap<String , Object>> value=(ArrayList<HashMap<String, Object>>) nodes.get("value");
			ArrayList<HashMap<String , Object>> new_value= new ArrayList<HashMap<String , Object>>();
			
			for(HashMap<String, Object> node_value : value)
			{
				new_value.add(node_value);
			}
			
			map.put("value", new_value);
			
			mNodes.add(map);
		}
		
		adapter.notifyDataSetChanged();
	}
	
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) 
		{
			ZWaveNode znode = new ZWaveNode(mNodes.get(position));
			
			((SharedClassApp)(ActivitySceneEditValue.this.getApplication())).setZWaveNode(znode);
			
			Log.v(TAG,"id=="+znode.id);
			
			Intent intent = new Intent();
			
			Bundle bundle= new Bundle();
    		bundle.putSerializable("location", null);
    		bundle.putSerializable("room_node_array", mNodes);
    		bundle.putInt("page", position);
    		bundle.putBoolean("SceneMode", true);
    		bundle.putString("scene_id", scene_id);
    		intent.putExtras(bundle);
    		
			intent.setClass(ActivitySceneEditValue.this, ActivityNodeList.class);
			startActivity(intent);
			
			PlayButtonSound.play(ActivitySceneEditValue.this);
			
			finish();
		}
	};
	
	public class MyCustomAdapter extends ArrayAdapter<HashMap<String, Object>>
    { 
    	ViewHolder viewHolder;
    	private LayoutInflater inflater;

    	public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Object>> arrayListItems) 
    	{
    		super(context, textViewResourceId, arrayListItems);
    		inflater = LayoutInflater.from(context);
        }
        // class for caching the views in a row
        private class ViewHolder 
        {
            ImageView icon;
            
            TextView name, location;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_scene_edit_value,null);
            	
        		viewHolder = new ViewHolder();

        		viewHolder.icon      = (ImageView) convertView.findViewById(R.id.item_image);
        		viewHolder.name       = (TextView)  convertView.findViewById(R.id.item_name);
        		viewHolder.location   = (TextView)  convertView.findViewById(R.id.item_location);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
            ZWaveNode znode = new ZWaveNode(mNodes.get(position));
            
            viewHolder.name.setText(znode.name_fix);
            viewHolder.location.setText(znode.location);
          
            int default_image=R.drawable.gw;
            
            if (znode.gtype.toLowerCase().equalsIgnoreCase("static pc controller") || znode.gtype.toLowerCase().indexOf("sensor")!=-1 || znode.gtype.toLowerCase().indexOf("repeater")!=-1)
            {
            	if (znode.gtype.toLowerCase().equalsIgnoreCase("static pc controller") || znode.gtype.toLowerCase().indexOf("repeater")!=-1)
            		default_image=R.drawable.gw;
            	else if (znode.name_fix.toLowerCase().indexOf("pir")!=-1 || znode.product.toLowerCase().indexOf("pir")!=-1)
            		default_image=R.drawable.iroff;
            	else if (znode.name_fix.toLowerCase().indexOf("motion")!=-1 || znode.product.toLowerCase().indexOf("motion")!=-1)
					default_image=R.drawable.iroff;
				else if (znode.name_fix.toLowerCase().indexOf("door")!=-1 || znode.product.toLowerCase().indexOf("door")!=-1)
					default_image=R.drawable.dooroff;
				else if (znode.name_fix.toLowerCase().indexOf("window")!=-1 || znode.product.toLowerCase().indexOf("window")!=-1)
					default_image=R.drawable.windowoff;
				else if (znode.name_fix.toLowerCase().indexOf("co")!=-1 || znode.product.toLowerCase().indexOf("co")!=-1)
					default_image=R.drawable.co_off;
				else if (znode.name_fix.toLowerCase().indexOf("smoke")!=-1 || znode.product.toLowerCase().indexOf("smoke")!=-1)
					default_image=R.drawable.smork_off;
				else if (znode.name_fix.toLowerCase().indexOf("shock")!=-1 || znode.product.toLowerCase().indexOf("shock")!=-1)
					default_image=R.drawable.shock_off;
				else if (znode.name_fix.toLowerCase().indexOf("water")!=-1 || znode.product.toLowerCase().indexOf("water")!=-1)
					default_image=R.drawable.water_off;
				else
					default_image=R.drawable.shock_off;
            	
            	String path=label_to_path_label(znode.icon,"off");
        		Bitmap bitmap = BitmapFactory.decodeFile(path);
        			
        		if (bitmap!=null)
        			viewHolder.icon.setImageBitmap(bitmap);
        		else
        			viewHolder.icon.setImageResource(default_image);
            }
            else if (znode.gtype.toLowerCase().indexOf("switch")!=-1 || znode.gtype.toLowerCase().indexOf("motor")!=-1)
            {
            	if (znode.name_fix.toLowerCase().indexOf("siren")!=-1 || znode.product.toLowerCase().indexOf("siren")!=-1)
            		default_image=R.drawable.alertoff;
            	else
            		default_image=R.drawable.dimmer_light_off;
            	
            	String path=label_to_path_label(znode.icon,"off");
    			Bitmap bitmap = BitmapFactory.decodeFile(path);
    			
    			if (bitmap!=null)
    				viewHolder.icon.setImageBitmap(bitmap);
    			else
    				viewHolder.icon.setImageResource(default_image);
            }
            else if (znode.gtype.toLowerCase().indexOf("lock")!=-1)
            {
            	default_image=R.drawable.doorkeypad_off;
            	
            	String path=label_to_path_label(znode.icon,"off");
    			Bitmap bitmap = BitmapFactory.decodeFile(path);
    			
    			if (bitmap!=null)
    				viewHolder.icon.setImageBitmap(bitmap);
    			else
    				viewHolder.icon.setImageResource(default_image);
            }
            
            return convertView;
    	}
    }
	
	private HashMap<String,Object> Node_id_to_node(final String node_id)
	{
		ArrayList<HashMap<String,Object>> nodes= new ArrayList<HashMap<String,Object>>();
		((SharedClassApp)(ActivitySceneEditValue.this.getApplication())).refreshNodesList(nodes);
		
		for (HashMap<String,Object> node : nodes)
		{
			ZWaveNode znode=new ZWaveNode(node);
			
			//Log.v(TAG,"zvalue="+znode.value);
			if (znode.id.equals(node_id))
				return node;
		}
		
		return null;
	}
	
	private String label_to_path_label (String label , String status)
	{
		CusPreference cp = new CusPreference(ActivitySceneEditValue.this);
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
	    			String path = Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+ary[0]+"/"+ary[1]+"/"+status;
	            	
	    			return path+"/"+ary[2];
	    		}
	    	}
	    }
		return "";
	}
}
