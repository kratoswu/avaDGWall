package com.avadesign;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.v4.PanelFragFactory;
import com.avadesign.v4.frag.AbstractPanelFrag;

/**
 * 用來顯示各類面板畫面的 activity, 面板類型如下:
 * <li> type 1: 4 個情境, 4 個開關, 最原始的 panel
 * <li> type 2: 6 個情境, 純粹的情境 panel
 * <li> type 3: 6 個開關, 第一個為 multiple switch 的 switch panel
 * <li> type 4: 溫濕度, 環控 panel
 * */
public class LayoutPrototypeActivity extends FragmentActivity implements ActionBar.TabListener {
    
    private ViewPager pager;
    
    private List<AbstractPanelFrag> frags = new ArrayList<AbstractPanelFrag>();
    
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_layout);
        
        initFrags();
        initPager();
    }

    private void initPager() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("");
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new PnlAdapter(getSupportFragmentManager()));
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
                pager.setCurrentItem(position);
            }
            
        });
        
        for (int i = 0; i < frags.size(); i++) {
            actionBar.addTab(actionBar.newTab().setText(frags.get(i).getFragTitle()).setTabListener(this));
        }
        
        pager.setCurrentItem(0);
    }

    private void initFrags() {
        PanelDetailBean beanType2 = new PanelDetailBean();
        beanType2.typeId = "2";
        beanType2.label = "新情境面板";
        AbstractPanelFrag fragType2 = PanelFragFactory.getFrag(beanType2);
        
        if (fragType2 != null) {
            frags.add(fragType2);
        }
        
        PanelDetailBean beanType3 = new PanelDetailBean();
        beanType3.typeId = "3";
        beanType3.label = "新燈控面板";
        AbstractPanelFrag fragType3 = PanelFragFactory.getFrag(beanType3);
        
        if (fragType3 != null) {
            frags.add(fragType3);
        }
    }

    private class PnlAdapter extends FragmentPagerAdapter {

        public PnlAdapter(FragmentManager fm) {
            super(fm);
            getActionBar().removeAllTabs();
        }

        public Fragment getItem(int index) {
            return frags.get(index);
        }

        public int getCount() {
            return frags.size();
        }

        public CharSequence getPageTitle(int position) {
            return frags.get(position).getFragTitle();
        }
        
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        pager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
    
}
