package com.thetestament.cread.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.NewUsersPostAdapter;
import com.thetestament.cread.helpers.DownvoteHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
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
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static com.thetestament.cread.helpers.DeepLinkHelper.getDeepLinkOnValidShareOption;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.processCroppedImage;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_USER_PROFILE_FROM_FEED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_USER_PROFILE_FROM_SUGGESTED_ADAPTER;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;

/**
 * Activity class to show new users post.
 */

public class NewUsersPostActivity extends BaseActivity implements listener.OnCollaborationListener {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.swipe_to_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    //endregion

    //region :Fields and constants
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    List<FeedModel> mDataList = new ArrayList<>();
    NewUsersPostAdapter mAdapter;
    SharedPreferenceHelper mHelper;

    private String mLastIndexKey;
    private boolean mRequestMoreData;

    @State
    String mEntityIDList;
    /**
     * Flag to maintain user down vote capability.
     */
    @State
    boolean mCanDownVote;
    @State
    String mEntityID, mEntityType;

    Bitmap mBitmap;
    FeedModel entitySpecificData;

    FragmentActivity mContext;

    @State
    String mShareOption = SHARE_OPTION_OTHER;

//endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_users_post);
        ButterKnife.bind(this);
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
        //Obtain reference of this screen
        mContext = this;
        //Method called
        initScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        //Set Listener
        new FeedHelper().setOnCaptureClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Method called
        initScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
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
            case REQUEST_CODE_OPEN_GALLERY:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    ImageHelper.startImageCropping(mContext
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
                                , mContext
                                , rootView
                                , mEntityID
                                , mEntityType);
                    } else {
                        //Method called
                        processCroppedImage(mCroppedImgUri, mContext, rootView, mEntityID, mEntityType);
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
                    mDataList.get(bundle.getInt("position")).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                    mDataList.get(bundle.getInt("position")).setDownvoteStatus(bundle.getBoolean("downvotestatus"));
                    mDataList.get(bundle.getInt("position")).setHatsOffCount(bundle.getLong("hatsOffCount"));
                    //Notify changes
                    mAdapter.notifyItemChanged(bundle.getInt("position"));
                }
                break;
            case REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED:
            case REQUEST_CODE_USER_PROFILE_FROM_FEED:
                if (resultCode == RESULT_OK) {
                    //Refresh data
                    loadData();
                }
                break;
            case REQUEST_CODE_USER_PROFILE_FROM_SUGGESTED_ADAPTER:
                if (resultCode == RESULT_OK) {
                    //Notify changes
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setBackButtonBehaviour();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        setBackButtonBehaviour();
        finish();
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
            chooseImageFromGallery();
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(mContext
                        , getString(R.string.error_msg_capture_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
            }
        }
    }


    //endregion

    //region :Private methods

    /**
     * Method to initialize views for this screen.
     */
    private void initScreen() {
        mEntityIDList = getIntent().getExtras().getString(Constant.NOTIFICATION_EXTRA_ENTITY_ID_LIST);
        //Set dialogParentView manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Set adapter
        mAdapter = new NewUsersPostAdapter(mDataList, mContext, mHelper.getUUID(), mCompositeDisposable);
        recyclerView.setAdapter(mAdapter);

        //Swipe refresh listener
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to null
                mLastIndexKey = null;
                //Load data here
                loadData();
            }
        });

        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initHatsOffListener(mAdapter);
        initShareListener(mAdapter);
        initDownVoteListener(mAdapter);

        //Load data here
        loadData();
    }


    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadData() {
        // if user device is connected to net
        if (getNetConnectionStatus(mContext)) {
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving NewUsersPost data.
     */
    private void getData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(NetworkHelper.getNewUserDataObservable(BuildConfig.URL + "/recommend-posts/load-firsts"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , mEntityIDList)
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
                            Crashlytics.setString("className", "NewUserPostActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "NewUserPostActivity");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        CreadApp.GET_RESPONSE_FROM_NETWORK_NEW_USERS_POST = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mDataList.size() == 0) {

                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
                            recyclerView.setLayoutAnimation(animation);
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
        mCompositeDisposable.add(NetworkHelper.getNewUserDataObservable(BuildConfig.URL + "/recommend-posts/load-firsts"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , mEntityIDList)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                parsePostsData(jsonObject, true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "NewUserPostActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mDataList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "NewUserPostActivity");
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
     * Initialize load more listener to retrieve next set of data from server if its available.
     *
     * @param adapter NewUsersPostAdapter reference.
     */
    private void initLoadMoreListener(NewUsersPostAdapter adapter) {

        adapter.setOnFeedLoadMoreListener(new listener.OnFeedLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (mRequestMoreData) {

                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mDataList.add(null);
                                               mAdapter.notifyItemInserted(mDataList.size() - 1);
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
     * @param adapter NewUsersPostAdapter reference.
     */
    private void initHatsOffListener(NewUsersPostAdapter adapter) {
        adapter.setHatsOffListener(new listener.OnHatsOffListener() {
            @Override
            public void onHatsOffClick(final FeedModel feedData, final int itemPosition) {
                HatsOffHelper hatsOffHelper = new HatsOffHelper(mContext);
                hatsOffHelper.updateHatsOffStatus(feedData.getEntityID(), feedData.getHatsOffStatus());
                // On hatsOffSuccessListener
                hatsOffHelper.setOnHatsOffSuccessListener(new HatsOffHelper.OnHatsOffSuccessListener() {
                    @Override
                    public void onSuccess() {
                    }
                });
                // On hatsOffSuccessListener
                hatsOffHelper.setOnHatsOffFailureListener(new HatsOffHelper.OnHatsOffFailureListener() {
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
     * Initialize share listener.
     */
    private void initShareListener(NewUsersPostAdapter adapter) {
        adapter.setOnShareListener(new listener.OnShareListener() {
            @Override
            public void onShareClick(Bitmap bitmap, FeedModel data, String shareOption) {
                mBitmap = bitmap;
                entitySpecificData = data;
                mShareOption = shareOption;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    getDeepLinkOnValidShareOption(mContext,
                            mCompositeDisposable,
                            rootView,
                            mHelper.getUUID(),
                            mHelper.getAuthToken(),
                            entitySpecificData,
                            bitmap,
                            mShareOption);

                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(mContext
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(mContext
                                , getString(R.string.error_msg_share_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE, shareWritePermission);
                    }
                }
            }
        });
    }

    /**
     * Initialize down vote listener.
     *
     * @param adapter NewUsersPostAdapter reference
     */
    private void initDownVoteListener(NewUsersPostAdapter adapter) {
        adapter.setOnDownvoteClickedListener(new listener.OnDownvoteClickedListener() {
            @Override
            public void onDownvoteClicked(FeedModel data, int position, ImageView imageDownVote) {

                DownvoteHelper downvoteHelper = new DownvoteHelper();

                // if already downVoted
                if (data.isDownvoteStatus()) {
                    downvoteHelper.initDownvoteProcess(mContext
                            , data
                            , mCompositeDisposable
                            , imageDownVote
                            , new Bundle()
                            , new Intent());
                } else

                {   // Show warning dialog
                    downvoteHelper.initDownvoteWarningDialog(mContext
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
     * Used to handle result of askForPermission for capture.
     */
    PermissionCallback captureWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //Select image from gallery
            chooseImageFromGallery();
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(mContext
                    , getString(R.string.error_msg_capture_permission_denied));
        }
    };


    /**
     * Used to handle result of askForPermission for share.
     */
    PermissionCallback shareWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            getDeepLinkOnValidShareOption(mContext,
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
            ViewHelper.getToast(mContext
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };


    /**
     * Open gallery so user can choose his/her capture for uploading.
     */
    private void chooseImageFromGallery() {
        //Launch gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_GALLERY);
    }

    /**
     * Method to launch parent activity.
     */
    private void setBackButtonBehaviour() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }
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
        JSONArray feedArray = mainData.getJSONArray("items");
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
                // if capture then if key cpshort exists not available for collaboration
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

                // if short then if key shcapture exists not available for collaboration
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

            mDataList.add(feedData);
            //Called from load more
            if (isLoadMore) {
                //Notify item insertion
                mAdapter.notifyItemInserted(mDataList.size() - 1);
            }
        }
    }
//endregion
}
