package com.avadesign;

import java.text.MessageFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


/**
 * Weather url sample: http://weather.yahooapis.com/forecastrss?w=28752308&u=c
 * 
 * woeid: 28752308
 * */
public class LocationWeatherActivity extends Activity {

    private TextView locationLbl;
    private LocationManager lm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_weather);

        locationLbl = (TextView) findViewById(R.id.locationLbl);
    }

    private class WOEIDTask extends AsyncTask<Double, Void, String> {

        protected String doInBackground(Double... params) {
            String qWUrlPattern = getString(R.string.woeid_url);
            String qWUrlStr = MessageFormat.format(qWUrlPattern, params[0], params[1]);

            try {
                HttpParams httpParams = new BasicHttpParams();

                HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                HttpConnectionParams.setSoTimeout(httpParams, 5000);

                DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

                HttpResponse httpRes = httpClient.execute(new HttpGet(qWUrlStr));

                if (httpRes.getStatusLine().getStatusCode() == 200) {
                    return new String(EntityUtils.toString(httpRes.getEntity()).getBytes("ISO-8859-1"), "UTF-8");
                } else {
                    return "Status: " + httpRes.getStatusLine().getStatusCode();
                }
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                return e.getMessage();
            }
        }

        protected void onPreExecute() {
            runOnUiThread(new Runnable() {

                public void run() {
                    locationLbl.setText("Wait...");
                }
            });
        }

        protected void onPostExecute(String result) {
            String xmlSource = result;
            String woeid = "";
            
            try {
                Document doc = DocumentHelper.parseText(xmlSource);
                
                for (Object o : doc.selectNodes("query/results/Result/woeid")) {
                    Element ele = (Element) o;
                    woeid = ele.getText();
                }
                
                Log.e(getClass().getSimpleName(), "WOEID: " + woeid);
                final String str = woeid;
                
                runOnUiThread(new Runnable() {
                    
                    public void run() {
                        locationLbl.setText("WOEID: " + str);
                    }
                });
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        }

    }

    protected void onStart() {
        super.onStart();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        Location loc = lm.getLastKnownLocation(lm.getBestProvider(c, true));

        if (loc != null) {
            try {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();
                new WOEIDTask().execute(lat, lon);
            } catch (Exception e) {
                Log.e("Location", e.getMessage(), e);
                locationLbl.setText(e.getMessage());
            }
        } else {
            locationLbl.setText("無法取得位置資訊!");
        }
    }

}
