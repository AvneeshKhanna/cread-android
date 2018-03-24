package com.thetestament.cread.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ColorAdapter;
import com.thetestament.cread.adapters.FontAdapter;
import com.thetestament.cread.adapters.InspirationAdapter;
import com.thetestament.cread.adapters.TemplateAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.ColorHelper;
import com.thetestament.cread.helpers.FontsHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.TemplateHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.OnDragTouchListener;
import com.thetestament.cread.listeners.OnSwipeGestureListener;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.ColorModel;
import com.thetestament.cread.models.FontModel;
import com.thetestament.cread.models.InspirationModel;
import com.thetestament.cread.models.TemplateModel;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.widgets.CustomEditText;
import com.thetestament.cread.widgets.CustomEditText.OnEditTextBackListener;
import com.thetestament.cread.widgets.SquareView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_INSPIRATION;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_AMATIC_SC_REGULAR;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_A_LOVE_OF_THUNDER;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_BLACKOUT_SUNRISE;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_BLACKOUT_TWOAM;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_BOHEMIAN_TYPEWRITER;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_FRESSH;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_KOMIKAAXIS;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_LANGDON;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_OSTRICH_ROUNDED;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_PACIFICO;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_POIRET_ONE_REGULAR;
import static com.thetestament.cread.helpers.FontsHelper.FONT_TYPE_YANONE_KAFFEESATZ;
import static com.thetestament.cread.helpers.FontsHelper.getFontType;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.helpers.TemplateHelper.FONT_SIZE_DEFAULT;
import static com.thetestament.cread.helpers.TemplateHelper.FONT_SIZE_LARGE;
import static com.thetestament.cread.helpers.TemplateHelper.FONT_SIZE_MEDIUM;
import static com.thetestament.cread.helpers.TemplateHelper.FONT_SIZE_SMALL;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_1;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_10;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_11;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_12;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_13;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_14;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_15;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_16;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_17;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_2;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_3;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_4;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_5;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_6;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_7;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_8;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_9;
import static com.thetestament.cread.helpers.TemplateHelper.TEMPLATE_NONE;
import static com.thetestament.cread.helpers.TemplateHelper.setContentShapeColor;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_INSPIRATION_CLICKED;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_AUTH_KEY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BG_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BOLD;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_EDIT_SHORT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_SHORT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTION_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_FONT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMAGE_TINT_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMG_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IS_SHADOW_SELECTED;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ITALIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_SHORT_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEMPLATE_NAME;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_GRAVITY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TEXT_SIZE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TV_HEIGHT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_TV_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_UUID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_X_POSITION;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_Y_POSITION;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_INSPIRATION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_PREVIEW_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_WRITE_EXTERNAL_STORAGE;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM_COLLABORATION_SHORT;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM_EDIT_SHORT;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CAPTION_TEXT;

/**
 * Here user creates his/her shorts and uploads on the server.
 */

public class ShortActivity extends BaseActivity implements OnEditTextBackListener {

    //region :Views binding with butter knife
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageContainer)
    SquareView squareView;
    @BindView(R.id.imageShort)
    ImageView imageShort;
    @BindView(R.id.textShort)
    CustomEditText textShort;
    @BindView(R.id.seekBarTextSize)
    AppCompatSeekBar seekBarTextSize;
    @BindView(R.id.dotShadow)
    View dotShadow;
    @BindView(R.id.btnLAlignText)
    ImageView btnAlignText;
    @BindView(R.id.dotBold)
    View dotBold;
    @BindView(R.id.dotItalic)
    View dotItalic;
    @BindView(R.id.btnFormatTextSize)
    View viewFormatTextSize;
    @BindView(R.id.formatOptions)
    View formatOptions;
    @BindView(R.id.imageProgressView)
    View imageProgressView;
    @BindView(R.id.textNote)
    TextView textNote;
    @BindView(R.id.recyclerViewInspiration)
    RecyclerView recyclerViewInspiration;
    @BindView(R.id.progressViewInspiration)
    View progressViewInspiration;
    @BindView(R.id.btnInspiration)
    AppCompatImageView buttonInspireMe;
    @BindView(R.id.btnRemoveImage)
    AppCompatImageView btnRemoveImage;

    //Font bottom sheet
    @BindView(R.id.bottomSheetView)
    NestedScrollView bottomSheetView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    //Color bottom sheet
    @BindView(R.id.colorBottomSheetView)
    NestedScrollView colorBottomSheetView;
    @BindView(R.id.colorRecyclerView)
    RecyclerView colorRecyclerView;
    //Template bottom sheet
    @BindView(R.id.templateBottomSheetView)
    NestedScrollView templateBottomSheetView;
    @BindView(R.id.templateRecyclerView)
    RecyclerView templateRecyclerView;
    //endregion

    //region :Fields and constants
    private BottomSheetBehavior sheetBehavior, colorSheetBehaviour, templateSheetBehaviour;
    //Define font typeface
    private Typeface mTextTypeface;


    @State
    String mCaptureUrl, mCaptureID = "", mShortID = "", mEntityID = "", mCaptionText = "", mSignatureText, mShortBgColor = "FFFFFFFF", mFontType = FONT_TYPE_BOHEMIAN_TYPEWRITER;

    /**
     * Flag to maintain imageWidth
     */
    @State
    int mImageWidth = 900;


    /**
     * Flag to maintain merchantable status i.e true if merchantable false otherwise.
     */
    @State
    boolean mIsMerchantable = true;

    /**
     * Flag to maintain user signature status i.e true if user signature is present false otherwise.
     */
    @State
    boolean signatureStatus = false;
    /**
     * Flag to maintain background image status i.e true if background image is present false otherwise.
     */
    @State
    boolean mIsImagePresent = false;
    /**
     * Flag to maintain image background color status i.e true if background color is present false otherwise.
     */
    @State
    boolean mIsBgColorPresent = false;
    /**
     * Flag to maintain color chooser dialog called status.
     */
    @State
    String mColorChooserType;

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
    int mGravityFlag = 2;

    /**
     * Flag to maintain templates selection status. True if selected false otherwise
     */
    @State
    boolean mIsShapeSelected = false;

    /**
     * Flag to store current selected template name.
     */
    @State
    String mShapeName = "none";


    /**
     * Flag to maintain shadow  status. 1 if shadow applied 0 otherwise
     */
    @State
    int mIsShadowSelected = 0;

    @State
    String mCalledFrom = PREVIEW_EXTRA_CALLED_FROM_SHORT;

    ShortActivity mContext;

    //ENUM for text gravity
    private enum TextGravity {
        Center, East, West
    }

    //Initially text gravity is "West"
    TextGravity textGravity = TextGravity.West;


    /**
     * Flag to maintain image tint status. 0(Zero) for default.
     */
    @State
    int mImageTintFlag = 0;

    /**
     * Flag to store image tint color
     */
    @State
    String mImageTintColor = "";


    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    //For inspiration view
    List<InspirationModel> mInspirationDataList = new ArrayList<>();
    InspirationAdapter mAdapter;
    private String mLastIndexKey;
    private boolean mRequestMoreData;

    FontAdapter fontAdapter;
    TemplateAdapter templateAdapter;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short);
        //ButterKnife view binding
        ButterKnife.bind(this);
        //Obtain reference
        mContext = this;
        //initialize for screen
        initScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    //Update flags
                    mIsBgColorPresent = false;
                    mIsImagePresent = true;
                    //show note textView
                    textNote.setVisibility(View.VISIBLE);
                    //Show remove button
                    btnRemoveImage.setVisibility(View.VISIBLE);
                }
                break;
            case REQUEST_CODE_PREVIEW_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    //Finish this screen
                    if (data != null) {
                        setResult(RESULT_OK, getIntent().putExtra(PREVIEW_EXTRA_CAPTION_TEXT
                                , data.getStringExtra(PREVIEW_EXTRA_CAPTION_TEXT)));
                    }
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Show prompt dialog
                showBackNavigationDialog();
                return true;
            case R.id.action_signature:
                //Method call
                toggleSignatureText();
                return true;
            case R.id.action_next:
                //If short text is empty
                if (TextUtils.getTrimmedLength(textShort.getText().toString()) == 0) {
                    //Show toast message
                    ViewHelper.getToast(this, "Short can't be empty. Please Write something.");
                } else {
                    //Remove underline
                    textShort.clearComposingText();
                    //Remove tint from imageView
                    removeImageTint();
                    getRuntimePermission();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        //Show prompt dialog
        showBackNavigationDialog();
    }

    /**
     * Edit text back listener
     */
    @Override
    public void onBack() {
        //initialize listener
        initDragListener();
        //Remove tint to imageView
        removeImageTint();
        //Toggle visibility
        formatOptions.setVisibility(View.VISIBLE);
        seekBarTextSize.setVisibility(View.VISIBLE);
        viewFormatTextSize.setVisibility(View.VISIBLE);
    }

    //endregion

    //region :Click functionality

    /**
     * Root view click functionality.
     */
    @OnClick(R.id.rootView)
    void rootViewOnClick() {
        //Method call
        hideBottomSheets();
    }

    @OnClick(R.id.imageContainer)
    void onContainerClick() {
        //Method call
        hideBottomSheets();
        //Hide edit text cursor
        textShort.setCursorVisible(false);
        //Remove tint to imageView
        removeImageTint();
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textShort.getWindowToken(), 0);

        //initialize listener
        initDragListener();

        //Toggle visibility
        formatOptions.setVisibility(View.VISIBLE);
        seekBarTextSize.setVisibility(View.VISIBLE);
        viewFormatTextSize.setVisibility(View.VISIBLE);
    }

    /**
     * Inspire me button click functionality to open InspirationActivity.
     */
    @OnClick(R.id.btnInspiration)
    void onBtnInspireClicked() {
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
    void onBtnLAlignTextClicked(ImageView btnAlignText) {

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
                if (mIsShapeSelected) {
                    if (mShapeName.equals(TemplateHelper.SHAPE_NAME_SIDE_LINE)) {
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_rightline));
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                    }
                }

                break;
            case 1:
                textShort.setGravity(Gravity.LEFT);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_left_32));
                mGravityFlag = 2;
                textGravity = TextGravity.West;

                if (mIsShapeSelected) {
                    if (mShapeName.equals(TemplateHelper.SHAPE_NAME_SIDE_LINE)) {
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_leftline));
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                    }
                }
                break;
            case 2:
                textShort.setGravity(Gravity.CENTER);
                btnAlignText.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_format_align_center_32));
                mGravityFlag = 0;
                textGravity = TextGravity.Center;
                if (mIsShapeSelected) {
                    if (mShapeName.equals(TemplateHelper.SHAPE_NAME_SIDE_LINE)) {
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_leftrightlines));
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                    }
                }
                break;
        }
    }

    /**
     * Functionality to show bottom sheet with font options .
     */
    @OnClick(R.id.btnFont)
    void onFontClicked() {
        //Show font bottomSheet
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //update font selection
        fontAdapter.updateSelectedFont(FontsHelper.getFontPosition(mFontType));
        recyclerView.smoothScrollToPosition(FontsHelper.getFontPosition(mFontType));

    }

    /**
     * Functionality to change canvas background
     */
    @OnClick(R.id.btnFormatBg)
    void changeBgColor() {
        if (mIsImagePresent) {
            //Show toast message
            ViewHelper.getToast(ShortActivity.this
                    , "Cannot add background color when an image is present");
        } else {
            //Set type
            mColorChooserType = "backGroundColor";
            imageShort.clearColorFilter();
            //Show color bottomSheet
            colorSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    /**
     * Click functionality to show material color palette dialog.
     */
    @OnClick(R.id.btnFormatTextColor)
    void onBtnFormatTextColorClicked() {
        //Set type
        mColorChooserType = "texColor";
        //Show color bottomSheet
        colorSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Click functionality to show template bottom sheet
     */
    @OnClick(R.id.btnTemplate)
    void templateOnClick() {
        //Show template bottom sheet
        templateSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (mIsShapeSelected) {
            //update template selection
            templateAdapter.updateSelectedTemplate(TemplateHelper.getTemplatePosition(mShapeName, mFontType));
            templateRecyclerView.smoothScrollToPosition(TemplateHelper.getTemplatePosition(mShapeName, mFontType));
        }
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
            //Toggle dot views visibility
            dotBold.setVisibility(View.INVISIBLE);
            dotItalic.setVisibility(View.VISIBLE);
        } else if (mItalicFlag == 0 && mBoldFlag == 1) {
            //Set typeface to bold_italic
            textShort.setTypeface(mTextTypeface, Typeface.BOLD_ITALIC);
            //Update flag
            mItalicFlag = 1;
            //Toggle dot views visibility
            dotBold.setVisibility(View.VISIBLE);
            dotItalic.setVisibility(View.VISIBLE);
        } else if (mItalicFlag == 1 && mBoldFlag == 0) {
            //Set typeface to normal
            textShort.setTypeface(mTextTypeface, Typeface.NORMAL);
            //Update flag
            mItalicFlag = 0;
            //Toggle dot views visibility
            dotBold.setVisibility(View.INVISIBLE);
            dotItalic.setVisibility(View.INVISIBLE);
        } else if (mItalicFlag == 1 && mBoldFlag == 1) {
            //Set typeface to bold
            textShort.setTypeface(mTextTypeface, Typeface.BOLD);
            //Update flag
            mItalicFlag = 0;
            //Toggle dot views visibility
            dotBold.setVisibility(View.VISIBLE);
            dotItalic.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Font bottom sheet close button click functionality to hide bottom sheet.
     */
    @OnClick(R.id.buttonClose)
    void onCloseBtnClick() {
        //Hide font bottom sheet
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Template bottom sheet close button click functionality to hide bottom sheet.
     */
    @OnClick(R.id.buttonTemplateClose)
    void onTemplateCloseBtnClick() {
        //Hide font bottom sheet
        templateSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Click functionality of remove image button
     */
    @OnClick(R.id.btnRemoveImage)
    void removeImageOnClick() {

        if (mIsImagePresent) {
            //show note textView
            textNote.setVisibility(View.INVISIBLE);
            //Remove image
            imageShort.setImageDrawable(null);
            imageShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_short_default_bg));
        } else {
            //Hide button
            btnRemoveImage.setVisibility(View.GONE);
            //Change text color
            textShort.setTextColor(ContextCompat.getColor(mContext, R.color.color_grey_600));
            textShort.setHintTextColor(ContextCompat.getColor(mContext, R.color.color_grey_600));
            // set content style color
            if (mIsShapeSelected) {
                setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
            }
            //Remove default bg
            imageShort.setBackground(null);
        }
        //Toggle flags
        mIsImagePresent = false;
        //Update value
        mCaptureID = "";
        mCaptureUrl = "";
        mIsMerchantable = true;


    }

    /**
     * Shadow button on click
     */
    @OnClick(R.id.btnFormatShadow)
    void shadowBtnOnClick() {
        if (mIsShadowSelected == 1) {
            //Update flags
            mIsShadowSelected = 0;
            //Remove shadow layer
            textShort.setShadowLayer(0, 0, 0, 0);
            //Show hide dot shadow
            dotShadow.setVisibility(View.INVISIBLE);
        } else {
            mIsShadowSelected = 1;
            //Apply shadow on text
            textShort.setShadowLayer(3, 3, 3
                    , ContextCompat.getColor(mContext, R.color.color_grey_600));
            //Show show dot shadow
            dotShadow.setVisibility(View.VISIBLE);
        }
    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize view for this screen.
     */
    private void initScreen() {
        //obtain shared preference reference
        mHelper = new SharedPreferenceHelper(this);

        //retrieve data from intent
        retrieveData();

        //Set default font
        mTextTypeface = ResourcesCompat.getFont(mContext, R.font.bohemian_typewriter);

        //set editText back listener
        textShort.setOnEditTextBackListener(this);
        //initialize seek bar
        initSeekBar(seekBarTextSize);

        //setup bottom sheets
        sheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        sheetBehavior.setPeekHeight(0);
        colorSheetBehaviour = BottomSheetBehavior.from(colorBottomSheetView);
        colorSheetBehaviour.setPeekHeight(0);
        templateSheetBehaviour = BottomSheetBehavior.from(templateBottomSheetView);
        templateSheetBehaviour.setPeekHeight(0);

        //initialise font and color and template bottomSheet
        initFontLayout();
        initColorLayout();
        initTemplateLayout();

        //initialize listener
        initDragListener();
        initDoubleTapListener();
    }

    /**
     * Method to retrieve data from intent and initialize this screen.
     */
    private void retrieveData() {
        if (getIntent().hasExtra(EXTRA_DATA)) {
            //Retrieve bundle data
            Bundle bundle = getIntent().getBundleExtra(EXTRA_DATA);
            //Called from short collaboration
            if (bundle.getString(SHORT_EXTRA_CALLED_FROM).equals(SHORT_EXTRA_CALLED_FROM_COLLABORATION_SHORT)) {
                //Hide button
                buttonInspireMe.setVisibility(View.INVISIBLE);
                //Retrieve data
                mCaptureID = bundle.getString(EXTRA_CAPTURE_ID);
                mCaptureUrl = bundle.getString(EXTRA_CAPTURE_URL);
                mIsMerchantable = bundle.getBoolean(EXTRA_MERCHANTABLE);
                //Load inspiration/capture image
                loadCapture(imageShort, mCaptureUrl);
                //Update flag
                mIsImagePresent = true;
                //show note text
                textNote.setVisibility(View.VISIBLE);
                // show image tint
                imageShort.setColorFilter(ContextCompat.getColor(ShortActivity.this, R.color.transparent_50));
                //Hide remove image button
                btnRemoveImage.setVisibility(View.GONE);
            }
            //Called from short editing
            else if (bundle.getString(SHORT_EXTRA_CALLED_FROM).equals(SHORT_EXTRA_CALLED_FROM_EDIT_SHORT)) {
                loadShortData(bundle.getString(EXTRA_ENTITY_ID), bundle.getString(EXTRA_ENTITY_TYPE));
                //Retrieve data
                mIsMerchantable = bundle.getBoolean(EXTRA_MERCHANTABLE);
                mCaptionText = bundle.getString(SHORT_EXTRA_CAPTION_TEXT);
                mEntityID = bundle.getString(EXTRA_ENTITY_ID);

                //Hide remove image button
                btnRemoveImage.setVisibility(View.GONE);

                mCalledFrom = PREVIEW_EXTRA_CALLED_FROM_EDIT_SHORT;
                //Method called
                initInspirationView();
            }
        } else {
            //Method called
            initInspirationView();
            //update font type and typeface
            mFontType = mHelper.getSelectedFont();
            mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
            //set typeface
            textShort.setTypeface(mTextTypeface);
        }
        //Set water mark text
        mSignatureText = "- " + mHelper.getFirstName() + " " + mHelper.getLastName();
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
                textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, i + 16);
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
     * Method to initialize font bottom sheet.
     */
    private void initFontLayout() {
        final ArrayList<FontModel> mFontDataList = new ArrayList<>();
        //initialize font data list
        for (String fontName : FontsHelper.fontTypes) {
            FontModel data = new FontModel();
            data.setFontName(fontName);
            mFontDataList.add(data);
        }
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext
                , LinearLayoutManager.HORIZONTAL, false);
        //Set layout manager
        recyclerView.setLayoutManager(layoutManager);
        //Set adapter
        fontAdapter = new FontAdapter(mFontDataList
                , mContext
                , mHelper.getSelectedFontPosition());
        //set adapter
        recyclerView.setAdapter(fontAdapter);
        //Scroll to last selected item position
        recyclerView.smoothScrollToPosition(mHelper.getSelectedFontPosition());

        //Font click listener
        fontAdapter.setOnFontClickListener(new listener.OnFontClickListener() {
            @Override
            public void onFontClick(Typeface typeface, String fontType, int itemPosition) {
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
                //Save current selected font in shared preference
                mHelper.setSelectedFont(mFontType);
                mHelper.setSelectedFontPosition(itemPosition);
                //Method called
                ViewHelper.scrollToNextItemPosition(layoutManager, recyclerView, itemPosition, mFontDataList.size());
            }
        });
    }

    /**
     * Method to initialize color bottom sheet.
     */
    private void initColorLayout() {
        final ArrayList<ColorModel> colorList = new ArrayList<>();
        //initialize color data list
        for (String colorValue : ColorHelper.colorList) {
            ColorModel data = new ColorModel();
            data.setColorValue(colorValue);
            colorList.add(data);
        }
        //Set layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext
                , LinearLayoutManager.HORIZONTAL
                , false);
        colorRecyclerView.setLayoutManager(layoutManager);
        //Set adapter
        ColorAdapter colorAdapter = new ColorAdapter(colorList, mContext);
        colorRecyclerView.setAdapter(colorAdapter);

        //Font click listener
        colorAdapter.setColorSelectListener(new listener.OnColorSelectListener() {
            @Override
            public void onColorSelected(int selectedColor, int itemPosition) {
                if (mColorChooserType.equals("texColor")) {
                    //Change short text color
                    textShort.setTextColor(selectedColor);
                    textShort.setHintTextColor(selectedColor);
                    if (mIsShapeSelected) {
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                    }

                } else if (mColorChooserType.equals("backGroundColor")) {
                    //Change backgroundColor
                    imageShort.setBackgroundColor(selectedColor);
                    //Update flag
                    mIsBgColorPresent = true;
                }
                //Method called
                ViewHelper.scrollToNextItemPosition(layoutManager, colorRecyclerView, itemPosition, colorList.size());
            }
        });
    }

    /**
     * Method to initialize template bottom sheet.
     */
    private void initTemplateLayout() {
        final ArrayList<TemplateModel> templateList = new ArrayList<>();
        //initialize color data list
        for (String templateName : TemplateHelper.templateList) {
            TemplateModel data = new TemplateModel();
            data.setTemplateName(templateName);
            templateList.add(data);
        }
        //Set layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext
                , LinearLayoutManager.HORIZONTAL
                , false);
        templateRecyclerView.setLayoutManager(layoutManager);
        //Set adapter
        templateAdapter = new TemplateAdapter(templateList, mContext);
        templateRecyclerView.setAdapter(templateAdapter);

        //Template click listener
        templateAdapter.setOnTemplateClickListener(new listener.OnTemplateClickListener() {
            @Override
            public void onTemplateClick(String templateName, int itemPosition) {
                //Method called
                ViewHelper.scrollToNextItemPosition(layoutManager, templateRecyclerView, itemPosition, templateList.size());

                switch (templateName) {
                    case TEMPLATE_NONE:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.bohemian_typewriter), Typeface.NORMAL);
                        mFontType = FONT_TYPE_BOHEMIAN_TYPEWRITER;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.LEFT);
                        //Change button drawable
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_left_32));

                        textGravity = TextGravity.West;
                        mGravityFlag = 2;
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;

                        //Update text size slider
                        seekBarTextSize.setProgress(0);
                        break;
                    case TEMPLATE_1:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.amatic_sc_regular), Typeface.BOLD);
                        mFontType = FONT_TYPE_AMATIC_SC_REGULAR;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 1;
                        dotBold.setVisibility(View.VISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        //Change button drawable
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, TemplateHelper.FONT_SIZE_MEDIUM);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;

                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);

                        break;
                    case TEMPLATE_2:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.amatic_sc_regular), Typeface.BOLD);
                        mFontType = FONT_TYPE_AMATIC_SC_REGULAR;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 1;
                        dotBold.setVisibility(View.VISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        // update italic status
                        // update bold flag
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_bottomtoplines));
                        mIsShapeSelected = true;
                        mShapeName = TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE;
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);

                        break;
                    case TEMPLATE_3:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.blackout_sunrise), Typeface.NORMAL);
                        mFontType = FONT_TYPE_BLACKOUT_SUNRISE;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_LARGE);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_LARGE - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;

                        break;

                    case TEMPLATE_4:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.fressh), Typeface.NORMAL);
                        mFontType = FONT_TYPE_FRESSH;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.LEFT);
                        textGravity = TextGravity.West;
                        mGravityFlag = 2;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_left_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;
                        break;
                    case TEMPLATE_5:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.komikaaxis), Typeface.NORMAL);
                        mFontType = FONT_TYPE_KOMIKAAXIS;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_SMALL);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_SMALL - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;
                        break;
                    case TEMPLATE_6:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.komikaaxis), Typeface.NORMAL);
                        mFontType = FONT_TYPE_KOMIKAAXIS;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_SMALL);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_SMALL - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_bottomtoplines));
                        mIsShapeSelected = true;
                        mShapeName = TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE;
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                        break;
                    case TEMPLATE_7:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.langdon), Typeface.NORMAL);
                        mFontType = FONT_TYPE_LANGDON;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_LARGE);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_LARGE - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;
                        break;
                    case TEMPLATE_8:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.a_love_of_thunder), Typeface.NORMAL);
                        mFontType = FONT_TYPE_A_LOVE_OF_THUNDER;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_LARGE);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_LARGE - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;
                        break;
                    case TEMPLATE_9:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.ostrich_rounded), Typeface.NORMAL);
                        mFontType = FONT_TYPE_OSTRICH_ROUNDED;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.LEFT);
                        textGravity = TextGravity.West;
                        mGravityFlag = 2;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_left_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;

                        break;
                    case TEMPLATE_10:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.ostrich_rounded), Typeface.NORMAL);
                        mFontType = FONT_TYPE_OSTRICH_ROUNDED;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.LEFT);
                        textGravity = TextGravity.West;
                        mGravityFlag = 2;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_left_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_leftline));
                        mIsShapeSelected = true;
                        mShapeName = TemplateHelper.SHAPE_NAME_SIDE_LINE;
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                        break;
                    case TEMPLATE_11:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.pacifico), Typeface.NORMAL);
                        mFontType = FONT_TYPE_PACIFICO;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;

                        break;
                    case TEMPLATE_12:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.pacifico), Typeface.NORMAL);
                        mFontType = FONT_TYPE_PACIFICO;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(0, 0, 0, 0);
                        mIsShadowSelected = 0;
                        dotShadow.setVisibility(View.INVISIBLE);
                        // update shape
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_quotemarks));
                        mIsShapeSelected = true;
                        mShapeName = TemplateHelper.SHAPE_NAME_QUOTE;
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                        break;
                    case TEMPLATE_13:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.poiret_one_regular), Typeface.NORMAL);
                        mFontType = FONT_TYPE_POIRET_ONE_REGULAR;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_LARGE);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_LARGE - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;

                        break;
                    case TEMPLATE_14:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.poiret_one_regular), Typeface.NORMAL);
                        mFontType = FONT_TYPE_POIRET_ONE_REGULAR;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_LARGE);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_LARGE - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_quotemarks));
                        mIsShapeSelected = true;
                        mShapeName = TemplateHelper.SHAPE_NAME_QUOTE;
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                        break;
                    case TEMPLATE_15:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.blackout_twoam), Typeface.NORMAL);
                        mFontType = FONT_TYPE_BLACKOUT_TWOAM;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_LARGE);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_LARGE - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;
                        break;
                    case TEMPLATE_16:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.yanone_kaffeesatz), Typeface.NORMAL);
                        mFontType = FONT_TYPE_YANONE_KAFFEESATZ;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(null);
                        mIsShapeSelected = false;
                        mShapeName = TemplateHelper.SHAPE_NAME_NONE;

                        break;
                    case TEMPLATE_17:
                        // set font
                        textShort.setTypeface(ResourcesCompat.getFont(mContext, R.font.yanone_kaffeesatz), Typeface.NORMAL);
                        mFontType = FONT_TYPE_YANONE_KAFFEESATZ;
                        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);
                        // update bold flag
                        mBoldFlag = 0;
                        dotBold.setVisibility(View.INVISIBLE);
                        // set gravity params
                        textShort.setGravity(Gravity.CENTER);
                        textGravity = TextGravity.Center;
                        mGravityFlag = 0;
                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                        //set font size
                        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, FONT_SIZE_MEDIUM);
                        //Update text size slider
                        seekBarTextSize.setProgress(FONT_SIZE_MEDIUM - FONT_SIZE_DEFAULT);
                        // update italic status
                        mItalicFlag = 0;
                        dotItalic.setVisibility(View.INVISIBLE);
                        //update shadow
                        textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
                        mIsShadowSelected = 1;
                        dotShadow.setVisibility(View.VISIBLE);
                        // update shape
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_bottomtoplines));
                        mIsShapeSelected = true;
                        mShapeName = TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE;
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                        break;

                    default:


                }
            }
        });
    }

    /**
     * Method to initialize  edit text drag listener.
     */
    private void initDragListener() {
        textShort.setOnTouchListener(new OnDragTouchListener(textShort, squareView, new OnDragTouchListener.OnDragActionListener() {
            @Override
            public void onDragStart(View view) {
                //Hide edit text cursor
                textShort.setCursorVisible(false);
            }

            @Override
            public void onDragEnd(View view) {
                //Show edit text cursor
                textShort.setCursorVisible(true);
                //Add tint to imageView
                imageShort.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_50));
                //Show keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textShort, 0);
                //Remove listener
                textShort.setOnTouchListener(null);
                //Toggle visibility
                formatOptions.setVisibility(View.INVISIBLE);
                seekBarTextSize.setVisibility(View.INVISIBLE);
                viewFormatTextSize.setVisibility(View.INVISIBLE);

                //Method call
                hideBottomSheets();
            }
        }));
    }

    /**
     * Method to initialize swipe listener on squareView.
     */
    private void initDoubleTapListener() {

        squareView.setOnTouchListener(new OnSwipeGestureListener(this) {

            @Override
            public void onDoubleClick() {

                if (mIsImagePresent) {
                    switch (mImageTintFlag) {
                        case 0:
                            //Apply tint
                            imageShort.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_30));
                            //Update flag
                            mImageTintFlag = 1;
                            //set tint color
                            mImageTintColor = "4D000000";
                            break;
                        case 1:
                            //Apply tint
                            imageShort.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_50));
                            //Update flag
                            mImageTintFlag = 2;
                            //set tint color
                            mImageTintColor = "80000000";
                            break;
                        case 2:
                            //Apply tint
                            imageShort.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_70));
                            //Update flag
                            mImageTintFlag = 3;
                            //set tint color
                            mImageTintColor = "B3000000";
                            break;
                        case 3:
                            //Clear filter
                            imageShort.clearColorFilter();
                            //Update flag
                            mImageTintFlag = 0;
                            //set tint color
                            mImageTintColor = "";
                            break;
                    }
                }

            }

            @Override
            public void onClick() {
                //Method call
                hideBottomSheets();

                //Hide edit text cursor
                textShort.setCursorVisible(false);
                //Remove tint to imageView
                removeImageTint();
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textShort.getWindowToken(), 0);

                //initialize listener
                initDragListener();

                //Toggle visibility
                formatOptions.setVisibility(View.VISIBLE);
                seekBarTextSize.setVisibility(View.VISIBLE);
                viewFormatTextSize.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Method to setup inspiration view and its functionality.
     */
    private void initInspirationView() {
        //Set layout manger for recyclerView
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext
                , LinearLayoutManager.HORIZONTAL, false);
        recyclerViewInspiration.setLayoutManager(layoutManager);
        //Set adapter
        mAdapter = new InspirationAdapter(mInspirationDataList, this, Constant.INSPIRATION_ITEM_TYPE_SMALL);
        recyclerViewInspiration.setAdapter(mAdapter);

        //Setup listener
        mAdapter.setInspirationSelectListener(new listener.OnInspirationSelectListener() {
            @Override
            public void onInspireImageSelected(InspirationModel model, int itemPosition) {
                //Method called
                ViewHelper.scrollToNextItemPosition(layoutManager, recyclerViewInspiration, itemPosition, mInspirationDataList.size());
                //Retrieve data
                mCaptureID = model.getCaptureID();
                mCaptureUrl = model.getCapturePic();
                mIsMerchantable = model.isMerchantable();
                //Load inspiration/capture image
                loadCapture(imageShort, mCaptureUrl);
                //Update flags
                mIsBgColorPresent = false;
                mIsImagePresent = true;
                //show note textView
                textNote.setVisibility(View.VISIBLE);

                //Show button
                btnRemoveImage.setVisibility(View.VISIBLE);
                //Toggle flags
                mIsBgColorPresent = false;
                mIsImagePresent = true;
            }
        });

        //Load inspiration data
        loadInspirationData();
        //initialize inspiration load more listener
        initLoadMoreListener(mAdapter);
    }

    /**
     * Method to load capture image.
     *
     * @param imageView View where image will be loaded.
     * @param imageUrl  URL of image to be loaded.
     */
    private void loadCapture(ImageView imageView, final String imageUrl) {
        //Show progress indicator
        imageProgressView.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load(imageUrl)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        //Hide progress indicator
                        imageProgressView.setVisibility(View.GONE);
                        //Add tint to imageView
                        //imageShort.setColorFilter(ContextCompat.getColor(ShortActivity.this, R.color.transparent_50));
                        Picasso.with(mContext).load(imageUrl).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                //Get image width
                                mImageWidth = bitmap.getWidth();

                                // Generate palette asynchronously and use it on a different
                                // thread using onGenerated()
                                Palette.from(bitmap)
                                        .generate(new Palette.PaletteAsyncListener() {
                                            @Override
                                            public void onGenerated(Palette palette) {
                                                Palette.Swatch swatch = palette.getDominantSwatch();
                                                if (swatch != null) {
                                                    String hexColorWithAlpha = Integer.toHexString(swatch.getBodyTextColor());
                                                    String hexColorWithoutAlpha = "#" + hexColorWithAlpha.substring(2, 8);
                                                    //set text color
                                                    textShort.setTextColor(Color.parseColor(hexColorWithoutAlpha));
                                                    //set hint color
                                                    textShort.setHintTextColor(Color.parseColor(hexColorWithoutAlpha));
                                                    // set shape color
                                                    if (mIsShapeSelected) {
                                                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                                                    }
                                                }
                                            }
                                        });
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
                        //Hide progress indicator
                        imageProgressView.setVisibility(View.GONE);
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
        //Hide button
        btnRemoveImage.setVisibility(View.GONE);

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

            //Retrieve background color if its present
            if (mIsBgColorPresent) {
                ColorDrawable drawable = (ColorDrawable) imageShort.getBackground();
                //Update bg color
                mShortBgColor = Integer.toHexString(drawable.getColor());
            }
            //Open next screen
            goToPreviewScreen(mHelper.getUUID()
                    , mHelper.getAuthToken()
                    , mCaptureID
                    , String.valueOf(textShort.getX() / divisionFactor)
                    , String.valueOf((textShort.getY() - squareView.getY()) / divisionFactor)
                    , String.valueOf(textShort.getWidth() / divisionFactor)
                    , String.valueOf(textShort.getHeight() / divisionFactor)
                    , textShort.getText().toString()
                    , String.valueOf(textShort.getTextSize() / divisionFactor)
                    , Integer.toHexString(textShort.getCurrentTextColor())
                    , textGravity.toString()
                    , String.valueOf(mImageWidth)
                    , String.valueOf(mIsMerchantable)
                    , mFontType
                    , mShortBgColor
                    , String.valueOf(mBoldFlag)
                    , String.valueOf(mItalicFlag)
                    , mImageTintColor
                    , mCalledFrom
                    , mShortID
                    , mCaptionText
                    , mEntityID
                    , mShapeName
                    , String.valueOf(mIsShadowSelected)
            );

        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        //Disable drawing cache
        squareView.setDrawingCacheEnabled(false);
        squareView.destroyDrawingCache();

        if (mIsImagePresent) {
            //show button
            btnRemoveImage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to open previewActivity.
     */
    private void goToPreviewScreen(String uuid, String authKey, String captureID
            , String xPosition, String yPosition, String tvWidth, String tvHeight
            , String text, String textSize, String textColor, String textGravity
            , String imgWidth, String merchantable, String font, String bgColor
            , String bold, String italic, String imageTintColor, String calledFrom
            , String shortID, String captionText, String entityID, String templateName, String isShadowSelected) {

        Intent intent = new Intent(ShortActivity.this, PreviewActivity.class);

        Bundle bundle = new Bundle();

        bundle.putString(PREVIEW_EXTRA_UUID, uuid);
        bundle.putString(PREVIEW_EXTRA_AUTH_KEY, authKey);
        bundle.putString(PREVIEW_EXTRA_CAPTURE_ID, captureID);
        bundle.putString(PREVIEW_EXTRA_X_POSITION, xPosition);
        bundle.putString(PREVIEW_EXTRA_Y_POSITION, yPosition);
        bundle.putString(PREVIEW_EXTRA_TV_WIDTH, tvWidth);
        bundle.putString(PREVIEW_EXTRA_TV_HEIGHT, tvHeight);
        bundle.putString(PREVIEW_EXTRA_TEXT, text);
        bundle.putString(PREVIEW_EXTRA_TEXT_SIZE, textSize);
        bundle.putString(PREVIEW_EXTRA_TEXT_COLOR, textColor);
        bundle.putString(PREVIEW_EXTRA_TEXT_GRAVITY, textGravity);
        bundle.putString(PREVIEW_EXTRA_IMG_WIDTH, imgWidth);
        bundle.putString(PREVIEW_EXTRA_MERCHANTABLE, merchantable);
        bundle.putString(PREVIEW_EXTRA_FONT, font);
        bundle.putString(PREVIEW_EXTRA_BG_COLOR, bgColor);
        bundle.putString(PREVIEW_EXTRA_BOLD, bold);
        bundle.putString(PREVIEW_EXTRA_ITALIC, italic);
        bundle.putString(PREVIEW_EXTRA_IMAGE_TINT_COLOR, imageTintColor);
        bundle.putString(PREVIEW_EXTRA_CALLED_FROM, calledFrom);
        bundle.putString(PREVIEW_EXTRA_SHORT_ID, shortID);
        bundle.putString(PREVIEW_EXTRA_CAPTION_TEXT, captionText);
        bundle.putString(PREVIEW_EXTRA_ENTITY_ID, entityID);
        bundle.putString(PREVIEW_EXTRA_TEMPLATE_NAME, templateName);
        bundle.putString(PREVIEW_EXTRA_IS_SHADOW_SELECTED, isShadowSelected);


        intent.putExtra(PREVIEW_EXTRA_DATA, bundle);
        startActivityForResult(intent, REQUEST_CODE_PREVIEW_ACTIVITY);
    }


    /**
     * Method to hide bottom sheets if they are expanded.
     */
    private void hideBottomSheets() {
        //Collapse font bottomSheet if its expanded
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        //Collapse color bottomSheet if its expanded
        if (colorSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            colorSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        //Collapse template bottomSheet if its expanded
        if (templateSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            templateSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /**
     * Method to clear image tint if it its not added explicitly.
     */
    private void removeImageTint() {
        if (mImageTintFlag == 0) {
            imageShort.clearColorFilter();
        }
    }

    /**
     * Method to retrieve short data from server when user edits his/her shorts.
     */
    private void loadShortData(String entityID, String entityType) {
        //Hide progress indicator
        imageProgressView.setVisibility(View.VISIBLE);
        //Map for request data
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("entityid", entityID);
        requestMap.put("type", entityType);

        Rx2AndroidNetworking.get(BuildConfig.URL + "/manage-short/load-specific")
                .addHeaders("uuid", mHelper.getUUID())
                .addHeaders("authkey", mHelper.getAuthToken())
                .addQueryParameter(requestMap)
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
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject responseObject = jsonObject.getJSONObject("data");
                                //Obtain multiplication factor
                                float factor = (float) squareView.getWidth()
                                        / (float) responseObject.getDouble("img_width");

                                //If capture image is not present
                                if (TextUtils.isEmpty(responseObject.getString("captureurl")) || responseObject.getString("captureurl").equals("null")) {

                                    if (responseObject.getString("bgcolor").equals("FFFFFFFF")) {

                                    } else {
                                        //Change backgroundColor
                                        imageShort.setBackgroundColor((int) Long.parseLong(responseObject.getString("bgcolor"), 16));
                                        //Update flag
                                        mIsBgColorPresent = true;
                                    }
                                } else {
                                    //Update flag
                                    mIsImagePresent = true;
                                    //show note text
                                    textNote.setVisibility(View.VISIBLE);

                                    Picasso.with(mContext)
                                            .load(responseObject.getString("captureurl"))
                                            .into(imageShort, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    //Hide progress indicator
                                                    imageProgressView.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void onError() {
                                                    //Hide progress indicator
                                                    imageProgressView.setVisibility(View.GONE);
                                                }
                                            });
                                }

                                //Update flags
                                mBoldFlag = responseObject.getInt("bold");
                                mItalicFlag = responseObject.getInt("italic");
                                mFontType = responseObject.getString("font");
                                mTextTypeface = getFontType(mFontType, mContext);
                                mImageWidth = responseObject.getInt("img_width");
                                if (!responseObject.isNull("capid")) {
                                    mCaptureID = responseObject.getString("capid");
                                }
                                mShortID = responseObject.getString("shoid");


                                //Set textView property
                                textShort.setText(responseObject.getString("text"));
                                textShort.setTextColor((int) Long.parseLong(responseObject.getString("textcolor"), 16));
                                textShort.setTextSize(ViewHelper.pixelsToSp(mContext, (float) responseObject.getDouble("textsize") * factor));

                                //if text shadow is present
                                if (responseObject.getLong("textshadow") == 1) {
                                    //Update flag
                                    mIsShadowSelected = 1;
                                    //Show dot indicator
                                    dotShadow.setVisibility(View.VISIBLE);
                                    //Apply text shadow
                                    textShort.setShadowLayer(3, 3, 3
                                            , ContextCompat.getColor(mContext, R.color.color_grey_600));
                                }

                                //set content style color
                                if (!responseObject.getString("shape").equals(TemplateHelper.SHAPE_NAME_NONE)) {
                                    switch (responseObject.getString("shape")) {
                                        case TemplateHelper.SHAPE_NAME_QUOTE:
                                            //update flag
                                            mIsShapeSelected = true;
                                            // update shape name
                                            mShapeName = TemplateHelper.SHAPE_NAME_QUOTE;
                                            //set shape
                                            textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_quotemarks));
                                            // set shape color
                                            setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                                            break;
                                        case TemplateHelper.SHAPE_NAME_SIDE_LINE:
                                            //update flag
                                            mIsShapeSelected = true;
                                            // update shape name
                                            mShapeName = TemplateHelper.SHAPE_NAME_SIDE_LINE;
                                            switch (TextGravity.valueOf(responseObject.getString("textgravity"))) {
                                                case West:
                                                    //set shape
                                                    textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_leftline));
                                                    break;

                                                case Center:
                                                    //set shape
                                                    textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_leftrightlines));
                                                    break;
                                                case East:
                                                    //set shape
                                                    textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_rightline));
                                                    break;
                                            }
                                            // set shape color
                                            setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                                            break;
                                        case TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE:

                                            //update flag
                                            mIsShapeSelected = true;
                                            // update shape name
                                            mShapeName = TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE;
                                            //set shape
                                            textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_bottomtoplines));
                                            // set shape color
                                            setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);

                                            break;
                                        case TemplateHelper.SHAPE_NAME_CORNER_LINE:
                                            //update flag
                                            mIsShapeSelected = true;
                                            // update shape name
                                            mShapeName = TemplateHelper.SHAPE_NAME_CORNER_LINE;
                                            //set shape
                                            textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_cornerlines));
                                            // set shape color
                                            setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                                            break;
                                    }
                                }


                                //Update short text typeface
                                if (mItalicFlag == 0 && mBoldFlag == 0) {
                                    //Set typeface to normal
                                    textShort.setTypeface(getFontType(mFontType, mContext), Typeface.NORMAL);
                                    //Toggle dot views visibility
                                    dotBold.setVisibility(View.INVISIBLE);
                                    dotItalic.setVisibility(View.INVISIBLE);
                                } else if (mItalicFlag == 0 && mBoldFlag == 1) {
                                    //Set typeface to bold
                                    textShort.setTypeface(getFontType(mFontType, mContext), Typeface.BOLD);
                                    //Toggle dot views visibility
                                    dotBold.setVisibility(View.VISIBLE);
                                    dotItalic.setVisibility(View.INVISIBLE);
                                } else if (mItalicFlag == 1 && mBoldFlag == 0) {
                                    //Set typeface to italic
                                    textShort.setTypeface(getFontType(mFontType, mContext), Typeface.ITALIC);
                                    //Toggle dot views visibility
                                    dotBold.setVisibility(View.INVISIBLE);
                                    dotItalic.setVisibility(View.VISIBLE);
                                } else if (mItalicFlag == 1 && mBoldFlag == 1) {
                                    //Set typeface to bold_italic
                                    textShort.setTypeface(getFontType(mFontType, mContext), Typeface.BOLD_ITALIC);
                                    //Toggle dot views visibility
                                    dotBold.setVisibility(View.VISIBLE);
                                    dotItalic.setVisibility(View.VISIBLE);
                                }

                                //Obtain x and y position of text
                                float dy = (float) (responseObject.getDouble("dy") - squareView.getY()) * factor;
                                float dx = (float) (responseObject.getDouble("dx") * factor);

                                //Update textView xPosition  and yPosition
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
                                        , RelativeLayout.LayoutParams.WRAP_CONTENT);
                                params.leftMargin = Math.round(dx); //Your X coordinate
                                params.topMargin = Math.round(dy); //Your Y coordinate
                                textShort.setLayoutParams(params);

                                //Update short text gravity
                                switch (TextGravity.valueOf(responseObject.getString("textgravity"))) {
                                    case Center:
                                        //Set text gravity
                                        textShort.setGravity(Gravity.CENTER);
                                        //Change button drawable
                                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_center_32));
                                        //Change gravity flag
                                        mGravityFlag = 0;
                                        //Set gravity variable
                                        textGravity = TextGravity.Center;
                                        break;
                                    case West:
                                        //Set text gravity
                                        textShort.setGravity(Gravity.LEFT);
                                        //Change button drawable
                                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_left_32));
                                        //Change gravity flag
                                        mGravityFlag = 2;
                                        //Set gravity variable
                                        textGravity = TextGravity.West;
                                        break;
                                    case East:
                                        //Set text gravity
                                        textShort.setGravity(Gravity.RIGHT);
                                        //Change button drawable
                                        btnAlignText.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_format_align_right_32));
                                        //Change gravity flag
                                        mGravityFlag = 1;
                                        //Set gravity variable
                                        textGravity = TextGravity.East;
                                        break;
                                }

                                //Update image tint
                                if (!TextUtils.isEmpty(responseObject.getString("imgtintcolor")) || responseObject.getString("imgtintcolor").equals("null")) {
                                    switch (responseObject.getString("imgtintcolor").toUpperCase()) {
                                        case "4D000000":
                                            //Apply tint
                                            imageShort.setColorFilter(ContextCompat.getColor(ShortActivity.this, R.color.transparent_30));
                                            //Update flag
                                            mImageTintFlag = 1;
                                            //set tint color
                                            mImageTintColor = "99000000";
                                            break;
                                        case "80000000":
                                            //Apply tint
                                            imageShort.setColorFilter(ContextCompat.getColor(ShortActivity.this, R.color.transparent_50));
                                            //Update flag
                                            mImageTintFlag = 2;
                                            //set tint color
                                            mImageTintColor = "80000000";
                                            break;
                                        case "B3000000":
                                            //Apply tint
                                            imageShort.setColorFilter(ContextCompat.getColor(ShortActivity.this, R.color.transparent_70));
                                            //Update flag
                                            mImageTintFlag = 3;
                                            //set tint color
                                            mImageTintColor = "B3000000";
                                            break;
                                        case "99000000":
                                            //Apply tint
                                            imageShort.setColorFilter(ContextCompat.getColor(ShortActivity.this, R.color.transparent_60));
                                            //Update flag
                                            mImageTintFlag = 3;
                                            //set tint color
                                            mImageTintColor = "99000000";
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            //Hide progress indicator
                            imageProgressView.setVisibility(View.GONE);
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Hide progress indicator
                        imageProgressView.setVisibility(View.GONE);
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress indicator
                        imageProgressView.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Initialize inspiration load more listener.
     *
     * @param adapter Inspiration adapter reference.
     */
    private void initLoadMoreListener(InspirationAdapter adapter) {
        adapter.setLoadMoreListener(new listener.OnInspirationLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //If next set of data available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mInspirationDataList.add(null);
                                               mAdapter.notifyItemInserted(mInspirationDataList.size() - 1);
                                           }
                                       }
                    );
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadInspirationData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            progressViewInspiration.setVisibility(View.VISIBLE);
            //Get data from server
            getInspirationData();
        } else {
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving inspiration data
     */
    private void getInspirationData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/inspiration-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_INSPIRATION)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Inspiration list
                                JSONArray array = mainData.getJSONArray("items");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject dataObj = array.getJSONObject(i);
                                    InspirationModel data = new InspirationModel();
                                    data.setEntityID(dataObj.getString("entityid"));
                                    data.setCaptureID(dataObj.getString("captureid"));
                                    data.setCapturePic(dataObj.getString("captureurl"));
                                    data.setMerchantable(dataObj.getBoolean("merchantable"));
                                    mInspirationDataList.add(data);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Hide progress view
                        progressViewInspiration.setVisibility(View.GONE);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress view
                        progressViewInspiration.setVisibility(View.GONE);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_INSPIRATION = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                        //No data
                        else if (mInspirationDataList.size() == 0) {
                            ViewHelper.getSnackBar(rootView, "Nothing to show.");
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
                            recyclerView.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }

    /**
     * Method to retrieve to next set of inspiration data from server.
     */
    private void loadMoreData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/inspiration-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_INSPIRATION)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mInspirationDataList.remove(mInspirationDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mInspirationDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {

                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Inspiration list
                                JSONArray array = mainData.getJSONArray("items");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject dataObj = array.getJSONObject(i);
                                    InspirationModel data = new InspirationModel();
                                    data.setEntityID(dataObj.getString("entityid"));
                                    data.setCaptureID(dataObj.getString("captureid"));
                                    data.setCapturePic(dataObj.getString("captureurl"));
                                    data.setMerchantable(dataObj.getBoolean("merchantable"));
                                    mInspirationDataList.add(data);
                                    //Notify item insertion
                                    mAdapter.notifyItemInserted(mInspirationDataList.size() - 1);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mInspirationDataList.remove(mInspirationDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mInspirationDataList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            //Notify changes
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }

    /**
     * Method to show dialog when user navigates back from this screen.
     */
    private void showBackNavigationDialog() {
        //Show prompt dialog
        CustomDialog.getBackNavigationDialog(mContext
                , "Discard changes?"
                , "If you go back now, you will loose your changes.");
    }

    /**
     * Method to add/remove user signature text from his/her content.
     */
    private void toggleSignatureText() {
        if (signatureStatus) {
            String s = textShort.getText().toString();
            String removedText = s.replace(mSignatureText, "").trim();
            textShort.setText(removedText);
            signatureStatus = false;
        } else {
            textShort.setText(textShort.getText() + "\n \n" + mSignatureText);
            signatureStatus = true;
        }
    }
    //endregion
}
