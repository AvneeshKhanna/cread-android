package com.thetestament.cread.activities;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FontAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.OnDragTouchListener;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FontModel;
import com.thetestament.cread.utils.SquareView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_SHORT_ID;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_SHORT_PIC;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_ASK_ALWAYS;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_NO;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_YES;
import static com.thetestament.cread.utils.Constant.fontTypes;

/**
 * This class shows the preview of collaboration.
 */

public class CollaborationActivity extends BaseActivity implements ColorChooserDialog.ColorCallback {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageContainer)
    SquareView squareView;
    @BindView(R.id.imageCapture)
    ImageView imageShort;
    @BindView(R.id.textShort)
    TextView textShort;
    @BindView(R.id.textSignature)
    TextView textSignature;
    @BindView(R.id.progressView)
    View viewProgress;
    @BindView(R.id.seekBarTextSize)
    AppCompatSeekBar seekBarTextSize;
    @BindView(R.id.dotBold)
    View dotBold;
    @BindView(R.id.dotItalic)
    View dotItalic;


    @BindView(R.id.bottomSheetView)
    NestedScrollView bottomSheetView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private BottomSheetBehavior sheetBehavior;
    //Define font typeface
    private Typeface mTextTypeface;

    private ArrayList<FontModel> mFontDataList = new ArrayList<>();


    @State
    String mShortID, mIsMerchantable, mSignatureText = "", mFontType = "helvetica_neue_medium.ttf";

    @State
    int mImageWidth = 650;


    /**
     * Flag to maintain bold typeface status i.e 1 for bold ,0 otherwise
     */
    @State
    int mBoldFlag = 0;

    /**
     * Flag to maintain italic typeface status i.e 1 for italic ,0 otherwise
     */
    @State
    int mItalicFlag = 0;
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

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaboration);
        ButterKnife.bind(this);
        //Obtain reference
        mHelper = new SharedPreferenceHelper(this);
        //initialize this screen
        initScreen();
        //initialize seek bar
        initSeekBar(seekBarTextSize);
        //For bottomSheet
        sheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        sheetBehavior.setPeekHeight(0);
        //Set default font
        mTextTypeface = ResourcesCompat.getFont(CollaborationActivity.this, R.font.ubuntu_medium);
        //initialise fontLayout bottomSheet
        initFontLayout();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //For permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collaboration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Show prompt dialog
                CustomDialog.getBackNavigationDialog(CollaborationActivity.this
                        , "Discard changes?"
                        , "If you go back now, you will loose your changes.");
                return true;
            case R.id.action_next:
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    generateImage();
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(this
                                , "Please grant storage permission from settings to create short");
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        //Change short text color
        textShort.setTextColor(selectedColor);
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        //Do nothing
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //Show prompt dialog
        CustomDialog.getBackNavigationDialog(CollaborationActivity.this
                , "Discard changes?"
                , "If you go back now, you will loose your changes.");
    }

    @OnClick(R.id.rootView)
    void rootViewOnClick() {
        //Collapse bottomSheet if its expanded
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
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
     * Functionality to change font type.
     */
    @OnClick(R.id.btnFont)
    void onFontClicked() {
        //Show bottomSheet
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Functionality to  show toast.
     */
    @OnClick(R.id.btnFormatBg)
    void changeBgColor() {
        Toast.makeText(this
                , "Functionality disabled"
                , Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Click functionality to show material color palette dialog.
     */
    @OnClick(R.id.btnFormatTextColor)
    void onBtnFormatTextColorClicked() {
        //Show color dialog
        showColorChooserDialog();
    }


    /**
     * Bold button click functionality to set typeface to bold.
     */
    @OnClick(R.id.btnFormatTextBold)
    void boldBtnOnClick() {
        if (mItalicFlag == 0 && mBoldFlag == 0) {
            //Set typeface to bold
            textShort.setTypeface(mTextTypeface, Typeface.BOLD);
            //Update flag
            mBoldFlag = 1;
            //Toggle dot views visibility
            dotBold.setVisibility(View.VISIBLE);
            dotItalic.setVisibility(View.INVISIBLE);
        } else if (mItalicFlag == 0 && mBoldFlag == 1) {
            //Set typeface to normal
            textShort.setTypeface(mTextTypeface, Typeface.NORMAL);
            //Update flag
            mBoldFlag = 0;
            //Toggle dot views visibility
            dotBold.setVisibility(View.INVISIBLE);
            dotItalic.setVisibility(View.INVISIBLE);
        } else if (mItalicFlag == 1 && mBoldFlag == 0) {
            //Set typeface to bold_italic
            textShort.setTypeface(mTextTypeface, Typeface.BOLD_ITALIC);
            //Update flag
            mBoldFlag = 1;
            //Toggle dot views visibility
            dotBold.setVisibility(View.VISIBLE);
            dotItalic.setVisibility(View.VISIBLE);
        } else if (mItalicFlag == 1 && mBoldFlag == 1) {
            //Set typeface to italic
            textShort.setTypeface(mTextTypeface, Typeface.ITALIC);
            //Update flag
            mBoldFlag = 0;
            //Toggle dot views visibility
            dotBold.setVisibility(View.INVISIBLE);
            dotItalic.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Italic button click functionality to set typeface of content
     */
    @OnClick(R.id.btnFormatTextItalic)
    void italicBtnOnclick() {

        if (mItalicFlag == 0 && mBoldFlag == 0) {
            //Set typeface to italic
            textShort.setTypeface(mTextTypeface, Typeface.ITALIC);
            //Update flag
            mItalicFlag = 1;
        } else if (mItalicFlag == 0 && mBoldFlag == 1) {
            //Set typeface to bold_italic
            textShort.setTypeface(mTextTypeface, Typeface.BOLD_ITALIC);
            //Update flag
            mItalicFlag = 1;
        } else if (mItalicFlag == 1 && mBoldFlag == 0) {
            //Set typeface to normal
            textShort.setTypeface(mTextTypeface, Typeface.NORMAL);
            //Update flag
            mItalicFlag = 0;
        } else if (mItalicFlag == 1 && mBoldFlag == 1) {
            //Set typeface to bold
            textShort.setTypeface(mTextTypeface, Typeface.BOLD);
            //Update flag
            mItalicFlag = 0;
        }

    }

    /**
     * Close button click functionality to hide bottom sheet.
     */
    @OnClick(R.id.buttonClose)
    void onCloseBtnClick() {
        //Hide bottom sheet
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Method to show color chooser dialog.
     */
    private void showColorChooserDialog() {
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
     * Used to handle result of askForPermission for capture.
     */
    PermissionCallback captureWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            generateImage();
        }

        @Override
        public void permissionRefused() {
            ViewHelper.getToast(CollaborationActivity.this
                    , "Please grant storage permission from settings to create short");
        }
    };

    /**
     * Method to initialize this screen.
     */
    private void initScreen() {
        //Retrieve data from intent
        Bundle data = getIntent().getBundleExtra(EXTRA_DATA);
        mShortID = data.getString(EXTRA_SHORT_ID);
        mIsMerchantable = data.getString(EXTRA_MERCHANTABLE);
        //Load capture pic
        loadCaptureImage(imageShort);
        //Check for signature
        checkSignatureStatus(mHelper);
        //Get short data from server
        loadShortData();
        //Set drag listener
        textShort.setOnTouchListener(new OnDragTouchListener(textShort));
    }


    /**
     * Initialize seekBar changeListener.
     *
     * @param appCompatSeekBar SeekBar reference.
     */
    private void initSeekBar(AppCompatSeekBar appCompatSeekBar) {
        appCompatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set text size
                textShort.setTextSize(TypedValue.COMPLEX_UNIT_PX, i + 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });
    }

    /**
     * Method to initialize font bottom sheet
     */
    private void initFontLayout() {
        //initialize font data list
        for (String fontName : fontTypes) {
            FontModel data = new FontModel();
            data.setFontName(fontName);
            mFontDataList.add(data);
        }

        //Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(CollaborationActivity.this, LinearLayoutManager.HORIZONTAL, false));
        //Set adapter
        FontAdapter fontAdapter = new FontAdapter(mFontDataList, CollaborationActivity.this);
        recyclerView.setAdapter(fontAdapter);

        //Font click listener
        fontAdapter.setOnFontClickListener(new listener.OnFontClickListener() {
            @Override
            public void onFontClick(Typeface typeface, String fontType) {
                //Set short text typeface
                if (mItalicFlag == 0 && mBoldFlag == 0) {
                    //Set typeface to bold
                    textShort.setTypeface(typeface, Typeface.NORMAL);
                } else if (mItalicFlag == 0 && mBoldFlag == 1) {
                    //Set typeface to normal
                    textShort.setTypeface(typeface, Typeface.BOLD);

                } else if (mItalicFlag == 1 && mBoldFlag == 0) {
                    //Set typeface to bold_italic
                    textShort.setTypeface(typeface, Typeface.ITALIC);
                } else if (mItalicFlag == 1 && mBoldFlag == 1) {
                    //Set typeface to italic
                    textShort.setTypeface(typeface, Typeface.BOLD_ITALIC);
                }

                //set typeface
                mTextTypeface = typeface;
                //Set font type
                mFontType = fontType;
            }
        });
    }

    /**
     * Method to load capture image for preview.
     */
    private void loadCaptureImage(ImageView imageView) {
        Picasso.with(this)
                .load(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.image_placeholder)
                .into(imageView);
    }

    /**
     * Method to check signature status and show appropriate dialog.
     *
     * @param helper SharedPreference reference.
     */
    private void checkSignatureStatus(SharedPreferenceHelper helper) {
        //Check for first time status
        if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_YES)) {
            mSignatureText = helper.getCaptureWaterMarkText();
            textSignature.setText(mSignatureText);
        } else if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_NO)) {
            //Hide view
            textSignature.setVisibility(View.GONE);
            //Do nothing
        } else if (helper.getWatermarkStatus().equals(WATERMARK_STATUS_ASK_ALWAYS)) {
            //Show watermark dialog
            getSignatureDialog();
        }
    }

    /**
     * Method to show watermark dialog.
     */
    private void getSignatureDialog() {
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
                        showSignatureInputDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Hide view
                        textSignature.setVisibility(View.GONE);
                        //If checkbox is selected
                        if (dialog.isPromptCheckBoxChecked()) {
                            //Save watermark status
                            mHelper.setWatermarkStatus(WATERMARK_STATUS_NO);
                        }
                        //Dismiss this dialog
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Method to show input dialog where user enters his/her signature.
     */
    private void showSignatureInputDialog() {
        new MaterialDialog.Builder(this)
                .title("Signature")
                .autoDismiss(false)
                .inputRange(1, 20, ContextCompat.getColor(CollaborationActivity.this, R.color.red))
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE)
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String s = String.valueOf(input).trim();
                        if (s.length() < 1) {
                            ViewHelper.getToast(CollaborationActivity.this, "This field can't be empty");
                        } else {
                            //Dismiss
                            dialog.dismiss();
                            mSignatureText = s;
                            //Set watermark
                            textSignature.setText(mSignatureText);
                            //Save watermark text
                            mHelper.setCaptureWaterMarkText(mSignatureText);
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * Method to retrieve short data from server.
     */
    private void loadShortData() {
        //Show progress view
        viewProgress.setVisibility(View.VISIBLE);

        final JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("uuid", mHelper.getUUID());
            requestObject.put("authkey", mHelper.getAuthToken());
            requestObject.put("shoid", mShortID);
        } catch (JSONException e) {
            e.printStackTrace();
            //Hide progress view
        }
        Rx2AndroidNetworking.post(BuildConfig.URL + "/manage-short/load-specific")
                .addJSONObjectBody(requestObject)
                .build()
                .getJSONObjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //Add composite disposable
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        float factor = (float) squareView.getWidth() / 650;
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject responseObject = jsonObject.getJSONObject("data");
                                //Retrieve data from server response
                                String text = responseObject.getString("text");
                                int textSize = responseObject.getInt("textsize");
                                int textColor = (int) Long.parseLong(responseObject.getString("textcolor"), 16);

                                //Set textView property
                                textShort.setText(text);
                                textShort.setTextSize(ViewHelper.pixelsToSp(CollaborationActivity.this, textSize * factor));
                                textShort.setTextColor(textColor);
                            }
                        } catch (JSONException e) {
                            //Hide progress view
                            viewProgress.setVisibility(View.GONE);
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        //Hide progress view
                        viewProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress view
                        viewProgress.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Method to generate image and show its preview.
     */
    private void generateImage() {

        //Obtain division factor
        float divisionFactor = (float) squareView.getWidth() / mImageWidth;

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
            showShortPreview(divisionFactor);
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
    private void showShortPreview(final float factor) {


        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_short_preview, false)
                .title("Preview")
                .positiveText("Upload")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                        // check net status
                        if (NetworkHelper.getNetConnectionStatus(CollaborationActivity.this)) {

                            //Update details on server
                            updateData(
                                    new File(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath())
                                    , new File(getImageUri(IMAGE_TYPE_USER_SHORT_PIC).getPath())
                                    , mShortID
                                    , String.valueOf(textShort.getX() / factor)
                                    , String.valueOf((textShort.getY() - squareView.getY()) / factor)
                                    , String.valueOf(textShort.getWidth() / factor)
                                    , String.valueOf(textShort.getHeight() / factor)
                                    , textShort.getText().toString()
                                    , String.valueOf(textShort.getTextSize() / factor)
                                    , Integer.toHexString(textShort.getCurrentTextColor())
                                    , textGravity.toString()
                                    , String.valueOf(mImageWidth)
                                    , mFontType
                                    , String.valueOf(mBoldFlag)
                                    , String.valueOf(mItalicFlag)
                            );
                        } else {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                        }
                    }
                })
                .show();


        ImageView imagePreview = dialog.getCustomView().findViewById(R.id.imageShortPreview);
        //Load preview image
        Picasso.with(this)
                .load(getImageUri(IMAGE_TYPE_USER_SHORT_PIC))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.image_placeholder)
                .into(imagePreview);
    }

    /**
     * Method to update capture details on server.
     */
    private void updateData(File imgHighRes, File imgLowRes, String shortID, String xPosition, String yPosition, String tvWidth, String tvHeight, String text, String textSize, String textColor, String textGravity, String imgWidth, String font, String bold, String italic) {
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
                .addMultipartParameter("watermark", mSignatureText)
                .addMultipartParameter("merchantable", mIsMerchantable)
                .addMultipartParameter("font", font)
                .addMultipartParameter("bold", bold)
                .addMultipartParameter("italic", italic)
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
                                    ViewHelper.getToast(CollaborationActivity.this, "Capture uploaded successfully.");
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
