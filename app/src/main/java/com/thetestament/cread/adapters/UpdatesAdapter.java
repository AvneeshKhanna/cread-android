package com.thetestament.cread.adapters;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.NotificationItemClick;
import com.thetestament.cread.models.UpdatesModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FB_FRIEND;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_HATSOFF;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_TOP_POST;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_POST;
import static com.thetestament.cread.utils.TimeUtils.getCustomTime;


/*Adapter class for UpdatesFragment RecyclerView.*/


public class UpdatesAdapter extends RecyclerView.Adapter {

    private NotificationItemClick notificationItemClick;

    private List<UpdatesModel> mUpdatesDataList;
    private FragmentActivity mContext;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private boolean mIsLoading;
    private listener.onNotificationsLoadMore onNotificationsLoadMore;


    public void setNotificationsLoadMoreListener(listener.onNotificationsLoadMore onNotificationsLoadMore) {
        this.onNotificationsLoadMore = onNotificationsLoadMore;
    }


    /**
     * Register a callback to be invoked when user clicks on notification item.
     */
    public void setNotificationItemClick(NotificationItemClick notificationItemClick) {
        this.notificationItemClick = notificationItemClick;
    }


    //Constructor
    public UpdatesAdapter(List<UpdatesModel> updatesDataList, FragmentActivity context) {
        this.mUpdatesDataList = updatesDataList;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_updates, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }


    @Override
    public int getItemViewType(int position) {
        return mUpdatesDataList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final UpdatesModel updatesData = mUpdatesDataList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            // parsing server date
            List<String> dateList = getCustomTime(updatesData.getTimeStamp());
            String timeStamp = dateList.get(1) + " " + dateList.get(0) + " at " + dateList.get(3);
            // set timestamp
            itemViewHolder.textTimestamp.setText(timeStamp);

            //set message
            itemViewHolder.textDescription.setText(getNotifMessage(updatesData));

            // set image url depending on the notification category
            String imageUrl = updatesData.getCategory()
                    .equals(NOTIFICATION_CATEGORY_CREAD_TOP_POST)
                    ? updatesData.getEntityImage() : updatesData.getActorImage();

            loadImage(imageUrl, itemViewHolder.imgNotification);

            //Change notification item color depending upon seen status
            initBackgroundColor(itemViewHolder, updatesData);

            //init notification item click
            initClick(itemViewHolder, updatesData, position);


        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mUpdatesDataList.size() - 1 && !mIsLoading) {
            if (onNotificationsLoadMore != null) {
                //Lode more data here
                onNotificationsLoadMore.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    @Override
    public int getItemCount() {
        return mUpdatesDataList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_timestamp)
        TextView textTimestamp;
        @BindView(R.id.text_description)
        TextView textDescription;
        @BindView(R.id.img_updates)
        CircleImageView imgNotification;
        int _id;

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

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Open screen depending on the type of the notification.
     *
     * @param itemViewHolder
     * @param updatesModel
     */

    private void initClick(final ItemViewHolder itemViewHolder, final UpdatesModel updatesModel, final int position) {

        //Notifications onClick functionality
        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatesModel.setUnread(false);
                initBackgroundColor(itemViewHolder, updatesModel);
                notificationItemClick.onNotificationClick(updatesModel, position);
            }
        });
    }

    /**
     * Method to load item image.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadImage(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_100)
                .into(imageView);
    }


    private void initBackgroundColor(ItemViewHolder itemViewHolder, UpdatesModel updatesData) {
        if (!updatesData.isUnread()) {
            itemViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            itemViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.highlight_color));
        }
    }


    /**
     * Creates the notification message according to the category of the notification
     *
     * @param updatesModel
     * @return
     */
    private String getNotifMessage(UpdatesModel updatesModel) {
        String message = "";

        switch (updatesModel.getCategory()) {
            case NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                message = updatesModel.getActorName() + " has started following you on Cread";
                break;

            case NOTIFICATION_CATEGORY_CREAD_BUY:
                message = updatesModel.isOtherCollaborator() ?
                        updatesModel.getActorName() + " has purchased a "
                                + updatesModel.getProductType().replaceAll("_", " ").toLowerCase() + " created using a post inspired by yours"
                        : updatesModel.getActorName() + " has purchased a " + updatesModel.getProductType().replaceAll("_", " ").toLowerCase()
                        + " created using your post";
                break;
            case NOTIFICATION_CATEGORY_CREAD_HATSOFF:
                message = updatesModel.isOtherCollaborator() ?
                        updatesModel.getActorName()
                                + " has given a hats-off to a post which was inspired by yours"
                        : updatesModel.getActorName() + " has given your post a hats-off";
                break;

            case NOTIFICATION_CATEGORY_CREAD_COMMENT:
                message = updatesModel.isOtherCollaborator() ?
                        updatesModel.getActorName()
                                + " has commented on a post inspired by yours"
                        : updatesModel.getActorName() + " has commented on your post";
                break;

            case NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER:
                message = updatesModel.getActorName()
                        + " also commented on a post you commented on";
                break;

            case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:
                message = updatesModel.getContentType().equals(CONTENT_TYPE_CAPTURE)
                        ? updatesModel.getActorName() + " uploaded a graphic art to your writing"
                        : updatesModel.getActorName() + " wrote on your graphic art";
                break;

            case NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT:
                message = updatesModel.getActorName() + " mentioned you in a comment";
                break;
            case NOTIFICATION_CATEGORY_PROFILE_MENTION_POST:
                message = updatesModel.getActorName() + " mentioned you in a post";
                break;


            case NOTIFICATION_CATEGORY_CREAD_FB_FRIEND:
                message = "Your Facebook friend " + updatesModel.getActorName() + " is now on Cread!";
                break;

            default:
        }

        return message;
    }
}
