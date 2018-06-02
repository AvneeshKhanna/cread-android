package com.thetestament.cread.networkmanager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Network manager class to provide utility methods related to 'Notification' network operation.
 */

public class NotificationNetworkManager {

    //region :Listeners

    /**
     * Interface definition for a callback to be invoked when user request for NotificationSeenStatus data.
     */
    public interface OnNotificationSeenStatusLoadListener {

        /**
         * @param updatesSeenStatus Updates screen  status.
         * @param chatSeenStatus    Chat screen status.
         */
        void onSuccess(boolean updatesSeenStatus, boolean chatSeenStatus);

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }


    /**
     * Interface definition for a callback to be invoked when user updates 'Updates screen' seen status.
     */
    public interface OnUpdatesSeenUpdateListener {

        void onSuccess();

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }


    /**
     * Interface definition for a callback to be invoked when user updates 'Chat screen ' seen data.
     */
    public interface OnChatSeenUpdateListener {

        void onSuccess();

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }
    //endregion

    //region :Network calls

    /**
     * RxJava2 implementation for retrieving notification seen status data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param loadListener        Listener reference.
     * @param requestFromNetwork  Whether to requests from network or cache.
     */
    public static void getNotificationSeenStatus(final Context context, CompositeDisposable compositeDisposable, boolean requestFromNetwork
            , final OnNotificationSeenStatusLoadListener loadListener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getNotificationSeenStatusObservable(BuildConfig.URL + "/updates/load-seen-status"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken()
                    , requestFromNetwork)
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>() {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    loadListener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    loadListener.onSuccess(mainData.getBoolean("updates_unseen")
                                            , mainData.getBoolean("chats_unseen"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "NotificationNetworkManager");
                                loadListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "NotificationNetworkManager");
                            //Set failure listener
                            loadListener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //Do nothing
                        }
                    })
            );
        } else {
            //Set listener
            loadListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * RxJava2 implementation to update 'Updates seen' status.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param updateListener      listener reference.
     */
    public static void updateUpdatesSeenStatus(final FragmentActivity context, CompositeDisposable compositeDisposable
            , final OnUpdatesSeenUpdateListener updateListener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //If device is connected to network
        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getUpdatesObservable(BuildConfig.URL + "/updates/update-unseen"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken())
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>() {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    updateListener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    if (mainData.getString("status").equals("done")) {
                                        //Set success listener
                                        updateListener.onSuccess();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "NotificationNetworkManager");
                                updateListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "NotificationNetworkManager");
                            //Set failure listener
                            updateListener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    })
            );
        } else {
            //Set listener
            updateListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * RxJava2 implementation to update 'Chat screen' seen status.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param updateListener      listener reference.
     */
    public static void updateChatSeenStatus(final FragmentActivity context, CompositeDisposable compositeDisposable
            , final OnChatSeenUpdateListener updateListener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //If device is connected to network
        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getChatUpdateObservable(BuildConfig.URL + "/chat-list/mark-as-seen"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken())
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>() {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    updateListener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    if (mainData.getString("status").equals("done")) {
                                        //Set success listener
                                        updateListener.onSuccess();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "NotificationNetworkManager");
                                updateListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "NotificationNetworkManager");
                            //Set failure listener
                            updateListener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    })
            );
        } else {
            //Set listener
            updateListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }

    //endregion

    //region :Network response observable

    /**
     * Method to return notification seen status data from the server.
     *
     * @param serverURL          URL of the server.
     * @param uuid               UUID of the user.
     * @param authKey            AuthKey of user.
     * @param requestFromNetwork Whether to requests from network or cache.
     * @return
     */
    private static Observable<JSONObject> getNotificationSeenStatusObservable(String serverURL, String uuid, String authKey, boolean requestFromNetwork) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers);
        if (requestFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }
        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to return data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     */
    public static Observable<JSONObject> getUpdatesObservable(String serverURL, String uuid, String authKey) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "NotificationNetworkManager");
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
     */
    public static Observable<JSONObject> getChatUpdateObservable(String serverURL, String uuid, String authKey) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "NotificationNetworkManager");
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }

    //endregion
}
