package com.project.sustain.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.project.sustain.R;
import com.project.sustain.model.User;
import com.project.sustain.model.UserManager;

/**
 * Activity that handles the logging in of the user
 */
public class LoginActivity extends AppCompatActivity {
    private EditText enteredUsername;
    private EditText enteredPassword;
    private UserManager mUserManager;
    private User mUser;
    private LoginResultListener mLoginResultListener;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserManager = new UserManager();

        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_login_toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Login to account");
            this.setSupportActionBar(toolbar);
            ActionBar support = getSupportActionBar();
            if (support != null) {
                support.setDisplayHomeAsUpEnabled(true);
                support.setDisplayShowHomeEnabled(true);
            }
        }
        // set Log In result listener
        mLoginResultListener = new LoginResultListener() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    try {
                        mUserManager.getCurrentUser();
                    } catch (UserNotAuthenticatedException e) {
                        Toast.makeText(getApplicationContext(), "Login failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Login failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable error) {
                Toast.makeText(getApplicationContext(), "Error logging in: " +
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        //set user result listener
        UserResultListener userResultListener = new UserResultListener() {
            @Override
            public void onComplete(User user) {
                if (user != null) {
                    mUser = user;
                } else {
                    mUser = new User();
                }
                startActivity(new Intent(LoginActivity.this, MainActivity.class)
                    .putExtra("user", mUser));
                finish();
            }

            @Override
            public void onError(Throwable error) {

            }
        };

        mUserManager.setUserResultListener(userResultListener);

        Button btnLogin = (Button) findViewById(R.id.buttonLogin);
        enteredUsername = (EditText) findViewById(R.id.editEmail);
        enteredPassword = (EditText) findViewById(R.id.editPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = enteredUsername.getText().toString();
                final String password = enteredPassword.getText().toString();

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // log in asynchronously
                mUserManager.setLoginResultListener(mLoginResultListener);
                mUserManager.logInUserEmailPassword(username, password);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLoginResultListener != null) {
            mUserManager.setLoginResultListener(mLoginResultListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLoginResultListener != null) {
            mUserManager.removeLoginResultListener();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
