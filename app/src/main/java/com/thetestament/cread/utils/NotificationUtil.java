package com.thetestament.cread.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
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
        boolean fi = NavUtils.shouldUpRecreateTask(activity, upIntent);
        boolean fo =activity.isTaskRoot();
        if (fi ||fo){
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(activity)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else{
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            TaskStackBuilder.create(activity)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();

        }
    }

    /**
     * Method to shoot personal chat new incoming message notification.
     *
     * @param message Message to be displayed in notification
     */
    public static void buildNotificationForPersonalChat(final Context context
            , String fromUUID, final String fromName
            , String chatId, final String message
            , SharedPreferenceHelper helper, final String imageUrl) {

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

        final PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(context);
        }

        Picasso.with(context)
                .load(imageUrl)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        //Method called
                        launchNotification(context, fromName, message, bitmap, pendingIntent);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        //Method called
                        launchNotification(context, fromName, message, R.drawable.ic_cread_notification_general, pendingIntent);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

    }

    /**
     * Method to shoot personal chat new incoming message notification.
     *
     * @param message Message to be displayed in notification
     */
    public static void buildNotificationForPersonalChat(final Context context
            , final String fromName
            , final String message
            , Intent intent, String imageUrl) {

        final PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(context);
        }

        Picasso.with(context)
                .load(imageUrl)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        //Method called
                        launchNotification(context, fromName, message, bitmap, pendingIntent);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        //Method called
                        launchNotification(context, fromName, message, R.drawable.ic_cread_notification_general, pendingIntent);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

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


    /**
     * OverLoaded method to launch notification with.
     *
     * @param context       Context to use.
     * @param fromName      Name of the sender.
     * @param message       Message to be displayed in notification.
     * @param bitmap        Bitmap of sender
     * @param pendingIntent Pending intent.
     */
    private static void launchNotification(final Context context
            , final String fromName
            , final String message
            , Bitmap bitmap
            , PendingIntent pendingIntent) {

        final NotificationCompat.Builder mNotification =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_GENERAL)
                        .setContentTitle(fromName)
                        .setSmallIcon(R.drawable.ic_stat_cread_logo)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setLargeIcon(bitmap)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(IMPORTANCE_HIGH);

        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE, mNotification.build());
    }

    /**
     * OverLoaded method to launch notification with.
     *
     * @param context       Context to use.
     * @param fromName      Name of the sender.
     * @param message       Message to be displayed in notification.
     * @param drawableID    Resource ID of error drawable
     * @param pendingIntent Pending intent.
     */
    private static void launchNotification(final Context context
            , final String fromName
            , final String message
            , int drawableID
            , PendingIntent pendingIntent) {

        final NotificationCompat.Builder mNotification =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_GENERAL)
                        .setContentTitle(fromName)
                        .setSmallIcon(R.drawable.ic_stat_cread_logo)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                drawableID))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(IMPORTANCE_HIGH);


        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE, mNotification.build());
    }

}
