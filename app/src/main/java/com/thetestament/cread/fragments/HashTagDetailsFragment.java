package com.thetestament.cread.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ExploreAdapter;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnServerRequestedListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.Constant;
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
import io.reactivex.disposables.CompositeDisposable;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.helpers.FeedHelper.updateFollowForAll;
import static com.thetestament.cread.helpers.ImageHelper.getImageUri;
import static com.thetestament.cread.helpers.NetworkHelper.getHashTagDetailsObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.BUNDLE_HASHTAG_NAME;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.IMAGE_TYPE_USER_CAPTURE_PIC;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_OPEN_GALLERY_FOR_CAPTURE;


public class HashTagDetailsFragment extends Fragment implements listener.OnCollaborationListener {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.noData)
    TextView noData;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    List<FeedModel> mDataList = new ArrayList<>();
    ExploreAdapter mAdapter;
    SharedPreferenceHelper mHelper;
    private Unbinder mUnbinder;
    private String mLastIndexKey;
    private boolean mRequestMoreData;
    int spanCount = 2;

    @State
    String mShortId, hashTag;

    @State
    String mEntityID, mEntityType;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getActivity());

        // get hash tag
        hashTag = getArguments().getString(BUNDLE_HASHTAG_NAME);

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

    }

    @Override
    public void onStart() {
        super.onStart();
        //Set Listener
        new FeedHelper().setOnCaptureClickListener(this);
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
                    ImageHelper.processCroppedImage(mCroppedImgUri, getActivity(), rootView, mEntityID, mEntityType);

                } else if (resultCode == UCrop.RESULT_ERROR) {
                    ViewHelper.getSnackBar(rootView, "Image could not be cropped due to some error");
                }
                break;
            case REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getBundleExtra(EXTRA_DATA);
                    //Update data
                    mDataList.get(bundle.getInt("position")).setHatsOffStatus(bundle.getBoolean("hatsOffStatus"));
                    mDataList.get(bundle.getInt("position")).setHatsOffCount(bundle.getLong("hatsOffCount"));
                    mDataList.get(bundle.getInt("position")).setFollowStatus(bundle.getBoolean("followstatus"));
                    mDataList.get(bundle.getInt("position")).setCaption(bundle.getString("caption"));
                    //update follow occurences
                    updateFollowForAll(mDataList.get(bundle.getInt("position")), mDataList);

                    if (bundle.getBoolean("deletestatus")) {
                        mDataList.remove(bundle.getInt("position"));
                    }

                    //Notify changes
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * Method to initialize swipe refresh layout.
     */
    private void initScreen() {
        //Set layout manger for recyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        //Set adapter
        mAdapter = new ExploreAdapter(mDataList, getActivity(), mHelper.getUUID(), HashTagDetailsFragment.this, Constant.ITEM_TYPES.GRID);
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data list
                mDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to nul
                mLastIndexKey = null;
                //Load data here
                loadHashTagData();
            }
        });


        //Initialize listeners
        initTabLayout();
        initListeners();
        //Load data here
        loadHashTagData();
    }

    /**
     * Method to initialize tab layout.
     */
    private void initTabLayout() {

        //initialize tabs icon tint
        tabLayout.getTabAt(0).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                //To change tab icon color from grey to white
                tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

                switch (tab.getPosition()) {


                    case 0:
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
                        recyclerView.setLayoutManager(gridLayoutManager);

                        mAdapter = new ExploreAdapter(mDataList, getActivity(), mHelper.getUUID(), HashTagDetailsFragment.this, Constant.ITEM_TYPES.GRID);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        break;

                    case 1:
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        mAdapter = new ExploreAdapter(mDataList, getActivity(), mHelper.getUUID(), HashTagDetailsFragment.this, Constant.ITEM_TYPES.LIST);
                        recyclerView.setAdapter(mAdapter);
                        initListeners();
                        break;


                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                //To change tab icon color from white color to grey
                tab.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey_custom), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void initListeners() {
        initLoadMoreListener();
        initCaptureListener();
        initFollowListener();
    }

    /**
     * Initialize load more listener.
     */
    private void initLoadMoreListener() {

        mAdapter.setOnExploreLoadMoreListener(new listener.OnExploreLoadMoreListener() {
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
                    loadMoreData();
                }
            }
        });
    }


    /**
     * Initialize follow listener.
     *
     */
    private void initFollowListener() {
        mAdapter.setOnExploreFollowListener(new listener.OnExploreFollowListener() {
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

        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(getActivity(),
                mCompositeDisposable,
                exploreData.getFollowStatus(),
                new JSONArray().put(exploreData.getUUID()),
                new listener.OnFollowRequestedListener() {
                    @Override
                    public void onFollowSuccess() {

                        // updates follow status in all occurrence of the followed user
                        updateFollowForAll(exploreData, mDataList);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFollowFailiure(String errorMsg) {

                        //set status to true if its false and vice versa
                        exploreData.setFollowStatus(!exploreData.getFollowStatus());
                        //notify changes
                        mAdapter.notifyItemChanged(itemPosition);

                        ViewHelper.getSnackBar(rootView, errorMsg);

                    }
                });
    }

    /**
     * Initialize capture listener.
     *
     */
    private void initCaptureListener() {
        mAdapter.setOnExploreCaptureClickListener(new listener.OnExploreCaptureClickListener() {
            @Override
            public void onClick(String shortId) {
                //Set entity id
                mShortId = shortId;
                //Check for Write permission
                if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //We have permission do whatever you want to do
                    ImageHelper.chooseImageFromGallery(HashTagDetailsFragment.this);
                } else {
                    //We do not own this permission
                    if (Nammu.shouldShowRequestPermissionRationale(HashTagDetailsFragment.this
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //User already refused to give us this permission or removed it
                        ViewHelper.getToast(getActivity()
                                , getString(R.string.error_msg_capture_permission_denied));
                    } else {
                        //First time asking for permission
                        Nammu.askForPermission(HashTagDetailsFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
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
            ImageHelper.chooseImageFromGallery(HashTagDetailsFragment.this);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(getActivity()
                    , getString(R.string.error_msg_capture_permission_denied));
        }
    };

    private void loadHashTagData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable,
                getHashTagDetailsObservable
                        (mHelper.getUUID(),
                                mHelper.getAuthToken(),
                                hashTag,
                                mLastIndexKey),
                getActivity(),
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        swipeRefreshLayout.setRefreshing(false);
                        //No connection Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {

                                // parse json and add data to data list
                                parseJSONData(jsonObject, false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        swipeRefreshLayout.setRefreshing(false);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));

                        } else if (mDataList.size() == 0) {
                            noData.setVisibility(View.VISIBLE);
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerView.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * load more data
     */
    private void loadMoreData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable,
                getHashTagDetailsObservable
                        (mHelper.getUUID(),
                                mHelper.getAuthToken(),
                                hashTag,
                                mLastIndexKey),
                getActivity(),
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                // add data to list
                                parseJSONData(jsonObject, true);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mDataList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

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
                });
    }


    /**
     * Method to parse JSON data returned from the server
     *
     * @param jsonObject Json object to parse
     * @param isLoadMore true if called from load more else false
     * @throws JSONException
     */
    private void parseJSONData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {

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
            if (dataObj.isNull("caption")) {
                exploreData.setCaption(null);
            } else {
                exploreData.setCaption(dataObj.getString("caption"));
            }


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
                    exploreData.setCollaboWithEntityID(collabObject.getString("entityid"));

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
                    exploreData.setCollaboWithEntityID(collabObject.getString("entityid"));
                } else {
                    exploreData.setAvailableForCollab(true);
                }
            }

            mDataList.add(exploreData);

            if (isLoadMore) {
                //Notify item changes
                mAdapter.notifyItemInserted(mDataList.size() - 1);
            }
        }
    }

    @Override
    public void collaborationOnGraphic() {

    }

    @Override
    public void collaborationOnWriting(String entityID, String entityType) {
        //Set entity id
        mEntityID = entityID;
        mEntityType = entityType;
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            ImageHelper.chooseImageFromGallery(HashTagDetailsFragment.this);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(HashTagDetailsFragment.this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(getActivity()
                        , getString(R.string.error_msg_capture_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(HashTagDetailsFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, captureWritePermission);
            }
        }
    }
}
