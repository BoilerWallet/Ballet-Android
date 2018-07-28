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
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.walletslist.WalletsListFragment;
import com.boilertalk.ballet.addwallet.AddWalletFragment;
import com.boilertalk.ballet.walletdetails.WalletDetailsFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity implements AddWalletFragment
        .OnFragmentInteractionListener, WalletDetailsFragment.OnFragmentInteractionListener {

    // The FrameLayout holding the fragments
    @BindView(R.id.navigation_content_view) FrameLayout fragmentView;

    // The navigation bar
    @BindView(R.id.navigation) BottomNavigationViewEx bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            /*
            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment curFrag = fragmentManager.getPrimaryNavigationFragment();
            if (curFrag != null) {
                fragmentTransaction.detach(curFrag);
            }

            String tag;
            switch (item.getItemId()) {
                case R.id.navigation_wallet:
                    tag = "NAVIGATION_WALLET";
                    break;
                case R.id.navigation_send:
                    tag = "NAVIGATION_SEND";
                    break;
                case R.id.navigation_receive:
                    tag = "NAVIGATION_RECEIVE";
                    break;
                case R.id.navigation_settings:
                    tag = "NAVIGATION_SETTINGS";
                    break;
                default:
                    return false;
            }
            Fragment fragment = fragmentManager.findFragmentByTag(tag);

            if (fragment == null) {
                switch (item.getItemId()) {
                    case R.id.navigation_wallet:
                        fragment = new WalletsListFragment();
                        break;
                    case R.id.navigation_send:
                        break;
                    case R.id.navigation_receive:
                        break;
                    case R.id.navigation_settings:
                        break;
                }
                fragmentTransaction.add(item.getItemId(), fragment, tag);
            } else {
                fragmentTransaction.attach(fragment);
            }

            fragmentTransaction.setPrimaryNavigationFragment(fragment);
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.commitNowAllowingStateLoss();

            // ---

            return true;
            */
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
