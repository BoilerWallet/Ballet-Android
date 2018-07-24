package com.boilertalk.ballet.realmClasses;

import java.util.UUID;

import io.realm.RealmObject;

public class Wallet extends RealmObject {
    private String WalletFileName;
    private String WalletName;
    private String s_uuid;

    public Wallet() {
        s_uuid = UUID.randomUUID().toString();
    }

    public String getWalletFileName() {
        return WalletFileName;
    }

    public void setWalletFileName(String walletFileName) {
        WalletFileName = walletFileName;
    }

    public String getWalletName() {
        return WalletName;
    }

    public void setWalletName(String walletName) {
        WalletName = walletName;
    }

    public UUID getUuid() {
        if((s_uuid == null) || s_uuid.equals("")) {
            getRealm().beginTransaction();
            s_uuid = UUID.randomUUID().toString();
            getRealm().commitTransaction();
        }
        return UUID.fromString(s_uuid);
    }
}
