package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.MemeFifthFragment;
import com.thetestament.cread.fragments.MemeFirstFragment;
import com.thetestament.cread.fragments.MemeFourthFragment;
import com.thetestament.cread.fragments.MemeSecondFragment;
import com.thetestament.cread.fragments.MemeThirdFragment;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

/**
 * Appcompat activity for Meme creation.
 */

public class MemeActivity extends BaseActivity {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    //endregion

    //region :Fields and constants
    /**
     * To maintain reference of this screen.
     */
    MemeActivity mContext;

    /**
     * Flag to maintain last selected meme layout.
     */
    @State
    int mLastSelectedLayout = 0;

    SharedPreferenceHelper mSpHelper;

    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme);
        ButterKnife.bind(this);
        //Method called
        initViews();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meme, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_meme_layout:
                //Method called
                replaceFragment(mLastSelectedLayout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize view for this screen.
     */
    private void initViews() {
        //obtain reference of this screen
        mContext = this;
        //Obtain reference of shared preference helper
        mSpHelper = new SharedPreferenceHelper(mContext);
        //Update flag
        mLastSelectedLayout = mSpHelper.getLastSelectedMemePosition();

        //Method called
        replaceFragment();
    }


    /**
     * Method to replace current screen with new fragment.
     *
     * @param memeLayoutPosition Position of selected meme layout.
     */
    public void replaceFragment(int memeLayoutPosition) {
        Fragment fragment;

        switch (memeLayoutPosition) {
            case 1:
                fragment = new MemeSecondFragment();
                mLastSelectedLayout = 2;
                mSpHelper.setLastSelectedMemePosition(mLastSelectedLayout);
                break;
            case 2:
                fragment = new MemeThirdFragment();
                mLastSelectedLayout = 3;
                mSpHelper.setLastSelectedMemePosition(mLastSelectedLayout);
                break;
            case 3:
                fragment = new MemeFourthFragment();
                mLastSelectedLayout = 4;
                mSpHelper.setLastSelectedMemePosition(mLastSelectedLayout);
                break;
            case 4:
                fragment = new MemeFifthFragment();
                mLastSelectedLayout = 5;
                mSpHelper.setLastSelectedMemePosition(mLastSelectedLayout);
                break;
            case 5:
                fragment = new MemeFirstFragment();
                mLastSelectedLayout = 1;
                mSpHelper.setLastSelectedMemePosition(mLastSelectedLayout);
                break;
            default:
                fragment = new MemeFirstFragment();
                mLastSelectedLayout = 1;
                mSpHelper.setLastSelectedMemePosition(mLastSelectedLayout);
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.meme_frame_layout, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    /**
     * Overridden method to replace current screen with new fragment.
     */
    public void replaceFragment() {
        Fragment fragment;

        switch (mLastSelectedLayout) {
            case 1:
                fragment = new MemeFirstFragment();
                break;
            case 2:
                fragment = new MemeSecondFragment();
                break;
            case 3:
                fragment = new MemeThirdFragment();
                break;
            case 4:
                fragment = new MemeFourthFragment();

                break;
            case 5:
                fragment = new MemeFifthFragment();
                break;
            default:
                fragment = new MemeFirstFragment();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.meme_frame_layout, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    //endregion
}
