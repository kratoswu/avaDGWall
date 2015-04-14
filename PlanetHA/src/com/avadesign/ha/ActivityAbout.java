package com.avadesign.ha;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.avadesign.ha.frame.BaseActivity;

public class ActivityAbout extends BaseActivity 
{
	private TextView text_html;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		text_html=(TextView)findViewById(R.id.TextView01);
		
		text_html.setMovementMethod(LinkMovementMethod.getInstance());
		text_html.setText(Html.fromHtml(getString(R.string.label_about_web)));
	}
}
