package com.boilertalk.ballet;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.boilertalk.ballet.database.ERC20TrackedToken;
import com.boilertalk.ballet.database.RPCUrl;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.VariableHolder;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
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

        // Fabric setup
        Fabric.with(this, new Crashlytics());

        //Application startup code
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(6) // Must be bumped when the schema changes
                .migration(new GeneralDatabaseMigrations()) // Migration to run instead of throwing an exception
                .build();
        Realm.setDefaultConfiguration(config);

    }

    private class GeneralDatabaseMigrations implements RealmMigration {

        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            if (oldVersion <= 0) {
                // Wallet UUID field
                realm.getSchema().get(Wallet.class.getSimpleName())
                        .addField("s_uuid", String.class, (FieldAttribute) null);
            }
            if (oldVersion <= 1) {
                // Wallet address field
                realm.getSchema().get(Wallet.class.getSimpleName())
                        .addField("address", String.class, (FieldAttribute) null);
            }
            if (oldVersion <= 2) {
                // RPCUrl model
                realm.getSchema().create(RPCUrl.class.getSimpleName())
                        .addField("s_uuid", String.class, (FieldAttribute) null)
                        .addField("name", String.class, (FieldAttribute) null)
                        .addField("url", String.class, (FieldAttribute) null)
                        .addField("chainId", int.class, (FieldAttribute) null)
                        .addField("isActive", boolean.class, (FieldAttribute) null);
            }
            if (oldVersion <= 3) {
                // Primary keys and (implicitly) indexes
                realm.getSchema().get(Wallet.class.getSimpleName())
                        .addPrimaryKey("s_uuid");
                realm.getSchema().get(RPCUrl.class.getSimpleName())
                        .addPrimaryKey("s_uuid");
            }
            if (oldVersion <= 4) {
                // Required primary keys
                realm.getSchema().get(Wallet.class.getSimpleName())
                        .setRequired("s_uuid", true);
                realm.getSchema().get(RPCUrl.class.getSimpleName())
                        .setRequired("s_uuid", true);
            }
            if (oldVersion <= 5) {
                // ERC20TrackedToken model
                realm.getSchema().create(ERC20TrackedToken.class.getSimpleName())
                        .addField("s_uuid", String.class, FieldAttribute.INDEXED, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED)
                        .addField("addressString", String.class, (FieldAttribute) null)
                        .addField("name", String.class, (FieldAttribute) null)
                        .addField("symbol", String.class, (FieldAttribute) null)
                        .addField("decimals", int.class, (FieldAttribute) null)
                        .addField("rpcUrlID", String.class, (FieldAttribute) null);
            }
        }
    }
}
