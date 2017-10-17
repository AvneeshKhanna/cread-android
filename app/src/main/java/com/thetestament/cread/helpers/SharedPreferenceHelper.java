package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.thetestament.cread.R;

/**
 * A utility class for managing SharedPreferences keys.
 */
public class SharedPreferenceHelper {

    SharedPreferences mSharedPreferences;
    Context mContext;

    /**
     * Required constructor.
     *
     * @param mContext Context to use.
     */
    public SharedPreferenceHelper(Context mContext) {
        this.mContext = mContext;
        mSharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.cread_preferences)
                , mContext.MODE_PRIVATE);
    }

    /**
     * Method to store key value pairs in shared preferences
     *
     * @param key   Shared preferences key
     * @param value The value to be stored
     */
    private void addPreferences(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Method to get uuid stored in shared preferences.
     *
     * @return Value of uuid.
     */
    public String getUUID() {
        return mSharedPreferences.getString(mContext.getString(R.string.uuid), null);
    }

    /**
     * Method to update uuid in shared preferences.
     *
     * @param uuid uuid to store.
     */
    public void setUUID(String uuid) {
        addPreferences(mContext.getString(R.string.uuid), uuid);
    }

    /**
     * Method to get the auth token stored in shared preferences.
     *
     * @return Value of stored auth token.
     */
    public String getAuthToken() {
        return mSharedPreferences.getString(mContext.getString(R.string.auth_token), null);
    }

    /**
     * Method to update auth token in shared preferences
     *
     * @param token auth token to store
     */
    public void setAuthToken(String token) {
        addPreferences(mContext.getString(R.string.auth_token), token);
    }


}
