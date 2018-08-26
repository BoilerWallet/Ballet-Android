package com.boilertalk.ballet.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.database.Wallet;
import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.spongycastle.crypto.generators.OpenBSDBCrypt;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.Context.MODE_PRIVATE;

public class SettingsChangePasswordFragment extends Fragment {

    @BindView(R.id.settings_password_old_layout) TextInputLayout oldPasswordLayout;
    @BindView(R.id.settings_password_old) EditText oldPasswordText;

    @BindView(R.id.settings_password_new_layout) TextInputLayout newPasswordLayout;
    @BindView(R.id.settings_password_new) EditText newPasswordText;

    @BindView(R.id.settings_password_new_repeat_layout) TextInputLayout newPasswordRepeatLayout;
    @BindView(R.id.settings_password_new_repeat) EditText newPasswordRepeatText;

    @BindView(R.id.settings_password_save) Button saveButton;

    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_change_password, container, false);

        // ButterKnife
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Actions

    @OnClick(R.id.settings_password_save)
    void saveButtonClicked() {
        final ProgressDialog pd = ProgressDialog.show(context, getString(R.string.loading_), getString(R.string.checking_password));

        GeneralAsyncTask<String, Boolean> passwordCheck = new GeneralAsyncTask<>();
        passwordCheck.setBackgroundCompletion(objects -> {
            if (objects.length < 1) {
                return false;
            }

            String password = objects[0];

            SharedPreferences sharedPref = context.getSharedPreferences(ConstantHolder.STANDARD_SHARED_PREFERENCES_FILE, MODE_PRIVATE);
            return OpenBSDBCrypt.checkPassword(sharedPref.getString(ConstantHolder.SHPREF_PASSHASH_KEY, ""), password.toCharArray());
        });
        passwordCheck.setPostExecuteCompletion(success -> {
            if (!success) {
                oldPasswordLayout.setError(getString(R.string.wrong_pw_snackbar));
                pd.dismiss();
                return;
            }
            oldPasswordLayout.setError(null);

            // TODO: Check password strength

            if (!newPasswordText.getText().toString().equals(newPasswordRepeatText.getText().toString())) {
                newPasswordRepeatLayout.setError(getString(R.string.pws_not_matching_snackbar));
                pd.dismiss();
                return;
            }
            newPasswordRepeatLayout.setError(null);

            // All ok. recrypt all wallet files

            RealmResults<Wallet> wallets = Realm.getDefaultInstance().where(Wallet.class).findAll();
            VariableHolder.getInstance().getLoadedWallets(context, wallets, (loadedWallets) -> {
                Log.d("SettingsChangePassword", "Yes loaded!" + loadedWallets.size() + "wallets");
                pd.dismiss();
            });
        });
        passwordCheck.execute(oldPasswordText.getText().toString());
    }

    // Context fix

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
