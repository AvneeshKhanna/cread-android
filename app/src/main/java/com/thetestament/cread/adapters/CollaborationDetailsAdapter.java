package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnCollaborationDetailsLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnCollaborationItemClickedListener;
import com.thetestament.cread.models.CollaborationDetailsModel;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.dionsegijn.konfetti.KonfettiView;

/**
 * Adapter class to provide a binding from data set to views that are displayed within collaboration details RecyclerView.
 */
public class CollaborationDetailsAdapter extends RecyclerView.Adapter<CollaborationDetailsAdapter.ItemViewHolder> {

    private List<CollaborationDetailsModel> mCollaborationList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnCollaborationDetailsLoadMoreListener loadMoreListener;
    private OnCollaborationItemClickedListener onCollaborationItemClickedListener;

    /**
     * Required constructor.
     *
     * @param mCollaborationList List of collaboration details data.
     * @param mContext           Context to use.
     */

    public CollaborationDetailsAdapter(List<CollaborationDetailsModel> mCollaborationList, FragmentActivity mContext) {
        this.mCollaborationList = mCollaborationList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(OnCollaborationDetailsLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }


    public void setCollaborationItemClickedListener(OnCollaborationItemClickedListener onCollaborationItemClickedListener) {
        this.onCollaborationItemClickedListener = onCollaborationItemClickedListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.item_collaboration_details
                        , parent
                        , false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        CollaborationDetailsModel data = mCollaborationList.get(position);
        //Set creator name
        holder.textCreatorName.setText(data.getUserName());
        //Load creator image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getProfilePic()), holder.imageCreator);

        //Set image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , holder.imageCollaboration
                , true);
        //Load content image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getEntityUrl())
                , holder.imageCollaboration);
        //Click functionality to launch profile of creator
        openCreatorProfile(holder.containerCreator, data.getUuid());

        // init click listener
        initItemClick(holder.imageCollaboration, data.getEntityID());

        //Method called
        //fixme update this with real data
        //initLiveFilters(Constant.LIVE_FILTER_BUBBLE, itemViewHolder);

        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mCollaborationList.size() - 1 && !mIsLoading) {
            if (loadMoreListener != null) {
                //Lode more data here
                loadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    @Override
    public int getItemCount() {
        return mCollaborationList.size();
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }


    /**
     * Method to initialize item click functionality.
     *
     * @param view     View to be clicked
     * @param entityID Entity id of the post.
     */
    private void initItemClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onCollaborationItemClickedListener.onItemClicked(entityID);
            }
        });
    }

    /**
     * Method to open creator profile.
     *
     * @param view        View to be clicked.
     * @param creatorUUID UUID of the creator.
     */
    private void openCreatorProfile(View view, final String creatorUUID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Method called
                IntentHelper.openProfileActivity(mContext, creatorUUID);
            }
        });
    }

    /**
     * Method to initialize live filter.
     *
     * @param filterName Name of filter to be applied.
     */
    private void initLiveFilters(String filterName, CollaborationDetailsAdapter.ItemViewHolder viewHolder) {
        switch (filterName) {
            case Constant.LIVE_FILTER_SNOW:
                viewHolder.weatherView.setWeatherData(PrecipType.SNOW);
                viewHolder.weatherView.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_RAIN:
                viewHolder.weatherView.setWeatherData(PrecipType.RAIN);
                viewHolder.weatherView.setVisibility(View.VISIBLE);
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
        @BindView(R.id.container_creator)
        RelativeLayout containerCreator;
        @BindView(R.id.image_creator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.text_creator_name)
        AppCompatTextView textCreatorName;
        @BindView(R.id.image_collaboration)
        SimpleDraweeView imageCollaboration;
        @BindView(R.id.image_container)
        FrameLayout imageContainer;
        @BindView(R.id.live_filter_bubble)
        GravView liveFilterBubble;
        @BindView(R.id.weather_view)
        WeatherView weatherView;
        @BindView(R.id.konfetti_view)
        KonfettiView konfettiView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
