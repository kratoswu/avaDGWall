package com.avadesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import android.annotation.SuppressLint;
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
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.PanelItemBean;
import com.avadesign.service.PollingService;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;
import com.avadesign.util.PanelDetailHelper;
import com.avadesign.util.PanelListHelper;
import com.avadesign.v4.PanelFragFactory;
import com.avadesign.v4.frag.AbstractPanelFrag;

@SuppressLint("NewApi")
public class ControlMainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager mViewPager;

    private AppSectionsPagerAdapter pageAdapter;

    private BroadcastReceiver receiver = new Receiver();

    private ProgressDialog waitPop;

    private List<AbstractPanelFrag> pnlFrags = new ArrayList<AbstractPanelFrag>();
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.door, menu);
        return true;
    }
    
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }

        return true;
    }

    private void stopPollingService() {
        Intent intent = new Intent(this, PollingService.class);
        stopService(intent);
    }

    private void startPollingService() {
        Intent intent = new Intent(this, PollingService.class);
        startService(intent);
    }

    private class LoadPanelInfoTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {
            try {
                loadPanelInfo();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPreExecute() {
            runOnUiThread(new Runnable() {

                public void run() {
                    waitPop = new ProgressDialog(ControlMainActivity.this, ProgressDialog.STYLE_SPINNER);
                    waitPop.setTitle("Downloading data...");
                    waitPop.show();
                }
            });
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                runOnUiThread(new Runnable() {

                    public void run() {
                        waitPop.dismiss();

                        // Refresh screen.
                        initPager();
                        mViewPager.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                // TODO Show error message
                runOnUiThread(new Runnable() {

                    public void run() {
                        AlertDialog.Builder builder = new Builder(ControlMainActivity.this);
                        builder.setTitle("Error").setMessage(R.string.connect_error).setCancelable(false);
                        builder.setNegativeButton("OK", new OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
                    }
                });
            }
        }

    }

    private void loadPanelInfo() throws Exception {
        // Loading panel list
        String urlStr = String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip),
                getAppPrefVal(R.string.key_gateway_port)) + getString(R.string.panel_list);
        String acc = getAppPrefVal(R.string.key_acc);
        String pwd = getAppPrefVal(R.string.key_pwd);
        
        Log.e("url", urlStr);

        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("action", "load");

        String xmlSource = HttpCommunicator.sendCmd(urlStr, paramMap, acc, pwd);

        List<PanelItemBean> pnlItems = PanelListHelper.parseListXml(DocumentHelper.parseText(xmlSource));
        List<PanelDetailBean> pnlDetails = new ArrayList<PanelDetailBean>();

        for (PanelItemBean pib : pnlItems) {
            paramMap = new HashMap<String, String>();
            paramMap.put("action", "load");
            paramMap.put("id", pib.id);
            Document pnlDetailDoc = HttpCommunicator.getPanelDetail(urlStr, paramMap, acc, pwd, true);

            pnlDetails.add(PanelDetailHelper.getDetailBean(pnlDetailDoc));
        }

        getAvaApp().setPnlDetails(pnlDetails);
    }

    private String getAppPrefVal(int resId) {
        return getAppPrefVal(getString(resId));
    }

    private String getAppPrefVal(String key) {
        return getAppPref().getValue(key);
    }

    private AvaPref getAppPref() {
        return getAvaApp().getAppPref();
    }

    private SharedClassApp getAvaApp() {
        return (SharedClassApp) getApplication();
    }

    protected void onDestroy() {
        unregisterReceiver(receiver);
        getAvaApp().setPnlDetails(new ArrayList<PanelDetailBean>());
        stopPollingService();
        super.onDestroy();
    }

    private void call401() {
        // TODO
    }

    private void call404() {
        // TODO
    }

    private void update() {
        // TODO
        Log.d(getClass().getSimpleName(), "Update now...");

        for (AbstractPanelFrag frag : pnlFrags) {
            frag.updateUI();
        }
    }

    private class Receiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PollingService.HTTP_401)) {
                call401();
            } else if (intent.getAction().equals(PollingService.HTTP_404)) {
                call404();
            } else if (intent.getAction().equals(PollingService.REFRESH_NODE_DATA)) {
                update();
            }
        }

    }

    @SuppressLint("NewApi")
    private class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private SparseArray<String> pageTitles = new SparseArray<String>();

        private SparseArray<Fragment> frags = new SparseArray<Fragment>();

        @SuppressLint("NewApi")
        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            getActionBar().removeAllTabs();

            synchronized (pageTitles) {
                pageTitles.clear();
            }

            synchronized (frags) {
                frags.clear();
            }

            List<PanelDetailBean> pnlDetails = getAvaApp().getPnlDetails();

            for (int i = 0; i < pnlDetails.size(); i++) {
                PanelDetailBean bean = pnlDetails.get(i);
                AbstractPanelFrag frag = PanelFragFactory.getFrag(bean);

                if (frag != null) {
                    pageTitles.append(i, bean.label);
                    frags.append(i, frag);
                    pnlFrags.add(frag);
                }
            }
        }

        public Fragment getItem(int position) {
            return frags.get(position);
        }

        public int getCount() {
            return frags.size();
        }

        public CharSequence getPageTitle(int position) {
            return pageTitles.get(position);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_main);

        LoadPanelInfoTask task = new LoadPanelInfoTask();
        task.execute(new Void[0]);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setVisibility(View.INVISIBLE);
        // initPager();

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(PollingService.HTTP_401);
        iFilter.addAction(PollingService.HTTP_404);
        iFilter.addAction(PollingService.REFRESH_NODE_DATA);

        registerReceiver(receiver, iFilter);

        // Start PollingService
        startPollingService();
    }

    private void initPager() {
        pageAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        getActionBar().setTitle("");
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
                mViewPager.setCurrentItem(position);
            }
        });

        for (int i = 0; i < pageAdapter.getCount(); i++) {
            getActionBar().addTab(getActionBar().newTab().setText(pageAdapter.getPageTitle(i)).setTabListener(this));
        }

        mViewPager.setCurrentItem(0);
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }

}
