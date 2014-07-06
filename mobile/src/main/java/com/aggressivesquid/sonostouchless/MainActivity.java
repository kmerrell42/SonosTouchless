package com.aggressivesquid.sonostouchless;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.aggressivesquid.sonostouchless.widget.DeviceListItem;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

public class MainActivity extends FragmentActivity {

    private static final ServiceId avTransportServiceId = new UDAServiceId("AVTransport");
    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayAdapter<DeviceDisplay> listAdapter;

    private AndroidUpnpService upnpService;

    private BrowseRegistryListener registryListener = new BrowseRegistryListener();

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Refresh the list with all known devices
            listAdapter.clear();
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
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
    private TextView errorDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ListView list = (ListView) findViewById(R.id.list);
        errorDisplay = (TextView) findViewById(R.id.error_display);

        listAdapter = new ArrayAdapter<DeviceDisplay>(this, R.layout.device_list_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                DeviceListItem view;
                if (convertView == null) {
                    view = (DeviceListItem) getLayoutInflater().inflate(R.layout.device_list_item, null);
                } else {
                    view = (DeviceListItem) convertView;
                }

                view.setData(getItem(position));
                return view;
            }
        };
        list.setAdapter(listAdapter);

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeviceDisplay deviceDisplay = (DeviceDisplay) parent.getItemAtPosition(position);
                Device device = deviceDisplay.getDevice();

                Service service = device.findService(avTransportServiceId);
                if (!deviceDisplay.isPaused()) {
                    pause(upnpService, service);
                    deviceDisplay.setPaused(true);
                } else {
                    play(upnpService, service);
                    deviceDisplay.setPaused(false);
                }
            }
        });

        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    void pause(AndroidUpnpService upnpService, Service service) {
        Action action = service.getAction("Pause");
        ActionInvocation actionInvocation = new ActionInvocation(action);

        // Executes asynchronous in the background
        upnpService.getControlPoint().execute(new ActionCallback(actionInvocation) {

            @Override
            public void success(ActionInvocation invocation) {
                assert invocation.getOutput().length == 0;
                Log.i(TAG, "Successfully called action!");
            }

            @Override
            public void failure(ActionInvocation invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                System.err.println(defaultMsg);
            }
        }
        );
    }

    private void play(AndroidUpnpService upnpService, Service service) {
        Action action = service.getAction("Play");

        ActionInvocation actionInvocation = new ActionInvocation(action);
        actionInvocation.setInput("Speed", "1");

        // Executes asynchronous in the background
        upnpService.getControlPoint().execute(new ActionCallback(actionInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        assert invocation.getOutput().length == 0;
                        Log.i(TAG, "Successfully called action!");
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        System.err.println(defaultMsg);
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0 && upnpService != null) {
            upnpService.getRegistry().removeAllRemoteDevices();
            upnpService.getControlPoint().search();
        }
        return false;
    }

    class BrowseRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            runOnUiThread(new Runnable() {
                public void run() {

                    String msg = String.format("Discovery failed of '%s' : %s : Couldn't retrieve device/service descriptors", device.getDisplayString(), ex.toString());
                    errorDisplay.setText(msg);
                }
            });
            deviceRemoved(device);
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            final Service service = device.findService(avTransportServiceId);
            if (service != null ) {
                boolean actionsAreAvailable = service.getActions().length > 0;
                boolean stateVarsAreAvailable = service.getStateVariables().length > 0;

                if (actionsAreAvailable && stateVarsAreAvailable) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            DeviceDisplay d = new DeviceDisplay(device);
                            int position = listAdapter.getPosition(d);
                            if (position >= 0) {
                                // Device already in the list, re-set new value at same position
                                listAdapter.remove(d);
                                listAdapter.insert(d, position);
                            } else {
                                listAdapter.add(d);
                            }
                        }
                    });
                }
            }

        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }

    public class DeviceDisplay {
        Device device;
        private boolean paused;

        public DeviceDisplay(Device device) {
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceDisplay that = (DeviceDisplay) o;
            return device.equals(that.device);
        }

        @Override
        public int hashCode() {
            return device.hashCode();
        }

        @Override
        public String toString() {
            // Display a little star while the device is being loaded
            return device.isFullyHydrated() ? device.getDisplayString() : device.getDisplayString() + " *";
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }
    }
}
