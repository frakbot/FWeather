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
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import net.frakbot.FWeather.receiver.ConnectivityBroadcastReceiver;

/**
 * Wraps a static BroadcastReceiver to get connection updates.
 */
public class ConnectionHelper {
    private static ConnectivityBroadcastReceiver mReceiver;
    private static boolean isRegistered;

    static {
        mReceiver = new ConnectivityBroadcastReceiver();
        isRegistered = false;
    }

    public static void registerConnectivityListener(Context context) {
        if (!isRegistered) {
            context.registerReceiver(mReceiver,
                                     new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            isRegistered = true;
        }
    }

    public static void unregisterConnectivityListener(Context context) {
        if (mReceiver != null && isRegistered) {
            try {
                context.unregisterReceiver(mReceiver);
            }
            catch (Exception ignored) {
            }
            isRegistered = false;
        }
    }
}
