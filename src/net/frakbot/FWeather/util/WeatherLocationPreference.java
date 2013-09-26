/*
 * Copyright 2013 Google Inc.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.activity.LocationChooserDialog;
import net.frakbot.util.log.FLog;

/**
 * A preference that allows the user to choose a location, using the Yahoo! GeoPlanet API.
 */
public class WeatherLocationPreference extends Preference {

    private static final String TAG = WeatherLocationPreference.class.getSimpleName();

    public static final String ACTION_SET_VALUE = "set_value";
    public static final String ACTION_CANCELED = "canceled";
    public static final String EXTRA_VALUE = "value";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ACTION_SET_VALUE.equals(intent.getAction())) {
                FLog.d(context, TAG, "Dialog canceled. Unregistering for broadcasts");

                try {
                    context.unregisterReceiver(mReceiver);
                }
                catch (Exception ignored) {
                }
                return;
            }

            if (!ACTION_SET_VALUE.equals(intent.getAction()) || !intent.hasExtra(EXTRA_VALUE)) {
                FLog.i(TAG, "Ignoring intent: " + intent);
                return;
            }

            setValue(intent.getStringExtra(EXTRA_VALUE));
        }
    };

    public WeatherLocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeatherLocationPreference(Context context) {
        super(context);
    }

    public WeatherLocationPreference(Context context, AttributeSet attrs,
                                     int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setValue(String value) {
        FLog.d(getContext(), TAG, "Value received. Unregistering for broadcasts");
        try {
            getContext().unregisterReceiver(mReceiver);
        }
        catch (Exception ignored) {
        }

        if (value == null) {
            value = "";
        }

        if (callChangeListener(value)) {
            persistString(value);
            notifyChanged();
        }
    }

    public static CharSequence getDisplayValue(Context context, String value) {
        if (TextUtils.isEmpty(value) || value.indexOf(',') < 0) {
            return context.getString(R.string.pref_weather_location_automatic);
        }

        String[] woeidAndDisplayName = value.split(",", 2);
        return woeidAndDisplayName[1];
    }

    public static String getWoeidFromValue(String value) {
        if (TextUtils.isEmpty(value) || value.indexOf(',') < 0) {
            return null;
        }

        String[] woeidAndDisplayName = value.split(",", 2);
        return woeidAndDisplayName[0];
    }

    @Override
    protected void onClick() {
        super.onClick();

        // We register the receiver just before opening the Activity.
        // We will unregister it when we receive a result (any) from the Activity.
        FLog.d(getContext(), TAG, "Registering for broadcasts");
        IntentFilter filter = new IntentFilter(ACTION_SET_VALUE);
        filter.addAction(ACTION_CANCELED);
        getContext().registerReceiver(mReceiver, filter);

        final Context context = getContext();
        FLog.d(context, TAG, "Preference clicked, launching dialog");
        context.startActivity(new Intent(context, LocationChooserDialog.class));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedString("") : (String) defaultValue);
    }
}