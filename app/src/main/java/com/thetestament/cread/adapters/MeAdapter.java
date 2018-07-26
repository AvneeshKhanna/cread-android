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
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
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
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.helpers.ContentHelper;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.GifHelper;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.LiveFilterHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ShareHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnContentDeleteListener;
import com.thetestament.cread.listeners.listener.OnContentEditListener;
import com.thetestament.cread.listeners.listener.OnShareLinkClickedListener;
import com.thetestament.cread.listeners.listener.OnUserActivityHatsOffListener;
import com.thetestament.cread.listeners.listener.OnUserActivityLoadMoreListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.SoundUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import nl.dionsegijn.konfetti.KonfettiView;

import static com.thetestament.cread.helpers.FeedHelper.setGridItemMargins;
import static com.thetestament.cread.helpers.FeedHelper.updatePostTimestamp;
import static com.thetestament.cread.helpers.LongShortHelper.checkLongFormStatus;
import static com.thetestament.cread.helpers.LongShortHelper.initLongFormPreviewClick;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_CAPTURE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_PROFILE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_FACEBOOK;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_INSTAGRAM;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_WHATSAPP;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Me RecyclerView.
 */
public class MeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //region :Item types
    private final int VIEW_TYPE_ITEM_USER_POST = 0;
    private final int VIEW_TYPE_ITEM_USER_REPOST = 1;
    private final int VIEW_TYPE_ITEM_COLLAB_POST = 2;
    private final int VIEW_TYPE_ITEM_GRID = 3;
    private final int VIEW_TYPE_LOADING = 4;
    //endregion

    //region :Private field and constants
    private List<FeedModel> mUserContentList;
    private FragmentActivity mContext;
    private Fragment mMeFragment;
    private boolean mIsLoading;
    private String mUUID;
    private String mItemType;
    private CompositeDisposable mCompositeDisposable;
    //endregion

    //region :Constructor

    /**
     * Required constructor.
     *
     * @param userContentList     List of feed data.
     * @param context             Context to be use.
     * @param UUID                UUID of user.
     * @param meFragment          Fragment reference.
     * @param itemType            Item type i.e {@link com.thetestament.cread.utils.Constant#ME_ITEM_TYPE_USER_POST_LIST}
     *                            {@link com.thetestament.cread.utils.Constant#ME_ITEM_TYPE_USER_POST_LIST}
     *                            {@link com.thetestament.cread.utils.Constant#ME_ITEM_TYPE_RE_POST_LIST}
     *                            {@link com.thetestament.cread.utils.Constant#ME_ITEM_TYPE_RE_POST_GRID}
     *                            {@link com.thetestament.cread.utils.Constant#ME_ITEM_TYPE_COLLAB_POST_LIST}
     *                            {@link com.thetestament.cread.utils.Constant#ME_ITEM_TYPE_COLLAB_POST_GRID}
     * @param compositeDisposable Composite disposable reference.
     */
    public MeAdapter(List<FeedModel> userContentList, FragmentActivity context, String UUID, Fragment meFragment, String itemType, CompositeDisposable compositeDisposable) {
        this.mUserContentList = userContentList;
        this.mContext = context;
        this.mUUID = UUID;
        this.mMeFragment = meFragment;
        this.mItemType = itemType;
        this.mCompositeDisposable = compositeDisposable;
    }
    //endregion

    //region :Listeners
    private OnUserActivityLoadMoreListener onLoadMore;
    private OnUserActivityHatsOffListener onHatsOffListener;
    private OnContentDeleteListener onContentDeleteListener;
    private OnContentEditListener onContentEditListener;
    private listener.OnShareListener onShareListener;
    private listener.OnGifShareListener onGifShareListener;
    private OnShareLinkClickedListener onShareLinkClickedListener;
    private listener.OnRepostDeleteListener onRepostDeleteListener;


    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setUserActivityLoadMoreListener(OnUserActivityLoadMoreListener onLoadMore) {
        this.onLoadMore = onLoadMore;
    }

    /**
     * Register a callback to be invoked when hats off is clicked.
     */
    public void setHatsOffListener(OnUserActivityHatsOffListener onHatsOffListener) {
        this.onHatsOffListener = onHatsOffListener;
    }

    /**
     * Register a callback to be invoked when user clicks on delete button.
     */
    public void setOnContentDeleteListener(OnContentDeleteListener onContentDeleteListener) {
        this.onContentDeleteListener = onContentDeleteListener;
    }

    /**
     * Register a callback to be invoked when user clicks on share button.
     */
    public void setOnShareListener(listener.OnShareListener onShareListener) {
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
     * Register a callback to be invoked when user clicks on delete the reposted post..
     */
    public void setOnRepostDeleteListener(listener.OnRepostDeleteListener onRepostDeleteListener) {
        this.onRepostDeleteListener = onRepostDeleteListener;
    }
    //endregion

    //region :Overridden methods
    @Override
    public int getItemViewType(int position) {
        if (mUserContentList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (mItemType.equals(Constant.ME_ITEM_TYPE_USER_POST_LIST)) {
            return VIEW_TYPE_ITEM_USER_POST;
        } else if (mItemType.equals(Constant.ME_ITEM_TYPE_RE_POST_LIST)) {
            return VIEW_TYPE_ITEM_USER_REPOST;
        } else if (mItemType.equals(Constant.ME_ITEM_TYPE_COLLAB_POST_LIST)) {
            return VIEW_TYPE_ITEM_COLLAB_POST;
        } else if (mItemType.equals(Constant.ME_ITEM_TYPE_USER_POST_GRID)
                || mItemType.equals(Constant.ME_ITEM_TYPE_RE_POST_GRID)
                || mItemType.equals(Constant.ME_ITEM_TYPE_COLLAB_POST_GRID)) {
            return VIEW_TYPE_ITEM_GRID;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM_USER_POST || viewType == VIEW_TYPE_ITEM_COLLAB_POST) {
            return new ListItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_me, parent, false));
        } else if (viewType == VIEW_TYPE_ITEM_USER_REPOST) {
            return new RePostViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_repost, parent, false));
        } else if (viewType == VIEW_TYPE_ITEM_GRID) {
            return new GridItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_grid, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final FeedModel data = mUserContentList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM_USER_POST
                || holder.getItemViewType() == VIEW_TYPE_ITEM_COLLAB_POST) {
            ListItemViewHolder itemViewHolder = (ListItemViewHolder) holder;
            // initialize the views and click actions
            initializeListItem(itemViewHolder, data, itemViewHolder.getAdapterPosition());

        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_USER_REPOST) {
            RePostViewHolder itemViewHolder = (RePostViewHolder) holder;
            // initialize the views and click actions
            initializeRepostViewHolder(itemViewHolder, data, itemViewHolder.getAdapterPosition());
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_GRID) {
            GridItemViewHolder itemViewHolder = (GridItemViewHolder) holder;
            initializeGridItem(itemViewHolder, data);
        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }
        //Load more data initialization
        initializeLoadMore(position);
    }

    @Override
    public int getItemCount() {
        return mUserContentList == null ? 0 : mUserContentList.size();
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM_USER_POST || holder.getItemViewType() == VIEW_TYPE_ITEM_COLLAB_POST) {
            ListItemViewHolder itemViewHolder = (ListItemViewHolder) holder;
            LiveFilterHelper.initLiveFilters(mUserContentList.get(holder.getAdapterPosition()).getLiveFilterName()
                    , itemViewHolder.whetherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_USER_REPOST) {

            RePostViewHolder itemViewHolder = (RePostViewHolder) holder;
            LiveFilterHelper.initLiveFilters(mUserContentList.get(holder.getAdapterPosition()).getLiveFilterName()
                    , itemViewHolder.whetherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_GRID) {
            GridItemViewHolder itemViewHolder = (GridItemViewHolder) holder;
            LiveFilterHelper.initLiveFilters(mUserContentList.get(holder.getAdapterPosition()).getLiveFilterName()
                    , itemViewHolder.whetherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);
        }
    }

    //endregion

    //region :Methods

    /**
     * Method is set loading status to false..
     */
    public void setLoaded() {
        mIsLoading = false;
    }


    private void initializeGridItem(GridItemViewHolder itemViewHolder, FeedModel data) {
        // Set margins
        setGridItemMargins(mContext, itemViewHolder.getAdapterPosition(), itemViewHolder.imageMe);
        //Load content image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                , itemViewHolder.imageMe);
        //check long form status
        checkLongFormStatus(itemViewHolder.containerLongShortPreview, data);
        //long form on click
        initLongFormPreviewClick(itemViewHolder.containerLongShortPreview, data, mContext, mCompositeDisposable);
        //Method called
        setDoubleTap(itemViewHolder, itemViewHolder.hatsOffView, data);
    }

    /**
     * Method to check hatsOff status and perform operation accordingly.
     *
     * @param hatsOffStatus  True if user has given hatsOff, false otherwise.
     * @param itemViewHolder ViewHolder object.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, ListItemViewHolder itemViewHolder) {
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
     *
     * @param hatsOffStatus  True if user has given hatsOff, false otherwise.
     * @param itemViewHolder ViewHolder object.
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
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final ListItemViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.containerHatsOff.setOnClickListener(new View.OnClickListener() {
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
                    }
                    //Toggle hatsOff status
                    itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                    //itemViewHolder.mIsRotated = !itemViewHolder.mIsRotated;
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
        itemViewHolder.containerHatsOff.setOnClickListener(new View.OnClickListener() {
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
                    }
                    //Toggle hatsOff status
                    itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                    //itemViewHolder.mIsRotated = !itemViewHolder.mIsRotated;
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
     * Compose onClick functionality.
     *
     * @param view     View to be clicked.
     * @param entityID Entity ID.
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
    private void shareOnClick(final ListItemViewHolder itemViewHolder, final FeedModel data) {
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
     * Method to load bitmap image to be shared
     */
    private void loadBitmapForSharing(final FeedModel data, final String shareOption) {

        Picasso.with(mContext).load(data.getContentImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //Set listener
                onShareListener.onShareClick(bitmap, data, shareOption);
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_SHARED_FROM_PROFILE, data.getEntityID());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_internal));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    /**
     * Initializes the views and click actions
     *
     * @param itemViewHolder view holder
     * @param data           Feed data
     * @param position       position of the item
     */
    private void initializeListItem(ListItemViewHolder itemViewHolder, final FeedModel data, final int position) {
        //Load creator profile picture
        ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                , itemViewHolder.imageCreator);
        //Set image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , itemViewHolder.imageContent
                , true);
        //Load content image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                , itemViewHolder.imageContent);

        // set text and click actions acc. to content type
        FeedHelper.performContentTypeSpecificOperations(mContext
                , data
                , itemViewHolder.collabCount
                , itemViewHolder.collabCount
                , itemViewHolder.buttonCollaborate
                , itemViewHolder.textCreatorName
                , true
                , true
                , itemViewHolder.collabCountDivider);

        // no need for creator options in explore
        final boolean shouldShowCreatorOptions = mUUID.equals(data.getUUID());

        // init content options menu
        if (!shouldShowCreatorOptions) {
            itemViewHolder.buttonMenu.setVisibility(View.GONE);
        } else {
            itemViewHolder.buttonMenu.setVisibility(View.VISIBLE);
            //open bottom sheet on clicking of 3 dots
            itemViewHolder.buttonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentHelper.getMenuActionsBottomSheet(mContext, position, data, onContentDeleteListener);
                }
            });
        }

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
        collaborationCountOnClick(itemViewHolder.collabCount, data.getEntityID(), data.getContentType());
        //check long form status
        checkLongFormStatus(itemViewHolder.collabCountDivider, data);
        //long form on click
        initLongFormPreviewClick(itemViewHolder.buttonLongWritingPreview, data, mContext, mCompositeDisposable);
        // init post timestamp
        updatePostTimestamp(itemViewHolder.textTimeStamp, data);
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
    private void initializeRepostViewHolder(RePostViewHolder itemViewHolder, final FeedModel data, final int position) {
        //Set reposter name , repost time and click functionality to open re-poster profile
        itemViewHolder.textRepostedBy.setText(data.getReposterName());
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

        // no need for creator options in explore
        final boolean shouldShowCreatorOptions = mUUID.equals(data.getReposterUUID());

        // init content options menu
        if (!shouldShowCreatorOptions) {
            itemViewHolder.buttonMenu.setVisibility(View.GONE);
        } else {
            itemViewHolder.buttonMenu.setVisibility(View.VISIBLE);
            //open bottom sheet on clicking of 3 dots
            itemViewHolder.buttonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentHelper.getRepostMenuBottomSheet(mContext, position, data, onRepostDeleteListener);
                }
            });
        }

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
        collaborationCountOnClick(itemViewHolder.collabCount, data.getEntityID(), data.getContentType());
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

    /**
     * Method to initialize load more listener.
     */
    private void initializeLoadMore(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mUserContentList.size() - 1 && !mIsLoading) {
            if (onLoadMore != null) {
                //Lode more data here
                onLoadMore.onLoadMore();
            }
            //toggle
            mIsLoading = true;
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
            bundle.putString("class_name", "me_feed");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_WRITE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_SHARED_FROM_PROFILE)) {
            bundle.putString("entity_id", entityID);
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_SHARED_FROM_PROFILE, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_CAPTURE_CLICKED)) {
            bundle.putString("class_name", "me_feed");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_CAPTURE_CLICKED, bundle);
        }
    }

    /**
     * Method to set double tap listener.
     *
     * @param holder      View to double tapped.
     * @param hatsOffView ImageView to be updated.
     * @param data        FeedModel data.
     */
    private void setDoubleTap(final RecyclerView.ViewHolder holder, final AppCompatImageView hatsOffView, final FeedModel data) {
        holder.itemView.setOnTouchListener(new OnGestureListener(mContext) {
            @Override
            public void onDoubleClick() {
                //region :Code to update hatsOff status
                //HatsOff given
                if (data.getHatsOffStatus()) {
                    SoundUtil.playHatsOffSound(mContext);
                }
                //HatsOff not given
                else {
                    //if device is connected  to internet
                    if (NetworkHelper.getNetConnectionStatus(mContext)) {

                        //update hatsOff data
                        data.setHatsOffCount(data.getHatsOffCount() + 1);
                        data.setHatsOffStatus(true);

                        HatsOffHelper helper = new HatsOffHelper(mContext);
                        helper.updateHatsOffStatus(data.getEntityID(), true);
                        helper.setOnHatsOffSuccessListener(new HatsOffHelper.OnHatsOffSuccessListener() {
                            @Override
                            public void onSuccess() {
                                //notify changes
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        });
                        helper.setOnHatsOffFailureListener(new HatsOffHelper.OnHatsOffFailureListener() {
                            @Override
                            public void onFailure(String errorMsg) {
                                //update hatsOff data
                                data.setHatsOffCount(data.getHatsOffCount() - 1);
                                data.setHatsOffStatus(false);
                                //notify changes
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        });
                    } else {
                        ViewHelper.getShortToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                    }

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
                //ItemView onClick functionality
                Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, data);
                bundle.putInt("position", holder.getAdapterPosition());

                Intent intent = new Intent(mContext, FeedDescriptionActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                mMeFragment.startActivityForResult(intent, REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY);
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

    //region :ViewHolder
    //ItemViewHolder class
    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        //Creator views
        @BindView(R.id.imageCreator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.textCreatorName)
        AppCompatTextView textCreatorName;
        @BindView(R.id.textTimestamp)
        AppCompatTextView textTimeStamp;
        @BindView(R.id.buttonCollaborate)
        AppCompatTextView buttonCollaborate;
        @BindView(R.id.buttonMenu)
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

        public ListItemViewHolder(View itemView) {
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

    //GridItemViewHolder class
    static class GridItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageGrid)
        SimpleDraweeView imageMe;
        @BindView(R.id.containerLongShortPreview)
        FrameLayout containerLongShortPreview;
        @BindView(R.id.live_filter_bubble)
        GravView liveFilterBubble;
        @BindView(R.id.whether_view)
        WeatherView whetherView;
        @BindView(R.id.konfetti_view)
        KonfettiView konfettiView;
        @BindView(R.id.hats_off_view)
        AppCompatImageView hatsOffView;

        public GridItemViewHolder(View itemView) {
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
    //endregion

}
