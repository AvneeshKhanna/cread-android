package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.FindFBFriendsActivity;
import com.thetestament.cread.activities.SearchActivity;
import com.thetestament.cread.adapters.ExploreAdapter;
import com.thetestament.cread.adapters.FeaturedArtistsAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.FirebaseEventHelper;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeaturedArtistsModel;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.networkmanager.ExploreNetworkManager;
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
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FEATURED_ARTISTS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.adapters.FeaturedArtistsAdapter.VIEW_TYPE_HEADER;
import static com.thetestament.cread.adapters.FeaturedArtistsAdapter.VIEW_TYPE_ITEM;
import static com.thetestament.cread.fragments.MeFragment.isCountOne;
import static com.thetestament.cread.helpers.FeedHelper.updateFollowForAll;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.processCroppedImage;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getUserDataObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.ITEM_TYPES.GRID;
import static com.thetestament.cread.utils.Constant.ITEM_TYPES.LIST;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;


public class ExploreFragment extends Fragment implements listener.OnCollaborationListener {

    //region :View binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.recycler_view_feat_artists)
    RecyclerView recyclerViewFeatArtists;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view_explore)
    RecyclerView recyclerViewExplore;
    @BindView(R.id.tab_layout_main)
    TabLayout mainTab;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.fabToggle)
    FloatingActionButton fabToggle;

    //endregion

    //region :Fields and constants
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    /**
     * List to maintain arts data.
     */
    List<FeedModel> mExploreDataList = new ArrayList<>();

    /**
     * List to maintain feature artist data.
     */
    List<FeaturedArtistsModel> mFeatArtistsList = new ArrayList<>();


    ExploreAdapter mAdapter;
    FeaturedArtistsAdapter mFeatArtistsAdapter;


    SharedPreferenceHelper mHelper;
    private Unbinder mUnbinder;

    /**
     * Flag to maintain request more data.
     */
    @State
    String mLastIndexKey;
    /**
     * Flag to maintain index key for next set of data.
     */
    @State
    boolean mRequestMoreData;

    public static Constant.ITEM_TYPES defaultItemType;


    @State
    String mEntityID, mEntityType;
    @State
    String mFirstName, mLastName, mProfilePicURL;
    @State
    long mPostCount, mFollowerCount, mCollaborationCount;


    /**
     * Flag to maintain user down vote capability.
     */
    @State
    boolean mCanDownVote;


    /**
     * Flag to store selected Category ID.
     */
    @State
    String mSelectedCategoryID = "";

    /**
     * Flag to maintain selected tab status.
     * Default is {@link com.thetestament.cread.utils.Constant#EXPLORE_SELECTED_TAB_POPULAR}
     */
    @State
    String mSelectedTab = Constant.EXPLORE_SELECTED_TAB_POPULAR;

    /**
     * Flag to maintain selected tab type. {@link com.thetestament.cread.utils.Constant#EXPLORE_TAB_TYPE_ART}
     * {@link com.thetestament.cread.utils.Constant#EXPLORE_TAB_TYPE_HUMOR}
     */
    @State
    String mTabType = Constant.EXPLORE_TAB_TYPE_ART;

    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getActivity());
        // Its own option menu
        setHasOptionsMenu(true);
        //inflate this view
        return inflater.inflate(R.layout.fragment_explore
                , container
                , false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ButterKnife view binding
        mUnbinder = ButterKnife.bind(this, view);
        //Method called
        initViews();
        //Explore screen open for first time
        if (mHelper.isExploreIntroFirstTime()) {
            CustomDialog.getGenericDialog(getActivity()
                    , getString(R.string.text_ok)
                    , "Discover Cread"
                    , "Find the best of Cread from all around the app. Explore the good stuff everyone is posting by navigating to this section"
                    , R.drawable.img_explore_intro);
            //update status
            mHelper.updateExploreIntroStatus(false);
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
                    mExploreDataList.get(bundle.getInt("position")).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                    mExploreDataList.get(bundle.getInt("position")).setDownvoteStatus(bundle.getBoolean("downvotestatus"));
                    mExploreDataList.get(bundle.getInt("position")).setHatsOffCount(bundle.getLong("hatsOffCount"));
                    mExploreDataList.get(bundle.getInt("position")).setFollowStatus(bundle.getBoolean("followstatus"));
                    mExploreDataList.get(bundle.getInt("position")).setCaption(bundle.getString("caption"));

                    //update follow occurences
                    updateFollowForAll(mExploreDataList.get(bundle.getInt("position")), mExploreDataList);

                    if (bundle.getBoolean("deletestatus")) {
                        mExploreDataList.remove(bundle.getInt("position"));
                    }

                    //Notify changes
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //Change action flag to SHOW_AS_ACTION_IF_ROOM
        menu.findItem(R.id.action_updates).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_explore, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_findfbFriends:
                startActivity(new Intent(getActivity(), FindFBFriendsActivity.class));
                return true;
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
        //Set entity id
        mEntityID = entityID;
        mEntityType = entityType;
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            ImageHelper.chooseImageFromGallery(ExploreFragment.this);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(ExploreFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_capture_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(ExploreFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
            }
        }
    }
    //endregion

    //region :Click functionality

    /**
     * Click functionality to toggle b/w  grid and list view.
     */
    @OnClick(R.id.fabToggle)
    void fabOnClick() {
        if (defaultItemType == GRID) {
            // Update preferences and flags
            mHelper.setFeedItemType(Constant.ITEM_TYPES.LIST);
            defaultItemType = LIST;
            //set layout manager
            recyclerViewExplore.setLayoutManager(new LinearLayoutManager(getActivity()));
            //Set adapter
            mAdapter = new ExploreAdapter(mExploreDataList, getActivity()
                    , mHelper.getUUID(), ExploreFragment.this
                    , Constant.ITEM_TYPES.LIST, mCompositeDisposable);
            recyclerViewExplore.setAdapter(mAdapter);
            initListeners();
        } else if (defaultItemType == LIST) {
            // Update preferences and flags
            mHelper.setFeedItemType(Constant.ITEM_TYPES.GRID);
            defaultItemType = GRID;
            // setting layout manager
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
            recyclerViewExplore.setLayoutManager(gridLayoutManager);
            //Set adapter
            mAdapter = new ExploreAdapter(mExploreDataList, getActivity()
                    , mHelper.getUUID(), ExploreFragment.this, GRID, mCompositeDisposable);
            recyclerViewExplore.setAdapter(mAdapter);
            initListeners();
        } else {
        }
    }


    //endregion

    //region :Private method

    /**
     * Method to initialize views for this screen.
     */
    private void initViews() {
        initSwipeRefreshLayout();
        //initializes grid view or list view from preferences
        initItemTypePreference();
        initMainTabLayout();
        initTabLayout();
        //Initialize listener
        initListeners();
        initFeatArtistClickListener();
    }

    /**
     * Method to initialize swipe refresh layout.
     */
    private void initSwipeRefreshLayout() {
        //init feat artists recycler view
        mFeatArtistsAdapter = new FeaturedArtistsAdapter(getActivity(), mFeatArtistsList);
        recyclerViewFeatArtists.setAdapter(mFeatArtistsAdapter);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data list
                mExploreDataList.clear();
                mFeatArtistsList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mFeatArtistsAdapter.notifyDataSetChanged();
                //hide featured view
                recyclerViewFeatArtists.setVisibility(View.GONE);
                mAdapter.setLoaded();
                //set last index key to nul
                mLastIndexKey = null;
                //Load data here
                getFeaturedArtistsData();

                if (mTabType.equals(Constant.EXPLORE_TAB_TYPE_ART)) {
                    loadExploreData();
                } else {
                    getMemeData();
                }
            }
        });

        //Load data here
        getFeaturedArtistsData();
        if (mTabType.equals(Constant.EXPLORE_TAB_TYPE_ART)) {
            loadExploreData();
        } else {
            getMemeData();
        }
    }


    /**
     * Method to initialize view type.
     */
    private void initItemTypePreference() {
        //Obtain item type
        defaultItemType = mHelper.getFeedItemType();

        //Type is GRID
        if (defaultItemType == GRID) {
            //Set layout manger for recyclerView
            recyclerViewExplore.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else if (defaultItemType == LIST) {
            recyclerViewExplore.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        //Set adapter
        mAdapter = new ExploreAdapter(mExploreDataList, getActivity(), mHelper.getUUID(), ExploreFragment.this, defaultItemType, mCompositeDisposable);
        recyclerViewExplore.setAdapter(mAdapter);
    }

    /**
     * Method to initialize tab layout.
     */
    private void initMainTabLayout() {
        mainTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        tabLayout.setVisibility(View.VISIBLE);
                        //Update flag here
                        mTabType = Constant.EXPLORE_TAB_TYPE_ART;
                        //Clear data list
                        mExploreDataList.clear();
                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //reset flags
                        mRequestMoreData = false;
                        mLastIndexKey = null;
                        mCompositeDisposable.clear();
                        //Load data here
                        loadExploreData();
                        break;
                    case 1:
                        tabLayout.setVisibility(View.GONE);
                        //Update flag here
                        mTabType = Constant.EXPLORE_TAB_TYPE_HUMOR;
                        //Clear data list
                        mExploreDataList.clear();
                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //reset flags
                        mRequestMoreData = false;
                        mLastIndexKey = null;
                        mCompositeDisposable.clear();
                        //Load data here
                        getMemeData();
                        break;
                    default:
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
     * Method to initialize tab layout.
     */
    private void initTabLayout() {
        AppCompatTextView tabOne = (AppCompatTextView) LayoutInflater.from(getActivity())
                .inflate(R.layout.custom_tab, null);
        tabOne.setText(R.string.tab_popular);
        tabOne.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_defined));
        tabOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_popular, 0, 0, 0);
        tabOne.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getActivity()
                , R.color.black_defined), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(0).setCustomView(tabOne);

        AppCompatTextView tabTwo = (AppCompatTextView) LayoutInflater.from(getActivity())
                .inflate(R.layout.custom_tab, null);
        tabTwo.setText(R.string.tab_recent);
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_recent, 0, 0, 0);
        tabLayout.getTabAt(1).setCustomView(tabTwo);

        AppCompatTextView tabThree = (AppCompatTextView) LayoutInflater.from(getActivity())
                .inflate(R.layout.custom_tab, null);
        tabThree.setText(R.string.tab_best);
        tabThree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_all_star, 0, 0, 0);
        tabLayout.getTabAt(2).setCustomView(tabThree);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Update text and icon color
                AppCompatTextView textView = (AppCompatTextView) tab.getCustomView();
                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_defined));
                textView.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getActivity()
                        , R.color.black_defined), PorterDuff.Mode.SRC_IN);
                switch (tab.getPosition()) {
                    case 0:
                        //Update flag here
                        mSelectedTab = Constant.EXPLORE_SELECTED_TAB_POPULAR;

                        //Clear data list
                        mExploreDataList.clear();
                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to nul
                        mLastIndexKey = null;
                        mCompositeDisposable.clear();
                        //Load data here
                        loadExploreData();
                        //Log firebase event
                        FirebaseEventHelper.logExploreTabSelectionEvent(getActivity(), mSelectedTab);
                        break;
                    case 1:
                        //Update flag here
                        mSelectedTab = Constant.EXPLORE_SELECTED_TAB_RECENT;

                        //Clear data list
                        mExploreDataList.clear();
                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to nul
                        mLastIndexKey = null;
                        mCompositeDisposable.clear();
                        //Load data here
                        loadExploreData();
                        //Log firebase event
                        FirebaseEventHelper.logExploreTabSelectionEvent(getActivity(), mSelectedTab);
                        break;

                    case 2:
                        //Update flag here
                        mSelectedTab = Constant.EXPLORE_SELECTED_TAB_BEST;

                        //Clear data list
                        mExploreDataList.clear();
                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to nul
                        mLastIndexKey = null;
                        mCompositeDisposable.clear();
                        //Load data here
                        loadExploreData();
                        //Log firebase event
                        FirebaseEventHelper.logExploreTabSelectionEvent(getActivity(), mSelectedTab);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                AppCompatTextView textView = (AppCompatTextView) tab.getCustomView();
                textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.grey_custom));
                textView.getCompoundDrawables()[0].setColorFilter(ContextCompat.getColor(getActivity()
                        , R.color.grey_custom), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    /**
     * Method to initialize listeners.
     */
    private void initListeners() {
        initLoadMoreListener();
        initCaptureListener();
        initFollowListener();
        ViewHelper.getFabCustomBehaviour(recyclerViewExplore, fabToggle);
    }

    /**
     * Method to initialize featured artist item click listener.
     */
    private void initFeatArtistClickListener() {
        mFeatArtistsAdapter.setFeatArtistClickListener(new listener.OnFeatArtistClickedListener() {
            @Override
            public void onFeatArtistClicked(int itemType, String uuid) {
                //Item type is header
                if (itemType == VIEW_TYPE_HEADER) {
                    CustomDialog.getGenericDialog(getActivity()
                            , getString(R.string.text_ok)
                            , getString(R.string.text_title_dialog_featured_artist)
                            , getString(R.string.text_desc_dialog_featured_artist)
                            , R.drawable.img_intro_feat_artist);
                } else if (itemType == VIEW_TYPE_ITEM) {
                    //Load user data and display it in dialog
                    getFeatArtistDetails(uuid);
                }
            }
        });
    }

    /**
     * Initialize load more listener.
     */
    private void initLoadMoreListener() {

        mAdapter.setOnExploreLoadMoreListener(new listener.OnExploreLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //If next set of data available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mExploreDataList.add(null);
                                               mAdapter.notifyItemInserted(mExploreDataList.size() - 1);
                                           }
                                       }
                    );
                    if (mTabType.equals(Constant.EXPLORE_TAB_TYPE_ART)) {
                        loadMoreData();
                    } else {
                        loadMemeMoreData();
                    }
                }
            }
        });
    }


    /**
     * Initialize follow listener.
     */
    private void initFollowListener() {
        mAdapter.setOnExploreFollowListener(new listener.OnExploreFollowListener() {
            @Override
            public void onFollowClick(FeedModel exploreData, int itemPosition) {
                updateFollowStatus(exploreData, itemPosition);
            }
        });
    }

    /**
     * Method to update follow status.
     *
     * @param exploreData  Model of current item
     * @param itemPosition Position of current item i.e integer
     */
    private void updateFollowStatus(final FeedModel exploreData, final int itemPosition) {

        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(getActivity(),
                mCompositeDisposable,
                exploreData.getFollowStatus(),
                new JSONArray().put(exploreData.getUUID()),
                new listener.OnFollowRequestedListener() {
                    @Override
                    public void onFollowSuccess() {
                        // updates follow status in all occurrence of the followed user
                        updateFollowForAll(exploreData, mExploreDataList);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFollowFailure(String errorMsg) {

                        //set status to true if its false and vice versa
                        exploreData.setFollowStatus(!exploreData.getFollowStatus());
                        //notify changes
                        mAdapter.notifyItemChanged(itemPosition);

                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }

    /**
     * Initialize capture listener.
     */
    private void initCaptureListener() {
        mAdapter.setOnExploreCaptureClickListener(new listener.OnExploreCaptureClickListener() {
            @Override
            public void onClick(String shortId) {
                //Set entity id
                mEntityID = shortId;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    ImageHelper.chooseImageFromGallery(ExploreFragment.this);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(ExploreFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_capture_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(ExploreFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
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
            ImageHelper.chooseImageFromGallery(ExploreFragment.this);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_capture_permission_denied));
        }
    };


    /**
     * Method to retrieve featureArtist detail from server.
     */
    private void getFeatArtistDetails(final String uuid) {
        // show loading dialog
        final MaterialDialog loadingDialog = CustomDialog.getProgressDialog(getActivity()
                , "Loading...");

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable,
                getUserDataObservableFromServer(BuildConfig.URL + "/user-profile/load-profile"
                        , mHelper.getUUID()
                        , mHelper.getAuthToken(),
                        uuid)
                , getActivity()
                , new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        //Show no connection snack abr
                        ViewHelper.getSnackBar(rootView
                                , getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mFirstName = mainData.getString("firstname");
                                mLastName = mainData.getString("lastname");
                                mProfilePicURL = mainData.getString("profilepicurl");
                                mPostCount = mainData.getLong("postcount");
                                mFollowerCount = mainData.getLong("followercount");
                                mCollaborationCount = mainData.getLong("collaborationscount");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "ExploreFragment");
                            connectionError[0] = true;
                        }

                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        // dismiss dialog
                        loadingDialog.dismiss();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "ExploreFragment");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {
                        // dismiss dialog
                        loadingDialog.dismiss();
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_ME = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            // show detail dialog
                            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                                    .customView(R.layout.dialog_featured_artist_profile, false)
                                    .show();

                            // init views
                            View rootViewDialog = dialog.getCustomView();
                            TextView artistName = rootViewDialog.findViewById(R.id.textFeatArtist);
                            SimpleDraweeView imageArtist = rootViewDialog.findViewById(R.id.imageFeatArtist);
                            final TextView textPostsCount = rootViewDialog.findViewById(R.id.textPostsCount);
                            final TextView textFollowersCount = rootViewDialog.findViewById(R.id.textFollowersCount);
                            final TextView textCollaborationsCount = rootViewDialog.findViewById(R.id.textCollaborationsCount);
                            TextView textPosts = rootViewDialog.findViewById(R.id.textPosts);
                            TextView textFollowers = rootViewDialog.findViewById(R.id.textFollowers);
                            TextView textCollaborations = rootViewDialog.findViewById(R.id.textCollaborations);

                            // set message for text views
                            String posts = isCountOne(mPostCount) ? "Post" : "Posts";
                            String followers = isCountOne(mFollowerCount) ? "Follower" : "Followers";
                            String collaborations = isCountOne(mCollaborationCount) ? "Collaboration" : "Collaborations";

                            // set text
                            textPosts.setText(posts);
                            textFollowers.setText(followers);
                            textCollaborations.setText(collaborations);

                            // set click listener
                            LinearLayout buttonViewProfile = rootViewDialog.findViewById(R.id.buttonViewProfile);
                            buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Method called
                                    IntentHelper.openProfileActivity(getActivity(), uuid);

                                    // dismiss dialog
                                    dialog.dismiss();

                                }
                            });

                            //Load user profile picture
                            ImageHelper.loadProgressiveImage(Uri.parse(mProfilePicURL), imageArtist);

                            //if last name is present
                            if (mLastName != null) {
                                //Set user name
                                artistName.setText(mFirstName + " " + mLastName);
                            } else {
                                //set user name
                                artistName.setText(mFirstName);
                            }
                            new Handler().post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       //Set user activity stats
                                                       textPostsCount.setText(String.valueOf(mPostCount));
                                                       textFollowersCount.setText(String.valueOf(mFollowerCount));
                                                       textCollaborationsCount.setText(String.valueOf(mCollaborationCount));

                                                   }
                                               }
                            );
                        }
                    }
                }
        );

    }

    /**
     * Method to retrieve featureArtist data from server.
     */
    private void getFeaturedArtistsData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable
                , ExploreNetworkManager.getFeatArtistsObservable(BuildConfig.URL + "/featured-artists/load", mHelper.getUUID(), mHelper.getAuthToken(), GET_RESPONSE_FROM_NETWORK_FEATURED_ARTISTS)
                , getActivity(), new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        ViewHelper.getSnackBar(rootView
                                , getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                //Featured artist list
                                JSONArray featuredArray = mainData.getJSONArray("featuredlist");
                                for (int i = 0; i < featuredArray.length(); i++) {
                                    FeaturedArtistsModel data = new FeaturedArtistsModel();

                                    JSONObject dataObj = featuredArray.getJSONObject(i);
                                    data.setUuid(dataObj.getString("uuid"));
                                    data.setName(dataObj.getString("name"));
                                    data.setImageUrl(dataObj.getString("profilepicurl"));
                                    mFeatArtistsList.add(data);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "ExploreFragment");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "ExploreFragment");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_FEATURED_ARTISTS = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mFeatArtistsList.size() == 0) {
                            // hide feat artists view
                            recyclerViewFeatArtists.setVisibility(View.GONE);

                        } else {
                            recyclerViewFeatArtists.setVisibility(View.VISIBLE);
                            mFeatArtistsAdapter.notifyDataSetChanged();
                        }

                    }
                });
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadExploreData() {
        // if user device is connected to net
        if (getNetConnectionStatus(getActivity())) {
            //Set progress indicator
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getExploreData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving explore data
     */
    private void getExploreData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(ExploreNetworkManager.getExploreFeedObservable(BuildConfig.URL + "/explore-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_EXPLORE
                , mSelectedCategoryID
                , mSelectedTab)
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
                                parsePostsData(jsonObject, false);
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
                        GET_RESPONSE_FROM_NETWORK_EXPLORE = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        } else if (mExploreDataList.size() == 0) {
                            ViewHelper.getSnackBar(rootView, "Nothing to show right now");
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerViewExplore.setLayoutAnimation(animation);
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
        mCompositeDisposable.add(ExploreNetworkManager.getExploreFeedObservable(BuildConfig.URL + "/explore-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_EXPLORE
                , mSelectedCategoryID,
                mSelectedTab)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());
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
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());
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
        //ExploreArray list
        JSONArray exploreArray = mainData.getJSONArray("feed");
        for (int i = 0; i < exploreArray.length(); i++) {
            JSONObject dataObj = exploreArray.getJSONObject(i);
            String type = dataObj.getString("type");

            FeedModel exploreData = new FeedModel();
            exploreData.setEntityID(dataObj.getString("entityid"));
            exploreData.setContentType(dataObj.getString("type"));
            exploreData.setUUID(dataObj.getString("uuid"));
            exploreData.setCreatorImage(dataObj.getString("profilepicurl"));
            exploreData.setCreatorName(dataObj.getString("creatorname"));
            exploreData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
            exploreData.setFollowStatus(dataObj.getBoolean("followstatus"));
            exploreData.setMerchantable(dataObj.getBoolean("merchantable"));
            exploreData.setDownvoteStatus(dataObj.getBoolean("downvotestatus"));
            exploreData.setEligibleForDownvote(mCanDownVote);
            exploreData.setPostTimeStamp(dataObj.getString("regdate"));
            exploreData.setLongForm(dataObj.getBoolean("long_form"));
            exploreData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
            exploreData.setCommentCount(dataObj.getLong("commentcount"));
            exploreData.setContentImage(dataObj.getString("entityurl"));
            exploreData.setCollabCount(dataObj.getLong("collabcount"));
            exploreData.setLiveFilterName(dataObj.getString("livefilter"));

            //if image width pr image height is null
            if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                exploreData.setImgWidth(1);
                exploreData.setImgHeight(1);
            } else {
                exploreData.setImgWidth(dataObj.getInt("img_width"));
                exploreData.setImgHeight(dataObj.getInt("img_height"));
            }

            if (dataObj.isNull("caption")) {
                exploreData.setCaption(null);
            } else {
                exploreData.setCaption(dataObj.getString("caption"));
            }


            if (type.equals(CONTENT_TYPE_CAPTURE)) {

                //Retrieve "CAPTURE_ID" if type is capture
                exploreData.setCaptureID(dataObj.getString("captureid"));
                // if capture
                // then if key cpshort exists
                // not available for collaboration
                if (!dataObj.isNull("cpshort")) {
                    JSONObject collabObject = dataObj.getJSONObject("cpshort");

                    exploreData.setAvailableForCollab(false);
                    // set collaborator details
                    exploreData.setCollabWithUUID(collabObject.getString("uuid"));
                    exploreData.setCollabWithName(collabObject.getString("name"));
                    exploreData.setCollaboWithEntityID(collabObject.getString("entityid"));

                } else {
                    exploreData.setAvailableForCollab(true);
                }

            } else if (type.equals(CONTENT_TYPE_SHORT)) {

                //Retrieve "SHORT_ID" if type is short
                exploreData.setShortID(dataObj.getString("shoid"));

                // if short
                // then if key shcapture exists
                // not available for collaboration
                if (!dataObj.isNull("shcapture")) {

                    JSONObject collabObject = dataObj.getJSONObject("shcapture");

                    exploreData.setAvailableForCollab(false);
                    // set collaborator details
                    exploreData.setCollabWithUUID(collabObject.getString("uuid"));
                    exploreData.setCollabWithName(collabObject.getString("name"));
                    exploreData.setCollaboWithEntityID(collabObject.getString("entityid"));
                } else {
                    exploreData.setAvailableForCollab(true);
                }
            }
            mExploreDataList.add(exploreData);
            //Called from load more
            if (isLoadMore) {
                //Notify item insertion
                mAdapter.notifyItemInserted(mExploreDataList.size() - 1);
            }


        }
    }


    /**
     * RxJava2 implementation for retrieving meme data.
     */
    private void getMemeData() {
        swipeRefreshLayout.setRefreshing(true);
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(ExploreNetworkManager.getMemeObservable(BuildConfig.URL + "/explore-feed/load-memes"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_EXPLORE)
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
                                parsePostsData(jsonObject, false);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "ExploreFragment");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "ExploreFragment");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_EXPLORE = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        } else if (mExploreDataList.size() == 0) {
                            ViewHelper.getSnackBar(rootView, "Nothing to show right now");
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerViewExplore.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }

    /**
     * Method to retrieve to next set of data from server.
     */
    private void loadMemeMoreData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        mCompositeDisposable.add(ExploreNetworkManager.getMemeObservable(BuildConfig.URL + "/explore-feed/load-memes"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_EXPLORE)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());
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
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());
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


    //endregion
}
