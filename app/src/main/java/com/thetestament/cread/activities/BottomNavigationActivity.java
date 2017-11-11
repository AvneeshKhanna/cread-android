package com.thetestament.cread.activities;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.ActionItemBadgeAdder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.database.NotificationsDBFunctions;
import com.thetestament.cread.fragments.ExploreFragment;
import com.thetestament.cread.fragments.FeedFragment;
import com.thetestament.cread.fragments.MeFragment;
import com.thetestament.cread.helpers.BottomNavigationViewHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.yalantis.ucrop.UCrop;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.startImageCropping;
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_updates:
                //Open updates screen
                // TODO uncomment

                Thread thread = new Thread(

                        new Runnable() {
                            @Override
                            public void run() {

                                NotificationsDBFunctions dbFunctions = new NotificationsDBFunctions(BottomNavigationActivity.this);
                                dbFunctions.accessNotificationsDatabase();
                                dbFunctions.setRead();

                                startActivity(new Intent(BottomNavigationActivity.this, UpdatesActivity.class));

                            }
                        }
                );

                thread.start();

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
        //if called from onClick of notifications
        if (getIntent().hasExtra("DATA")) {
            if (getIntent().getStringExtra("DATA").equals("startUpdatesFragment")) {
                //To select feed menu
                navigationView.setSelectedItemId(R.id.action_feed);
                //To open Feed Screen
                mCurrentFragment = new FeedFragment();
                //set fragment title
                mFragmentTag = TAG_FEED_FRAGMENT;
                replaceFragment(mCurrentFragment, mFragmentTag);
                //Launch updates activity
                // TODO uncomment
                /*startActivityForResult(new Intent(this, UpdatesActivity.class)
                        , REQUEST_CODE_UPDATES_ACTIVITY);*/
            }
        }
        //When app opened normally
        else {
            navigationView.setSelectedItemId(R.id.action_feed);
            //To open Feed Screen
            mCurrentFragment = new FeedFragment();
            //Set fragment tag
            mFragmentTag = TAG_FEED_FRAGMENT;
            replaceFragment(mCurrentFragment, mFragmentTag);
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
                        mCurrentFragment = new FeedFragment();
                        //set fragment tag
                        mFragmentTag = TAG_FEED_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag);
                        break;

                    case R.id.action_explore:
                        //Set title
                        setTitle("Explore");
                        mCurrentFragment = new ExploreFragment();
                        //Set fragment tag
                        mFragmentTag = TAG_EXPLORE_FRAGMENT;
                        replaceFragment(mCurrentFragment, mFragmentTag);
                        break;

                    case R.id.action_add:
                        getAddContentBottomSheetDialog();
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

    public void activateBottomNavigationItem(int id) {
        navigationView.setSelectedItemId(id);
    }


    /**
     * Method to show bottomSheet dialog with 'write a short' and 'Upload a capture' option.
     */
    private void getAddContentBottomSheetDialog() {
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
                //Open ShortActivity
                startActivity(new Intent(BottomNavigationActivity.this, ShortActivity.class));
                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();

            }
        });
        //Capture button functionality
        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRuntimePermission();
                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
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
                        , "Please grant storage permission from settings to upload your capture");
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
            Bitmap bitmap = BitmapFactory.decodeFile(getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC).getPath());
            //If resolution of image is greater than 2000x2000 then compress this image
            if (bitmap.getWidth() > 2000 && bitmap.getWidth() > 2000) {
                //Compress image
                //compressSpecific(uri, this, IMAGE_TYPE_USER_CAPTURE_PIC);
                //Open preview screen
                startActivity(new Intent(BottomNavigationActivity.this, CapturePreviewActivity.class));
            } else {
                //open preview screen
                startActivity(new Intent(BottomNavigationActivity.this, CapturePreviewActivity.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
        }
    }


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
}
