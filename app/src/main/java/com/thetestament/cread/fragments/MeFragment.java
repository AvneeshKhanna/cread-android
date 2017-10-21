package com.thetestament.cread.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.UpdateProfileImageActivity;
import com.thetestament.cread.adapters.FeedAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_WRITE_EXTERNAL_STORAGE;

/**
 * Fragment class to load user profile details and his/her recent activity.
 */

public class MeFragment extends Fragment {


    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeToRefreshLayout;
    @BindView(R.id.imageUser)
    CircleImageView imageUser;
    @BindView(R.id.textUserName)
    TextView textUserName;
    @BindView(R.id.textBio)
    TextView textBio;
    @BindView(R.id.buttonFollow)
    TextView buttonFollow;
    @BindView(R.id.textPostsCount)
    TextView textPostsCount;
    @BindView(R.id.textFollowersCount)
    TextView textFollowersCount;
    @BindView(R.id.textFollowingCount)
    TextView textFollowingCount;
    @State
    String mFirstName, mLastName, mProfilePicURL, mUserBio;
    @State
    String mEmail, mContactNumber, mPostCount, mFollowerCount, mFollowingCount;
    @State
    boolean mFollowStatus;
    @State
    String mRequestedUUID;
    List<FeedModel> mFeedDataList = new ArrayList<>();
    FeedAdapter mAdapter;
    private Unbinder mUnbinder;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private SharedPreferenceHelper mHelper;
    private int mPageNumber = 0;
    private boolean mRequestMoreData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate this view
        View view = inflater
                .inflate(R.layout.fragment_me
                        , container
                        , false);
        mUnbinder = ButterKnife.bind(this, view);
        //initialize preference helper
        mHelper = new SharedPreferenceHelper(getActivity());
        //Return this view
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //For smooth scrolling
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

        //Retrieve data from bundle
        String calledFrom = getArguments().getString("calledFrom");
        //if this screen is opened from BottomNavigationActivity
        if (calledFrom.equals("BottomNavigationActivity")) {
            mRequestedUUID = mHelper.getUUID();
            //Hide follow button
            buttonFollow.setVisibility(View.GONE);
        } else {
            mRequestedUUID = getArguments().getString("requesteduuid");
        }

        //initialize tab layout
        initTabLayout(tabLayout);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(getActivity(), UpdateProfileImageActivity.class)
                        , REQUEST_CODE_UPDATE_PROFILE_PIC);
            } else {
                ViewHelper.getToast(getActivity()
                        , "The app won't function properly since the permission for storage was denied.");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * User image click functionality to launch screen where user can edit his/her profile picture i.e DP .
     */
    @OnClick(R.id.imageUser)
    public void onUserImageClicked() {
        getRuntimePermission();
    }

    /**
     * Click functionality to open screen where user can edit his/her profile details.
     */
    @OnClick({R.id.textUserName, R.id.textBio})
    public void onUserNameClicked() {
        //Todo functionality
    }

    /**
     * Follow button click functionality to follow or un-follow.
     */
    @OnClick(R.id.buttonFollow)
    public void onFollowButtonClicked() {
        //// TODO: follow functionality
    }

    /**
     * Click functionality to launch screen where user can see list of people whom he/she is following.
     */
    @OnClick(R.id.containerFollowing)
    public void onFollowingContainerClicked() {
    }

    /**
     * Click functionality to launch followers screen.
     */
    @OnClick(R.id.containerFollowers)
    public void onFollowersContainerClicked() {
    }

    /**
     * PostContainer click functionality.
     */
    @OnClick(R.id.containerPosts)
    public void onPostsContainerClicked() {
    }

    /**
     * Method to initialize tab layout.
     *
     * @param tabLayout TabLayout
     */
    private void initTabLayout(TabLayout tabLayout) {
        loadProfileData();
        //setUp tabs here
        setUpTabs(tabLayout);
        //Listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                //To change the icon color from grey to primary color
                tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                //To change the icon color from primary color to grey
                tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                //do nothing
            }
        });
    }

    /**
     * Method to add tab items to tabLayout.
     *
     * @param tabLayout TabLayout where item to be added
     */
    private void setUpTabs(TabLayout tabLayout) {
        //Add tab items
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_apps_24));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_create_24));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_camera_alt_24));
        //initialize tabs icon tint
        tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
    }


    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadProfileData() {
        // if user device is connected to net
        if (getNetConnectionStatus(getActivity())) {
            swipeToRefreshLayout.setRefreshing(true);
            //Get user profile data from server
            getUserProfileData();
            //Get user timeline data from server
            //getUserTimeLineData();
        } else {
            swipeToRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView
                    , getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving user profile data from server.
     */
    private void getUserProfileData() {
        swipeToRefreshLayout.setRefreshing(true);
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/user-profile/load-profile"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mRequestedUUID)

                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mFirstName = mainData.getString("firstname");
                                mLastName = mainData.getString("lastname");
                                mProfilePicURL = mainData.getString("profilepicurl");
                                //     mUserBio = mainData.getString("bio");
                                mEmail = mainData.getString("email");
                                mContactNumber = mainData.getString("phone");
                                mFollowStatus = mainData.getBoolean("followstatus");
                                mPostCount = mainData.getString("postcount");
                                mFollowerCount = mainData.getString("followercount");
                                mFollowingCount = mainData.getString("followingcount");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeToRefreshLayout.setRefreshing(false);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_invalid_token));
                            //Dismiss progress indicator
                            swipeToRefreshLayout.setRefreshing(false);
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                            //Dismiss progress indicator
                            swipeToRefreshLayout.setRefreshing(false);
                        } else {
                            //Dismiss progress indicator
                            swipeToRefreshLayout.setRefreshing(false);
                            //Load user profile picture
                            loadUserPicture(mProfilePicURL, imageUser, getActivity());

                            //if last name is present
                            if (mLastName != null) {
                                //Set user name
                                textUserName.setText(mFirstName + " " + mLastName);
                            } else {
                                //set user name
                                textUserName.setText(mFirstName);
                            }

                            //Set user stats
                            textPostsCount.setText(mPostCount);
                            textFollowersCount.setText(mFollowerCount);
                            textFollowingCount.setText(mFollowerCount);

                            //If user bio present
                            if (mUserBio != null) {
                                //Set user bio
                                textBio.setText(mUserBio);
                            }
                            //Hide bio view
                            else {
                                textBio.setVisibility(View.GONE);
                            }
                            toggleFollowButton(mFollowStatus, getActivity());
                            appBarLayout.setVisibility(View.VISIBLE);
                        }
                    }
                })
        );
    }

    /**
     * Method to load user profile picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     * @param context   Context to be use.
     */
    private void loadUserPicture(String picUrl, CircleImageView imageView, Context context) {
        Picasso.with(context)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to toggle follow.
     *
     * @param followStatus true if following false otherwise.
     */
    private void toggleFollowButton(boolean followStatus, Context context) {
        if (followStatus) {
            ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_outline));
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.grey_dark));
        } else {
            //Change background
            ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_filled));
            //Change text color
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.white));
            //Change text
            buttonFollow.setText("Follwi");
        }
    }


    /**
     * Method to initialize swipe to refresh view and user timeline view .
     */
    private void initUserTimeline() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new FeedAdapter(mFeedDataList, getActivity());
        recyclerView.setAdapter(mAdapter);

        swipeToRefreshLayout.setRefreshing(true);
        swipeToRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mFeedDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set page count to zero
                mPageNumber = 0;
                //Load data here
                getUserTimeLineData();
            }
        });
        //Load profile data
        loadProfileData();

        //Initialize listener
        initLoadMoreListener(mAdapter);
    }

    /**
     * RxJava2 implementation for retrieving user timeline data from server.
     */
    private void getUserTimeLineData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/user-profile/load-timeline"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mRequestedUUID
                , 0)

                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mFirstName = mainData.getString("firstname");
                                mLastName = mainData.getString("lastname");
                                mProfilePicURL = mainData.getString("profilepicurl");
                                mUserBio = mainData.getString("bio");
                                mEmail = mainData.getString("email");
                                mContactNumber = mainData.getString("phone");
                                mFollowStatus = mainData.getBoolean("followstatus");
                                mPostCount = mainData.getString("postcount");
                                mFollowerCount = mainData.getString("followercount");
                                mFollowingCount = mainData.getString("followingcount");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeToRefreshLayout.setRefreshing(false);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_invalid_token));
                            //Dismiss progress indicator
                            swipeToRefreshLayout.setRefreshing(false);
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                            //Dismiss progress indicator
                            swipeToRefreshLayout.setRefreshing(false);
                        } else {
                            //Dismiss progress indicator
                            swipeToRefreshLayout.setRefreshing(false);
                            //Load user profile picture
                            loadUserPicture(mProfilePicURL, imageUser, getActivity());

                            //if last name is present
                            if (mLastName != null) {
                                //Set user name
                                textUserName.setText(mFirstName + " " + mLastName);
                            } else {
                                //set user name
                                textUserName.setText(mFirstName);
                            }

                            //Set user stats
                            textPostsCount.setText(mPostCount);
                            textFollowersCount.setText(mFollowerCount);
                            textFollowingCount.setText(mFollowerCount);

                            //If user bio present
                            if (mUserBio != null) {
                                //Set user bio
                                textBio.setText(mUserBio);
                            }
                            //Hide bio view
                            else {
                                textBio.setVisibility(View.GONE);
                            }
                            toggleFollowButton(mFollowStatus, getActivity());
                            appBarLayout.setVisibility(View.VISIBLE);
                        }
                    }
                })
        );
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter FeedAdapter reference.
     */
    private void initLoadMoreListener(FeedAdapter adapter) {

        adapter.setOnFeedLoadMoreListener(new listener.OnFeedLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (mRequestMoreData) {

                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mFeedDataList.add(null);
                                               mAdapter.notifyItemInserted(mFeedDataList.size() - 1);
                                           }
                                       }
                    );
                    //Increment page counter
                    mPageNumber += 1;
                    //Load new set of data
                    //loadMoreData();
                }
            }
        });
    }

    /**
     * Method to get WRITE_EXTERNAL_STORAGE permission and perform specified operation.
     */
    private void getRuntimePermission() {
        //Check for WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(getActivity()
                , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity()
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ViewHelper.getToast(getActivity()
                        , "Please grant storage permission from settings to edit your profile picture.");
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            }
        }
        //If permission is granted
        else {
            startActivityForResult(new Intent(getActivity(), UpdateProfileImageActivity.class)
                    , REQUEST_CODE_UPDATE_PROFILE_PIC);
        }
    }

}
