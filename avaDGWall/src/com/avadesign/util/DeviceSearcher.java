package com.avadesign.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.avadesign.R;
import com.avadesign.SearchDeviceActivity;

@SuppressLint("DefaultLocale")
public class DeviceSearcher {

    public static final String CMD_CONTROLLER = "WHOIS_AVA_ZWAVE#";

    public static final String CMD_CAM = "WHOIS_AVA_CAM#";

    /**
     * 發送推播, 取得回應都用這個 socket
     * */
    private DatagramSocket dataSocket;

    private Handler dataSocketStoper = new Handler();

    private SearchDeviceActivity activity;
    
    private ProgressDialog waitPop;

    private Map<String, Map<String, String>> deviceDataMap = new HashMap<String, Map<String, String>>();

    public DeviceSearcher(SearchDeviceActivity activity) {
        this.activity = activity;
    }

    public void findController(String cmd) {
        try {
            dataSocket = new DatagramSocket(10000);
            dataSocket.setBroadcast(true);

            ReceiveCommandTask task = new ReceiveCommandTask();
            task.execute();

            dataSocketStoper.postDelayed(new FinishSocketHelper(), 2000);

            new Thread(new MulticastSender(cmd)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 發送推播, 用以尋找裝置.
     * */
    private class MulticastSender implements Runnable {

        private String cmd;

        public MulticastSender(String cmd) {
            this.cmd = cmd;
        }

        public void run() {
            try {
                byte[] cmdByteArr = new String(cmd).getBytes();
                DatagramPacket dataPack = new DatagramPacket(cmdByteArr, cmdByteArr.length, InetAddress.getByName("255.255.255.255"), 10000);

                for (int i = 0; i < 3; i++) {
                    dataSocket.send(dataPack);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 接收裝置回應的 task
     * */
    private class ReceiveCommandTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            MulticastLock castLock = null;

            try {
//                Log.wtf(getClass().getSimpleName(), "Start task....");
                WifiManager wm = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
                castLock = wm.createMulticastLock("TAG");
                castLock.acquire();

                while (!dataSocket.isClosed()) {
                    int dataLength = 1024;
                    DatagramPacket dp = new DatagramPacket(new byte[dataLength], dataLength);
                    dataSocket.receive(dp);
                    String response = new String(dp.getData(), 0, dp.getLength());
                    Log.w("response", response);
                    Map<String, String> data = getDeviceData(response, dp);
                    
                    if (data.size() > 0) {
                        deviceDataMap.put(data.get("mac"), data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (castLock != null) {
                    castLock.release();
                }
                
                if (dataSocket != null) {
                    dataSocket.close();
                    dataSocket = null;
                }
            }
            return null;
        }

        private Map<String, String> getDeviceData(String src, DatagramPacket dp) throws Exception {
            Log.w("src", src);
            Map<String, String> result = new LinkedHashMap<String, String>();
            String[] strArr = src.substring(src.indexOf("#") + 1).split("&");
            
            for (String s : strArr) {
                if (s.toUpperCase().startsWith("MAC=")) {
                    result.put("mac", s.toUpperCase().replace("MAC=", ""));
                } else if (s.startsWith("version=")) {
                    result.put("version", s.replace("version=", ""));
                }
            }
            
            if (result.size() > 0) {
                result.put("port", "80");
                result.put("ip", dp.getAddress().toString().substring(1));
            }

            Log.w("result", result.toString());
            return result;
        }

        protected void onPreExecute() {
           // Display dialog.
            waitPop = new ProgressDialog(activity);
            waitPop.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitPop.setMessage(activity.getString(R.string.searching_device));
            
            activity.runOnUiThread(new Runnable() {
                
                public void run() {
                    waitPop.show();
                }
            });
        }

        protected void onPostExecute(String result) {
            // Close dialog.
            if (waitPop != null) {
                activity.runOnUiThread(new Runnable() {
                    
                    public void run() {
                        waitPop.dismiss();
                        waitPop = null;
                    }
                });
                
                System.gc();
                
                // Send data back to activity.
                activity.displaySearchResult(deviceDataMap);
            }
        }

    }

    private class FinishSocketHelper implements Runnable {

        public void run() {
            try {
                if (dataSocket != null) {
                    dataSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
