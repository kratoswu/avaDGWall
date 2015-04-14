package com.avadesign.ha.frame;

import android.content.Context;
import android.media.MediaPlayer;

import com.avadesign.ha.R;


public class PlayButtonSound 
{	
	public static void play(final Context context)
	{
		MediaPlayer media = new MediaPlayer();
		
		media=MediaPlayer.create(context, R.raw.click);
		
		if (media != null)
	    	media.stop();
		
		try 
		{
			media.prepare();
			
			media.setLooping(false);
				
			media.start();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
