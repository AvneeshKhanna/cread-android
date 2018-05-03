package com.thetestament.cread.helpers;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;

import com.thetestament.cread.R;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.Constant;

import java.util.ArrayList;
import java.util.List;

import static com.thetestament.cread.helpers.ImageHelper.getLocalBitmapUri;

public class ShareHelper {

    /**
     * Method to create intent choose so he/she can share the post.
     *
     * @param bitmap Bitmap to be shared.
     */
    public static void sharePost(Bitmap bitmap, Context context, FeedModel data, String deepLink) {

        String shareText = data.getCaption() == null ?
                context.getString(R.string.text_share_image, deepLink) :
                data.getCaption() + "\n\n" + "App: " + deepLink;

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
                queryIntentActivities(template, PackageManager.MATCH_DEFAULT_ONLY);

        //Exclude cread app
        for (ResolveInfo resolveInfo : candidates) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (!packageName.equals("com.thetestament.cread")) {
                Intent target = new Intent(Intent.ACTION_SEND);
                target.setType("*/*");
                target.putExtra(Intent.EXTRA_STREAM, uri);
                target.putExtra(Intent.EXTRA_TEXT, shareText);
                target.setPackage(packageName);
                target.setClassName(packageName, resolveInfo.activityInfo.name);
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
    public static void collabInviteImage(Bitmap bitmap, Context context, String shareText, String shareOption) {


        //Copy caption text to clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Caption", shareText);
        clipboard.setPrimaryClip(clip);

        //Show toast
        ViewHelper.getToast(context, "Caption copied! Paste before sharing");

        //Obtain uri of image to be shared
        Uri uri = getLocalBitmapUri(bitmap, context);


        switch (shareOption) {
            case Constant.SHARE_OPTION_WHATSAPP:


                Intent shareWhatsappIntent = new Intent();
                shareWhatsappIntent.setAction(Intent.ACTION_SEND);
                //Target whatsapp:
                shareWhatsappIntent.setPackage("com.whatsapp");
                //Add text and then Image URI
                shareWhatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                shareWhatsappIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareWhatsappIntent.setType("image/jpeg");
                shareWhatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(shareWhatsappIntent);


                break;

            case Constant.SHARE_OPTION_INSTAGRAM:


                Intent shareInstagramIntent = new Intent(Intent.ACTION_SEND);
                shareInstagramIntent.setPackage("com.instagram.android");
                shareInstagramIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareInstagramIntent.setType("image/*");
                shareInstagramIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(shareInstagramIntent);


                break;

            case Constant.SHARE_OPTION_FACEBOOK:


                Intent shareFacebookIntent = new Intent(Intent.ACTION_SEND);
                shareFacebookIntent.setPackage("com.facebook.katana");
                shareFacebookIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareFacebookIntent.setType("image/*");
                shareFacebookIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(shareFacebookIntent);

                break;


            default:
                List<Intent> targets = new ArrayList<>();
                Intent template = new Intent(Intent.ACTION_SEND);
                template.setType("*/*");

                List<ResolveInfo> candidates = context.getPackageManager().
                        queryIntentActivities(template, PackageManager.MATCH_DEFAULT_ONLY);

                //Exclude cread app
                for (ResolveInfo resolveInfo : candidates) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    if (!packageName.equals("com.thetestament.cread")) {
                        Intent target = new Intent(Intent.ACTION_SEND);
                        target.setType("*/*");
                        target.putExtra(Intent.EXTRA_STREAM, uri);
                        target.putExtra(Intent.EXTRA_TEXT, shareText);
                        target.setPackage(packageName);
                        target.setClassName(packageName, resolveInfo.activityInfo.name);
                        targets.add(target);
                    }
                }

                Intent chooser = Intent.createChooser(targets.remove(0), "Invite");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        targets.toArray(new Parcelable[targets.size()]));
                context.startActivity(chooser);

        }
    }


    /**
     * Method to check the whether app is installed or not and return their status i.e true of false.
     *
     * @param context     Context
     * @param packageName Package name of the application
     * @return Return true if app is installed  and false otherwise
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
