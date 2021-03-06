package com.thetestament.cread.activities;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.activities.MainActivity.phoneLogin;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_PIC_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_TOP_USER_INTERESTS;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_BIO;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_CONTACT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_EMAIL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_FIRST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_INTERESTS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_INTERESTS_COUNT;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_LAST_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_WATER_MARK_STATUS;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_WEB_STORE_LINK;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_USER_INTERESTS;
import static com.thetestament.cread.utils.Constant.USER_INTERESTS_CALLED_FROM_PROFILE;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_ASK_ALWAYS;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_NO;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_YES;

/**
 * Here user can view or edit his/her profile basic details.
 */
public class UpdateProfileDetailsActivity extends BaseActivity {

    //region :Views binding with Butter knife
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
    @BindView(R.id.textWebStoreUrl)
    AppCompatTextView textWebStoreUrl;
    @BindView(R.id.etContact)
    TextInputEditText etContact;
    @BindView(R.id.spinnerWaterMark)
    AppCompatSpinner spinnerWaterMark;
    @BindView(R.id.progressView)
    View progressView;
    @BindView(R.id.textUserInterests)
    AppCompatTextView textUserInterests;
    //endregion


    //region Fields and constant
    @State
    String mFirstName, mLastName, mEmail, mBio, mContact, mWaterMarkStatus, mWaterMarkText = "";

    /**
     * Flag to store wen store link.
     */
    @State
    String mWebStoreLink;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mPreferenceHelper;
    UpdateProfileDetailsActivity mContext;

    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_details);
        //Bind views
        ButterKnife.bind(this);
        //Obtain this reference
        mContext = this;
        //Get reference
        mPreferenceHelper = new SharedPreferenceHelper(mContext);
        //Method called
        initScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
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
        // Handling the result of fb mobile verification
        if (requestCode == Constant.REQUEST_CODE_FB_ACCOUNT_KIT) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if (loginResult.getError() != null) {
                ViewHelper.getSnackBar(rootView, loginResult.getError().getUserFacingMessage());
            } else if (loginResult.wasCancelled()) {
                ViewHelper.getSnackBar(rootView, "Contact number updation cancelled");
            } else {
                // showing dialog
                MaterialDialog dialog = CustomDialog.getProgressDialog(mContext
                        , "Updating Contact Number");
                // get phone number from account kit
                getPhoneNo(dialog);
            }

        } else if (requestCode == REQUEST_CODE_USER_INTERESTS && resultCode == RESULT_OK) {
            ArrayList<String> interests = data.getStringArrayListExtra(Constant.EXTRA_USER_INTERESTS_DATA);
            textUserInterests.setText(getUserInterestsMessage(interests.size(), interests));
        }
    }

    //endregion

    //region :Click functionality

    /**
     * Save button onClick functionality to update user details server.
     */
    @OnClick(R.id.buttonSave)
    void onSaveButtonClick() {
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

            switch (spinnerWaterMark.getSelectedItemPosition()) {
                case 0:
                    mWaterMarkStatus = "YES";
                    mPreferenceHelper.setWatermarkStatus(WATERMARK_STATUS_YES);
                    break;
                case 1:
                    mWaterMarkStatus = "NO";
                    mPreferenceHelper.setWatermarkStatus(WATERMARK_STATUS_NO);
                    break;
                case 2:
                    mWaterMarkStatus = "ASK_ALWAYS";
                    mPreferenceHelper.setWatermarkStatus(WATERMARK_STATUS_ASK_ALWAYS);
                    break;
            }

            //Check connection status
            if (getNetConnectionStatus(mContext)) {
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
    void onLockIconClick() {
        //To show prompt dialog
        new MaterialDialog.Builder(mContext)
                .title("Update Contact number")
                .content("Changing the Contact number requires validation using verification code. Do you wish to proceed?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        phoneLogin(mContext);
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
     * Edit interests on click functionality
     */
    @OnClick(R.id.buttonAddInterests)
    void onAddInterestsOnClick() {
        Intent intent = new Intent(this, UserInterestIntroductionActivity.class);
        intent.putExtra(EXTRA_USER_INTERESTS_CALLED_FROM, USER_INTERESTS_CALLED_FROM_PROFILE);
        intent.putExtra(EXTRA_PROFILE_PIC_URL, getIntent().getStringExtra(EXTRA_PROFILE_PIC_URL));
        startActivityForResult(intent, REQUEST_CODE_USER_INTERESTS);
    }


    /**
     * Watermark text onClick functionality.
     */
    @OnClick(R.id.textCopyRight)
    void watermarkOnClick() {
        //Method called
        showWaterMarkInputDialog();
    }

    /**
     * Click functionality to copy web store link
     */
    @OnClick(R.id.iconCopy)
    void onCopyIconClick() {
        // Gets a handle to the clipboard service.
        ClipboardManager manager = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        // Creates a new text clip to put on the clipboard
        ClipData clip = ClipData.newPlainText("webStoreLink", mWebStoreLink);
        // Set the clipboard's primary clip.
        manager.setPrimaryClip(clip);
        ViewHelper.getSnackBar(rootView, "Link copied to clipboard");
    }

    /**
     * Web profile link click functionality.
     */
    @OnClick(R.id.btnInfoWebProfileLink)
    void onWebProfileLinkOnClick() {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .title("Web Profile Link")
                .positiveText("Copy link")
                .customView(R.layout.dialog_profile_lweb_ink, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // Gets a handle to the clipboard service.
                        ClipboardManager manager = (ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        // Creates a new text clip to put on the clipboard
                        ClipData clip = ClipData.newPlainText("webStoreLink", mWebStoreLink);
                        // Set the clipboard's primary clip.
                        manager.setPrimaryClip(clip);
                        ViewHelper.getSnackBar(rootView, "Link copied to clipboard");
                    }
                })
                .show();
        AppCompatTextView linkText = dialog.getCustomView().findViewById(R.id.textProfileLink);
        linkText.setText(mWebStoreLink +
                "\n\nThis is the web version of the profile.");
    }
    //endregion

    //region :Private methods

    /**
     * Method to show input dialog where user enters his/her watermark.
     */
    private void showWaterMarkInputDialog() {
        new MaterialDialog.Builder(mContext)
                .title("Signature")
                .autoDismiss(false)
                .inputRange(1, 20, ContextCompat.getColor(UpdateProfileDetailsActivity.this, R.color.red))
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String s = String.valueOf(input).trim();
                        if (s.length() < 1) {
                            ViewHelper.getToast(UpdateProfileDetailsActivity.this, "This field can't be empty");
                        } else {
                            //Dismiss
                            dialog.dismiss();
                            mWaterMarkText = s;
                            //Save watermark text
                            mPreferenceHelper.setCaptureWaterMarkText(mWaterMarkText);
                        }
                    }
                })
                .build()
                .show();
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
        // Create an ArrayAdapter using the string array and a default spinner dialogParentView for water mark spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.water_mark_items, android.R.layout.simple_spinner_item);
        // Specify the dialogParentView to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    /**
     * To retrieve data from intent and set data to respective views.
     */
    private void retrieveIntentData() {
        //Obtain web store link
        mWebStoreLink = getIntent().getStringExtra(EXTRA_USER_WEB_STORE_LINK);
        textWebStoreUrl.setText(mWebStoreLink);

        mFirstName = getIntent().getStringExtra(EXTRA_USER_FIRST_NAME);
        etFirstName.setText(mFirstName);

        mContact = getIntent().getStringExtra(EXTRA_USER_CONTACT);
        etContact.setText(mContact);

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

        mWaterMarkStatus = getIntent().getStringExtra(EXTRA_USER_WATER_MARK_STATUS);
        int position;
        switch (mWaterMarkStatus) {
            case "YES":
                position = 0;
                break;
            case "NO":
                position = 1;
                break;
            case "ASK_ALWAYS":
                position = 2;
                break;
            default:
                position = 1;
                break;
        }
        spinnerWaterMark.setSelection(position);

        // get interest count
        long interestCount = getIntent().getLongExtra(EXTRA_USER_INTERESTS_COUNT, 0);
        // set message
        textUserInterests.setText(getUserInterestsMessage(interestCount, getIntent().getStringArrayListExtra(EXTRA_TOP_USER_INTERESTS)));

    }

    /**
     * Method to save user profile details on server.
     */
    public void saveUserDetails() {
        SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
        //Show progress view
        progressView.setVisibility(View.VISIBLE);

        JSONObject jsonObject = new JSONObject();
        JSONObject userObject = new JSONObject();

        try {
            //User data
            userObject.put("firstname", mFirstName);
            userObject.put("lastname", mLastName);
            userObject.put("email", mEmail);
            userObject.put("bio", mBio);
            userObject.put("watermarkstatus", mWaterMarkStatus);
            userObject.put("watermark", mWaterMarkText);
            //Request data
            jsonObject.put("uuid", helper.getUUID());
            jsonObject.put("authkey", helper.getAuthToken());
            jsonObject.put("userdata", userObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "UpdateProfileDetailsActivity");
            progressView.setVisibility(View.GONE);
        }

        Rx2AndroidNetworking.post(BuildConfig.URL + "/user-profile/update-profile")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        //Dismiss progress indicator
                        progressView.setVisibility(View.GONE);
                        try {
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                //Show token invalid status
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
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
                                    ViewHelper.getToast(mContext, "Details saved");
                                    //Finish this activity and navigate back to previous screen
                                    finish();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UpdateProfileDetailsActivity");
                            //Show error snack bar
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        //Dismiss progress indicator
                        progressView.setVisibility(View.GONE);
                        //Show server error message
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * Method to get phone number from account kit.
     *
     * @param dialog instance of the dialog.
     */
    private void getPhoneNo(final MaterialDialog dialog) {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {

                PhoneNumber number = account.getPhoneNumber();
                String phoneNo = number.toString();

                updateContactNumber(phoneNo, dialog);
            }

            @Override
            public void onError(AccountKitError accountKitError) {

                dialog.dismiss();
                ViewHelper.getSnackBar(rootView, accountKitError.getUserFacingMessage());
            }
        });
    }

    /**
     * Method to update the contact number on the server.
     *
     * @param newNumber the updated number which has been verified
     * @param dialog    instance of the dialog
     */
    private void updateContactNumber(final String newNumber, final MaterialDialog dialog) {

        SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
        JSONObject jsonObject = new JSONObject();

        try {
            //Request data
            jsonObject.put("uuid", helper.getUUID());
            jsonObject.put("authkey", helper.getAuthToken());
            jsonObject.put("phone", newNumber);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "UpdateProfileDetailsActivity");
            dialog.dismiss();
        }

        Rx2AndroidNetworking.post(BuildConfig.URL + "/user-profile/update-phone")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        dialog.dismiss();
                        try {
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject mainObject = jsonObject.getJSONObject("data");
                                if (mainObject.getString("status").equals("done")) {
                                    etContact.setText(newNumber);
                                    ViewHelper.getSnackBar(rootView, "Contact number updated");
                                } else if (mainObject.getString("status").equals("phone-exists")) {
                                    ViewHelper.getSnackBar(rootView, "Contact number you entered already exists");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UpdateProfileDetailsActivity");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }


                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "UpdateProfileDetailsActivity");
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onComplete() {
                        // do nothing
                    }
                });
    }


    /**
     * Returns the message to be displayed in interest section depending on the no of interests
     *
     * @param interestCount
     * @param interests
     * @return
     */
    private String getUserInterestsMessage(long interestCount, ArrayList<String> interests) {
        if (interestCount == 0) {
            return "Click on Edit to add your interests";
        } else if (interestCount == 1) {
            return interests.get(0);
        } else if (interestCount == 2) {
            return interests.get(0) + ", " + interests.get(1);
        } else if (interestCount == 3) {
            return interests.get(0) + ", " + interests.get(1) + " and 1 other";
        } else {
            return interests.get(0) + ", " + interests.get(1) + " and " + String.valueOf(interestCount - 2) + " others";
        }
    }

//endregion
}
