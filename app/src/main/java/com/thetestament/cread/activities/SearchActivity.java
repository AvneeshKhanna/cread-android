package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.thetestament.cread.R;
import com.thetestament.cread.fragments.SearchHashTagFragment;
import com.thetestament.cread.fragments.SearchPeopleFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.utils.Constant.TAG_SEARCH_PEOPLE_FRAGMENT;

/**
 * AppCompatActivity class to provide search functionality.
 */

public class SearchActivity extends BaseActivity {

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    @State
    String mFragmentTag;
    Fragment mCurrentFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //Bind views
        ButterKnife.bind(this);
        //initialize tabLayout
        initializeTabLayoutListener();

        //SavedInstanceState is not null
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
            mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, mFragmentTag);
        } else {
            mCurrentFragment = new SearchPeopleFragment();
            mFragmentTag = TAG_SEARCH_PEOPLE_FRAGMENT;
            replaceFragment(mCurrentFragment, mFragmentTag);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        }
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


    /**
     * Method to initialize tabLayout click listener.
     */
    private void initializeTabLayoutListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mCurrentFragment = new SearchPeopleFragment();
                        mFragmentTag = TAG_SEARCH_PEOPLE_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag);
                        break;
                    case 1:
                        mCurrentFragment = new SearchHashTagFragment();
                        mFragmentTag = TAG_SEARCH_PEOPLE_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * Method to replace current screen with new fragment.
     *
     * @param fragment    Fragment to be open.
     * @param tagFragment Tag for the fragment to be opened.
     */
    public void replaceFragment(Fragment fragment, String tagFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment, tagFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

}
