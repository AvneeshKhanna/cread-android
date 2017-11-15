package com.thetestament.cread.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.OnDragTouchListener;
import com.thetestament.cread.utils.SquareView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_INSPIRATION_CLICKED;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_SHORT_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_INSPIRATION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_WRITE_EXTERNAL_STORAGE;

/**
 * Here user creates his/her shorts and uploads on the server.
 */

public class ShortActivity extends BaseActivity implements ColorChooserDialog.ColorCallback {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageContainer)
    SquareView squareView;
    @BindView(R.id.imageShort)
    ImageView imageShort;
    @BindView(R.id.textShort)
    EditText textShort;
    @BindView(R.id.textWaterMark)
    TextView textWaterMark;

    @State
    String mShortText, mCaptureUrl, mCaptureID = "";
    @State
    boolean mIsMerchantable =true;
    @State
    int mImageWidth = 650;

    /**
     * Flag to maintain gravity status i.e 0 for center , 1 for right and 2 for left.
     */
    @State
    int mGravityFlag = 0;

    //ENUM for text gravity
    private enum TextGravity {
        Center, East, West
    }

    //Initially text gravity is "CENTER"
    TextGravity textGravity = TextGravity.Center;
    /**
     * Flag for switching b/w edit mode and drag mode.
     * 0 for edit mode and 1 for drag mode.
     */
    int mToggleMovement = 0;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    int[] location = new int[2];

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short);
        //ButterKnife view binding
        ButterKnife.bind(this);
        mHelper = new SharedPreferenceHelper(this);
        //initialize screen
        initScreen();

        textShort.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                int[] newLocation = new int[2];
                view.getLocationOnScreen(newLocation);

                /** Do whatever is needed with old and new locations **/
                location = newLocation;

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_INSPIRATION_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getBundleExtra(EXTRA_DATA);
                    //Retrieve data
                    mCaptureID = bundle.getString(EXTRA_CAPTURE_ID);
                    mCaptureUrl = bundle.getString(EXTRA_CAPTURE_URL);
                    mIsMerchantable = bundle.getBoolean(EXTRA_MERCHANTABLE);
                    //Load inspiration/capture image
                    loadCapture(imageShort, mCaptureUrl);
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateImage();
            } else {
                ViewHelper.getToast(this, getString(R.string.error_msg_permission_denied));
            }
        }
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
        getMenuInflater().inflate(R.menu.menu_short, menu);

        final ImageView imageView = (ImageView) menu.findItem(R.id.action_toggle).getActionView();
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit_24));
        //Screen opened for first time
        if (mHelper.isToggleButtonTooltipFirstTime()) {
            ViewHelper.getToolTip(imageView, "Here goes .....", this);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toggle mode i.e edit to drag and vice versa
                if (mToggleMovement == 0) {
                    //Set drag listener
                    textShort.setOnTouchListener(new OnDragTouchListener(textShort));
                    //Hide edit text cursor
                    textShort.setCursorVisible(false);
                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textShort.getWindowToken(), 0);
                    //Change icon
                    imageView.setImageDrawable(ContextCompat.getDrawable(ShortActivity.this, R.drawable.ic_drag_24));
                    //Change flag
                    mToggleMovement = 1;
                } else {
                    //Remove drag listener
                    textShort.setOnTouchListener(null);
                    //Shoe edit text cursor
                    textShort.setCursorVisible(true);
                    //Show keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textShort, 0);
                    //Change icon
                    imageView.setImageDrawable(ContextCompat.getDrawable(ShortActivity.this, R.drawable.ic_edit_24));
                    // item.setIcon(R.drawable.ic_edit_24);
                    //Change flag
                    mToggleMovement = 0;
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back to previous screen
                finish();
                return true;
            case R.id.action_next:
                getRuntimePermission();
                return true;
            case R.id.action_toggle:
                //Toggle mode i.e edit to drag and vice versa
                if (mToggleMovement == 0) {
                    //Set drag listener
                    textShort.setOnTouchListener(new OnDragTouchListener(textShort));
                    //Hide edit text cursor
                    textShort.setCursorVisible(false);
                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textShort.getWindowToken(), 0);
                    //Change icon
                    item.setIcon(R.drawable.ic_drag_24);
                    //Change flag
                    mToggleMovement = 1;
                } else {
                    //Remove drag listener
                    textShort.setOnTouchListener(null);
                    //Shoe edit text cursor
                    textShort.setCursorVisible(true);
                    //Show keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textShort, 0);
                    //Change icon
                    item.setIcon(R.drawable.ic_edit_24);
                    //Change flag
                    mToggleMovement = 0;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        //Change short text color
        textShort.setTextColor(selectedColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        //do nothing
    }

    /**
     * Inspire me button click functionality to open InspirationActivity.
     */
    @OnClick(R.id.btnInspireMe)
    public void onBtnInspireClicked() {
        startActivityForResult(new Intent(this, InspirationActivity.class)
                , REQUEST_CODE_INSPIRATION_ACTIVITY);
        //Log firebase event
        Bundle bundle = new Bundle();
        bundle.putString("uuid", mHelper.getUUID());
        FirebaseAnalytics.getInstance(this).logEvent(FIREBASE_EVENT_INSPIRATION_CLICKED, bundle);
    }

    /**
     * Functionality to toggle the text gravity.
     */
    @OnClick(R.id.btnLAlignText)
    public void onBtnLAlignTextClicked(ImageView btnAlignText) {

        switch (mGravityFlag) {
            case 0:
                //Set text gravity
                textShort.setGravity(Gravity.RIGHT);
                //Change button drawable
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_right_32));
                //Change gravity flag
                mGravityFlag = 1;
                //Set gravity variable
                textGravity = TextGravity.East;
                break;
            case 1:
                textShort.setGravity(Gravity.LEFT);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_left_32));
                mGravityFlag = 2;
                textGravity = TextGravity.West;
                break;
            case 2:
                textShort.setGravity(Gravity.CENTER);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_center_32));
                mGravityFlag = 0;
                textGravity = TextGravity.Center;
                break;
        }
    }

    /**
     * Click functionality to increase 'Short text' size by five unit..
     */
    @OnClick(R.id.btnFormatTextSizePlus)
    public void onBtnFormatTextSizePlusClicked() {
        int ts = (int) textShort.getTextSize() + 5;
        //Increase text size by 5 unit
        textShort.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
    }

    /**
     * Click functionality to decrease 'Short text' by five unit.
     */
    @OnClick(R.id.btnFormatTextSizeMinus)
    public void onBtnFormatTextSizeMinusClicked() {
        int ts = (int) textShort.getTextSize() - 5;
        //Decrease text size by five unit
        textShort.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
    }

    /**
     * Click functionality to show material color palette dialog.
     */
    @OnClick(R.id.btnFormatTextColor)
    public void onBtnFormatTextColorClicked() {
        // Pass a context, along with the title of the dialog
        new ColorChooserDialog.Builder(this, R.string.text_color)
                // title of dialog when viewing shades of a color
                .titleSub(R.string.text_color)
                // when true, will display accent palette instead of primary palette
                .accentMode(false)
                // changes label of the done button
                .doneButton(R.string.md_done_label)
                // changes label of the cancel button
                .cancelButton(R.string.md_cancel_label)
                // changes label of the back button
                .backButton(R.string.md_back_label)
                // defaults to true, false will disable changing action buttons' color to currently selected color
                .dynamicButtonColor(true)
                .show(); // an AppCompatActivity which implements ColorCallback
    }

    /**
     * Method to retrieve data from intent and initialize this screen.
     */
    private void initScreen() {
        if (getIntent().hasExtra(EXTRA_DATA)) {
            Bundle bundle = getIntent().getBundleExtra(EXTRA_DATA);
            //Retrieve data
            mCaptureID = bundle.getString(EXTRA_CAPTURE_ID);
            mCaptureUrl = bundle.getString(EXTRA_CAPTURE_URL);
            mIsMerchantable = bundle.getBoolean(EXTRA_MERCHANTABLE);
            //Load inspiration/capture image
            loadCapture(imageShort, mCaptureUrl);
        }

        //Set water mark text
        textWaterMark.setText(mHelper.getFirstName() + " " + mHelper.getLastName());
    }

    /**
     * Method to load capture image.
     *
     * @param imageView View where image will be loaded.
     * @param imageUrl  URL of image to be loaded.
     */
    private void loadCapture(ImageView imageView, final String imageUrl) {
        Picasso.with(this)
                .load(imageUrl)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.with(ShortActivity.this).load(imageUrl).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                //Get image width
                                mImageWidth = bitmap.getWidth();
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    /**
     * Method to get WRITE_EXTERNAL_STORAGE permission and perform specified operation.
     */
    private void getRuntimePermission() {
        //Check for WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this
                , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ViewHelper.getToast(this
                        , "Please grant storage permission from settings to create your short");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            }
        }
        //If permission is granted
        else {
            generateImage();
        }
    }

    /**
     * Method to generate short image and show its preview.
     */
    private void generateImage() {
        //Hide edit text cursor
        textShort.setCursorVisible(false);


        //Enable drawing cache
        squareView.setDrawingCacheEnabled(true);
        squareView.buildDrawingCache();
        Bitmap bm = squareView.getDrawingCache();

        //Scaled bitmap
        Bitmap bitmap = Bitmap.createScaledBitmap(bm, mImageWidth, mImageWidth, true);
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Cread/Short/short_pic.jpg");
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.close();
            //Show preview
            showShortPreview();
        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        //Disable drawing cache
        squareView.setDrawingCacheEnabled(false);
        squareView.destroyDrawingCache();

    }

    /**
     * Method to show preview of generated image.
     */
    private void showShortPreview() {


        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_short_preview, false)
                .title("Preview")
                .positiveText("Upload")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();
                        Rect myViewRect = new Rect();
                        textShort.getGlobalVisibleRect(myViewRect);
                        float x = myViewRect.left;
                        float y = myViewRect.top;
                        //Update details on server
                        updateShort(new File(getImageUri(IMAGE_TYPE_USER_SHORT_PIC).getPath())
                                , mCaptureID
                                // , String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getLeft()))
                                , String.valueOf(scaledPixelsToPx(ShortActivity.this, location[0]))
                                //, String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getTop()))
                                , String.valueOf(scaledPixelsToPx(ShortActivity.this, location[1]))
                                , String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getWidth()))
                                , String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getHeight()))
                                , textShort.getText().toString()
                                , String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getTextSize()))
                                , Integer.toHexString(textShort.getCurrentTextColor())
                                , textGravity.toString()
                                , String.valueOf(mImageWidth)
                        );
                    }
                })
                .show();


        ImageView imagePreview = dialog.getCustomView().findViewById(R.id.imageShortPreview);
        // TextView buttonUpload = dialog.getCustomView().findViewById(R.id.buttonUpload);
        //Click listener
        /*buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
        //Load preview image
        Picasso.with(this)
                .load(getImageUri(IMAGE_TYPE_USER_SHORT_PIC))
                .error(R.drawable.image_placeholder)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imagePreview);
    }

    /**
     * Update short image and other details on server.
     */
    private void updateShort(File file, String captureID, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth) {

        int merchantable;
        if (mIsMerchantable) {
            merchantable = 1;
        } else {
            merchantable = 0;
        }

        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your short")
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
                .addMultipartParameter("uuid", mHelper.getUUID())
                .addMultipartParameter("authkey", mHelper.getAuthToken())
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
                .addMultipartParameter("textgravity", textGravity)
                .addMultipartParameter("merchantable", String.valueOf(merchantable))
                .addMultipartParameter("top", String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getTop())))
                .addMultipartParameter("bottom", String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getBottom())))
                .addMultipartParameter("left", String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getLeft())))
                .addMultipartParameter("right", String.valueOf(scaledPixelsToPx(ShortActivity.this, textShort.getRight())))
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
                                    ViewHelper.getToast(ShortActivity.this, "Short uploaded successfully.");
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

    public static float scaledPixelsToPx(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
