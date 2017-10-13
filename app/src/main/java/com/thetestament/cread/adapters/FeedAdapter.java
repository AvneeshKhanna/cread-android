package com.thetestament.cread.adapters;

import android.content.Intent;
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

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.listeners.listener.OnFeedLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnHatsOffListener;
import com.thetestament.cread.models.FeedModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DATA;


public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<FeedModel> mFeedList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnFeedLoadMoreListener onFeedLoadMoreListener;
    private OnHatsOffListener onHatsOffListener;

    /**
     * Required constructor.
     *
     * @param mFeedList List of feed data.
     * @param mContext  Context to be use.
     */
    public FeedAdapter(List<FeedModel> mFeedList, FragmentActivity mContext) {
        this.mFeedList = mFeedList;
        this.mContext = mContext;
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
                    .inflate(R.layout.item_feed_loading, parent, false));
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
            //Set creator name
            itemViewHolder.textCreatorName.setText(data.getCreatorName());
            //Load image feed
            loadFeedImage(data.getImage(), itemViewHolder.imageFeed);


            //Check for content type
            switch (data.getContentType()) {
                case CONTENT_TYPE_CAPTURE:
                    itemViewHolder.imageWorkType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_camera_alt_24));
                    break;
                case CONTENT_TYPE_SHORT:
                    itemViewHolder.imageWorkType.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_create_24));
                    break;
                default:
                    //do nothing
                    break;
            }

            //Click functionality to launch profile of creator
            openCreatorProfile(itemViewHolder.containerCreator, data.getEntityID());
            //Compose click functionality
            composeOnClick(itemViewHolder.buttonCompose, data.getEntityID());
            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder.containerShare, data.getEntityID());
            //ItemView onClick functionality
            itemViewOnClick(itemViewHolder.itemView, data);
            //HatsOff onClick functionality
            hatsOffOnClick(itemViewHolder, data);

            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);


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
     * Method to load creator profile picture.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadFeedImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .into(imageView);
        //Todo No image placeholder
    }

    /**
     * Method to open creator profile.
     */
    private void openCreatorProfile(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(EXTRA_ENTITY_ID, entityID);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * Compose onClick functionality.
     */
    private void commentOnClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO functionality remaining
            }
        });
    }

    /**
     * Compose onClick functionality.
     */
    private void shareOnClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO functionality remaining
            }
        });
    }

    /**
     * Compose onClick functionality.
     */
    private void composeOnClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO functionality remaining
            }
        });
    }

    /**
     * ItemView onClick functionality.
     */
    private void itemViewOnClick(View view, final FeedModel feedModel) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FeedDescriptionActivity.class);
                intent.putExtra(EXTRA_FEED_DATA, feedModel);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * HatsOff onClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final ItemViewHolder itemViewHolder, final FeedModel data) {
        itemViewHolder.imageHatsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User has already given the hats off
                if (itemViewHolder.mIsHatsOff) {
                    //Animation for hats off
                    itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.reverse_rotate_animation_hats_off));
                    //Toggle hatsOff status
                    itemViewHolder.mIsHatsOff = false;
                    //Toggle hatsOff tint
                    itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.grey));
                    //Listener
                    onHatsOffListener.onHatsOffClick(data, itemViewHolder.mIsHatsOff);
                    data.setHatsOffCount(data.getHatsOffCount() - 1);
                } else {
                    //Animation for hats off
                    itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off));
                    //Toggle hatsOff status
                    itemViewHolder.mIsHatsOff = true;
                    //Listener
                    onHatsOffListener.onHatsOffClick(data, itemViewHolder.mIsHatsOff);
                    //Toggle hatsOff tint
                    itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    //Change hatsOffCount i.e increase by one
                    data.setHatsOffCount(data.getHatsOffCount() + 1);
                }
                //Update hats off here
                data.setHatsOffStatus(itemViewHolder.mIsHatsOff);
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
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageFeed)
        ImageView imageFeed;
        @BindView(R.id.buttonCompose)
        ImageView buttonCompose;
        @BindView(R.id.imageHatsOff)
        ImageView imageHatsOff;
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