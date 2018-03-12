package com.thetestament.cread.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;

import com.androidnetworking.common.Priority;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_COMMENTS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_HATSOFF;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_UPDATES;

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
    public static Observable<JSONObject> getObservableFromServer(String serverURL, String uuid, String authKey, String requestedUUID, String lastIndexKey, boolean getResponseFromNetwork) {

        // used in follow activity and me fragment


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
     * Method to return data from the server.
     *
     * @param serverURL    URL of the server.
     * @param uuid         UUID of the user.
     * @param authKey      AuthKey of user i.e String.*
     * @param lastIndexKey Last index key
     */
    public static Observable<JSONObject> getObservableFromServer(String serverURL, String uuid, String authKey, String lastIndexKey, boolean getResponseFromNetwork) {

        // used in feed fragemnt, explore fragment and inspiration activity, chat list

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

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParams);


        if (GET_RESPONSE_FROM_NETWORK_HATSOFF) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
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
        queryParams.put(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);


        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addQueryParameter(queryParams)
                .addHeaders(headers);

        if (GET_RESPONSE_FROM_NETWORK_COMMENTS) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
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
        queryParams.put(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

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
        queryParam.put(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

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

    /**
     * Method to return requested data from the server.
     *
     * @param queryMessage Search text entered by the user.
     * @param lastIndexKey Url of next page.
     * @param searchType   SearchType i.e USER or HASH TAG
     */
    public static Observable<JSONObject> getSearchObservableServer(String queryMessage, String lastIndexKey, String searchType) {

        String inputText;
        //Replace spaces if search type is HASHTAG
        if (searchType.equals(Constant.SEARCH_TYPE_HASHTAG)) {
            inputText = queryMessage.replaceAll("\\s+", "");
        } else {
            inputText = queryMessage;
        }
        return Rx2AndroidNetworking.get(BuildConfig.URL + "/search/load")
                .addQueryParameter("keyword", inputText)
                .addQueryParameter("lastindexkey", lastIndexKey)
                .addQueryParameter("searchtype", searchType)
                .setPriority(Priority.HIGH)
                .build()
                .getJSONObjectObservable();
    }


    public static Observable<JSONObject> getEntitySpecificObservable(String uuid, String authkey, String entityID) {

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authkey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(BuildConfig.URL + "/entity-manage/load-specific")
                .addHeaders(headers)
                .addQueryParameter("entityid", entityID)
                .addQueryParameter(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        if (GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }


    public static Observable<JSONObject> updateFollowStatusObservable(String uuid, String authkey, boolean register, JSONArray followees) {
        final JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authkey);
            jsonObject.put("register", register);
            jsonObject.put("followees", followees);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        return Rx2AndroidNetworking
                .post(BuildConfig.URL + "/follow/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    public static Observable<JSONObject> updateDownvoteStatusObservable(String uuid, String authkey, boolean downvote, String entityid) {
        final JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authkey);
            jsonObject.put("register", downvote);
            jsonObject.put("entityid", entityid);


        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        return Rx2AndroidNetworking
                .post(BuildConfig.URL + "/downvote/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }


    public static Observable<JSONObject> getDeletePostObservable(String uuid, String authkey, String entityID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authkey);
            jsonObject.put("entityid", entityID);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        return Rx2AndroidNetworking
                .post(BuildConfig.URL + "/entity-manage/delete")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }

    public static Observable<JSONObject> getUpdatesObservable(String uuid, String authKey, String lastIndexKey) {

        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);
        header.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(BuildConfig.URL + "/updates/load/")
                .addHeaders(header)
                .addQueryParameter("lastindexkey", lastIndexKey);

        if (GET_RESPONSE_FROM_NETWORK_UPDATES) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder.build().getJSONObjectObservable();

    }

    public static Observable<JSONObject> getUpdateUnreadObservable(String uuid, String authKey, String updateid) {
        final JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("updateid", updateid);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        return Rx2AndroidNetworking
                .post(BuildConfig.URL + "/updates/update-unread")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
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


    /**
     * Method to return chat details data from the server.
     *
     * @param serverURL    URL of the server.
     * @param receiverUUID UUID of the message receiver.
     * @param senderUUID   UUID of message sender.
     * @param lastIndexKey Last index key.
     */
    public static Observable<JSONObject> getChatDataObservableFromServer(String serverURL, String receiverUUID, String senderUUID, String lastIndexKey) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("to_uuid", receiverUUID);
        queryParams.put("from_uuid", senderUUID);
        queryParams.put("lastindexkey", lastIndexKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addQueryParameter(queryParams);
        //if true then load data from network
        if (GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to return chat request count from the server.
     *
     * @param uuid      UUID of user
     * @param serverURL URL of the server.
     */
    public static Observable<JSONObject> getChatRequestCountObservableFromServer(String serverURL, String uuid, boolean getResponseFromNetwork) {

        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(header)
                .addQueryParameter(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder.build().getJSONObjectObservable();
    }


    /**
     * Method to return data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     * @param chatID    chat ID of conversation.
     */
    public static Observable<JSONObject> getupdateChatReadStatusObservable(String serverURL, String uuid, String authKey, String chatID) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("chatid", chatID);

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
     * @param serverURL     URL of the server
     * @param uuid          UUID of the user.
     * @param authKey       Authentication key of the user
     */
    public static Observable<JSONObject> getFeatArtistsObservable(String serverURL, String uuid, String authKey, boolean getResponseFromNetwork) {


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

}
