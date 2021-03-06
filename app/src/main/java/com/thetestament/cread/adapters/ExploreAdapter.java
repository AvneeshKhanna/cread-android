package com.thetestament.cread.adapters;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.gaurav.gesto.OnGestureListener;
import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.WeatherView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationDetailsActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.LiveFilterHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnExploreCaptureClickListener;
import com.thetestament.cread.listeners.listener.OnExploreFollowListener;
import com.thetestament.cread.listeners.listener.OnExploreLoadMoreListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.Constant.ITEM_TYPES;
import com.thetestament.cread.utils.SoundUtil;
import com.thetestament.cread.utils.TextUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import nl.dionsegijn.konfetti.KonfettiView;

import static com.thetestament.cread.helpers.FeedHelper.setGridItemMargins;
import static com.thetestament.cread.helpers.FeedHelper.updatePostTimestamp;
import static com.thetestament.cread.helpers.LongShortHelper.checkLongFormStatus;
import static com.thetestament.cread.helpers.LongShortHelper.initLongFormPreviewClick;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_MEME;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_CAPTURE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_EXPLORE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a explore RecyclerView.
 */
public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    //region :View types
    private final int VIEW_TYPE_ITEM_LIST = 0;
    private final int VIEW_TYPE_MEME = 1;
    private final int VIEW_TYPE_ITEM_GRID = 2;
    private final int VIEW_TYPE_LOADING = 3;
    //endregion

    //region :Fields and constants
    private List<FeedModel> mExploreList;
    private FragmentActivity mContext;
    private Fragment mExploreFragment;
    private boolean mIsLoading;
    private String mUUID;
    private ITEM_TYPES mItemType;
    private CompositeDisposable mCompositeDisposable;
    //endregion

    //region :Listeners
    private OnExploreLoadMoreListener onExploreLoadMoreListener;
    private OnExploreFollowListener onExploreFollowListener;
    private OnExploreCaptureClickListener onExploreCaptureClickListener;

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

    //endregion

    //region :Constructor

    /**
     * Required constructor.
     *
     * @param exploreList         List of explore data.
     * @param context             Context to be use.
     * @param UUID                UUID of user.
     * @param exploreFragment     Explore fragment reference.
     * @param itemType            Item type whether its LIST or GRID
     * @param compositeDisposable CompositeDisposable reference.
     */
    public ExploreAdapter(List<FeedModel> exploreList, FragmentActivity context, String UUID, Fragment exploreFragment, ITEM_TYPES itemType, CompositeDisposable compositeDisposable) {
        this.mExploreList = exploreList;
        this.mContext = context;
        this.mUUID = UUID;
        this.mExploreFragment = exploreFragment;
        this.mItemType = itemType;
        this.mCompositeDisposable = compositeDisposable;
    }
    //endregion

    //region :Overridden methods
    @Override
    public int getItemViewType(int position) {
        if (mExploreList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (mItemType == ITEM_TYPES.LIST) {
            if (mExploreList.get(position).getContentType().equals(CONTENT_TYPE_MEME)) {
                return VIEW_TYPE_MEME;
            } else {
                return VIEW_TYPE_ITEM_LIST;
            }
        } else if (mItemType == ITEM_TYPES.GRID) {
            return VIEW_TYPE_ITEM_GRID;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM_LIST) {
            return new ListItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_explore, parent, false));
        } else if (viewType == VIEW_TYPE_ITEM_GRID) {
            return new GridItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_grid, parent, false));
        } else if (viewType == VIEW_TYPE_MEME) {
            return new MemeViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_meme_explore, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final FeedModel data = mExploreList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM_LIST) {
            final ListItemViewHolder itemViewHolder = (ListItemViewHolder) holder;
            initListItem(itemViewHolder, data);
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_GRID) {
            final GridItemViewHolder itemViewHolder = (GridItemViewHolder) holder;
            initGridItem(itemViewHolder, data);
        }
        if (holder.getItemViewType() == VIEW_TYPE_MEME) {
            final MemeViewHolder itemViewHolder = (MemeViewHolder) holder;
            initMemeItem(itemViewHolder, data);
        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }
        //Method called
        initLoadMoreListener(position);

    }

    @Override
    public int getItemCount() {
        return mExploreList == null ? 0 : mExploreList.size();
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM_LIST) {
            final ListItemViewHolder itemViewHolder = (ListItemViewHolder) holder;
            LiveFilterHelper.initLiveFilters(mExploreList.get(holder.getAdapterPosition()).getLiveFilterName()
                    , itemViewHolder.whetherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);
        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_GRID) {
            final GridItemViewHolder itemViewHolder = (GridItemViewHolder) holder;
            LiveFilterHelper.initLiveFilters(mExploreList.get(holder.getAdapterPosition()).getLiveFilterName()
                    , itemViewHolder.whetherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);
        }
    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize load more listener.
     */
    private void initLoadMoreListener(int position) {
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


    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to initialize list item.
     *
     * @param itemViewHolder ListItemViewHolder
     * @param data           Data for the item
     */
    private void initListItem(ListItemViewHolder itemViewHolder, FeedModel data) {
        //Load creator profile picture
        ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                , itemViewHolder.imageCreator);

        //Set text and click actions according to content type
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
        checkFollowStatus(data, itemViewHolder.buttonFollow);

        //Set image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , itemViewHolder.imageExplore
                , true);
        //Load explore feed image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                , itemViewHolder.imageExplore);


        //Follow button click functionality
        followOnClick(itemViewHolder.getAdapterPosition(), data, itemViewHolder.buttonFollow);
        //Method called
        setDoubleTap(itemViewHolder, itemViewHolder.hatsOffView, data);
        //Collaboration count click functionality
        collaborationCountOnClick(itemViewHolder.collabCount, data.getEntityID(), data.getContentType());

        //check long form status
        checkLongFormStatus(itemViewHolder.containerLongShortPreview, data);
        //long form on click
        initLongFormPreviewClick(itemViewHolder.containerLongShortPreview, data, mContext, mCompositeDisposable);
        // init post timestamp
        updatePostTimestamp(itemViewHolder.textTimeStamp, data);

    }


    /**
     * Method to initialize Meme item.
     *
     * @param itemViewHolder MemeViewHolder
     * @param data           Data for the item
     */
    private void initMemeItem(MemeViewHolder itemViewHolder, FeedModel data) {
        //Load creator profile picture
        ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                , itemViewHolder.imgCreator);
        //Set user creator name and its click functionality
        itemViewHolder.creatorName.setText(TextUtils.getSpannedString(data.getCreatorName() + " added meme"
                , new ForegroundColorSpan(Color.BLACK)
                , 0
                , data.getCreatorName().length()
                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        openProfileActivity(itemViewHolder.creatorName, data.getUUID());

        //Check follow status
        checkFollowStatus(data, itemViewHolder.btnFollow);

        //Set image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , itemViewHolder.imgExplore
                , true);
        //Load explore feed image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                , itemViewHolder.imgExplore);


        //Follow button click functionality
        followOnClick(itemViewHolder.getAdapterPosition(), data, itemViewHolder.btnFollow);
        //Method called
        setDoubleTap(itemViewHolder, itemViewHolder.hatsOffView, data);

        // init post timestamp
        updatePostTimestamp(itemViewHolder.textTimestamp, data);
    }

    /**
     * Method to initialize grid item.
     *
     * @param itemViewHolder GridItemViewHolder
     * @param data           Data for the item
     */
    private void initGridItem(GridItemViewHolder itemViewHolder, FeedModel data) {
        // set margins
        setGridItemMargins(mContext, itemViewHolder.getAdapterPosition(), itemViewHolder.imageExplore);
        //Load explore feed image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage()), itemViewHolder.imageExplore);

        //Method called
        setDoubleTap(itemViewHolder, itemViewHolder.hatsOffView, data);

        //check long form status
        checkLongFormStatus(itemViewHolder.containerLongShortPreview, data);
        //long form on click
        initLongFormPreviewClick(itemViewHolder.containerLongShortPreview, data, mContext, mCompositeDisposable);

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
                    setAnalytics(FIREBASE_EVENT_FOLLOW_FROM_EXPLORE);
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
     * @param data           data
     * @param itemViewHolder item view holder
     */
    private void checkFollowStatus(FeedModel data, View btnFollow) {
        if (data.getFollowStatus() || mUUID.equals(data.getUUID())) {
            btnFollow.setVisibility(View.GONE);
        } else {
            // show follow button
            btnFollow.setVisibility(View.VISIBLE);
        }
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
                        helper.setOnHatsOffSuccessListener(new HatsOffHelper.OnHatsOffSuccessListener() {
                            @Override
                            public void onSuccess() {
                                //do nothing
                            }
                        });
                        helper.updateHatsOffStatus(data.getEntityID(), true);
                        helper.setOnHatsOffFailureListener(new HatsOffHelper.OnHatsOffFailureListener() {
                            @Override
                            public void onFailure(String errorMsg) {
                                //update hatsOff data
                                data.setHatsOffCount(data.getHatsOffCount() - 1);
                                data.setHatsOffStatus(false);
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
                mExploreFragment.startActivityForResult(intent, REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY);
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

    //region :ViewHolder class
    //ListItemViewHolder class
    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.buttonFollow)
        TextView buttonFollow;
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageExplore)
        SimpleDraweeView imageExplore;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.collabCount)
        TextView collabCount;
        @BindView(R.id.containerLongShortPreview)
        FrameLayout containerLongShortPreview;
        @BindView(R.id.textTimestamp)
        TextView textTimeStamp;
        @BindView(R.id.live_filter_bubble)
        GravView liveFilterBubble;
        @BindView(R.id.whether_view)
        WeatherView whetherView;
        @BindView(R.id.konfetti_view)
        KonfettiView konfettiView;
        @BindView(R.id.hats_off_view)
        AppCompatImageView hatsOffView;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //GridItemViewHolder class
    static class GridItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageContainer)
        FrameLayout squareView;
        @BindView(R.id.imageGrid)
        SimpleDraweeView imageExplore;
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

    //MemePostViewHolder
    static class MemeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_creator)
        SimpleDraweeView imgCreator;
        @BindView(R.id.btn_follow)
        AppCompatTextView btnFollow;
        @BindView(R.id.creator_name)
        TextView creatorName;
        @BindView(R.id.text_timestamp)
        TextView textTimestamp;
        @BindView(R.id.container_creator_specific)
        LinearLayout containerCreatorSpecific;
        @BindView(R.id.container_creator)
        RelativeLayout containerCreator;
        @BindView(R.id.img_explore)
        SimpleDraweeView imgExplore;
        @BindView(R.id.hats_off_view)
        AppCompatImageView hatsOffView;
        @BindView(R.id.container_image)
        FrameLayout containerImage;

        public MemeViewHolder(View itemView) {
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