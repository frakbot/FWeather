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
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.activity.SettingsActivity;
import net.frakbot.FWeather.updater.weather.JSONWeatherParser;
import net.frakbot.FWeather.updater.weather.WeatherHttpClient;
import net.frakbot.FWeather.updater.weather.model.Weather;
import net.frakbot.FWeather.util.*;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/**
 * Updater service for the widgets.
 *
 * @author Sebastiano Poggi, Francesco Pontillo
 */
public class UpdaterService extends IntentService {

    public static final String TAG = UpdaterService.class.getSimpleName();
    private WidgetUiHelper mWidgetUiHelper;

    private static final String DESIRED_LANGUAGE_PREF = "ui_override_language";

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
        FLog.d(this, TAG, "onCreate");

        FLog.i(this, TAG, "Initializing the UpdaterService");
        mWidgetUiHelper = new WidgetUiHelper(this);
        mHandler = new Handler();

        // Initialize the amazing LocationHelper
        // (the method is idempotent)
        LocationHelper.init(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FLog.d(this, TAG, "onHandleIntent");
        int[] appWidgetIds = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            FLog.d(this, TAG, "Intent with no widgets ID received, ignoring\n\t> " + intent);
            return;
        }

        if (intent.getBooleanExtra(EXTRA_USER_FORCE_UPDATE, false)) {
            FLog.i(this, TAG, "User has requested a forced update");
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

        FLog.i(this, TAG, "Starting widgets update");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        assert appWidgetManager != null;

        // Get the latest weather info (new or cached)
        Weather weather = null;
        try {
            weather = WeatherHelper.getWeather(this);
        } catch (LocationHelper.LocationNotReadyYetException justWaitException) {
            // If the location is not ready yet, leave the View unchanged
            FLog.d(this, TAG, "The LocationHelper is not reayd yet, the updater will be called again soon.");
            return;
        }

        Locale defaultLocale = null, selectedLocale;
        if ((selectedLocale = getUserSelectedLocale()) != null) {
            defaultLocale = switchLocale(selectedLocale);
        }

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            FLog.i(this, TAG, "Updating the widget views for widget #" + appWidgetId);

            // Get the widget layout and update it
            RemoteViews views = new RemoteViews(getPackageName(),
                                                getWidgetLayout(appWidgetManager, appWidgetId));
            updateViews(views, weather, appWidgetIds);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        // If the we switched the locale, let's restore the default one
        if (selectedLocale != null) {
            switchLocale(defaultLocale);
        }

        // Reschedule the alarm
        AlarmHelper.rescheduleAlarm(this);

        FLog.i(this, TAG, "All widgets updated successfully");
    }

    /**
     * Change the Locale used for further (even implicit) calls to <code>getResources()</code>
     * @param selectedLocale The new locale to use
     * @return the Locale used before the switch. It should be restored after use.
     */
    private Locale switchLocale(Locale selectedLocale) {
        Resources standardResources = getResources();
        AssetManager assets = standardResources.getAssets();
        DisplayMetrics metrics = standardResources.getDisplayMetrics();
        Configuration config = new Configuration(standardResources.getConfiguration());

        // Backup the current default locale, in order to restore it after the update
        Locale currentLocale = config.locale;
        config.locale = selectedLocale;

        // no need to assign this to a variable: the app will use these resources until they are changed again
        new Resources(assets, metrics, config);

        return currentLocale;
    }

    /**
     * Check the current default language against the language selected by the user in the preferences screen
     * @return the Locale related to the language choosen by the user or <code>null</code> if the user
     * didn't choose any other locale
     */
    private Locale getUserSelectedLocale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = getResources().getStringArray(R.array.pref_key_ui_override_language_values)[0];
        String desiredLanguage = prefs.getString(DESIRED_LANGUAGE_PREF, defaultValue);

        Configuration defaultConfiguration = new Configuration(getResources().getConfiguration());
        String defaultLanguage = defaultConfiguration.locale.getLanguage();

        if (desiredLanguage == null || desiredLanguage.equals(defaultLanguage) || desiredLanguage.equals(defaultValue)) {
            // No need to change locale
            return null;
        }

        return new Locale(desiredLanguage);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetLayout(AppWidgetManager appWidgetManager, int widgetId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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
            views.setViewVisibility(R.id.img_weathericon, View.INVISIBLE);
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

}
