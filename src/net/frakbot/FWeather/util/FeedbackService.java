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

/////////////////////////// PARTLY BASED ON CODE FROM THOSE PROJECTS /////////////////////////////
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2009 Xtralogic, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frakbot.FWeather.util;

import android.annotation.TargetApi;
import android.app.ApplicationErrorReport;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.global.Const;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An utility class that allows users to send feedbacks,
 * either through the Play Store or via email.
 */
public class FeedbackService extends IntentService {

    private static final String TAG = FeedbackService.class.getSimpleName();

    public FeedbackService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FLog.d(TAG, "Handling 'send feedback' intent");
        ProgressDialog progressDialog = new ProgressDialog(this);

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.preparing_feedback));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (isInstalledFromPlayStore() && canSendPlayStoreFeedback()) {
            sendNativeFeedback(progressDialog);
        }
        else {
            // Use the fallback "share" mechanism
            sendFeedbackEmail(progressDialog);
        }
    }

    /**
     * Sends a feedback using the Android/Play Store built-in feedback mechanism.
     * Falls back on email if anything goes wrong (on our side)
     *
     * @param progressDialog The progress dialog (to dismiss once done)
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void sendNativeFeedback(ProgressDialog progressDialog) {
        Intent i = new Intent(Intent.ACTION_APP_ERROR);

        // Use the native ApplicationErrorReport
        ApplicationErrorReport report = new ApplicationErrorReport();
        report.processName = getApplication().getPackageName();
        report.packageName = report.processName;
        report.time = System.currentTimeMillis();
        report.type = ApplicationErrorReport.TYPE_RUNNING_SERVICE;
        report.systemApp = false;

        report.runningServiceInfo = new ApplicationErrorReport.RunningServiceInfo();
        report.runningServiceInfo.serviceDetails = "USER FEEDBACK";
        report.runningServiceInfo.durationMillis = 42;   // LOL?

        i.putExtra(Intent.EXTRA_BUG_REPORT, report);

        FLog.i(TAG, "Starting feedback intent");
        try {
            progressDialog.dismiss();
            startActivity(i);
        }
        catch (Exception e) {
            FLog.w(this, "Unable to dispatch feedback Intent, falling back to email", e);
            sendFeedbackEmail(progressDialog);
        }
    }

    /**
     * Sends a feedback email (fallback for when the Android feedback mechanism
     * doesn't work or isn't available).
     *
     * @param progressDialog The progress dialog (to dismiss once done)
     */
    private void sendFeedbackEmail(ProgressDialog progressDialog) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"frakbot+fweather@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "[FEEDBACK] " + getString(R.string.app_name));
        email.setType("message/rfc822");

        final File logFile = collectLogcat();
        if (logFile != null && logFile.exists()) {
            email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
            email.putExtra(Intent.EXTRA_TEXT, generateFeedbackBody());
        }
        else {
            email.putExtra(Intent.EXTRA_TEXT, "\n\nCouldn't attach logcat\n\n" + generateFeedbackBody());
        }

        try {
            FLog.i(TAG, "Sending feedback email");

            progressDialog.dismiss();
            startActivity(Intent.createChooser(email, getString(
                R.string.feedback_send_chooser_title)));
        }
        catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, getString(R.string.toast_feedback_mail_error), Toast.LENGTH_LONG)
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
        sb.append("Kernel: ").append(getKernelVersion());

        // App info
        sb.append("App version: ").append(getAppVersionNumber(this));

        return sb.toString();
    }

    /**
     * Determines if the app is installed from the Google Play Store.
     *
     * @return Returns true if the app is installed from the Google
     * Play Store, or false if it has been installed from other
     * sources (sideload, other app stores, etc)
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Built-in reporting only works from Android 4.0 onwards
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_APP_ERROR);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);

        return list != null && list.size() > 0;
    }

    /**
     * Retrieves the app version number.
     *
     * @param context The Context to retrieve the number for
     *
     * @return Returns the app version, or "N/A" if it can't be read
     */
    private static String getAppVersionNumber(Context context) {
        String version = "N/A";
        try {
            PackageInfo packageInfo = context.getPackageManager()
                                             .getPackageInfo(context.getPackageName(), 0);
            version = packageInfo.versionName;
        }
        catch (Exception e) {
            FLog.e(TAG, "Unable to read the app version!", e);
        }

        return version;
    }

    /**
     * Reads the kernel version to a string.
     *
     * @return Returns the kernel version, or "N/A" if it can't be read
     */
    private String getKernelVersion() {
        String procVersionStr;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
            try {
                procVersionStr = reader.readLine();
            }
            finally {
                reader.close();
            }

            if (procVersionStr == null) {
                FLog.e(TAG, "Unable to read the kernel version!");
                return "N/A";
            }

            return procVersionStr;
        }
        catch (IOException e) {
            FLog.e(TAG, "Getting kernel version failed", e);
            return "N/A";
        }
    }

    /**
     * Collects a dump of the system log (logcat) to a file.
     *
     * @return Returns the File where the logcat has been dumped to,
     * or null if there was any problem.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File collectLogcat() {
        FLog.i(TAG, "Collecting logcat");
        File shareFile = null;

        // Do the housekeeping
        cleanupLogcatCache();

        try {
            // Create the temp logcat file
            final DateFormat dateFormat = new SimpleDateFormat("YYYYMMDD_HHmmss", Locale.US);
            shareFile = new File(getCacheDir(), Const.APP_NAME + dateFormat.format(new Date()) + ".log");
            shareFile.createNewFile();
            shareFile.setReadable(true, false);

            ArrayList<String> commandLine = new ArrayList<String>();
            commandLine.add("logcat");
            commandLine.add("-d");                                  // DUMP the whole log
            commandLine.add("-f " + shareFile.getAbsolutePath());   // to shareFile
            commandLine.add("-v threadtime");                       // in the threadtime format
            commandLine.add("*:V");                                 // for all tags, starting at VERBOSE level

            Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));

            process.waitFor();
            FLog.d(TAG, "Logcat collected to " + shareFile.getAbsolutePath());
        }
        catch (IOException e) {
            FLog.e(TAG, "Log collection failed", e);
        }
        catch (InterruptedException e) {
            FLog.w(TAG, "Log collection waiting was interrupted - log might be truncated!");
        }

        return shareFile;
    }

    /**
     * Cleans up the cache dir from any leftover cache files.
     * Do not invoke this method right after starting the log collection
     * or it might delete the log you're collecting as well!
     */
    private void cleanupLogcatCache() {
        FLog.d(TAG, "Cleaning up log files from cache dir");
        final File cacheDir = getCacheDir();
        final File[] logFiles = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".log");
            }
        });

        int count = 0;
        for (File logFile : logFiles) {
            count += logFile.delete() ? 1 : 0;
        }

        FLog.d(TAG, "Pruned " + count + " log file(s) from cache");
    }

}
