package com.thetestament.cread.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;

import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.listeners.listener;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * A helper class for providing utility method for network related operations.
 */

public class NetworkHelper {


    /**
     * Method to check internet connection status and return boolean i.e true and false.
     *
     * @param context Context: The context to use. Usually your Application or Activity object.
     * @return boolean i.e true if device is connected to internet false otherwise.
     */
    public static boolean getNetConnectionStatus(Context context) {
        boolean connectionStatus;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        connectionStatus = networkInfo != null && networkInfo.isConnected();
        return connectionStatus;
    }


    /**
     * Method to return requested data from the server.
     *
     * @param serverURL     URL of the server
     * @param uuid          UUID of the user.
     * @param authKey       Authentication key of the user
     * @param requestedUUID UUID of user whose profile data to be loaded.
     * @param lastIndexKey  Last index key.
     */
    public static Observable<JSONObject> getObservableFromServer(String serverURL, String uuid, String authKey, String requestedUUID, String lastIndexKey) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("requesteduuid", requestedUUID);
            jsonObject.put("lastindexkey", lastIndexKey);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to return data from the server.
     *
     * @param serverURL    URL of the server.
     * @param uuid         UUID of the user.
     * @param authKey      AuthKey of user i.e String.*
     * @param lastIndexKey Last index key
     */
    public static Observable<JSONObject> getObservableFromServer(String serverURL, String uuid, String authKey, String lastIndexKey) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("lastindexkey", lastIndexKey);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Reactive approach to retrieve user profile data from server.
     *
     * @param serverURL     URL of the server.
     * @param uuid          UUID of the user.
     * @param authKey       AuthKey of user.
     * @param requestedUUID UUID of user whose profile data to be loaded.
     */
    public static Observable<JSONObject> getUserDataObservableFromServer(String serverURL, String uuid, String authKey, String requestedUUID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("requesteduuid", requestedUUID);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to return data from the server.
     *
     * @param serverURL    URL of the server.
     * @param entityID     Entity id of post.
     * @param uuid         UUID of the user.
     * @param authKey      AuthKey of user.
     * @param lastIndexKey Last index key.
     */
    public static Observable<JSONObject> getHatsOffObservableFromServer(String serverURL, String entityID, String uuid, String authKey, String lastIndexKey) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);
            jsonObject.put("lastindexkey", lastIndexKey);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to return requested data from the server.
     *
     * @param serverURL    URL of the server.
     * @param uuid         UUID of the user.
     * @param authKey      Authentication key of the user.
     * @param entityID     Entity ID of the post.
     * @param lastIndexKey Last index key.
     * @param loadAll      True for all comments false otherwise.
     */
    public static Observable<JSONObject> getCommentObservableFromServer(String serverURL, String uuid, String authKey, String entityID, String lastIndexKey, boolean loadAll) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);
            jsonObject.put("lastindexkey", lastIndexKey);
            jsonObject.put("loadall", loadAll);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }

    /**
     * Method to return data from the server.
     *
     * @param serverURL    URL of the server.
     * @param entityID     Entity id of post.
     * @param entityType   Type of entity i.e CAPTURE ,SHORT.
     * @param uuid         UUID of the user.
     * @param authKey      AuthKey of user.
     * @param lastIndexKey Last index key.
     */
    public static Observable<JSONObject> getCollaborationDetailsObservableFromServer(String serverURL, String entityID, String entityType, String uuid, String authKey, String lastIndexKey) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);
            jsonObject.put("entitytype", entityType);
            jsonObject.put("lastindexkey", lastIndexKey);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


  /**
     * Method to return data from the server.
     *
     * @param serverURL    URL of the server.
     * @param uuid         UUID of the user.
     * @param authKey      AuthKey of user.
     * @param lastIndexKey Last index key.
     */
    public static Observable<JSONObject> getRoyaltiesObersvable(String serverURL, String uuid, String authKey, String lastIndexKey, boolean requestRoyaltiesData) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("lastindexkey", lastIndexKey);
            jsonObject.put("toloadtotal", requestRoyaltiesData);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
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
     * @param entityID  entityID.
     * @param entityURL entityURL
     */
    public static Observable<JSONObject> getDeepLinkObservable(String serverURL, String uuid, String authKey, String entityID, String entityURL) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);
            jsonObject.put("entityurl", entityURL);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }





    public static void requestServer(CompositeDisposable compositeDisposable, Observable<JSONObject> jsonObjectObservable, FragmentActivity context, final listener.OnServerRequestedListener listener)
    {
        if(getNetConnectionStatus(context))
        {
            compositeDisposable.add(jsonObjectObservable
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>()
                    {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            listener.onNextCalled(jsonObject);
                        }

                        @Override
                        public void onError(Throwable e) {
                            listener.onErrorCalled(e);
                        }

                        @Override
                        public void onComplete() {
                            listener.onCompleteCalled();
                        }
                    })

            );

        }

        else
        {
            listener.onDeviceOffline();
        }
    }

}
