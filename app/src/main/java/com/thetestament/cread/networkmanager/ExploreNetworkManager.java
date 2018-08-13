package com.thetestament.cread.networkmanager;

import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.utils.Constant;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Network manager class to provide utility methods related to 'Explore feed' network operation.
 */

public class ExploreNetworkManager {

    /**
     * Method to return explore feed data from the server.
     *
     * @param serverURL              URL of the server.
     * @param uuid                   UUID of the user.
     * @param authKey                AuthKey of user i.e String.
     * @param lastIndexKey           Last index key
     * @param getResponseFromNetwork boolean which indicate whether to load from network or cache.
     * @param categoryID
     * @return
     */
    public static Observable<JSONObject> getExploreFeedObservable(String serverURL, String uuid, String authKey
            , String lastIndexKey, boolean getResponseFromNetwork, String categoryID, String selectedTab) {

        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);
        header.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(header)
                .addQueryParameter("lastindexkey", lastIndexKey)
                .addQueryParameter("mintid", categoryID)
                .addQueryParameter("sortby", selectedTab)
                .addQueryParameter(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder.build().getJSONObjectObservable();
    }


    /**
     * Method to return featured artist data from the server.
     *
     * @param serverURL URL of the server
     * @param uuid      UUID of the user.
     * @param authKey   Authentication key of the user.
     */
    public static Observable<JSONObject> getFeatArtistsObservable(String serverURL, String uuid
            , String authKey, boolean getResponseFromNetwork) {

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);


        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }

    /**
     * Method to return meme observable data.
     *
     * @param serverURL              URL of the server.
     * @param uuid                   UUID of the user.
     * @param authKey                AuthKey of user.
     * @param lastIndexKey           Last index key
     * @param getResponseFromNetwork boolean which indicate whether to load from network or cache.
     * @return Observable<JSONObject>
     */
    public static Observable<JSONObject> getMemeObservable(String serverURL, String uuid, String authKey
            , String lastIndexKey, boolean getResponseFromNetwork) {

        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);
        header.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(header)
                .addQueryParameter("lastindexkey", lastIndexKey)
                .addQueryParameter(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder.build().getJSONObjectObservable();
    }

}
