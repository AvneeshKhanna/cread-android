package com.thetestament.cread.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.view.Menu;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;


/**
 * Activity class where user can view and edit his/her profile picture.
 */

public class UpdateProfileImageActivity extends BaseActivity {


    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.imageProfile)
    ImageView imageProfile;

    @State
    Uri galleryImgUri;
    @State
    Uri croppedImgUri;

    private Uri toSetUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_image);
        ButterKnife.bind(this);
        //To load profile image
        initProfileImage();
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

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                croppedImgUri = result.getUri();
                ProfileDataUploader profileDataUploader = new ProfileDataUploader(UpdateProfileImageActivity.this, null, 0);
                profileDataUploader.setImageUploadListener(new ProfileDataUploader.OnImageUploadListener() {
                    @Override
                    public void onUploadComplete(Boolean completed) {
                        if (completed) {
                            try {
                                toSetUri = copyCroppedImg(croppedImgUri, UpdateProfileImageActivity.this);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Picasso picasso = Picasso.with(UpdateProfileImageActivity.this);
                            picasso.invalidate(toSetUri);
                            picasso.load(toSetUri)
                                    .into(imageProfile);

                            ViewHelper.getToast(UpdateProfileImageActivity.this
                                    , "Picture saved");

                        } else {
                            ViewHelper.getToast(UpdateProfileImageActivity.this
                                    , "Error occurred");
                        }
                    }
                });
                try {
                    profileDataUploader.uploadImage(copyCroppedImg(croppedImgUri, UpdateProfileImageActivity.this).getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                try {
                    throw result.getError();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Image could not be cropped due to some error");
                }
            }
        } else if (requestCode == REQUEST_CODE_GALLERY_IMAGE) {

            if (resultCode == RESULT_OK) {
                galleryImgUri = data.getData();
                // To crop the selected image from the gallery
                CropImage.activity(galleryImgUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            } else {
                ViewHelper.getToast(this, "Image from gallery was not attached");
            }
        }
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_profilr_image, menu);
        return true;
    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back from this screen
                finish();
                return true;
            case R.id.action_edit:
                //Edit button functionality
                Intent galleryImage = new Intent(Intent.ACTION_PICK);
                galleryImage.setType("image*//*");
                startActivityForResult(galleryImage, REQUEST_CODE_GALLERY_IMAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/


    /**
     * Method to initialize/load profile picture.
     */
    private void initProfileImage() {
        Picasso.with(this)
                .load("")
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .error(R.drawable.ic_account_circle_48)
                .into(imageProfile);
    }
}
