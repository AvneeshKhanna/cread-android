package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatImageView;
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

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.PrecipType;
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
import com.thetestament.cread.helpers.NetworkHelper;
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

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_RECOMMENDED_ARTIST = 2;


    private List<FeedModel> mFeedList;
    private FragmentActivity mContext;
    private Fragment mFeedFragment;
    private boolean mIsLoading;
    private String mUUID;
    private CompositeDisposable mCompositeDisposable;
    private Bitmap bitmap;

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
    public void setOnDownVoteClickedListener(OnDownvoteClickedListener onDownvoteClickedListener) {
        this.onDownvoteClickedListener = onDownvoteClickedListener;
    }

    @Override
    public int getItemViewType(int position) {
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
            ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorImage())
                    , itemViewHolder.imageCreator);

            //Set image width and height
            AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                    , data.getImgHeight()
                    , itemViewHolder.imageFeed
                    , true);


            //Load feed image
            ImageHelper.loadProgressiveImage(Uri.parse(data.getContentImage())
                    , itemViewHolder.imageFeed);
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
            initLiveFilters(data.getLiveFilterName(), itemViewHolder);
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
     * @param itemViewHolder
     * @param data
     */
    private void shareOnClick(final ItemViewHolder itemViewHolder, final FeedModel data) {
        itemViewHolder.logoWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    new GifHelper(mContext, bitmap, itemViewHolder.frameLayout, SHARE_OPTION_WHATSAPP)
                            .startHandlerTask(new Handler(), 0);
                } else {
                    loadBitmapForSharing(data, SHARE_OPTION_WHATSAPP);
                }
            }
        });

        itemViewHolder.logoFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    new GifHelper(mContext, bitmap, itemViewHolder.frameLayout, SHARE_OPTION_FACEBOOK)
                            .startHandlerTask(new Handler(), 0);
                } else {
                    loadBitmapForSharing(data, SHARE_OPTION_FACEBOOK);
                }

            }
        });

        itemViewHolder.logoInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    new GifHelper(mContext, bitmap, itemViewHolder.frameLayout, SHARE_OPTION_INSTAGRAM)
                            .startHandlerTask(new Handler(), 0);
                } else {
                    loadBitmapForSharing(data, SHARE_OPTION_INSTAGRAM);
                }

            }
        });

        itemViewHolder.logoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    new GifHelper(mContext, bitmap, itemViewHolder.frameLayout, SHARE_OPTION_OTHER)
                            .startHandlerTask(new Handler(), 0);
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
     * Method to initialize live filter.
     *
     * @param filterName Name of filter to be applied.
     */
    private void initLiveFilters(String filterName, ItemViewHolder viewHolder) {
        switch (filterName) {
            case Constant.LIVE_FILTER_SNOW:
                viewHolder.whetherView.setWeatherData(PrecipType.SNOW);
                viewHolder.whetherView.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_RAIN:
                viewHolder.whetherView.setWeatherData(PrecipType.RAIN);
                viewHolder.whetherView.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_BUBBLE:
                viewHolder.liveFilterBubble.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_CONFETTI:
                viewHolder.konfettiView.setVisibility(View.VISIBLE);
                ViewHelper.showKonfetti(viewHolder.konfettiView);
                break;
            case Constant.LIVE_FILTER_NONE:
                //do nothing
                break;
        }
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageFeed)
        SimpleDraweeView imageFeed;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.imageHatsOff)
        ImageView imageHatsOff;
        @BindView(R.id.containerHatsOff)
        LinearLayout containerHatsOff;
        @BindView(R.id.containerComment)
        LinearLayout containerComment;
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
        @BindView(R.id.logoWhatsapp)
        AppCompatImageView logoWhatsapp;
        @BindView(R.id.logoFacebook)
        AppCompatImageView logoFacebook;
        @BindView(R.id.logoInstagram)
        AppCompatImageView logoInstagram;
        @BindView(R.id.logoMore)
        AppCompatImageView logoMore;
        @BindView(R.id.layoutShareOptions)
        LinearLayout layoutShareOptions;
        @BindView(R.id.container)
        FrameLayout frameLayout;
        @BindView(R.id.live_filter_bubble)
        GravView liveFilterBubble;
        @BindView(R.id.whether_view)
        WeatherView whetherView;
        @BindView(R.id.konfetti_view)
        KonfettiView konfettiView;

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

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        }
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return super.onFailedToRecycleView(holder);
    }
}
