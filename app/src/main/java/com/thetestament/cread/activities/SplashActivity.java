package com.thetestament.cread.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import static com.thetestament.cread.BuildConfig.DEBUG;
import static com.thetestament.cread.utils.Constant.MINIMUM_APP_VERSION_KEY;

/**
 * Launcher screen for the app.
 */

public class SplashActivity extends AppCompatActivity {


    //region :Overridden methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For fullscreen display
        ViewHelper.initFullScreen(this);
        //Set layout files
        setContentView(R.layout.activity_splash);
        //initialize force update system
        //Method called
        initForceUpdateSystem();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

    }
    //endregion

    //region Private method

    /**
     * To make the screen wait for some time and then start the next screen.
     */
    private void waitScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                openNextScreen();
            }
        }, 2000);//Screen wait for 2000 milli seconds
    }


    /**
     * Method to launch new screen depending upon the condition.
     */
    private void openNextScreen() {
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(this);
        String uuid = spHelper.getUUID();
        String authkey = spHelper.getAuthToken();

        //if UUID and AuthKey are present
        if (uuid != null && authkey != null) {
            //Open BottomNavigationActivity screen
            startActivity(new Intent(this, BottomNavigationActivity.class));
        } else {
            // to generate token when the app is installed first time
            // so that it can be sent to the server when user logs in
            FirebaseInstanceId.getInstance().getToken();

            //If product tour is yet to be opened
            if (spHelper.isProductTourFirstTime()) {
                //Open MainActivity screen
                startActivity(new Intent(this, ProductTourActivity.class));
            } else {
                //Open MainActivity screen
                startActivity(new Intent(this, MainActivity.class));
            }
            //Finish this call
            finish();
        }
    }


    /**
     * To initialize force app update system
     */
    private void initForceUpdateSystem() {
        //Get Remote Config Instance
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config);

        long cacheExpiration; // 30 minutes in seconds.

        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (BuildConfig.DEBUG) {
            cacheExpiration = 0;
        } else {
            cacheExpiration = 1800;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(SplashActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                            // Product update dialog called
                            getProductUpdateDialog(mFirebaseRemoteConfig);
                        } else {
                            // initialize deep link
                            initDeepLink();
                        }
                    }
                });
    }


    /**
     * Method to display update dialog depending upon the Remote config value
     */
    private void getProductUpdateDialog(FirebaseRemoteConfig firebaseRemoteConfig) {

        Long minimumAppVersion = firebaseRemoteConfig.getLong(MINIMUM_APP_VERSION_KEY);

        if (minimumAppVersion > BuildConfig.VERSION_CODE) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title("Update required")
                    .content("We have recently made some important changes for proper functioning of the app. Please update to the latest version to get the best experience.")
                    .contentGravity(GravityEnum.START)
                    .positiveText("DO IT NOW")
                    .positiveColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .autoDismiss(false)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //Method called
                            IntentHelper.openPlayStore(SplashActivity.this);
                            dialog.dismiss();
                            finish();
                        }
                    });
            MaterialDialog dialog = builder.build();
            dialog.show();
        } else {
            // initialize deep link
            initDeepLink();
        }
    }


    /**
     * Method to get check for deep link.
     */
    private void initDeepLink() {
        final SharedPreferenceHelper spHelper = new SharedPreferenceHelper(SplashActivity.this);

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {

                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }

                        // saving deep link in shared preferences
                        if (deepLink != null) {
                            spHelper.setDeepLink(deepLink.toString());
                        } else {
                            spHelper.setDeepLink(null);
                        }

                        //load data
                        waitScreen();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "SplashActivity");
                        // set as null
                        spHelper.setDeepLink(null);

                        //load data
                        waitScreen();
                    }
                });
    }

    //endregion

}
