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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
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
    private static String mUserAgent;

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

        initUserAgent();

        // Set the default preference values stored in the xml files
        PreferenceManager.setDefaultValues(this, R.xml.pref_advanced, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_customization, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
    }

    /**
     * Initializes the User-Agent string for the app.
     */
    private void initUserAgent() {
        // Get the app name and version
        PackageManager pm;
        String packageName;
        String versionName;
        pm = getPackageManager();
        packageName = getPackageName();

        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;

            mUserAgent = String.format("%1$s/%2$s", Const.APP_NAME, versionName);
        }
        catch (PackageManager.NameNotFoundException e) {
            // Unable to retrieve app info, solely rely on the hardcoded app name
            mUserAgent = Const.APP_NAME;
        }
    }

    /**
     * Returns the Yahoo! Weather API key.
     *
     * @return Returns the Yahoo! Weather API key
     */
    public static String getApiKey() {
        return mApiKey;
    }

    /**
     * Returns the User-Agent string to use for HTTP headers.
     * @return Returns the User-Agent string
     */
    public static String getUserAgent() {
        return mUserAgent;
    }
}
