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

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
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
public class LocationHelper implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        android.location.LocationListener,
        com.google.android.gms.location.LocationListener {

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
    public static Location getLastKnownSurroundings(PendingIntent pendingIntent) {
        // Checks if the LocationHelper has been initialized
        checkForInit();

        // If we are not connected
        if (!isConnected) {
            // Re-start the pending intent when the connection is established
            mPendingIntent = pendingIntent;
        }
        // If the last known location is null
        else if (lastKnownSurroundings == null && pendingIntent != null) {
            if (hasPlayServices) {
                // If we have play services, request a single update to mLocationClient
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setNumUpdates(1);
                // The pendingIntent will be started when the location is retrieved for the first time
                mLocationClient.requestLocationUpdates(locationRequest, pendingIntent);
            } else {
                // Otherwise, request a single update to mLocationManager
                final Criteria criteria = LocationHelper.getDefaultCriteria();
                mLocationManager.requestSingleUpdate(criteria, pendingIntent);
            }
        }

        // Return the last known surroundings
        return lastKnownSurroundings;
    }

    /**
     * Bootstraps the appropriate location modules
     */
    private void bootstrapLocationHelper() {
        if (hasPlayServices) {
            mLocationClient = new LocationClient(mContext, this, this);
            mLocationClient.connect();
        } else {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            final Criteria criteria = getDefaultCriteria();
            final String provider = mLocationManager.getBestProvider(criteria, true);

            if (TextUtils.isEmpty(provider)) {
                Log.w(TAG, "No available provider, unable to bootstrap location");
                return;
            }

            mLocationManager.requestLocationUpdates(provider, 0, 0, this, Looper.myLooper());
        }

        // At this point, either mLocationClient or mLocationManager are doing their initialization stuff
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

    /**
     * Callback method called every time there is a location update.
     * @param location the new Location
     */
    @Override
    public void onLocationChanged(Location location) {
        // The LocationManager does not have a connection callback,
        // so we have to rely on listening to location changes
        if (!hasPlayServices && mPendingIntent != null) {
            // The LocationManager has connected
            Log.d(TAG, "LocationManager has connected.");
            onGenericConnected();
        }

        // Update the location
        Log.d(TAG, "Location has been updated!");
        lastKnownSurroundings = location;
    }

    /**
     * Should handle connection, unused for now.
     */
    private void onGenericConnected() {
        isConnected = true;
        // If there is a pending intent
        if (mPendingIntent != null) {
            // Start it
            try {
                mContext.startIntentSender(mPendingIntent.getIntentSender(), null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Could not launch Intent.", e);
            }
        }
    }

    /**
     * Should handle disconnection, unused for now.
     */
    private void onGenericDisconnected() {
        isConnected = false;
    }

    // NOBODY-CARES-kinda-stuff

    @Override
    public void onConnected(Bundle bundle) {
        // The LocationClient has connected
        Log.d(TAG, "LocationClient has connected.");

        LocationRequest request = LocationRequest.create();
        mLocationClient.requestLocationUpdates(request, this);

        onGenericConnected();
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

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Nobody cares
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Nobody cares
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Nobody cares
    }
}
