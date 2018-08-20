package com.boilertalk.ballet.settings;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.ERC20TrackedToken;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.ERC20TokenContract;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.Contract;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import rx.functions.Action1;

public class SettingsTrackNewERC20TokenFragment extends DialogFragment {

    @BindView(R.id.settings_erc20_contract_address_layout) TextInputLayout contractAddressInputLayout;
    @BindView(R.id.settings_erc20_contract_address) EditText contractAddressInput;
    @BindView(R.id.settings_token_button_test) Button testButton;
    @BindView(R.id.settings_erc20_contract_name) EditText contractNameInput;

    @BindView(R.id.settings_token_blocky_image) CircleImageView blockiesPreview;
    @BindView(R.id.settings_token_image_progress_spinner) ProgressBar blockiesPreviewProgressBar;
    @BindView(R.id.settings_token_name) TextView namePreview;
    @BindView(R.id.settings_token_supply) TextView totalSupplyPreview;

    @BindView(R.id.settings_token_save) Button saveButton;

    private class TrackResult {

        String address;
        int decimals;
        String symbol;

        TrackResult(String address, int decimals, String symbol) {
            this.address = address;
            this.decimals = decimals;
            this.symbol = symbol;
        }
    }
    private TrackResult result;

    public SettingsTrackNewERC20TokenFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_track_new_erc20_token, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deactivatePreview();

    }

    // Helpers

    private void deactivatePreview() {
        blockiesPreviewProgressBar.setVisibility(View.GONE);
        namePreview.setVisibility(View.GONE);
        totalSupplyPreview.setVisibility(View.GONE);
    }

    // Actions

    @OnClick(R.id.settings_token_button_test)
    void testButtonClicked() {
        String address = contractAddressInput.getText().toString();

        // Check address checksum
        if (!Keys.toChecksumAddress(address.toLowerCase()).equals(address)) {
            contractAddressInputLayout.setError("Checksum didn't match");
            return;
        }
        contractAddressInputLayout.setError(null);

        // One request at a time
        testButton.setEnabled(false);

        TransactionManager txManager = new ReadonlyTransactionManager(VariableHolder.getInstance().activeWeb3j(), "0x0000000000000000000000000000000000000000");

        ERC20TokenContract contract = ERC20TokenContract.load(address, VariableHolder.getInstance().activeWeb3j(), txManager, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);

        class ContractTestResult {
            String name;
            String symbol;
            BigInteger totalSupply;
            BigInteger decimals;

            ContractTestResult(String name, String symbol, BigInteger totalSupply, BigInteger decimals) {
                this.name = name;
                this.symbol = symbol;
                this.totalSupply = totalSupply;
                this.decimals = decimals;
            }
        }

        GeneralAsyncTask<ERC20TokenContract, ContractTestResult> task = new GeneralAsyncTask<>();
        task.setBackgroundCompletion(objects -> {
            if (objects.length < 1) {
                return null;
            }

            ERC20TokenContract c = objects[0];

            try {
                String name = c.name().send();
                String symbol = c.symbol().send();
                BigInteger totalSupply = c.totalSupply().send();
                BigInteger decimals = c.decimals().send();

                return new ContractTestResult(name, symbol, totalSupply, decimals);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        task.setPostExecuteCompletion(o -> {
            if (o == null) {
                // TODO: Error handling

                // Reactivate test button
                testButton.setEnabled(true);

                return;
            }

            // Name
            String nameText = o.name + " (" + o.symbol + ")";
            contractNameInput.setText(nameText);
            namePreview.setText(nameText);

            // Total supply
            String supplyText = getString(R.string.settings_token_total_supply, o.totalSupply.toString());
            totalSupplyPreview.setText(supplyText);

            // Blockies
            EtherBlockies blockies = new EtherBlockies(address.toLowerCase().toCharArray(), 8, 4);
            Bitmap blockiebmp = Bitmap.createScaledBitmap(
                    blockies.getBitmap(),
                    ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                    false
            );
            blockiesPreview.setImageBitmap(blockiebmp);

            // Enable previews
            namePreview.setVisibility(View.VISIBLE);
            totalSupplyPreview.setVisibility(View.VISIBLE);
            blockiesPreview.setVisibility(View.VISIBLE);

            // Enable save button and test button
            saveButton.setEnabled(true);
            testButton.setEnabled(true);

            // Set result
            this.result = new TrackResult(address, o.decimals.intValue(), o.symbol);
        });

        task.execute(contract);
    }

    @OnClick(R.id.settings_token_save)
    void saveButtonClicked() {
        if (result == null) {
            return;
        }

        String name = contractNameInput.getText().toString();
        if (name.isEmpty()) {
            // TODO: Error
            return;
        }

        // Deactivate buttons while saving to prevent double savings
        testButton.setEnabled(false);
        saveButton.setEnabled(false);

        Realm realm = Realm.getDefaultInstance();

        // Start realm transaction
        realm.beginTransaction();

        ERC20TrackedToken token = realm.createObject(ERC20TrackedToken.class, UUID.randomUUID().toString());
        token.setAddressString(Keys.toChecksumAddress(result.address));
        token.setName(name);
        token.setDecimals(result.decimals);
        token.setSymbol(result.symbol);
        token.setRpcUrlID(VariableHolder.getInstance().activeUrl().getUuid().toString());

        // Done with realm
        realm.commitTransaction();

        // Reset everything
        deactivatePreview();
        contractAddressInput.setText("");
        contractNameInput.setText("");

        // Close Dialog, send activity result
    }
}
