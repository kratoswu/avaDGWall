package com.avadesign;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.avadesign.comp.DeviceListDialog;
import com.avadesign.util.DeviceSearcher;
import com.avadesign.v4.frag.DPSettingFrag;

public class DPSettingActivity extends SearchDeviceActivity implements ActionBar.TabListener {

    private ViewPager pager;
    private PageAdapter adapter;
    private int currentIdx = 0;
    private List<String> pageTitleList = new LinkedList<String>();
    private List<DPSettingFrag> fragList = new LinkedList<DPSettingFrag>();
    private List<JSONObject> dpList = new LinkedList<JSONObject>();
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();
        }

        return true;
    }

    public void scanCam() {
        DeviceSearcher searcher = new DeviceSearcher(this);
        searcher.findDevice(DeviceSearcher.CMD_CAM);
    }

    public void displaySearchResult(final Map<String, Map<String, String>> deviceDataMap) {
        runOnUiThread(new Runnable() {

            public void run() {
                DeviceListDialog dialog = new DeviceListDialog(DPSettingActivity.this, deviceDataMap);
                dialog.show();
            }
        });
    }

    public void updateIPAddrField(Map<String, String> data) {
        DPSettingFrag frag = fragList.get(currentIdx);

        if (frag != null) {
            frag.updateIPAddr(data.get("ip"));
        }
    }

    public void saveDpData(int seq, JSONObject data) {
        synchronized (dpList) {
            if (seq < dpList.size()) {
                dpList.remove(seq);
                dpList.add(seq, data);
            } else {
                dpList.add(data);
            }
        }

        saveToPref();

        currentIdx = 0;
        refreshPager();
    }

    private void saveToPref() {
        Set<String> dpSet = new LinkedHashSet<String>();

        for (JSONObject o : dpList) {
            dpSet.add(o.toString());
        }

        ((SharedClassApp) getApplication()).getAppPref().setValueSet(getString(R.string.key_dplist), dpSet);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dp_setting_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();
            return true;
        } else if (id == R.id.add_dp) {
            addNewDpFrag();
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNewDpFrag() {
        DPSettingFrag frag = new DPSettingFrag();
        frag.setSequence(fragList.size());
        fragList.add(frag);
        // pageTitleList.add("New door phone");
        refreshPager();
        currentIdx = fragList.size() - 1;
        pager.setCurrentItem(currentIdx);
    }

    private void loadDPData() {
        clearPagerData();

        // 取得 dp 資訊, 建立畫面
        Set<String> dpDataSrc = ((SharedClassApp) getApplication()).getAppPref().getValueSet(getString(R.string.key_dplist));

        if (dpDataSrc != null && dpDataSrc.size() > 0) {
            try {
                initData(dpDataSrc);
            } catch (Exception e) {
                clearPagerData();
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    private void initData(Set<String> dpDataSrc) throws Exception {
        int i = 0;

        for (String dpInfo : dpDataSrc) {
            JSONObject o = new JSONObject(dpInfo);
            dpList.add(o);
            pageTitleList.add(o.getString(getString(R.string.key_dp_label)));

            DPSettingFrag dpFrag = new DPSettingFrag();
            dpFrag.setData(o);
            dpFrag.setSequence(i);

            fragList.add(dpFrag);

            i++;
        }
    }

    private void clearPagerData() {
        synchronized (fragList) {
            fragList.clear();
        }

        synchronized (pageTitleList) {
            pageTitleList.clear();
        }

        synchronized (dpList) {
            dpList.clear();
        }
    }

    public void rmDPFrag() {
        if (fragList.size() == 1) {
            synchronized (fragList) {
                fragList.clear();
            }

            synchronized (dpList) {
                dpList.clear();
            }
        } else if (fragList.size() > currentIdx && fragList.get(currentIdx) != null) {
            synchronized (fragList) {
                fragList.remove(currentIdx);
            }

            synchronized (dpList) {
                if (currentIdx < dpList.size()) {
                    dpList.remove(currentIdx);
                }
            }
        }

        currentIdx = 0;
        saveToPref();
        refreshPager();
    }

    private class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
            getActionBar().removeAllTabs();
        }

        public Fragment getItem(int position) {
            return fragList.get(position);
        }

        public int getCount() {
            return fragList.size();
        }

        public CharSequence getPageTitle(int position) {
            String label = "DP";

            try {
                label = position < dpList.size() ? dpList.get(position).getString(getString(R.string.key_dp_label)) : "";
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return label;
        }

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dp_setting);
        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setHomeButtonEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        loadDPData();
        initPager();
    }

    private void refreshPager() {
        adapter = new PageAdapter(getSupportFragmentManager());
        pager.removeAllViews();
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            public void onPageSelected(int position) {
                currentIdx = position;
                getActionBar().setSelectedNavigationItem(position);
                pager.setCurrentItem(position);
            }

        });

        for (int i = 0; i < adapter.getCount(); i++) {
            getActionBar().addTab(getActionBar().newTab().setText(adapter.getPageTitle(i)).setTabListener(this));
        }

        pager.setCurrentItem(currentIdx);
    }

    private void initPager() {
        adapter = new PageAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.dpActPager);
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            public void onPageSelected(int position) {
                currentIdx = position;
                getActionBar().setSelectedNavigationItem(position);
            }

        });

        for (int i = 0; i < adapter.getCount(); i++) {
            getActionBar().addTab(getActionBar().newTab().setText(adapter.getPageTitle(i)).setTabListener(this));
        }

        currentIdx = 0;
        pager.setCurrentItem(0);
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        int position = tab.getPosition();
        currentIdx = position;
        pager.setCurrentItem(position);
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

}
