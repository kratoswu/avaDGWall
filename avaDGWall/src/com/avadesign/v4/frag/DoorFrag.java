package com.avadesign.v4.frag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.linphone.LinphoneManager;
import org.linphone.core.LinphoneCall;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.CaptioningManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.avadesign.DoorActivity;
import com.avadesign.R;
import com.avadesign.util.StringUtil;

public class DoorFrag extends Fragment {

    /*
     * motion jpeg test URL: http://192.168.1.36/cgi/mjpg/mjpeg.cgi size: 614,
     * 372
     */
    private ImageView doorVideo;
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
//    private Button muteBtn;
    private Button snapBtn;
    private boolean isMute;

    private Timer timer;

    private boolean isOffline;

    private boolean isRefreshing;

    private Bitmap currentCapture;
    
    private Button volumeUpBtn;
    
    private Button volumeDownBtn;

    public DoorFrag() {
    }

    public DoorFrag(String urlStr, String camAcc, String camPwd, int sipPort, DoorActivity doorAct) {
        this.urlStr = urlStr;
        this.camAcc = camAcc;
        this.camPwd = camPwd;
        this.sipPort = sipPort;
        this.doorAct = doorAct;

        try {
            camHost = new URL(this.urlStr).getHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTalkingState() {
        answerBtn.setVisibility(View.INVISIBLE);
    }

    public void onResume() {
        super.onResume();
        resumeVideo();
    }

    public void resumeVideo() {
        if (doorVideo != null) {
            timer = new Timer();
            timer.schedule(new ImgTimerTask(urlStr, doorVideo, timer), 0, 10);
        }
    }

    public void onPause() {
        pauseVideo();
        super.onPause();
    }

    public void pauseVideo() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void teminateCall() {
        if (LinphoneManager.getLc().isIncall()) {
            LinphoneManager.getInstance().terminateCall();
        }
    }

    public void onDestroy() {
        pauseVideo();

        System.gc();
        super.onDestroy();
    }

    private String getLogTag() {
        return getClass().getSimpleName();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_door, container, false);
        initAnswerBtn(rootView);
        initOpenDoorBtn(rootView);
        initHangUpBtn(rootView);
//        initMuteBtn(rootView);
        initSnapBtn(rootView);

        doorVideo = (ImageView) rootView.findViewById(R.id.doorVideo);
        
        initVolumeDownBtn(rootView);
        initVolumeUpBtn(rootView);
        
        return rootView;
    }

    private void initVolumeDownBtn(View rootView) {
        volumeDownBtn = (Button) rootView.findViewById(R.id.volume_down_btn);
        volumeDownBtn.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                ((DoorActivity) getActivity()).volumeDown();
            }
        });
    }

    private void initVolumeUpBtn(View rootView) {
        volumeUpBtn = (Button) rootView.findViewById(R.id.volume_up_btn);
        volumeUpBtn.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                ((DoorActivity) getActivity()).volumeUp();
            }
        });
    }

    private void initSnapBtn(View rootView) {
        snapBtn = (Button) rootView.findViewById(R.id.snapBtn);
        snapBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (currentCapture != null) {
                    new SaveCaptureTask().execute(new Bitmap[] { currentCapture });
                }
            }
        });
    }

//    private void initMuteBtn(View rootView) {
//        muteBtn = (Button) rootView.findViewById(R.id.muteBtn);
//        muteBtn.setOnClickListener(new OnClickListener() {
//
//            public void onClick(View v) {
//                try {
//                    isMute = !isMute;
//                    LinphoneManager.getLc().muteMic(isMute);
//
//                    if (LinphoneManager.getLc().isMicMuted()) {
//                        muteBtn.setBackgroundResource(R.drawable.mute_btn_1);
//                    } else {
//                        muteBtn.setBackgroundResource(R.drawable.mute_btn);
//                    }
//                } catch (Exception e) {
//                    Log.e("ERROR", e.getMessage(), e);
//                }
//            }
//        });
//    }

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

                        setTalkingState();
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

    private class ImgTimerTask extends TimerTask {

        private String urlStr;
        private ImageView imgView;
        private Timer parent;

        public ImgTimerTask(String url, ImageView imgView, Timer parent) {
            this.urlStr = url;
            this.imgView = imgView;
            this.parent = parent;
        }

        public void run() {
            if (!StringUtil.isEmptyString(urlStr) && currentFrag && !isRefreshing) {
                AsyncTask<String, Void, Bitmap> dit = new DownloadImgTask(imgView).execute(new String[] { urlStr });

                try {
                    if (dit.get() == null) {
                        parent.cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    parent.cancel();
                }
            }
        }

    }

    private class SaveCaptureTask extends AsyncTask<Bitmap, Void, Void> {

        protected Void doInBackground(Bitmap... params) {
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            File file = new File(path, "cap.jpg");

            try {
                fOut = new FileOutputStream(file);
                Bitmap capture = params[0];
                capture.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();

                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            } catch (Exception e) {
                Log.e("", e.getMessage(), e);
            } finally {
                if (fOut != null) {
                    try {
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

    }

    private class DownloadImgTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imgView;

        public DownloadImgTask(ImageView imgView) {
            this.imgView = imgView;
        }

        protected Bitmap doInBackground(String... params) {
            isRefreshing = true;
            String urlStr = params[0];
            Bitmap bm = null;

            try {
                java.net.URL url = new java.net.URL(urlStr);
                url.openConnection().setReadTimeout(500);
                InputStream is = url.openStream();
                bm = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                Log.e("", e.getMessage(), e);
            }

            return bm;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imgView.setImageBitmap(result);
                currentCapture = result;
            } else {
                Bitmap errIcon = BitmapFactory.decodeResource(getResources(), R.drawable.disconnect);
                imgView.setImageBitmap(errIcon);
            }

            isRefreshing = false;
        }

    }

}
