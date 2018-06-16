package com.boilertalk.ballet;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WalletsListFragment extends Fragment {
    private static final String TAG = "WalletsListFragmet";
    private FloatingActionButton addWalletButton;

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

        addWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "create wallet clicked");
                AddWalletFragment awf = new AddWalletFragment();
                awf.show(getActivity().getSupportFragmentManager(), "AddWalletDialog");
            }
        });
    }
}
