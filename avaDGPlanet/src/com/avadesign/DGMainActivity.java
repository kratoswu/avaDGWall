package com.avadesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.avadesign.util.AvaPref;

/**
 * 新版的對講主畫面, 現改成進入對講功能後, user 需選擇門口機再顯示門口機畫面.
 * */
public class DGMainActivity extends Activity {

    private ListView dpList;

    private LayoutInflater inflater;

    private DpItemListAdapter dpListAdapter;

    private AvaPref appPref;

    private Set<String> getValueSet(int resId) {
        return appPref.getValueSet(getString(resId));
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

    private class DpItemListAdapter extends ArrayAdapter<JSONObject> {

        private List<JSONObject> data;

        public DpItemListAdapter(Context context, int resource, List<JSONObject> data) {
            super(context, resource, data);

            this.data = data;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.dg_list_row, null);
                convertView.setBackgroundResource(R.drawable.cmpt_item_0);
            }

            TextView label = (TextView) convertView.findViewById(R.id.dpItemLbl);

            try {
                label.setText(data.get(position).getString(getString(R.string.key_dp_label)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dgmain);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        appPref = ((SharedClassApp) getApplication()).getAppPref();

        if (getActionBar() != null) {
            getActionBar().hide();
        }

        initDpList();
    }

    private void initDpList() {
        dpList = (ListView) findViewById(R.id.dpList);

        try {
            dpListAdapter = new DpItemListAdapter(this, R.layout.dg_list_row, getDpList());
            dpList.setAdapter(dpListAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
