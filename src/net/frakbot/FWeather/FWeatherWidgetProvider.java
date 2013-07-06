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

package net.frakbot.FWeather;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import net.frakbot.FWeather.updater.UpdaterService;

public class FWeatherWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final ApplicationInfo info = context.getApplicationInfo();
        final String tag = info != null ? info.name : "FWeather";
        Log.i(tag, "Update started");

        final Intent updaterIntent =
            new Intent(context, UpdaterService.class)
                .putExtra(UpdaterService.EXTRA_WIDGET_IDS, appWidgetIds);

        context.startService(updaterIntent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

            final ApplicationInfo info = context.getApplicationInfo();
            final String tag = info != null ? info.name : "FWeather";

            Log.i(tag, "Widget options changed, updating it");
            final Intent updaterIntent =
                new Intent(context, UpdaterService.class)
                    .putExtra(UpdaterService.EXTRA_WIDGET_IDS, new int[] {appWidgetId});

            context.startService(updaterIntent);
        }
    }
}
