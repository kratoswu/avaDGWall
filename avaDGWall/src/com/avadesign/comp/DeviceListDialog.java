package com.avadesign.comp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.avadesign.R;
import com.avadesign.SearchDeviceActivity;

public class DeviceListDialog extends Dialog {

    private ListView deviceList;

    private List<Map<String, String>> deviceDataList;

    private SearchDeviceActivity context;

    private SimpleAdapter listAdapter;
    
    private Button cancelBtn;

    public DeviceListDialog(SearchDeviceActivity context, Map<String, Map<String, String>> deviceDataMap) {
        super(context);
        this.context = context;
        deviceDataList = new ArrayList<Map<String, String>>(deviceDataMap.values());
    }

    /**
     * super 那邊沒有做事, 不需要再呼叫 super.onCreate()
     * */
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_device_list);

        initDeviceList();
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

    private void initDeviceList() {
        deviceList = (ListView) findViewById(R.id.deviceList);
        // Initialize device list
        deviceList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> item = deviceDataList.get(position);
                context.updateIPAddrField(item);

                dismiss();
            }
        });

        listAdapter = new SimpleAdapter(context, deviceDataList, android.R.layout.simple_expandable_list_item_2, new String[] { "ip", "mac" },
                new int[] { android.R.id.text1, android.R.id.text2 });
        
        deviceList.setAdapter(listAdapter);
    }
}
