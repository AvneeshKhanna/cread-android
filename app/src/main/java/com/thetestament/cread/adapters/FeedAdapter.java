package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationDetailsActivity;
import com.thetestament.cread.activities.CommentsActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnFeedCaptureClickListener;
import com.thetestament.cread.listeners.listener.OnFeedLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnHatsOffListener;
import com.thetestament.cread.listeners.listener.OnShareDialogItemClickedListener;
import com.thetestament.cread.listeners.listener.OnShareLinkClickedListener;
import com.thetestament.cread.listeners.listener.OnShareListener;
import com.thetestament.cread.models.FeedModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.helpers.FeedHelper.collabOnCollab;
import static com.thetestament.cread.helpers.FeedHelper.getCollabCountText;
import static com.thetestament.cread.helpers.FeedHelper.getCreatorText;
import static com.thetestament.cread.helpers.FeedHelper.initializeShareDialog;
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
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_CAPTURE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_MAIN_FEED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Feed RecyclerView.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<FeedModel> mFeedList;
    private FragmentActivity mContext;
    private Fragment mFeedFragment;
    private boolean mIsLoading;
    private String mUUID;

    private SharedPreferenceHelper mHelper;

    private OnFeedLoadMoreListener onFeedLoadMoreListener;
    private OnHatsOffListener onHatsOffListener;
    private OnFeedCaptureClickListener onFeedCaptureClickListener;
    private OnShareListener onShareListener;
    private OnShareLinkClickedListener onShareLinkClickedListener;

    /**
     * Required constructor.
     *
     * @param mFeedList List of feed data.
     * @param mContext  Context to be use.
     * @param mUUID     UUID of the user
     */
    public FeedAdapter(List<FeedModel> mFeedList, FragmentActivity mContext, String mUUID, Fragment mFeedFragment) {
        this.mFeedList = mFeedList;
        this.mContext = mContext;
        this.mUUID = mUUID;
        this.mFeedFragment = mFeedFragment;

        mHelper = new SharedPreferenceHelper(mContext);
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
     * Register a callback to be invoked when user clicks on capture button.
     */
    public void setOnFeedCaptureClickListener(OnFeedCaptureClickListener onFeedCaptureClickListener) {
        this.onFeedCaptureClickListener = onFeedCaptureClickListener;
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

    @Override
    public int getItemViewType(int position) {
        return mFeedList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
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
            //Load feed image
            loadFeedImage(data.getContentImage(), itemViewHolder.imageFeed);

            // set text and click actions acc. to content type
            //performContentTypeSpecificOperations(itemViewHolder, data);
            FeedHelper.performContentTypeSpecificOperations(mContext
                    , data
                    , itemViewHolder.collabCount
                    , itemViewHolder.collabCount
                    , itemViewHolder.buttonCollaborate
                    , itemViewHolder.textCreatorName
                    , true
                    , true
                    , itemViewHolder.lineSepartor);

            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);

            //ItemView collabOnWritingClick functionality
            itemViewOnClick(itemViewHolder.itemView, data, position);

            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder.containerShare, data.getContentImage(), data.getEntityID(), data.getCreatorName());
            //HatsOff collabOnWritingClick functionality
            hatsOffOnClick(itemViewHolder, data, position);
            //Collaboration count click functionality
            collaborationCountOnClick(itemViewHolder.collabCount, data.getEntityID(), data.getContentType());


        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
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
                .error(R.drawable.ic_account_circle_48)
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
     * Method that performs operations according to content type and collaboration functionality
     *
     * @param itemViewHolder item view holder
     * @param data           data
     */

    private void performContentTypeSpecificOperations(FeedAdapter.ItemViewHolder itemViewHolder, FeedModel data) {

        // initialize text
        String text = getCreatorText(mContext, data.getContentType(), data.isAvailableForCollab(), data.getCreatorName(), data.getCollabWithName());

        //Check for content type
        switch (data.getContentType()) {
            case CONTENT_TYPE_CAPTURE:

                // set Collaboration count text
                if (data.getCollabCount() != 0) {
                    itemViewHolder.collabCount.setText(getCollabCountText(mContext, data.getCollabCount(), data.getContentType(), data.isAvailableForCollab()));
                    itemViewHolder.collabCount.setVisibility(View.VISIBLE);
                    itemViewHolder.lineSepartor.setVisibility(View.VISIBLE);

                } else {
                    itemViewHolder.collabCount.setVisibility(View.GONE);
                    itemViewHolder.lineSepartor.setVisibility(View.GONE);
                }

                if (data.isAvailableForCollab()) {
                    // For stand alone capture
                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);
                    //write click functionality on capture
                    writeOnClick(itemViewHolder.buttonCollaborate, data.getCaptureID(), data.getContentImage(), data.getEntityID(), data.isMerchantable());

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


                    collabOnCollab(itemViewHolder.buttonCollaborate, mContext, data.getEntityID(), data.isMerchantable(), data.getContentType());

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

                // Set collaboration count text
                if (data.getCollabCount() != 0) {
                    itemViewHolder.collabCount.setText(getCollabCountText(mContext, data.getCollabCount(), data.getContentType(), data.isAvailableForCollab()));
                    itemViewHolder.collabCount.setVisibility(View.VISIBLE);
                    itemViewHolder.lineSepartor.setVisibility(View.VISIBLE);

                } else {
                    itemViewHolder.collabCount.setVisibility(View.GONE);
                    itemViewHolder.lineSepartor.setVisibility(View.GONE);
                }

                // check if available for collaboration
                if (data.isAvailableForCollab()) {
                    // for stand alone short
                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);
                    // capture click functionality on short
                    captureOnClick(itemViewHolder.buttonCollaborate, data.getEntityID(), data.getShortID());

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = -1; // since no collabwith
                    int collabWithEndPos = -1; // since no collabwith

                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());

                } else {
                    // showing collaborate button
                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);

                    collabOnCollab(itemViewHolder.buttonCollaborate, mContext, data.getEntityID(), data.isMerchantable(), data.getContentType());
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
     * Method to check hatsOff status and perform operation accordingly.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, ItemViewHolder itemViewHolder) {
        if (hatsOffStatus) {
            itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
            //Animation for hats off
            itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off_fast));
            itemViewHolder.mIsHatsOff = true;
        } else {
            itemViewHolder.imageHatsOff.setColorFilter(Color.TRANSPARENT);
            itemViewHolder.mIsHatsOff = false;
        }
    }


    /**
     * ItemView collabOnWritingClick functionality.
     *
     * @param view      View to be clicked.
     * @param feedModel Data set for current item
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
                mFeedFragment.startActivityForResult(intent, REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY);
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
    private void writeOnClick(View view, final String captureID, final String captureURL, final String entityID, final boolean merchantable) {
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
                setAnalytics(FIREBASE_EVENT_WRITE_CLICKED, entityID);
            }
        });
    }

    /**
     * capture collabOnWritingClick functionality.
     *
     * @param view View to be clicked.
     */
    private void captureOnClick(View view, final String entityID, final String shoID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mHelper.isWriteIconTooltipFirstTime()) {
                    // open dialog
                    getCaptureOnClickDialog(shoID);
                } else {
                    onFeedCaptureClickListener.onClick(shoID);
                }
                //Log Firebase event
                setAnalytics(FIREBASE_EVENT_CAPTURE_CLICKED, entityID);
            }
        });
    }


    /**
     * Compose collabOnWritingClick functionality.
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
     * Share collabOnWritingClick functionality.
     *
     * @param view       View to be clicked.
     * @param pictureUrl URL of the picture to be shared.
     */
    private void shareOnClick(View view, final String pictureUrl, final String entityID, final String creatorName) {
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
                                loadBitmapForSharing(pictureUrl, entityID);
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
     * HatsOff collabOnWritingClick functionality.
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
                        itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.reverse_rotate_animation_hats_off));
                        //Toggle hatsOff tint
                        itemViewHolder.imageHatsOff.setColorFilter(Color.TRANSPARENT);
                        //Update hats of count i.e decrease by one
                        data.setHatsOffCount(data.getHatsOffCount() - 1);
                    } else {
                        //Animation for hats off
                        itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off));
                        //Toggle hatsOff tint
                        itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
                        //Change hatsOffCount i.e increase by one
                        data.setHatsOffCount(data.getHatsOffCount() + 1);
                    }
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
     * Method to show intro dialog when user collaborated by clicking on capture
     *
     * @param shoID short ID on which user is collaborating
     */
    private void getCaptureOnClickDialog(final String shoID) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_generic, false)
                .positiveText(mContext.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open capture functionality

                        onFeedCaptureClickListener.onClick(shoID);

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


        ///Set filler image
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


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_collab_intro));
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

    /**
     * Method to load bitmap image to be shared
     */
    private void loadBitmapForSharing(final String pictureUrl, final String entityID) {
        Picasso.with(mContext).load(pictureUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //Set Listener
                onShareListener.onShareClick(bitmap);
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_SHARED_FROM_MAIN_FEED, entityID);
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
        @BindView(R.id.collabCount)
        TextView collabCount;
        @BindView(R.id.lineSeparatorTop)
        View lineSepartor;

        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;

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
}
