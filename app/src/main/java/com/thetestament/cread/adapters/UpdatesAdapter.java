/*
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

import com.thetestament.cread.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


*/
/**
 * Adapter class for UpdatesFragment RecyclerView.
 *//*

public class UpdatesAdapter extends RecyclerView.Adapter<UpdatesAdapter.ViewHolder> {

    private NotificationItemClick notificationItemClick;

    */
/**
     * Interface definition for a callback to be invoked when a notification is clicked.
     *//*

    public interface NotificationItemClick {
        void onNotificationClick(String notificationType, String shareID);
    }

    */
/**
     * Register a callback to be invoked when user clicks on notification item.
     *//*

    public void setNotificationItemClick(NotificationItemClick notificationItemClick) {
        this.notificationItemClick = notificationItemClick;
    }

    private List<UpdatesData> mUpdatesDataList;
    private FragmentActivity mContext;

    //Constructor
    public UpdatesAdapter(List<UpdatesData> updatesDataList, FragmentActivity context) {
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
        final UpdatesData updatesData = mUpdatesDataList.get(position);

        holder.textDescription.setText(updatesData.getMessage());
        holder.imgNotification.setImageResource(updatesData.getLogo());
        holder.textTimestamp.setText(updatesData.getTimeStamp());
        holder._id = updatesData.get_ID();

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
                initClick(updatesData.getCategory(), updatesData.getCampaignID(), updatesData.getShareID());
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

    */
/**
     * AsyncTask to update the notification seen status.
     *//*

    class UpdateSeenStatus extends AsyncTask<ViewHolder, Void, ViewHolder> {
        @Override
        protected ViewHolder doInBackground(ViewHolder... holder) {

            NotificationsDBFunctions notificationsDBFunctions
                    = new NotificationsDBFunctions((AppCompatActivity) mContext);
            notificationsDBFunctions.accessFormDatabase();
            notificationsDBFunctions.setSeen(holder[0]._id);
            return holder[0];
        }

        @Override
        protected void onPostExecute(ViewHolder holder) {
            //set item visibility to transparent
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    */
/**
     * Open screen depending on the type of the notification.
     *
     * @param category Notification category
     * @param cmId     campaign id.
     * @param shareID  ShareID
     *//*

    private void initClick(String category, String cmId, String shareID) {
        switch (category) {
            case NOTIFICATION_CATEGORY_CREAD_CAMPAIGN_SPECIFIC:
                //Launch Campaign description activity
                Intent intent = new Intent(mContext, CampaignDescriptionActivity.class);
                intent.putExtra(CAMPAIGN_DESC_CALLED_FROM, CAMPAIGN_DESC_FROM_UPDATES);
                intent.putExtra(EXTRA_CAMPAIGN_ID_DATA, cmId);
                mContext.startActivity(intent);
                //Finish Updates activity
                mContext.finish();
                break;

            case NOTIFICATION_CATEGORY_CREAD_SHARE_STATUS:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_SHARE_STATUS, shareID);
                break;

            case NOTIFICATION_CATEGORY_CREAD_CAMPAIGN_UNLOCKED:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_CAMPAIGN_UNLOCKED, shareID);
                break;

            case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_GENERAL, shareID);
                break;

            case NOTIFICATION_CATEGORY_CREAD_TOP_GIVERS:
                //Listener
                notificationItemClick.onNotificationClick(NOTIFICATION_CATEGORY_CREAD_TOP_GIVERS, shareID);
                break;
            default:
                break;
        }
    }
}
*/
