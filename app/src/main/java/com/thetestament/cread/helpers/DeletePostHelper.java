package com.thetestament.cread.helpers;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnDeleteRequestedListener;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_INSPIRATION;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.helpers.NetworkHelper.getDeletePostObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;

/**
 * Class to provide utility method related to delete post.
 */
public class DeletePostHelper {


    /**
     * @param context                   Context to use.
     * @param compositeDisposable       Composite disposable  reference.
     * @param entityID                  Entity Id of content to be deleted.
     * @param OnDeleteRequestedListener Listener reference.
     */
    public static void deletePost(final FragmentActivity context, CompositeDisposable compositeDisposable, String entityID,
                                  final OnDeleteRequestedListener OnDeleteRequestedListener) {

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

        requestServer(compositeDisposable,
                getDeletePostObservable(spHelper.getUUID(), spHelper.getAuthToken(), entityID),
                context,
                new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        OnDeleteRequestedListener.onDeleteFailure(context.getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                OnDeleteRequestedListener.onDeleteFailure(context.getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_INSPIRATION = true;
                                    OnDeleteRequestedListener.onDeleteSuccess();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "DeletePostHelper");
                            OnDeleteRequestedListener.onDeleteFailure(context.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        //Dismiss dialog
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "DeletePostHelper");
                        OnDeleteRequestedListener.onDeleteFailure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {
                        //do nothing
                    }
                });

    }


    /**
     * Method to show confirmation dialog before deletion.
     *
     * @param context                 Context to use.
     * @param index                   position of item in adapter.
     * @param entityID                Entity id of content.
     * @param onContentDeleteListener OnContentDeleteListener
     */
    public static void showDeleteConfirmationDialog(FragmentActivity context, final int index, final String entityID, final listener.OnContentDeleteListener onContentDeleteListener) {
        new MaterialDialog.Builder(context)
                .content("Are you sure want to delete this?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        onContentDeleteListener.onDelete(entityID, index);
                        materialDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .build()
                .show();
    }


}




