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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import net.frakbot.FWeather.FWeatherWidgetProvider;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.updater.UpdaterService;
import net.frakbot.FWeather.updater.weather.model.WeatherData;
import net.frakbot.FWeather.widget.FontTextView;
import net.frakbot.util.log.FLog;

/**
 * Helper class that deals with finding resources to assign to
 * the widget views.
 * <p/>
 * <b>WARNING!</b> Unavoidable spaghetti lies in here.
 */
public class WidgetHelper {

    private static final String PLACEHOLDER_COLOR = "%%COLOR%%";

    private Context mContext;

    public WidgetHelper(Context c) {
        mContext = c;
    }

    /**
     * Gets the instances of the FWeather widget.
     * @param context   the Context
     * @return          Array of int containing all of the widget ids
     */
    public static int[] getWidgetIds(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);

        return mgr.getAppWidgetIds(new ComponentName(context, FWeatherWidgetProvider.class));
    }

    /**
     * Builds an update Intent of all the widgets we currently have. It can optionally
     * also be silent (no UI).
     * The Intent is not started, you will have to do it yourself.
     *
     * @param forced True if this is a forced update request, false otherwise
     * @param silent True if this is a silent forced update request, false otherwise
     */
    public static Intent getUpdaterIntent(Context context, boolean forced, boolean silent) {
        Intent i = new Intent(context, UpdaterService.class);
        int[] ids = getWidgetIds(context);

        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(UpdaterService.EXTRA_WIDGET_IDS, ids);
        if (forced) {
            if (silent) {
                // Code-originated forced update (config changes, etc)
                i.putExtra(UpdaterService.EXTRA_SILENT_FORCE_UPDATE, true);
            }
            else {
                // User-forced updated
                i.putExtra(UpdaterService.EXTRA_USER_FORCE_UPDATE, true);
            }
        }

        return i;
    }

    /**
     * Gets the string representing the weather.
     *
     *
     * @param weather  The weather to get the string for
     * @param darkMode True if the widget is in dark mode, false otherwise
     *
     * @return Returns the corresponding weather string
     */
    public Spanned getWeatherString(WeatherData weather, boolean darkMode) {
        final int weatherId;
        if (weather != null) {
            weatherId = weather.conditionCode;
        }
        else {
            weatherId = -1;
        }

        // Codes list: http://developer.yahoo.com/weather/
        if (weatherId == 3 || weatherId == 4 || (weatherId >= 37 && weatherId <= 39) ||
            weatherId == 45 || weatherId == 47) {// Thunderstorm
            return getColoredSpannedString(R.string.weather_thunderstorm, R.color.weather_thunderstorm,
                                           R.color.weather_thunderstorm_dark, darkMode);
        }
        else if (weatherId == 8 || weatherId == 9) {
            // Drizzle
            return getColoredSpannedString(R.string.weather_drizzle, R.color.weather_drizzle,
                                           R.color.weather_drizzle_dark, darkMode);
        }
        else if (weatherId == 10 || weatherId == 12 || weatherId == 40) {
            // Rain
            return getColoredSpannedString(R.string.weather_rainy, R.color.weather_rainy,
                                           R.color.weather_rainy_dark, darkMode);
        }
        else if (weatherId == 17 || weatherId == 35) {
            // Hail
            return getColoredSpannedString(R.string.weather_hail, R.color.weather_hail,
                                           R.color.weather_hail_dark, darkMode);
        }
        else if ((weatherId >= 13 && weatherId <= 16) || weatherId == 18 ||
                 (weatherId >= 41 && weatherId <= 43) || weatherId == 46) {
            // Snow
            return getColoredSpannedString(R.string.weather_snowy, R.color.weather_snowy,
                                           R.color.weather_snowy_dark, darkMode);
        }
        else if (weatherId >= 19 && weatherId <= 22) {
            // Atmosphere (mist, smoke, etc)
            return getColoredSpannedString(R.string.weather_haze, R.color.weather_haze,
                                           R.color.weather_haze_dark, darkMode);
        }
        else if (weatherId == 32 || weatherId == 34 ||
                 weatherId == 31 || weatherId == 33) {
            // Sunny or mostly sunny (day&night)
            return getColoredSpannedString(R.string.weather_sunny, R.color.weather_sunny,
                                           R.color.weather_sunny_dark, darkMode);
        }
        else if (weatherId == 30 || weatherId == 44 || weatherId == 29) {
            // Partly cloudy (day&night)
            return getColoredSpannedString(R.string.weather_partly_cloudy, R.color.weather_partly_cloudy,
                                           R.color.weather_partly_cloudy_dark, darkMode);
        }
        else if (weatherId >= 26 && weatherId <= 28) {
            // Cloudy
            return getColoredSpannedString(R.string.weather_cloudy, R.color.weather_cloudy,
                                           R.color.weather_cloudy_dark, darkMode);
        }
        else if (weatherId == 23 || weatherId == 24) {
            // Windy
            return getColoredSpannedString(R.string.weather_windy, R.color.weather_windy,
                                           R.color.weather_windy_dark, darkMode);
        }
        else if (weatherId == 25) {
            // Cold
            return getColoredSpannedString(R.string.weather_cold, R.color.weather_cold,
                                           R.color.weather_cold_dark, darkMode);
        }
        else if (weatherId == 36) {
            // Hot
            return getColoredSpannedString(R.string.weather_hot, R.color.weather_hot,
                                           R.color.weather_hot_dark, darkMode);
        }
        else if (weatherId >= 0 && weatherId <= 2) {
            // Extreme weather
            return getColoredSpannedString(R.string.weather_extreme, R.color.weather_extreme,
                                           R.color.weather_extreme_dark, darkMode);
        }
        else if (weatherId == 3200) {
            // Error: no weather available
            return getColoredSpannedString(R.string.weather_no_weather, R.color.weather_no_weather,
                                           R.color.weather_no_weather_dark, darkMode);
        }

        else if (weatherId == 10000) {
            // Error: no location available
            return getColoredSpannedString(R.string.weather_no_location, R.color.weather_no_location,
                                           R.color.weather_no_location_dark, darkMode);
        }
        else {
            return getColoredSpannedString(R.string.weather_wtf, R.color.weather_wtf,
                                           R.color.weather_wtf_dark, darkMode);
        }
    }

    /**
     * Returns the specified spanned string with the correct highlighting color.
     *
     * @param stringId     The Resource ID of the string
     * @param lightColorId The Resource ID of the highlight color in normal (light) mode
     * @param darkColorId  The Resource ID of the highlight color in dark mode
     * @param darkMode     True if the widget is in dark mode, false otherwise
     *
     * @return Returns the spanned, colored string
     */
    public Spanned getColoredSpannedString(int stringId, int lightColorId, int darkColorId, boolean darkMode) {
        int color = mContext.getResources().getColor(!darkMode ? lightColorId : darkColorId);
        String string = mContext.getString(stringId)
                                .replace(PLACEHOLDER_COLOR, String.format("#%06X", (0xFFFFFF & color)));
        return Html.fromHtml(string);
    }

    /**
     * Gets the ID of the image representing the weather.
     *
     *
     * @param weather  The weather to get the image for
     * @param darkMode True if the widget is in dark mode, false otherwise
     *
     * @return Returns the corresponding weather image ID
     */
    public int getWeatherImageId(WeatherData weather, boolean darkMode) {
        final int weatherId;
        if (weather != null) {
            weatherId = weather.conditionCode;
        }
        else {
            weatherId = -1;
        }

        // Codes list: http://developer.yahoo.com/weather/
        if (weatherId == 3 || weatherId == 4 || (weatherId >= 37 && weatherId <= 39) ||
            weatherId == 45 || weatherId == 47) {
            // Thunderstorm
            return !darkMode ? R.drawable.weather_thunderstorm : R.drawable.weather_thunderstorm_dark;
        }
        else if (weatherId == 8 || weatherId == 9) {
            // Drizzle
            return !darkMode ? R.drawable.weather_drizzle : R.drawable.weather_drizzle_dark;
        }
        else if (weatherId == 10 || weatherId == 12 || weatherId == 40) {
            // Rain
            return !darkMode ? R.drawable.weather_rain : R.drawable.weather_rain_dark;
        }
        else if (weatherId == 17 || weatherId == 35) {
            // Hail
            return !darkMode ? R.drawable.weather_hail : R.drawable.weather_hail_dark;
        }
        else if ((weatherId >= 13 && weatherId <= 16) || weatherId == 18 ||
                 (weatherId >= 41 && weatherId <= 43) || weatherId == 46) {
            // Snow
            return !darkMode ? R.drawable.weather_snow : R.drawable.weather_snow_dark;
        }
        else if (weatherId >= 19 && weatherId <= 22) {
            // Atmosphere (mist, smoke, etc)
            return !darkMode ? R.drawable.weather_haze : R.drawable.weather_haze_dark;
        }
        else if (weatherId == 32 || weatherId == 34) {
            // Sunny or mostly sunny (day)
            return !darkMode ? R.drawable.weather_clear_day : R.drawable.weather_clear_day_dark;
        }
        else if (weatherId == 31 || weatherId == 33) {
            // Clear (night)
            return !darkMode ? R.drawable.weather_clear_night : R.drawable.weather_clear_night_dark;
        }
        else if (weatherId >= 26 && weatherId <= 28) {
            // Cloudy
            return !darkMode ? R.drawable.weather_cloudy : R.drawable.weather_cloudy_dark;
        }
        else if (weatherId == 30 || weatherId == 44) {
            // Partly cloudy (day)
            return !darkMode ? R.drawable.weather_partly_cloudy_day : R.drawable.weather_partly_cloudy_day_dark;
        }
        else if (weatherId == 29) {
            // Partly cloudy (night)
            return !darkMode ? R.drawable.weather_partly_cloudy_night : R.drawable.weather_partly_cloudy_night_dark;
        }
        else if (weatherId == 23 || weatherId == 24) {
            // Windy
            return !darkMode ? R.drawable.weather_windy : R.drawable.weather_windy_dark;
        }
        else if (weatherId == 25) {
            // Cold
            return !darkMode ? R.drawable.weather_cold : R.drawable.weather_cold_dark;
        }
        else if (weatherId == 36) {
            // Hot
            return !darkMode ? R.drawable.weather_hot : R.drawable.weather_hot_dark;
        }
        else if (weatherId >= 0 && weatherId <= 2) {
            // Extreme weather
            return !darkMode ? R.drawable.weather_extreme : R.drawable.weather_extreme_dark;
        }
        else if (weatherId == 3200) {
            // Error: no weather available (ATM has a generic error icon)
            return !darkMode ? R.drawable.weather_wtf : R.drawable.weather_wtf_dark;
        }
        else if (weatherId == 10000) {
            // Error: no location available  (ATM has a generic error icon)
            return !darkMode ? R.drawable.weather_wtf : R.drawable.weather_wtf_dark;
        }
        else {
            // Unknown weather
            return !darkMode ? R.drawable.weather_wtf : R.drawable.weather_wtf_dark;
        }
    }

    /**
     * Gets the temperature string for the weather.
     *
     *
     * @param weather  The weather to get the temperature string for
     * @param darkMode True if the widget is in dark mode, false otherwise
     *
     * @return Returns the temperature string
     */
    public CharSequence getTempString(WeatherData weather, boolean darkMode) {
        final float temp;
        if (weather != null) {
            if (weather.conditionCode == WeatherData.WEATHER_ID_ERR_NO_LOCATION) {

                // Error: no location available
                return getColoredSpannedString(R.string.temp_no_location, R.color.temp_no_location,
                                               R.color.temp_no_location_dark, darkMode);
            }
            temp = weather.temperature;
        }
        else {
            return getColoredSpannedString(R.string.temp_wtf, R.color.temp_wtf,
                                           R.color.temp_wtf_dark, darkMode);
        }

        if (temp < 0f) {
            return getColoredSpannedString(R.string.temp_freezing, R.color.temp_freezing,
                                           R.color.temp_freezing_dark, darkMode);
        }
        else if (temp < 15f) {
            return getColoredSpannedString(R.string.temp_cold, R.color.temp_cold,
                                           R.color.temp_cold_dark, darkMode);
        }
        else if (temp < 28f) {
            return getColoredSpannedString(R.string.temp_warm, R.color.temp_warm,
                                           R.color.temp_warm_dark, darkMode);
        }
        else {
            return getColoredSpannedString(R.string.temp_hot, R.color.temp_hot,
                                           R.color.temp_hot_dark, darkMode);
        }
    }

    /**
     * Gets the right color for the widget background, depending on the user
     * preferences and the dark mode state.
     *
     * @param bgOpacityPrefValue The value of the {@link net.frakbot.FWeather.R.string#pref_key_ui_bgopacity}
     *                           preference
     * @param darkMode True if the widget is in dark mode (and thus requires a bright BG), false otherwise
     * @return Returns the color to be assigned to the widget BG
     */
    public int getWidgetBGColor(int bgOpacityPrefValue, boolean darkMode) {
        TypedArray colors = mContext.getResources()
                                    .obtainTypedArray(darkMode ? R.array.bg_colors_darkmode : R.array.bg_colors);

        // We assume a fully transparent BG color as default
        switch (bgOpacityPrefValue) {
            case 0:
                return colors.getColor(0, 0x00000000);
            case 25:
                return colors.getColor(1, 0x00000000);
            case 50:
                return colors.getColor(2, 0x00000000);
            case 75:
                return colors.getColor(3, 0x00000000);
            case 100:
                return colors.getColor(4, 0x00000000);
            default:
                FLog.w("WidgetHelper", "Invalid BG preference value detected: " + bgOpacityPrefValue);
                return 0x00000000;
        }
    }

    /**
     * Makes a customized Toast on the specified Context,
     * using the text and duration provided.
     *
     * @param c        The Context to build the Toast within
     * @param text     The text of the Toast
     * @param duration The duration of the Toast (see Toast's duration)
     *
     * @return Returns the initialized Toast
     */
    public static Toast makeToast(Context c, CharSequence text, int duration) {
        final View view = LayoutInflater.from(c).inflate(R.layout.toast_layout, null);
        ((FontTextView) view.findViewById(android.R.id.content)).setText(text);

        Toast t = new Toast(c);
        t.setView(view);
        t.setDuration(duration);
        return t;
    }

    /**
     * Makes a customized Toast on the specified Context,
     * using the text and duration provided.
     *
     * @param c        The Context to build the Toast within
     * @param text     The Resource ID of the text of the Toast
     * @param duration The duration of the Toast (see Toast's duration)
     *
     * @return Returns the initialized Toast
     */
    public static Toast makeToast(Context c, int text, int duration) {
        final View view = LayoutInflater.from(c).inflate(R.layout.toast_layout, null);
        ((FontTextView) view.findViewById(android.R.id.content)).setText(text);

        Toast t = new Toast(c);
        t.setView(view);
        t.setDuration(duration);
        t.setGravity(Gravity.BOTTOM, 0, c.getResources().getDimensionPixelSize(R.dimen.toast_yoffset));
        return t;
    }
}