package com.thetestament.cread.helpers;

import android.support.v4.app.FragmentActivity;

import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnFollowRequestedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FOLLOWING;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.helpers.NetworkHelper.updateFollowStatusObservable;

public class FollowHelper {

    public void updateFollowStatus(final FragmentActivity context,
                                   CompositeDisposable compositeDisposable,
                                   boolean register,
                                   JSONArray followees,
                                   final OnFollowRequestedListener onFollowRequestedListener
    ) {

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

        requestServer(compositeDisposable,
                updateFollowStatusObservable(spHelper.getUUID()
                        , spHelper.getAuthToken()
                        , register
                        , followees)
                , context
                , new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        onFollowRequestedListener
                                .onFollowFailiure(context
                                        .getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //Set listener
                                onFollowRequestedListener.onFollowFailiure((context.getString(R.string.error_msg_invalid_token)));
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
                                    GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS = true;
                                    GET_RESPONSE_FROM_NETWORK_FOLLOWING = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                                    //Set listener
                                    onFollowRequestedListener.onFollowSuccess();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            //Set listener
                            onFollowRequestedListener.onFollowFailiure((context.getString(R.string.error_msg_internal)));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        //Set listener
                        onFollowRequestedListener.onFollowFailiure((context.getString(R.string.error_msg_server)));

                    }

                    @Override
                    public void onCompleteCalled() {

                        //Do nothing

                    }
                });
    }
}
