package com.boilertalk.ballet.addwallet;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.Keyutils;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

import java.io.IOException;

import io.realm.Realm;

public class AddWalletFragment extends DialogFragment {
    private OnFragmentInteractionListener mListener;
    private View selectedContainer = null;

    public AddWalletFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_wallet, null);
        Button reloadButton = view.findViewById(R.id.button_reload);
        Button createButton = view.findViewById(R.id.button_create);
        final EditText walletNameInput = view.findViewById(R.id.add_wallet_input_name);

        Log.d("LOOL", "created!");
        final LinearLayout[] blockieContainers = new LinearLayout[6];
        final ImageView[] blockies = new ImageView[6];
        final ECKeyPair[] genKeypairs = new ECKeyPair[6];
        final Credentials[] genCredentials = new Credentials[6];
        final EtherBlockies[] genEtherBlockies = new EtherBlockies[6];

        blockieContainers[0] = view.findViewById(R.id.create_wallet_blockie_1);
        blockieContainers[0].setTag(0);
        blockieContainers[1] = view.findViewById(R.id.create_wallet_blockie_2);
        blockieContainers[1].setTag(1);
        blockieContainers[2] = view.findViewById(R.id.create_wallet_blockie_3);
        blockieContainers[2].setTag(2);
        blockieContainers[3] = view.findViewById(R.id.create_wallet_blockie_4);
        blockieContainers[3].setTag(3);
        blockieContainers[4] = view.findViewById(R.id.create_wallet_blockie_5);
        blockieContainers[4].setTag(4);
        blockieContainers[5] = view.findViewById(R.id.create_wallet_blockie_6);
        blockieContainers[5].setTag(5);
        for(LinearLayout blockieContainer : blockieContainers) {
            blockieContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("lal", "click!");
                    if(selectedContainer != null) {
                        selectedContainer.setBackgroundResource(0);
                    }
                    view.setBackgroundResource(R.color.pink300);
                    selectedContainer = view;
                }
            });
        }
        blockies[0] = view.findViewById(R.id.create_wallet_blockie_1).findViewById(R.id
                .create_wallet_blockie_img);
        blockies[1] = view.findViewById(R.id.create_wallet_blockie_2).findViewById(R.id
                .create_wallet_blockie_img);
        blockies[2] = view.findViewById(R.id.create_wallet_blockie_3).findViewById(R.id
                .create_wallet_blockie_img);
        blockies[3] = view.findViewById(R.id.create_wallet_blockie_4).findViewById(R.id
                .create_wallet_blockie_img);
        blockies[4] = view.findViewById(R.id.create_wallet_blockie_5).findViewById(R.id
                .create_wallet_blockie_img);
        blockies[5] = view.findViewById(R.id.create_wallet_blockie_6).findViewById(R.id
                .create_wallet_blockie_img);
        for(int i = 0; i < blockies.length; i++) {
            genKeypairs[i] = Keyutils.ramdomECKeyPair();
            genCredentials[i] = Credentials.create(genKeypairs[i]);

            TextView addr = (TextView) blockieContainers[i].findViewById(R.id.create_wallet_address);
            addr.setText(Keys.toChecksumAddress(genCredentials[i].getAddress()));

            genEtherBlockies[i] = new EtherBlockies(genCredentials[i].getAddress().toCharArray(), 8, 4);
            genEtherBlockies[i].printinfo();

            Bitmap blockiebmp = Bitmap.createScaledBitmap(genEtherBlockies[i].getBitmap(),
                    1024, 1024, false);

            blockies[i].setImageBitmap(blockiebmp);
        }
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedContainer != null) {
                    selectedContainer.setBackgroundResource(0);
                    selectedContainer = null;
                }
                for(int i = 0; i < blockies.length; i++) {
                    genKeypairs[i] = Keyutils.ramdomECKeyPair();
                    genCredentials[i] = Credentials.create(genKeypairs[i]);

                    TextView addr = (TextView) blockieContainers[i].findViewById(R.id.create_wallet_address);
                    addr.setText(Keys.toChecksumAddress(genCredentials[i].getAddress()));

                    genEtherBlockies[i] = new EtherBlockies(genCredentials[i].getAddress().toCharArray(), 8, 4);
                    genEtherBlockies[i].printinfo();

                    Bitmap blockiebmp = Bitmap.createScaledBitmap(genEtherBlockies[i].getBitmap(),
                            1024, 1024, false);

                    blockies[i].setImageBitmap(blockiebmp);
                }
            }
        });
        final View viow = view;
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ddfdferr", "createclicked!!!");
                if(selectedContainer == null) {
                    Log.d("DGMJZT", "shbde");
                    Snackbar.make(viow, getString(R.string.create_wallet_warn_no_wallet_selected)
                            , Snackbar.LENGTH_SHORT).show();
                } else {
                    if(walletNameInput.getText().length() == 0) {
                        Snackbar.make(viow, getString(R.string
                                .create_wallet_warn_no_name_entered), Snackbar.LENGTH_SHORT).show();
                        walletNameInput.requestFocus();
                    } else {
                        //save wallet in the database and create a wallet file
                        final ProgressDialog pd = android.app.ProgressDialog.show(viow.getContext(),
                                getString(R.string.loading_), getString(R.string.saving_wallet));
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                String interesting = null;
                                try {
                                    interesting = WalletUtils.generateWalletFile(VariableHolder
                                                    .getInstance().getPassword(), genKeypairs[(int)selectedContainer.getTag()],
                                            getContext().getDir(ConstantHolder.WALETFILES_FOLDER,
                                                    Context.MODE_PRIVATE),
                                            false);
                                } catch (CipherException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d("WWWWW", "Creating walletfile: " + interesting);

                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                //things in realm
                                Wallet wallet = realm.createObject(Wallet.class);
                                wallet.setWalletFileName(interesting);
                                wallet.setWalletName(walletNameInput.getText().toString());
                                //done with realm
                                realm.commitTransaction();

                                dismiss();
                                pd.dismiss();
                            }
                        });

                    }
                }
            }
        });


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
