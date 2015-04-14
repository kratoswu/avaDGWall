package com.google.android.gcm;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.avadesign.ha.frame.GCMReceiver;

public abstract class GCMBaseReceiver extends IntentService {  
    private static final String C2DM_RETRY = "com.google.android.c2dm.intent.RETRY";     
    public static final String REGISTRATION_CALLBACK_INTENT = "com.google.android.c2dm.intent.REGISTRATION";    
    private static final String C2DM_INTENT = "com.google.android.c2dm.intent.RECEIVE";  
  
    // Logging tag  
    private static final String TAG = "C2DM";  
  
    // Extras in the registration callback intents.  
    public static final String EXTRA_UNREGISTERED = "unregistered";  
    public static final String EXTRA_ERROR = "error";  
    public static final String EXTRA_REGISTRATION_ID = "registration_id";  
    public static final String ERR_SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE";  
    public static final String ERR_ACCOUNT_MISSING = "ACCOUNT_MISSING";  
    public static final String ERR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";  
    public static final String ERR_TOO_MANY_REGISTRATIONS = "TOO_MANY_REGISTRATIONS";  
    public static final String ERR_INVALID_PARAMETERS = "INVALID_PARAMETERS";  
    public static final String ERR_INVALID_SENDER = "INVALID_SENDER";  
    public static final String ERR_PHONE_REGISTRATION_ERROR = "PHONE_REGISTRATION_ERROR";  
      
    // wakelock  
    private static final String WAKELOCK_KEY = "C2DM_LIB";  
  
    private static PowerManager.WakeLock mWakeLock;  
    private final String senderId;  
  
    /** 
     * The C2DMReceiver class must create a no-arg constructor and pass the  
     * sender id to be used for registration. 
     */  
    public GCMBaseReceiver(String senderId) {  
        // senderId is used as base name for threads, etc.  
        super(senderId);  
        this.senderId = senderId;  
    }  
    
    /** 
     * Called when a cloud message has been received. 
     */  
    protected abstract void onMessage(Context context, Intent intent);  
  
    /** 
     * Called on registration error. Override to provide better 
     * error messages. 
     *   
     * This is called in the context of a Service - no dialog or UI. 
     */  
   public abstract void onError(Context context, String errorId);  
  
    /** 
     * Called when a registration token has been received. 
     */  
    public void onRegistered(Context context, String registrationId) throws IOException {  
        // registrationId will also be saved  
    }  
  
    /** 
     * Called when the device has been unregistered. 
     */  
    public void onUnregistered(Context context) {  
    }  
  
    @Override  
    public final void onHandleIntent(Intent intent) {  
        try {  
            Context context = getApplicationContext();  
            if (intent.getAction().equals(REGISTRATION_CALLBACK_INTENT)) {  
                handleRegistration(context, intent);
            } else if (intent.getAction().equals(C2DM_INTENT)) {  
                onMessage(context, intent);
            } else if (intent.getAction().equals(C2DM_RETRY)) {  
                GCMessaging.register(context, senderId); 
            }  
        } finally {  
            //  Release the power lock, so phone can get back to sleep.  
            // The lock is reference counted by default, so multiple   
            // messages are ok.  
              
            // If the onMessage() needs to spawn a thread or do something else,  
            // it should use it's own lock.  
            mWakeLock.release();  
        }  
    }  
  
      
    /** 
     * Called from the broadcast receiver.  
     * Will process the received intent, call handleMessage(), registered(), etc. 
     * in background threads, with a wake lock, while keeping the service  
     * alive.  
     */  
    static void runIntentInService(Context context, Intent intent) {  
        if (mWakeLock == null) {  
            // This is called from BroadcastReceiver, there is no init.  
            PowerManager pm =   
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,   
                    WAKELOCK_KEY);  
        }  
        mWakeLock.acquire();  
         
        // Use a naming convention, similar with how permissions and intents are   
        // used. Alternatives are introspection or an ugly use of statics.   
        intent.setClassName(context, GCMReceiver.class.getName());  
          
        context.startService(intent);  
  
    }  
      
    private void handleRegistration(final Context context, Intent intent) {  
        final String registrationId = intent.getStringExtra(EXTRA_REGISTRATION_ID);  
        String error = intent.getStringExtra(EXTRA_ERROR);  
        String removed = intent.getStringExtra(EXTRA_UNREGISTERED);  
        Log.v(TAG, "handleRegistration");  
        
        Log.v(TAG, "dmControl: registrationId = " + registrationId +  
                ", error = " + error + ", removed = " + removed);  
        if (Log.isLoggable(TAG, Log.DEBUG)) {  
            Log.d(TAG, "dmControl: registrationId = " + registrationId +  
                ", error = " + error + ", removed = " + removed);  
        }  
        if (removed != null) {  
            // Remember we are unregistered  
            GCMessaging.clearRegistrationId(context);  
            onUnregistered(context);  
            return;  
        } else if (error != null) {  
            // we are not registered, can try again  
            GCMessaging.clearRegistrationId(context);  
            // Registration failed  
            Log.e(TAG, "Registration error " + error);  
            onError(context, error);  
            if ("SERVICE_NOT_AVAILABLE".equals(error)) {  
                long backoffTimeMs = GCMessaging.getBackoff(context);  
                  
                Log.d(TAG, "Scheduling registration retry, backoff = " + backoffTimeMs);  
                Intent retryIntent = new Intent(C2DM_RETRY);  
                PendingIntent retryPIntent = PendingIntent.getBroadcast(context,   
                        0 /*requestCode*/, retryIntent, 0 /*flags*/);  
                  
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);  
                am.set(AlarmManager.ELAPSED_REALTIME,  
                        backoffTimeMs, retryPIntent);  
  
                // Next retry should wait longer.  
                backoffTimeMs *= 2;  
                GCMessaging.setBackoff(context, backoffTimeMs);  
            }   
        } else {  
            try {  
                onRegistered(context, registrationId);  
                GCMessaging.setRegistrationId(context, registrationId); //save in Preferences
            } catch (IOException ex) {  
                Log.e(TAG, "Registration error " + ex.getMessage());  
            }  
        }  
    }  
}  