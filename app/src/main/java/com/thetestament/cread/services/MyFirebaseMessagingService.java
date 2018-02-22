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
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.BottomNavigationActivity;
import com.thetestament.cread.activities.ChatDetailsActivity;
import com.thetestament.cread.activities.UpdatesActivity;
import com.thetestament.cread.fragments.SettingsFragment;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.utils.ImageRoundCorners;
import com.thetestament.cread.utils.NotificationDataSaver;
import com.thetestament.cread.utils.NotificationDataSaver.OnCompleteListener;

import java.util.Map;

import io.smooch.ui.ConversationActivity;

import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_COMMENTS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FOLLOWING;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_HATSOFF;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_INSPIRATION;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_UPDATES;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ACTOR_ID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_CATEGORY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ENTITY_IMAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_MESSAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_OTHER_COLLABORATOR;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_PERSISTABLE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_HATSOFF;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_TEAM_CHAT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_TOP_POST;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PERSONAL_CHAT_MESSAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_POST;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CHANNEL_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_COMMENT_OTHER;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_HATSOFF;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_TEAM_CHAT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_CREAD_TOP_POST;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_PROFILE_MENTION_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_ID_PROFILE_MENTION_POST;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String category, message, entityID, actorUserID, actorUserImage, persistable, entityImage;
    private boolean otherCollaborator = false;
    private int mId = 0;
    private int resId = 0;
    private Map<String, String> data;
    private final String TAG = getClass().getSimpleName();
    private Intent intent;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        data = remoteMessage.getData();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onMessageReceived: " + data);
        }

        category = data.get("category");
        persistable = data.get("persistable");
        message = data.get("message");


        if (!TextUtils.isEmpty(category)) {
            performCategorySpecificOperations();
        }
    }

    /**
     * Method to perform respective operations depending upon the category of notifications.
     */
    private void performCategorySpecificOperations() {

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(getApplicationContext());

        boolean isValidCategory = true;

        switch (category) {

            case NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                mId = NOTIFICATION_ID_CREAD_FOLLOW;
                actorUserID = data.get("actorid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_FOLLOWING = true;
                GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS = true;
                GET_RESPONSE_FROM_NETWORK_ME = true;
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:
                mId = NOTIFICATION_ID_CREAD_COLLABORATE;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_ME = true;
                GET_RESPONSE_FROM_NETWORK_INSPIRATION = true;
                GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS = true;
                GET_RESPONSE_FROM_NETWORK_MAIN = true;
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_CREAD_HATSOFF:
                mId = NOTIFICATION_ID_CREAD_HATSOFF;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                otherCollaborator = Boolean.parseBoolean(data.get("other_collaborator"));
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_HATSOFF = true;
                GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                GET_RESPONSE_FROM_NETWORK_ME = true;
                GET_RESPONSE_FROM_NETWORK_MAIN = true;
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_CREAD_COMMENT:
                mId = NOTIFICATION_ID_CREAD_COMMENT;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                otherCollaborator = Boolean.parseBoolean(data.get("other_collaborator"));
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_COMMENTS = true;
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_CREAD_BUY:
                mId = NOTIFICATION_ID_CREAD_BUY;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                otherCollaborator = Boolean.parseBoolean(data.get("other_collaborator"));
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                mId = NOTIFICATION_ID_CREAD_GENERAL;
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, BottomNavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                break;

            case NOTIFICATION_CATEGORY_CREAD_TEAM_CHAT:
                mId = NOTIFICATION_ID_CREAD_TEAM_CHAT;
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, ConversationActivity.class);
                intent.setFlags(0);
                break;
            case NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER:
                mId = NOTIFICATION_ID_CREAD_COMMENT_OTHER;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_COMMENTS = true;
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_CREAD_TOP_POST:
                mId = NOTIFICATION_ID_CREAD_TOP_POST;
                entityID = data.get("entityid");
                entityImage = data.get("entityimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                break;
            case NOTIFICATION_CATEGORY_PROFILE_MENTION_POST:
                mId = NOTIFICATION_ID_PROFILE_MENTION_POST;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                GET_RESPONSE_FROM_NETWORK_ME = true;
                GET_RESPONSE_FROM_NETWORK_MAIN = true;
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT:
                mId = NOTIFICATION_ID_PROFILE_MENTION_COMMENT;
                entityID = data.get("entityid");
                actorUserImage = data.get("actorimage");
                resId = R.drawable.ic_cread_notification_general;
                intent = new Intent(this, UpdatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                GET_RESPONSE_FROM_NETWORK_COMMENTS = true;
                GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                //set notification indicator status
                spHelper.setNotifIndicatorStatus(true);
                break;
            case NOTIFICATION_CATEGORY_PERSONAL_CHAT_MESSAGE:
                mId = NOTIFICATION_ID_PERSONAL_CHAT_MESSAGE;
                intent = new Intent(this, ChatDetailsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                //Set bundle data
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_CHAT_UUID, data.get("from_uuid"));
                bundle.putString(EXTRA_CHAT_USER_NAME, data.get("from_name"));
                bundle.putString(EXTRA_CHAT_ID, data.get("chatid"));
                bundle.putInt(EXTRA_CHAT_ITEM_POSITION, 0);
                bundle.putString(EXTRA_CHAT_DETAILS_CALLED_FROM, EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION);
                intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);
                //Update flags
                GET_RESPONSE_FROM_NETWORK_CHAT_LIST = true;
                GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS = true;
                //set personal chat indicator status
                spHelper.setPersonalChatIndicatorStatus(true);
                break;
            default:
                isValidCategory = false;
                break;
        }

        //show notification only if valid category
        if (isValidCategory) {
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
                    //If notification is from personal chat
                    if (category.equals(NOTIFICATION_CATEGORY_PERSONAL_CHAT_MESSAGE)) {
                        buildNotificationForPersonalChat(message, mId, intent);
                    } else {
                        buildNotification(message, mId, intent);
                    }
                }
            }
        });

        Bundle data = new Bundle();
        data.putString(NOTIFICATION_BUNDLE_DATA_MESSAGE, message);
        data.putString(NOTIFICATION_BUNDLE_DATA_CATEGORY, category);
        data.putString(NOTIFICATION_BUNDLE_DATA_ACTOR_ID, actorUserID);
        data.putString(NOTIFICATION_BUNDLE_DATA_ENTITY_ID, entityID);
        data.putString(NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE, actorUserImage);
        data.putString(NOTIFICATION_BUNDLE_DATA_ENTITY_IMAGE, entityImage);
        data.putString(NOTIFICATION_BUNDLE_DATA_PERSISTABLE, persistable);
        data.putBoolean(NOTIFICATION_BUNDLE_DATA_OTHER_COLLABORATOR, otherCollaborator);
        nData.save(getApplicationContext(), data);

    }

    /**
     * Method to shoot notification.
     *
     * @param message Message to be displayed in notification
     * @param mId     id for notification*
     */
    private void buildNotification(String message, final int mId, Intent intent) {

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
     * Method to shoot notification.
     *
     * @param message Message to be displayed in notification
     * @param mId     id for notification*
     */
    private void buildNotificationForPersonalChat(String message, final int mId, Intent intent) {

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel();
        }

        // create RemoteView
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification_personal_chat);
        remoteViews.setImageViewResource(R.id.imageUser, R.drawable.ic_account_circle_48);
        remoteViews.setTextViewText(R.id.textUserName, data.get("from_name"));
        remoteViews.setTextViewText(R.id.textUserMessage, message);
        remoteViews.setTextColor(R.id.textUserName, ContextCompat.getColor(getApplicationContext(), R.color.grey_dark));
        remoteViews.setTextColor(R.id.textUserMessage, ContextCompat.getColor(getApplicationContext(), R.color.black_overlay));

        final NotificationCompat.Builder mNotification =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
                        .setContentTitle(data.get("from_name"))
                        .setSmallIcon(R.drawable.ic_stat_cread_logo)
                        .setContentText(message)
                        .setContent(remoteViews)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(IMPORTANCE_HIGH);


        // To push notification from background thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Picasso
                        .with(getApplicationContext())
                        .load(data.get("from_profilepicurl"))
                        .transform(new ImageRoundCorners())
                        .error(R.drawable.ic_account_circle_48)
                        .into(remoteViews, R.id.imageUser, mId, mNotification.build());
            }
        });


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
