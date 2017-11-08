package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CommentsActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.HatsOffActivity;
import com.thetestament.cread.helpers.ViewHelper;
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
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.USER_ACTIVITY_TYPE_ALL;
import static com.thetestament.cread.utils.Constant.USER_ACTIVITY_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.USER_ACTIVITY_TYPE_SHORT;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Me RecyclerView.
 */
public class MeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_ITEM_SHORT = 2;
    private final int VIEW_TYPE_ITEM_CAPTURE = 3;

    private List<FeedModel> mUserContentList;
    private FragmentActivity mContext;
    private boolean mIsLoading;
    private String mUUID;
    private String mUserActivityType;

    private OnUserActivityLoadMoreListener onLoadMore;
    private OnUserActivityHatsOffListener onHatsOffListener;

    /**
     * Required constructor.
     *
     * @param mUserContentList List of feed data.
     * @param mContext         Context to be use.
     */
    public MeAdapter(List<FeedModel> mUserContentList, FragmentActivity mContext, String mUUID, String mUserActivityType) {
        this.mUserContentList = mUserContentList;
        this.mContext = mContext;
        this.mUUID = mUUID;
        this.mUserActivityType = mUserActivityType;
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

            switch (mUserActivityType) {
                case USER_ACTIVITY_TYPE_ALL:
                    itemViewHolder.itemView.setVisibility(View.VISIBLE);
                    break;
                case USER_ACTIVITY_TYPE_SHORT:
                    if (data.getContentType().equals(CONTENT_TYPE_CAPTURE)) {
                        itemViewHolder.itemView.setVisibility(View.GONE);
                    } else {
                        itemViewHolder.itemView.setVisibility(View.VISIBLE);
                    }
                    break;
                case USER_ACTIVITY_TYPE_CAPTURE:
                    if (data.getContentType().equals(CONTENT_TYPE_SHORT)) {
                        itemViewHolder.itemView.setVisibility(View.GONE);
                    } else {
                        itemViewHolder.itemView.setVisibility(View.VISIBLE);
                    }
                    break;

            }

            //Load creator profile picture
            loadCreatorPic(data.getCreatorImage(), itemViewHolder.imageCreator);
            //Set creator name
            itemViewHolder.textCreatorName.setText(data.getCreatorName());
            //Load story image
            loadStoryImage(data.getContentImage(), itemViewHolder.imageStory);

            //Check for content type
            switch (data.getContentType()) {
                case CONTENT_TYPE_CAPTURE:
                    itemViewHolder.imageWorkType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_camera_alt_24));
                    break;
                case CONTENT_TYPE_SHORT:
                    itemViewHolder.imageWorkType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_create_24));
                    break;
                default:
            }

            if (data.getUUID().equals(mUUID)) {
                //itemViewHolder.buttonMore.setVisibility(View.VISIBLE);
            }
            //// TODO: more button functionality

            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);

            //ItemView onClick functionality
            itemViewOnClick(itemViewHolder.itemView, data);
            //hats off container click functionality
            hatsOffContainerOnClick(itemViewHolder.containerHatsOff, data.getEntityID());
            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder.containerShare, data.getContentImage());
            //HatsOff onClick functionality
            hatsOffOnClick(itemViewHolder, data, position);


        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }


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

    @Override
    public int getItemCount() {
        return mUserContentList == null ? 0 : mUserContentList.size();
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    public void setmUserActivityType(String s) {
        mUserActivityType = s;
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
     * Method to load story image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadStoryImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);
    }


    /**
     * ItemView onClick functionality.
     *
     * @param view      View to be clicked.
     * @param feedModel Data set for current item
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
     * @param view     View to be clicked.
     * @param entityID Entity ID.
     */
    private void hatsOffContainerOnClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, HatsOffActivity.class);
                intent.putExtra("entityID", entityID);
                mContext.startActivity(intent);
            }
        });
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
                intent.putExtra("entityID", entityID);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * Share onClick functionality.
     *
     * @param view       View to be clicked.x
     * @param pictureUrl URL of the picture to be shared.
     */
    private void shareOnClick(View view, final String pictureUrl) {
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
            }
        });
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
            itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.grey));
            itemViewHolder.mIsHatsOff = false;
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
        @BindView(R.id.buttonMore)
        ImageView buttonMore;
        @BindView(R.id.imageStory)
        ImageView imageStory;
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
