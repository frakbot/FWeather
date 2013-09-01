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
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import net.frakbot.FWeather.R;
import net.frakbot.FWeather.global.Const;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An utility class that allows users to send feedbacks,
 * either through the Play Store or via email.
 */
public class FeedbackService extends IntentService {

    private static final String TAG = FeedbackService.class.getSimpleName();
    public static final int NOTIF_ID_FEEDBACK = 6543;
    public static final String CACHE_PATH = "/data/local/tmp/";

    public FeedbackService() {
        super("FWeather feedback service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FLog.d(TAG, "Handling 'send feedback' intent");
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setProgress(100, 0, true)
         .setOngoing(true)
         .setPriority(Notification.PRIORITY_LOW)
         .setContentTitle(getString(R.string.preparing_feedback))
         .setTicker(getString(R.string.preparing_feedback));

        startForeground(NOTIF_ID_FEEDBACK, b.build());

        if (isInstalledFromPlayStore() && canSendPlayStoreFeedback()) {
            sendNativeFeedback();
        }
        else {
            // Use the fallback "share" mechanism
            sendFeedbackEmail();
        }
    }

    /**
     * Sends a feedback using the Android/Play Store built-in feedback mechanism.
     * Falls back on email if anything goes wrong (on our side)
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void sendNativeFeedback() {
        Intent i = new Intent(Intent.ACTION_APP_ERROR);

        // Use the native ApplicationErrorReport
        ApplicationErrorReport report = new ApplicationErrorReport();
        report.processName = getApplication().getPackageName();
        report.packageName = report.processName;
        report.time = System.currentTimeMillis();
        report.type = ApplicationErrorReport.TYPE_ANR;  // Fake ANR so that it shows up on the Play Store Dev Console
        report.systemApp = false;

        report.anrInfo = new ApplicationErrorReport.AnrInfo();
        report.anrInfo.activity = "none";
        report.anrInfo.cause = "USER FEEDBACK";
        report.anrInfo.info = "USER FEEDBACK";

        i.putExtra(Intent.EXTRA_BUG_REPORT, report);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        FLog.i(TAG, "Starting feedback intent");
        try {
            startActivity(i);
            stopForeground(true);
        }
        catch (Exception e) {
            FLog.w(this, "Unable to dispatch feedback Intent, falling back to email", e);
            sendFeedbackEmail();
        }
    }

    /**
     * Sends a feedback email (fallback for when the Android feedback mechanism
     * doesn't work or isn't available).
     */
    private void sendFeedbackEmail() {
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

            startActivity(Intent.createChooser(email, getString(R.string.feedback_send_chooser_title))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            stopForeground(true);
        }
        catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_feedback_mail_error), Toast.LENGTH_LONG)
                 .show();
            FLog.e(TAG, "Unable to send the feedback email", e);
            stopForeground(true);
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
        sb.append("Build: ").append(Build.FINGERPRINT).append("\n");
        sb.append("Kernel: ").append(getKernelVersion()).append("\n");

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

        final File externalCacheDir = getExternalCacheDir();
        if (externalCacheDir == null) {
            FLog.w(TAG, "External storage not available. Can't attach logcat!");
            return null;
        }

        try {
            // Create the temp logcat file
            final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            shareFile = new File(externalCacheDir, Const.APP_NAME + "-" + dateFormat.format(new Date()) + ".log");
            shareFile.createNewFile();

            String commandLine = "logcat -d " +                     // DUMP the whole log
                                 "-v threadtime " +                 // in the threadtime format
                                 "*:V";                             // for all tags, starting at VERBOSE level

            Process process = Runtime.getRuntime().exec(commandLine);

            process.waitFor();
            BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

            FileWriter fw = new FileWriter(shareFile, false);
            String line;
            int linesCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                fw.write(line);
                fw.write("\n");
                linesCount++;
            }
            fw.flush();
            fw.close();

            FLog.d(TAG, "Logcat collected to " + shareFile.getAbsolutePath() + " (" + linesCount + " lines)");
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
        final File cacheDir = getExternalCacheDir();
        if (cacheDir == null) {
            FLog.w(TAG, "External storage not available. Can't cleanup cache");
            return;
        }

        final File[] logFiles = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".log");
            }
        });

        if (logFiles == null) {
            FLog.d(TAG, "No log files to prune from cache");
            return;
        }

        int count = 0;
        for (File logFile : logFiles) {
            count += logFile.delete() ? 1 : 0;
        }

        FLog.d(TAG, "Pruned " + count + " log file(s) from cache");
    }

}
