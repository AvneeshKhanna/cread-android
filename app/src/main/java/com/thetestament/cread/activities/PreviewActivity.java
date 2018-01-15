package com.thetestament.cread.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.thetestament.cread.adapters.FilterAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.CustomFilters;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FilterModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;
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
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMAGE_TINT_COLOR;
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
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    //region Butter knife view binding
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imagePreview)
    ImageView imagePreview;
    @BindView(R.id.etCaption)
    AppCompatEditText etCaption;
    @BindView(R.id.filterBottomSheetView)
    NestedScrollView filterBottomSheetView;
    @BindView(R.id.filterRecyclerView)
    RecyclerView filterRecyclerView;
    //endregion

    //region :Fields and constants
    private BottomSheetBehavior filterSheetBehavior;
    private List<FilterModel> mFilterDataList = new ArrayList<>();
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    Bundle mBundle;
    @State
    String mCalledFrom;
    Bitmap bmp = null;
    //endregion

    //region :Overridden methods
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
        //setup bottom sheets
        filterSheetBehavior = BottomSheetBehavior.from(filterBottomSheetView);
        filterSheetBehavior.setPeekHeight(0);

        initFilterView();
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
        getMenuInflater().inflate(R.menu.menu_preview, menu);
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

            case R.id.action_filter:
                toggleBottomSheet();
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
    //endregion

    /**
     * Update button click functionality
     */
    @OnClick(R.id.buttonUpdate)
    void updateOnClick() {
        if (NetworkHelper.getNetConnectionStatus(PreviewActivity.this)) {
            performUpdateOperation();
            //Show font bottomSheet
        } else {
            //Show no connection message
            ViewHelper.getToast(this, getString(R.string.error_msg_no_connection));
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
    private void updateData(File imgHighRes, File imgLowRes, String shortID, String uuid, String authToken, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String signature, String merchantable, String font, String bold, String italic, String captionText, String imageTintColor) {
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
                .addMultipartParameter("imgtintcolor", imageTintColor)
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

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                                    GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS = true;

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
    private void updateShort(File file, String captureID, String uuid, String authToken, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String merchantable, String font, String bgColor, String bold, String italic, String captionText, String imageTintColor) {

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
                .addMultipartParameter("imgtintcolor", imageTintColor)
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

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = true;
                                    GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS = true;


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
     * Method to update the requested data on server.
     */
    private void performUpdateOperation() {
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
                    , mBundle.getString(PREVIEW_EXTRA_IMAGE_TINT_COLOR)
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
                    , mBundle.getString(PREVIEW_EXTRA_IMAGE_TINT_COLOR)
            );
        } else {
            //do nothing
        }
    }

    /**
     * Method to initialize filter view.
     */
    private void initFilterView() {
        final FilterAdapter adapter = new FilterAdapter(PreviewActivity.this, mFilterDataList);

        adapter.setOnFilterSelectListener(new listener.OnFilterSelectListener() {
            @Override
            public void onFilterSelected(Bitmap bitmap) {
                imagePreview.setImageBitmap(bitmap);
            }
        });

        filterRecyclerView.setLayoutManager(new LinearLayoutManager(PreviewActivity.this, LinearLayoutManager.HORIZONTAL, false));
        filterRecyclerView.setHasFixedSize(true);
        filterRecyclerView.setAdapter(adapter);

        String path = ImageHelper.getImageUri(IMAGE_TYPE_USER_SHORT_PIC).toString();
        path = Uri.parse(path).getPath();
        if (path != null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inMutable = true;
            opts.inJustDecodeBounds = false;


            bmp = BitmapFactory.decodeFile(path, opts);
            opts.inSampleSize = calculateInSampleSize(opts, 300, 300);
            bmp = BitmapFactory.decodeFile(path, opts);


            Handler handler = new Handler();
            Runnable r = new Runnable() {
                public void run() {
                    FilterModel original = new FilterModel("Original", bmp, null);
                    FilterModel starLit = new FilterModel("Star Lit", bmp, CustomFilters.getStarLitFilter());
                    FilterModel blueMess = new FilterModel("Blue Mess", bmp, CustomFilters.getBlueMessFilter());
                    FilterModel aweStruckVibe = new FilterModel("Awe Struck Vibe", bmp, CustomFilters.getAweStruckVibeFilter());
                    FilterModel limeStutter = new FilterModel("Lime Stutter", bmp, CustomFilters.getLimeStutterFilter());
                    FilterModel nightWhisper = new FilterModel("Night Whisper", bmp, CustomFilters.getNightWhisperFilter());
                    FilterModel blackAndWhite = new FilterModel("Black & White", bmp, CustomFilters.getBlackAndWhiteFilter());
                    FilterModel sepia = new FilterModel("Sepia", bmp, CustomFilters.getSepiaFilter());
                    FilterModel amazon = new FilterModel("Amazon", bmp, CustomFilters.getAmazonFilter());
                    FilterModel adele = new FilterModel("Adele", bmp, CustomFilters.getAdeleFilter());
                    FilterModel cruz = new FilterModel("Cruz", bmp, CustomFilters.getCruzFilter());
                    FilterModel metropolis = new FilterModel("Metropolis", bmp, CustomFilters.getMetropolisFilter());
                    FilterModel audrey = new FilterModel("Audrey", bmp, CustomFilters.getAudreyFilter());
                    FilterModel rise = new FilterModel("Rise", bmp, CustomFilters.getRiseFilter(PreviewActivity.this));
                    FilterModel mars = new FilterModel("Mars", bmp, CustomFilters.getMarsFilter());
                    FilterModel april = new FilterModel("April", bmp, CustomFilters.getAprilFilter(PreviewActivity.this));
                    FilterModel han = new FilterModel("Han", bmp, CustomFilters.getHanFilter(PreviewActivity.this));
                    FilterModel oldMan = new FilterModel("Old Man", bmp, CustomFilters.getOldManFilter(PreviewActivity.this));
                    FilterModel clarendon = new FilterModel("Clarendon", bmp, CustomFilters.getClarendonFilter());

                    mFilterDataList.clear();
                    mFilterDataList.add(original); // Original Image
                    mFilterDataList.add(starLit);
                    mFilterDataList.add(blueMess);
                    mFilterDataList.add(aweStruckVibe);
                    mFilterDataList.add(limeStutter);
                    mFilterDataList.add(nightWhisper);
                    mFilterDataList.add(blackAndWhite);
                    mFilterDataList.add(sepia);
                    mFilterDataList.add(amazon);
                    mFilterDataList.add(adele);
                    mFilterDataList.add(cruz);
                    mFilterDataList.add(metropolis);
                    mFilterDataList.add(audrey);
                    mFilterDataList.add(rise);
                    mFilterDataList.add(mars);
                    mFilterDataList.add(april);
                    mFilterDataList.add(han);
                    mFilterDataList.add(oldMan);
                    mFilterDataList.add(clarendon);

                    adapter.notifyDataSetChanged();

                }
            };
            handler.post(r);
        }

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Method to toggle visibility of filter bottomSheet.
     */
    private void toggleBottomSheet() {
        if (filterSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            //Hide bottom sheet
            filterSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (filterSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            //Show bottom sheet
            filterSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

}
