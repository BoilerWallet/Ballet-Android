package com.boilertalk.ballet.walletDetails;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.boilertalk.ballet.Etherscan.EtherscanAPI;
import com.boilertalk.ballet.Etherscan.EtherscanTransaction;
import com.boilertalk.ballet.R;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.SSLHelper;
import com.boilertalk.ballet.toolbox.VariableHolder;
import com.boilertalk.ballet.toolbox.iResult;

import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class WalletDetailsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private UUID walletId = null;
    private VariableHolder.LoadedWallet lw = null;
    private TextView balv;

    public WalletDetailsFragment() {
        // Required empty public constructor
    }

    public void setUuid(UUID walletId) {
        this.walletId = walletId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lw = VariableHolder.getWalletAt(walletId);
        balv = view.findViewById(R.id.wallet_details_balance);

        if(lw != null) {
            ((TextView)view.findViewById(R.id.wallet_details_name)).setText(lw.name);
            ((TextView)view.findViewById(R.id.wallet_details_address)).setText(Keys.toChecksumAddress(lw
                    .getCredentials().getAddress()));
            ((Button)view.findViewById(R.id.wallet_details_copyaddress)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService
                            (Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getString(R.string.wallet_address), Keys.toChecksumAddress(lw
                            .getCredentials().getAddress()));
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(view, R.string.copied_address, Snackbar
                            .LENGTH_SHORT).show();
                }
            });
            EtherBlockies eb = new EtherBlockies(lw.getCredentials().getAddress().toCharArray(),
                    8, 4);
            Bitmap scaledBlockie = Bitmap.createScaledBitmap(eb.getBitmap(),
                    ConvertHelper.dpToPixels(80, getResources()), ConvertHelper
                            .dpToPixels(80, getResources()),
                    false);
            ((CircleImageView)view.findViewById(R.id.wallet_details_blockie)).setImageBitmap(scaledBlockie);
            //get the balance
            new AsyncTask<Object, Object, Object>() {
                String balanceStr = null;
                @Override
                protected Object doInBackground(Object[] objects) {
                    BigInteger balance = BigInteger.ZERO;
                    SSLHelper.initializeSSLContext(getContext());
                    try {
                        balance = VariableHolder.getWeb3j().ethGetBalance(lw.getCredentials()
                                        .getAddress(),
                                DefaultBlockParameterName.LATEST).send().getBalance();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    balanceStr = Convert.fromWei(balance.toString(),
                            Convert.Unit.ETHER).toString();
                    return null;
                }
                @Override
                protected void onPostExecute(Object o) {
                    balv.setText(balv.getText().toString().replace("$BALANCE$",
                            balanceStr));
                }
            }.execute(null, null, null);

            final RecyclerView thr = view.findViewById(R.id.wallet_details_transactions);

            EtherscanAPI esa = new EtherscanAPI(lw.getCredentials().getAddress(), 20);
            esa.async_getNextPage(new iResult<ArrayList<EtherscanTransaction>>() {
                @Override
                public void onResult(final ArrayList<EtherscanTransaction> result) {
                    //final ArrayList<EtherscanTransaction> f_result = result;
                    Log.d("AAAA", "resilt " + Integer.toString(result.size()));
                    thr.setAdapter(new RecyclerView.Adapter() {
                        @Override
                        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            Context context = parent.getContext();
                            LayoutInflater inflater = LayoutInflater.from(context);

                            View holded = inflater.inflate(R.layout.transaction_history_entry, parent,
                                    false);
                            return new RecyclerView.ViewHolder(holded) {};
                        }

                        @Override
                        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                            ((TextView)holder.itemView.findViewById(R.id
                                    .transaction_sender_receiver))
                                    .setText(getString(R.string.sender_receiver)
                                            .replace("$SENDER$", result.get(position).srcAddr)
                                            .replace("$RECEIVER$", result.get(position).dstAddr));
                            ((TextView)holder.itemView.findViewById(R.id
                                    .transaction_sender_receiver)).setSelected(true);
                        }

                        @Override
                        public int getItemCount() {
                            return result.size();
                        }
                    });

                }
            });

        } else {
            //TODO: load wallet here
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
