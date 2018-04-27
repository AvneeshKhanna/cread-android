package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.google.firebase.crash.FirebaseCrash;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CollaborationDetailsAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnCollaborationItemClickedListener;
import com.thetestament.cread.models.CollaborationDetailsModel;
import com.thetestament.cread.models.FeedModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.helpers.FeedHelper.parseEntitySpecificJSON;
import static com.thetestament.cread.helpers.NetworkHelper.getCollaborationDetailsObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getEntitySpecificObservable;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;

/**
 * To show the details of people who collaborated with other user contents.
 */
public class CollaborationDetailsActivity extends BaseActivity {

    //region :Butter knife view binding
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    MultiSnapRecyclerView recyclerView;
    @BindView(R.id.viewProgress)
    View viewProgress;
    //endregion

    //region :Fields and constants
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    List<CollaborationDetailsModel> mDataList = new ArrayList<>();
    CollaborationDetailsAdapter mAdapter;
    private AppCompatActivity mContext;

    FeedModel entitySpecificData;

    @State
    String mEntityID, mLastIndexKey = null;
    @State
    String mEntityType;
    @State
    boolean mRequestMoreData;


    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaboration_details);
        //Bind view
        ButterKnife.bind(this);
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
        mContext = this;
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
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
//endregion

    //region :Private methods

    /**
     * Method to initialize views and retrieve data from intent.
     */
    private void initView() {
        //Retrieve data from intent
        Bundle bundle = getIntent().getBundleExtra(EXTRA_DATA);
        mEntityID = bundle.getString(EXTRA_ENTITY_ID);
        mEntityType = bundle.getString(EXTRA_ENTITY_TYPE);

        //Set layout manger for recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext
                , LinearLayoutManager.HORIZONTAL
                , false);
        recyclerView.setLayoutManager(layoutManager);

        //Set adapter
        mAdapter = new CollaborationDetailsAdapter(mDataList, mContext);
        recyclerView.setAdapter(mAdapter);
        //initialize swipeRefreshLayout
        initScreen();
    }

    /**
     * Method to initialize swipe to refresh view.
     */
    private void initScreen() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mDataList.clear();
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index value to null
                mLastIndexKey = null;
                loadCollaborationData();
            }
        });
        // Initialize click listener
        initCollaborationItemClickListener();
        //Initialize listener
        initLoadMoreListener(mAdapter);
        //Load more data
        loadCollaborationData();
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadCollaborationData() {
        // if user device is connected to net
        if (getNetConnectionStatus(mContext)) {
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getCollaborationData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving collaboration details data.
     */
    private void getCollaborationData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getCollaborationDetailsObservableFromServer(BuildConfig.URL + "/entity-manage/load-collab-details"
                , mEntityID
                , mEntityType
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
                                //Collaboration details  list
                                JSONArray detailsArray = mainData.getJSONArray("items");
                                for (int i = 0; i < detailsArray.length(); i++) {
                                    JSONObject dataObj = detailsArray.getJSONObject(i);
                                    CollaborationDetailsModel model = new CollaborationDetailsModel();
                                    model.setUserName(dataObj.getString("name"));
                                    model.setUuid(dataObj.getString("uuid"));
                                    model.setProfilePic(dataObj.getString("profilepicurl"));
                                    model.setEntityUrl(dataObj.getString("entityurl"));
                                    model.setEntityID(dataObj.getString("entityid"));

                                    //if image width and image height is null
                                    if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                                        model.setImgWidth(1);
                                        model.setImgHeight(1);
                                    } else {
                                        model.setImgWidth(dataObj.getInt("img_width"));
                                        model.setImgHeight(dataObj.getInt("img_height"));
                                    }

                                    mDataList.add(model);
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
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                        //Error occurred
                        else if (mDataList.size() == 0) {
                            ViewHelper.getSnackBar(rootView, "No data");
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
                            recyclerView.setLayoutAnimation(animation);
                            //Notify changes
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter CollaborationDetails reference.
     */
    private void initLoadMoreListener(CollaborationDetailsAdapter adapter) {

        //Load more data listener
        adapter.setLoadMoreListener(new listener.OnCollaborationDetailsLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
                if (mRequestMoreData) {
                    //Show progress view
                    viewProgress.setVisibility(View.VISIBLE);
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    /**
     * Initialize collaboration item click listener.
     */
    private void initCollaborationItemClickListener() {

        mAdapter.setCollaborationItemClickedListener(new OnCollaborationItemClickedListener() {
            @Override
            public void onItemClicked(String entityID) {
                // open feed description activity
                getFeedDetails(entityID);
            }
        });
    }

    /**
     * Method to retrieve next set of data from server.
     */
    private void loadMoreData() {

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getCollaborationDetailsObservableFromServer(BuildConfig.URL + "/entity-manage/load-collab-details"
                , mEntityID
                , mEntityType
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
                        //Hide progress view
                        viewProgress.setVisibility(View.GONE);
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Collaboration details  list
                                JSONArray hatsOffArray = mainData.getJSONArray("items");
                                for (int i = 0; i < hatsOffArray.length(); i++) {
                                    JSONObject dataObj = hatsOffArray.getJSONObject(i);
                                    CollaborationDetailsModel model = new CollaborationDetailsModel();
                                    model.setUserName(dataObj.getString("name"));
                                    model.setUuid(dataObj.getString("uuid"));
                                    model.setProfilePic(dataObj.getString("profilepicurl"));
                                    model.setEntityUrl(dataObj.getString("entityurl"));
                                    model.setEntityID(dataObj.getString("entityid"));
                                    //if image width and image height is null
                                    if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                                        model.setImgWidth(1);
                                        model.setImgHeight(1);
                                    } else {
                                        model.setImgWidth(dataObj.getInt("img_width"));
                                        model.setImgHeight(dataObj.getInt("img_height"));
                                    }
                                    mDataList.add(model);
                                    //Notify changes
                                    mAdapter.notifyItemInserted(mDataList.size() - 1);
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
                        //Hide progress view
                        viewProgress.setVisibility(View.GONE);
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
     * RxJava2 implementation for retrieving feed details
     *
     * @param entityID
     */
    private void getFeedDetails(final String entityID) {
        viewProgress.setVisibility(View.VISIBLE);

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(this);

        requestServer(mCompositeDisposable,
                getEntitySpecificObservable(spHelper.getUUID(),
                        spHelper.getAuthToken(),
                        entityID),
                this,
                new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        viewProgress.setVisibility(View.GONE);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        viewProgress.setVisibility(View.GONE);

                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                entitySpecificData = parseEntitySpecificJSON(jsonObject, entityID);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        viewProgress.setVisibility(View.GONE);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

                        GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = false;

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));

                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, entitySpecificData);
                            bundle.putInt("position", -1);

                            Intent intent = new Intent(CollaborationDetailsActivity.this, FeedDescriptionActivity.class);
                            intent.putExtra(EXTRA_DATA, bundle);
                            startActivity(intent);
                        }
                    }
                });
    }

    //endregion
}
