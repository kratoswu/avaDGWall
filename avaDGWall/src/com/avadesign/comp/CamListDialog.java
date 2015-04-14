package com.avadesign.comp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.avadesign.R;
import com.avadesign.SharedClassApp;
import com.avadesign.util.AvaPref;

public class CamListDialog extends Dialog {
    
    private AvaPref appPref;
    
    private ListView camListView;
    
    private List<JSONObject> camDataList;
    
    private String prefKey;
    
    private Button cancelBtn;
    
    private Activity activity;

    public CamListDialog(Activity activity, int keyId) {
        super(activity);
        
        this.activity = activity;
        appPref = ((SharedClassApp) activity.getApplication()).getAppPref();
        prefKey = activity.getString(keyId);
        camDataList = new ArrayList<JSONObject>();
        Set<String> camDataSet = appPref.getValueSet(activity.getString(R.string.key_camlist));
        
        if (camDataSet != null && camDataSet.size() > 0) {
            try {
                for (String s : camDataSet) {
                    JSONObject data = new JSONObject(s);
                    camDataList.add(data);
                }
            } catch (Exception e) {
                Log.e("Initializing cam list data", e.getMessage(), e);
                
                synchronized (camDataList) {
                    camDataList.clear();
                }
            }
        }
    }
    
    private class CamListAdapter extends ArrayAdapter<JSONObject> {

        public CamListAdapter(Context context, int resource, List<JSONObject> objects) {
            super(context, resource, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.cam_dialog_list_row, null);
            }
            
            try {
                TextView lbl = (TextView) convertView.findViewById(R.id.camLbl);
                JSONObject camItem = camDataList.get(position);
                lbl.setText(camItem.getString(activity.getString(R.string.key_cam_label)));
            } catch (Exception e) {
                Log.e("CamListAdapter", e.getMessage(), e);
            }
            
            return convertView;
        }
        
    }

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_cam_list);
        
        initCamListView();
        initCancelBtn();
    }

    private void initCancelBtn() {
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initCamListView() {
        camListView = (ListView) findViewById(R.id.cameraList);
        camListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject data = camDataList.get(position);
                
                try {
//                    Log.v(prefKey, data.toString());
                    appPref.setValue(prefKey, data.getString(activity.getString(R.string.key_cam_mac)));
                } catch (Exception e) {
                    Log.e("Save cam data", e.getMessage(), e);
                } finally {
                    dismiss();
                }
            }
        });
        
        CamListAdapter adapter = new CamListAdapter(activity, R.layout.cam_dialog_list_row, camDataList);
        camListView.setAdapter(adapter);
    }

}
