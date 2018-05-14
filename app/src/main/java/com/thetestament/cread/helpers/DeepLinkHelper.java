package com.thetestament.cread.helpers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnDeepLinkRequestedListener;
import com.thetestament.cread.models.FeedModel;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.NetworkHelper.getEntitySpecificDeepLinkObservable;
import static com.thetestament.cread.helpers.NetworkHelper.getUserSpecificDeepLinkObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.helpers.ShareHelper.isAppInstalled;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_FACEBOOK;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_INSTAGRAM;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_WHATSAPP;
import static com.thetestament.cread.utils.Constant.SHARE_SOURCE_FROM_CREATE;
import static com.thetestament.cread.utils.Constant.SHARE_SOURCE_FROM_SHARE;

/**
 * Created by prakharchandna on 12/03/18.
 */

public class DeepLinkHelper {

    public void getDeepLinkFromServer(final FragmentActivity context, CompositeDisposable compositeDisposable, Observable<JSONObject> deepLinkObservable, final OnDeepLinkRequestedListener onDeepLinkRequestedListener) {


        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Processing")
                .content(context.getString(R.string.waiting_msg))
                .progress(true, 0)
                .show();

        requestServer(compositeDisposable,
                deepLinkObservable,
                context,
                new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        onDeepLinkRequestedListener.onDeepLinkFailiure(context.getString(R.string.error_msg_no_connection), dialog);

                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        dialog.dismiss();

                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {

                                onDeepLinkRequestedListener.onDeepLinkFailiure(context.getString(R.string.error_msg_invalid_token), dialog);
                            }
                            //Token is valid
                            else {

                                JSONObject mainData = jsonObject.getJSONObject("data");
                                String deepLink = mainData.getString("link");

                                onDeepLinkRequestedListener.onDeepLinkSuccess(deepLink, dialog);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            onDeepLinkRequestedListener.onDeepLinkFailiure(context.getString(R.string.error_msg_internal), dialog);

                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        dialog.dismiss();
                        onDeepLinkRequestedListener.onDeepLinkFailiure(context.getString(R.string.error_msg_server), dialog);

                    }

                    @Override
                    public void onCompleteCalled() {
                        // do nothing
                    }
                });
    }


    /**
     * Method to share deep link via Intent
     *
     * @param context context
     * @param text    text to share
     */
    public static void shareDeepLink(FragmentActivity context, String text) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Share Link"));
    }

    /**
     * Checks if the selected sharing option is available and then gets the deep link from the server
     *
     * @param context
     * @param compositeDisposable
     * @param rootView
     * @param uuid
     * @param authkey
     * @param data
     * @param bitmap
     */
    public static void getDeepLinkOnValidShareOption(final FragmentActivity context, CompositeDisposable compositeDisposable, final View rootView, String uuid, String authkey, final FeedModel data, final Bitmap bitmap, final String shareOption) {

        boolean isValidShare = true;

        switch (shareOption) {

            case SHARE_OPTION_WHATSAPP:

                if (isAppInstalled(context, "com.whatsapp")) {


                } else {
                    isValidShare = false;
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Whatsapp installed");
                }
                break;

            case SHARE_OPTION_FACEBOOK:

                if (isAppInstalled(context, "com.facebook.katana")) {


                } else {
                    isValidShare = false;
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Facebook installed");

                }
                break;

            case SHARE_OPTION_INSTAGRAM:
                if (isAppInstalled(context, "com.instagram.android")) {

                } else {
                    isValidShare = false;
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Instagram installed");
                }
                break;

            default:
                break;
        }

        if (isValidShare) {
            DeepLinkHelper deepLinkHelper = new DeepLinkHelper();
            deepLinkHelper.getDeepLinkFromServer(context
                    , compositeDisposable
                    , getEntitySpecificDeepLinkObservable(BuildConfig.URL + "/entity-share-link/generate-dynamic-link",
                            uuid,
                            authkey,
                            data.getEntityID(),
                            data.getContentImage(),
                            data.getCreatorName(),
                            SHARE_SOURCE_FROM_SHARE)
                    , new listener.OnDeepLinkRequestedListener() {
                        @Override
                        public void onDeepLinkSuccess(String deepLink, MaterialDialog dialog) {
                            dialog.dismiss();
                            // share link

                            String shareText = data.getCaption() == null ?
                                    context.getString(R.string.text_share_image, deepLink) :
                                    data.getCaption() + "\n\n" + "App: " + deepLink;

                            ShareHelper.sharePost(bitmap, context, shareText, shareOption);

                        }

                        @Override
                        public void onDeepLinkFailiure(String errorMsg, MaterialDialog dialog) {
                            dialog.dismiss();
                            ViewHelper.getSnackBar(rootView, errorMsg);

                        }
                    });
        }

    }


    /**
     * Method to get the deep link from server
     *
     * @param context
     * @param compositeDisposable
     * @param rootView
     * @param uuid
     * @param authkey
     * @param entityID
     * @param entityUrl
     */
    public static void generateDeepLinkForCollabInvite(final FragmentActivity context, CompositeDisposable compositeDisposable, final View rootView, String uuid, String authkey, String entityID, final String entityUrl, String creatorName, final String contentType, final String shareOption, final String caption) {

        DeepLinkHelper deepLinkHelper = new DeepLinkHelper();
        deepLinkHelper.getDeepLinkFromServer(context
                , compositeDisposable
                , getEntitySpecificDeepLinkObservable(BuildConfig.URL + "/entity-share-link/generate-dynamic-link",
                        uuid,
                        authkey,
                        entityID,
                        entityUrl,
                        creatorName,
                        SHARE_SOURCE_FROM_CREATE),
                new listener.OnDeepLinkRequestedListener() {
                    @Override
                    public void onDeepLinkSuccess(final String deepLink, final MaterialDialog dialog) {
                        // check content type


                        Picasso.with(context).load(entityUrl).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                // dsimiss dialog
                                dialog.dismiss();
                                // finish preview activity
                                context.finish();

                                String shareText = TextUtils.isEmpty(caption) ?
                                        context.getString(R.string.text_share_image_created, deepLink) :
                                        caption + "\n\n" + "App: " + deepLink;

                                ShareHelper.sharePost(bitmap, context, shareText, shareOption);

                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                dialog.dismiss();
                                ViewHelper.getToast(context, context.getString(R.string.error_msg_internal));
                                context.finish();
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {


                            }
                        });


                    }


                    @Override
                    public void onDeepLinkFailiure(String errorMsg, MaterialDialog dialog) {
                        dialog.dismiss();
                        ViewHelper.getToast(context, errorMsg);
                        context.finish();
                    }
                });
    }


    public static void generateUserSpecificDeepLink(final FragmentActivity context, final CompositeDisposable compositeDisposable, final String uuid, final String authkey) {
        // show invite dialog
        final MaterialDialog inviteDialog = new MaterialDialog.Builder(context)
                .title("Invite Friends")
                .content(context.getString(R.string.text_desc_dialog_invite_friends))
                .positiveText("Invite")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        // generate deep link
                        DeepLinkHelper deepLinkHelper = new DeepLinkHelper();
                        deepLinkHelper.getDeepLinkFromServer(context
                                , compositeDisposable
                                , getUserSpecificDeepLinkObservable(BuildConfig.URL + "/user-referral/get-referral-link", uuid, authkey)
                                , new OnDeepLinkRequestedListener() {
                                    @Override
                                    public void onDeepLinkSuccess(String deepLink, MaterialDialog dialog) {
                                        dialog.dismiss();
                                        // open invite dialog
                                        FeedHelper.inviteFriends(context, deepLink);

                                    }

                                    @Override
                                    public void onDeepLinkFailiure(String errorMsg, MaterialDialog dialog) {

                                        dialog.dismiss();
                                        ViewHelper.getToast(context, errorMsg);

                                    }
                                });
                    }
                })
                .show();
    }


}
