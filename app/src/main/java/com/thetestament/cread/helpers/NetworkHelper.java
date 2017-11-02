package com.thetestament.cread.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;

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
     * @param pageNumber    Page no to be loaded i.e integer
     */
    public static Observable<JSONObject> getObservableFromServer(String serverURL, String uuid, String authKey, String requestedUUID, int pageNumber) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("requesteduuid", requestedUUID);
            jsonObject.put("page", pageNumber);
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
     * Method to return user timeLine data from the server.
     *
     * @param serverURL  URL of the server.
     * @param uuid       UUID of the user.
     * @param authKey    AuthKey of user i.e String.
     * @param pageNumber Page no to be loaded i.e integer
     */
    public static Observable<JSONObject> getObservableFromServer(String serverURL, String uuid, String authKey, int pageNumber) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("page", pageNumber);
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
     * @param serverURL  URL of the server.
     * @param entityID   Entity id of post.
     * @param uuid       UUID of the user.
     * @param authKey    AuthKey of user.
     * @param pageNumber Page no to be loaded.
     */
    public static Observable<JSONObject> getHatsOffObservableFromServer(String serverURL, String entityID, String uuid, String authKey, int pageNumber) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);
            jsonObject.put("page", pageNumber);
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
     * @param serverURL  URL of the server.
     * @param uuid       UUID of the user.
     * @param authKey    Authentication key of the user.
     * @param entityID   Entity ID of the post.
     * @param pageNumber Page no to be loaded.
     * @param loadAll    True for all comments false otherwise.
     */
    public static Observable<JSONObject> getCommentObservableFromServer(String serverURL, String uuid, String authKey, String entityID, int pageNumber, boolean loadAll) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("entityid", entityID);
            jsonObject.put("page", pageNumber);
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

}
