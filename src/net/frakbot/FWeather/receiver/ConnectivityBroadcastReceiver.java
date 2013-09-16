/*
 * Copyright (C) 2006 The Android Open Source Project Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */

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

package net.frakbot.FWeather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import net.frakbot.FWeather.updater.UpdaterService;
import net.frakbot.FWeather.util.ConnectionHelper;
import net.frakbot.FWeather.util.WidgetHelper;
import net.frakbot.util.log.FLog;

/**
 * BroadcastReceiver listening for changes of connection:
 *   - it has to be registered when the connection is not available
 *   - upon receiving new information, check whether the connection has become available
 *   - launches the UpdaterService, thus disabling itself
 * @author Francesco Pontillo
 */
public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = ConnectivityBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            FLog.w(TAG, "onReceived() called with " + intent);
            return;
        }

        boolean connectivity = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

        // If there's connectivity
        if (connectivity) {
            ConnectionHelper.unregisterConnectivityListener(context.getApplicationContext());
            // Restart the updater service, that will disable this broadcast receiver
            final Intent updaterIntent =
                    new Intent(context, UpdaterService.class)
                            .putExtra(UpdaterService.EXTRA_WIDGET_IDS, WidgetHelper.getWidgetIds(context));
            context.startService(updaterIntent);
        }
    }
}
