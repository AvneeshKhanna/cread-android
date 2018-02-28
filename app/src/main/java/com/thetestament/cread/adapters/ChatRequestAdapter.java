package com.thetestament.cread.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ChatDetailsActivity;
import com.thetestament.cread.listeners.listener.OnChatRequestLoadMoreListener;
import com.thetestament.cread.models.ChatListModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_REQUEST;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CHAT_DETAILS_FROM_CHAT_REQUEST;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a chat request RecyclerView.
 */

public class ChatRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private List<ChatListModel> mChatRequestList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnChatRequestLoadMoreListener loadMoreListener;

    /**
     * Required constructor.
     *
     * @param chatListModel List of chat request data.
     * @param mContext      Context to use.
     */
    public ChatRequestAdapter(List<ChatListModel> chatListModel, FragmentActivity mContext) {
        this.mChatRequestList = chatListModel;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(OnChatRequestLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mChatRequestList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_chat_request, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatListModel data = mChatRequestList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Load profile picture
            loadProfilePicture(data.getProfileImgUrl(), itemViewHolder.imageUser);
            //set receiver user name
            itemViewHolder.textUserName.setText(data.getReceiverName());
            //set Last message
            itemViewHolder.textLastMessage.setText(data.getLastMessage());

            //Update read indicator
            if (data.getUnreadStatus()) {
                itemViewHolder.textIndicator.setVisibility(View.VISIBLE);
            } else {
                itemViewHolder.textIndicator.setVisibility(View.INVISIBLE);
            }
            //Click functionality
            itemViewOnClick(itemViewHolder.itemView, data, position);

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }
        //Method called
        setupLoadMoreListener(position);
    }

    @Override
    public int getItemCount() {
        return mChatRequestList == null ? 0 : mChatRequestList.size();
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to load profile picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadProfilePicture(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_100)
                .into(imageView);
    }

    /**
     * ItemView onClick functionality.
     *
     * @param data     DataList of selected item
     * @param position Position of item in list
     */
    private void itemViewOnClick(View view, final ChatListModel data, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open ChatDetailsActivity
                Intent intent = new Intent(mContext, ChatDetailsActivity.class);
                //Set bundle data
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_CHAT_UUID, data.getReceiverUUID());
                bundle.putString(EXTRA_CHAT_USER_NAME, data.getReceiverName());
                bundle.putString(EXTRA_CHAT_ID, data.getChatID());
                bundle.putInt(EXTRA_CHAT_ITEM_POSITION, position);
                bundle.putString(EXTRA_CHAT_DETAILS_CALLED_FROM, EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_REQUEST);

                intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);

                mContext.startActivityForResult(intent, REQUEST_CODE_CHAT_DETAILS_FROM_CHAT_REQUEST);

                //Update read status of item
                if (data.getUnreadStatus()) {
                    data.setUnreadStatus(false);
                    notifyItemChanged(position);
                }
            }
        });
    }


    /**
     * Method to setup load more listener
     */
    private void setupLoadMoreListener(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mChatRequestList.size() - 1 && !mIsLoading) {
            if (loadMoreListener != null) {
                //Lode more data here
                loadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageUser)
        CircleImageView imageUser;
        @BindView(R.id.textUserName)
        TextView textUserName;
        @BindView(R.id.textLastMessage)
        TextView textLastMessage;
        @BindView(R.id.textIndicator)
        TextView textIndicator;

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
