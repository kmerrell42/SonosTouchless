package com.aggressivesquid.sonostouchless;

import com.aggressivesquid.sonostouchless.di.Container;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Container.getInstance().initialize(this);

    }
}
