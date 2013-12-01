package net.frakbot.FWeather.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import net.frakbot.global.Const;
import net.frakbot.util.log.FLog;

/**
 * Helper for the Google Analytics tracking.
 * Updated for Google Analytics library v3.
 *
 * @author Francesco Pontillo
 */
public class TrackerHelper {

    private static final String TAG = TrackerHelper.class.getSimpleName();

    /**
     * Track a start event for an {@link Activity}.
     * @param activity The {@link Activity} that was started
     */
    public static void activityStart(Activity activity) {
        EasyTracker.getInstance(activity).activityStart(activity);
    }

    /**
     * Track a stop event for an {@link Activity}.
     * @param activity The {@link Activity} that was stopped
     */
    public static void activityStop(Activity activity) {
        EasyTracker.getInstance(activity).activityStop(activity);
    }

    /**
     * Send an exception to Google Analytics, if the user hasn't opted out.
     *
     * @param context The {@link Context}
     * @param description The description of the occurred {@link Exception}
     * @param fatal true if the {@link Exception} was fatal, false otherwise
     */
    public static void sendException(Context context, String description, boolean fatal) {
        EasyTracker exceptionTracker = EasyTracker.getInstance(context);
        exceptionTracker.send(
                MapBuilder.createException(description, fatal).build());
    }

    /**
     * Send the preference change to Google Analytics, if the user has chosen to do so.
     * It also sets the Google Analytics optout option, if the changed preference is ANALYTICS.
     *
     * @param context The {@link Context}
     * @param preferenceKey The key of the changed preference
     * @param value The new value for the changed preference
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
            EasyTracker.getInstance(context).send(MapBuilder.createEvent(
                    Const.Preferences.PREFERENCE, Const.Preferences.CHANGE, preferenceKey, value
            ).build());
            FLog.d(context, TAG, "Tracked preference changed event");
        }
        else {
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
