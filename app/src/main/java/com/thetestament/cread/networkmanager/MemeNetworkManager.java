package com.thetestament.cread.networkmanager;

import android.content.Context;

import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.models.MemeImageModel;

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

/**
 * Network manager class to provide utility methods related to 'Meme' network operation.
 */

public class MemeNetworkManager {


    /**
     * Interface definition for a callback to be invoked when user request for 'Meme images' data.
     */
    public interface OnMemeImageLoadListener {

        /**
         * @param jsonObject Meme images jsonObject.
         */
        void onSuccess(JSONObject jsonObject);

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }

    /**
     * RxJava2 implementation for retrieving 'Meme image' data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param loadListener        listener reference.
     * @param lastIndexKey        Last index key.
     */
    public static void getMemeImageData(final Context context, CompositeDisposable compositeDisposable, String lastIndexKey
            , final OnMemeImageLoadListener loadListener) {
        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //Labels data list
        final List<MemeImageModel> dataLIst = new ArrayList<>();
        //fixme update url , keys  extra
        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getMemeImageObservable(BuildConfig.URL + "/meme/load"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken()
                    , lastIndexKey
                    , false)
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
                                    loadListener.onSuccess(jsonObject);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                loadListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            //Set failure listener
                            loadListener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                        }
                    })
            );
        } else {
            //Set listener
            loadListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * Method to return data of meme images from the server.
     *
     * @param serverURL              URL of the server.
     * @param uuid                   UUID of the user.
     * @param authKey                AuthKey of user i.e String.
     * @param lastIndexKey           Last index key
     * @param getResponseFromNetwork boolean which indicate whether to load from network or cache.
     * @return
     */
    public static Observable<JSONObject> getMemeImageObservable(String serverURL, String uuid, String authKey
            , String lastIndexKey, boolean getResponseFromNetwork) {

        Map<String, String> header = new HashMap<>();
        header.put("uuid", uuid);
        header.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(header)
                .addQueryParameter("lastindexkey", lastIndexKey);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }
        return requestBuilder.build().getJSONObjectObservable();
    }

}
