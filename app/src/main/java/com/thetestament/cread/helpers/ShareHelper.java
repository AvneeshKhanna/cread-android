package com.thetestament.cread.helpers;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;

import com.thetestament.cread.R;
import com.thetestament.cread.models.FeedModel;

import java.util.ArrayList;
import java.util.List;

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

        //Obtain uri of image to be shared
        Uri uri = getLocalBitmapUri(bitmap, context);

        List<Intent> targets = new ArrayList<>();
        Intent template = new Intent(Intent.ACTION_SEND);
        template.setType("image/*");

        List<ResolveInfo> candidates = context.getPackageManager().
                queryIntentActivities(template, 0);

        //Exclude cread app
        for (ResolveInfo resolveInfo : candidates) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (!packageName.equals("com.thetestament.cread")) {
                Intent target = new Intent(Intent.ACTION_SEND);
                target.setType("image/*");
                target.putExtra(Intent.EXTRA_STREAM, uri);
                target.setPackage(packageName);
                targets.add(target);
            }
        }

        Intent chooser = Intent.createChooser(targets.remove(0), "Share");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                targets.toArray(new Parcelable[targets.size()]));
        context.startActivity(chooser);


        //Create intent chooser
        /*Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image*//* ");
        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, context));
        context.startActivity(Intent.createChooser(intent, "Share"));*/
    }


    /**
     * Method to create intent choose so he/she can share the post.
     *
     * @param bitmap Bitmap to be shared.
     */
    public static void collabInviteImage(Bitmap bitmap, Context context, String shareText) {


        //Copy caption text to clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Caption", shareText);
        clipboard.setPrimaryClip(clip);

        //Show toast
        ViewHelper.getToast(context, "Caption copied! Paste before sharing");

        //Obtain uri of image to be shared
        Uri uri = getLocalBitmapUri(bitmap, context);

        List<Intent> targets = new ArrayList<>();
        Intent template = new Intent(Intent.ACTION_SEND);
        template.setType("*/*");

        List<ResolveInfo> candidates = context.getPackageManager().
                queryIntentActivities(template, 0);

        //Exclude cread app
        for (ResolveInfo resolveInfo : candidates) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (!packageName.equals("com.thetestament.cread")) {
                Intent target = new Intent(Intent.ACTION_SEND);
                target.setType("*/*");
                target.putExtra(Intent.EXTRA_STREAM, uri);
                target.putExtra(Intent.EXTRA_TEXT, shareText);
                target.setPackage(packageName);
                targets.add(target);
            }
        }

        Intent chooser = Intent.createChooser(targets.remove(0), "Invite");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                targets.toArray(new Parcelable[targets.size()]));
        context.startActivity(chooser);


        //Create intent chooser
        /*Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image*//* ");
        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, context));
        context.startActivity(Intent.createChooser(intent, "Share"));*/
    }

}
