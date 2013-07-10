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
import net.frakbot.FWeather.global.Const;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Log class for handling custom logic.
 *
 * @author Francesco Pontillo, Sebastiano Poggi
 */
@SuppressWarnings("UnusedDeclaration")
public class FLog {

    /** Log tag max length, as defined by Android */
    public static final int TAG_MAX_LENGTH = 23;
    private static final String SCRIPT_SETPROP = "setprop %1$s %2$s";
    private static String TAG_PREFIX = null;

    private static LogLevel sForcedLevel = null;

    /*
     *  You can change the default level by setting a system property:
     *      'setprop log.tag.<YOUR_LOG_TAG> <LEVEL>'
     *  Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS.
     *  SUPPRESS will turn off all logging for your tag.
     */
    private static boolean DEBUG = false;
    private static boolean VERBOSE = false;

    /**
     * Return code that indicates that the FLog class hasn't been
     * initialized yet.
     */
    public static final int ERROR_NOT_INITIALIZED = Integer.MIN_VALUE;

    /**
     * Return code that indicates that the log has been filtered out
     * because of the current log level preferences.
     */
    public static final int ERROR_FILTERED = Integer.MIN_VALUE + 1;

    private static final AtomicBoolean sInitialized = new AtomicBoolean(false);
    private static final Object sLock = new Object();

    @SuppressWarnings("FieldCanBeLocal")
    private static SharedPreferences.OnSharedPreferenceChangeListener sDebugChangeListener =
        new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (Const.Preferences.DEBUG.equals(key)) {
                    FLog.d("FLogPrefWatch", "Debug mode preference change detected.");

                    boolean forceDebug = sharedPreferences.getBoolean(key, false);
                    FLog.v("FLogPrefWatch", "New DEBUG value: " + forceDebug);

                    setLogLevel(forceDebug ? LogLevel.DEBUG : null);
                }
            }
        };

    private FLog() {
    }

    /**
     * Initialize the logging system.
     *
     * @param context The context used to initialize FLog
     */
    public static void initLog(Context context) {
        if (sInitialized.get()) {
            FLog.d("FLog", "Trying to re-initialize FLog, ignoring");
            return;
        }

        synchronized (sLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.registerOnSharedPreferenceChangeListener(sDebugChangeListener);

            // Initialize the log tag (we could remove all Context-requesting log methods...)
            getTag(context, "FLogInit");

            sInitialized.set(true);
        }
    }

    /**
     * Uninitialize the logging system, disposing all unneeded resources.
     *
     * @param context The context used to uninitialize FLog
     */
    public static void uninitLog(Context context) {
        if (!sInitialized.get()) {
            FLog.d("FLog", "Trying to uninitialize an uninitialized FLog, ignoring");
            return;
        }

        synchronized (sLock) {
            if (sDebugChangeListener != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.unregisterOnSharedPreferenceChangeListener(sDebugChangeListener);
            }

            TAG_PREFIX = null;
            sInitialized.set(false);
        }
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
        if (!sInitialized.get()) return printNotInitializedError();
        return VERBOSE ? Log.v(getTag(context, tag), msg) : ERROR_FILTERED;
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        if (!sInitialized.get()) return printNotInitializedError();
        return VERBOSE ? Log.v(getTag(null, tag), msg) : ERROR_FILTERED;
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
        if (!sInitialized.get()) return printNotInitializedError();
        return VERBOSE ? Log.v(getTag(context, tag), msg, tr) : ERROR_FILTERED;
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
        if (!sInitialized.get()) return printNotInitializedError();
        return VERBOSE ? Log.v(getTag(null, tag), msg, tr) : ERROR_FILTERED;
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
        if (!sInitialized.get()) return printNotInitializedError();
        return DEBUG ? Log.d(getTag(context, tag), msg) : ERROR_FILTERED;
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        if (!sInitialized.get()) return printNotInitializedError();
        return DEBUG ? Log.d(getTag(null, tag), msg) : ERROR_FILTERED;
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
        if (!sInitialized.get()) return printNotInitializedError();
        return DEBUG ? Log.d(getTag(context, tag), msg, tr) : ERROR_FILTERED;
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
        if (!sInitialized.get()) return printNotInitializedError();
        return DEBUG ? Log.d(getTag(null, tag), msg, tr) : ERROR_FILTERED;
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
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
        if (!sInitialized.get()) return printNotInitializedError();
        return Log.wtf(getTag(null, tag), msg, tr);
    }

    /** Updates the logging levels, by checking the system properties. */
    @SuppressWarnings({"ConstantConditions", "PointlessBooleanExpression"})
    public static void recheckLogLevels() {
        String tmpTag = Const.APP_NAME;
        if (tmpTag.length() > TAG_MAX_LENGTH) {
            tmpTag = tmpTag.substring(0, TAG_MAX_LENGTH);
            Log.w("FLog", "(recheck) The app name is too long, it's been trimmed: " + tmpTag);
        }

        VERBOSE = sForcedLevel == null ?
                  Log.isLoggable(tmpTag, Log.VERBOSE) :
                  sForcedLevel.toInt() == LogLevel.VERBOSE.toInt();

        DEBUG = sForcedLevel == null ?
                Log.isLoggable(tmpTag, Log.DEBUG) :
                sForcedLevel.toInt() <= LogLevel.DEBUG.toInt();
    }

    /**
     * Changes the log level for the app.
     *
     * @param level The forced logging level, or null to disable forcing.
     *
     * @return Returns true if the logging level has been changed,
     *         false otherwise.
     */
    public static boolean setLogLevel(LogLevel level) {
        synchronized (sLock) {
            sForcedLevel = level;

            recheckLogLevels();
        }

        //noinspection ConstantConditions
        FLog.i("FLog", "Log level now forced to " + (level != null ? level.toString() : "[not forced]"));

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
        synchronized (sLock) {
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
            int versionCode;
            pm = context.getPackageManager();
            packageName = context.getPackageName();

            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                versionCode = info.versionCode;

                TAG_PREFIX = String.format("%s-v%d", Const.APP_NAME, versionCode);
            }
            catch (PackageManager.NameNotFoundException e) {
                // Unable to retrieve app info, solely rely on the hardcoded app name
                return Const.APP_NAME + (!TextUtils.isEmpty(tagSuffix) ? "-" + tagSuffix : "");
            }

            return TAG_PREFIX + (!TextUtils.isEmpty(tagSuffix) ? "-" + tagSuffix : "");
        }
    }

    /**
     * Prints an error message in the logcat stating that FLog
     * hasn't yet been initialized.
     *
     * @return Always returns {@link #ERROR_NOT_INITIALIZED}
     */
    private static int printNotInitializedError() {
        Log.w(getTag(null, "FLog"), "FLog not initialized yet! Get you shit together, man");
        return ERROR_NOT_INITIALIZED;
    }
}
