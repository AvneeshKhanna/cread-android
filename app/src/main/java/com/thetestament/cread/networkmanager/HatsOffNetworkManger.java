package com.thetestament.cread.networkmanager;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.fragments.SettingsFragment;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

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
 * Network manager class to provide utility methods related to 'HatsOff' network operation.
 */

public class HatsOffNetworkManger {

    /**
     * Interface definition for a callback to be invoked when hatsOff status updated on server.
     */
    public interface OnHatsOffResponseListener {
        void onSuccess();

        void onFailure(String errorMsg);
    }


    /**
     * Method to update hats off status.
     *
     * @param context   Context to use.
     * @param entityID  Entity ID  of the content.
     * @param isHatsOff boolean true if user has given hats off to campaign, false otherwise.
     * @param listener
     */
    public static void updateHatsOffStatus(final FragmentActivity context, String entityID, boolean isHatsOff, final OnHatsOffResponseListener listener) {

        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Boolean keySettingsHatsOffSound = defaultSharedPreferences
                .getBoolean(SettingsFragment.KEY_SETTINGS_HATSOFFSOUND, true);

        // if hatsOff true play sound
        if (isHatsOff && keySettingsHatsOffSound) {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.hatsoff);
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
        SharedPreferenceHelper helper = new SharedPreferenceHelper(context);
        try {
            jsonObject.put("uuid", helper.getUUID());
            jsonObject.put("authkey", helper.getAuthToken());
            jsonObject.put("entityid", entityID);
            jsonObject.put("register", isHatsOff);
        } catch (JSONException e) {
            e.printStackTrace();
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
                                listener.onFailure(context.getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    // set feeds data to be loaded from network instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_HATSOFF = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                                    //Set listener
                                    listener.onSuccess();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Set listener
                            listener.onFailure(context.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        //Set listener
                        listener.onFailure(context.getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Do nothing
                    }
                });

    }

}
