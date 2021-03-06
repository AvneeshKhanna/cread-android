package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.fragments.SettingsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_HATSOFF;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;

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

        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        Boolean key_settings_hatsoffsound = defaultSharedPreferences
                .getBoolean(SettingsFragment.KEY_SETTINGS_HATSOFFSOUND, true);

        // if hatsoff true play sound
        if (isHatsOff && key_settings_hatsoffsound) {
            MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.hatsoff);
            //Listener for track completion
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
            //Play sound
            mediaPlayer.start();
        }



        final JSONObject jsonObject = new JSONObject();
        SharedPreferenceHelper helper = new SharedPreferenceHelper(mContext);

        try {
            jsonObject.put("uuid", helper.getUUID());
            jsonObject.put("authkey", helper.getAuthToken());
            jsonObject.put("entityid", entityID);
            jsonObject.put("register", isHatsOff);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "HatsOffHelper");
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

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_HATSOFF = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;

                                    //Set listener
                                    onHatsOffSuccessListener.onSuccess();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "HatsOffHelper");
                            //Set listener
                            onHatsOffFailureListener.onFailure(mContext.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "HatsOffHelper");
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
