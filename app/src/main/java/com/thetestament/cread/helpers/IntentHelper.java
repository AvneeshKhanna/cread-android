package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.activities.BottomNavigationActivity;
import com.thetestament.cread.activities.ChatDetailsActivity;
import com.thetestament.cread.activities.ContentPreview;
import com.thetestament.cread.activities.FollowActivity;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.activities.WebViewActivity;
import com.thetestament.cread.utils.Constant;

import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_WITH_US;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_REQUESTED_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_TITLE;
import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_URL;

/**
 * A helper class to provide utility method for intent related operations.
 */

public class IntentHelper {

    /**
     * Method to open ContentPreview activity.
     *
     * @param context       Context to use.
     * @param imageWidth    Width of image.
     * @param imageHeight   Height of image.
     * @param imageUrl      Url of the image.
     * @param signatureText Signature text of the user.
     */
    public static void openContentPreviewActivity(Context context, int imageWidth, int imageHeight, String imageUrl, String signatureText) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.CONTENT_PREVIEW_EXTRA_IMAGE_WIDTH, imageWidth);
        bundle.putInt(Constant.CONTENT_PREVIEW_EXTRA_IMAGE_HEIGHT, imageHeight);
        bundle.putString(Constant.CONTENT_PREVIEW_EXTRA_IMAGE_URL, imageUrl);
        bundle.putString(Constant.CONTENT_PREVIEW_EXTRA_SIGNATURE_TEXT, signatureText);

        Intent intent = new Intent(context, ContentPreview.class);
        intent.putExtra(Constant.CONTENT_PREVIEW_EXTRA_DATA, bundle);
        context.startActivity(intent);
    }

    /**
     * Method to open FollowActivity screen.
     *
     * @param context       Context to use.
     * @param requestedUUID UUID of the user.
     * @param followType    'following'/'followers'
     */
    public static void openFollowActivity(Context context, String requestedUUID, String followType) {
        Intent intent = new Intent(context, FollowActivity.class);
        intent.putExtra(EXTRA_FOLLOW_REQUESTED_UUID, requestedUUID);
        intent.putExtra(EXTRA_FOLLOW_TYPE, followType);
        context.startActivity(intent);
    }

    /**
     * Method to open ProfileActivity screen.
     *
     * @param context Context to use.
     * @param uuid    UUID of user whose profile to be opened.
     */
    public static void openProfileActivity(Context context, String uuid) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(EXTRA_PROFILE_UUID, uuid);
        context.startActivity(intent);
    }

    /**
     * Method to redirect user to Cread app on google play store.
     *
     * @param context Context to use.
     */
    public static void openPlayStore(Context context) {
        //To get the package name
        String appPackageName = context.getPackageName();
        try {
            //To redirect to google play store
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            //if play store is not installed
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }


    /**
     * Open WebViewActivity screen.
     *
     * @param context   Context to use.
     * @param webUrl    Url to be opened.
     * @param titleText Title text for webViewActivity.
     */
    public static void openWebViewActivity(Context context, String webUrl, String titleText) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_WEB_VIEW_URL, webUrl);
        intent.putExtra(EXTRA_WEB_VIEW_TITLE, titleText);
        context.startActivity(intent);
    }


    /**
     * Method to open chat details screen with Cread kalakaar.
     *
     * @param context Context to use.
     */
    public static void openChatWithCreadKalakaar(FragmentActivity context) {
        //Open ChatDetailsActivity with open
        Intent intent = new Intent(context, ChatDetailsActivity.class);
        //Set bundle data
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CHAT_UUID, BuildConfig.CREAD_KALAKAAR_UUID);
        bundle.putString(EXTRA_CHAT_USER_NAME, "Cread Kalakaar");
        bundle.putString(EXTRA_CHAT_ID, "");
        bundle.putInt(EXTRA_CHAT_ITEM_POSITION, 0);
        bundle.putString(EXTRA_CHAT_DETAILS_CALLED_FROM, EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_WITH_US);

        intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);
        context.startActivity(intent);
    }


    /**
     * Open BottomNavigationActivity screen.
     *
     * @param context Context to use.
     */
    public static void openBottomNavigationctivity(Context context) {
        Intent intent = new Intent(context, BottomNavigationActivity.class);
        context.startActivity(intent);
    }

}
