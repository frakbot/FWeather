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

package net.frakbot.FWeather.fragments;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceFragment;

/**
 * PreferenceFragment extension that takes care of registering/unregistering against
 * SharedPreferences changes and notifying any changes to the underlying BackupManager.
 *
 * @author Francesco Pontillo
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BackupPreferenceFragment extends PreferenceFragment implements
                                                                 SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(
        SharedPreferences sharedPreferences, String s) {
        new BackupManager(getActivity()).dataChanged();
    }
}
