package com.thetestament.cread.helpers;

import android.support.v4.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.helpers.NetworkHelper.updateUserInterestStatusObservable;
import static com.thetestament.cread.listeners.listener.OnUserInterestClickedListener;

/**
 * Created by prakharchandna on 07/05/18.
 */

public class UserInterestsHelper {


    /**
     * Method to update follow status.
     *
     * @param context                       Context to use.
     * @param compositeDisposable           CompositeDisposable reference.
     * @param register                      Whether user want to follow or un-follow, true for follow false otherwise.
     * @param onUserInterestClickedListener
     */
    public void updateUserInterests(final FragmentActivity context, CompositeDisposable compositeDisposable,
                                    boolean register,
                                    String interestId,
                                    final OnUserInterestClickedListener onUserInterestClickedListener) {

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

        requestServer(compositeDisposable,
                updateUserInterestStatusObservable(spHelper.getUUID()
                        , spHelper.getAuthToken()
                        , register
                        , interestId)
                , context
                , new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        onUserInterestClickedListener
                                .onInterestFailure(context
                                        .getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //Set listener
                                onUserInterestClickedListener.onInterestFailure((context.getString(R.string.error_msg_invalid_token)));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {

                                    // get response from network
                                    CreadApp.GET_RESPONSE_FROM_NETWORK_ME = true;
                                    //Set listener
                                    onUserInterestClickedListener.onInterestSuccess();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UserInterestHelper");
                            //Set listener
                            onUserInterestClickedListener.onInterestFailure((context.getString(R.string.error_msg_internal)));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "UserInterestHelper");
                        //Set listener
                        onUserInterestClickedListener.onInterestFailure((context.getString(R.string.error_msg_server)));
                    }

                    @Override
                    public void onCompleteCalled() {
                        //Do nothing
                    }
                });
    }

}
