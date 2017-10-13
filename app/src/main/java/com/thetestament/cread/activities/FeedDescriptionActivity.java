package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.models.ExploreModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;

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


    @BindView(R.id.containerHatsOff)
    LinearLayout containerHatsOff;
    @BindView(R.id.containerComment)
    LinearLayout containerComment;
    @BindView(R.id.containerShares)
    LinearLayout containerShares;


    @State
    String mCampaignCreator, mClientImageUrl;
    @State
    String mContentTitle, mContentID, mImageURL, mContentURL;
    @State
    int mCampaignShareCount;
    @State
    long mCommentsCount;
    @State
    boolean mHatsOffStatus;
    @State
    boolean mToggleStatus = false;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    ExploreModel exploreModel;


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
            /*case REQUEST_CODE_EXPLORE_COMMENT:
                if (resultCode == RESULT_OK) {
                    getTopComments();
                }*/
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


    @OnClick(R.id.textHatsOffCount)
    void onTextHatsOffCountClicked() {
        Intent intent = new Intent(this, HatsOffActivity.class);
        intent.putExtra("cmid", mContentID);
        startActivity(intent);
    }

    /*
        @OnClick(R.id.textSharesCount)*/
    void onTextSharesCountClicked() {
        //Intent intent = new Intent(this, ShareDetailsActivity.class);
        //intent.putExtra("cmid", mContentID);
        //startActivity(intent);
    }

    /**
     * Method to initialize Toolbar
     */
    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Get data from intent
        retrieveIntentData();
        //Load client image
        // loadClientImage();
    }

    @OnClick(R.id.containerHatsOff)
    void onContainerHatsOffClicked() {
        //For tap animation
        // imageHatsOff.setAnimation(AnimationUtils.loadAnimation(this, R.anim.image_tap));

        //User has already given the hats off
        if (mHatsOffStatus) {
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.reverse_rotate_animation_hats_off));
            //Toggle hatsOff status
            mHatsOffStatus = false;
            exploreModel.setHatsOffCount(exploreModel.getHatsOffCount() - 1);
            //If hats off count is zero
            if (exploreModel.getHatsOffCount() < 1) {
                textHatsOffCount.setVisibility(View.GONE);
            }
            //hats off count is more than zero
            else {
                //Change hatsOffCount i.e decrease by one
                textHatsOffCount.setVisibility(View.VISIBLE);
                textHatsOffCount.setText(exploreModel.getHatsOffCount() + " Hats-off");
            }
            //Toggle hatsOff tint
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.grey));
            //updateHatsOffStatus(this, mContentID, mHatsOffStatus);

        } else {
            //Animation for hats off
            imageHatsOff.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_animation_hats_off));
            //Toggle hatsOff status
            mHatsOffStatus = true;
            //Change hatsOffCount i.e increase by one
            textHatsOffCount.setVisibility(View.VISIBLE);
            exploreModel.setHatsOffCount(exploreModel.getHatsOffCount() + 1);
            textHatsOffCount.setText(exploreModel.getHatsOffCount() + " Hats-off");
            //Toggle hatsOff tint
            imageHatsOff.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
            //updateHatsOffStatus(this, mContentID, mHatsOffStatus);

        }
    }


    @OnClick({R.id.containerComment, R.id.textShowComments})
    void onContainerCommentClicked() {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra("cmid", mContentID);
        //startActivityForResult(intent, REQUEST_CODE_EXPLORE_COMMENT);
    }

    /**
     * Method to retrieve data from intent and set it to respective views.
     */
    private void retrieveIntentData() {
/*
        exploreModel = getIntent().getParcelableExtra(EXTRA_EXPLORE_DESCRIPTION_DATA);

        //Set member variable
        mContentID = exploreModel.getCampaignID();
        mContentTitle = exploreModel.getCampaignTitle();
        mImageURL = exploreModel.getCampaignImagePath();
        mContentURL = exploreModel.getCampaignBaseUrl();
        mCampaignCreator = exploreModel.getCampaignCreator();
        mClientImageUrl = exploreModel.getCreatorImagePath();
        mCampaignShareCount = exploreModel.getCampaignShareNo();
        mHatsOffStatus = exploreModel.isHatsOff();
        mCommentsCount = exploreModel.getCommentsCount();
        //Set view text
        textCreatorTitle.setText(mCampaignCreator);
        textCampaignTitle.setText(mContentTitle);
        textCampaignDesc.setText(exploreModel.getCampaignDescription());
        //Set request text
        initRequestText(mCampaignCreator);
*/

        //If comment is zero than hide the view
        if (mCommentsCount == 0) {
            showAllComments.setVisibility(View.GONE);
        } else {
            //load comments
            //getTopComments();
        }

        //Check for hats of count
        if (exploreModel.getHatsOffCount() > 0) {
            textHatsOffCount.setText(exploreModel.getHatsOffCount() + " Hats-off");
        } else {
            textHatsOffCount.setVisibility(View.GONE);
        }

        //Check for shares count
        /*if (exploreModel.getCampaignShareNo() > 0) {
            textSharesCount.setText(exploreModel.getCampaignShareNo() + " Shares");
        } else {
            textSharesCount.setVisibility(View.GONE);
        }*/

        //Check whether user has given hats off to this campaign or not
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
     * Method to load client profile picture
     *//*
    private void loadClientImage() {
        //Load campaign image
        Picasso.with(this)
                .load(mClientImageUrl)
                .error(R.drawable.ic_placeholder_creator_48)
                .into(imageCreator);
    }*/

    /**
     * Change the request text color dynamically.
     *
     * @param campaignCreator Name of the person/organisation who created this campaign.
     */
  /*  private void initRequestText(String campaignCreator) {

        String articleText = "content";
        String requestText = "Please help " + campaignCreator + " by sharing this " + articleText + " in your network";

        Spannable spannableText
                = new SpannableString(requestText);
        //To changeText color dynamically
        int creatorStartIndex = requestText.indexOf(campaignCreator);
        int creatorEndIndex = requestText.lastIndexOf(campaignCreator);
        int articleStartIndex = requestText.indexOf(articleText);
        int articleEndIndex = requestText.lastIndexOf(articleText);

        //For creator text
        spannableText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.black_defined))
                , creatorStartIndex, creatorEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //For article text
        spannableText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.black_defined))
                , articleStartIndex, articleEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textRequest.setText(spannableText);

    }*/

    /**
     * Method to update hats off status of campaign.
     *
     * @param context    Context to be use.
     * @param campaignID Campaign ID i.e String
     * @param isHatsOff  boolean true if user has given hats off to campaign, false otherwise.
     */
  /*  private void updateHatsOffStatus(Context context, String campaignID, boolean isHatsOff) {
        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("uuid", AccountManagerUtils.getUserID(context));
            jsonObject.put("authkey", myApplication.getAuthToken());
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
                                SnackBarHelper.getSnackBar(rootView
                                        , getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Do nothing
                                } else {
                                    SnackBarHelper.getSnackBar(rootView
                                            , getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            SnackBarHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        SnackBarHelper.getSnackBar(rootView
                                , getString(R.string.error_msg_server));
                    }
                });

    }*/

    /**
     * RxJava2 implementation for retrieving comment data from server
     */
/*
    private void getTopComments() {
        //For smooth scrolling
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        final List<CommentsModel> mCommentsList = new ArrayList<>();
        mCompositeDisposable.add(getObservableFromServer(this, BuildConfig.URL + "/comment/load", mContentID, 0)
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
                                    CampaignDescriptionCommentsModel commentsData = new CampaignDescriptionCommentsModel();
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
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(ExploreDescriptionActivity.this, resId);
                            recyclerView.setLayoutAnimation(animation);

                            //Change visibility
                            recyclerView.setVisibility(View.VISIBLE);
                            //Set layout manager
                            recyclerView.setLayoutManager(new LinearLayoutManager(ExploreDescriptionActivity.this));
                            //Set adapter
                            recyclerView.setAdapter(new CampaignDescriptionCommentAdapter(mCommentsList, ExploreDescriptionActivity.this));

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
*/
    @OnClick(R.id.containerCreator)
    void onViewClicked() {
        // TODO: Profile launching functionality
    }

    @OnClick(R.id.containerShares)
    void shareOnClick() {
        //Todo share onClick functionality
    }
}
