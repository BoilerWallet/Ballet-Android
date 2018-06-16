package com.boilertalk.ballet;

import android.app.Application;

import io.realm.Realm;

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Application startup code
        Realm.init(this);
    }
}
