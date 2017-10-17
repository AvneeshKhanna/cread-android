package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

import com.thetestament.cread.R;

public class SharedPreferenceHelper {

    SharedPreferences sharedPreferences;
    Context mContext;

    public SharedPreferenceHelper(Context context) {

        sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.cread_preferences), context.MODE_PRIVATE);
        mContext = context;
    }

    /**
     * method to store key value pairs in shared preferences
     * @param key Shared preferences key
     * @param value the value to be stored
     */
    private void addPreferences(String key, String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    /**
     * setter method to store uuid in shared preferences
     * @param uuid uuid to store
     */
    public void setUUID(String uuid) {

        addPreferences(mContext.getString(R.string.uuid),uuid);
    }

    /**
     * getter method to get uuid stored in shared preferences
     * @return value of uuid
     */
    public String getUUID()
    {
        return sharedPreferences.getString(mContext.getString(R.string.uuid),null);
    }

    /**
     * setter method to store auth token in shared preferences
     * @param token auth token to store
     */
    public void setAuthToken(String token)
    {
        addPreferences(mContext.getString(R.string.auth_token),token);
    }

    /**
     * getter method to get the auth token stored in shared preferences
     * @return value of stored auth token
     */
    public String getAuthToken()
    {
        return sharedPreferences.getString(mContext.getString(R.string.auth_token),null);
    }


}
