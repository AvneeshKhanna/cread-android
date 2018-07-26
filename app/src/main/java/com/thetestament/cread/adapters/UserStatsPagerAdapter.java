package com.thetestament.cread.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener.OnUserStatsClickedListener;

import static com.thetestament.cread.utils.Constant.GratitudeNumbers.BADGES;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.COLLABORATIONS;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.COMMENT;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.FOLLOWERS;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.FOLLOWING;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.HATSOFF;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.POSTS;


public class UserStatsPagerAdapter extends PagerAdapter {

    FragmentActivity mContext;
    private int[] layouts;
    private OnUserStatsClickedListener onUserStatsClickedListener;


    public UserStatsPagerAdapter(FragmentActivity mContext, int[] layouts) {
        this.mContext = mContext;
        this.layouts = layouts;
    }

    /**
     * Method to set listener
     *
     * @param onUserStatsClickedListener
     */
    public void setUserStatsClickedListener(OnUserStatsClickedListener onUserStatsClickedListener) {
        this.onUserStatsClickedListener = onUserStatsClickedListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(layouts[position], container, false);
        // init views
        initializeViews(position, view);
        container.addView(view);
        return view;

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

    /**
     * Method to initialize views and their listeners
     *
     * @param position
     * @param view
     */
    private void initializeViews(int position, View view) {
        switch (position) {
            case 0:
                // set click listener
                final LinearLayout containerPosts = view.findViewById(R.id.containerPosts);
                containerPosts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        onUserStatsClickedListener.onUserStatsClicked(POSTS, containerPosts);
                    }
                });
                // set tag
                TextView postsCount = view.findViewById(R.id.textPostsCount);
                postsCount.setTag(POSTS);

                // set click listener
                final LinearLayout containerBadges = view.findViewById(R.id.containerBadges);
                containerBadges.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onUserStatsClickedListener.onUserStatsClicked(BADGES, containerBadges);
                    }
                });
                // set tag
                TextView postsBadges = view.findViewById(R.id.textBadgeCount);
                postsBadges.setTag(BADGES);

                // set click listener
                final LinearLayout containerFollowers = view.findViewById(R.id.containerFollowers);
                containerFollowers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        onUserStatsClickedListener.onUserStatsClicked(FOLLOWERS, containerFollowers);
                    }
                });
                // set tag
                TextView followersCount = view.findViewById(R.id.textFollowersCount);
                followersCount.setTag(FOLLOWERS);

                // set click listener
                final LinearLayout containerFollowing = view.findViewById(R.id.containerFollowing);
                containerFollowing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onUserStatsClickedListener.onUserStatsClicked(FOLLOWING, containerFollowing);
                    }
                });
                // set tag
                TextView followingCount = view.findViewById(R.id.textFollowingCount);
                followingCount.setTag(FOLLOWING);
                break;


            case 1:
                //set click listener
                final LinearLayout containerHatsOff = view.findViewById(R.id.containerHatsOff);
                containerHatsOff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onUserStatsClickedListener.onUserStatsClicked(HATSOFF, containerHatsOff);
                    }
                });
                // set tag for hatsoff
                TextView hatsoffCount = view.findViewById(R.id.textHatsOffCount);
                hatsoffCount.setTag(HATSOFF);

                // click listener
                final LinearLayout containerComments = view.findViewById(R.id.containerComments);
                containerComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        onUserStatsClickedListener.onUserStatsClicked(COMMENT, containerComments);
                    }
                });
                // set tag for comments
                TextView commentsCount = view.findViewById(R.id.textCommentsCount);
                commentsCount.setTag(COMMENT);

                // click listener
                final LinearLayout containerCollaborations = view.findViewById(R.id.containerCollaborations);
                containerCollaborations.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        onUserStatsClickedListener.onUserStatsClicked(COLLABORATIONS, containerCollaborations);
                    }
                });
                // set tag for collaborations
                TextView collaborationsCount = view.findViewById(R.id.textCollaborationsCount);
                collaborationsCount.setTag(COLLABORATIONS);
                break;
        }
    }
}
