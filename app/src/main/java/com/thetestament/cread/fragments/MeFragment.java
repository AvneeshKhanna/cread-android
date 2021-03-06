package com.thetestament.cread.fragments;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.Spannable;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.BottomNavigationActivity;
import com.thetestament.cread.activities.ChatDetailsActivity;
import com.thetestament.cread.activities.ChatListActivity;
import com.thetestament.cread.activities.RoyaltiesActivity;
import com.thetestament.cread.activities.UpdateProfileDetailsActivity;
import com.thetestament.cread.activities.UpdateProfileImageActivity;
import com.thetestament.cread.adapters.MeAdapter;
import com.thetestament.cread.adapters.OtherUserAchievementsAdapter;
import com.thetestament.cread.adapters.UserStatsPagerAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.DeletePostHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.FirebaseEventHelper;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.GifHelper;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnServerRequestedListener;
import com.thetestament.cread.listeners.listener.OnUserStatsClickedListener;
import com.thetestament.cread.models.AchievementsModels;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.networkmanager.AchivementNetworkManager;
import com.thetestament.cread.networkmanager.MeNetworkManager;
import com.thetestament.cread.networkmanager.NotificationNetworkManager;
import com.thetestament.cread.networkmanager.RepostNetworkManager;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.Constant.GratitudeNumbers;
import com.thetestament.cread.utils.Constant.ITEM_TYPES;
import com.thetestament.cread.utils.TextUtils;
import com.thetestament.cread.utils.UserStatsViewPager;
import com.thetestament.cread.widgets.InOutInterpolator;
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
import me.relex.circleindicator.CircleIndicator;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.helpers.DeepLinkHelper.getDeepLinkOnValidShareOption;
import static com.thetestament.cread.helpers.FeedHelper.updateFollowForAll;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.processCroppedImage;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
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
import static com.thetestament.cread.utils.Constant.EXTRA_IS_PROFILE_EDITABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_PIC_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_TOP_USER_INTERESTS;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_BIO;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_CONTACT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_EMAIL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_FIRST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_IMAGE_PATH;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_INTERESTS_COUNT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_LAST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_WATER_MARK_STATUS;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_WEB_STORE_LINK;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.BADGES;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.COLLABORATIONS;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.COMMENT;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.FOLLOWERS;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.FOLLOWING;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.HATSOFF;
import static com.thetestament.cread.utils.Constant.GratitudeNumbers.POSTS;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.ITEM_TYPES.GRID;
import static com.thetestament.cread.utils.Constant.ITEM_TYPES.LIST;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CHAT_DETAILS_FROM_USER_PROFILE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_ROYALTIES_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_DETAILS;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_PIC;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;

/**
 * Fragment class to load user profile details and his/her recent activity.
 */
public class MeFragment extends Fragment implements listener.OnCollaborationListener {

    //region :View binding with Butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.imageUser)
    CircleImageView imageUser;
    @BindView(R.id.view_top_artist)
    AppCompatTextView viewTopArtist;
    @BindView(R.id.textUserName)
    TextView textUserName;
    @BindView(R.id.textBio)
    TextView textBio;
    @BindView(R.id.imageFeatured)
    AppCompatImageView imageFeatured;
    @BindView(R.id.buttonFollow)
    TextView buttonFollow;
    @BindView(R.id.viewPagerUserStats)
    UserStatsViewPager viewPagerUserStats;
    @BindView(R.id.indicator)
    CircleIndicator indicator;
    @BindView(R.id.containerMessage)
    LinearLayout containerMessage;
    @BindView(R.id.buttonMessage)
    AppCompatImageView buttonMessage;
    @BindView(R.id.buttonProfileSettings)
    ImageButton buttonProfileSettings;
    @BindView(R.id.dotIndicator)
    View dotIndicatorWebStoreLink;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeToRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.view_no_data)
    LinearLayout viewNoData;
    @BindView(R.id.progressView)
    View progressView;
    @BindView(R.id.fabChat)
    FloatingActionButton fabChat;

    //Bottom sheet view achievement
    @BindView(R.id.achievement_bottom_sheet_view)
    NestedScrollView nsAchievements;
    @BindView(R.id.recycler_view_achievements)
    RecyclerView recyclerViewAchievements;

    /**
     * BottomSheet behaviour for live filters.
     */
    BottomSheetBehavior achievementsSheetBehavior;
    //endregion

    //region :Fields and constant
    /**
     * Flag to maintain user first name.
     */
    @State
    String mFirstName;
    /**
     * Flag to maintain user last name.
     */
    @State
    String mLastName;
    /**
     * Flag to maintain user profile pic url.
     */
    @State
    String mProfilePicURL;
    /**
     * Flag to maintain user biography.
     */
    @State
    String mUserBio;

    /**
     * Flag to maintain user post count.
     */
    @State
    long mPostCount;
    /**
     * Flag to maintain user follower count.
     */
    @State
    long mFollowerCount;
    /**
     * Flag to maintain count of user whom he is following.
     */
    @State
    long mFollowingCount;
    /**
     * Flag to maintain total no of times user has been selected as featured artist.
     */
    @State
    long mFeatureCount;
    /**
     * Flag to maintain total no hatsOff received on user posts.
     */
    @State
    long mHatsoffCount;
    /**
     * Flag to maintain total no comments received on user posts.
     */
    @State
    long mCommentsCount;
    /**
     * Flag to maintain total no of collaboration count.
     */
    @State
    long mCollaborationCount;

    /**
     * Flag to maintain total no of badge count.
     */
    @State
    long mBadgeCount;

    /**
     * Flag to maintain user email.
     */
    @State
    String mEmail;
    /**
     * Flag to maintain user contact number.
     */
    @State
    String mContactNumber;
    /**
     * Flag to maintain watermark status.
     */
    @State
    String mWaterMarkStatus;

    /**
     * Flag to maintain whether user is following the user or not.
     */
    @State
    boolean mFollowStatus;

    /**
     * Flag to maintain whether user can edit his/her profile or not.
     */
    @State
    boolean isProfileEditable;
    /**
     * Flag to maintain whether is user selected as  artist or not.
     */
    @State
    boolean mIsFeatured;
    /**
     * Flag to maintain user's down vote privileges.
     */
    @State
    boolean mCanDownvote;

    /**
     * Flag to maintain top artist status.
     */
    @State
    boolean mIsTopArtist;

    /**
     * Flag to maintain UUID of user whose profile to be loaded.
     */
    @State
    String mRequestedUUID;
    /**
     * Flag to maintain user interest count.
     */
    @State
    long mInterestCount = 0;

    /**
     * Flag to maintain user web store link.
     */
    @State
    String mWebStoreUrl;


    /**
     * List to store user posts data list.
     */
    List<FeedModel> mUserPostDataList = new ArrayList<>();
    /**
     * List to store collaboration data list.
     */
    List<FeedModel> mCollabList = new ArrayList<>();

    /**
     * List to store re-post data list.
     */
    List<FeedModel> mRepostDataList = new ArrayList<>();

    /**
     * Flag to store last index key of user posts data.
     */
    @State
    String mUserPostsLastIndexKey;
    /**
     * Flag to maintain whether next set of user posts data is available or not.
     */
    @State
    boolean mUserPostsRequestMoreData;
    /**
     * Flag to store last index key of collaboration posts data.
     */
    @State
    String mCollabPostsLastIndexKey;
    /**
     * Flag to maintain whether next set of collaboration posts data is available or not.
     */
    @State
    boolean mCollabPostsRequestMoreData;
    /**
     * Flag to store last index key of re-posted posts data.
     */
    @State
    String mRePostsLastIndexKey;
    /**
     * Flag to maintain whether next set of re posts data is available or not.
     */
    @State
    boolean mRePostsRequestMoreData;


    MeAdapter mAdapter;
    Unbinder mUnbinder;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    private int[] mLayouts;

    private ArrayList<String> mSelectedInterest = new ArrayList<>();
    private ObjectAnimator mAnimator;


    Constant.ITEM_TYPES defaultItemType;

    @State
    String mItemType;


    /**
     * Parent view of live filter
     */
    FrameLayout mFrameLayout;
    RelativeLayout mWaterMarkView;


    /**
     * Flag to maintain live filter value.
     */
    @State
    String mLiveFilter;


    @State
    String mEntityID, mEntityType;
    Bitmap mBitmap;

    @State
    String mShareOption = SHARE_OPTION_OTHER;

    FeedModel mFeedData;
    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Obtain preference helper
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
        //Unbind butterKnife view binding
        mUnbinder.unbind();
        //Dispose CompositeDisposable
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
                    ViewHelper.getSnackBar(rootView, getString(R.string.error_img_not_attached));
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
                    int position = bundle.getInt("position");
                    //Update data
                    if (tabLayout.getSelectedTabPosition() == 2) {
                        mCollabList.get(position).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                        mCollabList.get(position).setHatsOffCount(bundle.getLong("hatsOffCount"));
                        mCollabList.get(position).setFollowStatus(bundle.getBoolean("followstatus"));
                        mCollabList.get(position).setDownvoteStatus(bundle.getBoolean("downvotestatus"));
                        //mCollabList.get(bundle.getInt("position")).setLiveFilterName(bundle.getString("filtername"));

                        updateFollowForAll(mCollabList.get(bundle.getInt("position")), mCollabList);

                    } else {
                        mUserPostDataList.get(position).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                        mUserPostDataList.get(position).setHatsOffCount(bundle.getLong("hatsOffCount"));
                        mUserPostDataList.get(position).setFollowStatus(bundle.getBoolean("followstatus"));
                        mUserPostDataList.get(position).setCaption(bundle.getString("caption"));
                        mUserPostDataList.get(position).setDownvoteStatus(bundle.getBoolean("downvotestatus"));
                        mUserPostDataList.get(position).setLiveFilterName(bundle.getString("filtername"));
                        updateFollowForAll(mUserPostDataList.get(position), mUserPostDataList);

                        if (bundle.getBoolean("deletestatus")) {
                            mUserPostDataList.remove(bundle.getInt("position"));
                            mAdapter.notifyItemRemoved(bundle.getInt("position") + 1);
                        }

                        ImagePipeline imagePipeline = Fresco.getImagePipeline();
                        imagePipeline.evictFromCache(Uri.parse(mUserPostDataList.get(position).getContentImage()));
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
            if (updatesMenuItem != null) {
                //Change action flag for updates icon
                updatesMenuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_royalties:
                startRoyaltiesActivity();
                return true;
            case R.id.action_chat_with_cread:
                //Open chat details screen
                IntentHelper.openChatWithCreadKalakaar(getActivity());
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

    //endregion

    //region :Click functionality

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
    @OnClick(R.id.buttonProfileSettings)
    public void onUserNameClicked() {
        //Hide dot indicator view
        dotIndicatorWebStoreLink.setVisibility(View.GONE);
        //If profile is editable
        if (isProfileEditable) {
            Intent intent = new Intent(getActivity(), UpdateProfileDetailsActivity.class);
            intent.putExtra(EXTRA_USER_FIRST_NAME, mFirstName);
            intent.putExtra(EXTRA_USER_LAST_NAME, mLastName);
            intent.putExtra(EXTRA_USER_EMAIL, mEmail);
            intent.putExtra(EXTRA_USER_BIO, mUserBio);
            intent.putExtra(EXTRA_USER_CONTACT, mContactNumber);
            intent.putExtra(EXTRA_USER_WEB_STORE_LINK, mWebStoreUrl);
            intent.putExtra(EXTRA_USER_WATER_MARK_STATUS, mWaterMarkStatus);
            intent.putExtra(EXTRA_TOP_USER_INTERESTS, mSelectedInterest);
            intent.putExtra(EXTRA_USER_INTERESTS_COUNT, mInterestCount);
            intent.putExtra(EXTRA_PROFILE_PIC_URL, mProfilePicURL);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_PROFILE_DETAILS);
        } else {
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title("Web Profile Link")
                    .positiveText("Copy link")
                    .customView(R.layout.dialog_profile_lweb_ink, false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // Gets a handle to the clipboard service.
                            ClipboardManager manager = (ClipboardManager)
                                    getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            // Creates a new text clip to put on the clipboard
                            ClipData clip = ClipData.newPlainText("webStoreLink", mWebStoreUrl);
                            // Set the clipboard's primary clip.
                            manager.setPrimaryClip(clip);
                            ViewHelper.getSnackBar(rootView, "Link copied to clipboard");
                        }
                    })
                    .show();
            AppCompatTextView linkText = dialog.getCustomView().findViewById(R.id.textProfileLink);
            linkText.setText(mWebStoreUrl +
                    "\n\nThis is the web profile link of " + mFirstName + ".");
        }
        //Update status
        mHelper.updateWebStoreDotIndicatorStatus(false);
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
            FirebaseEventHelper.logFollowFromProfileEvent(getActivity()
                    , mHelper.getUUID());
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
            //Method called
            updateChatSeenStatus();

            //if its not null
            if (mAnimator != null) {
                mAnimator.cancel();
            }
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
     * Click functionality to show tooltip for featured artist.
     */
    @OnClick(R.id.imageFeatured)
    void featuredOnClick() {
        String dialogDesc = mFirstName + " is a " + mFeatureCount + " times featured artist.";
        String dialogTitle = getString(R.string.text_title_dialog_featured_artist_me);

        CustomDialog.getGenericDialog(getActivity(),
                getString(R.string.text_ok),
                TextUtils.getSpannedString(dialogTitle,
                        new RelativeSizeSpan(1f),
                        0,
                        0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE),
                TextUtils.getSpannedString(dialogDesc,
                        new RelativeSizeSpan(1.35f),
                        dialogDesc.indexOf(String.valueOf(mFeatureCount)),
                        dialogDesc.indexOf(String.valueOf(mFeatureCount)) + String.valueOf(mFeatureCount).length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE),
                R.drawable.img_intro_feat_artist);
    }

    /**
     * Top artist click functionality to show tooltip.
     */
    @OnClick(R.id.view_top_artist)
    void topArtistOnClick() {
        //Show tooltip
        ViewHelper.getToolTip(viewTopArtist
                , getString(R.string.text_top_artist)
                , getActivity());
    }

    /*
    * Create button click functionality.
    * */
    @OnClick(R.id.buttonCreate)
    void onCreateClick() {
        ((BottomNavigationActivity) getActivity()).getAddContentBottomSheetDialog();
    }


    /*
    * Close  button click functionality.
    * */
    @OnClick(R.id.btn_close)
    void onBtnClose() {
        //Hide bottom sheet
        achievementsSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    /**
     * Badge click functionality.
     */
    void badgeClickFunctionality() {
        //If user can edit his/her profile
        if (mRequestedUUID.equals(mHelper.getUUID())) {
            IntentHelper.openAchievementsActivity(getActivity(), mRequestedUUID);
        } else {
            if (achievementsSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                //Hide bottom sheet
                achievementsSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else if (achievementsSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                loadUserAchievementsData();
            }
        }
    }

    //endregion

    //region :Private methods

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
            //Change settings drawable
            buttonProfileSettings.setImageDrawable(ContextCompat.getDrawable(getActivity()
                    , R.drawable.ic_link_vector));
        }

        //Condition to toggle visibility of follow button and chat list icon
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

        //if first time
        if (mHelper.isWebStoreDotIndicatorFirstTime()) {
            //Show dot indicator view
            dotIndicatorWebStoreLink.setVisibility(View.VISIBLE);
        }

        //initialize tab layout
        initTabLayout(tabLayout);
        initUserStatsPager();
        initSwipeRefreshLayout();

        //Setup achievement bottom sheets
        achievementsSheetBehavior = BottomSheetBehavior.from(nsAchievements);
        achievementsSheetBehavior.setPeekHeight(0);
    }

    /**
     * Method to initialize tab layout.
     *
     * @param tabLayout TabLayout.
     */
    private void initTabLayout(TabLayout tabLayout) {
        //SetUp tabs
        setUpTabs(tabLayout, getActivity());
        //Listener for tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //To change tab icon color from grey to primary color
                tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

                // hide no data view if it's visible
                viewNoData.setVisibility(View.GONE);
                if (defaultItemType == GRID) {
                    recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                } else {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
                //Method called
                updateItemType(tab.getPosition(), defaultItemType);
                switch (tab.getPosition()) {
                    case 0:
                        mAdapter = new MeAdapter(mUserPostDataList, getActivity(), mHelper.getUUID(), MeFragment.this, mItemType, mCompositeDisposable);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        break;
                    case 1:
                        mCompositeDisposable.clear();
                        mRepostDataList.clear();
                        mRePostsLastIndexKey = null;
                        mRePostsRequestMoreData = false;
                        mAdapter = new MeAdapter(mRepostDataList, getActivity(), mHelper.getUUID(), MeFragment.this, mItemType, mCompositeDisposable);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        getUserRePostsData(false);
                        break;
                    case 2:
                        mCompositeDisposable.clear();
                        mCollabList.clear();
                        mCollabPostsLastIndexKey = null;
                        mCollabPostsRequestMoreData = false;
                        mAdapter = new MeAdapter(mCollabList, getActivity(), mHelper.getUUID(), MeFragment.this, mItemType, mCompositeDisposable);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        getCollabData(false);
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
                if (defaultItemType == GRID) {
                    //Set layout manger
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    //Update flag
                    defaultItemType = ITEM_TYPES.LIST;
                    //update shared preference
                    mHelper.setFeedItemType(LIST);
                } else if (defaultItemType == LIST) {
                    //Set layout manger
                    recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                    //Update flag
                    defaultItemType = ITEM_TYPES.GRID;
                    //update shared preference
                    mHelper.setFeedItemType(GRID);
                }
                //Method called
                updateItemType(tab.getPosition(), defaultItemType);
                //Change GRID to LIST and vice versa
                switch (tab.getPosition()) {
                    //uploaded posts
                    case 0:
                        //Set adapter
                        mAdapter = new MeAdapter(mUserPostDataList, getActivity(), mHelper.getUUID(), MeFragment.this, mItemType, mCompositeDisposable);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        break;
                    //re-posted posts
                    case 1:
                        //Set adapter
                        mAdapter = new MeAdapter(mRepostDataList, getActivity(), mHelper.getUUID(), MeFragment.this, mItemType, mCompositeDisposable);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        break;
                    //Collaboration posts
                    case 2:
                        //Set adapter
                        mAdapter = new MeAdapter(mCollabList, getActivity(), mHelper.getUUID(), MeFragment.this, mItemType, mCompositeDisposable);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * Method to add tab items to tabLayout.
     *
     * @param tabLayout TabLayout where item to be added.
     * @param context   Context to use.
     */
    private void setUpTabs(TabLayout tabLayout, Context context) {
        //Add tab items
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_me_all_tab));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_repost));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_collab));

        //Select first tab
        tabLayout.getTabAt(0).select();
        //initialize tabs icon tint
        tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(context
                , R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(context
                , R.color.grey_custom), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(ContextCompat.getColor(context
                , R.color.grey_custom), PorterDuff.Mode.SRC_IN);
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
                    case BADGES:
                        //Method called
                        badgeClickFunctionality();
                        break;
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
                                mFeatureCount = mainData.getLong("featurecount");
                                mPostCount = mainData.getLong("postcount");
                                mBadgeCount = mainData.getLong("badgecount");
                                mFollowerCount = mainData.getLong("followercount");
                                mFollowingCount = mainData.getLong("followingcount");
                                mHatsoffCount = mainData.getLong("hatsoffscount");
                                mCommentsCount = mainData.getLong("commentscount");
                                mCollaborationCount = mainData.getLong("collaborationscount");
                                mEmail = mainData.getString("email");
                                mContactNumber = mainData.getString("phone");
                                mWaterMarkStatus = mainData.getString("watermarkstatus");
                                mIsFeatured = mainData.getBoolean("featured");
                                mWebStoreUrl = mainData.getString("web_profile_link");
                                mIsTopArtist = mainData.getBoolean("topartist");


                                mInterestCount = mainData.getLong("interestcount");

                                JSONArray interestArray = mainData.getJSONArray("topinterests");
                                for (int i = 0; i < interestArray.length(); i++) {
                                    mSelectedInterest.add(interestArray.getString(i));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "MeFragment");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeToRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "MeFragment");
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
                                                       if (viewPagerUserStats != null) {
                                                           ((TextView) viewPagerUserStats.findViewWithTag(POSTS)).setText(String.valueOf(mPostCount));
                                                           ((TextView) viewPagerUserStats.findViewWithTag(BADGES)).setText(String.valueOf(mBadgeCount) + " badges");
                                                           ((TextView) viewPagerUserStats.findViewWithTag(FOLLOWERS)).setText(String.valueOf(mFollowerCount));
                                                           ((TextView) viewPagerUserStats.findViewWithTag(FOLLOWING)).setText(String.valueOf(mFollowingCount));
                                                           ((TextView) viewPagerUserStats.findViewWithTag(HATSOFF)).setText(String.valueOf(mHatsoffCount));
                                                           ((TextView) viewPagerUserStats.findViewWithTag(COMMENT)).setText(String.valueOf(mCommentsCount));
                                                           ((TextView) viewPagerUserStats.findViewWithTag(COLLABORATIONS)).setText(String.valueOf(mCollaborationCount));
                                                       }

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

                            //check featured status
                            if (mIsFeatured) {
                                imageFeatured.setVisibility(View.VISIBLE);
                            } else {
                                imageFeatured.setVisibility(View.GONE);
                            }

                            //check top artist status
                            if (mIsTopArtist) {
                                viewTopArtist.setVisibility(View.VISIBLE);
                            } else {
                                viewTopArtist.setVisibility(View.GONE);
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

                            // check if badge intro for first time
                            if (mHelper.isBadgeIntroFirstTime()) {
                                mAnimator = ObjectAnimator.ofFloat((TextView) viewPagerUserStats.findViewWithTag(BADGES)
                                        , "translationX"
                                        , 0, 13, 0)
                                ;
                                mAnimator.setInterpolator(new InOutInterpolator());
                                mAnimator.setStartDelay(500);
                                mAnimator.setDuration(3000);
                                mAnimator.setRepeatCount(8);
                                mAnimator.start();
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
    public static void loadUserPicture(String picUrl, CircleImageView imageView, Context context) {
        Picasso.with(context)
                .load(picUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.ic_account_circle_100)
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
                        //Set result ok
                        getActivity().setResult(RESULT_OK);
                    }

                    @Override
                    public void onFollowFailure(String errorMsg) {

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
     * Method to initialize swipe to refresh view.
     */
    private void initSwipeRefreshLayout() {
        //Method called
        initItemTypePreference();

        swipeToRefreshLayout.setRefreshing(true);
        swipeToRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));

        //Load profile data
        loadProfileData();
        //Initialize listeners for normal list
        initListeners();
    }


    /**
     * Method to set user posts view preference. i.e GRID or LIST.
     */
    private void initItemTypePreference() {
        defaultItemType = mHelper.getFeedItemType();
        if (defaultItemType == GRID) {
            //Set layout manger for recyclerView
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else if (mHelper.getFeedItemType() == LIST) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
        //Method called
        updateItemType(tabLayout.getSelectedTabPosition(), defaultItemType);
        //Set adapter
        mAdapter = new MeAdapter(mUserPostDataList, getActivity(), mHelper.getUUID(), MeFragment.this, mItemType, mCompositeDisposable);
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
            getUserPostsData(false);
        } else {
            swipeToRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * Initializes the swipe refresh listener based on the selected tab position.
     *
     * @param selectedTabPosition Position of selected tab.
     */
    private void initOnSwipeRefreshListener(final int selectedTabPosition) {
        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (selectedTabPosition) {
                    //User posts
                    case 0:
                        //Clear data
                        mUserPostDataList.clear();
                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to null
                        mUserPostsLastIndexKey = null;
                        //Load data here
                        getUserPostsData(false);
                        break;
                    // Re-posts
                    case 1:
                        //Clear data
                        mRepostDataList.clear();

                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to null
                        mRePostsLastIndexKey = null;
                        //Load data here
                        getUserRePostsData(false);
                        break;
                    //Collaboration posts
                    case 2:
                        //Clear data
                        mCollabList.clear();
                        //Notify for changes
                        mAdapter.notifyDataSetChanged();
                        mAdapter.setLoaded();
                        //set last index key to null
                        mCollabPostsLastIndexKey = null;
                        //Load data here
                        getCollabData(false);

                }

            }
        });


    }

    /**
     * RxJava2 implementation for retrieving user posts data from server.
     *
     * @param isLoadNextData True to load next set of data false otherwise.
     */
    private void getUserPostsData(final boolean isLoadNextData) {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(MeNetworkManager.getUserPostsObservable(BuildConfig.URL + "/user-profile/load-timeline"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mRequestedUUID
                , mUserPostsLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_ME)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //if load next set of user posts data
                        if (isLoadNextData) {
                            //Remove loading item
                            mUserPostDataList.remove(mUserPostDataList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mUserPostDataList.size());
                        }
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                //Method called
                                parsePostsData(jsonObject, isLoadNextData);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //if load next set of user posts data
                        if (isLoadNextData) {
                            //Remove loading item
                            mUserPostDataList.remove(mUserPostDataList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mUserPostDataList.size());
                        }
                        swipeToRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
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
                        }
                        //No data
                        else if (mUserPostDataList.size() == 0) {
                            if (isProfileEditable) {
                                //Show no data view
                                viewNoData.setVisibility(View.VISIBLE);
                            } else {
                                //Hide no data view
                                viewNoData.setVisibility(View.GONE);
                            }
                            ViewHelper.getSnackBar(rootView, getString(R.string.message_no_post));
                        } else {
                            //if load next set of user posts data
                            if (isLoadNextData) {
                                //Notify changes
                                mAdapter.setLoaded();
                            } else {
                                //set to false
                                CreadApp.GET_RESPONSE_FROM_NETWORK_ME = false;
                                //Apply 'Slide Up' animation
                                recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity()
                                        , R.anim.layout_animation_from_bottom));
                                mAdapter.notifyDataSetChanged();
                                //Hide no data view
                                viewNoData.setVisibility(View.GONE);
                            }

                        }
                    }
                })
        );
    }

    /**
     * Method to parse Json object.
     *
     * @param jsonObject JsonObject to be parsed.
     * @param isLoadMore Whether called from load more or not.
     * @throws JSONException
     */
    private void parsePostsData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {
        JSONObject mainData = jsonObject.getJSONObject("data");
        mUserPostsRequestMoreData = mainData.getBoolean("requestmore");
        mUserPostsLastIndexKey = mainData.getString("lastindexkey");
        mCanDownvote = mainData.getBoolean("candownvote");
        //User posts array list
        JSONArray UserPostsArray = mainData.getJSONArray("items");
        for (int i = 0; i < UserPostsArray.length(); i++) {
            JSONObject dataObj = UserPostsArray.getJSONObject(i);
            String type = dataObj.getString("type");

            FeedModel data = new FeedModel();
            data.setEntityID(dataObj.getString("entityid"));
            data.setContentType(dataObj.getString("type"));
            data.setUUID(dataObj.getString("uuid"));
            data.setCreatorImage(dataObj.getString("profilepicurl"));
            data.setCreatorName(dataObj.getString("creatorname"));
            data.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
            data.setMerchantable(dataObj.getBoolean("merchantable"));
            data.setDownvoteStatus(dataObj.getBoolean("downvotestatus"));
            data.setEligibleForDownvote(mCanDownvote);
            data.setPostTimeStamp(dataObj.getString("regdate"));
            data.setLongForm(dataObj.getBoolean("long_form"));
            data.setHatsOffCount(dataObj.getLong("hatsoffcount"));
            data.setCommentCount(dataObj.getLong("commentcount"));
            data.setContentImage(dataObj.getString("entityurl"));
            data.setFollowStatus(dataObj.getBoolean("followstatus"));
            data.setCollabCount(dataObj.getLong("collabcount"));
            data.setLiveFilterName(dataObj.getString("livefilter"));
            //if image width or image height is null
            if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                data.setImgWidth(1);
                data.setImgHeight(1);
            } else {
                data.setImgWidth(dataObj.getInt("img_width"));
                data.setImgHeight(dataObj.getInt("img_height"));
            }
            if (dataObj.isNull("caption")) {
                data.setCaption(null);
            } else {
                data.setCaption(dataObj.getString("caption"));
            }

            // if capture
            if (type.equals(CONTENT_TYPE_CAPTURE)) {
                //Retrieve "CAPTURE_ID" if type is capture
                data.setCaptureID(dataObj.getString("captureid"));
                //if key cpshort exists
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
            }
            //if short
            else if (type.equals(CONTENT_TYPE_SHORT)) {
                //Retrieve "SHORT_ID" if type is short
                data.setShortID(dataObj.getString("shoid"));
                //if key shcapture exists
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
            mUserPostDataList.add(data);
            //Called from load more
            if (isLoadMore) {
                //Notify item insertion
                mAdapter.notifyItemInserted(mUserPostDataList.size() - 1);
            }
        }
    }


    /**
     * RxJava2 implementation for retrieving collaboration posts data from server.
     *
     * @param isLoadNextData True to load next set of data false otherwise.
     */
    private void getCollabData(final boolean isLoadNextData) {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        //if load first set of collaboration posts data
        if (!isLoadNextData) {
            swipeToRefreshLayout.setRefreshing(true);
        }
        requestServer(mCompositeDisposable,
                MeNetworkManager.getCollaborationPostsObservable(BuildConfig.URL + "/user-profile/load-collab-timeline",
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        mRequestedUUID,
                        mCollabPostsLastIndexKey,
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
                        //if load next set of collaboration posts data
                        if (isLoadNextData) {
                            //Remove loading item
                            mCollabList.remove(mCollabList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mCollabList.size());
                        }
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                parseCollabData(jsonObject, isLoadNextData);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        //if load next set of collaboration posts data
                        if (isLoadNextData) {
                            //Remove loading item
                            mCollabList.remove(mCollabList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mCollabList.size());
                        }
                        e.printStackTrace();
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
                            if (isProfileEditable) {
                                //Show no data view
                                viewNoData.setVisibility(View.VISIBLE);
                            } else {
                                //Show snack bar
                                viewNoData.setVisibility(View.GONE);
                                ViewHelper.getSnackBar(rootView, "No collaboration yet");
                            }

                        } else {
                            //if load next set of collaboration posts data
                            if (isLoadNextData) {
                                //Notify changes
                                mAdapter.setLoaded();
                            } else {
                                //Apply 'Slide Up' animation
                                recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity()
                                        , R.anim.layout_animation_from_bottom));
                                mAdapter.notifyDataSetChanged();
                            }
                            //Hide no data view
                            viewNoData.setVisibility(View.GONE);
                        }
                    }
                });

    }

    /**
     * Method to parse Json object.
     *
     * @param jsonObject JsonObject to be parsed.
     * @param isLoadMore Whether called from load more or not.
     * @throws JSONException
     */
    private void parseCollabData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {
        JSONObject mainData = jsonObject.getJSONObject("data");
        mCollabPostsRequestMoreData = mainData.getBoolean("requestmore");
        mCollabPostsLastIndexKey = mainData.getString("lastindexkey");
        mCanDownvote = mainData.getBoolean("candownvote");
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
            data.setDownvoteStatus(dataObj.getBoolean("downvotestatus"));
            data.setEligibleForDownvote(mCanDownvote);
            data.setPostTimeStamp(dataObj.getString("regdate"));
            data.setLongForm(dataObj.getBoolean("long_form"));
            data.setHatsOffCount(dataObj.getLong("hatsoffcount"));
            data.setCommentCount(dataObj.getLong("commentcount"));
            data.setContentImage(dataObj.getString("entityurl"));
            data.setFollowStatus(dataObj.getBoolean("followstatus"));
            data.setCollabCount(dataObj.getLong("collabcount"));
            data.setLiveFilterName(dataObj.getString("livefilter"));
            //if image width pr image height is null
            if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                data.setImgWidth(1);
                data.setImgHeight(1);
            } else {
                data.setImgWidth(dataObj.getInt("img_width"));
                data.setImgHeight(dataObj.getInt("img_height"));
            }
            if (dataObj.isNull("caption")) {
                data.setCaption(null);
            } else {
                data.setCaption(dataObj.getString("caption"));
            }

            // if capture
            if (type.equals(CONTENT_TYPE_CAPTURE)) {
                //Retrieve "CAPTURE_ID" if type is capture
                data.setCaptureID(dataObj.getString("captureid"));
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
            }
            // if short
            else if (type.equals(CONTENT_TYPE_SHORT)) {
                //Retrieve "SHORT_ID" if type is short
                data.setShortID(dataObj.getString("shoid"));
                //if key shcapture exists
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

    /**
     * RxJava2 implementation for retrieving user re-posts data from server.
     *
     * @param isLoadNextData True to load next set of data false otherwise.
     */
    private void getUserRePostsData(final boolean isLoadNextData) {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        //if load first set of collaboration posts data
        if (!isLoadNextData) {
            swipeToRefreshLayout.setRefreshing(true);
        }

        mCompositeDisposable.add(MeNetworkManager.getUserRePostsObservable(BuildConfig.URL + "/user-profile/load-repost-timeline"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mRequestedUUID
                , mRePostsLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_ME)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //if load next set of re posts data
                        if (isLoadNextData) {
                            //Remove loading item
                            mRepostDataList.remove(mRepostDataList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mRepostDataList.size());
                        }
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                //Method called
                                parseRePostsData(jsonObject, isLoadNextData);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //if load next set of re posts data
                        if (isLoadNextData) {
                            //Remove loading item
                            mRepostDataList.remove(mRepostDataList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mRepostDataList.size());
                        }
                        swipeToRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
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
                        }
                        //No data
                        else if (mRepostDataList.size() == 0) {
                            if (isProfileEditable) {
                                //Show no data view
                                viewNoData.setVisibility(View.VISIBLE);
                            } else {
                                //Hide no data view
                                viewNoData.setVisibility(View.GONE);
                            }
                            ViewHelper.getSnackBar(rootView, getString(R.string.message_no_post));
                        } else {
                            //if load next set of re posts data
                            if (isLoadNextData) {
                                //Notify changes
                                mAdapter.setLoaded();
                            } else {
                                //set to false
                                CreadApp.GET_RESPONSE_FROM_NETWORK_ME = false;
                                //Apply 'Slide Up' animation
                                recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity()
                                        , R.anim.layout_animation_from_bottom));
                                mAdapter.notifyDataSetChanged();
                                //Hide no data view
                                viewNoData.setVisibility(View.GONE);
                            }

                        }
                    }
                })
        );
    }

    /**
     * Method to parse Json object.
     *
     * @param jsonObject JsonObject to be parsed.
     * @param isLoadMore Whether called from load more or not.
     * @throws JSONException
     */
    private void parseRePostsData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {
        JSONObject mainData = jsonObject.getJSONObject("data");
        mRePostsRequestMoreData = mainData.getBoolean("requestmore");
        mRePostsLastIndexKey = mainData.getString("lastindexkey");
        mCanDownvote = mainData.getBoolean("candownvote");
        //User posts array list
        JSONArray UserPostsArray = mainData.getJSONArray("items");
        for (int i = 0; i < UserPostsArray.length(); i++) {
            JSONObject dataObj = UserPostsArray.getJSONObject(i);
            String type = dataObj.getString("type");

            FeedModel data = new FeedModel();
            data.setEntityID(dataObj.getString("entityid"));
            data.setContentType(dataObj.getString("type"));
            data.setUUID(dataObj.getString("uuid"));
            data.setCreatorImage(dataObj.getString("profilepicurl"));
            data.setCreatorName(dataObj.getString("creatorname"));
            data.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
            data.setMerchantable(dataObj.getBoolean("merchantable"));
            data.setDownvoteStatus(dataObj.getBoolean("downvotestatus"));
            data.setEligibleForDownvote(mCanDownvote);
            //data.setPostTimeStamp(dataObj.getString("regdate"));
            data.setPostTimeStamp(dataObj.getString("postdate"));
            data.setLongForm(dataObj.getBoolean("long_form"));
            data.setHatsOffCount(dataObj.getLong("hatsoffcount"));
            data.setCommentCount(dataObj.getLong("commentcount"));
            data.setContentImage(dataObj.getString("entityurl"));
            data.setFollowStatus(dataObj.getBoolean("followstatus"));
            data.setCollabCount(dataObj.getLong("collabcount"));
            data.setLiveFilterName(dataObj.getString("livefilter"));
            //data.setRepostDate(dataObj.getString("repostdate"));
            data.setRepostDate(dataObj.getString("regdate"));
            data.setReposterUUID(dataObj.getString("reposteruuid"));
            data.setReposterName(dataObj.getString("repostername"));
            data.setRepostID(dataObj.getString("repostid"));

            //if image width or image height is null
            if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                data.setImgWidth(1);
                data.setImgHeight(1);
            } else {
                data.setImgWidth(dataObj.getInt("img_width"));
                data.setImgHeight(dataObj.getInt("img_height"));
            }
            if (dataObj.isNull("caption")) {
                data.setCaption(null);
            } else {
                data.setCaption(dataObj.getString("caption"));
            }

            // if capture
            if (type.equals(CONTENT_TYPE_CAPTURE)) {
                //Retrieve "CAPTURE_ID" if type is capture
                data.setCaptureID(dataObj.getString("captureid"));
                //if key cpshort exists
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
            }
            //if short
            else if (type.equals(CONTENT_TYPE_SHORT)) {
                //Retrieve "SHORT_ID" if type is short
                data.setShortID(dataObj.getString("shoid"));
                //if key shcapture exists
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
            mRepostDataList.add(data);
            //Called from load more
            if (isLoadMore) {
                //Notify item insertion
                mAdapter.notifyItemInserted(mRepostDataList.size() - 1);
            }
        }
    }


    private void initListeners() {
        initLoadMoreListener(tabLayout.getSelectedTabPosition());
        initHatsOffListener(mAdapter);
        initializeDeleteListener(mAdapter);
        initShareListener(mAdapter);
        initGifShareListener(mAdapter);
        initRepostDeleteListener(mAdapter);

        // init swipe refresh listener for list
        initOnSwipeRefreshListener(tabLayout.getSelectedTabPosition());
    }

    /**
     * Initialize load more listener.
     *
     * @param selectedTabPosition currently selected tab position
     */
    private void initLoadMoreListener(final int selectedTabPosition) {
        mAdapter.setUserActivityLoadMoreListener(new listener.OnUserActivityLoadMoreListener() {
            @Override
            public void onLoadMore() {
                switch (selectedTabPosition) {
                    //User posts
                    case 0:
                        if (mUserPostsRequestMoreData) {

                            new Handler().post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       mUserPostDataList.add(null);
                                                       mAdapter.notifyItemInserted(mUserPostDataList.size() - 1);
                                                   }
                                               }
                            );
                            //Load new set of data
                            getUserPostsData(true);
                        }

                        break;
                    //Re-posts
                    case 1:
                        if (mRePostsRequestMoreData) {
                            new Handler().post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       mRepostDataList.add(null);
                                                       mAdapter.notifyItemInserted(mRepostDataList.size() - 1);
                                                   }
                                               }
                            );
                            //Load new set of data
                            getUserRePostsData(true);
                        }
                        break;
                    //Collaboration post
                    case 2:
                        if (mCollabPostsRequestMoreData) {
                            new Handler().post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       mCollabList.add(null);
                                                       mAdapter.notifyItemInserted(mCollabList.size() - 1);
                                                   }
                                               }
                            );
                            //Load new set of data
                            getCollabData(true);
                        }
                }
            }
        });
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
                // On hatsOffFailureListener
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
     *
     * @param meAdapter MeAdapter reference.
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
     * Initialize delete listener
     *
     * @param meAdapter MeAdapter reference.
     */
    private void initRepostDeleteListener(MeAdapter meAdapter) {
        meAdapter.setOnRepostDeleteListener(new listener.OnRepostDeleteListener() {
            @Override
            public void onDelete(String repostID, final int position) {

                RepostNetworkManager.deleteRepost(getActivity(), mCompositeDisposable, repostID, new RepostNetworkManager.OnRepostDeleteListener() {
                    @Override
                    public void onSuccess() {
                        //Remove item from list and notify changes
                        mRepostDataList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        mAdapter.notifyItemRangeChanged(position, mRepostDataList.size());
                        ViewHelper.getSnackBar(rootView, "Post deleted");
                        CreadApp.GET_RESPONSE_FROM_NETWORK_ME = true;
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
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
        final MaterialDialog dialog = CustomDialog.getProgressDialog(getActivity(), "Deleting...");

        DeletePostHelper.deletePost(getActivity(),
                mCompositeDisposable,
                entityID,
                new listener.OnDeleteRequestedListener() {
                    @Override
                    public void onDeleteSuccess() {
                        dialog.dismiss();
                        //Remove item from list and notify changes
                        mUserPostDataList.remove(itemPosition);
                        mAdapter.notifyItemRemoved(itemPosition);
                        mAdapter.notifyItemRangeChanged(itemPosition, mUserPostDataList.size());

                        ViewHelper.getSnackBar(rootView, "Item deleted");
                        //Update user post count
                        mPostCount -= 1;
                        ((TextView) viewPagerUserStats.findViewWithTag(POSTS)).setText(String.valueOf(mPostCount));
                    }


                    @Override
                    public void onDeleteFailure(String errorMsg) {
                        dialog.dismiss();
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }


    /**
     * Initialize share listener.
     */
    private void initShareListener(MeAdapter meAdapter) {
        meAdapter.setOnShareListener(new listener.OnShareListener() {
            @Override
            public void onShareClick(Bitmap bitmap, FeedModel data, String shareOption) {
                mBitmap = bitmap;
                mFeedData = data;
                mShareOption = shareOption;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    // generates deep link
                    // and opens the share dialog
                    getDeepLinkOnValidShareOption(getActivity(),
                            mCompositeDisposable,
                            rootView,
                            mHelper.getUUID(),
                            mHelper.getAuthToken(),
                            data,
                            bitmap,
                            mShareOption);

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
     * Initialize gif share listener.
     */
    private void initGifShareListener(MeAdapter meAdapter) {
        meAdapter.setOnGifShareListener(new listener.OnGifShareListener() {
            @Override
            public void onGifShareClick(FrameLayout frameLayout, String shareOption, RelativeLayout watermarkView, String liveFilter) {
                mFrameLayout = frameLayout;
                mShareOption = shareOption;
                mWaterMarkView = watermarkView;
                mLiveFilter = liveFilter;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    new GifHelper(getActivity(), mBitmap, frameLayout, shareOption, true, watermarkView, mLiveFilter)
                            .startHandlerTask(new Handler(), 0);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(MeFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_share_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(MeFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, shareGifPermission);
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
            getDeepLinkOnValidShareOption(getActivity(),
                    mCompositeDisposable,
                    rootView,
                    mHelper.getUUID(),
                    mHelper.getAuthToken(),
                    mFeedData,
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
     * Used to handle result of askForPermission for gif sharing
     */
    PermissionCallback shareGifPermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //We have permission do whatever you want to do
            new GifHelper(getActivity(), mBitmap, mFrameLayout, mShareOption, true, mWaterMarkView, mLiveFilter)
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
            Crashlytics.logException(e);
            Crashlytics.setString("className", "MeFragment");
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
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "MeFragment");
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


    private void startGratitudeScroll() {
        viewPagerUserStats.setCurrentItem(1);
        mHelper.updateGratitudeScroll(false);
    }

    public static boolean isCountZero(long count) {
        return count == 0;
    }

    public static boolean isCountOne(long count) {
        return count == 1;
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
            mAnimator = ObjectAnimator.ofFloat(fabChat, "translationX", 0, 25, 0);
            mAnimator.setInterpolator(new InOutInterpolator());
            mAnimator.setStartDelay(500);
            mAnimator.setDuration(3000);
            mAnimator.setRepeatCount(8);
            mAnimator.start();
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //if this view is not null
                if (fabChat != null) {
                    //Scroll Down
                    if (dy > 0 && fabChat.getVisibility() == View.VISIBLE) {
                        fabChat.hide();
                    }
                    //Scroll Up
                    else if (dy < 0 && fabChat.getVisibility() != View.VISIBLE) {
                        fabChat.show();
                    }
                }
            }
        });
    }

    /**
     * Method to launch screen where user can see list of people whom he/she is following.
     */
    public void onFollowingContainerClicked() {

        if (mFollowingCount > 0) {
            IntentHelper.openFollowActivity(getActivity()
                    , mRequestedUUID, "following");
        } else {
            ViewHelper.getSnackBar(rootView, "User is not following anyone");
        }
    }

    /**
     * Method  to launch followers screen.
     */
    public void onFollowersContainerClicked() {
        if (mFollowerCount > 0) {
            IntentHelper.openFollowActivity(getActivity()
                    , mRequestedUUID, "followers");
        } else {
            ViewHelper.getSnackBar(rootView, "No followers");
        }
    }

    /**
     * Method to update chat seen status.
     */
    private void updateChatSeenStatus() {
        NotificationNetworkManager.updateChatSeenStatus(getActivity()
                , mCompositeDisposable
                , new NotificationNetworkManager.OnChatSeenUpdateListener() {
                    @Override
                    public void onSuccess() {
                        //update flags in SharedPreference
                        mHelper.setPersonalChatIndicatorStatus(false);
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });
    }


    /**
     * Method to update itemType.
     *
     * @param selectedTabPosition Position of selected tab.
     * @param itemTypes           View type i.e LIST or GRID
     */
    private void updateItemType(int selectedTabPosition, ITEM_TYPES itemTypes) {
        if (selectedTabPosition == 0 && itemTypes == LIST) {
            mItemType = Constant.ME_ITEM_TYPE_USER_POST_LIST;
        } else if (selectedTabPosition == 0 && itemTypes == GRID) {
            mItemType = Constant.ME_ITEM_TYPE_USER_POST_GRID;
        } else if (selectedTabPosition == 1 && itemTypes == LIST) {
            mItemType = Constant.ME_ITEM_TYPE_RE_POST_LIST;
        } else if (selectedTabPosition == 1 && itemTypes == GRID) {
            mItemType = Constant.ME_ITEM_TYPE_RE_POST_GRID;
        } else if (selectedTabPosition == 2 && itemTypes == LIST) {
            mItemType = Constant.ME_ITEM_TYPE_COLLAB_POST_LIST;
        } else if (selectedTabPosition == 2 && itemTypes == GRID) {
            mItemType = Constant.ME_ITEM_TYPE_COLLAB_POST_GRID;
        }
    }

    /**
     * Load achievements data.
     */
    private void loadUserAchievementsData() {
        final List<AchievementsModels> achievementDataList = new ArrayList<>();
        //Show loading view
        progressView.setVisibility(View.VISIBLE);
        AchivementNetworkManager.getAchievementsData(getActivity(), mCompositeDisposable, mRequestedUUID, new AchivementNetworkManager.OnAchievementsLoadListener() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {

                    JSONObject mainData = jsonObject.getJSONObject("data");
                    //Achievements list
                    JSONArray achievementArray = mainData.getJSONArray("items");
                    for (int i = 0; i < achievementArray.length(); i++) {
                        AchievementsModels data = new AchievementsModels();

                        JSONObject dataObj = achievementArray.getJSONObject(i);
                        data.setBadgeTitle(dataObj.getString("title"));
                        data.setBadgeImageUrl(dataObj.getString("imgurl"));
                        data.setBadgeUnlock(dataObj.getBoolean("unlocked"));
                        data.setUnlockDescription(dataObj.getString("description"));
                        achievementDataList.add(data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Set layout manager
                final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity()
                        , LinearLayoutManager.HORIZONTAL
                        , false);
                recyclerViewAchievements.setLayoutManager(layoutManager);
                //Set adapter
                OtherUserAchievementsAdapter adapter = new OtherUserAchievementsAdapter(achievementDataList, getActivity());
                recyclerViewAchievements.setHasFixedSize(true);
                recyclerViewAchievements.setAdapter(adapter);

                //Show bottom sheet
                achievementsSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                //Hide loading view
                progressView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String errorMsg) {
                //Show error msg snack bar.
                ViewHelper.getSnackBar(rootView, errorMsg);
                //Hide loading view
                progressView.setVisibility(View.GONE);
            }
        });
    }

    //endregion

}
