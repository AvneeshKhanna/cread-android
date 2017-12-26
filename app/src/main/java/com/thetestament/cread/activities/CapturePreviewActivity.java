package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import okhttp3.OkHttpClient;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_ASK_ALWAYS;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_NO;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_YES;

/**
 * Class to show preview of capture.
 */

public class CapturePreviewActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageCapture)
    ImageView imageCapture;
    @BindView(R.id.textWaterMark)
    TextView textWaterMark;
    @BindView(R.id.etCaption)
    AppCompatEditText etCaption;

    @State
    String mWaterMarkText = "", isMerchantable;
    private SharedPreferenceHelper mHelper;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_preview);
        ButterKnife.bind(this);
        //Get sharedPreference
        mHelper = new SharedPreferenceHelper(this);
        //Retrieve data from intent
        isMerchantable = getIntent().getStringExtra("isMerchantable");
        //load capture image for preview
        loadCaptureImage();
        checkWatermarkStatus(mHelper);
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
                //Show prompt dialog
                CustomDialog.getBackNavigationDialog(CapturePreviewActivity.this
                        , "Discard capture?"
                        , "If you go back now, you will loose your capture.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //Show prompt dialog
        CustomDialog.getBackNavigationDialog(CapturePreviewActivity.this
                , "Discard capture?"
                , "If you go back now, you will loose your capture.");
    }

    /**
     * Update button click functionality
     */
    @OnClick(R.id.buttonUpdate)
    void updateOnClick() {
        // check net status
        if (NetworkHelper.getNetConnectionStatus(CapturePreviewActivity.this)) {
            //Upload image on server
            uploadCapture(new File(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath())
                    , etCaption.getText().toString());
        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * Method to load capture image for preview.
     */
    private void loadCaptureImage() {
        Picasso.with(this)
                .load(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.ic_account_circle_48)
                .into(imageCapture);
    }

    /**
     * Method to check  whether 'Upload a capture' is clicked for first time or not. If yes then show the appropriate dialog.
     *
     * @param helper SharedPreference reference.
     */
    private void checkWatermarkStatus(SharedPreferenceHelper helper) {
        //Check for first time status
        if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_YES)) {
            mWaterMarkText = helper.getCaptureWaterMarkText();
            textWaterMark.setText(mWaterMarkText);
        } else if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_NO)) {
            //Hide watermark
            textWaterMark.setVisibility(View.GONE);
        } else if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_ASK_ALWAYS)) {
            //Show watermark dialog
            getWaterWorkDialog();
        }
    }

    /**
     * Method to show watermark dialog.
     */
    private void getWaterWorkDialog() {
        new MaterialDialog.Builder(this)
                .content("Do you wish to add your signature? It will be visible on the bottom left of the image.")
                .positiveText(R.string.text_yes)
                .negativeText(R.string.text_no)
                .checkBoxPrompt("Remember this", false, null)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //If checkbox is selected
                        if (dialog.isPromptCheckBoxChecked()) {
                            //Save watermark status
                            mHelper.setWatermarkStatus(WATERMARK_STATUS_YES);
                        }
                        //Dismiss this dialog
                        dialog.dismiss();
                        //Show input dialog
                        showWaterMarkInputDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //If checkbox is selected
                        if (dialog.isPromptCheckBoxChecked()) {
                            //Save watermark status
                            mHelper.setWatermarkStatus(WATERMARK_STATUS_NO);
                        }
                        //Hide watermark
                        textWaterMark.setVisibility(View.GONE);
                        //Dismiss this dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Method to show input dialog where user enters his/her watermark.
     */
    private void showWaterMarkInputDialog() {
        new MaterialDialog.Builder(this)
                .title("Signature")
                .autoDismiss(false)
                .inputRange(1, 20, ContextCompat.getColor(CapturePreviewActivity.this, R.color.red))
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String s = String.valueOf(input).trim();
                        if (s.length() < 1) {
                            ViewHelper.getToast(CapturePreviewActivity.this, "This field can't be empty");
                        } else {
                            //Dismiss
                            dialog.dismiss();
                            mWaterMarkText = s;
                            //Set watermark
                            textWaterMark.setVisibility(View.VISIBLE);
                            textWaterMark.setText(mWaterMarkText);
                            //Save watermark text
                            mHelper.setCaptureWaterMarkText(mWaterMarkText);
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * Method to upload capture on server.
     *
     * @param file File to be saved i.e image file .
     */
    private void uploadCapture(File file, String captionText) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your graphic art")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        //Configure  OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();


        AndroidNetworking.upload(BuildConfig.URL + "/capture-upload")
                .addMultipartFile("captured-image", file)
                .addMultipartParameter("uuid", mHelper.getUUID())
                .addMultipartParameter("authkey", mHelper.getAuthToken())
                .addMultipartParameter("watermark", mWaterMarkText)
                .addMultipartParameter("merchantable", isMerchantable)
                .addMultipartParameter("caption", captionText)
                .setOkHttpClient(okHttpClient)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView,
                                        getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(CapturePreviewActivity.this, "Graphic art uploaded successfully");

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;


                                    //finish this activity
                                    finish();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        dialog.dismiss();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });

    }
}
