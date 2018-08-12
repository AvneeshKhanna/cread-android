package com.thetestament.cread.helpers;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.Constant;

import java.util.ArrayList;
import java.util.List;

import static com.thetestament.cread.helpers.ImageHelper.getLocalBitmapUri;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_FACEBOOK;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_INSTAGRAM;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_WHATSAPP;

/**
 * Helper class to provide utility methods related to share operation.
 */
public class ShareHelper {

    /**
     * Method to create intent choose so he/she can share the post.
     *
     * @param bitmap Bitmap to be shared.
     */
    public static void sharePost(Bitmap bitmap, Context context, String shareText, String shareOption) {
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

                Intent chooser = Intent.createChooser(targets.remove(0), "Share");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        targets.toArray(new Parcelable[targets.size()]));
                context.startActivity(chooser);

        }
    }


    /**
     * Method to check the whether app is installed or not and return their status i.e true or false.
     *
     * @param context     Context to use.
     * @param packageName Package name of the application.
     * @return Return true if app is installed and false otherwise.
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    /**
     * Content sharing functionality on click of share button.
     *
     * @param context            Context to use.
     * @param data               FeedModal data.
     * @param onGifShareListener OnGifShareListener reference.
     * @param onShareListener    OnShareListener reference.
     * @param logoWhatsApp       WhatsApp logo view.
     * @param logoFaceBook       Facebook logo view.
     * @param logoInstagram      Instagram logo view.
     * @param logoMore           Moro logo view.
     * @param frameLayout        Parent of main content.
     * @param waterMarkCreadView Cread watermark view.
     */
    public static void shareOnClick(final FragmentActivity context, final FeedModel data
            , final listener.OnGifShareListener onGifShareListener, final listener.OnShareListener onShareListener
            , View logoWhatsApp, View logoFaceBook, View logoInstagram, View logoMore
            , final FrameLayout frameLayout, final RelativeLayout waterMarkCreadView) {

        //WhatsApp click functionality
        logoWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If app is installed
                if (ShareHelper.isAppInstalled(context, Constant.PACKAGE_NAME_WHATSAPP)) {
                    //If live filter is present
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(frameLayout, SHARE_OPTION_WHATSAPP
                                , waterMarkCreadView, data.getLiveFilterName());
                    } else {
                        //Load bitmap for sharing
                        loadBitmapForSharing(context, data, SHARE_OPTION_WHATSAPP, onShareListener);
                    }
                }
                //App in not installed
                else {
                    //Show error msg
                    ViewHelper.getToast(context, context.getString(R.string.error_no_whats_app));
                }
            }
        });

        //Facebook click functionality
        logoFaceBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If app is installed
                if (ShareHelper.isAppInstalled(context, Constant.PACKAGE_NAME_FACEBOOK)) {
                    //If live filter is present
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(frameLayout, SHARE_OPTION_FACEBOOK
                                , waterMarkCreadView, data.getLiveFilterName());
                    } else {
                        //Load bitmap for sharing
                        loadBitmapForSharing(context, data, SHARE_OPTION_FACEBOOK, onShareListener);
                    }
                }
                //App in not installed
                else {
                    //Show error msg
                    ViewHelper.getToast(context,
                            context.getString(R.string.error_no_facebook));
                }
            }
        });

        //Instagram click functionality
        logoInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If app is installed
                if (ShareHelper.isAppInstalled(context, Constant.PACKAGE_NAME_INSTAGRAM)) {
                    //If live filter is present
                    if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                        onGifShareListener.onGifShareClick(frameLayout, SHARE_OPTION_INSTAGRAM
                                , waterMarkCreadView, data.getLiveFilterName());
                    } else {
                        //Load bitmap for sharing
                        loadBitmapForSharing(context, data, SHARE_OPTION_INSTAGRAM, onShareListener);
                    }
                }
                //App in not installed
                else {
                    //Show error msg
                    ViewHelper.getToast(context, context.getString(R.string.error_no_instagram));
                }

            }
        });
        //More btn click functionality
        logoMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GifHelper.hasLiveFilter(data.getLiveFilterName())) {
                    onGifShareListener.onGifShareClick(frameLayout, SHARE_OPTION_OTHER
                            , waterMarkCreadView, data.getLiveFilterName());
                } else {
                    loadBitmapForSharing(context, data, SHARE_OPTION_OTHER, onShareListener);
                }
            }
        });


    }


    /**
     * Method to load bitmap image to be shared.
     *
     * @param context         Context to use.
     * @param data            FeedModel data.
     * @param shareOption     {@link com.thetestament.cread.utils.Constant#SHARE_OPTION_WHATSAPP}
     *                        {@link com.thetestament.cread.utils.Constant#SHARE_OPTION_FACEBOOK}
     *                        {@link com.thetestament.cread.utils.Constant#SHARE_OPTION_INSTAGRAM}
     *                        {@link com.thetestament.cread.utils.Constant#SHARE_OPTION_OTHER}
     * @param onShareListener OnShareListener reference.
     */
    private static void loadBitmapForSharing(final FragmentActivity context, final FeedModel data
            , final String shareOption, final listener.OnShareListener onShareListener) {

        Picasso.with(context).load(data.getContentImage()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //Set listener
                onShareListener.onShareClick(bitmap, data, shareOption);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ViewHelper.getToast(context, context.getString(R.string.error_msg_internal));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

}
