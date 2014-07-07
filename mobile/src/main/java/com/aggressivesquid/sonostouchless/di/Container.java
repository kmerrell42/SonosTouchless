package com.aggressivesquid.sonostouchless.di;

import android.app.Application;
import android.content.Context;

import com.aggressivesquid.sonostouchless.model.user.UserDataDao;

public class Container {

    private static Container instance;

    private Context application;

    private UserDataDao userDataDao;

    private Container() {}

    public static Container getInstance() {
        if (instance == null) {
            instance = new Container();
        }
        return instance;
    }

    public void initialize(Application application) {
        this.application = application;
    }

    public synchronized Application getApplication() {
        return (Application) application;
    }

    public synchronized UserDataDao getUserDataDao() {
        if (userDataDao == null) {
            userDataDao = new UserDataDao(getApplication());
        }
        return userDataDao;
    }
}
