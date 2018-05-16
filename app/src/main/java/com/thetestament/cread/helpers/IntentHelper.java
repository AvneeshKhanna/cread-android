package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.thetestament.cread.activities.ContentPreview;
import com.thetestament.cread.activities.FollowActivity;
import com.thetestament.cread.utils.Constant;

import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_REQUESTED_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_TYPE;

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
}
