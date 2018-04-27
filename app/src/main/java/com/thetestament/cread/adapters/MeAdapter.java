package com.thetestament.cread.adapters;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationDetailsActivity;
import com.thetestament.cread.activities.CommentsActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.NetworkHelper;
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
import com.thetestament.cread.utils.Constant.ITEM_TYPES;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.CreadApp.IMAGE_LOAD_FROM_NETWORK_ME;
import static com.thetestament.cread.helpers.ContentHelper.getMenuActionsBottomSheet;
import static com.thetestament.cread.helpers.FeedHelper.initializeShareDialog;
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

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Me RecyclerView.
 */
public class MeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM_LIST = 0;
    private final int VIEW_TYPE_ITEM_GRID = 1;
    private final int VIEW_TYPE_ITEM_LIST_COLLAB = 2;
    private final int VIEW_TYPE_LOADING = 3;

    private List<FeedModel> mUserContentList;
    private FragmentActivity mContext;
    private Fragment mMeFragment;
    private boolean mIsLoading;
    private String mUUID;
    private SharedPreferenceHelper mHelper;
    private ITEM_TYPES mItemType;
    private CompositeDisposable mCompositeDisposable;

    private OnUserActivityLoadMoreListener onLoadMore;
    private OnUserActivityHatsOffListener onHatsOffListener;
    private OnContentDeleteListener onContentDeleteListener;
    private OnContentEditListener onContentEditListener;
    private listener.OnShareListener onShareListener;
    private OnShareLinkClickedListener onShareLinkClickedListener;

    /**
     * Required constructor.
     *
     * @param mUserContentList List of feed data.
     * @param mContext         Context to be use.
     * @param mUUID            UUID of user.
     */
    public MeAdapter(List<FeedModel> mUserContentList, FragmentActivity mContext, String mUUID, Fragment mMeFragment, ITEM_TYPES mItemType, CompositeDisposable mCompositeDisposable) {
        this.mUserContentList = mUserContentList;
        this.mContext = mContext;
        this.mUUID = mUUID;
        this.mMeFragment = mMeFragment;
        this.mItemType = mItemType;
        this.mCompositeDisposable = mCompositeDisposable;

        mHelper = new SharedPreferenceHelper(mContext);
    }

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
     * Register a callback to be invoked when user clicks on share link button.
     */
    public void setOnShareLinkClickedListener(OnShareLinkClickedListener onShareLinkClickedListener) {
        this.onShareLinkClickedListener = onShareLinkClickedListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mUserContentList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (mItemType == ITEM_TYPES.LIST) {
            return VIEW_TYPE_ITEM_LIST;
        } else if (mItemType == ITEM_TYPES.GRID) {
            return VIEW_TYPE_ITEM_GRID;
        } else if (mItemType == ITEM_TYPES.COLLABLIST) {
            return VIEW_TYPE_ITEM_LIST_COLLAB;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM_LIST || viewType == VIEW_TYPE_ITEM_LIST_COLLAB) {
            return new ListItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_me, parent, false));
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
        if (holder.getItemViewType() == VIEW_TYPE_ITEM_LIST
                || holder.getItemViewType() == VIEW_TYPE_ITEM_LIST_COLLAB) {
            final ListItemViewHolder itemViewHolder = (ListItemViewHolder) holder;
            // initialize the views and click actions
            initializeListItem(itemViewHolder, data, itemViewHolder.getAdapterPosition());

        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_GRID) {
            final GridItemViewHolder itemViewHolder = (GridItemViewHolder) holder;
            // Set margins
            setGridItemMargins(mContext, position, itemViewHolder.imageMe);
            //Load content image
            loadContentImage(data.getContentImage(), itemViewHolder.imageMe);
            //item click functionality
            itemViewOnClick(itemViewHolder.itemView, data, position, true);
            //check long form status
            checkLongFormStatus(itemViewHolder.containerLongShortPreview, data);
            //long form on click
            initLongFormPreviewClick(itemViewHolder.containerLongShortPreview, data, mContext, mCompositeDisposable);
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

    /**
     * Method is set loading status to false..
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to load creator profile picture.
     *
     * @param picUrl    Picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadCreatorPic(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_100)
                .into(imageView);
    }

    /**
     * Method to load content image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadContentImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);

        RequestCreator requestCreator = Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder);

        if (IMAGE_LOAD_FROM_NETWORK_ME) {
            requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE);
            requestCreator.networkPolicy(NetworkPolicy.NO_CACHE);
        }


        requestCreator.into(imageView);

    }

    /**
     * Method to setVisibility on delete button and initialize delete button functionality.
     *
     * @param creatorID UUID of content creator.
     */
    private void initializeMenuButton(TextView menu, String creatorID) {
        if (mUUID.equals(creatorID) && mItemType != ITEM_TYPES.COLLABLIST) {
            //Show delete button
            menu.setVisibility(View.VISIBLE);
        } else {
            //Hide delete button
            menu.setVisibility(View.GONE);
        }
    }


    /**
     * ItemView onClick functionality.
     *
     * @param view                 View to be clicked.
     * @param feedModel            Data set for current item.
     * @param position             Position of item
     * @param showSharedTransition show transition if its true
     */
    private void itemViewOnClick(View view, final FeedModel feedModel, final int position, final boolean showSharedTransition) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set transition name
                ViewCompat.setTransitionName(view, feedModel.getEntityID());

                Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, feedModel);
                bundle.putInt("position", position);

                Intent intent = new Intent(mContext, FeedDescriptionActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);


                //If API is greater than LOLLIPOP
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && showSharedTransition) {
                    ActivityOptions transitionActivityOptions = ActivityOptions
                            .makeSceneTransitionAnimation(mContext, view, ViewCompat.getTransitionName(view));
                    //start activity result
                    mMeFragment.startActivityForResult(intent
                            , REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY
                            , transitionActivityOptions.toBundle());
                } else {
                    mMeFragment.startActivityForResult(intent, REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY);
                }
            }
        });
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
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final ListItemViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.containerHatsOff.setOnClickListener(new View.OnClickListener() {
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
     * @param view       View to be clicked.x
     * @param pictureUrl URL of the picture to be shared.
     * @param entityID   Entity id of content.
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

                        //dismiss dialog
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
                //Set listener
                onShareListener.onShareClick(bitmap, data);
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
        loadCreatorPic(data.getCreatorImage(), itemViewHolder.imageCreator);
        //Set image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , itemViewHolder.imageContent);
        //Load content image
        loadContentImage(data.getContentImage(), itemViewHolder.imageContent);

        // set text and click actions acc. to content type
        FeedHelper.performContentTypeSpecificOperations(mContext
                , data
                , itemViewHolder.collabCount
                , itemViewHolder.collabCount
                , itemViewHolder.buttonCollaborate
                , itemViewHolder.textCreatorName
                , true
                , true
                , itemViewHolder.lineSepartor);

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
                    getMenuActionsBottomSheet(mContext, position, data, onContentDeleteListener, shouldShowCreatorOptions);
                }
            });
        }

        //ItemView onClick functionality
        itemViewOnClick(itemViewHolder.itemView, data, position, false);
        //Check whether user has given hats off to this campaign or not
        checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);
        //HatsOff onClick functionality
        hatsOffOnClick(itemViewHolder, data, position);
        //Comment click functionality
        commentOnClick(itemViewHolder.containerComment, data.getEntityID());
        //Share click functionality
        shareOnClick(itemViewHolder.containerShare, data.getContentImage(), data.getEntityID(), data.getCreatorName(), data);
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

    //ItemViewHolder class
    static class ListItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        CircleImageView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.imageContent)
        AppCompatImageView imageContent;
        @BindView(R.id.imageHatsOff)
        ImageView imageHatsOff;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.containerHatsOff)
        LinearLayout containerHatsOff;
        @BindView(R.id.containerComment)
        LinearLayout containerComment;
        @BindView(R.id.containerShare)
        LinearLayout containerShare;
        @BindView(R.id.buttonMenu)
        ImageView buttonMenu;
        @BindView(R.id.collabCount)
        TextView collabCount;
        @BindView(R.id.lineSeparatorTop)
        View lineSepartor;
        @BindView(R.id.containerLongShortPreview)
        FrameLayout containerLongShortPreview;
        @BindView(R.id.textTimestamp)
        TextView textTimeStamp;

        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;
        //Variable to maintain  hats off view rotation status
        private boolean mIsRotated = false;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //GridItemViewHolder class
    static class GridItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imageGrid)
        ImageView imageMe;
        @BindView(R.id.containerLongShortPreview)
        FrameLayout containerLongShortPreview;

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
}
