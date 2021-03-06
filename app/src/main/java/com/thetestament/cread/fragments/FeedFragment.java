package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.BottomNavigationActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.FindFBFriendsActivity;
import com.thetestament.cread.activities.RecommendedArtistsActivity;
import com.thetestament.cread.activities.SearchActivity;
import com.thetestament.cread.adapters.FeedAdapter;
import com.thetestament.cread.adapters.SuggestedArtistsAdapter;
import com.thetestament.cread.helpers.DownvoteHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.GifHelper;
import com.thetestament.cread.helpers.HashTagOfTheDayHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.SuggestedArtistsModel;
import com.thetestament.cread.networkmanager.FeedNetworkManager;
import com.thetestament.cread.networkmanager.HatsOffNetworkManger;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;
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
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.helpers.DeepLinkHelper.getDeepLinkOnValidShareOption;
import static com.thetestament.cread.helpers.FeedHelper.parseEntitySpecificJSON;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.processCroppedImage;
import static com.thetestament.cread.helpers.NetworkHelper.getEntitySpecificObservable;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_DEEP_LINK_USED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_EXPLORE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FIND_FRIENDS;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED_ADAPTER;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_USER_PROFILE_FROM_FEED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_USER_PROFILE_FROM_SUGGESTED_ADAPTER;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;

/**
 * Fragment class to show posts of followed users.
 */
public class FeedFragment extends Fragment implements listener.OnCollaborationListener {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.recyclerViewRecommendedArtists)
    RecyclerView recyclerViewRecommendedArtists;
    @BindView(R.id.layout_hash_tag_of_the_day)
    View viewHashTagOfTheDay;
    @BindView(R.id.txt_hash_tag_of_the_day)
    AppCompatTextView textHashTagOfTheDay;
    @BindView(R.id.badge_new_posts)
    AppCompatTextView newPostIndicator;
    @BindView(R.id.layout_suggested_artists)
    View viewSuggestedArtists;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view_feed)
    RecyclerView recyclerViewFeed;
    @BindView(R.id.view_no_posts)
    LinearLayout viewNoPosts;


    //endregion

    //region :Fields and constants

    /**
     * Flag to maintain last index key for next set for data.
     */
    @State
    String mLastIndexKey;

    /**
     * Flag to maintain whether next set of data is available or not.
     */
    @State
    boolean mRequestMoreData;

    /**
     * Flag to maintain whether user has power of down vote or not.
     */
    @State
    boolean mCanDownVote;

    /**
     * CompositeDisposable for rx java.
     */
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    /**
     * List to store feed data.
     */
    List<FeedModel> mFeedDataList = new ArrayList<>();

    /**
     * FeedAdapter global reference.
     */
    FeedAdapter mAdapter;

    /**
     * Global reference of SharedPreferenceHelper.
     */
    SharedPreferenceHelper mHelper;

    /**
     * Unbinder reference for butter knife.
     */
    Unbinder mUnbinder;

    /**
     * Flag to maintain Hash tag of the day text.
     */
    @State
    String textHashTagOFTheDay = "";


    @State
    String mEntityID, mEntityType;
    Bitmap mBitmap;
    FeedModel entitySpecificData;

    @State
    String mShareOption = SHARE_OPTION_OTHER;


    /**
     * Parent view of live filter
     */
    FrameLayout mFrameLayout;
    RelativeLayout mWatermarkView;

    /**
     * Flag to maintain live filter value.
     */
    @State
    String mLiveFilter;

    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Its own option menu
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_feed
                , container
                , false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        //Method called
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mHelper.getHTagNewPostsIndicatorVisibility()) {
            newPostIndicator.setVisibility(View.GONE);
        } else {
            newPostIndicator.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Set Listener
        new FeedHelper().setOnCaptureClickListener(this);
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
                    ImageHelper.startImageCropping(getActivity()
                            , this
                            , data.getData()
                            , getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));
                } else {
                    ViewHelper.getSnackBar(rootView
                            , getString(R.string.error_img_not_attached));
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get image width and height
                    float width = data.getIntExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, 1800);
                    float height = data.getIntExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, 1800);

                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);
                    //Check for image manipulation
                    if (AspectRatioUtils.getSquareImageManipulation(width, height)) {
                        //Create square image with blurred background
                        ImageHelper.performSquareImageManipulation(mCroppedImgUri
                                , getActivity()
                                , rootView
                                , mEntityID
                                , mEntityType);
                    } else {
                        //Method called
                        processCroppedImage(mCroppedImgUri, getActivity(), rootView, mEntityID, mEntityType);
                    }

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView
                            , getString(R.string.error_img_not_cropped));
                }
                break;
            case REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getBundleExtra(EXTRA_DATA);
                    //Update data
                    mFeedDataList.get(bundle.getInt("position")).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                    mFeedDataList.get(bundle.getInt("position")).setDownvoteStatus(bundle.getBoolean("downvotestatus"));
                    mFeedDataList.get(bundle.getInt("position")).setHatsOffCount(bundle.getLong("hatsOffCount"));
                    //Notify changes
                    mAdapter.notifyItemChanged(bundle.getInt("position"));
                }
                break;
            case REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED:
            case REQUEST_CODE_USER_PROFILE_FROM_FEED:
                if (resultCode == RESULT_OK) {
                    //Refresh data
                    loadFeedData();
                }
                break;
            case REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED_ADAPTER:
            case REQUEST_CODE_USER_PROFILE_FROM_SUGGESTED_ADAPTER:
                if (resultCode == RESULT_OK) {
                    //Notify changes
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_feed, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //Start search activity
                startActivity(new Intent(getActivity(), SearchActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void collaborationOnGraphic() {

    }

    @Override
    public void collaborationOnWriting(String entityID, String entityType) {
        mEntityID = entityID;
        mEntityType = entityType;
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            ImageHelper.chooseImageFromGallery(FeedFragment.this);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(FeedFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_capture_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(FeedFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
            }
        }
    }
    //endregion

    //region :Click functionality

    /**
     * Click functionality to open FindFBFriend activity.
     */
    @OnClick(R.id.btn_fb_friends)
    public void onFindFBFriendsClicked() {
        //Open FindFBFriendsActivity screen
        startActivity(new Intent(getActivity(), FindFBFriendsActivity.class));
        //Log firebase event
        setAnalytics(FIREBASE_EVENT_FIND_FRIENDS);
    }

    /**
     * Click functionality to open explore fragment.
     */
    @OnClick(R.id.btn_explore)
    public void onExploreFriendsClicked() {
        ((BottomNavigationActivity) getActivity()).activateBottomNavigationItem(R.id.action_explore);
        ((BottomNavigationActivity) getActivity()).replaceFragment(new ExploreFragment(), Constant.TAG_EXPLORE_FRAGMENT, false);
        //Log firebase event
        setAnalytics(FIREBASE_EVENT_EXPLORE_CLICKED);
    }

    /**
     * <p>Click functionality to open a new screen where user can see list of artists whom he/she can follow.</p>
     */
    @OnClick(R.id.textShowMoreArtists)
    void onArtistMoreClick() {
        //Open RecommendedArtists Screen
        Intent intent = new Intent(getActivity(), RecommendedArtistsActivity.class);
        this.startActivityForResult(intent, REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED);
    }

    /**
     * Click functionality to show HashTagOgTheDay infoDialog
     */
    @OnClick(R.id.btn_info)
    void infoButtonOnClick() {
        //Show dialog here
        getHashTagOfTheDayInfoDialog(textHashTagOFTheDay);
    }

    //endregion

    //region :Private methods

    /**
     * Method to initialize views for this screen.
     */
    private void initViews() {
        //Obtain SharedPreference reference
        mHelper = new SharedPreferenceHelper(getActivity());
        //Method called
        initSwipeRefreshLayout();

        //This screen opened for first time
        if (mHelper.isWelcomeFirstTime()) {
            //Show welcome dialog and then check deep link status
            showWelcomeMessage();
        } else {
            // not the first time so check for deep link directly
            initDeepLink();
        }
    }

    /**
     * Method to initialize swipe to refresh view.
     */
    private void initSwipeRefreshLayout() {
        //Set layout manger for recyclerView
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new FeedAdapter(mFeedDataList, getActivity(), mHelper.getUUID(), FeedFragment.this, mCompositeDisposable);
        recyclerViewFeed.setAdapter(mAdapter);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mFeedDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to null
                mLastIndexKey = null;

                if (viewNoPosts != null) {
                    // hide no posts view
                    viewNoPosts.setVisibility(View.GONE);
                }
                //Load data here
                loadFeedData();
            }
        });

        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initHatsOffListener(mAdapter);
        initShareListener(mAdapter);
        initGifShareListener(mAdapter);
        initDownVoteListener(mAdapter);
        //Load data here
        loadFeedData();
    }


    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadFeedData() {
        // if user device is connected to net
        if (getNetConnectionStatus(getActivity())) {
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            //Hide no posts view
            viewNoPosts.setVisibility(View.GONE);
            getFeedData(false);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving feed data.
     *
     * @param isLoadMore True if called to load next set of data false otherwise.x
     */
    private void getFeedData(final boolean isLoadMore) {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(FeedNetworkManager.getFeedObservable(BuildConfig.URL + "/feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_MAIN)
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
                                parsePostsData(jsonObject, isLoadMore);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_MAIN = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mFeedDataList.size() == 0) {
                            //Show no post view
                            viewNoPosts.setVisibility(View.VISIBLE);
                            //Load Suggested artist data here
                            FeedNetworkManager.getSuggestedArtistData(getActivity(), mCompositeDisposable, new FeedNetworkManager.OnSuggestedArtistLoadListener() {
                                @Override
                                public void onSuccess(List<SuggestedArtistsModel> dataList) {
                                    //Toggle view visibility
                                    appBarLayout.setVisibility(View.VISIBLE);
                                    viewSuggestedArtists.setVisibility(View.VISIBLE);
                                    viewHashTagOfTheDay.setVisibility(View.GONE);
                                    //Show recommended artists view here
                                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    appBarLayout.setLayoutParams(params);
                                    //Set layout manager
                                    recyclerViewRecommendedArtists.setLayoutManager(new LinearLayoutManager(getActivity()
                                            , LinearLayoutManager.HORIZONTAL
                                            , false));
                                    //Set adapter
                                    recyclerViewRecommendedArtists.setAdapter(new SuggestedArtistsAdapter(dataList, getActivity()
                                            , FeedFragment.this
                                            , true));
                                }

                                @Override
                                public void onFailure(String errorMsg) {
                                    //Show error snack bar
                                    ViewHelper.getSnackBar(rootView, errorMsg);
                                }
                            });
                        } else {
                            //Hide recommended artist view
                            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                            params.height = 0;
                            appBarLayout.setLayoutParams(params);
                            //Method called
                            addSuggestedArtistData();
                            addHashTagOfTheDayItem();

                            //Apply 'Slide Up' animation
                            recyclerViewFeed.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity()
                                    , R.anim.layout_animation_from_bottom));
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }


    /**
     * Method to retrieve to next set of data from server.
     */
    private void loadMoreData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        mCompositeDisposable.add(FeedNetworkManager.getFeedObservable(BuildConfig.URL + "/feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_MAIN)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mFeedDataList.remove(mFeedDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mFeedDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                parsePostsData(jsonObject, true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mFeedDataList.remove(mFeedDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mFeedDataList.size());
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
                        } else {
                            //Notify changes
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }


    /**
     * Used to handle result of askForPermission for capture.
     */
    PermissionCallback captureWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //Select image from gallery
            ImageHelper.chooseImageFromGallery(FeedFragment.this);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_capture_permission_denied));
        }
    };


    /**
     * Initialize share listener.
     */
    private void initShareListener(FeedAdapter feedAdapter) {
        feedAdapter.setOnShareListener(new listener.OnShareListener() {
            @Override
            public void onShareClick(Bitmap bitmap, FeedModel data, String shareOption) {
                mBitmap = bitmap;
                entitySpecificData = data;
                mShareOption = shareOption;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    getDeepLinkOnValidShareOption(getActivity(),
                            mCompositeDisposable,
                            rootView,
                            mHelper.getUUID(),
                            mHelper.getAuthToken(),
                            entitySpecificData,
                            bitmap,
                            mShareOption);

                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(FeedFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_share_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(FeedFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, shareWritePermission);
                    }
                }
            }
        });
    }

    /**
     * Initialize gif share listener.
     */
    private void initGifShareListener(FeedAdapter feedAdapter) {
        feedAdapter.setOnGifShareListener(new listener.OnGifShareListener() {
            @Override
            public void onGifShareClick(FrameLayout frameLayout, String shareOption, RelativeLayout waterMarkView, String liveFilter) {
                mFrameLayout = frameLayout;
                mShareOption = shareOption;
                mWatermarkView = waterMarkView;
                mLiveFilter = liveFilter;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    new GifHelper(getActivity(), mBitmap, frameLayout, shareOption, true, waterMarkView, mLiveFilter)
                            .startHandlerTask(new Handler(), 0);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(FeedFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_share_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(FeedFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, shareGifPermission);
                    }
                }
            }
        });
    }

    /**
     * Initialize down vote listener.
     *
     * @param feedAdapter FeedAdapter reference
     */
    private void initDownVoteListener(FeedAdapter feedAdapter) {
        feedAdapter.setOnDownVoteClickedListener(new listener.OnDownVoteClickedListener() {
            @Override
            public void onDownVoteClicked(FeedModel data, int position, ImageView imageDownVote) {

                DownvoteHelper downVoteHelper = new DownvoteHelper();

                // if already downVoted
                if (data.isDownvoteStatus()) {
                    downVoteHelper.initDownvoteProcess(getActivity()
                            , data
                            , mCompositeDisposable
                            , imageDownVote
                            , new Bundle()
                            , new Intent());
                } else

                {   // Show warning dialog
                    downVoteHelper.initDownvoteWarningDialog(getActivity()
                            , data
                            , mCompositeDisposable
                            , imageDownVote
                            , new Bundle()
                            , new Intent());
                }
            }
        });
    }

    /**
     * Initialize deep link functionality.
     */
    private void initDeepLink() {
        // if deep link parse it
        if (mHelper.getDeepLink() != null) {
            Uri deepLinkUri = Uri.parse(mHelper.getDeepLink());
            // set to null
            mHelper.setDeepLink(null);

            String entityID = deepLinkUri.getQueryParameter("entityid");
            // if not null
            // then redirect to entity specific screen
            if (entityID != null) {
                // get share source from deep link
                String shareSource = deepLinkUri.getQueryParameter("share_source");

                Bundle bundle = new Bundle();
                bundle.putString("share_source", shareSource);
                // log event
                FirebaseAnalytics
                        .getInstance(getContext())
                        .logEvent(FIREBASE_EVENT_DEEP_LINK_USED, bundle);

                getFeedDetails(entityID);
            }

        }
    }

    /**
     * Used to handle result of askForPermission for share
     */
    PermissionCallback shareWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            getDeepLinkOnValidShareOption(getActivity(),
                    mCompositeDisposable,
                    rootView,
                    mHelper.getUUID(),
                    mHelper.getAuthToken(),
                    entitySpecificData,
                    mBitmap,
                    mShareOption);

        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };

    /**
     * Used to handle result of askForPermission for gif sharing
     */
    PermissionCallback shareGifPermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //We have permission do whatever you want to do
            new GifHelper(getActivity(), mBitmap, mFrameLayout, mShareOption, true, mWatermarkView, mLiveFilter)
                    .startHandlerTask(new Handler(), 0);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };


    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent) {

        Bundle bundle = new Bundle();
        bundle.putString("uuid", mHelper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_FIND_FRIENDS)) {
            FirebaseAnalytics.getInstance(getActivity()).logEvent(FIREBASE_EVENT_FIND_FRIENDS, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_EXPLORE_CLICKED)) {
            FirebaseAnalytics.getInstance(getActivity()).logEvent(FIREBASE_EVENT_EXPLORE_CLICKED, bundle);
        }

    }


    /**
     * RxJava2 implementation for retrieving feed details
     *
     * @param entityID
     */
    private void getFeedDetails(final String entityID) {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(getActivity());

        requestServer(mCompositeDisposable,
                getEntitySpecificObservable(spHelper.getUUID(),
                        spHelper.getAuthToken(),
                        entityID),
                getActivity(),
                new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                entitySpecificData = parseEntitySpecificJSON(jsonObject, entityID);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "FeedFragment");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "FeedFragment");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {
                        GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));

                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, entitySpecificData);
                            bundle.putInt("position", -1);

                            Intent intent = new Intent(getActivity(), FeedDescriptionActivity.class);
                            intent.putExtra(EXTRA_DATA, bundle);
                            getActivity().startActivity(intent);

                            getActivity().finish();
                        }
                    }
                });
    }


    /**
     * Method to add suggested artist data.
     */
    private void addSuggestedArtistData() {
        //Code to add Suggested artists list
        int index;
        int dataSize = mFeedDataList.size();
        if (dataSize == 1 || dataSize == 2) {
            index = 1;
            mFeedDataList.add(index, new FeedModel());
            mAdapter.updateRecommendedArtistIndex(index);
        } else if (dataSize == 3 || dataSize == 4) {
            index = 2;
            mFeedDataList.add(index, new FeedModel());
            mAdapter.updateRecommendedArtistIndex(index);
        } else if (dataSize == 5 || dataSize == 6) {
            index = 3;
            mFeedDataList.add(index, new FeedModel());
            mAdapter.updateRecommendedArtistIndex(index);
        } else if (dataSize == 7 || dataSize == 8 || dataSize == 9) {
            index = 5;
            mFeedDataList.add(index, new FeedModel());
            mAdapter.updateRecommendedArtistIndex(index);
        } else if (dataSize > 9) {
            index = 7;
            mFeedDataList.add(index, new FeedModel());
            mAdapter.updateRecommendedArtistIndex(index);
        }
    }

    /**
     * Method to retrieve HashTagOgTheDay text and show it to user.
     */
    private void addHashTagOfTheDayItem() {
        HashTagOfTheDayHelper hashTagOfTheDayHelper = new HashTagOfTheDayHelper();
        hashTagOfTheDayHelper.getHatsOfTheDay(getActivity(), mCompositeDisposable
                , new listener.OnHashTagOfTheDayLoadListener() {
                    @Override
                    public void onSuccess(String hashTagOfTheDay, long hTagPostCount) {
                        if (!TextUtils.isEmpty(hashTagOfTheDay) && !hashTagOfTheDay.equals("null")) {
                            //Set text
                            textHashTagOfTheDay.setText("#" + hashTagOfTheDay);
                            textHashTagOFTheDay = textHashTagOfTheDay.getText().toString();
                            //Set hash tag of the day
                            FeedHelper feedHelper = new FeedHelper();
                            feedHelper.setHashTags(textHashTagOfTheDay, getActivity(), R.color.colorPrimary, hTagPostCount);
                            //Set View
                            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                            appBarLayout.setLayoutParams(params);
                            //Toggle view visibility
                            appBarLayout.setVisibility(View.VISIBLE);
                            viewSuggestedArtists.setVisibility(View.GONE);
                            viewHashTagOfTheDay.setVisibility(View.VISIBLE);

                            // case when hashtag of the day remains same
                            if (hashTagOfTheDay.equals(mHelper.getHTagOfTheDay())) {
                                if (hTagPostCount > mHelper.getHTagCount()) {
                                    newPostIndicator.setVisibility(View.VISIBLE);
                                    mHelper.setHTagNewPostsIndicatorVisibility(true);
                                } else {
                                    newPostIndicator.setVisibility(View.GONE);
                                    mHelper.setHTagNewPostsIndicatorVisibility(false);
                                }
                            }
                            // when hash tag of the day is different on server and app
                            // i.e. it has been updated
                            else {
                                if (hTagPostCount > 0) {
                                    newPostIndicator.setVisibility(View.VISIBLE);
                                    mHelper.setHTagNewPostsIndicatorVisibility(true);
                                } else {
                                    newPostIndicator.setVisibility(View.GONE);
                                    mHelper.setHTagNewPostsIndicatorVisibility(false);
                                }

                                // update count for the hash tag
                                mHelper.setHTagCount(0);

                            }

                            mHelper.setHTagOfTheDay(hashTagOfTheDay);


                            //Check for first time run status
                            if (mHelper.isHashTagOfTheDayFirstTime()) {
                                //Show dialog
                                getHashTagOfTheDayInfoDialog("#" + hashTagOfTheDay);
                                //Update flag
                                mHelper.updateHashTagOfTheDayStatus(false);
                            }
                        }
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        //Show snack abr
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }


    /**
     * Method to parse Json.
     *
     * @param jsonObject JsonObject to be parsed
     * @param isLoadMore Whether called from load more or not.
     * @throws JSONException
     */
    private void parsePostsData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {
        JSONObject mainData = jsonObject.getJSONObject("data");
        mRequestMoreData = mainData.getBoolean("requestmore");
        mLastIndexKey = mainData.getString("lastindexkey");
        mCanDownVote = mainData.getBoolean("candownvote");
        //FeedArray list
        JSONArray feedArray = mainData.getJSONArray("feed");
        int feedArrayLength = feedArray.length();
        for (int i = 0; i < feedArrayLength; i++) {

            JSONObject dataObj = feedArray.getJSONObject(i);
            String type = dataObj.getString("type");

            FeedModel feedData = new FeedModel();
            feedData.setEntityID(dataObj.getString("entityid"));
            feedData.setContentType(dataObj.getString("type"));
            feedData.setUUID(dataObj.getString("uuid"));
            feedData.setCreatorImage(dataObj.getString("profilepicurl"));
            feedData.setCreatorName(dataObj.getString("creatorname"));
            feedData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
            feedData.setMerchantable(dataObj.getBoolean("merchantable"));
            feedData.setDownvoteStatus(dataObj.getBoolean("downvotestatus"));
            feedData.setEligibleForDownvote(mCanDownVote);
            feedData.setPostTimeStamp(dataObj.getString("regdate"));
            feedData.setLongForm(dataObj.getBoolean("long_form"));
            feedData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
            feedData.setCommentCount(dataObj.getLong("commentcount"));
            feedData.setContentImage(dataObj.getString("entityurl"));
            feedData.setCollabCount(dataObj.getLong("collabcount"));
            feedData.setLiveFilterName(dataObj.getString("livefilter"));
            feedData.setPostType(dataObj.getString("posttype"));

            if (dataObj.getString("posttype").equals("REPOST")) {
                feedData.setRepostDate(dataObj.getString("repostdate"));
                feedData.setReposterUUID(dataObj.getString("reposteruuid"));
                feedData.setReposterName(dataObj.getString("repostername"));
                //feedData.setRepostID(dataObj.getString("repostid"));
            }

            //if image width pr image height is null
            if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                feedData.setImgWidth(1);
                feedData.setImgHeight(1);
            } else {
                feedData.setImgWidth(dataObj.getInt("img_width"));
                feedData.setImgHeight(dataObj.getInt("img_height"));
            }

            if (dataObj.isNull("caption")) {
                feedData.setCaption(null);
            } else {
                feedData.setCaption(dataObj.getString("caption"));
            }

            if (type.equals(CONTENT_TYPE_CAPTURE)) {

                //Retrieve "CAPTURE_ID" if type is capture
                feedData.setCaptureID(dataObj.getString("captureid"));
                // if capture
                // then if key cpshort exists
                // not available for collaboration
                if (!dataObj.isNull("cpshort")) {
                    JSONObject collabObject = dataObj.getJSONObject("cpshort");

                    feedData.setAvailableForCollab(false);
                    // set collaborator details
                    feedData.setCollabWithUUID(collabObject.getString("uuid"));
                    feedData.setCollabWithName(collabObject.getString("name"));
                    feedData.setCollaboWithEntityID(collabObject.getString("entityid"));
                } else {
                    feedData.setAvailableForCollab(true);
                }

            } else if (type.equals(CONTENT_TYPE_SHORT)) {
                //Retrieve "SHORT_ID" if type is short
                feedData.setShortID(dataObj.getString("shoid"));

                // if short
                // then if key shcapture exists
                // not available for collaboration
                if (!dataObj.isNull("shcapture")) {

                    JSONObject collabObject = dataObj.getJSONObject("shcapture");

                    feedData.setAvailableForCollab(false);
                    // set collaborator details
                    feedData.setCollabWithUUID(collabObject.getString("uuid"));
                    feedData.setCollabWithName(collabObject.getString("name"));
                    feedData.setCollaboWithEntityID(collabObject.getString("entityid"));
                } else {
                    feedData.setAvailableForCollab(true);
                }
            }

            mFeedDataList.add(feedData);
            //Called from load more
            if (isLoadMore) {
                //Notify item insertion
                mAdapter.notifyItemInserted(mFeedDataList.size() - 1);
            }

        }


    }


    /**
     * Initialize load more listener to retrieve next set of data from server if its available.
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
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    /**
     * Initialize hats off listener.
     *
     * @param adapter FeedAdapter reference.
     */
    private void initHatsOffListener(FeedAdapter adapter) {
        adapter.setHatsOffListener(new listener.OnHatsOffListener() {
            @Override
            public void onHatsOffClick(final FeedModel feedData, final int itemPosition) {

                HatsOffNetworkManger.updateHatsOffStatus(getActivity(), feedData.getEntityID(), feedData.getHatsOffStatus()
                        , new HatsOffNetworkManger.OnHatsOffResponseListener() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(String errorMsg) {
                                //set status to true if its false and vice versa
                                feedData.setHatsOffStatus(!feedData.getHatsOffStatus());
                                //notify changes
                                mAdapter.notifyItemChanged(itemPosition);
                                ViewHelper.getSnackBar(rootView, errorMsg);
                            }
                        });
            }
        });
    }


    /**
     * Method to show welcome Message when user land on this screen for the first time.
     */
    private void showWelcomeMessage() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_generic, false)
                .positiveText(getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        //update status
                        mHelper.updateWelcomeDialogStatus(false);
                        //Method called
                        initDeepLink();
                    }
                })
                .show();

        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);

        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.img_welcome));
        //Set title text
        textTitle.setText("Welcome to Cread");
        //Set description text
        textDesc.setText("Cread is a Social platform where artists can collaborate and showcase their work to earn recognition, goodwill and revenues.");
    }


    /**
     * Method to show HashTagOfTheDay info dialog.
     */
    private void getHashTagOfTheDayInfoDialog(String hashTagText) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_generic, false)
                .positiveText(R.string.text_create)
                .negativeText(R.string.text_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                        //Open add content bottomSheet
                        ((BottomNavigationActivity) getActivity()).getAddContentBottomSheetDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                    }
                })

                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);
        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.img_hash_tag_dialog));
        //Set title text
        textTitle.setText("Hashtag for Today");
        //Set description text
        textDesc.setText("Create an artwork with " + hashTagText + " in your caption and your post will get featured under this hashtag for today");
    }

    //endregion
}
