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
import android.text.TextUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

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
     * Bootstraps the appropriate location modules
     */
    private void bootstrapLocationHelper() {
        if (hasPlayServices) {
            // Setup the listener
            mLocationClientListener = new LocationClientListener();

            mLocationClient = new LocationClient(mContext, mLocationClientListener, mLocationClientListener);
            mLocationClient.connect();
        } else {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            final Criteria criteria = getDefaultCriteria();
            final String provider = mLocationManager.getBestProvider(criteria, true);

            if (TextUtils.isEmpty(provider)) {
                FLog.w(mContext, TAG, "No available provider, unable to bootstrap location");
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
        // Checks if the LocationHelper has been initialized
        checkForInit();

        // If we are not connected
        if (!isConnected) {
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
    private static void checkForInit() {
        if (!isInit())
            throw new IllegalStateException("The LocationHelper was not initialized.");
    }

    /**
     * Returns the default criteria set for the LocationManager.
     * @return the default Criteria
     */
    private static Criteria getDefaultCriteria() {
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
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
            // The LocationClient has connected
            FLog.d(mContext, TAG, "LocationClient has connected.");

            LocationRequest request = LocationRequest.create();
            mLocationClient.requestLocationUpdates(request, this);

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
            // Nobody cares
            onGenericDisconnected();
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            // Nobody cares
            onGenericDisconnected();
        }
    }

    private class LocationManagerListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

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
        isConnected = false;
    }

}
