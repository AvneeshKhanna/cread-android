package com.thetestament.cread.activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.MeFragment;

import icepick.Icepick;
import icepick.State;

/**
 * This class shows the other users profile.
 */
public class ProfileActivity extends BaseActivity {

    @State
    String mUUID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_profile);
        //Retrieve data from the intent
        mUUID = getIntent().getStringExtra("uuid");
        initScreen();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    /**
     * Method to load Me fragment.
     */
    private void initScreen() {
        Bundle meBundle = new Bundle();
        meBundle.putString("calledFrom", "ProfileActivity");
        meBundle.putString("UUID", mUUID);
        Fragment fragment = new MeFragment();
        fragment.setArguments(meBundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerProfile, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
}
