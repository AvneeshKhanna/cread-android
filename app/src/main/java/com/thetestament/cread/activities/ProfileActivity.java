package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.MeFragment;

import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;

/**
 * This class shows the other users profile.
 */
public class ProfileActivity extends BaseActivity {

    //region :Fields and constants

    /**
     * Flag to store UUID.
     */
    @State
    String mUUID;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_profile);
        //Retrieve data from the intent
        mUUID = getIntent().getStringExtra(EXTRA_PROFILE_UUID);
        initScreen();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }
    //endregion

    //region :Private methods

    /**
     * Method to load Me fragment.
     */
    private void initScreen() {
        Bundle meBundle = new Bundle();
        meBundle.putString("calledFrom", "ProfileActivity");
        meBundle.putString("requesteduuid", mUUID);
        Fragment fragment = new MeFragment();
        fragment.setArguments(meBundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerProfile, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }
    //endregion
}
