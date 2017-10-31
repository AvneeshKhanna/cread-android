package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

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
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import okhttp3.OkHttpClient;

import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;

/**
 * Class to show preview of capture screen.
 */

public class CapturePreviewActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageCapture)
    ImageView imageCapture;
    @State
    String mWaterMarkText = "";
    private SharedPreferenceHelper mHelper;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_preview);
        ButterKnife.bind(this);
        //Get sharedPreference
        mHelper = new SharedPreferenceHelper(this);
        //load image
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
        getMenuInflater().inflate(R.menu.menu_capture_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back from this screen
                finish();
                return true;
            case R.id.action_done:
                //Upload image on server
                uploadCapture(new File(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Method to load capture image.
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
        if (helper.getWatermarkStatus()) {
            //Show watermark dialog
            getWaterWorkDialog();
        } else {
            //do nothing
        }
    }

    /**
     * Method to show watermark dialog.
     */
    private void getWaterWorkDialog() {
        new MaterialDialog.Builder(this)
                .content("Do you wish to add your watermark?")
                .positiveText(R.string.text_yes)
                .negativeText(R.string.text_no)
                .checkBoxPrompt("Remember this", false, null)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Save watermark status
                        mHelper.setWatermarkStatus(!dialog.isPromptCheckBoxChecked());
                        //Dismiss this dialog
                        dialog.dismiss();
                        //Show input dialog
                        showWaterMarkInputDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Save watermark status
                        mHelper.setWatermarkStatus(!dialog.isPromptCheckBoxChecked());
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
                .title("Watermark")
                .autoDismiss(false)
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
    private void uploadCapture(File file) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your capture")
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
                                    ViewHelper.getToast(CapturePreviewActivity.this, "Your capture uploaded.");
                                    //finish this activity
                                    finish();
                                } else {
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
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
