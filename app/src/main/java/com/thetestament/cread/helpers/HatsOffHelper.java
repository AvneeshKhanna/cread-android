package com.thetestament.cread.helpers;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Helper class to update HatsOff status.
 */

public class HatsOffHelper {


    private Context mContext;
    private OnHatsOffSuccessListener onHatsOffSuccessListener;
    private OnHatsOffFailureListener onHatsOffFailureListener;

    /**
     * Interface definition for a callback to be invoked when hatsOff status successfully gets updated on server.
     */
    public interface OnHatsOffSuccessListener {
        void onSuccess();
    }

    /**
     * Interface definition for a callback to be invoked when hatsOff failed.
     */
    public interface OnHatsOffFailureListener {
        void onFailure(String errorMsg);
    }

    /**
     * Register a callback to be invoked when hatsOff status successfully gets updated on server.
     */
    public void setOnHatsOffFailureListener(OnHatsOffFailureListener onHatsOffFailureListener) {
        this.onHatsOffFailureListener = onHatsOffFailureListener;
    }

    /**
     * Register a callback to be invoked when hatsOff failed.
     */
    public void setOnHatsOffSuccessListener(OnHatsOffSuccessListener onHatsOffSuccessListener) {
        this.onHatsOffSuccessListener = onHatsOffSuccessListener;
    }

    /**
     * Required constructor
     *
     * @param mContext Constructor to use.
     */
    public HatsOffHelper(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Method to update hats off status.
     *
     * @param entityID  Entity ID  of the content.
     * @param isHatsOff boolean true if user has given hats off to campaign, false otherwise.
     */
    public void updateHatsOffStatus(String entityID, boolean isHatsOff) {
        final JSONObject jsonObject = new JSONObject();
        SharedPreferenceHelper helper = new SharedPreferenceHelper(mContext);

        try {
            jsonObject.put("uuid", helper.getUUID());
            jsonObject.put("authkey", helper.getAuthToken());
            jsonObject.put("entityid", entityID);
            jsonObject.put("register", isHatsOff);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        Rx2AndroidNetworking.post(BuildConfig.URL + "/hatsoff/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //Set listener
                                onHatsOffFailureListener.onFailure(mContext.getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Set listener
                                    onHatsOffSuccessListener.onSuccess();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            //Set listener
                            onHatsOffFailureListener.onFailure(mContext.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Set listener
                        onHatsOffFailureListener.onFailure(mContext.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Do nothing
                    }
                });

    }

}
