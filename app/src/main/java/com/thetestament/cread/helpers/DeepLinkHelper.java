package com.thetestament.cread.helpers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnDeepLinkRequestedListener;
import com.thetestament.cread.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.NetworkHelper.getDeepLinkObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;

/**
 * Created by prakharchandna on 12/03/18.
 */

public class DeepLinkHelper {

    public void getDeepLinkFromServer(final FragmentActivity context, CompositeDisposable compositeDisposable, String uuid, String authkey, String entityID, String entityUrl, String creatorName, final OnDeepLinkRequestedListener onDeepLinkRequestedListener) {


        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Processing")
                .content(context.getString(R.string.waiting_msg))
                .progress(true, 0)
                .show();

        requestServer(compositeDisposable, getDeepLinkObservable(BuildConfig.URL + "/entity-share-link/generate-dynamic-link",
                uuid,
                authkey,
                entityID,
                entityUrl,
                creatorName),
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
    public static void generateDeepLink(final FragmentActivity context, CompositeDisposable compositeDisposable, final View rootView, String uuid, String authkey, String entityID, String entityUrl, String creatorName) {

        DeepLinkHelper deepLinkHelper = new DeepLinkHelper();
        deepLinkHelper.getDeepLinkFromServer(context
                , compositeDisposable
                , uuid
                , authkey
                , entityID
                , entityUrl
                , creatorName
                , new listener.OnDeepLinkRequestedListener() {
                    @Override
                    public void onDeepLinkSuccess(String deepLink, MaterialDialog dialog) {
                        dialog.dismiss();
                        // share link
                        shareDeepLink(context, deepLink);

                    }

                    @Override
                    public void onDeepLinkFailiure(String errorMsg, MaterialDialog dialog) {
                        dialog.dismiss();
                        ViewHelper.getSnackBar(rootView, errorMsg);

                    }
                });
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
    public static void generateDeepLinkForCollabInvite(final FragmentActivity context, CompositeDisposable compositeDisposable, final View rootView, String uuid, String authkey, String entityID, final String entityUrl, String creatorName, final String contentType) {

        DeepLinkHelper deepLinkHelper = new DeepLinkHelper();
        deepLinkHelper.getDeepLinkFromServer(context
                , compositeDisposable
                , uuid
                , authkey
                , entityID
                , entityUrl
                , creatorName
                , new listener.OnDeepLinkRequestedListener() {
                    @Override
                    public void onDeepLinkSuccess(final String deepLink, final MaterialDialog dialog) {
                        // check content type


                        Picasso.with(context).load(entityUrl).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                dialog.dismiss();

                                if (contentType.equals(Constant.CONTENT_TYPE_CAPTURE))

                                {
                                    ShareHelper.collabInviteImage(bitmap, context, context.getString(R.string.text_invite_collab_capture) + "\n\n" + deepLink);
                                } else

                                {   // open share dialog
                                    ShareHelper.collabInviteImage(bitmap, context, context.getString(R.string.text_invite_collab_short) + "\n\n" + deepLink);
                                }

                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                dialog.dismiss();
                                ViewHelper.getToast(context, context.getString(R.string.error_msg_internal));
                                context.finish();
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                dialog.dismiss();
                                context.finish();

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


}
