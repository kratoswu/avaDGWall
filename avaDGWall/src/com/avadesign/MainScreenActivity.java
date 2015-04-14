package com.avadesign;

import static android.content.Intent.ACTION_MAIN;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.linphone.LinphoneManager;
import org.linphone.LinphoneManager.EcCalibrationListener;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;
import org.linphone.LinphoneSimpleListener.LinphoneOnCallStateChangedListener;
import org.linphone.LinphoneSimpleListener.LinphoneOnMessageReceivedListener;
import org.linphone.LinphoneSimpleListener.LinphoneOnRegistrationStateChangedListener;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneCore.EcCalibratorStatus;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneFriend;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.service.WeatherService;
import com.avadesign.task.GetCamDataTask;
import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;

/**
 * @author Phoenix
 * @version
 * <pre>
 * 2014-12-03 Phoenix 追加程式啟動時載入 Qualia cam data 功能.
 * </pre>
 * */
public class MainScreenActivity extends FragmentActivity implements LinphoneOnCallStateChangedListener, LinphoneOnMessageReceivedListener,
        LinphoneOnRegistrationStateChangedListener, EcCalibrationListener {

    private static MainScreenActivity instance;

    private Handler mHandler;
    private ServiceWaitThread mThread;
    private Map<EcCalibratorStatus, String> statusMsgMap = new HashMap<EcCalibratorStatus, String>();
    
    private AvaPref appPref;
    private ImageView weatherImgView;
    private TextView tempInfo;
    
    private TextView dateInfo;
    private TextView timeInfo;
    private TextView weekDayInfo;
    
    private Map<String, Integer> weatherImgResMap = new HashMap<String, Integer>();
    
    private Handler dateTimeHandler;
    private Timer dateTimer;
    
    private boolean hasRegistered;
    
    private Button securityBtn;
    private Button doorBtn;
    private Button nvrBtn;
    private Button avaCallBtn;
    private Button ctrlBtn;
    
    private TimerTask dateTimeTask = new TimerTask() {
        
        public void run() {
            dateTimeHandler.post(new Runnable() {
                
                public void run() {
                    Date date = new Date();
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH : mm");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    SimpleDateFormat weekDayFormat = new SimpleDateFormat("EEEE");
                    
                    timeInfo.setText(timeFormat.format(date));
                    dateInfo.setText(dateFormat.format(date));
                    weekDayInfo.setText(weekDayFormat.format(date));
                }
            });
        }
    };
    
    private String getWeatherCode() {
        return ((SharedClassApp) getApplication()).getWeatherCode();
    }
    
    private String getTemperature() {
        return ((SharedClassApp) getApplication()).getTemperature();
    }
    
    protected void onStart() {
        super.onStart();
        
        SharedClassApp app = (SharedClassApp) getApplication();
        
        if (!StringUtil.isEmptyString(app.getWeatherCode()) && !StringUtil.isEmptyString(app.getTemperature())) {
            updateWeatherInfo();
        }
        
        /*
         * TODO
         * 功能 button 要改成依設定決定顯示與否.
         * */
        displayFuncBtns();
    }

    private void updateWeatherInfo() {
        if (tempInfo != null && weatherImgView != null) {
            tempInfo.setText(getTemperature() + " " + '\u00B0' + "C");
            
            Integer resId = weatherImgResMap.get(getWeatherCode());
            
            if (resId != null) {
                weatherImgView.setImageResource(resId);
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();

                if (action.equals(WeatherService.LOCATION_ERROR)) {
//                    Toast.makeText(MainScreenActivity.this, R.string.location_err, Toast.LENGTH_SHORT).show();
                } else if (action.equals(WeatherService.WEATHER_ERROR)) {
//                    Toast.makeText(MainScreenActivity.this, R.string.weather_err, Toast.LENGTH_SHORT).show();
                } else if (action.equals(WeatherService.REFRESH_WEATHER)) {
                    updateWeatherInfo();
                }
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        }

    };
    
    private boolean isEmptyDpList() {
        Set<String> dpSet = appPref.getValueSet(getString(R.string.key_dplist));
        return dpSet == null || dpSet.size() == 0;
    }

    private boolean invalidateGatewaySettings() {
        if (appPref != null) {
            boolean result = false;

            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_gateway_ip)));
            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_gateway_port)));
            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_acc)));
            result = StringUtil.isEmptyString(appPref.getValue(getString(R.string.key_pwd)));

            return result;
        }

        return true;
    }

    private void initMsgMap() {
        statusMsgMap.clear();
        statusMsgMap.put(EcCalibratorStatus.Done, "Done");
        statusMsgMap.put(EcCalibratorStatus.DoneNoEcho, "No echo");
        statusMsgMap.put(EcCalibratorStatus.Failed, "Failed");
    }

    public void onNotifyPresenceReceived(LinphoneFriend friend) {
    }

    public void onNewSubscriptionRequestReceived(LinphoneFriend friend, String sipUri) {
    }

    public void displayCustomToast(final String message, final int duration) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

                TextView toastText = (TextView) layout.findViewById(R.id.toastMessage);
                toastText.setText(message);

                final Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(duration);
                toast.setView(layout);
                toast.show();
            }
        });
    }

    public static final boolean isInstanciated() {
        return instance != null;
    }

    public static MainScreenActivity getInstance() {
        if (instance == null) {
            throw new RuntimeException("Main activity not instantiated yet");
        }

        return instance;
    }

    protected void onResume() {
        super.onResume();

        if (!LinphoneService.isReady()) {
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
        }

        LinphoneManager.removeListener(this);
        LinphoneManager.addListener(this);
    }

    protected void onServiceReady() {
        try {
            LinphoneManager.getInstance().startEcCalibration(this);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onServiceReady();
                }
            });
            mThread = null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            return false;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Log.w("Main.onCreate()", "Invoked...");
        
        if (!hasRegistered) {
            /*
             * 2014-12-03, edited by Phoenix.
             * 啟動時取得 cam data
             * */
            GetCamDataTask getCamDataTask = new GetCamDataTask(this);
            getCamDataTask.execute(new Void[0]);
            hasRegistered = true;
        }
        
        // Hide action bar.
        ActionBar actBar = getActionBar();

        if (actBar != null) {
            actBar.hide();
        }

        mHandler = new Handler();
        
        instance = this;

        if (!LinphoneService.isReady()) {
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
            mThread = new ServiceWaitThread();
            mThread.start();
        }

        initMsgMap();
        
        initWeatherImgResMap();
        
        appPref = ((SharedClassApp) getApplication()).getAppPref();
        
        // initialize UI
        initWeatherImgView();
        initTempInfo();
        dateInfo = (TextView) findViewById(R.id.dateInfo);
        timeInfo = (TextView) findViewById(R.id.timeInfo);
        weekDayInfo = (TextView) findViewById(R.id.weekDayInfo);

        /*
         * Register Receiver
         */
        IntentFilter itFilter = new IntentFilter();
        itFilter.addAction(WeatherService.REFRESH_WEATHER);
        itFilter.addAction(WeatherService.LOCATION_ERROR);
        itFilter.addAction(WeatherService.WEATHER_ERROR);
        registerReceiver(receiver, itFilter);

        /*
         * Start weather service
         */
        startService(new Intent(this, WeatherService.class));
        
        dateTimeHandler = new Handler();
        dateTimer = new Timer();
        dateTimer.schedule(dateTimeTask, 0, 1000);
    }

    private void displayFuncBtns() {
        initSecurityBtn();
        initDoorBtn();
        initNvrBtn();
        initAvaCallBtn();
        initCtrlBtn();
    }

    private void initCtrlBtn() {
        ctrlBtn = (Button) findViewById(R.id.ctrlBtn);
        int visibility = appPref.getBooleanVal(getString(R.string.key_func_ctrl)) ? View.VISIBLE : View.GONE;
        ctrlBtn.setVisibility(visibility);
    }

    private void initAvaCallBtn() {
        avaCallBtn = (Button) findViewById(R.id.avaCallBtn);
        int visibility = appPref.getBooleanVal(getString(R.string.key_func_call)) ? View.VISIBLE : View.GONE;
        avaCallBtn.setVisibility(visibility);
    }

    private void initNvrBtn() {
        nvrBtn = (Button) findViewById(R.id.nvrBtn);
        int visibility = appPref.getBooleanVal(getString(R.string.key_func_nvr)) ? View.VISIBLE : View.GONE;
        nvrBtn.setVisibility(visibility);
    }

    private void initDoorBtn() {
        doorBtn = (Button) findViewById(R.id.doorBtn);
        int visibility = appPref.getBooleanVal(getString(R.string.key_func_door)) ? View.VISIBLE : View.GONE;
        doorBtn.setVisibility(visibility);
    }

    private void initSecurityBtn() {
        securityBtn = (Button) findViewById(R.id.securityBtn);
        int visibility = appPref.getBooleanVal(getString(R.string.key_func_janitor)) ? View.VISIBLE : View.GONE;
        securityBtn.setVisibility(visibility);
    }

    private void initTempInfo() {
        tempInfo = (TextView) findViewById(R.id.tempInfo);
        
        if (!StringUtil.isEmptyString(getTemperature())) {
            tempInfo.setText(getTemperature() + " " + '\u00B0' + "C");
        } else {
            tempInfo.setText("20" + '\u00B0' + "C");
        }
    }

    private void initWeatherImgView() {
        weatherImgView = (ImageView) findViewById(R.id.weatherImgView);
        String weatherCode = getWeatherCode();
        
        if (!StringUtil.isEmptyString(weatherCode)) {
            weatherImgView.setImageResource(weatherImgResMap.get(weatherCode));
        } else {
            weatherImgView.setImageResource(R.drawable.weather4);
        }
    }

    protected void onDestroy() {
        Log.e("", "Main destroy...");
        unregisterReceiver(receiver);
        stopService(new Intent(this, WeatherService.class));
        super.onDestroy();
    }

    public void onRegistrationStateChanged(RegistrationState state) {
        if (state != RegistrationState.RegistrationOk) {
            if (state == RegistrationState.RegistrationFailed) {
                runOnUiThread(new Runnable() {
                    
                    public void run() {
//                        Toast.makeText(MainScreenActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public void onMessageReceived(LinphoneAddress from, LinphoneChatMessage message, int id) {
    }
    
    public void onCallStateChanged(LinphoneCall call, final State state, String message) {
        if (state == LinphoneCall.State.StreamsRunning) {
            LinphoneManager.getLc().enableSpeaker(true);
        }

        Log.w("State", state.toString());
    }

    public void onEcCalibrationStatus(final EcCalibratorStatus status, int delayMs) {
        runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(MainScreenActivity.this, "Status: " + statusMsgMap.get(status), Toast.LENGTH_LONG).show();
            }
        });

        LinphonePreferences.instance().setEchoCancellation(status != EcCalibratorStatus.DoneNoEcho);
        LinphonePreferences.instance().firstLaunchSuccessful();
    }
    
    private void initWeatherImgResMap() {
        weatherImgResMap.put("1", R.drawable.weather3);
        weatherImgResMap.put("2", R.drawable.weather3);
        weatherImgResMap.put("5", R.drawable.weather3);
        weatherImgResMap.put("6", R.drawable.weather3);
        weatherImgResMap.put("7", R.drawable.weather3);
        weatherImgResMap.put("8", R.drawable.weather3);
        weatherImgResMap.put("9", R.drawable.weather3);
        weatherImgResMap.put("10", R.drawable.weather3);
        weatherImgResMap.put("11", R.drawable.weather3);
        weatherImgResMap.put("12", R.drawable.weather3);
        
        weatherImgResMap.put("30", R.drawable.weather1);
        weatherImgResMap.put("34", R.drawable.weather1);
        
        weatherImgResMap.put("21", R.drawable.weather4);
        weatherImgResMap.put("28", R.drawable.weather4);
        
        weatherImgResMap.put("20", R.drawable.weather2);
        weatherImgResMap.put("26", R.drawable.weather2);
        weatherImgResMap.put("27", R.drawable.weather2);
        
        weatherImgResMap.put("3", R.drawable.weather8);
        weatherImgResMap.put("4", R.drawable.weather8);
        weatherImgResMap.put("17", R.drawable.weather8);
        weatherImgResMap.put("18", R.drawable.weather8);
        
        weatherImgResMap.put("13", R.drawable.weather10);
        weatherImgResMap.put("14", R.drawable.weather10);
        weatherImgResMap.put("15", R.drawable.weather10);
        weatherImgResMap.put("16", R.drawable.weather10);
    }
    
    public void callSecurity(View v) {
        String sipID = appPref.getValue(getString(R.string.key_security_sip));
        Intent it = new Intent(this, AvaCallActivity.class);
        it.putExtra("sipID", sipID);
        it.putExtra("isVideoCall", true);
        
        startActivity(it);
        
        // TODO 這段是用來測試調整麥克風音量的 code.
//        LinphonePreferences linP = LinphonePreferences.instance();
//        
//        if (linP.getMicGain() <= 1f) {
//            Log.e("setMicGain", "4");
//            linP.setMicGain(10f);
//        } else {
//            Log.e("setMicGain", "1");
//            linP.setMicGain(0.1f);
//        }
    }
    
    public void goToCall(View v) {
        startActivity(new Intent(this, ContactListActivity.class));
    }

    public void goToCtrl(View v) {
        if(invalidateGatewaySettings()) {
            Toast.makeText(MainScreenActivity.this, R.string.empty_pref, Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(MainScreenActivity.this, ControlMainActivity.class));
        }
    }
    
    public void goToNVR(View v) {
        startActivity(new Intent(this, NVRActivity.class));
    }

    public void goToSetting(View v) {
        startActivity(new Intent(MainScreenActivity.this, MaintainLoginActivity.class));
    }

    public void goToDP(View v) {
        if (appPref == null) {
            Toast.makeText(MainScreenActivity.this, R.string.empty_dp_list, Toast.LENGTH_LONG).show();
        } else if (isEmptyDpList()) {
            Toast.makeText(MainScreenActivity.this, R.string.empty_dp_list, Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(MainScreenActivity.this, DoorActivity.class));
        }
    }

}
