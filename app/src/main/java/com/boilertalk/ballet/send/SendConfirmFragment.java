package com.boilertalk.ballet.send;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

public class SendConfirmFragment extends DialogFragment {
    @BindView(R.id.sender_blockie)      CircleImageView senderBlockieImageView;
    @BindView(R.id.receiver_blockie)    CircleImageView receiverBlockieImageView;
    @BindView(R.id.sender_addr)         TextView senderAddressTextView;
    @BindView(R.id.receiver_addr)       TextView receiverAddressTextView;
    @BindView(R.id.send_amount)         TextView sendAmountTextView;
    @BindView(R.id.sender_address_2)    TextView senderAddressTextView2;
    @BindView(R.id.receiver_address_2)  TextView receiverAddressTextView2;
    @BindView(R.id.send_amount_2)       TextView sendAmountTextView2;
    @BindView(R.id.send_sender_balance) TextView senderBalanceTextView;
    @BindView(R.id.send_coin)           TextView coinTextView;
    @BindView(R.id.send_network)        TextView networkTextView;
    @BindView(R.id.send_gas_limit)      TextView gasLimitTextView;
    @BindView(R.id.send_gas_price)      TextView gasPriceTextView;
    @BindView(R.id.send_max_fee)        TextView maxFeeTextView;
    @BindView(R.id.send_nonce)          TextView nonceTextView;
    @BindView(R.id.send_confirm_cancel_button)  Button cancelButton;
    @BindView(R.id.send_confirm_send_button)    Button sendButton;


    public SendConfirmFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_send_confirm, null);

        ButterKnife.bind(this, view);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        String s_uuid = getArguments().getString("senderUuid");
        Realm realm = Realm.getDefaultInstance();
        Wallet senderWallet = realm.where(Wallet.class).equalTo("s_uuid", s_uuid).findFirst();
        EtherBlockies blockies = senderWallet.etherBlockies(8, 4);
        Bitmap blockiebmp = Bitmap.createScaledBitmap(
                blockies.getBitmap(),
                ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                false
        );
        senderBlockieImageView.setImageBitmap(blockiebmp);
        String receiverAddress = getArguments().getString("receiverAddress");
        blockiebmp = Bitmap.createScaledBitmap(
                new EtherBlockies(receiverAddress.toLowerCase().toCharArray(),8, 4).getBitmap(),
                ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                false
        );
        receiverBlockieImageView.setImageBitmap(blockiebmp);

        senderAddressTextView.setText(senderWallet.checksumAddress());
        senderAddressTextView2.setText(getString(R.string.send_confirm_from_address_addr).replace("$ADDR$",senderWallet.checksumAddress()));
        receiverAddressTextView.setText(receiverAddress);
        receiverAddressTextView2.setText(getString(R.string.send_confirm_to_address_addr).replace("$ADDR$",receiverAddress));

        BigDecimal sendAmount = (BigDecimal) getArguments().getSerializable("amount");
        sendAmountTextView.setText(sendAmount.toPlainString() + getString(R.string.unit_ETH));
        sendAmountTextView2.setText(getString(R.string.send_confirm_amount_to_send_amount).replace("$AMOUNT$", sendAmount.toPlainString()));

        GeneralAsyncTask<String, String> gbat = new GeneralAsyncTask<>();
        gbat.setBackgroundCompletion((addresses) -> {
            if(addresses.length != 1) {
                return null;
            }
            BigInteger bi = null;
            try {
                bi = VariableHolder.getInstance().activeWeb3j().ethGetBalance(addresses[0], DefaultBlockParameterName.LATEST).send().getBalance();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(bi != null) {
                return Convert.fromWei(bi.toString(), Convert.Unit.ETHER).toString();
            } else {
                return null;
            }
        });
        gbat.setPostExecuteCompletion((value) ->
                senderBalanceTextView.setText(getString(R.string.send_confirm_sender_balance)
                        .replace("$BALANCE$", value)));
        gbat.execute(senderWallet.getAddress());

        coinTextView.setText(getString(R.string.send_confirm_coin_coin)
                .replace("$COIN$", getString(R.string.unit_ETH)));
        networkTextView.setText(getString(R.string.send_confirm_network_network)
                .replace("$NETWORK$", VariableHolder.getInstance().activeUrl().getUrl()));
        int gasLimit = getArguments().getInt("gasLimit");
        gasLimitTextView.setText(getString(R.string.send_confirm_gas_limit_gaslimit)
                .replace("$GASLIMIT$", Integer.toString(gasLimit)));
        BigDecimal gasPrice = (BigDecimal) getArguments().getSerializable("gasPrice");
        gasPriceTextView.setText(getString(R.string.send_confirm_gas_price_gasprice)
                .replace("$GASPRICE$", gasPrice.toPlainString() + getString(R.string.unit_gwei)));
        BigDecimal maxTxFee = gasPrice.multiply(new BigDecimal(Integer.toString(gasLimit)));
        maxFeeTextView.setText(getString(R.string.send_confirm_max_tx_fee_fee)
                .replace("$FEE$", Convert.fromWei(Convert.toWei(maxTxFee.toPlainString(), Convert.Unit.GWEI), Convert.Unit.ETHER).toPlainString() + getString(R.string.unit_ETH)));
        GeneralAsyncTask<String, String> gnat = new GeneralAsyncTask<>();
        gnat.setBackgroundCompletion((addresses) -> {
            if(addresses.length != 1) {
                return null;
            }
            BigInteger nonce = null;
            try {
                nonce = VariableHolder.getInstance().activeWeb3j().ethGetTransactionCount(
                        addresses[0],
                        DefaultBlockParameterName.LATEST
                ).send().getTransactionCount();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(nonce == null) {
                return null;
            } else {
                return nonce.toString();
            }

        });
        gnat.setPostExecuteCompletion((nonce) ->
                nonceTextView.setText(getString(R.string.send_confirm_nonce_nonce)
                        .replace("$NONCE$", nonce)));
        gnat.execute(senderWallet.getAddress());

        //marquee sht
        senderAddressTextView2.setSelected(true);
        receiverAddressTextView2.setSelected(true);
        networkTextView.setSelected(true);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO send transaction
                getTargetFragment().onActivityResult(getTargetRequestCode(), 1,
                        new Intent());
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }
}
