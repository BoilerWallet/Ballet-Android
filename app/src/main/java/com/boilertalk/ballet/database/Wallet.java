package com.boilertalk.ballet.database;

import com.boilertalk.ballet.toolbox.EtherBlockies;

import org.web3j.crypto.Keys;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Wallet extends RealmObject {

    @PrimaryKey @Index private String s_uuid;

    private String WalletFileName;
    private String WalletName;
    private String address;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String checksumAddress() {
        return Keys.toChecksumAddress(address);
    }

    public EtherBlockies etherBlockies(int size, int scale) {
        return new EtherBlockies(address.toCharArray(), size, scale);
    }
}
