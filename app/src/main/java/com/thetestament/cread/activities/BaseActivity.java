package com.thetestament.cread.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.thetestament.cread.utils.Constant.ACTION_LOG_OUT;

/**
 * Abstract activity that every other Activity in this application must implement.
 */

public class BaseActivity extends AppCompatActivity {

    BroadcastReceiver receiver;

    //For calligraphy library
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_LOG_OUT);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                finish();
            }
        };

        registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(receiver != null)
        {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate away from current screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
