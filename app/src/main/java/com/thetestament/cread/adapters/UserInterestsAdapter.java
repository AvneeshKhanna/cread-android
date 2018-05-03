package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnInterestsLoadMoreListener;
import com.thetestament.cread.models.UserInterestsModel;
import com.thetestament.cread.widgets.SquareView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by prakharchandna on 02/05/18.
 */

public class UserInterestsAdapter extends RecyclerView.Adapter {

    private FragmentActivity mContext;
    private ArrayList<UserInterestsModel> mInterestsList;
    private OnInterestsLoadMoreListener mLoadMoreListener;
    private listener.OnInterestClickedListener mInterestClickedListener;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private boolean mIsLoading;

    public UserInterestsAdapter(FragmentActivity mContext, ArrayList<UserInterestsModel> mInterestList) {
        this.mContext = mContext;
        this.mInterestsList = mInterestList;
    }

    public void setLoadMoreInterestsListener(OnInterestsLoadMoreListener mLoadMoreListener) {
        this.mLoadMoreListener = mLoadMoreListener;
    }

    public void setUserInterestClickedListener(listener.OnInterestClickedListener mInterestClickedListener) {
        this.mInterestClickedListener = mInterestClickedListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mInterestsList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.item_user_interest, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        UserInterestsModel data = mInterestsList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //set interest name
            itemViewHolder.textInterestName.setText(data.getInterestName());
            // set shadow
            itemViewHolder.textInterestName.setShadowLayer(3, 3, 3
                    , ContextCompat.getColor(mContext, R.color.color_grey_600));
            // set grid margins
            FeedHelper.setGridItemMargins(mContext, position, itemViewHolder.imageUserInterest);
            //Load interest picture
            ImageHelper.loadImageFromPicasso(mContext, itemViewHolder.imageUserInterest, data.getInterestImageURL(), R.drawable.image_placeholder);
            //Click functionality
            itemViewOnClick(itemViewHolder.itemView, data, position);

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //If last item is visible to user

    }

    @Override
    public int getItemCount() {
        return mInterestsList == null ? 0 : mInterestsList.size();
    }

    /**
     * Sets interest item click action
     *
     * @param view
     * @param data
     * @param position
     */
    private void itemViewOnClick(View view, final UserInterestsModel data, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // invoke callback
                mInterestClickedListener.onInterestClicked(data, position);
            }
        });
    }


    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }


    /**
     * Method to initialize load more listener.
     */
    private void initializeLoadMore(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mInterestsList.size() - 1 && !mIsLoading) {
            if (mLoadMoreListener != null) {
                //Lode more data here
                mLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageUserInterest)
        ImageView imageUserInterest;
        @BindView(R.id.textInterestName)
        TextView textInterestName;
        @BindView(R.id.containerUserInterestText)
        FrameLayout containerUserInterestText;
        @BindView(R.id.imageContainer)
        SquareView imageContainer;

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
