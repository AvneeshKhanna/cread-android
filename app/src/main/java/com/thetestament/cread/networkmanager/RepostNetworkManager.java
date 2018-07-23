package com.thetestament.cread.networkmanager;

import android.support.v4.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Network manager class to provide utility methods related to 'Re-post' network operation.
 */

public class RepostNetworkManager {


    /**
     * Interface definition for a callback to be invoked when user re-posts other user post.
     */
    public interface OnRepostSaveListener {

        void onSuccess();

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }

    /**
     * Interface definition for a callback to be invoked when user deletes re-posted posts.
     */
    public interface OnRepostDeleteListener {

        void onSuccess();

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }


    /**
     * RxJava2 implementation  to update re-posted data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param entityID            entity ID.
     * @param listener            listener reference.
     */
    public static void saveRepost(final FragmentActivity context, CompositeDisposable compositeDisposable, String entityID
            , final OnRepostSaveListener listener) {
        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //If device is connected to network
        if (NetworkHelper.getNetConnectionStatus(context)) {
            //Show dialog
            final MaterialDialog dialog = CustomDialog.getProgressDialog(context
                    , "Reposting...");

            compositeDisposable.add(getRepostObservable(BuildConfig.URL + "/repost/add"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken()
                    , entityID)
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>() {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            //Dismiss dialog
                            dialog.dismiss();
                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    listener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    if (mainData.getString("status").equals("done")) {
                                        //Set success listener
                                        listener.onSuccess();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                listener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            //Set failure listener
                            listener.onFailure(context.getString(R.string.error_msg_server));
                            //Dismiss dialog
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    })
            );
        } else {
            //Set listener
            listener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * RxJava2 implementation  to delete re-posted data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param repostID            repost ID.
     * @param listener            listener reference.
     */
    public static void deleteRepost(final FragmentActivity context, CompositeDisposable compositeDisposable, String repostID
            , final OnRepostDeleteListener listener) {
        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //If device is connected to network
        if (NetworkHelper.getNetConnectionStatus(context)) {
            //Show dialog
            final MaterialDialog dialog = CustomDialog.getProgressDialog(context
                    , "Removing...");

            compositeDisposable.add(getDeleteRepostObservable(BuildConfig.URL + "/repost/delete"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken()
                    , repostID)
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>() {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            //Dismiss dialog
                            dialog.dismiss();
                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    listener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    if (mainData.getString("status").equals("done")) {
                                        //Set success listener
                                        listener.onSuccess();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                listener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            //Set failure listener
                            listener.onFailure(context.getString(R.string.error_msg_server));
                            //Dismiss dialog
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    })
            );
        } else {
            //Set listener
            listener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * Method to return data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     * @param entityID  entity ID of post to be re-posted.
     */
    private static Observable<JSONObject> getRepostObservable(String serverURL, String uuid, String authKey, String entityID) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }

    /**
     * Method to return data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     * @param repostID  re-post ID of post to be removed.
     */
    private static Observable<JSONObject> getDeleteRepostObservable(String serverURL, String uuid, String authKey, String repostID) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("repostid", repostID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }

}
