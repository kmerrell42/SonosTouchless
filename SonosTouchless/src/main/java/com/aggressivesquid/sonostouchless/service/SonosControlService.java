package com.aggressivesquid.sonostouchless.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

public class SonosControlService extends IntentService {
    private static final String TAG = SonosControlService.class.getSimpleName();

    private static final ServiceId AVTRANSPORT_SERVICE_ID = new UDAServiceId("AVTransport");

    private AndroidUpnpService upnpService;
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Refresh the list with all known devices
            for (Device device : upnpService.getRegistry().getDevices()) {
                if (deviceIsSonos(device)) {
                    registryListener.deviceAdded(device);
                }
            }

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Search asynchronously for all devices
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    private boolean deviceIsSonos(Device device) {
        return device.getDetails().getManufacturerDetails().getManufacturer().contains("Sonos");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SonosControlService(String name) {
        super(name);
    }

    public SonosControlService() {
        super("SonosControlService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

    }

    class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        public void deviceAdded(Device device) {
            if (deviceIsSonos(device)) {
                sonosDeviceAdded(device);
            }
        }

        private void sonosDeviceAdded(final Device device) {
            final org.teleal.cling.model.meta.Service service = device.findService(AVTRANSPORT_SERVICE_ID);
            if (service != null ) {

                // TODO: Pick the devices from a setting menu
                boolean isBedroom = service.getDevice().getDetails().getFriendlyName().contains("Bedroom");
                boolean actionsAreAvailable = service.getActions().length > 0;
                boolean stateVarsAreAvailable = service.getStateVariables().length > 0;

                if (!isBedroom && actionsAreAvailable && stateVarsAreAvailable) {
                    play(upnpService, service);
                }
            }

        }
    }

    private void play(AndroidUpnpService upnpService, org.teleal.cling.model.meta.Service service) {
        Action action = service.getAction("Play");

        ActionInvocation actionInvocation = new ActionInvocation(action);
        actionInvocation.setInput("Speed", "1");

        // Executes asynchronous in the background
        upnpService.getControlPoint().execute(new ActionCallback(actionInvocation) {

            @Override
            public void success(ActionInvocation invocation) {
                assert invocation.getOutput().length == 0;
                Log.i(TAG, "Successfully called action!");
                stopSelf();
            }

            @Override
            public void failure(ActionInvocation invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                System.err.println(defaultMsg);
                stopSelf();
            }
        }
        );
    }

}
