package com.avadesign.ha.frame;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.avadesign.ha.R;

public class ServicePolling extends Service 
{
	private final String TAG = this.getClass().getSimpleName();
	private ArrayList<HashMap<String,Object>> mNodes;
	private final int pooling_period_local = 500;
	private final int pooling_period_cloud = 2000;
	private Timer timer = null;
	private CusPreference cp;
	
	public final static String REFRESH_NODE_DATA = "REFRESH_NODE_DATA";
	public final static String HTTP_401 = "HTTP_401";
	public final static String HTTP_404 = "HTTP_404";
		
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return null;
	}
	
	@Override
	public void onCreate() 
	{
		cp = new CusPreference(ServicePolling.this);
		
		timer = new Timer();
		
		timer.schedule(mTimerTask, 500, cp.isLocalUsed() ? pooling_period_local : pooling_period_cloud );
		
		super.onCreate();
		Log.d(TAG,"ServicePolling start");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		return super.onStartCommand(intent, flags, startId);
	}
		
	@Override
	public void onDestroy() 
	{
		if(timer!=null)
			timer.cancel();
		
		super.onDestroy();
		Log.d(TAG,"ServicePolling stop");
	}
	
	private void updateData() 
	{
		((SharedClassApp)(ServicePolling.this.getApplication())).setNodesList(mNodes);
		
		ZWaveNode mZWaveNode = null;
		if((mZWaveNode=((SharedClassApp)(ServicePolling.this.getApplication())).getZWaveNode())!=null){
			String targetNodeId = mZWaveNode.id;
			
			for(HashMap<String,Object> map:mNodes){
				ZWaveNode node = new ZWaveNode(map);
				boolean isFound = false;
				if(node.id.equals(targetNodeId)){
					((SharedClassApp)(ServicePolling.this.getApplication())).setZWaveNode(node);
					break;
				}
				
				if(!isFound){
					((SharedClassApp)(ServicePolling.this.getApplication())).setZWaveNode(null); //node has removed
				}
			}
		}
		
		Intent i = new Intent(REFRESH_NODE_DATA);
		sendBroadcast(i);
		
		mNodes = null;
	}
	
	private void broad401Error()
	{
		Intent i = new Intent(HTTP_401);
		sendBroadcast(i);
	}
	
	private void broad404Error()
	{
		Intent i = new Intent(HTTP_404);
		sendBroadcast(i);
		Log.v("polling","polling 404");
		
		//timer.cancel();
		//timer.schedule(mTimerTask, 1000, cp.isLocalUsed() ? pooling_period_local : pooling_period_cloud );
	}

	private TimerTask mTimerTask= new TimerTask()
	{
	    public void run() 
	    {
	    	if(cp.isStopPolling())
	    	{
	    		Log.v(TAG,"stop polling");
	    		return;
	    	}
	    	mNodes = new ArrayList<HashMap<String,Object>>();
	    	try
	    	{
				String us = String.format(cp.isLocalUsed()?getString(R.string.local_url_syntax):getString(R.string.server_url_syntax),cp.isLocalUsed()?cp.getControllerIP():getString(R.string.server_ip),cp.isLocalUsed()?String.valueOf(cp.getControllerPort()):getString(R.string.server_port))+"poll2.xml"+(cp.isLocalUsed()?"":"?mac="+cp.getControllerMAC()+"&username="+cp.getControllerAcc()+"&userpwd="+cp.getControllerPwd()+"&tunnelid=0");
				URL url = new URL(us);
				
				Log.v(TAG,"url="+us);
				
				/*
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
				*/
				
				DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
				
				httpClient.getCredentialsProvider().setCredentials(
		                new AuthScope(url.getHost(), url.getPort()), 
		                new UsernamePasswordCredentials(cp.getControllerAcc(), cp.getControllerPwd()));   
	            
				HttpGet httpRequest = new HttpGet(us);
				
				BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
				int code = httpResponse.getStatusLine().getStatusCode();
	    		
				//Log.d(TAG,"http status code:"+code);
				if (code == 200)
				{
					String xml = EntityUtils.toString(httpResponse.getEntity());
					
					boolean success = parserXML(new String(xml.getBytes("ISO-8859-1"),"UTF-8")); //Memory Leak?
					xml = null;
					//Log.d(TAG,"Get lastest data:"+success);
					
					if(success)
					{
						updateData();
					}
					
					System.gc();
				}
				else if(code == 401)
				{
					cp.setStopPolling(true);
					broad401Error();
				}
				else if(code == 404)
				{
					//broad404Error();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				//Log.d(TAG,"http error:"+e.toString());
				
				broad404Error();
			}
		}
	    
	    private boolean parserXML(String xml)
	    {
			try{
				Document document = DocumentHelper.parseText(xml);
				Iterator<?> it = document.selectNodes("/poll/admin").iterator();
				while (it.hasNext()) 
				{
					Element ele = (Element) it.next();
					((SharedClassApp)(ServicePolling.this.getApplication())).setIsActive(ele.attribute("active").getValue().equalsIgnoreCase("true"));
					((SharedClassApp)(ServicePolling.this.getApplication())).setControllerState(ele.getStringValue());
				}

				it = document.selectNodes("/poll/node").iterator();
				while (it.hasNext()) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					Element ele = (Element) it.next();
					map.putAll(getAttr(ele));
					map.put("value", getVals(ele));
					mNodes.add(map);
				}
				document = null;
				it = null;
				
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		//Get element attributes
		private HashMap<String, String> getAttr(Element ele){
			HashMap<String, String> map = new HashMap<String, String>();
			try{
				List<?> attributes = ele.attributes();
				for (int i = 0; i < attributes.size(); i++) {
					Attribute a = ((Attribute) attributes.get(i));
					map.put(a.getName(), a.getValue());
				}
				map.put("sort_id", map.get("id"));
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return map;
		}
		
		//Get <value>
		private List<HashMap<String, Object>> getVals(Element element){
			List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			try{
				Iterator<?> it = element.elementIterator();
				while (it.hasNext()) {
					Element ele = (Element) it.next();
					if(ele.getQualifiedName().equalsIgnoreCase("value")){
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.putAll(getAttr(ele));
						
						List<String> items = getItems(ele);
						if(items.size()>0)
							/*
							 * if it has <item> element, the attribute will has "current", we will get this attr with getAttr(ele), so no need to put this key ourselves.
							 * <value .. current="1">
							 * 	<item>..</item>
							 * </value>
							 */
							map.put("item", items);
						else{
							String current = ele.getTextTrim();
							
							/*
							 * without <item>, add the current value ourselves.
							 */
							map.put("current",current);
						}
							
						String help = getHelp(ele);
						if(help.length()>0)
							map.put("help", help);
						
						list.add(map);
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return list;
		}
		
		//Get <item>
		private List<String> getItems(Element element){
			List<String> list = new ArrayList<String>();
			try{
				Iterator<?> it = element.elementIterator();
				while (it.hasNext()) {
					Element ele = (Element) it.next();
					if(ele.getQualifiedName().equals("item")){
						list.add(ele.getStringValue());
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return list;
		}
		
		//Get <help>
		private String getHelp(Element element){
			String help = "";
			try{
				Iterator<?> it = element.elementIterator();
				while (it.hasNext()) {
					Element ele = (Element) it.next();
					if(ele.getQualifiedName().equals("help")){
						help = ele.getStringValue();
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return help;
		}
	};
	
}
