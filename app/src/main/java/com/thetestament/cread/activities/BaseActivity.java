package com.thetestament.cread.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Abstract activity that every other Activity in this application must implement.
 */

public class BaseActivity extends AppCompatActivity {

    //For calligraphy library
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
