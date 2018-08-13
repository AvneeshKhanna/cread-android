package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.widgets.SquareImageView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_MEME;

/**
 * Fragment to create meme.
 */

public class MemeSixthFragment extends Fragment {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.img_meme)
    SquareImageView imgMeme;
    //endregion

    //region :Fields and constant
    Unbinder mUnbinder;

    /**
     * To maintain bitmap of meme image selection
     */
    Bitmap mBitmap;
    //endregion

    //region :Required constructor
    public MemeSixthFragment() {
    }
    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Its own option menu
        setHasOptionsMenu(true);
        //inflate this view
        return inflater.inflate(R.layout.fragment_meme_sixth
                , container
                , false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ButterKnife view binding
        mUnbinder = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Required for permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GALLERY_FOR_MEME:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    ImageHelper.startImageCroppingWithSquare(getActivity()
                            , MemeSixthFragment.this
                            , data.getData()
                            , getImageUri(Constant.IMAGE_TYPE_USER_MEME_ONE));
                } else {
                    ViewHelper.getSnackBar(rootView
                            , getString(R.string.error_img_not_attached));
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);

                    imgMeme.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Picasso.with(getActivity())
                            .load(mCroppedImgUri)
                            .error(R.drawable.image_placeholder)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(imgMeme);
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, getString(R.string.error_img_not_cropped));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_meme_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                getWritePermissionForMemeGeneration();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //endregion

    //region :Click functionality

    /**
     * Image click functionality.
     */
    @OnClick(R.id.img_meme)
    void imgOnClick() {
        //Method called
        getWritePermissionForGallery();
    }
    //endregion

    //region :Private methods


    /**
     * Method to check storage runtime permission for opening the gallery.
     */
    private void getWritePermissionForGallery() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            ImageHelper.chooseImageFromGallery(MemeSixthFragment.this
                    , Constant.REQUEST_CODE_OPEN_GALLERY_FOR_MEME);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(MemeSixthFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeSixthFragment.this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , writeForGalleryPermission);
            }
        }
    }


    /**
     * Used to handle result of askForPermission for storage.
     */
    PermissionCallback writeForGalleryPermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //We have permission do whatever you want to do
            ImageHelper.chooseImageFromGallery(MemeSixthFragment.this
                    , Constant.REQUEST_CODE_OPEN_GALLERY_FOR_MEME);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };


    /**
     * Method to check for write storage runtime permission and generate meme images.
     */
    private void getWritePermissionForMemeGeneration() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            generateMemeImage();
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(MemeSixthFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeSixthFragment.this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , writeForMemePermission);
            }
        }
    }

    /**
     * Used to handle result of askForPermission for meme image generation.
     */
    PermissionCallback writeForMemePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //We have permission do whatever you want to do
            generateMemeImage();
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };

    /**
     * Method to generate meme image and show its preview.
     */
    private void generateMemeImage() {
        //Enable drawing cache
        container.setDrawingCacheEnabled(true);
        //Obtain bitmap
        Bitmap bm = container.getDrawingCache();

        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Cread/Meme/meme_pic.jpg");
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.close();
            //Code for preview screen
            IntentHelper.openPreviewActivityFromMeme(getActivity());
        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        //Disable drawing cache
        container.setDrawingCacheEnabled(false);
    }


//endregion
}
