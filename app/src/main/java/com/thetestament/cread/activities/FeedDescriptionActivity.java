package com.thetestament.cread.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FeedDescriptionAdapter;
import com.thetestament.cread.helpers.DownvoteHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.ShareHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.CommentsModel;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.RxUtils;
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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MORE_POSTS;
import static com.thetestament.cread.dialog.DialogHelper.getDeletePostDialog;
import static com.thetestament.cread.helpers.DeepLinkHelper.generateDeepLink;
import static com.thetestament.cread.helpers.DeletePostHelper.deletePost;
import static com.thetestament.cread.helpers.FeedHelper.updateFollowForAll;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.NetworkHelper.getCommentObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getFeedDescPostsObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FROM_UPDATES_COMMENT_MENTION;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTION_TEXT;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_COMMENTS_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_POST;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY;
import static com.thetestament.cread.utils.Constant.USER_ACTION_TYPE_VIEW;

/**
 * Class to show detailed information of explore/feed item.
 */
public class FeedDescriptionActivity extends BaseActivity implements listener.OnCollaborationListener {

    //region: Butterknife view bindings
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.recyclerViewPosts)
    RecyclerView recyclerViewPosts;
    @BindView(R.id.viewProgress)
    View progressBar;
    //endregion

    //region: variables and flags
    private SharedPreferenceHelper mHelper;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private FeedModel mFeedData;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppCompatActivity mContext;
    FeedModel shareSpecificEntity;

    @State
    String mEntityID, mEntityType;

    @State
    int mItemPosition;

    Bitmap mBitmap;
    @State
    boolean isUserCreator;
    @State
    boolean shouldScroll;
    @State
    boolean mRequestMoreData;

    @State
    Bundle resultBundle = new Bundle();

    @State
    String mLastIndexKey;

    private List<FeedModel> mPostsList = new ArrayList<>();
    Intent resultIntent = new Intent();
    FeedDescriptionAdapter mAdapter;
    //endregion

    //region: Overidden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_description);
        ButterKnife.bind(this);

        //Obtain reference of this activity
        mContext = this;
        //ShredPreference reference
        mHelper = new SharedPreferenceHelper(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // check if user is also creator of this post

        //initialize views
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Set listener
        FeedHelper feedHelper = new FeedHelper();
        feedHelper.setOnCaptureClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_COMMENTS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    //Load comments data
                    getTopComments(mFeedData.getEntityID());
                }
                break;
            case REQUEST_CODE_OPEN_GALLERY:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    ImageHelper.startImageCropping(this, data.getData(), getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));
                } else {
                    ViewHelper.getSnackBar(rootView, "Image from gallery was not attached");
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);

                    ImageHelper.performSquareImageManipulation(mCroppedImgUri, FeedDescriptionActivity.this
                            , rootView, mEntityID, mEntityType);

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;

            case REQUEST_CODE_EDIT_POST:
                if (resultCode == RESULT_OK) {

                    // get edited caption
                    String editedCaption = data.getStringExtra(PREVIEW_EXTRA_CAPTION_TEXT);

                    // update the caption
                    mFeedData.setCaption(editedCaption);
                    mPostsList.set(0, mFeedData);
                    // editing is only allowed for 0 position
                    mAdapter.notifyItemChanged(0);


                    resultBundle.putString("caption", editedCaption);
                    setResult(RESULT_OK, resultIntent);
                }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //For permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
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
            chooseImageFromGallery();
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(this
                        , getString(R.string.error_msg_capture_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
            }
        }
    }
    //endregion



    //region: private methods

    /**
     * Method to initialize views for this screen.
     */
    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Get data from intent
        retrieveIntentData();
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter FeedAdapter reference.
     */
    private void initLoadMoreListener(FeedDescriptionAdapter adapter) {

        adapter.setOnFeedLoadMoreListener(new listener.OnFeedLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (mRequestMoreData) {

                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mPostsList.add(null);
                                               mAdapter.notifyItemInserted(mPostsList.size() - 1);
                                           }
                                       }
                    );
                    //Load new set of data
                    getNextPostsData();
                }
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
                generateDeepLink(mContext,
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
    private void initShareListener(FeedDescriptionAdapter feedAdapter) {
        feedAdapter.setOnShareListener(new listener.OnShareListener() {
            @Override
            public void onShareClick(Bitmap bitmap, FeedModel data) {
                mBitmap = bitmap;
                shareSpecificEntity = data;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    ShareHelper.sharePost(bitmap, mContext, shareSpecificEntity);
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

    private void initDownvoteListener(FeedDescriptionAdapter feedAdapter) {
        feedAdapter.setOnDownvoteClickedListener(new listener.OnDownvoteClickedListener() {
            @Override
            public void onDownvoteClicked(FeedModel data, int position, ImageView imageDownvote) {

                DownvoteHelper downvoteHelper = new DownvoteHelper();

                if (position == 0) {
                    // if already downvoted
                    if (data.isDownvoteStatus()) {
                        downvoteHelper.initDownvoteProcess(mContext
                                , data
                                , mCompositeDisposable
                                , imageDownvote
                                , resultBundle
                                , resultIntent);
                    } else

                    {   // show warning dialog
                        downvoteHelper.initDownvoteWarningDialog(mContext, data, mCompositeDisposable, imageDownvote, resultBundle, resultIntent);

                    }
                } else {
                    // if already downvoted
                    if (data.isDownvoteStatus()) {
                        downvoteHelper.initDownvoteProcess(mContext
                                , data
                                , mCompositeDisposable
                                , imageDownvote
                                , new Bundle()
                                , new Intent());
                    } else

                    {   // show warning dialog
                        downvoteHelper.initDownvoteWarningDialog(mContext, data, mCompositeDisposable, imageDownvote, new Bundle(), new Intent());

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
    private void initHatsOffListener(FeedDescriptionAdapter adapter) {
        adapter.setHatsOffListener(new listener.OnHatsOffListener() {
            @Override
            public void onHatsOffClick(final FeedModel feedData, final int itemPosition) {

                HatsOffHelper hatsOffHelper = new HatsOffHelper(mContext);
                hatsOffHelper.updateHatsOffStatus(feedData.getEntityID(), feedData.getHatsOffStatus());
                // On hatsOffSuccessListener
                hatsOffHelper.setOnHatsOffSuccessListener(new HatsOffHelper.OnHatsOffSuccessListener() {
                    @Override
                    public void onSuccess() {

                        if (itemPosition == 0) {
                            resultBundle.putLong("hatsOffCount", feedData.getHatsOffCount());
                            resultBundle.putBoolean("hatsOffStatus", feedData.getHatsOffStatus());

                            //Return result ok
                            setResult(RESULT_OK, resultIntent);
                        }

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
     * Initialize follow listener.
     */
    private void initFollowListener() {
        mAdapter.setOnFollowListener(new listener.OnExploreFollowListener() {
            @Override
            public void onFollowClick(FeedModel exploreData, int itemPosition) {
                updateFollowStatus(exploreData, itemPosition);
            }
        });
    }

    /**
     * Initialize delete listener
     * * @param adapter MeAdapter reference.
     */
    private void initializeDeleteListener(FeedDescriptionAdapter adapter) {
        adapter.setOnContentDeleteListener(new listener.OnContentDeleteListener() {
            @Override
            public void onDelete(String entityID, int position) {

                deleteContent(entityID, position);
            }
        });
    }

    /**
     * /**
     * Method to update follow status.
     *
     * @param data         Model of current item
     * @param itemPosition Position of current item i.e integer
     */
    private void updateFollowStatus(final FeedModel data, final int itemPosition) {

        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(mContext,
                mCompositeDisposable,
                data.getFollowStatus(),
                new JSONArray().put(data.getUUID()),
                new listener.OnFollowRequestedListener() {


                    @Override
                    public void onFollowSuccess() {

                        // updates follow status in all occurrence of the followed user
                        updateFollowForAll(data, mPostsList);
                        mAdapter.notifyDataSetChanged();

                        if (itemPosition == 0) {
                            resultBundle.putBoolean("followstatus", data.getFollowStatus());
                            //Return result ok
                            setResult(RESULT_OK, resultIntent);
                        }

                    }

                    @Override
                    public void onFollowFailure(String errorMsg) {

                        //set status to true if its false and vice versa
                        data.setFollowStatus(!data.getFollowStatus());
                        //notify changes
                        mAdapter.notifyItemChanged(itemPosition);

                        ViewHelper.getSnackBar(rootView, errorMsg);

                    }
                });
    }

    private void deleteContent(String entityID, final int position) {
        final MaterialDialog dialog = getDeletePostDialog(mContext);

        deletePost(mContext,
                mCompositeDisposable,
                entityID,
                new listener.OnDeleteRequestedListener() {
                    @Override
                    public void onDeleteSuccess() {
                        dialog.dismiss();
                        //Remove item from list
                        mPostsList.remove(position);
                        //Update adapter
                        mAdapter.notifyItemRemoved(position);

                        if (position == 0) {
                            resultBundle.putBoolean("deletestatus", true);
                            //Return result ok
                            setResult(RESULT_OK, resultIntent);
                        }
                        ViewHelper.getToast(mContext, getString(R.string.msg_post_deleted));
                        finish();
                    }

                    @Override
                    public void onDeleteFailure(String errorMsg) {

                        dialog.dismiss();
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }

    /**
     * Method to retrieve data from intent and set it to respective views.
     */
    private void retrieveIntentData() {

        Bundle bundle = getIntent().getBundleExtra(EXTRA_DATA);

        mFeedData = bundle.getParcelable(EXTRA_FEED_DESCRIPTION_DATA);
        mItemPosition = bundle.getInt("position");

        shouldScroll = bundle.getBoolean(EXTRA_FROM_UPDATES_COMMENT_MENTION, false);

        // method to initialize the result data
        initResultBundle();

        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(mContext));

        mPostsList.add(mFeedData);

        mAdapter = new FeedDescriptionAdapter(mPostsList, mContext, mCompositeDisposable, shouldScroll);
        recyclerViewPosts.setAdapter(mAdapter);

        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initHatsOffListener(mAdapter);
        initShareListener(mAdapter);
        initShareLinkClickedListener();
        initDownvoteListener(mAdapter);
        initFollowListener();
        initializeDeleteListener(mAdapter);


        initViewsFromData();


        storeUserActionsData(mFeedData.getEntityID(), USER_ACTION_TYPE_VIEW);
    }

    /**
     * Initializes the result data to their existing values
     */
    private void initResultBundle() {
        // setting result data
        // hatsoff count and status, follow and delete are set to existing values
        // and are updated when these actions actions are performed on this screen
        resultBundle.putInt("position", mItemPosition);
        resultBundle.putLong("hatsOffCount", mFeedData.getHatsOffCount());
        resultBundle.putBoolean("hatsOffStatus", mFeedData.getHatsOffStatus());
        resultBundle.putBoolean("followstatus", mFeedData.getFollowStatus());
        resultBundle.putBoolean("deletestatus", false);
        resultBundle.putBoolean("downvotestatus", mFeedData.isDownvoteStatus());
        resultBundle.putString("caption", mFeedData.getCaption());
        resultIntent.putExtra(EXTRA_DATA, resultBundle);
    }

    private void initViewsFromData() {

        //if comments exist load comments data and then more posts data
        if (mFeedData.getCommentCount() > 0) {
            //load comments
            getTopComments(mFeedData.getEntityID());
        }
        // load more posts data straightaway
        else {
            getPostsData();
        }


    }


    private void storeUserActionsData(String entityid, String eventType) {
        // store data only if user and creator are not same
        if (!mFeedData.getUUID().equals(mHelper.getUUID())) {
            RxUtils.getUserActionsDataObservable(mContext, entityid, eventType)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new Observer() {

                        @Override
                        public void onSubscribe(Disposable d) {

                            mCompositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(Object o) {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {


                        }


                    });
        }

    }


    /**
     * RxJava2 implementation for retrieving comment data from server.
     *
     * @param entityID ID for current story,
     */
    private void getTopComments(String entityID) {


        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        final List<CommentsModel> mCommentsList = new ArrayList<>();

        mCompositeDisposable.add(getCommentObservableFromServer(BuildConfig.URL + "/comment/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , entityID
                , null
                , false)
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
                                //Comments list
                                JSONArray commentsArray = mainData.getJSONArray("comments");
                                for (int i = 0; i < commentsArray.length(); i++) {
                                    JSONObject dataObj = commentsArray.getJSONObject(i);
                                    CommentsModel commentsData = new CommentsModel();
                                    commentsData.setUuid(dataObj.getString("uuid"));
                                    commentsData.setFirstName(dataObj.getString("firstname"));
                                    commentsData.setLastName(dataObj.getString("lastname"));
                                    commentsData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    commentsData.setComment(dataObj.getString("comment"));
                                    commentsData.setCommentId(dataObj.getString("commid"));
                                    mCommentsList.add(commentsData);
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
                        FirebaseCrash.report(e);
                    }

                    @Override
                    public void onComplete() {


                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, mContext.getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, mContext.getString(R.string.error_msg_internal));
                        } else {

                            new Handler().post(new Runnable() {
                                                   @Override
                                                   public void run() {

                                                       FeedDescriptionAdapter.ItemViewHolder itemViewHolder = (FeedDescriptionAdapter.ItemViewHolder) recyclerViewPosts.findViewHolderForAdapterPosition(0);
                                                       if (itemViewHolder != null) {
                                                           //No data
                                                           if (mCommentsList.size() == 0) {
                                                               //Hide views
                                                               itemViewHolder.viewTopComments.setVisibility(View.GONE);
                                                               itemViewHolder.textShowComments.setVisibility(View.GONE);
                                                           } else {


                                                               //Change visibility
                                                               itemViewHolder.viewTopComments.setVisibility(View.VISIBLE);
                                                               itemViewHolder.textShowComments.setVisibility(View.VISIBLE);
                                                               // update comments view and list
                                                               mAdapter.updateCommentsList(mCommentsList);
                                                               mAdapter.updateCommentsView(mCommentsList, itemViewHolder);

                                                               // scroll to bottom if opened from updates comment mention
                                                               if (shouldScroll) {
                                                                   itemViewHolder.viewTopComments.requestFocus();
                                                               }
                                                           }

                                                       }
                                                   }


                                               }
                            );


                        }

                        // get more posts
                        getPostsData();
                    }
                })
        );
    }


    /**
     * Loads the more posts data
     */
    private void getPostsData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        progressBar.setVisibility(View.VISIBLE);

        requestServer(mCompositeDisposable,
                getFeedDescPostsObservableFromServer(BuildConfig.URL + "/recommend-posts/details",
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        mFeedData.getEntityID(),
                        mFeedData.getUUID(),
                        mFeedData.getCollabWithUUID(),
                        mLastIndexKey,
                        GET_RESPONSE_FROM_NETWORK_MORE_POSTS),
                mContext,
                new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        progressBar.setVisibility(View.GONE);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));

                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                parsePostsData(jsonObject, false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        e.printStackTrace();
                        FirebaseCrash.report(e);

                        progressBar.setVisibility(View.GONE);

                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onCompleteCalled() {

                        progressBar.setVisibility(View.GONE);

                        GET_RESPONSE_FROM_NETWORK_MORE_POSTS = false;

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mPostsList.size() == 0) {
                            //fixme

                        } else {
                            // Token status invalid
                            if (tokenError[0]) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Error occurred
                            else if (connectionError[0]) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                            } else {

                                mAdapter.notifyItemRangeInserted(1, mPostsList.size());

                            }
                        }
                    }
                });

    }

    /**
     * Loads the next set of collab data
     */
    private void getNextPostsData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};


        requestServer(mCompositeDisposable,
                getFeedDescPostsObservableFromServer(BuildConfig.URL + "/recommend-posts/details",
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        mFeedData.getEntityID(),
                        mFeedData.getUUID(),
                        mFeedData.getCollabWithUUID(),
                        mLastIndexKey,
                        GET_RESPONSE_FROM_NETWORK_MORE_POSTS),
                mContext,
                new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //Remove loading item
                            mPostsList.remove(mPostsList.size() - 1);
                            //Notify changes
                            mAdapter.notifyItemRemoved(mPostsList.size());

                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {

                                parsePostsData(jsonObject, true);
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
                        mPostsList.remove(mPostsList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mPostsList.size());
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
                        } else if (mPostsList.size() == 0) {
                            //Show no data view
                            //Fixme
                        } else {
                            //Notify changes
                            mAdapter.setLoaded();
                        }

                    }
                });
    }

    private void parsePostsData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {

        JSONObject mainData = jsonObject.getJSONObject("data");
        mRequestMoreData = mainData.getBoolean("requestmore");
        mLastIndexKey = mainData.getString("lastindexkey");

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
            data.setEligibleForDownvote(mainData.getBoolean("candownvote"));
            data.setLongForm(dataObj.getBoolean("long_form"));
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

            mPostsList.add(data);

            if (isLoadMore) {
                //Notify item changes
                mAdapter.notifyItemInserted(mPostsList.size() - 1);
            }
        }
    }

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
            ViewHelper.getToast(FeedDescriptionActivity.this
                    , getString(R.string.error_msg_capture_permission_denied));
        }
    };

    /**
     * Used to handle result of askForPermission for share
     */
    PermissionCallback shareWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //sharePost(mBitmap);
            ShareHelper.sharePost(mBitmap, mContext, shareSpecificEntity);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(FeedDescriptionActivity.this
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };
    //endregion

}
