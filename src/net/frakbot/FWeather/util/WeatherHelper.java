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

package net.frakbot.FWeather.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.updater.weather.CantGetWeatherException;
import net.frakbot.FWeather.updater.weather.YahooWeatherApiClient;
import net.frakbot.FWeather.updater.weather.model.WeatherData;
import net.frakbot.util.log.FLog;

import java.io.IOException;
import java.util.Arrays;

import static net.frakbot.FWeather.updater.weather.YahooWeatherApiClient.getLocationInfo;

/**
 * Helper class for retrieving weather information.
 * <p/>
 * Parts from Roman Nurik's DashClock.
 *
 * @author Francesco Pontillo and Sebastiano Poggi
 */
public class WeatherHelper {

    private static final String TAG = WeatherHelper.class.getSimpleName();
    private static WeatherData mCachedWeather = null;

    /**
     * Gets the current weather at the user's location
     *
     * @param context The current {@link Context}.
     *
     * @return Returns the weather info, if available, or null
     * if there was any error during the download.
     */
    public static WeatherData getWeather(Context context)
        throws LocationHelper.LocationNotReadyYetException, IOException {
        FLog.i(context, TAG, "Starting weather update");

        // Get the current location
        final Location location = getLocation(context);

        if (location == null) {
            TrackerHelper.sendException(context, "No location found", false);
            FLog.e(context, TAG, "No location available, can't update");

            WeatherData errWeather = new WeatherData();
            errWeather.conditionCode = WeatherData.WEATHER_ID_ERR_NO_LOCATION;
            return errWeather;
        }

        // Use manual location if defined
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String manualLocationWoeid =
            WeatherLocationPreference.getWoeidFromValue(
                sp.getString(context.getString(R.string.pref_key_weather_location), null));


        WeatherData weather;
        if (!TextUtils.isEmpty(manualLocationWoeid)) {
            YahooWeatherApiClient.LocationInfo locationInfo = new YahooWeatherApiClient.LocationInfo();
            locationInfo.woeids = Arrays.asList(manualLocationWoeid);
            weather = getWeatherDataForLocationInfo(locationInfo);
        }
        else {
            weather = getWeatherDataForLocation(location);
        }

        FLog.i(context, TAG, "Weather update done");
        if (weather != null) {
            FLog.d(context, TAG, "Got weather:\n\t> " + weather);
        }
        else {
            FLog.v(context, TAG, "No weather received");
        }

        // Update the cached value
        mCachedWeather = weather;
        FLog.v(context, TAG, "Cached weather information updated");

        return weather;
    }

    /**
     * Gets the current location.
     *
     * @param context The current {@link Context}.
     *
     * @return Returns the current location
     */
    public static Location getLocation(Context context) throws LocationHelper.LocationNotReadyYetException {
        final Intent intent = WidgetHelper.getUpdaterIntent(context, false, false);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 42, intent, 0);
        return LocationHelper.getLastKnownSurroundings(pendingIntent);
    }

    /**
     * Checks if there is any network connection active (or activating).
     *
     * @param context The current {@link Context}.
     *
     * @return Returns true if there is an active connection, false otherwise
     */
    public static boolean checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Returns the latest known weather information.
     *
     * @return The cached {@link WeatherData}
     */
    public static WeatherData getLatestWeather() {
        return mCachedWeather;
    }

    private static WeatherData getWeatherDataForLocation(Location location) {
        try {
            FLog.d(TAG, "Using location: " + location.getLatitude() + "," + location.getLongitude());
            return YahooWeatherApiClient.getWeatherForLocationInfo(getLocationInfo(location));
        }
        catch (CantGetWeatherException e) {
            FLog.e(TAG, "Unable to retrieve weather", e);
            return null;
        }
    }

    private static WeatherData getWeatherDataForLocationInfo(YahooWeatherApiClient.LocationInfo location) {
        try {
            FLog.d(TAG, "Using manual location. WOEIDs count: " + location.woeids.size());
            return YahooWeatherApiClient.getWeatherForLocationInfo(location);
        }
        catch (CantGetWeatherException e) {
            FLog.e(TAG, "Unable to retrieve weather", e);
            return null;
        }
        catch (NullPointerException e) {
            FLog.e(TAG, "Unable to retrieve weather: no WOEIDs!", e);
            return null;
        }
    }

}
