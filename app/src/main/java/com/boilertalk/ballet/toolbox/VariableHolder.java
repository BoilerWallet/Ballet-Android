package com.boilertalk.ballet.toolbox;


import android.content.Context;
import android.support.annotation.NonNull;

import com.boilertalk.ballet.database.RPCUrl;
import com.boilertalk.ballet.database.Wallet;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class VariableHolder {

    private static final VariableHolder instance = new VariableHolder();

    public static VariableHolder getInstance() {
        return instance;
    }

    private VariableHolder() {}

    // Properties

    private String password;
    private HashMap<UUID, LoadedWallet> loadedWallets = new HashMap<>();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getters

    public void getLoadedWallet(@NonNull String keystorePath, @NonNull Wallet wallet, @NonNull iResult<LoadedWallet> completion) {
        LoadedWallet loadedWallet = loadedWallets.get(wallet.getUuid());
        if (loadedWallet != null) {
            completion.onResult(loadedWallet);
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
        task.setPostExecuteCompletion((decrypted) -> {
            // Cache decrypted wallet
            loadedWallets.put(decrypted.wallet.getUuid(), decrypted);

            completion.onResult(decrypted);
        });

        task.execute(wallet);
    }

    public void getLoadedWallets(@NonNull Context context, @NonNull List<Wallet> wallets, @NonNull iResult<List<LoadedWallet>> completion) {
        List<LoadedWallet> loaded = new ArrayList<>();

        List<Wallet> toBeDecrypted = new ArrayList<>();

        for (int i = 0; i < wallets.size(); i++) {
            Wallet wallet = wallets.get(i);
            LoadedWallet loadedWallet = loadedWallets.get(wallet.getUuid());
            if (loadedWallet != null) {
                loaded.add(loadedWallet);
                continue;
            }

            toBeDecrypted.add(wallet);
        }

        class DecryptedWallet {

            Credentials credentials;
            UUID walletUuid;

            DecryptedWallet(Credentials credentials, UUID walletUuid) {
                this.credentials = credentials;
                this.walletUuid = walletUuid;
            }
        }

        // We have to decrypt the missing wallets
        GeneralAsyncTask<String, List<DecryptedWallet>> task = new GeneralAsyncTask<>();
        task.setBackgroundCompletion((params) -> {
            List<Wallet> walletParams = new ArrayList<>();
            Realm realm = Realm.getDefaultInstance();
            for (String uuid : params) {
                walletParams.add(realm.where(Wallet.class).equalTo("s_uuid", uuid).findFirst());
            }

            List<DecryptedWallet> decrypted = new ArrayList<>();

            for (Wallet wallet : walletParams) {
                String source = wallet.walletPath(context);
                try {
                    Credentials credentials = WalletUtils.loadCredentials(
                            VariableHolder.getInstance().getPassword(),
                            source
                    );
                    DecryptedWallet l = new DecryptedWallet(credentials, wallet.getUuid());

                    decrypted.add(l);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CipherException e) {
                    e.printStackTrace();
                }
            }

            return decrypted;
        });
        task.setPostExecuteCompletion((decrypted) -> {
            // Create LoadedWallets from DecryptedWallets
            List<LoadedWallet> innerLoadedWallets = new ArrayList<>();
            Realm realm = Realm.getDefaultInstance();
            for (DecryptedWallet decryptedWallet : decrypted) {
                Wallet wallet = realm.where(Wallet.class).equalTo("s_uuid", decryptedWallet.walletUuid.toString()).findFirst();

                innerLoadedWallets.add(new LoadedWallet(decryptedWallet.credentials, wallet));
            }

            // Cache decrypted wallets in main thread
            for (LoadedWallet l : innerLoadedWallets) {
                loadedWallets.put(l.wallet.getUuid(), l);
            }

            loaded.addAll(innerLoadedWallets);

            completion.onResult(loaded);
        });

        // Thread fix for realm...
        // Pass uuids to background thread
        String[] tmpDecryptables = new String[toBeDecrypted.size()];
        for (int i = 0; i < toBeDecrypted.size(); i++) {
            tmpDecryptables[i] = toBeDecrypted.get(i).getUuid().toString();
        }
        task.execute(tmpDecryptables);
    }

    // Reset stuff

    public void resetLoadedWallets() {
        loadedWallets = new HashMap<>();
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

    // RPC stuff

    /**
     * Creates and returns the default mainnet RPCUrl.
     * <p>
     * The given instance of <code>Realm</code> should have an active started transaction.
     *
     * @param realm The instance of realm to be used for instance creation.
     * @return The default mainnet RPCUrl.
     */
    private RPCUrl defaultMainnetRPCUrl(Realm realm) {
        RPCUrl mainnet = realm.createObject(RPCUrl.class, UUID.randomUUID().toString());
        mainnet.setName("Infura Mainnet");
        mainnet.setUrl("https://mainnet.infura.io/v3/9bde93f620304aa4a340c65799ce6796");
        mainnet.setChainId(1);
        mainnet.setActive(true);

        return mainnet;
    }

    /**
     * Creates and returns a list of default RPCUrls for this app.
     * <p>
     * The given instance of <code>Realm</code> should have an active started transaction.
     *
     * @param realm The instance of realm to be used for instance creation.
     * @return The list of default RPCUrls.
     */
    private List<RPCUrl> defaultRPCUrls(Realm realm) {
        RPCUrl mainnet = defaultMainnetRPCUrl(realm);

        RPCUrl ropsten = realm.createObject(RPCUrl.class, UUID.randomUUID().toString());
        ropsten.setName("Infura Ropsten");
        ropsten.setUrl("https://ropsten.infura.io/v3/9bde93f620304aa4a340c65799ce6796");
        ropsten.setChainId(3);
        ropsten.setActive(false);

        RPCUrl rinkeby = realm.createObject(RPCUrl.class, UUID.randomUUID().toString());
        rinkeby.setName("Infura Rinkeby");
        rinkeby.setUrl("https://rinkeby.infura.io/v3/9bde93f620304aa4a340c65799ce6796");
        rinkeby.setChainId(4);
        rinkeby.setActive(false);

        RPCUrl kovan = realm.createObject(RPCUrl.class, UUID.randomUUID().toString());
        kovan.setName("Infura Kovan");
        kovan.setUrl("https://kovan.infura.io/v3/9bde93f620304aa4a340c65799ce6796");
        kovan.setChainId(42);
        kovan.setActive(false);

        List<RPCUrl> urls = new ArrayList<RPCUrl>();

        // Add default rpc urls
        urls.add(mainnet);
        urls.add(ropsten);
        urls.add(rinkeby);
        urls.add(kovan);

        return urls;
    }

    /**
     * Returns the current active RPCUrl, adding defaults if not added yet.
     *
     * @return The active RPCUrl.
     */
    public RPCUrl activeUrl() {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<RPCUrl> urls = realm.where(RPCUrl.class).findAll();
        if (urls.size() == 0) {
            // Add default urls

            // Begin realm transaction
            realm.beginTransaction();

            // Create default RPCUrls
            List<RPCUrl> newUrls = defaultRPCUrls(realm);

            // Done with realm
            realm.commitTransaction();
        }

        return realm.where(RPCUrl.class).equalTo("isActive", true).findFirst();
    }

    /**
     * Returns the currently active (selected) instance of Web3j to be used for all calls within
     * the app.
     *
     * @return The instance of Web3j to be used in this app.
     */
    public Web3j activeWeb3j() {
        return Web3jFactory.build(new HttpService(activeUrl().getUrl()));
    }
}
