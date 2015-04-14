package com.avadesign.ha.frame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;


public class CusPreference {
	private SharedPreferences setting;
	private Editor editor;
	
	@SuppressLint("CommitPrefEdits")
	public CusPreference(Context context)
	{
		setting = context.getSharedPreferences("SETTING_PREF", 0);
		editor = setting.edit();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String,String>> getIcon_Image()
	{
		String wordBase64 = setting.getString("newWord", "");
		
		byte[] base64Bytes = Base64.decode(wordBase64.getBytes(), 0);
				
		ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
		
		ObjectInputStream ois;
	       
		try 
		{
			ois = new ObjectInputStream(bais);
			
			ArrayList<HashMap<String,String>> result = (ArrayList<HashMap<String,String>>) ois.readObject();
			
			return result;
		} 
		catch (StreamCorruptedException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}    
		return null;
	}
		
	public boolean setIcon_Image(ArrayList<HashMap<String,String>> image_array) 
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(3000);
		
        ObjectOutputStream oos=null;
        
        try 
        {
        	oos = new ObjectOutputStream(baos);
        	oos.writeObject(image_array);
        }
        catch (IOException e) 
        {
        	e.printStackTrace();
        }
        
        String newWord = new String(Base64.encode(baos.toByteArray(),0));
        
        editor.putString("newWord", newWord);
        editor.commit();
        
		return editor.commit();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String,Object>> getGatewayHistory()
	{
		String wordBase64 = setting.getString("history", "");
		
		byte[] base64Bytes = Base64.decode(wordBase64.getBytes(), 0);
				
		ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
		
		ObjectInputStream ois;
	       
		try 
		{
			ois = new ObjectInputStream(bais);
			
			ArrayList<HashMap<String, Object>> result = (ArrayList<HashMap<String,Object>>) ois.readObject();
			
			return result;
		} 
		catch (StreamCorruptedException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}    
		return null;
	}

	public boolean setGatewayHistory(ArrayList<HashMap<String,Object>> his_array) 
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(3000);
		
        ObjectOutputStream oos=null;
        
        try 
        {
        	oos = new ObjectOutputStream(baos);
        	oos.writeObject(his_array);
        }
        catch (IOException e) 
        {
        	e.printStackTrace();
        }
        
        String newWord = new String(Base64.encode(baos.toByteArray(),0));
        
        editor.putString("history", newWord);
        editor.commit();
        
		return editor.commit();
	}
	
//	setController----------------------------------------------------------------------------
	
	public boolean setControllerMAC(String mac) 
	{
		editor.putString("C_MAC", mac.toUpperCase(Locale.getDefault()));
		return editor.commit();
	}
	
	public String getControllerMAC()
	{
		return setting.getString("C_MAC", "");
	}
	
	public boolean setControllerIP(String ip) 
	{
		editor.putString("C_IP", ip);
		return editor.commit();
	}
	
	public String getControllerIP()
	{
		return setting.getString("C_IP", "");
	}
	
	public boolean setControllerAcc(String acc) 
	{
		editor.putString("C_ACC", acc);
		return editor.commit();
	}
	
	public String getControllerAcc()
	{
		return setting.getString("C_ACC", "");
	}
	
	public boolean setControllerPwd(String pwd) 
	{
		editor.putString("C_PWD", pwd);
		return editor.commit();
	}
	
	public String getControllerPwd()
	{
		return setting.getString("C_PWD", "");
	}
	
	public boolean setControllerPort(int port) 
	{
		editor.putInt("C_PORT", port);
		return editor.commit();
	}
	
	public int getControllerPort()
	{
		return setting.getInt("C_PORT", 0);
	}
	
	public boolean setControllerVersion(String version) 
	{
		editor.putString("C_VERSION", version);
		return editor.commit();
	}
	
	public String getControllerVersion()
	{
		return setting.getString("C_VERSION", "");
	}
	
//	----------------------------------------------------------------------------
	
	public boolean setUpdateTime(long time) 
	{
		editor.putLong("C_UpdateTime", time);
		return editor.commit();
	}
	
	public Long getUpdateTime()
	{
		return setting.getLong("C_UpdateTime", 0);
	}
	
	
	
	public boolean setIsLocalUsed(boolean value) {
		editor.putBoolean("IS_LOCAL", value);
		return editor.commit();
	}
	
	public boolean isLocalUsed(){
		return setting.getBoolean("IS_LOCAL", true);
	}
	
	public boolean setIsPush(boolean value) {
		editor.putBoolean("IsPush", value);
		return editor.commit();
	}
	
	public boolean isPush(){
		return setting.getBoolean("IsPush", true);
	}
	
	
	
	
	public boolean setUserAccount(String value) {
		editor.putString("ADMIN_NAME", value);
		return editor.commit();
	}
	
	public String getUserAccount(){
		return setting.getString("ADMIN_NAME", "");
	}
	
	public boolean setUserPassword(String value) {
		editor.putString("ADMIN_PWD", value);
		return editor.commit();
	}
	
	public String getUserPassword(){
		return setting.getString("ADMIN_PWD", "");
	}
	
	public boolean setAutoLogin(boolean value) {
		editor.putBoolean("AUTO_LOGIN", value);
		return editor.commit();
	}
	
	public boolean getAutoLogin(){ //when got error, like http 401
		return setting.getBoolean("AUTO_LOGIN", false);
	}
	
	
	
	
	
	
	public boolean setStopPolling(boolean value) {
		editor.putBoolean("STOP_POLLING", value);
		return editor.commit();
	}
	
	public boolean isStopPolling(){ //when got error, like http 401
		return setting.getBoolean("STOP_POLLING", false);
	}
	
	
	public boolean setFilter(int filter) {
		editor.putInt("filter", filter);
		return editor.commit();
	}
	
	public int getFilter(){
		return setting.getInt("filter", 0);
	}
}
