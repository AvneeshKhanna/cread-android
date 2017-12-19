package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.AppCompatEditText;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.dialog.CustomDialog;
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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_SHORT_PIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_AUTH_KEY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BG_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BOLD;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_COLLABORATION;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_SHORT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_FONT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMG_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ITALIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_SHORT_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_SIGNATURE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_GRAVITY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_SIZE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TV_HEIGHT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TV_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_UUID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_X_POSITION;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_Y_POSITION;

/**
 * AppcompatActivity to show preview and option to write caption.
 */

public class PreviewActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imagePreview)
    ImageView imagePreview;
    @BindView(R.id.etCaption)
    AppCompatEditText etCaption;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    Bundle mBundle;
    @State
    String mCalledFrom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);
        //Retrieve data intent
        mBundle = getIntent().getBundleExtra(PREVIEW_EXTRA_DATA);
        //Set variable
        mCalledFrom = mBundle.getString(PREVIEW_EXTRA_CALLED_FROM);
        //Load image
        loadPreviewImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
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
                CustomDialog.getBackNavigationDialog(PreviewActivity.this
                        , "Discard changes?"
                        , "If you go back now, you will loose your changes.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        //Show prompt dialog
        CustomDialog.getBackNavigationDialog(PreviewActivity.this
                , "Discard changes?"
                , "If you go back now, you will loose your changes.");
    }

    /**
     * Update button click functionality
     */
    @OnClick(R.id.buttonUpdate)
    void updateOnClick() {
        /*if (TextUtils.getTrimmedLength(etCaption.getText().toString()) == 0) {
            //Show toast message
            ViewHelper.getToast(this, "Caption can't be empty. Please Write something.");
        }*/ /*else {*/
        if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_COLLABORATION)) {
            updateData(new File(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath())
                    , new File(getImageUri(IMAGE_TYPE_USER_SHORT_PIC).getPath())
                    , mBundle.getString(PREVIEW_EXTRA_SHORT_ID)
                    , mBundle.getString(PREVIEW_EXTRA_UUID)
                    , mBundle.getString(PREVIEW_EXTRA_AUTH_KEY)
                    , mBundle.getString(PREVIEW_EXTRA_X_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_Y_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_TV_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_TV_HEIGHT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_SIZE)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_GRAVITY)
                    , mBundle.getString(PREVIEW_EXTRA_IMG_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_SIGNATURE)
                    , mBundle.getString(PREVIEW_EXTRA_MERCHANTABLE)
                    , mBundle.getString(PREVIEW_EXTRA_FONT)
                    , mBundle.getString(PREVIEW_EXTRA_BOLD)
                    , mBundle.getString(PREVIEW_EXTRA_ITALIC)
                    , etCaption.getText().toString()
            );
        } else if (mCalledFrom.equals(PREVIEW_EXTRA_CALLED_FROM_SHORT)) {
            updateShort(new File(getImageUri(IMAGE_TYPE_USER_SHORT_PIC).getPath())
                    , mBundle.getString(PREVIEW_EXTRA_CAPTURE_ID)
                    , mBundle.getString(PREVIEW_EXTRA_UUID)
                    , mBundle.getString(PREVIEW_EXTRA_AUTH_KEY)
                    , mBundle.getString(PREVIEW_EXTRA_X_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_Y_POSITION)
                    , mBundle.getString(PREVIEW_EXTRA_TV_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_TV_HEIGHT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_SIZE)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_TEXT_GRAVITY)
                    , mBundle.getString(PREVIEW_EXTRA_IMG_WIDTH)
                    , mBundle.getString(PREVIEW_EXTRA_MERCHANTABLE)
                    , mBundle.getString(PREVIEW_EXTRA_FONT)
                    , mBundle.getString(PREVIEW_EXTRA_BG_COLOR)
                    , mBundle.getString(PREVIEW_EXTRA_BOLD)
                    , mBundle.getString(PREVIEW_EXTRA_ITALIC)
                    , etCaption.getText().toString()
            );
        } else {
            //do nothing
        }
    }

    /**
     * Method to load preview image.
     */
    private void loadPreviewImage() {
        //Load preview image
        Picasso.with(this)
                .load(getImageUri(IMAGE_TYPE_USER_SHORT_PIC))
                .error(R.drawable.image_placeholder)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imagePreview);
    }

    /**
     * Method to update capture/collaboration details on server.
     */
    private void updateData(File imgHighRes, File imgLowRes, String shortID, String uuid, String authToken, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String signature, String merchantable, String font, String bold, String italic, String captionText) {
        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your capture")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);

        final MaterialDialog dialog = builder.build();
        dialog.show();


        Rx2AndroidNetworking.upload(BuildConfig.URL + "/capture-upload/collaborated")
                .setOkHttpClient(okHttpClient)
                .addMultipartFile("capture-img-high", imgHighRes)
                .addMultipartFile("capture-img-low", imgLowRes)
                .addMultipartParameter("shoid", shortID)
                .addMultipartParameter("uuid", uuid)
                .addMultipartParameter("authkey", authToken)
                .addMultipartParameter("dx", xPosition)
                .addMultipartParameter("dy", yPosition)
                .addMultipartParameter("txt_width", tvWidth)
                .addMultipartParameter("txt_height", tvHeight)
                .addMultipartParameter("img_width", imgWidth)
                .addMultipartParameter("img_height", imgWidth)
                .addMultipartParameter("text", text)
                .addMultipartParameter("textsize", textSize)
                .addMultipartParameter("textcolor", textColor)
                .addMultipartParameter("textgravity", textGravity)
                .addMultipartParameter("watermark", signature)
                .addMultipartParameter("merchantable", merchantable)
                .addMultipartParameter("font", font)
                .addMultipartParameter("bold", bold)
                .addMultipartParameter("italic", italic)
                .addMultipartParameter("caption", captionText)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        //Add disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(PreviewActivity.this, "Capture uploaded successfully.");
                                    setResult(RESULT_OK);
                                    //Navigate back to previous market
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
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //do nothing
                    }
                });
    }

    /**
     * Update short image and other details on server.
     */
    private void updateShort(File file, String captureID, String uuid, String authToken, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String merchantable, String font, String bgColor, String bold, String italic, String captionText) {

        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your writing")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);

        final MaterialDialog dialog = builder.build();
        dialog.show();


        Rx2AndroidNetworking.upload(BuildConfig.URL + "/short-upload")
                .setOkHttpClient(okHttpClient)
                .addMultipartFile("short-image", file)
                .addMultipartParameter("captureid", captureID)
                .addMultipartParameter("uuid", uuid)
                .addMultipartParameter("authkey", authToken)
                .addMultipartParameter("dx", xPosition)
                .addMultipartParameter("dy", yPosition)
                .addMultipartParameter("txt_width", tvWidth)
                .addMultipartParameter("txt_height", tvHeight)
                .addMultipartParameter("img_width", imgWidth)
                .addMultipartParameter("img_height", imgWidth)
                .addMultipartParameter("text", text)
                .addMultipartParameter("textsize", textSize)
                .addMultipartParameter("textcolor", textColor)
                .addMultipartParameter("textgravity", textGravity)
                .addMultipartParameter("merchantable", merchantable)
                .addMultipartParameter("font", font)
                .addMultipartParameter("bgcolor", bgColor)
                .addMultipartParameter("bold", bold)
                .addMultipartParameter("italic", italic)
                .addMultipartParameter("caption", captionText)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        //Add disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(PreviewActivity.this, "Writing uploaded successfully.");
                                    setResult(RESULT_OK);
                                    //Navigate back to previous market
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
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //do nothing
                    }
                });
    }
}
