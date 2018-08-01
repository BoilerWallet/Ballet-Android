package com.boilertalk.ballet.navigation;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.send.SendFragment;
import com.boilertalk.ballet.walletslist.WalletsListFragment;
import com.boilertalk.ballet.addwallet.AddWalletFragment;
import com.boilertalk.ballet.walletdetails.WalletDetailsFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity implements AddWalletFragment
        .OnFragmentInteractionListener, WalletDetailsFragment.OnFragmentInteractionListener {

    // The FrameLayout holding the fragments
    @BindView(R.id.navigation_content_view) FrameLayout fragmentView;

    // The navigation bar
    @BindView(R.id.navigation) BottomNavigationViewEx bottomNavigationView;

    // Cache fragments
    private SparseArray<Fragment> cachedFragments = new SparseArray<Fragment>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;

            fragment = cachedFragments.get(item.getItemId());

            if (fragment == null) {
                switch (item.getItemId()) {
                    case R.id.navigation_wallet:
                        fragment = new WalletsListFragment();
                        break;
                    case R.id.navigation_send:
                        fragment = new SendFragment();
                        break;
                    case R.id.navigation_receive:
                        break;
                    case R.id.navigation_settings:
                        break;
                }
            }

            cachedFragments.put(item.getItemId(), fragment);

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.navigation_content_view, fragment)
                        .commit();
                return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        // Custom options
        bottomNavigationView.enableItemShiftingMode(false);
        bottomNavigationView.enableShiftingMode(false);
        bottomNavigationView.enableAnimation(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.navigation_wallet);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("MAIN", uri.toString());
    }
}
