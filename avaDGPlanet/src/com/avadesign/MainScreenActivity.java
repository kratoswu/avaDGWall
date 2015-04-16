package com.avadesign;

import static android.content.Intent.ACTION_MAIN;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import at.theengine.android.simple_rss2_android.RSSItem;
import at.theengine.android.simple_rss2_android.SimpleRss2Parser;
import at.theengine.android.simple_rss2_android.SimpleRss2ParserCallback;

import com.avadesign.model.ZWaveNode;
import com.avadesign.model.ZWaveNodeValue;
import com.avadesign.service.PollingService;
import com.avadesign.service.WeatherService;
import com.avadesign.task.GetCamDataTask;
import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;
import com.avadesign.v4.frag.AbstractPanelFrag;

/**
 * @author Phoenix
 * @version <pre>
 * 2014-12-03 Phoenix 追加程式啟動時載入 Qualia cam data 功能.
 * </pre>
 * */
public class MainScreenActivity extends FragmentActivity implements LinphoneOnCallStateChangedListener, LinphoneOnMessageReceivedListener,
        LinphoneOnRegistrationStateChangedListener, EcCalibrationListener {

    private static MainScreenActivity instance;

    // https://www.youtube.com/embed/--l1oCcUEoI
    private String webUrlStr = "http://edition.cnn.com";
    // private String webUrlStr = "https://www.youtube.com/embed/--l1oCcUEoI";
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

    private Handler uiHandler;
    private Timer dateTimer;

    private boolean hasRegistered;

    private boolean isOnSecurity;

    private Button secSwhBtn;

    private WebView newsWebView;

    private ZWaveNode alarmNode;

    private BroadcastReceiver warningReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PollingService.HTTP_401)) {
                call401();
            } else if (intent.getAction().equals(PollingService.HTTP_404)) {
                call404();
            } else if (intent.getAction().equals(PollingService.REFRESH_NODE_DATA)) {
                checkAlarm();
            }
        }

    };

    private void call401() {
        runOnUiThread(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub

            }
        });
    }

    private void call404() {
        runOnUiThread(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub

            }
        });
    }

    private void checkAlarm() {
        ArrayList<HashMap<String, Object>> nodeMapList = new ArrayList<HashMap<String, Object>>();
        getAvaApp().refreshNodesList(nodeMapList);
        for (HashMap<String, Object> nodeMap : nodeMapList) {
            ZWaveNode node = new ZWaveNode(nodeMap);

            for (ZWaveNodeValue val : node.value) {
                if (hasAlarm(val)) {
                    alarmNode = node;
                    goToWarningActivity();
                    return;
                }
            }
        }
    }

    private SharedClassApp getAvaApp() {
        return (SharedClassApp) getApplication();
    }

    private void goToWarningActivity() {
        StringBuffer msgBuff = new StringBuffer(alarmNode.name);
        msgBuff.append(getString(R.string.is_warning));

        Intent intent = new Intent(this, WarningActivity.class);
        intent.putExtra("errMsg", msgBuff.toString());

        startActivity(intent);
    }

    private boolean hasAlarm(ZWaveNodeValue zVal) {
        if (zVal.class_c.equalsIgnoreCase("ALARM") && zVal.label.equalsIgnoreCase("Alarm Level")) {
            return !zVal.current.equals("0");
        } else {
            return false;
        }
    }

    private void stopPollingService() {
        Intent intent = new Intent(this, PollingService.class);
        stopService(intent);
    }

    private void startPollingService() {
        Intent intent = new Intent(this, PollingService.class);
        startService(intent);
    }

    private class ContentViewClient extends WebViewClient {

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            return true;
        }

    }

    private TimerTask dateTimeTask = new TimerTask() {

        public void run() {
            uiHandler.post(new Runnable() {

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

    public void switchSecurity(View v) {
        isOnSecurity = !isOnSecurity;
        int imgId = isOnSecurity ? R.drawable.on_old : R.drawable.off_old;
        String lbl = isOnSecurity ? "AWAY" : "AT HOME";
        secSwhBtn.setBackgroundResource(imgId);
        secSwhBtn.setText(lbl);

        /*
         * TODO 根據 button 的狀態來決定啟動或關閉 polling
         * */
        if (isOnSecurity) {
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(PollingService.HTTP_401);
            iFilter.addAction(PollingService.HTTP_404);
            iFilter.addAction(PollingService.REFRESH_NODE_DATA);

            registerReceiver(warningReceiver, iFilter);
            startPollingService();
        } else {
            unregisterReceiver(warningReceiver);
            stopPollingService();
        }
    }

    private String getWeatherCode() {
        return getAvaApp().getWeatherCode();
    }

    private String getTemperature() {
        return getAvaApp().getTemperature();
    }

    protected void onStart() {
        super.onStart();

        SharedClassApp app = (SharedClassApp) getApplication();

        if (!StringUtil.isEmptyString(app.getWeatherCode()) && !StringUtil.isEmptyString(app.getTemperature())) {
            updateWeatherInfo();
        }
    }

    private void updateWeatherInfo() {
        if (tempInfo != null && weatherImgView != null) {
            tempInfo.setText(getTemperature() + " " + '\u00B0' + "C");

            Integer resId = getWeatherImgResource();

            if (resId != null) {
                weatherImgView.setImageResource(resId);
            }
        }
    }

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();

                if (action.equals(WeatherService.LOCATION_ERROR)) {
                    // Toast.makeText(MainScreenActivity.this,
                    // R.string.location_err, Toast.LENGTH_SHORT).show();
                } else if (action.equals(WeatherService.WEATHER_ERROR)) {
                    // Toast.makeText(MainScreenActivity.this,
                    // R.string.weather_err, Toast.LENGTH_SHORT).show();
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

        case KeyEvent.KEYCODE_HOME:
            return false;

        case KeyEvent.KEYCODE_APP_SWITCH:
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
             * 2014-12-03, edited by Phoenix. 啟動時取得 cam data
             */
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

        appPref = getAvaApp().getAppPref();

        // initialize UI
        initWeatherImgView();
        initTempInfo();
        dateInfo = (TextView) findViewById(R.id.dateInfo);
        timeInfo = (TextView) findViewById(R.id.timeInfo);
        weekDayInfo = (TextView) findViewById(R.id.weekDayInfo);
        secSwhBtn = (Button) findViewById(R.id.secSwhBtn);
        String lbl = isOnSecurity ? "AWAY" : "AT HOME";
        secSwhBtn.setText(lbl);

        // TODO Web view
        newsWebView = (WebView) findViewById(R.id.newsWebView);
        newsWebView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int newProgress) {
                setProgress(newProgress * 1000);
            }

        });
        newsWebView.setWebViewClient(new ContentViewClient());
        WebSettings contentSettings = newsWebView.getSettings();
        contentSettings.setLoadsImagesAutomatically(true);
        contentSettings.setJavaScriptEnabled(true);
        contentSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        contentSettings.setPluginState(WebSettings.PluginState.ON);
        newsWebView.clearCache(true);
        // http://www.spiegel.de/
        newsWebView.loadUrl(webUrlStr);

        /*
         * Register Receiver
         */
        IntentFilter itFilter = new IntentFilter();
        itFilter.addAction(WeatherService.REFRESH_WEATHER);
        itFilter.addAction(WeatherService.LOCATION_ERROR);
        itFilter.addAction(WeatherService.WEATHER_ERROR);
        registerReceiver(weatherReceiver, itFilter);

        /*
         * Start weather service
         */
        startService(new Intent(this, WeatherService.class));

        uiHandler = new Handler();
        dateTimer = new Timer();
        dateTimer.schedule(dateTimeTask, 0, 1000);
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

        if (!StringUtil.isEmptyString(getWeatherCode())) {
            Log.v("Weather code", getWeatherCode());
            weatherImgView.setImageResource(getWeatherImgResource());
        } else {
            weatherImgView.setImageResource(R.drawable.weather4);
        }
    }

    private Integer getWeatherImgResource() {
        String code = getWeatherCode();
        boolean containsKey = weatherImgResMap.containsKey(code);

        if (!containsKey) {
            Log.e("Unknown Weather Code:", code);
        }

        return containsKey ? weatherImgResMap.get(code) : R.drawable.weather00;
    }

    protected void onDestroy() {
        Log.e("", "Main destroy...");
        unregisterReceiver(weatherReceiver);
        stopService(new Intent(this, WeatherService.class));
        stopPollingService();

        if (dateTimer != null) {
            dateTimer.cancel();
        }

        super.onDestroy();
    }

    public void onRegistrationStateChanged(RegistrationState state) {
        if (state != RegistrationState.RegistrationOk) {
            if (state == RegistrationState.RegistrationFailed) {
                runOnUiThread(new Runnable() {

                    public void run() {
                        // Toast.makeText(MainScreenActivity.this,
                        // "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public void onMessageReceived(LinphoneAddress from, LinphoneChatMessage message, int id) {
    }

    public void onCallStateChanged(LinphoneCall call, final State state, String message) {
        Log.e("State from MainScn", state.toString());

        if (state == LinphoneCall.State.StreamsRunning) {
            LinphoneManager.getLc().enableSpeaker(true);
        } else if (state == LinphoneCall.State.IncomingReceived) {
            // TODO
            runOnUiThread(new Runnable() {

                public void run() {
                    newsWebView.loadUrl(webUrlStr);
                }
            });
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
        weatherImgResMap.put("33", R.drawable.weather1);

        weatherImgResMap.put("21", R.drawable.weather4);
        weatherImgResMap.put("28", R.drawable.weather4);
        weatherImgResMap.put("29", R.drawable.weather4);

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
    }

    public void goToCall(View v) {
        startActivity(new Intent(this, ContactListActivity.class));
    }

    public void goToCtrl(View v) {
        if (invalidateGatewaySettings()) {
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
