package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.thetestament.cread.R;

import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_ASK_ALWAYS;

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
     * Method to store key value pairs in shared preferences.
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
     * Method to update auth token in shared preferences.
     *
     * @param token auth token to store.
     */
    public void setAuthToken(String token) {
        addPreferences(mContext.getString(R.string.auth_token), token);
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
     * @return Value of user first name.
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
     * Method to update Welcome dialog status.
     *
     * @param status boolean value i.e true or false
     */
    public void updateWelcomeDialogStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_welcome_dialog), status);
        editor.apply();
    }

    /**
     * Method to retrieve the "Welcome dialog first time run status".
     *
     * @return True by default
     */
    public boolean isWelcomeFirstTime() {
        return mSharedPreferences
                .getBoolean(mContext.getString(R.string.key_welcome_dialog), true);
    }

    /**
     * Method to update explore intro dialog status.
     *
     * @param status boolean value i.e true or false
     */
    public void updateExploreIntroStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_explore_intro), status);
        editor.apply();
    }

    /**
     * Method to retrieve the explore intro dialog first time run status.
     *
     * @return True by default
     */
    public boolean isExploreIntroFirstTime() {
        return mSharedPreferences
                .getBoolean(mContext.getString(R.string.key_explore_intro), true);
    }


    /**
     * Method to update capture intro dialog status.
     *
     * @param status boolean value i.e true or false
     */
    public void updateCaptureIntroStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_capture_intro), status);
        editor.apply();
    }

    /**
     * Method to retrieve capture intro dialog first time run status.
     *
     * @return True by default
     */
    public boolean isCaptureFirstTime() {
        return mSharedPreferences
                .getBoolean(mContext.getString(R.string.key_capture_intro), true);
    }

    /**
     * Method to update short intro dialog status.
     *
     * @param status boolean value i.e true or false
     */
    public void updateShortIntroStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_short_intro), status);
        editor.apply();
    }

    /**
     * Method to retrieve buy intro dialog first time run status.
     *
     * @return True by default
     */
    public boolean isBuyingFirstTime() {
        return mSharedPreferences
                .getBoolean(mContext.getString(R.string.key_buy_dialog), true);
    }

    public void updateBuyingStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_buy_dialog), status);
        editor.apply();
    }

    /**
     * Method to retrieve short intro dialog first time run status.
     *
     * @return True by default
     */
    public boolean isShortFirstTime() {
        return mSharedPreferences
                .getBoolean(mContext.getString(R.string.key_short_intro), true);
    }

    /**
     * Method to update 'Have button' tooltip status.
     *
     * @param status boolean value i.e true or false
     */
    public void updateHaveButtonToolTipStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_have_tooltip), status);
        editor.apply();
    }

    /**
     * Method to retrieve "Have button " tooltip status.
     *
     * @return True by default.
     */
    public boolean isHaveButtonTooltipFirstTime() {
        return mSharedPreferences
                .getBoolean(mContext.getString(R.string.key_have_tooltip), true);
    }

    /**
     * Method to return watermark status i.e Yes , No and Ask Always.
     *
     * @return Return "Ask Always by default.
     */
    public String getWatermarkStatus() {
        return mSharedPreferences.getString(mContext.getString(R.string.key_watermark_status), WATERMARK_STATUS_ASK_ALWAYS);
    }

    /**
     * Method to update watermark status.
     *
     * @param status Status value to be updated i.e true or false.
     */
    public void setWatermarkStatus(String status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(mContext.getString(R.string.key_watermark_status), status);
        editor.apply();
    }


    /**
     * Method to update capture watermark text in shared preferences.
     *
     * @param watermarkText Capture watermark text.
     */
    public void setCaptureWaterMarkText(String watermarkText) {
        addPreferences(mContext.getString(R.string.key_capture_water_mark_text), watermarkText);
    }

    /**
     * Method to get capture watermark text stored in shared preferences.
     *
     * @return Default value is user name.
     */
    public String getCaptureWaterMarkText() {
        return mSharedPreferences.getString(mContext.getString(R.string.key_last_name), getFirstName() + " " + getLastName());
    }

    /**
     * Method to update 'Write button' tooltip status.
     *
     * @param status boolean value i.e true or false
     */
    public void updateWriteIconToolTipStatus(boolean status) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(mContext.getString(R.string.key_write_tooltip), status);
        editor.apply();
    }

    /**
     * Method to retrieve "Write button " tooltip status.
     *
     * @return True by default.
     */
    public boolean isWriteIconTooltipFirstTime() {
        return mSharedPreferences
                .getBoolean(mContext.getString(R.string.key_write_tooltip), true);
    }


    /**
     * Method to clear all key value pairs in cread shared preferences
     */
    public void clearSharedPreferences() {
        mSharedPreferences.edit().clear().apply();
    }


}
