package com.boilertalk.ballet.walletslist;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.addwallet.AddWalletFragment;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.walletdetails.WalletDetailsFragment;
import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.SSLHelper;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;

public class WalletsListFragment extends Fragment {

    private static final String TAG = "WalletsListFragmet";
    private FloatingActionButton addWalletButton;
    private RecyclerView walletsListRecycler;
    private SwipeRefreshLayout refresher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallets_list, container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addWalletButton = (FloatingActionButton) getActivity().findViewById(R.id.add_wallet_button);
        walletsListRecycler = (RecyclerView) view.findViewById(R.id.wallets_list_view);
        refresher = view.findViewById(R.id.wallets_list_refresher);

        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RecyclerView.Adapter ad = walletsListRecycler.getAdapter();
                if(ad != null) {
                    ad.notifyDataSetChanged();
                }
                refresher.setRefreshing(false);
            }
        });


        addWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "create wallet clicked");
                AddWalletFragment awf = new AddWalletFragment();
                awf.show(getActivity().getSupportFragmentManager(), "AddWalletDialog");
            }
        });

        class WLRA extends RecyclerView.Adapter {
            class mvh extends RecyclerView.ViewHolder {

                public mvh(View itemView) {
                    super(itemView);
                }
            }
            Realm realm;
            final RealmResults<Wallet> wallets;

            public WLRA() {
                this.realm = Realm.getDefaultInstance();
                wallets = realm.where(Wallet.class).findAll();
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);

                View holded = inflater.inflate(R.layout.fragment_wallets_list_entry, parent,
                        false);
                holded.findViewById(R.id.wallet_address).setSelected(true);
                RecyclerView.ViewHolder holder = new mvh(holded);

                return holder;
            }

            class LoadWalletParams {

                UUID walletUuid;
                String walletSource;

                LoadWalletParams(UUID walletUuid, String walletSource) {
                    this.walletUuid = walletUuid;
                    this.walletSource = walletSource;
                }
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                final ProgressBar pb = holder.itemView.findViewById(R.id.image_progress_spinner);
                final String walletName = wallets.get(position).getWalletName();
                ((TextView) holder.itemView.findViewById(R.id.wallet_name)).setText(walletName);

                final RecyclerView.ViewHolder holder_f = holder;
                final int position_f = position;
                final String walletfilename = wallets.get(position_f).getWalletFileName();
                final UUID walletUuid = wallets.get(position).getUuid();

                GeneralAsyncTask<LoadWalletParams, Credentials> asyncTask = new GeneralAsyncTask<>();
                asyncTask.setBackgroundCompletion((params) -> {
                    Credentials credentials = null;

                    if (params.length < 1) {
                        return null;
                    }
                    LoadWalletParams param = params[0];

                    VariableHolder.LoadedWallet lw;
                    if((lw = VariableHolder.getInstance().getWalletAt(param.walletUuid)) == null) {
                        try {
                            credentials = WalletUtils.loadCredentials(
                                    VariableHolder.getInstance().getPassword(),
                                    param.walletSource
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (CipherException e) {
                            e.printStackTrace();
                        }
                    } else {
                        credentials = lw.getCredentials();
                    }

                    return credentials;
                });
                asyncTask.setPostExecuteCompletion((credentials) -> {
                    if (credentials != null) {
                        // Create loaded wallet
                        final VariableHolder.LoadedWallet loadedWallet = new VariableHolder.LoadedWallet(credentials, walletName);

                        // Save loaded wallet
                        VariableHolder.getInstance().putWallet(walletUuid, loadedWallet);

                        // Create blockies
                        EtherBlockies blockies = new EtherBlockies(credentials.getAddress().toCharArray(),
                                8, 4);
                        //TODO: scale for correct dp only
                        Bitmap blockiebmp = Bitmap.createScaledBitmap(
                                blockies.getBitmap(),
                                ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                                false
                        );

                        // Set values
                        ((CircleImageView) holder_f.itemView.findViewById(R.id.blocky_image)).setVisibility(View.VISIBLE);
                        pb.setVisibility(View.GONE);

                        ((CircleImageView) holder_f.itemView.findViewById(R.id.blocky_image)).setImageBitmap(blockiebmp);

                        ((TextView) holder_f.itemView.findViewById(R.id.wallet_address)).setText(Keys.toChecksumAddress(credentials.getAddress()));

                        holder_f.itemView.setOnClickListener((view) -> {
                            WalletDetailsFragment walletDetailsFragment = new WalletDetailsFragment();
                            walletDetailsFragment.setUuid(walletUuid);
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.navigation_content_view, walletDetailsFragment)
                                    .addToBackStack(null).commit();
                        });

                        //set the balance text views
                        final TextView balv = holder_f.itemView.findViewById(R.id.wallet_balance);

                        GeneralAsyncTask<Object, String> getBalanceTask = new GeneralAsyncTask<>();
                        getBalanceTask.setBackgroundCompletion((params) -> {
                            BigInteger balance = BigInteger.ZERO;
                            SSLHelper.initializeSSLContext(getContext());
                            try {
                                balance = VariableHolder.getInstance().getWeb3j().ethGetBalance(
                                        credentials.getAddress(),
                                        DefaultBlockParameterName.LATEST
                                ).send().getBalance();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return Convert.fromWei(balance.toString(), Convert.Unit.ETHER).toString();
                        });
                        getBalanceTask.setPostExecuteCompletion((result) -> {
                            String balanceTemplate = getString(R.string.balance_eth_template);
                            balv.setText(balanceTemplate.replace("$BALANCE$", result));
                        });

                        getBalanceTask.execute();
                    } else {
                        Log.d("LDEN", "lol isnull gg ez win nubs plz deinstall");
                    }
                });

                String walletSource = getContext().getDir(ConstantHolder.WALETFILES_FOLDER, Context.MODE_PRIVATE).getAbsolutePath() + "/" + walletfilename;
                asyncTask.execute(new LoadWalletParams(walletUuid, walletSource));
            }

            @Override
            public int getItemCount() {
                return wallets.size();
            }
        }

        walletsListRecycler.setAdapter(new WLRA());
    }
}
