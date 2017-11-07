package com.thetestament.cread.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.Picasso;
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

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
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

    @State
    String mShortText, mPicUrl;

    //Initially text gravity is "CENTER"
    TextGravity textGravity = TextGravity.CENTER;
    /**
     * Flag to maintain gravity status i.e 0 for center , 1 for right and 2 for left.
     */
    @State
    int mGravityFlag = 0;

    int mToggleMovement = 0;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short);
        //ButterKnife view binding
        ButterKnife.bind(this);
        //initialize screen
        // initScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateImage();
            } else {
                ViewHelper.getToast(this
                        , "The app won't function properly since the permission for storage was denied.");
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
                //// TODO: Upload functionality

                 /*updateShort(new File("")
                , String.valueOf(textShort.getX())
                , String.valueOf(textShort.getY())
                , String.valueOf(textShort.getWidth())
                , String.valueOf(textShort.getHeight())
                , textShort.getText().toString()
                , String.valueOf(textShort.getTextSize())
                , Integer.toHexString(textShort.getCurrentTextColor())
                , textGravity.toString()
        );*/
                return true;

            case R.id.action_toggle:
                //
                if (mToggleMovement == 0) {
                    textShort.setOnTouchListener(new OnDragTouchListener(textShort));
                    textShort.setCursorVisible(false);
                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textShort.getWindowToken(), 0);
                    //Change icon
                    item.setIcon(R.drawable.ic_drag_24);
                    mToggleMovement = 1;
                } else {
                    textShort.setOnTouchListener(null);
                    textShort.setCursorVisible(true);
                    //Show keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textShort, 0);
                    //Change icon
                    item.setIcon(R.drawable.ic_edit_24);
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
    }

    /**
     * Post button click functionality.
     */
    @OnClick(R.id.btnInspireMe)
    public void onBtnPostClicked() {
        getRuntimePermission();
    }

    /**
     * Click functionality to toggle the text gravity.
     */
    @OnClick(R.id.btnLAlignText)
    public void onBtnLAlignTextClicked(ImageView btnAlignText) {

        switch (mGravityFlag) {
            case 0:
                textShort.setGravity(Gravity.RIGHT);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_right_32));
                mGravityFlag = 1;
                textGravity = TextGravity.RIGHT;
                break;
            case 1:
                textShort.setGravity(Gravity.LEFT);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_left_32));
                mGravityFlag = 2;
                textGravity = TextGravity.LEFT;
                break;
            case 2:
                textShort.setGravity(Gravity.CENTER);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_center_32));
                mGravityFlag = 0;
                textGravity = TextGravity.CENTER;
                break;
        }
    }

    /**
     * Click functionality to increase 'Short text' by one sp.
     */
    @OnClick(R.id.btnFormatTextSizePlus)
    public void onBtnFormatTextSizePlusClicked() {
        int ts = (int) textShort.getTextSize() + 5;
        //Increase text size by one sp
        textShort.setTextSize(TypedValue.COMPLEX_UNIT_PX, ts);
    }

    /**
     * Click functionality to decrease 'Short text' by one sp.
     */
    @OnClick(R.id.btnFormatTextSizeMinus)
    public void onBtnFormatTextSizeMinusClicked() {

        int ts = (int) textShort.getTextSize() - 5;
        //Decrease text size by one sp
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
        //Retrieve data
        mShortText = getIntent().getStringExtra("shortText");
        mPicUrl = getIntent().getStringExtra("pictureUrl");

        //Set text
        textShort.setText(mShortText);
        //Load inspiration image
        Picasso.with(this)
                .load(mPicUrl)
                .error(R.drawable.image_placeholder)
                .into(imageShort);
    }


    private void generateImage() {

        int ratio = 3000 / squareView.getWidth();

        squareView.setDrawingCacheEnabled(true);
        squareView.buildDrawingCache();
        Bitmap bm = squareView.getDrawingCache();

        Bitmap bitmap = Bitmap.createScaledBitmap(bm, 3000, 3000, false);
        File file = null;
        try {
            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ImageAfterAddingText_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        squareView.setDrawingCacheEnabled(false);
        squareView.destroyDrawingCache();

        openScreenshot(file);
        /*Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.image_placeholder);

        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.valueOf(textGravity.toString()));
        paint.setTextSize(textShort.getTextSize());
        paint.setColor(textShort.getCurrentTextColor());
        paint.setStyle(Paint.Style.FILL);
        canvas.drawBitmap(src, 0f, 0f, null);
        float height = paint.measureText(textShort.getText().toString());
        float width = paint.measureText(textShort.getText().toString());
        float x_coord = (src.getWidth() - width) / 2;
        canvas.drawText(textShort.getText().toString(), x_coord, height + 15f, paint); // 15f is to put space between top edge and the text, if you want to change it, you can
        try {
            dest.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File("/sdcard/ImageAfterAddingText.jpg")));
            // dest is Bitmap, if you want to preview the final image, you can display it on screen also before saving
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/




/*
        Picasso.with(this).load(mPicUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ViewHelper.getToast(ShortActivity.this, getString(R.string.error_msg_no_image));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
*/

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

    private void openScreenshot(File imageFile) {
        Uri uri = FileProvider.getUriForFile(ShortActivity.this, BuildConfig.APPLICATION_ID + ".provider", imageFile);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION); //must for reading data from directory

        startActivity(intent);
    }


    /**
     * Update short image and other details on server.
     */
    private void updateShort(File file, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity) {
        SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
        //Configure OkHttpClient for time out
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.MINUTES)
                .readTimeout(20, TimeUnit.MINUTES)
                .writeTimeout(20, TimeUnit.MINUTES)
                .build();

        Rx2AndroidNetworking.upload(BuildConfig.URL + "/short-upload")
                .addMultipartFile("short-image", file)
                .addMultipartParameter("uuid", helper.getUUID())
                .addMultipartParameter("authkey", helper.getAuthToken())
                .addMultipartParameter("dx", xPosition)
                .addMultipartParameter("dy", yPosition)
                .addMultipartParameter("width", tvWidth)
                .addMultipartParameter("hieght", tvHeight)
                .addMultipartParameter("text", text)
                .addMultipartParameter("textsize", textSize)
                .addMultipartParameter("textcolor", textColor)
                .addMultipartParameter("textgravity", textGravity)
                .setOkHttpClient(okHttpClient)
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
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    ViewHelper.getToast(ShortActivity.this, "Your Short uploaded.");
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
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private enum TextGravity {
        CENTER, RIGHT, LEFT
    }
}
