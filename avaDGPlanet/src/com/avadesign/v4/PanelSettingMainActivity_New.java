package com.avadesign.v4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.avadesign.AddNewPnlActivity;
import com.avadesign.MainScreenActivity;
import com.avadesign.R;
import com.avadesign.SettingMainScreenActivity;
import com.avadesign.SharedClassApp;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.PanelItemBean;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.service.PollingService;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;
import com.avadesign.util.PanelDetailHelper;
import com.avadesign.util.PanelListHelper;
import com.avadesign.v4.frag.AbstractPanelTemplateFrag;

public class PanelSettingMainActivity_New extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private Map<String, SceneBean> sceneMap = new HashMap<String, SceneBean>();
    private AvaPref appPref;
    private Map<String, ZWaveNode> deviceNodeMap = new HashMap<String, ZWaveNode>();
    private ProgressDialog waitPop;
    private List<PanelItemBean> pnlItems = new ArrayList<PanelItemBean>();
    private List<PanelDetailBean> pnlDetails = new ArrayList<PanelDetailBean>();
    private int currentIdx;
    private List<AbstractPanelTemplateFrag> frags = new LinkedList<AbstractPanelTemplateFrag>();
    private boolean hadLoadedPnlInfo;
    private List<Map<String, String>> tempData;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();
        }

        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pnl_setting_menu_new, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            /*
             * 2014-11-19, edited by Phoenix.
             * 改成回到設定主畫面.
             * */
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();

            return true;
        } else if (id == R.id.action_remove) {
            // Remove panel
            if (currentIdx < frags.size()) {
                // Display a dialog, when user clicks OK, remove selected panel
                displayRmConfirmDialog();
            }
        } else if (id == R.id.action_add) {
            startActivity(new Intent(this, AddNewPnlActivity.class));
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayRmConfirmDialog() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("Confirm").setMessage(getString(R.string.rm_pnl_confirm)).setCancelable(false);

        builder.setPositiveButton("YES", new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Remove panel
                PanelItemBean b = pnlItems.get(currentIdx);

                if (b != null) {
                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("action", "remove");
                    paramMap.put("id", b.id);
                    LoadPanelListTask pnlTask = new LoadPanelListTask(paramMap);
                    pnlTask.execute(new Void[0]);
                }
            }
        });

        builder.setNegativeButton("NO", new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).create().show();
    }

    public void savePanelInfo() {
        SavePanelDetailTask task = new SavePanelDetailTask();
        task.execute(new Void[0]);
    }

    public void appendTempData(Map<String, String> data) {
        tempData.add(data);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PollingService.REFRESH_NODE_DATA)) {
                update();
            }
        }
    };

    protected void onStart() {
        super.onStart();

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("action", "load");
        LoadPanelListTask task = new LoadPanelListTask(paramMap);
        task.execute(new Void[0]);
    }

    private class SavePanelDetailTask extends AsyncTask<Void, Void, Void> {

        private PanelDetailBean result;

        protected void onPreExecute() {
            runOnUiThread(new Runnable() {

                public void run() {
                    waitPop = new ProgressDialog(PanelSettingMainActivity_New.this);
                    waitPop.setTitle("Save data...");
                    waitPop.show();
                }
            });
        }

        protected void onPostExecute(Void rsult) {
            runOnUiThread(new Runnable() {

                public void run() {
                    AbstractPanelTemplateFrag currentFrag = frags.get(currentIdx);
                    currentFrag.setData(result);
                    currentFrag.refresh(deviceNodeMap, sceneMap);
                    waitPop.dismiss();
                }
            });
        }

        protected Void doInBackground(Void... params) {
            if (tempData != null && tempData.size() > 0) {
                String urlStr = String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip),
                        getAppPrefVal(R.string.key_gateway_port)) + getString(R.string.panel_list);
                String acc = getAppPrefVal(R.string.key_acc);
                String pwd = getAppPrefVal(R.string.key_pwd);
                String xmlSrc = null;

                try {
                    synchronized (tempData) {
                        for (Map<String, String> data : tempData) {
                            xmlSrc = HttpCommunicator.sendCmd(urlStr, data, acc, pwd);
                            Log.i("xml", xmlSrc);
                        }
                    }

                    Document pnlDetailDoc = DocumentHelper.parseText(xmlSrc);
                    result = PanelDetailHelper.getDetailBean(pnlDetailDoc);
                } catch (Exception e) {
                    Log.e(PanelSettingMainActivity_New.class.getSimpleName(), e.getMessage(), e);
                }
            }

            return null;
        }

    }

    private class LoadPanelListTask extends AsyncTask<Void, Void, Void> {

        private Map<String, String> paramMap;

        public LoadPanelListTask(Map<String, String> paramMap) {
            this.paramMap = paramMap;
        }

        protected Void doInBackground(Void... params) {
            hadLoadedPnlInfo = false;
            String urlStr = String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip),
                    getAppPrefVal(R.string.key_gateway_port)) + getString(R.string.panel_list);
            String xmlSource = HttpCommunicator.sendCmd(urlStr, paramMap, getAppPrefVal(R.string.key_acc), getAppPrefVal(R.string.key_pwd));

            try {
                loadPnlInfo(urlStr, xmlSource);
            } catch (Exception e) {
                System.gc();
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }

            return null;
        }

        protected void onPreExecute() {
            runOnUiThread(new Runnable() {

                public void run() {
                    waitPop = new ProgressDialog(PanelSettingMainActivity_New.this, ProgressDialog.STYLE_SPINNER);
                    waitPop.setTitle("Downloading data...");
                    waitPop.show();
                }
            });
        }

        protected void onPostExecute(Void result) {
            runOnUiThread(new Runnable() {

                public void run() {
                    waitPop.dismiss();

                    /*
                     * 2014-11-19, edited by Phoenix.
                     * 面板上有更動的話, 改成直接 refresh 即可.
                     * */
                    refreshPager();
                }
            });
        }
    }

    private void loadPnlInfo(String urlStr, String xmlSource) throws Exception {
        pnlDetails.clear();
        pnlItems.clear();
        frags = new LinkedList<AbstractPanelTemplateFrag>();

        pnlItems.addAll(PanelListHelper.parseListXml(DocumentHelper.parseText(xmlSource)));

        for (PanelItemBean pi : pnlItems) {
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("action", "load");
            paramMap.put("id", pi.id);
            Document pnlDetailDoc = HttpCommunicator.getPanelDetail(urlStr, paramMap, getAppPrefVal(R.string.key_acc),
                    getAppPrefVal(R.string.key_pwd), true);

            PanelDetailBean detailBean = PanelDetailHelper.getDetailBean(pnlDetailDoc);
            pnlDetails.add(detailBean);

            frags.add(PanelTemplateFragFactory.getInstance().getTemplateFrag(detailBean));
        }

        hadLoadedPnlInfo = true;
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
            String acc = getAppPrefVal(R.string.key_acc);
            String pwd = getAppPrefVal(R.string.key_pwd);

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

    protected void onDestroy() {
        unregisterReceiver(receiver);
        stopPollingService();

        System.gc();

        super.onDestroy();
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            getActionBar().removeAllTabs();
        }

        public Fragment getItem(int position) {
            return frags.get(position);
        }

        public int getCount() {
            return frags.size();
        }

        public CharSequence getPageTitle(int position) {
            return frags.get(position).getTitle();
        }

    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_pnl_setting_test);
        appPref = getAvaApp().getAppPref();
        tempData = new ArrayList<Map<String, String>>();

        if (getActionBar() != null) {
            getActionBar().setTitle("");
        }

        viewPager = (ViewPager) findViewById(R.id.testPnlPager);

        startPollingService();

        LoadSceneTask sceneTask = new LoadSceneTask();
        sceneTask.execute(new String[0]);

        // Register broadcast receiver
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(PollingService.HTTP_401);
        iFilter.addAction(PollingService.HTTP_404);
        iFilter.addAction(PollingService.REFRESH_NODE_DATA);
        registerReceiver(receiver, iFilter);
    }

    private void updateDeviceNode() {
        ArrayList<HashMap<String, Object>> contentList = new ArrayList<HashMap<String, Object>>();
        getAvaApp().refreshNodesList(contentList);

        deviceNodeMap = new HashMap<String, ZWaveNode>();

        for (HashMap<String, Object> content : contentList) {
            ZWaveNode node = new ZWaveNode(content);
            deviceNodeMap.put(node.id, node);
        }
    }

    private void update() {
        // Update UI
        if (pagerAdapter != null) {
            updateDeviceNode();

            // Refresh fragments
            if (hadLoadedPnlInfo) {
                for (AbstractPanelTemplateFrag frag : frags) {
                    frag.refresh(deviceNodeMap, sceneMap);
                }
            }
        }
    }

    private void startPollingService() {
        startService(new Intent(this, PollingService.class));
    }

    private void stopPollingService() {
        stopService(new Intent(this, PollingService.class));
    }

    private void refreshPager() {
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        
        /*
         * 2014-11-19, edited by Phoenix.
         * refresh 時要把 pager 裡所有的 view 都移除掉, 這很重要. 不然會造成畫面顯示不正常.
         * */
        viewPager.removeAllViews();
        
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                currentIdx = position;
                getActionBar().setSelectedNavigationItem(position);
                viewPager.setCurrentItem(position);
            }
        });

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            getActionBar().addTab(getActionBar().newTab().setText(pagerAdapter.getPageTitle(i)).setTabListener(this));
        }

        currentIdx = 0;
        viewPager.setCurrentItem(currentIdx);
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        currentIdx = tab.getPosition();
        viewPager.setCurrentItem(currentIdx);
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

}
