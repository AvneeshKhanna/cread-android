package com.thetestament.cread.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.thetestament.cread.fragments.ProductTourFirstFragment;
import com.thetestament.cread.fragments.ProductTourSecondFragment;
import com.thetestament.cread.fragments.ProductTourThirdFragment;

/**
 * ViewpagerAdapter for product tour.
 */

public class ProductTourViewPagerAdapter extends FragmentPagerAdapter {

    public ProductTourViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new ProductTourFirstFragment();
                break;
            case 1:
                fragment = new ProductTourSecondFragment();
                break;
            case 2:
                fragment = new ProductTourThirdFragment();
                break;
            default:
                fragment = new ProductTourFirstFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
