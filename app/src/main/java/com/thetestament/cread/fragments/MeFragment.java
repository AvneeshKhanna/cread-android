package com.thetestament.cread.fragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import com.thetestament.cread.activities.ChatDetailsActivity;
import com.thetestament.cread.activities.ChatListActivity;
import com.thetestament.cread.activities.FollowActivity;
import com.thetestament.cread.activities.RoyaltiesActivity;
import com.thetestament.cread.activities.UpdateProfileDetailsActivity;
import com.thetestament.cread.activities.UpdateProfileImageActivity;
import com.thetestament.cread.adapters.MeAdapter;
import com.thetestament.cread.adapters.UserStatsPagerAdapter;
import com.thetestament.cread.helpers.DeletePostHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ShareHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnServerRequestedListener;
import com.thetestament.cread.listeners.listener.OnUserStatsClickedListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.Constant.GratitudeNumbers;
import com.thetestament.cread.utils.Constant.ITEM_TYPES;
import com.thetestament.cread.utils.UserStatsViewPager;
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
import io.smooch.ui.ConversationActivity;
import me.relex.circleindicator.CircleIndicator;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.dialog.DialogHelper.getDeletePostDialog;
import static com.thetestament.cread.fragments.ExploreFragment.defaultItemType;
import static com.thetestament.cread.helpers.FeedHelper.generateDeepLink;
import static com.thetestament.cread.helpers.FeedHelper.updateFollowForAll;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getUserDataObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_PROFILE;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_FOLLOW_STATUS;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_LIST_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_REQUESTED_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_IS_PROFILE_EDITABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_BIO;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_CONTACT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_EMAIL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_FIRST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_IMAGE_PATH;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_LAST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_WATER_MARK_STATUS;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_PROFILE;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.COLLABORATIONS;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.COMMENT;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.FOLLOWERS;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.FOLLOWING;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.HATSOFF;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.POSTS;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.ITEM_TYPES.COLLABLIST;
import static com.thetestament.cread.utils.Constant.ITEM_TYPES.GRID;
import static com.thetestament.cread.utils.Constant.ITEM_TYPES.LIST;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CHAT_DETAILS_FROM_USER_PROFILE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_ROYALTIES_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_DETAILS;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_PIC;

/**
 * Fragment class to load user profile details and his/her recent activity.
 */
public class MeFragment extends Fragment implements listener.OnCollaborationListener {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
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
    @BindView(R.id.viewPagerUserStats)
    UserStatsViewPager viewPagerUserStats;
    @BindView(R.id.indicator)
    CircleIndicator indicator;
    @BindView(R.id.viewNoData)
    LinearLayout viewNoData;
    @BindView(R.id.progressView)
    View progressView;
    @BindView(R.id.fabChat)
    FloatingActionButton fabChat;
    @BindView(R.id.buttonMessage)
    AppCompatImageView buttonMessage;
    @BindView(R.id.containerMessage)
    LinearLayout containerMessage;

    //Chat badge view
    View badgeView;

    @State
    String mFirstName, mLastName, mProfilePicURL, mUserBio;
    @State
    long mPostCount, mFollowerCount, mFollowingCount, mHatsoffCount, mCommentsCount, mCollaborationCount;
    @State
    String mEmail, mContactNumber, mWaterMarkStatus;
    @State
    boolean mFollowStatus, isProfileEditable;
    @State
    String mRequestedUUID;

    @State
    String mEntityID, mEntityType;
    Bitmap mBitmap;

    FeedModel mFeedData;

    private List<FeedModel> mUserActivityDataList = new ArrayList<>();
    private List<FeedModel> mCollabList = new ArrayList<>();
    private MeAdapter mAdapter;
    private Unbinder mUnbinder;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private SharedPreferenceHelper mHelper;
    private String mLastIndexKey;
    private String mCollabLastIndexKey;
    private boolean mCollabRequestMoreData;
    private boolean mRequestMoreData;
    private int[] mLayouts;
    private int spanCount = 2;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Initialize preference helper
        mHelper = new SharedPreferenceHelper(getActivity());
        // Its own option menu
        setHasOptionsMenu(true);
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
        //initialize this screen
        initScreen();
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
                    ImageHelper.startImageCropping(getContext()
                            , this
                            , data.getData()
                            , getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));

                } else {
                    ViewHelper.getSnackBar(rootView, "Image from gallery was not attached");
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);
                    ImageHelper.processCroppedImage(mCroppedImgUri
                            , getActivity()
                            , rootView, mEntityID, mEntityType);

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
                        mUserBio = "";
                        //textBio.setText("Write what describes you");
                        textBio.setHint("Write what describes you");
                    }

                    //Retrieve email and watermark status
                    mEmail = data.getExtras().getString(EXTRA_USER_EMAIL);
                    mWaterMarkStatus = data.getExtras().getString(EXTRA_USER_WATER_MARK_STATUS);
                }
                break;
            case REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getBundleExtra(EXTRA_DATA);
                    //Update data
                    if (tabLayout.getSelectedTabPosition() == 2) {
                        mCollabList.get(bundle.getInt("position")).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                        mCollabList.get(bundle.getInt("position")).setHatsOffCount(bundle.getLong("hatsOffCount"));
                        mCollabList.get(bundle.getInt("position")).setFollowStatus(bundle.getBoolean("followstatus"));

                        updateFollowForAll(mCollabList.get(bundle.getInt("position")), mCollabList);

                    } else {
                        mUserActivityDataList.get(bundle.getInt("position")).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                        mUserActivityDataList.get(bundle.getInt("position")).setHatsOffCount(bundle.getLong("hatsOffCount"));
                        mUserActivityDataList.get(bundle.getInt("position")).setFollowStatus(bundle.getBoolean("followstatus"));
                        mUserActivityDataList.get(bundle.getInt("position")).setCaption(bundle.getString("caption"));

                        updateFollowForAll(mUserActivityDataList.get(bundle.getInt("position")), mUserActivityDataList);

                        if (bundle.getBoolean("deletestatus")) {
                            mUserActivityDataList.remove(bundle.getInt("position"));
                            mAdapter.notifyItemRemoved(bundle.getInt("position") + 1);
                        }
                    }
                    //Notify changes
                    mAdapter.notifyItemChanged(bundle.getInt("position"));
                }
                break;
            case REQUEST_CODE_CHAT_DETAILS_FROM_USER_PROFILE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getBundleExtra(EXTRA_CHAT_DETAILS_DATA);
                    mFollowStatus = bundle.getBoolean(EXTRA_CHAT_FOLLOW_STATUS);
                    //Toggle follow button and message button
                    toggleFollowButton(mFollowStatus, getActivity());
                    toggleMessageButton(mFollowStatus, getActivity());
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        if (mHelper.getUUID().equals(mRequestedUUID)) {
            inflater.inflate(R.menu.menu_fragment_me, menu);

            // get instance of update menu item
            MenuItem updatesMenuItem = menu.findItem(R.id.action_updates);

            // if it exists set its flag
            if (updatesMenuItem != null) {   //Change action flag for updates icon
                updatesMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
            //Method called
            setupBadge(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_royalties:
                startRoyaltiesActivity();
                return true;
            case R.id.action_chat_with_cread:
                ConversationActivity.show(getActivity());
                //Update status
                mHelper.setChatMsgReadStatus(true);
                //Hide badge view
                badgeView.setVisibility(View.GONE);
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

    /**
     * User image click functionality to launch screen where user can edit his/her profile picture.
     */
    @OnClick(R.id.imageUser)
    public void onUserImageClicked() {
        //If profile is editable
        if (isProfileEditable) {
            getRuntimePermission();
        } else {
            openProfilePreview();
        }
    }

    /**
     * Click functionality to open screen where user can edit his/her profile details.
     */
    @OnClick(R.id.textUserName)
    public void onUserNameClicked() {
        //If profile is editable
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
     * Click functionality to edit user bio.
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
            //toggle follow and message button
            toggleFollowButton(mFollowStatus, getActivity());
            toggleMessageButton(mFollowStatus, getActivity());

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
     * Click functionality to open a chat list screen.
     */
    @OnClick(R.id.fabChat)
    void fabOnClick() {
        //Open chat list activity
        Intent intent = new Intent(getActivity(), ChatListActivity.class);
        intent.putExtra(EXTRA_CHAT_LIST_CALLED_FROM, "MeFragment");
        startActivity(intent);
        //if new  message is present
        if (mHelper.getPersonalChatIndicatorStatus()) {
            //Change FAB  background color to color accent
            fabChat.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity()
                    , R.color.colorAccent)));
            //update flags in SharedPreference
            mHelper.setPersonalChatIndicatorStatus(false);
        }
    }

    /**
     * Click functionality of message button.
     */
    @OnClick(R.id.buttonMessage)
    void messageOnClick() {
        //if user is following the user
        if (mFollowStatus) {
            //Open ChatDetailsActivity
            Intent intent = new Intent(getActivity(), ChatDetailsActivity.class);
            //Set bundle data
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_CHAT_UUID, mRequestedUUID);
            bundle.putString(EXTRA_CHAT_USER_NAME, mFirstName);
            bundle.putString(EXTRA_CHAT_ID, "");
            bundle.putInt(EXTRA_CHAT_ITEM_POSITION, 0);
            bundle.putString(EXTRA_CHAT_DETAILS_CALLED_FROM, EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_PROFILE);

            intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);
            startActivityForResult(intent, REQUEST_CODE_CHAT_DETAILS_FROM_USER_PROFILE);
        } else {
            ViewHelper.getSnackBar(rootView, "Follow this person to chat");
        }
    }

    /**
     * Click functionality to launch screen where user can see list of people whom he/she is following.
     */
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

            // show royalties dialog if first time
            if (mHelper.isMeFragmentFirstTime()) {
                showRoyaltiesDialog();
            }

        } else {
            mRequestedUUID = getArguments().getString("requesteduuid");
            //Disable profile editing
            isProfileEditable = false;
            //show chat dialog first time
            if (mHelper.isChatDialogFirstTime()) {
                //Show dialog here
                getChatDialog();
                //Update status
                mHelper.updateChatDialogStatus(false);
            }
        }

        //Condition to toggle visibility of follow button and cha list icon
        if (mHelper.getUUID().equals(mRequestedUUID)) {
            //Hide follow button
            buttonFollow.setVisibility(View.GONE);
            //Hide message button and container
            buttonMessage.setVisibility(View.GONE);
            containerMessage.setVisibility(View.GONE);
            //Show chat FAB
            fabChat.setVisibility(View.VISIBLE);
            //Fab custom behaviour
            getFabCustomBehaviour(recyclerView);
        } else {
            //Show follow button
            buttonFollow.setVisibility(View.VISIBLE);
            //Hide chat FAB
            fabChat.setVisibility(View.GONE);
        }


        //initialize tab layout
        initUserStatsPager();
        initSwipeRefreshLayout();
        initTabLayout(tabLayout);
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

                // hide no data view if it's visible
                viewNoData.setVisibility(View.GONE);

                switch (tab.getPosition()) {
                    case 0:
                        // setting pref
                        mHelper.setFeedItemType(GRID);
                        // grid layout for all data
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
                        recyclerView.setLayoutManager(gridLayoutManager);

                        mAdapter = new MeAdapter(mUserActivityDataList, getActivity(), mHelper.getUUID(), MeFragment.this, ITEM_TYPES.GRID);
                        recyclerView.setAdapter(mAdapter);
                        initListeners(LIST);
                        break;
                    case 1:
                        // setting pref
                        mHelper.setFeedItemType(LIST);
                        // list layout for all data
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mAdapter = new MeAdapter(mUserActivityDataList, getActivity(), mHelper.getUUID(), MeFragment.this, LIST);
                        recyclerView.setAdapter(mAdapter);
                        initListeners(LIST);
                        break;
                    case 2:
                        // list layout for collab data
                        mCollabList.clear();
                        mCollabLastIndexKey = null;
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mAdapter = new MeAdapter(mCollabList, getActivity(), mHelper.getUUID(), MeFragment.this, ITEM_TYPES.COLLABLIST);
                        recyclerView.setAdapter(mAdapter);
                        initListeners(COLLABLIST);
                        getCollabData();
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
     * Method to initialize sliders for the view pager.
     */
    private void initSliders() {
        mLayouts = new int[]{
                R.layout.user_stats_page1,
                R.layout.user_stats_page2
        };
    }

    /**
     * Method to initialize view pager adapter.
     */
    private void initUserStatsPager() {
        initSliders();
        UserStatsPagerAdapter mUserStatsAdapter = new UserStatsPagerAdapter(getActivity(), mLayouts);
        // initialize click listeners on user stats
        initUserStatsClickedListener(mUserStatsAdapter);
        viewPagerUserStats.setAdapter(mUserStatsAdapter);
        indicator.setViewPager(viewPagerUserStats);
    }

    /**
     * Method to initialize listeners on the different user stats containers.
     *
     * @param userStatsPagerAdapter adapter of view pager
     */
    private void initUserStatsClickedListener(UserStatsPagerAdapter userStatsPagerAdapter) {
        userStatsPagerAdapter.setUserStatsClickedListener(new OnUserStatsClickedListener() {
            @Override
            public void onUserStatsClicked(GratitudeNumbers gratitudeNumbers, LinearLayout view) {

                switch (gratitudeNumbers) {
                    case FOLLOWERS:
                        onFollowersContainerClicked();
                        break;
                    case FOLLOWING:
                        onFollowingContainerClicked();
                        break;
                    case HATSOFF:

                        String hatsOffTooltip = isCountZero(mHatsoffCount)
                                ? "No hats off yet"
                                : mFirstName
                                + " has received a total of "
                                + mHatsoffCount
                                + " hats off on their posts";

                        ViewHelper
                                .getToolTip(view
                                        , hatsOffTooltip
                                        , getActivity());
                        break;

                    case COMMENT:
                        String commentTooltip = isCountZero(mCommentsCount)
                                ? "No comments yet"
                                : mFirstName
                                + " has received a total of "
                                + mCommentsCount
                                + " comments on their posts";
                        ViewHelper
                                .getToolTip(view
                                        , commentTooltip
                                        , getActivity());
                        break;

                    case COLLABORATIONS:
                        String collaborationsTooltip = isCountZero(mCollaborationCount)
                                ? "No collaborations yet"
                                : mCollaborationCount
                                + " posts have been created by others using "
                                + mFirstName
                                + "'s"
                                + " posts";
                        ViewHelper
                                .getToolTip(view
                                        , collaborationsTooltip
                                        , getActivity());
                        break;
                }
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
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_me_all_tab));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_list));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_collab));
        //initialize tabs icon tint

        //initialize tabs icon tint
        if (defaultItemType == GRID) {
            tabLayout.getTabAt(0).select();

            tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
        } else if (defaultItemType == LIST) {
            tabLayout.getTabAt(1).select();

            tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
        }
    }

    private void initItemTypePreference() {
        defaultItemType = mHelper.getFeedItemType();

        if (defaultItemType == GRID) {
            //Set layout manger for recyclerView
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
            recyclerView.setLayoutManager(gridLayoutManager);
        } else if (mHelper.getFeedItemType() == LIST) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        //Set adapter
        mAdapter = new MeAdapter(mUserActivityDataList, getActivity(), mHelper.getUUID(), MeFragment.this, defaultItemType);
        recyclerView.setAdapter(mAdapter);
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
                                mHatsoffCount = mainData.getLong("hatsoffscount");
                                mCommentsCount = mainData.getLong("commentscount");
                                mCollaborationCount = mainData.getLong("collaborationscount");
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

                            new Handler().post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       //Set user activity stats
                                                       ((TextView) viewPagerUserStats.findViewWithTag(POSTS)).setText(String.valueOf(mPostCount));
                                                       ((TextView) viewPagerUserStats.findViewWithTag(FOLLOWERS)).setText(String.valueOf(mFollowerCount));
                                                       ((TextView) viewPagerUserStats.findViewWithTag(FOLLOWING)).setText(String.valueOf(mFollowingCount));
                                                       ((TextView) viewPagerUserStats.findViewWithTag(HATSOFF)).setText(String.valueOf(mHatsoffCount));
                                                       ((TextView) viewPagerUserStats.findViewWithTag(COMMENT)).setText(String.valueOf(mCommentsCount));
                                                       ((TextView) viewPagerUserStats.findViewWithTag(COLLABORATIONS)).setText(String.valueOf(mCollaborationCount));

                                                   }
                                               }
                            );

                            //If user bio present
                            if (mUserBio != null && !mUserBio.isEmpty() && !mUserBio.equals("null")) {
                                textBio.setVisibility(View.VISIBLE);
                                //Set user bio
                                textBio.setText(mUserBio);
                            } else {
                                //mUserBio = "Write what describes you";
                                //Hide for other users
                                if (!isProfileEditable) {
                                    textBio.setVisibility(View.GONE);
                                }
                            }

                            //toggle follow and message button
                            toggleFollowButton(mFollowStatus, getActivity());
                            toggleMessageButton(mFollowStatus, getActivity());

                            appBarLayout.setVisibility(View.VISIBLE);

                            // check if screen open for first time
                            if (mHelper.isGratitudeFirstTime()) {

                                // scroll to gratitude page
                                startGratitudeScroll();
                            }
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
     * Method to toggle message button tint color and background.
     *
     * @param followStatus true if following false otherwise.
     * @param context      Context to use
     */
    private void toggleMessageButton(boolean followStatus, Context context) {
        if (followStatus) {
            //Update background color
            ViewCompat.setBackground(buttonMessage
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_outline));
            //Update image tint
            buttonMessage.setColorFilter(ContextCompat.getColor(context
                    , R.color.grey_dark));
        } else {
            //Update background color
            ViewCompat.setBackground(buttonMessage
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_filled));
            //Update image tint
            buttonMessage.setColorFilter(ContextCompat.getColor(context
                    , R.color.white));
        }
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

        //If API is greater than LOLLIPOP
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions transitionActivityOptions = ActivityOptions
                    .makeSceneTransitionAnimation(getActivity(), imageUser, ViewCompat.getTransitionName(imageUser));
            //start activity result
            startActivityForResult(intent
                    , REQUEST_CODE_UPDATE_PROFILE_PIC
                    , transitionActivityOptions.toBundle());
        } else {
            startActivityForResult(intent, REQUEST_CODE_UPDATE_PROFILE_PIC);
        }
    }

    /**
     * Open ProfilePreview screen.
     */
    private void openProfilePreview() {

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_profile_preview, false)
                .show();
        //Obtain views reference
        ImageView previewImage = dialog.getCustomView().findViewById(R.id.imageProfilePreview);

        //Load profile picture
        Picasso.with(getActivity())
                .load(mProfilePicURL)
                //.networkPolicy(NetworkPolicy.NO_CACHE)
                //.memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.ic_person_56)
                .into(previewImage);
    }


    /**
     * Method to update follow status.
     */
    private void updateFollowStatus() {

        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(getActivity(),
                mCompositeDisposable,
                mFollowStatus,
                new JSONArray().put(mRequestedUUID),
                new listener.OnFollowRequestedListener() {
                    @Override
                    public void onFollowSuccess() {

                        buttonFollow.setEnabled(true);

                        if (mFollowStatus) {
                            //Increase count by one
                            mFollowerCount += 1;
                            //Set count
                            ((TextView) viewPagerUserStats.findViewWithTag(FOLLOWERS)).setText(String.valueOf(mFollowerCount));
                        } else {
                            //Decrease count by one
                            mFollowerCount -= 1;
                            //Set count
                            ((TextView) viewPagerUserStats.findViewWithTag(FOLLOWERS)).setText(String.valueOf(mFollowerCount));
                        }
                    }

                    @Override
                    public void onFollowFailiure(String errorMsg) {

                        buttonFollow.setEnabled(true);
                        //set status to true if its false and vice versa
                        mFollowStatus = !mFollowStatus;
                        //toggle follow and message button
                        toggleFollowButton(mFollowStatus, getActivity());
                        toggleMessageButton(mFollowStatus, getActivity());

                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }

    /**
     * Method to initialize swipe to refresh view and user timeline view .
     */
    private void initSwipeRefreshLayout() {

        // show list items or grid items from pref
        initItemTypePreference();

        swipeToRefreshLayout.setRefreshing(true);
        swipeToRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));

        //Load profile data
        loadProfileData();
        //Initialize listeners for normal list
        initListeners(LIST);
    }

    /**
     * Initializes the swipe refresh listener based on the item type
     *
     * @param itemType itemType
     */
    private void initOnSwipeRefreshListener(final ITEM_TYPES itemType) {

        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                switch (itemType) {
                    case LIST:
                        //Clear data
                        mUserActivityDataList.clear();

                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to null
                        mLastIndexKey = null;
                        //Load data here
                        getUserTimeLineData();
                        break;

                    case COLLABLIST:
                        //Clear data
                        mCollabList.clear();

                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to null
                        mCollabLastIndexKey = null;
                        //Load data here
                        getCollabData();

                }

            }
        });


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
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_ME)
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
                                    data.setFollowStatus(dataObj.getBoolean("followstatus"));
                                    data.setCollabCount(dataObj.getLong("collabcount"));
                                    if (dataObj.isNull("caption")) {
                                        data.setCaption(null);
                                    } else {
                                        data.setCaption(dataObj.getString("caption"));
                                    }

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
                                            data.setCollaboWithEntityID(collabObject.getString("entityid"));

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
                                            data.setCollaboWithEntityID(collabObject.getString("entityid"));
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
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_ME = false;
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
     * Loads the collaboration data
     */
    private void getCollabData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        swipeToRefreshLayout.setRefreshing(true);

        requestServer(mCompositeDisposable,
                getObservableFromServer(BuildConfig.URL + "/user-profile/load-collab-timeline",
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        mRequestedUUID,
                        mCollabLastIndexKey,
                        GET_RESPONSE_FROM_NETWORK_ME),
                getActivity(),
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                        swipeToRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                parseCollabData(jsonObject, false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        e.printStackTrace();
                        FirebaseCrash.report(e);

                        swipeToRefreshLayout.setRefreshing(false);

                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onCompleteCalled() {

                        swipeToRefreshLayout.setRefreshing(false);

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mCollabList.size() == 0) {
                            //Show no data view
                            viewNoData.setVisibility(View.VISIBLE);
                        } else {
                            // Token status invalid
                            if (tokenError[0]) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Error occurred
                            else if (connectionError[0]) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
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
                    }
                });

    }

    /**
     * Loads the next set of collab data
     */
    private void getNextCollabData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};


        requestServer(mCompositeDisposable,
                getObservableFromServer(BuildConfig.URL + "/user-profile/load-collab-timeline",
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        mRequestedUUID,
                        mCollabLastIndexKey,
                        GET_RESPONSE_FROM_NETWORK_ME),
                getActivity(),
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //Remove loading item
                            mCollabList.remove(mCollabList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mCollabList.size());

                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {

                                parseCollabData(jsonObject, true);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        //Remove loading item
                        mCollabList.remove(mCollabList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mCollabList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onCompleteCalled() {

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mCollabList.size() == 0) {
                            //Show no data view
                            viewNoData.setVisibility(View.VISIBLE);
                        } else {
                            //Notify changes
                            mAdapter.setLoaded();
                        }

                    }
                });
    }

    private void parseCollabData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {

        JSONObject mainData = jsonObject.getJSONObject("data");
        mCollabRequestMoreData = mainData.getBoolean("requestmore");
        mCollabLastIndexKey = mainData.getString("lastindexkey");
        //Collab array list
        JSONArray collabArray = mainData.getJSONArray("items");
        for (int i = 0; i < collabArray.length(); i++) {
            JSONObject dataObj = collabArray.getJSONObject(i);

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
            data.setFollowStatus(dataObj.getBoolean("followstatus"));
            data.setCollabCount(dataObj.getLong("collabcount"));
            if (dataObj.isNull("caption")) {
                data.setCaption(null);
            } else {
                data.setCaption(dataObj.getString("caption"));
            }

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
                    data.setCollaboWithEntityID(collabObject.getString("entityid"));

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
                    data.setCollaboWithEntityID(collabObject.getString("entityid"));
                } else {
                    data.setAvailableForCollab(true);
                }
            }

            mCollabList.add(data);

            if (isLoadMore) {
                //Notify item changes
                mAdapter.notifyItemInserted(mCollabList.size() - 1);
            }
        }
    }

    private void initListeners(ITEM_TYPES itemType) {
        initLoadMoreListener(itemType);
        initHatsOffListener(mAdapter);
        initializeDeleteListener(mAdapter);
        initShareListener(mAdapter);
        initShareLinkClickedListener();

        // init swipe refresh listener for list
        initOnSwipeRefreshListener(itemType);
    }

    /**
     * Initialize load more listener.
     *
     * @param itemType
     */
    private void initLoadMoreListener(final ITEM_TYPES itemType) {

        mAdapter.setUserActivityLoadMoreListener(new listener.OnUserActivityLoadMoreListener() {
            @Override
            public void onLoadMore() {

                switch (itemType) {
                    case LIST:
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

                        break;

                    case COLLABLIST:
                        if (mCollabRequestMoreData) {
                            new Handler().post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       mCollabList.add(null);
                                                       mAdapter.notifyItemInserted(mCollabList.size() - 1);
                                                   }
                                               }
                            );
                            //Load new set of data
                            getNextCollabData();
                        }
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
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_ME)
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
                                    data.setFollowStatus(dataObj.getBoolean("followstatus"));
                                    data.setCollabCount(dataObj.getLong("collabcount"));
                                    if (dataObj.isNull("caption")) {
                                        data.setCaption(null);
                                    } else {
                                        data.setCaption(dataObj.getString("caption"));
                                    }


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
                                            data.setCollaboWithEntityID(collabObject.getString("entityid"));

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
                                            data.setCollaboWithEntityID(collabObject.getString("entityid"));
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
            public void onHatsOffClick(final FeedModel data, final int itemPosition) {
                HatsOffHelper hatsOffHelper = new HatsOffHelper(getActivity());
                hatsOffHelper.updateHatsOffStatus(data.getEntityID(), data.getHatsOffStatus());
                // On hatsOffSuccessListener
                hatsOffHelper.setOnHatsOffSuccessListener(new HatsOffHelper.OnHatsOffSuccessListener() {
                    @Override
                    public void onSuccess() {
                        //Do nothing
                    }
                });
                // On hatsOffSuccessListener
                hatsOffHelper.setOnHatsOffFailureListener(new HatsOffHelper.OnHatsOffFailureListener() {
                    @Override
                    public void onFailure(String errorMsg) {
                        //set status to true if its false and vice versa
                        data.setHatsOffStatus(!data.getHatsOffStatus());
                        //notify changes
                        mAdapter.notifyItemChanged(itemPosition);
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
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

                deleteContent(entityID, position);
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

        // init dialog
        final MaterialDialog dialog = getDeletePostDialog(getActivity());

        DeletePostHelper.deletepost(getActivity(),
                mCompositeDisposable,
                entityID,
                new listener.onDeleteRequestedListener() {
                    @Override
                    public void onDeleteSuccess() {

                        dialog.dismiss();

                        //Remove item from list
                        mUserActivityDataList.remove(itemPosition);
                        //Update adapter
                        mAdapter.notifyItemRemoved(itemPosition);
                        ViewHelper.getSnackBar(rootView, "Item deleted");
                        //Update user post count
                        mPostCount -= 1;
                        ((TextView) viewPagerUserStats.findViewWithTag(POSTS)).setText(String.valueOf(mPostCount));
                    }


                    @Override
                    public void onDeleteFailiure(String errorMsg) {
                        dialog.dismiss();
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }


    /**
     * Initialize share link listener.
     */
    private void initShareLinkClickedListener() {
        mAdapter.setOnShareLinkClickedListener(new listener.OnShareLinkClickedListener() {


            @Override
            public void onShareLinkClicked(String entityID, String entityURL, String creatorName) {

                // generates deep link
                // and opens the share dialog
                generateDeepLink(getActivity(),
                        mCompositeDisposable,
                        rootView,
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        entityID,
                        entityURL,
                        creatorName);
            }
        });
    }

    /**
     * Initialize share listener.
     */
    private void initShareListener(MeAdapter meAdapter) {
        meAdapter.setOnShareListener(new listener.OnShareListener() {
            @Override
            public void onShareClick(Bitmap bitmap, FeedModel data) {
                mBitmap = bitmap;
                mFeedData = data;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    ShareHelper.sharePost(bitmap, getContext(), data);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(MeFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_share_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(MeFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, shareWritePermission);
                    }
                }
            }
        });
    }

    /**
     * Used to handle result of askForPermission for share
     */
    PermissionCallback shareWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            ShareHelper.sharePost(mBitmap, getContext(), mFeedData);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };


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

    private void startRoyaltiesActivity() {
        getActivity().startActivityForResult(
                new Intent(getActivity(),
                        RoyaltiesActivity.class).
                        putExtra(EXTRA_IS_PROFILE_EDITABLE, isProfileEditable)
                , REQUEST_CODE_ROYALTIES_ACTIVITY);
    }

    /**
     * Method to show the royalties dialog
     */
    private void showRoyaltiesDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_generic, false)
                .positiveText("More")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        startRoyaltiesActivity();
                        mHelper.updateMeFragmentStatus(false);
                    }
                })
                .show();

        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.img_intro_royalty));
        //Set title text
        textTitle.setText(getActivity().getString(R.string.title_dialog_me));
        //Set description text
        textDesc.setText(getActivity().getString(R.string.text_dialog_me));

    }


    private void startGratitudeScroll() {
        viewPagerUserStats.setCurrentItem(1);
        mHelper.updateGratitudeScroll(false);
    }

    public static boolean isCountZero(long count) {
        return count == 0;
    }

    /**
     * Method to setup chat icon badge indicator and its functionality.
     *
     * @param menu MeFragment menu.
     */
    private void setupBadge(final Menu menu) {
        //Action layout of chat icon
        View actionView = menu.findItem(R.id.action_chat_with_cread).getActionView();
        //Obtain reference of badgeView
        badgeView = actionView.findViewById(R.id.dotBadge);
        //Action view click functionality
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(menu.findItem(R.id.action_chat_with_cread));
            }
        });


        //Toggle visibility of dot indicator
        if (mHelper.isChatMsgRead()) {
            //Hide badge view
            badgeView.setVisibility(View.GONE);
        } else {
            //Show Badge View
            badgeView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to hide and show the FAB depending upon scrolling behaviour of user.
     *
     * @param recyclerView View to be scrolled.
     */
    private void getFabCustomBehaviour(RecyclerView recyclerView) {
        //if new message is present
        if (mHelper.getPersonalChatIndicatorStatus()) {
            //change fab background color to green
            fabChat.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity()
                    , R.color.green)));
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //Scroll Down
                if (dy > 0 && fabChat.getVisibility() == View.VISIBLE) {
                    fabChat.hide();
                }
                //Scroll Up
                else if (dy < 0 && fabChat.getVisibility() != View.VISIBLE) {
                    fabChat.show();
                }
            }
        });
    }

    /**
     * Method to show the chat introduction dialog.
     */
    private void getChatDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_generic, false)
                .positiveText(R.string.text_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                        //Update status
                        mHelper.updateChatDialogStatus(false);
                    }
                })
                .show();

        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.img_chat_intro_dialog));
        //Set title text
        textTitle.setText(getActivity().getString(R.string.title_dialog_chat));
        //Set description text
        textDesc.setText(getActivity().getString(R.string.text_dialog_chat_desc));

    }


}
