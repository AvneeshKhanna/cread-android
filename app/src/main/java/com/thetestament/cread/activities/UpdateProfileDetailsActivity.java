package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

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
import static com.thetestament.cread.utils.Constant.EXTRA_USER_CONTACT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_COPY_RIGHT_STATUS;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_EMAIL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_FIRST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_LAST_NAME;


public class UpdateProfileDetailsActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.etFirstName)
    TextInputEditText etFirstName;
    @BindView(R.id.etLastName)
    TextInputEditText etLastName;
    @BindView(R.id.etEmail)
    TextInputEditText etEmail;
    @BindView(R.id.etContact)
    TextInputEditText etContact;
    @BindView(R.id.switchButton)
    SwitchCompat btnCopyRight;

    @State
    String mFirstName, mLastName, mContact, mEmail;
    @State
    boolean mCopyRightStatus;

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
     * Save button onClick functionality.
     */
    @OnClick(R.id.buttonSave)
    public void onSaveButtonClick(Button button) {
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

            //Check for copyright switch
            if (btnCopyRight.isChecked()) {
                mCopyRightStatus = true;
            } else {
                mCopyRightStatus = false;
            }

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
                .title("Update mContact number")
                .content("Changing the mContact number requires validation using verification code. Do you wish to proceed?")
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

        try {
            jsonObject.put("uuid", helper.getUUID());
            jsonObject.put("authkey", helper.getUUID());
            jsonObject.put("firstname", mFirstName);
            jsonObject.put("lastname", mLastName);
            jsonObject.put("email", mEmail);
            jsonObject.put("copyrightstatus", mCopyRightStatus);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            dialog.dismiss();
        }
        //Todo change server url
        AndroidNetworking.post(BuildConfig.URL + "/user-profile/update/")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("tokenstatus").equals("invalid")) {
                                dialog.dismiss();
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                dialog.dismiss();
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    Intent returnIntent = getIntent();
                                    Bundle returnData = new Bundle();
                                    //returnData.putString(EXTRA_USER_FIRST_NAME, mFirstName);
                                    //returnData.putString(EXTRA_USER_LAST_NAME, mLastName);

                                    returnIntent.putExtras(returnData);
                                    setResult(RESULT_OK, returnIntent);
                                    Toast.makeText(UpdateProfileDetailsActivity.this
                                            , "Details saved"
                                            , Toast.LENGTH_SHORT)
                                            .show();
                                    finish();
                                } else {
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Dismiss progress indicator
                        dialog.dismiss();
                        //Show server error message
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to initialize this screen.
     */
    private void initScreen() {
        //Disable contact view
        etContact.setEnabled(false);
        //Get data from intent
        retrieveIntentData();
        //set view data/text
        populateViews();

    }

    /**
     * To retrieve data from intent.
     */
    private void retrieveIntentData() {
        //Get data from intent
        mFirstName = getIntent().getStringExtra(EXTRA_USER_FIRST_NAME);
        mEmail = getIntent().getStringExtra(EXTRA_USER_EMAIL);
        mContact = getIntent().getStringExtra(EXTRA_USER_CONTACT);
        mCopyRightStatus = getIntent().getBooleanExtra(EXTRA_USER_COPY_RIGHT_STATUS, false);

        //If last name is not null
        if (!getIntent().getStringExtra(EXTRA_USER_LAST_NAME).equals("null")) {
            mLastName = getIntent().getStringExtra(EXTRA_USER_LAST_NAME);
            //set last name
            etLastName.setText(mLastName);
        }
    }

    /**
     * Method to populate views.
     */
    private void populateViews() {
        //Populate TextInputLayoutWith data
        etFirstName.setText(mFirstName);
        etLastName.setText(mLastName);
        etContact.setText(mContact);
        etEmail.setText(mEmail);
        btnCopyRight.setChecked(mCopyRightStatus);
    }

}
