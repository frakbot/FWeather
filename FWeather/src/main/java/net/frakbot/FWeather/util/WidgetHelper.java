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
import net.frakbot.common.SantaLittleHelper;
import net.frakbot.common.WeatherResources;
import net.frakbot.global.Const;
import net.frakbot.util.log.FLog;

/**
 * Helper class that deals with finding resources to assign to
 * the widget views.
 * <p/>
 * <b>WARNING!</b> Unavoidable spaghetti lies in here (maybe not anymore).
 */
public class WidgetHelper {

    private Context mContext;

    public WidgetHelper(Context c) {
        mContext = c;
    }

    /**
     * Gets the instances of the FWeather widget.
     *
     * @param context the Context
     * @return Array of int containing all of the widget ids
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
            } else {
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
    public Spanned getWeatherMainString(WeatherData weather, boolean darkMode) {
        final int weatherId;
        if (weather != null) {
            weatherId = weather.conditionCode;
        } else {
            weatherId = -1;
        }

        Spanned randomSpanned = getGenericRandomWeatherSpanned("weather_code", weatherId, darkMode);
        return randomSpanned;
    }

    /**
     * Gets the string ID representing the weather.
     *
     * @param weather  The weather to get the string for
     *
     * @return Returns the corresponding weather string
     */
    public int getWeatherMainStringArrayId(WeatherData weather) {
        final int weatherId;
        if (weather != null) {
            weatherId = weather.conditionCode;
        } else {
            weatherId = -1;
        }
        return getGenericStringNameArrayId("weather_code", weatherId);
    }

    public int getRandomStringFromArray(int arrayId) {
        String[] candidates = mContext.getResources().getStringArray(arrayId);
        return getRandomStringId(candidates);
    }

    /**
     * Gets the temperature string for the weather.
     *
     * @param weather  The weather to get the temperature string for
     * @param darkMode True if the widget is in dark mode, false otherwise
     * @return Returns the temperature string
     */
    public Spanned getWeatherTempString(WeatherData weather, boolean darkMode) {
        final float temp;

        if (weather != null) {
            if (weather.conditionCode == WeatherData.WEATHER_ID_ERR_NO_LOCATION) {
                // Error: no location available
                temp = WeatherData.WEATHER_ID_ERR_NO_LOCATION;
            } else if (weather.conditionCode == WeatherData.WEATHER_ID_ERR_NO_NETWORK) {
                // Error: no location or no network available
                temp = WeatherData.WEATHER_ID_ERR_NO_NETWORK;
            } else {
                temp = weather.temperature;
            }
        } else {
            temp = WeatherData.WEATHER_ID_ERR_WTF;
        }

        int tempRangeDescriptor = WeatherData.WEATHER_ID_ERR_WTF;

        // Loop for every temperature
        int[] temperatures = new int[] {-10002, -10001, -10000, -1, 15, 28, 1000};
        for (int t : temperatures) {
            // If the range minimum bound matches
            if (temp <= t) {
                // Select it and let's grab a beer
                tempRangeDescriptor = t;
                break;
            }
        }

        // Select a random temeprature string, formst it and blah blah
        Spanned randomSpanned = getGenericRandomWeatherSpanned("weather_temp", tempRangeDescriptor, darkMode);
        return randomSpanned;
    }

    /**
     * Gets the temperature string ID for the weather.
     *
     * @param weather  The weather to get the temperature string ID for
     * @return Returns the temperature string ID
     */
    public int getWeatherTempStringArrayId(WeatherData weather) {
        final float temp;

        if (weather != null) {
            if (weather.conditionCode == WeatherData.WEATHER_ID_ERR_NO_LOCATION) {
                // Error: no location available
                temp = WeatherData.WEATHER_ID_ERR_NO_LOCATION;
            } else if (weather.conditionCode == WeatherData.WEATHER_ID_ERR_NO_NETWORK) {
                // Error: no location or no network available
                temp = WeatherData.WEATHER_ID_ERR_NO_NETWORK;
            } else {
                temp = weather.temperature;
            }
        } else {
            temp = WeatherData.WEATHER_ID_ERR_WTF;
        }

        int tempRangeDescriptor = WeatherData.WEATHER_ID_ERR_WTF;

        // Loop for every temperature
        int[] temperatures = new int[] {-10002, -10001, -10000, -1, 15, 28, 1000};
        for (int t : temperatures) {
            // If the range minimum bound matches
            if (temp <= t) {
                // Select it and let's grab a beer
                tempRangeDescriptor = t;
                break;
            }
        }

        return getGenericStringNameArrayId("weather_temp", tempRangeDescriptor);
    }

    /**
     * Gets the ID of the image representing the weather.
     *
     * @param weather  The weather to get the image for
     * @param darkMode True if the widget is in dark mode, false otherwise
     * @return Returns the corresponding weather image ID
     */
    public int getWeatherImageId(WeatherData weather, boolean darkMode) {
        final String packageName = mContext.getPackageName();
        final int weatherId;

        if (weather != null) {
            weatherId = weather.conditionCode;
        } else {
            weatherId = -1;
        }

        String valueId = buildResourceName("weather_image", weatherId);
        // Get the resource string id
        int stringId = mContext.getResources().getIdentifier(valueId, "string", packageName);
        String imageName = mContext.getResources().getString(stringId);
        if (imageName == null) {
            imageName = "err_wtf";
        }
        if (darkMode) {
            imageName += "_dark";
        }

        return mContext.getResources().getIdentifier(imageName, "drawable", packageName);
    }

    /**
     * Build a resource name starting from the prefix and an integer value.
     * The resulting {@link java.lang.String} will be in the following formats:
     *  - prefix_value if the value is >= 0
     *  - prefix_m_value if the value is < 0
     *
     * @param prefix    The prefix for the resource name
     * @param value     The value to build the resource name from
     * @return          A string in the "prefix_[m_]value" format
     */
    private String buildResourceName(String prefix, int value) {
        // Create the array identifier string, such as "code_32" or "code_m_10000"
        String valueId = ((Integer) Math.abs(Integer.valueOf(value))).toString();
        if (value < 0) {
            valueId = "m_" + valueId;
        }
        valueId = prefix + "_" + valueId;
        return valueId;
    }

    /**
     * Get a generic {@link java.lang.String} array by using the prefix and the value.
     *
     * @param prefix   The prefix for the string array (e.g. "code")
     * @param value    The value to attach to the prefix
     *
     * @return The randomly selected {@link android.text.Spanned} text
     */
    private String[] getGenericStringNameArray(String prefix, int value) {
        final String packageName = mContext.getPackageName();
        // Create the array identifier string, such as "code_32" or "code_m_10000"
        String valueId = buildResourceName(prefix, value);
        // Get the array int id
        int arrayId = mContext.getResources().getIdentifier(valueId, "array", packageName);
        if (arrayId == 0) {
            FLog.w(mContext, "WidgetHelper", String.format("No resource named %s", valueId));
            return null;
        }
        // Get the candidate strings
        String[] candidates = mContext.getResources().getStringArray(arrayId);
        return candidates;
    }

    private int getGenericStringNameArrayId(String prefix, int value) {
        final String packageName = mContext.getPackageName();
        // Create the array identifier string, such as "code_32" or "code_m_10000"
        String valueId = buildResourceName(prefix, value);
        // Get the array int id
        int arrayId = mContext.getResources().getIdentifier(valueId, "array", packageName);
        if (arrayId == 0) {
            FLog.w(mContext, "WidgetHelper", String.format("No resource named %s", valueId));
            return 0;
        }
        return arrayId;
    }

    /**
     * Get a generic random weather {@link android.text.Spanned} by doing the following:
     * 1) Uses the prefix and the value to retrieve a custom {@link java.lang.String} array
     * 2) Selects a pseudo-random {@link java.lang.String} from the retrieved array
     * 3) Gets the corresponding string with the random selected name
     * 4) Gets the default "light" color with the same string name
     * 5) Gets the "dark" color by appending "_dark" to the selected string name
     *
     * @param prefix   The prefix for the string array (e.g. "code")
     * @param value    The value to attach to the prefix
     * @param darkMode true to return a dark {@link android.text.Spanned} text
     *
     * @return The randomly selected {@link android.text.Spanned} text
     */
    private Spanned getGenericRandomWeatherTextId(String prefix, int value, boolean darkMode) {
        String packageName = mContext.getPackageName();
        // Get a pseudo-random string
        String theChosenOne = getRandomString(getGenericStringNameArray(prefix, value));
        // Retrieve the string name, the color and dark color
        int theChosenOneId = mContext.getResources().getIdentifier(theChosenOne, "string", packageName);
        int colorId = mContext.getResources().getIdentifier(theChosenOne, "color", packageName);
        int colorDarkId = mContext.getResources().getIdentifier(theChosenOne + "_dark", "color", packageName);
        // Return the spanned string
        return SantaLittleHelper.getColoredSpannedString(mContext, theChosenOneId, colorId, colorDarkId, darkMode);
    }

    /**
     * Get a generic random weather {@link android.text.Spanned} by doing the following:
     * 1) Uses the prefix and the value to retrieve a custom {@link java.lang.String} array
     * 2) Selects a pseudo-random {@link java.lang.String} from the retrieved array
     * 3) Gets the corresponding string with the random selected name
     * 4) Gets the default "light" color with the same string name
     * 5) Gets the "dark" color by appending "_dark" to the selected string name
     *
     * @param prefix   The prefix for the string array (e.g. "code")
     * @param value    The value to attach to the prefix
     * @param darkMode true to return a dark {@link android.text.Spanned} text
     *
     * @return The randomly selected {@link android.text.Spanned} text
     */
    private Spanned getGenericRandomWeatherSpanned(String prefix, int value, boolean darkMode) {
        String packageName = mContext.getPackageName();
        // Get a pseudo-random string
        String theChosenOne = getRandomString(getGenericStringNameArray(prefix, value));
        // Retrieve the string name, the color and dark color
        int theChosenOneId = mContext.getResources().getIdentifier(theChosenOne, "string", packageName);
        int colorId = mContext.getResources().getIdentifier(theChosenOne, "color", packageName);
        int colorDarkId = mContext.getResources().getIdentifier(theChosenOne + "_dark", "color", packageName);
        // Return the spanned string
        return SantaLittleHelper.getColoredSpannedString(mContext, theChosenOneId, colorId, colorDarkId, darkMode);
    }

    /**
     * Get a pseudo-random string ID from a string array.
     *
     * @param candidates The candidates {@link java.lang.String}s
     * @return A pseudo-random {@link java.lang.String} ID as an int
     */
    private int getRandomStringId(String[] candidates) {
        if (candidates == null || candidates.length <= 0) {
            return -1;
        }
        // Save some computing power when possible
        if (candidates.length == 1) {
            return 0;
        }
        int min = 0;
        int max = candidates.length - 1;
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    /**
     * Get a pseudo-random string from a string array.
     *
     * @param candidates The candidates {@link java.lang.String}s
     * @return A pseudo-random {@link java.lang.String}
     */
    private String getRandomString(String[] candidates) {
        return candidates[getRandomStringId(candidates)];
    }

    /**
     * Gets the right color for the widget background, depending on the user
     * preferences and the dark mode state.
     *
     * @param bgOpacityPrefValue The value of the {@link net.frakbot.FWeather.R.string#pref_key_ui_bgopacity}
     *                           preference
     * @param darkMode           True if the widget is in dark mode (and thus requires a bright BG), false otherwise
     * @return Returns the color to be assigned to the widget BG
     */
    public int getWidgetBGColor(int bgOpacityPrefValue, boolean darkMode) {
        TypedArray colors = mContext.getResources()
                .obtainTypedArray(darkMode ? R.array.bg_colors_darkmode : R.array.bg_colors);

        final int opacityStep = 25;
        // Normalize the opacity
        int modulus = bgOpacityPrefValue % opacityStep;
        if (modulus != 0) {
            FLog.w("WidgetHelper", "Invalid BG preference value detected: " + bgOpacityPrefValue);
            bgOpacityPrefValue = bgOpacityPrefValue - modulus;
        }

        // We assume a fully transparent BG color as default
        return colors.getColor(bgOpacityPrefValue/opacityStep, 0x00000000);

    }

    /**
     * Makes a customized Toast on the specified Context,
     * using the text and duration provided.
     *
     * @param c        The Context to build the Toast within
     * @param text     The text of the Toast
     * @param duration The duration of the Toast (see Toast's duration)
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

    /**
     * Get the sharing string by using the given weather data.
     *
     * @param weatherData The {@link net.frakbot.FWeather.updater.weather.model.WeatherData} to build the share
     *                    string from
     * @return The sharing string
     */
    public String getShareString(WeatherData weatherData) {
        String weather = getWeatherMainString(weatherData, false).toString();
        return weather + " " + Const.Share.VIA;
    }

    /**
     * Get the sharing string by using the given weather data.
     *
     * @param weatherResources The {@link net.frakbot.common.WeatherResources} to build the share
     *                    string from
     * @return The sharing string
     */
    public String getShareString(WeatherResources weatherResources) {
        String weather = mContext.getResources().getStringArray(weatherResources.getMainTextArrayId())[weatherResources.getMainTextPosition()];
        String shareText = Html.fromHtml(mContext.getString(mContext.getResources().getIdentifier(weather, "string", mContext.getPackageName()))).toString();
        return shareText + " " + Const.Share.VIA;
    }
}