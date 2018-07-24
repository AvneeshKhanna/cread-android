package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.listeners.listener.OnFollowLoadMoreListener;
import com.thetestament.cread.models.FollowModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a follow RecyclerView.
 */
public class FollowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<FollowModel> mFollowList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnFollowLoadMoreListener onFollowLoadMoreListener;

    /**
     * Required constructor.
     *
     * @param mFollowList List of follow  data.
     * @param mContext    Context to use.
     */

    public FollowAdapter(List<FollowModel> mFollowList, FragmentActivity mContext) {
        this.mFollowList = mFollowList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnFollowLoadMoreListener(OnFollowLoadMoreListener onFollowLoadMoreListener) {
        this.onFollowLoadMoreListener = onFollowLoadMoreListener;
    }


    @Override
    public int getItemViewType(int position) {
        return mFollowList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_follow, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FollowModel data = mFollowList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //set user name
            itemViewHolder.textUserName.setText(data.getFirstName() + " " + data.getLastName());
            //Load profile picture
            ImageHelper.loadProgressiveImage(Uri.parse(data.getProfilePicUrl())
                    , itemViewHolder.imageUser);
            //Click functionality
            itemViewOnClick(itemViewHolder.itemView, data.getUuid());
            //If artist is top artist
            if (data.isTopArtist()) {
                //toggle visibility
                itemViewHolder.viewTopArtist.setVisibility(View.VISIBLE);
            } else {
                //toggle visibility
                itemViewHolder.viewTopArtist.setVisibility(View.GONE);
            }

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //If last item is visible to user and new set of data is to yet to be loaded
        initializeLoadMore(position);
    }

    @Override
    public int getItemCount() {
        return mFollowList == null ? 0 : mFollowList.size();
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }


    /**
     * ItemView onClick functionality.
     *
     * @param uuid unique ID of the person whose profile to be opened.
     */
    private void itemViewOnClick(View view, final String uuid) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Method called
                IntentHelper.openProfileActivity(mContext, uuid);
            }
        });
    }


    /**
     * Method to initialize load more listener.
     */
    private void initializeLoadMore(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mFollowList.size() - 1 && !mIsLoading) {
            if (onFollowLoadMoreListener != null) {
                //Lode more data here
                onFollowLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageUser)
        SimpleDraweeView imageUser;
        @BindView(R.id.textUserName)
        TextView textUserName;
        @BindView(R.id.view_top_artist)
        AppCompatImageView viewTopArtist;

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
