package com.thetestament.cread.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.SettingsFragment;

public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Add settings fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, new SettingsFragment())
                .commit();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish this activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
