package com.avadesign.util;

import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressLint("CommitPrefEdits")
public class AvaPref {
    private SharedPreferences pref;
    private Editor prefEditor;

    public AvaPref(Context context, String prefName) {
        pref = context.getSharedPreferences(prefName, 0);
        prefEditor = pref.edit();
    }

    public Set<String> getValueSet(String key) {
        return pref.getStringSet(key, null);
    }

    public void setValueSet(String key, Set<String> valueSet) {
        prefEditor.putStringSet(key, valueSet);
        prefEditor.commit();
    }

    public String getValue(String key) {
        return pref.getString(key, "");
    }

    public void setValue(String key, String value) {
        prefEditor.putString(key, value);
        prefEditor.commit();
    }
    
    public boolean getBooleanVal(String key) {
        boolean b = Boolean.parseBoolean(pref.getString(key, "false"));
        return b;
    }
}
