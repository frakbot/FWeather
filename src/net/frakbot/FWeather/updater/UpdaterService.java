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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.google.android.gms.location.LocationClient;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.activity.SettingsActivity;
import net.frakbot.FWeather.updater.weather.JSONWeatherParser;
import net.frakbot.FWeather.updater.weather.WeatherHttpClient;
import net.frakbot.FWeather.updater.weather.model.Weather;
import net.frakbot.FWeather.util.LocationHelper;
import net.frakbot.FWeather.util.TrackerHelper;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * Updater service for the widgets.
 *
 * @author Sebastiano Poggi, Francesco Pontillo
 */
public class UpdaterService extends IntentService {

    public static final String TAG = UpdaterService.class.getSimpleName();
    private WidgetUiHelper mWidgetUiHelper;

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
        Log.d(TAG, "onCreate");

        Log.i(TAG, "Initializing the UpdaterService");
        mWidgetUiHelper = new WidgetUiHelper(this);
        mHandler = new Handler();

        // Initialize the amazing LocationHelper
        // (the method is idempotent)
        LocationHelper.init(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
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
                    WidgetUiHelper.makeToast(UpdaterService.this, R.string.toast_force_update, Toast.LENGTH_LONG)
                                  .show();
                }
            });
        }

        Log.i(TAG, "Starting widgets update");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        assert appWidgetManager != null;

        // Update the weather info
        Weather weather = null;
        try {
            weather = getWeather();
        } catch (LocationHelper.LocationNotReadyYetException justWaitException) {
            // If the location is not ready yet, leave the View unchanged
            Log.d(TAG, "The LocationHelper is not reayd yet, the updater will be called again soon.");
            return;
        }

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            Log.i(TAG, "Updating the widget views for widget #" + appWidgetId);

            // Get the widget layout and update it
            RemoteViews views = new RemoteViews(getPackageName(),
                                                getWidgetLayout(appWidgetManager, appWidgetId));
            updateViews(views, weather, appWidgetIds);

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
    private void updateViews(RemoteViews views, Weather weather, int[] widgetIds) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean darkMode = prefs.getBoolean(getString(R.string.pref_key_ui_darkmode), false);

        // Determine the main text color for the widget
        int textColor;
        if (!darkMode) {
            textColor = getResources().getColor(R.color.text_widget_main_color);
        }
        else {
            textColor = getResources().getColor(R.color.text_widget_main_color_dark);
        }

        // Show/hide elements, and update them only if needed
        views.setTextViewText(R.id.txt_weather, mWidgetUiHelper.getWeatherString(weather, darkMode));
        views.setTextColor(R.id.txt_weather, textColor);

        if (prefs.getBoolean(getString(R.string.pref_key_ui_toggle_temperature_info), true)) {
            views.setViewVisibility(R.id.txt_temp, View.VISIBLE);
            views.setTextViewText(R.id.txt_temp, mWidgetUiHelper.getTempString(weather, darkMode));
            views.setTextColor(R.id.txt_temp, textColor);
        }
        else {
            views.setViewVisibility(R.id.txt_temp, View.GONE);
        }

        if (prefs.getBoolean(getString(R.string.pref_key_ui_toggle_weather_icon), true)) {
            views.setViewVisibility(R.id.img_weathericon, View.VISIBLE);
            views.setImageViewResource(R.id.img_weathericon, mWidgetUiHelper.getWeatherImageId(weather, darkMode));
        }
        else {
            views.setViewVisibility(R.id.img_weathericon, View.GONE);
        }

        if (prefs.getBoolean(getString(R.string.pref_key_ui_toggle_buttons), true)) {
            views.setViewVisibility(R.id.btn_info, View.VISIBLE);
            views.setViewVisibility(R.id.btn_refresh, View.VISIBLE);
        }
        else {
            views.setViewVisibility(R.id.btn_info, View.GONE);
            views.setViewVisibility(R.id.btn_refresh, View.GONE);
        }

        // Initalize OnClick listeners
        Intent i = new Intent(this, SettingsActivity.class);
        views.setOnClickPendingIntent(R.id.btn_info,
                                      PendingIntent.getActivity(this, 0, i, 0));

        i = new Intent(this, UpdaterService.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(UpdaterService.EXTRA_WIDGET_IDS, widgetIds);
        i.putExtra(UpdaterService.EXTRA_USER_FORCE_UPDATE, true);
        views.setOnClickPendingIntent(R.id.btn_refresh, PendingIntent.getService(this, 0, i, 0));
    }

    /**
     * Gets the current weather at the user's location
     *
     * @return Returns the weather info, if available, or null
     *         if there was any error during the download.
     */
    private Weather getWeather() throws LocationHelper.LocationNotReadyYetException {
        if (!checkNetwork()) {
            Log.e(TAG, "Can't update weather, no network connectivity available");
            return null;
        }

        Log.i(TAG, "Starting weather update");

        // Get the current location
        final Location location = getLocation();

        if (location == null) {
            TrackerHelper.sendException(this, "No location found", false);
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

        if (TextUtils.isEmpty(json)) {
            Log.e(TAG, "No weather available, can't update");
            TrackerHelper.sendException(this, "No weather data", false);
            return null;
        }

        try {
            weather = JSONWeatherParser.getWeather(json);
        }
        catch (JSONException e) {
            Log.e(TAG, "Weather data is not valid, can't update");
            TrackerHelper.sendException(this, "Invalid weather JSON", false);
            return null;
        }

        Log.i(TAG, "Weather update done");
        Log.v(TAG, "Got weather:\n\t> " + weather);
        return weather;
    }

    /**
     * Gets the current location.
     *
     * @return Returns the current location
     */
    private Location getLocation() throws LocationHelper.LocationNotReadyYetException {
        final Intent intent = WidgetUiHelper.getUpdaterIntent(this, false, false);
        final PendingIntent pendingIntent = PendingIntent.getService(this, 42, intent, 0);
        return LocationHelper.getLastKnownSurroundings(pendingIntent);
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
