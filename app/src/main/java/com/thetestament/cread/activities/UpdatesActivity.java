/*
package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.thetestament.cread.R;


import static com.thetestament.cread.utils.Constant.FRAGMENT_TAG_UPDATES_FRAGMENT;


*/
/**
 * AppcompatActivity class for Updates system i.e notification system.
 *//*


public class UpdatesActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updates);
        initView();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Finish this activity when user click on back navigation button
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    */
/**
     * Method to initialize views for this screen.
     *//*

    private void initView() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.updates_container, new UpdatesFragment(), FRAGMENT_TAG_UPDATES_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
*/
