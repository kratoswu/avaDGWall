package com.avadesign;

import java.io.InputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avadesign.R;
import com.avadesign.util.StringUtil;

public class ActivateCodeActivity extends Activity {
    
    private TextView actCodeLbl;
    private ImageView qrcodeImg;
    
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
            startActivity(new Intent(this, SettingMainScreenActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activatecode);
        
        actCodeLbl = (TextView) findViewById(R.id.activateCodeLbl);
        qrcodeImg = (ImageView) findViewById(R.id.qrcodeImgView);
        
        String actCode = getActCode();
        
        if (!StringUtil.isEmptyString(actCode)) {
            String urlStr = "http://54.215.11.15:8080/avaCamera/ActiveQRServlet?actCode=" + actCode;
            actCodeLbl.setText("Activate Code: " + actCode);
            new DownloadImgTask(qrcodeImg).execute(new String[]{urlStr});
        } else {
            Toast.makeText(this, "Cannot get activate code.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getActCode() {
        String jsonSrc = ((SharedClassApp) getApplication()).getAppPref().getValue(getString(R.string.key_cam_data));
        
        try {
            JSONObject jsonObj = new JSONObject(jsonSrc);
            return jsonObj.getString("activeCode");
        } catch (Exception e) {
            Log.e("", e.getMessage(), e);
        }
        
        return null;
    }
    
    private class DownloadImgTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imgView;

        public DownloadImgTask(ImageView imgView) {
            this.imgView = imgView;
        }

        protected Bitmap doInBackground(String... params) {
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
            } else {
                Bitmap errIcon = BitmapFactory.decodeResource(getResources(), R.drawable.disconnect);
                imgView.setImageBitmap(errIcon);
            }
        }

    }

}
