/*
 * Copyright 2014 Sebastiano Poggi and Francesco Pontillo
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
package net.frakbot.fweather;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.mariux.teleport.lib.TeleportService;

import net.frakbot.FWeather.R;

public class WearService extends TeleportService {

    private static final String TAG = WearService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        setOnGetMessageTask(new StartActivityTask());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            String action = intent.getAction();
            if ("net.frakbot.fweather.WEATHER_UPDATE".equals(action)) {
                showNotification();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(){
        Intent notificationIntent = new Intent(this, WeatherActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setDisplayIntent(notificationPendingIntent)
                        .addAction(new NotificationCompat.Action(android.R.drawable.ic_menu_share, getString(R.string.share), createAppPendingIntent(this)))
                        .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.sky));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("FWeather update")
                .extend(wearableExtender);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1, notification);
    }

    private static PendingIntent createAppPendingIntent(Context context) {
        Intent notificationIntent = createAppIntent(context);
        return PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static Intent createAppIntent(Context context) {
        PackageManager manager = context.getPackageManager();
        Intent notificationIntent = manager.getLaunchIntentForPackage("net.frakbot.fweather");
        if (notificationIntent == null) {
            notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse("net.frakbot.fweather"));
        }
        return notificationIntent;
    }

    //Task that shows the path of a received message
    public class StartActivityTask extends TeleportService.OnGetMessageTask {

        @Override
        protected void onPostExecute(String path) {
            Log.d(TAG, "Received message with path: ");

            //let`s reset the task (otherwise it will be executed only once)
            setOnGetMessageTask(new StartActivityTask());
        }
    }

}
