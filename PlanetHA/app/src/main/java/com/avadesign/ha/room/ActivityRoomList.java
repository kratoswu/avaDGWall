package com.avadesign.ha.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avadesign.ha.R;
import com.avadesign.ha.camera.ActivityCameraView;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.node.ActivityDeviceList;
import com.avadesign.ha.scene.ActivitySceneView;

public class ActivityRoomList extends BaseActivity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private LinearLayout admintoollayout;
	
	private Button control,scene,cam,back,add,del;
	
	private GridView gridview;
	
	private PictureListAdapter adapter;
	
	private ArrayList<HashMap<String,String>> location;
	
	private GetListTask mGetListTask;
	
	private Boolean delete;
	
	private ArrayList<HashMap<String,String>> loc_icon_array;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_list);
		
		FindView();
		
		Setlistener();
	}
		
	@Override
	protected void onResume() 
	{
		delete=false;
		
		control.setSelected(true);
		
		admintoollayout.setVisibility(cp.getUserName().equals("admin") ? View.VISIBLE : View.GONE);

		GetLocationIconAry();
		
		GetLocCommand();
		
		super.onResume();
	}
	
	private void FindView()
	{
		admintoollayout= (LinearLayout)findViewById(R.id.admintoolLayout);
		add= (Button)findViewById(R.id.tab_add);
		del= (Button)findViewById(R.id.tab_del);
		
		gridview = (GridView)this.findViewById(R.id.gridview);
		
		control= (Button)findViewById(R.id.tab_control);
		scene  = (Button)findViewById(R.id.tab_scene);
		cam    = (Button)findViewById(R.id.tab_cam);
		back   = (Button)findViewById(R.id.tab_back);
	}
	
	private void Setlistener()
	{
		add.setOnClickListener(admin_button_down);
		del.setOnClickListener(admin_button_down);
		add.setTag(1);
		del.setTag(2);
		
		control.setOnClickListener(tab_button_down);
		scene.setOnClickListener(tab_button_down);
		cam.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		control.setTag(1);
		scene.setTag(2);
		cam.setTag(3);
		back.setTag(4);
		
		back.setVisibility(View.GONE);
		
		location=new ArrayList<HashMap<String,String>>();
		adapter = new PictureListAdapter(this, gridview, null, null);
		
		gridview.setOnItemClickListener(grid_down);
		gridview.setAdapter(adapter);
		
		loc_icon_array= new ArrayList<HashMap<String,String>>();
	}
	
	private void GetLocationIconAry()
	{
		ArrayList<HashMap<String,String>> image_array=(ArrayList<HashMap<String,String>>)cp.getIcon_Image();
		
		loc_icon_array.clear();
		
		if (image_array!=null)
		{
			for(HashMap<String,String> map :  image_array)
			{
				if (map.get("group").equalsIgnoreCase("location"))
				{
					loc_icon_array.add(map);
				}
			}
		}
	}
			
	private GridView.OnItemClickListener grid_down = new OnItemClickListener()
	{
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			Log.v(TAG,"int="+arg2);
			
			if (!delete)
			{
				/*
				String room;
				
				if (arg2!=location.size())
				{
					final HashMap<String, String> loc=location.get(arg2);
					room=loc.get("location");
				}
				else
				{
					room="no_room";
				}
				*/
				ArrayList<String> loc_array= new ArrayList<String>();
				
				for(HashMap<String,String> map : location)
				{
					loc_array.add((String) map.get("location"));
				}
				
				Intent intent = new Intent();
				Bundle bundle= new Bundle();
        		bundle.putInt("page", arg2);
        		bundle.putSerializable("location", location);
        		intent.putExtras(bundle);
        		intent.setClass(ActivityRoomList.this, ActivityDeviceList.class);
        		//intent.setClass(ActivityRoomList.this, ActivityDeviceList.class);
    			startActivity(intent);	
			}
			else
			{
				if (arg2!=location.size())
				{
					final HashMap<String, String> DelItem=location.get(arg2);
					
					AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityRoomList.this);
					delAlertDialog.setTitle(R.string.room_del_title);
					delAlertDialog.setMessage(getString(R.string.room_del_message)+DelItem.get("location"));
					delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface arg0, int arg1) 
						{
							RemoveLocCommand(DelItem.get("id"));
						}
					});
					delAlertDialog.setNegativeButton(R.string.alert_button_cancel, null);
					delAlertDialog.show();
				}				
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
					control.setSelected(true);
					//scene.setSelected(false);
					//cam.setSelected(false);
					
					Intent intent = new Intent();
	        		intent.setClass(ActivityRoomList.this, ActivityRoomList.class);
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
	        		intent.setClass(ActivityRoomList.this, ActivitySceneView.class);
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
    				intent.setClass(ActivityRoomList.this, ActivityCameraView.class);
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
	
	private Button.OnClickListener admin_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer) v.getTag())
			{			
				case 1:
				{
					Intent intent = new Intent();
					Bundle bundle= new Bundle();
					bundle.putSerializable("image_array", loc_icon_array);
	        		intent.putExtras(bundle);
    				intent.setClass(ActivityRoomList.this, ActivityRoomAdd.class);
    				startActivity(intent);

					break;
				}
				case 2:
				{
					delete=!delete;
					del.setSelected(delete);
					adapter.notifyDataSetChanged();
				}
				default:
					break;
			}
		}
	};
		
	private void GetLocCommand()
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"location_list.cgi","action","load"});
	}
	
	private void RemoveLocCommand(String id)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"location_list.cgi","action","remove","id",id});
	}
	
	@SuppressWarnings("unused")
	private void AddLocCommand(String name)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"location_list.cgi","action","add","name",name});
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
			if (!params[2].equals("load"))
				map.put(params[3], params[4]);
			
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getUserName());
				map.put("userpwd", cp.getUserPwd());
				map.put("tunnelid", "0");
			}
						
			list = SendHttpCommand.getlist(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
					map, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed(),"room");
			

			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetListTask = null;
			
			cancelProgress(); 
			
			if (list!=null)
			{
				location.clear();
				
				for (HashMap<String,String> map : list) 
				{
					map.get("location");
					
					location.add(map);
					
					Log.v(TAG,"id-"+map.get("id")+" loc="+map.get("location"));
				}
				adapter.notifyDataSetChanged();
			}
			else
			{
				call404();
			}
		}
	}
	
	public class PictureListAdapter extends BaseAdapter
	{
		ViewHolder viewHolder;
        private LayoutInflater mInflater;      

        
        public PictureListAdapter(Context context, GridView gridView, List<String> items, List<String> paths)
        {
            mInflater = LayoutInflater.from(context);
        }
        
        private class ViewHolder 
        {
            TextView room;
            RelativeLayout relativeLayout;
            ImageView image,image_delete;
        }
        
        @Override
        public int getCount() 
        {
        	return location.size()+1;
        }

        @Override
        public Object getItem(int position) 
        {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) 
        {
            // TODO Auto-generated method stub
            return position;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            // TODO Auto-generated method stub
            if (convertView == null) 
            {
            	convertView = mInflater.inflate(R.layout.item_room,null);
            	
            	viewHolder = new ViewHolder();

        		viewHolder.room= (TextView) convertView.findViewById(R.id.textView1);
        		viewHolder.relativeLayout =(RelativeLayout)convertView.findViewById(R.id.RelativeLayout1);
        		viewHolder.image = (ImageView)convertView.findViewById(R.id.imageView2);
        		viewHolder.image_delete = (ImageView)convertView.findViewById(R.id.imageView1);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
              
            if (position==location.size())
            {
            	viewHolder.room.setText(getString(R.string.room_no_room));
            	viewHolder.relativeLayout.setSelected(false);
            	
            	viewHolder.image.setImageDrawable(getResources().getDrawable(R.drawable.room));
            }
            else
            {
            	viewHolder.image.setImageDrawable(getResources().getDrawable(R.drawable.room));
            	
            	viewHolder.relativeLayout.setSelected(delete);
            	viewHolder.image_delete.setVisibility(delete ? View.VISIBLE : View.INVISIBLE);

            	String loc=location.get(position).get("location").toString();
                
            	viewHolder.room.setText(loc);
            	
            	String path = Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+"image"+"/"+"location";
            	String file_name=null;
            	
            	for(HashMap<String,String> map : loc_icon_array)
            	{
            		String lab=location.get(position).get("image");
            		
            		if (lab.equalsIgnoreCase(map.get("type")))
            		{
            			file_name=map.get("Normal");
            			break;
            		}
            	}
            	
            	if (file_name!=null)
            	{
            		String [] name=file_name.split("/");
            	
            		if (name.length>=3)
                	{
                		Log.v(TAG,"=="+path+"/"+name[2]);
                		Bitmap bitmap = BitmapFactory.decodeFile(path+"/"+name[2]);
                		viewHolder.image.setImageBitmap(bitmap);
                	}
            	}
            }
            
            return convertView;   
        }
    }
}
