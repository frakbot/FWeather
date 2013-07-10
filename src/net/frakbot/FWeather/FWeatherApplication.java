/*
 * Copyright 2013 Sebastiano Poggi and Francesco Pontillo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frakbot.FWeather;

import android.app.Application;
import android.util.Log;
import android.preference.PreferenceManager;
import net.frakbot.FWeather.global.Const;
import net.frakbot.FWeather.util.FLog;
import net.frakbot.FWeather.util.LogLevel;

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
        Log.i(Const.APP_NAME, "App starting...");

        FLog.initLog(this);

        //noinspection ConstantConditions
        FLog.setLogLevel(BuildConfig.DEBUG ? LogLevel.VERBOSE : null);

        FLog.d("Application", "FLog up and running");

        super.onCreate();

        // This will fail if you didn't define your own API key string!
        mApiKey = getString(R.string.weather_api_key);

        // Set the default preference values stored in the xml files
        PreferenceManager.setDefaultValues(this, R.xml.pref_advanced, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_customization, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
    }

    /**
     * Returns the OpenWeatherMap API key.
     *
     * @return Returns the OpenWeatherMap API key
     */
    public static String getApiKey() {
        return mApiKey;
    }
}
