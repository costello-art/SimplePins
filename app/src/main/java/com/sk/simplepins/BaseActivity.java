package com.sk.simplepins;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

/**
 * Created by Sviat on Nov 02, 2016.
 */

public class BaseActivity extends AppCompatActivity {
    public void startActivityAndFinishCurrent(Context context, Class<?> cls) {
        Intent newActivity = new Intent(context, cls);
        startActivity(newActivity);
        finish();
    }

    public void logoutAndStartLoginActivity() {
        if (AccessToken.getCurrentAccessToken() == null) {
            startLoginActivity();
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance().logOut();
                startLoginActivity();

            }
        }).executeAsync();
    }

    private void startLoginActivity() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }
}