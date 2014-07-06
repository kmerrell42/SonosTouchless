package com.aggressivesquid.sonostouchless.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aggressivesquid.sonostouchless.MainActivity.DeviceDisplay;
import com.aggressivesquid.sonostouchless.R;
import org.teleal.cling.model.meta.Device;

public class DeviceListItem extends LinearLayout {
    private TextView nameView;
    private TextView thing1View;

    public DeviceListItem(Context context) {
        super(context);
    }

    public DeviceListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeviceListItem(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        nameView = (TextView) findViewById(R.id.name);
        thing1View = (TextView) findViewById(R.id.thing1);
    }

    public void setData(DeviceDisplay deviceDisplay) {

        Device device = deviceDisplay.getDevice();
        nameView.setText(device.getDetails().getFriendlyName());
        thing1View.setText(device.getDetails().toString());
    }
}
