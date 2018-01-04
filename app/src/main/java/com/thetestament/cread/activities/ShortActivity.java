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
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FontAdapter;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.OnDragTouchListener;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FontModel;
import com.thetestament.cread.utils.SquareView;
import com.thetestament.cread.widgets.CustomEditText;
import com.thetestament.cread.widgets.CustomEditText.OnEditTextBackListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.FontsHelper.fontTypes;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_INSPIRATION_CLICKED;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_AUTH_KEY;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BG_COLOR;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_BOLD;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_SHORT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_FONT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_IMG_WIDTH;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ITALIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_MERCHANTABLE;
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

/**
 * Here user creates his/her shorts and uploads on the server.
 */

public class ShortActivity extends BaseActivity implements ColorChooserDialog.ColorCallback, OnEditTextBackListener {

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


    @BindView(R.id.bottomSheetView)
    NestedScrollView bottomSheetView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private BottomSheetBehavior sheetBehavior;
    //Define font typeface
    private Typeface mTextTypeface;

    private ArrayList<FontModel> mFontDataList = new ArrayList<>();

    @State
    String mCaptureUrl, mCaptureID = "", mSignatureText, mShortBgColor = "FFFFFFFF", mFontType = "montserrat_regular.ttf";

    @State
    int mImageWidth = 650;


    /**
     * Flag to maintain merchantable status i.e true if merchantable is present false otherwise.
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
        setContentView(R.layout.activity_short);
        //ButterKnife view binding
        ButterKnife.bind(this);
        //set listener
        textShort.setOnEditTextBackListener(this);

        mHelper = new SharedPreferenceHelper(this);
        //initialize screen
        initScreen();
        //initialize seek bar
        initSeekBar(seekBarTextSize);
        //For bottomSheet
        sheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        sheetBehavior.setPeekHeight(0);
        //Set default font
        mTextTypeface = ResourcesCompat.getFont(ShortActivity.this, R.font.montserrat_regular);
        //initialise fontLayout bottomSheet
        initFontLayout();
        //initialize listener
        initDragListener();
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
                }
                break;
            case REQUEST_CODE_PREVIEW_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    //Finish this screen
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
                CustomDialog.getBackNavigationDialog(ShortActivity.this
                        , "Discard changes?"
                        , "If you go back now, you will loose your changes.");
                return true;
            case R.id.action_signature:
                if (signatureStatus) {
                    String s = textShort.getText().toString();
                    String removedText = s.replace(mSignatureText, "").trim();
                    textShort.setText(removedText);
                    signatureStatus = false;
                } else {
                    textShort.setText(textShort.getText() + "\n \n" + mSignatureText);
                    signatureStatus = true;
                }
                return true;
            case R.id.action_next:
                //If short text is empty
                if (TextUtils.getTrimmedLength(textShort.getText().toString()) == 0) {
                    //Show toast message
                    ViewHelper.getToast(this, "Short can't be empty. Please Write something.");
                } else {
                    //Remove underline
                    textShort.clearComposingText();
                    //Remove tint to imageView
                    imageShort.clearColorFilter();

                    getRuntimePermission();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        if (mColorChooserType.equals("texColor")) {
            //Change short text color
            textShort.setTextColor(selectedColor);
        } else if (mColorChooserType.equals("backGroundColor")) {
            //Change backgroundColor
            imageShort.setBackgroundColor(selectedColor);
            //Update flag
            mIsBgColorPresent = true;
        }

    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
        //do nothing
    }

    @Override
    public void onBackPressed() {
        //Show prompt dialog
        CustomDialog.getBackNavigationDialog(ShortActivity.this
                , "Discard changes?"
                , "If you go back now, you will loose your changes.");
    }

    /**
     * Edit text back listener
     */
    @Override
    public void onBack() {
        //initialize listener
        initDragListener();
        //Remove tint to imageView
        imageShort.clearColorFilter();
        //Toggle visibility
        formatOptions.setVisibility(View.VISIBLE);
        seekBarTextSize.setVisibility(View.VISIBLE);
        viewFormatTextSize.setVisibility(View.VISIBLE);
    }

    /**
     * Click functionality to hide bottom sheet.
     */
    @OnClick(R.id.rootView)
    void rootViewOnClick() {
        //Collapse bottomSheet if its expanded
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @OnClick(R.id.imageContainer)
    void onContainerClick() {
        //Collapse bottomSheet if its expanded
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        //Hide edit text cursor
        textShort.setCursorVisible(false);
        //Remove tint to imageView
        imageShort.clearColorFilter();
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
    @OnClick(R.id.btnInspireMe)
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
     * Functionality to change canvas bac
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
            //Show color dialog
            showColorChooserDialog();
        }
    }

    /**
     * Click functionality to show material color palette dialog.
     */
    @OnClick(R.id.btnFormatTextColor)
    void onBtnFormatTextColorClicked() {
        //Set type
        mColorChooserType = "texColor";
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
            //Update flag
            mIsImagePresent = true;
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
     * Method to initialize font bottom sheet.
     */
    private void initFontLayout() {
        //initialize font data list
        for (String fontName : fontTypes) {
            FontModel data = new FontModel();
            data.setFontName(fontName);
            mFontDataList.add(data);
        }

        //Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(ShortActivity.this, LinearLayoutManager.HORIZONTAL, false));
        //Set adapter
        FontAdapter fontAdapter = new FontAdapter(mFontDataList, ShortActivity.this);
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
                imageShort.setColorFilter(ContextCompat.getColor(ShortActivity.this, R.color.transparent));
                //Show keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(textShort, 0);
                //Remove listener
                textShort.setOnTouchListener(null);
                //Toggle visibility
                formatOptions.setVisibility(View.GONE);
                seekBarTextSize.setVisibility(View.GONE);
                viewFormatTextSize.setVisibility(View.GONE);
            }
        }));
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
                        Picasso.with(ShortActivity.this).load(imageUrl).into(new Target() {
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
                    , PREVIEW_EXTRA_CALLED_FROM_SHORT

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
    private void goToPreviewScreen(String uuid, String authKey, String captureID
            , String xPosition, String yPosition, String tvWidth, String tvHeight
            , String text, String textSize, String textColor, String textGravity
            , String imgWidth, String merchantable, String font, String bgColor
            , String bold, String italic, String calledFrom) {

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
        bundle.putString(PREVIEW_EXTRA_CALLED_FROM, calledFrom);

        intent.putExtra(PREVIEW_EXTRA_DATA, bundle);
        startActivityForResult(intent, REQUEST_CODE_PREVIEW_ACTIVITY);
    }

}
