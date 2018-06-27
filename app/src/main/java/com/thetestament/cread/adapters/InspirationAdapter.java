package com.thetestament.cread.adapters;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.WeatherView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.LiveFilterHelper;
import com.thetestament.cread.listeners.listener.OnInspirationLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnInspirationSelectListener;
import com.thetestament.cread.models.InspirationModel;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.dionsegijn.konfetti.KonfettiView;

import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_IMAGE_HEIGHT;
import static com.thetestament.cread.utils.Constant.EXTRA_IMAGE_WIDTH;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a inspiration RecyclerView.
 */

public class InspirationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_ITEM_DETAIL = 2;
    private List<InspirationModel> mInspirationList;
    private FragmentActivity mContext;
    private boolean mIsLoading;
    private String mItemType;

    private OnInspirationLoadMoreListener loadMoreListener;
    private OnInspirationSelectListener inspirationSelectListener;

    /**
     * Required constructor.
     *
     * @param inspirationList List of inspiration data.
     * @param context         Context to be use.
     */
    public InspirationAdapter(List<InspirationModel> inspirationList, FragmentActivity context, String itemType) {
        this.mInspirationList = inspirationList;
        this.mContext = context;
        this.mItemType = itemType;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(OnInspirationLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    /**
     * Register a callback to be invoked when user select image from list.
     */
    public void setInspirationSelectListener(OnInspirationSelectListener inspirationSelectListener) {
        this.inspirationSelectListener = inspirationSelectListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mInspirationList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            if (mItemType.equals(Constant.INSPIRATION_ITEM_TYPE_SMALL)) {
                return VIEW_TYPE_ITEM;
            } else if (mItemType.equals(Constant.INSPIRATION_ITEM_TYPE_DETAIL)) {
                return VIEW_TYPE_ITEM_DETAIL;
            }
        }
        //Default view to  be loaded
        return VIEW_TYPE_ITEM_DETAIL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_inspiration, parent, false));
        } else if (viewType == VIEW_TYPE_ITEM_DETAIL) {
            return new ItemViewHolderDetail(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_inspiration_detail, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        InspirationModel data = mInspirationList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Load inspiration image
            ImageHelper.loadProgressiveImage(Uri.parse(data.getCapturePic())
                    , itemViewHolder.imageInspiration);
            //ItemView onClick functionality
            itemViewOnClick(itemViewHolder.itemView, data, itemViewHolder.getAdapterPosition());

        } else if (holder.getItemViewType() == VIEW_TYPE_ITEM_DETAIL) {
            final ItemViewHolderDetail itemViewHolder = (ItemViewHolderDetail) holder;
            //Load creator profile picture
            ImageHelper.loadProgressiveImage(Uri.parse(data.getCreatorProfilePic())
                    , itemViewHolder.imageCreator);
            //Set creator name
            itemViewHolder.textCreatorName.setText(data.getCreatorName());

            //Set image width and height
            AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                    , data.getImgHeight()
                    , itemViewHolder.imageInspiration
                    , true);
            //Load inspiration image
            ImageHelper.loadProgressiveImage(Uri.parse(data.getCapturePic())
                    , itemViewHolder.imageInspiration);

            //Click functionality to launch profile of creator
            openCreatorProfile(itemViewHolder.containerCreator, data.getUUID());

            //ItemView onClick functionality
            itemOnClick(itemViewHolder.itemView, data);
            //Method called
            LiveFilterHelper.initLiveFilters(data.getLiveFilterName()
                    , itemViewHolder.whetherView
                    , itemViewHolder.konfettiView
                    , itemViewHolder.liveFilterBubble
                    , mContext);

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }


        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mInspirationList.size() - 1 && !mIsLoading) {
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
        return mInspirationList == null ? 0 : mInspirationList.size();
    }


    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }


    /**
     * ItemView click functionality.
     */
    private void itemViewOnClick(View view, final InspirationModel data, final int itemPosition) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set listener
                inspirationSelectListener.onInspireImageSelected(data, itemPosition);
            }
        });
    }

    /**
     * ItemView click functionality.
     */
    private void itemOnClick(View view, final InspirationModel data) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_CAPTURE_ID, data.getCaptureID());
                bundle.putString(EXTRA_CAPTURE_URL, data.getCapturePic());
                bundle.putBoolean(EXTRA_MERCHANTABLE, data.isMerchantable());
                bundle.putInt(EXTRA_IMAGE_WIDTH, data.getImgWidth());
                bundle.putInt(EXTRA_IMAGE_HEIGHT, data.getImgHeight());
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DATA, bundle);
                mContext.setResult(Activity.RESULT_OK, intent);
                //finis this activity
                mContext.finish();
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


    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.inspirationImage)
        SimpleDraweeView imageInspiration;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //ItemViewDetailsHolder class
    static class ItemViewHolderDetail extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.textCreatorName)
        AppCompatTextView textCreatorName;
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageInspiration)
        SimpleDraweeView imageInspiration;
        @BindView(R.id.live_filter_bubble)
        GravView liveFilterBubble;
        @BindView(R.id.whether_view)
        WeatherView whetherView;
        @BindView(R.id.konfetti_view)
        KonfettiView konfettiView;

        public ItemViewHolderDetail(View itemView) {
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
