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

package net.frakbot.FWeather.updater;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.updater.weather.JSONWeatherParser;
import net.frakbot.FWeather.updater.weather.WeatherHttpClient;
import net.frakbot.FWeather.updater.weather.model.Weather;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Updater service for the widgets.
 * <p/>
 * Author: Sebastiano Poggi
 * Created on: 6/30/13 Time: 11:57 AM
 * File version: 1.0
 * <p/>
 * Changelog:
 * Version 1.0
 * * Initial revision
 */
public class UpdaterService extends IntentService {

    public static final String TAG = UpdaterService.class.getSimpleName();

    private LocationManager mLocationManager;

    public static final String EXTRA_USER_FORCE_UPDATE = "the_motherfocker_wants_us_to_do_stuff";
    public static final String EXTRA_SILENT_FORCE_UPDATE = "a_ninja_is_making_me_do_it";
    public static final String EXTRA_WIDGET_IDS = "widget_ids";
    private Handler mHandler;

    public UpdaterService() {
        super(UpdaterService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Initializing the UpdaterService");
        mHandler = new Handler();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        bootstrapLocationProvider();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int[] appWidgetIds = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            Log.d(TAG, "Intent with no widgets ID received, ignoring\n\t> " + intent);
            return;
        }

        if (intent.getBooleanExtra(EXTRA_USER_FORCE_UPDATE, false)) {
            Log.i(TAG, "User has requested a forced update");
            // TODO: custom Toast layout? Would be nice.
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // We need this because the IntentService thread is too fast and dies too soon,
                    // resulting in the toast being on screen for an unpercievable time
                    Toast.makeText(UpdaterService.this, R.string.toast_force_update, Toast.LENGTH_LONG).show();
                }
            });
        }

        Log.i(TAG, "Starting widgets update");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        assert appWidgetManager != null;

        // Update the weather info
        Weather weather = getWeather();

        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            Log.i(TAG, "Updating the widget views for widget #" + appWidgetId);

            // Get the widget layout and update it
            RemoteViews views = new RemoteViews(getPackageName(),
                                                getWidgetLayout(appWidgetManager, appWidgetId));
            updateViews(views, weather);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        Log.i(TAG, "All widgets updated successfully");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetLayout(AppWidgetManager appWidgetManager, int widgetId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Bundle myOptions = appWidgetManager.getAppWidgetOptions(widgetId);

            // Get the value of OPTION_APPWIDGET_HOST_CATEGORY
            int maxHeight = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, -1);

            // If the value is WIDGET_CATEGORY_KEYGUARD, it's a lockscreen widget
            if (maxHeight < 200) {
                return R.layout.fweather_small;
            }
            else if (maxHeight < 300) {
                return R.layout.fweather_medium;
            }
            else {
                return R.layout.fweather_large;
            }
        }
        else {
            return R.layout.fweather_large;
        }
    }

    /**
     * Updates the widget's views.
     *
     * @param views   The RemoteViews to use
     * @param weather The weather to update with
     */
    private void updateViews(RemoteViews views, Weather weather) {
        views.setTextViewText(R.id.txt_weather, getWeatherString(weather));

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.getBoolean(getString(R.string.pref_key_ui_toggle_temperature_info), true)) {
            views.setViewVisibility(R.id.txt_temp, View.VISIBLE);
            views.setTextViewText(R.id.txt_temp, getTempString(weather));
        }
        else {
            views.setViewVisibility(R.id.txt_temp, View.GONE);
        }

        if (preferences.getBoolean(getString(R.string.pref_key_ui_toggle_weather_icon), true)) {
            views.setViewVisibility(R.id.img_weathericon, View.VISIBLE);
            views.setImageViewResource(R.id.img_weathericon, getWeatherImageId(weather));
        }
        else {
            views.setViewVisibility(R.id.img_weathericon, View.GONE);
        }
    }

    /**
     * Gets the string representing the weather.
     *
     * @param weather The weather to get the string for
     *
     * @return Returns the corresponding weather string
     */
    private Spanned getWeatherString(Weather weather) {
        final int weatherId;
        if (weather != null) {
            weatherId = weather.mCurrentCondition.getWeatherId();
        }
        else {
            weatherId = -1;
        }

        // Codes list: http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 299) {
            // Thunderstorm
            return Html.fromHtml(getString(R.string.weather_thunderstorm));
        }
        else if (weatherId >= 300 && weatherId <= 399) {
            // Drizzle
            return Html.fromHtml(getString(R.string.weather_drizzle));
        }
        else if (weatherId >= 500 && weatherId <= 599) {
            // Rain
            return Html.fromHtml(getString(R.string.weather_rainy));
        }
        else if (weatherId >= 600 && weatherId <= 699) {
            // Snow
            return Html.fromHtml(getString(R.string.weather_snowy));
        }
        else if (weatherId >= 700 && weatherId <= 799) {
            // Atmosphere (mist, smoke, etc)
            return Html.fromHtml(getString(R.string.weather_haze));
        }
        else if (weatherId == 800 || weatherId == 801) {
            // Sunny or mostly sunny
            return Html.fromHtml(getString(R.string.weather_sunny));
        }
        else if (weatherId == 802 || weatherId == 803) {
            // Mostly cloudy
            return Html.fromHtml(getString(R.string.weather_mostly_cloudy));
        }
        else if (weatherId >= 804 && weatherId <= 899) {
            // Cloudy
            return Html.fromHtml(getString(R.string.weather_cloudy));
        }
        else if (weatherId >= 900 && weatherId <= 999) {
            // Extreme weather
            return Html.fromHtml(getString(R.string.weather_extreme));
        }
        else {
            return Html.fromHtml(getString(R.string.weather_wtf));
        }
    }

    /**
     * Gets the ID of the image representing the weather.
     *
     * @param weather The weather to get the image for
     *
     * @return Returns the corresponding weather image ID
     */
    private int getWeatherImageId(Weather weather) {
        final int weatherId;
        if (weather != null) {
            weatherId = weather.mCurrentCondition.getWeatherId();
        }
        else {
            weatherId = -1;
        }

        // Codes list: http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 299) {
            // Thunderstorm
            return R.drawable.thunderstorm;
        }
        else if (weatherId >= 300 && weatherId <= 399) {
            // Drizzle
            return R.drawable.drizzle;
        }
        else if (weatherId >= 500 && weatherId <= 599) {
            // Rain
            return R.drawable.drizzle;
        }
        else if (weatherId >= 600 && weatherId <= 699) {
            // Snow
            return R.drawable.snow;
        }
        else if (weatherId >= 700 && weatherId <= 799) {
            // Atmosphere (mist, smoke, etc)
            return R.drawable.haze;
        }
        else if (weatherId == 800 || weatherId == 801) {
            // Sunny or mostly sunny
            return isDay(weather) ? R.drawable.clear_day : R.drawable.clear_night;
        }
        else if (weatherId == 802 || weatherId == 803) {
            // Mostly cloudy
            return isDay(weather) ? R.drawable.mostly_cloudy_day : R.drawable.mostly_cloudy_night;
        }
        else if (weatherId >= 804 && weatherId <= 899) {
            // Cloudy
            return R.drawable.cloudy;
        }
        else if (weatherId >= 900 && weatherId <= 999) {
            // Extreme weather
            return R.drawable.extreme;
        }
        else {
            return R.drawable.wtf;
        }
    }

    /**
     * Determines if it's day or night as reported by the weather provider.
     *
     * @return Returns true if it's day, false if it's night.
     */
    private boolean isDay(Weather weather) {
        long sunrise = weather.mLocation.getSunrise();
        long sunset = weather.mLocation.getSunset();
        final long currTime = System.currentTimeMillis() / 1000;
        return currTime > sunrise && currTime < sunset;
    }

    /**
     * Gets the temperature string for the weather.
     *
     * @param weather The weather to get the temperature string for
     *
     * @return Returns the temperature string
     */
    private CharSequence getTempString(Weather weather) {
        final float temp;
        if (weather != null) {
            temp = weather.mTemperature.getTemp();
        }
        else {
            return Html.fromHtml(getString(R.string.temp_wtf));
        }

        if (temp < 0f) {
            return Html.fromHtml(getString(R.string.temp_freezing));
        }
        else if (temp < 15f) {
            return Html.fromHtml(getString(R.string.temp_cold));
        }
        else if (temp < 28f) {
            return Html.fromHtml(getString(R.string.temp_warm));
        }
        else {
            return Html.fromHtml(getString(R.string.temp_hot));
        }
    }

    /**
     * Requests a location update, that will call this service again
     * as soon as the location is ready.
     */
    private void bootstrapLocationProvider() {
        final Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        final String provider = mLocationManager.getBestProvider(criteria, true);

        if (TextUtils.isEmpty(provider)) {
            Log.w(TAG, "No available provider, unable to bootstrap location");
            return;
        }

        final Location lastLocation = mLocationManager.getLastKnownLocation(provider);
        if (lastLocation == null ||
            System.currentTimeMillis() - lastLocation.getTime() > 7200000) {

            Log.d(TAG, "Bootstrapping the location provider");
            final Intent intent = new Intent(this, UpdaterService.class);
            mLocationManager.requestSingleUpdate(provider,
                                                 PendingIntent.getService(this, 42, intent, 0));
        }
    }

    /**
     * Gets the current weather at the user's location
     *
     * @return Returns the weather info, if available, or null
     *         if there was any error during the download.
     */
    private Weather getWeather() {
        if (!checkNetwork()) {
            Log.e(TAG, "Can't update weather, no network connectivity available");
            return null;
        }

        Log.i(TAG, "Starting weather update");

        // Get the current location
        final Location location = getLocation();

        if (location == null) {
            Log.e(TAG, "No location available, can't update");
            return null;
        }

        // Get the city name, if possible
        String cityName = getCityName(location);

        Weather weather;
        String json;

        if (!TextUtils.isEmpty(cityName)) {
            json = ((new WeatherHttpClient()).getCityWeatherJsonData(cityName));
        }
        else {
            // No city name available. Use latlon values instead
            json = ((new WeatherHttpClient()).getLocationWeatherJsonData(location));
        }

        try {
            weather = JSONWeatherParser.getWeather(json);
        }
        catch (JSONException e) {
            Log.e(TAG, "Weather data is not valid, can't update");
            return null;
        }

        Log.i(TAG, "Weather update done");
        Log.v(TAG, "Got weather:\n\t> " + weather);
        return weather;
    }

    /**
     * Gets the city name (where available), suffixed with the
     * country code.
     *
     * @param location The Location to get the name for.
     *
     * @return Returns the city name and country (eg. "London,UK")
     *         if available, null otherwiseù
     */
    private String getCityName(Location location) {
        String cityName = null;
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this);
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
        return cityName;
    }

    /**
     * Gets the current location.
     *
     * @return Returns the current location
     */
    private Location getLocation() {
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        final String provider = mLocationManager.getBestProvider(criteria, true);
        if (TextUtils.isEmpty(provider)) {
            Log.w(TAG, "No available provider, unable to determine location");
            return null;
        }
        return mLocationManager.getLastKnownLocation(provider);
    }

    /**
     * Checks if there is any network connection active (or activating).
     *
     * @return Returns true if there is an active connection, false otherwise
     */
    private boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
