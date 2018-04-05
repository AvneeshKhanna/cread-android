package com.thetestament.cread.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ColorAdapter;
import com.thetestament.cread.adapters.FontAdapter;
import com.thetestament.cread.adapters.TemplateAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.CaptureHelper;
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
import com.thetestament.cread.models.TemplateModel;
import com.thetestament.cread.widgets.SquareView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

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
import static com.thetestament.cread.helpers.FontsHelper.fontTypes;
import static com.thetestament.cread.helpers.FontsHelper.getFontType;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
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
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_AUTH_KEY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BOLD;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_COLLABORATION;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_FONT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMAGE_TINT_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMG_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IS_SHADOW_SELECTED;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ITALIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_LONG_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_SHORT_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_SIGNATURE;
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
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_PREVIEW_ACTIVITY;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_ASK_ALWAYS;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_NO;
import static com.thetestament.cread.utils.Constant.WATERMARK_STATUS_YES;

/**
 * This class shows the preview of collaboration.
 */

public class CollaborationActivity extends BaseActivity {

    //region :Views binding with butter knife
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageContainer)
    SquareView squareView;
    @BindView(R.id.imageCapture)
    ImageView imageShort;
    @BindView(R.id.textShort)
    AppCompatTextView textShort;
    @BindView(R.id.textSignature)
    TextView textSignature;
    @BindView(R.id.progressView)
    View viewProgress;
    @BindView(R.id.seekBarTextSize)
    AppCompatSeekBar seekBarTextSize;
    @BindView(R.id.dotShadow)
    View dotShadow;
    @BindView(R.id.btnLAlignText)
    AppCompatTextView btnAlignText;
    @BindView(R.id.btnFont)
    AppCompatTextView btnFont;
    @BindView(R.id.btnFormatBg)
    AppCompatTextView btnFormatBg;
    @BindView(R.id.btnFormatTextColor)
    AppCompatTextView btnFormatTextColor;
    @BindView(R.id.btnTemplate)
    AppCompatTextView btnTemplate;
    @BindView(R.id.btnFormatTextBold)
    AppCompatTextView dotBold;
    @BindView(R.id.btnFormatTextItalic)
    AppCompatTextView dotItalic;

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
    String mEntityID, mShortID, mIsMerchantable, mSignatureText = "", mFontType = FONT_TYPE_BOHEMIAN_TYPEWRITER;

    @State
    String mEntityType;
    @State
    int mImageWidth = 900;


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

    //ENUM for text gravity
    private enum TextGravity {
        Center, East, West
    }

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

    /**
     * Flag to maintain templates selection status. True if selected false otherwise
     */
    @State
    boolean mIsShapeSelected = false;

    /**
     * Flag to store current selected template name.
     */
    @State
    String mShapeName = TemplateHelper.SHAPE_NAME_NONE;

    /**
     * Flag to maintain shadow  status. 0 if shadow applied 1 otherwise
     */
    @State
    int mIsShadowSelected = 0;

    //Initially text gravity is "CENTER"
    TextGravity textGravity = TextGravity.West;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    CollaborationActivity mContext;
    FontAdapter fontAdapter;
    TemplateAdapter templateAdapter;

    @State
    String longStoryText = "";
    //endregion

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaboration);
        ButterKnife.bind(this);
        //Obtain reference
        mContext = this;
        //Obtain reference
        mHelper = new SharedPreferenceHelper(this);
        //initialize this screen
        initScreen();
        //initialize seek bar
        initSeekBar(seekBarTextSize);
        //For bottomSheet
        sheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        sheetBehavior.setPeekHeight(0);
        colorSheetBehaviour = BottomSheetBehavior.from(colorBottomSheetView);
        colorSheetBehaviour.setPeekHeight(0);
        templateSheetBehaviour = BottomSheetBehavior.from(templateBottomSheetView);
        templateSheetBehaviour.setPeekHeight(0);
        //Set default font
        mTextTypeface = ResourcesCompat.getFont(CollaborationActivity.this, R.font.bohemian_typewriter);
        //initialise font , color and template bottomSheet
        initFontLayout();
        initColorLayout();
        initTemplateLayout();
        //initialize listener
        initSwipeListener();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
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
                         , getString(R.string.msg_text_navigate_back));
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
    public void onBackPressed() {
        //super.onBackPressed();
        //Show prompt dialog
        CustomDialog.getBackNavigationDialog(CollaborationActivity.this
                , "Discard changes?"
                , getString(R.string.msg_text_navigate_back));
    }

    @OnClick(R.id.rootView)
    void rootViewOnClick() {
        //Method call
        hideBottomSheets();
    }

    /**
     * Functionality to toggle the text gravity.
     */
    @OnClick(R.id.btnLAlignText)
    public void onBtnLAlignTextClicked() {

        switch (mGravityFlag) {
            case 0:
                applyGravity(Gravity.RIGHT, R.drawable.ic_format_align_right_32
                        , 1, TextGravity.East);
                if (mIsShapeSelected) {
                    if (mShapeName.equals(TemplateHelper.SHAPE_NAME_SIDE_LINE)) {
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_rightline));
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                    }
                }
                break;
            case 1:
                applyGravity(Gravity.LEFT, R.drawable.ic_format_align_left_32
                        , 2, TextGravity.West);
                if (mIsShapeSelected) {
                    if (mShapeName.equals(TemplateHelper.SHAPE_NAME_SIDE_LINE)) {
                        textShort.setBackground(ContextCompat.getDrawable(mContext, R.drawable.contentshape_leftline));
                        setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
                    }
                }
                break;
            case 2:
                applyGravity(Gravity.CENTER, R.drawable.ic_format_align_center_32
                        , 0, TextGravity.Center);
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
     * Functionality to change font type.
     */
    @OnClick(R.id.btnFont)
    void onFontClicked() {
        //Show bottomSheet
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //update font selection
        fontAdapter.updateSelectedFont(FontsHelper.getFontPosition(mFontType));
        recyclerView.smoothScrollToPosition(FontsHelper.getFontPosition(mFontType));
    }

    /**
     * Functionality to show toast.
     */
    @OnClick(R.id.btnFormatBg)
    void changeBgColor() {
        ViewHelper.getToast(CollaborationActivity.this
                , "'Cannot add background color when an image is present");
    }

    /**
     * Click functionality to show material color palette dialog.
     */
    @OnClick(R.id.btnFormatTextColor)
    void onBtnFormatTextColorClicked() {
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
    }

    /**
     * Bold button click functionality to set typeface to bold.
     */
    @OnClick(R.id.btnFormatTextBold)
    void boldBtnOnClick() {
        if (mItalicFlag == 0 && mBoldFlag == 0) {
            applyBold(Typeface.BOLD, 1, R.drawable.ic_format_bold_selected
                    , R.drawable.ic_format_italic_32);
        } else if (mItalicFlag == 0 && mBoldFlag == 1) {
            //Method called
            applyBold(Typeface.NORMAL, 0, R.drawable.ic_format_bold_32
                    , R.drawable.ic_format_italic_32);
        } else if (mItalicFlag == 1 && mBoldFlag == 0) {
            //Method called
            applyBold(Typeface.BOLD_ITALIC, 1, R.drawable.ic_format_bold_selected
                    , R.drawable.ic_format_italic_selected);
        } else if (mItalicFlag == 1 && mBoldFlag == 1) {
            //Method called
            applyBold(Typeface.ITALIC, 0, R.drawable.ic_format_bold_32
                    , R.drawable.ic_format_italic_selected);
        }
    }

    /**
     * Italic button click functionality to set typeface of content
     */
    @OnClick(R.id.btnFormatTextItalic)
    void italicBtnOnclick() {

        if (mItalicFlag == 0 && mBoldFlag == 0) {
            //Method call
            applyItalic(Typeface.ITALIC, 1, R.drawable.ic_format_bold_32
                    , R.drawable.ic_format_italic_selected);
        } else if (mItalicFlag == 0 && mBoldFlag == 1) {
            //Method call
            applyItalic(Typeface.BOLD_ITALIC, 1, R.drawable.ic_format_bold_selected
                    , R.drawable.ic_format_italic_selected);
        } else if (mItalicFlag == 1 && mBoldFlag == 0) {
            //Method call
            applyItalic(Typeface.NORMAL, 0, R.drawable.ic_format_bold_32
                    , R.drawable.ic_format_italic_32);
        } else if (mItalicFlag == 1 && mBoldFlag == 1) {
            //Method call
            applyItalic(Typeface.BOLD, 0, R.drawable.ic_format_bold_selected
                    , R.drawable.ic_format_italic_32);
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
     * Template bottom sheet close button click functionality to hide bottom sheet.
     */
    @OnClick(R.id.buttonTemplateClose)
    void onTemplateCloseBtnClick() {
        //Hide font bottom sheet
        templateSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (mIsShapeSelected) {
            //update template selection
            templateAdapter.updateSelectedTemplate(TemplateHelper.getTemplatePosition(mShapeName, mFontType));
            templateRecyclerView.smoothScrollToPosition(TemplateHelper.getTemplatePosition(mShapeName, mFontType));
        }
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
        //Set format option button
        setFormatOptionsIcon();
        //Retrieve data from intent
        Bundle data = getIntent().getBundleExtra(EXTRA_DATA);
        mEntityID = data.getString(EXTRA_ENTITY_ID);
        mEntityType = data.getString(EXTRA_ENTITY_TYPE);
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
     * Method to set format option icon.
     */
    private void setFormatOptionsIcon() {
        btnAlignText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_align_left_32, 0, 0);
        btnFont.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_font_32dp, 0, 0);
        btnFormatBg.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_color_fill_32, 0, 0);
        btnFormatTextColor.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_color_text_32, 0, 0);
        btnTemplate.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_style_32, 0, 0);
        dotBold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_bold_32, 0, 0);
        dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_italic_32, 0, 0);
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
     * Method to initialize font bottom sheet
     */
    private void initFontLayout() {
        final ArrayList<FontModel> mFontDataList = new ArrayList<>();
        //initialize font data list
        for (String fontName : fontTypes) {
            FontModel data = new FontModel();
            data.setFontName(fontName);
            mFontDataList.add(data);
        }

        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext
                , LinearLayoutManager.HORIZONTAL, false);
        //Set layout manager
        recyclerView.setLayoutManager(layoutManager);
        //Set adapter
        fontAdapter = new FontAdapter(mFontDataList, mContext, mHelper.getSelectedFontPosition());
        recyclerView.setAdapter(fontAdapter);
        //Font click listener
        fontAdapter.setOnFontClickListener(new listener.OnFontClickListener() {
            @Override
            public void onFontClick(Typeface typeface, String fontType, int itemPosition) {
                //Set short text typeface
                if (mItalicFlag == 0 && mBoldFlag == 0) {
                    //Set typeface to normal
                    textShort.setTypeface(typeface, Typeface.NORMAL);
                } else if (mItalicFlag == 0 && mBoldFlag == 1) {
                    //Set typeface to bold
                    textShort.setTypeface(typeface, Typeface.BOLD);
                } else if (mItalicFlag == 1 && mBoldFlag == 0) {
                    //Set typeface to italic
                    textShort.setTypeface(typeface, Typeface.ITALIC);
                } else if (mItalicFlag == 1 && mBoldFlag == 1) {
                    //Set typeface to bold_italic
                    textShort.setTypeface(typeface, Typeface.BOLD_ITALIC);
                }

                //set typeface
                mTextTypeface = typeface;
                //Set font type
                mFontType = fontType;
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
                //Change short text color
                textShort.setTextColor(selectedColor);
                if (mIsShapeSelected) {
                    setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
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
                        //Method called
                        applyTemplateStyle(R.font.bohemian_typewriter, Typeface.NORMAL, FONT_TYPE_BOHEMIAN_TYPEWRITER, 0
                                , Gravity.LEFT, R.drawable.ic_format_align_left_32, 2, TextGravity.West
                                , FONT_SIZE_DEFAULT, 0, 0, false, TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_1:
                        //Method called
                        applyTemplateStyle(R.font.amatic_sc_regular, Typeface.BOLD, FONT_TYPE_AMATIC_SC_REGULAR, 1
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_MEDIUM, 0, 1, false, TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_2:
                        //Method called
                        applyTemplateStyle(R.font.amatic_sc_regular, Typeface.BOLD, FONT_TYPE_AMATIC_SC_REGULAR, 1
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_MEDIUM, 0, 1, true
                                , TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE, R.drawable.contentshape_bottomtoplines);
                        break;
                    case TEMPLATE_3:
                        //Method called
                        applyTemplateStyle(R.font.blackout_sunrise, Typeface.NORMAL, FONT_TYPE_BLACKOUT_SUNRISE, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_LARGE, 0, 0, false, TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;

                    case TEMPLATE_4:
                        //Method called
                        applyTemplateStyle(R.font.fressh, Typeface.NORMAL, FONT_TYPE_FRESSH, 0
                                , Gravity.LEFT, R.drawable.ic_format_align_left_32, 2, TextGravity.West
                                , FONT_SIZE_MEDIUM, 0, 0, false, TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_5:
                        //Method called
                        applyTemplateStyle(R.font.komikaaxis, Typeface.NORMAL, FONT_TYPE_KOMIKAAXIS, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_SMALL, 0, 1, false, TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_6:
                        //Method called
                        applyTemplateStyle(R.font.komikaaxis, Typeface.NORMAL, FONT_TYPE_KOMIKAAXIS, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_SMALL, 0, 1, true
                                , TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE, R.drawable.contentshape_bottomtoplines);

                        break;
                    case TEMPLATE_7:
                        //Method called
                        applyTemplateStyle(R.font.langdon, Typeface.NORMAL, FONT_TYPE_LANGDON, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_LARGE, 0, 0, false
                                , TemplateHelper.SHAPE_NAME_NONE, 0);

                        break;
                    case TEMPLATE_8:
                        //Method called
                        applyTemplateStyle(R.font.a_love_of_thunder, Typeface.NORMAL, FONT_TYPE_A_LOVE_OF_THUNDER, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_LARGE, 0, 1, false
                                , TemplateHelper.SHAPE_NAME_NONE, 0);

                        break;
                    case TEMPLATE_9:
                        //Method called
                        applyTemplateStyle(R.font.ostrich_rounded, Typeface.NORMAL, FONT_TYPE_OSTRICH_ROUNDED, 0
                                , Gravity.LEFT, R.drawable.ic_format_align_left_32, 2, TextGravity.West
                                , FONT_SIZE_MEDIUM, 0, 0, false
                                , TemplateHelper.SHAPE_NAME_NONE, 0);

                        break;
                    case TEMPLATE_10:
                        //Method called
                        applyTemplateStyle(R.font.ostrich_rounded, Typeface.NORMAL, FONT_TYPE_OSTRICH_ROUNDED, 0
                                , Gravity.LEFT, R.drawable.ic_format_align_left_32, 2, TextGravity.West
                                , FONT_SIZE_MEDIUM, 0, 0, false
                                , TemplateHelper.SHAPE_NAME_SIDE_LINE, R.drawable.contentshape_leftline);
                        break;
                    case TEMPLATE_11:
                        //Method called
                        applyTemplateStyle(R.font.pacifico, Typeface.NORMAL, FONT_TYPE_PACIFICO, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_MEDIUM, 0, 0, false
                                , TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_12:
                        //Method called
                        applyTemplateStyle(R.font.pacifico, Typeface.NORMAL, FONT_TYPE_PACIFICO, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_MEDIUM, 0, 0, true
                                , TemplateHelper.SHAPE_NAME_QUOTE, R.drawable.contentshape_quotemarks);
                        break;
                    case TEMPLATE_13:
                        //Method called
                        applyTemplateStyle(R.font.poiret_one_regular, Typeface.NORMAL, FONT_TYPE_POIRET_ONE_REGULAR, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_LARGE, 0, 1, false
                                , TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_14:
                        //Method called
                        applyTemplateStyle(R.font.poiret_one_regular, Typeface.NORMAL, FONT_TYPE_POIRET_ONE_REGULAR, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_LARGE, 0, 1, true
                                , TemplateHelper.SHAPE_NAME_QUOTE, R.drawable.contentshape_quotemarks);
                        break;
                    case TEMPLATE_15:
                        //Method called
                        applyTemplateStyle(R.font.blackout_twoam, Typeface.NORMAL, FONT_TYPE_BLACKOUT_TWOAM, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_LARGE, 0, 1, false
                                , TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_16:
                        //Method called
                        applyTemplateStyle(R.font.yanone_kaffeesatz, Typeface.NORMAL, FONT_TYPE_YANONE_KAFFEESATZ, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_MEDIUM, 0, 1, false
                                , TemplateHelper.SHAPE_NAME_NONE, 0);
                        break;
                    case TEMPLATE_17:
                        //Method called
                        applyTemplateStyle(R.font.yanone_kaffeesatz, Typeface.NORMAL, FONT_TYPE_YANONE_KAFFEESATZ, 0
                                , Gravity.CENTER, R.drawable.ic_format_align_center_32, 0, TextGravity.Center
                                , FONT_SIZE_MEDIUM, 0, 1, true
                                , TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE, R.drawable.contentshape_bottomtoplines);
                        break;

                    default:


                }
            }
        });
    }

    /**
     * Method to initialize swipe listener on squareView.
     */
    private void initSwipeListener() {

        squareView.setOnTouchListener(new OnSwipeGestureListener(this) {

            @Override
            public void onDoubleClick() {
                switch (mImageTintFlag) {
                    case 0:
                        //Apply tint
                        imageShort.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_30));
                        //Update flag
                        mImageTintFlag = 1;
                        //set tint color
                        mImageTintColor = "4d000000";
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
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.with(mContext)
                                .load(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC))
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
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

                    }
                });
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

        //Map for request data
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("entityid", mEntityID);
        requestMap.put("type", mEntityType);


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
                        float factor = (float) squareView.getWidth() / 650;
                        try {
                            //if token status is not invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject responseObject = jsonObject.getJSONObject("data");

                                //retrieve long text
                                if (!TextUtils.isEmpty(responseObject.getString("text_long"))
                                        && !responseObject.getString("text_long").equals("null")) {

                                    longStoryText = responseObject.getString("text_long");

                                }


                                //Retrieve data from server response
                                mShortID = responseObject.getString("shoid");
                                String text = responseObject.getString("text");
                                int textSize = responseObject.getInt("textsize");
                                int textColor = (int) Long.parseLong(responseObject.getString("textcolor"), 16);
                                String fontType = responseObject.getString("font");
                                mBoldFlag = responseObject.getInt("bold");
                                mItalicFlag = responseObject.getInt("italic");

                                //Set textView property
                                textShort.setText(text);
                                textShort.setTextSize(ViewHelper.pixelsToSp(CollaborationActivity.this, textSize * factor));
                                // textShort.setTextColor(textColor);
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


                                //Set short text typeface
                                if (mItalicFlag == 0 && mBoldFlag == 0) {
                                    //Set typeface to normal
                                    textShort.setTypeface(getFontType(fontType, CollaborationActivity.this), Typeface.NORMAL);
                                    //Toggle view selection
                                    dotBold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_bold_32, 0, 0);
                                    dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_italic_32, 0, 0);
                                } else if (mItalicFlag == 0 && mBoldFlag == 1) {
                                    //Set typeface to bold
                                    textShort.setTypeface(getFontType(fontType, CollaborationActivity.this), Typeface.BOLD);
                                    //Toggle view selection
                                    dotBold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_bold_selected, 0, 0);
                                    dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_italic_32, 0, 0);
                                } else if (mItalicFlag == 1 && mBoldFlag == 0) {
                                    //Set typeface to italic
                                    textShort.setTypeface(getFontType(fontType, CollaborationActivity.this), Typeface.ITALIC);
                                    //Toggle view selection
                                    dotBold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_bold_32, 0, 0);
                                    dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_italic_selected, 0, 0);
                                } else if (mItalicFlag == 1 && mBoldFlag == 1) {
                                    //Set typeface to bold_italic
                                    textShort.setTypeface(getFontType(fontType, CollaborationActivity.this), Typeface.BOLD_ITALIC);
                                    //Toggle view selection
                                    dotBold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_bold_selected, 0, 0);
                                    dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_italic_selected, 0, 0);
                                }
                                //set typeface
                                mTextTypeface = getFontType(fontType, CollaborationActivity.this);
                                //Set font type
                                mFontType = fontType;
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

        //if signature text is not empty
        if (!TextUtils.isEmpty(mSignatureText)) {
            CaptureHelper.generateSignatureOnCapture(mSignatureText
                    , textSignature.getWidth()
                    , textSignature.getHeight()

                    , imageShort.getWidth());
        }
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
            // showShortPreview(divisionFactor);
            goToPreviewScreen(mHelper.getUUID()
                    , mHelper.getAuthToken()
                    , mShortID
                    , String.valueOf(textShort.getX() / divisionFactor)
                    , String.valueOf((textShort.getY() - squareView.getY()) / divisionFactor)
                    , String.valueOf(textShort.getWidth() / divisionFactor)
                    , String.valueOf(textShort.getHeight() / divisionFactor)
                    , textShort.getText().toString()
                    , String.valueOf(textShort.getTextSize() / divisionFactor)
                    , Integer.toHexString(textShort.getCurrentTextColor())
                    , textGravity.toString()
                    , String.valueOf(mImageWidth)
                    , mSignatureText
                    , mIsMerchantable
                    , mFontType
                    , String.valueOf(mBoldFlag)
                    , String.valueOf(mItalicFlag)
                    , mImageTintColor
                    , PREVIEW_EXTRA_CALLED_FROM_COLLABORATION
                    , mShapeName
                    , String.valueOf(mIsShadowSelected)
                    , longStoryText
            );

        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        //Disable drawing cache
        squareView.setDrawingCacheEnabled(false);
        squareView.destroyDrawingCache();

    }

    /**
     * Method to open previewActivity.
     */
    private void goToPreviewScreen(String uuid, String authKey, String shortID
            , String xPosition, String yPosition, String tvWidth, String tvHeight
            , String text, String textSize, String textColor, String textGravity
            , String imgWidth, String signature, String merchantable, String font
            , String bold, String italic, String imageTintColor, String calledFrom
            , String templateName, String isShadowSelected, String longText) {

        Intent intent = new Intent(CollaborationActivity.this, PreviewActivity.class);

        Bundle bundle = new Bundle();

        bundle.putString(PREVIEW_EXTRA_UUID, uuid);
        bundle.putString(PREVIEW_EXTRA_AUTH_KEY, authKey);
        bundle.putString(PREVIEW_EXTRA_SHORT_ID, shortID);
        bundle.putString(PREVIEW_EXTRA_X_POSITION, xPosition);
        bundle.putString(PREVIEW_EXTRA_Y_POSITION, yPosition);
        bundle.putString(PREVIEW_EXTRA_TV_WIDTH, tvWidth);
        bundle.putString(PREVIEW_EXTRA_TV_HEIGHT, tvHeight);
        bundle.putString(PREVIEW_EXTRA_TEXT, text);
        bundle.putString(PREVIEW_EXTRA_TEXT_SIZE, textSize);
        bundle.putString(PREVIEW_EXTRA_TEXT_COLOR, textColor);
        bundle.putString(PREVIEW_EXTRA_TEXT_GRAVITY, textGravity);
        bundle.putString(PREVIEW_EXTRA_IMG_WIDTH, imgWidth);
        bundle.putString(PREVIEW_EXTRA_SIGNATURE, signature);
        bundle.putString(PREVIEW_EXTRA_MERCHANTABLE, merchantable);
        bundle.putString(PREVIEW_EXTRA_FONT, font);
        bundle.putString(PREVIEW_EXTRA_BOLD, bold);
        bundle.putString(PREVIEW_EXTRA_ITALIC, italic);
        bundle.putString(PREVIEW_EXTRA_IMAGE_TINT_COLOR, imageTintColor);
        bundle.putString(PREVIEW_EXTRA_CALLED_FROM, calledFrom);
        bundle.putString(PREVIEW_EXTRA_TEMPLATE_NAME, templateName);
        bundle.putString(PREVIEW_EXTRA_IS_SHADOW_SELECTED, isShadowSelected);
        bundle.putString(PREVIEW_EXTRA_LONG_TEXT, longText.trim());

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
        //Template color bottomSheet if its expanded
        if (templateSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            templateSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /**
     * Method to apply gravity on text and update required flags.
     *
     * @param gravity         Gravity i.e Gravity.LEFT , Gravity.RIGHT and Gravity.CENTER
     * @param gravityDrawable ID of drawable to be applied.
     * @param gravityFlag     Gravity status i.e 0 for center , 1 for right and 2 for left.
     * @param tGravity        Enum gravity  i.e CENTER, WEST and EAST
     */
    private void applyGravity(int gravity, int gravityDrawable, int gravityFlag, CollaborationActivity.TextGravity tGravity) {
        //Set text gravity
        textShort.setGravity(gravity);
        //Change button drawable
        btnAlignText.setCompoundDrawablesWithIntrinsicBounds(0, gravityDrawable, 0, 0);
        //Change gravity flag
        mGravityFlag = gravityFlag;
        //Set gravity variable
        textGravity = tGravity;
    }

    /**
     * Method to apply bold style on text and update required flags.
     *
     * @param textStyle      Text style  i.e Typeface.ITALIC , Typeface.BOLD and Typeface.NORMAL
     * @param boldFlagValue  1 if bold selected 0 otherwise.
     * @param boldDrawable   Drawable for bold button
     * @param italicDrawable Drawable for italic button
     */
    private void applyBold(int textStyle, int boldFlagValue, int boldDrawable, int italicDrawable) {
        //Set typeface to bold
        textShort.setTypeface(mTextTypeface, textStyle);
        //Update flag
        mBoldFlag = boldFlagValue;
        //Toggle view selection
        dotBold.setCompoundDrawablesWithIntrinsicBounds(0, boldDrawable, 0, 0);
        dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, italicDrawable, 0, 0);
    }

    /**
     * Method to apply italic style on text and update required flags.
     *
     * @param textStyle       Text style  i.e Typeface.ITALIC , Typeface.BOLD and Typeface.NORMAL
     * @param italicFlagValue 1 if italic selected 0 otherwise.
     * @param boldDrawable    Drawable for bold button
     * @param italicDrawable  Drawable for italic button
     */
    private void applyItalic(int textStyle, int italicFlagValue, int boldDrawable, int italicDrawable) {
        //Set typeface to bold
        textShort.setTypeface(mTextTypeface, textStyle);
        //Update flag
        mItalicFlag = italicFlagValue;
        //Toggle view selection
        dotBold.setCompoundDrawablesWithIntrinsicBounds(0, boldDrawable, 0, 0);
        dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, italicDrawable, 0, 0);
    }

    /**
     * Method to apply selected template style and update required flags.
     *
     * @param fontValue       Font to be applied text
     * @param fontStyle       Font style  i.e Typeface.BOLD , Typeface.ITALIC , Typeface.NORMAL
     * @param fontType        Type of font
     * @param boldFlag        1 if bold applied 0 otherwise.
     * @param gravity         Gravity i.e Gravity.LEFT , Gravity.RIGHT , Gravity.Center
     * @param gravityDrawable ID of drawable to be applied on text alignment button.
     * @param gravityFlag     Gravity  0 for center , 1 for right and 2 for left.
     * @param tGravity        TextGravity i.e WEST , EAST , CENTER
     * @param fontSize        Font size to be applied
     * @param italicFlag      1 if italic applied 0 otherwise.
     * @param shadowFlag      1 if text shadow applied 0 otherwise.
     * @param isShapeSelected true if shape  selected false otherwise.
     * @param shapeName       Current selected name
     * @param shapeID         ID of drawable to be applied for shape.
     */
    private void applyTemplateStyle(int fontValue, int fontStyle, String fontType, int boldFlag
            , int gravity, int gravityDrawable, int gravityFlag, TextGravity tGravity
            , int fontSize, int italicFlag, int shadowFlag, boolean isShapeSelected
            , String shapeName, int shapeID) {
        // set font and update flags
        textShort.setTypeface(ResourcesCompat.getFont(mContext, fontValue), fontStyle);
        mFontType = fontType;
        mTextTypeface = FontsHelper.getFontType(mFontType, mContext);

        // update bold flag
        mBoldFlag = boldFlag;
        if (mBoldFlag == 0) {
            dotBold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_bold_32, 0, 0);
        } else {
            dotBold.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_bold_selected, 0, 0);
        }

        // set gravity params
        applyGravity(gravity, gravityDrawable, gravityFlag, tGravity);

        //Update font size and text size slider
        textShort.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        seekBarTextSize.setProgress(fontSize - FONT_SIZE_DEFAULT);

        // update italic status
        mItalicFlag = italicFlag;
        if (mItalicFlag == 0) {
            dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_italic_32, 0, 0);
        } else {
            dotItalic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_format_italic_selected, 0, 0);
        }

        //update shadow
        if (shadowFlag == 0) {
            textShort.setShadowLayer(0, 0, 0, 0);
            mIsShadowSelected = 0;
            dotShadow.setVisibility(View.INVISIBLE);
        } else {
            textShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(mContext, R.color.color_grey_600));
            mIsShadowSelected = 1;
            dotShadow.setVisibility(View.VISIBLE);
        }

        //if shape selected
        if (isShapeSelected) {
            // update shape
            textShort.setBackground(ContextCompat.getDrawable(mContext, shapeID));
            mIsShapeSelected = true;
            mShapeName = shapeName;
            setContentShapeColor(textShort.getCurrentTextColor(), mShapeName, textShort, mContext);
        } else {
            textShort.setBackground(null);
            mIsShapeSelected = false;
            mShapeName = shapeName;
        }

    }
}
