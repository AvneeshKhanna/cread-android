package com.thetestament.cread.helpers;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.helpers.NetworkHelper.updateDownvoteStatusObservable;

/**
 * Created by prakharchandna on 05/03/18.
 */

public class DownvoteHelper {

    public void updateDownvoteStatus(final FragmentActivity context,
                                     CompositeDisposable compositeDisposable,
                                     boolean downvote,
                                     String entityid,
                                     final listener.OnDownvoteRequestedListener onDownvoteRequestedListener) {
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

        requestServer(compositeDisposable, updateDownvoteStatusObservable(spHelper.getUUID()
                , spHelper.getAuthToken()
                , downvote
                , entityid)
                , context
                , new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        onDownvoteRequestedListener.onDownvoteFailiure(context
                                .getString(R.string.error_msg_no_connection));

                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {


                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //Set listener
                                onDownvoteRequestedListener.onDownvoteFailiure((context.getString(R.string.error_msg_invalid_token)));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                                    //Set listener
                                    onDownvoteRequestedListener.onDownvoteSuccess();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "DeletePostHelper");
                            //Set listener
                            onDownvoteRequestedListener.onDownvoteFailiure((context.getString(R.string.error_msg_internal)));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "DeletePostHelper");
                        //Set listener
                        onDownvoteRequestedListener.onDownvoteFailiure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {


                    }
                });
    }


    public void updateDownvoteUI(ImageView icon, boolean isDownvoted, FragmentActivity context) {
        if (isDownvoted) {

            icon.setColorFilter(ContextCompat.getColor(context, R.color.blue));

        } else {

            icon.setColorFilter(Color.TRANSPARENT);

        }
    }

    public void initDownvoteProcess(final FragmentActivity context, final FeedModel data, CompositeDisposable compositeDisposable, final ImageView iconDownvote, final Bundle resultBundle, final Intent resultIntent) {
        //update text
        data.setDownvoteStatus(!data.isDownvoteStatus());
        updateDownvoteUI(iconDownvote, data.isDownvoteStatus(), context);

        updateDownvoteStatus(context, compositeDisposable, data.isDownvoteStatus(), data.getEntityID(), new listener.OnDownvoteRequestedListener() {
            @Override
            public void onDownvoteSuccess() {

                // do nothing
                resultBundle.putBoolean("downvotestatus", data.isDownvoteStatus());
                context.setResult(RESULT_OK, resultIntent);

            }

            @Override
            public void onDownvoteFailiure(String errorMsg) {

                ViewHelper.getToast(context, errorMsg);
                // revert status
                data.setDownvoteStatus(!data.isDownvoteStatus());
                updateDownvoteUI(iconDownvote, data.isDownvoteStatus(), context);

            }
        });

    }


    public void initDownvoteWarningDialog(final FragmentActivity context, final FeedModel data, final CompositeDisposable compositeDisposable, final ImageView iconDownvote, final Bundle resultBundle, final Intent resultIntent) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .title(R.string.text_title_dialog_downvote_warning)
                .content(R.string.text_desc_dialog_downvote_warning)
                .positiveText(context.getString(R.string.text_title_dialog_downvote_warning))
                .negativeText(context.getString(R.string.text_dialog_button_cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        // do nothing
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //dismiss dialog
                        dialog.dismiss();
                        // show toast
                        ViewHelper.getToast(context, "Downvoted");

                        //update downvote status
                        initDownvoteProcess(context
                                , data
                                , compositeDisposable
                                , iconDownvote
                                , resultBundle
                                , resultIntent);


                    }
                });

        final MaterialDialog dialog = builder.build();
        dialog.show();

    }


}
