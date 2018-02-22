package com.thetestament.cread.activities;


import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v13.view.ViewCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CommentsAdapter;
import com.thetestament.cread.adapters.ShareDialogAdapter;
import com.thetestament.cread.database.NotificationsDBFunctions;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ShareHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnContentDeleteListener;
import com.thetestament.cread.models.CommentsModel;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.RxUtils;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

import static com.thetestament.cread.CreadApp.IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION;
import static com.thetestament.cread.activities.BottomNavigationActivity.AUTHORITY;
import static com.thetestament.cread.activities.BottomNavigationActivity.mAccount;
import static com.thetestament.cread.dialog.DialogHelper.getDeletePostDialog;
import static com.thetestament.cread.helpers.ContentHelper.getMenuActionsBottomSheet;
import static com.thetestament.cread.helpers.DeletePostHelper.deletepost;
import static com.thetestament.cread.helpers.FeedHelper.generateDeepLink;
import static com.thetestament.cread.helpers.FeedHelper.initCaption;
import static com.thetestament.cread.helpers.FeedHelper.initializeShareDialog;
import static com.thetestament.cread.helpers.FeedHelper.updateDotSeperatorVisibility;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.NetworkHelper.getCommentObservableFromServer;
import static com.thetestament.cread.utils.Constant.*;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FROM_UPDATES_COMMENT_MENTION;
import static com.thetestament.cread.utils.Constant.EXTRA_SHORT_UUID;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_CAPTURE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_FEED_DESCRIPTION;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_HAVE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTION_TEXT;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_COMMENTS_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_POST;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY;

/**
 * Class to show detailed information of explore/feed item.
 */
public class FeedDescriptionActivity extends BaseActivity implements listener.OnCollaborationListener {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageCreator)
    CircleImageView imageCreator;
    @BindView(R.id.textCreatorName)
    TextView textCreatorName;
    @BindView(R.id.contentImage)
    ImageView image;
    @BindView(R.id.containerCommentsCount)
    LinearLayout containerCommentsCount;
    @BindView(R.id.containerHatsoffCount)
    LinearLayout containerHatsOffCount;
    @BindView(R.id.containerCollabCount)
    LinearLayout containerCollabCount;
    @BindView(R.id.textHatsOffCount)
    TextView textHatsoffCount;
    @BindView(R.id.textCommentsCount)
    TextView textCommentsCount;
    @BindView(R.id.textCollabCount)
    TextView textCollabCount;
    @BindView(R.id.imageHatsOff)
    ImageView imageHatsOff;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.textShowComments)
    TextView showAllComments;
    @BindView(R.id.buttonHave)
    LinearLayout buttonHave;
    @BindView(R.id.lineSeparatorTop)
    View lineSeparatorTop;
    @BindView(R.id.containerHatsOff)
    LinearLayout containerHatsOff;
    @BindView(R.id.lineSeparatorBottom)
    View lineSeparatorBottom;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.buttonCollaborate)
    TextView buttonCollaborate;
    @BindView(R.id.textTitle)
    TextView textTitle;
    @BindView(R.id.dotSeperator)
    TextView dotSeperator;
    @BindView(R.id.buttonFollow)
    TextView buttonFollow;
    @BindView(R.id.buttonMenu)
    TextView buttonMenu;


    private SharedPreferenceHelper mHelper;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private FeedModel mFeedData;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppCompatActivity mContext;

    @State
    int mItemPosition;

    Bitmap mBitmap;
    @State
    boolean isUserCreator;
    @State
    boolean shouldScroll;

    @State
    Bundle resultBundle = new Bundle();

    Intent resultIntent = new Intent();

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
                    ImageHelper.processCroppedImage(mCroppedImgUri, FeedDescriptionActivity.this, rootView, mFeedData.getEntityID(), mFeedData.getContentType());

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;

            case REQUEST_CODE_EDIT_POST:
                if (resultCode == RESULT_OK) {

                    // reload image
                    // because short can be edited
                    // and it's image will change

                    loadStoryImage(mFeedData.getContentImage(), image);

                    // get edited caption
                    String editedCaption = data.getStringExtra(PREVIEW_EXTRA_CAPTION_TEXT);

                    // update the caption
                    mFeedData.setCaption(editedCaption);
                    initCaption(mContext, mFeedData, textTitle);

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

    /**
     * HatsOffCount click functionality to open "HatsOffActivity" screen.
     */
    @OnClick(R.id.containerHatsoffCount)
    void hatsOffCountOnClick() {
        Intent intent = new Intent(this, HatsOffActivity.class);
        intent.putExtra(EXTRA_ENTITY_ID, mFeedData.getEntityID());
        startActivity(intent);
    }


    @OnClick(R.id.textTitle)
    void onTitleClicked() {
        if (TextViewCompat.getMaxLines(textTitle) == 3) {
            // expand title
            textTitle.setMaxLines(Integer.MAX_VALUE);
        } else {
            // collapse title
            textTitle.setMaxLines(3);
        }
    }

    /**
     * Have button click functionality.
     */
    @OnClick(R.id.buttonHave)
    public void onViewClicked() {
        if (mFeedData.isMerchantable()) {
            Intent intent = new Intent(this, MerchandisingProductsActivity.class);
            intent.putExtra(EXTRA_ENTITY_ID, mFeedData.getEntityID());
            intent.putExtra(EXTRA_CAPTURE_URL, mFeedData.getContentImage());
            // getting short uuid and capture uuid
            if (mFeedData.getContentType().equals(CONTENT_TYPE_SHORT)) {
                intent.putExtra(EXTRA_SHORT_UUID, mFeedData.getUUID());
                intent.putExtra(EXTRA_CAPTURE_UUID, mFeedData.getCollabWithUUID());
            } else if (mFeedData.getContentType().equals(CONTENT_TYPE_CAPTURE)) {
                intent.putExtra(EXTRA_SHORT_UUID, mFeedData.getCollabWithUUID());
                intent.putExtra(EXTRA_CAPTURE_UUID, mFeedData.getUUID());
            }
            startActivity(intent);
        } else {
            ViewHelper.getSnackBar(rootView, "Due to low resolution this image is not available for purchase.");
        }
        //Log firebase event
        setAnalytics(FIREBASE_EVENT_HAVE_CLICKED);
    }

    /**
     * Click functionality to open "CommentsActivity" screen.
     */
    @OnClick({R.id.containerComment, R.id.textShowComments, R.id.containerCommentsCount})
    void onCommentsClicked() {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra(EXTRA_ENTITY_ID, mFeedData.getEntityID());
        startActivityForResult(intent, REQUEST_CODE_COMMENTS_ACTIVITY);
    }

    /**
     * HatsOff Click functionality.
     */
    @OnClick(R.id.containerHatsOff)
    void onContainerHatsOffClicked() {
        // check net status
        if (NetworkHelper.getNetConnectionStatus(FeedDescriptionActivity.this)) {
            //User has already given hats off
            if (mFeedData.getHatsOffStatus()) {
                //Animation for hats off
                imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.reverse_rotate_animation_hats_off_30_degree));
                //Toggle hatsOff tint
                imageHatsOff.setColorFilter(Color.TRANSPARENT);
                //Toggle hatsOff status
                mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                //Update hatsOffCount
                mFeedData.setHatsOffCount(mFeedData.getHatsOffCount() - 1);
                //If hats off count is zero
                if (mFeedData.getHatsOffCount() < 1) {
                    containerHatsOffCount.setVisibility(View.GONE);
                }
                //hats off count is more than zero
                else {
                    //Change hatsOffCount i.e decrease by one
                    containerHatsOffCount.setVisibility(View.VISIBLE);
                    textHatsoffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));
                }

            } else {
                //Animation for hats off
                imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation_hats_off_30_degree));
                //Toggle hatsOff tint
                imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                //Toggle hatsOff status
                mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                //Update hatsOffCount
                mFeedData.setHatsOffCount(mFeedData.getHatsOffCount() + 1);
                //Change hatsOffCount i.e increase by one
                containerHatsOffCount.setVisibility(View.VISIBLE);
                textHatsoffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));
            }

            updateDotSeperatorVisibility(mFeedData, dotSeperator);
            //update hats off status on server
            updateHatsOffStatus();

        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * Share button click functionality.
     */
    @OnClick(R.id.containerShares)
    void shareOnClick() {

        ShareDialogAdapter adapter = new ShareDialogAdapter(mContext, initializeShareDialog(mContext));
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .adapter(adapter, null)
                .show();
        adapter.setShareDialogItemClickedListener(new listener.OnShareDialogItemClickedListener() {
            @Override
            public void onShareDialogItemClicked(int index) {

                // dismiss dialog
                dialog.dismiss();

                switch (index) {
                    case 0:
                        // image sharing
                        //so load image
                        loadBitmapForSharing();
                        break;
                    case 1:
                        // link sharing
                        // get deep link from server
                        generateDeepLink(mContext,
                                mCompositeDisposable,
                                rootView,
                                mHelper.getUUID(),
                                mHelper.getAuthToken(),
                                mFeedData.getEntityID(),
                                mFeedData.getContentImage(),
                                mFeedData.getCreatorName());
                        break;

                }
            }
        });
    }

    /**
     * Collaboration count click functionality to launch collaborationDetailsActivity.
     */
    @OnClick(R.id.containerCollabCount)
    void collaborationCountOnClick() {

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ENTITY_ID, mFeedData.getEntityID());
        bundle.putString(EXTRA_ENTITY_TYPE, mFeedData.getContentType());

        Intent intent = new Intent(FeedDescriptionActivity.this, CollaborationDetailsActivity.class);
        intent.putExtra(EXTRA_DATA, bundle);
        startActivity(intent);
    }

    @OnClick(R.id.buttonFollow)
    void onFollowClick() {
        // check net status
        if (NetworkHelper.getNetConnectionStatus(mContext)) {
            //Toggle status
            mFeedData.setFollowStatus(!mFeedData.getFollowStatus());
            //Toggle follow button
            toggleFollowButton(true);
            // update status on server
            updateFollowStatus();
            //Log firebase event
            setAnalytics(FIREBASE_EVENT_FOLLOW_FROM_FEED_DESCRIPTION);
        } else {
            ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
        }
    }

    @OnClick(R.id.buttonMenu)
    void onMenuClick() {
        getMenuActionsBottomSheet(mContext, mItemPosition, mFeedData, initDeletePostClick());
    }

    /**
     * Method to initialize views for this screen.
     */
    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Get data from intent
        retrieveIntentData();
        //Load story image
        loadStoryImage(mFeedData.getContentImage(), image);
        //Load creator image
        loadCreatorImage(mFeedData.getCreatorImage(), imageCreator);
        //toggle hats off status
        toggleHatsOffStatus();
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
        resultBundle.putString("caption", mFeedData.getCaption());
        resultIntent.putExtra(EXTRA_DATA, resultBundle);
    }

    private void initViewsFromData() {
        FeedHelper.performContentTypeSpecificOperations(mContext
                , mFeedData
                , textCollabCount
                , containerCollabCount
                , buttonCollaborate
                , textCreatorName
                , false
                , false
                , null);

        // set caption if it exists
        // else hide the caption view

        // initialize caption
        initCaption(mContext, mFeedData, textTitle);

        // update dot visibility
        updateDotSeperatorVisibility(mFeedData, dotSeperator);

        //Check for hats of count
        if (mFeedData.getHatsOffCount() > 0) {
            //Set hatsOff count
            textHatsoffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));
        } else {
            //Hide hatsOff count textView
            containerHatsOffCount.setVisibility(View.GONE);
        }

        //Check for comment count
        if (mFeedData.getCommentCount() > 0) {
            //Set comment count
            textCommentsCount.setText(String.valueOf(mFeedData.getCommentCount()));
            //load comments
            getTopComments(mFeedData.getEntityID());
        } else {
            containerCommentsCount.setVisibility(View.GONE);
            showAllComments.setVisibility(View.GONE);
        }

        //Show tooltip on have button
        showTooltip();

        //If API is greater than LOLLIPOP
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //For shared transition
            image.setTransitionName(mFeedData.getEntityID());
            ActivityCompat.postponeEnterTransition(this);
        }
        // check if user is the creator
        isUserCreator = mFeedData.getUUID().equals(mHelper.getUUID());

        toggleFollowButton(false);
        // show menu options if user is creator
        showMenuOptions();
    }


    private void storeUserActionsData(String entityid, String eventType)
    {
        // store data only if user and creator are not same
        if(!mFeedData.getUUID().equals(mHelper.getUUID()))
        {
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


    private void updateDataOnServer(final String uuid, String authkey, JSONArray userActions) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authkey);
            jsonObject.put("user_events", userActions);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        AndroidNetworking.post(BuildConfig.URL + "/user-events/save")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Token status is invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                // do nothing
                            } else {
                                JSONObject mainData = response.getJSONObject("data");

                                if (mainData.getString("status").equals("done")) {

                                    // data uploaded successfully
                                    // delete users data
                                    NotificationsDBFunctions notificationsDBFunctions = new NotificationsDBFunctions(mContext);
                                    notificationsDBFunctions.accessNotificationsDatabase();

                                    notificationsDBFunctions.deleteUserActionsData(uuid);

                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        FirebaseCrash.report(anError);
                    }
                });
    }

    /**
     * Method to load story image.
     *
     * @param imgLink Image Url.
     * @param image   Where image to be displayed.
     */
    private void loadStoryImage(String imgLink, ImageView image) {

        if (IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION) {
            Picasso.with(this).invalidate(mFeedData.getContentImage());
        }


        RequestCreator requestCreator = Picasso.with(this)
                .load(imgLink)
                .error(R.drawable.image_placeholder);

        if (IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION) {
            requestCreator.networkPolicy(NetworkPolicy.NO_CACHE);
        }


        requestCreator.into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        ActivityCompat.startPostponedEnterTransition(FeedDescriptionActivity.this);
                    }

                    @Override
                    public void onError() {
                        ActivityCompat.startPostponedEnterTransition(FeedDescriptionActivity.this);
                    }
                });
    }

    /**
     * Method to load creator profile picture.
     *
     * @param creatorImage View where image to be displayed.
     * @param imageURL     Image url.
     */
    private void loadCreatorImage(String imageURL, CircleImageView creatorImage) {
        Picasso.with(this)
                .load(imageURL)
                .error(R.drawable.ic_account_circle_48)
                .into(creatorImage);
    }

    /**
     * Method to load bitmap image to be shared
     */
    private void loadBitmapForSharing() {
        Picasso.with(this).load(mFeedData.getContentImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBitmap = bitmap;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    //sharePost(bitmap);
                    ShareHelper.sharePost(bitmap, FeedDescriptionActivity.this, mFeedData);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(FeedDescriptionActivity.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(FeedDescriptionActivity.this
                                , getString(R.string.error_msg_share_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(FeedDescriptionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, shareWritePermission);
                    }
                }
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ViewHelper.getToast(FeedDescriptionActivity.this, getString(R.string.error_msg_internal));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }



    /**
     * Method to toggle hats off status.
     */
    private void toggleHatsOffStatus() {
        if (mFeedData.getHatsOffStatus()) {
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation_hats_off_fast));
        } else {
            imageHatsOff.setColorFilter(Color.TRANSPARENT);
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.reverse_rotate_animation_hats_off_30_degree));
        }
    }

    private void toggleFollowButton(boolean showToast) {
        // toggle visibility of follow button
        if (mFeedData.getFollowStatus() || isUserCreator) {

            buttonFollow.setVisibility(View.GONE);

            if (showToast) {
                ViewHelper.getToast(mContext, "You are now following " + mFeedData.getCreatorName());
            }

        } else {
            buttonFollow.setVisibility(View.VISIBLE);
        }
    }

    /**
     * If user is creator then it shows menu options
     */
    private void showMenuOptions() {
        if (isUserCreator) {
            buttonMenu.setVisibility(View.VISIBLE);
        } else {
            buttonMenu.setVisibility(View.GONE);
        }
    }

    /**
     * RxJava2 implementation for retrieving comment data from server.
     *
     * @param entityID ID for current story,
     */
    private void getTopComments(String entityID) {
        //For smooth scrolling
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

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
                        //Hide views
                        recyclerView.setVisibility(View.GONE);
                        showAllComments.setVisibility(View.GONE);
                        FirebaseCrash.report(e);
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
                        else if (mCommentsList.size() == 0) {
                            //Hide views
                            recyclerView.setVisibility(View.GONE);
                            showAllComments.setVisibility(View.GONE);
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(FeedDescriptionActivity.this, resId);
                            recyclerView.setLayoutAnimation(animation);

                            //Change visibility
                            recyclerView.setVisibility(View.VISIBLE);
                            showAllComments.setVisibility(View.VISIBLE);
                            //Set layout manager
                            recyclerView.setLayoutManager(new LinearLayoutManager(FeedDescriptionActivity.this));
                            //Set adapter
                            recyclerView.setAdapter(new CommentsAdapter(mCommentsList, FeedDescriptionActivity.this, mHelper.getUUID(), false));
                            // scroll to bottom if opened from updates comment mention
                            if (shouldScroll) {
                                recyclerView.requestFocus();
                            }
                        }
                    }
                })
        );
    }

    /**
     * Method to show tooltip on have button
     */
    private void showTooltip() {
        if (mHelper.isHaveButtonTooltipFirstTime()) {
            //Show tooltip on have button
            ViewHelper.getToolTip(buttonHave
                    , "Like the photo? Print and order it!"
                    , FeedDescriptionActivity.this);
        }
        //Update status
        mHelper.updateHaveButtonToolTipStatus(false);
    }

    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", mHelper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_HAVE_CLICKED)) {
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_HAVE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION)) {
            bundle.putString("entity_id", mFeedData.getEntityID());
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_WRITE_CLICKED)) {
            bundle.putString("class_name", "feed_description");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_WRITE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_CAPTURE_CLICKED)) {
            bundle.putString("class_name", "feed_description");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_CAPTURE_CLICKED, bundle);
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
     * Initializes delete on click listener
     *
     * @return
     */
    private OnContentDeleteListener initDeletePostClick() {

        OnContentDeleteListener onContentDeleteListener = new OnContentDeleteListener() {
            @Override
            public void onDelete(String entityID, int position) {

                final MaterialDialog dialog = getDeletePostDialog(mContext);

                deletepost(mContext,
                        mCompositeDisposable,
                        mFeedData.getEntityID(),
                        new listener.onDeleteRequestedListener() {
                            @Override
                            public void onDeleteSuccess() {
                                dialog.dismiss();

                                resultBundle.putBoolean("deletestatus", true);
                                //Return result ok
                                setResult(RESULT_OK, resultIntent);
                                ViewHelper.getToast(mContext, getString(R.string.msg_post_deleted));
                                finish();
                            }

                            @Override
                            public void onDeleteFailiure(String errorMsg) {

                                dialog.dismiss();
                                ViewHelper.getSnackBar(rootView, errorMsg);
                            }
                        });
            }
        };

        return onContentDeleteListener;
    }

    /**
     * Method to update hatsOff status on server
     */
    private void updateHatsOffStatus() {
        HatsOffHelper hatsOffHelper = new HatsOffHelper(FeedDescriptionActivity.this);
        hatsOffHelper.updateHatsOffStatus(mFeedData.getEntityID(), mFeedData.getHatsOffStatus());
        // On hatsOffSuccessListener
        hatsOffHelper.setOnHatsOffSuccessListener(new HatsOffHelper.OnHatsOffSuccessListener() {
            @Override
            public void onSuccess() {

                resultBundle.putLong("hatsOffCount", mFeedData.getHatsOffCount());
                resultBundle.putBoolean("hatsOffStatus", mFeedData.getHatsOffStatus());

                //Return result ok
                setResult(RESULT_OK, resultIntent);
            }
        });
        // On hatsOffSuccessListener
        hatsOffHelper.setOnHatsOffFailureListener(new HatsOffHelper.OnHatsOffFailureListener() {
            @Override
            public void onFailure(String errorMsg) {
                mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                updateHatsOffCount();
                toggleHatsOffStatus();
                ViewHelper.getSnackBar(rootView, errorMsg);
            }
        });
    }

    /**
     * Method to update follow status.
     */
    private void updateFollowStatus() {

        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(mContext,
                mCompositeDisposable,
                mFeedData.getFollowStatus(),
                new JSONArray().put(mFeedData.getUUID()),
                new listener.OnFollowRequestedListener() {
                    @Override
                    public void onFollowSuccess() {

                        resultBundle.putBoolean("followstatus", mFeedData.getFollowStatus());
                        //Return result ok
                        setResult(RESULT_OK, resultIntent);
                    }

                    @Override
                    public void onFollowFailiure(String errorMsg) {
                        //Toggle status
                        mFeedData.setFollowStatus(!mFeedData.getFollowStatus());
                        //Toggle follow button
                        toggleFollowButton(false);
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }

    /**
     * Method to update hatsOffCount after server response.
     */
    private void updateHatsOffCount() {
        if (mFeedData.getHatsOffStatus()) {
            //Update hatsOffCount
            mFeedData.setHatsOffCount(mFeedData.getHatsOffCount() + 1);
            //Set visibility on and set hatsOff count
            containerHatsOffCount.setVisibility(View.VISIBLE);
            textHatsoffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));


        } else {
            //Update hatsOffCount
            mFeedData.setHatsOffCount(mFeedData.getHatsOffCount() - 1);
            //If hats off count is zero
            if (mFeedData.getHatsOffCount() < 1) {
                containerHatsOffCount.setVisibility(View.GONE);
            }
            //hats off count is more than zero
            else {
                containerHatsOffCount.setVisibility(View.VISIBLE);
                textHatsoffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));
            }
        }

        updateDotSeperatorVisibility(mFeedData, dotSeperator);
    }

    /**
     * Used to handle result of askForPermission for share
     */
    PermissionCallback shareWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //sharePost(mBitmap);
            ShareHelper.sharePost(mBitmap, mContext, mFeedData);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(FeedDescriptionActivity.this
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };

    @Override
    public void collaborationOnGraphic() {

    }

    @Override
    public void collaborationOnWriting(String entityID, String entityType) {
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

}
