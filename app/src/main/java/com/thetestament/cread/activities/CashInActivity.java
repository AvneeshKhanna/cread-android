package com.thetestament.cread.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import org.fabiomsr.moneytextview.MoneyTextView;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.thetestament.cread.utils.Constant.EXTRA_CASH_IN_AMOUNT;
import static com.thetestament.cread.utils.Constant.EXTRA_MIN_CASH_AMT;


/**
 * Here user can redeem or donate his/her credited amount.
 */

public class CashInActivity extends BaseActivity {

    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.text_cash_in_amount)
    MoneyTextView textCashInAmount;
    @BindView(R.id.text_amount_desc)
    TextView textDesc;

    @State
    double minCashInAmt, creditAmt;

    private SharedPreferenceHelper mHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_in);
        ButterKnife.bind(this);
        //For data retrieval
        retrieveData();

        //initialize preference manager
        mHelper = new SharedPreferenceHelper(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back to previous screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    //Cash In button click functionality
    @OnClick(R.id.button_cash_in)
    public void onCashInButtonClick() {
        //If credit amount is more than min amount
        if (creditAmt >= minCashInAmt) {
            getContactNoDialog();
        } else {
            ViewHelper.getSnackBar(rootView, "Insufficient balance");
        }
    }


    /**
     * Method to show transaction successful dialog.
     */
    private void getTransactionSuccessfulDialog() {
        new MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.CENTER)
                .title("Successful")
                .content("You successfully transferred \u20B9 " + creditAmt + " to your Paytm wallet. ")
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        if (mHelper.isAppRated()) {
                            getRatingDialog();
                        }
                        dialog.dismiss();
                    }
                }).build()
                .show();
    }


    /**
     * Method to show rating dialog.
     */
    private void getRatingDialog() {
        new MaterialDialog.Builder(this)
                .title("Rate us")
                .content(R.string.text_rate_us)
                .positiveText("Rate now")
                .negativeText("Later")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        redirectToPlayStore();
                        //Set rating status to false
                        mHelper.setRatingStatus(false);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build()
                .show();

    }

    /**
     * Method to show dialog to retrieve contact number and perform transaction.
     */
    private void getContactNoDialog() {
        new MaterialDialog.Builder(this)
                .title("")
                .iconRes(R.drawable.img_paytm_logo)
                .content("The amount will be transferred to the paytm wallet linked to this number ")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRange(10, 10)
                .input("Your paytm number", null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        //Perform transactions
                        performTransaction(input.toString());
                    }
                })
                .build()
                .show();
    }


    /**
     * Method to transfer user credited amount into his/her Paytm wallet.
     */
    private void performTransaction(String payTmContactNo) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Processing your request")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();


        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("userpaytmcontact", payTmContactNo);
            jsonObject.put("amount", creditAmt);
        } catch (JSONException e) {
            dialog.dismiss();
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "CashInActivity");
        }
        AndroidNetworking.post(BuildConfig.URL + "/redeem-from-wallet")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            //if token status is invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                //token status is valid
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("success")) {
                                    getTransactionSuccessfulDialog();
                                    ViewHelper.getSnackBar(rootView, "Transfer successful");
                                    textCashInAmount.setAmount(0);
                                    creditAmt = 0;
                                    setResult(RESULT_OK);
                                } else if (dataObject.getString("status").equals("invalid-user")) {
                                    ViewHelper.getSnackBar(rootView, "Paytm account doesn't exist");

                                } else if (dataObject.getString("status").equals("invalid-contact")) {
                                    ViewHelper.getSnackBar(rootView, "Invalid contact number");
                                } else {
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "CashInActivity");
                            dialog.dismiss();
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        dialog.dismiss();
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }


    /**
     * Method to retrieve data from intent and set the views.
     */
    private void retrieveData() {
        //Retrieve data from intent
        minCashInAmt = getIntent().getDoubleExtra(EXTRA_MIN_CASH_AMT, 0);
        creditAmt = getIntent().getDoubleExtra(EXTRA_CASH_IN_AMOUNT, 0);

        //Set Views
        textDesc.setText("You can transfer this amount directly to your paytm wallet. Minimum wallet balance should be \u20b9 " + minCashInAmt);
        textCashInAmount.setAmount((float) creditAmt);
    }

    /**
     * Method to redirect user to Cread app on google play store
     */
    private void redirectToPlayStore() {
        //To get the package name
        String appPackageName = CashInActivity.this.getPackageName();
        try {
            //To redirect to google play store
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            //if play store is not installed
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

}
