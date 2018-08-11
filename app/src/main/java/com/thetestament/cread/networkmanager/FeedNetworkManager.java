package com.thetestament.cread.networkmanager;

import android.support.v4.app.FragmentActivity;

import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.models.SuggestedArtistsModel;
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

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS;

/**
 * Network manager class to provide utility methods related to 'Main feed' network operation.
 */

public class FeedNetworkManager {

    /**
     * Interface definition for a callback to be invoked when user request for SuggestedArtist data.
     */
    public interface OnSuggestedArtistLoadListener {

        /**
         * List of suggested artist data.
         */
        void onSuccess(List<SuggestedArtistsModel> dataList);

        /**
         * Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }


    /**
     * Method to retrieve SuggestedList data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param listener            OnSuggestedArtistLoadListener reference
     */
    public static void getSuggestedArtistData(final FragmentActivity context
            , CompositeDisposable compositeDisposable
            , final OnSuggestedArtistLoadListener listener) {

        if (NetworkHelper.getNetConnectionStatus(context)) {

            //Obtain SharedPreferenceHelper reference
            SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
            final List<SuggestedArtistsModel> dataLIst = new ArrayList<>();

            compositeDisposable.add(geSuggestedArtistsObservable(BuildConfig.URL + "/recommend-users/load"
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
                                    listener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    //Suggested artists list
                                    JSONArray jsonArray = mainData.getJSONArray("items");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject dataObj = jsonArray.getJSONObject(i);
                                        SuggestedArtistsModel artistsModel = new SuggestedArtistsModel();
                                        //Set artists property
                                        artistsModel.setArtistUUID(dataObj.getString("uuid"));
                                        artistsModel.setArtistName(dataObj.getString("name"));
                                        artistsModel.setArtistProfilePic(dataObj.getString("profilepicurl"));
                                        dataLIst.add(artistsModel);
                                    }
                                    listener.onSuccess(dataLIst);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                listener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            listener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //Set to false
                            GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS = false;
                        }
                    }));
        } else {
            //Set listener
            listener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * Method to return suggested artists Observable.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     */
    private static Observable<JSONObject> geSuggestedArtistsObservable(String serverURL, String uuid, String authKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers);

        if (GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS) {
            requestBuilder.getResponseOnlyFromNetwork();
        }
        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to return data from the server.
     *
     * @param serverURL              URL of the server.
     * @param uuid                   UUID of the user.
     * @param authKey                AuthKey of user i.e String.*
     * @param lastIndexKey           Last index key
     * @param getResponseFromNetwork Whether to load data from network and cache.
     * @return
     */
    public static Observable<JSONObject> getFeedObservable(String serverURL, String uuid, String authKey, String lastIndexKey, boolean getResponseFromNetwork) {
        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);
        header.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(header)
                .addQueryParameter("lastindexkey", lastIndexKey)
                .addQueryParameter("repostsupport", "yes")
                .addQueryParameter(Constant.PLATFORM_KEY, Constant.PLATFORM_VALUE);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }
        return requestBuilder.build().getJSONObjectObservable();
    }

}
