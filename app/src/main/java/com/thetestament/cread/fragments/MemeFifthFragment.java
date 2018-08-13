package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.squareup.picasso.Target;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.MemeActivity;
import com.thetestament.cread.adapters.MemeImageAdapter;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.MemeImageModel;
import com.thetestament.cread.networkmanager.MemeNetworkManager;
import com.thetestament.cread.utils.Constant;
import com.thetestament.cread.utils.MemeUtil;
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
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_MEME_ONE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_MEME;

/**
 * Fragment to create meme.
 */

public class MemeFifthFragment extends Fragment {

    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.img_meme)
    AppCompatImageView imgMeme;
    @BindView(R.id.tv_right)
    AppCompatTextView tvRight;

    @BindView(R.id.meme_bottom_sheet_view)
    NestedScrollView bottomSheetView;
    @BindView(R.id.recycler_view)
    RecyclerView memeImageRecyclerView;
    //endregion

    //region :Fields and constant
    SharedPreferenceHelper mHelper;
    Unbinder mUnbinder;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
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

    /**
     * To maintain bitmap of meme image selection
     */
    Bitmap mBitmap;
    //endregion

    //region :Required constructor
    public MemeFifthFragment() {
    }
    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Its own option menu
        setHasOptionsMenu(true);
        //inflate this view
        return inflater.inflate(R.layout.fragment_meme_fifth
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
        mCompositeDisposable.dispose();
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
                    ImageHelper.startImageCroppingWith918(getActivity()
                            , MemeFifthFragment.this
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

                    imgMeme.setScaleType(ImageView.ScaleType.FIT_XY);
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
        toggleBottomSheet();
    }

    /**
     * RootView click functionality to hide bottomSheetView.
     */
    @OnClick(R.id.root_view)
    void rootViewOnClick() {
        //bottomSheet expanded
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            //Hide bottomSheetView
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //Show view
            ((MemeActivity) getActivity()).toggleRecyclerViewVisibility(View.VISIBLE);
        }
    }

    /**
     * Click functionality to hide bottom sheet.
     */
    @OnClick(R.id.btn_close)
    void onCloseBtnClick() {
        //Method called
        toggleBottomSheet();
    }

    /**
     * Click functionality for top textView.
     */
    @OnClick(R.id.tv_right)
    void onTvTopClick() {
        MemeUtil.showMemeInputDialog(getActivity(),tvRight);

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

        //Method called
        loadMemeImageData(false);
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
                    loadMemeImageData(true);
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
                    //Hide bottomSheet
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    //Show view
                    ((MemeActivity) getActivity()).toggleRecyclerViewVisibility(View.VISIBLE);
                    //code to launch gallery
                    getWritePermissionForGallery();
                } else {
                    loadMemeBitmapForCropping(data);
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
            ImageHelper.chooseImageFromGallery(MemeFifthFragment.this
                    , Constant.REQUEST_CODE_OPEN_GALLERY_FOR_MEME);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(MemeFifthFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeFifthFragment.this
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
            ImageHelper.chooseImageFromGallery(MemeFifthFragment.this
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
            if (Nammu.shouldShowRequestPermissionRationale(MemeFifthFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeFifthFragment.this
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
     * Method to load meme image data from server
     *
     * @param isLoadMore True if user is loading for next set of data false otherwise.
     */
    private void loadMemeImageData(final boolean isLoadMore) {
        MemeNetworkManager.getMemeImageData(getActivity()
                , mCompositeDisposable
                , mLastIndexKey
                , new MemeNetworkManager.OnMemeImageLoadListener() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        //called from load more
                        if (isLoadMore) {
                            //Remove loading item
                            mDataList.remove(mDataList.size() - 1);
                            mAdapter.notifyItemRemoved(mDataList.size());
                        }

                        try {
                            JSONObject mainData = jsonObject.getJSONObject("data");
                            mRequestMoreData = mainData.getBoolean("requestmore");
                            mLastIndexKey = mainData.getString("lastindexkey");
                            //List
                            JSONArray jsonArray = mainData.getJSONArray("items");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                MemeImageModel model = new MemeImageModel();
                                //Set property
                                model.setImageUrl(jsonArray.getString(i));
                                model.setType("item");
                                mDataList.add(model);

                                //Called from load more
                                if (isLoadMore) {
                                    //Notify item insertion
                                    mAdapter.notifyItemInserted(mDataList.size() - 1);
                                }
                            }

                            //called from load more
                            if (isLoadMore) {
                                //Notify changes
                                mAdapter.setLoaded();
                            } else {
                                //Notify item insertion
                                //Notify changes
                                mAdapter.setLoaded();
                                mAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        //Show snack bar
                        ViewHelper.getSnackBar(rootView, errorMsg);
                        //called from load more
                        if (isLoadMore) {
                            //Remove loading item
                            mDataList.remove(mDataList.size() - 1);
                            mAdapter.notifyItemRemoved(mDataList.size());
                        }
                    }
                });
    }


    /**
     * Method to toggle bottomSheet view.
     */
    private void toggleBottomSheet() {
        //If bottomSheet hidden
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            //Show bottomSheetView
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            //Hide view
            ((MemeActivity) getActivity()).toggleRecyclerViewVisibility(View.GONE);
        }
        //bottomSheet expanded
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            //Hide bottomSheetView
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //Show view
            ((MemeActivity) getActivity()).toggleRecyclerViewVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to load bitmap image to be shared
     */
    private void loadMemeBitmapForCropping(final MemeImageModel data) {
        Picasso.with(getActivity()).load(data.getImageUrl()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBitmap = bitmap;
                //Method called
                getWritePermissionForImageSelection();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ViewHelper.getToast(getActivity(), getActivity().getString(R.string.error_msg_no_image));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });

    }


    /**
     * Method to check storage runtime permission for meme image selection.
     */
    private void getWritePermissionForImageSelection() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            saveBmpFileToDevice(mBitmap);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(MemeFifthFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(MemeFifthFragment.this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , writeForImageSelectionPermission);
            }
        }
    }


    /**
     * Used to handle result of askForPermission for storage.
     */
    PermissionCallback writeForImageSelectionPermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //We have permission do whatever you want to do
            saveBmpFileToDevice(mBitmap);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };


    /**
     * Method to save bitmap to device storage.
     *
     * @param bitmap Bitmap to be saved.
     */
    private void saveBmpFileToDevice(Bitmap bitmap) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Cread/Meme/meme_pic_one.jpg");
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.close();
            //Code to launch image cropper
            ImageHelper.startImageCroppingWith918(getActivity()
                    , MemeFifthFragment.this
                    , ImageHelper.getImageUri(IMAGE_TYPE_USER_MEME_ONE)
                    , ImageHelper.getImageUri(IMAGE_TYPE_USER_MEME_ONE));
        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

    }



    //endregion

}
