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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport;
import android.app.backup.BackupManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.global.Const;
import net.frakbot.FWeather.util.FLog;
import net.frakbot.FWeather.util.TrackerHelper;
import net.frakbot.FWeather.util.WidgetHelper;
import org.jraf.android.backport.switchwidget.SwitchPreference;

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
    private static final String TAG = SettingsActivity.class.getSimpleName();

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = null;
    private int mNewWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null
            && AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())) {

            mNewWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                              AppWidgetManager.INVALID_APPWIDGET_ID);

            // See http://code.google.com/p/android/issues/detail?id=2539
            setResult(RESULT_CANCELED, new Intent()
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mNewWidgetId));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        TrackerHelper.activityStart(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sets and shows the title in the ActionBar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            setupActionBar(actionBar);
        }

        buildListener();
        setupSimplePreferencesScreen();
    }

    private void setupActionBar(ActionBar actionBar) {
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        Button btnDone = (Button) getLayoutInflater().inflate(R.layout.include_ab_done, null);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNewWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mNewWidgetId));
                }

                finish();
            }
        });

        actionBar.setCustomView(btnDone);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the SharedPreferences listener (me, duh)
        PreferenceManager.getDefaultSharedPreferences(this)
                         .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Update the widgets after the configuration is closed
        FLog.d(this, "SettingsActivity", "Closing the settings Activity; updating widgets");
        requestWidgetsUpdate(true, true);
        TrackerHelper.activityStop(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        // Register a SharedPreferences listener (me, duh)
        PreferenceManager.getDefaultSharedPreferences(this)
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

        // Add 'customization' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_customization);
        screen.addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_customization);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        screen.addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);
        setupRefreshNowOnClickListener(findPreference(getString(R.string.pref_key_sync_force)));

        // Add 'advanced' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_advanced);
        screen.addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_advanced);
        setupAnalyticsOnChangeListener((SwitchPreference) findPreference(getString(R.string.pref_key_analytics)));

        // Add 'info' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_info);
        screen.addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_info);
        setupFeedbackOnClickListener(findPreference(getString(R.string.pref_key_feedback)));
        setupChangelogOnClickListener(findPreference(getString(R.string.pref_key_changelog)));

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_sync_frequency)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ui_override_language)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_ui_bgopacity)));
    }

    /**
     * Sets up the OnClick listener for the refresh now preference.
     *
     * @param preference The refresh now preference
     */
    private void setupRefreshNowOnClickListener(Preference preference) {
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FLog.i(SettingsActivity.this, TAG, "Forcing weather update (user request)");
                requestWidgetsUpdate(true);

                return true;
            }
        });
    }

    /**
     * Requests an update of all the widgets we currently have.
     *
     * @param forced True if this is a forced update request, false otherwise
     */
    private void requestWidgetsUpdate(boolean forced) {
        requestWidgetsUpdate(forced, false);
    }

    /**
     * Requests an update of all the widgets we currently have. It can optionally
     * also be silent (no UI).
     *
     * @param forced True if this is a forced update request, false otherwise
     * @param silent True if this is a silent forced update request, false otherwise
     */
    private void requestWidgetsUpdate(boolean forced, boolean silent) {
        Intent i = WidgetHelper.getUpdaterIntent(this, forced, silent);

        startService(i);
    }

    /**
     * Sets up the OnClick listener for the feedback preference.
     * Inspired by http://blog.tomtasche.at/2012/10/use-built-in-feedback-mechanism-on.html
     *
     * @param preference The feedback preference
     */
    public void setupFeedbackOnClickListener(Preference preference) {
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                FLog.i(SettingsActivity.this, TAG, "Sending feedback");

                if (isInstalledFromPlayStore() && canSendPlayStoreFeedback()) {
                    Intent intent = new Intent(Intent.ACTION_APP_ERROR);

                    // Use the native ApplicationErrorReport
                    ApplicationErrorReport report = new ApplicationErrorReport();
                    report.processName = getApplication().getPackageName();
                    report.packageName = report.processName;
                    report.time = System.currentTimeMillis();
                    report.type = ApplicationErrorReport.TYPE_NONE;
                    report.systemApp = false;

                    intent.putExtra(Intent.EXTRA_BUG_REPORT, report);

                    FLog.i(TAG, "Starting feedback intent");
                    try {
                        startActivity(intent);
                    }
                    catch (Exception e) {
                        FLog.w(SettingsActivity.this, "Unable to dispatch feedback Intent, falling back to email", e);
                        sendFeedbackEmail();
                    }
                }
                else {
                    // Use the fallback "share" mechanism
                    sendFeedbackEmail();
                }
                return true;
            }
        });
    }

    /**
     * Sends a feedback email (fallback mechanism when the Android feedback mechanism
     * doesn't work or isn't available).
     */
    private void sendFeedbackEmail() {
        // TODO: attach logcat
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[] {"frakbot+fweather@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "[FEEDBACK] " + getString(R.string.app_name));
        email.putExtra(Intent.EXTRA_TEXT, generateFeedbackBody());
        email.setType("message/rfc822");

        try {
            FLog.i(TAG, "Sending feedback email");
            startActivity(Intent.createChooser(email, getString(R.string.feedback_send_chooser_title)));
        }
        catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_feedback_mail_error),
                           Toast.LENGTH_LONG)
                 .show();
            FLog.e(TAG, "Unable to send the feedback email", e);
        }
    }

    /**
     * Builds a feedback email body with some basic system info.
     *
     * @return Returns the generated system info.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
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
     * Determines if there is at least one component in the system that is able
     * to actually send feedbacks (usually it's the Play Store, but we've seen
     * this fail in at least one case).
     *
     * @return Returns true if the feedback Intent can be handled, false otherwise.
     */
    private boolean canSendPlayStoreFeedback() {
        Intent intent = new Intent(Intent.ACTION_APP_ERROR);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);

        return list != null && list.size() > 0;
    }

    /**
     * Handles the preference change by requesting the TrackerHelper to send an event.
     * @param preference    The changed preference
     * @param newValue      The new value
     */
    private void handlePreferenceChange(Preference preference, Object newValue) {
        Long value = (long) 0;
        if (preference.getKey().equals(Const.Preferences.ANALYTICS)) {
            if (newValue == Boolean.FALSE)
                value = (long) 0;
            else
                value = (long) 1;
        } else if (preference.getKey().equals(Const.Preferences.SYNC_FREQUENCY)) {
            value = Long.valueOf((String)newValue);
        }
        TrackerHelper.preferenceChange(this, preference.getKey(), value);
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
                        // Handle the generic preference change
                        handlePreferenceChange(preference, value);
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
     * Sets up the Changelog preference click listener.
     *
     * @param preference The Changelog preference.
     */
    public void setupChangelogOnClickListener(Preference preference) {
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder b = new AlertDialog.Builder(SettingsActivity.this/*,
                                                                R.style.Theme_FWeather_Settings_Dialog*/);
                b.setTitle(R.string.pref_title_changelog)
                 .setView(getLayoutInflater().inflate(R.layout.dialog_changelog, null))
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 });

                b.show();

                return true;
            }
        });
    }

    /**
     * Sets up the Analytics preference listener.
     *
     * @param preference The Analytics preference.
     */
    public void setupAnalyticsOnChangeListener(SwitchPreference preference) {
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object newValue) {
                // Handle the generic preference change
                handlePreferenceChange(preference, newValue);

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

                    FLog.i(SettingsActivity.this, TAG, "Disabled Google Analytics");

                    return true;
                }
                else {
                    Toast.makeText(SettingsActivity.this, R.string.analytics_enabled_thanks, Toast.LENGTH_SHORT)
                         .show();

                    FLog.i(SettingsActivity.this, TAG, "Enabled Google Analytics");
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
    public void bindPreferenceSummaryToValue(Preference preference) {
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
     * Broadcasts an action that causes the update of the updater alarm.
     * If the preference changes, the update rate of the application data
     * will change too, and a new update will be requested.
     */
    private void sendSyncPreferenceChangedBroadcast() {
        Intent i = new Intent(Const.Intents.SYNC_RATE_PREFERENCE_CHANGED_ACTION);
        sendBroadcast(i);
    }

}
