package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

/**
 * Launcher screen for the app.
 */

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For fullscreen display
        initFullScreen();
        //Set layout files
        setContentView(R.layout.activity_splash);

        waitScreen();
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


    private void openNextScreen() {

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(SplashActivity.this);
        String uuid = spHelper.getUUID();
        String authkey = spHelper.getAuthToken();

        if(uuid!= null && authkey != null)
        {
            startActivity(new Intent(this, BottomNavigationActivity.class));
             //startActivity(new Intent(this, MerchandizingProductsActivity.class));
            //startActivity(new Intent(this, FindFBFriendsActivity.class));
        }
        else {

            //startActivity(new Intent(this, MerchandizingProductsActivity.class));
            //startActivity(new Intent(this, FindFBFriendsActivity.class));
            startActivity(new Intent(this, MainActivity.class));
            //startActivity(new Intent(this, BottomNavigationActivity.class));
        }

        //startActivity(new Intent(this,MainActivity.class));
        //startActivity(new Intent(this, BottomNavigationActivity.class));
    }

    /**
     * To open this screen in full screen mode.
     */
    private void initFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
