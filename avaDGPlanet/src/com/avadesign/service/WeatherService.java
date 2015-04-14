package com.avadesign.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.avadesign.R;
import com.avadesign.SharedClassApp;
import com.avadesign.util.StringUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * 取得天氣資訊的 service. 先用定位取得目前經緯度後, 再以經緯度取得 Yahoo place 的 WOEID. <br />
 * Yahoo Weather API: <br />
 * http://weather.yahooapis.com/forecastrss?w=28752308&u=c <br />
 * w 是指 WOEID <br />
 * u 是溫度單位, c 表示為攝氏, f 表示為華氏.
 * */
public class WeatherService extends Service {

    public static final String TEMP = "temp";

    public static final String WEATHER_CODE = "weatherCode";

    public static final String REFRESH_WEATHER = "REFRESH_WEATHER";

    public static final String LOCATION_ERROR = "LOCATION_ERROR";

    public static final String WEATHER_ERROR = "WEATHER_ERROR";

    private static final String tag = WeatherService.class.getSimpleName();

    private LocationManager lm;

    private Timer timer;

    private long refreshTime = 1000 * 60 * 5;

    private TimerTask timerTask = new TimerTask() {

        public void run() {
            getWeatherInfo();
        }
    };

    private void broadcastWeatherError() {
        sendBroadcast(new Intent(WEATHER_ERROR));
    }

    private void broadcastLocationError() {
        sendBroadcast(new Intent(LOCATION_ERROR));
    }

    private Intent getYahooWeatherInfo(String woeid, String unit) throws Exception {
        String wUrl = getString(R.string.weather_url) + "?w=" + woeid + "&u=" + unit;
        Log.v("Weather URL", wUrl);

        HttpParams httpParams = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpResponse httpRes = httpClient.execute(new HttpGet(wUrl));

        if (httpRes.getStatusLine().getStatusCode() == 200) {
            String code = "";
            String temp = "";
            String xmlSource = new String(EntityUtils.toString(httpRes.getEntity()).getBytes("ISO-8859-1"), "UTF-8");
            Document doc = DocumentHelper.parseText(xmlSource);

            for (Object o : doc.selectNodes("rss/channel/item/yweather:condition")) {
                Element ele = (Element) o;

                for (Object oAtt : ele.attributes()) {
                    Attribute attr = (Attribute) oAtt;

                    if (attr.getName().equals("code")) {
                        code = attr.getValue();
                    } else if (attr.getName().equals(TEMP)) {
                        temp = attr.getValue();
                    }
                }
            }

            Intent it = new Intent(REFRESH_WEATHER);

            // TODO
            Log.v("weatherCode", code);
            it.putExtra(WEATHER_CODE, code);
            SharedClassApp app = (SharedClassApp) getApplication();
            app.setWeatherCode(code);

            Log.v("temperature", temp);
            it.putExtra(TEMP, temp);
            app.setTemperature(temp);

            return it;
        } else {
            throw new Exception("HTTP ERROR, Code: " + httpRes.getStatusLine().getStatusCode());
        }
    }

    /**
     * 取得目前位置
     * */
    private Location getCurrentLocation() throws Exception {
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        location = location == null ? lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) : location;
        return lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * 以經緯度, 透過 yahoo 的 query API 取得 WOEID
     * */
    private String getWoeid(double latitude, double longitude) throws Exception {
        String qWUrlPattern = getString(R.string.woeid_url);
        String qWUrlStr = MessageFormat.format(qWUrlPattern, latitude, longitude);

        HttpParams httpParams = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpResponse httpRes = httpClient.execute(new HttpGet(qWUrlStr));

        if (httpRes.getStatusLine().getStatusCode() == 200) {
            String xmlSource = new String(EntityUtils.toString(httpRes.getEntity()).getBytes("ISO-8859-1"), "UTF-8");
            String woeid = "";

            // Parsing xml response
            Document doc = DocumentHelper.parseText(xmlSource);

            for (Object o : doc.selectNodes("query/results/Result/woeid")) {
                Element ele = (Element) o;
                woeid = ele.getText();
            }

            if (StringUtil.isEmptyString(woeid)) {
                throw new Exception("Cannot get woeid!");
            }

            return woeid;
        } else {
            throw new Exception("HTTP ERROR, Code: " + httpRes.getStatusLine().getStatusCode());
        }
    }

    public void onCreate() {
        super.onCreate();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        timer = new Timer();
        timer.schedule(timerTask, 0, refreshTime);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(tag, tag + " start.....");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }

        Log.v(tag, "WeatherService stop......");
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getWeatherInfo() {
//        try {
//            Location loc = getCurrentLocation();
//
//            if (loc == null) {
//                broadcastLocationError();
//            } else {
//                try {
//                    sendBroadcast(getYahooWeatherInfo(getWoeid(loc.getLatitude(), loc.getLongitude()), "c"));
//                } catch (Exception e) {
//                    Log.e(tag, e.getMessage(), e);
//                    broadcastWeatherError();
//                }
//            }
//        } catch (Exception e) {
//            Log.w(getClass().getSimpleName(), e.getMessage(), e);
//        }

        try {
            /*
             * TODO 2014-02-16, edited by Phoenix
             * 先給客戶至漢諾威參展, 直接寫漢諾威的 woeid
             * */
            Log.e("getWeatherInfo", "");
//            sendBroadcast(getYahooWeatherInfo("657169", "c"));
            sendBroadcast(getYahooWeatherInfo("2436704", "c"));
        } catch (Exception e) {
            Log.e(tag, e.getMessage(), e);
            broadcastWeatherError();
        }
    }

}
