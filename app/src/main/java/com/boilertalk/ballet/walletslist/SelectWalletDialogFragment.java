package com.boilertalk.ballet.walletslist;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;

import org.web3j.crypto.Keys;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;

public class SelectWalletDialogFragment extends DialogFragment {

    public SelectWalletDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_wallet_select, null);

        RecyclerView walletsListView = view.findViewById(R.id.wallets_list_view);


        RealmResults<Wallet> wallets = Realm.getDefaultInstance().where(Wallet.class).findAll();
        walletsListView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                Context context = parent.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);

                View holded = inflater.inflate(R.layout.fragment_wallet_select_entry, parent,
                        false);
                return new RecyclerView.ViewHolder(holded) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                Wallet wallet = wallets.get(position);
                TextView walletName = holder.itemView.findViewById(R.id.wallet_name);
                TextView walletAddress = holder.itemView.findViewById(R.id.wallet_address);
                CircleImageView walletBlockie = holder.itemView.findViewById(R.id.blocky_image);
                ProgressBar loadingBlockie = holder.itemView.findViewById(R.id.image_progress_spinner);

                walletName.setText(wallet.getWalletName());
                walletAddress.setText(wallet.checksumAddress());
                EtherBlockies blockies = wallet.etherBlockies(8, 4);
                Bitmap blockiebmp = Bitmap.createScaledBitmap(
                        blockies.getBitmap(),
                        ConvertHelper.dpToPixels(56, getResources()), ConvertHelper.dpToPixels(56, getResources()),
                        false
                );
                loadingBlockie.setVisibility(View.GONE);
                walletBlockie.setVisibility(View.VISIBLE);
                walletBlockie.setImageBitmap(blockiebmp);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), 1,
                                new Intent().putExtra("wallet_uuid", wallet.getUuid().toString()));
                        dismiss();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return wallets.size();
            }
        });

        builder.setView(view);
        return builder.create();
    }
}
