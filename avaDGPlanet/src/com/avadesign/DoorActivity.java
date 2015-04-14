package com.avadesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.linphone.LinphoneManager;
import org.linphone.LinphoneManager.EcCalibrationListener;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneSimpleListener.LinphoneOnCallStateChangedListener;
import org.linphone.LinphoneUtils;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.EcCalibratorStatus;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.avadesign.util.AvaPref;
import com.avadesign.v4.frag.DoorFrag;

public class DoorActivity extends FragmentActivity implements ActionBar.TabListener, EcCalibrationListener, LinphoneOnCallStateChangedListener,
        LinphoneChatMessage.StateListener {

    private ViewPager pager;

    private PagerAdapter pageAdapter;

    private Map<String, DoorFrag> monitorMap = new LinkedHashMap<String, DoorFrag>();

    private Map<String, DoorFrag> monitorMap2 = new LinkedHashMap<String, DoorFrag>();

    private Map<EcCalibratorStatus, String> statusMsgMap = new HashMap<EcCalibratorStatus, String>();

    private LinphoneCall call;

    private int currentItemIdx;

    private AvaPref appPref;
    
    private SparseArray<Fragment> frags = new SparseArray<Fragment>();
    
    private int idleTime;
    
    private Handler finishHandler = new Handler();

    private Timer idleTimer;
    
    private MediaPlayer mPlayer;
    
    private TimerTask idleTask = new TimerTask() {
        
        public void run() {
            if (!isInCall()) {
                if (getIdleTime() > 0) {
                    setIdleTime(getIdleTime() - 1);
                } else {
                    finishHandler.post(new Runnable() {
                        
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        }
    };
    
    private LinphoneCall.State currentState;

    private boolean isInCall() {
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        
        if (lc != null) {
            return lc.isIncall();
        } else {
            return false;
        }
    }
    
    public LinphoneCall getCall() {
        return call;
    }

    public void openDoor(String sipUrlStr) {
        new OpenDoorTask(sipUrlStr).execute(new Void[0]);
    }

    private class OpenDoorTask extends AsyncTask<Void, Void, Void> {

        private static final String CMD_DOOR_UNLOCK = "doorunlock";
        private String sipUrlStr;

        public OpenDoorTask (String sipUrlStr) {
            this.sipUrlStr = sipUrlStr;
        }

        protected Void doInBackground(Void... params) {
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();

            if (lc != null) {
                LinphoneChatRoom chatRoom = lc.getOrCreateChatRoom(sipUrlStr);

                // Send message
                chatRoom.sendMessage(CMD_DOOR_UNLOCK);
                
                if (getIdleTime() > 0) {
                    setIdleTime(10);
                }
            }

            return null;
        }

    }

    private List<JSONObject> getDpList() throws Exception {
        List<JSONObject> result = new ArrayList<JSONObject>();
        Set<String> dpVals = getValueSet(R.string.key_dplist);

        if (dpVals != null && dpVals.size() > 0) {
            for (String s : dpVals) {
                JSONObject obj = new JSONObject(s);
                result.add(obj);
            }
        }

        return result;
    }

    private Set<String> getValueSet(int resId) {
        return appPref.getValueSet(getString(resId));
    }

    protected void onDestroy() {
        currentState = null;
        
        if (idleTimer != null) {
            idleTimer.cancel();
        }
        
        LinphoneManager.removeListener(this);
        LinphoneManager.getLc().terminateAllCalls();

        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
    }

    public void answer() {
        if (call != null) {
            Log.e("answer", call.getState().toString());
            LinphoneCallParams params = LinphoneManager.getLc().createDefaultCallParameters();
            params.enableLowBandwidth(!LinphoneUtils.isHightBandwidthConnection(this));

            Log.e("accept", "");
            LinphoneManager.getInstance().acceptCallWithParams(call, params);
            
            Log.e("answer end", "");
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LinphoneManager.getLc().terminateAllCalls();
//            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }

        return true;
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private SparseArray<String> pageTitles = new SparseArray<String>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);

            getActionBar().removeAllTabs();

            synchronized (pageTitles) {
                pageTitles.clear();
            }

            synchronized (frags) {
                frags.clear();
            }

            try {
                List<JSONObject> dpList = getDpList();

                for (int i = 0; i < dpList.size(); i++) {
                    JSONObject dp = dpList.get(i);
                    pageTitles.append(i, dp.getString(getString(R.string.key_dp_label)));

                    String url = "http://" + dp.getString(getString(R.string.key_dp_ip)) + "/image.cgi";
                    String acc = dp.getString(getString(R.string.key_dp_acc));
                    String pwd = dp.getString(getString(R.string.key_dp_pwd));
                    String sipId = dp.getString(getString(R.string.key_dp_sip));

                    DoorFrag frag = new DoorFrag(url, acc, pwd, 5060, DoorActivity.this);
                    frag.setSequence(i);
                    frags.append(i, frag);

                    monitorMap.put(dp.getString("ip"), frag);
                    monitorMap2.put(sipId, frag);
                }
            } catch (Exception e) {
                Log.e("Loading Door Phone Error", e.getMessage(), e);
            }
        }

        public CharSequence getPageTitle(int position) {
            return pageTitles.get(position);
        }

        public Fragment getItem(int position) {
            if (position < frags.size()) {
//                Log.e("", "currentIdx: " + currentItemIdx + ", position: " + position);
//                ((DoorFrag) frags.get(position)).setCurrentFrag(currentItemIdx == position);
                return frags.get(position);
            } else {
                return null;
            }
        }

        public int getCount() {
            return frags.size();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);
        
        // TODO
//        LinphonePreferences mPrefs = LinphonePreferences.instance();
//        mPrefs.enableVideo(false);
//        mPrefs.setInitiateVideoCall(false);
//        mPrefs.setAutomaticallyAcceptVideoRequests(false);

        appPref = ((SharedClassApp) getApplication()).getAppPref();

        initMsgMap();

        pager = (ViewPager) findViewById(R.id.doorPager);
        initPager();

        LinphoneManager.addListener(this);

        /*
         * get current call
         */
        if (LinphoneManager.getLcIfManagerNotDestroyedOrNull() != null) {
            List<LinphoneCall> calls = LinphoneUtils.getLinphoneCalls(LinphoneManager.getLc());

            for (LinphoneCall call : calls) {
                if (call.getState() == LinphoneCall.State.IncomingReceived) {
                    this.call = call;
                    break;
                }
            }

            if (call != null) {
                startRing();
                String sipAddr = call.getRemoteAddress().asString();
                Log.e("remote addr", sipAddr);
                boolean isSip = sipAddr.contains("@");
                String key = isSip ? sipAddr.substring(sipAddr.indexOf(":") + 1, sipAddr.indexOf("@")) : sipAddr.substring(sipAddr.indexOf(":") + 1);
                Map<String, DoorFrag> dpData = isSip ? monitorMap2 : monitorMap;
                Log.e("key", key);

                if (dpData.containsKey(key)) {
                    pageAdapter.notifyDataSetChanged();
                    DoorFrag doorFrag = dpData.get(key);
                    currentItemIdx = doorFrag.getSequence();
                    LinphoneManager.getInstance().changeStatusToOnThePhone();
                } else {
                    LinphoneManager.getLc().terminateAllCalls();
                    Toast.makeText(this, sipAddr + " does not exist.", Toast.LENGTH_LONG).show();
                    setIdleTime(0);
                    return;
                }
            } else {
                setIdleTime(180);
            }
        } else {
            currentItemIdx = 0;
            setIdleTime(180);
        }

        pager.setCurrentItem(currentItemIdx);
        idleTimer = new Timer();
        idleTimer.schedule(idleTask, 0, 1000);
    }

    private void initMsgMap() {
        statusMsgMap.clear();
        statusMsgMap.put(EcCalibratorStatus.Done, "Done");
        statusMsgMap.put(EcCalibratorStatus.DoneNoEcho, "No echo");
        statusMsgMap.put(EcCalibratorStatus.Failed, "Failed");
    }

    private void initPager() {
        pageAdapter = new PagerAdapter(getSupportFragmentManager());

        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        pager.setAdapter(pageAdapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            public void onPageSelected(int position) {
//                Toast.makeText(DoorActivity.this, "Page changed", Toast.LENGTH_SHORT).show();
                getActionBar().setSelectedNavigationItem(position);
                
                if (currentState == LinphoneCall.State.StreamsRunning) {
                    /*
                     * 切換頁時的掛斷改在這裡處理, 以免在對講畫面時收到 call 後就自己掛斷.
                     * */
                    LinphoneManager.getLcIfManagerNotDestroyedOrNull().terminateAllCalls();
                    setIdleTime(180);
                }
                
                currentItemIdx = position;
                pager.setCurrentItem(position);
                refreshFragImg();
            }

        });

        for (int i = 0; i < pageAdapter.getCount(); i++) {
            getActionBar().addTab(getActionBar().newTab().setText(pageAdapter.getPageTitle(i)).setTabListener(this));
        }

        // pager.setCurrentItem(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.door, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        currentItemIdx = tab.getPosition();
        pager.setCurrentItem(tab.getPosition());
        refreshFragImg();
    }

    private void refreshFragImg() {
        for (int i = 0; i < frags.size(); i++) {
            ((DoorFrag) frags.get(i)).setCurrentFrag(currentItemIdx == i);
        }
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    public void onEcCalibrationStatus(final EcCalibratorStatus status, int delayMs) {
        runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(DoorActivity.this, "Status: " + statusMsgMap.get(status), Toast.LENGTH_LONG).show();
            }
        });

        LinphonePreferences.instance().setEchoCancellation(status != EcCalibratorStatus.DoneNoEcho);
        LinphonePreferences.instance().firstLaunchSuccessful();
    }

    public void onCallStateChanged(LinphoneCall call, final State state, String message) {
        Log.e("State", state.toString());
        currentState = state;
        
        if (state == LinphoneCall.State.CallEnd) {
            setIdleTime(0);
            stopRing();
        } if (state == LinphoneCall.State.StreamsRunning) {
            stopRing();
        }
    }

    private void startRing() {
        Log.e("Start Ring", "");
        mPlayer = MediaPlayer.create(DoorActivity.this, R.raw.planet);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            
            public void onCompletion(MediaPlayer mp) {
                mPlayer.start();
            }
        });
        
        mPlayer.setLooping(false);
        mPlayer.start();
    }

    private void stopRing() {
        Log.e("Stop Ring", "");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void onLinphoneChatMessageStateChanged(LinphoneChatMessage msg, org.linphone.core.LinphoneChatMessage.State state) {
        // Do nothing...
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

}
