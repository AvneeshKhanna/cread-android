package com.thetestament.cread.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.fragments.ExploreFragment;
import com.thetestament.cread.fragments.FeedFragment;
import com.thetestament.cread.fragments.MeFragment;
import com.thetestament.cread.helpers.BottomNavigationViewHelper;
import com.thetestament.cread.helpers.CaptureHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnServerRequestedListener;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.ImageHelper.compressSpecific;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.startImageCropping;
import static com.thetestament.cread.helpers.NetworkHelper.getRestartHerokuObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.helpers.ViewHelper.convertToPx;
import static com.thetestament.cread.utils.Constant.EXTRA_OPEN_SPECIFIC_BOTTOMNAV_FRAGMENT;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_EXPLORE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FEED_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_NOTIFICATION_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_CAPTURE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_POST;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_ROYALTIES_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_WRITE_EXTERNAL_STORAGE;
import static com.thetestament.cread.utils.Constant.TAG_EXPLORE_FRAGMENT;
import static com.thetestament.cread.utils.Constant.TAG_FEED_FRAGMENT;
import static com.thetestament.cread.utils.Constant.TAG_ME_FRAGMENT;

/**
 * Class to provide bottom navigation functionality.
 */

public class BottomNavigationActivity extends BaseActivity {

    //region -Butter knife view binding
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.toolBar)
    Toolbar toolbar;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView navigationView;
    //endregion

    //region -Fields and constants
    /**
     * Flag to maintain tag of currently selected fragment.
     */
    @State
    String mFragmentTag;
    /**
     * Flag to maintain recently selected fragment.
     */
    Fragment mCurrentFragment;

    /**
     * View to show dot indicator on 'ME'.
     */
    View personalChatIndicator;

    /**
     * Resource ID of currently selected bottomNavigation menu.
     */
    @State
    int mSelectedItemID;

    /**
     * Flag to maintain whether this screen is open from third party apps(using intent fillet) or not.
     */
    @State
    boolean mCalledFromSendIntent = false;

    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferenceHelper mHelper;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private BottomNavigationActivity mContext;
    View badgeView;


    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.thetestament.cread.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "cread.com";
    // The account name
    public static final String ACCOUNT = "Cread";
    // Instance fields
    public static Account mAccount;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    //endregion

    //region -Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //Obtain sharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
        //Obtain reference of this activity
        mContext = this;
        //Bind View to this activity
        ButterKnife.bind(this);
        //Set actionbar
        setSupportActionBar(toolbar);
        //Set title
        setTitle("Cread");

        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
            mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, mFragmentTag);
        } else {
            //To load appropriate screen
            loadScreen();
        }

        //To setup account for sync adapter
        createSyncAccount(this);
        //Initialize navigation view
        initBottomNavigation();
        //Method call
        captureSendIntent(mHelper, getIntent());
        //Add badge view on me tab icon
        addPersonalChatIndicator();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Toggle personal chat indicator
        togglePersonalChatIndicator(mHelper.getPersonalChatIndicatorStatus());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // or sharing image from gallery
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            captureSendIntent(mHelper, intent);
        }
        //When new intent for specific fragment
        else if (intent.hasExtra(EXTRA_OPEN_SPECIFIC_BOTTOMNAV_FRAGMENT)) {
            initSpecificFragment(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GALLERY:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    startImageCropping(mContext
                            , data.getData()
                            , getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));
                } else {
                    ViewHelper.getSnackBar(rootView
                            , getString(R.string.error_img_not_attached));
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get image width and height
                    float width = data.getIntExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, 1800);
                    float height = data.getIntExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, 1800);

                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);

                    //Check for image manipulation
                    if (AspectRatioUtils.getSquareImageManipulation(width, height)) {
                        //Create square image with blurred background
                        performSquareImageManipulation(mCroppedImgUri);
                    } else {
                        //Method called
                        processCroppedImage(mCroppedImgUri);
                    }

                    //Finish this screen if called from other apps
                    if (mCalledFromSendIntent) {
                        finish();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView
                            , getString(R.string.error_img_not_cropped));
                }
                break;

            case REQUEST_CODE_ROYALTIES_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    // open bottom sheet
                    getAddContentBottomSheetDialog();
                }
                break;
            case REQUEST_CODE_EDIT_POST:
                if (resultCode == RESULT_OK) {
                    initMeFragment(true);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
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
                chooseImageFromGallery();
            } else {
                ViewHelper.getToast(this
                        , getString(R.string.error_msg_permission_denied));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentFragment.isAdded()) {
            getSupportFragmentManager().putFragment(outState, mFragmentTag, mCurrentFragment);
        }
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cread, menu);
        //Set visibility of restart Heroku option according to build config
        menu.findItem(R.id.action_restart_heroku)
                .setVisible(BuildConfig.VISIBILITY_RESTART_HEROKU_OPTION);

        setupBadge(menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_updates:
                //Open updates screen
                startActivity(new Intent(mContext, UpdatesActivity.class));
                // set status to false
                mHelper.setNotifIndicatorStatus(false);
                // hide badge
                badgeView.setVisibility(View.GONE);
                //Log firebase analytics
                setAnalytics(FIREBASE_EVENT_NOTIFICATION_CLICKED);

                return true;
            case R.id.action_settings:
                //Launch settings activity
                startActivity(new Intent(mContext, SettingsActivity.class));
                return true;

            case R.id.action_restart_heroku:
                restartHerokuServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //endregion

    //region -Private methods

    /**
     * Method to load required screen.
     */
    private void loadScreen() {
        //When bottom nav opened to open specific fragment
        if (getIntent().hasExtra(EXTRA_OPEN_SPECIFIC_BOTTOMNAV_FRAGMENT)) {
            initSpecificFragment(getIntent());
        } else {
            openFeedFragment();
        }
    }

    /**
     * Method to initialize BottomNavigation view item click functionality.
     */
    private void initBottomNavigation() {
        //To disable shift mode
        BottomNavigationViewHelper.disableShiftMode(navigationView);
        //BottomNavigation navigation listener implementation
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Bottom navigation click functionality
                switch (item.getItemId()) {
                    case R.id.action_feed:
                        //Set title
                        setTitle("Cread");
                        getSupportActionBar().setElevation(
                                convertToPx(mContext, 4));
                        setTheme(R.style.BottomNavigationActivityTheme);
                        mCurrentFragment = new FeedFragment();
                        //set fragment tag
                        mFragmentTag = TAG_FEED_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag, false);
                        //Log firebase event
                        setAnalytics(FIREBASE_EVENT_FEED_CLICKED);
                        //Update flag
                        mSelectedItemID = R.id.action_feed;
                        break;

                    case R.id.action_explore:
                        //Set title
                        setTitle("Explore");
                        getSupportActionBar().setElevation(0);
                        setTheme(R.style.ZeroElevationActivityTheme);
                        mCurrentFragment = new ExploreFragment();
                        //Set fragment tag
                        mFragmentTag = TAG_EXPLORE_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag, false);
                        //Log firebase event
                        setAnalytics(FIREBASE_EVENT_EXPLORE_CLICKED);
                        //Update flag
                        mSelectedItemID = R.id.action_explore;
                        break;

                    case R.id.action_add:
                        getAddContentBottomSheetDialog();
                        //Log firebase event
                        setAnalytics(FIREBASE_EVENT_WRITE_CLICKED);
                        break;

                    case R.id.action_me:
                        //if new messages are
                        togglePersonalChatIndicator(false);
                        initMeFragment(false);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Method to replace current screen with new fragment.
     *
     * @param fragment    Fragment to be open.
     * @param tagFragment Tag for the fragment to be opened.
     */
    public void replaceFragment(Fragment fragment, String tagFragment, boolean allowStateLoss) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navigationView, fragment, tagFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        if (allowStateLoss) {
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            fragmentTransaction.commit();
        }
    }

    /**
     * Method to show bottomSheet dialog with 'write a short' and 'Upload a capture' option.
     */
    public void getAddContentBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = this.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_add_content, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        LinearLayout buttonWrite = sheetView.findViewById(R.id.buttonWrite);
        LinearLayout buttonCapture = sheetView.findViewById(R.id.buttonCapture);

        //Write button functionality
        buttonWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHelper.isShortFirstTime()) {
                    getShortDialog();
                } else {
                    //Open ShortActivity
                    startActivity(new Intent(mContext, ShortActivity.class));
                }
                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
        //Capture button functionality
        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Capture clicked for first time
                if (mHelper.isCaptureFirstTime()) {
                    //Show capture intro dialog
                    getCaptureIntroDialog();
                } else {
                    getRuntimePermission();
                }
                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });

        //Bottom sheet dialog dismiss listener
        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //Select previous selected menu item
                navigationView.getMenu().findItem(mSelectedItemID).setChecked(true);

            }
        });

    }

    /**
     * Method to get WRITE_EXTERNAL_STORAGE permission and perform specified operation.
     */
    public void getRuntimePermission() {
        //Check for WRITE_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this
                , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ViewHelper.getToast(this
                        , getString(R.string.error_msg_capture_permission_denied));
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            }
        }
        //If permission is granted
        else {
            chooseImageFromGallery();
        }
    }

    /**
     * Open gallery so user can choose his/her capture for uploading.
     */
    private void chooseImageFromGallery() {
        //Launch gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_GALLERY);
    }

    /**
     * Method to perform required operation on cropped image.
     *
     * @param uri Uri of cropped image.
     */
    private void processCroppedImage(Uri uri) {
        try {
            //Decode image file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;

            //If resolution of image is greater than 3000x13000 then compress this image
            if (Math.min(imageHeight, imageWidth) >= 3000) {
                //Compress image
                compressSpecific(uri, this, IMAGE_TYPE_USER_CAPTURE_PIC);
                //Open preview screen
                Bundle bundle = new Bundle();
                bundle.putString(PREVIEW_EXTRA_MERCHANTABLE, "1");
                bundle.putString(PREVIEW_EXTRA_CALLED_FROM, PREVIEW_EXTRA_CALLED_FROM_CAPTURE);

                Intent intent = new Intent(mContext, PreviewActivity.class);
                intent.putExtra(PREVIEW_EXTRA_DATA, bundle);
                startActivity(intent);
            } else if (Math.min(imageHeight, imageWidth) >= 1800) {
                //Open preview screen
                Bundle bundle = new Bundle();
                bundle.putString(PREVIEW_EXTRA_MERCHANTABLE, "1");
                bundle.putString(PREVIEW_EXTRA_CALLED_FROM, PREVIEW_EXTRA_CALLED_FROM_CAPTURE);

                Intent intent = new Intent(mContext, PreviewActivity.class);
                intent.putExtra(PREVIEW_EXTRA_DATA, bundle);
                startActivity(intent);
            } else {
                getMerchantableDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "BottomNavigationActivity");
            ViewHelper.getSnackBar(rootView, getString(R.string.error_img_not_cropped));
        }
    }

    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent) {
        SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
        Bundle bundle = new Bundle();
        bundle.putString("uuid", helper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_FEED_CLICKED)) {
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_FEED_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_EXPLORE_CLICKED)) {
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_EXPLORE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_NOTIFICATION_CLICKED)) {
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_NOTIFICATION_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_WRITE_CLICKED)) {
            bundle.putString("class_name", "from_add_item");
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_WRITE_CLICKED, bundle);
        }

    }

    /**
     * Method to show intro dialog when user land on this screen for the first time.
     */
    private void getCaptureIntroDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_generic, false)
                .positiveText(getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getRuntimePermission();
                        dialog.dismiss();
                        //update status
                        mHelper.updateCaptureIntroStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);

        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_capture_intro));
        //Set title text
        textTitle.setText("Upload your best shot!");
        //Set description text
        textDesc.setText("A photograph says a thousand words. Sharing your work here can inspire thousands of writers. All the best!");
    }

    /**
     * Method to show intro dialog when user land on this screen for the first time.
     */
    private void getShortDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_generic, false)
                .positiveText(getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open ShortActivity
                        startActivity(new Intent(mContext, ShortActivity.class));
                        dialog.dismiss();
                        //update status
                        mHelper.updateShortIntroStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);

        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.img_short_intro));
        //Set title text
        textTitle.setText("Write your masterpiece here");
        //Set description text
        textDesc.setText("This is where you must share your words. We'll save it as an image to inspire people and prevent plagiarism.");
    }

    /**
     * Method to show dialog when user tries to upload low resolution image.
     */
    private void getMerchantableDialog() {
        new MaterialDialog.Builder(this)
                .title("Note")
                .content("The resolution of this image is not printable. Other users won't be able to order a print version of it. Do you wish to proceed?")
                .positiveText("Proceed")
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open preview screen
                        Bundle bundle = new Bundle();
                        bundle.putString(PREVIEW_EXTRA_MERCHANTABLE, "0");
                        bundle.putString(PREVIEW_EXTRA_CALLED_FROM, PREVIEW_EXTRA_CALLED_FROM_CAPTURE);

                        Intent intent = new Intent(mContext, PreviewActivity.class);
                        intent.putExtra(PREVIEW_EXTRA_DATA, bundle);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void activateBottomNavigationItem(int id) {
        navigationView.setSelectedItemId(id);
    }

    private void restartHerokuServer() {
        requestServer(mCompositeDisposable,
                getRestartHerokuObservable(),
                this,
                new OnServerRequestedListener<String>() {
                    @Override
                    public void onDeviceOffline() {
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(String response) {
                        ViewHelper.getSnackBar(rootView, response);
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "BottomNavigationActivity");
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

                    }
                });
    }

    private void initMeFragment(boolean allowStateLoss) {
        //Set title
        setTitle("Me");
        getSupportActionBar().setElevation(
                convertToPx(mContext, 4));
        setTheme(R.style.BottomNavigationActivityTheme);
        Bundle meBundle = new Bundle();
        meBundle.putString("calledFrom", "BottomNavigationActivity");
        mCurrentFragment = new MeFragment();
        mCurrentFragment.setArguments(meBundle);
        //set fragment tag
        mFragmentTag = TAG_ME_FRAGMENT;
        replaceFragment(mCurrentFragment, mFragmentTag, allowStateLoss);
        //Update flag
        mSelectedItemID = R.id.action_me;
    }

    /**
     * Method to handle send intent from other apps.
     *
     * @param helper SharedPreference reference
     */
    private void captureSendIntent(SharedPreferenceHelper helper, Intent intent) {
        // Get intent, action and MIME type
        String action = intent.getAction();
        String type = intent.getType();

        //User is not logged in
        if (TextUtils.isEmpty(helper.getAuthToken()) && TextUtils.isEmpty(helper.getUUID())) {
            //Update flag
            mCalledFromSendIntent = true;
            //Open main screen
            ViewHelper.getToast(mContext, getString(R.string.text_msg_share_loggedin));
            startActivity(new Intent(mContext, MainActivity.class));
            //Finish this activity
            finish();
        }
        //user is logged in
        else {
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if (type.startsWith("image/")) {
                    //Retrieve image uri
                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        // To crop the received image
                        startImageCropping(this, imageUri, getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));
                    }
                }
            }
        }
    }

    /**
     * Method to setup updates icon badge indicator and its functionality.
     *
     * @param menu BottomNavigation menu.
     */
    private void setupBadge(final Menu menu) {
        //Action layout of chat icon
        View actionView = menu.findItem(R.id.action_updates).getActionView();
        //Obtain reference of badgeView
        badgeView = actionView.findViewById(R.id.updatesDotBadge);
        //Action view click functionality
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(menu.findItem(R.id.action_updates));
            }
        });

        //Toggle visibility of dot indicator
        if (mHelper.shouldShowNotifIndicator()) {
            //Hide badge view
            badgeView.setVisibility(View.VISIBLE);
        } else {
            //Show Badge View
            badgeView.setVisibility(View.GONE);
        }
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, AUTHORITY, new Bundle(), 25200);
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }

    }

    /**
     * Method to open FeedFragment and initialize required flags/variables.
     */
    private void openFeedFragment() {
        //When app opened normally
        navigationView.setSelectedItemId(R.id.action_feed);
        //To open Feed Screen
        mCurrentFragment = new FeedFragment();
        //Set fragment tag
        mFragmentTag = TAG_FEED_FRAGMENT;
        replaceFragment(mCurrentFragment, mFragmentTag, false);
        //Update flag
        mSelectedItemID = R.id.action_feed;
    }

    private void initSpecificFragment(Intent intent) {
        switch (intent.getStringExtra(EXTRA_OPEN_SPECIFIC_BOTTOMNAV_FRAGMENT)) {
            case TAG_EXPLORE_FRAGMENT:
                activateBottomNavigationItem(R.id.action_explore);
                //To open Explore Screen
                mCurrentFragment = new ExploreFragment();
                //Set fragment tag
                mFragmentTag = TAG_EXPLORE_FRAGMENT;
                replaceFragment(mCurrentFragment, mFragmentTag, false);
                //Update flag
                mSelectedItemID = R.id.action_explore;
                break;
            case TAG_ME_FRAGMENT:
                break;
            case TAG_FEED_FRAGMENT:
                break;
            default:
                openFeedFragment();
        }
    }

    /**
     * Method to add notification indicator to Me icon.
     */
    private void addPersonalChatIndicator() {
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) navigationView.getChildAt(0);

        View v = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) v;

        personalChatIndicator = LayoutInflater.from(this)
                .inflate(R.layout.layout_personal_chat_indicator, bottomNavigationMenuView, false);
        itemView.addView(personalChatIndicator);
    }

    /**
     * Toggle personal chat indicator.
     *
     * @param showIndicator Whether to show indicator or not .
     */
    private void togglePersonalChatIndicator(boolean showIndicator) {
        if (showIndicator) {
            //Show personal chat indicator
            personalChatIndicator.findViewById(R.id.notificationsBadge).setVisibility(View.VISIBLE);
        } else {
            //Hide personal chat indicator
            personalChatIndicator.findViewById(R.id.notificationsBadge).setVisibility(View.GONE);
        }
    }


    /**
     * Method to perform square image manipulation.
     *
     * @param croppedImageUri
     */
    private void performSquareImageManipulation(Uri croppedImageUri) {
        try {
            //Compress image
            compressSpecific(croppedImageUri, this, IMAGE_TYPE_USER_CAPTURE_PIC);
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "BottomNavigationActivity");
        }
        //Decode image file
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        int scaleFactor;
        boolean isLandscape;

        //Image is not available in square from
        if (imageWidth != imageHeight) {
            if (imageWidth > imageHeight) {
                scaleFactor = imageWidth;
                isLandscape = true;
            } else {
                scaleFactor = imageHeight;
                isLandscape = false;
            }

            //Decode bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(new File(croppedImageUri.getPath()).getAbsolutePath());

            //Method call
            CaptureHelper.createSquareBlurBitmap(mContext
                    , bitmap
                    , scaleFactor
                    , isLandscape);

            //Method call
            processCroppedImage(ImageHelper.getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));
        }
        //Image is available in square form
        else {
            //Method called
            processCroppedImage(croppedImageUri);
        }
    }

    //endregion
}
