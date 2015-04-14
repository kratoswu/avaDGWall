package com.avadesign.ha.node;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.SendHttpCommand;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;


public class ActivityNodeEdit extends BaseActivity 
{
	private ListView listview;
	
	ArrayList<HashMap<String,String>> location;
	
	private ArrayList<HashMap<String,Object>> list_array;
	
	private ArrayList<ZWaveNodeValue> config_array;
	
	private GridView gridview;
	
	private Button tab_del;
	
	private MyCustomAdapter adapter;
	
	private Boolean touch=false;
	
	private SendCommandTask mSendCommandTask;
	
	private Boolean action=false;
	
	private Integer flag;
	
	private PictureListAdapter padapter;
	
	private Integer icon_flag;
	
	private ArrayList<HashMap<String,String>> normal_array;
	
	private Integer set_flag;
	
	//private boolean NodeIcon_ClassIcon;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_node_edit);
		
		RegisterBroadcast();
		
		FindView();
		
		Setlistener();
		
		Bundle bundle=this.getIntent().getExtras();
		
		location=(ArrayList<HashMap<String, String>>) bundle.getSerializable("location");
		
		padapter.notifyDataSetChanged();
	}
	
	private void FindView()
	{
		tab_del=(Button) findViewById(R.id.tab_del);
		listview = (ListView)findViewById(R.id.listview);
		gridview = (GridView)findViewById(R.id.gridview);
	}
	
	private void Setlistener()
	{
		CusPreference cp = new CusPreference(ActivityNodeEdit.this);
		
		ArrayList<HashMap<String,String>> image_array=(ArrayList<HashMap<String,String>>)cp.getIcon_Image();
		normal_array= new ArrayList<HashMap<String,String>>();
		normal_array.clear();
		
		for(HashMap<String,String> map :  image_array)
		{
			if (map.get("group").equalsIgnoreCase("device"))
			{
				normal_array.add(map);
			}
		}
		
		list_array = new ArrayList<HashMap<String,Object>>();
        config_array = new ArrayList<ZWaveNodeValue>();
        
		adapter = new MyCustomAdapter(this, R.layout.item_edit,list_array);
		listview.setAdapter(adapter);
		listview.setOnScrollListener(scrollview_roll);
		
		tab_del.setOnClickListener(button_down);
		
		padapter = new PictureListAdapter(this, gridview, null, null);
		gridview.setOnItemClickListener(grid_down);
		gridview.setAdapter(padapter);
	}
	
	private GridView.OnItemClickListener grid_down = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			Log.v(TAG,"int="+arg2);
			
			listview.setVisibility(View.VISIBLE);
			gridview.setVisibility(View.INVISIBLE);
			
			HashMap<String,String> loc_map=normal_array.get(arg2);
        	
			String normal_path=loc_map.get("Normal");
			String [] ary=normal_path.split("/");
			
			if (ary.length>=3)
        	{
				String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.avadesign.ha"+"/"+ary[0]+"/"+ary[1]+"/normal/"+ary[2];
				
				HashMap<String,Object> map=list_array.get(icon_flag);
				
				map.put("value", path);
				
				ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
				
				ArrayList<ZWaveNodeValue> user_array=new ArrayList<ZWaveNodeValue>();
				
				for(ZWaveNodeValue zvalue : znode.value)
		        {
					if (zvalue.genre.equalsIgnoreCase("user"))
		        	{
		        		if (zvalue.label.toLowerCase().indexOf("icon")!=-1 && !zvalue.class_c.equalsIgnoreCase("battery"))
		        		{
		        			if (zvalue.class_c.equalsIgnoreCase("switch binary") || zvalue.class_c.equalsIgnoreCase("switch multilevel") || zvalue.class_c.equalsIgnoreCase("sensor binary") || zvalue.class_c.equalsIgnoreCase("door lock"))
		        				user_array.add(zvalue);
		        		}
		        	}
		        }
		        
				if (icon_flag-set_flag==0)
				{
					//Log.v(TAG,"loc_map ="+loc_map);
		        	SetNodeIconCommand(znode.id, loc_map.get("type"));
				}
				else
				{
					if (user_array.size()>0)
		        	{
		        		ZWaveNodeValue zvalue=user_array.get(icon_flag-set_flag-1);
		        		final String pid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
						SetValueCommand(pid,loc_map.get("type"));
		        	}
		        }
		        adapter.notifyDataSetChanged();
        	}
			
			
		}
	};
	
	private Button.OnClickListener button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			Builder builder = new AlertDialog.Builder(ActivityNodeEdit.this);
			
			builder.setTitle(getString(R.string.edit_node_remove_title));
			
			builder.setMessage(getString(R.string.edit_node_remove_message));
			
			builder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
					sendDetectCommand(znode.id);
				}
			});
			
			builder.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() 
			{
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					//Toast.makeText(ActivityNodeEdit.this, "您按下Cancel按鈕", Toast.LENGTH_SHORT).show();
				}
			});
			builder.create();
			builder.show();
			
			
		}
	};
	
	private OnScrollListener  scrollview_roll = new OnScrollListener()
	{

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			touch=false;
		}
		
	};
	
	public void initData()
	{  
		list_array.clear();
        ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
        
        Log.v(TAG,"id="+znode.id);
        
//Section 1
        
        HashMap<String, Object> map=new HashMap<String,Object>();
        map.put("lab", "title");
        map.put("value", getString(R.string.edit_node_group_status));
        map.put("section", "1");
        list_array.add(map);
        
        map=new HashMap<String,Object>();
        map.put("lab", getString(R.string.edit_node_group_status_type));
        map.put("value", znode.gtype);
        map.put("section", "1");
        list_array.add(map);
        
        map=new HashMap<String,Object>();
        map.put("lab", getString(R.string.edit_node_last_time));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        map.put("value", sdf.format(znode.time*1000));
        map.put("section", "1");
        list_array.add(map);
        
        map=new HashMap<String,Object>();
        map.put("lab", getString(R.string.edit_node_group_status_status));
        map.put("value", znode.status);
        map.put("section", "1");
        list_array.add(map);
        
//Section 2        
        map=new HashMap<String,Object>();
        map.put("lab", "title");
        map.put("value", getString(R.string.edit_node_group_info));
        map.put("section", "2");
        list_array.add(map);
        
        map=new HashMap<String,Object>();
        map.put("lab", getString(R.string.edit_node_group_info_name));
        map.put("value", znode.name);
        map.put("section", "2");
        list_array.add(map);
        
        map=new HashMap<String,Object>();
        map.put("lab", getString(R.string.edit_node_group_info_location));
        map.put("value", znode.location);
        map.put("section", "2");
        list_array.add(map);
         
//Section 3       
        set_flag=list_array.size()+1;
        
        ArrayList<HashMap<String,Object>> list_ary=new ArrayList<HashMap<String,Object>>();
        
        map=new HashMap<String,Object>();
        map.put("lab", "title");
        map.put("value", getString(R.string.edit_node_group_icon));
        map.put("section", "3");
        list_ary.add(map);
        
        //main icon
        String path=label_to_path_label(znode.icon,"normal");
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		
		map=new HashMap<String,Object>();
		if (bitmap!=null)
			map.put("value", path);
		else
			map.put("value", null);
		
		map.put("lab", "bitmap");
		map.put("section", "3");
		list_ary.add(map);
        
        
		for(ZWaveNodeValue zvalue : znode.value)
        {
    		if (zvalue.class_c.equalsIgnoreCase("switch binary") || zvalue.class_c.equalsIgnoreCase("switch multilevel") || zvalue.class_c.equalsIgnoreCase("sensor binary") || zvalue.class_c.equalsIgnoreCase("door lock"))
    		{
    			if (zvalue.units.toLowerCase().equalsIgnoreCase("icon"))
    			{
    				path=label_to_path_label(zvalue.current,"normal");
        			map=new HashMap<String,Object>();
            		map.put("lab", "bitmap");
            		map.put("value", path);
            		map.put("section", "3");
            		list_ary.add(map);
    			}
    		}
        }
        

        list_array.addAll(list_ary);
 
//Section 4         
        
        for(ZWaveNodeValue zvalue : znode.value)
        {
        	if (zvalue.class_c.equalsIgnoreCase("ALARM"))
        	{
        		if (zvalue.label.equalsIgnoreCase("Mode"))
        		{
        			map=new HashMap<String,Object>();
        	        map.put("lab", zvalue.label);
        	        map.put("value", zvalue.current);
        	        map.put("section", "4");
        	        list_array.add(map);
        		}
        	}
        }
        
        
//Section 5        
        config_array.clear();
        for(ZWaveNodeValue zvalue : znode.value)
        {
        	if (zvalue.genre.equalsIgnoreCase("config"))
        	{
        		config_array.add(zvalue);
        	}
        }
        
        if (config_array.size()!=0)
        {
        	 map=new HashMap<String,Object>();
             map.put("lab", "title");
             map.put("value", getString(R.string.edit_node_group_configer));
             map.put("section", "4");
             list_array.add(map);
             
             for(ZWaveNodeValue zvalue : config_array)
             {
            	 if (!zvalue.class_c.toLowerCase().equalsIgnoreCase("door lock"))
                 {
            		 map=new HashMap<String,Object>();
                     map.put("lab", zvalue.label);
                     map.put("section", "5");
                     if (zvalue.units.equals(""))
                     	map.put("value", zvalue.current);
                     else
                     	map.put("value", zvalue.current+" "+zvalue.units);
                     list_array.add(map);
                 }
             }
             //hasConfig=true;
        }
        
        adapter.notifyDataSetChanged();
    } 
	
	private String label_to_path_label (String label , String status)
	{
		CusPreference cp = new CusPreference(ActivityNodeEdit.this);
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
	    			String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.avadesign.ha"+"/"+ary[0]+"/"+ary[1]+"/"+status;
	            	
	    			return path+"/"+ary[2];
	    		}
	    	}
	    }
		
		return "";
	}
	@Override
	protected void onResume() 
	{
		initData();
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		UnRegisterBroadcast();
		
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() 
	{
		sendSaveCommand();
		
		return;
	}
	
	@Override
	protected void callBroadcastdone()
	{
		attemptGetData();
	}
	
	private void attemptGetData()
	{
		if (action)
		{
			if (!((SharedClassApp)(ActivityNodeEdit.this.getApplication())).isActive())
			{
				String State=((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getControllerState();
				
				String[] array = State.split(":");
				
				if (array.length >=2)
				{
					Log.v(TAG,array[0]);
					Log.v(TAG,array[1]);
					
					String title =array[0];
					String msg   =array[1];
					
					if (flag==1)
					{
						Builder builder = new AlertDialog.Builder(ActivityNodeEdit.this);
						
						builder.setTitle(title);
						
						builder.setMessage(msg);
						
						builder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
						{
							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								//Toast.makeText(ActivityNodeEdit.this, "您按下OK按鈕", Toast.LENGTH_SHORT).show();
								action=true;
							}
						});

						builder.create();
						builder.show();
					}
					else
					{
						if (msg.equalsIgnoreCase(" (10)The node is OK."))
						{
							Builder builder = new AlertDialog.Builder(ActivityNodeEdit.this);
							
							builder.setTitle(title);
							
							builder.setMessage(msg);
							
							builder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
							{
								@Override
								public void onClick(DialogInterface dialog, int which) 
								{
									//Toast.makeText(ActivityNodeEdit.this, "您按下OK按鈕", Toast.LENGTH_SHORT).show();
								}
							});

							builder.create();
							builder.show();
						}
						else
						{
							Builder builder = new AlertDialog.Builder(ActivityNodeEdit.this);
							
							builder.setTitle(getString(R.string.edit_node_fail_title));
							
							builder.setMessage(getString(R.string.edit_node_fail_message));
							
							builder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
							{
								@Override
								public void onClick(DialogInterface dialog, int which) 
								{
									ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
									sendRemoveCommand(znode.id);
									//Toast.makeText(ActivityNodeEdit.this, "您按下OK按鈕", Toast.LENGTH_SHORT).show();
								}
							});
							
							builder.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() 
							{
								@Override
								public void onClick(DialogInterface dialog, int which) 
								{
									//Toast.makeText(ActivityNodeEdit.this, "您按下Cancel按鈕", Toast.LENGTH_SHORT).show();
								}
							});
							builder.create();
							builder.show();
						}
					}
				}
			}
			action=false;
		}
	}
	
	private void sendRemoveCommand(String node_id)
	{
		flag=1;
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"admpost.html","fun","remfn","node","node"+node_id});
	}
	
	private void sendDetectCommand(String node_id)
	{
		flag=0;
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"admpost.html","fun","hnf","node","node"+node_id});
	}
	
	private void SetNodeIconCommand(final String id, final String label)
	{
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"node_icon.cgi",id,label});
	}
	
	private void SetValueCommand(final String device, final String action)
	{
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"valuepost.html",device,action});
	}
	
	private void SetNameLocCommand(final String action, final String node_id, final String value)
	{
		if(mSendCommandTask!=null)
			return;
		
		mSendCommandTask = new SendCommandTask();
		mSendCommandTask.execute(new String[]{"nodepost.html","fun",action,"node","node"+node_id,"value",value});
	}
		
	private void sendSaveCommand()
	{
		if (action)
		{
			finish();
		}
		else
		{
			if(mSendCommandTask!=null)
				return;
			
			mSendCommandTask = new SendCommandTask();
			mSendCommandTask.execute(new String[]{"savepost.html","fun","save"});
		}
	}
	
	private class SendCommandTask extends AsyncTask<String, Void, Boolean> 
	{
		String fun="";
		
		@Override
		protected void onPreExecute() 
		{
			callProgress();
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... args) 
		{
			boolean success = false;
			
			CusPreference cp = new CusPreference(ActivityNodeEdit.this);
			
			Map<String, String> map = new HashMap<String,String>();
			
			fun=args[2];
			
			for(int i=2;i<args.length;i+=2)
			{
				map.put(args[i-1], args[i]);
			}
			
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getControllerAcc());
				map.put("userpwd", cp.getControllerPwd());
				map.put("tunnelid", "0");
			}
			
			success = SendHttpCommand.send(
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+args[0],
					map, 
					cp.getControllerAcc(), 
					cp.getControllerPwd(), 
					cp.isLocalUsed());
			Log.v(TAG,"success="+success);
			
			return success;
		}

		@Override
		protected void onPostExecute(final Boolean success) 
		{
			if (success) 
			{
				Toast.makeText(ActivityNodeEdit.this, getString(R.string.dialog_message_succeed), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(ActivityNodeEdit.this, getString(R.string.dialog_message_failed), Toast.LENGTH_SHORT).show();
			}
			
			mSendCommandTask = null;
			
			cancelProgress();
			
			Log.v(TAG,"fun="+fun);
			if (fun.equalsIgnoreCase("save"))
				finish();
		}

		@Override
		protected void onCancelled() 
		{
			cancelProgress();
			super.onCancelled();
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
        	return normal_array.size();
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
              
            viewHolder.relativeLayout.setSelected(false);
        	viewHolder.image_delete.setVisibility(View.INVISIBLE);
        	viewHolder.room.setVisibility(View.GONE);
            
        	HashMap<String,String> device_map=normal_array.get(position);
        	
        	String normal_path=device_map.get("Normal");
        	String [] ary=normal_path.split("/");
        	
        	if (ary.length>=3)
        	{
        		String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.avadesign.ha"+"/"+ary[0]+"/"+ary[1]+"/normal";
        		
        		Bitmap bitmap = BitmapFactory.decodeFile(path+"/"+ary[2]);
        		viewHolder.image.setImageBitmap(bitmap);
        	}
        	
            return convertView;   
        }
    }
	
	public class MyCustomAdapter extends ArrayAdapter<HashMap<String,Object>>
    { 
		ViewHolder viewHolder;
		private LayoutInflater inflater; 

    	public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String,Object>> arrayListItems) 
    	{
    		super(context, textViewResourceId, arrayListItems);
    		inflater = LayoutInflater.from(context);
        }
    	
    	private class ViewHolder 
        {
    		@SuppressWarnings("unused")
			TextView item_edit_title_textview,item_edit_layout_text_textview1,item_edit_layout_text_textview2,item_edit_layout_edit_textview,item_edit_layout_spinner_textview,item_edit_layout_config_textview;
    		
    		Spinner item_edit_layout_spinner_spinner,item_edit_layout_config_spinner;
    		
    		TextView item_edit_layout_edit_editview,icon_title;
    		
    		Button image_select,edit_name;
    		ImageView icon;
    		
    		@SuppressWarnings("unused")
			LinearLayout item_edit_layout_text,item_edit_layout_edit,item_edit_layout_spinner,item_edit_layout_config,item_edit_layout_edit_layout,item_edit_layout_image;
        }
    	
    	@Override  
        public View getView(int position, View convertView, ViewGroup parent) 
        {  
        	if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_edit,null);
            	
        		viewHolder = new ViewHolder();
        		
        		viewHolder.item_edit_layout_text	=(LinearLayout) convertView.findViewById(R.id.item_edit_layout_text);
        		viewHolder.item_edit_layout_edit	=(LinearLayout) convertView.findViewById(R.id.item_edit_layout_edit);
        		viewHolder.item_edit_layout_spinner	=(LinearLayout) convertView.findViewById(R.id.item_edit_layout_spinner);
        		viewHolder.item_edit_layout_config	=(LinearLayout) convertView.findViewById(R.id.item_edit_layout_config);
        		viewHolder.item_edit_layout_edit_layout		=(LinearLayout) convertView.findViewById(R.id.item_edit_layout_edit_layout);
        		
        		viewHolder.item_edit_layout_image	=(LinearLayout) convertView.findViewById(R.id.item_edit_layout_image);
        		viewHolder.image_select				=(Button)convertView.findViewById(R.id.button1);
        		viewHolder.icon						=(ImageView)convertView.findViewById(R.id.imageview);
        		
        		viewHolder.item_edit_title_textview	=(TextView) convertView.findViewById(R.id.item_edit_title_textview);
        		viewHolder.item_edit_layout_text_textview1	=(TextView) convertView.findViewById(R.id.item_edit_layout_text_textview1);
        		viewHolder.item_edit_layout_text_textview2	=(TextView) convertView.findViewById(R.id.item_edit_layout_text_textview2);
        		viewHolder.item_edit_layout_edit_textview	=(TextView) convertView.findViewById(R.id.item_edit_layout_edit_textview);
        		viewHolder.item_edit_layout_spinner_textview	=(TextView) convertView.findViewById(R.id.item_edit_layout_spinner_textview);
        		viewHolder.item_edit_layout_config_textview	=(TextView) convertView.findViewById(R.id.item_edit_layout_config_textview);
        		
        		viewHolder.item_edit_layout_spinner_spinner	=(Spinner) convertView.findViewById(R.id.item_edit_layout_spinner_spinner);
        		viewHolder.item_edit_layout_config_spinner	=(Spinner) convertView.findViewById(R.id.item_edit_layout_config_spinner);
        		
        		viewHolder.item_edit_layout_edit_editview   =(TextView) convertView.findViewById(R.id.item_edit_layout_edit_edittext);
        		viewHolder.edit_name				=(Button)convertView.findViewById(R.id.Button01);
        		
        		viewHolder.icon_title			=(TextView) convertView.findViewById(R.id.TextView01);
        		
        		convertView.setTag(viewHolder);
            }
        	else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
        	final HashMap<String,Object> map=list_array.get(position);
        	
        	if (((String)map.get("lab")).equalsIgnoreCase("title"))
        	{
        		viewHolder.item_edit_title_textview.setVisibility(View.VISIBLE);
        		viewHolder.item_edit_layout_text.setVisibility(View.GONE);
        		viewHolder.item_edit_layout_edit.setVisibility(View.GONE);
        		viewHolder.item_edit_layout_spinner.setVisibility(View.GONE);
        		viewHolder.item_edit_layout_config.setVisibility(View.GONE);
        		viewHolder.item_edit_layout_image.setVisibility(View.GONE);
        		
        		viewHolder.item_edit_title_textview.setText((String)map.get("value"));
        	}
        	else
        	{
        		if (map.get("section").equals("1"))
        		{
        			viewHolder.item_edit_title_textview.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_text.setVisibility(View.VISIBLE);
            		viewHolder.item_edit_layout_edit.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_spinner.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_config.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_image.setVisibility(View.GONE);
            		
            		viewHolder.item_edit_layout_text_textview1.setText((String)map.get("lab"));
            		viewHolder.item_edit_layout_text_textview2.setText((String)map.get("value"));
        		}
        		else if (map.get("section").equals("2"))
        		{
        			viewHolder.item_edit_title_textview.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_text.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_config.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_image.setVisibility(View.GONE);
            		
            		if (map.get("lab").equals(getString(R.string.edit_node_group_info_name)))
            		{
                		viewHolder.item_edit_layout_edit.setVisibility(View.VISIBLE);
                		viewHolder.item_edit_layout_spinner.setVisibility(View.GONE);
                		
                		viewHolder.item_edit_layout_edit_textview.setText((String)map.get("lab"));
                		
                		viewHolder.item_edit_layout_edit_editview.setText((String)map.get("value"));
                		
                		viewHolder.edit_name.setOnClickListener(new Button.OnClickListener()
                		{
							@Override
							public void onClick(View v) 
							{
								AlertDialog.Builder editDialog = new AlertDialog.Builder(ActivityNodeEdit.this);
								
								editDialog.setTitle(R.string.edit_node_name);
								//editDialog.setMessage(R.string.room_add_message);
								
								final EditText editText = new EditText(ActivityNodeEdit.this);
								editText.setText((String)map.get("value"));
								
								editDialog.setView(editText);
								    
								editDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface arg0, int arg1) 
									{
										map.put("value", editText.getText().toString());
										//viewHolder.item_edit_layout_edit_editview.setText((String)map.get("value"));
										
										ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
										
										if (editText.getText().toString().equals(""))
											SetNameLocCommand("nam",znode.id," ");
										else
											SetNameLocCommand("nam",znode.id,editText.getText().toString());
										
										adapter.notifyDataSetChanged();
									}
								});
								editDialog.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() 
								{
									public void onClick(DialogInterface arg0, int arg1) 
									{
								    
									}
								});
								editDialog.show();
								
								
							}
                			
                		});
            		}
            		else
            		{
            			viewHolder.item_edit_layout_edit.setVisibility(View.GONE);
            			viewHolder.item_edit_layout_spinner.setVisibility(View.VISIBLE);
            			
            			viewHolder.item_edit_layout_edit_textview.setText((String)map.get("lab"));
            			
            			final ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
            			
            			final ArrayList<String> loc_array=new ArrayList<String >();
            			
            			Boolean have=false;
            			for (int i=0;i<location.size();i++)
            			{
            				loc_array.add(location.get(i).get("location"));
            				if (znode.location.equals(location.get(i).get("location")))
            				{
            					have=true;
            				}
            			}
            			
            			if (!have)
            				loc_array.add(znode.location);
            			
            			viewHolder.item_edit_layout_spinner_spinner.setAdapter(createArrayAdapter(loc_array));
            			
            			for (int i=0;i<loc_array.size();i++)
            			{
            				if (znode.location.equals(loc_array.get(i)))
            					viewHolder.item_edit_layout_spinner_spinner.setSelection(i);
            			}

            			viewHolder.item_edit_layout_spinner_spinner.setOnTouchListener(
            					new Spinner.OnTouchListener()
            					{
									@Override
									public boolean onTouch(View v,MotionEvent event) 
									{
										if (event.getAction() == MotionEvent.ACTION_UP)
										{
											touch=true;
										}
										else
										{
											touch=false;
										}
										return false;
									}
            						
            					});
            			
            			viewHolder.item_edit_layout_spinner_spinner.setOnItemSelectedListener(
            					new Spinner.OnItemSelectedListener()
            					{
            						public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            						{
            							ArrayList<String> ary=new ArrayList<String>();
            							
            							for(String str : loc_array)
            							{
            								ary.add(str);
            							}
            							
            							if (touch)
            							{
            								if (ary.get(position).equals(""))
                								SetNameLocCommand("loc",znode.id," ");
                							else
                								SetNameLocCommand("loc",znode.id,ary.get(position));
            								
            								touch=false;	
            							}
            						}
            						public void onNothingSelected(AdapterView<?> arg0) 
            						{
            							Log.v(TAG,"Nothing");
	            					}
            					});
            		}
        		}
        		else if (map.get("section").equals("3"))
        		{
        			viewHolder.item_edit_title_textview.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_text.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_edit.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_spinner.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_config.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_image.setVisibility(View.VISIBLE);
            		
            		if (position==0)
            			viewHolder.icon_title.setText(getString(R.string.EDIT_ICON_MAIN_TITLE));            			
            		else
            			viewHolder.icon_title.setText(getString(R.string.EDIT_ICON_FUN_TITLE));
            		
            		String path=(String)map.get("value");
            		
            		Bitmap bitmap = BitmapFactory.decodeFile(path);
        			
        			if (bitmap!=null)
        				viewHolder.icon.setImageBitmap(bitmap);
        			
        			viewHolder.image_select.setTag(position);
        			
        			viewHolder.image_select.setOnClickListener(new Button.OnClickListener()
            		{
						@Override
						public void onClick(View v) 
						{
							gridview.setVisibility(View.VISIBLE);
							listview.setVisibility(View.INVISIBLE);
							icon_flag=(Integer)v.getTag();
							padapter.notifyDataSetChanged();
							
							
						}
					});
        		}
        		else if (map.get("section").equals("4"))
        		{
        			viewHolder.item_edit_title_textview.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_text.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_edit.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_spinner.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_config.setVisibility(View.VISIBLE);
            		viewHolder.item_edit_layout_image.setVisibility(View.GONE);
            		
            		viewHolder.item_edit_layout_config_textview.setText((String)map.get("lab"));
            		
            		
            		final ArrayList<String> arm_array=new ArrayList<String >();
            		arm_array.add("Arm");
        			arm_array.add("Bypass");
            		
            		viewHolder.item_edit_layout_config_spinner.setAdapter(createArrayAdapter(arm_array));
            		
            		for (int i=0;i<arm_array.size();i++)
        			{
            			Log.v(TAG,"arm_array.get(i)="+arm_array.get(i));
            			Log.v(TAG,"map.get(value)="+map.get("value"));
            			
        				if (map.get("value").toString().equals(arm_array.get(i)))
        				{
        					viewHolder.item_edit_layout_config_spinner.setSelection(i);
        					Log.v(TAG,"==");
        				}
        				else
        				{
        					Log.v(TAG,"!=");
        				}
        			}
            		
            		final ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
            		
            		int alli=-1;
            		for(int iii=0;iii<znode.value.size();iii++)
                    {
            			ZWaveNodeValue zvalue1=znode.value.get(iii);
            			
                    	if (zvalue1.class_c.equalsIgnoreCase("ALARM"))
                    	{
                    		if (zvalue1.label.equalsIgnoreCase("Mode"))
                    		{
                    			alli=iii;
                    		}
                    	}
                    }
            		
            		final ZWaveNodeValue zvalue = znode.value.get(alli);
            		
            		final String pid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
            		
            		viewHolder.item_edit_layout_config_spinner.setOnTouchListener(
        					new Spinner.OnTouchListener()
        					{
								@Override
								public boolean onTouch(View v,MotionEvent event) 
								{
									if (event.getAction() == MotionEvent.ACTION_UP)
									{
										touch=true;
									}
									else
									{
										touch=false;
									}
									return false;
								}
        						
        					});
        			
        			viewHolder.item_edit_layout_config_spinner.setOnItemSelectedListener(
                					new Spinner.OnItemSelectedListener()
                					{
                						public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
                						{
                							ArrayList<String> ary=new ArrayList<String>();
                							
                							for(String str : arm_array)
                							{
                								ary.add(str);
                							}
                							
                							if (touch)
                							{
                								SetValueCommand(pid, ary.get(position));
                								touch=false;
                								map.put("value", ary.get(position));
                							}
                							
                						}
                						public void onNothingSelected(AdapterView<?> arg0) 
                						{
                							Log.v(TAG,"Nothing");
                    					}
                					});
        		}
        		else if (map.get("section").equals("5"))
        		{
        			viewHolder.item_edit_title_textview.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_text.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_edit.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_spinner.setVisibility(View.GONE);
            		viewHolder.item_edit_layout_config.setVisibility(View.VISIBLE);
            		viewHolder.item_edit_layout_image.setVisibility(View.GONE);
            		
            		viewHolder.item_edit_layout_config_textview.setText((String)map.get("lab"));
            		
            		final ZWaveNode znode = ((SharedClassApp)(ActivityNodeEdit.this.getApplication())).getZWaveNode();
        			
            		final ArrayList<String> con_array=new ArrayList<String >();
        			
        			String[] string_item = null;
        			
        			for (final ZWaveNodeValue zvalue : config_array)
        			{
        				if (map.get("lab").equals(zvalue.label))
        				{
        					if (zvalue.type.equalsIgnoreCase("list"))
        					{
        						string_item=zvalue.item;
        						
        						for(String str : string_item)
        						{
        							if (zvalue.units.equals(""))
        								con_array.add(str);
        							else
        								con_array.add(str+zvalue.units);
        						}
        					}
        					else if (zvalue.type.equalsIgnoreCase("button") || zvalue.type.equalsIgnoreCase("bool"))
            				{
        						con_array.add("True");
        						con_array.add("False");
            				}
        					else if (zvalue.type.equalsIgnoreCase("byte") || zvalue.type.equalsIgnoreCase("short") || zvalue.type.equalsIgnoreCase("int"))
            				{
        						for(int i=0;i<601;i++)
        		                {
        							con_array.add(String.valueOf(i)+" "+zvalue.units);
        		                }
            				}
        					
        					final String pid = znode.id+"-"+zvalue.class_c+"-"+zvalue.genre+"-"+zvalue.type+"-"+zvalue.instance+"-"+zvalue.index;
        					
        					viewHolder.item_edit_layout_config_spinner.setAdapter(createArrayAdapter(con_array));
                			for (int i=0;i<con_array.size();i++)
                			{
                				if (map.get("value").equals(con_array.get(i)))
                					viewHolder.item_edit_layout_config_spinner.setSelection(i);
                			}
                			
                			viewHolder.item_edit_layout_config_spinner.setOnTouchListener(
                					new Spinner.OnTouchListener()
                					{
        								@Override
        								public boolean onTouch(View v,MotionEvent event) 
        								{
        									if (event.getAction() == MotionEvent.ACTION_UP)
        									{
        										touch=true;
        									}
        									else
        									{
        										touch=false;
        									}
        									return false;
        								}
                						
                					});
                			
                			viewHolder.item_edit_layout_config_spinner.setOnItemSelectedListener(
                        					new Spinner.OnItemSelectedListener()
                        					{
                        						public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
                        						{
                        							ArrayList<String> ary=new ArrayList<String>();
                        							
                        							for(String str : con_array)
                        							{
                        								ary.add(str);
                        							}
                        							
                        							if (touch)
                        							{
                        								if (zvalue.type.equalsIgnoreCase("short") || zvalue.type.equalsIgnoreCase("byte") || zvalue.type.equalsIgnoreCase("int"))
                        								{
                        									SetValueCommand(pid, String.valueOf(position));
                        								}
                        								else
                        									SetValueCommand(pid, ary.get(position));
                        								touch=false;
                        							}
                        							
                        						}
                        						public void onNothingSelected(AdapterView<?> arg0) 
                        						{
                        							Log.v(TAG,"Nothing");
                            					}
                        					});
                			
                			break;
        				}
        			}
        		}
        	}
        	
			return convertView;
        }
    }
	
	private ArrayAdapter<String> createArrayAdapter(ArrayList<String> array) 
	{
		ArrayList<String> ary=new ArrayList<String>();
		
		for(String str : array)
		{
			ary.add(str);
		}
		
        return new ArrayAdapter<String>(this,R.layout.custom_spinner, ary);
    }

}
