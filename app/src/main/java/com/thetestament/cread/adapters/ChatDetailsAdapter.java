package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
    private final int VIEW_TYPE_HEADER = 2;

    public static final String VIEW_TYPE_MESSAGE_SENT_VALUE = "messageSent";
    public static final String VIEW_TYPE_MESSAGE_RECEIVED_VALUE = "messageReceived";
    public static final String VIEW_TYPE_MESSAGE_HEADER_VALUE = "messageHeader";

    private List<ChatDetailsModel> mChatDetailsList;
    private FragmentActivity mContext;


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

        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        /*if (mChatDetailsList.get(position)
                .getChatUserType().equals(VIEW_TYPE_MESSAGE_HEADER_VALUE)) {
            return VIEW_TYPE_HEADER;

        } */
        else {
            return mChatDetailsList.get(position-1)
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
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_chat_details_header, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT) {
            ChatDetailsModel data = mChatDetailsList.get(position-1 );
            MessageSentViewHolder sentViewHolder = (MessageSentViewHolder) holder;
            //set text here
            sentViewHolder.textSent.setText(data.getMessage());
        } else if (holder.getItemViewType() == VIEW_TYPE_MESSAGE_RECEIVED) {
            ChatDetailsModel data = mChatDetailsList.get(position-1 );
            MessageReceivedViewHolder receivedViewHolder = (MessageReceivedViewHolder) holder;
            //set text here
            receivedViewHolder.textReceived.setText(data.getMessage());
        } else if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            //Click functionality
            headerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Set listener
                    loadMoreListener.onLoadMore();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mChatDetailsList.size()+1;
    }

    /**
     * Method to update load more view visibility.
     *
     * @param holder     HeaderViewHolder reference
     * @param visibility Visibility flags i.e GONE , VISIBLE, INVISIBLE
     */
    public void setLoadMoreViewVisibility(HeaderViewHolder holder, int visibility) {
        holder.loadMoreView.setVisibility(visibility);
    }

    /**
     * Method to update progress view visibility.
     *
     * @param holder     HeaderViewHolder reference
     * @param visibility Visibility flags i.e GONE , VISIBLE, INVISIBLE
     */
    public void setLoadingIconVisibility(HeaderViewHolder holder, int visibility) {
        holder.loadMoreViewProgress.setVisibility(visibility);
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

    //Header ViewHolder class
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.loadMoreView)
        LinearLayout loadMoreView;
        @BindView(R.id.loadMoreViewProgress)
        View loadMoreViewProgress;

        public HeaderViewHolder(View headerView) {
            super(headerView);
            ButterKnife.bind(this, headerView);
        }
    }

}
