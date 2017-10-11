package com.thetestament.cread.adapters;


import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;
import com.thetestament.cread.models.ExploreModel;

import java.util.List;

import butterknife.ButterKnife;

public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<ExploreModel> mExploreList;
    private FragmentActivity mContext;
    private boolean mIsLoading;


    public ExploreAdapter(List<ExploreModel> mExploreList, FragmentActivity mContext) {
        this.mExploreList = mExploreList;
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        return mExploreList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new FeedAdapter.ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_explore, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new FeedAdapter.LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_explore_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //LoadingViewHolder class
    static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}