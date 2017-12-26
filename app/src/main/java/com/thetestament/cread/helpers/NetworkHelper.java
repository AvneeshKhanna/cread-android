package com.thetestament.cread.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;

import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.listeners.listener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;

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

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requesteduuid", requestedUUID);
        queryParams.put("lastindexkey", lastIndexKey);

        return Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParams)
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
        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);
        header.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(header)
                .addQueryParameter("lastindexkey", lastIndexKey);

        if (GET_RESPONSE_FROM_NETWORK_MAIN) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder.build().getJSONObjectObservable();
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

        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);
        header.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(header)
                .addQueryParameter("requesteduuid", requestedUUID);

        if (GET_RESPONSE_FROM_NETWORK_ME) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder.build()
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


        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityid", entityID);
        queryParams.put("lastindexkey", lastIndexKey);

        return Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParams)
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

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("entityid", entityID);
        queryParams.put("lastindexkey", lastIndexKey);
        queryParams.put("loadall", loadAll);


        return Rx2AndroidNetworking.get(serverURL)
                .addQueryParameter(queryParams)
                .addHeaders(headers)
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

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);


        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityid", entityID);
        queryParams.put("lastindexkey", lastIndexKey);
        queryParams.put("entitytype", entityType);

        return Rx2AndroidNetworking.get(serverURL)
                .addQueryParameter(queryParams)
                .addHeaders(headers)
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
    public static Observable<JSONObject> getRoyaltiesObservable(String serverURL, String uuid, String authKey, String lastIndexKey, boolean requestRoyaltiesData) {

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("lastindexkey", lastIndexKey);
        queryParams.put("toloadtotal", requestRoyaltiesData);


        return Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParams)
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
    public static Observable<JSONObject> getDeepLinkObservable(String serverURL, String uuid, String authKey, String entityID, String entityURL, String creatorName) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);
            jsonObject.put("entityurl", entityURL);
            jsonObject.put("creatorname", creatorName);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    public static Observable<JSONObject> getHashTagDetailsObservable(String uuid, String authkey, String hashtag, String lastIndexKey) {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("htag", hashtag);
        queryParam.put("lastindexkey", lastIndexKey);

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authkey);

        return Rx2AndroidNetworking.get(BuildConfig.URL + "/hashtag/feed")
                .addQueryParameter(queryParam)
                .addHeaders(headers)
                .build()
                .getJSONObjectObservable();
    }

    public static Observable<String> getRestartHerokuObservable() {
        return Rx2AndroidNetworking.get("http://cread-server-main.ap-northeast-1.elasticbeanstalk.com/dev-utils/restart-heroku")
                .build()
                .getStringObservable();
    }

    public static <T> void requestServer(CompositeDisposable compositeDisposable, final Observable<T> observableObject, FragmentActivity context, final listener.OnServerRequestedListener listener) {

        if (getNetConnectionStatus(context)) {
            compositeDisposable.add(observableObject
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<T>() {
                        @Override
                        public void onNext(T jsonObject) {
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

        } else {
            listener.onDeviceOffline();
        }
    }

}
