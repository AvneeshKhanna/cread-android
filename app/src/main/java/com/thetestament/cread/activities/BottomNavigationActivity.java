package com.thetestament.cread.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.database.NotificationsDBFunctions;
import com.thetestament.cread.fragments.ExploreFragment;
import com.thetestament.cread.fragments.FeedFragment;
import com.thetestament.cread.fragments.MeFragment;
import com.thetestament.cread.helpers.BottomNavigationViewHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.yalantis.ucrop.UCrop;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.helpers.ImageHelper.compressCroppedImg;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.startImageCropping;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_EXPLORE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FEED_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_NOTIFICATION_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_WRITE_EXTERNAL_STORAGE;
import static com.thetestament.cread.utils.Constant.TAG_EXPLORE_FRAGMENT;
import static com.thetestament.cread.utils.Constant.TAG_FEED_FRAGMENT;
import static com.thetestament.cread.utils.Constant.TAG_ME_FRAGMENT;

/**
 * Class to provide bottom navigation functionality.
 */

public class BottomNavigationActivity extends BaseActivity {

    @BindView(R.id.rootView)
    RelativeLayout rootView;
    @BindView(R.id.toolBar)
    Toolbar toolbar;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView navigationView;

    @State
    String mFragmentTag;
    Fragment mCurrentFragment;

    private int mUnreadCount = 0;

    @State
    int mSelectedItemID;

    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferenceHelper mHelper;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //Obtain sharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
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
        //Initialize navigation view
        initBottomNavigation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //switch (requestCode) {
         /*   case REQUEST_CODE_OPEN_GALLERY:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    CropImage.activity(data.getData())
                            .setAllowRotation(true)
                            .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                            .setAspectRatio(1, 1)
                            .setActivityTitle("Edit")
                            .start(BottomNavigationActivity.this);
                } else {
                    ViewHelper.getSnackBar(rootView, "Image from gallery was not attached");
                }
                break;
            //For more information please visit "https://github.com/ArthurHub/Android-Image-Cropper"
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    Uri resultUri = result.getUri();
                    processCroppedImage(resultUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;
        }*/
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GALLERY:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    startImageCropping(this, data.getData(), getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));
                } else {
                    ViewHelper.getSnackBar(rootView, "Image from gallery was not attached");
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);
                    processCroppedImage(mCroppedImgUri);

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseImageFromGallery();
            } else {
                ViewHelper.getToast(this
                        , "The app won't function properly since the permission for storage was denied.");
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
        UpdateNotificationBadge updateNotificationBadge = new UpdateNotificationBadge();
        updateNotificationBadge.execute(menu);

        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cread, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_updates:
                // To hide the badge
                mUnreadCount = 0;
                ActionItemBadge.update(BottomNavigationActivity.this
                        , item
                        , ContextCompat.getDrawable(BottomNavigationActivity.this, R.drawable.ic_notifications_24)
                        , ActionItemBadge.BadgeStyles.DARK_GREY, null);

                Thread thread = new Thread(
                        new Runnable() {
                            @Override
                            public void run() {

                                NotificationsDBFunctions dbFunctions = new NotificationsDBFunctions(BottomNavigationActivity.this);
                                dbFunctions.accessNotificationsDatabase();
                                dbFunctions.setRead();
                                //Open updates screen
                                startActivity(new Intent(BottomNavigationActivity.this, UpdatesActivity.class));
                            }
                        }
                );
                thread.start();

                //Log firebase analytics
                setAnalytics(FIREBASE_EVENT_NOTIFICATION_CLICKED);

                return true;
            case R.id.action_settings:
                //Launch settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Method to load required screen.
     */
    private void loadScreen() {
        //When app opened normally
        navigationView.setSelectedItemId(R.id.action_feed);
        //To open Feed Screen
        mCurrentFragment = new FeedFragment();
        //Set fragment tag
        mFragmentTag = TAG_FEED_FRAGMENT;
        replaceFragment(mCurrentFragment, mFragmentTag);

        mSelectedItemID = R.id.action_feed;
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
                        mCurrentFragment = new FeedFragment();
                        //set fragment tag
                        mFragmentTag = TAG_FEED_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag);
                        //Log firebase event
                        setAnalytics(FIREBASE_EVENT_FEED_CLICKED);
                        //Update flag
                        mSelectedItemID = R.id.action_feed;
                        break;

                    case R.id.action_explore:
                        //Set title
                        setTitle("Explore");
                        mCurrentFragment = new ExploreFragment();
                        //Set fragment tag
                        mFragmentTag = TAG_EXPLORE_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag);
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
                        //Set title
                        setTitle("Me");
                        Bundle meBundle = new Bundle();
                        meBundle.putString("calledFrom", "BottomNavigationActivity");
                        mCurrentFragment = new MeFragment();
                        mCurrentFragment.setArguments(meBundle);
                        //set fragment tag
                        mFragmentTag = TAG_ME_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag);
                        //Update flag
                        mSelectedItemID = R.id.action_me;
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
    public void replaceFragment(Fragment fragment, String tagFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.navigationView, fragment, tagFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
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
                    startActivity(new Intent(BottomNavigationActivity.this, ShortActivity.class));
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
            Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
            //If resolution of image is greater than 2000x2000 then compress this image
            if (bitmap.getWidth() > 1800 && bitmap.getWidth() > 1800) {
                //Compress image
                compressCroppedImg(uri, this, IMAGE_TYPE_USER_CAPTURE_PIC);
                //Open preview screen
                Intent intent = new Intent(BottomNavigationActivity.this, CapturePreviewActivity.class);
                intent.putExtra("isMerchantable", "1");
                startActivity(intent);
            } else {
                getMerchantableDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
        }
    }

    /**
     * async task to update the unread count in notification badge
     */
    class UpdateNotificationBadge extends AsyncTask<Menu, Void, Menu> {


        @Override
        protected Menu doInBackground(Menu... menus) {

            NotificationsDBFunctions notificationsDBFunctions = new NotificationsDBFunctions(BottomNavigationActivity.this);
            notificationsDBFunctions.accessNotificationsDatabase();

            mUnreadCount = notificationsDBFunctions.getUnreadCount();


            return menus[0];
        }

        @Override
        protected void onPostExecute(Menu menu) {
            super.onPostExecute(menu);

            //you can add some logic (hide it if the count == 0)
            if (mUnreadCount > 0) {
                ActionItemBadge.update(BottomNavigationActivity.this, menu.findItem(R.id.action_updates), ContextCompat.getDrawable(BottomNavigationActivity.this, R.drawable.ic_notifications_24)/*FontAwesome.Icon.faw_android*/, ActionItemBadge.BadgeStyles.DARK_GREY, mUnreadCount);

            } else {

                // setting badge count parameter as null to hide the badge
                ActionItemBadge.update(BottomNavigationActivity.this, menu.findItem(R.id.action_updates), ContextCompat.getDrawable(BottomNavigationActivity.this, R.drawable.ic_notifications_24)/*FontAwesome.Icon.faw_android*/, ActionItemBadge.BadgeStyles.DARK_GREY, null);

            }


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
                        startActivity(new Intent(BottomNavigationActivity.this, ShortActivity.class));
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
     * Method to show intro dialog when user land on this screen for the first time.
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
                        //open preview screen
                        Intent intent = new Intent(BottomNavigationActivity.this, CapturePreviewActivity.class);
                        intent.putExtra("isMerchantable", "0");
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .show();
    }


    public void activateBottomNavigationItem(int id) {
        navigationView.setSelectedItemId(id);
    }


}
