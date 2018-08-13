package com.thetestament.cread.networkmanager;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.models.ExploreCategoryModel;
import com.thetestament.cread.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE_CATEGORY;

/**
 * Network manager class to provide utility methods related to 'Explore feed' network operation.
 */

public class ExploreNetworkManager {


    /**
     * Interface definition for a callback to be invoked when user request for 'Explore Category' data.
     */
    public interface OnExploreCategoryLoadListener {

        /**
         * @param dataList Explore category data list.
         */
        void onSuccess(List<ExploreCategoryModel> dataList);

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }

    /**
     * RxJava2 implementation for retrieving 'Explore Category' data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param loadListener        listener reference.
     */
    public static void getExploreCategoryData(final Context context, CompositeDisposable compositeDisposable
            , final OnExploreCategoryLoadListener loadListener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //Labels data list
        final List<ExploreCategoryModel> dataLIst = new ArrayList<>();

        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getExploreCategoryObservable(BuildConfig.URL + "/entity-interests/load-macro"
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
                                    loadListener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    //Suggested artists list
                                    JSONArray jsonArray = mainData.getJSONArray("items");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject dataObj = jsonArray.getJSONObject(i);
                                        ExploreCategoryModel model = new ExploreCategoryModel();
                                        //Set category property
                                        model.setCategoryText(dataObj.getString("mintname"));
                                        model.setCategoryID(dataObj.getString("mintid"));
                                        model.setCategoryType(dataObj.getString("type"));
                                        dataLIst.add(model);
                                    }
                                    loadListener.onSuccess(dataLIst);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "ExploreNetworkManager");
                                loadListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "ExploreNetworkManager");
                            //Set failure listener
                            loadListener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //Set to false
                            GET_RESPONSE_FROM_NETWORK_EXPLORE_CATEGORY = false;
                        }
                    })
            );
        } else {
            //Set listener
            loadListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * Method to return explore category data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     * @return
     */
    private static Observable<JSONObject> getExploreCategoryObservable(String serverURL, String uuid, String authKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers);

        if (CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE_CATEGORY) {
            requestBuilder.getResponseOnlyFromNetwork();
        }
        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }


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
