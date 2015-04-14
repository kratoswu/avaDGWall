/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.avadesign.ha.frame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.EditText;

import com.avadesign.ha.R;

public class DialogSetAuth{
	
	public static void show(final Context context)
	{
		final CusPreference cp = new CusPreference(context);
		View view= View.inflate(context,R.layout.dialog_set_auth,null);
		
		final EditText uname_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_uname_txt));
		final EditText upwd_edit = (EditText)(view.findViewById(R.id.dialog_set_auth_upwd_txt));
		
		uname_edit.setText(cp.getUserName());
		upwd_edit.setText(cp.getUserPwd());
		
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(context);
		
		builder.setTitle(context.getText(R.string.dialog_title_set_auth));
		builder.setView(view);
		builder.setMessage(context.getText(R.string.dialog_message_set_auth));
		builder.setCancelable(false);
		builder.setPositiveButton(context.getText(R.string.alert_button_ok), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				cp.setUserName(uname_edit.getText().toString());
				cp.setUserPwd(upwd_edit.getText().toString());
				cp.setStopPolling(false);				
			}
		});
		builder.setNegativeButton(context.getText(R.string.alert_button_cancel), new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				// TODO Auto-generated method stub
			}
		});
		
		builder.create().show();
	}
}
