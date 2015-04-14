package com.avadesign;

import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;

public abstract class SearchDeviceActivity extends FragmentActivity {
    
    protected LayoutInflater inflater;
    
    public abstract void displaySearchResult(Map<String, Map<String, String>> deviceDataMap);
    
    public abstract void updateIPAddrField(Map<String, String> data);
    
}
