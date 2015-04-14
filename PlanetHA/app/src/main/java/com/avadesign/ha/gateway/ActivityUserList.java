package com.avadesign.ha.gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityUserList extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private Button add,edit,back;
	
	private ListView listview;
	
	private ProgressDialog mDialog_SPINNER;
	
	private ArrayList<HashMap<String,String>> userlist;
	
	private GetListTask mGetListTask;
		
	private MyCustomAdapter mAdapter;
	
	private Boolean Edit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activityuserlist);
		
		
		listview = (ListView)this.findViewById(R.id.activity_room_list);
		
		add=(Button)findViewById(R.id.tab_add);
		edit=(Button)findViewById(R.id.tab_edit);
		back=(Button)findViewById(R.id.tab_back);
		
		add.setOnClickListener(tab_button_down);
		edit.setOnClickListener(tab_button_down);
		back.setOnClickListener(tab_button_down);
		
		add.setTag(1);
		edit.setTag(2);
		back.setTag(3);
		
		back.setVisibility(View.GONE);
		
		userlist=new ArrayList<HashMap<String,String>>();
		mAdapter = new MyCustomAdapter(this, R.layout.item_user,userlist);
		listview.setAdapter(mAdapter);
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mDialog_SPINNER.setCancelable(false);
	}
	
	@Override
	protected void onResume() 
	{
		Edit=false;		
		
		GetListCommand();
		
		super.onResume();
	}

	private void GetListCommand()
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"account.cgi","action","load"});
	}
	
	private void AddListCommand(final String acc , final String pwd)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"account.cgi","action","add","account",acc,"pwd",pwd});
	}
	
	private void ModifiedListCommand(final String acc , final String pwd)
	{
		new Thread()
		{
			public void run()
			{
				CusPreference cp = new CusPreference(ActivityUserList.this);
				
				Map<String, String> map = new HashMap<String,String>();
				map.put("action","update");
				map.put("level","user");
				map.put("account",acc);
				map.put("pwd",pwd);
				
				if(!cp.isLocalUsed()){
					map.put("mac", cp.getControllerMAC());
					map.put("username", cp.getUserName());
					map.put("userpwd", cp.getUserPwd());
					map.put("tunnelid", "0");
				}
				
				SendHttpCommand.send(String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"account.cgi",
						map, 
						cp.getUserName(), 
						cp.getUserPwd(), 
						cp.isLocalUsed());
			}
		}.start();
	}
	
	private void RemoveLocCommand(final String acc)
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute(new String[]{"account.cgi","action","remove","account",acc});
	}
	
	private class GetListTask extends AsyncTask<String, Void, Boolean> 
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
			CusPreference cp = new CusPreference(ActivityUserList.this);
			
			Map<String, String> map = new HashMap<String,String>();
			map.put(params[1], params[2]);
			if (params[2].equals("add"))
			{
				map.put(params[3], params[4]);
				map.put(params[5], params[6]);
			}
			else if (params[2].equals("update"))
			{
				map.put(params[3], params[4]);
				map.put(params[5], params[6]);
				map.put(params[7],params[8]);
			}
			else if (params[2].equals("remove"))
			{
				map.put(params[3], params[4]);
			}
			if(!cp.isLocalUsed()){
				map.put("mac", cp.getControllerMAC());
				map.put("username", cp.getUserName());
				map.put("userpwd", cp.getUserPwd());
				map.put("tunnelid", "0");
			}
			
			//list=new ArrayList<HashMap<String,String>>();
			
			list = SendHttpCommand.getlist(
					
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+params[0],
					map, 
					cp.getUserName(), 
					cp.getUserPwd(), 
					cp.isLocalUsed(),"user");
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetListTask = null;
			mDialog_SPINNER.dismiss();
			
			if (list!=null)
			{
				userlist.clear();
				for (HashMap<String,String> map : list) 
				{
					Log.v(TAG,"id-"+map.get("user")+" loc="+map.get("last_change"));
					userlist.add(map);
				}
				
				mAdapter.notifyDataSetChanged();
			}
			else
			{
				userlist.clear();
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private Button.OnClickListener edit_button_down = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			HashMap<String, String> map=userlist.get((Integer) v.getTag());
			final String name=map.get("user");
			
			View view= View.inflate(ActivityUserList.this,R.layout.dialog_set_auth,null);
			
			final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
			final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
			
			uname_edit.setVisibility(View.GONE);
			
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(ActivityUserList.this);
			builder.setTitle(ActivityUserList.this.getText(R.string.dialog_title_edit_user));
			builder.setView(view);
			builder.setMessage(ActivityUserList.this.getText(R.string.dialog_message_edit_user));
			builder.setCancelable(false);
			builder.setPositiveButton(ActivityUserList.this.getText(R.string.alert_button_ok), new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
					ModifiedListCommand(name,upwd_edit.getText().toString());
				}
			});
			builder.setNegativeButton(ActivityUserList.this.getText(R.string.alert_button_cancel), new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface arg0, int arg1) 
				{
					//return false;
				}
			});
			 
			builder.create().show();
		}
	};
	
	private Button.OnClickListener del_button_down = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			HashMap<String,String> map=userlist.get((Integer) v.getTag());
			
			final String name=map.get("user");
			
			AlertDialog.Builder delAlertDialog = new AlertDialog.Builder(ActivityUserList.this);
			   delAlertDialog.setTitle(R.string.dialog_title_del_user);
			   delAlertDialog.setMessage(getString(R.string.dialog_message_del_user)+map.get("user"));
			   delAlertDialog.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() 
			   {
				   public void onClick(DialogInterface arg0, int arg1) 
				   {
					   RemoveLocCommand(name);
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
	};
	
	private Button.OnClickListener tab_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			switch ((Integer) v.getTag())
			{			
				case 1:
				{
					AddUser_show();
					break;
				}
				case 2:
				{
					Edit=!Edit;
					edit.setSelected(Edit);
					mAdapter.notifyDataSetChanged();
					break;
				}
				case 3:
				{
					finish();
					break;
				}
				default:
					break;
			}
			
			PlayButtonSound.play(ActivityUserList.this);
		}
	};
	
	private void AddUser_show()
	{
		View view= View.inflate(ActivityUserList.this,R.layout.dialog_set_auth,null);
		
		final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
		final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
		
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(ActivityUserList.this);
		
		builder.setTitle(ActivityUserList.this.getText(R.string.dialog_title_add_user));
		builder.setView(view);
		builder.setMessage(ActivityUserList.this.getText(R.string.dialog_message_add_user));
		builder.setCancelable(false);
		builder.setPositiveButton(ActivityUserList.this.getText(R.string.alert_button_ok), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				AddListCommand(uname_edit.getText().toString(),upwd_edit.getText().toString());
			}
		});
		builder.setNegativeButton(ActivityUserList.this.getText(R.string.alert_button_cancel), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				//return false;
			}
		});
		
		builder.create().show();
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
            ImageButton edit,del;
            TextView name;
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
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
        	
            viewHolder.edit.setOnClickListener(edit_button_down);
            viewHolder.del.setOnClickListener(del_button_down);
            
            viewHolder.edit.setTag(position);
            viewHolder.del.setTag(position);
            
            if(Edit)
            {
            	viewHolder.edit.setVisibility(View.VISIBLE);
            	viewHolder.del.setVisibility(View.VISIBLE);
            }
            else
            {
            	viewHolder.edit.setVisibility(View.INVISIBLE);
            	viewHolder.del.setVisibility(View.INVISIBLE);
            }
            String name=userlist.get(position).get("user").toString();
            
            viewHolder.name.setText(name);
            	
            return convertView;
    	}
    }
}
