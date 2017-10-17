package com.thetestament.cread.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v13.view.ViewCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CommentsAdapter;
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
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_COMMNETS_ACTIVTY;

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
    @BindView(R.id.image)
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

    @State
    String mEntityID, mCreatorImageUrl;
    @State
    boolean mHatsOffStatus;
    @State
    long mHatsOffCount, mCommentsCount;

    @State
    String mContentTitle, mImageURL, mContentURL;


    @State
    boolean mToggleStatus = false;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    FeedModel mFeedData;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_description);
        ButterKnife.bind(this);
        //initialize views
        initViews();

        //Load campaign image
        loadCampaignImage();
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
            case REQUEST_CODE_COMMNETS_ACTIVTY:
                if (resultCode == RESULT_OK) {
                    //Load comments data
                    getTopComments();
                }
        }

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

    /**
     * HatsOffCount click functionality to open "HatsOffActivity" screen.
     */
    @OnClick(R.id.textHatsOffCount)
    void hatsOffCountOnClick() {
        Intent intent = new Intent(this, HatsOffActivity.class);
        intent.putExtra("entityID", mEntityID);
        startActivity(intent);
    }

    /**
     * Click functionality to open "CommentsActivity" screen.
     */
    @OnClick({R.id.containerComment, R.id.textShowComments, R.id.textCommentsCount})
    void onCommentsClicked() {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra("entityID", mEntityID);
        startActivityForResult(intent, REQUEST_CODE_COMMNETS_ACTIVTY);
    }

    /**
     * HatsOff onClick functionality.
     */
    @OnClick(R.id.containerHatsOff)
    void onContainerHatsOffClicked() {
        //User has already given hats off
        if (mHatsOffStatus) {
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.reverse_rotate_animation_hats_off));
            //Toggle hatsOff status
            mHatsOffStatus = false;
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
            //Toggle hatsOff tint
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.grey));
            updateHatsOffStatus(this, mEntityID, mHatsOffStatus);

        } else {
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation_hats_off));
            //Toggle hatsOff status
            mHatsOffStatus = true;
            //Change hatsOffCount i.e increase by one
            textHatsOffCount.setVisibility(View.VISIBLE);
            mFeedData.setHatsOffCount(mFeedData.getHatsOffCount() + 1);
            textHatsOffCount.setText(mFeedData.getHatsOffCount() + " Hats-off");
            //Toggle hatsOff tint
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
            updateHatsOffStatus(this, mEntityID, mHatsOffStatus);
        }
    }


    /**
     * Click functionality to open creator profile.
     */
    @OnClick(R.id.containerCreator)
    void onProfileContainer() {
        // TODO: Profile launching functionality
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    /**
     * Share button click functionality.
     */
    @OnClick(R.id.containerShares)
    void shareOnClick() {
        //Todo share onClick functionality
    }

    /**
     * Method to initialize Toolbar
     */
    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Get data from intent
        retrieveIntentData();
        //Load client image
        loadCreatorImage(mImageURL);
    }


    /**
     * Method to retrieve data from intent and set it to respective views.
     */
    private void retrieveIntentData() {

        mFeedData = getIntent().getParcelableExtra(EXTRA_FEED_DESCRIPTION_DATA);

        //Set member variable
        mEntityID = mFeedData.getEntityID();
        mCreatorImageUrl = mFeedData.getCreatorImage();
        mHatsOffStatus = mFeedData.isHatsOffStatus();

        //Set views
        textCreatorName.setText(mFeedData.getCreatorName());


        //If comment is zero than hide the view
        if (mCommentsCount == 0) {
            showAllComments.setVisibility(View.GONE);
        } else {
            //load comments
            getTopComments();
        }

        //Check for hats of count
        if (mFeedData.getHatsOffCount() > 0) {
            textHatsOffCount.setText(mFeedData.getHatsOffCount() + " Hats-off");
        } else {
            textHatsOffCount.setVisibility(View.GONE);
        }

        //Check for shares count
        if (mFeedData.getCommentCount() > 0) {
            textCommentsCount.setText(mFeedData.getCommentCount() + " Comments");
        } else {
            textCommentsCount.setVisibility(View.GONE);
        }

        //Check whether user has given hats off or not
        if (mHatsOffStatus) {
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation_hats_off_fast));
            mHatsOffStatus = true;
        } else {
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.grey));
            mHatsOffStatus = false;
        }
    }

    /**
     * Method to load story image.
     */
    private void loadCampaignImage() {
        // TODO: error handling
        //Load campaign image
        Picasso.with(this)
                .load(mImageURL)
                .into(image);
    }

    /**
     * Method to load creator profile picture.
     */
    private void loadCreatorImage(String imageURL) {
        //Load campaign image
        Picasso.with(this)
                .load(imageURL)
                .error(R.drawable.ic_account_circle_48)
                .into(imageCreator);
    }


    /**
     * Method to update hats off status.
     *
     * @param context    Context to be use.
     * @param campaignID Campaign ID i.e String
     * @param isHatsOff  boolean true if user has given hats off to campaign, false otherwise.
     */
    private void updateHatsOffStatus(Context context, String campaignID, boolean isHatsOff) {
        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
            jsonObject.put("cmid", campaignID);
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
                                ViewHelper.getSnackBar(rootView
                                        , getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Do nothing
                                } else {
                                    ViewHelper.getSnackBar(rootView
                                            , getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        ViewHelper.getSnackBar(rootView
                                , getString(R.string.error_msg_server));
                    }
                });

    }


    /**
     * RxJava2 implementation for retrieving comment data from server
     */
    private void getTopComments() {
        //For smooth scrolling
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        final List<CommentsModel> mCommentsList = new ArrayList<>();
        mCompositeDisposable.add(getObservableFromServer(this, BuildConfig.URL + "/comment/load", mEntityID, 0)
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
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                        }
                        //No data
                        else if (mCommentsList.size() == 0) {
                            //Hide views
                            recyclerView.setVisibility(View.GONE);
                            showAllComments.setVisibility(View.GONE);
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(FeedDescriptionActivity
                                    .this, resId);
                            recyclerView.setLayoutAnimation(animation);

                            //Change visibility
                            recyclerView.setVisibility(View.VISIBLE);
                            //Set layout manager
                            recyclerView.setLayoutManager(new LinearLayoutManager(FeedDescriptionActivity.this));
                            //Set adapter
                            recyclerView.setAdapter(new CommentsAdapter(mCommentsList, FeedDescriptionActivity.this));

                            //If there is a comment
                            if (mCommentsList.size() > 0) {
                                showAllComments.setVisibility(View.VISIBLE);
                            } else {
                                showAllComments.setVisibility(View.GONE);
                            }
                        }
                    }
                })
        );
    }


}
