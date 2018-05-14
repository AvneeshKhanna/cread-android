package com.thetestament.cread.networkmanager;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.models.LabelsModel;

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

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_HASHTAG_SUGGETION;

/**
 * Network manager class to provide utility methods related to 'Hash tag' network operation.
 */

public class HashTagNetworkManager {


    /**
     * Interface definition for a callback to be invoked when user request for HashTagSuggestion data.
     */
    public interface OnHashTagSuggestionLoadListener {

        /**
         * @param dataList     Hash tag suggestion data list
         * @param maxSelection Maximum no. of labels can be selected.
         */
        void onSuccess(List<LabelsModel> dataList, int maxSelection);

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }

    /**
     * RxJava2 implementation for retrieving hash tag suggestion data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param loadListener
     * @param entityID            Entity ID of the content to be uploaded
     * @param contentType         Type of the content i.e GRAPHIC or WRITING
     */
    public static void getHashTagSuggestionData(final Context context, CompositeDisposable compositeDisposable
            , String entityID, String contentType, final OnHashTagSuggestionLoadListener loadListener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //Labels data list
        final List<LabelsModel> dataLIst = new ArrayList<>();

        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getHashTagSuggestionObservableFromServer(BuildConfig.URL + "/entity-interests/load"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken()
                    , entityID
                    , contentType)
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
                                    //retrieve max selection data
                                    int maxSelection = mainData.getInt("max_selection");
                                    //Suggested artists list
                                    JSONArray jsonArray = mainData.getJSONArray("interests");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject dataObj = jsonArray.getJSONObject(i);
                                        LabelsModel labelsModel = new LabelsModel();
                                        //Set labels property
                                        labelsModel.setLabelsID(dataObj.getString("intid"));
                                        labelsModel.setLabel(dataObj.getString("intname"));
                                        labelsModel.setSelected(dataObj.getBoolean("selected"));
                                        dataLIst.add(labelsModel);
                                    }
                                    loadListener.onSuccess(dataLIst, maxSelection);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(e);
                                loadListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            //Set failure listener
                            loadListener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //Set to false
                            GET_RESPONSE_FROM_NETWORK_HASHTAG_SUGGETION = false;
                        }
                    })
            );
        } else {
            //Set listener
            loadListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * Method to return suggested hash tag data from the server.
     *
     * @param serverURL   URL of the server.
     * @param uuid        UUID of the user.
     * @param authKey     AuthKey of user.
     * @param entityID    Entity ID of the content to be uploaded
     * @param contentType Type of the content i.e GRAPHIC or WRITING
     * @return
     */
    private static Observable<JSONObject> getHashTagSuggestionObservableFromServer(String serverURL, String uuid, String authKey, String entityID, String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityid", entityID);
        queryParams.put("type", contentType);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addQueryParameter(queryParams)
                .addHeaders(headers);

        if (CreadApp.GET_RESPONSE_FROM_NETWORK_HASHTAG_SUGGETION) {
            requestBuilder.getResponseOnlyFromNetwork();
        }
        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }

}
