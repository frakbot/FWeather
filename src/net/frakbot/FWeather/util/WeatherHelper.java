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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import net.frakbot.FWeather.updater.weather.JSONWeatherParser;
import net.frakbot.FWeather.updater.weather.WeatherHttpClient;
import net.frakbot.FWeather.updater.weather.model.Weather;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Helper class for retrieving weather information.
 * @author Francesco Pontillo
 */
public class WeatherHelper {

    private static final String TAG = WeatherHelper.class.getSimpleName();
    private static Weather mCachedWeather = null;

    /**
     * Gets the current weather at the user's location
     *
     * @param context The current {@link Context}.
     * @return Returns the weather info, if available, or null
     *         if there was any error during the download.
     */
    public static Weather getWeather(Context context) throws LocationHelper.LocationNotReadyYetException {
        if (!checkNetwork(context)) {
            FLog.e(context, TAG, "Can't update weather, no network connectivity available");
            if (mCachedWeather != null) {
                FLog.w(context, TAG, "Sending cached weather information");
            }
            return mCachedWeather;
        }

        FLog.i(context, TAG, "Starting weather update");

        // Get the current location
        final Location location = getLocation(context);

        if (location == null) {
            TrackerHelper.sendException(context, "No location found", false);
            FLog.e(context, TAG, "No location available, can't update");
            return null;
        }

        // Get the city name, if possible
        String cityName = getCityName(context, location);

        Weather weather;
        String json;

        if (!TextUtils.isEmpty(cityName)) {
            json = ((new WeatherHttpClient(context)).getCityWeatherJsonData(cityName));
        } else {
            // No city name available. Use latlon values instead
            json = ((new WeatherHttpClient(context)).getLocationWeatherJsonData(location));
        }

        if (TextUtils.isEmpty(json)) {
            FLog.e(context, TAG, "No weather available, can't update");
            TrackerHelper.sendException(context, "No weather data", false);
            return null;
        }

        try {
            weather = JSONWeatherParser.getWeather(json);
        }
        catch (JSONException e) {
            FLog.e(context, TAG, "Weather data is not valid, can't update");
            TrackerHelper.sendException(context, "Invalid weather JSON", false);
            return null;
        }

        FLog.i(context, TAG, "Weather update done");
        FLog.v(context, TAG, "Got weather:\n\t> " + weather);

        // Update the cached value
        mCachedWeather = weather;
        FLog.v(context, TAG, "Cached weather information updated");

        return weather;
    }

    /**
     * Gets the current location.
     *
     * @param context The current {@link Context}.
     * @return Returns the current location
     */
    public static Location getLocation(Context context) throws LocationHelper.LocationNotReadyYetException {
        final Intent intent = WidgetHelper.getUpdaterIntent(context, false, false);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 42, intent, 0);
        return LocationHelper.getLastKnownSurroundings(pendingIntent);
    }

    /**
     * Gets the city name (where available), suffixed with the
     * country code.
     *
     * @param context The current {@link Context}.
     * @param location The Location to get the name for.
     *
     * @return Returns the city name and country (eg. "London,UK")
     *         if available, null otherwiseù
     */
    public static String getCityName(Context context, Location location) {
        String cityName = null;
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            }
            catch (IOException ignored) {
            }

            if (addresses != null && !addresses.isEmpty()) {
                final Address address = addresses.get(0);
                final String city = address.getLocality();
                if (!TextUtils.isEmpty(city)) {
                    // We only set the city name if we actually have it
                    // (to avoid the country code avoiding returning null)
                    cityName = city + "," + address.getCountryCode();
                }
            }
        }

        String encodedCityName = null;

        try {
            encodedCityName = URLEncoder.encode(cityName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            FLog.d(context, TAG, "Could not encode city name, assume no city available.");
        } catch (NullPointerException enp) {
            FLog.d(context, TAG, "Could not encode city name, assume no city available.");
        }

        return encodedCityName;
    }

    /**
     * Checks if there is any network connection active (or activating).
     *
     * @param context The current {@link Context}.
     * @return Returns true if there is an active connection, false otherwise
     */
    public static boolean checkNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
