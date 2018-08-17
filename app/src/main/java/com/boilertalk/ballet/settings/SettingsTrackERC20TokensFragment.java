package com.boilertalk.ballet.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.addwallet.AddWalletFragment;
import com.boilertalk.ballet.database.ERC20TrackedToken;
import com.boilertalk.ballet.database.RPCUrl;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.VariableHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsTrackERC20TokensFragment extends Fragment {

    @BindView(R.id.selected_network_color) ImageView selectedNetworkColor;
    @BindView(R.id.selected_network_name) TextView selectedNetworkName;

    @BindView(R.id.tokens_list_view) RecyclerView tokensListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_track_erc20_tokens, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RPCUrl selectedNetwork = VariableHolder.getInstance().activeUrl();

        // Set current network details
        if (selectedNetwork.isMainnet()) {
            selectedNetworkColor.setImageDrawable(getResources().getDrawable(R.drawable.network_circle_mainnet));
        } else if (selectedNetwork.isTestnet()) {
            selectedNetworkColor.setImageDrawable(getResources().getDrawable(R.drawable.network_circle_testnet));
        } else {
            selectedNetworkColor.setImageDrawable(getResources().getDrawable(R.drawable.network_circle_custom));
        }
        selectedNetworkName.setText(selectedNetwork.getName());

        // Setup tokens list
        RealmResults<ERC20TrackedToken> tokens = Realm.getDefaultInstance()
                .where(ERC20TrackedToken.class)
                .equalTo("rpcUrlID", selectedNetwork.getUuid().toString())
                .findAll();

        tokensListView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);

                View holded = inflater.inflate(R.layout.fragment_settings_track_erc20_tokens_entry, parent, false);
                return new RecyclerView.ViewHolder(holded) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ERC20TrackedToken token = tokens.get(position);
                if (token == null) {
                    return;
                }

                ImageView blockie = holder.itemView.findViewById(R.id.blocky_image);
                ProgressBar blockieProgress = holder.itemView.findViewById(R.id.image_progress_spinner);
                TextView tokenName = holder.itemView.findViewById(R.id.settings_track_entry_token_name);
                TextView tokenAddress = holder.itemView.findViewById(R.id.settings_track_entry_token_address);

                tokenName.setText(token.getName());
                tokenAddress.setText(token.getAddressString());

                EtherBlockies blockies = new EtherBlockies(token.getAddressString().toLowerCase().toCharArray(), 8, 4);
                Bitmap blockiebmp = Bitmap.createScaledBitmap(
                        blockies.getBitmap(),
                        ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                        false
                );
                blockie.setImageBitmap(blockiebmp);
                blockie.setVisibility(View.VISIBLE);
                blockieProgress.setVisibility(View.GONE);
            }

            @Override
            public int getItemCount() {
                return tokens.size();
            }
        });
    }

    // Actions

    @OnClick(R.id.settings_track_token_button)
    void trackTokenButtonClicked() {
        SettingsTrackNewERC20TokenFragment trackNewERC20TokenFragment = new SettingsTrackNewERC20TokenFragment();

        FragmentActivity fragment = getActivity();
        if (fragment == null) {
            return;
        }

        trackNewERC20TokenFragment.show(fragment.getSupportFragmentManager(), "AddTokenDialog");
    }
}
