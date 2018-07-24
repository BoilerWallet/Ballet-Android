package com.boilertalk.ballet.navigation;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.walletsList.WalletsListFragment;
import com.boilertalk.ballet.addWallet.AddWalletFragment;
import com.boilertalk.ballet.walletDetails.WalletDetailsFragment;

public class NavigationActivity extends AppCompatActivity implements AddWalletFragment
        .OnFragmentInteractionListener, WalletDetailsFragment.OnFragmentInteractionListener {

    private FrameLayout fragmentView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_wallet:
                    getSupportFragmentManager().beginTransaction().add(R.id
                            .navigation_content_view, new WalletsListFragment()).commit();
                    return true;
                case R.id.navigation_send:
                    return true;
                case R.id.navigation_receive:
                    return true;
                case R.id.navigation_settings:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        fragmentView = findViewById(R.id.navigation_content_view);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_wallet);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("MAIN", uri.toString());
    }
}
