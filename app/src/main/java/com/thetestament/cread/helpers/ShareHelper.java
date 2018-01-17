package com.thetestament.cread.helpers;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.thetestament.cread.R;
import com.thetestament.cread.models.FeedModel;

import static com.thetestament.cread.helpers.ImageHelper.getLocalBitmapUri;

public class ShareHelper {

    /**
     * Method to create intent choose so he/she can share the post.
     *
     * @param bitmap Bitmap to be shared.
     */
    public static void sharePost(Bitmap bitmap, Context context, FeedModel data) {

        String shareText = data.getCaption() == null ?
                context.getString(R.string.text_share_image) :
                data.getCaption();

        //Copy caption text to clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Caption", shareText);
        clipboard.setPrimaryClip(clip);

        //Show toast
        ViewHelper.getToast(context, "Caption copied! Paste before sharing");

        //Create intent chooser
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, context));
        context.startActivity(Intent.createChooser(intent, "Share"));
    }
}
