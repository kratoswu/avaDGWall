package com.avadesign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.avadesign.frag.DeviceDetailFrag;
import com.avadesign.model.ZWaveNode;
import com.avadesign.model.bean.SceneBean;
import com.avadesign.service.PollingService;
import com.avadesign.task.SavePanelDetailTask;
import com.avadesign.util.AvaPref;
import com.avadesign.util.HttpCommunicator;
import com.avadesign.util.PanelDetailHelper;

public class PanelSettingActivity extends Activity {

    private ArrayList<HashMap<String, String>> componentTempData = new ArrayList<HashMap<String, String>>();
    private DeviceDetailFrag detailFrag;
    private ListView cmptList;
    private ComponentListAdapter listAdapter;
    private LayoutInflater inflater;
    private AvaPref appPref;
    private Document detailDoc;
    private HashMap<String, ZWaveNode> deviceNodeMap = new HashMap<String, ZWaveNode>();
    private Receiver receiver = new Receiver();
    private Map<String, SceneBean> sceneMap = new HashMap<String, SceneBean>();
    private String acc;
    private String pwd;
    private ActionBar actBar;
    private String panelID;

    private String getAppPrefVal(int resId) {
        return appPref.getValue(getString(resId));
    }

    private String getGatewayPath() {
        return String.format(getString(R.string.local_url_pattern), getAppPrefVal(R.string.key_gateway_ip), getAppPrefVal(R.string.key_gateway_port));
    }

    private class LoadSceneTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... params) {
            String urlStr = getGatewayPath() + "/" + getString(R.string.scenepost);
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("fun", "load");

            List<HashMap<String, String>> result = HttpCommunicator.getSceneList(urlStr, paramMap, acc, pwd, true);
            sceneMap.clear();

            if (result != null) {
                for (HashMap<String, String> element : result) {
                    SceneBean scene = new SceneBean();
                    scene.id = element.get("id");
                    scene.label = element.get("label");

                    sceneMap.put(scene.id, scene);
                }
            }

            return true;
        }

    }

    protected void onDestroy() {
        unregisterReceiver(receiver);
        stopPollingService();
        super.onDestroy();
    }

    private void stopPollingService() {
        Intent intent = new Intent(this, PollingService.class);
        stopService(intent);
    }

    private void startPollingService() {
        Intent intent = new Intent(this, PollingService.class);
        startService(intent);
    }

    private class Receiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PollingService.REFRESH_NODE_DATA)) {
                updateDeviceNode();
            }
        }

    }

    private void updateDeviceNode() {
        ArrayList<HashMap<String, Object>> contentList = new ArrayList<HashMap<String, Object>>();
        ((SharedClassApp) getApplication()).refreshNodesList(contentList);

        synchronized (deviceNodeMap) {
            deviceNodeMap = new HashMap<String, ZWaveNode>();

            for (HashMap<String, Object> content : contentList) {
                ZWaveNode node = new ZWaveNode(content);
                deviceNodeMap.put(node.id, node);
            }
        }
    }

    private class LoadPanelDetailTask extends AsyncTask<String, Void, Boolean> {

        private String getString(int resId) {
            return PanelSettingActivity.this.getString(resId);
        }

        protected Boolean doInBackground(String... params) {
            HashMap<String, String> connParamMap = new HashMap<String, String>();
            connParamMap.put("action", "load");
            connParamMap.put("id", params[0]);

            String hostAddr = appPref.getValue(getString(R.string.key_gateway_ip));
            String hostPort = appPref.getValue(getString(R.string.key_gateway_port));
            String acc = appPref.getValue(getString(R.string.key_acc));
            String pwd = appPref.getValue(getString(R.string.key_pwd));
            String url = String.format(getString(R.string.local_url_pattern), hostAddr, hostPort) + "/panel_list.cgi";

            detailDoc = HttpCommunicator.getPanelDetail(url, connParamMap, acc, pwd, true);

            runOnUiThread(new Runnable() {

                public void run() {
                    refreshList();
                }
            });

            return true;
        }

    }

    private class ComponentListAdapter extends ArrayAdapter<HashMap<String, String>> {

        private int selectedIdx = -1;

        public ComponentListAdapter(Context context, int textViewResourceId, List<HashMap<String, String>> objects) {
            super(context, textViewResourceId, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.cmpt_row, null);
            }

            TextView cmptName = (TextView) convertView.findViewById(R.id.cmptLbl);
            HashMap<String, String> item = componentTempData.get(position);
            cmptName.setText(item.get("type") + " " + item.get("id"));

            // 變更背景圖
            int bgRes = position == selectedIdx ? R.drawable.cmpt_item_1 : R.drawable.cmpt_item_0;
            convertView.setBackgroundResource(bgRes);

            return convertView;
        }

        public void setSelectedIdx(int selectedIdx) {
            this.selectedIdx = selectedIdx;
            this.notifyDataSetInvalidated();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pnl_setting);
        startPollingService();

        appPref = getAvaApp().getAppPref();
        acc = appPref.getValue(getString(R.string.key_acc));
        pwd = appPref.getValue(getString(R.string.key_pwd));

        /*
         * TODO Get data from intent
         */
        String pnlId = "59";
        // String pnlId = getIntent().getStringExtra("pnlId");
        loadPanelDetailData(pnlId);

        LoadSceneTask sceneTask = new LoadSceneTask();
        sceneTask.execute(new String[0]);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Initialize UI
        cmptList = (ListView) findViewById(R.id.act_cmptList);
        cmptList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listAdapter.setSelectedIdx(position);
                Log.i("position: ", position + "");

                HashMap<String, String> item = componentTempData.get(position);
                // 將整理好的資料傳到 fragment
                detailFrag.loadData(panelID, item, deviceNodeMap, sceneMap);
            }
        });

        // Initialize fragment
        detailFrag = (DeviceDetailFrag) getFragmentManager().findFragmentById(R.id.act_deviceDetailFrag);

        actBar = getActionBar();

        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(PollingService.HTTP_401);
        iFilter.addAction(PollingService.HTTP_404);
        iFilter.addAction(PollingService.REFRESH_NODE_DATA);
        registerReceiver(receiver, iFilter);
    }

    private SharedClassApp getAvaApp() {
        return (SharedClassApp) getApplication();
    }

    private void loadPanelDetailData(String pnlId) {
        LoadPanelDetailTask task = new LoadPanelDetailTask();
        task.execute(pnlId);
    }

    private void refreshList() {
        // panelNameLbl.setText(PanelDetailHelper.parseElement(detailDoc.getRootElement()).get("label"));

        if (actBar != null) {
            actBar.setTitle(PanelDetailHelper.parseElement(detailDoc.getRootElement()).get("label"));
        }

        panelID = PanelDetailHelper.parseElement(detailDoc.getRootElement()).get("id");
        componentTempData = new ArrayList<HashMap<String, String>>();
        componentTempData.addAll(PanelDetailHelper.getSwitchDetails(detailDoc));
        componentTempData.addAll(PanelDetailHelper.getSceneBtnDetails(detailDoc));
        componentTempData.addAll(PanelDetailHelper.getTempDetails(detailDoc));
        componentTempData.addAll(PanelDetailHelper.getFanDetails(detailDoc));

        listAdapter = new ComponentListAdapter(this, R.layout.device_row, componentTempData);
        cmptList.setAdapter(listAdapter);
        cmptList.invalidateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pnl_setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            Intent it = new Intent(this, MainScreenActivity.class);
            startActivity(it);
            return true;
        } else if (id == R.id.action_save) {
            // Save data
            List<Map<String, String>> paramData = detailFrag.getTempData();
            SavePanelDetailTask saveTask = new SavePanelDetailTask(getAvaApp());
            saveTask.execute(paramData.toArray(new Map[paramData.size()]));

            // Exit
            Intent it = new Intent(this, MainScreenActivity.class);
            startActivity(it);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
