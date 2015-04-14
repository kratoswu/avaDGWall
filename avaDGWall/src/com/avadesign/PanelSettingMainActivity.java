package com.avadesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.PanelItemBean;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.service.PollingService;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;
import com.avadesign.util.PanelDetailHelper;
import com.avadesign.util.PanelListHelper;
import com.avadesign.v4.frag.PanelDetailFrag;

public class PanelSettingMainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager pager;
    private PageAdapter pageAdapter;
    private List<PanelItemBean> pnlItems;
    private List<PanelDetailBean> pnlDetails;
    private AvaPref appPref;
    private Map<String, SceneBean> sceneMap;
    private String acc;
    private String pwd;
    private ProgressDialog waitPop;
    private Receiver receiver = new Receiver();
    private Map<String, ZWaveNode> deviceNodeMap;
    private boolean hasInitiated;
    private List<Map<String, String>> tempData = new ArrayList<Map<String, String>>();
    private int currentIdx;

    public void rmPanel(String pnlId) {
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("action", "remove");
        paramMap.put("id", pnlId);
        LoadPanelListTask pnlTask = new LoadPanelListTask(paramMap);
        pnlTask.execute(new Void[0]);
    }

    protected void onStart() {
        super.onStart();

        // Panel list.
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("action", "load");
        LoadPanelListTask pnlTask = new LoadPanelListTask(paramMap);
        pnlTask.execute(new Void[0]);
    }

    private class SavePanelDetailTask extends AsyncTask<Map, Void, Void> {

        private AvaPref appPref;
        private SharedClassApp app;

        public SavePanelDetailTask(SharedClassApp avaApp) {
            appPref = avaApp.getAppPref();
            app = avaApp;
        }

        private String getValue(String key) {
            return appPref.getValue(key);
        }

        private String getString(int resId) {
            return app.getString(resId);
        }

        @SuppressWarnings("unchecked")
        protected Void doInBackground(Map... params) {
            if (params != null && params.length > 0) {
                runOnUiThread(new Runnable() {

                    public void run() {
                        waitPop = new ProgressDialog(PanelSettingMainActivity.this);
                        waitPop.setTitle("Save data...");
                        waitPop.show();
                    }
                });

                String urlStr = String.format(getString(R.string.local_url_pattern), getValue(getString(R.string.key_gateway_ip)),
                        getValue(getString(R.string.key_gateway_port))) + getString(R.string.panel_list);
                String acc = getValue(getString(R.string.key_acc));
                String pwd = getValue(getString(R.string.key_pwd));

                Log.i(getTag(), "URL: [" + urlStr + "]");
                Log.i(getTag(), "acc: [" + acc + "]");
                Log.i(getTag(), "pwd: [" + pwd + "]");

                for (Map paramMap : params) {
                    HttpCommunicator.send(urlStr, ((Map<String, String>) paramMap), acc, pwd, true);
                }
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            runOnUiThread(new Runnable() {

                public void run() {
                    if (waitPop != null) {
                        waitPop.dismiss();
                    }

                    startActivity(new Intent(PanelSettingMainActivity.this, MainScreenActivity.class));
                }
            });
        }

        private String getTag() {
            return getClass().getSimpleName();
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            startActivity(new Intent(this, MainScreenActivity.class));

            return true;
        } else if (id == R.id.action_save) {
            SavePanelDetailTask saveTask = new SavePanelDetailTask(getAvaApp());
            saveTask.execute(tempData.toArray(new Map[tempData.size()]));

            return true;
        } else if (id == R.id.action_add) {
            startActivity(new Intent(this, AddNewPnlActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pnl_setting_menu, menu);
        return true;
    }

    public void appendTempData(Map<String, String> data) {
        tempData.add(data);
    }

    private void updateDeviceNode() {
        ArrayList<HashMap<String, Object>> contentList = new ArrayList<HashMap<String, Object>>();
        ((SharedClassApp) getApplication()).refreshNodesList(contentList);

        deviceNodeMap = new HashMap<String, ZWaveNode>();

        for (HashMap<String, Object> content : contentList) {
            ZWaveNode node = new ZWaveNode(content);
            deviceNodeMap.put(node.id, node);
        }
    }

    private void update() {
        if (hasInitiated && pageAdapter != null) {
            // update UI
            updateDeviceNode();
            SparseArray<PanelDetailFrag> frags = pageAdapter.getAllFrags();
            for (int i = 0; i < frags.size(); i++) {
                frags.get(i).updateDeviceList(deviceNodeMap);
                frags.get(i).updateSceneMap(sceneMap);
            }
        }
    }

    private class Receiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PollingService.REFRESH_NODE_DATA)) {
                update();
            }
        }

    }

    private void startPollingService() {
        startService(new Intent(this, PollingService.class));
    }

    private void stopPollingService() {
        stopService(new Intent(this, PollingService.class));
    }

    private void initPager() {
        pageAdapter = new PageAdapter(getSupportFragmentManager());

        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        pager.setAdapter(pageAdapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                currentIdx = position;
                getActionBar().setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < pageAdapter.getCount(); i++) {
            getActionBar().addTab(getActionBar().newTab().setText(pageAdapter.getPageTitle(i)).setTabListener(this));
        }

        currentIdx = 0;
        pager.setCurrentItem(currentIdx);
    }

    protected void onDestroy() {
        unregisterReceiver(receiver);
        stopPollingService();
        pnlItems = new ArrayList<PanelItemBean>();
        pnlDetails = new ArrayList<PanelDetailBean>();
        sceneMap = new HashMap<String, SceneBean>();
        System.gc();
        hasInitiated = false;
        super.onDestroy();
    }

    private class LoadPanelListTask extends AsyncTask<Void, Void, Void> {

        private Map<String, String> paramMap;

        public LoadPanelListTask(Map<String, String> paramMap) {
            this.paramMap = paramMap;
        }

        protected Void doInBackground(Void... params) {
            runOnUiThread(new Runnable() {

                public void run() {
                    waitPop = new ProgressDialog(PanelSettingMainActivity.this, ProgressDialog.STYLE_SPINNER);
                    waitPop.setTitle("Downloading data...");
                    waitPop.show();
                }
            });

            // Loading panel list
            String urlStr = String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip),
                    getAppPrefVal(R.string.key_gateway_port)) + getString(R.string.panel_list);

            String xmlSource = HttpCommunicator.sendCmd(urlStr, paramMap, acc, pwd);

            try {
                pnlDetails.clear();
                pnlItems.clear();

                pnlItems.addAll(PanelListHelper.parseListXml(DocumentHelper.parseText(xmlSource)));

                for (PanelItemBean pi : pnlItems) {
                    paramMap = new HashMap<String, String>();
                    paramMap.put("action", "load");
                    paramMap.put("id", pi.id);
                    Document pnlDetailDoc = HttpCommunicator.getPanelDetail(urlStr, paramMap, acc, pwd, true);

                    pnlDetails.add(PanelDetailHelper.getDetailBean(pnlDetailDoc));
                }
            } catch (Exception e) {
                System.gc();
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }

            runOnUiThread(new Runnable() {

                public void run() {
                    waitPop.dismiss();

                    // Refresh screen.
                    initPager();
                    pager.setVisibility(View.VISIBLE);
                    hasInitiated = true;
                }
            });

            return null;
        }

    }

    private SharedClassApp getAvaApp() {
        return (SharedClassApp) getApplication();
    }

    private String getAppPrefVal(int resId) {
        return appPref.getValue(getString(resId));
    }

    private String getGatewayPath() {
        return String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip), getAppPrefVal(R.string.key_gateway_port));
    }

    private class LoadSceneTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
            String urlStr = getGatewayPath() + "/" + getString(R.string.scenepost);
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("fun", "load");

            List<HashMap<String, String>> result = HttpCommunicator.getSceneList(urlStr, paramMap, acc, pwd, true);
            sceneMap.clear();

            if (result != null) {
                for (HashMap<String, String> element : result) {
                    SceneBean scene = new SceneBean();
                    scene.id = element.get("id");
                    scene.label = element.get("label");

                    sceneMap.put(scene.id, scene);
                }
            }

            return true;
        }

    }

    private class PageAdapter extends FragmentPagerAdapter {

        private SparseArray<String> pageTitles;
        private SparseArray<PanelDetailFrag> frags;

        public PageAdapter(FragmentManager fm) {
            super(fm);
            getActionBar().removeAllTabs();

            pageTitles = new SparseArray<String>();
            frags = new SparseArray<PanelDetailFrag>();

            if (pnlDetails != null || pnlDetails.size() > 0) {
                for (int i = 0; i < pnlDetails.size(); i++) {
                    PanelDetailBean bean = pnlDetails.get(i);
                    pageTitles.append(i, bean.label);
                    frags.append(i, new PanelDetailFrag(bean));
                }
            }
        }

        public SparseArray<PanelDetailFrag> getAllFrags() {
            return frags;
        }

        public CharSequence getPageTitle(int position) {
            return pageTitles.get(position);
        }

        public Fragment getItem(int position) {
            return frags.get(position);
        }

        public int getCount() {
            return frags.size();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_setting_main);

        if (getActionBar() != null) {
            getActionBar().setTitle("");
        }

        pnlItems = new ArrayList<PanelItemBean>();
        pnlDetails = new ArrayList<PanelDetailBean>();
        sceneMap = new HashMap<String, SceneBean>();

        // App preferences.
        appPref = getAvaApp().getAppPref();
        acc = appPref.getValue(getString(R.string.key_acc));
        pwd = appPref.getValue(getString(R.string.key_pwd));

        // UI initialize.
        pager = (ViewPager) findViewById(R.id.panelPager);
        pager.setVisibility(View.INVISIBLE);

        // Start polling service
        startPollingService();

        // Load all scenes.
        LoadSceneTask sceneTask = new LoadSceneTask();
        sceneTask.execute(new String[0]);

        // Register broadcast receiver
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(PollingService.HTTP_401);
        iFilter.addAction(PollingService.HTTP_404);
        iFilter.addAction(PollingService.REFRESH_NODE_DATA);
        registerReceiver(receiver, iFilter);
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        currentIdx = tab.getPosition();
        pager.setCurrentItem(currentIdx);
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }

}
