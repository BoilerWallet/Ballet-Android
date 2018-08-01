package com.boilertalk.ballet.send;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class SendFragment extends Fragment {
    private CircleImageView senderBlockieView;
    private TextView        senderNameView;
    private TextView        senderAddrView;
    private Button          selectAccountButton;
    private EditText        receiverAddressInput;
    private EditText        ammountInput;
    private EditText        gasLimitInput;
    private TextView        feeInfoView;
    private SeekBar         feeInput;


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

        //TODO use those views...
    }
}
