package com.boilertalk.ballet.receive;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.VariableHolder;
import com.boilertalk.ballet.walletslist.SelectWalletDialogFragment;

import net.glxn.qrgen.android.QRCode;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class ReceiveFragment extends Fragment {

    private static final int RC_SELECT_WALLET = 1;

    private static final String SELECTED_WALLET_BUNDLE_KEY = "SELECTED_ACCOUNT_UUID";

    @BindView(R.id.selected_account_blockie) CircleImageView selectedAccountImage;
    @BindView(R.id.selected_account_text) TextView selectedAccountText;
    @BindView(R.id.selected_account_address_text) TextView selectedAccountAddressText;
    @BindView(R.id.receive_qr_code) ImageView receiveQRCode;
    @BindView(R.id.receive_select_account_button) Button selectAccountButton;

    private String selectedAccountUUID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore state
        String savedUUID = savedInstanceState != null ? savedInstanceState.getString(SELECTED_WALLET_BUNDLE_KEY) : null;
        if (savedUUID != null) {
            setAccount(savedUUID);
        }

        selectAccountButton.setOnClickListener((view1) -> {
            SelectWalletDialogFragment swdf = new SelectWalletDialogFragment();
            swdf.setTargetFragment(this, RC_SELECT_WALLET);
            swdf.show(getActivity().getSupportFragmentManager(), "SelectWalletDialog");
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RC_SELECT_WALLET:
                if(resultCode == 1) {
                    String s_uuid = data.getExtras().getString("wallet_uuid");
                    setAccount(s_uuid);
                }
                break;
            default:
                return;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SELECTED_WALLET_BUNDLE_KEY, selectedAccountUUID);
    }

    // Helpers

    private void setAccount(String walletUUID) {
        // Set selected account for state restoring
        selectedAccountUUID = walletUUID;

        Realm realm = Realm.getDefaultInstance();
        Wallet selectedWallet = realm.where(Wallet.class).equalTo("s_uuid", walletUUID).findFirst();;
        // Create blockies
        EtherBlockies blockies = selectedWallet.etherBlockies(8, 4);
        Bitmap blockiebmp = Bitmap.createScaledBitmap(
                blockies.getBitmap(),
                ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                false
        );
        selectedAccountImage.setImageBitmap(blockiebmp);
        selectedAccountText.setText(selectedWallet.getWalletName());
        selectedAccountAddressText.setText(selectedWallet.getAddress());

        // Set QR Code
        // See https://github.com/ethereum/EIPs/blob/master/EIPS/eip-681.md
        String qrUrl = "ethereum:" + selectedWallet.getAddress() + "@" + VariableHolder.getInstance().activeUrl().getChainId();
        int qrSize = perfectQRSize();
        Bitmap qrBm = QRCode.from(qrUrl).withSize(qrSize, qrSize).bitmap();
        receiveQRCode.setImageBitmap(qrBm);
    }

    private int perfectQRSize() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return Math.max(width, height);
    }
}
