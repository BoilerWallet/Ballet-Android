package com.boilertalk.ballet.database;

import android.graphics.Color;
import android.support.annotation.Nullable;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class RPCUrl extends RealmObject {

    @PrimaryKey @Index private String s_uuid;

    private String name;
    private String url;
    private int chainId;
    private boolean isActive = false;

    public RPCUrl() {
        s_uuid = UUID.randomUUID().toString();
    }

    // Getter and Setter

    public UUID getUuid() {
        if((s_uuid == null) || s_uuid.equals("")) {
            getRealm().beginTransaction();
            s_uuid = UUID.randomUUID().toString();
            getRealm().commitTransaction();
        }
        return UUID.fromString(s_uuid);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Convenient getters

    /**
     * Returns true iff this network represents the Ethereum Mainnet.
     *
     * @return True iff this network is an Ethereum Mainnet, false otherwise.
     */
    public boolean isMainnet() {
        return chainId == 1;
    }

    /**
     * Returns true iff this network represents an official Ethereum Testnet.
     *
     * @return True iff this network is an official Ethereum Testnet, false otherwise.
     */
    public boolean isTestnet() {
        return chainId == 2 || chainId == 3 || chainId == 4 || chainId == 42;
    }

    /**
     * Returns the base Etherscan url for this network if available.
     *
     * @return The base Etherscan url, or null.
     */
    public @Nullable String etherscanBaseUrl() {
        switch (chainId) {
            case 1:
                return "https://etherscan.io";
            case 3:
                return "https://ropsten.etherscan.io";
            case 4:
                return "https://rinkeby.etherscan.io";
            case 42:
                return "https://kovan.etherscan.io";
            default:
                return null;
        }
    }

    /**
     * Returns the Etherscan API url for this network if available.
     *
     * @return The Etherscan API url, or null.
     */
    public @Nullable String etherscanApiUrl() {
        switch (chainId) {
            case 1:
                return "https://api.etherscan.io";
            case 3:
                return "https://api-ropsten.etherscan.io";
            case 4:
                return "https://api-rinkeby.etherscan.io";
            case 42:
                return "https://api-kovan.etherscan.io";
            default:
                return null;
        }
    }

    /**
     * Returns the default color to be used for highlighting this network.
     *
     * @return The color as an argb int to be used with <code>android.graphics.Color</code>.
     */
    public int networkColor() {
        if (isMainnet()) {
            return Color.rgb(76/255, 175/255, 80/255);
        } else if (isTestnet()) {
            return Color.rgb(255/255, 193/255, 7/255);
        } else {
            return Color.rgb(33/255, 150/255, 243/255);
        }
    }
}
