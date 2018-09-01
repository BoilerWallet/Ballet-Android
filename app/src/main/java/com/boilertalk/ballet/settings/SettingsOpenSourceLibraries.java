package com.boilertalk.ballet.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.networking.SettingsMarkdownAPI;
import com.mukesh.MarkdownView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsOpenSourceLibraries extends Fragment {

    private Context context;

    @BindView(R.id.settings_open_source_markdown_view) MarkdownView renderView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_open_source_libraries, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ProgressDialog pd = ProgressDialog.show(context, getString(R.string.loading_), getString(R.string.settings_open_source_progress_message));

        SettingsMarkdownAPI.getOpenSourceLibrariesAsync((res) -> {
            pd.dismiss();

            if (res == null) {
                Log.e("SettingsOpenSource", "Open source libraries fetch failed.");

                // Show error
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setPositiveButton(R.string.settings_dialog_button_ok, (d, w) -> {
                            d.dismiss();
                        })
                        .setTitle(R.string.settings_open_source_error_title)
                        .setMessage(R.string.settings_open_source_error_message)
                        .show();

                return;
            }

            renderView.setMarkDownText(res);
        });
    }

    // Context fix

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
