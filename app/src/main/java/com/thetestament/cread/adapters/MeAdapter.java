package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CommentsActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnContentDeleteListener;
import com.thetestament.cread.listeners.listener.OnUserActivityHatsOffListener;
import com.thetestament.cread.listeners.listener.OnUserActivityLoadMoreListener;
import com.thetestament.cread.models.FeedModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.helpers.ImageHelper.getLocalBitmapUri;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_PROFILE;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Me RecyclerView.
 */
public class MeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private List<FeedModel> mUserContentList;
    private FragmentActivity mContext;
    private boolean mIsLoading;
    private String mUUID;

    private OnUserActivityLoadMoreListener onLoadMore;
    private OnUserActivityHatsOffListener onHatsOffListener;
    private OnContentDeleteListener onContentDeleteListener;

    /**
     * Required constructor.
     *
     * @param mUserContentList List of feed data.
     * @param mContext         Context to be use.
     * @param mUUID            UUID of user.
     */
    public MeAdapter(List<FeedModel> mUserContentList, FragmentActivity mContext, String mUUID) {
        this.mUserContentList = mUserContentList;
        this.mContext = mContext;
        this.mUUID = mUUID;
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

    @Override
    public int getItemViewType(int position) {
        return mUserContentList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_me, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FeedModel data = mUserContentList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            //Load creator profile picture
            loadCreatorPic(data.getCreatorImage(), itemViewHolder.imageCreator);
            //Set creator name
            itemViewHolder.textCreatorName.setText(data.getCreatorName());
            //Set content type drawable
            setContentType(data.getContentType(), itemViewHolder.imageWorkType, itemViewHolder.buttonCompose);
            //Initialize delete button
            initializeDeleteButton(data.getUUID(), itemViewHolder.buttonDelete, position, data.getEntityID());

            //Load content image
            loadContentImage(data.getContentImage(), itemViewHolder.imageContent);
            //ItemView onClick functionality
            itemViewOnClick(itemViewHolder.itemView, data);
            //Compose click functionality
            composeOnClick(itemViewHolder.buttonCompose, data.getCaptureID(), data.getContentImage(), data.getEntityID(), data.isMerchantable());

            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);
            //HatsOff onClick functionality
            hatsOffOnClick(itemViewHolder, data, position);
            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder.containerShare, data.getContentImage(), data.getEntityID());


        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //Load more data  initialization
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
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to set content image depending upon its type.
     *
     * @param contentType      Type of content.
     * @param imageContentType Content imageView.
     */
    private void setContentType(String contentType, ImageView imageContentType, ImageView composeButton) {
        //Check for content type
        switch (contentType) {
            case CONTENT_TYPE_CAPTURE:
                imageContentType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_camera_alt_24));
                composeButton.setVisibility(View.VISIBLE);
                break;
            case CONTENT_TYPE_SHORT:
                imageContentType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_create_24));
                composeButton.setVisibility(View.GONE);
                break;
            default:
        }
    }


    /**
     * Method to setVisibility on delete button and initialize delete button functionality.
     *
     * @param creatorID UUID of content creator.
     */
    private void initializeDeleteButton(String creatorID, ImageView deleteButton, int index, String entityID) {
        if (mUUID.equals(creatorID)) {
            //Show delete button
            deleteButton.setVisibility(View.VISIBLE);
            //Delete click functionality
            deleteButtonOnClick(index, entityID, deleteButton);
        } else {
            //Hide delete button
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Delete button click functionality.
     *
     * @param index    position of item in adapter.
     * @param entityID Entity id of content.
     */
    private void deleteButtonOnClick(final int index, final String entityID, ImageView deleteButton) {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog(index, entityID);
            }
        });
    }

    /**
     * Method to show confirmation dialog before deletion.
     *
     * @param index    position of item in adapter.
     * @param entityID Entity id of content.
     */
    private void showDeleteConfirmationDialog(final int index, final String entityID) {
        new MaterialDialog.Builder(mContext)
                .content("Are you sure want to delete this?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        onContentDeleteListener.onDelete(entityID, index);
                        materialDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .build()
                .show();
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
    }


    /**
     * ItemView onClick functionality.
     *
     * @param view      View to be clicked.
     * @param feedModel Data set for current item.
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
     * Compose onClick functionality.
     *
     * @param view       View to be clicked.
     * @param captureID  CaptureID of image.
     * @param captureURL Capture image url.
     */
    private void composeOnClick(View view, final String captureID, final String captureURL, final String entityID, final boolean merchantable) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_CAPTURE_ID, captureID);
                bundle.putString(EXTRA_CAPTURE_URL, captureURL);
                bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);
                Intent intent = new Intent(mContext, ShortActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                mContext.startActivity(intent);

                //Log firebase event
                Bundle eventBundle = new Bundle();
                eventBundle.putString("uuid", mUUID);
                eventBundle.putString("entity_id", entityID);
                FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_SHARED_FROM_PROFILE, eventBundle);
            }
        });
    }

    /**
     * Method to check hatsOff status and perform operation accordingly.
     *
     * @param hatsOffStatus  True if user has given hatsOff, false otherwise.
     * @param itemViewHolder ViewHolder object.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, ItemViewHolder itemViewHolder) {
        if (hatsOffStatus) {
            itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
            //Animation for hats off
            itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off_fast));
            itemViewHolder.mIsHatsOff = true;
        } else {
            itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.grey));
            itemViewHolder.mIsHatsOff = false;
        }
    }


    /**
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final ItemViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.containerHatsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //User has already given the hats off
                    if (itemViewHolder.mIsHatsOff) {
                        //Animation for hats off
                        itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.reverse_rotate_animation_hats_off));
                        //Toggle hatsOff tint
                        itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.grey));
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
    private void shareOnClick(View view, final String pictureUrl, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Picasso.with(mContext).load(pictureUrl).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, mContext));
                        mContext.startActivity(Intent.createChooser(intent, "Share"));
                        //Log firebase event
                        Bundle bundle = new Bundle();
                        bundle.putString("uuid", mUUID);
                        bundle.putString("entity_id", entityID);
                        FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_SHARED_FROM_PROFILE, bundle);
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
        });
    }


    public void updateList(List<FeedModel> list) {
        mUserContentList = list;
        notifyDataSetChanged();
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

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        CircleImageView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.imageWorkType)
        ImageView imageWorkType;
        @BindView(R.id.buttonDelete)
        ImageView buttonDelete;
        @BindView(R.id.imageContent)
        ImageView imageContent;
        @BindView(R.id.buttonCompose)
        ImageView buttonCompose;
        @BindView(R.id.imageHatsOff)
        ImageView imageHatsOff;
        @BindView(R.id.containerHatsOff)
        LinearLayout containerHatsOff;
        @BindView(R.id.containerComment)
        LinearLayout containerComment;
        @BindView(R.id.containerShare)
        LinearLayout containerShare;

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

}
