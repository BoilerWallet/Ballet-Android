package com.boilertalk.ballet.send;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.boilertalk.ballet.walletslist.SelectWalletDialogFragment;

import org.web3j.crypto.Keys;

import java.math.BigDecimal;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class SendFragment extends Fragment {
    private static final int RC_SELECT_WALLET = 1;
    private static final int RC_SEND_CONFIRM = 2;
    private CircleImageView senderBlockieView;
    private TextView        senderNameView;
    private TextView        senderAddrView;
    private Button          selectAccountButton;
    private EditText        receiverAddressInput;
    private EditText        amountInput;
    private Button          currencyButton;
    private EditText        gasLimitInput;
    private TextView        feeInfoView;
    private SeekBar         feeInput;
    private Button          sendButton;
    private Wallet          selectedWallet;
    private Context         context;
    private int             gasPriceSliderPos;
    private BigDecimal      gasPriceGwei;


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
        amountInput             = view.findViewById(R.id.send_ammount_edittext);
        currencyButton          = view.findViewById(R.id.send_select_currency_button);
        gasLimitInput           = view.findViewById(R.id.send_gas_limit_edittext);
        feeInfoView             = view.findViewById(R.id.send_fee_description_text);
        feeInput                = view.findViewById(R.id.send_fee_seeker);
        sendButton              = view.findViewById(R.id.send_select_send_button);




        currencyButton.setOnClickListener((v) -> {
            // TODO: Open SettingsTrackERC20TokensFragment as dialog and setArguments (SettingsTrackERC20TokensFragment.FOR_SELECTING_KEY = true)
        });




        //restore selected wallet on fragment rebuild
        if(savedInstanceState != null) {
            Intent i = new Intent();
            String uuid = savedInstanceState.getString("walletUuid");
            if(uuid != null) {
                i.putExtra("wallet_uuid", uuid);
                onActivityResult(RC_SELECT_WALLET, 1, i);
            }
        } else {
            //put defaults for inputtexts
            gasLimitInput.setText(Integer.toString(21000));

        }

            //get fee info
            EthGasStationAPI.async_getGasInfo((gasInfo) -> {
                //set fee seeker
                feeInput.setMax((int) Math.ceil((gasInfo.fastestPrice - gasInfo.safeLowPrice) * 1000));
                feeInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        gasPriceSliderPos = i;
                        feeInfoView.setText(gasInfo.getFormattedInfoString(context, (i / 1000.0) + gasInfo.safeLowPrice));
                        gasPriceGwei = new BigDecimal((i / 1000.0) + gasInfo.safeLowPrice);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                if (savedInstanceState != null) {
                    gasPriceSliderPos = savedInstanceState.getInt("gasPriceSliderPos");
                    feeInput.setProgress(gasPriceSliderPos);
                    feeInfoView.setText(gasInfo.getFormattedInfoString(context, (gasPriceSliderPos / 1000.0) + gasInfo.safeLowPrice));
                    gasPriceGwei = new BigDecimal((gasPriceSliderPos / 1000.0) + gasInfo.safeLowPrice);
                } else {
                    feeInput.setProgress((int) Math.ceil((gasInfo.averagePrice - gasInfo.safeLowPrice) * 1000));
                    feeInfoView.setText(gasInfo.getFormattedInfoString(context, gasInfo.averagePrice));
                    gasPriceGwei = new BigDecimal(gasInfo.averagePrice);
                }

            });


        selectAccountButton.setOnClickListener((view1) -> {
            SelectWalletDialogFragment swdf = new SelectWalletDialogFragment();
            swdf.setTargetFragment(this, RC_SELECT_WALLET);
            swdf.show(getActivity().getSupportFragmentManager(), "SelectWalletDialog");
        });

        SendFragment that = this;
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedWallet == null) {
                    Snackbar.make(view, R.string.send_no_src_wallet, Snackbar.LENGTH_SHORT).show();
                    selectAccountButton.requestFocus();
                    return;
                }
                String receiverAddress = receiverAddressInput.getText().toString();
                BigDecimal amount;
                try {
                    amount = new BigDecimal(amountInput.getText().toString());
                } catch (NumberFormatException n) {
                    amount = null;
                }
                int gasLimit = Integer.parseInt(gasLimitInput.getText().toString());
                if(! Keys.toChecksumAddress(receiverAddress.toLowerCase()).equals(receiverAddress)) {
                    //address invalid
                    Snackbar.make(view, getString(R.string.send_invalid_receiver_addr), Snackbar.LENGTH_SHORT).show();
                    receiverAddressInput.requestFocus();
                    return;
                }

                if((amount == null) || (amount.compareTo(BigDecimal.ZERO) <= 0)) {
                    //amount invalid
                    Snackbar.make(view, R.string.send_invalid_amount, Snackbar.LENGTH_SHORT).show();
                    amountInput.requestFocus();
                    return;
                }

                if(gasLimit <= 0) {
                    //gas limit invalid
                    Snackbar.make(view, R.string.send_invalid_gas_limit, Snackbar.LENGTH_SHORT).show();
                    gasLimitInput.requestFocus();
                    return;
                }

                SendConfirmFragment scf = new SendConfirmFragment();
                Bundle arg = new Bundle();
                arg.putString("senderUuid", selectedWallet.getUuid().toString());
                arg.putString("receiverAddress", receiverAddress);
                arg.putSerializable("amount", amount);
                arg.putInt("gasLimit", gasLimit);
                arg.putSerializable("gasPrice", gasPriceGwei);
                scf.setArguments(arg);
                scf.setTargetFragment(that, RC_SEND_CONFIRM);
                scf.show(getActivity().getSupportFragmentManager(), "SendConfirmDialog");
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RC_SELECT_WALLET:
                if(resultCode == 1) {
                    String s_uuid = data.getExtras().getString("wallet_uuid");
                    Realm realm = Realm.getDefaultInstance();
                    selectedWallet = realm.where(Wallet.class).equalTo("s_uuid", s_uuid).findFirst();
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
            case RC_SEND_CONFIRM:
                if(resultCode == 1) {
                    //clear values because send is done
                    senderBlockieView.setImageBitmap(null);
                    senderNameView.setText("");
                    senderAddrView.setText("");
                    receiverAddressInput.setText("");
                    amountInput.setText("");
                    gasLimitInput.setText("");
                    selectedWallet = null;

                    Snackbar.make(sendButton, R.string.send_success, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(sendButton, R.string.send_failure, Snackbar.LENGTH_SHORT).show();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("gasPriceSliderPos", gasPriceSliderPos);
        outState.putString("walletUuid", (selectedWallet != null) ? selectedWallet.getUuid().toString() : null);
    }
}
