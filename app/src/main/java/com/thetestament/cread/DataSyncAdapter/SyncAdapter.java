package com.thetestament.cread.DataSyncAdapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.database.NotificationsDBFunctions;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by prakharchandna on 19/02/18.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {


    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    Context mContext;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();

    }


    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient
            contentProviderClient, SyncResult syncResult) {


        Log.d("tag", "onPerformSync: called ");

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(mContext);

        // check whether the user is logged in
        if (spHelper.getUUID() != null && spHelper.getAuthToken() != null) {

            NotificationsDBFunctions notificationsDBFunctions = new NotificationsDBFunctions(mContext);
            notificationsDBFunctions.accessNotificationsDatabase();

            // update data on server
            updateDataOnServer(spHelper.getUUID(),
                    spHelper.getAuthToken(),
                    notificationsDBFunctions.getUserActionsData(spHelper.getUUID()));

        }
    }


    private void updateDataOnServer(final String uuid, String authkey, JSONArray userActions) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("uuid", uuid);
            jsonObject.put("authkey", authkey);
            jsonObject.put("user_events", userActions);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        AndroidNetworking.post(BuildConfig.URL + "/user-events/save")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Token status is invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                // do nothing
                            } else {
                                JSONObject mainData = response.getJSONObject("data");

                                if (mainData.getString("status").equals("done")) {

                                    // data uploaded successfully
                                    // delete users data
                                    NotificationsDBFunctions notificationsDBFunctions = new NotificationsDBFunctions(mContext);
                                    notificationsDBFunctions.accessNotificationsDatabase();

                                    notificationsDBFunctions.deleteUserActionsData(uuid);

                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                        FirebaseCrash.report(anError);
                    }
                });
    }


}
