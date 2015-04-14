package com.avadesign.ha.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.camera.ActivityCameraView;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.ServicePolling;
import com.avadesign.ha.room.ActivityRoomList;

public class ActivitySceneView extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private LinearLayout admintoollayout;
	
	private Button control,scene,cam,back,add,edit,del;
	
	private GridView gridview;
	
	private PictureListAdapter adapter;

	private Handler mHandler = new Handler();
	
	private ArrayList<HashMap<String,String>> scene_list;
	
	private GetSceneTask mGetSceneTask;
	private ProgressDialog mDialog_SPINNER;
	private boolean Edit,delete;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scene_view);
		
		FindView();
		
		Setlistener();
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
	}
	
	private void FindView()
	{
		admintoollayout= (LinearLayout)findViewById(R.id.admintoolLayout);
		add= (Button)findViewById(R.id.tab_add);
		edit= (Button)findViewById(R.id.tab_edit);
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
		edit.setOnClickListener(admin_button_down);
		del.setOnClickListener(admin_button_down);
		add.setTag(1);
		edit.setTag(2);
		del.setTag(3);
		
		control.setOnClickListener(tab_button_down);
		scene.setOnClickListener(tab_button_down);
		cam.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		control.setTag(1);
		scene.setTag(2);
		cam.setTag(3);
		back.setTag(4);
		
		back.setVisibility(View.GONE);
	
		scene_list = new ArrayList<HashMap<String,String>>();
		adapter = new PictureListAdapter(this, gridview, null, null);
		gridview.setOnItemClickListener(grid_down);
		gridview.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() 
	{
		Edit=delete=false;
		
		edit.setSelected(Edit);
		del.setSelected(delete);
		
		CusPreference cp = new CusPreference(ActivitySceneView.this);
		admintoollayout.setVisibility(cp.getUserName().equals("admin") ? View.VISIBLE : View.GONE);
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		registerReceiver(mBroadcastReceiver, mFilter);

		GetSceneCommand();
		
		scene.setSelected(true);
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
				DialogSetAuth.show(ActivitySceneView.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivitySceneView.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivitySceneView.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
			}
		}
	};
	
	private GridView.OnItemClickListener grid_down = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			Log.v(TAG,"int="+arg2);
			
			HashMap<String,String> map=scene_list.get(arg2);
			
			if (!Edit && !delete)
			{
				SetSceneCommand(map.get("id"));
				//RunSceneCommand(map.get("id"));
			}
			else if (Edit)
			{
				Log.v(TAG, map.get("id"));
				Log.v(TAG, map.get("label"));
				
				Intent intent = new Intent();
				intent.setClass(ActivitySceneView.this, ActivitySceneEdit.class);
				Bundle bundle=new Bundle();
				bundle.putString("scene_id",map.get("id"));
				bundle.putString("scene_name",map.get("label"));
				intent.putExtras(bundle);
				startActivity(intent);
				
			}
			else if (delete)
			{
				//Log.v(TAG,map.get("id"));
				
				final String id=map.get("id");
				
				String delete_name=map.get("label").equals("") ? "New Scene" : map.get("label");
				
				AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivitySceneView.this);
				   delAlertDialog.setTitle(R.string.scene_delete_title);
				   delAlertDialog.setMessage(getString(R.string.scene_delete_message) + delete_name);
				   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface arg0, int arg1) 
					   {
						   RemoveSceneCommand(id);
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
			
			PlayButtonSound.play(ActivitySceneView.this);
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
					AddSceneCommand();
					break;
				}
				case 2:
				{
					Edit=!Edit;
					edit.setSelected(Edit);
					
					delete=false;
					del.setSelected(false);
					
					adapter.notifyDataSetChanged();
					break;
				}
				case 3:
				{
					delete=!delete;
					del.setSelected(delete);
					
					Edit=false;
					edit.setSelected(false);
					
					adapter.notifyDataSetChanged();
					break;
				}
				default:
					break;
			}
			PlayButtonSound.play(ActivitySceneView.this);
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
					intent.setClass(ActivitySceneView.this, ActivityRoomList.class);
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
					intent.setClass(ActivitySceneView.this, ActivitySceneView.class);
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
            		intent.setClass(ActivitySceneView.this, ActivityCameraView.class);
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
			
			PlayButtonSound.play(ActivitySceneView.this);
		}
	};
			
	private void SetSceneCommand(final String scene_id)
	{
		mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
		mDialog_SPINNER.show();
		
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivitySceneView.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("fun","execute");
				map.put("id",scene_id);
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					map.put("tunnelid", "0");
				}
				
				SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"scenepost.html",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
				
				mDialog_SPINNER.dismiss(); 
			}
		}.start();
	}
	
	private void GetSceneCommand()
	{
		if(mGetSceneTask!=null)
			return;
		
		mGetSceneTask = new GetSceneTask();
		mGetSceneTask.execute(new String[]{"scenepost.html","fun","load"});
	}
	/*
	private void RunSceneCommand(String scene_id)
	{
		if(mGetSceneTask!=null)
			return;
		
		mGetSceneTask = new GetSceneTask();
		mGetSceneTask.execute(new String[]{"scenepost.html","fun","execute","id",scene_id});
	}
	*/
	private void RemoveSceneCommand(String scene_id)
	{
		if(mGetSceneTask!=null)
			return;
		
		mGetSceneTask = new GetSceneTask();
		mGetSceneTask.execute(new String[]{"scenepost.html","fun","delete","id",scene_id});
	}
	
	private void AddSceneCommand()
	{
		if(mGetSceneTask!=null)
			return;
		
		mGetSceneTask = new GetSceneTask();
		mGetSceneTask.execute(new String[]{"scenepost.html","fun","create"});
	}
		
	private class GetSceneTask extends AsyncTask<String, Void, Boolean> 
	{		
		ArrayList<HashMap<String,String>> list;
		
		Boolean add_boo=false;
		
		
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
			if (params[2].equals("create"))
			{
				add_boo=true;
			}
			
			CusPreference cp = new CusPreference(ActivitySceneView.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(params[1], params[2]);
			if ( params[2].equals("delete") || params[2].equals("execute") )
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
					cp.isLocalUsed(),"scene");
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetSceneTask = null;
			mDialog_SPINNER.dismiss();
			
			if (list!=null)
			{
				scene_list.clear();
				for (HashMap<String,String> map : list) 
				{
					scene_list.add(map);
					
					Log.v(TAG,"id-"+map.get("id")+" label="+map.get("label"));
				}

				//if (add_boo)
				//{
				//	mHandler.postDelayed(add_Runnable, 500);
				//}
				//else
					adapter.notifyDataSetChanged();
			}
			else
			{
				scene_list.clear();
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	private Runnable add_Runnable = new Runnable()
	{
		@Override
		public void run() 
		{
			try
			{
				HashMap<String, String> map=scene_list.get(scene_list.size()-1);
				
				Log.v(TAG, map.get("id"));
				Log.v(TAG, map.get("label"));
				
				Intent intent = new Intent();
				intent.setClass(ActivitySceneView.this, ActivitySceneEdit.class);
				Bundle bundle=new Bundle();
				bundle.putString("scene_id",map.get("id"));
				bundle.putString("scene_name",map.get("label"));
				intent.putExtras(bundle);
				startActivity(intent);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d(TAG,"Error:"+e.toString());
			}
		}
	};
	
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
            TextView scene;
            RelativeLayout relativeLayout;
            ImageView image;
        }
        
        @Override
        public int getCount() 
        {
        	return scene_list.size();
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
            	convertView = mInflater.inflate(R.layout.item_scene,null);
            	
            	viewHolder = new ViewHolder();

        		viewHolder.scene= (TextView) convertView.findViewById(R.id.textView1);
        		viewHolder.relativeLayout =(RelativeLayout)convertView.findViewById(R.id.RelativeLayout1);
        		viewHolder.image = (ImageView)convertView.findViewById(R.id.imageView1);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
              
            if (delete || Edit)
            {
            	viewHolder.relativeLayout.setSelected(true);
            	viewHolder.image.setVisibility(View.VISIBLE);
            	
            	if (Edit)
            		viewHolder.image.setImageResource(R.drawable.edit);
            	else
            		viewHolder.image.setImageResource(R.drawable.remove);
            }
            else
            {
            	viewHolder.relativeLayout.setSelected(false);
            	viewHolder.image.setVisibility(View.INVISIBLE);
            }

        	String scene_name=scene_list.get(position).get("label").toString();
            
        	viewHolder.scene.setText(scene_name.equals("") ? "New Scene" : scene_name);
            
            return convertView;   
        }
    }
	/*
	public class MyCustomAdapter extends ArrayAdapter<HashMap<String, String>>
    { 
    	ViewHolder viewHolder;
    	
    	public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, String>> arrayListItems) 
    	{
    		super(context, textViewResourceId, arrayListItems);
    		// initial state as false
        }
        // class for caching the views in a row
        private class ViewHolder 
        {
            ImageView detail;
            TextView name;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.scene_item,null);
            	
        		viewHolder = new ViewHolder();

        		viewHolder.detail     = (ImageView) convertView.findViewById(R.id.item_scene_edit);
        		viewHolder.name       = (TextView)  convertView.findViewById(R.id.item_scene_name);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
            if(edit)
            	viewHolder.detail.setVisibility(View.VISIBLE);
            else
            	viewHolder.detail.setVisibility(View.INVISIBLE);
            
            String name=scene.get(position).get("label").toString();
            
            viewHolder.name.setText(name.equals("") ? "New Scene" : name);
            	
            return convertView;
    	}
    }
    */
}
