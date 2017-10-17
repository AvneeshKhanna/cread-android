package com.thetestament.cread.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;

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
     * Method to check internet connection status and return boolean i.e true and  false.
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
     * Reactive approach to retrieve profile data from server.
     *
     * @param serverURL     URL of the server.
     * @param uuid          UUID of the user.
     * @param authKey       AuthKey of user i.e String
     * @param requestedUUID UUID of user whose profile data to be loaded.
     */
    public static Observable<JSONObject> getObservableFromServer(String serverURL, String uuid, String authKey, String requestedUUID) {
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
     * @param context    Context where this method will be called.
     * @param serverURL  URL of the server.
     * @param pageNumber Page no to be loaded i.e int
     */
    public static Observable<JSONObject> getObservableFromServer(FragmentActivity context, String serverURL, int pageNumber) {
        JSONObject jsonObject = new JSONObject();
        try {
            //// TODO:  retrieve data
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
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
     * Method to return data from the server
     *
     * @param context   Context where this method will be called
     * @param serverURL URL of the server
     * @param entityID  entity id
     */
    public static Observable<JSONObject> getObservableFromServer(FragmentActivity context, String serverURL, String entityID, int pageNumber) {

        JSONObject jsonObject = new JSONObject();
        try {
            //// TODO:
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
            jsonObject.put("entity", entityID);
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
}
