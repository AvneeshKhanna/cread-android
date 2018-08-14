package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.MemeUtil;
import com.thetestament.cread.widgets.SquareView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

/**
 * Fragment to create meme.
 */

public class MemeSeventhFragment extends Fragment {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.container)
    SquareView container;
    @BindView(R.id.tv)
    AppCompatTextView textView;
    //endregion

    //region :Fields and constant
    Unbinder mUnbinder;

    //endregion

    //region :Required constructor
    public MemeSeventhFragment() {
    }
    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Its own option menu
        setHasOptionsMenu(true);
        //inflate this view
        return inflater.inflate(R.layout.fragment_meme_seventh
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
                validateMemeCreation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //endregion

    //region :Click functionality

    /**
     * Click functionality for bottom textView.
     */
    @OnClick(R.id.tv)
    void onTvBottomClick() {
        MemeUtil.showMemeInputDialog(getActivity(), textView);
    }
    //endregion

    //region :Private methods

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
            if (Nammu.shouldShowRequestPermissionRationale(MemeSeventhFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeSeventhFragment.this
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


    /**
     * Method to validate meme creation and then generate meme.
     */
    private void validateMemeCreation() {
        //if text equal to hint text
        if (textView.getText().toString().equals(getString(R.string.hint_text_meme))) {
            ViewHelper.getToast(getContext(), "Text field can't be empty");
        }
        //If empty
        else if (TextUtils.isEmpty(textView.getText().toString().trim())) {
            ViewHelper.getToast(getContext(), "Text field can't be empty");
        } else {
            getWritePermissionForMemeGeneration();
        }

    }

//endregion
}
