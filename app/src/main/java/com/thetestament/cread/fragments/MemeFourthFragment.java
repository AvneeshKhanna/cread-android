package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.disposables.CompositeDisposable;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

/**
 * Created by gaurav on 06/08/18.
 */

public class MemeFourthFragment extends Fragment {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.et_top)
    AppCompatEditText etTop;
    @BindView(R.id.img_meme)
    AppCompatImageView imgMeme;
    @BindView(R.id.img_meme_two)
    AppCompatImageView imgMemeTwo;

    @BindView(R.id.meme_bottom_sheet_view)
    NestedScrollView bottomSheetView;
    @BindView(R.id.recycler_view)
    RecyclerView memeImageRecyclerView;
    //endregion

    //region :Fields and constant
    SharedPreferenceHelper mHelper;
    Unbinder mUnbinder;
    CompositeDisposable mCompositeDisposable;
    BottomSheetBehavior bottomSheetBehavior;
    //endregion

    public MemeFourthFragment() {
    }


    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getActivity());
        // Its own option menu
        setHasOptionsMenu(true);
        //inflate this view
        return inflater
                .inflate(R.layout.fragment_meme_fourth
                        , container
                        , false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ButterKnife view binding
        mUnbinder = ButterKnife.bind(this, view);
        //Method called
        initViews();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        //mCompositeDisposable.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Required for permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //Update menu icon
        menu.findItem(R.id.action_meme_layout).setIcon(R.drawable.ic_meme_layout_4);
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
                //fixme code for next screen
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
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Click functionality to hide bottom sheet.
     */
    @OnClick(R.id.btn_close)
    void onCloseBtnClick() {
        //Hide bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    //endregion

    //region :Private methods

    /**
     * Initialize views for this screen.
     */
    private void initViews() {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getActivity());

        //initialize bottom sheet here
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setPeekHeight(0);
    }


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
            if (Nammu.shouldShowRequestPermissionRationale(MemeFourthFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeFourthFragment.this
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
        //Hide edit text cursor
        etTop.setCursorVisible(false);

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
            //fixme code to launch next screen
        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        //Disable drawing cache
        container.setDrawingCacheEnabled(false);
    }

    //endregion

}
