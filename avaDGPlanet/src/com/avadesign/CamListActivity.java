package com.avadesign;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class CamListActivity extends Activity {
    
    private ListView camListView;
    private LayoutInflater inflater;
    private List<JSONObject> camDataList;
    private CamListAdapter listAdapter;

    protected void onResume() {
        super.onResume();
        
        loadCamList();
        resetListView();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.add_cam) {
            startActivity(new Intent(this, EditCamActivity.class));
        } else if (id == R.id.match_scn) {
            startActivity(new Intent(this, NVRScnSettingActivity.class));
        } else if (id == R.id.action_exit) {
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();
        }
        
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cam_list_menu, menu);
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_camlist);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        loadCamList();
        
        initCamListView();
    }
    
    private class CamListAdapter extends ArrayAdapter<JSONObject> {

        public CamListAdapter(Context context, int resource, List<JSONObject> objects) {
            super(context, resource, objects);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.cam_list_row, null);
            }
            
            try {
                TextView lbl = (TextView) convertView.findViewById(R.id.camLbl);
                JSONObject o = camDataList.get(position);
                lbl.setText(o.getString(getString(R.string.key_cam_label)));
                
                Button editBtn = (Button) convertView.findViewById(R.id.editCamBtn);
                editBtn.setOnClickListener(new OnClickListener() {
                    
                    public void onClick(View v) {
                        goToEdit(position);
                    }
                });
                
                Button rmBtn = (Button) convertView.findViewById(R.id.rmCamBtn);
                rmBtn.setOnClickListener(new OnClickListener() {
                    
                    public void onClick(View v) {
                        rmCamData(position);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return convertView;
        }
        
    }
    
    private void rmCamData(int position) {
        checkCamScn(camDataList.get(position));
        
        synchronized (camDataList) {
            camDataList.remove(position);
        }
        
        try {
            Set<String> camDataSet = new LinkedHashSet<String>();
            
            for (JSONObject o : camDataList) {
                camDataSet.add(o.toString());
            }
            
            getAppPref().setValueSet(getString(R.string.key_camlist), camDataSet);
            resetListView();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void checkCamScn(JSONObject o) {
        try {
            String mac = o.getString(getString(R.string.key_cam_mac));
            checkCamScn(R.string.key_cam_00, mac);
            checkCamScn(R.string.key_cam_01, mac);
            checkCamScn(R.string.key_cam_02, mac);
            checkCamScn(R.string.key_cam_03, mac);
            checkCamScn(R.string.key_cam_04, mac);
            checkCamScn(R.string.key_cam_05, mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void checkCamScn(int key, String mac) {
        AvaPref appPref = getAppPref();
        String keyStr = getString(key);
        String oldValue = appPref.getValue(keyStr);
        
        if (!StringUtil.isEmptyString(oldValue) && oldValue.equals(mac)) {
            appPref.setValue(keyStr, "");
        }
    }
    
    private void goToEdit(int position) {
        try {
            JSONObject o = camDataList.get(position);
            Intent it = new Intent(this, EditCamActivity.class);
            it.putExtra("oldCamData", o.toString());
            startActivity(it);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCamListView() {
        camListView = (ListView) findViewById(R.id.camList);
        resetListView();
    }

    private void resetListView() {
        listAdapter = new CamListAdapter(this, R.layout.cam_list_row, camDataList);
        camListView.setAdapter(listAdapter);
    }

    private void loadCamList() {
        camDataList = new ArrayList<JSONObject>();
        Set<String> stringDataSet = getAppPref().getValueSet(getString(R.string.key_camlist));
        
        try {
            for (String s : stringDataSet) {
                JSONObject data = new JSONObject(s);
                camDataList.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            synchronized (camDataList) {
                camDataList.clear();
            }
        }
    }

    private AvaPref getAppPref() {
        return ((SharedClassApp) getApplication()).getAppPref();
    }

}
