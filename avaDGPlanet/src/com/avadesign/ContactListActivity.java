package com.avadesign;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.linphone.LinphoneManager;
import org.linphone.LinphoneSimpleListener.LinphoneOnCallStateChangedListener;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.avadesign.task.LoadContactInfoTask;
import com.avadesign.task.SendCallNotificationTask;
import com.avadesign.util.AvaPref;
import com.avadesign.util.StringUtil;

public class ContactListActivity extends Activity implements LinphoneChatMessage.StateListener, LinphoneOnCallStateChangedListener {
    
    private ListView contactListView;
    private LayoutInflater inflater;
    private List<JSONObject> contacts = new ArrayList<JSONObject>();
    private ArrayAdapter<JSONObject> adapter;
    private ProgressDialog waitPop;
    
    protected void onStart() {
        super.onStart();
        
        LinphoneManager.removeListener(this);
        LinphoneManager.addListener(this);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.door, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            startActivity(new Intent(this, MainScreenActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.contact_title);
        }
        
        // Initialize UI
        initContactListView();
        
        // Real code, do not remove.
        AvaPref pref = ((SharedClassApp) getApplication()).getAppPref();
        String camDataStr = pref.getValue(getString(R.string.key_cam_data));
        
        if (!StringUtil.isEmptyString(camDataStr)) {
            try {
                JSONObject camData = new JSONObject(camDataStr);
                String[] sipArr = camData.getString(getString(R.string.key_dial_list)).split(";");
                
                LoadContactInfoTask task = new LoadContactInfoTask(this) {

                    protected void handleResult(List<JSONObject> result) {
                        contacts = result;
                        
                        runOnUiThread(new Runnable() {
                            
                            public void run() {
                                resetListView();
                            }
                        });
                    }
                };
                task.execute(sipArr);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        
        // Test code.
//        try {
//            contacts = new ArrayList<JSONObject>();
//            JSONObject fakeContact = new JSONObject();
//            fakeContact.put("acc", "Fake user");
//            fakeContact.put("sipID", "2001");
//            
//            contacts.add(fakeContact);
//            
//            runOnUiThread(new Runnable() {
//                
//                public void run() {
//                    resetListView();
//                }
//            });
//        } catch (Exception e) {
//            Log.e(getClass().getSimpleName(), e.getMessage(), e);
//        }
    }
    
    private void initContactListView() {
        contactListView = (ListView) findViewById(R.id.contactList);
        resetListView();
    }

    private void resetListView() {
        adapter = new ContactListAdapter(this, R.layout.contact_list_row, contacts);
        contactListView.setAdapter(adapter);
    }
    
    private class ContactListAdapter extends ArrayAdapter<JSONObject> {

        public ContactListAdapter(Context context, int resource, List<JSONObject> contacts) {
            super(context, resource, contacts);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.contact_list_row, null);
            }
            
            try {
                TextView lbl = (TextView) convertView.findViewById(R.id.contactNameLbl);
                JSONObject contact = contacts.get(position);
                lbl.setText(contact.getString("acc"));
                
//                Button audioCallBtn = (Button) convertView.findViewById(R.id.audioCallBtn);
//                audioCallBtn.setOnClickListener(new CallBtnListener(R.id.audioCallBtn, contact.getString("sipID")));
                
                Button videoCallBtn = (Button) convertView.findViewById(R.id.videoCallBtn);
                videoCallBtn.setOnClickListener(new CallBtnListener(R.id.videoCallBtn, contact.getString("sipID")));
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
            
            return convertView;
        }
        
    }
    
    private class CallBtnListener implements View.OnClickListener {
        
        private String sipId;
        
        public CallBtnListener(int btnType, String sipId) {
            this.sipId = sipId;
        }

        public void onClick(View v) {
            /*
             * send push.
             * */
            int sipIDInt = Integer.parseInt(sipId);
            sipIDInt %= 1000000;
            SendCallNotificationTask pushTask = new SendCallNotificationTask(ContactListActivity.this);
            pushTask.execute(sipIDInt + "");
            
//            Intent it = new Intent(ContactListActivity.this, AvaCallActivity.class);
//            it.putExtra("sipID", sipId);
//            
//            startActivity(it);
            
            // 改成傳 IM, 不直接 call
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            
            if (lc != null) {
                LinphoneChatRoom chatRoom = lc.getOrCreateChatRoom(sipId);
                LinphoneChatMessage msg = chatRoom.createLinphoneChatMessage("status_call");
                chatRoom.sendMessage(msg, ContactListActivity.this);
                
                waitPop = new ProgressDialog(ContactListActivity.this);
                waitPop.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                waitPop.setMessage("Call...");
                
                runOnUiThread(new Runnable() {
                    
                    public void run() {
                        waitPop.show();
                    }
                });
            }
        }
        
    }

    private void dismissWaitPop() {
        runOnUiThread(new Runnable() {
            
            public void run() {
                waitPop.dismiss();
                waitPop = null;
            }
        });
    }

    public void onCallStateChanged(LinphoneCall call, org.linphone.core.LinphoneCall.State state, String message) {
       if (state == LinphoneCall.State.IncomingReceived) {
           if (waitPop != null) {
               dismissWaitPop();
           }
       }
    }

    public void onLinphoneChatMessageStateChanged(LinphoneChatMessage msg, org.linphone.core.LinphoneChatMessage.State state) {
    }

}
