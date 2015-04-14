package com.avadesign;

import java.util.List;

import org.linphone.CallManager;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneSimpleListener.LinphoneOnCallStateChangedListener;
import org.linphone.LinphoneUtils;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.PayloadType;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.avadesign.util.StringUtil;

public class AvaCallActivity extends Activity implements LinphoneOnCallStateChangedListener {

    private Handler handler;

    private SurfaceView videoView;

    private AndroidVideoWindowImpl androidVideoWindowImpl;

    private Button videoAnsBtn;

    private Button hangUpBtn;

    private LinphoneCall call;

    private boolean isIncommingCall;

    public void volumeUp(View v) {
        AudioManager am=(AudioManager) getSystemService(AUDIO_SERVICE);
        am.adjustStreamVolume (AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    public void volumeDown(View v) {
        AudioManager mAudioManager=(AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioManager.adjustStreamVolume (AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

//    public void captureScn(View v) {
//        // TODO
//        if (videoView != null) {
//            videoView.buildDrawingCache(true);
//            Bitmap capture = videoView.getDrawingCache(true).copy(Config.ARGB_8888, false);
//            videoView.destroyDrawingCache();
//
//            if (capture != null) {
//                new SaveCaptureTask().execute(new Bitmap[] { capture });
//            } else {
//                Log.e("Capture failed", "Cannot get capture from surfaceview.");
//            }
//        }
//    }

//    public Bitmap screenShot(View view) {
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//        return bitmap;
//    }

//    private class SaveCaptureTask extends AsyncTask<Bitmap, Void, Void> {
//
//        protected Void doInBackground(Bitmap... params) {
//            String path = Environment.getExternalStorageDirectory().toString();
//            OutputStream fOut = null;
//            File file = new File(path + "/DCIM/Camera", "cap.jpg");
//
//            try {
//                fOut = new FileOutputStream(file);
//                Bitmap capture = params[0];
//                capture.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                fOut.flush();
//
////                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
//            } catch (Exception e) {
//                Log.e("", e.getMessage(), e);
//            } finally {
//                if (fOut != null) {
//                    try {
//                        fOut.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            return null;
//        }
//    }

    protected void onDestroy() {
        if (videoView != null) {
            videoView = null;
        }

        LinphoneManager.removeListener(this);

        super.onDestroy();
    }

    protected void onPause() {
        if (androidVideoWindowImpl != null) {
            synchronized (androidVideoWindowImpl) {
                LinphoneManager.getLc().setVideoWindow(null);
            }
        }

        if (videoView != null) {
            ((GLSurfaceView) videoView).onPause();
        }

        super.onPause();
    }

    protected void onResume() {
        super.onResume();

        if (videoView != null) {
            ((GLSurfaceView) videoView).onResume();
        }

        if (androidVideoWindowImpl != null) {
            synchronized (androidVideoWindowImpl) {
                LinphoneManager.getLc().setVideoWindow(androidVideoWindowImpl);
            }
        }
    }

    private void turnOffCam() {
        LinphonePreferences mPrefs = LinphonePreferences.instance();
        mPrefs.enableVideo(false);
        mPrefs.setInitiateVideoCall(false);
        mPrefs.setAutomaticallyAcceptVideoRequests(false);
    }

    // public void switchAudio(View v) {
    // // Audio answer
    // if (call == null) {
    // return;
    // }
    //
    // isVideoCall = false;
    // videoAnsBtn.setVisibility(View.INVISIBLE);
    //
    // turnOffCam();
    // answer();
    // }

    public void switchVideo(View v) {
        // Video answer
        if (call == null) {
            return;
        }

        // isVideoCall = true;
        videoAnsBtn.setVisibility(View.INVISIBLE);
        videoView.setBackgroundResource(0);

        answer();

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        LinphonePreferences mPrefs = LinphonePreferences.instance();

        turnOnCam(lc, mPrefs);

        getHandler().post(new Runnable() {

            public void run() {
                if (!call.getRemoteParams().isLowBandwidthEnabled()) {
                    LinphoneManager.getInstance().addVideo();
                    showVideoView();
                }
            }
        });
    }

    public void answer() {
        if (call != null) {
            Log.e("call state", call.getState().toString());
            LinphoneCallParams params = LinphoneManager.getLc().createDefaultCallParameters();
            params.enableLowBandwidth(!LinphoneUtils.isHightBandwidthConnection(this));

            if (LinphoneManager.getInstance().acceptCallWithParams(call, params)) {
                Toast.makeText(this, "accepted call", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Getting call failed...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showVideoView() {
        Log.w(getClass().getSimpleName(), "Bluetooth not available, using speaker");
        LinphoneManager.getInstance().routeAudioToSpeaker();
        LinphoneManager.stopProximitySensorForActivity(this);
    }

    public void hangUp(View v) {
        hangUp();
    }

    private void hangUp() {
        LinphoneManager.getLcIfManagerNotDestroyedOrNull().terminateAllCalls();
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinphoneManager.addListener(this);

        LinphoneCore linCore = LinphoneManager.getLcIfManagerNotDestroyedOrNull();

        if (linCore != null) {
            List<LinphoneCall> calls = LinphoneUtils.getLinphoneCalls(linCore);

            for (LinphoneCall call : calls) {
                if (call.getState() == LinphoneCall.State.IncomingReceived) {
                    this.call = call;
                    Log.e(getClass().getSimpleName(), "Got a call");
                    LinphoneManager.getInstance().changeStatusToOnThePhone();
                    isIncommingCall = true;
                }

                break;
            }
        }

        if (call == null) {
            Intent it = getIntent();
            String sipID = it.getStringExtra("sipID");

            if (!StringUtil.isEmptyString(sipID)) {
                Toast.makeText(this, sipID, Toast.LENGTH_SHORT).show();
                LinphoneManager.getInstance().newOutgoingCall(sipID, "");
                isIncommingCall = false;
            } else {
                /*
                 * 沒 call, 也取不到要撥打的 sip ID, 直接結束.
                 */
                finish();
                return;
            }
        }

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        LinphonePreferences mPrefs = LinphonePreferences.instance();

        mPrefs.setEchoCancellation(true);

        for (final PayloadType pt : lc.getAudioCodecs()) {
            try {
                if (pt.getMime().equals("PCMA") || pt.getMime().equals("PCMU"))
                    LinphoneManager.getLcIfManagerNotDestroyedOrNull().enablePayloadType(pt, true);
                else
                    LinphoneManager.getLcIfManagerNotDestroyedOrNull().enablePayloadType(pt, false);
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        if (LinphoneManager.getLc().getCallsNb() > 0) {
            LinphoneCall call = LinphoneManager.getLc().getCurrentCall();
            LinphoneUtils.isCallEstablished(call);
        }

        // Initialize layout
        setContentView(R.layout.activity_avacall);

        videoAnsBtn = (Button) findViewById(R.id.answerBtn);
        hangUpBtn = (Button) findViewById(R.id.hangupBtn);

        /*
         * 這時的 video view 還是 invisible 的狀態
         */
        videoView = (SurfaceView) findViewById(R.id.videoSurface);
        androidVideoWindowImpl = new AndroidVideoWindowImpl(videoView);
        androidVideoWindowImpl.setListener(new AndroidVideoWindowImpl.VideoWindowListener() {

            public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                LinphoneManager.getLc().setVideoWindow(vw);
                videoView = surface;
            }

            public void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl vw) {
                try {
                    LinphoneManager.getLc().setVideoWindow(null);
                } catch (Exception e) {
                }
            }

            public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                // Do nothing
            }

            public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl vw) {
                // Do nothing
            }
        });

        androidVideoWindowImpl.init();

        /*
         * Initialize 完畢, 可以顯示 video.
         */
        videoView.setVisibility(View.VISIBLE);
        // isVideoCall = getIntent().getBooleanExtra("isVideoCall", false);

        // if (!isVideoCall) {
        // videoView.setBackgroundResource(R.drawable.test_img);
        // }

//        if (StringUtil.isEmptyString(getIntent().getStringExtra("sipID"))) {
//            videoAnsBtn.setVisibility(View.VISIBLE);
//        } else {
            videoAnsBtn.setVisibility(View.INVISIBLE);
//        }

        if (isIncommingCall) {
            answer();
        }
    }

    private void turnOnCam(LinphoneCore lc, LinphonePreferences mPrefs) {
        // 打開 pad 的相機鏡頭
        try {
            int videoDeviceId = LinphoneManager.getLc().getVideoDevice();
            AndroidCamera[] camArr = AndroidCameraConfiguration.retrieveCameras();

            for (AndroidCamera cam : camArr) {
                Log.e("isFrontFacing", cam.frontFacing + "");

                /*
                 * 2015-01-06, commentted by Phoenix. Note: 有些機種的前鏡頭,
                 * frontFacing 會是 false, 所以得實機測過, 依據實際的情況去調整.
                 */
                if (cam.frontFacing) {
                    videoDeviceId = cam.id;
                    break;
                }
            }

            LinphoneManager.getLc().setVideoDevice(videoDeviceId);
            CallManager.getInstance().updateCall();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }

        mPrefs.enableVideo(true);
        mPrefs.setInitiateVideoCall(true);
        mPrefs.setAutomaticallyAcceptVideoRequests(true);

        for (final PayloadType pt : lc.getVideoCodecs()) {
            try {
                if (pt.getMime().equals("H264"))
                    LinphoneManager.getLcIfManagerNotDestroyedOrNull().enablePayloadType(pt, true);
                else
                    LinphoneManager.getLcIfManagerNotDestroyedOrNull().enablePayloadType(pt, false);
            } catch (LinphoneCoreException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }

        return handler;
    }

    public void onCallStateChanged(LinphoneCall call, State state, String message) {
        /*
         * Tracing call state for debug, do not remove.
         */
         Log.e("State", state.toString());
        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();

        if (state == LinphoneCall.State.Connected) {
            LinphonePreferences mPrefs = LinphonePreferences.instance();
            turnOnCam(lc, mPrefs);
        } else if (state == LinphoneCall.State.CallEnd || state == LinphoneCall.State.Error) {
            hangUp();
        }
    }

}
