package com.boilertalk.ballet.receive;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.toolbox.ConvertHelper;

import net.glxn.qrgen.android.QRCode;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ReceiveFragment extends Fragment {

    @BindView(R.id.selected_account_blockie) CircleImageView selectedAccountImage;
    @BindView(R.id.selected_account_text) TextView selectedAccountText;
    @BindView(R.id.selected_account_address_text) TextView selectedAccountAddressText;
    @BindView(R.id.receive_qr_code) ImageView receiveQRCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int width = receiveQRCode.getWidth();
        int height = receiveQRCode.getHeight();
        int size = Math.min(width, height);
        int dpSize = ConvertHelper.dpToPixels(300, getResources());

        Bitmap bm = QRCode.from("hahalol").withSize(dpSize, dpSize).bitmap();
        // receiveQRCode.setImageBitmap(bm);
    }
}
