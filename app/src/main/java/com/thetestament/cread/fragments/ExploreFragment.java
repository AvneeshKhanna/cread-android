package com.thetestament.cread.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.FindFBFriendsActivity;
import com.thetestament.cread.adapters.ExploreAdapter;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;


public class ExploreFragment extends Fragment {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    List<FeedModel> mExploreDataList = new ArrayList<>();
    ExploreAdapter mAdapter;
    SharedPreferenceHelper mHelper;
    private Unbinder mUnbinder;
    private String mLastIndexKey;
    private boolean mRequestMoreData;

    @State
    String mShortId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getActivity());
        // Its own option menu
        setHasOptionsMenu(true);
        //inflate this view
        return inflater
                .inflate(R.layout.fragment_explore
                        , container
                        , false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ButterKnife view binding
        mUnbinder = ButterKnife.bind(this, view);
        initScreen();

        //Explore screen open for first time
        if (mHelper.isExploreIntroFirstTime()) {
            getExploreIntroDialog();
        }
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
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE:
                if (resultCode == RESULT_OK) {
                    // To crop the selected image
                    ImageHelper.startImageCropping(getActivity(), this, data.getData(), getImageUri(IMAGE_TYPE_USER_CAPTURE_PIC));
                } else {
                    ViewHelper.getSnackBar(rootView, "Image from gallery was not attached");
                }
                break;
            //For more information please visit "https://github.com/Yalantis/uCrop"
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    //Get cropped image Uri
                    Uri mCroppedImgUri = UCrop.getOutput(data);
                    ImageHelper.processCroppedImage(mCroppedImgUri, getActivity(), rootView, mShortId);

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_fragment_explore, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_findfbFriends:
                startActivity(new Intent(getActivity(), FindFBFriendsActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method to initialize swipe refresh layout.
     */
    private void initScreen() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new ExploreAdapter(mExploreDataList, getActivity(), mHelper.getUUID());
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data list
                mExploreDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to nul
                mLastIndexKey = null;
                //Load data here
                loadExploreData();
            }
        });

        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initFollowListener(mAdapter);
        initCaptureListener(mAdapter);
        //Load data here
        loadExploreData();
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter ExploreAdapter reference.
     */
    private void initLoadMoreListener(ExploreAdapter adapter) {

        adapter.setOnExploreLoadMoreListener(new listener.OnExploreLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //If next set of data available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mExploreDataList.add(null);
                                               mAdapter.notifyItemInserted(mExploreDataList.size() - 1);
                                           }
                                       }
                    );

                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadExploreData() {
        // if user device is connected to net
        if (getNetConnectionStatus(getActivity())) {
            //Get data from server
            getFeedData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving explore data
     */
    private void getFeedData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/explore-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //ExploreArray list
                                JSONArray exploreArray = mainData.getJSONArray("feed");
                                for (int i = 0; i < exploreArray.length(); i++) {
                                    JSONObject dataObj = exploreArray.getJSONObject(i);
                                    String type = dataObj.getString("type");

                                    FeedModel exploreData = new FeedModel();
                                    exploreData.setEntityID(dataObj.getString("entityid"));
                                    exploreData.setContentType(dataObj.getString("type"));
                                    exploreData.setUUID(dataObj.getString("uuid"));
                                    exploreData.setCreatorImage(dataObj.getString("profilepicurl"));
                                    exploreData.setCreatorName(dataObj.getString("creatorname"));
                                    exploreData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    exploreData.setFollowStatus(dataObj.getBoolean("followstatus"));
                                    exploreData.setMerchantable(dataObj.getBoolean("merchantable"));
                                    exploreData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    exploreData.setCommentCount(dataObj.getLong("commentcount"));
                                    exploreData.setContentImage(dataObj.getString("entityurl"));
                                    exploreData.setCollabCount(dataObj.getLong("collabcount"));


                                    if (type.equals(CONTENT_TYPE_CAPTURE)) {

                                        //Retrieve "CAPTURE_ID" if type is capture
                                        exploreData.setCaptureID(dataObj.getString("captureid"));
                                        // if capture
                                        // then if key cpshort exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("cpshort")) {
                                            JSONObject collabObject = dataObj.getJSONObject("cpshort");

                                            exploreData.setAvailableForCollab(false);
                                            // set collaborator details
                                            exploreData.setCollabWithUUID(collabObject.getString("uuid"));
                                            exploreData.setCollabWithName(collabObject.getString("name"));

                                        } else {
                                            exploreData.setAvailableForCollab(true);
                                        }

                                    } else if (type.equals(CONTENT_TYPE_SHORT)) {

                                        //Retrieve "SHORT_ID" if type is short
                                        exploreData.setShortID(dataObj.getString("shoid"));

                                        // if short
                                        // then if key shcapture exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("shcapture")) {

                                            JSONObject collabObject = dataObj.getJSONObject("shcapture");

                                            exploreData.setAvailableForCollab(false);
                                            // set collaborator details
                                            exploreData.setCollabWithUUID(collabObject.getString("uuid"));
                                            exploreData.setCollabWithName(collabObject.getString("name"));
                                        } else {
                                            exploreData.setAvailableForCollab(true);
                                        }
                                    }
                                    mExploreDataList.add(exploreData);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerView.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }

    /**
     * Method to retrieve to next set of data from server.
     */
    private void loadMoreData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/explore-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //ExploreArray list
                                JSONArray exploreArray = mainData.getJSONArray("feed");
                                for (int i = 0; i < exploreArray.length(); i++) {
                                    JSONObject dataObj = exploreArray.getJSONObject(i);
                                    String type = dataObj.getString("type");

                                    FeedModel exploreData = new FeedModel();
                                    exploreData.setEntityID(dataObj.getString("entityid"));
                                    exploreData.setContentType(dataObj.getString("type"));
                                    exploreData.setUUID(dataObj.getString("uuid"));
                                    exploreData.setCreatorImage(dataObj.getString("profilepicurl"));
                                    exploreData.setCreatorName(dataObj.getString("creatorname"));
                                    exploreData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    exploreData.setFollowStatus(dataObj.getBoolean("followstatus"));
                                    exploreData.setMerchantable(dataObj.getBoolean("merchantable"));
                                    exploreData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    exploreData.setCommentCount(dataObj.getLong("commentcount"));
                                    exploreData.setContentImage(dataObj.getString("entityurl"));
                                    exploreData.setCollabCount(dataObj.getLong("collabcount"));


                                    if (type.equals(CONTENT_TYPE_CAPTURE)) {

                                        //Retrieve "CAPTURE_ID" if type is capture
                                        exploreData.setCaptureID(dataObj.getString("captureid"));
                                        // if capture
                                        // then if key cpshort exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("cpshort")) {
                                            JSONObject collabObject = dataObj.getJSONObject("cpshort");

                                            exploreData.setAvailableForCollab(false);
                                            // set collaborator details
                                            exploreData.setCollabWithUUID(collabObject.getString("uuid"));
                                            exploreData.setCollabWithName(collabObject.getString("name"));

                                        } else {
                                            exploreData.setAvailableForCollab(true);
                                        }

                                    } else if (type.equals(CONTENT_TYPE_SHORT)) {

                                        //Retrieve "SHORT_ID" if type is short
                                        exploreData.setShortID(dataObj.getString("shoid"));

                                        // if short
                                        // then if key shcapture exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("shcapture")) {

                                            JSONObject collabObject = dataObj.getJSONObject("shcapture");

                                            exploreData.setAvailableForCollab(false);
                                            // set collaborator details
                                            exploreData.setCollabWithUUID(collabObject.getString("uuid"));
                                            exploreData.setCollabWithName(collabObject.getString("name"));
                                        } else {
                                            exploreData.setAvailableForCollab(true);
                                        }
                                    }


                                    mExploreDataList.add(exploreData);

                                    //Notify item insertion
                                    mAdapter.notifyItemInserted(mExploreDataList.size() - 1);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            //Notify changes
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }

    /**
     * Initialize follow listener.
     *
     * @param adapter ExploreAdapter reference.
     */
    private void initFollowListener(ExploreAdapter adapter) {
        adapter.setOnExploreFollowListener(new listener.OnExploreFollowListener() {
            @Override
            public void onFollowClick(FeedModel exploreData, int itemPosition) {
                updateFollowStatus(exploreData, itemPosition);
            }
        });
    }

    /**
     * Method to update follow status.
     *
     * @param exploreData  Model of current item
     * @param itemPosition Position of current item i.e integer
     */
    private void updateFollowStatus(final FeedModel exploreData, final int itemPosition) {
        final JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(exploreData.getUUID());

            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("register", exploreData.getFollowStatus());
            jsonObject.put("followees", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        AndroidNetworking.post(BuildConfig.URL + "/follow/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Token status is not valid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                //set status to true if its false and vice versa
                                exploreData.setFollowStatus(!exploreData.getFollowStatus());
                                //notify changes
                                mAdapter.notifyItemChanged(itemPosition);
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {

                                    for (FeedModel f : mExploreDataList) {
                                        if (f.getUUID().equals(exploreData.getUUID())) {
                                            f.setFollowStatus(exploreData.getFollowStatus());
                                        }
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            //set status to true if its false and vice versa
                            exploreData.setFollowStatus(!exploreData.getFollowStatus());
                            //notify changes
                            mAdapter.notifyItemChanged(itemPosition);
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //set status to true if its false and vice versa
                        exploreData.setFollowStatus(!exploreData.getFollowStatus());
                        //notify changes
                        mAdapter.notifyItemChanged(itemPosition);
                        anError.printStackTrace();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

    /**
     * Initialize capture listener.
     *
     * @param exploreAdapter ExploreAdapter reference
     */
    private void initCaptureListener(ExploreAdapter exploreAdapter) {
        exploreAdapter.setOnExploreCaptureClickListener(new listener.OnExploreCaptureClickListener() {
            @Override
            public void onClick(String shortId) {
                //Set entity id
                mShortId = shortId;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    ImageHelper.chooseImageFromGallery(ExploreFragment.this);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(ExploreFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_capture_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(ExploreFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
                    }
                }
            }
        });
    }

    /**
     * Used to handle result of askForPermission for capture.
     */
    PermissionCallback captureWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            //Select image from gallery
            ImageHelper.chooseImageFromGallery(ExploreFragment.this);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_capture_permission_denied));
        }
    };


    /**
     * Method to show intro dialog when user land on this screen for the first time.
     */
    private void getExploreIntroDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_generic, false)
                .positiveText(getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        //update status
                        mHelper.updateExploreIntroStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);

        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.img_explore_intro));
        //Set title text
        textTitle.setText("You complete art.");
        //Set description text
        textDesc.setText("And the reverse is true as well. Appreciating art is perhaps the greatest of all arts; we hope you find yourself mastering it here. ");
    }

}
