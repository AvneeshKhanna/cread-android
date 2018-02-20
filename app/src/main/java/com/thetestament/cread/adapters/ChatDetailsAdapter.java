package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener.OnChatDeleteListener;
import com.thetestament.cread.listeners.listener.OnChatDetailsLoadMoreListener;
import com.thetestament.cread.models.ChatDetailsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a chatDetails RecyclerView.
 */

public class ChatDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_MESSAGE_SENT = 0;
    private final int VIEW_TYPE_MESSAGE_RECEIVED = 1;
    private final int VIEW_TYPE_LOADING = 2;

    public static final String VIEW_TYPE_MESSAGE_SENT_VALUE = "messageSent";
    public static final String VIEW_TYPE_MESSAGE_RECEIVED_VALUE = "messageReceived";

    private List<ChatDetailsModel> mChatDetailsList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnChatDetailsLoadMoreListener loadMoreListener;
    private OnChatDeleteListener chatDeleteListener;

    /**
     * Required constructor.
     *
     * @param chatDetailsList List of chat details data.
     * @param mContext        Context to use.
     */

    public ChatDetailsAdapter(List<ChatDetailsModel> chatDetailsList, FragmentActivity mContext) {
        this.mChatDetailsList = chatDetailsList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(OnChatDetailsLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    /**
     * Register a callback to be invoked when user clicks on delete button.
     */
    public void setChatDeleteListener(OnChatDeleteListener chatDeleteListener) {
        this.chatDeleteListener = chatDeleteListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mChatDetailsList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            return mChatDetailsList.get(position)
                    .getChatUserType().equals(VIEW_TYPE_MESSAGE_SENT_VALUE)
                    ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            return new MessageSentViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_chat_details_sent, parent, false));
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            return new MessageReceivedViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_chat_details_received, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatDetailsModel data = mChatDetailsList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            MessageSentViewHolder sentViewHolder = (MessageSentViewHolder) holder;
            //set text here
            sentViewHolder.textSent.setText(data.getMessage());
        }
        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_RECEIVED) {
            MessageReceivedViewHolder receivedViewHolder = (MessageReceivedViewHolder) holder;
            receivedViewHolder.textReceived.setText(data.getMessage());

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }
        //Method called
        setupLoadMoreListener(position);
    }

    @Override
    public int getItemCount() {
        return mChatDetailsList == null ? 0 : mChatDetailsList.size();
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to setup load more listener
     */
    private void setupLoadMoreListener(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mChatDetailsList.size() - 1 && !mIsLoading) {
            if (loadMoreListener != null) {
                //Lode more data here
                loadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    //MessageSent viewHolder class
    static class MessageSentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textSent)
        TextView textSent;

        public MessageSentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //MessageReceived viewHolder class
    static class MessageReceivedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textReceived)
        TextView textReceived;

        public MessageReceivedViewHolder(View itemView) {
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
