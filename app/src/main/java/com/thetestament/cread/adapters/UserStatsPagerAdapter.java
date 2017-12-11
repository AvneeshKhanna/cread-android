package com.thetestament.cread.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;


public class UserStatsPagerAdapter extends PagerAdapter {

    FragmentActivity mContext;
    private int[] layouts;


    public UserStatsPagerAdapter(FragmentActivity mContext) {
        this.mContext = mContext;

        layouts = new int[]{R.layout.user_stats_page1, R.layout.user_stats_page2};

    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        return LayoutInflater.from(mContext).inflate(layouts[position], container, false);

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return layouts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
