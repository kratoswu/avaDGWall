package com.avadesign.ha.schedule;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.avadesign.ha.R;
import com.avadesign.ha.frame.BaseActivity;
import com.avadesign.ha.frame.SendHttpCommand;

public class ActivityScheduleViewEdit extends BaseActivity {
    private Button tab_save;

    private ArrayList<HashMap<String, String>> scene_array;

    private EditText edit_name, edit_time, edit_date;
    private Spinner spinner_period, spinner_day, spinner_scene;

    private Handler handler = new MyHandler(this);

    private Boolean EditMode;

    private HashMap<String, String> schedule_map;

    private RelativeLayout relativeLayout;

    private LinearLayout linearLayout;

    private TextView weekday_delaytime;

    private SendCommandTask mSendCommandTask;

    private int hour;
    private int min;
    private int timezone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view_edit);

        FindView();

        Setlistener();

        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        min = c.get(Calendar.MINUTE);

        timezone = TimeZone.getDefault().getRawOffset() / 3600000;

        // Log.v(TAG,"aaa="+timezone/3600000);
        setBtnText();
    }

    private void FindView() {
        edit_name = (EditText) findViewById(R.id.editText1);
        edit_time = (EditText) findViewById(R.id.editText3);
        edit_date = (EditText) findViewById(R.id.editText2);
        edit_date.setOnFocusChangeListener(TextFocusChange);

        spinner_period = (Spinner) findViewById(R.id.spinner2);
        spinner_day = (Spinner) findViewById(R.id.spinner3);
        spinner_scene = (Spinner) findViewById(R.id.spinner5);

        relativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout01);

        linearLayout = (LinearLayout) findViewById(R.id.LinearLayout01);

        weekday_delaytime = (TextView) findViewById(R.id.textView3);

        tab_save = (Button) findViewById(R.id.tab_save);
    }

    private void Setlistener() {
        scene_array = new ArrayList<HashMap<String, String>>();
        tab_save.setOnClickListener(admin_button_down);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            ActivityScheduleViewEdit activity = (ActivityScheduleViewEdit) mActivity.get();

            String MsgString = (String) msg.obj;

            if (MsgString.equals("scene_ok"))
                activity.SetLayout();
            else if (MsgString.equals("layout_ok"))
                activity.ChangeLayout();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onResume() {
        super.onResume();

        edit_name.clearFocus();
        edit_time.clearFocus();

        EditMode = false;

        Bundle bundle = this.getIntent().getExtras();
        schedule_map = (HashMap<String, String>) bundle.getSerializable("map");

        if (schedule_map.size() > 0) {
            EditMode = true;
            tab_save.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.selector_tab_update), null, null);
            tab_save.setText(R.string.tab_button_update);
        }

        GetSceneCommand();
    }

    private Button.OnClickListener admin_button_down = new Button.OnClickListener() {
        public void onClick(View v) {
            String period = spinner_period.getSelectedItemPosition() == 0 ? "week" : spinner_period.getSelectedItemPosition() == 1 ? "day" : "none";
            String weekday = "";
            if (period.equalsIgnoreCase("none"))
                weekday = edit_time.getText().toString();
            else
                weekday = String.valueOf(spinner_day.getSelectedItemPosition());

            String time = edit_date.getText().toString();
            String[] array = time.split(":");

            int HOUR = 0;
            int MINUTE = 0;

            if (array.length > 0) {
                HOUR = Integer.valueOf(array[0]);

                if (HOUR - timezone < 0)
                    HOUR = HOUR - timezone + 24;
                else
                    HOUR = HOUR - timezone;

                MINUTE = Integer.valueOf(array[1]);
            }

            if (EditMode) {
                /*
                 * 2015-04-02, edited by Phoenix. 要改成可以更改時間與周期
                 */
                // SaveScheduleCommand(edit_name.getText().toString(),"","","","",schedule_map.get("id"));
                SaveScheduleCommand(edit_name.getText().toString(), period, weekday, HOUR + "", MINUTE + "", schedule_map.get("id"));
            } else {
                HashMap<String, String> map = scene_array.get(spinner_scene.getSelectedItemPosition());

                String scene_id = map.get("id");

                /*
                 * Log.v(TAG,edit_name.getText().toString()); Log.v(TAG,period);
                 * Log.v(TAG,weekday); Log.v(TAG,String.valueOf(HOUR));
                 * Log.v(TAG,String.valueOf(MINUTE)); Log.v(TAG,scene_id);
                 */
                SaveScheduleCommand(edit_name.getText().toString(), period, weekday, String.valueOf(HOUR), String.valueOf(MINUTE), scene_id);
            }
        }
    };

    private EditText.OnFocusChangeListener TextFocusChange = new OnFocusChangeListener() {
        @SuppressWarnings("deprecation")
        @Override
        public void onFocusChange(View arg0, boolean arg1) {
            // Log.v(TAG,"Edit="+arg0+", focus="+arg1);
            if (arg1) {
                if (arg0 == edit_date) {
                    arg0.clearFocus();
                    showDialog(0);
                }
            }

        }

    };

    private void SetLayout() {
        String[] string_period = new String[3];
        string_period[0] = getString(R.string.schedule_period_week);// "Week";
        string_period[1] = getString(R.string.schedule_period_day);// "Day";
        string_period[2] = getString(R.string.schedule_period_once);// "Once";

        ArrayAdapter<String> adapter_period = new ArrayAdapter<String>(this, R.layout.custom_spinner, string_period);
        adapter_period.setDropDownViewResource(R.layout.custom_spinner);
        spinner_period.setAdapter(adapter_period);
        spinner_period.setOnItemSelectedListener(Spinner_select);

        String[] string_scene = new String[scene_array.size()];
        for (int i = 0; i < scene_array.size(); i++) {
            HashMap<String, String> map = scene_array.get(i);
            string_scene[i] = map.get("label");
        }

        ArrayAdapter<String> adapter_scene = new ArrayAdapter<String>(this, R.layout.custom_spinner, string_scene);
        adapter_scene.setDropDownViewResource(R.layout.custom_spinner);
        spinner_scene.setAdapter(adapter_scene);
        spinner_scene.setOnItemSelectedListener(Spinner_select);

        String[] string_day = new String[7];
        string_day[0] = getString(R.string.schedule_day_sum);// "Sum";
        string_day[1] = getString(R.string.schedule_day_mon);// "Mon";
        string_day[2] = getString(R.string.schedule_day_tue);// "Tue";
        string_day[3] = getString(R.string.schedule_day_wed);// "Wed";
        string_day[4] = getString(R.string.schedule_day_thu);// "Thu";
        string_day[5] = getString(R.string.schedule_day_fri);// "Fri";
        string_day[6] = getString(R.string.schedule_day_sat);// "Sat";

        ArrayAdapter<String> adapter_day = new ArrayAdapter<String>(this, R.layout.custom_spinner, string_day);
        adapter_day.setDropDownViewResource(R.layout.custom_spinner);
        spinner_day.setAdapter(adapter_day);
        spinner_day.setOnItemSelectedListener(Spinner_select);

        Message message;
        String obj = "layout_ok";
        message = handler.obtainMessage(1, obj);
        handler.sendMessage(message);
    }

    private void ChangeLayout() {
        if (EditMode) {
            /*
             * 2015-04-02, edited by Phoenix. 改成可以調整時間周期
             */
            // edit_time.setEnabled(false);
            // spinner_period.setEnabled(false);
            // spinner_day.setEnabled(false);
            // edit_date.setEnabled(false);

            if (!cp.getControllerAcc().equals("admin")) {
                edit_name.setEnabled(false);
                spinner_scene.setEnabled(false);
            }

            edit_name.setText((String) schedule_map.get("label"));

            if (schedule_map.get("period").equalsIgnoreCase("week")) {
                spinner_period.setSelection(0);

                spinner_day.setSelection(Integer.valueOf(schedule_map.get("weekday")));

            } else if (schedule_map.get("period").equalsIgnoreCase("day")) {
                spinner_period.setSelection(1);
            } else {
                spinner_period.setSelection(2);
            }

            String scene = Scene_id_to_name((String) schedule_map.get("scene"));

            for (int i = 0; i < scene_array.size(); i++) {
                HashMap<String, String> map = scene_array.get(i);

                if (map.get("label").equalsIgnoreCase(scene))
                    spinner_scene.setSelection(i);
            }

            String HOUR = schedule_map.get("hour");
            String MINUTE = schedule_map.get("minute");

            if (Integer.valueOf(HOUR) + timezone >= 24)
                hour = Integer.valueOf(HOUR) + timezone - 24;
            else
                hour = Integer.valueOf(HOUR) + timezone;

            min = Integer.valueOf(MINUTE);

            setBtnText();
        }
    }

    private Spinner.OnItemSelectedListener Spinner_select = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            // Log.v(TAG,"position"+spinner_mode.getSelectedItemPosition());

            if (arg0.getId() == R.id.spinner2) {
                if (arg2 == 0) {
                    relativeLayout.setVisibility(View.VISIBLE);
                    edit_time.setVisibility(View.INVISIBLE);
                    spinner_day.setVisibility(View.VISIBLE);

                    weekday_delaytime.setVisibility(View.VISIBLE);
                    weekday_delaytime.setText(getString(R.string.schedule_day));

                    linearLayout.setVisibility(View.VISIBLE);
                } else if (arg2 == 1) {
                    relativeLayout.setVisibility(View.GONE);
                    weekday_delaytime.setVisibility(View.GONE);

                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    relativeLayout.setVisibility(View.VISIBLE);
                    edit_time.setVisibility(View.VISIBLE);
                    spinner_day.setVisibility(View.INVISIBLE);

                    weekday_delaytime.setVisibility(View.VISIBLE);
                    weekday_delaytime.setText(getString(R.string.schedule_delay));

                    linearLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0:
            return new TimePickerDialog(this, timePickerListener, hour, min, true);
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            min = minute;
            setBtnText();
        }
    };

    private void setBtnText() {
        // time_button.setText(String.format("%02d",
        // hour)+":"+String.format("%02d", min));
        edit_date.setText(String.format("%02d", hour) + ":" + String.format("%02d", min));
    }

    private String Scene_id_to_name(String scene_id) {
        for (HashMap<String, String> map : scene_array) {
            if (map.get("id").equals(scene_id))
                return map.get("label");
        }
        return "";
    }

    private void SaveScheduleCommand(final String label, final String period, final String weekday, final String hour, final String minute,
            final String scene_id) {
        if (mSendCommandTask != null)
            return;

        mSendCommandTask = new SendCommandTask();

        /*
         * TODO Schedule 所有的內容要能編輯, 最直接的方法就是移除舊的, 再將原先的值追加成新的. 這裡只要直接呼叫 task 即可,
         * detail 的部分由 task 執行.
         */
        // if (EditMode) {
        // mSendCommandTask.execute(new String[] { "scene_schedule.cgi",
        // "action", "update", "label", label, "id", scene_id, "active", "true"
        // });
        // } else {
        // if (period.equalsIgnoreCase("week"))
        // mSendCommandTask.execute(new String[] { "scene_schedule.cgi",
        // "action", "add", "label", label, "period", period, "weekday",
        // weekday,
        // "hour", hour, "minute", minute, "id", scene_id, "active", "true" });
        // else if (period.equalsIgnoreCase("day"))
        // mSendCommandTask.execute(new String[] { "scene_schedule.cgi",
        // "action", "add", "label", label, "period", period, "hour", hour,
        // "minute", minute, "id", scene_id, "active", "true" });
        // else
        // mSendCommandTask.execute(new String[] { "scene_schedule.cgi",
        // "action", "add", "label", label, "period", "delay", weekday, "id",
        // scene_id, "active", "true" });
        // }

        if (period.equalsIgnoreCase("week"))
            mSendCommandTask.execute(new String[] { "scene_schedule.cgi", "action", "add", "label", label, "period", period, "weekday", weekday,
                    "hour", hour, "minute", minute, "id", scene_id, "active", "true" });
        else if (period.equalsIgnoreCase("day"))
            mSendCommandTask.execute(new String[] { "scene_schedule.cgi", "action", "add", "label", label, "period", period, "hour", hour, "minute",
                    minute, "id", scene_id, "active", "true" });
        else
            mSendCommandTask.execute(new String[] { "scene_schedule.cgi", "action", "add", "label", label, "period", "delay", weekday, "id",
                    scene_id, "active", "true" });
    }

    private void GetSceneCommand() {
        if (mSendCommandTask != null)
            return;

        mSendCommandTask = new SendCommandTask();
        mSendCommandTask.execute(new String[] { "scenepost.html", "fun", "load" });
    }

    private String getGateWayURL() {
        // TODO
        boolean isLocal = cp.isLocalUsed();
        String pattern = isLocal ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax);
        String gatewayIP = isLocal ? cp.getControllerIP() : getString(R.string.server_ip);
        String port = isLocal ? cp.getControllerPort() + "" : getString(R.string.server_port);

        return String.format(pattern, gatewayIP, port);
    }

    private class SendCommandTask extends AsyncTask<String, Void, Boolean> {
        ArrayList<HashMap<String, String>> list;

        String fun = "";

        @Override
        protected void onPreExecute() {
            callProgress();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> map = new HashMap<String, String>();

            fun = params[2];

            for (int i = 2; i < params.length; i += 2) {
                map.put(params[i - 1], params[i]);
            }

            if (!cp.isLocalUsed()) {
                map.put("mac", cp.getControllerMAC());
                map.put("username", cp.getControllerAcc());
                map.put("userpwd", cp.getControllerPwd());
            }

            if (fun.equalsIgnoreCase("load")) {
                // list = SendHttpCommand.getlist(
                // String.format(cp.isLocalUsed() ?
                // getString(R.string.local_url_syntax) :
                // getString(R.string.server_url_syntax),
                // cp.isLocalUsed() ? cp.getControllerIP() :
                // getString(R.string.server_ip),
                // cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) :
                // getString(R.string.server_port))
                // + params[0], map, cp.getControllerAcc(),
                // cp.getControllerPwd(), cp.isLocalUsed(), "scene");

                list = SendHttpCommand.getlist(getGateWayURL() + params[0], map, cp.getControllerAcc(), cp.getControllerPwd(), cp.isLocalUsed(),
                        "scene");
            } else {
//                SendHttpCommand.send(
//                        String.format(cp.isLocalUsed() ? getString(R.string.local_url_syntax) : getString(R.string.server_url_syntax),
//                                cp.isLocalUsed() ? cp.getControllerIP() : getString(R.string.server_ip),
//                                cp.isLocalUsed() ? String.valueOf(cp.getControllerPort()) : getString(R.string.server_port))
//                                + params[0], map, cp.getControllerAcc(), cp.getControllerPwd(), cp.isLocalUsed());

                /*
                 * 2015-04-08, edited by Phoenix.
                 * 在新增之前先檢查是否為 EditMode, 是的話就表示是編輯, 要先移除再新增.
                 * */
                if (EditMode) {
                    // 移除
                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("id", map.get("id"));
                    paramMap.put("action", "remove");

                    if (!cp.isLocalUsed()) {
                        paramMap.put("mac", cp.getControllerMAC());
                        paramMap.put("username", cp.getControllerAcc());
                        paramMap.put("userpwd", cp.getControllerPwd());
                    }

                    boolean result = SendHttpCommand.send(getGateWayURL() + "/scene_schedule.cgi", paramMap, cp.getControllerAcc(), cp.getControllerPwd(), cp.isLocalUsed());

                    if (result) {
                        HashMap<String, String> sceneMap = scene_array.get(spinner_scene.getSelectedItemPosition());
                        String scene_id = sceneMap.get("id");
                        map.put("id", scene_id);
                        SendHttpCommand.send(getGateWayURL() + params[0], map, cp.getControllerAcc(), cp.getControllerPwd(), cp.isLocalUsed());
                    }
                } else {
                    SendHttpCommand.send(getGateWayURL() + params[0], map, cp.getControllerAcc(), cp.getControllerPwd(), cp.isLocalUsed());
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSendCommandTask = null;
            cancelProgress();

            if (fun.equalsIgnoreCase("load")) {
                if (list != null) {
                    scene_array.clear();

                    for (HashMap<String, String> map : list) {
                        scene_array.add(map);

                        Log.v(TAG, "id-" + map.get("id") + " label=" + map.get("label"));
                    }

                    Message message;
                    String obj = "scene_ok";
                    message = handler.obtainMessage(1, obj);
                    handler.sendMessage(message);
                }
            } else {
                finish();
            }
        }
    }
}
