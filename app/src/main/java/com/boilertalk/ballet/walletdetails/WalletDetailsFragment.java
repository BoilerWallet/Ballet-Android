package com.boilertalk.ballet.walletdetails;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.networking.EtherscanAPI;
import com.boilertalk.ballet.networking.EtherscanTransaction;
import com.boilertalk.ballet.R;
import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.ConvertHelper;
import com.boilertalk.ballet.toolbox.EtherBlockies;
import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.toolbox.LimitedBigDecimalString;
import com.boilertalk.ballet.toolbox.SSLHelper;
import com.boilertalk.ballet.toolbox.VariableHolder;
import com.boilertalk.ballet.toolbox.iResult;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.web3j.abi.datatypes.Bool;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class WalletDetailsFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private Wallet wallet = null;
    private TextView balv;

    public WalletDetailsFragment() {
        // Required empty public constructor
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
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
        balv = view.findViewById(R.id.wallet_details_balance);

        ((TextView) view.findViewById(R.id.wallet_details_name)).setText(wallet.getWalletName());
        ((TextView) view.findViewById(R.id.wallet_details_address)).setText(Keys.toChecksumAddress(wallet.checksumAddress()));
        ((Button) view.findViewById(R.id.wallet_details_copyaddress)).setOnClickListener(view1 -> {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.wallet_address), Keys.toChecksumAddress(wallet.checksumAddress()));
            clipboard.setPrimaryClip(clip);
            Snackbar.make(view1, R.string.copied_address, Snackbar.LENGTH_SHORT).show();
        });

        EtherBlockies eb = wallet.etherBlockies(8, 4);
        Bitmap scaledBlockie = Bitmap.createScaledBitmap(
                eb.getBitmap(),
                ConvertHelper.dpToPixels(80, getResources()), ConvertHelper.dpToPixels(80, getResources()),
                false
        );
        ((CircleImageView) view.findViewById(R.id.wallet_details_blockie)).setImageBitmap(scaledBlockie);

        //get the balance

        GeneralAsyncTask<String, String> asyncTask = new GeneralAsyncTask<>();
        asyncTask.setBackgroundCompletion((params) -> {
            if (params.length < 1) {
                return null;
            }
            String address = params[0];

            BigInteger balance = BigInteger.ZERO;
            SSLHelper.initializeSSLContext(getContext());
            try {
                balance = VariableHolder.getInstance().activeWeb3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Convert.fromWei(balance.toString(), Convert.Unit.ETHER).toString();
        });
        asyncTask.setPostExecuteCompletion((balance) -> {
            String balanceTemplate = getString(R.string.balance_eth_template);
            balv.setText(balanceTemplate.replace("$BALANCE$", balance));
        });
        asyncTask.execute(wallet.getAddress());

        final RecyclerView thr = view.findViewById(R.id.wallet_details_transactions);

        final EtherscanAPI esa = new EtherscanAPI(wallet.getAddress(), 20);
        esa.async_getNextPage(new iResult<ArrayList<EtherscanTransaction>>() {
            @Override
            public void onResult(final ArrayList<EtherscanTransaction> result) {
                //final ArrayList<EtherscanTransaction> f_result = result;
                class bw {
                    public boolean b;
                    public bw(boolean b) {
                        this.b = b;
                    }
                }
                final bw txListComplete = new bw((result.size() < 20) ? true : false);
                final ArrayList<EtherscanTransaction> f_result = result;
                Log.d("AAAA", "resilt " + Integer.toString(result.size()) + " comp " + Boolean.toString(txListComplete.b));
                thr.setAdapter(new RecyclerView.Adapter() {
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        Context context = parent.getContext();
                        LayoutInflater inflater = LayoutInflater.from(context);

                        View holded = inflater.inflate(R.layout.transaction_history_entry, parent,
                                false);
                        return new RecyclerView.ViewHolder(holded) {
                        };
                    }

                    @Override
                    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                        if(position >= result.size()) {
                            ProgressBar pb = ((ProgressBar)holder.itemView.findViewById(R.id.tx_loading_spinner));
                            View content = holder.itemView.findViewById(R.id.tx_content);
                            content.setVisibility(View.GONE);
                            pb.setVisibility(View.VISIBLE);
                        } else {
                            boolean fromself = false, toself = false;
                            TextView fromTo = ((TextView) holder.itemView
                                    .findViewById(R.id.transaction_sender_receiver));
                            TextView balance = ((TextView) holder.itemView
                                    .findViewById(R.id.transaction_balance));
                            ImageView icon = ((ImageView) holder.itemView
                                    .findViewById(R.id.transaction_type_img));
                            RelativeTimeTextView txtimeView = ((RelativeTimeTextView) holder.itemView
                                    .findViewById(R.id.transaction_time));
                            EtherscanTransaction etx = result.get(position);

                            if (etx.dstAddr.equalsIgnoreCase(wallet.getAddress())) {
                                toself = true;
                            }
                            if (etx.srcAddr.equalsIgnoreCase(wallet.getAddress())) {
                                fromself = true;
                            }
                            fromTo.setText(getString(R.string.sender_receiver)
                                    .replace("$SENDER$",
                                            fromself ? getString(R.string.self) :
                                                    etx.srcAddr)
                                    .replace("$RECEIVER$",
                                            toself ? getString(R.string.self) :
                                                    etx.dstAddr));
                            fromTo.setSelected(true);

                            if (fromself == true && toself == false) {
                                icon.setImageResource(R.drawable.ic_arrow_upward);
                                icon.setColorFilter(ContextCompat.getColor(getContext(), R.color
                                                .red500),
                                        PorterDuff.Mode.SRC_ATOP);
                            } else if (fromself == false && toself == true) {
                                icon.setImageResource(R.drawable.ic_arrow_downward);
                                icon.setColorFilter(ContextCompat.getColor(getContext(), R.color
                                                .green500),
                                        PorterDuff.Mode.SRC_ATOP);
                            } else if (fromself == true && toself == true) {
                                icon.setImageResource(R.drawable.ic_selftoself_transaction);
                                icon.setColorFilter(ContextCompat.getColor(getContext(), R.color
                                                .colorPrimary),
                                        PorterDuff.Mode.SRC_ATOP);
                            }

                            String balanceStr = LimitedBigDecimalString.createString(6,
                                    Convert.fromWei(Long.toString(etx.value), Convert.Unit.ETHER));
                            balance.setText(getString(R.string.balance_eth_template)
                                    .replace("$BALANCE$", balanceStr));

                            txtimeView.setReferenceTime(etx.timestamp * 1000);
                            Log.d("TIMES", "TAMP " + Long.toString(etx.timestamp));
                        }
                    }

                    @Override
                    public int getItemCount() {
                        if(txListComplete.b) {
                            return result.size();
                        } else {
                            return result.size() + 1;
                        }
                    }
                });
                thr.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    boolean loading = false;
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if(txListComplete.b == false) {
                            if (dy > 0) {
                                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                                int visibleItemCount = lm.getChildCount();
                                int totalItemCount = lm.getItemCount();
                                int pastVisibleItemCount = lm.findFirstVisibleItemPosition();
                                if ((visibleItemCount + pastVisibleItemCount) >=
                                        (totalItemCount - 1/*reserve*/)) {
                                    if(!loading) {
                                        loading = true;
                                        esa.async_getNextPage(new iResult<ArrayList<EtherscanTransaction>>() {
                                            @Override
                                            public void onResult(ArrayList<EtherscanTransaction> result) {
                                                loading = false;
                                                f_result.addAll(result);
                                                if(result.size() < 20) {
                                                    txListComplete.b = true;
                                                }
                                                thr.getAdapter().notifyDataSetChanged();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                });

            }
        });
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
