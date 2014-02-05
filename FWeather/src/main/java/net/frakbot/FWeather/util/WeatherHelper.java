/*
 * Copyright 2014 Sebastiano Poggi and Francesco Pontillo
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
import net.frakbot.global.Const;
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
    private static final long WEATHER_CACHE_DURATION_MILLIS = 2 * 60 * 60 * 1000;   // Two hours cache expiry time
    private static WeatherData mCachedWeather = null;
    private static long mCachedWeatherTimestamp = Long.MIN_VALUE;
    private static boolean mCacheDataRead = false;

    public static WeatherData getWeather(Context context)
            throws LocationHelper.LocationNotReadyYetException, IOException {
        return getWeather(context, false);
    }

    /**
     * Gets the current weather at the user's location
     *
     * @param context The current {@link Context}.
     *
     * @return Returns the weather info, if available, or null
     * if there was any error during the download.
     */
    public static WeatherData getWeather(Context context, boolean forced)
        throws LocationHelper.LocationNotReadyYetException, IOException {
        FLog.i(context, TAG, "Starting weather update");

        if (forced) {
            FLog.i(context, TAG, "Update was forced, clear the cache.");
            clearCache(context);
        }

        // Read the cached data if needed
        if (!mCacheDataRead) {
            readDataFromCache(context);
            mCacheDataRead = true;
        }

        WeatherData weather;

        if (!checkNetwork(context)) {
            FLog.w(TAG, "No network seems to be available!");

            // Try to resort to cached weather data
            weather = getLatestWeather();
            if (weather != null) {
                FLog.i(TAG, "Using cached weather data...");
                return weather;
            }

            // No cached weather (or stale cached weather data), nothing we can do...
            FLog.e(TAG, "No cached weather available, can't use it either. That's a wrap, ladies and gentlemen");
            weather = new WeatherData();
            weather.conditionCode = WeatherData.WEATHER_ID_ERR_NO_NETWORK;
            return weather;
        }

        // Use manual location if defined
        String manualLocationWoeid = null;
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp != null) {
            manualLocationWoeid = WeatherLocationPreference.getWoeidFromValue(
                sp.getString(context.getString(R.string.pref_key_weather_location), null));
        }

        if (!TextUtils.isEmpty(manualLocationWoeid)) {
            FLog.d(TAG, "Using manual location WOEID");
            YahooWeatherApiClient.LocationInfo locationInfo = new YahooWeatherApiClient.LocationInfo();
            locationInfo.woeids = Arrays.asList(manualLocationWoeid);
            weather = getWeatherDataForLocationInfo(locationInfo);
        }
        else {
            // Get the current location
            final Location location = getLocation(context);

            if (location == null) {
                TrackerHelper.sendException(context, "No location found", false);
                FLog.e(context, TAG, "No location available, can't update");

                WeatherData errWeather = new WeatherData();
                errWeather.conditionCode = WeatherData.WEATHER_ID_ERR_NO_LOCATION;
                return errWeather;
            }

            weather = getWeatherDataForLocation(location);
        }

        FLog.i(context, TAG, "Weather update done");
        if (weather != null) {
            FLog.d(context, TAG, "Got weather:\n\t> " + weather);
        }
        else {
            FLog.v(context, TAG, "No weather received");
        }

        saveDataToCache(context, weather);

        return weather;
    }

    /**
     * Saves the current weather data to both the permanent and
     * in-memory caches.
     *
     * @param context The current {@link Context}.
     * @param weather The weather data to save in the cache
     */
    private static void saveDataToCache(Context context, WeatherData weather) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp == null) {
            FLog.e(TAG, "Unable to access the shared preferences, can't save to cache");
            return;
        }

        if (weather == null) {
            FLog.v(TAG, "Clearing cached weather information (null data)");
            clearCache(context, true);
            return;
        }

        // Update the cached value
        mCachedWeather = weather;
        mCachedWeatherTimestamp = System.currentTimeMillis();

        SharedPreferences.Editor e = sp.edit();
        e.putString(Const.Preferences.LOCATION_CACHE, weather.serializeToString())
         .putLong(Const.Preferences.LOCATION_CACHE_TIMESTAMP, mCachedWeatherTimestamp)
         .commit();

        FLog.v(context, TAG, "Cached weather information updated");
    }

    /**
     * Retrieved the last weather data from the permanent cache, if still valid.
     *
     * @param context The current {@link android.content.Context}.
     */
    private static void readDataFromCache(Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp == null) {
            FLog.e(TAG, "Unable to access the shared preferences, can't read from cache");
            return;
        }

        // Read the cached value
        mCachedWeatherTimestamp = sp.getLong(Const.Preferences.LOCATION_CACHE_TIMESTAMP, Long.MIN_VALUE);
        mCachedWeather = WeatherData.deserializeFromString(sp.getString(Const.Preferences.LOCATION_CACHE, null));

        // Validate the cache age
        if (!isLatestWeatherStillGood()) {
            mCachedWeatherTimestamp = Long.MIN_VALUE;
        }

        FLog.v(context, TAG, "Cached weather information retrieved from permanent storage");
    }

    /**
     * Clear the cache and resets the cache-handling objects.
     * @param context The current {@link Context}.
     * @param persist true to persist to {@link android.content.SharedPreferences}, false to clear the memory cache
     */
    private static void clearCache(Context context, boolean persist) {
        FLog.v(TAG, "Clearing cached weather information (as requested)");
        mCachedWeather = null;
        mCachedWeatherTimestamp = Long.MIN_VALUE;

        if (persist) {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (sp == null) {
                FLog.e(TAG, "Unable to access the shared preferences, can't save to cache");
                return;
            }

            SharedPreferences.Editor e = sp.edit();
            e.remove(Const.Preferences.LOCATION_CACHE)
                    .remove(Const.Preferences.LOCATION_CACHE_TIMESTAMP)
                    .commit();
        }
    }
    /**
     * Clear the in memory cache and resets the cache-handling objects.
     * @param context The current {@link Context}.
     */
    private static void clearCache(Context context) {
        clearCache(context, false);
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
        // If the cached weather has become stale
        if (!isLatestWeatherStillGood()) {
            // Clear the cached timestamp
            FLog.v(TAG, "Stale cache detected, clearing the cached weather");
            mCachedWeatherTimestamp = Long.MIN_VALUE;
            // Clear the cache value
            mCachedWeather = null;
        }

        return mCachedWeather;
    }

    /**
     * Returns a value indicating wether the latest known weather information
     * is still fresh enough to be used, or if it's become stale.
     *
     * @return Returns true if there is a cached weather and if said cached
     * weather data is not stale.
     */
    public static boolean isLatestWeatherStillGood() {
        final long weatherAgeMillis = getLatestWeatherAgeMillis();
        return mCachedWeatherTimestamp != Long.MIN_VALUE &&
               mCachedWeather != null &&
               weatherAgeMillis < WEATHER_CACHE_DURATION_MILLIS;
    }

    /**
     * Returns a value indicating the age in milliseconds of the latest known
     * weather information, if any is available.
     *
     * @return Returns the cache age if there is a cached weather, or
     * {@link Long#MIN_VALUE} if there is no cached weather.
     */
    public static long getLatestWeatherAgeMillis() {
        if (mCachedWeather == null) {
            mCachedWeatherTimestamp = Long.MIN_VALUE;
            return mCachedWeatherTimestamp;
        }

        return System.currentTimeMillis() - mCachedWeatherTimestamp;
    }

    private static WeatherData getWeatherDataForLocation(Location location) {
        WeatherData weatherData = null;
        try {
            FLog.d(TAG, "Using location: " + location.getLatitude() + "," + location.getLongitude());
            weatherData = getWeatherWithRetry(getLocationInfoWithRetry(location));
        }
        catch (CantGetWeatherException e) {
            FLog.e(TAG, "Unable to retrieve weather", e);
        }
        return weatherData;
    }

    private static WeatherData getWeatherDataForLocationInfo(YahooWeatherApiClient.LocationInfo location) {
        try {
            FLog.d(TAG, "Using manual location. WOEIDs count: " + location.woeids.size());
            return getWeatherWithRetry(location);
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

    /**
     * Internal method to retry weather fetching from the Yahoo weather provider.
     * @param location  The {@link net.frakbot.FWeather.updater.weather.YahooWeatherApiClient.LocationInfo}
     * @return          The {@link net.frakbot.FWeather.updater.weather.model.WeatherData} containing weather information
     * @throws CantGetWeatherException  If there's some network error
     */
    private static WeatherData getWeatherWithRetry(YahooWeatherApiClient.LocationInfo location)
            throws CantGetWeatherException {
        CantGetWeatherException lastException = null;
        for (int i = 0; i < Const.Thresholds.MAX_FETCH_WEATHER_ATTEMPTS; i++) {
            try {
                WeatherData weatherData = YahooWeatherApiClient.getWeatherForLocationInfo(location);
                return weatherData;
            } catch (CantGetWeatherException e) {
                FLog.w(TAG, String.format(
                        "Weather fetching attempt number %d has failed. %d attempts remaining.",
                        i+1, Const.Thresholds.MAX_FETCH_WEATHER_ATTEMPTS-i-1));
                // Save the last exception for me
                lastException = e;
            }
        }
        FLog.e(TAG, String.format(
                "Maximum number (%d) of weather fetching attempts reached. Giving up.",
                Const.Thresholds.MAX_FETCH_WEATHER_ATTEMPTS));
        // If we are here, it means that MAX_FETCH_WEATHER_ATTEMPTS have been made without a result, give up!
        throw lastException;
    }

    /**
     * Internal method to retry location fetching from the Yahoo weather provider.
     * @param location  The {@link net.frakbot.FWeather.updater.weather.YahooWeatherApiClient.LocationInfo}
     * @return          The {@link net.frakbot.FWeather.updater.weather.model.WeatherData} containing weather information
     * @throws CantGetWeatherException  If there's some network error
     */

    /**
     * Internal method to retry location fetching from the Yahoo weather provider.
     * @param location  The known {@link android.location.Location}
     * @return          The {@link net.frakbot.FWeather.updater.weather.YahooWeatherApiClient.LocationInfo} returned
     *                  by the Yahoo weather provider
     * @throws CantGetWeatherException  If there's some parsing or network error
     */
    private static YahooWeatherApiClient.LocationInfo getLocationInfoWithRetry(Location location)
            throws CantGetWeatherException {
        CantGetWeatherException lastException = null;
        for (int i = 0; i < Const.Thresholds.MAX_FETCH_LOCATION_ATTEMPTS; i++) {
            try {
                YahooWeatherApiClient.LocationInfo locationInfo = getLocationInfo(location);
                return locationInfo;
            } catch (CantGetWeatherException e) {
                FLog.w(TAG, String.format(
                        "Location fetching attempt number %d has failed. %d attempts remaining.",
                        i+1, Const.Thresholds.MAX_FETCH_LOCATION_ATTEMPTS-i-1));
                // Save the last exception for me
                lastException = e;
            }
        }
        FLog.e(TAG, String.format(
                "Maximum number (%d) of Location fetching attempts reached. Giving up.",
                Const.Thresholds.MAX_FETCH_LOCATION_ATTEMPTS));
        // If we are here, it means that MAX_FETCH_WEATHER_ATTEMPTS have been made without a result, give up!
        throw lastException;
    }

}
