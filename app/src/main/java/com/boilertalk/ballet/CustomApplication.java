package com.boilertalk.ballet;

import android.app.Application;

import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.VariableHolder;

import io.realm.Realm;

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Application startup code
        Realm.init(this);
    }
}
