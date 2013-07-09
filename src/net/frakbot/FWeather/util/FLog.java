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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Log class for handling custom logic.
 * @author Francesco Pontillo
 */
public class FLog {

    private FLog() {
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(Context context, String tag, String msg) {
        return Log.v(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(Context context, String tag, String msg, Throwable tr) {
        return Log.v(getTag(context, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(Context context, String tag, String msg) {
        return Log.d(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(Context context, String tag, String msg, Throwable tr) {
        return Log.d(getTag(context, tag), msg, tr);
    }

    /**
     * Send an {@link android.util.Log#INFO} log message.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(Context context, String tag, String msg) {
        return Log.i(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(Context context, String tag, String msg, Throwable tr) {
        return Log.i(getTag(context, tag), msg, tr);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(Context context, String tag, String msg) {
        return Log.w(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(Context context, String tag, String msg, Throwable tr) {
        return Log.w(getTag(context, tag), msg, tr);
    }

    /*
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(Context context, String tag, Throwable tr) {
        return Log.w(getTag(context, tag), tr);
    }

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(Context context, String tag, String msg) {
        return Log.e(getTag(context, tag), msg);
    }

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(Context context, String tag, String msg, Throwable tr) {
        return Log.e(getTag(context, tag), msg, tr);
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT with the call stack.
     * Depending on system configuration, a report may be added to the
     * {@link android.os.DropBoxManager} and/or the process may be terminated
     * immediately with an error dialog.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static int wtf(Context context, String tag, String msg) {
        return Log.wtf(getTag(context, tag), msg);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link android.util.Log#wtf(String, String)}, with an exception to log.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.
     * @param tr An exception to log.
     */
    public static int wtf(Context context, String tag, Throwable tr) {
        return Log.wtf(getTag(context, tag), tr);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link android.util.Log#wtf(String, Throwable)}, with a message as well.
     * @param context Used to retrieve the version number.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.  May be null.
     */
    public static int wtf(Context context, String tag, String msg, Throwable tr) {
        return Log.wtf(getTag(context, tag), msg, tr);
    }

    private static String getTag(Context context, String originalTag) {
        // Get app version
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
            versionName = "NA";
        }

        String tag = "FW-" + versionName + "-" + originalTag;

        return tag;
    }

}
