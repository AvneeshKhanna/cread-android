package com.thetestament.cread.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ChatDetailsActivity;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CHANNEL_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE;

/**
 * Class which provides utility methods for personal chat and notifications.
 */

public class NotificationUtil {
    /**
     * Method to play a sound when user receives or send  a message.
     *
     * @param context           Context to use.
     * @param mPreferenceHelper SharedPreferenceHelper reference.
     */
    public static void notifyNewMessage(Context context, SharedPreferenceHelper mPreferenceHelper) {
        //Play sound if its enabled by user
        if (mPreferenceHelper.isChatSoundEnabled()) {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.sound_one);
            //Listener for track completion
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
            //Play sound
            mediaPlayer.start();
        }
    }


    /**
     * Method to launch BottomNavigation screen on back button press.
     *
     * @param activity AppCompatActivity reference
     */
    public static void getNotificationBackButtonBehaviour(AppCompatActivity activity) {
        Intent upIntent = NavUtils.getParentActivityIntent(activity);
        if (NavUtils.shouldUpRecreateTask(activity, upIntent) || activity.isTaskRoot()) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(activity)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(activity, upIntent);
        }
    }

    /**
     * Method to shoot personal chat new incoming message notification.
     *
     * @param message Message to be displayed in notification
     */
    public static void buildNotificationForPersonalChat(final Context context, String fromUUID, String fromName, String chatId, String message, SharedPreferenceHelper helper, final String imageUrl) {

        Intent intent = new Intent(context, ChatDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //Set bundle data
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CHAT_UUID, fromUUID);
        bundle.putString(EXTRA_CHAT_USER_NAME, fromName);
        bundle.putString(EXTRA_CHAT_ID, chatId);
        bundle.putInt(EXTRA_CHAT_ITEM_POSITION, 0);
        bundle.putString(EXTRA_CHAT_DETAILS_CALLED_FROM, EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION);
        intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);
        //Update flags
        GET_RESPONSE_FROM_NETWORK_CHAT_LIST = true;
        GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS = true;
        //set personal chat indicator status
        helper.setPersonalChatIndicatorStatus(true);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(context);
        }

        // create RemoteView
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification_personal_chat);
        remoteViews.setImageViewResource(R.id.imageUser, R.drawable.ic_account_circle_48);
        remoteViews.setTextViewText(R.id.textUserName, fromName);
        remoteViews.setTextViewText(R.id.textUserMessage, message);
        remoteViews.setTextColor(R.id.textUserName, ContextCompat.getColor(context, R.color.grey_dark));
        remoteViews.setTextColor(R.id.textUserMessage, ContextCompat.getColor(context, R.color.black_overlay));

        final NotificationCompat.Builder mNotification =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_GENERAL)
                        .setContentTitle(fromName)
                        .setSmallIcon(R.drawable.ic_stat_cread_logo)
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContent(remoteViews)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(IMPORTANCE_HIGH);


        // To push notification from background thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Picasso
                        .with(context)
                        .load(imageUrl)
                        .error(R.drawable.ic_account_circle_48)
                        .into(remoteViews, R.id.imageUser, NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE, mNotification.build());
            }
        });

        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE, mNotification.build());
    }

    /**
     * Creates a notification channel for personal chat
     */
    @TargetApi(26)
    private static void createNotificationChannel(Context context) {
        // creating notification channel
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        // The user-visible name of the channel.
        CharSequence name = context.getString(R.string.gen_notif_channel_name);
        // The user-visible description of the channel.
        String description = context.getString(R.string.gen_notif_channel_desc);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_GENERAL, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);

        mNotificationManager.createNotificationChannel(mChannel);
    }

}
