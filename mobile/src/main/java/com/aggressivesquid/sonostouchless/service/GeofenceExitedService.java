package com.aggressivesquid.sonostouchless.service;

import android.app.IntentService;
import android.content.Intent;

public class GeofenceExitedService extends IntentService {
    /**
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceExitedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // persist a flag to be checked when we connect to wifi again that we have left home


    }
}
