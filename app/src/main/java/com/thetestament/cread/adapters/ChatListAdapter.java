package com.thetestament.cread.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ChatDetailsActivity;
import com.thetestament.cread.activities.ChatRequestActivity;
import com.thetestament.cread.listeners.listener.OnChatListLoadMoreListener;
import com.thetestament.cread.models.ChatListModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CHAT_DETAILS;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a chatList RecyclerView.
 */

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private List<ChatListModel> mChatList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnChatListLoadMoreListener loadMoreListener;

    /**
     * Required constructor.
     *
     * @param chatListModel List of chat list data.
     * @param mContext      Context to use.
     */
    public ChatListAdapter(List<ChatListModel> chatListModel, FragmentActivity mContext) {
        this.mChatList = chatListModel;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(OnChatListLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mChatList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            return mChatList.get(position).getItemType() == VIEW_TYPE_ITEM
                    ? VIEW_TYPE_ITEM : VIEW_TYPE_HEADER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_chat_list, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_chat_list_header, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatListModel data = mChatList.get(position);

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
        } else if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            //Set request count text
            headerViewHolder.textRequestCount.setText(data.getLastMessage());
            //Click functionality
            headerViewOnClick(headerViewHolder.itemView);
        }

        //Method called
        setupLoadMoreListener(position);
    }

    @Override
    public int getItemCount() {
        return mChatList == null ? 0 : mChatList.size();
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
                .error(R.drawable.ic_account_circle_48)
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
                //Intent intent = new Intent(mContext, ChatRequestActivity.class);
                //Set bundle data
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_CHAT_UUID, data.getReceiverUUID());
                bundle.putString(EXTRA_CHAT_USER_NAME, data.getReceiverName());
                bundle.putString(EXTRA_CHAT_ID, data.getChatID());
                bundle.putInt(EXTRA_CHAT_ITEM_POSITION, position);

                intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);

                mContext.startActivityForResult(intent, REQUEST_CODE_CHAT_DETAILS);

                //Update read status of item
                if (data.getUnreadStatus()) {
                    data.setUnreadStatus(false);
                    notifyItemChanged(position);
                }
            }
        });
    }

    /**
     * ItemView onClick functionality.
     *
     * @param view View to be clicked
     */
    private void headerViewOnClick(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open ChatRequestActivity
                Intent intent = new Intent(mContext, ChatRequestActivity.class);
                mContext.startActivity(intent);
            }
        });
    }


    /**
     * Method to setup load more listener
     */
    private void setupLoadMoreListener(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mChatList.size() - 1 && !mIsLoading) {
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

    //HeaderViewHolder class
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textRequestCount)
        AppCompatTextView textRequestCount;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
