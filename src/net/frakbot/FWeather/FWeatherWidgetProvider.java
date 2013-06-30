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
