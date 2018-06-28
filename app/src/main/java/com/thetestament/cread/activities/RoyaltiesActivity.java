package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.RoyaltiesFragment;

import static com.thetestament.cread.utils.Constant.TAG_ROYALTIES_FRAGMENT;

public class RoyaltiesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_royalties);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Royalties");

        //initialize view
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                //Navigate back to previous screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*Method to initialize views for this screen*/


    private void initView() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.royalties_container, new RoyaltiesFragment(), TAG_ROYALTIES_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
