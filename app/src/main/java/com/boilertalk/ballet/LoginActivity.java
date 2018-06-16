package com.boilertalk.ballet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.boilertalk.ballet.toolbox.ConstantHolder;
import com.boilertalk.ballet.toolbox.VariableHolder;

import org.spongycastle.crypto.generators.OpenBSDBCrypt;

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
            passwordText.setText(R.string.set_password);
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
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {

                                SecureRandom sera = new SecureRandom();
                                sera.nextBytes(salt);
                                String bcryptString = OpenBSDBCrypt.generate(passwordText.getText()
                                                .toString().toCharArray(), salt,12);
                                Log.d("lllll", "pass " + passwordText.getText().toString());

                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(ConstantHolder.SHPREF_PASSHASH_KEY, bcryptString);
                                editor.apply();

                                pd.dismiss();
                            }
                        });
                    } else {
                        //TODO: notify the user of his unmatching passwords
                        passwordConfirmText.requestFocus();
                        Snackbar wpSnackbar = Snackbar.make(view,
                                R.string.pws_not_matching_snackbar, Snackbar.LENGTH_SHORT);
                        wpSnackbar.show();
                    }
                } else {
                    final Snackbar wpSnackbar = Snackbar.make(view, R.string.wrong_pw_snackbar,
                            Snackbar.LENGTH_SHORT);
                    final ProgressDialog pd = ProgressDialog.show(view.getContext(), getString(R
                                    .string.loading_), "Checking password");
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(OpenBSDBCrypt.checkPassword(sharedPref.getString(ConstantHolder
                                            .SHPREF_PASSHASH_KEY, null),
                                    passwordText.getText().toString().toCharArray())) {
                                //correct
                                VariableHolder.setPassword(passwordText.getText().toString());
                                pd.dismiss();
                                Intent navPageIntent = new Intent(getApplicationContext(), NavigationActivity.class);
                                startActivity(navPageIntent);
                            } else {
                                //incorrect
                                //TODO notify user of his wrong password
                                passwordText.requestFocus();

                                pd.dismiss();
                                wpSnackbar.show();
                            }
                        }
                    });
                }
            }
        });
    }

}
