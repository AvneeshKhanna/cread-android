package com.thetestament.cread.helpers;

import android.support.v4.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
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

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_HASHTAG_OF_THE_DAY;

/**
 * A helper class to provide utility methods for HashTagOfTheDay.
 */

public class HashTagOfTheDayHelper {

    /**
     * Method to retrieve HashTahOfTheDay data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param listener            OnHashTagOfTheDayLoadListener reference
     */
    public void getHatsOfTheDay(final FragmentActivity context
            , CompositeDisposable compositeDisposable
            , final listener.OnHashTagOfTheDayLoadListener listener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        compositeDisposable.add(getHatsOfTheDayDataFromServer(BuildConfig.URL + "/hashtag/day/load"
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
                                listener.onSuccess(mainData.getString("htag"), mainData.getLong("hpostcount"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "HashTagOfTheDayHelper");
                            listener.onFailure(context.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "HashTagOfTheDayHelper");
                        listener.onFailure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Update flag
                        GET_RESPONSE_FROM_NETWORK_HASHTAG_OF_THE_DAY = false;
                    }
                }));
    }


    /**
     * Method to return HashTahOfTheDay data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     */
    public static Observable<JSONObject> getHatsOfTheDayDataFromServer(String serverURL, String uuid, String authKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers);

        if (GET_RESPONSE_FROM_NETWORK_HASHTAG_OF_THE_DAY) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }

}
