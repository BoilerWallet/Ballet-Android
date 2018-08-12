package com.boilertalk.ballet.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.boilertalk.ballet.navigation.NavigationActivity;
import com.boilertalk.ballet.R;
import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.GeneralAsyncTask;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.spongycastle.crypto.generators.OpenBSDBCrypt;
import org.web3j.abi.datatypes.Bool;

import java.security.SecureRandom;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private EditText passwordText, passwordConfirmText;
    private Button goButton;
    private boolean createPasswordMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences(ConstantHolder
                .STANDARD_SHARED_PREFERENCES_FILE, MODE_PRIVATE);
        if (sharedPref.contains(ConstantHolder.SHPREF_PASSHASH_KEY)) {
            createPasswordMode = false;
        } else {
            createPasswordMode = true;
        }

        passwordText = findViewById(R.id.editText_pass);
        passwordConfirmText = findViewById(R.id.editText_passconfirm);
        if(createPasswordMode) {
            ((TextInputLayout)findViewById(R.id.editText_pass_container)).setHint(getString(R.string.set_password));
        } else {
            passwordConfirmText.setVisibility(View.GONE);
        }
        goButton = findViewById(R.id.button_go);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(createPasswordMode) {
                    if(passwordText.getText().toString().equals(passwordConfirmText.getText()
                            .toString())) {
                        //TODO: check if password is strong enough

                        final byte salt[] = new byte[16];

                        final ProgressDialog pd = ProgressDialog.show(view.getContext(), getString(R
                                        .string.loading_), getString(R.string.hashing_pw_in_progress));

                        GeneralAsyncTask<String, String> newpwt = new GeneralAsyncTask<>();
                        newpwt.setBackgroundCompletion((pass) -> {
                            SecureRandom sera = new SecureRandom();
                            sera.nextBytes(salt);
                            String bcryptString = OpenBSDBCrypt.generate(pass[0].toCharArray(),
                                    salt,12);

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(ConstantHolder.SHPREF_PASSHASH_KEY, bcryptString);
                            editor.apply();

                            return bcryptString;
                        });
                        newpwt.setPostExecuteCompletion((bcryptString) -> {
                            VariableHolder.getInstance().setPassword(passwordText.getText().toString());
                            //"delete" password in activity
                            passwordText.setText("");
                            passwordConfirmText.setText("");
                            Intent navPageIntent = new Intent(getApplicationContext(), NavigationActivity.class);
                            startActivity(navPageIntent);
                            finish();
                        });
                        newpwt.execute(passwordText.getText().toString());
                        
                    } else {
                        passwordConfirmText.requestFocus();
                        Snackbar wpSnackbar = Snackbar.make(view,
                                R.string.pws_not_matching_snackbar, Snackbar.LENGTH_SHORT);
                        wpSnackbar.show();
                    }
                } else {
                    final Snackbar wpSnackbar = Snackbar.make(view, R.string.wrong_pw_snackbar,
                            Snackbar.LENGTH_SHORT);
                    final ProgressDialog pd = ProgressDialog.show(view.getContext(), getString(R
                                    .string.loading_), getString(R.string.checking_password));
                    GeneralAsyncTask<String, Boolean> checkpwt = new GeneralAsyncTask<String, Boolean>();
                    checkpwt.setBackgroundCompletion((pass) -> new Boolean(OpenBSDBCrypt.checkPassword(
                            sharedPref.getString(ConstantHolder.SHPREF_PASSHASH_KEY, null),
                            pass[0].toCharArray())));
                    checkpwt.setPostExecuteCompletion((isCorrect) -> {
                        if(isCorrect.booleanValue()) {
                            //correct
                            VariableHolder.getInstance().setPassword(passwordText.getText().toString());
                            //"delete" password in activity
                            passwordText.setText("");
                            Intent navPageIntent = new Intent(getApplicationContext(), NavigationActivity.class);
                            startActivity(navPageIntent);
                            finish();
                        } else {
                            //incorrect
                            passwordText.requestFocus();

                            pd.dismiss();
                            wpSnackbar.show();
                        }
                    });
                    checkpwt.execute(passwordText.getText().toString());
                }
            }
        });
    }

}
