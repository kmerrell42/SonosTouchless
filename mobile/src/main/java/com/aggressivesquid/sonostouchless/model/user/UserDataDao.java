package com.aggressivesquid.sonostouchless.model.user;

import android.content.Context;
import android.content.SharedPreferences;

public class UserDataDao {
    private static final String PREF_NAME = "userData";
    private static final String EXTRA_LEFT_FENCE = "hasLeftFence";
    private final SharedPreferences prefs;

    public UserDataDao(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasUserLeftFence() {
        return prefs.getBoolean(EXTRA_LEFT_FENCE, true);
    }

    public void setUserLeftFence(boolean hasLeft) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(EXTRA_LEFT_FENCE, hasLeft);
        editor.commit();
    }
}
