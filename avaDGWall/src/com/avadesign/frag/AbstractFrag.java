package com.avadesign.frag;

import com.avadesign.SharedClassApp;
import com.avadesign.util.AvaPref;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractFrag extends Fragment {
    public abstract int getLayoutResId();
    
    public abstract void initView(View rootView);

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResId(), container, false);
        
        initView(rootView);
    
        return rootView;
    }
    
    protected String getAppPrefVal(int keyResId) {
        return getAppPrefVal(getString(keyResId));
    }

    protected String getAppPrefVal(String key) {
        return getAppPref().getValue(key);
    }

    protected AvaPref getAppPref() {
        return getAvaApp().getAppPref();
    }
    
    protected SharedClassApp getAvaApp() {
        return (SharedClassApp) getActivity().getApplication();
    }
}
