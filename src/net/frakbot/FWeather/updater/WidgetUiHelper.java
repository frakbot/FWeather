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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import net.frakbot.FWeather.FWeatherWidgetProvider;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.updater.weather.model.Weather;
import net.frakbot.FWeather.widget.FontTextView;

/**
 * Helper class that deals with finding resources to assign to
 * the widget views.
 * <p/>
 * <b>WARNING!</b> Unavoidable spaghetti lies in here.
 */
public class WidgetUiHelper {

    private static final String PLACEHOLDER_COLOR = "%%COLOR%%";

    private Context mContext;

    public WidgetUiHelper(Context c) {
        mContext = c;
    }

    /**
     * Gets the instances of the FWeather widget.
     * @param context   the Context
     * @return          Array of int containing all of the widget ids
     */
    public static int[] getWidgetIds(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, FWeatherWidgetProvider.class));

        return ids;
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
     * @param weather  The weather to get the string for
     * @param darkMode True if the widget is in dark mode, false otherwise
     *
     * @return Returns the corresponding weather string
     */
    public Spanned getWeatherString(Weather weather, boolean darkMode) {
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
            return getColoredSpannedString(R.string.weather_thunderstorm, R.color.weather_thunderstorm,
                                           R.color.weather_thunderstorm_dark, darkMode);
        }
        else if (weatherId >= 300 && weatherId <= 399) {
            // Drizzle
            return getColoredSpannedString(R.string.weather_drizzle, R.color.weather_drizzle,
                                           R.color.weather_drizzle_dark, darkMode);
        }
        else if (weatherId >= 500 && weatherId <= 599) {
            // Rain
            return getColoredSpannedString(R.string.weather_rainy, R.color.weather_rainy,
                                           R.color.weather_rainy_dark, darkMode);
        }
        else if (weatherId >= 600 && weatherId <= 699) {
            // Snow
            return getColoredSpannedString(R.string.weather_snowy, R.color.weather_snowy,
                                           R.color.weather_snowy_dark, darkMode);
        }
        else if (weatherId >= 700 && weatherId <= 799) {
            // Atmosphere (mist, smoke, etc)
            return getColoredSpannedString(R.string.weather_haze, R.color.weather_haze,
                                           R.color.weather_haze_dark, darkMode);
        }
        else if (weatherId == 800 || weatherId == 801) {
            // Sunny or mostly sunny
            return getColoredSpannedString(R.string.weather_sunny, R.color.weather_sunny,
                                           R.color.weather_sunny_dark, darkMode);
        }
        else if (weatherId == 802 || weatherId == 803) {
            // Mostly cloudy
            return getColoredSpannedString(R.string.weather_mostly_cloudy, R.color.weather_mostly_cloudy,
                                           R.color.weather_mostly_cloudy_dark, darkMode);
        }
        else if (weatherId >= 804 && weatherId <= 899) {
            // Cloudy
            return getColoredSpannedString(R.string.weather_cloudy, R.color.weather_cloudy,
                                           R.color.weather_cloudy_dark, darkMode);
        }
        else if (weatherId >= 900 && weatherId <= 999) {
            // Extreme weather
            return getColoredSpannedString(R.string.weather_extreme, R.color.weather_extreme,
                                           R.color.weather_extreme_dark, darkMode);
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
     * @param weather  The weather to get the image for
     * @param darkMode True if the widget is in dark mode, false otherwise
     *
     * @return Returns the corresponding weather image ID
     */
    public int getWeatherImageId(Weather weather, boolean darkMode) {
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
            return !darkMode ? R.drawable.weather_thunderstorm : R.drawable.weather_thunderstorm_dark;
        }
        else if (weatherId >= 300 && weatherId <= 399) {
            // Drizzle
            return !darkMode ? R.drawable.weather_drizzle : R.drawable.weather_drizzle_dark;
        }
        else if (weatherId >= 500 && weatherId <= 599) {
            // Rain
            return !darkMode ? R.drawable.weather_drizzle : R.drawable.weather_drizzle_dark;
        }
        else if (weatherId >= 600 && weatherId <= 699) {
            // Snow
            return !darkMode ? R.drawable.weather_snow : R.drawable.weather_snow_dark;
        }
        else if (weatherId >= 700 && weatherId <= 799) {
            // Atmosphere (mist, smoke, etc)
            return !darkMode ? R.drawable.weather_haze : R.drawable.weather_haze_dark;
        }
        else if (weatherId == 800 || weatherId == 801) {
            // Sunny or mostly sunny
            if (isDay(weather)) {
                return !darkMode ? R.drawable.weather_clear_day : R.drawable.weather_clear_day_dark;
            }
            else {
                return !darkMode ? R.drawable.weather_clear_night : R.drawable.weather_clear_night_dark;
            }
        }
        else if (weatherId == 802 || weatherId == 803) {
            // Mostly cloudy
            if (isDay(weather)) {
                return !darkMode ? R.drawable.weather_mostly_cloudy_day : R.drawable.weather_mostly_cloudy_day_dark;
            }
            else {
                return !darkMode ? R.drawable.weather_mostly_cloudy_night : R.drawable.weather_mostly_cloudy_night_dark;
            }
        }
        else if (weatherId >= 804 && weatherId <= 899) {
            // Cloudy
            return !darkMode ? R.drawable.weather_cloudy : R.drawable.weather_cloudy_dark;
        }
        else if (weatherId >= 900 && weatherId <= 999) {
            // Extreme weather
            return !darkMode ? R.drawable.weather_extreme : R.drawable.weather_extreme_dark;
        }
        else {
            // Unknown weather
            return !darkMode ? R.drawable.weather_wtf : R.drawable.weather_wtf_dark;
        }
    }

    /**
     * Determines if it's day or night as reported by the weather provider.
     *
     * @return Returns true if it's day, false if it's night.
     */
    public boolean isDay(Weather weather) {
        long sunrise = weather.mLocation.getSunrise();
        long sunset = weather.mLocation.getSunset();
        final long currTime = System.currentTimeMillis() / 1000;
        return currTime > sunrise && currTime < sunset;
    }

    /**
     * Gets the temperature string for the weather.
     *
     * @param weather  The weather to get the temperature string for
     * @param darkMode True if the widget is in dark mode, false otherwise
     *
     * @return Returns the temperature string
     */
    public CharSequence getTempString(Weather weather, boolean darkMode) {
        final float temp;
        if (weather != null) {
            temp = weather.mTemperature.getTemp();
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
        return t;
    }
}