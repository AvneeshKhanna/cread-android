package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;
import com.thetestament.cread.models.MemeImageModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Meme RecyclerView i.e.
 */
public class MemeImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //region :Item types
    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_LOADING = 1;
    //endregion

    //region :Fields and constants
    private List<MemeImageModel> mDataList;
    private FragmentActivity mContext;
    private boolean mIsLoading;
    //endregion

    //region :Required constructor

    /**
     * Required constructor.
     *
     * @param dataList List of meme layout data.
     * @param context  Context to be use.
     */
    public MemeImageAdapter(List<MemeImageModel> dataList, FragmentActivity context) {
        this.mDataList = dataList;
        this.mContext = context;
    }
    //endregion

    //region :Listener
    private com.thetestament.cread.listeners.listener.OnMemeImageLoadMoreListener loadMoreListener;
    private com.thetestament.cread.listeners.listener.OnMemeClickListener listener;

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(com.thetestament.cread.listeners.listener.OnMemeImageLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    /**
     * Register a callback to be invoked when user selects meme image.
     */
    public void setListener(com.thetestament.cread.listeners.listener.OnMemeClickListener listener) {
        this.listener = listener;
    }

    //endregion

    //region :Overridden methods

    @Override
    public int getItemViewType(int position) {
        if (mDataList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            if (mDataList.get(position).getType().equals("header")) {
                return VIEW_TYPE_HEADER;
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
                    .inflate(R.layout.item_meme_image, parent, false));
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_meme_header, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MemeImageModel data = mDataList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Method called
            itemClickFunctionality(itemViewHolder.itemView, data, holder.getAdapterPosition(), VIEW_TYPE_ITEM);
        } else if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            //Method called
            itemClickFunctionality(headerViewHolder.itemView
                    , data
                    , holder.getAdapterPosition()
                    , VIEW_TYPE_HEADER);
        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mDataList.size() - 1 && !mIsLoading) {
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
        return mDataList == null ? 0 : mDataList.size();
    }
    //endregion

    //region :ItemView holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_meme)
        AppCompatImageView imgMeme;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }

    //Header viewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
            //Bind view
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

    //region :private methods


    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }


    /**
     * ItemView click functionality.
     *
     * @param view     View wto be clicked.
     * @param data     MemeImageModel data.
     * @param position Position of item in the list.
     */
    private void itemClickFunctionality(View view, final MemeImageModel data, final int position, final int viewType) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set Listener
                listener.onImageSelected(data, position, viewType);
            }
        });
    }

    //endregion
}
