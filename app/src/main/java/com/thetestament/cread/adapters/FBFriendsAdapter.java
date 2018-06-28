package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.listeners.listener.OnFollowFriendsClickedListener;
import com.thetestament.cread.listeners.listener.OnFriendsLoadMoreListener;
import com.thetestament.cread.models.FBFriendsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FBFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<FBFriendsModel> mFriendsList;
    private FragmentActivity mContext;
    private boolean mIsLoading;


    private OnFriendsLoadMoreListener onFriendsLoadMoreListener;
    private OnFollowFriendsClickedListener followFriendsClickedListener;

    /**
     * Constructor
     *
     * @param mFriendsList list of friends
     * @param mContext     Context
     */
    public FBFriendsAdapter(List<FBFriendsModel> mFriendsList, FragmentActivity mContext) {
        this.mFriendsList = mFriendsList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnLoadMoreListener(OnFriendsLoadMoreListener onFriendsLoadMoreListener) {
        this.onFriendsLoadMoreListener = onFriendsLoadMoreListener;
    }

    public void setFollowFriendsClickedListener(OnFollowFriendsClickedListener followFriendsClickedListener) {
        this.followFriendsClickedListener = followFriendsClickedListener;
    }

    @Override
    public int getItemViewType(int position) {

        return mFriendsList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_fb_friends, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        } /*else if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.header_find_friends, parent, false));
        }*/

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            // -1 because of header
            FBFriendsModel data = mFriendsList.get(position);

            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //set user name
            itemViewHolder.textUserName.setText(data.getFirstName() + " " + data.getLastName());
            //Load profile picture
            ImageHelper.loadProgressiveImage(Uri.parse(data.getProfilePicUrl())
                    , itemViewHolder.imageUser);


            itemViewHolder.isFollowing = data.isFollowStatus();
            toggleFollowButton(data.isFollowStatus(), itemViewHolder.buttonFollow);

            followButtonClick(itemViewHolder, data);

            itemViewOnClick(itemViewHolder.itemView, data.getUuid());

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        } /*else if (holder.getItemViewType() == VIEW_TYPE_HEADER) {

            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.friendCount.setText("You have " + mFriendCount + " friends on Cread");
        }
*/

        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mFriendsList.size() - 1 && !mIsLoading) {
            if (onFriendsLoadMoreListener != null) {
                //Lode more data here
                onFriendsLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }


    }

    @Override
    public int getItemCount() {
        return mFriendsList == null ? 0 : mFriendsList.size();
    }


    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageUser)
        SimpleDraweeView imageUser;
        @BindView(R.id.textUserName)
        TextView textUserName;
        @BindView(R.id.buttonFollow)
        TextView buttonFollow;

        //Variable to maintain follow status
        private boolean isFollowing = false;


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

    //HeaderViewHolder class
    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textFriendCount)
        TextView friendCount;


        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
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


    private void followButtonClick(final ItemViewHolder itemViewHolder, final FBFriendsModel data) {
        itemViewHolder.buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                itemViewHolder.isFollowing = !itemViewHolder.isFollowing;
                toggleFollowButton(itemViewHolder.isFollowing, itemViewHolder.buttonFollow);
                data.setFollowStatus(itemViewHolder.isFollowing);

                followFriendsClickedListener.onFollowClicked(itemViewHolder.getAdapterPosition(), data);

            }
        });

    }


    /**
     * Method to toggle follow.
     *
     * @param followStatus true if following false otherwise.
     */
    private void toggleFollowButton(boolean followStatus, TextView followButton) {
        if (followStatus) {
            ViewCompat.setBackground(followButton
                    , ContextCompat.getDrawable(mContext
                            , R.drawable.button_outline));
            followButton.setTextColor(ContextCompat.getColor(mContext
                    , R.color.grey_dark));
            //Change text to 'following'
            followButton.setText("Following");
        } else {
            //Change background
            ViewCompat.setBackground(followButton
                    , ContextCompat.getDrawable(mContext
                            , R.drawable.button_filled));
            //Change text color
            followButton.setTextColor(ContextCompat.getColor(mContext
                    , R.color.white));
            //Change text to 'follow'
            followButton.setText("Follow");
        }
    }
}
