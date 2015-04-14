package com.avadesign.ha;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.avadesign.ha.frame.CusPreference;
import com.avadesign.ha.frame.DialogSetAuth;
import com.avadesign.ha.frame.PlayButtonSound;
import com.avadesign.ha.frame.ServicePolling;
import com.avadesign.ha.frame.SharedClassApp;
import com.avadesign.ha.frame.ZWaveNode;
import com.avadesign.ha.frame.ZWaveNodeValue;

public class ActivityReport extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private Spinner report_sources_spinner,report_unit_spinner;
	
	private EditText start_edit,end_edit;
	
	private Button tab_report;
	
	private ArrayList<HashMap<String,Object>> mNodes;
	
	private ImageView item_image;//,imageview_graph;
	
	//private TextView text_nodata;
	
	private String NODE_ID;
	
	
	
	private Bitmap btm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		
		FindView();
		
		Setlistener();
	}
	
	private void FindView()
	{
		report_sources_spinner	=(Spinner)findViewById(R.id.item_report_sources_spinner);
		report_unit_spinner		=(Spinner)findViewById(R.id.item_report_unit_spinner);
		
		start_edit	=(EditText)findViewById(R.id.report_start_edit);
		end_edit	=(EditText)findViewById(R.id.report_end_edit);
		
		tab_report	=(Button)findViewById(R.id.tab_report);
		
		item_image	=(ImageView)findViewById(R.id.item_report_image);
		
		//imageview_graph	=(ImageView)findViewById(R.id.imageview_graph);
		
		//text_nodata =(TextView)findViewById(R.id.textView1);
	}
	
	private void Setlistener()
	{
		mNodes = new ArrayList<HashMap<String,Object>>();
		
		tab_report.setOnClickListener(admin_button_down);
		tab_report.setTag(0);
		
		String [] string_unit=new String [4];
		string_unit[0]=getString(R.string.REPORT_VOLTAGE);
		string_unit[1]=getString(R.string.REPORT_CURRENT);
		string_unit[2]=getString(R.string.REPORT_WATTAGE);
		string_unit[3]=getString(R.string.REPORT_ENERGY);
		
		ArrayAdapter<String> adapter_unit=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_unit);
		adapter_unit.setDropDownViewResource(R.layout.custom_spinner);
		report_unit_spinner.setAdapter(adapter_unit);
		report_unit_spinner.setOnItemSelectedListener(Spinner_select);
	}
	
	protected void onResume() 
	{
		super.onResume();
		
		CusPreference cp = new CusPreference(ActivityReport.this);
		cp.setStopPolling(false);
		
		start_edit.clearFocus();
		end_edit.clearFocus();
		start_edit.setOnFocusChangeListener(TextFocusChange);
		end_edit.setOnFocusChangeListener(TextFocusChange);
		
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ServicePolling.HTTP_401);
		mFilter.addAction(ServicePolling.HTTP_404);
		mFilter.addAction(ServicePolling.REFRESH_NODE_DATA);
		registerReceiver(mBroadcastReceiver, mFilter);
	}
	
	/*
	@Override
	public void onBackPressed() 
	{
		if (imageview_graph.VISIBLE==0)
		{
			show=false;
			imageview_graph.setVisibility(View.INVISIBLE);
			text_nodata.setVisibility(View.INVISIBLE);
		}
		else
		{
			finish();
		}
	}
	*/
	
	private Spinner.OnItemSelectedListener Spinner_select=new Spinner.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
		{
			//Log.v(TAG,"position"+spinner_mode.getSelectedItemPosition());
			
			if (arg0.getId()==R.id.item_report_sources_spinner)
			{
				ZWaveNode mZWaveNode=new ZWaveNode(mNodes.get(arg2));
				
				//Log.v(TAG,"id="+mZWaveNode.id);
				
				SetZWaveNode_image(mZWaveNode.id);
			}
			else
			{
				int j=report_unit_spinner.getSelectedItemPosition();
				
				Log.v(TAG,"select="+j);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) 
		{
			
		}
	};
	
	@Override
	protected void onPause() 
	{
		CusPreference cp = new CusPreference(ActivityReport.this);
		cp.setStopPolling(true);
		
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
				DialogSetAuth.show(ActivityReport.this);
			}
			else if(intent.getAction().equals(ServicePolling.HTTP_404))
			{
				CusPreference cp = new CusPreference(ActivityReport.this);
				if(!cp.getControllerIP().equals(""))
					Toast.makeText(ActivityReport.this, getString(R.string.status_no_connect), Toast.LENGTH_SHORT).show();
			}
			else if(intent.getAction().equals(ServicePolling.REFRESH_NODE_DATA))
			{
				GetNode();
			}
		}
	};
	
	private void GetNode()
	{
		ArrayList<HashMap<String,Object>> mNodes1 = new ArrayList<HashMap<String,Object>>();
		
		((SharedClassApp)(ActivityReport.this.getApplication())).refreshNodesList(mNodes1);
		
		for(int i=0;i<mNodes1.size();i++)
		{
			ZWaveNode znode = new ZWaveNode(mNodes1.get(i));
			
			for(ZWaveNodeValue zvalue : znode.value)
			{
				if (zvalue.class_c.equalsIgnoreCase("meter"))
				{
					mNodes.add(mNodes1.get(i));
					break;
				}
			}

		};
		
		CusPreference cp = new CusPreference(ActivityReport.this);
		cp.setStopPolling(true);
		
		String [] string_node=new String [mNodes.size()];
		for(int i=0;i<mNodes.size();i++)
		{
			ZWaveNode mZWaveNode= new ZWaveNode(mNodes.get(i));
			string_node[i]=getZWaveNode_name(mZWaveNode);
		}
		
		ArrayAdapter<String> adapter_node=new ArrayAdapter<String>(this, R.layout.custom_spinner,string_node);
		adapter_node.setDropDownViewResource(R.layout.custom_spinner);
		report_sources_spinner.setAdapter(adapter_node);
		report_sources_spinner.setOnItemSelectedListener(Spinner_select);
	}
	
	private Button.OnClickListener admin_button_down = new Button.OnClickListener()
	{
		public void onClick(View v) 
		{
			if ((Integer) v.getTag()==0)
			{
				CusPreference cp = new CusPreference(ActivityReport.this);
				
				int ii=report_sources_spinner.getSelectedItemPosition();
				int jj=report_unit_spinner.getSelectedItemPosition();
				int timezone=TimeZone.getDefault().getRawOffset()/3600000;
				
				Log.v(TAG,"ii="+ii);
				Log.v(TAG,"jj="+jj);
				
				String unit="";
				
				if (jj==0)
					unit="volt";
				else if (jj==1)
			        unit="ampere";
			    else if (jj==2)
			        unit="watt";
			    else if (jj==3)
			        unit="kwh";
				
				Log.v(TAG,"fromData="+start_edit.getText().toString());
				Log.v(TAG,"toData="+end_edit.getText().toString());
				Log.v(TAG,"timezone="+timezone);
				
				HashMap<String, String> map = new HashMap<String,String>();
				map.put("mac", cp.getControllerMAC());
				map.put("id", NODE_ID);
				map.put("unit" , unit);
				map.put("fromDate", start_edit.getText().toString());
				map.put("toDate", end_edit.getText().toString());
				map.put("timezoneOffset", String.valueOf(timezone));
				
				Intent intent = new Intent();
				Bundle bundle= new Bundle();
	    		bundle.putSerializable("map", map);
	    		intent.putExtras(bundle);
	    		intent.setClass(ActivityReport.this, ActivityReportImage.class);
				startActivity(intent);
			}
			PlayButtonSound.play(ActivityReport.this);
		}
	};
	
	private EditText.OnFocusChangeListener TextFocusChange =new OnFocusChangeListener()
	{
		@SuppressWarnings("deprecation")
		@Override
		public void onFocusChange(View arg0, boolean arg1) 
		{
			//Log.v(TAG,"Edit="+arg0+", focus="+arg1);
			if (arg1)
			{
				if (arg0==start_edit)
				{
					arg0.clearFocus();
					showDialog(0);
				}
				else
				{
					arg0.clearFocus();
					showDialog(1);
				}
			}
			
		}
	};
	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
		final int id_num=id;
		
		Calendar calendar = Calendar.getInstance();  
		
		DatePickerDialog.OnDateSetListener dateListener=new DatePickerDialog.OnDateSetListener() 
		{
	        @Override
	        public void onDateSet(DatePicker view, int year, int month,int day) 
	        {
	            if (id_num==0)
	            	start_edit.setText(setDateFormat(year,month,day));
	            else
	            	end_edit.setText(setDateFormat(year,month,day));
	        }
	    };
		
		return new DatePickerDialog(this,dateListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)); 
		
		//return new DatePickerDialog(this,, mYear,mMon, mDay);
	}

	private String setDateFormat(int year,int monthOfYear,int dayOfMonth)
	{
        return String.valueOf(monthOfYear + 1) + "/" + String.valueOf(dayOfMonth) + "/" + String.valueOf(year);
    }
	
	private String getZWaveNode_name(final ZWaveNode mZWaveNode)
	{
		if (mZWaveNode.name.equals("") || mZWaveNode.name.equals(" "))
        {
        	if (mZWaveNode.product.equals(""))
        		return mZWaveNode.gtype;
        	else
        		return mZWaveNode.product;
        }
        else
        	return mZWaveNode.name;
	}
	
	private void SetZWaveNode_image(final String node_id)
	{
		Log.v(TAG,"node_id="+node_id);
		
		NODE_ID=node_id;
		
		for(HashMap<String,Object> map : mNodes)
		{		
			ZWaveNode znode = new ZWaveNode(map);
			
			if (znode.id.equals(node_id))
			{
				int default_image=-1;
				
				if(znode.gtype.toLowerCase().indexOf("switch")!=-1)
				{
					default_image=R.drawable.dimmer_light_off;
				}
				else if(znode.gtype.toLowerCase().indexOf("sensor")!=-1)
				{
					default_image=R.drawable.shock_off;
				}
				else if(znode.gtype.toLowerCase().indexOf("door lock")!=-1)
				{
					default_image=R.drawable.doorkeypad_off;
				}
				else if(znode.gtype.toLowerCase().indexOf("motor")!=-1)
				{
					default_image=R.drawable.window_cover_off;
				}
				else
				{
					default_image=R.drawable.gw;
				}
				
				String path=label_to_path_label(znode.icon,"normal");
    			
    			Bitmap bitmap = BitmapFactory.decodeFile(path);
    			
    			if (bitmap!=null)
    				item_image.setImageBitmap(bitmap);
    			else
    				item_image.setImageResource(default_image);
				
				break;
			}
		}
	}
	
	private String label_to_path_label (String label , String status)
	{
		CusPreference cp = new CusPreference(ActivityReport.this);
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
