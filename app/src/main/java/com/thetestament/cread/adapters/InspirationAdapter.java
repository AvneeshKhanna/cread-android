package com.thetestament.cread.adapters;


import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener.OnInspirationLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnInspirationSelectListener;
import com.thetestament.cread.models.InspirationModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a inspiration RecyclerView.
 */

public class InspirationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<InspirationModel> mInspirationList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnInspirationLoadMoreListener loadMoreListener;
    private OnInspirationSelectListener inspirationSelectListener;

    /**
     * Required constructor.
     *
     * @param mInspirationList List of inspiration data.
     * @param mContext         Context to be use.
     */
    public InspirationAdapter(List<InspirationModel> mInspirationList, FragmentActivity mContext) {
        this.mInspirationList = mInspirationList;
        this.mContext = mContext;
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
        return mInspirationList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_inspiration, parent, false));
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
            loadInspirationImage(data.getCapturePic(), itemViewHolder.imageInspiration);
            //ItemView onClick functionality
            itemViewOnClick(itemViewHolder.itemView, data);

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
     * Method to load inspiration image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadInspirationImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);
    }

    /**
     * ItemView click functionality.
     */
    private void itemViewOnClick(View view, final InspirationModel data) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set listener
                inspirationSelectListener.onInspireImageSelected(data);
            }
        });
    }


    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.inspirationImage)
        ImageView imageInspiration;

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
