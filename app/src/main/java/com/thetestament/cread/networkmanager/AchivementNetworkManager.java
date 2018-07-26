package com.thetestament.cread.networkmanager;

import android.support.v4.app.FragmentActivity;

import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ACHIEVEMENTS;

/**
 * Network manager class to provide utility methods related to 'Badge' network operation.
 */

public class AchivementNetworkManager {


    /**
     * Interface definition for a callback to be invoked when user request for 'Achievements' data.
     */
    public interface OnAchievementsLoadListener {
        /**
         * @param jsonObject JsonObject
         */
        void onSuccess(JSONObject jsonObject);

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }


    /**
     * RxJava2 implementation  to retrieve achievements data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param listener            listener reference.
     * @param requestedUUID       UUID of user whose badges to be loaded.
     */
    public static void getAchievementsData(final FragmentActivity context, CompositeDisposable compositeDisposable, String requestedUUID,
                                           final OnAchievementsLoadListener listener) {
        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //If device is connected to network
        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getAchievementsObservable(BuildConfig.URL + "/badges/load"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken()
                    , GET_RESPONSE_FROM_NETWORK_ACHIEVEMENTS
                    , requestedUUID)
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
                                    //Set success listener
                                    listener.onSuccess(jsonObject);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                listener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            //Set failure listener
                            listener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    })
            );
        } else {
            //Set listener
            listener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * Method to return achievements data from the server.
     *
     * @param serverURL              URL of the server.
     * @param uuid                   UUID of the user.
     * @param authKey                Authentication key of the user.
     * @param getResponseFromNetwork If true then get receive response from network,
     * @param requestedUUID          UUID of user whose badges to be loaded.
     * @return Observable<JSONObject>
     */
    public static Observable<JSONObject> getAchievementsObservable(String serverURL, String uuid
            , String authKey, boolean getResponseFromNetwork, String requestedUUID) {

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);


        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addQueryParameter("requesteduuid", requestedUUID)
                .addHeaders(headers);

        if (getResponseFromNetwork) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }
}

