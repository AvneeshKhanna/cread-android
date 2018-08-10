package com.thetestament.cread.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.gaurav.gesto.OnGestureListener;
import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.WeatherView;
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
import com.thetestament.cread.helpers.GifHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.LiveFilterHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ShareHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.SuggestionHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnDownvoteClickedListener;
import com.thetestament.cread.listeners.listener.OnFeedLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnHatsOffListener;
import com.thetestament.cread.listeners.listener.OnShareLinkClickedListener;
import com.thetestament.cread.listeners.listener.OnShareListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.SuggestedArtistsModel;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.SoundUtil;
import com.thetestament.cread.utils.TextUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import nl.dionsegijn.konfetti.KonfettiView;

import static com.thetestament.cread.helpers.FeedHelper.initCaption;
import static com.thetestament.cread.helpers.FeedHelper.initSocialActionsCount;
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
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_FACEBOOK;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_INSTAGRAM;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_WHATSAPP;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Feed RecyclerView.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //region :ViewTypes
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_MEME = 1;
    private final int VIEW_TYPE_ITEM_USER_REPOST = 2;
    private final int VIEW_TYPE_ITEM_USER_REPOST_MEME = 3;
    private final int VIEW_TYPE_LOADING = 4;
    private final int VIEW_TYPE_RECOMMENDED_ARTIST = 5;
    //endregion

    //region :Field and constant
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

    //endregion

    //region :Constructor

    /**
     * Required constructor.
     *
     * @param feedList            List of feed data.
     * @param context             Context to be use.
     * @param UUID                UUID of the user
     * @param feedFragment        FeedFragment reference
     * @param compositeDisposable CompositeDisposable reference
     */
    public FeedAdapter(List<FeedModel> feedList, FragmentActivity context, String UUID, Fragment feedFragment, CompositeDisposable compositeDisposable) {
        this.mFeedList = feedList;
        this.mContext = context;
        this.mUUID = UUID;
        this.mFeedFragment = feedFragment;
        this.mCompositeDisposable = compositeDisposable;
    }
    //endregion

    //region :Listener
    private OnFeedLoadMoreListener onFeedLoadMoreListener;
    private OnHatsOffListener onHatsOffListener;
    private OnShareListener onShareListener;
    private listener.OnGifShareListener onGifShareListener;
    private OnShareLinkClickedListener onShareLinkClickedListener;
    private OnDownvoteClickedListener onDownvoteClickedListener;

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
     * Register a callback to be invoked when user clicks on share button for gif sharing.
     */
    public void setOnGifShareListener(listener.OnGifShareListener onGifShareListener) {
        this.onGifShareListener = onGifShareListener;
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
    public void setOnDownVoteClickedListener(OnDownvoteClickedListener onDownvoteClickedListener) {
        this.onDownvoteClickedListener = onDownvoteClickedListener;
    }

    //endregion

    //region :Overridden methods
    @Override
    public int getItemViewType(int position) {
        if (mFeedList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            if (position == recommendedArtistIndex) {
                return VIEW_TYPE_RECOMMENDED_ARTIST;
            } else if (mFeedList.get(position).getPostType().equals("REPOST")) {
                if (mFeedList.get(position).getContentType().equals(Constant.CONTENT_TYPE_MEME)) {
                    return VIEW_TYPE_ITEM_USER_REPOST_MEME;
                } else {
                    return VIEW_TYPE_ITEM_USER_REPOST;
                }
            } else {
                if (mFeedList.get(position).getContentType().equals(Constant.CONTENT_TYPE_MEME)) {
                    return VIEW_TYPE_MEME;
                } else {
                    return VIEW_TYPE_ITEM;
                }

            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_feed, parent, false));
        } else if (viewType == VIEW_TYPE_MEME) {
            return new MemeViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_meme, parent, false));
        } else if (viewType == VIEW_TYPE_ITEM_USER_REPOST) {
            return new RePostViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_repost, parent, false));
        } else if (viewType == VIEW_TYPE_ITEM_USER_REPOST_MEME) {
            return new RePostMemeViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_meme_repost, parent, false));
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
            ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                    , itemViewHolder.imageCreator);

            //Set image width and height
            AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                    , data.getImgHeight()
                    , itemViewHolder.imageContent
                    , true);


            //Load feed image
            ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                    , itemViewHolder.imageContent);
            // Set text and click actions acc. to content type
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
            updateDownvoteAndSeperatorVisibility(data, itemViewHolder.dotSeparatorRight
                    , itemViewHolder.imageDownVote);

            //check down vote status
            DownvoteHelper downvoteHelper = new DownvoteHelper();
            downvoteHelper.updateDownvoteUI(itemViewHolder.imageDownVote, data.isDownvoteStatus(), mContext);
            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);

            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder, data);
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
            downVoteOnClick(itemViewHolder.imageDownVote, data, position, itemViewHolder);
            //check long form status
            checkLongFormStatus(itemViewHolder.containerLongShortPreview, data);
            //long form on click
            initLongFormPreviewClick(itemViewHolder.containerLongShortPreview, data, mContext, mCompositeDisposable);

            //Initialize HatsOff and comment count
            initSocialActionsCount(mContext,
                    data,
                    itemViewHolder.containerHatsOffCount,
                    itemViewHolder.textHatsOffCount,
                    itemViewHolder.containerCommentCount,
                    itemViewHolder.textCommentsCount,
                    itemViewHolder.dotSeparator);

            // initialize caption
            initCaption(mContext, data, itemViewHolder.textTitle);
            // init post timestamp
            updatePostTimestamp(itemViewHolder.textTimeStamp, data);

            //Method called
            setDoubleTap(itemViewHolder, itemViewHolder.hatsOffView, data);
            //Method called
            FeedHelper.updateRepost(itemViewHolder.containerRepost, mContext, mCompositeDisposable, data.getEntityID());
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
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_USER_REPOST) {
            final RePostViewHolder viewHolder = (RePostViewHolder) holder;
            initializeRepostViewHolder(viewHolder, data, holder.getAdapterPosition());
        } else if (holder.getItemViewType() == VIEW_TYPE_MEME) {
            final MemeViewHolder viewHolder = (MemeViewHolder) holder;
            initializeMemeViewHolder(viewHolder, data, holder.getAdapterPosition());
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_USER_REPOST_MEME) {
            final RePostMemeViewHolder viewHolder = (RePostMemeViewHolder) holder;
            initializeRepostMemeViewHolder(viewHolder, data, holder.getAdapterPosition());
        }

        //Method called
        initLoadMoreListener(position);
    }

    @Override
    public int getItemCount() {
        return mFeedList == null ? 0 : mFeedList.size();
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            LiveFilterHelper.initLiveFilters(mFeedList.get(holder.getAdapterPosition()).getLiveFilterName()
                    , itemViewHolder.weatherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_USER_REPOST) {
            final RePostViewHolder itemViewHolder = (RePostViewHolder) holder;
            LiveFilterHelper.initLiveFilters(mFeedList.get(holder.getAdapterPosition()).getLiveFilterName()
                    , itemViewHolder.whetherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);
        }
    }
    //endregion

    //region :Private methods

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to initialize load more listener.
     */
    private void initLoadMoreListener(int position) {
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
     * Method to check hatsOff status and perform operation accordingly.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, RePostViewHolder itemViewHolder) {
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
     * Method to check hatsOff status and perform operation accordingly.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, MemeViewHolder itemViewHolder) {
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
     * Method to check hatsOff status and perform operation accordingly.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, RePostMemeViewHolder itemViewHolder) {
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
     * @param itemViewHolder
     * @param data
     */
    private void shareOnClick(final ItemViewHolder itemViewHolder, final FeedModel data) {
        itemViewHolder.logoWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_WHATSAPP)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_WHATSAPP, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_WHATSAPP);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_whats_app));
                }
            }
        });

        itemViewHolder.logoFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_FACEBOOK)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_FACEBOOK, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());

                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_FACEBOOK);
                    }
                } else {
                    ViewHelper.getToast(mContext,
                            mContext.getString(R.string.error_no_facebook));
                }
            }
        });

        itemViewHolder.logoInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_INSTAGRAM)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_INSTAGRAM, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_INSTAGRAM);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_instagram));
                }

            }
        });

        itemViewHolder.logoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_OTHER, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());
                } else {
                    loadBitmapForSharing(data, SHARE_OPTION_OTHER);
                }
            }
        });


    }

    /**
     * Share onClick functionality.
     *
     * @param itemViewHolder
     * @param data
     */
    private void shareOnClick(final RePostMemeViewHolder itemViewHolder, final FeedModel data) {
        itemViewHolder.logoWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_WHATSAPP)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_WHATSAPP, itemViewHolder.waterMarkCread, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_WHATSAPP);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_whats_app));
                }
            }
        });

        itemViewHolder.logoFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_FACEBOOK)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_FACEBOOK, itemViewHolder.waterMarkCread, data.getLiveFilterName());

                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_FACEBOOK);
                    }
                } else {
                    ViewHelper.getToast(mContext,
                            mContext.getString(R.string.error_no_facebook));
                }
            }
        });

        itemViewHolder.logoInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_INSTAGRAM)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_INSTAGRAM, itemViewHolder.waterMarkCread, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_INSTAGRAM);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_instagram));
                }

            }
        });

        itemViewHolder.logoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_OTHER, itemViewHolder.waterMarkCread, data.getLiveFilterName());
                } else {
                    loadBitmapForSharing(data, SHARE_OPTION_OTHER);
                }
            }
        });


    }

    /**
     * Share onClick functionality.
     *
     * @param itemViewHolder
     * @param data
     */
    private void shareOnClick(final RePostViewHolder itemViewHolder, final FeedModel data) {
        itemViewHolder.logoWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_WHATSAPP)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_WHATSAPP, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_WHATSAPP);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_whats_app));
                }
            }
        });

        itemViewHolder.logoFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_FACEBOOK)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_FACEBOOK, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());

                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_FACEBOOK);
                    }
                } else {
                    ViewHelper.getToast(mContext,
                            mContext.getString(R.string.error_no_facebook));
                }
            }
        });

        itemViewHolder.logoInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_INSTAGRAM)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_INSTAGRAM, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_INSTAGRAM);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_instagram));
                }

            }
        });

        itemViewHolder.logoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_OTHER, itemViewHolder.waterMarkCreadView, data.getLiveFilterName());
                } else {
                    loadBitmapForSharing(data, SHARE_OPTION_OTHER);
                }
            }
        });


    }

    /**
     * Share onClick functionality.
     *
     * @param itemViewHolder
     * @param data
     */
    private void shareOnClick(final MemeViewHolder itemViewHolder, final FeedModel data) {
        itemViewHolder.logoWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_WHATSAPP)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_WHATSAPP, itemViewHolder.waterMarkCread, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_WHATSAPP);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_whats_app));
                }
            }
        });

        itemViewHolder.logoFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_FACEBOOK)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_FACEBOOK, itemViewHolder.waterMarkCread, data.getLiveFilterName());

                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_FACEBOOK);
                    }
                } else {
                    ViewHelper.getToast(mContext,
                            mContext.getString(R.string.error_no_facebook));
                }
            }
        });

        itemViewHolder.logoInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareHelper.isAppInstalled(mContext, Constant.PACKAGE_NAME_INSTAGRAM)) {
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_INSTAGRAM, itemViewHolder.waterMarkCread, data.getLiveFilterName());
                    } else {
                        loadBitmapForSharing(data, SHARE_OPTION_INSTAGRAM);
                    }
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_no_instagram));
                }

            }
        });

        itemViewHolder.logoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    onGifShareListener.onGifShareClick(itemViewHolder.frameLayout, SHARE_OPTION_OTHER, itemViewHolder.waterMarkCread, data.getLiveFilterName());
                } else {
                    loadBitmapForSharing(data, SHARE_OPTION_OTHER);
                }
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
                SharedPreferenceHelper sh = new SharedPreferenceHelper(mContext);
                if (sh.isHatsOffFirstTime()) {
                    //Show tooltip
                    ViewHelper.getToolTip(itemViewHolder.imageHatsOff
                            , mContext.getString(R.string.tooltip_hats_off_double_tap)
                            , mContext);
                    //Update sp value here
                    sh.updateHatsOffStatusStatus(false);
                }


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
                            itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
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
                        itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
                    }

                    updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

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
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final RePostViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.imageHatsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceHelper sh = new SharedPreferenceHelper(mContext);
                if (sh.isHatsOffFirstTime()) {
                    //Show tooltip
                    ViewHelper.getToolTip(itemViewHolder.imageHatsOff
                            , mContext.getString(R.string.tooltip_hats_off_double_tap)
                            , mContext);
                    //Update sp value here
                    sh.updateHatsOffStatusStatus(false);
                }


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
                            //itemViewHolder.containerHatsOffCount.setVisibility(View.GONE);
                        }
                        //hats off count is more than zero
                        else {
                            //Change hatsOffCount i.e decrease by one
                            //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                            //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
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
                        //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                        //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
                    }

                    //updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

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
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final MemeViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.imageHatsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceHelper sh = new SharedPreferenceHelper(mContext);
                if (sh.isHatsOffFirstTime()) {
                    //Show tooltip
                    ViewHelper.getToolTip(itemViewHolder.imageHatsOff
                            , mContext.getString(R.string.tooltip_hats_off_double_tap)
                            , mContext);
                    //Update sp value here
                    sh.updateHatsOffStatusStatus(false);
                }


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
                            //itemViewHolder.containerHatsOffCount.setVisibility(View.GONE);
                        }
                        //hats off count is more than zero
                        else {
                            //Change hatsOffCount i.e decrease by one
                            //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                            //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
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
                        //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                        //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
                    }

                    //updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

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
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final RePostMemeViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.imageHatsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceHelper sh = new SharedPreferenceHelper(mContext);
                if (sh.isHatsOffFirstTime()) {
                    //Show tooltip
                    ViewHelper.getToolTip(itemViewHolder.imageHatsOff
                            , mContext.getString(R.string.tooltip_hats_off_double_tap)
                            , mContext);
                    //Update sp value here
                    sh.updateHatsOffStatusStatus(false);
                }


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
                            //itemViewHolder.containerHatsOffCount.setVisibility(View.GONE);
                        }
                        //hats off count is more than zero
                        else {
                            //Change hatsOffCount i.e decrease by one
                            //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                            //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
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
                        //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                        //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));
                    }

                    //updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

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
     * Collaboration count click functionality to launch collaborationDetailsActivity.
     *
     * @param view       view to be clicked.
     * @param entityID   Entity id of the content.
     * @param entityType Type of content i.e CAPTURE or SHORT
     */
    private void collaborationContainerOnClick(View view, final String entityID, final String entityType) {
        view.setOnClickListener(new View.OnClickListener() {
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

                onDownvoteClickedListener.onDownvoteClicked(data, position, itemViewHolder.imageDownVote);
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
     * HatsOffCount click functionality to open "HatsOffActivity" screen.
     */
    void hatsOffCountOnClick(MemeViewHolder itemViewHolder, final FeedModel data) {

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
    private void loadBitmapForSharing(final FeedModel data, final String shareOption) {
        Picasso.with(mContext).load(data.getContentImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //Set Listener
                onShareListener.onShareClick(bitmap, data, shareOption);
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


    /**
     * Initializes the views and click actions for RePostViewHolder.
     *
     * @param itemViewHolder ViewHolder.
     * @param data           Feed data.
     * @param position       item position in data list.
     */
    private void initializeRepostViewHolder(RePostViewHolder itemViewHolder, final FeedModel data, final int position) {
        //Set reposter name , repost time and click functionality to open re-poster profile
        itemViewHolder.textRepostedBy.setText(data.getReposterName() + " reposted this");
        FeedHelper.setRepostTime(itemViewHolder.textRepostedtime, data);
        openProfileActivity(itemViewHolder.textRepostedBy, data.getReposterUUID());

        //Load creator profile picture
        ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                , itemViewHolder.imageCreator);
        //Set content image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , itemViewHolder.imageContent
                , true);
        //Load content image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                , itemViewHolder.imageContent);


        //Set text and click actions acc. to content type
        FeedHelper.performContentTypeSpecificOperations(mContext
                , data
                , itemViewHolder.collabCount
                , itemViewHolder.collabCount
                , itemViewHolder.buttonCollaborate
                , itemViewHolder.textCreatorName
                , true
                , true
                , itemViewHolder.collabCountDivider);


        itemViewHolder.buttonMenu.setVisibility(View.GONE);

        //Method called
        setDoubleTap(itemViewHolder, itemViewHolder.hatsOffView, data);
        //Check whether user has given hats off to this campaign or not
        checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);
        //HatsOff onClick functionality
        hatsOffOnClick(itemViewHolder, data, position);
        //Comment click functionality
        commentOnClick(itemViewHolder.containerComment, data.getEntityID());
        //Share click functionality
        shareOnClick(itemViewHolder, data);
        //Collaboration count click functionality
        collaborationContainerOnClick(itemViewHolder.collabCount, data.getEntityID(), data.getContentType());
        //check long form status
        checkLongFormStatus(itemViewHolder.buttonLongWritingPreview, data);
        //long form on click
        initLongFormPreviewClick(itemViewHolder.buttonLongWritingPreview, data, mContext, mCompositeDisposable);
        // init post timestamp
        updatePostTimestamp(itemViewHolder.textTimeStamp, data);
        //Method called
        FeedHelper.updateRepost(itemViewHolder.containerRepost, mContext, mCompositeDisposable, data.getEntityID());
    }


    /**
     * Initializes the views and click actions
     *
     * @param itemViewHolder view holder
     * @param data           Feed data
     * @param position       position of the item
     */
    private void initializeMemeViewHolder(MemeViewHolder itemViewHolder, final FeedModel data, final int position) {
        //Load creator profile picture
        ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                , itemViewHolder.imgCreator);
        //Set user creator name and its click functionality
        itemViewHolder.textCreatorName.setText(TextUtils.getSpannedString(data.getCreatorName() + " added meme"
                , new ForegroundColorSpan(Color.BLACK)
                , 0
                , data.getCreatorName().length()
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        openProfileActivity(itemViewHolder.textCreatorName, data.getUUID());
        //init post timestamp
        updatePostTimestamp(itemViewHolder.textTimeStamp, data);

        //Set image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , itemViewHolder.contentImage
                , true);
        //Load content image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                , itemViewHolder.contentImage);


        //Check whether user has given hats off to this campaign or not
        checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);

        //Comment click functionality
        commentOnClick(itemViewHolder.containerComment, data.getEntityID());

        //HatsOff onClick functionality
        hatsOffOnClick(itemViewHolder, data, position);

        //Share click functionality
        shareOnClick(itemViewHolder, data);

        // caption on click
        onTitleClicked(itemViewHolder.textCaption);

        // on HatsOff count click
        hatsOffCountOnClick(itemViewHolder, data);

        //Comment click functionality
        commentOnClick(itemViewHolder.containerComment, data.getEntityID());

        //Initialize HatsOff and comment count
        initSocialActionsCount(mContext,
                data,
                itemViewHolder.containerHatsOffCount,
                itemViewHolder.textHatsOffCount,
                itemViewHolder.containerCommentsCount,
                itemViewHolder.textCommentsCount,
                itemViewHolder.dotSeparator);

        // initialize caption
        initCaption(mContext, data, itemViewHolder.textCaption);
        //Method called
        setDoubleTap(itemViewHolder, itemViewHolder.hatsOffView, data);
        //Method called
        FeedHelper.updateRepost(itemViewHolder.containerRepost, mContext, mCompositeDisposable, data.getEntityID());

    }


    /**
     * Initializes the views and click actions for RePostViewHolder.
     *
     * @param itemViewHolder ViewHolder.
     * @param data           Feed data.
     * @param position       item position in data list.
     */
    private void initializeRepostMemeViewHolder(RePostMemeViewHolder itemViewHolder, final FeedModel data, final int position) {
        //Load creator profile picture
        ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                , itemViewHolder.imageCreator);
        //Set user creator name and its click functionality
        itemViewHolder.textCreatorName.setText(TextUtils.getSpannedString(data.getCreatorName() + " added meme"
                , new ForegroundColorSpan(Color.BLACK)
                , 0
                , data.getCreatorName().length()
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        openProfileActivity(itemViewHolder.textCreatorName, data.getUUID());
        //init post timestamp
        updatePostTimestamp(itemViewHolder.textTimestamp, data);


        //Set reposter name , repost time and click functionality to open re-poster profile
        itemViewHolder.textRepostedBy.setText(data.getReposterName() + " reposted this");
        FeedHelper.setRepostTime(itemViewHolder.textRepostedTime, data);
        openProfileActivity(itemViewHolder.textRepostedBy, data.getReposterUUID());

        //Set content image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , itemViewHolder.contentImage
                , true);
        //Load content image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                , itemViewHolder.contentImage);


        itemViewHolder.buttonMenu.setVisibility(View.GONE);

        //Method called
        setDoubleTap(itemViewHolder, itemViewHolder.doubleTapHatsOffView, data);
        //Check whether user has given hats off to this campaign or not
        checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);
        //HatsOff onClick functionality
        hatsOffOnClick(itemViewHolder, data, position);
        //Comment click functionality
        commentOnClick(itemViewHolder.containerComment, data.getEntityID());
        //Share click functionality
        shareOnClick(itemViewHolder, data);

        //Method called
        FeedHelper.updateRepost(itemViewHolder.containerRepost, mContext, mCompositeDisposable, data.getEntityID());
    }


    /**
     * Method to set double tap listener.
     *
     * @param itemViewHolder View to double tapped.
     * @param hatsOffView    ImageView to be updated.
     * @param data           FeedModel data.
     */
    private void setDoubleTap(final ItemViewHolder itemViewHolder, final AppCompatImageView hatsOffView, final FeedModel data) {
        itemViewHolder.itemView.setOnTouchListener(new OnGestureListener(mContext) {
            @Override
            public void onDoubleClick() {
                //region :Code to update hatsOff status
                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //User has already given the hats off
                    if (itemViewHolder.mIsHatsOff) {
                        SoundUtil.playHatsOffSound(mContext);
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
                        itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));


                        updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

                        //Toggle hatsOff status
                        itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                        //Update hats off here
                        data.setHatsOffStatus(itemViewHolder.mIsHatsOff);
                        //Listener
                        onHatsOffListener.onHatsOffClick(data, itemViewHolder.getAdapterPosition());
                    }

                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }
                //endregion

                //region :Animation code starts here
                hatsOffView.setVisibility(View.VISIBLE);
                hatsOffView.setScaleY(0.1f);
                hatsOffView.setScaleX(0.1f);
                AnimatorSet animatorSet = new AnimatorSet();

                ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 0.1f, 1f);
                imgScaleUpYAnim.setDuration(300);
                imgScaleUpYAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 0.1f, 1f);
                imgScaleUpXAnim.setDuration(300);
                imgScaleUpXAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);

                ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 1f, 0f);
                imgScaleDownYAnim.setDuration(300);
                imgScaleDownYAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 1f, 0f);
                imgScaleDownXAnim.setDuration(300);
                imgScaleDownXAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);

                animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim);
                animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hatsOffView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                //endregion animation code end here
            }

            @Override
            public void onClick() {
                //do nothing
            }
        });
    }

    /**
     * Method to set double tap listener.
     *
     * @param itemViewHolder View to double tapped.
     * @param hatsOffView    ImageView to be updated.
     * @param data           FeedModel data.
     */
    private void setDoubleTap(final RePostViewHolder itemViewHolder, final AppCompatImageView hatsOffView, final FeedModel data) {
        itemViewHolder.itemView.setOnTouchListener(new OnGestureListener(mContext) {
            @Override
            public void onDoubleClick() {
                //region :Code to update hatsOff status
                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //User has already given the hats off
                    if (itemViewHolder.mIsHatsOff) {
                        SoundUtil.playHatsOffSound(mContext);
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
                        //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                        //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));


                        //updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

                        //Toggle hatsOff status
                        itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                        //Update hats off here
                        data.setHatsOffStatus(itemViewHolder.mIsHatsOff);
                        //Listener
                        onHatsOffListener.onHatsOffClick(data, itemViewHolder.getAdapterPosition());
                    }

                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }
                //endregion

                //region :Animation code starts here
                hatsOffView.setVisibility(View.VISIBLE);
                hatsOffView.setScaleY(0.1f);
                hatsOffView.setScaleX(0.1f);
                AnimatorSet animatorSet = new AnimatorSet();

                ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 0.1f, 1f);
                imgScaleUpYAnim.setDuration(300);
                imgScaleUpYAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 0.1f, 1f);
                imgScaleUpXAnim.setDuration(300);
                imgScaleUpXAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);

                ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 1f, 0f);
                imgScaleDownYAnim.setDuration(300);
                imgScaleDownYAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 1f, 0f);
                imgScaleDownXAnim.setDuration(300);
                imgScaleDownXAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);

                animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim);
                animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hatsOffView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                //endregion animation code end here
            }

            @Override
            public void onClick() {
                //do nothing
            }
        });
    }

    /**
     * Method to set double tap listener.
     *
     * @param itemViewHolder View to double tapped.
     * @param hatsOffView    ImageView to be updated.
     * @param data           FeedModel data.
     */
    private void setDoubleTap(final MemeViewHolder itemViewHolder, final AppCompatImageView hatsOffView, final FeedModel data) {
        itemViewHolder.itemView.setOnTouchListener(new OnGestureListener(mContext) {
            @Override
            public void onDoubleClick() {
                //region :Code to update hatsOff status
                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //User has already given the hats off
                    if (itemViewHolder.mIsHatsOff) {
                        SoundUtil.playHatsOffSound(mContext);
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
                        //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                        //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));


                        //updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

                        //Toggle hatsOff status
                        itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                        //Update hats off here
                        data.setHatsOffStatus(itemViewHolder.mIsHatsOff);
                        //Listener
                        onHatsOffListener.onHatsOffClick(data, itemViewHolder.getAdapterPosition());
                    }

                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }
                //endregion

                //region :Animation code starts here
                hatsOffView.setVisibility(View.VISIBLE);
                hatsOffView.setScaleY(0.1f);
                hatsOffView.setScaleX(0.1f);
                AnimatorSet animatorSet = new AnimatorSet();

                ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 0.1f, 1f);
                imgScaleUpYAnim.setDuration(300);
                imgScaleUpYAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 0.1f, 1f);
                imgScaleUpXAnim.setDuration(300);
                imgScaleUpXAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);

                ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 1f, 0f);
                imgScaleDownYAnim.setDuration(300);
                imgScaleDownYAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 1f, 0f);
                imgScaleDownXAnim.setDuration(300);
                imgScaleDownXAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);

                animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim);
                animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hatsOffView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                //endregion animation code end here
            }

            @Override
            public void onClick() {
                //do nothing
            }
        });
    }

    /**
     * Method to set double tap listener.
     *
     * @param itemViewHolder View to double tapped.
     * @param hatsOffView    ImageView to be updated.
     * @param data           FeedModel data.
     */
    private void setDoubleTap(final RePostMemeViewHolder itemViewHolder, final AppCompatImageView hatsOffView, final FeedModel data) {
        itemViewHolder.itemView.setOnTouchListener(new OnGestureListener(mContext) {
            @Override
            public void onDoubleClick() {
                //region :Code to update hatsOff status
                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //User has already given the hats off
                    if (itemViewHolder.mIsHatsOff) {
                        SoundUtil.playHatsOffSound(mContext);
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
                        //itemViewHolder.containerHatsOffCount.setVisibility(View.VISIBLE);
                        //itemViewHolder.textHatsOffCount.setText(String.valueOf(data.getHatsOffCount()));


                        //updateDotSeperatorVisibility(data, itemViewHolder.dotSeparator);

                        //Toggle hatsOff status
                        itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                        //Update hats off here
                        data.setHatsOffStatus(itemViewHolder.mIsHatsOff);
                        //Listener
                        onHatsOffListener.onHatsOffClick(data, itemViewHolder.getAdapterPosition());
                    }

                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }
                //endregion

                //region :Animation code starts here
                hatsOffView.setVisibility(View.VISIBLE);
                hatsOffView.setScaleY(0.1f);
                hatsOffView.setScaleX(0.1f);
                AnimatorSet animatorSet = new AnimatorSet();

                ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 0.1f, 1f);
                imgScaleUpYAnim.setDuration(300);
                imgScaleUpYAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 0.1f, 1f);
                imgScaleUpXAnim.setDuration(300);
                imgScaleUpXAnim.setInterpolator(Constant.DECCELERATE_INTERPOLATOR);

                ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 1f, 0f);
                imgScaleDownYAnim.setDuration(300);
                imgScaleDownYAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 1f, 0f);
                imgScaleDownXAnim.setDuration(300);
                imgScaleDownXAnim.setInterpolator(Constant.ACCELERATE_INTERPOLATOR);

                animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim);
                animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hatsOffView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                //endregion animation code end here
            }

            @Override
            public void onClick() {
                //do nothing
            }
        });
    }

    /**
     * Method to open ProfileActivity screen.
     *
     * @param view View to be clicked.
     * @param uuid UUID of user whose profile to  be loaded.
     */
    private void openProfileActivity(View view, final String uuid) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentHelper.openProfileActivity(mContext, uuid);
            }
        });
    }

    //endregion

    //region :ViewHolders
    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.textCollabCount)
        TextView textCollabCount;
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
        TextView textHatsOffCount;
        @BindView(R.id.textCommentsCount)
        TextView textCommentsCount;
        @BindView(R.id.dotSeperator)
        TextView dotSeparator;
        @BindView(R.id.dotSeperatorRight)
        TextView dotSeparatorRight;
        @BindView(R.id.imageDownvote)
        ImageView imageDownVote;
        @BindView(R.id.containerLongShortPreview)
        FrameLayout containerLongShortPreview;
        @BindView(R.id.textTimestamp)
        TextView textTimeStamp;

        //Main content views
        @BindView(R.id.container_main_content)
        FrameLayout frameLayout;
        @BindView(R.id.content_image)
        SimpleDraweeView imageContent;
        @BindView(R.id.live_filter_bubble)
        GravView liveFilterBubble;
        @BindView(R.id.whether_view)
        WeatherView weatherView;
        @BindView(R.id.konfetti_view)
        KonfettiView konfettiView;
        @BindView(R.id.water_mark_cread)
        RelativeLayout waterMarkCreadView;
        @BindView(R.id.double_tap_hats_off_view)
        AppCompatImageView hatsOffView;

        //Social actions views
        @BindView(R.id.container_hats_off)
        LinearLayout containerHatsOff;
        @BindView(R.id.image_hats_off)
        AppCompatImageView imageHatsOff;
        @BindView(R.id.container_comment)
        LinearLayout containerComment;
        @BindView(R.id.container_repost)
        LinearLayout containerRepost;

        //Share views
        @BindView(R.id.logoWhatsapp)
        AppCompatImageView logoWhatsapp;
        @BindView(R.id.logoFacebook)
        AppCompatImageView logoFacebook;
        @BindView(R.id.logoInstagram)
        AppCompatImageView logoInstagram;
        @BindView(R.id.logoMore)
        AppCompatImageView logoMore;


        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;
        //Variable to maintain  hats off view rotation status
        private boolean mIsRotated = false;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //MemeViewHolder class
    static class MemeViewHolder extends RecyclerView.ViewHolder {
        //Creator views
        @BindView(R.id.img_creator)
        SimpleDraweeView imgCreator;
        @BindView(R.id.text_creator_name)
        AppCompatTextView textCreatorName;
        @BindView(R.id.text_time_stamp)
        AppCompatTextView textTimeStamp;

        //Main content views
        @BindView(R.id.container_main_content)
        FrameLayout frameLayout;
        @BindView(R.id.content_image)
        SimpleDraweeView contentImage;
        @BindView(R.id.water_mark_cread)
        RelativeLayout waterMarkCread;
        @BindView(R.id.double_tap_hats_off_view)
        AppCompatImageView hatsOffView;

        //Social action count views
        @BindView(R.id.icon_hats_off)
        AppCompatImageView iconHatsOff;
        @BindView(R.id.text_hats_off_count)
        AppCompatTextView textHatsOffCount;
        @BindView(R.id.container_hats_off_count)
        LinearLayout containerHatsOffCount;
        @BindView(R.id.dot_separator)
        AppCompatTextView dotSeparator;
        @BindView(R.id.icon_comment)
        AppCompatImageView iconComment;
        @BindView(R.id.text_comments_count)
        AppCompatTextView textCommentsCount;
        @BindView(R.id.container_comments_count)
        LinearLayout containerCommentsCount;

        //Caption text
        @BindView(R.id.text_caption)
        AppCompatTextView textCaption;

        //Social actions views
        @BindView(R.id.image_hats_off)
        AppCompatImageView imageHatsOff;
        @BindView(R.id.container_hats_off)
        LinearLayout containerHatsOff;
        @BindView(R.id.container_comment)
        LinearLayout containerComment;
        @BindView(R.id.container_repost)
        LinearLayout containerRepost;

        //Share views
        @BindView(R.id.logoWhatsapp)
        AppCompatImageView logoWhatsapp;
        @BindView(R.id.logoFacebook)
        AppCompatImageView logoFacebook;
        @BindView(R.id.logoInstagram)
        AppCompatImageView logoInstagram;
        @BindView(R.id.logoMore)
        AppCompatImageView logoMore;

        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;
        //Variable to maintain  hats off view rotation status
        private boolean mIsRotated = false;

        public MemeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //RePost ViewHolder
    static class RePostViewHolder extends RecyclerView.ViewHolder {
        //Repost views
        @BindView(R.id.text_reposted_by)
        AppCompatTextView textRepostedBy;
        @BindView(R.id.text_reposted_time)
        AppCompatTextView textRepostedtime;

        //Creator views
        @BindView(R.id.image_creator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.text_creator_name)
        AppCompatTextView textCreatorName;
        @BindView(R.id.text_timestamp)
        AppCompatTextView textTimeStamp;
        @BindView(R.id.button_collaborate)
        AppCompatTextView buttonCollaborate;
        @BindView(R.id.button_menu)
        AppCompatImageView buttonMenu;

        //Main content views
        @BindView(R.id.container_main_content)
        FrameLayout frameLayout;
        @BindView(R.id.content_image)
        SimpleDraweeView imageContent;
        @BindView(R.id.live_filter_bubble)
        GravView liveFilterBubble;
        @BindView(R.id.whether_view)
        WeatherView whetherView;
        @BindView(R.id.konfetti_view)
        KonfettiView konfettiView;
        @BindView(R.id.water_mark_cread)
        RelativeLayout waterMarkCreadView;
        @BindView(R.id.double_tap_hats_off_view)
        AppCompatImageView hatsOffView;

        //Long writing preview , collab count and collab count divider
        @BindView(R.id.container_long_writing_preview)
        FrameLayout buttonLongWritingPreview;
        @BindView(R.id.collab_count)
        AppCompatTextView collabCount;
        @BindView(R.id.collab_count_divider)
        View collabCountDivider;

        //Social actions views
        @BindView(R.id.container_hats_off)
        LinearLayout containerHatsOff;
        @BindView(R.id.image_hats_off)
        AppCompatImageView imageHatsOff;
        @BindView(R.id.container_comment)
        LinearLayout containerComment;
        @BindView(R.id.container_repost)
        LinearLayout containerRepost;

        //Share views
        @BindView(R.id.logoWhatsapp)
        AppCompatImageView logoWhatsapp;
        @BindView(R.id.logoFacebook)
        AppCompatImageView logoFacebook;
        @BindView(R.id.logoInstagram)
        AppCompatImageView logoInstagram;
        @BindView(R.id.logoMore)
        AppCompatImageView logoMore;


        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;
        //Variable to maintain  hats off view rotation status
        private boolean mIsRotated = false;

        public RePostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //RepostMemeViewHolder class
    static class RePostMemeViewHolder extends RecyclerView.ViewHolder {
        //Reposted views
        @BindView(R.id.text_reposted_by)
        AppCompatTextView textRepostedBy;
        @BindView(R.id.text_reposted_time)
        AppCompatTextView textRepostedTime;
        @BindView(R.id.button_menu)
        AppCompatImageView buttonMenu;

        //Creator views
        @BindView(R.id.image_creator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.text_creator_name)
        AppCompatTextView textCreatorName;
        @BindView(R.id.text_timestamp)
        AppCompatTextView textTimestamp;

        //Main content view
        @BindView(R.id.container_main_content)
        FrameLayout frameLayout;
        @BindView(R.id.content_image)
        SimpleDraweeView contentImage;
        @BindView(R.id.water_mark_cread)
        RelativeLayout waterMarkCread;
        @BindView(R.id.double_tap_hats_off_view)
        AppCompatImageView doubleTapHatsOffView;

        //Social action views
        @BindView(R.id.image_hats_off)
        AppCompatImageView imageHatsOff;
        @BindView(R.id.container_hats_off)
        LinearLayout containerHatsOff;
        @BindView(R.id.container_comment)
        LinearLayout containerComment;
        @BindView(R.id.container_repost)
        LinearLayout containerRepost;

        //Share views
        @BindView(R.id.logoWhatsapp)
        AppCompatImageView logoWhatsapp;
        @BindView(R.id.logoFacebook)
        AppCompatImageView logoFacebook;
        @BindView(R.id.logoInstagram)
        AppCompatImageView logoInstagram;
        @BindView(R.id.logoMore)
        AppCompatImageView logoMore;

        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;
        //Variable to maintain  hats off view rotation status
        private boolean mIsRotated = false;

        public RePostMemeViewHolder(View itemView) {
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
    //endregion

}
