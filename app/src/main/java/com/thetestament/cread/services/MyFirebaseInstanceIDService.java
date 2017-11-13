package com.thetestament.cread.services;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Log.d(getClass().getSimpleName(), "onTokenRefresh: called");

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(getApplicationContext());

        String uuid = spHelper.getUUID();
        String authkey = spHelper.getAuthToken();

        // to check if the user is logged in
        // send token to server only if the user is logged in
        if(uuid != null && authkey != null )
        {
            updateFcmTokenOnServer(uuid);
        }

    }

    /**
     * Method to update the refreshed fcm token on server
     * @param uuid user id
     */
    private void updateFcmTokenOnServer(String uuid)
    {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Rx2AndroidNetworking.post(BuildConfig.URL + "/user-access/update-fcmtoken")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                        new CompositeDisposable().add(d);
                    }

                    @Override
                    public void onNext(@NonNull JSONObject jsonObject) {

                        // do nothing

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                        e.printStackTrace();
                        FirebaseCrash.report(e);
                    }

                    @Override
                    public void onComplete() {

                        // do nothing
                    }
                });
    }
}
