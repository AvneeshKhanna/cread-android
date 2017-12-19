package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.takusemba.multisnaprecyclerview.MultiSnapRecyclerView;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.CollaborationDetailsAdapter;
import com.thetestament.cread.helpers.NetworkHelper;
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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getCollaborationDetailsObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;

/**
 * To show the details of people who collaborated with other user contents.
 */
public class CollaborationDetailsActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    MultiSnapRecyclerView recyclerView;
    @BindView(R.id.viewProgress)
    View viewProgress;


    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    List<CollaborationDetailsModel> mDataList = new ArrayList<>();
    CollaborationDetailsAdapter mAdapter;

    @State
    String mEntityID, mLastIndexKey = null;
    @State
    String mEntityType;
    @State
    boolean mRequestMoreData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaboration_details);
        //Bind view
        ButterKnife.bind(this);
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
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

    /**
     * Method to initialize views and retrieve data from intent.
     */
    private void initView() {
        //Retrieve data from intent
        Bundle bundle = getIntent().getBundleExtra(EXTRA_DATA);
        mEntityID = bundle.getString(EXTRA_ENTITY_ID);
        mEntityType = bundle.getString(EXTRA_ENTITY_TYPE);

        //Set layout manger for recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this
                , LinearLayoutManager.HORIZONTAL
                , false);
        recyclerView.setLayoutManager(layoutManager);

        //Set adapter
        mAdapter = new CollaborationDetailsAdapter(mDataList, this);
        recyclerView.setAdapter(mAdapter);
        //initialize swipeRefreshLayout
        initScreen();
    }

    /**
     * Method to initialize swipe to refresh view.
     */
    private void initScreen() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this
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
        if (getNetConnectionStatus(this)) {
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
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(CollaborationDetailsActivity.this, resId);
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

        mAdapter.setCollaborationitemClickedListener(new OnCollaborationItemClickedListener() {
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

        // check net status
        if (NetworkHelper.getNetConnectionStatus(CollaborationDetailsActivity.this)) {
            final boolean[] tokenError = {false};
            final boolean[] connectionError = {false};

            final FeedModel feedData = new FeedModel();

            viewProgress.setVisibility(View.VISIBLE);

            SharedPreferenceHelper spHelper = new SharedPreferenceHelper(CollaborationDetailsActivity.this);

            JSONObject data = new JSONObject();
            try {
                data.put("uuid", spHelper.getUUID());
                data.put("authkey", spHelper.getAuthToken());
                data.put("entityid", entityID);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Rx2AndroidNetworking.post(BuildConfig.URL + "/entity-manage/load-specific")
                    .addJSONObjectBody(data)
                    .build()
                    .getJSONObjectObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new Observer<JSONObject>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {
                            mCompositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(@NonNull JSONObject jsonObject) {

                            viewProgress.setVisibility(View.GONE);

                            try {
                                //Token status is invalid
                                if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                    tokenError[0] = true;
                                } else {
                                    JSONObject mainObject = jsonObject.getJSONObject("data");

                                    JSONObject dataObj = mainObject.getJSONObject("entity");
                                    String type = dataObj.getString("type");

                                    feedData.setEntityID(entityID);
                                    feedData.setCaptureID(dataObj.getString("captureid"));
                                    feedData.setContentType(dataObj.getString("type"));
                                    feedData.setUUID(dataObj.getString("uuid"));
                                    feedData.setCreatorImage(dataObj.getString("profilepicurl"));
                                    feedData.setCreatorName(dataObj.getString("creatorname"));
                                    feedData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    feedData.setMerchantable(dataObj.getBoolean("merchantable"));
                                    feedData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    feedData.setCommentCount(dataObj.getLong("commentcount"));
                                    feedData.setContentImage(dataObj.getString("entityurl"));

                                    feedData.setCollabCount(dataObj.getLong("collabcount"));
                                    if (dataObj.isNull("caption")) {
                                        feedData.setCaption(null);
                                    } else {
                                        feedData.setCaption(dataObj.getString("caption"));
                                    }

                                    if (type.equals(CONTENT_TYPE_CAPTURE)) {

                                        //Retrieve "CAPTURE_ID" if type is capture
                                        feedData.setCaptureID(dataObj.getString("captureid"));
                                        // if capture
                                        // then if key cpshort exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("cpshort")) {
                                            JSONObject collabObject = dataObj.getJSONObject("cpshort");

                                            feedData.setAvailableForCollab(false);
                                            // set collaborator details
                                            feedData.setCollabWithUUID(collabObject.getString("uuid"));
                                            feedData.setCollabWithName(collabObject.getString("name"));

                                        } else {
                                            feedData.setAvailableForCollab(true);
                                        }

                                    } else if (type.equals(CONTENT_TYPE_SHORT)) {

                                        //Retrieve "SHORT_ID" if type is short
                                        feedData.setShortID(dataObj.getString("shoid"));

                                        // if short
                                        // then if key shcapture exists
                                        // not available for collaboration
                                        if (!dataObj.isNull("shcapture")) {

                                            JSONObject collabObject = dataObj.getJSONObject("shcapture");

                                            feedData.setAvailableForCollab(false);
                                            // set collaborator details
                                            feedData.setCollabWithUUID(collabObject.getString("uuid"));
                                            feedData.setCollabWithName(collabObject.getString("name"));
                                        } else {
                                            feedData.setAvailableForCollab(true);
                                        }
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(e);
                                connectionError[0] = true;

                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

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

                            } else

                            {

                                Bundle bundle = new Bundle();
                                bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, feedData);
                                bundle.putInt("position", -1);

                                Intent intent = new Intent(CollaborationDetailsActivity.this, FeedDescriptionActivity.class);
                                intent.putExtra(EXTRA_DATA, bundle);
                                startActivity(intent);

                                finish();
                            }
                        }
                    });
        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }
}
