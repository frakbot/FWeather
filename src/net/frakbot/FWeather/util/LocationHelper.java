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

package net.frakbot.FWeather.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import net.frakbot.util.log.FLog;

/**
 * Location helper class, takes care of abstracting the location modules.
 * How to use:
 * 1.   Initialize with LocationHelper.init()
 * 2.   Get the last known location with LocationHelper.getLastKnownSurroundings(pendingIntent)
 *      The pendingIntent, if not null, will be called when the first location is retrieved
 *      iif the returned location is null.
 *
 * @author Francesco Pontillo
 */
public class LocationHelper {

    private static final String TAG = LocationHelper.class.getSimpleName();

    private static boolean isInitialized;
    private static Context mContext;
    private static boolean hasPlayServices;

    private static LocationClient mLocationClient;
    private static LocationManager mLocationManager;
    private static boolean isConnected;

    private static final LocationHelper _instance = new LocationHelper();

    private static PendingIntent mPendingIntent;
    private static Location lastKnownSurroundings; // EITS for the win
    private static LocationClientListener mLocationClientListener;
    private static LocationManagerListener mLocationManagerListener;

    private static final int PLAY_SERVICES_CONNECTION_RETRIES = 5;
    private static int mPlayServicesConnRetriesLeft = PLAY_SERVICES_CONNECTION_RETRIES;

    static {
        isInitialized = false;
        isConnected = false;
        hasPlayServices = false;
        lastKnownSurroundings = null;
    }

    /**
     * Initializes the LocationHelper with a given context.
     * This method is idempotent.
     *
     * @param context Context used for initializing stuff
     */
    public static void init(Context context) {
        if (isInitialized) return;

        isInitialized = true;
        mContext = context;
        hasPlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext)
                == ConnectionResult.SUCCESS;

        _instance.bootstrapLocationHelper();
    }

    /**
     * Reinitializes the LocationHelper. Unregisters and registers again
     * all the listeners. Useful in case the connection with the location
     * services has been lost. This method is NOT idempotent and can be called
     * only after LocationHelper has been initialized.
     */
    public static void reinit() {
        if (!isInitialized) {
            throw new IllegalStateException("Cannot reinitialize LocationHelper if it's not been initialized yet!");
        }

        // First of all, unregister all listeners and clients
        isInitialized = false;

        try {
            mLocationClient.unregisterConnectionCallbacks(mLocationClientListener);
            mLocationClient.unregisterConnectionFailedListener(mLocationClientListener);
        } catch (Throwable ignored) {}
        mLocationClientListener = null;

        try {
            mLocationClient.disconnect();
        } catch (Throwable ignored) {}
        mLocationClient = null;

        try {
            mLocationManager.removeUpdates(mLocationManagerListener);
        } catch (Throwable ignored) {}
        mLocationManagerListener = null;
        mLocationManager = null;

        // Then redo the initialization
        init(mContext);
    }

    /**
     * Bootstraps the appropriate location modules
     */
    private void bootstrapLocationHelper() {
        FLog.d(TAG, "Bootstrapping location provider. Has Play Services: " + hasPlayServices);
        if (hasPlayServices) {
            // Setup the listener
            mLocationClientListener = new LocationClientListener();

            mLocationClient = new LocationClient(mContext, mLocationClientListener, mLocationClientListener);
            mLocationClient.connect();
        } else {
            FLog.i(TAG, "Play Services aren't available on the device. Falling back to compatibility mode.");
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            final Criteria criteria = getDefaultCriteria();
            final String provider = mLocationManager.getBestProvider(criteria, true);

            if (TextUtils.isEmpty(provider)) {
                FLog.w(mContext, TAG, "No provider available, unable to bootstrap location");
                return;
            }

            // Setup the listener
            mLocationManagerListener = new LocationManagerListener();

            mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationManagerListener, Looper.myLooper());
        }

        // At this point, either mLocationClient or mLocationManager are doing their initialization stuff
    }

    /**
     * Checks if the LocationHelper was initialized.
     * @return true if initialized, false otherwise
     */
    public static boolean isInit() {
        return isInitialized;
    }

    /**
     * Returns the last known surroudings, if any.
     * When called with a pendingIntent, iif the PendingIntent is not null and the last
     * location is null too, the pending intent will be started when the first update is got.
     *
     * @param pendingIntent PendingIntent to be called on the first update
     * @return              the last known Location
     * @see "http://www.youtube.com/watch?v=2UNj5Oqs29g"
     */
    public static Location getLastKnownSurroundings(PendingIntent pendingIntent)
            throws LocationNotReadyYetException {
        // Checks if the LocationHelper has been initialized and if we are connected
        if (!checkForInit() || !isConnected) {
            // Re-start the pending intent when the connection is established
            mPendingIntent = pendingIntent;
            throw new LocationNotReadyYetException();
        }

        // Return the last known surroundings
        return lastKnownSurroundings;
    }

    /**
     * Throws an exception if the LocationHelper wasn't initialized.
     */
    private static boolean checkForInit() {
        if (!isInit())
            throw new IllegalStateException("The LocationHelper was not initialized.");

        // Check if the passive location providers are enabled (this is useful when debugging!)
        if (!isPassiveLocationEnabled()) {
            FLog.w(TAG, "Passive location provider is not active/available! We'll only be using GPS");
        }

        // Check if the Location Services are active (this might have been changed
        // since the last update went on) -- the location client has to be connected
        // whenever Play Services are available and we are updating!
        if (hasPlayServices && !mLocationClient.isConnected()) {
            FLog.w(TAG, String.format("The location client is not connected yet!.\n" +
                                      "\t> HasPlayServices: %s, connected: %s",
                                      hasPlayServices, mLocationClient.isConnected()));
            return false;
        }

        return true;
    }

    private static boolean isPassiveLocationEnabled() {
        String availProviders =
            Settings.Secure.getString(mContext.getContentResolver(),
                                      Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        return availProviders.contains("network");
    }

    /**
     * Returns the default criteria set for the LocationManager.
     * @return the default Criteria
     */
    private static Criteria getDefaultCriteria() {
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_LOW);
        criteria.setVerticalAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setSpeedRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        return criteria;
    }

    public static class LocationNotReadyYetException extends Exception {

    }

    private class LocationClientListener implements
            com.google.android.gms.location.LocationListener,
            GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener {

        @Override
        public void onConnected(Bundle bundle) {
            if (!mLocationClient.isConnected()) {
                // Strange shit happens here sometimes...
                FLog.w(mContext, TAG,
                       "LocationClient's onConnected was called, but the LocationClient is not connected. WTF!");
                onDisconnected();
            }

            // The LocationClient has connected
            FLog.d(mContext, TAG, "LocationClient has connected.");

            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
            mLocationClient.requestLocationUpdates(request, this);

            mPlayServicesConnRetriesLeft = PLAY_SERVICES_CONNECTION_RETRIES;
            onGenericConnected();

            Location currentLocation = mLocationClient.getLastLocation();
            if (currentLocation != null)
                updateLocation(currentLocation);
        }

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onDisconnected() {
            onGenericDisconnected();

            FLog.i(mContext, TAG, "Trying to reconnect the LocationClient...");
            mLocationClient.connect();
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            onGenericDisconnected();

            mPlayServicesConnRetriesLeft--; // Decrement the connection retries left

            // TODO: seriously handle errors here (service unavailable, Play Services update
            // needed, etc)
            if (mPlayServicesConnRetriesLeft > 0) {
                FLog.i(mContext, TAG, "Trying to reconnect the LocationClient after this error: " + connectionResult +
                                      "\n\t> Retries left: " + mPlayServicesConnRetriesLeft);
                mLocationClient.connect();
            }
            else {
                FLog.e(TAG, "Unable to connect to the LocationClient after " + PLAY_SERVICES_CONNECTION_RETRIES +
                            " retries. Switching to compatibility mode.");
                hasPlayServices = false;
                mLocationClient.unregisterConnectionCallbacks(this);
                mLocationClient.unregisterConnectionFailedListener(this);
                mLocationClient = null;
                mLocationClientListener = null;
                bootstrapLocationHelper();
            }
        }
    }

    private class LocationManagerListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            final String statusString;
            if (status == 0) {
                statusString = "UNAVAILABLE";
            }
            else if (status == 1) {
                statusString = "TEMPORARILY UNAVAILABLE";
            }
            else {
                statusString = "AVAILABLE";
            }

            FLog.i(TAG, "LocationManager status change for provider " + provider + ": new status is " + statusString);
        }

        @Override
        public void onProviderEnabled(String provider) {
            FLog.d(TAG, "LocationManager has new provider enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            FLog.d(TAG, "LocationManager has no longer this provider (disabled): " + provider);
        }
    }

    /**
     * Callback method called every time there is a location update.
     * @param location the new Location
     */
    private void updateLocation(Location location) {
        // Update the location
        FLog.d(mContext, TAG, "Location has been updated!");
        lastKnownSurroundings = location;

        // The LocationManager does not have a connection callback,
        // so we have to rely on listening to location changes
        if (!hasPlayServices && !isConnected) {
            // The LocationManager has connected
            FLog.d(mContext, TAG, "LocationManager has connected.");
            onGenericConnected();
        }
        tryUpdateWidgets();
    }

    /**
     * Should handle connection, unused for now.
     */
    private void onGenericConnected() {
        isConnected = true;
    }

    /**
     * Tries to update the widgets by calling the updater IntentService
     */
    private void tryUpdateWidgets() {
        // If there is a pending intent
        if (mPendingIntent != null) {
            // Start it
            try {
                mContext.startIntentSender(mPendingIntent.getIntentSender(), null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                FLog.e(mContext, TAG, "Could not launch Intent.", e);
            }
        }
    }

    /**
     * Should handle disconnection, unused for now.
     */
    private void onGenericDisconnected() {
        FLog.i(TAG, "Current location provider has disconnected. HasPlayServices: " + hasPlayServices);
        isConnected = false;
    }

}
