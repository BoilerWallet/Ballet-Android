package com.boilertalk.ballet.send;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.networking.EthGasStationAPI;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.VariableHolder;
import com.boilertalk.ballet.walletslist.SelectWalletDialogFragment;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class SendFragment extends Fragment {
    private static final int RC_SELECT_WALLET = 1;
    private CircleImageView senderBlockieView;
    private TextView        senderNameView;
    private TextView        senderAddrView;
    private Button          selectAccountButton;
    private EditText        receiverAddressInput;
    private EditText        ammountInput;
    private EditText        gasLimitInput;
    private TextView        feeInfoView;
    private SeekBar         feeInput;
    private Wallet          selectedWallet;
    private Context         context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send_select, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //find those views...
        senderBlockieView       = view.findViewById(R.id.send_sender_blockie);
        senderNameView          = view.findViewById(R.id.send_sender_name_text);
        senderAddrView          = view.findViewById(R.id.send_sender_address_text);
        selectAccountButton     = view.findViewById(R.id.send_select_account_button);
        receiverAddressInput    = view.findViewById(R.id.send_receiver_addr_edittext);
        ammountInput            = view.findViewById(R.id.send_ammount_edittext);
        gasLimitInput           = view.findViewById(R.id.send_gas_limit_edittext);
        feeInfoView             = view.findViewById(R.id.send_fee_description_text);
        feeInput                = view.findViewById(R.id.send_fee_seeker);

        //get fee info
        EthGasStationAPI.async_getGasInfo((gasInfo) -> {
            //set fee seeker
            feeInput.setMax((int) Math.ceil((gasInfo.fastestPrice - gasInfo.safeLowPrice) * 1000000));
            feeInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    feeInfoView.setText(gasInfo.getFormattedInfoString(context, (i/1000000.0)+gasInfo.safeLowPrice));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            feeInput.setProgress((int) Math.ceil((gasInfo.averagePrice - gasInfo.safeLowPrice) * 1000000));
            feeInfoView.setText(gasInfo.getFormattedInfoString(context, gasInfo.averagePrice));
        });

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
                    Realm realm = Realm.getDefaultInstance();
                    selectedWallet = realm.where(Wallet.class).equalTo("s_uuid", s_uuid).findFirst();;
                    // Create blockies
                    EtherBlockies blockies = selectedWallet.etherBlockies(8, 4);
                    Bitmap blockiebmp = Bitmap.createScaledBitmap(
                            blockies.getBitmap(),
                            ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                            false
                    );
                    senderBlockieView.setImageBitmap(blockiebmp);
                    senderNameView.setText(selectedWallet.getWalletName());
                    senderAddrView.setText(selectedWallet.getAddress());
                }
                break;
            default:
                return;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
