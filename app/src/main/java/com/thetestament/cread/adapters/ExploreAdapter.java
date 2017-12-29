package com.thetestament.cread.adapters;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationDetailsActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnExploreCaptureClickListener;
import com.thetestament.cread.listeners.listener.OnExploreFollowListener;
import com.thetestament.cread.listeners.listener.OnExploreLoadMoreListener;
import com.thetestament.cread.models.FeedModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.helpers.FeedHelper.collabOnCollab;
import static com.thetestament.cread.helpers.FeedHelper.getCollabCountText;
import static com.thetestament.cread.helpers.FeedHelper.getCreatorText;
import static com.thetestament.cread.helpers.FeedHelper.initializeSpannableString;
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
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_EXPLORE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a explore RecyclerView.
 */

public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<FeedModel> mExploreList;
    private FragmentActivity mContext;
    private Fragment mExploreFragment;
    private boolean mIsLoading;
    private String mUUID;
    private SharedPreferenceHelper mHelper;


    private OnExploreLoadMoreListener onExploreLoadMoreListener;
    private OnExploreFollowListener onExploreFollowListener;
    private OnExploreCaptureClickListener onExploreCaptureClickListener;


    /**
     * Required constructor.
     *
     * @param mExploreList List of explore data.
     * @param mContext     Context to be use.
     * @param mUUID        UUID of user.
     */
    public ExploreAdapter(List<FeedModel> mExploreList, FragmentActivity mContext, String mUUID, Fragment mExploreFragment) {
        this.mExploreList = mExploreList;
        this.mContext = mContext;
        this.mUUID = mUUID;
        this.mExploreFragment = mExploreFragment;

        mHelper = new SharedPreferenceHelper(mContext);
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnExploreLoadMoreListener(OnExploreLoadMoreListener onExploreLoadMoreListener) {
        this.onExploreLoadMoreListener = onExploreLoadMoreListener;
    }

    /**
     * Register a callback to be invoked when user clicks on follow button.
     */
    public void setOnExploreFollowListener(OnExploreFollowListener onExploreFollowListener) {
        this.onExploreFollowListener = onExploreFollowListener;
    }

    /**
     * Register a callback to be invoked when user clicks on capture button.
     */
    public void setOnExploreCaptureClickListener(OnExploreCaptureClickListener onExploreCaptureClickListener) {
        this.onExploreCaptureClickListener = onExploreCaptureClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mExploreList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_explore, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FeedModel data = mExploreList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Load creator profile picture
            loadCreatorPic(data.getCreatorImage(), itemViewHolder.imageCreator);
            //Set creator name
            //itemViewHolder.textCreatorName.setText(data.getCreatorName());

            // set text and click actions according to content type
            //performContentTypeSpecificOperations(itemViewHolder, data);
            FeedHelper.performContentTypeSpecificOperations(mContext
                    , data
                    , itemViewHolder.collabCount
                    , itemViewHolder.collabCount
                    , itemViewHolder.buttonCollaborate
                    , itemViewHolder.textCreatorName
                    , true
                    , false
                    , null);

            //Check follow status
            checkFollowStatus(data, itemViewHolder);

            //Load explore feed image
            loadFeedImage(data.getContentImage(), itemViewHolder.imageExplore);


            //Follow button click functionality
            followOnClick(position, data, itemViewHolder.buttonFollow);
            //ItemView collabOnWritingClick functionality
            itemViewOnClick(itemViewHolder.itemView, data, position);
            //Collaboration count click functionality
            collaborationCountOnClick(itemViewHolder.collabCount, data.getEntityID(), data.getContentType());

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }


        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mExploreList.size() - 1 && !mIsLoading) {
            if (onExploreLoadMoreListener != null) {
                //Lode more data here
                onExploreLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }

    }

    @Override
    public int getItemCount() {
        return mExploreList == null ? 0 : mExploreList.size();
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to load creator profile picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadCreatorPic(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to load explore feed image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadFeedImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);
    }

    /**
     * Method to open creator profile.
     *
     * @param view        View to be clicked.
     * @param creatorUUID UUID of the creator.
     */
    private void openCreatorProfile(View view, final String creatorUUID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(EXTRA_PROFILE_UUID, creatorUUID);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * Follow button collabOnWritingClick functionality
     *
     * @param itemPosition index  of the item.
     * @param data         Model for current item.
     * @param buttonFollow View to be clicked.
     */
    private void followOnClick(final int itemPosition, final FeedModel data, final TextView buttonFollow) {
        buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //Toggle follow button
                    toggleFollowButton(data.getFollowStatus(), buttonFollow, data.getCreatorName());
                    //Toggle status
                    data.setFollowStatus(!data.getFollowStatus());
                    //set listener
                    onExploreFollowListener.onFollowClick(data, itemPosition);

                    //Log firebase event
                    setAnalytics(FIREBASE_EVENT_FOLLOW_FROM_EXPLORE);
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }


            }
        });
    }

    /**
     * write collabOnWritingClick functionality.
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
     * capture collabOnWritingClick functionality.
     *
     * @param view View to be clicked.
     *             * @param shoid    short ID
     */
    private void captureOnClick(View view, final String shoid) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mHelper.isWriteIconTooltipFirstTime()) {

                    // open dialog
                    getCaptureOnClickDialog(shoid);
                } else {
                    onExploreCaptureClickListener.onClick(shoid);
                }
                //Log Firebase event
                setAnalytics(FIREBASE_EVENT_CAPTURE_CLICKED);
            }
        });
    }


    /**
     * ItemView click functionality.
     */
    private void itemViewOnClick(View view, final FeedModel feedModel, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, feedModel);
                bundle.putInt("position", position);

                Intent intent = new Intent(mContext, FeedDescriptionActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                mExploreFragment.startActivityForResult(intent, REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY);
            }
        });
    }

    /**
     * Method that performs operations according to content type and collaboration functionality
     *
     * @param itemViewHolder item view holder
     * @param data           data
     */

    private void performContentTypeSpecificOperations(ExploreAdapter.ItemViewHolder itemViewHolder, FeedModel data) {

        // initialize text
        String text = getCreatorText(mContext, data.getContentType(), data.isAvailableForCollab(), data.getCreatorName(), data.getCollabWithName());

        //Check for content type
        switch (data.getContentType()) {
            case CONTENT_TYPE_CAPTURE:

                // set collab count text
                if (data.getCollabCount() != 0) {
                    itemViewHolder.collabCount.setText(getCollabCountText(mContext, data.getCollabCount(), data.getContentType(), data.isAvailableForCollab()));
                    itemViewHolder.collabCount.setVisibility(View.VISIBLE);


                } else {
                    itemViewHolder.collabCount.setVisibility(View.GONE);
                }

                if (data.isAvailableForCollab()) {

                    // for stand alone capture

                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);
                    // set text
                    //itemViewHolder.buttonCollaborate.setText("Write");

                    //write click functionality on capture
                    writeOnClick(itemViewHolder.buttonCollaborate, data.getCaptureID(), data.getContentImage(), data.isMerchantable());

                    //String text = data.getCreatorName() + " added a capture ";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = -1;
                    int collabWithEndPos = -1;

                    // get clickable text;
                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());

                } else {

                    // showing collaborate button
                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);

                    collabOnCollab(itemViewHolder.buttonCollaborate, mContext, data.getEntityID(), data.isMerchantable());

                    //String text = data.getCreatorName() + " added a capture to " + data.getCollabWithName() + "'s short";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(data.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + data.getCollabWithName().length() + 2; // +2 for 's

                    // get clickable text
                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());


                }

                break;

            case CONTENT_TYPE_SHORT:

                // set collab count text
                if (data.getCollabCount() != 0) {
                    itemViewHolder.collabCount.setText(getCollabCountText(mContext, data.getCollabCount(), data.getContentType(), data.isAvailableForCollab()));
                    itemViewHolder.collabCount.setVisibility(View.VISIBLE);
                } else {
                    itemViewHolder.collabCount.setVisibility(View.GONE);
                }

                // check if available for collab
                if (data.isAvailableForCollab()) {

                    // for stand alone short

                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);
                    // set text
                    //itemViewHolder.buttonCollaborate.setText("Capture");

                    // capture click functionality on short
                    captureOnClick(itemViewHolder.buttonCollaborate, data.getShortID());

                    //String text = data.getCreatorName() + " wrote a short ";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = -1; // since no collabwith
                    int collabWithEndPos = -1; // since no collabwith

                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());


                } else {
                    // showing collaborate button
                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);

                    collabOnCollab(itemViewHolder.buttonCollaborate, mContext, data.getEntityID(), data.isMerchantable());
                    //String text = data.getCreatorName() + " wrote a short on " + data.getCollabWithName() + "'s capture";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(data.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + data.getCollabWithName().length() + 2; // +2 to incorporate 's

                    // get clickable text
                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());

                }

                break;
            default:
        }

    }


    /**
     * Method to toggle follow.
     *
     * @param followStatus true if following false otherwise.
     * @param buttonFollow VIew to be clicked.
     * @param creatorName  Name of content creator
     */
    private void toggleFollowButton(boolean followStatus, TextView buttonFollow, String creatorName) {
        if (followStatus) {
           /* //Change background
            ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_filled));
            //Change text color
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.white));
            //Change text to 'follow'
            buttonFollow.setText("Follow");*/
            // this case won't happen since follow button won't be visible and therefore user cannot click on it
            // show follow button
            buttonFollow.setVisibility(View.VISIBLE);

        } else {
            /*ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_outline));
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.grey_dark));
            //Change text to 'following'
            buttonFollow.setText("Following");*/
            buttonFollow.setVisibility(View.GONE);
            ViewHelper.getToast(mContext, "You are now following " + creatorName);

        }
    }


    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", mUUID);
        if (firebaseEvent.equals(FIREBASE_EVENT_WRITE_CLICKED)) {
            bundle.putString("class_name", "explore_item");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_WRITE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_FOLLOW_FROM_EXPLORE)) {
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_FOLLOW_FROM_EXPLORE, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_CAPTURE_CLICKED)) {
            bundle.putString("class_name", "explore_item");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_CAPTURE_CLICKED, bundle);
        }

    }

    /**
     * Method to check follow status..
     *
     * @param data data
     *             * @param itemViewHolder item view holder
     */
    private void checkFollowStatus(FeedModel data, ExploreAdapter.ItemViewHolder itemViewHolder) {
        if (data.getFollowStatus() || mUUID.equals(data.getUUID())) {
            /*ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_outline));
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.grey_dark));
            //Change text to 'following'
            buttonFollow.setText("Following");*/
            // hide follow button
            itemViewHolder.buttonFollow.setVisibility(View.GONE);
        } else {
            //Change background
            /*ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_filled));
            //Change text color
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.white));
            //Change text to 'follow'
            buttonFollow.setText("Follow");*/
            // show follow button
            itemViewHolder.buttonFollow.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Method to show intro dialog when user collaborated by clicking on capture
     *
     * @param shoid short ID on which user is collaborating
     */
    private void getCaptureOnClickDialog(final String shoid) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_generic, false)
                .positiveText(mContext.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open capture functionality

                        onExploreCaptureClickListener.onClick(shoid);

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
        textTitle.setText(mContext.getString(R.string.title_dialog_collab_capture));
        //Set description text
        textDesc.setText(mContext.getString(R.string.text_dialog_collab_capture));
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


        //Set filler im
        fillerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_collab_intro));
        //Set title text

        //Set title text
        textTitle.setText(mContext.getString(R.string.title_dialog_collab_short));
        //Set description text
        textDesc.setText(mContext.getString(R.string.text_dialog_collab_short));
    }

    /**
     * Collaboration count click functionality to launch collaborationDetailsActivity.
     *
     * @param textView   View to be clicked
     * @param entityID   Entity id of the content.
     * @param entityType Type of content i.e CAPTURE or SHORT
     */
    private void collaborationCountOnClick(TextView textView, final String entityID, final String entityType) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_ENTITY_ID, entityID);
                bundle.putString(EXTRA_ENTITY_TYPE, entityType);

                Intent intent = new Intent(mContext, CollaborationDetailsActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                mContext.startActivity(intent);
            }
        });
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        CircleImageView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.buttonFollow)
        TextView buttonFollow;
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageExplore)
        ImageView imageExplore;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.collabCount)
        TextView collabCount;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //LoadingViewHolder class
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.viewProgress)
        View progressView;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}