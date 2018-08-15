package com.boilertalk.ballet.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.RPCUrl;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsSelectNetworkFragment extends Fragment {

    private Context context;

    @BindView(R.id.networks_list_view) RecyclerView networkList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_select_network, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View setup
        networkList.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));

        // List setup
        RealmResults<RPCUrl> urls = Realm.getDefaultInstance().where(RPCUrl.class).findAll();

        networkList.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);

                View holded = inflater.inflate(R.layout.fragment_settings_select_network_entry, parent, false);
                return new RecyclerView.ViewHolder(holded) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                RPCUrl url = urls.get(position);
                if (url == null) {
                    return;
                }

                ImageView networkColor = holder.itemView.findViewById(R.id.network_entry_color);
                TextView networkName = holder.itemView.findViewById(R.id.network_entry_name);
                TextView networkUrl = holder.itemView.findViewById(R.id.network_entry_url);
                LottieAnimationView checkbox = holder.itemView.findViewById(R.id.network_entry_checkbox);

                // Set speed to 2.5
                checkbox.setSpeed(2.5f);

                // Set network color
                if (url.isMainnet()) {
                    networkColor.setImageDrawable(getResources().getDrawable(R.drawable.network_circle_mainnet));
                } else if (url.isTestnet()) {
                    networkColor.setImageDrawable(getResources().getDrawable(R.drawable.network_circle_testnet));
                } else {
                    networkColor.setImageDrawable(getResources().getDrawable(R.drawable.network_color_custom));
                }

                networkName.setText(url.getName());
                networkUrl.setText(url.getUrl());

                if (url.isActive()) {
                    checkbox.playAnimation();
                } else {
                    checkbox.setProgress(0);
                }

                holder.itemView.setOnClickListener(view -> {
                    Realm realm = Realm.getDefaultInstance();

                    // Begin realm transaction
                    realm.beginTransaction();

                    // Set all urls to inactive first
                    for (RPCUrl u : urls) {
                        u.setActive(false);
                    }
                    // Set selected url to active
                    url.setActive(true);

                    // Done with realm
                    realm.commitTransaction();

                    // Reload list
                    networkList.getAdapter().notifyDataSetChanged();
                });
            }

            @Override
            public int getItemCount() {
                return urls.size();
            }
        });
    }

    // Context fix

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
