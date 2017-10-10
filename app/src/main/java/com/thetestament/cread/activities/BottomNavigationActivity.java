package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.AddFragment;
import com.thetestament.cread.fragments.ExploreFragment;
import com.thetestament.cread.fragments.FeedFragment;
import com.thetestament.cread.fragments.MeFragment;
import com.thetestament.cread.helpers.BottomNavigationViewHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATES_ACTIVITY;
import static com.thetestament.cread.utils.Constant.TAG_Add_FRAGMENT;
import static com.thetestament.cread.utils.Constant.TAG_EXPLORE_FRAGMENT;
import static com.thetestament.cread.utils.Constant.TAG_FEED_FRAGMENT;
import static com.thetestament.cread.utils.Constant.TAG_ME_FRAGMENT;

/**
 * Class to provide bottom navigation functionality.
 */

public class BottomNavigationActivity extends BaseActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView navigationView;

    @State
    String mFragmentTag;
    Fragment mCurrentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        //Bind View to this activity
        ButterKnife.bind(this);

        //Set actionbar
        setSupportActionBar(toolbar);
        //Set title
        setTitle("Cread");

        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
            mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, mFragmentTag);
        } else {
            //To load screen
            loadScreen();
        }

        //Initialize navigation view
        initBottomNavigation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, mFragmentTag, mCurrentFragment);
        }
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cread
                , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_updates:
                //Open updates screen
                startActivityForResult(new Intent(this, UpdatesActivity.class), REQUEST_CODE_UPDATES_ACTIVITY);
                return true;
            case R.id.action_settings:
                //Launch settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    /**
     * Method to load required screen.
     */
    private void loadScreen() {
        //if called from onClick of notifications
        if (getIntent().hasExtra("DATA")) {
            if (getIntent().getStringExtra("DATA").equals("startUpdatesFragment")) {
                //To select feed menu
                navigationView.setSelectedItemId(R.id.action_feed);

                //To open Feed Screen
                mCurrentFragment = new FeedFragment();
                //set fragment title
                mFragmentTag = TAG_FEED_FRAGMENT;
                replaceFragment(mCurrentFragment, mFragmentTag);
                //Launch updates activity
                startActivityForResult(new Intent(this, UpdatesActivity.class), REQUEST_CODE_UPDATES_ACTIVITY);
            }

        }
        //When app opened normally
        else {
            navigationView.setSelectedItemId(R.id.action_feed);
            //To open Feed Screen
            mCurrentFragment = new FeedFragment();
            //set fragment title
            mFragmentTag = TAG_FEED_FRAGMENT;
            replaceFragment(mCurrentFragment, mFragmentTag);
        }
    }

    /**
     * Method to replace current screen with new fragment.
     *
     * @param fragment    Fragment to be open.
     * @param tagFragment Tag for the fragment to be opened.
     */
    private void replaceFragment(Fragment fragment, String tagFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navigationView, fragment, tagFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    /**
     * Method to initialize BottomNavigation view.
     */
    private void initBottomNavigation() {
        //To disable shift mode
        BottomNavigationViewHelper.disableShiftMode(navigationView);
        //BottomNavigation navigation listener implementation
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Bottom navigation click functionality
                switch (item.getItemId()) {
                    case R.id.action_feed:
                        //Set title
                        setTitle("Cread");
                        mCurrentFragment = new FeedFragment();
                        //set fragment tag
                        mFragmentTag = TAG_FEED_FRAGMENT;
                        break;

                    case R.id.action_explore:
                        //Set title
                        setTitle("Explore");
                        mCurrentFragment = new ExploreFragment();
                        //Set fragment tag
                        mFragmentTag = TAG_EXPLORE_FRAGMENT;
                        break;

                    case R.id.action_add:
                        //Set title
                        setTitle("Add");
                        mCurrentFragment = new AddFragment();
                        //set fragment tag
                        mFragmentTag = TAG_Add_FRAGMENT;
                        break;

                    case R.id.action_me:
                        //Set title
                        setTitle("Me");
                        mCurrentFragment = new MeFragment();
                        //set fragment tag
                        mFragmentTag = TAG_ME_FRAGMENT;
                        break;
                }
                replaceFragment(mCurrentFragment, mFragmentTag);
                return true;
            }
        });
    }

}
