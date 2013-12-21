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
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.activity.SettingsActivity;
import net.frakbot.FWeather.updater.weather.model.WeatherData;
import net.frakbot.FWeather.util.*;
import net.frakbot.global.Const;
import net.frakbot.util.log.FLog;

import java.io.IOException;
import java.util.Locale;

/**
 * Updater service for the widgets.
 * TODO: deregister all location providers when no widgets are available
 *
 * @author Sebastiano Poggi, Francesco Pontillo
 */
public class UpdaterService extends IntentService {

    public static final String TAG = UpdaterService.class.getSimpleName();
    private WidgetHelper mWidgetHelper;

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
        mWidgetHelper = new WidgetHelper(this);
        mHandler = new Handler();

        // Initialize the amazing LocationHelper
        // (the method is idempotent)
        LocationHelper.init(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FLog.d(this, TAG, "onHandleIntent");

        // Deregister the connection listener, if any
        ConnectionHelper.unregisterConnectivityListener(getApplicationContext());

        // First thing, recheck the log filtering levels
        FLog.recheckLogLevels();

        int[] appWidgetIds = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            FLog.d(this, TAG, "Intent with no widgets ID received, ignoring\n\t> " + intent);
            return;
        }

        if (intent.getBooleanExtra(EXTRA_USER_FORCE_UPDATE, false)) {
            FLog.i(this, TAG, "User has requested a forced update");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // We need this because the IntentService thread is too fast and dies too soon,
                    // resulting in the toast being on screen for an unpercievable time
                    WidgetHelper.makeToast(UpdaterService.this, R.string.toast_force_update, Toast.LENGTH_LONG)
                                .show();
                }
            });
        }

        FLog.i(this, TAG, "Starting widgets update");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        assert appWidgetManager != null;

        // Get the latest weather info (new or cached)
        WeatherData weather;
        try {
            weather = WeatherHelper.getWeather(this);
        }
        catch (LocationHelper.LocationNotReadyYetException justWaitException) {
            // If the location is not ready yet, leave the View unchanged
            FLog.d(this, TAG, "The LocationHelper is not ready yet, the updater will be called again " +
                              "when a location is available.");
            weather = new WeatherData();
            weather.conditionCode = WeatherData.INVALID_CONDITION;
        }
        catch (IOException e) {
            // Caught if there are connection issues
            // Get the latest cached weather information
            FLog.e(this, TAG, "Error while fetching the weather, using a cached value", e);
            weather = WeatherHelper.getLatestWeather();
            // Register a connection listener
            FLog.d(this, TAG, "Registering a connection listener");
            ConnectionHelper.registerConnectivityListener(getApplicationContext());
        }

        Locale defaultLocale = null, selectedLocale;
        if ((selectedLocale = getUserSelectedLocale(this)) != null) {
            defaultLocale = switchLocale(this, selectedLocale);
        }

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            FLog.i(this, TAG, "Updating the widget views for widget #" + appWidgetId);

            // Get the widget layout and update it
            RemoteViews views = new RemoteViews(getPackageName(),
                                                getWidgetLayout(appWidgetManager, appWidgetId));
            updateViews(views, weather, appWidgetIds);
            setupViewsForKeyguard(views, appWidgetManager, appWidgetId);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        // If we switched the locale, let's restore the default one
        if (selectedLocale != null) {
            switchLocale(this, defaultLocale);
        }

        // Reschedule the alarm
        AlarmHelper.rescheduleAlarm(this);

        FLog.i(this, TAG, "All widgets updated successfully");
    }

    /**
     * Change the Locale used for further (even implicit) calls to <code>getResources()</code> .
     *
     * @param selectedLocale The new locale to use
     * @param context The current context
     *
     * @return Returns the Locale used before the switch. It should be restored after use.
     */
    public static Locale switchLocale(Context context, Locale selectedLocale) {
        Resources standardResources = context.getResources();
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
     *
     * @param context The current context
     *
     * @return Returns the Locale related to the language choosen by the user or <code>null</code> if the user
     * didn't choose any other locale
     */
    public static Locale getUserSelectedLocale(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = context.getResources().getStringArray(R.array.pref_key_ui_override_language_values)[0];
        String desiredLanguage = prefs.getString(Const.Preferences.UI_OVERRIDE_LANGUAGE, defaultValue);

        Configuration defaultConfiguration = new Configuration(context.getResources().getConfiguration());
        String defaultLanguage = defaultConfiguration.locale.getLanguage();

        if (desiredLanguage == null || desiredLanguage.equals(defaultLanguage) || desiredLanguage.equals(
            defaultValue)) {
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
    private void updateViews(RemoteViews views, WeatherData weather, int[] widgetIds) {
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
        views.setTextViewText(R.id.txt_weather, mWidgetHelper.getWeatherString(weather, darkMode));
        views.setTextColor(R.id.txt_weather, textColor);
        int bgColorPrefValue = getWidgetBgColorPrefValue(prefs);
        views.setInt(R.id.content, "setBackgroundColor",
                     mWidgetHelper.getWidgetBGColor(bgColorPrefValue, darkMode));

        if (prefs.getBoolean(getString(R.string.pref_key_ui_toggle_temperature_info), true)) {
            views.setViewVisibility(R.id.txt_temp, View.VISIBLE);
            views.setTextViewText(R.id.txt_temp, mWidgetHelper.getTempString(weather, darkMode));
            views.setTextColor(R.id.txt_temp, textColor);
        }
        else {
            views.setViewVisibility(R.id.txt_temp, View.GONE);
        }

        if (prefs.getBoolean(getString(R.string.pref_key_ui_toggle_weather_icon), true)) {
            views.setViewVisibility(R.id.img_weathericon, View.VISIBLE);
            views.setImageViewResource(R.id.img_weathericon, mWidgetHelper.getWeatherImageId(weather, darkMode));
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

        // The pending intent (Magnum PI, ha!) for the main TextViews
        PendingIntent magnumPI = null;
        // If the user hasn't enabled location settings and there's no information available
        if (weather != null && weather.conditionCode == WeatherData.WEATHER_ID_ERR_NO_LOCATION) {
            // When the user tap on the main contents, redirect him/her to the proper settings
            magnumPI = PendingIntent.getActivity(this, 0, new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
        }
        views.setOnClickPendingIntent(R.id.txt_weather, magnumPI);
        views.setOnClickPendingIntent(R.id.txt_temp, magnumPI);

        // Create and set the PendingIntent for the share action
        PendingIntent sharePendingIntent = null;
        if (weather != null && weather.conditionCode >= 0) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mWidgetHelper.getShareString(weather));
            sharePendingIntent = PendingIntent.getActivity(this, 1, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.btn_share, sharePendingIntent);
    }

    /**
     * Gets the widget BG color opacity preference value, handling
     * any format errors that could arise.
     *
     * @param prefs The SharedPreferences to retrieve the value from
     *
     * @return Returns the preference value, or a default value of 0 if there is
     * any issue with the preference value retrieval.
     */
    private int getWidgetBgColorPrefValue(SharedPreferences prefs) {
        try {
            return Integer.parseInt(prefs.getString(getString(R.string.pref_key_ui_bgopacity), "%NOVAL%"));
        }
        catch (NumberFormatException e) {
            FLog.w(TAG, "Invalid preference value for UI BG opacity, defaulting to 0", e);
            return 0;
        }
    }

    /**
     * Sets up the widget views to adapt to it being on the lockscreen.
     * This method doesn't do anything on Android 4.1.x and earlier, since
     * there were no lockscreen widgets.
     *
     * @param views            The widget RemoteViews
     * @param appWidgetManager The widget manager used to detect whether the widget
     *                         is on the lockscreen
     * @param widgetId         The ID of the widget
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setupViewsForKeyguard(RemoteViews views, AppWidgetManager appWidgetManager, int widgetId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Bundle myOptions = appWidgetManager.getAppWidgetOptions(widgetId);

            final int category = myOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY,
                                                  AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN);

            if (category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD) {
                FLog.v(TAG, "Hiding the refresh button: widget " + widgetId + " is on the keyguard");
                views.setViewVisibility(R.id.btn_refresh, View.GONE);
            }
        }
    }

}
