package com.boilertalk.ballet.database;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class ERC20TrackedToken extends RealmObject {

    @PrimaryKey @Index @Required private String s_uuid;

    private String addressString;

    private String name;

    private String symbol;

    private int decimals;

    private String rpcUrlID;

    // Getter and Setter

    public UUID getUuid() {
        if((s_uuid == null) || s_uuid.equals("")) {
            getRealm().beginTransaction();
            s_uuid = UUID.randomUUID().toString();
            getRealm().commitTransaction();
        }
        return UUID.fromString(s_uuid);
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getRpcUrlID() {
        return rpcUrlID;
    }

    public void setRpcUrlID(String rpcUrlID) {
        this.rpcUrlID = rpcUrlID;
    }
}
