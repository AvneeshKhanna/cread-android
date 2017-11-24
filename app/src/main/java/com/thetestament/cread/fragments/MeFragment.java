package com.thetestament.cread.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.BottomNavigationActivity;
import com.thetestament.cread.activities.FollowActivity;
import com.thetestament.cread.activities.UpdateProfileDetailsActivity;
import com.thetestament.cread.activities.UpdateProfileImageActivity;
import com.thetestament.cread.adapters.MeAdapter;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getUserDataObservableFromServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_REQUESTED_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_BIO;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_CONTACT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_EMAIL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_FIRST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_IMAGE_PATH;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_LAST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_WATER_MARK_STATUS;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_PROFILE;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_DETAILS;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_PIC;

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
    @BindView(R.id.viewNoData)
    LinearLayout viewNoData;
    @BindView(R.id.progressView)
    View progressView;

    @State
    String mFirstName, mLastName, mProfilePicURL, mUserBio;
    @State
    long mPostCount, mFollowerCount, mFollowingCount;
    @State
    String mEmail, mContactNumber, mWaterMarkStatus;
    @State
    boolean mFollowStatus, isProfileEditable;
    @State
    String mRequestedUUID;

    @State
    String mShortID;

    private List<FeedModel> mUserActivityDataList = new ArrayList<>();
    private MeAdapter mAdapter;
    private Unbinder mUnbinder;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private SharedPreferenceHelper mHelper;
    private String mLastIndexKey;
    private boolean mRequestMoreData;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Initialize preference helper
        mHelper = new SharedPreferenceHelper(getActivity());
        //Inflate this view
        return inflater.inflate(R.layout.fragment_me
                , container
                , false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //For view binding
        mUnbinder = ButterKnife.bind(this, view);
        //For smooth scrolling
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);
        //initialize this screen
        initScreen();
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
        //Required for permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    ImageHelper.startImageCropping(getContext(), this, data.getData(), getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));

                } else {
                    ViewHelper.getSnackBar(rootView, "Image from gallery was not attached");
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);
                    ImageHelper.processCroppedImage(mCroppedImgUri, getActivity(), rootView, mShortID);

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;
            case REQUEST_CODE_UPDATE_PROFILE_PIC:
                if (resultCode == RESULT_OK) {
                    mProfilePicURL = data.getExtras().getString(EXTRA_USER_IMAGE_PATH);
                    //load user profile
                    loadUserPicture(mProfilePicURL, imageUser, getActivity());
                }
                break;
            case REQUEST_CODE_UPDATE_PROFILE_DETAILS:
                if (resultCode == RESULT_OK) {
                    //Get user first name
                    mFirstName = data.getExtras().getString(EXTRA_USER_FIRST_NAME);
                    //if last name is present
                    if (data.getExtras().getString(EXTRA_USER_LAST_NAME) != null) {
                        //Get user last name
                        mLastName = data.getExtras().getString(EXTRA_USER_LAST_NAME);
                        //Set user name
                        textUserName.setText(mFirstName + " " + mLastName);
                    } else {
                        //set user name
                        textUserName.setText(mFirstName);
                    }

                    //If user bio present
                    if (data.getExtras().getString(EXTRA_USER_BIO) != null) {
                        //Get user bio
                        mUserBio = data.getExtras().getString(EXTRA_USER_BIO);
                        //Set user bio
                        textBio.setText(mUserBio);
                    } else {
                        //Set user bio
                        textBio.setText("Write what describes you");
                    }

                    //Retrieve email and watermark status
                    mEmail = data.getExtras().getString(EXTRA_USER_EMAIL);
                    mWaterMarkStatus = data.getExtras().getString(EXTRA_USER_WATER_MARK_STATUS);
                }
                break;
        }

    }

    /**
     * User image click functionality to launch screen where user can edit his/her profile picture.
     */
    @OnClick(R.id.imageUser)
    public void onUserImageClicked() {
        //If profile is editable
        if (isProfileEditable) {
            getRuntimePermission();
        }
    }

    /**
     * Click functionality to open screen where user can edit his/her profile details.
     */
    @OnClick(R.id.textUserName)
    public void onUserNameClicked() {
        if (isProfileEditable) {
            Intent intent = new Intent(getActivity(), UpdateProfileDetailsActivity.class);
            intent.putExtra(EXTRA_USER_FIRST_NAME, mFirstName);
            intent.putExtra(EXTRA_USER_LAST_NAME, mLastName);
            intent.putExtra(EXTRA_USER_EMAIL, mEmail);
            intent.putExtra(EXTRA_USER_BIO, mUserBio);
            intent.putExtra(EXTRA_USER_CONTACT, mContactNumber);
            intent.putExtra(EXTRA_USER_WATER_MARK_STATUS, mWaterMarkStatus);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_PROFILE_DETAILS);
        }
    }

    /**
     * Click functionality to edit user bio
     */
    @OnClick(R.id.textBio)
    public void onUserBioClicked() {
        if (isProfileEditable) {
            showBioInputDialog();
        }
    }


    /**
     * Follow button click functionality to follow or un-follow.
     */
    @OnClick(R.id.buttonFollow)
    public void onFollowButtonClicked() {

        // check net status
        if (NetworkHelper.getNetConnectionStatus(getActivity())) {
            //Disable follow button
            buttonFollow.setEnabled(false);
            //set status to true if its false and vice versa
            mFollowStatus = !mFollowStatus;
            //toggle follow button
            toggleFollowButton(mFollowStatus, getActivity());
            //Update status on server
            updateFollowStatus();

            //Log firebase event
            Bundle bundle = new Bundle();
            bundle.putString("uuid", mHelper.getUUID());
            FirebaseAnalytics.getInstance(getActivity()).logEvent(FIREBASE_EVENT_FOLLOW_FROM_PROFILE, bundle);
        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * Click functionality to launch screen where user can see list of people whom he/she is following.
     */
    @OnClick(R.id.containerFollowing)
    public void onFollowingContainerClicked() {

        if (mFollowingCount > 0) {
            Intent intent = new Intent(getActivity(), FollowActivity.class);
            intent.putExtra(EXTRA_FOLLOW_REQUESTED_UUID, mRequestedUUID);
            intent.putExtra(EXTRA_FOLLOW_TYPE, "following");
            startActivity(intent);
        } else {
            ViewHelper.getSnackBar(rootView, "User is not following anyone");
        }
    }

    /**
     * Click functionality to launch followers screen.
     */
    @OnClick(R.id.containerFollowers)
    public void onFollowersContainerClicked() {
        if (mFollowerCount > 0) {
            Intent intent = new Intent(getActivity(), FollowActivity.class);
            intent.putExtra(EXTRA_FOLLOW_REQUESTED_UUID, mRequestedUUID);
            intent.putExtra(EXTRA_FOLLOW_TYPE, "followers");
            startActivity(intent);
        } else {
            ViewHelper.getSnackBar(rootView, "No followers");
        }
    }

    /**
     * PostContainer click functionality.
     */
    @OnClick(R.id.containerPosts)
    void onPostsContainerClicked() {
    }

    /*
    * Create button click functionality.
    * */
    @OnClick(R.id.buttonCreate)
    void onCreateClick() {
        ((BottomNavigationActivity) getActivity()).getAddContentBottomSheetDialog();
    }


    /**
     * Method to initialize views for this screen.
     */
    private void initScreen() {
        //Retrieve data from bundle
        String calledFrom = getArguments().getString("calledFrom");
        //if this screen is opened from BottomNavigationActivity
        if (calledFrom.equals("BottomNavigationActivity")) {
            mRequestedUUID = mHelper.getUUID();
            //Enable profile editing
            isProfileEditable = true;
        } else {
            mRequestedUUID = getArguments().getString("requesteduuid");
            //Disable profile editing
            isProfileEditable = false;
        }

        //Condition to  toggle visibility of follow button
        if (mHelper.getUUID().equals(mRequestedUUID)) {
            //Hide follow button
            buttonFollow.setVisibility(View.GONE);
        } else {
            //Show follow button
            buttonFollow.setVisibility(View.VISIBLE);
        }
        //initialize tab layout
        initTabLayout(tabLayout);
        initSwipeRefreshLayout();
    }

    /**
     * Method to initialize tab layout.
     *
     * @param tabLayout TabLayout.
     */
    private void initTabLayout(TabLayout tabLayout) {
        //SetUp tabs
        setUpTabs(tabLayout);
        //Listener for tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //To change tab icon color from grey to primary color
                tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                switch (tab.getPosition()) {
                    case 0:
                        mAdapter.updateList(mUserActivityDataList);
                        break;
                    case 1:
                        List<FeedModel> temp = new ArrayList<>();
                        for (FeedModel f : mUserActivityDataList) {
                            if (f.getContentType().equals(CONTENT_TYPE_SHORT)) {
                                temp.add(f);
                            }
                        }
                        mAdapter.updateList(temp);
                        break;
                    case 2:
                        List<FeedModel> temp1 = new ArrayList<>();
                        for (FeedModel f : mUserActivityDataList) {
                            if (f.getContentType().equals(CONTENT_TYPE_CAPTURE)) {
                                temp1.add(f);
                            }
                        }
                        mAdapter.updateList(temp1);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //To change tab icon color from primary color to grey
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
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_short_tab_24));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_camera_tab_24));
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
            getUserTimeLineData();
        } else {
            swipeToRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving user profile data from server.
     */
    private void getUserProfileData() {
        swipeToRefreshLayout.setRefreshing(true);
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getUserDataObservableFromServer(BuildConfig.URL + "/user-profile/load-profile"
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
                                mUserBio = mainData.getString("bio");
                                mFollowStatus = mainData.getBoolean("followstatus");
                                mPostCount = mainData.getLong("postcount");
                                mFollowerCount = mainData.getLong("followercount");
                                mFollowingCount = mainData.getLong("followingcount");
                                mEmail = mainData.getString("email");
                                mContactNumber = mainData.getString("phone");
                                mWaterMarkStatus = mainData.getString("watermarkstatus");
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
                        //Dismiss progress indicator
                        swipeToRefreshLayout.setRefreshing(false);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
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

                            //Set user activity stats
                            textPostsCount.setText(String.valueOf(mPostCount));
                            textFollowersCount.setText(String.valueOf(mFollowerCount));
                            textFollowingCount.setText(String.valueOf(mFollowingCount));

                            //If user bio present
                            if (mUserBio != null && !mUserBio.isEmpty() && !mUserBio.equals("null")) {
                                textBio.setVisibility(View.VISIBLE);
                                //Set user bio
                                textBio.setText(mUserBio);
                            } else {
                                mUserBio = "Write what describes you";
                                //Hide for other users
                                if (!isProfileEditable) {
                                    textBio.setVisibility(View.GONE);
                                }
                            }

                            //Toggle follow button
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
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to toggle follow button text color , text and background.
     *
     * @param followStatus true if following false otherwise.
     * @param context      Context to use
     */
    private void toggleFollowButton(boolean followStatus, Context context) {
        if (followStatus) {
            ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_outline));
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.grey_dark));
            //Change text to 'following'
            buttonFollow.setText("Following");
        } else {
            //Change background
            ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_filled));
            //Change text color
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.white));
            //Change text to 'follow'
            buttonFollow.setText("Follow");
        }
    }

    /**
     * Method to get WRITE_EXTERNAL_STORAGE permission and perform specified operation.
     */
    private void getRuntimePermission() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            openUpdateImageScreen();
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(MeFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , "Please grant storage permission from settings to edit your profile picture.");
            } else {
                Nammu.askForPermission(MeFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, profilePicWritePermission);
            }
        }
    }

    /**
     * Open UpdateProfileImageActivity screen.
     */
    private void openUpdateImageScreen() {
        Intent intent = new Intent(getActivity(), UpdateProfileImageActivity.class);
        intent.putExtra(EXTRA_USER_IMAGE_PATH, mProfilePicURL);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_PROFILE_PIC);
    }

    /**
     * Method to update follow status.
     */
    private void updateFollowStatus() {
        final JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(mRequestedUUID);

            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("register", mFollowStatus);
            jsonObject.put("followees", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        Rx2AndroidNetworking.post(BuildConfig.URL + "/follow/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        //Enable follow button
                        buttonFollow.setEnabled(true);
                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //set status to true if its false and vice versa
                                mFollowStatus = !mFollowStatus;
                                //toggle follow button
                                toggleFollowButton(mFollowStatus, getActivity());
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    if (mFollowStatus) {
                                        //Increase count by one
                                        mFollowerCount += 1;
                                        //Set count
                                        textFollowersCount.setText(String.valueOf(mFollowerCount));
                                    } else {
                                        //Decrease count by one
                                        mFollowerCount -= 1;
                                        //Set count
                                        textFollowersCount.setText(String.valueOf(mFollowerCount));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            //set status to true if its false and vice versa
                            mFollowStatus = !mFollowStatus;
                            //toggle follow button
                            toggleFollowButton(mFollowStatus, getActivity());
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        //Enable follow button
                        buttonFollow.setEnabled(true);
                        //set status to true if its false and vice versa
                        mFollowStatus = !mFollowStatus;
                        //toggle follow button
                        toggleFollowButton(mFollowStatus, getActivity());
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Do nothing
                    }
                });

    }

    /**
     * Method to initialize swipe to refresh view and user timeline view .
     */
    private void initSwipeRefreshLayout() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new MeAdapter(mUserActivityDataList, getActivity(), mHelper.getUUID());
        //  mAdapter.setUserActivityType(USER_ACTIVITY_TYPE_ALL);
        recyclerView.setAdapter(mAdapter);

        swipeToRefreshLayout.setRefreshing(true);
        swipeToRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mUserActivityDataList.clear();

                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to null
                mLastIndexKey = null;
                //Load data here
                getUserTimeLineData();
            }
        });
        //Load profile data
        loadProfileData();
        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initHatsOffListener(mAdapter);
        initializeDeleteListener(mAdapter);
        initCaptureListener(mAdapter);
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
                , mLastIndexKey)
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
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //UserActivity array list
                                JSONArray UserActivityArray = mainData.getJSONArray("items");
                                for (int i = 0; i < UserActivityArray.length(); i++) {
                                    JSONObject dataObj = UserActivityArray.getJSONObject(i);
                                    String type = dataObj.getString("type");

                                    FeedModel data = new FeedModel();
                                    data.setEntityID(dataObj.getString("entityid"));
                                    data.setContentType(dataObj.getString("type"));
                                    data.setUUID(dataObj.getString("uuid"));
                                    data.setCreatorImage(dataObj.getString("profilepicurl"));
                                    data.setCreatorName(dataObj.getString("creatorname"));
                                    data.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    data.setMerchantable(dataObj.getBoolean("merchantable"));
                                    data.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    data.setCommentCount(dataObj.getLong("commentcount"));
                                    data.setContentImage(dataObj.getString("entityurl"));
                                    data.setCollabCount(dataObj.getLong("collabcount"));

                                    if (type.equals(CONTENT_TYPE_CAPTURE)) {

                                        //Retrieve "CAPTURE_ID" if type is capture
                                        data.setCaptureID(dataObj.getString("captureid"));
                                        // if capture
                                        // then if key cpshort exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("cpshort")) {
                                            JSONObject collabObject = dataObj.getJSONObject("cpshort");

                                            data.setAvailableForCollab(false);
                                            // set collaborator details
                                            data.setCollabWithUUID(collabObject.getString("uuid"));
                                            data.setCollabWithName(collabObject.getString("name"));

                                        } else {
                                            data.setAvailableForCollab(true);
                                        }

                                    } else if (type.equals(CONTENT_TYPE_SHORT)) {

                                        //Retrieve "SHORT_ID" if type is short
                                        data.setShortID(dataObj.getString("shoid"));

                                        // if short
                                        // then if key shcapture exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("shcapture")) {

                                            JSONObject collabObject = dataObj.getJSONObject("shcapture");

                                            data.setAvailableForCollab(false);
                                            // set collaborator details
                                            data.setCollabWithUUID(collabObject.getString("uuid"));
                                            data.setCollabWithName(collabObject.getString("name"));
                                        } else {
                                            data.setAvailableForCollab(true);
                                        }
                                    }


                                    mUserActivityDataList.add(data);
                                }
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
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        swipeToRefreshLayout.setRefreshing(false);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mUserActivityDataList.size() == 0) {
                            //Show no data view
                            if (isProfileEditable) {
                                viewNoData.setVisibility(View.VISIBLE);
                            } else {
                                //Hide no data view
                                viewNoData.setVisibility(View.GONE);
                            }
                            ViewHelper.getSnackBar(rootView, "User hasn't uploaded anything yet");
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerView.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                            //Hide no data view
                            viewNoData.setVisibility(View.GONE);
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
    private void initLoadMoreListener(MeAdapter adapter) {

        adapter.setUserActivityLoadMoreListener(new listener.OnUserActivityLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (mRequestMoreData) {

                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mUserActivityDataList.add(null);
                                               mAdapter.notifyItemInserted(mUserActivityDataList.size() - 1);
                                           }
                                       }
                    );
                    //Load new set of data
                    getUserTimeLineNextData();
                }
            }
        });
    }

    /**
     * RxJava2 implementation for retrieving next set of user timeline data from server.
     */
    private void getUserTimeLineNextData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/user-profile/load-timeline"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mRequestedUUID
                , mLastIndexKey)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mUserActivityDataList.remove(mUserActivityDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mUserActivityDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //UserActivity array list
                                JSONArray UserActivityArray = mainData.getJSONArray("items");
                                for (int i = 0; i < UserActivityArray.length(); i++) {
                                    JSONObject dataObj = UserActivityArray.getJSONObject(i);
                                    String type = dataObj.getString("type");

                                    FeedModel data = new FeedModel();
                                    data.setEntityID(dataObj.getString("entityid"));
                                    data.setContentType(dataObj.getString("type"));
                                    data.setUUID(dataObj.getString("uuid"));
                                    data.setCreatorImage(dataObj.getString("profilepicurl"));
                                    data.setCreatorName(dataObj.getString("creatorname"));
                                    data.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    data.setMerchantable(dataObj.getBoolean("merchantable"));
                                    data.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    data.setCommentCount(dataObj.getLong("commentcount"));
                                    data.setContentImage(dataObj.getString("entityurl"));
                                    data.setCollabCount(dataObj.getLong("collabcount"));


                                    if (type.equals(CONTENT_TYPE_CAPTURE)) {

                                        //Retrieve "CAPTURE_ID" if type is capture
                                        data.setCaptureID(dataObj.getString("captureid"));
                                        // if capture
                                        // then if key cpshort exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("cpshort")) {
                                            JSONObject collabObject = dataObj.getJSONObject("cpshort");

                                            data.setAvailableForCollab(false);
                                            // set collaborator details
                                            data.setCollabWithUUID(collabObject.getString("uuid"));
                                            data.setCollabWithName(collabObject.getString("name"));

                                        } else {
                                            data.setAvailableForCollab(true);
                                        }

                                    } else if (type.equals(CONTENT_TYPE_SHORT)) {


                                        //Retrieve "SHORT_ID" if type is short
                                        data.setShortID(dataObj.getString("shoid"));// if short
                                        // then if key shcapture exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("shcapture")) {

                                            JSONObject collabObject = dataObj.getJSONObject("shcapture");

                                            data.setAvailableForCollab(false);
                                            // set collaborator details
                                            data.setCollabWithUUID(collabObject.getString("uuid"));
                                            data.setCollabWithName(collabObject.getString("name"));
                                        } else {
                                            data.setAvailableForCollab(true);
                                        }
                                    }

                                    mUserActivityDataList.add(data);
                                    //Notify item insertion
                                    mAdapter.notifyItemInserted(mUserActivityDataList.size() - 1);

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mUserActivityDataList.remove(mUserActivityDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mUserActivityDataList.size());
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                        //No data
                        else if (mUserActivityDataList.size() == 0) {
                            ViewHelper.getSnackBar(rootView, "Nothing to show");
                        } else {
                            //Notify changes
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }

    /**
     * Initialize hats off listener.
     *
     * @param adapter FeedAdapter reference.
     */
    private void initHatsOffListener(MeAdapter adapter) {
        adapter.setHatsOffListener(new listener.OnUserActivityHatsOffListener() {
            @Override
            public void onHatsOffClick(FeedModel data, int itemPosition) {
                updateHatsOffStatus(data, itemPosition);
            }
        });
    }

    /**
     * Method to update hats off status of campaign.
     *
     * @param data         Model of current item
     * @param itemPosition Position of current item i.e integer
     */
    private void updateHatsOffStatus(final FeedModel data, final int itemPosition) {

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("entityid", data.getEntityID());
            jsonObject.put("register", data.getHatsOffStatus());
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        AndroidNetworking.post(BuildConfig.URL + "/hatsoff/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Token status is not valid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                //set status to true if its false and vice versa
                                data.setHatsOffStatus(!data.getHatsOffStatus());
                                //notify changes
                                mAdapter.notifyItemChanged(itemPosition);
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Do nothing
                                }
                            }
                        } catch (JSONException e) {
                            //set status to true if its false and vice versa
                            data.setHatsOffStatus(!data.getHatsOffStatus());
                            //notify changes
                            mAdapter.notifyItemChanged(itemPosition);
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //set status to true if its false and vice versa
                        data.setHatsOffStatus(!data.getHatsOffStatus());
                        //notify changes
                        mAdapter.notifyItemChanged(itemPosition);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Initialize delete listener
     * * @param adapter MeAdapter reference.
     */
    private void initializeDeleteListener(MeAdapter meAdapter) {
        meAdapter.setOnContentDeleteListener(new listener.OnContentDeleteListener() {
            @Override
            public void onDelete(String entityID, int position) {

                // check net status
                if (NetworkHelper.getNetConnectionStatus(getActivity())) {
                    deleteContent(entityID, position);
                } else {
                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                }

            }
        });
    }

    /**
     * Method to delete content.
     *
     * @param entityID     Entity id of item to be deleted.
     * @param itemPosition Position of current item.
     */
    private void deleteContent(String entityID, final int itemPosition) {


        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title("Deleting")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("entityid", entityID);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            dialog.dismiss();
        }

        Rx2AndroidNetworking.post(BuildConfig.URL + "/entity-manage/delete")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        //Add disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    //Remove item from list
                                    mUserActivityDataList.remove(itemPosition);
                                    //Update adapter
                                    mAdapter.notifyItemRemoved(itemPosition);
                                    ViewHelper.getSnackBar(rootView, "Item deleted");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        //Dismiss dialog
                        dialog.dismiss();
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //do nothing
                    }
                });
    }

    /**
     * Initialize capture listener.
     *
     * @param meAdapter MeAdapter reference
     */
    private void initCaptureListener(MeAdapter meAdapter) {
        meAdapter.setOnMeCaptureClickListener(new listener.OnMeCaptureClickListener() {
            @Override
            public void onClick(String shortID) {
                //Set entity id
                mShortID = shortID;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    ImageHelper.chooseImageFromGallery(MeFragment.this);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(MeFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_capture_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(MeFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
                    }
                }
            }
        });
    }

    /**
     * Used to handle result of askForPermission for capture.
     */
    PermissionCallback captureWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //Select image from gallery
            ImageHelper.chooseImageFromGallery(MeFragment.this);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_capture_permission_denied));
        }
    };

    /**
     * Used to handle result of askForPermission for Profile pic.
     */
    PermissionCallback profilePicWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            openUpdateImageScreen();
        }

        @Override
        public void permissionRefused() {
            ViewHelper.getToast(getActivity()
                    , "Please grant storage permission from settings to edit your profile picture.");
        }
    };


    /**
     * Method to show input dialog where user enters his/her watermark.
     */
    private void showBioInputDialog() {
        new MaterialDialog.Builder(getActivity())
                .title("Write what describes you")
                .autoDismiss(false)
                .inputRange(1, 80, ContextCompat.getColor(getActivity(), R.color.red))
                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(null, mUserBio, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String s = String.valueOf(input).trim();
                        if (s.length() < 1) {
                            ViewHelper.getToast(getActivity(), "This field can't be empty");
                        } else {
                            //Dismiss
                            dialog.dismiss();
                            mUserBio = s;
                            //Save details to server
                            saveUserDetails();
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * Method to save user profile details on server.
     */
    public void saveUserDetails() {
        //Show progress view
        progressView.setVisibility(View.VISIBLE);

        JSONObject jsonObject = new JSONObject();
        JSONObject userObject = new JSONObject();

        try {
            //User data
            userObject.put("firstname", mFirstName);
            userObject.put("lastname", mLastName);
            userObject.put("email", mEmail);
            userObject.put("bio", mUserBio);
            userObject.put("watermarkstatus", mWaterMarkStatus);
            userObject.put("watermark", "");
            //Request data
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("userdata", userObject);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            progressView.setVisibility(View.GONE);
        }

        Rx2AndroidNetworking.post(BuildConfig.URL + "/user-profile/update-profile")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        //Dismiss progress indicator
                        progressView.setVisibility(View.GONE);
                        try {
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //Show token invalid status
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    //Update user bio text
                                    textBio.setText(mUserBio);
                                    //Show toast
                                    ViewHelper.getSnackBar(rootView, "Details saved");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            //Show error snack bar
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        //Dismiss progress indicator
                        progressView.setVisibility(View.GONE);
                        //Show server error message
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


}
