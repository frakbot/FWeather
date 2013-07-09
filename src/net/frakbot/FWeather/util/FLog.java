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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import net.frakbot.FWeather.BuildConfig;
import net.frakbot.FWeather.global.Const;

import java.io.IOException;

/**
 * Log class for handling custom logic.
 *
 * @author Francesco Pontillo
 */
public class FLog {

    private static String TAG_PREFIX = null;

    /*
     *  You can change the default level by setting a system property:
     *      'setprop log.tag.<YOUR_LOG_TAG> <LEVEL>'
     *  Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS.
     *  SUPPRESS will turn off all logging for your tag.
     */
    public static boolean DEBUG = false;
    public static boolean VERBOSE = false;
    private boolean mInitialized = false;

    private FLog() {
    }

    /**
     * Initialize the logging system.
     *
     * @param context The context used to initialize FLog
     */
    public void initLog(Context context) {
        if (mInitialized) {
            FLog.d("FLog", "Trying to re-initialize FLog, ignoring");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (Const.Preferences.DEBUG.equals(key)) {
                    FLog.d("FLogPrefWatch", "Debug mode preference change detected.");

                    boolean val = sharedPreferences.getBoolean(key, false);
                    FLog.v("FLogPrefWatch", "New DEBUG value: " + val);

                    if (val) {
                        if (VERBOSE) {
                            Log.i("FLogPrefWatch", "Ignoring DEBUG change, we're already in VERBOSE level");
                            return;
                        }
                        else if (DEBUG) {
                            Log.i("FLogPrefWatch", "Ignoring DEBUG change, we're already in DEBUG level");
                            return;
                        }
                    }

                    setLogLevel(val ? LogLevel.DEBUG : LogLevel.INFO);
                }
            }
        });
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     */
    public static int v(Context context, String tag, String msg) {
        return Log.v(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        return Log.v(getTag(null, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     * @param tr      An exception to log
     */
    public static int v(Context context, String tag, String msg, Throwable tr) {
        return Log.v(getTag(context, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        return Log.v(getTag(null, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     */
    public static int d(Context context, String tag, String msg) {
        return Log.d(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        return Log.d(getTag(null, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     * @param tr      An exception to log
     */
    public static int d(Context context, String tag, String msg, Throwable tr) {
        return Log.d(getTag(context, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        return Log.d(getTag(null, tag), msg, tr);
    }

    /**
     * Send an {@link android.util.Log#INFO} log message.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     */
    public static int i(Context context, String tag, String msg) {
        return Log.i(getTag(context, tag), msg);
    }

    /**
     * Send an {@link android.util.Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        return Log.i(getTag(null, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     * @param tr      An exception to log
     */
    public static int i(Context context, String tag, String msg, Throwable tr) {
        return Log.i(getTag(context, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        return Log.i(getTag(null, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     */
    public static int w(Context context, String tag, String msg) {
        return Log.w(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        return Log.w(getTag(null, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     * @param tr      An exception to log
     */
    public static int w(Context context, String tag, String msg, Throwable tr) {
        return Log.w(getTag(context, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(getTag(null, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param tr      An exception to log
     */
    public static int w(Context context, String tag, Throwable tr) {
        return Log.w(getTag(context, tag), tr);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param tr  An exception to log
     */
    public static int w(String tag, Throwable tr) {
        return Log.w(getTag(null, tag), tr);
    }

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     */
    public static int e(Context context, String tag, String msg) {
        return Log.e(getTag(context, tag), msg);
    }

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        return Log.e(getTag(null, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.  It usually identifies
     *                the class or activity where the log call occurs.
     * @param msg     The message you would like logged.
     * @param tr      An exception to log
     */
    public static int e(Context context, String tag, String msg, Throwable tr) {
        return Log.e(getTag(context, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(getTag(null, tag), msg, tr);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT with the call stack.
     * Depending on system configuration, a report may be added to the
     * {@link android.os.DropBoxManager} and/or the process may be terminated
     * immediately with an error dialog.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.
     * @param msg     The message you would like logged.
     */
    public static int wtf(Context context, String tag, String msg) {
        return Log.wtf(getTag(context, tag), msg);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT with the call stack.
     * Depending on system configuration, a report may be added to the
     * {@link android.os.DropBoxManager} and/or the process may be terminated
     * immediately with an error dialog.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static int wtf(String tag, String msg) {
        return Log.wtf(getTag(null, tag), msg);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link android.util.Log#wtf(String, String)}, with an exception to log.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.
     * @param tr      An exception to log.
     */
    public static int wtf(Context context, String tag, Throwable tr) {
        return Log.wtf(getTag(context, tag), tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link android.util.Log#wtf(String, String)}, with an exception to log.
     *
     * @param tag Used to identify the source of a log message.
     * @param tr  An exception to log.
     */
    public static int wtf(String tag, Throwable tr) {
        return Log.wtf(getTag(null, tag), tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link android.util.Log#wtf(String, Throwable)}, with a message as well.
     *
     * @param context Used to retrieve the version number.
     * @param tag     Used to identify the source of a log message.
     * @param msg     The message you would like logged.
     * @param tr      An exception to log.  May be null.
     */
    public static int wtf(Context context, String tag, String msg, Throwable tr) {
        return Log.wtf(getTag(context, tag), msg, tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link android.util.Log#wtf(String, Throwable)}, with a message as well.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr  An exception to log.  May be null.
     */
    public static int wtf(String tag, String msg, Throwable tr) {
        return Log.wtf(getTag(null, tag), msg, tr);
    }

    /** Updates the logging levels, by checking the system properties. */
    @SuppressWarnings({"ConstantConditions", "PointlessBooleanExpression"})
    public static void recheckLogLevels() {
        VERBOSE = BuildConfig.DEBUG || Log.isLoggable(Const.APP_NAME, Log.VERBOSE);
        DEBUG = BuildConfig.DEBUG || Log.isLoggable(Const.APP_NAME, Log.DEBUG);
    }

    /**
     * Changes the log level for the app.
     *
     * @param level The new logging level.
     * @return Returns true if the logging level has been changed,
     *         false otherwise.
     */
    public static boolean setLogLevel(LogLevel level) {
        if (!setProp(String.format("log.tag.%s", Const.APP_NAME), level.toString())) {
            return false;
        }

        recheckLogLevels();

        // Double check if the value was set, just in case
        if (level == LogLevel.DEBUG && !DEBUG) {
            FLog.w("FLog", "Seems like the logging level wasn't actually set to DEBUG");
            return false;
        }
        else if (level == LogLevel.VERBOSE && (!VERBOSE || !DEBUG)) {
            FLog.w("FLog", "Seems like the logging level wasn't actually set to VERBOSE");
            return false;
        }

        return true;
    }

    /**
     * Sets a property value by calling the <code>setprop</code> program.
     * <p/>
     * You can also set properties from a connected computer issuing:
     * <pre>adb shell setprop {propName} {value}</pre>
     *
     * @param propName The name of the property to write.
     * @param propVal  The value of the property to set.
     *
     * @return Returns true if the operation was successfull, or false
     *         if there was any error.
     */
    private static boolean setProp(String propName, String propVal) {
        try {
            Process proc = Runtime.getRuntime().exec(new String[] {"/system/bin/setprop", propName, propVal});
            if (proc.waitFor() != 0) {
                FLog.e("FLog", "Couldn't write property " + propName + ".");
                return false;
            }
        }
        catch (IOException e) {
            FLog.e("FLog", "Couldn't write property " + propName + ".", e);
            return false;
        }
        catch (InterruptedException e) {
            FLog.w("FLog", "Interrupted while waiting for setprop to exit.");
            return false;
        }

        return true;
    }

    /**
     * Gets the log tag using the base log tag (the app name and version),
     * plus the specified log tag.
     * <p/>
     * The base log tag is prepared using the provided Context, if it's still
     * uninitialized. If the Context is null, we use the hardcoded
     * {@link Const#APP_NAME} constant, and postpone the log tag initialization.
     *
     * @param context   The Context used if the base log tag has to be initialized
     * @param tagSuffix The tag suffix (specific log tag)
     *
     * @return Returns the full log tag, if possible, or some hacked together
     *         version of it if the base log tag has not been initialized yet
     */
    private static String getTag(Context context, String tagSuffix) {
        if (TAG_PREFIX != null) {
            // The TAG_PREFIX has been initialized, we're good to go!
            return TAG_PREFIX + (!TextUtils.isEmpty(tagSuffix) ? "-" + tagSuffix : "");
        }

        if (context == null) {
            // This is the case where we still haven't inizialized TAG_PREFIX, and
            // we're still not able to. Fall back onto the hardcoded APP_NAME (no version).
            return Const.APP_NAME + (!TextUtils.isEmpty(tagSuffix) ? "-" + tagSuffix : "");
        }

        // Get the app name and version
        PackageManager pm;
        String packageName;
        String versionName;
        pm = context.getPackageManager();
        packageName = context.getPackageName();

        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A";
        }

        TAG_PREFIX = String.format("%s-%s", Const.APP_NAME, versionName);

        return TAG_PREFIX + (!TextUtils.isEmpty(tagSuffix) ? "-" + tagSuffix : "");
    }
}
