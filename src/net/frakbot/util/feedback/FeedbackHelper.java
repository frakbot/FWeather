package net.frakbot.util.feedback;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import net.frakbot.util.log.FLog;

import java.util.List;

/**
 * Sends a feedback using Android's feedback mechanism (through Google, or any other
 * vendor's mechanism that implements the Android feedback system interface).
 * "Inspired" by Google's own way of reporting feedbacks.
 */
public class FeedbackHelper {

    private static final String TAG = FeedbackHelper.class.getSimpleName();

    private static final Intent BUG_REPORT_INTENT = new Intent("android.intent.action.BUG_REPORT");
    public static final int MAX_SCREENSHOT_SIZE = 1048576;

    /**
     * Determines if the device supports sending feedback through
     * Google's own feedback mechanism.
     *
     * @param context The context used to test the device.
     *
     * @return Returns true if the device supports sending feedback through
     * Google's own feedback mechanism, false otherwise.
     */
    public static boolean canSendAndroidFeedback(Context context) {
        // TODO: re-enable Android feedback once it's confirmed it works (and how) on Google's side
        return false; // canHandleIntent(context, BUG_REPORT_INTENT);
    }

    /**
     * Checks if there is any service available on the device that can handle
     * the specified intent.
     *
     * @param c      The context used to test the device.
     * @param intent The intent to check support for.
     *
     * @return Returns true if the specified intent can be resolved to a service
     * on the device, false otherwise.
     */
    private static boolean canHandleIntent(Context c, Intent intent) {
        PackageManager pm = c.getPackageManager();
        List<ResolveInfo> services;

        if (pm != null) {
            services = pm.queryIntentServices(intent, 1);
            return !services.isEmpty();
        }

        return false;
    }

    /**
     * Determines if the app is installed from the Google Play Store.
     *
     * @return Returns true if the app is installed from the Google
     * Play Store, or false if it has been installed from other
     * sources (sideload, other app stores, etc)
     */
    private static boolean isInstalledFromPlayStore(Context c) {
        PackageManager pm = c.getPackageManager();
        if (pm == null) {
            Log.i(TAG, "Unable to retrieve the PackageManager, assuming app is sideloaded");
            return false;
        }

        String installationSource = pm.getInstallerPackageName(c.getPackageName());
        return "com.android.vending".equals(installationSource) ||
               "com.google.android.feedback".equals(installationSource);   // This is for Titanium Backup compatibility
    }

    /**
     * Sends a feedback through Google's own feedback mechanism (or any other
     * vendor's mechanism that implements the Android feedback system interface)
     * for the specified Activity.
     *
     * @param a The Activity to send a feedback for.
     */
    public static void sendFeedback(Activity a) {
        if (!canSendAndroidFeedback(a) || !isInstalledFromPlayStore(a)) {
            Log.w(TAG, "Android feedback mechanism is not available, or app is sideloaded.\n" +
                       "Using email fallback mechanism.");
            sendFeedbackEmail(a);
            return;
        }

        // Bind to the feedback service (this allows us to send the screenshot as well --
        // if we only sent the feedback intent, we couldn't have done it)
        FeedbackServiceBinder binder = new FeedbackServiceBinder(a);
        a.bindService(BUG_REPORT_INTENT, binder, Context.BIND_AUTO_CREATE);
    }

    /**
     * Sends a feedback email (fallback for when the Android feedback mechanism
     * doesn't work or isn't available).
     */
    private static void sendFeedbackEmail(Activity a) {
        FLog.d(TAG, "Starting feedback email thread");

        EmailSender emailSender = new EmailSender(a);
        Thread t = new Thread(emailSender);
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName("FeedbackEmailSender");
        t.run();
    }

    /**
     * A ServiceConnection implementation that fetches and sends the Activity
     * screenshot to the feedback service (most likely, Google's own).
     */
    static class FeedbackServiceBinder implements ServiceConnection {
        private final Activity mActivity;

        /**
         * Initializes the ServiceConnection instance.
         *
         * @param activity The Activity to send a feedback for.
         */
        public FeedbackServiceBinder(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                Parcel parcel = Parcel.obtain();
                Bitmap screenshot = getCurrentScreenshot(mActivity);
                if (screenshot != null) {
                    screenshot.writeToParcel(parcel, 0);
                }

                // Send the screenshot (if any) to the service
                service.transact(1, parcel, null, 0);
            }
            catch (RemoteException e) {
                Log.e(TAG, "Error connecting to bug report service", e);
                mActivity.unbindService(this);
            }
            catch (Throwable t) {
                mActivity.unbindService(this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Nothing to do here
        }
    }

    /**
     * Grabs a screenshot of the specified Activity.
     *
     * @param activity The Activity to grab a screenshot of.
     *
     * @return Returns the captured (and resized, if needed) screenshot
     * of the Activity, or null if it wasn't captured.
     */
    private static Bitmap getCurrentScreenshot(Activity activity) {
        try {
            View currentView = activity.getWindow().getDecorView().getRootView();
            if (currentView == null) {
                return null;
            }

            boolean drawingCacheWasEnabled = currentView.isDrawingCacheEnabled();
            currentView.setDrawingCacheEnabled(true);

            Bitmap bitmap = currentView.getDrawingCache();
            if (bitmap != null) {
                bitmap = resizeBitmap(bitmap);
            }

            if (!drawingCacheWasEnabled) {
                // Restore the initial drawing cache state
                currentView.setDrawingCacheEnabled(false);
                currentView.destroyDrawingCache();
            }

            return bitmap;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Resizes the screenshot if needed by dividing by two its dimensions
     * until the image raw size is less than or equal to {@link #MAX_SCREENSHOT_SIZE}.
     *
     * @param bitmap The bitmap to resize.
     *
     * @return Returns the bitmap, resized if needed. The returned bitmap is
     * always a copy of the original one!
     */
    private static Bitmap resizeBitmap(Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);     // 2 bytes per pixel
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Divide the bitmap dimensions by two until the resulting image size is less
        // than MAX_SCREENSHOT_SIZE, at 2 BPP (it's a RGB_565 copy of the original!)
        while (width * height * 2 > MAX_SCREENSHOT_SIZE) {
            width /= 2;
            height /= 2;
        }

        // Scale only if needed!
        if (bitmap.getWidth() != width) {
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        return bitmap;
    }
}



