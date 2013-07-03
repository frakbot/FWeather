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

package net.frakbot.FWeather.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.global.Const;

import java.util.List;

/**
 * A {@link android.preference.PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends SherlockPreferenceActivity implements
                                                                 OnSharedPreferenceChangeListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = null;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sets and shows the title in the ActionBar
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //        getSupportActionBar().setTitle(R.string.activity_settings);  TODO
        //        getSupportActionBar().setIcon(R.drawable.ab_icon_normal);

        buildListener();
        setupSimplePreferencesScreen();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the SharedPreferences listener (me, duh)
        getPreferenceManager().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        // Register a SharedPreferences listener (me, duh)
        getPreferenceManager()
            .getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * This gets called whenever a SharedPreference changes;
     * then, it notifies the BackupManager that something has changed.
     * {@inheritDoc}
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        new BackupManager(this).dataChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // TODO: DashClock-like "done" button in the left part of ActionBar
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
        setPreferenceScreen(screen);

        // Add 'data and sync' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        screen.addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        // Add 'advanced' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_advanced);
        screen.addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_advanced);
        setupAnalyticsOnChangeListener((CheckBoxPreference) findPreference(getString(R.string.pref_key_analytics)));

        // Add 'info' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_info);
        screen.addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_info);
        setupFeedbackOnClickListener(findPreference(getString(R.string.pref_key_feedback)));

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sync_frequency)));
    }

    /**
     * Sets up the OnClick listener for the feedback preference.
     * Inspired by http://blog.tomtasche.at/2012/10/use-built-in-feedback-mechanism-on.html
     *
     * @param preference The feedback preference
     */
    private void setupFeedbackOnClickListener(Preference preference) {
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.i(SettingsActivity.class.getSimpleName(), "Sending feedback");

                if (isInstalledFromPlayStore()) {
                    Intent intent = new Intent(Intent.ACTION_APP_ERROR);

                    // Use the native ApplicationErrorReport
                    ApplicationErrorReport report = new ApplicationErrorReport();
                    report.processName = getApplication().getPackageName();
                    report.packageName = report.processName;
                    report.time = System.currentTimeMillis();
                    report.type = ApplicationErrorReport.TYPE_NONE;
                    report.systemApp = false;

                    intent.putExtra(Intent.EXTRA_BUG_REPORT, report);

                    startActivity(intent);
                }
                else {
                    // Use the backup email mechanism
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[] {"frakbot@gmail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "[FEEDBACK] " + getString(R.string.app_name));
                    email.putExtra(Intent.EXTRA_TEXT, generateFeedbackBody());
                    email.setType("message/rfc822");
                    startActivity(Intent.createChooser(email, getString(R.string.feedback_send_chooser_title)));
                }
                return true;
            }
        });
    }

    /**
     * Builds a feedback email body with some basic system info.
     *
     * @return Returns the generated system info.
     */
    private String generateFeedbackBody() {
        StringBuilder sb = new StringBuilder("\n\n" +
                                             "-----------\n" +
                                             "System info\n" +
                                             "-----------\n\n");

        // HW information
        sb.append("Device model: ").append(Build.MODEL).append("\n");
        sb.append("Manifacturer: ").append(Build.MANUFACTURER).append("\n");
        sb.append("Brand: ").append(Build.BRAND).append("\n");
        sb.append("CPU ABI: ").append(Build.CPU_ABI).append("\n");
        sb.append("Product: ").append(Build.PRODUCT).append("\n").append("\n");

        // SW information
        sb.append("Android version: ").append(Build.VERSION.CODENAME).append("\n");
        sb.append("Release: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("Incremental: ").append(Build.VERSION.INCREMENTAL).append("\n");
        sb.append("Build: ").append(Build.FINGERPRINT);

        return sb.toString();
    }

    /**
     * Determines if the app is installed from the Google Play Store.
     *
     * @return Returns true if the app is installed from the Google
     *         Play Store, or false if it has been installed from other
     *         sources (sideload, other app stores, etc)
     */
    private boolean isInstalledFromPlayStore() {
        PackageManager pm = getPackageManager();
        String installationSource = pm.getInstallerPackageName(getPackageName());
        return "com.android.vending".equals(installationSource) ||
               "com.google.android.feedback".equals(installationSource);   // This is for Titanium Backup compatibility
    }

    /**
     * Sets up the Analytics preference listener.
     *
     * @param preference The Analytics preference.
     */
    private void setupAnalyticsOnChangeListener(CheckBoxPreference preference) {
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object newValue) {
                if (newValue == Boolean.FALSE) {

                    AlertDialog.Builder b = new AlertDialog.Builder(SettingsActivity.this);
                    b.setMessage(R.string.analytics_disable_warning)
                     .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = b.create();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Toast.makeText(SettingsActivity.this, R.string.analytics_disabled,
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    });
                    dialog.show();

                    Log.i(SettingsActivity.class.getSimpleName(), "Enabled Google Analytics");

                    return true;
                }
                else {
                    Toast.makeText(SettingsActivity.this, R.string.analytics_enabled_thanks, Toast.LENGTH_SHORT)
                         .show();

                    Log.i(SettingsActivity.class.getSimpleName(), "Enabled Google Analytics");
                    return true;
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
               >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link android.preference.PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    @SuppressWarnings({"ConstantConditions", "PointlessBooleanExpression"})
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
               || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
               || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference
            .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
            preference,
            PreferenceManager.getDefaultSharedPreferences(
                preference.getContext()).getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends BackupPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            ((SettingsActivity) getActivity()).bindPreferenceSummaryToValue(
                findPreference(getString(R.string.pref_key_sync_frequency)));
        }
    }

    /**
     * This fragment shows advanced settings only. It is used when the activity
     * is showing a two-pane settings UI.
     */
    @SuppressLint("ValidFragment")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public class AdvancedPreferenceFragment extends BackupPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_advanced);

            setupAnalyticsOnChangeListener((CheckBoxPreference) findPreference(getString(R.string.pref_key_analytics)));
        }
    }

    /**
     * This fragment shows information only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @SuppressLint("ValidFragment")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public class InformationPreferenceFragment extends BackupPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_info);

            setupFeedbackOnClickListener(findPreference(getString(R.string.pref_key_feedback)));
        }
    }

    /** Builds the listener for the preference changes. */
    private void buildListener() {
        sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                String stringValue = value.toString();

                if (preference.getKey().equals(Const.Preferences.SYNC_FREQUENCY)) {
                    SharedPreferences prefs = preference.getSharedPreferences();
                    // If the old value differs from the new value
                    if (!prefs.getString(Const.Preferences.SYNC_FREQUENCY, "-1").equals(stringValue)) {
                        sendSyncPreferenceChangedBroadcast();
                    }
                }

                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference
                        .setSummary(index >= 0 ? listPreference.getEntries()[index]
                                               : null);

                }
                else {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                }
                return true;
            }
        };
    }

    /**
     * Broadcasts an action that causes the update of the updater alarm.
     * If the preference changes, the update rate of the application data
     * will change too, and a new update will be requested.
     */
    private void sendSyncPreferenceChangedBroadcast() {
        Intent i = new Intent(Const.Intents.SYNC_RATE_PREFERENCE_CHANGED_ACTION);
        sendBroadcast(i);
    }

    /**
     * PreferenceFragment extension that takes care of registering/unregistering against
     * SharedPreferences changes and notifying any changes to the underlying BackupManager.
     *
     * @author Francesco Pontillo
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BackupPreferenceFragment extends PreferenceFragment implements
                                                                            OnSharedPreferenceChangeListener {

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

}
