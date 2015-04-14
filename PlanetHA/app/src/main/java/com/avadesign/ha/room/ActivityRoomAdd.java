package com.avadesign.ha.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityRoomAdd extends BaseActivity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private ImageView icon;
	private EditText edit_text;
	private Button button_save,button_select;
	private GridView gridview;
	private PictureListAdapter adapter;
	private ArrayList<HashMap<String,String>> loc_icon_array;
	private LinearLayout layout;
	private String icon_lab=null;
	private GetListTask mGetListTask;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_add);
		
		Bundle bundle=this.getIntent().getExtras();
		loc_icon_array=(ArrayList<HashMap<String,String>>) bundle.getSerializable("image_array");
		
		FindView();
		
		Setlistener();
		
		adapter.notifyDataSetChanged();
	}
	
	private void FindView()
	{
		button_save= (Button)findViewById(R.id.tab_save);
		button_select= (Button)findViewById(R.id.button1);
		
		gridview = (GridView)findViewById(R.id.gridview);
		layout=(LinearLayout)findViewById(R.id.Layout1);
		icon= (ImageView)findViewById(R.id.imageView1);
		edit_text=(EditText)findViewById(R.id.editText1);
	}
	
	private void Setlistener()
	{
		button_save.setOnClickListener(tab_button_down);
		
		button_select.setOnClickListener(button_down);
				
		adapter = new PictureListAdapter(this, gridview, null, null);
		gridview.setOnItemClickListener(grid_down);
		gridview.setAdapter(adapter);
	}
	
	private GridView.OnItemClickListener grid_down = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			Log.v(TAG,"int="+arg2);
			
			layout.setVisibility(View.VISIBLE);
			gridview.setVisibility(View.INVISIBLE);
			
			HashMap<String,String> loc_map=loc_icon_array.get(arg2);
        	
        	String path = Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+"image"+"/"+"location";
        	String file_name=loc_map.get("Normal");
        	
        	String [] name=file_name.split("/");
        	
        	if (name.length>=3)
        	{
        		Bitmap bitmap = BitmapFactory.decodeFile(path+"/"+name[2]);
        		icon.setImageBitmap(bitmap);
        		
        		icon_lab=loc_map.get("type");
        	}
		}
	};
	
	private Button.OnClickListener tab_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			AddLocCommand(edit_text.getText().toString());
		}
	};
	
	private Button.OnClickListener button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			gridview.setVisibility(View.VISIBLE);
			layout.setVisibility(View.INVISIBLE);
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
            TextView room;
            RelativeLayout relativeLayout;
            ImageView image,image_delete;
        }
        
        @Override
        public int getCount() 
        {
        	return loc_icon_array.size();
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
            
        	HashMap<String,String> loc_map=loc_icon_array.get(position);
        	
        	String path = Environment.getExternalStorageDirectory().toString()+"/"+getString(R.string.app_name)+"/"+"image"+"/"+"location";
        	String file_name=loc_map.get("Normal");
        	
        	String [] name=file_name.split("/");
        	
        	if (name.length>=3)
        	{
        		Bitmap bitmap = BitmapFactory.decodeFile(path+"/"+name[2]);
        		viewHolder.image.setImageBitmap(bitmap);
        	}
        	
            return convertView;   
        }
    }
	
	private void AddLocCommand(String name)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"location_list.cgi","action","add","name",name,"image",icon_lab});
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
			CusPreference cp = new CusPreference(ActivityRoomAdd.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(params[1], params[2]);
			map.put(params[3], params[4]);
			map.put(params[5], params[6]);
			
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
			cancelProgress();
			
			mGetListTask = null;
			
			if (list!=null)
			{
				finish();
			}
			else
			{
				call404();
			}
		}
	}
}
