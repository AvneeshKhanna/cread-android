package com.thetestament.cread.networkmanager;

import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.utils.Constant;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Network manager class to provide utility methods related to 'Me Screen' network operation.
 */

public class MeNetworkManager {


    /**
     * Method to return user posts Observable.
     *
     * @param serverURL              URL of the server
     * @param uuid                   UUID of the user.
     * @param authKey                Authentication key of the user
     * @param requestedUUID          UUID of user whose profile data to be loaded.
     * @param lastIndexKey           Last index key.
     * @param getResponseFromNetwork Whether to load data from network or cache.
     * @return Response of user posts.
     */
    public static Observable<JSONObject> getUserPostsObservable(String serverURL, String uuid, String authKey, String requestedUUID, String lastIndexKey, boolean getResponseFromNetwork) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requesteduuid", requestedUUID);
        queryParams.put("lastindexkey", lastIndexKey);
        queryParams.put(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParams);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }

    /**
     * Method to return collaboration posts Observable.
     *
     * @param serverURL              URL of the server
     * @param uuid                   UUID of the user.
     * @param authKey                Authentication key of the user
     * @param requestedUUID          UUID of user whose profile data to be loaded.
     * @param lastIndexKey           Last index key.
     * @param getResponseFromNetwork Whether to load data from network or cache.
     * @return Response of Collaboration posts.
     */
    public static Observable<JSONObject> getCollaborationPostsObservable(String serverURL, String uuid, String authKey, String requestedUUID, String lastIndexKey, boolean getResponseFromNetwork) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requesteduuid", requestedUUID);
        queryParams.put("lastindexkey", lastIndexKey);
        queryParams.put(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParams);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }

    /**
     * Method to return user re-posts Observable.
     *
     * @param serverURL              URL of the server
     * @param uuid                   UUID of the user.
     * @param authKey                Authentication key of the user
     * @param requestedUUID          UUID of user whose profile data to be loaded.
     * @param lastIndexKey           Last index key.
     * @param getResponseFromNetwork Whether to load data from network or cache.
     * @return Response of user re posts.
     */
    public static Observable<JSONObject> getUserRePostsObservable(String serverURL, String uuid, String authKey, String requestedUUID, String lastIndexKey, boolean getResponseFromNetwork) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("requesteduuid", requestedUUID);
        queryParams.put("lastindexkey", lastIndexKey);
        queryParams.put(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParams);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }

}
