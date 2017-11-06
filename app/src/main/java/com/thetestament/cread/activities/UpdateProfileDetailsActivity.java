package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_BIO;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_CONTACT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_EMAIL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_FIRST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_LAST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_WATER_MARK_STATUS;

/**
 * Here user can view or edit his/her profile basic details.
 */
public class UpdateProfileDetailsActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.etFirstName)
    TextInputEditText etFirstName;
    @BindView(R.id.etLastName)
    TextInputEditText etLastName;
    @BindView(R.id.etEmail)
    TextInputEditText etEmail;
    @BindView(R.id.etBio)
    TextInputEditText etBio;
    @BindView(R.id.etContact)
    TextInputEditText etContact;
    @BindView(R.id.spinnerWaterMark)
    AppCompatSpinner spinnerWaterMark;

    @State
    String mFirstName, mLastName, mEmail, mBio, mContact, mWaterMarkStatus;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_details);
        ButterKnife.bind(this);
        initScreen();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        }
    }

    /**
     * Save button onClick functionality to update user details server.
     */
    @OnClick(R.id.buttonSave)
    public void onSaveButtonClick() {
        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            etFirstName.requestFocus();
            etFirstName.setError("This field is required");
        } else if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            etEmail.requestFocus();
            etEmail.setError("This field is required");
        } else {
            //Retrieve text
            mFirstName = etFirstName.getText().toString().trim();
            mLastName = etLastName.getText().toString().trim();
            mEmail = etEmail.getText().toString().trim();
            mBio = etBio.getText().toString().trim();
            mWaterMarkStatus = spinnerWaterMark.getSelectedItem().toString();

            //Check connection status
            if (getNetConnectionStatus(UpdateProfileDetailsActivity.this)) {
                //Save user details
                saveUserDetails();
            } else {
                //Show no connection snack bar message
                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
            }
        }
    }

    /**
     * Contact lock icon onClick functionality
     */
    @OnClick(R.id.lockIconContact)
    public void onLockIconClick() {
        //To show prompt dialog
        new MaterialDialog.Builder(this)
                .title("Update Contact number")
                .content("Changing the Contact number requires validation using verification code. Do you wish to proceed?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // TODO: Account kit functionality
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).build()
                .show();
    }

    /**
     * Method to save user profile details on server.
     */
    public void saveUserDetails() {
        SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
        //Material progress dialog
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Saving your details")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0)
                .build();
        dialog.show();
        JSONObject jsonObject = new JSONObject();
        JSONObject userObject = new JSONObject();

        try {
            //User data
            userObject.put("firstname", mFirstName);
            userObject.put("lastname", mLastName);
            userObject.put("email", mEmail);
            userObject.put("bio", mBio);
            userObject.put("watermarkstatus", mWaterMarkStatus);
            //Request data
            jsonObject.put("uuid", helper.getUUID());
            jsonObject.put("authkey", helper.getAuthToken());
            jsonObject.put("userdata", userObject);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            dialog.dismiss();
        }
        AndroidNetworking.post(BuildConfig.URL + "/user-profile/update-profile")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Dismiss progress indicator
                        dialog.dismiss();
                        try {
                            if (response.getString("tokenstatus").equals("invalid")) {
                                //Show token invalid status
                                ViewHelper.getSnackBar(rootView
                                        , getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {

                                    Intent returnIntent = getIntent();
                                    Bundle returnData = new Bundle();
                                    returnData.putString(EXTRA_USER_FIRST_NAME, mFirstName);
                                    returnData.putString(EXTRA_USER_LAST_NAME, mLastName);
                                    returnData.putString(EXTRA_USER_EMAIL, mEmail);
                                    returnData.putString(EXTRA_USER_BIO, mBio);
                                    returnData.putString(EXTRA_USER_WATER_MARK_STATUS, mWaterMarkStatus);
                                    returnIntent.putExtras(returnData);
                                    //Set result ok
                                    setResult(RESULT_OK, returnIntent);
                                    //Show toast
                                    ViewHelper.getToast(UpdateProfileDetailsActivity.this, "Details saved");
                                    //Finish this activity and navigate back to previous screen
                                    finish();
                                } else {
                                    //Show error snack bar
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            //Show error snack bar
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Dismiss progress indicator
                        dialog.dismiss();
                        //Show server error message
                        ViewHelper.getSnackBar(rootView
                                , getString(R.string.error_msg_server));
                        anError.printStackTrace();
                    }
                });
    }

    /**
     * Method to initialize this screen.
     */
    private void initScreen() {
        //Disable contact edit text
        etContact.setEnabled(false);
        //set spinner
        setSpinnerAdapter(spinnerWaterMark);
        //Get data from intent
        retrieveIntentData();
    }

    /**
     * Method to set adapter for spinner depending upon spinner type
     *
     * @param spinner Spinner object
     */
    private void setSpinnerAdapter(AppCompatSpinner spinner) {

        ArrayAdapter<CharSequence> adapter = null;
        // Create an ArrayAdapter using the string array and a default spinner layout for water mark spinner
        adapter = ArrayAdapter.createFromResource(this,
                R.array.water_mark_items, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    /**
     * To retrieve data from intent and set data to respective views.
     */
    private void retrieveIntentData() {

        mFirstName = getIntent().getStringExtra(EXTRA_USER_FIRST_NAME);
        etFirstName.setText(mFirstName);

        mContact = getIntent().getStringExtra(EXTRA_USER_CONTACT);
        etContact.setText(mContact);

        mWaterMarkStatus = getIntent().getStringExtra(EXTRA_USER_WATER_MARK_STATUS);
        spinnerWaterMark.setPrompt(mWaterMarkStatus);

        //If last name is not null
        if (!getIntent().getStringExtra(EXTRA_USER_LAST_NAME).equals("null")) {
            mLastName = getIntent().getStringExtra(EXTRA_USER_LAST_NAME);
            //set last name
            etLastName.setText(mLastName);
        }
        //If email is not present
        if (!getIntent().getStringExtra(EXTRA_USER_EMAIL).equals("null")) {
            mEmail = getIntent().getStringExtra(EXTRA_USER_EMAIL);
            //set email
            etEmail.setText(mEmail);
        }
        //If user bio is not null
        if (!getIntent().getStringExtra(EXTRA_USER_BIO).equals("null")) {
            mBio = getIntent().getStringExtra(EXTRA_USER_BIO);
            //set user bio
            etBio.setText(mBio);
        }
    }

}
