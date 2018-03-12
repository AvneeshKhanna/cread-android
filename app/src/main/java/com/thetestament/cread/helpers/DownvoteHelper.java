package com.thetestament.cread.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
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
                            FirebaseCrash.report(e);
                            //Set listener
                            onDownvoteRequestedListener.onDownvoteFailiure((context.getString(R.string.error_msg_internal)));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Set listener
                        onDownvoteRequestedListener.onDownvoteFailiure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {


                    }
                });
    }


    public void updateDownvoteText(TextView view, ImageView icon, boolean isDownvoted, FragmentActivity context) {
        if (isDownvoted) {
            view.setTextColor(ContextCompat.getColor(context, R.color.blue_dark));
            icon.setColorFilter(ContextCompat.getColor(context, R.color.blue_dark));
            view.setText("Downvoted");
        } else {
            view.setTextColor(ContextCompat.getColor(context, R.color.grey));
            icon.setColorFilter(ContextCompat.getColor(context, R.color.grey));
            view.setText("Downvote");
        }
    }

    public void initDownvoteProcess(final FragmentActivity context, final FeedModel data, CompositeDisposable compositeDisposable, final TextView textDownvote, final ImageView iconDownvote, final Bundle resultBundle, final Intent resultIntent) {
        //update text
        data.setDownvoteStatus(!data.isDownvoteStatus());
        updateDownvoteText(textDownvote, iconDownvote, data.isDownvoteStatus(), context);

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
                updateDownvoteText(textDownvote, iconDownvote, data.isDownvoteStatus(), context);

            }
        });

    }


}
