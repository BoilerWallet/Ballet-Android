package com.boilertalk.ballet.toolbox;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.util.HashMap;
import java.util.UUID;

public class VariableHolder {
    private static String password;
    private static Web3j web3jInstance = null;
    private static HashMap<UUID, LoadedWallet> loadedWallets = new HashMap<>();

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        VariableHolder.password = password;
    }

    public static Web3j getWeb3j() {
        if(web3jInstance == null) {
            web3jInstance = Web3jFactory.build(new HttpService("https://ropsten.infura.io" +
                    "/m6d0dZdIbdR5d6bvHDQj"));
        }
        return web3jInstance;
    }

    public static LoadedWallet getWalletAt(UUID uuid) {
        return loadedWallets.get(uuid);
    }

    //returns true if wallet has been added, false if it was already in the map
    public static boolean putWallet(UUID uuid, LoadedWallet lwAlda) {
        if(loadedWallets.containsKey(uuid)) {
            return false;
        } else {
            loadedWallets.put(uuid, lwAlda);
            return true;
        }
    }

    public static class LoadedWallet {
        public String name;
        private org.web3j.crypto.Credentials credentials;

        public LoadedWallet(org.web3j.crypto.Credentials credentials, String name) {
            this.name = name;
            this.credentials = credentials;
        }

        public org.web3j.crypto.Credentials getCredentials() {
            return credentials;
        }
    }

}
