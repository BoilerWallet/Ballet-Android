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
    private static final String NAVIGATION_SELECTED_ITEM_ID_TAG = "navigation_item_selected";

    // The FrameLayout holding the fragments
    @BindView(R.id.navigation_content_view) FrameLayout fragmentView;

    // The navigation bar
    @BindView(R.id.navigation) BottomNavigationViewEx bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            boolean success = false;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment currFrag = fragmentManager.getPrimaryNavigationFragment();
            if(currFrag != null) {
                if(currFrag.getTag().equals("details")) {
                    fragmentManager.popBackStackImmediate();
                    fragmentTransaction.detach(fragmentManager
                            .findFragmentByTag("navitem_nr_" + R.id.navigation_wallet));
                }
                fragmentTransaction.detach(currFrag);
            }

            String tag = "navitem_nr_" + Integer.toString(item.getItemId());
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if(fragment == null) {
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
                if(fragment != null) {
                    fragmentTransaction.add(fragmentView.getId(), fragment, tag);

                    success = true;
                }
            } else {
                fragmentTransaction.attach(fragment);

                success = true;
            }

            if(success) {
                fragmentTransaction.setPrimaryNavigationFragment(fragment);
                fragmentTransaction.setReorderingAllowed(true);
                fragmentTransaction.commitNowAllowingStateLoss();
            }

            return success;
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
        if((savedInstanceState != null) && savedInstanceState.containsKey(NAVIGATION_SELECTED_ITEM_ID_TAG)) {
            bottomNavigationView.setSelectedItemId(savedInstanceState.getInt(NAVIGATION_SELECTED_ITEM_ID_TAG));
        } else {
            bottomNavigationView.setSelectedItemId(R.id.navigation_wallet);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("navigation_item_selected", bottomNavigationView.getSelectedItemId());
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("MAIN", uri.toString());
    }
}
