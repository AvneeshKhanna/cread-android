package com.thetestament.cread.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.BottomNavigationActivity;
import com.thetestament.cread.activities.UpdatesActivity;
import com.thetestament.cread.fragments.SettingsFragment;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.NotificationDataSaver;
import com.thetestament.cread.utils.NotificationDataSaver.OnCompleteListener;

import java.util.Map;

import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ACTOR_ID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_CATEGORY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_MESSAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_PERSISTABLE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_HATSOFF;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CHANNEL_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_HATSOFF;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String category, message, entityID, actorUserID, actorUserImage, persistable;
    private int mId = 0;
    private int resId = 0;
    private Map<String, String> data;
    private final String TAG = getClass().getSimpleName();
    private Intent intent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        data = remoteMessage.getData();
        Log.d(TAG, "onMessageReceived: " + data);

        category = data.get("category");
        persistable = data.get("persistable");
        message = data.get("message");


        performCategorySpecificOperations();
    }

    /**
     * Method to perform respective operations depending upon the category of notifications.
     */
    private void performCategorySpecificOperations() {

        boolean isValidCategory = true;

        switch (category) {

            case Constant.NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                mId = NOTIFICATION_ID_CREAD_FOLLOW;
                actorUserID = data.get("actorid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:
                mId = NOTIFICATION_ID_CREAD_COLLABORATE;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NOTIFICATION_CATEGORY_CREAD_HATSOFF:
                mId = NOTIFICATION_ID_CREAD_HATSOFF;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NOTIFICATION_CATEGORY_CREAD_COMMENT:
                mId = NOTIFICATION_ID_CREAD_COMMENT;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NOTIFICATION_CATEGORY_CREAD_BUY:
                mId = NOTIFICATION_ID_CREAD_BUY;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                mId = NOTIFICATION_ID_CREAD_GENERAL;
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, BottomNavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;
            default:
                isValidCategory = false;
                break;
        }

        //show notification only if valid category
        if(isValidCategory)
        {
            initialiseNotification();
        }
    }


    /**
     * Method to save notification data into local DB for notification panel and shooting a notification
     * after a callback.
     */
    private void initialiseNotification() {

        NotificationDataSaver nData = new NotificationDataSaver();

        nData.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                //Suppress notifications if they are off according to Settings config
                SharedPreferences defaultSharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(MyFirebaseMessagingService.this);
                Boolean key_settings_notifications = defaultSharedPreferences
                        .getBoolean(SettingsFragment.KEY_SETTINGS_NOTIFICATIONS, true);

                if (!key_settings_notifications) {
                } else {
                    buildNotification(message, mId, intent);
                }
            }
        });

        Bundle data = new Bundle();
        data.putString(NOTIFICATION_BUNDLE_DATA_MESSAGE, message);
        data.putString(NOTIFICATION_BUNDLE_DATA_CATEGORY, category);
        data.putString(NOTIFICATION_BUNDLE_DATA_ACTOR_ID, actorUserID);
        data.putString(NOTIFICATION_BUNDLE_DATA_ENTITY_ID, entityID);
        data.putString(NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE, actorUserImage);
        data.putString(NOTIFICATION_BUNDLE_DATA_PERSISTABLE, persistable);
        nData.save(getApplicationContext(), data);
    }

    /**
     * Method to shoot notification.
     *
     * @param message Message to be displayed in notification
     * @param mId     id for notification*
     */
    private void buildNotification(String message, int mId, Intent intent) {

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
        }


        NotificationCompat.Builder mNotification =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
                        .setContentTitle(getString(R.string.app_name))
                        .setSmallIcon(R.drawable.ic_stat_cread_logo)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                resId))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(IMPORTANCE_HIGH);


        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(mId, mNotification.build());
    }


    /**
     * creates a notification channel
     */
    @TargetApi(26)
    private void createNotificationChannel() {
        // creating notification channel
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // The user-visible name of the channel.
        CharSequence name = getString(R.string.gen_notif_channel_name);
        // The user-visible description of the channel.
        String description = getString(R.string.gen_notif_channel_desc);
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
