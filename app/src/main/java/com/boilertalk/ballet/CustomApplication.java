package com.boilertalk.ballet;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.VariableHolder;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.exceptions.RealmMigrationNeededException;

public class CustomApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //Application startup code
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1) // Must be bumped when the schema changes
                .migration(new MyMigration()) // Migration to run instead of throwing an exception
                .build();
        Realm.setDefaultConfiguration(config);

    }

    private class MyMigration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            realm.getSchema().get("com.boilertalk.ballet.database.Wallet").addField("s_uuid",
                    String.class, (FieldAttribute) null);
        }
    }
}
