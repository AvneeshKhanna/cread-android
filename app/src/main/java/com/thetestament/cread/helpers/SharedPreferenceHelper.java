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
     * Method to update user first name in shared preferences.
     *
     * @param firstName User first name.
     */
    public void setFirstName(String firstName) {
        addPreferences(mContext.getString(R.string.key_first_name), firstName);
    }

    /**
     * Method to get the user first name stored in shared preferences.
     *
     * @return Value of stored user first name.
     */
    public String getFirstName() {
        return mSharedPreferences.getString(mContext.getString(R.string.key_first_name), null);
    }

    /**
     * Method to update user last name in shared preferences.
     *
     * @param lastName User last name.
     */
    public void setLastName(String lastName) {
        addPreferences(mContext.getString(R.string.key_last_name), lastName);
    }

    /**
     * Method to get the user last name stored in shared preferences.
     *
     * @return Value of stored user last name.
     */
    public String getLastName() {
        return mSharedPreferences.getString(mContext.getString(R.string.key_last_name), null);
    }

    /**
     * Method to update auth token in shared preferences
     *
     * @param token auth token to store
     */
    public void setAuthToken(String token) {
        addPreferences(mContext.getString(R.string.auth_token), token);
    }

    /**
     * Method to return watermark status i.e true for checked false otherwise.
     *
     * @return Return true by default.
     */
    public boolean getWatermarkStatus() {
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_watermark_status), true);
    }

    /**
     * Method to update capture status.
     *
     * @param status Status value to be updated i.e true or false.
     */
    public void setWatermarkStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_watermark_status), status);
        editor.apply();
    }

    /**
     * Method to clear all key value pairs in cread shared preferences
     */
    public void clearSharedPreferences() {
        mSharedPreferences.edit().clear().apply();
    }
}
