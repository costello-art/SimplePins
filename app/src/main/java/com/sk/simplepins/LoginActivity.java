package com.sk.simplepins;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by Sviat on Nov 02, 2016.
 */

public class LoginActivity extends BaseActivity implements FacebookCallback<LoginResult>, DialogInterface.OnClickListener {

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private ProfileTracker mProfileTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkGooglePlayServices();
        checkForLogin();

        loginButton = (LoginButton) findViewById(R.id.login_button);

        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, this);
    }

    private void checkForLogin() {
        if (AccessToken.getCurrentAccessToken() != null) {
            startActivityAndFinishCurrent(this, MainActivity.class);
        }
    }

    private void checkGooglePlayServices() {
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SERVICE_MISSING || result == ConnectionResult.SERVICE_DISABLED) {
            new AlertDialog.Builder(this)
                    .setTitle("Google Play Services")
                    .setMessage("Google play services required, please install/update")
                    .setCancelable(false)
                    .setPositiveButton("Ok", this)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        if (Profile.getCurrentProfile() != null) {
            startActivityAndFinishCurrent(this, MainActivity.class);
        }

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                mProfileTracker.stopTracking();

                startActivityAndFinishCurrent(LoginActivity.this, MainActivity.class);
            }
        };

        mProfileTracker.startTracking();
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {
        Toast.makeText(this, getString(R.string.err_unable_to_login) + error.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        finish();
    }
}