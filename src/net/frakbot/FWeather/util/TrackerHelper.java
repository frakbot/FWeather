package net.frakbot.FWeather.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.analytics.tracking.android.EasyTracker;
import net.frakbot.FWeather.global.Const;

/**
 * Created by Francesco Pontillo on 03/07/13.
 */
public class TrackerHelper {

    private static final String TAG = TrackerHelper.class.getSimpleName();

    public static void activityStart(Activity context) {
        EasyTracker.getInstance().activityStart(context);
    }

    public static void activityStop(Activity context) {
        EasyTracker.getInstance().activityStop(context);
    }

    public static void sendException(Context context, String description, boolean fatal) {
        EasyTracker.getInstance().setContext(context);
        EasyTracker.getTracker().sendException(description, fatal);
    }

    public static void preferenceChange(Context context, String preferenceKey, Long value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean track = prefs.getBoolean(Const.Preferences.ANALYTICS, false);
        if (track) {
            EasyTracker.getTracker().sendEvent(
                    Const.Preferences.PREFERENCE, Const.Preferences.CHANGE, preferenceKey, value);
            FLog.d(context, TAG, "Tracked preference changed event");
        } else {
            FLog.d(context, TAG, "Could not track preference changed event, analytics is disabled by user");
        }
    }
}
