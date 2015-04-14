package com.avadesign.ha.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.ServicePolling;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;

public class ActivitySceneEdit extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private Button tab_add,tab_del;
	
	private TextView scene_name_text,seekbar_text;
	
	private EditText scene_name_edit;
	
	private ListView listview;
	
	private MyCustomAdapter adapter;
	
	private ArrayList<HashMap<String,String>> scene_content;
	
	private GetSceneContentTask mGetSceneContentTask;
	
	private ProgressDialog mDialog_SPINNER;
	
	private String scene_id;
	
	private ArrayList<HashMap<String,Object>> mNodes;
	
	private Boolean delete=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scene_edit);
		
		FindView();
		
		Setlistener();

		scene_name_text.setText(getString(R.string.scene_name));
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
	}
	
	private void FindView()
	{
		tab_add=(Button)findViewById(R.id.tab_add);
		tab_del=(Button)findViewById(R.id.tab_del);
		
		scene_name_text = (TextView)this.findViewById(R.id.textView1);
		scene_name_edit = (EditText)this.findViewById(R.id.editText1);
		
		seekbar_text    = (TextView)this.findViewById(R.id.seekbar_text);
		
		listview = (ListView)this.findViewById(R.id.activity_scene_edit_list);
	}
	
	private void Setlistener()
	{
		tab_add.setOnClickListener(tab_button_down);
		tab_del.setOnClickListener(tab_button_down);
		tab_add.setTag(1);
		tab_del.setTag(2);
		
		mNodes=new ArrayList<HashMap<String,Object>>();
		scene_content=new ArrayList<HashMap<String,String>>();
		adapter = new MyCustomAdapter(this, R.layout.item_scene_edit,scene_content);
		
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(listItem_down);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		
		CusPreference cp = new CusPreference(ActivitySceneEdit.this);
		cp.setStopPolling(false);
		
		mNodes.clear();
		
		Bundle bundle=this.getIntent().getExtras();
		scene_id=bundle.getString("scene_id");
		String scene_name=bundle.getString("scene_name");;
		
		scene_name_edit.setText(scene_name);
		scene_name_edit.clearFocus();
		LoadSceneContentCommand(scene_id);
		
		delete=false;
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
	}

	@Override
	protected void onPause() 
	{
		CusPreference cp = new CusPreference(ActivitySceneEdit.this);
		cp.setStopPolling(true);
		unregisterReceiver(mBroadcastReceiver);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if(intent.getAction().equals(ServicePolling.HTTP_401))
			{
				DialogSetAuth.show(ActivitySceneEdit.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivitySceneEdit.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivitySceneEdit.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
				if (mNodes.size()==0)
				{
					((SharedClassApp)(ActivitySceneEdit.this.getApplication())).refreshNodesList(mNodes);
					adapter.notifyDataSetChanged();
					CusPreference cp = new CusPreference(ActivitySceneEdit.this);
					cp.setStopPolling(true);
				}
			}
		}
	};
	
	@Override
	public void onBackPressed() 
	{
		SetSceneNameCommand("label" , scene_id , scene_name_edit.getText().toString());
		finish();
	}
	
	private Button.OnClickListener tab_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer)v.getTag())
			{
				case 1:
				{
					Intent intent = new Intent();
					intent.setClass(ActivitySceneEdit.this, ActivitySceneEditValue.class);
					Bundle bundle=new Bundle();
					bundle.putString("scene_id",scene_id);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case 2:
				{
					delete=!delete;
					tab_del.setSelected(delete);
					adapter.notifyDataSetChanged();
					break;
				}
				default:
					break;
			}
			
			PlayButtonSound.play(ActivitySceneEdit.this);
		}
	};
	
	private ListView.OnItemClickListener listItem_down = new ListView.OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) 
		{
			final HashMap<String,String> map=scene_content.get(position);
		
			if (!delete)
			{
				if (map.get("current").equalsIgnoreCase("false"))
					map.put("current","True");
				else if (map.get("current").equalsIgnoreCase("true"))
					map.put("current","False");
				else if (map.get("current").equalsIgnoreCase("unsecured"))
					map.put("current","Secured");
				else if (map.get("current").equalsIgnoreCase("secured"))
					map.put("current","Unsecured");
				else if (map.get("current").equalsIgnoreCase("Arm"))
					map.put("current","Bypass");
				else if (map.get("current").equalsIgnoreCase("Bypass"))
					map.put("current","Arm");
				else if (map.get("current").equalsIgnoreCase("0"))
					map.put("current","99");
				else if (Integer.valueOf(map.get("current"))>0)
					map.put("current","0");
				//scene_content.set(position,map1);

				adapter.notifyDataSetChanged();
				
				SetSceneValueCommand("update" , scene_id , map.get("node")+"-"+map.get("class")+"-"+map.get("genre")+"-"+map.get("type")+"-"+map.get("instance")+"-"+map.get("index"),map.get("current"));
			}
			else
			{
				AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivitySceneEdit.this);
				delAlertDialog.setTitle(R.string.scene_edit_delete_title);
				   delAlertDialog.setMessage(R.string.scene_edit_delete_message);
				   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
				   {
					   public void onClick(DialogInterface arg0, int arg1) 
					   {
							SetSceneValueCommand("remove" , scene_id , map.get("node")+"-"+map.get("class")+"-"+map.get("genre")+"-"+map.get("type")+"-"+map.get("instance")+"-"+map.get("index"),map.get("current"));
						
							scene_content.remove(position);
							adapter.notifyDataSetChanged();
							
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
			
			PlayButtonSound.play(ActivitySceneEdit.this);
		}
	};
	
	private void LoadSceneContentCommand(String scene_id)
	{
		if(mGetSceneContentTask!=null)
			return;
		
		mGetSceneContentTask = new GetSceneContentTask();
		mGetSceneContentTask.execute(new String[]{"scenepost.html","fun","values","id",scene_id});
	}
	
	private class GetSceneContentTask extends AsyncTask<String, Void, Boolean> 
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
			CusPreference cp = new CusPreference(ActivitySceneEdit.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(params[1], params[2]);
			//if (params[2].equals("values"))
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
					cp.isLocalUsed(),"scenevalue");
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetSceneContentTask = null;
			mDialog_SPINNER.dismiss();
			
			if (list!=null)
			{
				scene_content.clear();
				for (HashMap<String,String> map : list) 
				{
					scene_content.add(map);
					
					Log.v(TAG,"value="+map.get("node")+"-"+map.get("class")+"-"+map.get("instance")+"-"+map.get("index")+"-"+map.get("type")+"-"+map.get("genre")+"-"+map.get("label")+"-"+map.get("units")+"-"+map.get("current"));
				}
				adapter.notifyDataSetChanged();
			}
			else
			{
				scene_content.clear();
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
            ImageView item_node_image;
            
            TextView item_node_name,seek_level,item_node_sensor_mode;
            SeekBar seek;
            
            LinearLayout lay_seek;
            RelativeLayout relativeLayout;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_scene_edit,null);
            	
        		viewHolder = new ViewHolder();

        		viewHolder.item_node_image     	= (ImageView) convertView.findViewById(R.id.item_node_image);
        		viewHolder.item_node_name		  = (TextView) convertView.findViewById(R.id.item_node_name);
        		viewHolder.item_node_sensor_mode = (TextView) convertView.findViewById(R.id.item_node_sensor_mode);
        		
        		viewHolder.lay_seek		= (LinearLayout) convertView.findViewById(R.id.Layout_Seek);
        		viewHolder.seek			= (SeekBar) convertView.findViewById(R.id.seekbar);
        		viewHolder.seek_level = (TextView) convertView.findViewById(R.id.textview);
        		
        		viewHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.RelativeLayout1);
    
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
            
            HashMap<String,String> node=scene_content.get(position);
            
            String node_id=node.get("node");
            
            ZWaveNode znode=new ZWaveNode(Node_id_to_node(node_id));
            
            viewHolder.lay_seek.setVisibility(View.INVISIBLE);
            viewHolder.item_node_sensor_mode.setVisibility(View.INVISIBLE);
            
            viewHolder.item_node_name.setText(znode.name_fix);
            
            viewHolder.relativeLayout.setSelected(delete);
            
            String value=node.get("current");
            
            if (node.get("class").equalsIgnoreCase("alarm"))
            {
            	viewHolder.item_node_sensor_mode.setVisibility(View.VISIBLE);
            	viewHolder.item_node_sensor_mode.setText(value);
            	
            	for(ZWaveNodeValue zvalue : znode.value)
        		{
					if (zvalue.class_c.equalsIgnoreCase("alarm"))
        			{
						viewHolder.item_node_image.setImageResource(value.equalsIgnoreCase("Bypass") ? R.drawable.unsafe : R.drawable.safe);
    						
						break;
        			}
        		}
            }
            else
            {
            	viewHolder.item_node_sensor_mode.setVisibility(View.VISIBLE);
            	viewHolder.item_node_sensor_mode.setText(value);
            	
            	String status=null;
				int default_image=-1;
            	
            	if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0") || value.equalsIgnoreCase("unsecured"))
            	{
            		String sts_text=getString(R.string.device_status)+" : " + getString(R.string.device_off);
            		viewHolder.item_node_sensor_mode.setText(sts_text);
            		
            		status="off";
            		
            		if (znode.name.toLowerCase().indexOf("alarm")!=-1 || znode.product.toLowerCase().indexOf("alarm")!=-1)
            			default_image=R.drawable.alertoff;
            		else if (znode.name.toLowerCase().indexOf("lock")!=-1 || znode.product.toLowerCase().indexOf("lock")!=-1)
            			default_image=R.drawable.doorkeypad_off;
            		else
            			default_image=R.drawable.dimmer_light_off;
            	}
            	else
            	{
            		String sts_text=getString(R.string.device_status)+" : " + getString(R.string.device_on);
            		viewHolder.item_node_sensor_mode.setText(sts_text);
            		
            		status="on";
            		
            		if (znode.name.toLowerCase().indexOf("alarm")!=-1 || znode.product.toLowerCase().indexOf("alarm")!=-1)
            			default_image=R.drawable.alerton;
            		else if (znode.name.toLowerCase().indexOf("lock")!=-1 || znode.product.toLowerCase().indexOf("lock")!=-1)
            			default_image=R.drawable.doorkeypad_on;
            		else
            			default_image=R.drawable.dimmer_light_on;
            	}
            	
            	if (node.get("class").equalsIgnoreCase("switch multilevel"))
            	{
            		viewHolder.lay_seek.setVisibility(View.VISIBLE);
            		viewHolder.item_node_sensor_mode.setVisibility(View.INVISIBLE);
            		
            		viewHolder.seek_level.setText(String.valueOf(Integer.valueOf(value))+"%");
            			
            		viewHolder.seek.setTag(position);
            		viewHolder.seek.setOnSeekBarChangeListener(Seek_down);
            		viewHolder.seek.setProgress(Integer.valueOf(value));
            	}
            	
            	int have_multi=0;
                
                for (ZWaveNodeValue zvalue : znode.value)
                {
                	if (zvalue.class_c.equalsIgnoreCase("switch binary"))
                	{
                		if (zvalue.label.equalsIgnoreCase("switch"))
                			have_multi++;
                	}
                }
                
                if (have_multi>1)
                {
                	viewHolder.item_node_name.setText("["+node.get("instance")+"]"+znode.name_fix);
                }
                
                String path=label_to_path_label(znode.icon, status);
            	Bitmap bitmap = BitmapFactory.decodeFile(path);

    			if (bitmap!=null)
    				viewHolder.item_node_image.setImageBitmap(bitmap);
    			else
    				viewHolder.item_node_image.setImageResource(default_image);
    			
    			if (znode.gtype.toLowerCase().indexOf("switch")!=-1)
    			{
    				for (ZWaveNodeValue zvalue : znode.value)
                    {
                    	if (zvalue.class_c.equalsIgnoreCase("switch binary") && (have_multi>1 ? zvalue.instance.equalsIgnoreCase(node.get("instance")) : true))
                    	{
                    		if (zvalue.label.equalsIgnoreCase("switchbinary icon"))
                    		{
                    			path=label_to_path_label(zvalue.current,status);
        						
        						bitmap = BitmapFactory.decodeFile(path);
                    			
        						if (bitmap!=null)
        							viewHolder.item_node_image.setImageBitmap(bitmap);
        						else
        							viewHolder.item_node_image.setImageResource(default_image);
        						
        						break;
                    		}
                    	}
                    }
    			}
            }
            return convertView;
    	}
    }
	
	private String label_to_path_label (String label , String status)
	{
		CusPreference cp = new CusPreference(ActivitySceneEdit.this);
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
	
	SeekBar.OnSeekBarChangeListener Seek_down=new SeekBar.OnSeekBarChangeListener()
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
		{
			// TODO Auto-generated method stub
			
			HashMap<String,String> map=scene_content.get((Integer) seekBar.getTag());
			
			map.put("current",String.valueOf(progress));
			
			seekbar_text.setText(String.valueOf(progress)+"%");
			
			Log.v(TAG,"Change value");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) 
		{
			Log.v(TAG,"Touch down");
			
			seekbar_text.setVisibility(View.VISIBLE);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) 
		{			
			Log.v(TAG,"Touch up");
			
			HashMap<String,String> map=scene_content.get((Integer) seekBar.getTag());
			
			Log.v(TAG,map.get("current"));		
			
			SetSceneValueCommand("update",scene_id , map.get("node")+"-"+map.get("class")+"-"+map.get("genre")+"-"+map.get("type")+"-"+map.get("instance")+"-"+map.get("index"),map.get("current"));
			//sendCommand(scene_id , map.get("node")+"-"+map.get("class")+"-"+map.get("genre")+"-"+map.get("type")+"-"+map.get("instance")+"-"+map.get("index"),map.get("current"));
			seekbar_text.setVisibility(View.GONE);
			
			adapter.notifyDataSetChanged();
		}
	};
	
	private HashMap<String,Object> Node_id_to_node(final String node_id)
	{
		ArrayList<HashMap<String,Object>> nodes= new ArrayList<HashMap<String,Object>>();
		((SharedClassApp)(ActivitySceneEdit.this.getApplication())).refreshNodesList(nodes);
		
		for (HashMap<String,Object> node : nodes)
		{
			ZWaveNode znode=new ZWaveNode(node);
			
			if (znode.id.equals(node_id))
				return node;
		}
		
		return null;
	}
	
	private void SetSceneValueCommand(final String fun , final String id , final String vid , final String value)
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivitySceneEdit.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("fun",fun);
				map.put("id",id);
				map.put("vid",vid);
				map.put("value",value);
				
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
			}
		}.start();
	}
	
	private void SetSceneNameCommand(final String fun , final String id , final String lab)
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivitySceneEdit.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("fun",fun);
				map.put("id",id);
				map.put("label",lab);
				
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
			}
		}.start();
	}
	
	/*
	private void sendCommand(String fun , String id , String vid , String value)
	{
		if(mSendCommandTask!=null){
			return;
		}
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"scenepost.html","fun",fun,"id",id,"vid",vid,"value",value});
	}
	
	private class SendCommandTask extends AsyncTask<String, Void, Boolean> 
	{
		
		@Override
		protected void onPreExecute() {
			mDialog_SPINNER.setMessage(getString(R.string.dialog_message_wait));
			mDialog_SPINNER.show();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... args) {
			boolean success = false;
			CusPreference cp = new CusPreference(ActivitySceneEdit.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(args[1], args[2]);
			map.put(args[3], args[4]);
			map.put(args[5], args[6]);
			map.put(args[7], args[8]);
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", "admin");
				map.put("userpwd", "admin");
			}
			
			success = SendHttpCommand.send(String.format(cp.isLocalUsed()?getString(R.string.local_url_syntax):getString(R.string.server_url_syntax),cp.getControllerIP(),String.valueOf(cp.getControllerPort()))+args[0],
					map, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed());
			
			return success;
		}

		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mDialog_SPINNER.dismiss();
			
			if (success)
				Toast.makeText(ActivitySceneEdit.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(ActivitySceneEdit.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
			
			mSendCommandTask = null;
			
		}
	}
	*/
}
