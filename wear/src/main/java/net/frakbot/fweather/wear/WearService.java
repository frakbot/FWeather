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
package net.frakbot.fweather.wear;

import android.util.Log;

import com.mariux.teleport.lib.TeleportService;

public class WearService extends TeleportService {

    private static final String TAG = WearService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        setOnGetMessageTask(new StartActivityTask());

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
