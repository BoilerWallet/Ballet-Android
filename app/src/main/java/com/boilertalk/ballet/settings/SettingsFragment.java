package com.boilertalk.ballet.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.ERC20TrackedToken;
import com.boilertalk.ballet.database.RPCUrl;
import com.boilertalk.ballet.toolbox.VariableHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsFragment extends Fragment {

    @BindView(R.id.settings_select_network_subtitle) TextView selectedNetwork;
    @BindView(R.id.settings_tracked_erc20_tokens_subtitle) TextView tokensCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RPCUrl url = VariableHolder.getInstance().activeUrl();

        // Set active url name
        selectedNetwork.setText(url.getName());

        // Set tokens count
        long tc = Realm.getDefaultInstance()
                .where(ERC20TrackedToken.class)
                .equalTo("rpcUrlID", url.getUuid().toString())
                .count();
        tokensCount.setText("" + tc);
    }

    // Actions

    @OnClick(R.id.settings_select_network_view)
    void selectNetworkClicked() {
        SettingsSelectNetworkFragment selectNetworkFragment = new SettingsSelectNetworkFragment();

        showFragment(selectNetworkFragment);
    }

    @OnClick(R.id.settings_tracked_tokens_view)
    void trackedTokensClicked() {
        SettingsTrackERC20TokensFragment trackERC20TokensFragment = new SettingsTrackERC20TokensFragment();

        showFragment(trackERC20TokensFragment);
    }

    @OnClick(R.id.settings_change_password_view)
    void changePasswordClicked() {

    }

    // Helpers

    private void showFragment(Fragment fragment) {
        FragmentManager manager = getFragmentManager();
        if (manager == null) {
            return;
        }

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.detach(this);
        fragmentTransaction.setPrimaryNavigationFragment(fragment);
        fragmentTransaction.add(R.id.navigation_content_view, fragment, "details");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
