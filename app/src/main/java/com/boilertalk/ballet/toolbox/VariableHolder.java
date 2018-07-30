package com.boilertalk.ballet.toolbox;

import android.support.annotation.NonNull;

import com.boilertalk.ballet.database.Wallet;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class VariableHolder {

    private static final VariableHolder instance = new VariableHolder();

    public static VariableHolder getInstance() {
        return instance;
    }

    private VariableHolder() {}

    private String password;
    private Web3j web3jInstance = null;
    private HashMap<UUID, LoadedWallet> loadedWallets = new HashMap<>();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Web3j getWeb3j() {
        if(web3jInstance == null) {
            web3jInstance = Web3jFactory.build(new HttpService("https://ropsten.infura.io/m6d0dZdIbdR5d6bvHDQj"));
        }
        return web3jInstance;
    }

    public interface LoadedWalletCompletion {

        void loaded(LoadedWallet result);
    }

    public void getLoadedWallet(@NonNull String keystorePath, @NonNull Wallet wallet, @NonNull LoadedWalletCompletion completion) {
        LoadedWallet loadedWallet = loadedWallets.get(wallet.getUuid());
        if (loadedWallet != null) {
            completion.loaded(loadedWallet);
            return;
        }

        // We have to decrypt it...
        GeneralAsyncTask<Wallet, LoadedWallet> task = new GeneralAsyncTask<>();
        task.setBackgroundCompletion((params) -> {
            if (params.length < 1) {
                return null;
            }
            Wallet innerWallet = params[0];

            String source = keystorePath;
            try {
                Credentials credentials = WalletUtils.loadCredentials(
                        VariableHolder.getInstance().getPassword(),
                        source
                );
                LoadedWallet loaded = new LoadedWallet(credentials, wallet);

                return loaded;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CipherException e) {
                e.printStackTrace();
            }

            return null;
        });
        task.setPostExecuteCompletion(completion::loaded);

        task.execute(wallet);
    }

    public static class LoadedWallet {

        private Wallet wallet;

        private org.web3j.crypto.Credentials credentials;

        public LoadedWallet(org.web3j.crypto.Credentials credentials, Wallet wallet) {
            this.wallet = wallet;
            this.credentials = credentials;
        }

        public org.web3j.crypto.Credentials getCredentials() {
            return credentials;
        }

        public Wallet getWallet() {
            return wallet;
        }

        public EtherBlockies etherBlockies(int size, int scale) {
            return new EtherBlockies(credentials.getAddress().toCharArray(), size, scale);
        }

        public String checksumAddress() {
            return Keys.toChecksumAddress(credentials.getAddress());
        }
    }
}
