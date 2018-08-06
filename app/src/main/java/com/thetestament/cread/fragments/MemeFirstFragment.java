package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.widgets.SquareImageView;

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

/**
 * Fragment ot create meme.
 */

public class MemeFirstFragment extends Fragment {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.et_top)
    AppCompatEditText etTop;
    @BindView(R.id.img_meme)
    SquareImageView imgMeme;
    @BindView(R.id.et_bottom)
    AppCompatEditText etBottom;
    //endregion

    //region :Fields and constant
    SharedPreferenceHelper mHelper;
    Unbinder mUnbinder;
    CompositeDisposable mCompositeDisposable;
    //endregion

    //region :Required constructor
    public MemeFirstFragment() {
    }
    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getActivity());
        // Its own option menu
        setHasOptionsMenu(true);
        //inflate this view
        return inflater.inflate(R.layout.fragment_meme_first
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
                generateMemeImage();
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
    }
    //endregion

    //region :Private methods

    /**
     * Method to generate meme image and show its preview.
     */
    private void generateMemeImage() {
        //Hide edit text cursor
        etTop.setCursorVisible(false);
        etBottom.setCursorVisible(false);

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
        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        //Disable drawing cache
        container.setDrawingCacheEnabled(false);
    }
    //endregion
}
