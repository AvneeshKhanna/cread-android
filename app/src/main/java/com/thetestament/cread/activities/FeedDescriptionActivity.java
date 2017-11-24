package com.thetestament.cread.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v13.view.ViewCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CommentsAdapter;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.models.CommentsModel;
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
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static com.thetestament.cread.helpers.FeedHelper.initializeSpannableString;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.getLocalBitmapUri;
import static com.thetestament.cread.helpers.NetworkHelper.getCommentObservableFromServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_CAPTURE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_HAVE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_COMMENTS_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY;

/**
 * Class to show detailed information of explore/feed item.
 */
public class FeedDescriptionActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageCreator)
    CircleImageView imageCreator;
    @BindView(R.id.textCreatorName)
    TextView textCreatorName;
    @BindView(R.id.contentImage)
    ImageView image;
    @BindView(R.id.textHatsOffCount)
    TextView textHatsOffCount;
    @BindView(R.id.textCommentsCount)
    TextView textCommentsCount;
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
    @BindView(R.id.imageComment)
    ImageView imageComment;
    @BindView(R.id.containerComment)
    LinearLayout containerComment;
    @BindView(R.id.imageShare)
    ImageView imageShare;
    @BindView(R.id.textShare)
    TextView textShare;
    @BindView(R.id.containerShares)
    LinearLayout containerShares;
    @BindView(R.id.lineSeparatorBottom)
    View lineSeparatorBottom;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.collabCount)
    TextView collabCount;
    @BindView(R.id.buttonCollaborate)
    TextView buttonCollaborate;


    private SharedPreferenceHelper mHelper;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private FeedModel mFeedData;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppCompatActivity mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_description);
        ButterKnife.bind(this);

        mContext = this;

        //ShredPreference reference
        mHelper = new SharedPreferenceHelper(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //initialize views
        initViews();
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
                    ImageHelper.processCroppedImage(mCroppedImgUri, FeedDescriptionActivity.this, rootView, mFeedData.getShortID());

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    /**
     * HatsOffCount click functionality to open "HatsOffActivity" screen.
     */
    @OnClick(R.id.textHatsOffCount)
    void hatsOffCountOnClick() {
        Intent intent = new Intent(this, HatsOffActivity.class);
        intent.putExtra(EXTRA_ENTITY_ID, mFeedData.getEntityID());
        startActivity(intent);
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
    @OnClick({R.id.containerComment, R.id.textShowComments, R.id.textCommentsCount})
    void onCommentsClicked() {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra(EXTRA_ENTITY_ID, mFeedData.getEntityID());
        startActivityForResult(intent, REQUEST_CODE_COMMENTS_ACTIVITY);
    }

    /**
     * HatsOff onClick functionality.
     */
    @OnClick(R.id.imageHatsOff)
    void onContainerHatsOffClicked() {

        // check net status
        if (NetworkHelper.getNetConnectionStatus(FeedDescriptionActivity.this)) {
            //User has already given hats off
            if (mFeedData.getHatsOffStatus()) {
                //Animation for hats off
                imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.reverse_rotate_animation_hats_off));
                //Toggle hatsOff tint
                imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.grey));
                //Toggle hatsOff status
                mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                //Update hatsOffCount
                mFeedData.setHatsOffCount(mFeedData.getHatsOffCount() - 1);
                //If hats off count is zero
                if (mFeedData.getHatsOffCount() < 1) {
                    textHatsOffCount.setVisibility(View.GONE);
                }
                //hats off count is more than zero
                else {
                    //Change hatsOffCount i.e decrease by one
                    textHatsOffCount.setVisibility(View.VISIBLE);
                    textHatsOffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));
                }

            } else {
                //Animation for hats off
                imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation_hats_off));
                //Toggle hatsOff tint
                imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                //Toggle hatsOff status
                mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                //Update hatsOffCount
                mFeedData.setHatsOffCount(mFeedData.getHatsOffCount() + 1);
                //Change hatsOffCount i.e increase by one
                textHatsOffCount.setVisibility(View.VISIBLE);
                textHatsOffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));
            }
            //update hats off status on server
            updateHatsOffStatus(mFeedData.getEntityID(), mFeedData.getHatsOffStatus());
        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * Share button click functionality.
     */
    @OnClick(R.id.containerShares)
    void shareOnClick() {
        Picasso.with(this).load(mFeedData.getContentImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, FeedDescriptionActivity.this));
                startActivity(Intent.createChooser(intent, "Share"));
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
     * Collaboration count click functionality to launch collaborationDetailsActivity.
     *
     * @param textView View to be clicked
     */
    @OnClick(R.id.collabCount)
    void collaborationCountOnClick(TextView textView) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_ENTITY_ID, mFeedData.getEntityID());
                bundle.putString(EXTRA_ENTITY_TYPE, mFeedData.getContentType());

                Intent intent = new Intent(FeedDescriptionActivity.this, CollaborationDetailsActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                startActivity(intent);
            }
        });
    }


    /**
     * Method to initialize Toolbar.
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

        mFeedData = getIntent().getParcelableExtra(EXTRA_FEED_DESCRIPTION_DATA);


        performContentTypeSpecificOperations();


        //Check for hats of count
        if (mFeedData.getHatsOffCount() > 0) {
            textHatsOffCount.setText(String.valueOf(mFeedData.getHatsOffCount()));
        } else {
            textHatsOffCount.setVisibility(View.GONE);
        }

        //Check for comment count
        if (mFeedData.getCommentCount() > 0) {
            //Set comment count
            textCommentsCount.setText(String.valueOf(mFeedData.getCommentCount()));
            //load comments
            getTopComments(mFeedData.getEntityID());
        } else {
            textCommentsCount.setVisibility(View.GONE);
            showAllComments.setVisibility(View.GONE);
        }

        //Show tooltip on have button
        showTooltip();
    }

    /**
     * Method to load story image.
     */
    private void loadStoryImage(String imgLink, ImageView image) {
        //Load campaign image
        Picasso.with(this)
                .load(imgLink)
                .error(R.drawable.image_placeholder)
                .into(image);
    }

    /**
     * Method to load creator profile picture.
     */
    private void loadCreatorImage(String imageURL, CircleImageView creatorImage) {
        //Load campaign image
        Picasso.with(this)
                .load(imageURL)
                .error(R.drawable.ic_account_circle_48)
                .into(creatorImage);
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
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.grey));
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.reverse_rotate_animation_hats_off));
        }
    }

    /**
     * Method to update hats off status.
     *
     * @param entityID  Campaign ID i.e String
     * @param isHatsOff boolean true if user has given hats off to campaign, false otherwise.
     */
    private void updateHatsOffStatus(String entityID, boolean isHatsOff) {
        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("entityid", entityID);
            jsonObject.put("register", isHatsOff);
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
                                mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                                toggleHatsOffStatus();
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Do nothing
                                } else {
                                    mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                                    toggleHatsOffStatus();
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                            toggleHatsOffStatus();
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        mFeedData.setHatsOffStatus(!mFeedData.getHatsOffStatus());
                        toggleHatsOffStatus();
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });

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
                            ViewHelper.getSnackBar(rootView, getString(R.string.auth_token));
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
                            recyclerView.setAdapter(new CommentsAdapter(mCommentsList, FeedDescriptionActivity.this, mHelper.getUUID()));
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
            ViewHelper.getToolTip(buttonHave, "Like the photo? Print and order it!", FeedDescriptionActivity.this);
        }
        //Update status
        mHelper.updateHaveButtonToolTipStatus(false);
    }

    /**
     * Method that performs operations according to content type and collaboration functionality*
     */

    private void performContentTypeSpecificOperations() {
        //Check for content type
        switch (mFeedData.getContentType()) {
            case CONTENT_TYPE_CAPTURE:

                // set collab count text
                if (mFeedData.getCollabCount() != 0) {
                    collabCount.setText(mFeedData.getCollabCount() + " others added a short to it");
                    collabCount.setVisibility(View.VISIBLE);

                } else {
                    collabCount.setVisibility(View.GONE);
                }

                if (mFeedData.isAvailableForCollab()) {

                    // for stand alone capture

                    buttonCollaborate.setVisibility(View.VISIBLE);
                    // set text
                    buttonCollaborate.setText("Write");

                    //write click functionality on capture
                    writeOnClick(buttonCollaborate, mFeedData.getCaptureID(), mFeedData.getContentImage(), mFeedData.isMerchantable());

                    String text = mFeedData.getCreatorName() + " added a capture ";

                    // get text indexes
                    int creatorStartPos = text.indexOf(mFeedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + mFeedData.getCreatorName().length();
                    int collabWithStartPos = -1;
                    int collabWithEndPos = -1;

                    // get clickable text;
                    initializeSpannableString(mContext, textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, mFeedData.getUUID(), mFeedData.getCollabWithUUID());


                } else {

                    // hiding collaborate button
                    buttonCollaborate.setVisibility(View.GONE);

                    String text = mFeedData.getCreatorName() + " added a capture to " + mFeedData.getCollabWithName() + "'s short";

                    // get text indexes
                    int creatorStartPos = text.indexOf(mFeedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + mFeedData.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(mFeedData.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + mFeedData.getCollabWithName().length() + 2; // +2 for 's

                    // get clickable text
                    initializeSpannableString(mContext, textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, mFeedData.getUUID(), mFeedData.getCollabWithUUID());


                }

                break;

            case CONTENT_TYPE_SHORT:

                // set collab count text
                if (mFeedData.getCollabCount() != 0) {
                    collabCount.setText(mFeedData.getCollabCount() + " others added a capture to it");
                    collabCount.setVisibility(View.VISIBLE);
                } else {
                    collabCount.setVisibility(View.GONE);
                }

                // check if available for collab
                if (mFeedData.isAvailableForCollab()) {

                    // for stand alone short

                    buttonCollaborate.setVisibility(View.VISIBLE);
                    // set text
                    buttonCollaborate.setText("Capture");

                    // capture click functionality on short
                    captureOnClick(buttonCollaborate);

                    String text = mFeedData.getCreatorName() + " wrote a short ";

                    // get text indexes
                    int creatorStartPos = text.indexOf(mFeedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + mFeedData.getCreatorName().length();
                    int collabWithStartPos = -1; // since no collabwith
                    int collabWithEndPos = -1; // since no collabwith

                    initializeSpannableString(mContext, textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, mFeedData.getUUID(), mFeedData.getCollabWithUUID());


                } else {
                    // hiding collaborate button
                    buttonCollaborate.setVisibility(View.GONE);

                    String text = mFeedData.getCreatorName() + " wrote a short on " + mFeedData.getCollabWithName() + "'s capture";

                    // get text indexes
                    int creatorStartPos = text.indexOf(mFeedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + mFeedData.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(mFeedData.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + mFeedData.getCollabWithName().length() + 2; // +2 to incorporate 's

                    // get clickable text
                    initializeSpannableString(mContext, textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, mFeedData.getUUID(), mFeedData.getCollabWithUUID());

                }

                break;
            default:
        }

    }

    /**
     * write onClick functionality.
     *
     * @param view       View to be clicked.
     * @param captureID  CaptureID of image.
     * @param captureURL Capture image url.
     */
    private void writeOnClick(View view, final String captureID, final String captureURL, final boolean merchantable) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mHelper.isCaptureIconTooltipFirstTime()) {
                    getShortOnClickDialog(captureID, captureURL, merchantable);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(EXTRA_CAPTURE_ID, captureID);
                    bundle.putString(EXTRA_CAPTURE_URL, captureURL);
                    bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);
                    Intent intent = new Intent(mContext, ShortActivity.class);
                    intent.putExtra(EXTRA_DATA, bundle);
                    mContext.startActivity(intent);
                }
                //Log Firebase event
                setAnalytics(FIREBASE_EVENT_WRITE_CLICKED);
            }
        });
    }

    /**
     * capture onClick functionality.
     *
     * @param view View to be clicked.
     */
    private void captureOnClick(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mHelper.isWriteIconTooltipFirstTime()) {

                    // open dialog
                    getCaptureOnClickDialog();
                } else {
                    startCaptureCollaboration();
                }
                //Log Firebase event
                setAnalytics(FIREBASE_EVENT_CAPTURE_CLICKED);
            }
        });
    }

    /**
     * Method to show intro dialog when user collaborated by clicking on capture
     */
    private void getCaptureOnClickDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_generic, false)
                .positiveText(mContext.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open capture functionality
                        startCaptureCollaboration();

                        dialog.dismiss();
                        //update status
                        mHelper.updateWriteIconToolTipStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_collab_intro));
        //Set title text
        textTitle.setText(getString(R.string.title_dialog_collab_capture));
        //Set description text
        textDesc.setText(getString(R.string.text_dialog_collab_capture));
    }

    /**
     * Method to show intro dialog when user collaborated by clicking on capture
     *
     * @param captureID    capture ID
     * @param captureURL   capture URl
     * @param merchantable merchantable true or false
     */
    private void getShortOnClickDialog(final String captureID, final String captureURL, final boolean merchantable) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_generic, false)
                .positiveText(mContext.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open short functionality

                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_CAPTURE_ID, captureID);
                        bundle.putString(EXTRA_CAPTURE_URL, captureURL);
                        bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);
                        Intent intent = new Intent(mContext, ShortActivity.class);
                        intent.putExtra(EXTRA_DATA, bundle);
                        mContext.startActivity(intent);

                        dialog.dismiss();
                        //update status
                        mHelper.updateCaptureIconToolTipStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_collab_intro));
        //Set title text
        textTitle.setText(getString(R.string.title_dialog_collab_short));
        //Set description text
        textDesc.setText(getString(R.string.text_dialog_collab_short));
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
     * Method to start the capture collaboration process
     */
    private void startCaptureCollaboration() {
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

}
