package com.boilertalk.ballet.RealmClasses;

import io.realm.RealmObject;

public class Wallet extends RealmObject {
    private String WalletFileName;
    private String WalletName;

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
}
