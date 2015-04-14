package com.avadesign;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;

import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.v4.PanelFragFactory;
import com.avadesign.v4.PanelTemplateFragFactory;
import com.avadesign.v4.frag.AbstractPanelFrag;
import com.avadesign.v4.frag.AbstractPanelTemplateFrag;

public class CtrlPnlTestActivity extends FragmentActivity implements TabListener {
    
    private ViewPager mViewPager;

    private AppSectionsPagerAdapter pageAdapter;
    
    private List<AbstractPanelTemplateFrag> pnlFrags = new ArrayList<AbstractPanelTemplateFrag>();
    
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_ctrlpnl_test);
        
        if (getActionBar() != null) {
            getActionBar().setTitle("");
        }
        
        initPager();
    }

    private void initPager() {
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        
        PanelDetailBean bean02 = new PanelDetailBean();
        bean02.label = "情境面板";
        bean02.typeId = "2";
        AbstractPanelTemplateFrag frag02 = PanelTemplateFragFactory.getInstance().getTemplateFrag(bean02);
        pnlFrags.add(frag02);
        
        PanelDetailBean bean03 = new PanelDetailBean();
        bean03.label = "燈控面板";
        bean03.typeId = "3";
        AbstractPanelTemplateFrag frag03 = PanelTemplateFragFactory.getInstance().getTemplateFrag(bean03);
        pnlFrags.add(frag03);
        
        pageAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.removeAllViews();
        
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
    }
    
    private class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        @SuppressLint("NewApi")
        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            getActionBar().removeAllTabs();

        }

        public Fragment getItem(int position) {
            return pnlFrags.get(position);
        }

        public int getCount() {
            return pnlFrags.size();
        }

        public CharSequence getPageTitle(int position) {
            return pnlFrags.get(position).getTitle();
        }

    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
    
//    private SharedClassApp getAvaApp() {
//        return (SharedClassApp) getApplication();
//    }

}
