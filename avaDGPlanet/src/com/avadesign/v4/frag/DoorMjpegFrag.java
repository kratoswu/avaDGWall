package com.avadesign.v4.frag;

import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.linphone.LinphoneManager;
import org.linphone.core.LinphoneCall;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.avadesign.DoorActivity;
import com.avadesign.R;
import com.avadesign.util.StringUtil;
import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;

public class DoorMjpegFrag extends Fragment {
    /*
     * motion jpeg test URL: http://192.168.1.36/cgi/mjpg/mjpeg.cgi size: 614,
     * 372
     */
    private MjpegView doorVideo;
    // private boolean suspending;
    private String urlStr;
    private String camAcc;
    private String camPwd;
    private String camHost;
    private Button answerBtn;
    private int sipPort;
    private int sequence;
    private DoorActivity doorAct;
    private boolean currentFrag;
    private Button openDoorBtn;
    private Button hangUpBtn;
    // TODO
    private Button muteBtn;
    private Button snapBtn;
    private boolean isMute;
    
    public DoorMjpegFrag(String urlStr, String camAcc, String camPwd, int sipPort, DoorActivity doorAct) {
        this.urlStr = urlStr;
        this.camAcc = camAcc;
        this.camPwd = camPwd;
        this.sipPort = sipPort;
        this.doorAct = doorAct;
    }
    
    public void onResume() {
        super.onResume();
        resumeVideo();
    }

    public void resumeVideo() {
        if (doorVideo != null && !doorVideo.isStreaming()) {
            new LoadMjpegTask().execute(new String[0]);
            // suspending = false;
        }
    }

    public void onPause() {
//        Toast.makeText(getActivity(), urlStr + " had been paused.", Toast.LENGTH_SHORT).show();
        pauseVideo();
        super.onPause();
    }

    public void pauseVideo() {
        if (doorVideo != null && doorVideo.isStreaming()) {
            doorVideo.stopPlayback();
            // suspending = true;
        }

//        teminateCall();
    }

    private void teminateCall() {
        if (LinphoneManager.getLc().isIncall()) {
            LinphoneManager.getInstance().terminateCall();
        }
    }

    public void onDestroy() {
        pauseVideo();

        if (doorVideo != null) {
            doorVideo.freeCameraMemory();
        }

        System.gc();
        super.onDestroy();
    }

    private String getLogTag() {
        return getClass().getSimpleName();
    }

    private class LoadMjpegTask extends AsyncTask<String, Void, MjpegInputStream> {

        protected MjpegInputStream doInBackground(String... params) {
            try {
                Log.v(getLogTag(), "Start task");
                URL url = new URL(urlStr);
                HttpResponse res = null;
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpParams httpParams = httpclient.getParams();

                HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
                HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);

                camHost = url.getHost();
                httpclient.getCredentialsProvider().setCredentials(new AuthScope(url.getHost(), url.getPort()),
                        new UsernamePasswordCredentials(camAcc, camPwd));

                HttpGet req = new HttpGet(urlStr);
                res = httpclient.execute(req);

                if (res.getStatusLine().getStatusCode() == 200) {
                    return new MjpegInputStream(res.getEntity().getContent());
                }
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Connect to " + urlStr + " failed.", e);
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            try {
                doorVideo.setSource(result);

                if (result != null) {
                    result.setSkip(1);
                }

                doorVideo.setDisplayMode(MjpegView.SIZE_BEST_FIT);
                doorVideo.showFps(false);
            } catch (Exception e) {
                // TODO
            }
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_door, container, false);
        initAnswerBtn(rootView);
        initOpenDoorBtn(rootView);
        initHangUpBtn(rootView);
        initMuteBtn(rootView);
        initSnapBtn(rootView);

        doorVideo = (MjpegView) rootView.findViewById(R.id.doorVideo);

        // get dpi information.
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        // Toast.makeText(getActivity(), "DPI: " + dm.densityDpi,
        // Toast.LENGTH_SHORT).show();

        if (doorVideo != null) {
            // 以取得的 dpi info 來決定要多大的畫面
            int dpi = dm.densityDpi;
            int width = dpi <= 160 ? 614 : 1228;
            int height = dpi <= 160 ? 372 : 744;
            doorVideo.setResolution(width, height);
        }

        if (currentFrag) {
            new LoadMjpegTask().execute(new String[0]);
        }
        return rootView;
    }

    private void initSnapBtn(View rootView) {
        snapBtn = (Button) rootView.findViewById(R.id.snapBtn);
    }

    private void initMuteBtn(View rootView) {
        muteBtn = (Button) rootView.findViewById(R.id.muteBtn);
        muteBtn.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                try {
                    LinphoneManager.getLc().muteMic(!isMute);
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                }
            }
        });
    }

    private void initHangUpBtn(View rootView) {
        hangUpBtn = (Button) rootView.findViewById(R.id.hangupBtn);
        hangUpBtn.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                LinphoneManager.getInstance().terminateCall();
            }
        });
    }

    private void initOpenDoorBtn(View rootView) {
        openDoorBtn = (Button) rootView.findViewById(R.id.openDoorBtn);
        openDoorBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (!StringUtil.isEmptyString(camHost)) {
                    doorAct.openDoor("sip:" + camHost + ":" + sipPort);
                    
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                    }
                    
                    teminateCall();
                }
            }
        });
    }

    private void initAnswerBtn(View rootView) {
        answerBtn = (Button) rootView.findViewById(R.id.answerBtn);
        answerBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (!StringUtil.isEmptyString(camHost)) {
                    try {
                        LinphoneCall call = doorAct.getCall();
                        
                        if (call != null && call.getState() == LinphoneCall.State.IncomingReceived) {
                            doorAct.answer();
                        } else if (!LinphoneManager.getLc().isIncall()) {
                            String sipUrlStr = "sip:" + camHost + ":" + sipPort;
                            LinphoneManager.getInstance().newOutgoingCall(sipUrlStr, "");
                        }
                        
                        answerBtn.setEnabled(false);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage(), e);
                    }
                }
            }
        });
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public boolean isCurrentFrag() {
        return currentFrag;
    }

    public void setCurrentFrag(boolean currentFrag) {
        this.currentFrag = currentFrag;
    }
}
