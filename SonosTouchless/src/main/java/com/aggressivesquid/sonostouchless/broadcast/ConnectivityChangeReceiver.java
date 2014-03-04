package com.aggressivesquid.sonostouchless.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.aggressivesquid.sonostouchless.service.SonosControlService;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    public ConnectivityChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isConnected()) {
            // TODO: Only do this if the user has left the geofence and has returned
            Intent serviceIntent = new Intent(context, SonosControlService.class);
            context.startService(serviceIntent);
        }
    }
}
