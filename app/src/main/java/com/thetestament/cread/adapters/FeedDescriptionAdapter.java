package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationDetailsActivity;
import com.thetestament.cread.activities.CommentsActivity;
import com.thetestament.cread.activities.HatsOffActivity;
import com.thetestament.cread.activities.MerchandisingProductsActivity;
import com.thetestament.cread.helpers.DownvoteHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ProfileMentionsHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.CommentsModel;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.AspectRatioUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.CreadApp.IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION;
import static com.thetestament.cread.adapters.CommentsAdapter.openCreatorProfile;
import static com.thetestament.cread.adapters.CommentsAdapter.toggleComment;
import static com.thetestament.cread.helpers.ContentHelper.getMenuActionsBottomSheet;
import static com.thetestament.cread.helpers.FeedHelper.initCaption;
import static com.thetestament.cread.helpers.FeedHelper.initializeShareDialog;
import static com.thetestament.cread.helpers.FeedHelper.updateDotSeperatorVisibility;
import static com.thetestament.cread.helpers.FeedHelper.updateDownvoteAndSeperatorVisibility;
import static com.thetestament.cread.helpers.FeedHelper.updatePostTimestamp;
import static com.thetestament.cread.helpers.LongShortHelper.checkLongFormStatus;
import static com.thetestament.cread.helpers.LongShortHelper.initLongFormPreviewClick;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_SHORT_UUID;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_CAPTURE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_FEED_DESCRIPTION;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_HAVE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_COMMENTS_ACTIVITY;

/**
 * Created by prakharchandna on 05/04/18.
 */

public class FeedDescriptionAdapter extends RecyclerView.Adapter {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_HEADER = 2;
    private List<FeedModel> mFeedList;
    private List<CommentsModel> mCommentsList = new ArrayList<>();
    private FragmentActivity mContext;
    private boolean mIsLoading;
    /*
    Flag to check whether to scroll to comments section
    when opened from updates screen
     */
    private boolean shouldScroll;

    private SharedPreferenceHelper mHelper;
    private CompositeDisposable mCompositeDisposable;

    private listener.OnFeedLoadMoreListener onFeedLoadMoreListener;
    private listener.OnHatsOffListener onHatsOffListener;
    private listener.OnShareListener onShareListener;
    private listener.OnShareLinkClickedListener onShareLinkClickedListener;
    private listener.OnDownvoteClickedListener onDownvoteClickedListener;
    private listener.OnExploreFollowListener onExploreFollowListener;
    private listener.OnContentDeleteListener onContentDeleteListener;


    /**
     * Required constructor.
     *
     * @param mFeedList            List of feed data.
     * @param mContext             Context to be use.
     * @param mCompositeDisposable
     */
    public FeedDescriptionAdapter(List<FeedModel> mFeedList, FragmentActivity mContext, CompositeDisposable mCompositeDisposable, boolean shouldScroll) {
        this.mFeedList = mFeedList;
        this.mContext = mContext;
        this.shouldScroll = shouldScroll;

        this.mCompositeDisposable = mCompositeDisposable;

        mHelper = new SharedPreferenceHelper(mContext);
    }


    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnFeedLoadMoreListener(listener.OnFeedLoadMoreListener onFeedLoadMoreListener) {
        this.onFeedLoadMoreListener = onFeedLoadMoreListener;
    }

    /**
     * Register a callback to be invoked when hats off is clicked.
     */
    public void setHatsOffListener(listener.OnHatsOffListener onHatsOffListener) {
        this.onHatsOffListener = onHatsOffListener;
    }


    /**
     * Register a callback to be invoked when user clicks on share button.
     */
    public void setOnShareListener(listener.OnShareListener onShareListener) {
        this.onShareListener = onShareListener;
    }

    /**
     * Register a callback to be invoked when user clicks on share link button.
     */
    public void setOnShareLinkClickedListener(listener.OnShareLinkClickedListener onShareLinkClickedListener) {
        this.onShareLinkClickedListener = onShareLinkClickedListener;
    }

    /**
     * Register a callback to be invoked when user clicks on downvote button.
     */
    public void setOnDownvoteClickedListener(listener.OnDownvoteClickedListener onDownvoteClickedListener) {
        this.onDownvoteClickedListener = onDownvoteClickedListener;
    }

    /**
     * Register a callback to be invoked when user clicks on follow button.
     */
    public void setOnFollowListener(listener.OnExploreFollowListener onExploreFollowListener) {
        this.onExploreFollowListener = onExploreFollowListener;
    }

    /**
     * Register a callback to be invoked when user clicks on delete button.
     */
    public void setOnContentDeleteListener(listener.OnContentDeleteListener onContentDeleteListener) {
        this.onContentDeleteListener = onContentDeleteListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mFeedList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (isPositionHeader(position)) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_feed_description, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.header_more_posts, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final FeedModel data = mFeedList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Load creator profile picture
            loadCreatorPic(data.getCreatorImage(), itemViewHolder.imageCreator);
            //Set image width and height
            AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                    , data.getImgHeight()
                    , itemViewHolder.image);
            //Load feed image
            loadFeedImage(data.getContentImage(), itemViewHolder.image);

            // set text and click actions acc. to content type
            FeedHelper.performContentTypeSpecificOperations(mContext
                    , data
                    , itemViewHolder.textCollabCount
                    , itemViewHolder.containerCollabCount
                    , itemViewHolder.buttonCollaborate
                    , itemViewHolder.textCreatorName
                    , false
                    , false
                    , null);

            //update downvote and dot seperator visibility
            updateDownvoteAndSeperatorVisibility(data, itemViewHolder.dotSeperatorRight, itemViewHolder.imageDownvote);
            //check downvote status
            DownvoteHelper downvoteHelper = new DownvoteHelper();
            downvoteHelper.updateDownvoteUI(itemViewHolder.imageDownvote, data.isDownvoteStatus(), mContext);
            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);

            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder.containerShares, data.getContentImage(), data.getEntityID(), data.getCreatorName(), data);
            //HatsOff onClick functionality
            hatsOffOnClick(itemViewHolder, data, position);
            //Collaboration count click functionality
            collaborationContainerOnClick(itemViewHolder.containerCollabCount, data.getEntityID(), data.getContentType());
            //have button click
            onHaveViewClicked(data, itemViewHolder);
            // caption on click
            onTitleClicked(itemViewHolder.textTitle);
            // on hatsoff count click
            hatsOffCountOnClick(itemViewHolder, data);
            //Comment count click functionality
            commentOnClick(itemViewHolder.containerCommentsCount, data.getEntityID());
            // show all comments click functionality
            commentOnClick(itemViewHolder.textShowComments, data.getEntityID());
            // downvote click
            downvoteOnClick(itemViewHolder.imageDownvote, data, position, itemViewHolder);
            //check long form status
            checkLongFormStatus(itemViewHolder.containerLongShortPreview, data);
            //long form on click
            initLongFormPreviewClick(itemViewHolder.containerLongShortPreview, data, mContext, mCompositeDisposable);

            // initialize hatsoff and comment count
            //Check for hats of count
            if (data.getHatsOffCount() > 0) {
                // set visible
                itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                //Set hatsOff count
                itemViewHolder.textHatsoffCount.setText(String.valueOf(data.getHatsOffCount()));
            } else {
                //Hide hatsOff count textView
                itemViewHolder.containerHatsOffCount.setVisibility(View.GONE);
            }

            // update dot visibility
            updateDotSeperatorVisibility(data, itemViewHolder.dotSeperator);

            //Check for comment count
            if (data.getCommentCount() > 0) {
                // set visible
                itemViewHolder.containerCommentsCount.setVisibility(View.VISIBLE);
                //Set comment count
                itemViewHolder.textCommentsCount.setText(String.valueOf(data.getCommentCount()));
            } else {
                itemViewHolder.containerCommentsCount.setVisibility(View.GONE);
            }
            // toggle top comments view visibility
            if (position == 0 && data.getCommentCount() > 0) {
                itemViewHolder.viewTopComments.setVisibility(View.VISIBLE);
                itemViewHolder.textShowComments.setVisibility(View.VISIBLE);

                //update comments view
                updateCommentsView(mCommentsList, itemViewHolder);
            } else {
                itemViewHolder.viewTopComments.setVisibility(View.GONE);
                itemViewHolder.textShowComments.setVisibility(View.GONE);
            }
            // initialize caption
            initCaption(mContext, data, itemViewHolder.textTitle);
            // check follow status
            checkFollowStatus(data, itemViewHolder);
            // follow click
            followOnClick(position, data, itemViewHolder.buttonFollow);
            // check whether to show menu options
            showMenuOptions(data, itemViewHolder, position);

            if (position == 0) {
                showTooltip(itemViewHolder);
            }

            // show downvote intro dialog
            if (data.isEligibleForDownvote() && mHelper.isDownvoteDialogFirstTime()) {
                // show dialog
                getDownvoteDialog(itemViewHolder);
            }

            // init post timestamp
            updatePostTimestamp(itemViewHolder.textTimeStamp, data);

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        } else if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            // if size is greater than 2 (original data and header are present in the list)
            // more posts exist
            // set text
            if (mFeedList.size() > 2) {
                // set visible
                headerViewHolder.textHeader.setVisibility(View.VISIBLE);

                FeedModel originalData = mFeedList.get(0);
                // get creator and collab first name
                String creatorName = originalData.getCreatorName();
                String creatorFName = creatorName.split(" ")[0];
                String collabName = originalData.getCollabWithName();
                String collabFName = "";
                if (collabName != null) {
                    collabFName = collabName.split(" ")[0];
                }

                String morePostsText = collabName == null
                        || originalData.getUUID().equals(originalData.getCollabWithUUID())
                        ? "More from " + creatorFName : "More from " + creatorFName + " and " + collabFName;
                headerViewHolder.textHeader.setText(morePostsText);
            } else {
                // hide view
                headerViewHolder.textHeader.setVisibility(View.GONE);
            }


        }


        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mFeedList.size() - 1 && !mIsLoading && position != 0) {
            if (onFeedLoadMoreListener != null) {
                //Lode more data here
                onFeedLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }

    }

    @Override
    public int getItemCount() {
        return mFeedList == null ? 0 : mFeedList.size();
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootView)
        CardView rootView;
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
        @BindView(R.id.buttonHave)
        LinearLayout buttonHave;
        @BindView(R.id.lineSeparatorTop)
        View lineSeparatorTop;
        @BindView(R.id.containerHatsOff)
        LinearLayout containerHatsOff;
        @BindView(R.id.lineSeparatorBottom)
        View lineSeparatorBottom;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.textTitle)
        TextView textTitle;
        @BindView(R.id.dotSeperator)
        TextView dotSeperator;
        @BindView(R.id.buttonFollow)
        TextView buttonFollow;
        @BindView(R.id.buttonMenu)
        ImageView buttonMenu;
        @BindView(R.id.imageDownvote)
        ImageView imageDownvote;
        @BindView(R.id.dotSeperatorRight)
        TextView dotSeperatorRight;
        @BindView(R.id.containerLongShortPreview)
        FrameLayout containerLongShortPreview;
        @BindView(R.id.containerComment)
        LinearLayout containerComment;
        @BindView(R.id.containerShares)
        LinearLayout containerShares;
        @BindView(R.id.viewTopComments)
        public LinearLayout viewTopComments;
        @BindView(R.id.textShowComments)
        public TextView textShowComments;
        @BindView(R.id.textTimestamp)
        TextView textTimeStamp;

        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;
        //Variable to maintain  hats off view rotation status
        private boolean mIsRotated = false;

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

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textHeader)
        TextView textHeader;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void updateCommentsList(List<CommentsModel> mCommentsList) {
        this.mCommentsList = mCommentsList;
    }

    public void updateCommentsView(List<CommentsModel> comments, ItemViewHolder itemViewHolder) {
        itemViewHolder.viewTopComments.removeAllViews();

        for (CommentsModel comment : comments) {
            View commentView = LayoutInflater.from(mContext).inflate(R.layout.item_comment, itemViewHolder.viewTopComments, false);
            itemViewHolder.viewTopComments.addView(commentView);

            CircleImageView imageUser = commentView.findViewById(R.id.imageUser);
            TextView textUserName = commentView.findViewById(R.id.textUserName);
            TextView textComment = commentView.findViewById(R.id.textComment);

            ImageHelper.loadImageFromPicasso(mContext, imageUser, comment.getProfilePicUrl(), R.drawable.ic_account_circle_100);
            textUserName.setText(comment.getFirstName() + " " + comment.getLastName());
            textComment.setText(comment.getComment());

            // set profile mentions
            ProfileMentionsHelper.setProfileMentionsForViewing(comment.getComment(), mContext, textComment);

            // set hash tags on comment
            FeedHelper feedHelper = new FeedHelper();
            feedHelper.setHashTags(textComment, mContext, R.color.blue_dark);

            // Expand and collapse comments.
            toggleComment(textComment);
            //Open creator profile
            openCreatorProfile(textUserName, comment.getUuid(), mContext);
            openCreatorProfile(imageUser, comment.getUuid(), mContext);


        }
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
                .error(R.drawable.ic_account_circle_100)
                .into(imageView);
    }

    /**
     * Method to load feed image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadFeedImage(String imageUrl, ImageView imageView) {
        if (IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION) {
            Picasso.with(mContext).invalidate(imageUrl);
        }


        RequestCreator requestCreator = Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder);

        if (IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION) {
            requestCreator.networkPolicy(NetworkPolicy.NO_CACHE);
        }


        requestCreator.into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                ActivityCompat.startPostponedEnterTransition(mContext);
            }

            @Override
            public void onError() {
                ActivityCompat.startPostponedEnterTransition(mContext);
            }
        });
    }

    /**
     * Method to check hatsOff status and perform operation accordingly.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, ItemViewHolder itemViewHolder) {
        if (hatsOffStatus) {
            //Change color
            itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
            //Set rotation to 30
            itemViewHolder.imageHatsOff.setRotation(30);
            //update flags
            itemViewHolder.mIsHatsOff = true;
            itemViewHolder.mIsRotated = true;
        } else {
            //Change color to transparent
            itemViewHolder.imageHatsOff.setColorFilter(Color.TRANSPARENT);
            //Set rotation to 0
            itemViewHolder.imageHatsOff.setRotation(0);
            //update flags
            itemViewHolder.mIsHatsOff = false;
            itemViewHolder.mIsRotated = false;
        }
    }

    /**
     * Compose onClick functionality.
     *
     * @param view     View to be clicked.
     * @param entityID Entity ID
     */
    private void commentOnClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(EXTRA_ENTITY_ID, entityID);
                // fixme
                mContext.startActivityForResult(intent, REQUEST_CODE_COMMENTS_ACTIVITY);
            }
        });
    }

    /**
     * Share onClick functionality.
     *
     * @param view       View to be clicked.
     * @param pictureUrl URL of the picture to be shared.
     */
    private void shareOnClick(View view, final String pictureUrl, final String entityID, final String creatorName, final FeedModel data) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                                loadBitmapForSharing(data);
                                break;
                            case 1:
                                // link sharing
                                // get deep link from server
                                onShareLinkClickedListener.onShareLinkClicked(entityID, pictureUrl, creatorName);
                                break;

                        }
                    }
                });


            }
        });
    }


    /**
     * Method to load bitmap image to be shared
     */
    private void loadBitmapForSharing(final FeedModel data) {
        Picasso.with(mContext).load(data.getContentImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //Set Listener
                onShareListener.onShareClick(bitmap, data);
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION, data.getEntityID());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_image));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

    }

    /**
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final ItemViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.imageHatsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //User has already given the hats off
                    if (itemViewHolder.mIsHatsOff) {
                        //Animation for hats off
                        if (itemViewHolder.mIsRotated) {
                            itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.reverse_rotate_animation_hats_off_60_degree));
                        } else {
                            itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.reverse_rotate_animation_hats_off_30_degree));
                        }
                        //Toggle hatsOff tint
                        itemViewHolder.imageHatsOff.setColorFilter(Color.TRANSPARENT);
                        //Update hats of count i.e decrease by one
                        data.setHatsOffCount(data.getHatsOffCount() - 1);
                        //If hats off count is zero
                        if (data.getHatsOffCount() < 1) {
                            itemViewHolder.containerHatsOffCount.setVisibility(View.GONE);
                        }
                        //hats off count is more than zero
                        else {
                            //Change hatsOffCount i.e decrease by one
                            itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                            itemViewHolder.textHatsoffCount.setText(String.valueOf(data.getHatsOffCount()));
                        }
                    } else {
                        //Animation for hats off
                        if (itemViewHolder.mIsRotated) {
                            itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off_0_degree));
                        } else {
                            itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off_30_degree));
                        }
                        //Toggle hatsOff tint
                        itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
                        //Change hatsOffCount i.e increase by one
                        data.setHatsOffCount(data.getHatsOffCount() + 1);
                        //Change hatsOffCount i.e increase by one
                        itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                        itemViewHolder.textHatsoffCount.setText(String.valueOf(data.getHatsOffCount()));
                    }

                    updateDotSeperatorVisibility(data, itemViewHolder.dotSeperator);

                    //Toggle hatsOff status
                    itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                    //Update hats off here
                    data.setHatsOffStatus(itemViewHolder.mIsHatsOff);
                    //Listener
                    onHatsOffListener.onHatsOffClick(data, itemPosition);
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }
            }
        });
    }

    /**
     * Collaboration count click functionality to launch collaborationDetailsActivity.
     *
     * @param linearLayout
     * @param entityID     Entity id of the content.
     * @param entityType   Type of content i.e CAPTURE or SHORT
     */
    private void collaborationContainerOnClick(LinearLayout linearLayout, final String entityID, final String entityType) {
        linearLayout.setOnClickListener(new View.OnClickListener() {
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


    /**
     * Have button click functionality.
     */

    public void onHaveViewClicked(final FeedModel data, final ItemViewHolder itemViewHolder) {

        itemViewHolder.buttonHave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (data.isMerchantable()) {
                    Intent intent = new Intent(mContext, MerchandisingProductsActivity.class);
                    intent.putExtra(EXTRA_ENTITY_ID, data.getEntityID());
                    intent.putExtra(EXTRA_CAPTURE_URL, data.getContentImage());
                    // getting short uuid and capture uuid
                    if (data.getContentType().equals(CONTENT_TYPE_SHORT)) {
                        intent.putExtra(EXTRA_SHORT_UUID, data.getUUID());
                        intent.putExtra(EXTRA_CAPTURE_UUID, data.getCollabWithUUID());
                    } else if (data.getContentType().equals(CONTENT_TYPE_CAPTURE)) {
                        intent.putExtra(EXTRA_SHORT_UUID, data.getCollabWithUUID());
                        intent.putExtra(EXTRA_CAPTURE_UUID, data.getUUID());
                    }
                    mContext.startActivity(intent);
                } else {
                    ViewHelper.getSnackBar(itemViewHolder.itemView, "Due to low resolution this image is not available for purchase.");
                }
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_HAVE_CLICKED, data.getEntityID());
            }
        });


    }

    /**
     * Caption click listner
     */
    void onTitleClicked(final TextView textTitle) {

        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextViewCompat.getMaxLines(textTitle) == 3) {
                    // expand title
                    textTitle.setMaxLines(Integer.MAX_VALUE);
                } else {
                    // collapse title
                    textTitle.setMaxLines(3);
                }
            }
        });

    }

    /**
     * HatsOffCount click functionality to open "HatsOffActivity" screen.
     */

    void hatsOffCountOnClick(ItemViewHolder itemViewHolder, final FeedModel data) {

        itemViewHolder.containerHatsOffCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, HatsOffActivity.class);
                intent.putExtra(EXTRA_ENTITY_ID, data.getEntityID());
                mContext.startActivity(intent);
            }
        });
    }

    private void downvoteOnClick(ImageView imageView, final FeedModel data, final int position, final ItemViewHolder itemViewHolder) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onDownvoteClickedListener.onDownvoteClicked(data, position, itemViewHolder.imageDownvote);
            }
        });
    }

    /**
     * Follow button functionality
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
                    setAnalytics(FIREBASE_EVENT_FOLLOW_FROM_FEED_DESCRIPTION, null);
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }
            }
        });
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
            // this case won't happen since follow button won't be visible and therefore user cannot click on it
            // show follow button
            buttonFollow.setVisibility(View.VISIBLE);

        } else {
            buttonFollow.setVisibility(View.GONE);
            ViewHelper.getToast(mContext, "You are now following " + creatorName);

        }
    }

    /**
     * Method to check follow status..
     *
     * @param data data
     *             * @param itemViewHolder item view holder
     */
    private void checkFollowStatus(FeedModel data, ItemViewHolder itemViewHolder) {
        if (data.getFollowStatus() || mHelper.getUUID().equals(data.getUUID())) {
            itemViewHolder.buttonFollow.setVisibility(View.GONE);
        } else {
            // show follow button
            itemViewHolder.buttonFollow.setVisibility(View.VISIBLE);
        }
    }

    /**
     * If user is creator then it shows menu options
     */
    private void showMenuOptions(final FeedModel data, ItemViewHolder itemViewHolder, final int position) {
        if (mHelper.getUUID().equals(data.getUUID()) && position == 0) {
            itemViewHolder.buttonMenu.setVisibility(View.VISIBLE);

            //open bottom sheet on clicking of 3 dots
            itemViewHolder.buttonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMenuActionsBottomSheet(mContext, position, data, onContentDeleteListener, true);
                }
            });
        } else {
            itemViewHolder.buttonMenu.setVisibility(View.GONE);
        }
    }


    /**
     * Method to show tooltip on have button
     */
    private void showTooltip(ItemViewHolder itemViewHolder) {
        if (mHelper.isHaveButtonTooltipFirstTime()) {
            //Show tooltip on have button
            ViewHelper.getToolTip(itemViewHolder.buttonHave
                    , "Like the photo? Print and order it!"
                    , mContext);
        }
        //Update status
        mHelper.updateHaveButtonToolTipStatus(false);
    }


    /**
     * Method to show the downvote introduction dialog.
     */
    private void getDownvoteDialog(final ItemViewHolder itemViewHolder) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_generic, false)
                .positiveText("Show me")
                .onPositive(new SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                        //Update status
                        mHelper.updateDownvoteDialogStatus(false);
                        //show tooltip
                        ViewHelper.getToolTip(itemViewHolder.imageDownvote, "Click to downvote", mContext);


                    }
                })
                .show();
        // update key
        mHelper.updateDownvoteDialogStatus(false);

        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_intro_dialog_downvote));
        //Set title text
        textTitle.setText(mContext.getString(R.string.title_dialog_downvote));
        //Set description text
        textDesc.setText(mContext.getString(R.string.text_dialog_downvote_desc));

    }


    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent, String entityID) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", mHelper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_HAVE_CLICKED)) {
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_HAVE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION)) {
            bundle.putString("entity_id", entityID);
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_SHARED_FROM_FEED_DESCRIPTION, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_WRITE_CLICKED)) {
            bundle.putString("class_name", "feed_description");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_WRITE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_CAPTURE_CLICKED)) {
            bundle.putString("class_name", "feed_description");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_CAPTURE_CLICKED, bundle);
        }
    }

}