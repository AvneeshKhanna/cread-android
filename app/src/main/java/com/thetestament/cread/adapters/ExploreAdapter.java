package com.thetestament.cread.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.MerchandisingProductsActivity;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.activities.ShortActivity;
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

import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_EXPLORE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a explore RecyclerView.
 */

public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<FeedModel> mExploreList;
    private FragmentActivity mContext;
    private boolean mIsLoading;
    private String mUUID;

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
    public ExploreAdapter(List<FeedModel> mExploreList, FragmentActivity mContext, String mUUID) {
        this.mExploreList = mExploreList;
        this.mContext = mContext;
        this.mUUID = mUUID;
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

            SpannableString ss = new SpannableString("Biswa kalyan Rath wrote a short on Avnnesh khanna's Capture today");
            ClickableSpan collaboratorSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    mContext.startActivity(new Intent(mContext, MerchandisingProductsActivity.class));
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    ds.setColor(ContextCompat.getColor(mContext, R.color.grey_dark));
                }
            };
            ss.setSpan(collaboratorSpan, 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ClickableSpan collaboratedWithSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    mContext.startActivity(new Intent(mContext, MerchandisingProductsActivity.class));
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    ds.setColor(ContextCompat.getColor(mContext, R.color.grey_dark));
                }
            };
            //ss.setSpan(collaboratedWithSpan, 35, 51, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


            itemViewHolder.textCreatorName.setText(ss);
            itemViewHolder.textCreatorName.setMovementMethod(LinkMovementMethod.getInstance());
            itemViewHolder.textCreatorName.setHighlightColor(Color.TRANSPARENT);

            //Hide follow button  if creator and content consumer is same
            if (mUUID.equals(data.getUUID())) {
                itemViewHolder.buttonFollow.setVisibility(View.INVISIBLE);
            } else {
                //Show follow button
                itemViewHolder.buttonFollow.setVisibility(View.VISIBLE);
            }

            //Load explore feed image
            loadFeedImage(data.getContentImage(), itemViewHolder.imageExplore);

            //Check follow status
            // TODO uncomment
            //checkFollowStatus(mContext, data.getFollowStatus(), itemViewHolder.buttonFollow);

            //Check for content type
            switch (data.getContentType()) {
                case CONTENT_TYPE_CAPTURE:
                    //itemViewHolder.imageWorkType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_camera_alt_24));
                    itemViewHolder.buttonCompose.setVisibility(View.VISIBLE);
                    //Show tooltip oh edit button
                    if (position == 0) {
                        SharedPreferenceHelper helper = new SharedPreferenceHelper(mContext);
                        if (helper.isWriteIconTooltipFirstTime()) {
                            ViewHelper.getToolTip(itemViewHolder.buttonCompose, "Have some thoughts about this photo? Tap to write on it", mContext);
                        }
                        helper.updateWriteIconToolTipStatus(false);
                    }
                    break;
                case CONTENT_TYPE_SHORT:
                    //itemViewHolder.imageWorkType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_create_24));
                    itemViewHolder.buttonCompose.setVisibility(View.GONE);
                    break;
                default:
            }
            //Click functionality to launch profile of creator
            openCreatorProfile(itemViewHolder.containerCreator, data.getUUID());
            //Follow button click functionality
            followOnClick(position, data, itemViewHolder.buttonFollow);
            //Compose click functionality
            composeOnClick(itemViewHolder.buttonCompose, data.getCaptureID(), data.getContentImage(), data.isMerchantable());
            //ItemView onClick functionality
            itemViewOnClick(itemViewHolder.itemView, data);


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
     * Follow button onClick functionality
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
                    toggleFollowButton(mContext, data.getFollowStatus(), buttonFollow);
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
     * Compose onClick functionality.
     *
     * @param view       View to be clicked.
     * @param captureID  Capture ID of the content.
     * @param captureURL Capture image url.
     * @param
     */
    private void composeOnClick(View view, final String captureID, final String captureURL, final boolean merchantable) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onExploreCaptureClickListener.onClick(captureID);
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_CAPTURE_ID, captureID);
                bundle.putString(EXTRA_CAPTURE_URL, captureURL);
                bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);
                Intent intent = new Intent(mContext, ShortActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                mContext.startActivity(intent);
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_WRITE_CLICKED);
            }
        });
    }

    /**
     * ItemView click functionality.
     */
    private void itemViewOnClick(View view, final FeedModel feedModel) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FeedDescriptionActivity.class);
                intent.putExtra(EXTRA_FEED_DESCRIPTION_DATA, feedModel);
                mContext.startActivity(intent);
            }
        });
    }


    /**
     * Method to toggle follow.
     *
     * @param followStatus true if following false otherwise.
     * @param context      Context to use.
     * @param buttonFollow VIew to be clicked.
     */
    private void toggleFollowButton(Context context, boolean followStatus, TextView buttonFollow) {
        if (followStatus) {
            //Change background
            ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_filled));
            //Change text color
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.white));
            //Change text to 'follow'
            buttonFollow.setText("Follow");

        } else {
            ViewCompat.setBackground(buttonFollow
                    , ContextCompat.getDrawable(context
                            , R.drawable.button_outline));
            buttonFollow.setTextColor(ContextCompat.getColor(context
                    , R.color.grey_dark));
            //Change text to 'following'
            buttonFollow.setText("Following");
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
        }

    }

    /**
     * Method to check follow status..
     *
     * @param followStatus true if following false otherwise.
     * @param context      Context to use.
     * @param buttonFollow VIew to be clicked.
     */
    private void checkFollowStatus(Context context, boolean followStatus, TextView buttonFollow) {
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
        @BindView(R.id.buttonCompose)
        ImageView buttonCompose;

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