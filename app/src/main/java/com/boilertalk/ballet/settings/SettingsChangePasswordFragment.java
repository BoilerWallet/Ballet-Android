package com.boilertalk.ballet.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

            String newPassword = newPasswordText.getText().toString();
            if (!newPassword.equals(newPasswordRepeatText.getText().toString())) {
                newPasswordRepeatLayout.setError(getString(R.string.pws_not_matching_snackbar));
                pd.dismiss();
                return;
            }
            newPasswordRepeatLayout.setError(null);

            // All ok. recrypt all wallet files

            RealmResults<Wallet> wallets = Realm.getDefaultInstance().where(Wallet.class).findAll();
            VariableHolder.getInstance().getLoadedWallets(context, wallets, (loadedWallets) -> {
                Log.d("SettingsChangePassword", "Loaded " + loadedWallets.size() + " wallets.");
                pd.dismiss();

                class SavableWallet {

                    String newFileName;
                    VariableHolder.LoadedWallet loadedWallet;

                    SavableWallet(String newFileName, VariableHolder.LoadedWallet loadedWallet) {
                        this.newFileName = newFileName;
                        this.loadedWallet = loadedWallet;
                    }
                }

                GeneralAsyncTask<VariableHolder.LoadedWallet, List<SavableWallet>> encryptionTask = new GeneralAsyncTask<>();
                encryptionTask.setBackgroundCompletion((innerLoadedWallets) -> {
                    List<SavableWallet> savableWallets = new ArrayList<>();
                    for (VariableHolder.LoadedWallet wallet : innerLoadedWallets) {
                        ECKeyPair keyPair = wallet.getCredentials().getEcKeyPair();

                        String interesting;
                        try {
                            interesting = WalletUtils.generateWalletFile(newPassword, keyPair, context.getDir(ConstantHolder.WALETFILES_FOLDER, Context.MODE_PRIVATE), false);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        } catch (CipherException e) {
                            e.printStackTrace();
                            return null;
                        }

                        // Saving will happen later in one transaction.
                        savableWallets.add(new SavableWallet(interesting, wallet));
                    }

                    return savableWallets;
                });
                encryptionTask.setPostExecuteCompletion((savableWallets) -> {
                    if (savableWallets == null) {
                        // TODO: Show fail.
                        return;
                    }

                    // Save old wallet paths to clean up the directory
                    List<String> oldWalletFileNames = new ArrayList<>();

                    // We decrypted all wallet files and encrypted them with the new password.
                    // It's time to change all wallet filenames in a single realm transaction (to prevent loss)
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    for (SavableWallet savableWallet : savableWallets) {
                        Wallet wallet = savableWallet.loadedWallet.getWallet();
                        oldWalletFileNames.add(wallet.getWalletFileName());

                        wallet.setWalletFileName(savableWallet.newFileName);
                    }

                    // Commit all new filenames
                    realm.commitTransaction();

                    // Save the new password hash
                    GeneralAsyncTask<String, String> newpwt = new GeneralAsyncTask<>();
                    newpwt.setBackgroundCompletion((pass) -> {
                        final byte salt[] = new byte[16];
                        SecureRandom sera = new SecureRandom();
                        sera.nextBytes(salt);
                        String bcryptString = OpenBSDBCrypt.generate(pass[0].toCharArray(),
                                salt,12);

                        return bcryptString;
                    });
                    newpwt.setPostExecuteCompletion((bcryptString) -> {
                        // Save password hash in shared preferences
                        SharedPreferences sharedPref = context.getSharedPreferences(ConstantHolder.STANDARD_SHARED_PREFERENCES_FILE, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(ConstantHolder.SHPREF_PASSHASH_KEY, bcryptString);
                        editor.apply();

                        // We are "done". Cleanup step.
                        // Delete old wallet files, reset textfields, reset VariableHolder.

                        // Delete old wallet files
                        for (String filename : oldWalletFileNames) {
                            boolean s = new File(context.getDir(ConstantHolder.WALETFILES_FOLDER, Context.MODE_PRIVATE), filename).delete();
                            Log.d("SettingsChangePassword", "Deletion of " + filename + ". Success: " + s);
                        }

                        // Reset variable holder (simulate new login)
                        VariableHolder.getInstance().setPassword(newPassword);
                        VariableHolder.getInstance().resetLoadedWallets();

                        // Reset password textfields
                        oldPasswordText.setText("");
                        newPasswordText.setText("");
                        newPasswordRepeatText.setText("");

                        // Finally stop the progress dialog and show the success dialog.
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setPositiveButton("OK", (d, w) -> {
                                    d.dismiss();
                                })
                                .setTitle("Success")
                                .setMessage("Your password was changed.")
                                .show();
                        pd.dismiss();
                    });

                    // Run password hash task
                    newpwt.execute(newPassword);
                });

                // Run encryption task
                VariableHolder.LoadedWallet[] tmpLoadedArray = new VariableHolder.LoadedWallet[loadedWallets.size()];
                tmpLoadedArray = loadedWallets.toArray(tmpLoadedArray);
                encryptionTask.execute(tmpLoadedArray);
            });
        });

        // Run password check task
        passwordCheck.execute(oldPasswordText.getText().toString());
    }

    // Context fix

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
