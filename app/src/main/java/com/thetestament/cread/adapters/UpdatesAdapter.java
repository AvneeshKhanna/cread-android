package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.database.NotificationsDBFunctions;
import com.thetestament.cread.models.UpdatesModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_HATSOFF;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_TOP_POST;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_POST;


/*Adapter class for UpdatesFragment RecyclerView.*/


public class UpdatesAdapter extends RecyclerView.Adapter<UpdatesAdapter.ViewHolder> {

    private NotificationItemClick notificationItemClick;

    private List<UpdatesModel> mUpdatesDataList;
    private FragmentActivity mContext;


    /**
     * Interface definition for a callback to be invoked when a notification is clicked.
     */
    public interface NotificationItemClick {
        void onNotificationClick(String notificationType, String shareID);
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
    public UpdatesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_updates, parent, false));
    }

    @Override
    public void onBindViewHolder(final UpdatesAdapter.ViewHolder holder, int position) {
        final UpdatesModel updatesData = mUpdatesDataList.get(position);

        holder.textDescription.setText(updatesData.getMessage());
        holder.textTimestamp.setText(updatesData.getTimeStamp());
        holder._id = updatesData.get_ID();

        // set image url depending on the notification category
        String imageUrl = updatesData.getCategory()
                .equals(NOTIFICATION_CATEGORY_CREAD_TOP_POST)
                ? updatesData.getEntityImage() : updatesData.getActorImage();

        loadImage(imageUrl, holder.imgNotification);

        //Change notification item color depending upon seen status
        if (updatesData.getSeen().equals("true")) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.highlight_color));
        }

        //Notifications onClick functionality
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Background task to update seen status
                new UpdateSeenStatus().execute(holder);
                initClick(updatesData.getCategory(), updatesData.getEntityID(), updatesData.getActorID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUpdatesDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_timestamp)
        TextView textTimestamp;
        @BindView(R.id.text_description)
        TextView textDescription;
        @BindView(R.id.img_updates)
        CircleImageView imgNotification;
        int _id;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    /**
     * AsyncTask to update the notification seen status.
     */
    class UpdateSeenStatus extends AsyncTask<ViewHolder, Void, ViewHolder> {
        @Override
        protected ViewHolder doInBackground(ViewHolder... holder) {

            NotificationsDBFunctions notificationsDBFunctions
                    = new NotificationsDBFunctions((AppCompatActivity) mContext);
            notificationsDBFunctions.accessNotificationsDatabase();
            notificationsDBFunctions.setSeen(holder[0]._id);
            return holder[0];
        }

        @Override
        protected void onPostExecute(ViewHolder holder) {
            //set item visibility to transparent
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }


    /**
     * Open screen depending on the type of the notification.
     *
     * @param category Notification category
     * @param entityID entity id.
     * @param userID   userID
     */

    private void initClick(String category, String entityID, String userID) {
        switch (category) {
            case NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                //Launch me fragment
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(EXTRA_PROFILE_UUID, userID);
                mContext.startActivity(intent);
                //Finish Updates activity
                mContext.finish();
                break;

            case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_COLLABORATE, entityID);
                break;

            case NOTIFICATION_CATEGORY_CREAD_HATSOFF:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_HATSOFF, entityID);
                break;

            case NOTIFICATION_CATEGORY_CREAD_COMMENT:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_COMMENT, entityID);
                break;

            case NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER, entityID);
                break;

            case NOTIFICATION_CATEGORY_CREAD_BUY:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_BUY, entityID);
                break;
            case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                //Listener
                // do nothing since it is not persistable and not present here
                break;
            case NOTIFICATION_CATEGORY_CREAD_TOP_POST:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_TOP_POST, entityID);
                break;
            case NOTIFICATION_CATEGORY_PROFILE_MENTION_POST:
                //Set Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_PROFILE_MENTION_POST, entityID);
                break;
            case NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT:
                //Set Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT, entityID);
                break;
            default:
                break;
        }
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
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }
}
