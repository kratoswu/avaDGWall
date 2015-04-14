package com.avadesign;

import java.text.MessageFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.avadesign.util.AvaPref;

public class SceneSettingActivity extends Activity {
    
    private WebView contentView;
    
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
        setContentView(R.layout.activity_scene_setting);
        
        initContentView();
    }
    
    private String getFmStr(String pattern, Object... args) {
        return MessageFormat.format(pattern, args);
    }

    private void initContentView() {
        contentView = (WebView) findViewById(R.id.contentView);
        
        /*
         * 要設 WebViewClient, 不然會變成由 browser 去開連結
         * */
        AvaPref pref = ((SharedClassApp) getApplication()).getAppPref();
        String urlPattern = "http://{0}:{1}/PadVersion/scene";
        String hostPattern = "{0}:{1}";
        String hostAddr = pref.getValue(getString(R.string.key_gateway_ip));
        String hostPort = pref.getValue(getString(R.string.key_gateway_port));
        String acc = pref.getValue(getString(R.string.key_acc));
        String pwd = pref.getValue(getString(R.string.key_pwd));
        contentView.setWebViewClient(new ContentViewClient());
        contentView.setHttpAuthUsernamePassword(getFmStr(hostPattern, hostAddr, hostPort), "", acc, pwd);
        
        WebSettings contentSettings = contentView.getSettings();
        contentSettings.setLoadsImagesAutomatically(true);
        contentSettings.setJavaScriptEnabled(true);
        
        contentView.loadUrl(getFmStr(urlPattern, hostAddr, hostPort));
    }
    
    private class ContentViewClient extends WebViewClient {

        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            handler.proceed("admin", "admin");
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            
            return true;
        }
        
    }

}
