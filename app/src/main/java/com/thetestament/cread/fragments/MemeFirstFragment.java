package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.MemeImageAdapter;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.MemeImageModel;
import com.thetestament.cread.networkmanager.MemeNetworkManager;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.widgets.SquareImageView;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.ImageHelper.startImageCroppingWithSquare;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_MEME;

/**
 * Fragment to create meme.
 */

public class MemeFirstFragment extends Fragment {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.tv_top)
    AppCompatTextView tvTop;
    @BindView(R.id.img_meme)
    SquareImageView imgMeme;
    @BindView(R.id.tv_bottom)
    AppCompatTextView tvBottom;

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

    List<MemeImageModel> mDataList = new ArrayList<>();
    MemeImageAdapter mAdapter;

    /**
     * Flag to store index key for next set of data.
     */
    @State
    String mLastIndexKey;

    /**
     * Flag to maintain whether next set of data is preset or not.
     */
    @State
    boolean mRequestMoreData;
    //endregion

    //region :Required constructor
    public MemeFirstFragment() {
    }
    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        //Method called
        initViews();
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
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GALLERY_FOR_MEME:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    startImageCroppingWithSquare(getActivity()
                            , MemeFirstFragment.this
                            , data.getData()
                            , getImageUri(Constant.IMAGE_TYPE_USER_SHARE_MEME));
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

                    Picasso.with(getActivity())
                            .load(mCroppedImgUri)
                            .into(imgMeme);

                    /*//Check for image manipulation
                    if (AspectRatioUtils.getSquareImageManipulation(width, height)) {
                        //Create square image with blurred background
                        performSquareImageManipulation(mCroppedImgUri);
                    } else {
                        //Method called
                        processCroppedImage(mCroppedImgUri);
                    }*/
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
        //Update menu icon
        menu.findItem(R.id.action_meme_layout).setIcon(R.drawable.ic_meme_layout_1);
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
        //Method called
        initMemeBottomSheetView();
    }


    /**
     * Initialize Meme bottom sheet view.
     **/
    private void initMemeBottomSheetView() {
        //initialize bottom sheet here
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setPeekHeight(0);

        //Hide header item to list
        MemeImageModel model = new MemeImageModel();
        model.setType("header");
        mDataList.add(model);

        //Set layout manager
        memeImageRecyclerView.setHasFixedSize(true);
        memeImageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()
                , LinearLayoutManager.HORIZONTAL, false));
        //Set meme adapter
        mAdapter = new MemeImageAdapter(mDataList, getActivity());
        memeImageRecyclerView.setAdapter(mAdapter);

        //Listener
        initMemeLoadMoreListener();
        initMemeClickListener();

    }

    /**
     * Method to initialize meme image load more listener.
     */
    private void initMemeLoadMoreListener() {
        mAdapter.setLoadMoreListener(new listener.OnMemeImageLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //If next set of data available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mDataList.add(null);
                                               mAdapter.notifyItemInserted(mDataList.size() - 1);
                                           }
                                       }
                    );
                    //Load new set of data
                    //fixme load data here
                }
            }
        });
    }

    /**
     * Method to initialize meme image click listener.
     */
    private void initMemeClickListener() {
        mAdapter.setListener(new listener.OnMemeClickListener() {
            @Override
            public void onImageSelected(MemeImageModel data, int itemPosition, int viewType) {
                if (viewType == MemeImageAdapter.VIEW_TYPE_HEADER) {
                    //code to launch gallery
                    getWritePermissionForGallery();
                } else {
                    //code to use image library
                }
            }
        });
    }


    /**
     * Method to check storage runtime permission for opening the gallery.
     */
    private void getWritePermissionForGallery() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            ImageHelper.chooseImageFromGallery(MemeFirstFragment.this
                    , Constant.REQUEST_CODE_OPEN_GALLERY_FOR_MEME);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(MemeFirstFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeFirstFragment.this
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
            ImageHelper.chooseImageFromGallery(MemeFirstFragment.this
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
            if (Nammu.shouldShowRequestPermissionRationale(MemeFirstFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeFirstFragment.this
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
            //fixme code to launch next screen
        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        //Disable drawing cache
        container.setDrawingCacheEnabled(false);
    }


    /**
     * Method to load meme image data from server
     */
    private void loadMemeImageData() {
        MemeNetworkManager.getMemeImageData(getActivity()
                , mCompositeDisposable
                , mLastIndexKey
                , new MemeNetworkManager.OnMemeImageLoadListener() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        //fix here
                        try {
                            JSONObject mainData = jsonObject.getJSONObject("data");
                            //List
                            JSONArray jsonArray = mainData.getJSONArray("items");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject dataObj = jsonArray.getJSONObject(i);
                                MemeImageModel model = new MemeImageModel();
                                //Set property
                                model.setEntityID(dataObj.getString("entityid"));
                                model.setEntityUrl(dataObj.getString("entityurl"));
                                model.setEntityUrl("item");
                                mDataList.add(model);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        //Show snack bar
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }


    //endregion
}
