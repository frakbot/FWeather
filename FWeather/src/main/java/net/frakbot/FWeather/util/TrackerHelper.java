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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Map;

import net.frakbot.FWeather.R;
import net.frakbot.global.Const;
import net.frakbot.util.log.FLog;

/**
 * Helper for the Google Analytics tracking.
 * Updated for Google Analytics library v4.
 *
 * @author Francesco Pontillo, Sebastiano Poggi
 */
public class TrackerHelper {

    private static final String TAG = TrackerHelper.class.getSimpleName();

    private static Tracker sTracker;

    private static synchronized Tracker getTracker(Context context) {
        if (sTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context.getApplicationContext());
            sTracker = analytics.newTracker(R.xml.google_analytics);
            sTracker.enableAutoActivityTracking(true);
        }
        return sTracker;
    }

    /**
     * Send an exception to Google Analytics, if the user hasn't opted out.
     *
     * @param context     The {@link Context}
     * @param description The description of the occurred {@link Exception}
     * @param fatal       true if the {@link Exception} was fatal, false otherwise
     */
    public static void sendException(Context context, String description, boolean fatal) {
        Tracker tracker = getTracker(context);
        tracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(description)
                .setFatal(fatal)
                .build());
    }

    /**
     * Send an exception to Google Analytics, if the user hasn't opted out.
     *
     * @param context The {@link Context}
     * @param label   The label for the event
     */
    public static void sendEvent(Context context, String label) {
        Tracker tracker = getTracker(context);
        tracker.send(new HitBuilders.EventBuilder()
                .setLabel(label)
                .build());
    }

    /**
     * Send the preference change to Google Analytics, if the user has chosen to do so.
     * It also sets the Google Analytics optout option, if the changed preference is ANALYTICS.
     *
     * @param context       The {@link Context}
     * @param preferenceKey The key of the changed preference
     * @param value         The new value for the changed preference
     */
    public static void preferenceChange(Context context, String preferenceKey, Long value) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        // Read the current tracking status from Google Analytics (defaults to true)
        boolean track = !analytics.getAppOptOut();

        // If the tracking preference has changed
        if (preferenceKey.equals(Const.Preferences.ANALYTICS)) {
            // Update the local tracking value
            track = (value == 1);
            // Tell analytics to track or not to track accordingly
            GoogleAnalytics.getInstance(context).setAppOptOut(!track);
        }

        if (track) {
            Tracker tracker = getTracker(context);
            Map<String, String> event = new HitBuilders.EventBuilder(Const.Preferences.PREFERENCE, Const.Preferences.CHANGE)
                    .setLabel(preferenceKey)
                    .setValue(value)
                    .build();
            tracker.send(event);
            FLog.d(context, TAG, "Tracked preference changed event");
        } else {
            FLog.d(context, TAG, "Could not track preference changed event, analytics is disabled by user");
        }
    }

    /**
     * Initialize the Google Analytics option for opting out of tracking by reading the {@link SharedPreferences}.
     * This method should be called every time the app is launched, as the optout value gets lost by the
     * Google Analytics library.
     *
     * @param context The {@link Context} of the application to set the optout option
     */
    public static void initGoogleAnalyticsOptout(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean track = prefs.getBoolean(Const.Preferences.ANALYTICS, false);
        GoogleAnalytics.getInstance(context).setAppOptOut(!track);
    }
}
