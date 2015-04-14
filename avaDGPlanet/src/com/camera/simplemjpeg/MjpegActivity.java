package com.camera.simplemjpeg;

import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.avadesign.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MjpegActivity extends Activity {
	private static final boolean DEBUG=true;
    private static final String TAG = "MJPEG";

    private MjpegView mv = null;

    private int width = 640;
    private int height = 480;

    private boolean suspending = false;

	final Handler handler = new Handler();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mjpeg);
        mv = (MjpegView) findViewById(R.id.mv);
        if(mv != null){
        	mv.setResolution(width, height);
        }

        new DoRead().execute(new String[0]);
    }


    public void onResume() {
    	if(DEBUG) Log.d(TAG,"onResume()");
        super.onResume();
        if(mv!=null){
        	if(suspending){
        		new DoRead().execute(new String[0]);
        		suspending = false;
        	}
        }

    }

    public void onStart() {
    	if(DEBUG) Log.d(TAG,"onStart()");
        super.onStart();
    }
    public void onPause() {
    	if(DEBUG) Log.d(TAG,"onPause()");
        super.onPause();
        if(mv!=null){
        	if(mv.isStreaming()){
		        mv.stopPlayback();
		        suspending = true;
        	}
        }
    }
    public void onStop() {
    	if(DEBUG) Log.d(TAG,"onStop()");
        super.onStop();
    }

    public void onDestroy() {
    	if(DEBUG) Log.d(TAG,"onDestroy()");

    	if(mv!=null){
    		mv.freeCameraMemory();
    	}

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return false;
    }

    public void setImageError(){
    	handler.post(new Runnable() {
    		@Override
    		public void run() {
    			setTitle("Image Error!");
    			return;
    		}
    	});
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5*1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5*1000);
            if(DEBUG) Log.d(TAG, "1. Sending http request");
            try {
                String urlStr = "http://192.168.1.36/cgi/mjpg/mjpeg.cgi";
                URL urlu = new URL(urlStr);

                httpclient.getCredentialsProvider().setCredentials(new AuthScope(urlu.getHost(), urlu.getPort()),
                        new UsernamePasswordCredentials("admin", "admin"));

                HttpGet req = new HttpGet(urlStr);
                res = httpclient.execute(req);

                Log.i(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (Exception e) {
            	if(DEBUG){
	                e.printStackTrace();
	                Log.d(TAG, "Request failed-ClientProtocolException", e);
            	}
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);

            if(result!=null){
            	result.setSkip(1);
            }

            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mv.showFps(false);
        }
    }

    public class RestartApp extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... v) {
        	MjpegActivity.this.finish();
            return null;
        }

        protected void onPostExecute(Void v) {
        	startActivity((new Intent(MjpegActivity.this, MjpegActivity.class)));
        }
    }
}
