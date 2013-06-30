package net.frakbot.FWeather;

import android.app.Application;

/**
 * Application handler
 * <p/>
 * Author: Sebastiano Poggi
 * Created on: 6/30/13 Time: 7:30 PM
 * File version: 1.0
 * <p/>
 * Changelog:
 * Version 1.0
 * * Initial revision
 */
public class FWeatherApplication extends Application {

    private static String mApiKey;

    @Override
    public void onCreate() {
        super.onCreate();

        // This will fail if you didn't define your own API key string!
        mApiKey = getString(R.string.weather_api_key);
    }

    public static String getApiKey() {
        return mApiKey;
    }
}
