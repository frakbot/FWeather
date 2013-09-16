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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import net.frakbot.global.Const;
import net.frakbot.util.log.FLog;

import java.util.Calendar;

/**
 * Helper class with convenience methods for managing the update alarm.
 *
 * @author Francesco Pontillo
 */
public class AlarmHelper {
    private static final String TAG = AlarmHelper.class.getSimpleName();

    /**
     * Reads the current alarm rate, deletes all pending alarms
     * and reschedule the given Intent for launch.t
     *
     * @param context The given Context
     */
    public static void rescheduleAlarm(Context context) {
        // Get the update rate from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String valuePreference = prefs.getString(Const.Preferences.SYNC_FREQUENCY, "-1");
        int value = Integer.decode(valuePreference);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, value);

        FLog.d(context, TAG, "Rescheduling FWeather update in " + Integer.toString(value) + " seconds...");

        // Delete all previous alarms
        deleteAlarms(context);

        // Get the AlarmManager instance
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Schedule the alarm (not repeating)
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), getUpdateIntent(context));

        FLog.d(context, TAG, "FWeather update scheduled in " + Integer.toString(value) + " seconds");
    }

    /**
     * Deletes all pending alarms.
     * @param context The given Context
     */
    public static void deleteAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getUpdateIntent(context));
    }

    private static PendingIntent getUpdateIntent(Context context) {
        Intent intent = new Intent(Const.Intents.ACTION_UPDATE_FRAKKING_WIDGET);
        return PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
