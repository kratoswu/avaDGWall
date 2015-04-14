package com.avadesign.ha.frame;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class SendHttpCommand 
{
	public static String update(String syntax)
	{
		try
		{
			/*
			HttpParams httpParameters = new BasicHttpParams();
			
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
			
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);
			
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			*/
			DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
			
			HttpGet httpRequest = new HttpGet(syntax);
			
			BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
			
			int code = httpResponse.getStatusLine().getStatusCode();
			
			if (code == 200)
			{
				String result = EntityUtils.toString(httpResponse.getEntity());
				
				String re_result=result.replace("\r", "").replace("\n", "");
				
				return re_result;  
			}
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		try 
		{
			Thread.sleep(1000); 
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static String getString (String syntax, Map<String, String> param /*,String username, String userpwd, boolean isLocal*/)
	{
		try
		{
			//URL url = new URL(syntax);

			DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
			
			//httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),new UsernamePasswordCredentials(username, userpwd));   
            			
			HttpPost httpRequest = new HttpPost(syntax);
			
			
			Iterator<Entry<String, String>> iter = param.entrySet().iterator();
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			while (iter.hasNext()) 
			{
			    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			    params.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
			} 
			
			
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			
			HttpResponse httpResponse = (HttpResponse)httpClient.execute(httpRequest);
			int code = httpResponse.getStatusLine().getStatusCode();
			
			if (code == 200)
			{
				String xml = EntityUtils.toString(httpResponse.getEntity());
				
				return xml;  
			}
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static Bitmap GetCamImage(final String syntax,final String username, final String userpwd ,final Integer size)
	{
		Bitmap bitmap =null;
		
		try 
		{
			URL url = new URL(syntax);
			
			DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
			
			httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),new UsernamePasswordCredentials(username, userpwd));   
            
			HttpGet httpRequest = new HttpGet(syntax);
			
			BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
			int code = httpResponse.getStatusLine().getStatusCode();
			
			if (code==200)
			{
				byte[] bytearray=EntityUtils.toByteArray(httpResponse.getEntity());
				
				Bitmap bm=BitmapFactory.decodeByteArray(bytearray, 0, bytearray.length);
    			
    			int width = bm.getWidth();

    			int height = bm.getHeight();

    			//Log.v("TAG","width="+width);
    			//Log.v("TAG","height="+height);
    			
    			//DisplayMetrics metrics = new DisplayMetrics();  
    		    //getWindowManager().getDefaultDisplay().getMetrics(metrics);

    		    //metrics.widthPixels
    		    //metrics.heightPixels
    			
    			int newWidth = size;

    			float scaleWidth = ((float) newWidth) / width;
    			
    			//Log.v(TAG,"scaleWidth="+scaleWidth);
    			
    			Matrix matrix = new Matrix();

    			matrix.postScale(scaleWidth, scaleWidth);
    			
    			bitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,true);
    			
    			return bitmap;
			}
			else
			{
				
			}
		} 
		catch(Exception e){
			e.printStackTrace();
		}
		
		return bitmap;
	}
	
	public static boolean send(String syntax, Map<String, String> param, String username, String userpwd, boolean isLocal)
	{
		boolean success = false;
		try
		{
			URL url = new URL(syntax);
			
			/*
			HttpParams httpParameters = new BasicHttpParams();
			
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
			
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);
			
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			*/
			DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
			
			httpClient.getCredentialsProvider().setCredentials(
	                new AuthScope(url.getHost(), url.getPort()), 
	                new UsernamePasswordCredentials(username, userpwd));   
            
			HttpPost httpRequest = new HttpPost(syntax);
			
			Iterator<Entry<String, String>> iter = param.entrySet().iterator();
			
			if(isLocal)
			{
				//without url encode
				StringBuilder params = new StringBuilder("");
				while (iter.hasNext()) 
				{
				    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
				    params.append((String)entry.getKey());
					params.append("=");
					params.append((String)entry.getValue());
					params.append("&");
				}
				
				if(params.toString().endsWith("&"))
				{
					params.delete(params.length()-1, params.length());
				}
				
				ByteArrayEntity reqEntity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
				httpRequest.setEntity(reqEntity);
			}
			else
			{
				//need url encode
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				
				while (iter.hasNext()) 
				{
				    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
				    params.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
				} 
				//Log.v("TAG","send-"+params);
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			
			//Log.v("TAG","send-"+syntax);
			
			BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
			int code = httpResponse.getStatusLine().getStatusCode();
			
			//Log.v("TAG","send-"+code);
			if (code == 200){
//				System.out.println(EntityUtils.toString(httpResponse.getEntity()));
				success = true;
			}
			else{
				success = false;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		/*
		try {
			Thread.sleep(1000); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return success;
	}
	
	public static String get_histiry(String syntax, String username, String userpwd)
	{
		try
		{
			URL url = new URL(syntax);
			
			/*
			HttpParams httpParameters = new BasicHttpParams();
			
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
			
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);
			
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			*/
			DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
			
			httpClient.getCredentialsProvider().setCredentials(
	                new AuthScope(url.getHost(), url.getPort()), 
	                new UsernamePasswordCredentials(username, userpwd));
			
			HttpGet httpRequest = new HttpGet(syntax);
			
			BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpRequest);
			int code = httpResponse.getStatusLine().getStatusCode();
			
			if (code == 200)
			{
				String xml = EntityUtils.toString(httpResponse.getEntity());
				
				//Log.v("TAG","xml"+xml);
				
				//ArrayList<HashMap<String,String>> location = parserXML(new String(xml.getBytes("ISO-8859-1"),"UTF-8"),room_scene);
				//xml = null;
				
				return xml;
			}
			return null;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<HashMap<String,String>> getlist(String syntax, Map<String, String> param, String username, String userpwd, boolean isLocal, String room_scene)
	{
		try
		{
			URL url = new URL(syntax);

			//Log.v("ActivityRoomList","url="+url);
			
			DefaultHttpClient httpClient=(DefaultHttpClient) HttpClientHelper.getHttpClient();
			
			httpClient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),new UsernamePasswordCredentials(username, userpwd));   
            			
			HttpPost httpRequest = new HttpPost(syntax);
			
			Iterator<Entry<String, String>> iter = param.entrySet().iterator();
			
			if(isLocal)
			{
				StringBuilder params = new StringBuilder("");
				while (iter.hasNext()) 
				{
				    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
				    params.append((String)entry.getKey());
					params.append("=");
					params.append((String)entry.getValue());
					params.append("&");
				}
				
				if(params.toString().endsWith("&"))
				{
					params.delete(params.length()-1, params.length());
				}
				
				ByteArrayEntity reqEntity = new ByteArrayEntity(params.toString().getBytes("UTF-8"));
				httpRequest.setEntity(reqEntity);
			}
			else
			{
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				
				while (iter.hasNext()) 
				{
				    Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
				    params.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
				} 
				
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			
			HttpResponse httpResponse = (HttpResponse)httpClient.execute(httpRequest);
			int code = httpResponse.getStatusLine().getStatusCode();
			
			if (code == 200)
			{
				String xml = EntityUtils.toString(httpResponse.getEntity());
				
				ArrayList<HashMap<String,String>> location = parserXML(new String(xml.getBytes("ISO-8859-1"),"UTF-8"),room_scene);
				xml = null;
				
				return location;  
			}
			return null;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		try 
		{
			Thread.sleep(1000); 
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static ArrayList<HashMap<String,String>> parserXML(String xml, String room_scene)
    {
		try{
			Document document = DocumentHelper.parseText(xml);
			
			//Log.v("TAG",""+xml);
			
			//Log.v("TAG",""+document);
			
			Iterator<?> it=null;
			
			if (room_scene.equals("room"))
				it = document.selectNodes("/location_list/item").iterator();
			else if (room_scene.equals("new_scene"))
				it = document.selectNodes("scenes").iterator();
			else if (room_scene.equals("scene"))
				it = document.selectNodes("scenes/sceneid").iterator();
			else if (room_scene.equals("scenevalue"))
				it = document.selectNodes("scenes/scenevalue").iterator();
			else if (room_scene.equals("trigger"))
				it = document.selectNodes("scene_trigger/trigger").iterator();
			else if (room_scene.equals("schedule"))
				it = document.selectNodes("scene_schedule/schedule").iterator();
			else if (room_scene.equals("user"))
				it = document.selectNodes("user_list/item").iterator();
			else if (room_scene.equals("notify"))
				it = document.selectNodes("notification_list/notify").iterator();
			else if (room_scene.equals("cam"))
				it = document.selectNodes("camera_list/camera").iterator();
			else if (room_scene.equals("history"))
				it = document.selectNodes("").iterator();
			else if (room_scene.equals("image"))
				it = document.selectNodes("image_list/image").iterator();
			
			ArrayList<HashMap<String,String>> loc = new ArrayList<HashMap<String,String>>();
			
			while (it.hasNext()) 
			{
				Element ele = (Element) it.next();
				loc.add(getAttr(ele));
			}
			document = null;
			it = null;
			
			//Log.v("TAG",""+loc);
			
			return loc;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	//Get element attributes
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
}
