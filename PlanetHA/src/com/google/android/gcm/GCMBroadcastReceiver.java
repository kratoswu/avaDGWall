package com.google.android.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GCMBroadcastReceiver extends BroadcastReceiver {  
    
    @Override  
    public final void onReceive(Context context, Intent intent) {  
        // To keep things in one place.  
        GCMBaseReceiver.runIntentInService(context, intent);  
        setResult(Activity.RESULT_OK, null /* data */, null /* extra */);          
    }  
}  
