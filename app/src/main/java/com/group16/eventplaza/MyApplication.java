package com.group16.eventplaza;

import android.app.Application;

import com.group16.eventplaza.utils.FirebaseUtil;

public class MyApplication extends Application {
    private static MyApplication instance;
    public static MyApplication getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        FirebaseUtil.saveAllUserNameById(this);
        FirebaseUtil.saveAllUserIdByEventId(this);
    }
}
