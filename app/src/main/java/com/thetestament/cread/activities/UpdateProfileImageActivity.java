package com.thetestament.cread.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.helpers.ImageHelper.compressCroppedImg;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.startImageCroppingWithSquare;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_IMAGE_PATH;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_PROFILE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY;


/**
 * Activity class where user can view and edit his/her profile picture.
 */

public class UpdateProfileImageActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageProfile)
    ImageView imageProfile;

    @State
    Uri mGalleryImgUri;
    @State
    Uri mCroppedImgUri;

    private Uri mCompressedUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_image);
        ButterKnife.bind(this);
        //Get data from intent
        String imageURL = getIntent().getStringExtra(EXTRA_USER_IMAGE_PATH);
        ActivityCompat.postponeEnterTransition(this);
        //To load profile image
        loadProfileImage(imageURL);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GALLERY:
                if (resultCode == RESULT_OK) {
                    //Get uri of selected image
                    mGalleryImgUri = data.getData();
                    // To crop the selected image
                    startImageCroppingWithSquare(this, mGalleryImgUri, getImageUri(IMAGE_TYPE_USER_PROFILE_PIC));
                } else {
                    ViewHelper.getSnackBar(rootView, "Image from gallery was not attached");
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    mCroppedImgUri = UCrop.getOutput(data);
                    //Method called
                    processProfilePicture(mCroppedImgUri);
                    /*try {
                        mCompressedUri = compressCroppedImg(mCroppedImgUri, this, IMAGE_TYPE_USER_PROFILE_PIC);
                        //save user profile
                        saveProfilePicture(new File(mCompressedUri.getPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                    }*/
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_profile_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back from this screen
                supportFinishAfterTransition();

                return true;
            case R.id.action_edit:
                //Launch gallery
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_OPEN_GALLERY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method to initialize/load profile picture.
     */
    private void loadProfileImage(String imageURL) {
        Picasso.with(this)
                .load(imageURL)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.ic_account_circle_100)
                .into(imageProfile, new Callback() {
                    @Override
                    public void onSuccess() {
                        ActivityCompat.startPostponedEnterTransition(UpdateProfileImageActivity.this);

                    }

                    @Override
                    public void onError() {
                        ActivityCompat.startPostponedEnterTransition(UpdateProfileImageActivity.this);
                    }
                });
    }

    /**
     * Save profile picture.
     *
     * @param file File to be saved.
     */
    private void saveProfilePicture(File file) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Uploading your profile picture")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        SharedPreferenceHelper helper = new SharedPreferenceHelper(this);
        AndroidNetworking.upload(BuildConfig.URL + "/user-profile/update-profile-picture")
                .addMultipartFile("display-pic", file)
                .addMultipartParameter("uuid", helper.getUUID())
                .addMultipartParameter("authkey", helper.getAuthToken())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
                            //if token status is not invalid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                ViewHelper.getSnackBar(rootView,
                                        getString(R.string.error_msg_invalid_token));
                            } else {
                                JSONObject dataObject = response.getJSONObject("data");
                                if (dataObject.getString("status").equals("done")) {
                                    //Update profile picture
                                    loadProfileImage(mCompressedUri.toString());
                                    Intent returnIntent = getIntent();
                                    Bundle returnData = new Bundle();
                                    returnData.putString(EXTRA_USER_IMAGE_PATH, dataObject.getString("profilepicurl"));
                                    returnIntent.putExtras(returnData);
                                    setResult(RESULT_OK, returnIntent);
                                } else {
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        dialog.dismiss();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Method to perform required operation on cropped image and upload it on server.
     *
     * @param croppedImgUri Uri of cropped image.
     */
    private void processProfilePicture(Uri croppedImgUri) {
        //Decode image file
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(croppedImgUri.getPath()).getAbsolutePath(), options);

        //If resolution of image is greater than 750x750 then compress this image
        if (options.outWidth >= 1250 && options.outHeight >= 1250) {
            try {
                mCompressedUri = compressCroppedImg(mCroppedImgUri, this, IMAGE_TYPE_USER_PROFILE_PIC);
                //save user profile
                saveProfilePicture(new File(mCompressedUri.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
                ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
            }
        } else {
            //save user profile
            saveProfilePicture(new File(mCroppedImgUri.getPath()));
        }
    }
}
