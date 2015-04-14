package com.avadesign.ha.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.schedule.ActivityScheduleView;
import com.avadesign.ha.trigger.ActivityTriggerView;

public class ActivitySceneView extends BaseActivity 
{	
	private LinearLayout admintoollayout;
	
	private Button trigger,scene,schedule,back,add,edit,del;
	
	private GridView gridview;
	
	private PictureListAdapter adapter;

	//private Handler mHandler = new Handler();
	
	private ArrayList<HashMap<String,String>> scene_list;
	
	private GetSceneTask mGetSceneTask;
	
	private boolean Edit,delete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scene_view);
		
		FindView();
		
		Setlistener();
	}
	
	private void FindView()
	{
		admintoollayout= (LinearLayout)findViewById(R.id.admintoolLayout);
		add= (Button)findViewById(R.id.tab_add);
		edit= (Button)findViewById(R.id.tab_edit);
		del= (Button)findViewById(R.id.tab_del);
		
		gridview = (GridView)this.findViewById(R.id.gridview);
		
		trigger		= (Button)findViewById(R.id.tab_trigger);
		scene  		= (Button)findViewById(R.id.tab_scene);
		schedule    = (Button)findViewById(R.id.tab_schedule);
		back   		= (Button)findViewById(R.id.tab_back);
	}
	
	private void Setlistener()
	{
		add.setOnClickListener(admin_button_down);
		edit.setOnClickListener(admin_button_down);
		del.setOnClickListener(admin_button_down);
		add.setTag(1);
		edit.setTag(2);
		del.setTag(3);
		
		trigger.setOnClickListener(tab_button_down);
		scene.setOnClickListener(tab_button_down);
		schedule.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		scene.setTag(1);
		trigger.setTag(2);
		schedule.setTag(3);
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
		
		scene.setSelected(true);
		
		admintoollayout.setVisibility(cp.getControllerAcc().equals("admin") ? View.VISIBLE : View.GONE);
		
		GetSceneCommand();
		
		super.onResume();
	}
	
	private GridView.OnItemClickListener grid_down = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			Log.v(TAG,"int="+arg2);
			
			HashMap<String,String> map=scene_list.get(arg2);
			
			if (!Edit && !delete)
			{
				//SetSceneCommand(map.get("id"));
				RunSceneCommand(map.get("id"));
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
					//
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
					scene.setSelected(true);
					
					Intent intent = new Intent();
					intent.setClass(ActivitySceneView.this, ActivitySceneView.class);
	    			startActivity(intent);
					
	    			finish();
					break;
				}
				case 2:
				{
					trigger.setSelected(true);
					
					Intent intent = new Intent();
					intent.setClass(ActivitySceneView.this, ActivityTriggerView.class);
	    			startActivity(intent);
					
	    			finish();
					break;
				}
				case 3:
				{
					schedule.setSelected(true);
					
            		Intent intent = new Intent();
            		intent.setClass(ActivitySceneView.this, ActivityScheduleView.class);
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
		/*
	private void SetSceneCommand(final String scene_id)
	{
		callProgress();
		
		new Thread()
		{
			public void run()
			{
				Map<String, String> map = new HashMap<String,String>();
				map.put("fun","execute");
				map.put("id",scene_id);
				if(!cp.isLocalUsed())
				{
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getControllerAcc());
					map.put("userpwd", cp.getControllerPwd());
					map.put("tunnelid", "0");
				}
				
				
				
				cancelProgress();
			}
		}.start();
	}
	*/
	private void GetSceneCommand()
	{
		if(mGetSceneTask!=null)
			return;
		
		mGetSceneTask = new GetSceneTask();
		mGetSceneTask.execute(new String[]{"scenepost.html","fun","load"});
	}
	
	private void RunSceneCommand(String scene_id)
	{
		if(mGetSceneTask!=null)
			return;
		
		mGetSceneTask = new GetSceneTask();
		mGetSceneTask.execute(new String[]{"scenepost.html","fun","execute","id",scene_id});
	}
	
	private void RemoveSceneCommand(String scene_id)
	{
		if(mGetSceneTask!=null)
			return;
		
		mGetSceneTask = new GetSceneTask();
		mGetSceneTask.execute(new String[]{"scenepost.html","fun","delete","id",scene_id});
	}
	
	private void AddSceneCommand()
	{
		View view= View.inflate(this,R.layout.dialog_set_auth,null);
		
		final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
		final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
		
		uname_edit.setHint(getText(R.string.scene_name));
		upwd_edit.setVisibility(View.GONE);
		
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(this);
		
		builder.setTitle(this.getText(R.string.new_scene_name));
		builder.setView(view);
		builder.setMessage(this.getText(R.string.input_scene_name));
		builder.setCancelable(false);
		builder.setPositiveButton(this.getText(R.string.alert_button_ok), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				if(mGetSceneTask!=null)
					return;
				
				mGetSceneTask = new GetSceneTask();
				mGetSceneTask.execute(new String[]{"scenepost.html","fun","create","label",uname_edit.getText().toString()});	
				
				Log.v(TAG,""+uname_edit.getText().toString());
			}
		});
		builder.setNegativeButton(this.getText(R.string.alert_button_cancel), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				// TODO Auto-generated method stub
			}
		});
		
		builder.create().show();
	}
		
	private class GetSceneTask extends AsyncTask<String, Void, Boolean> 
	{		
		ArrayList<HashMap<String,String>> list;
		Boolean new_scene=false;
		String new_name=null;
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
			
			fun=params[2];
			
			if (fun.equalsIgnoreCase("create"))
			{
				new_scene=true;
				new_name=params[4];
			}
			
			for(int i=2;i<params.length;i+=2)
			{
				map.put(params[i-1], params[i]);
			}
			
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getControllerAcc());
				map.put("userpwd", cp.getControllerPwd());
			}
			
			if (fun.equalsIgnoreCase("execute"))
			{
				SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
						map, 
						cp.getControllerAcc(), 
						cp.getControllerPwd(), 
						cp.isLocalUsed());
			}
			else
			{
				list = SendHttpCommand.getlist(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
						map, 
						cp.getControllerAcc(), 
						cp.getControllerPwd(), 
						cp.isLocalUsed(),new_scene ? "new_scene" : "scene");
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetSceneTask = null;
			cancelProgress();
			
			if (!fun.equalsIgnoreCase("execute"))
			{
				if (list!=null)
				{
					if (new_scene)
					{
						for (HashMap<String,String> map : list) 
						{
							Log.v(TAG,"id-"+map.get("sceneid"));
							
							Intent intent = new Intent();
							intent.setClass(ActivitySceneView.this, ActivitySceneEdit.class);
							Bundle bundle=new Bundle();
							bundle.putString("scene_id",map.get("sceneid"));
							bundle.putString("scene_name",new_name);
							intent.putExtras(bundle);
							startActivity(intent);
						}
					}
					else
					{	
						scene_list.clear();
						for (HashMap<String,String> map : list) 
						{
							scene_list.add(map);
							
							Log.v(TAG,"id-"+map.get("id")+" label="+map.get("label"));
						}
						adapter.notifyDataSetChanged();
					}
				}
				else
				{
					scene_list.clear();
					adapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	/*
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
	*/
	
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
