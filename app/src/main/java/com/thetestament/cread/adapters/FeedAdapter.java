package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationDetailsActivity;
import com.thetestament.cread.activities.CommentsActivity;
import com.thetestament.cread.activities.HatsOffActivity;
import com.thetestament.cread.activities.MerchandisingProductsActivity;
import com.thetestament.cread.activities.RecommendedArtistsActivity;
import com.thetestament.cread.helpers.DownvoteHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SuggestionHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnDownvoteClickedListener;
import com.thetestament.cread.listeners.listener.OnFeedLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnHatsOffListener;
import com.thetestament.cread.listeners.listener.OnShareDialogItemClickedListener;
import com.thetestament.cread.listeners.listener.OnShareLinkClickedListener;
import com.thetestament.cread.listeners.listener.OnShareListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.SuggestedArtistsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.FeedHelper.initCaption;
import static com.thetestament.cread.helpers.FeedHelper.initSocialActionsCount;
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
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_HAVE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_MAIN_FEED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED_ADAPTER;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Feed RecyclerView.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_RECOMMENDED_ARTIST = 2;
    private List<FeedModel> mFeedList;
    private FragmentActivity mContext;
    private Fragment mFeedFragment;
    private boolean mIsLoading;
    private String mUUID;
    private CompositeDisposable mCompositeDisposable;

    /**
     * Flag to store 'Recommended Artists' position in list. Default value is 5.
     */
    private int recommendedArtistIndex = 5;


    private OnFeedLoadMoreListener onFeedLoadMoreListener;
    private OnHatsOffListener onHatsOffListener;
    private OnShareListener onShareListener;
    private OnShareLinkClickedListener onShareLinkClickedListener;
    private OnDownvoteClickedListener onDownvoteClickedListener;

    /**
     * Required constructor.
     *
     * @param mFeedList List of feed data.
     * @param mContext  Context to be use.
     * @param mUUID     UUID of the user
     */
    public FeedAdapter(List<FeedModel> mFeedList, FragmentActivity mContext, String mUUID, Fragment mFeedFragment, CompositeDisposable mCompositeDisposable) {
        this.mFeedList = mFeedList;
        this.mContext = mContext;
        this.mUUID = mUUID;
        this.mFeedFragment = mFeedFragment;
        this.mCompositeDisposable = mCompositeDisposable;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnFeedLoadMoreListener(OnFeedLoadMoreListener onFeedLoadMoreListener) {
        this.onFeedLoadMoreListener = onFeedLoadMoreListener;
    }

    /**
     * Register a callback to be invoked when hats off is clicked.
     */
    public void setHatsOffListener(OnHatsOffListener onHatsOffListener) {
        this.onHatsOffListener = onHatsOffListener;
    }


    /**
     * Register a callback to be invoked when user clicks on share button.
     */
    public void setOnShareListener(OnShareListener onShareListener) {
        this.onShareListener = onShareListener;
    }

    /**
     * Register a callback to be invoked when user clicks on share link button.
     */
    public void setOnShareLinkClickedListener(OnShareLinkClickedListener onShareLinkClickedListener) {
        this.onShareLinkClickedListener = onShareLinkClickedListener;
    }

    /**
     * Register a callback to be invoked when user clicks on down vote button.
     */
    public void setOnDownvoteClickedListener(OnDownvoteClickedListener onDownvoteClickedListener) {
        this.onDownvoteClickedListener = onDownvoteClickedListener;
    }

    @Override
    public int getItemViewType(int position) {
        // return mFeedList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        if (mFeedList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            if (position == recommendedArtistIndex) {
                return VIEW_TYPE_RECOMMENDED_ARTIST;
            } else {
                return VIEW_TYPE_ITEM;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_feed, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        } else if (viewType == VIEW_TYPE_RECOMMENDED_ARTIST) {
            return new RecommendedArtistViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.layout_recommended_artists, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final FeedModel data = mFeedList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Load creator profile picture
            loadCreatorPic(data.getCreatorImage(), itemViewHolder.imageCreator);
            //Load feed image
            loadFeedImage(data.getContentImage(), itemViewHolder.imageFeed);

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

            //Update down vote and dot separator visibility
            updateDownvoteAndSeperatorVisibility(data, itemViewHolder.dotSeperatorRight, itemViewHolder.imageDownvote);
            //check down vote status
            DownvoteHelper downvoteHelper = new DownvoteHelper();
            downvoteHelper.updateDownvoteUI(itemViewHolder.imageDownvote, data.isDownvoteStatus(), mContext);
            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);

            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder.containerShare, data.getContentImage(), data.getEntityID(), data.getCreatorName(), data);
            //HatsOff onClick functionality
            hatsOffOnClick(itemViewHolder, data, position);
            //Collaboration count click functionality
            collaborationContainerOnClick(itemViewHolder.containerCollabCount, data.getEntityID(), data.getContentType());
            //have button click
            onHaveViewClicked(data, itemViewHolder);
            // caption on click
            onTitleClicked(itemViewHolder.textTitle);
            // on HatsOff count click
            hatsOffCountOnClick(itemViewHolder, data);
            //Comment count click functionality
            commentOnClick(itemViewHolder.containerCommentCount, data.getEntityID());
            // downVote click
            downVoteOnClick(itemViewHolder.imageDownvote, data, position, itemViewHolder);
            //check long form status
            checkLongFormStatus(itemViewHolder.containerLongShortPreview, data);
            //long form on click
            initLongFormPreviewClick(itemViewHolder.containerLongShortPreview, data, mContext, mCompositeDisposable);

            //Initialize HatsOff and comment count
            initSocialActionsCount(mContext,
                    data,
                    itemViewHolder.containerHatsOffCount,
                    itemViewHolder.textHatsoffCount,
                    itemViewHolder.containerCommentCount,
                    itemViewHolder.textCommentsCount,
                    itemViewHolder.dotSeperator);

            // initialize caption
            initCaption(mContext, data, itemViewHolder.textTitle);
            // init post timestamp
            updatePostTimestamp(itemViewHolder.textTimeStamp, data);
        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        } else if (holder.getItemViewType() == VIEW_TYPE_RECOMMENDED_ARTIST) {
            final RecommendedArtistViewHolder viewHolder = (RecommendedArtistViewHolder) holder;

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
            //Show progressView
            viewHolder.progressView.setVisibility(View.VISIBLE);

            SuggestionHelper helper = new SuggestionHelper();
            helper.getSuggestedArtist(mContext, mCompositeDisposable, new listener.OnSuggestedArtistLoadListener() {
                @Override
                public void onSuccess(List<SuggestedArtistsModel> dataList) {
                    //Hide itemView
                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    viewHolder.itemView.setLayoutParams(params);
                    //Hide progressView
                    viewHolder.progressView.setVisibility(View.GONE);
                    //Set Layout manager
                    viewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext
                            , LinearLayoutManager.HORIZONTAL
                            , false));
                    //Set adapter
                    viewHolder.recyclerView.setAdapter(new SuggestedArtistsAdapter(dataList
                            , mContext
                            , mFeedFragment
                            , false));
                    if (dataList.size() == 0) {
                        //Hide itemView
                        params.height = 0;
                        params.width = 0;
                        viewHolder.itemView.setLayoutParams(params);
                        //Hide view
                        viewHolder.itemView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(String errorMsg) {
                    //Hide itemView
                    params.height = 0;
                    params.width = 0;
                    viewHolder.itemView.setLayoutParams(params);
                    //Hide progressView
                    viewHolder.progressView.setVisibility(View.GONE);
                    //Show error toast
                    ViewHelper.getShortToast(mContext, errorMsg);
                }
            });
            //Click functionality
            viewHolder.textShowMoreArtists.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Open RecommendedArtists Screen
                    Intent intent = new Intent(mContext, RecommendedArtistsActivity.class);
                    mFeedFragment.startActivityForResult(intent
                            , REQUEST_CODE_RECOMMENDED_ARTISTS_FROM_FEED_ADAPTER);
                }
            });
        }


        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mFeedList.size() - 1 && !mIsLoading) {
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
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);
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
                mContext.startActivity(intent);
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
                adapter.setShareDialogItemClickedListener(new OnShareDialogItemClickedListener() {
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

    private void downVoteOnClick(ImageView imageView, final FeedModel data, final int position, final ItemViewHolder itemViewHolder) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onDownvoteClickedListener.onDownvoteClicked(data, position, itemViewHolder.imageDownvote);
            }
        });
    }


    /**
     * Caption click listener
     */
    private void onTitleClicked(final TextView textTitle) {

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
     * Have button click functionality.
     */
    private void onHaveViewClicked(final FeedModel data, final ItemViewHolder itemViewHolder) {

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
                setAnalytics(FIREBASE_EVENT_SHARED_FROM_MAIN_FEED, data.getEntityID());
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

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        CircleImageView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageFeed)
        ImageView imageFeed;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.imageHatsOff)
        ImageView imageHatsOff;
        @BindView(R.id.containerHatsOff)
        LinearLayout containerHatsOff;
        @BindView(R.id.containerComment)
        LinearLayout containerComment;
        @BindView(R.id.containerShare)
        LinearLayout containerShare;
        @BindView(R.id.textCollabCount)
        TextView textCollabCount;
        @BindView(R.id.lineSeparatorTop)
        View lineSepartor;
        @BindView(R.id.containerCollabCount)
        LinearLayout containerCollabCount;
        @BindView(R.id.textTitle)
        TextView textTitle;
        @BindView(R.id.buttonHave)
        LinearLayout buttonHave;
        @BindView(R.id.containerHatsoffCount)
        LinearLayout containerHatsOffCount;
        @BindView(R.id.containerCommentsCount)
        LinearLayout containerCommentCount;
        @BindView(R.id.textHatsOffCount)
        TextView textHatsoffCount;
        @BindView(R.id.textCommentsCount)
        TextView textCommentsCount;
        @BindView(R.id.dotSeperator)
        TextView dotSeperator;
        @BindView(R.id.dotSeperatorRight)
        TextView dotSeperatorRight;
        @BindView(R.id.imageDownvote)
        ImageView imageDownvote;
        @BindView(R.id.containerLongShortPreview)
        FrameLayout containerLongShortPreview;
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

    //RecommendedArtistViewHolder class
    static class RecommendedArtistViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textRecommendedArtists)
        AppCompatTextView textRecommendedArtists;
        @BindView(R.id.textShowMoreArtists)
        AppCompatTextView textShowMoreArtists;
        @BindView(R.id.recyclerViewRecommendedArtists)
        RecyclerView recyclerView;
        @BindView(R.id.progressView)
        View progressView;

        public RecommendedArtistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     * @param entityID      Entity id of the content.
     */
    private void setAnalytics(String firebaseEvent, String entityID) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", mUUID);
        if (firebaseEvent.equals(FIREBASE_EVENT_WRITE_CLICKED)) {
            bundle.putString("class_name", "main_feed");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_WRITE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_SHARED_FROM_MAIN_FEED)) {
            bundle.putString("entity_id", entityID);
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_SHARED_FROM_MAIN_FEED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_CAPTURE_CLICKED)) {
            bundle.putString("class_name", "main_feed");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_CAPTURE_CLICKED, bundle);
        }

    }

    /**
     * Method to update RecommendedArtistIndex.
     *
     * @param index Index value.
     */
    public void updateRecommendedArtistIndex(int index) {
        recommendedArtistIndex = index;
    }
}
