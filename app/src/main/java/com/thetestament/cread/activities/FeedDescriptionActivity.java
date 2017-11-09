package com.thetestament.cread.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v13.view.ViewCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CommentsAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.models.CommentsModel;
import com.thetestament.cread.models.FeedModel;

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

import static com.thetestament.cread.helpers.ImageHelper.getLocalBitmapUri;
import static com.thetestament.cread.helpers.NetworkHelper.getCommentObservableFromServer;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_HAVE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_COMMENTS_ACTIVITY;

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
    @BindView(R.id.textHatsOff)
    TextView textHatsOff;
    @BindView(R.id.containerHatsOff)
    LinearLayout containerHatsOff;
    @BindView(R.id.imageComment)
    ImageView imageComment;
    @BindView(R.id.textComment)
    TextView textComment;
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


    private SharedPreferenceHelper mHelper;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private FeedModel mFeedData;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_description);
        ButterKnife.bind(this);
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
        }

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
     * Click functionality to open creator profile.
     */
    @OnClick(R.id.containerCreator)
    void onProfileContainer() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(EXTRA_PROFILE_UUID, mFeedData.getUUID());
        startActivity(intent);
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
        Intent intent = new Intent(this, MerchandisingProductsActivity.class);
        intent.putExtra(EXTRA_ENTITY_ID, mFeedData.getEntityID());
        startActivity(intent);
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
    @OnClick(R.id.containerHatsOff)
    void onContainerHatsOffClicked() {
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
                textHatsOffCount.setText(mFeedData.getHatsOffCount() + " Hats-off");
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
            textHatsOffCount.setText(mFeedData.getHatsOffCount() + " Hats-off");
        }
        //update hats off status on server
        updateHatsOffStatus(mFeedData.getEntityID(), mFeedData.getHatsOffStatus());
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
        //Set creator name
        textCreatorName.setText(mFeedData.getCreatorName());

        //Check for hats of count
        if (mFeedData.getHatsOffCount() > 0) {
            textHatsOffCount.setText(mFeedData.getHatsOffCount() + " Hats-off");
        } else {
            textHatsOffCount.setVisibility(View.GONE);
        }

        //Check for comment count
        if (mFeedData.getCommentCount() > 0) {
            //Set comment count
            textCommentsCount.setText(mFeedData.getCommentCount() + " Comments");
            //load comments
            getTopComments(mFeedData.getEntityID());
        } else {
            textCommentsCount.setVisibility(View.GONE);
            showAllComments.setVisibility(View.GONE);
        }
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
                , 0
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
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", mHelper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_HAVE_CLICKED)) {
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_HAVE_CLICKED, bundle);
        } else {
            bundle.putString("entity_id", mFeedData.getEntityID());
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION, bundle);
        }
    }

}
