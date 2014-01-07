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

package net.frakbot.util.feedback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import net.frakbot.FWeather.R;
import net.frakbot.global.Const;
import net.frakbot.util.log.FLog;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A Runnable that prepares and sends a feedback email.
 */
/*package*/ class EmailSender implements Runnable {

    private static final String TAG = EmailSender.class.getSimpleName();
    private Context mContext;

    public EmailSender(Context context) {
        mContext = context;
    }

    @Override
    public void run() {
        final File cacheDir = mContext.getExternalCacheDir();

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[] {"frakbot+fweather@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "[FEEDBACK] " + mContext.getString(R.string.app_name));
        email.setType("message/rfc822");

        final File logFile = collectLogcat(cacheDir);
        if (logFile != null && logFile.exists()) {
            email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
            email.putExtra(Intent.EXTRA_TEXT, generateFeedbackBody(mContext));
        }
        else {
            email.putExtra(Intent.EXTRA_TEXT, "\n\n** Couldn't attach logcat **\n\n" + generateFeedbackBody(
                mContext));
        }

        try {
            FLog.i(TAG, "Sending feedback email");

            mContext.startActivity(
                Intent.createChooser(email, mContext.getString(R.string.feedback_send_chooser_title)));
        }
        catch (Exception e) {
            Toast.makeText(mContext, mContext.getString(R.string.toast_feedback_mail_error), Toast.LENGTH_LONG)
                 .show();
            FLog.e(TAG, "Unable to send the feedback email", e);
        }

        FLog.d(TAG, "Email sender thread finished");
    }


    /**
     * Builds a feedback email body with some basic system info.
     *
     * @param c The context used to retrieve the system information.
     *
     * @return Returns the generated system info.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private static String generateFeedbackBody(Context c) {
        StringBuilder sb = new StringBuilder("\n\n\n" +
                                             "(only write above this line)\n\n" +
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
        sb.append("App version: ").append(getAppVersionNumber(c));

        return sb.toString();
    }

    /**
     * Retrieves the app version number.
     *
     * @param c The Context to retrieve the number for
     *
     * @return Returns the app version, or "N/A" if it can't be read
     */
    private static String getAppVersionNumber(Context c) {
        String version = "N/A";
        try {
            final PackageManager packageManager = c.getPackageManager();
            if (packageManager == null) {
                return version;
            }

            PackageInfo packageInfo = packageManager.getPackageInfo(c.getPackageName(), 0);
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
    private static String getKernelVersion() {
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
     * @param externalCacheDir The cache directory.
     *
     * @return Returns the File where the logcat has been dumped to,
     * or null if there was any problem.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File collectLogcat(File externalCacheDir) {
        FLog.i(TAG, "Collecting logcat");
        File shareFile = null;

        // Do the housekeeping
        cleanupLogcatCache(externalCacheDir);

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
     *
     * @param cacheDir The cache directory.
     */
    private static void cleanupLogcatCache(File cacheDir) {
        FLog.d(TAG, "Cleaning up log files from cache dir");
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