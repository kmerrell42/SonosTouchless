package com.aggressivesquid.sonostouchless.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.aggressivesquid.sonostouchless.di.Container;
import com.aggressivesquid.sonostouchless.model.user.UserDataDao;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

public class GeofenceTransitionService extends IntentService {

    public static final String TAG = GeofenceTransitionService.class.getSimpleName();
    private final UserDataDao userDataDao = Container.getInstance().getUserDataDao();

    @SuppressWarnings("unused")
    public GeofenceTransitionService(String name) {
        super(name);
    }

    @SuppressWarnings("unused")
    public GeofenceTransitionService() { super(GeofenceTransitionService.class.getSimpleName()); }

    @Override
    protected void onHandleIntent(Intent intent) {
        // persist a flag to be checked when we connect to wifi again that we have left home

        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                            Integer.toString(errorCode)
            );
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Has the user left the fence? We don't care if they entered.
            if (LocationClient.getGeofenceTransition(intent) == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(TAG, "User has left the fence");
                userDataDao.setUserLeftFence(true);
            }
        }
    }
}
