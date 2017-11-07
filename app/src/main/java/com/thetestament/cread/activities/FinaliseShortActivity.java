package com.thetestament.cread.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
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

/**
 * Here user finalize his/her shorts and uploads on the server.
 */

public class FinaliseShortActivity extends BaseActivity implements ColorChooserDialog.ColorCallback {


    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageShort)
    ImageView imageShort;
    @BindView(R.id.textShort)
    TextView textShort;

    @BindView(R.id.imageContainer)
    SquareView squareView;

    @State
    String mShortText, mPicUrl;
    /**
     * Flag to maintain gravity status i.e 0 for center , 1 for right and 2 for left.
     */
    @State
    int mGravityFlag = 0;
    //Initially text gravity is "CENTER"
    TextGravity textGravity = TextGravity.CENTER;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalise_short);
        //ButterKnife view binding
        ButterKnife.bind(this);
        //initialize screen
        // initScreen();
        //Short text touch listener
        initTouchListener(textShort);
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
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        //Change text color
        textShort.setTextColor(selectedColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    /**
     * Post button click functionality.
     */
    @OnClick(R.id.btnPost)
    public void onBtnPostClicked() {
        generateImage();
        //Todo post btn functionality
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
        mShortText = getIntent().getStringExtra("");
        mPicUrl = getIntent().getStringExtra("");

        //Set text
        textShort.setText(mShortText);
        //Load inspiration image
        Picasso.with(this)
                .load(mPicUrl)
                .error(R.drawable.image_placeholder)
                .into(imageShort);
    }

    /**
     * Initialize touch listener for 'Short text'
     *
     * @param textView View where touch listener to be
     */
    private void initTouchListener(final TextView textView) {

        textView.setOnTouchListener(new View.OnTouchListener() {
            int initialX = 0;
            int initialY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = (int) motionEvent.getX();
                        initialY = (int) motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:

                        int currentX = (int) motionEvent.getX();
                        int currentY = (int) motionEvent.getY();

                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();

                        int left = lp.leftMargin + (currentX - initialX);
                        int top = lp.topMargin + (currentY - initialY);
                        int right = lp.rightMargin - (currentX - initialX);
                        int bottom = lp.bottomMargin - (currentY - initialY);

                        lp.rightMargin = right;
                        lp.leftMargin = left;
                        lp.bottomMargin = bottom;
                        lp.topMargin = top;

                        textView.setLayoutParams(lp);
                        break;
                    default:
                        break;
                }

                return true;
            }
        });
    }

    private void generateImage() {

        squareView.setDrawingCacheEnabled(true);
        squareView.destroyDrawingCache();
        squareView.buildDrawingCache();
        Bitmap bm = squareView.getDrawingCache();

        try {
            // File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            // FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, new FileOutputStream(new File("/sdcard/ImageAfterAddingText.jpg")));
            // out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                ViewHelper.getToast(FinaliseShortActivity.this, getString(R.string.error_msg_no_image));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
*/

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
                                    ViewHelper.getToast(FinaliseShortActivity.this, "Your Short uploaded.");
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
