package com.boilertalk.ballet.walletslist;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.Wallet;

public class SelectWalletDialogFragment extends DialogFragment {
    private WalletSelectedListener listener;

    public SelectWalletDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_wallets_list, null);

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (WalletSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + "must implement WalletSelectedListener");
        }
    }

    public interface WalletSelectedListener {
        void onWalletSelected(Wallet walllet);
    }
}
