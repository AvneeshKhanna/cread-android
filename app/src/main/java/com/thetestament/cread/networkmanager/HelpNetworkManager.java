package com.thetestament.cread.networkmanager;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.models.HelpModel;

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

/**
 * Network manager class to provide utility methods related to 'Help' network operation.
 */

public class HelpNetworkManager {


    /**
     * Interface definition for a callback to be invoked when user request for 'Help' data.
     */
    public interface OnHelpDataLoadListener {

        /**
         * @param dataList Explore category data list.
         */
        void onSuccess(List<HelpModel> dataList);

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }


    /**
     * Interface definition for a callback to be invoked when user updates feedback data.
     */
    public interface OnFeedBackUpdateListener {

        void onSuccess();

        /**
         * @param errorMsg Error message to be displayed.
         */
        void onFailure(String errorMsg);
    }


    /**
     * RxJava2 implementation for retrieving 'Help' data.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param loadListener        listener reference.
     */
    public static void getHelpData(final Context context, CompositeDisposable compositeDisposable
            , final OnHelpDataLoadListener loadListener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //Labels data list
        final List<HelpModel> dataLIst = new ArrayList<>();

        //If device is connected to network
        if (NetworkHelper.getNetConnectionStatus(context)) {
            compositeDisposable.add(getHelpObservable(BuildConfig.URL + "/support/load-ques"
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
                                        HelpModel model = new HelpModel();
                                        //Set help property
                                        model.setHelpID(dataObj.getString("qid"));
                                        model.setTitleText(dataObj.getString("question"));
                                        model.setDescText(dataObj.getString("answer"));
                                        model.setBtnText(dataObj.getString("action_txt"));
                                        model.setExpanded(false);
                                        dataLIst.add(model);
                                    }
                                    loadListener.onSuccess(dataLIst);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "HelpNetworkManager");
                                loadListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "HelpNetworkManager");
                            //Set failure listener
                            loadListener.onFailure(context.getString(R.string.error_msg_server));
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    })
            );
        } else {
            //Set listener
            loadListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * RxJava2 implementation for Help feedback update.
     *
     * @param context             Context to use.
     * @param compositeDisposable CompositeDisposable reference.
     * @param qaID                Question ID.
     * @param loadListener        listener reference.
     */
    public static void updateHelpFeedbackData(final FragmentActivity context, CompositeDisposable compositeDisposable, String qaID
            , final OnFeedBackUpdateListener loadListener) {

        //Obtain SharedPreferenceHelper reference
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        //If device is connected to network
        if (NetworkHelper.getNetConnectionStatus(context)) {

            //Show dialog
            final MaterialDialog dialog = CustomDialog.getProgressDialog(context
                    , "Saving feedback...");

            compositeDisposable.add(getHelpFeedBackObservable(BuildConfig.URL + "/support/update-ans"
                    , spHelper.getUUID()
                    , spHelper.getAuthToken()
                    , qaID)
                    //Run on a background thread
                    .subscribeOn(Schedulers.io())
                    //Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<JSONObject>() {
                        @Override
                        public void onNext(JSONObject jsonObject) {
                            //Dismiss dialog
                            dialog.dismiss();
                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    loadListener.onFailure(context.getString(R.string.error_msg_invalid_token));
                                } else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    if (mainData.getString("status").equals("done")) {
                                        //Set success listener
                                        loadListener.onSuccess();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "HelpNetworkManager");
                                loadListener.onFailure(context.getString(R.string.error_msg_internal));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "HelpNetworkManager");
                            //Set failure listener
                            loadListener.onFailure(context.getString(R.string.error_msg_server));
                            //Dismiss dialog
                            dialog.dismiss();
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }
                    })
            );
        } else {
            //Set listener
            loadListener.onFailure(context.getString(R.string.error_msg_no_connection));
        }

    }


    /**
     * Method to return 'Help' data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     * @return
     */
    private static Observable<JSONObject> getHelpObservable(String serverURL, String uuid, String authKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", uuid);
        headers.put("authkey", authKey);

        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers);

        if (false) {
            requestBuilder.getResponseOnlyFromNetwork();
        }
        return requestBuilder
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to return data from the server.
     *
     * @param serverURL URL of the server.
     * @param uuid      UUID of the user.
     * @param authKey   AuthKey of user.
     * @param qaID      Question ID.
     */
    public static Observable<JSONObject> getHelpFeedBackObservable(String serverURL, String uuid, String authKey, String qaID) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authKey);
            jsonObject.put("qid", qaID);

        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "HelpNetworkManager");
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable();
    }

}
