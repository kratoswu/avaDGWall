package com.avadesign.ha;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class ActivityAbout extends Activity 
{
	private final String TAG = this.getClass().getSimpleName();
	
	private TextView appversion,text_html;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		appversion=(TextView)findViewById(R.id.text_version);
		text_html=(TextView)findViewById(R.id.TextView01);
		
		appversion.setText("");
		
		text_html.setMovementMethod(LinkMovementMethod.getInstance());
		text_html.setText(Html.fromHtml(getString(R.string.label_about_web)));
		
		PackageManager manager = this.getPackageManager();
		try 
		{
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			appversion.setText("Current Version "+info.versionName);
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
