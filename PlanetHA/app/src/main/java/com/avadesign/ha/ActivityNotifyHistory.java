package com.avadesign.ha;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityNotifyHistory extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private ListView listview;
	private ProgressDialog mDialog_SPINNER;
	private ArrayList<HashMap<String,String>> history_array;
	private MyCustomAdapter adapter;
	private GetListTask mGetListTask;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notify_history);
		
		listview = (ListView)this.findViewById(R.id.listview);
		
		history_array=new ArrayList<HashMap<String,String>>();
		
		adapter = new MyCustomAdapter(this, R.layout.item_history,history_array);
		
		listview.setAdapter(adapter);
		
		mDialog_SPINNER = new ProgressDialog(this);
		mDialog_SPINNER.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
	
		GetListCommand();
	}
	private void GetListCommand()
	{
		if(mGetListTask!=null)
			return;
		
		mGetListTask = new GetListTask();
		mGetListTask.execute();
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
            TextView name,time;
        }

    	@Override public View getView(int position, View convertView, ViewGroup parent)
    	{
            if (convertView == null) 
            {
            	convertView = inflater.inflate(R.layout.item_history,null);
            	
        		viewHolder = new ViewHolder();

        		viewHolder.name = (TextView)convertView.findViewById(R.id.text_title);
        		viewHolder.time = (TextView)convertView.findViewById(R.id.text_detail);
        		
        		convertView.setTag(viewHolder);
            }
            else
            	viewHolder = (ViewHolder) convertView.getTag();
           
            String name=history_array.get(position).get("node_name").toString();
            String time=history_array.get(position).get("time").toString();
            
            
            String [] time_ary=time.split(" ");
            ArrayList<String> time_array=new ArrayList<String>();
			
			for(int i=0;i<time_ary.length;i++)
				time_array.add(time_ary[i]);
            
            //Log.v(TAG,""+time_array);
            
            viewHolder.name.setText(name+"is Alarm");
            
          //星期月日時年
            if (time_array.size()>6)
            	viewHolder.time.setText(time_array.get(6)+"-"+time_array.get(1)+"-"+time_array.get(3)+"-"+time_array.get(0)+"  "+time_array.get(4));
            else
                viewHolder.time.setText(time_array.get(5)+"-"+time_array.get(1)+"-"+time_array.get(2)+"-"+time_array.get(0)+"  "+time_array.get(3));
           
            	
            return convertView;
    	}
    }
	
	private class GetListTask extends AsyncTask<String, Void, Boolean> 
	{		
		private String xml;
		
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
			CusPreference cp = new CusPreference(ActivityNotifyHistory.this);

			xml = SendHttpCommand.get_histiry(
					String.format( cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax) , cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip) , cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port) )+"log/alarm.log"+(cp.isLocalUsed()?"":"?mac="+cp.getControllerMAC()+"&username="+cp.getUserName()+"&userpwd="+cp.getUserPwd()+"&tunnelid=0"), 
					cp.getUserName(), 
					cp.getUserPwd());
			
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) 
		{
			mGetListTask = null;
			mDialog_SPINNER.dismiss();
			
			
			if (xml!=null)
			{
				String rpxml=EregiReplace("(\r| |)", "",xml);
				
				String[] array = rpxml.split("\n"); 
				
				ArrayList<String> str_ary=new ArrayList<String>();
				
				for(int i=0;i<array.length;i++)
					str_ary.add(array[i]);
				
				//Log.v(TAG,"str_ary="+str_ary);
				//Log.v(TAG,"str_ary count="+str_ary.size());
				
				try 
				{
					String xxx="<history_list>"+xml+"</history_list>";
					
					Document document = DocumentHelper.parseText(xxx);
					//Log.v(TAG,""+xxx);
					
					Iterator<?> it=null;
					
					it = document.selectNodes("history_list/alarm").iterator();
					
					history_array.clear();
					
					int i=0;
					while (it.hasNext()) 
					{
						Element ele = (Element) it.next();
						HashMap<String, String> map=getAttr(ele);
						map.put("id",String.valueOf(i));
						history_array.add(map);
						i++;
					}
					document = null;
					it = null;
					
					//Log.v(TAG,"map="+history_array);
					
					Collections.sort(history_array, new Comparator<HashMap<String, String>>()
							{
								@Override
								public int compare(HashMap<String, String> lhs,HashMap<String, String> rhs) 
								{
									return Float.valueOf((String)rhs.get("id")).compareTo(Float.valueOf((String) lhs.get("id")));
								}
							});
					
					
					if (history_array==null)
						history_array.clear();
					
					adapter.notifyDataSetChanged();
					
				} 
				catch (DocumentException e) 
				{
					e.printStackTrace();
					Log.v(TAG,"catch");
				}
			}
		}
	}
	
	private static HashMap<String,String> getAttr(Element ele)
	{
		HashMap<String,String> map = new HashMap<String,String>();
		try
		{
			List<?> attributes = ele.attributes();
			for (int i = 0; i < attributes.size(); i++) 
			{
				Attribute a = ((Attribute) attributes.get(i));
				map.put(a.getName(), a.getValue());
				
				//Log.v("TAG",a.getName()+"="+a.getValue());
			}
			String current = ele.getTextTrim();
			map.put("current",current);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return map;
	}
	
	private String EregiReplace(String strFrom,String strTo,String strTarget)
	{
		if(strTarget == null)
		{
			return strTarget;
		}
			
		String strPattern="(?i)"+strFrom;
		Pattern p =Pattern.compile(strPattern);
		Matcher m =p.matcher(strTarget);
		
		if(m.find())
		{
			return strTarget.replaceAll(strFrom, strTo);
		}
		else
		{
			return strTarget;
		}			
	}
}
