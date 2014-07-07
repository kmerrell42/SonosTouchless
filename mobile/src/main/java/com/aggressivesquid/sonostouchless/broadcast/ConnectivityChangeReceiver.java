package com.aggressivesquid.sonostouchless.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.aggressivesquid.sonostouchless.di.Container;
import com.aggressivesquid.sonostouchless.model.user.UserDataDao;
import com.aggressivesquid.sonostouchless.service.SonosControlService;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    public static final String TAG = ConnectivityChangeReceiver.class.getSimpleName();
    private UserDataDao userDataDao = Container.getInstance().getUserDataDao();

    public ConnectivityChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isConnected() && userDataDao.hasUserLeftFence()) {
            Log.d(TAG, "Wifi connected and we have left the fence at some point");
            Intent serviceIntent = new Intent(context, SonosControlService.class);
            context.startService(serviceIntent);

            // Reset the flag since we do not want to start the service until the user has left,
            // then re-entered the fence. This prevents connectivity blips from triggering unwanted autoplay
            userDataDao.setUserLeftFence(false);
        }
    }
}
