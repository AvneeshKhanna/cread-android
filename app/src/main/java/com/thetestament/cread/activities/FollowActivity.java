package com.thetestament.cread.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FollowAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FollowModel;

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

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FOLLOWING;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_REQUESTED_UUID;
import static com.thetestament.cread.utils.Constant.EXTRA_FOLLOW_TYPE;

/**
 * Screen to show followers/following data from server.
 */
public class FollowActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private SharedPreferenceHelper mHelper;

    List<FollowModel> mFollowList = new ArrayList<>();
    FollowAdapter mAdapter;

    @State
    String mServerURL, mRequestedUUID, mLastIndexKey = null;
    @State
    boolean mRequestMoreData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        ButterKnife.bind(this);
        //initialize preference helper
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
     * Method to retrieve data from intent and initialize views.
     */
    private void initView() {
        //Retrieve  uuid from intent
        mRequestedUUID = getIntent().getStringExtra(EXTRA_FOLLOW_REQUESTED_UUID);
        if (getIntent().getStringExtra(EXTRA_FOLLOW_TYPE).equals("following")) {
            //set toolbar title
            setTitle("Following");
            //set server url
            mServerURL = "/follow/load-following";
        } else {
            //set toolbar title
            setTitle("Followers");
            //set server url
            mServerURL = "/follow/load-followers";
        }

        //Set dialogParentView manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Set adapter
        mAdapter = new FollowAdapter(mFollowList, this);
        recyclerView.setAdapter(mAdapter);
        //initialize swipeRefreshLayout
        initScreen();
    }

    /**
     * Method to initialize swipe refresh dialogParentView.
     */
    private void initScreen() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear list data
                mFollowList.clear();
                //Notify changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key
                mLastIndexKey = null;
                //Load follow data here
                loadFollowData();
            }
        });
        //Initialize listener
        initLoadMoreListener(mAdapter);
        //Load follow data
        loadFollowData();
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadFollowData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getFollowData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving follow data.
     */
    private void getFollowData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + mServerURL
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mRequestedUUID
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_FOLLOWING)

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
                                //Follow details list
                                JSONArray followArray = mainData.getJSONArray("users");
                                for (int i = 0; i < followArray.length(); i++) {
                                    JSONObject dataObj = followArray.getJSONObject(i);
                                    FollowModel followData = new FollowModel();
                                    followData.setUuid(dataObj.getString("uuid"));
                                    followData.setFirstName(dataObj.getString("firstname"));
                                    followData.setLastName(dataObj.getString("lastname"));
                                    followData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    followData.setTopArtist(dataObj.getBoolean("topartist"));
                                    mFollowList.add(followData);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "FollowActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "FollowActivity");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_FOLLOWING = false;
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
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(FollowActivity.this, resId);
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
     * @param adapter FollowAdapter reference/object.
     */
    private void initLoadMoreListener(FollowAdapter adapter) {

        //Load more data listener
        adapter.setOnFollowLoadMoreListener(new listener.OnFollowLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mFollowList.add(null);
                                               mAdapter.notifyItemInserted(mFollowList.size() - 1);
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
     * Method to retrieve next set of data from server.
     */
    private void loadMoreData() {

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + mServerURL
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mRequestedUUID
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_FOLLOWING)

                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mFollowList.remove(mFollowList.size() - 1);
                        mAdapter.notifyItemRemoved(mFollowList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Follow details list
                                JSONArray followArray = mainData.getJSONArray("users");
                                for (int i = 0; i < followArray.length(); i++) {
                                    JSONObject dataObj = followArray.getJSONObject(i);
                                    FollowModel followData = new FollowModel();
                                    followData.setUuid(dataObj.getString("uuid"));
                                    followData.setFirstName(dataObj.getString("firstname"));
                                    followData.setLastName(dataObj.getString("lastname"));
                                    followData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    followData.setTopArtist(dataObj.getBoolean("topartist"));
                                    mFollowList.add(followData);
                                    //Notify changes
                                    mAdapter.notifyItemInserted(mFollowList.size() - 1);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "FollowActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mFollowList.remove(mFollowList.size() - 1);
                        mAdapter.notifyItemRemoved(mFollowList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "FollowActivity");
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
                            //Update status
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }

}
