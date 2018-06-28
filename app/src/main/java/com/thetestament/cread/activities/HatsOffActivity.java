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
import com.thetestament.cread.adapters.HatsOffAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.HatsOffModel;

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

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_HATSOFF;
import static com.thetestament.cread.helpers.NetworkHelper.getHatsOffObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;


/**
 * Class which show hats off details i.e person who supported.
 */
public class HatsOffActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;


    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    List<HatsOffModel> mHatsOffList = new ArrayList<>();
    HatsOffAdapter mAdapter;

    @State
    String mEntityID, mLastIndexKey = null;
    @State
    boolean mRequestMoreData;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_hats_off);
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
        mEntityID = getIntent().getStringExtra(EXTRA_ENTITY_ID);

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(HatsOffActivity.this));
        //Set adapter
        mAdapter = new HatsOffAdapter(mHatsOffList, HatsOffActivity.this);
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
                mHatsOffList.clear();
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index value to null
                mLastIndexKey = null;
                loadHatsOffData();
            }
        });
        //Initialize listener
        initLoadMoreListener(mAdapter);
        //Load more data
        loadHatsOffData();
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadHatsOffData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getHatsOffData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving hatsOff data.
     */
    private void getHatsOffData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getHatsOffObservableFromServer(BuildConfig.URL + "/hatsoff/load"
                , mEntityID
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
                                //Hats off details list
                                JSONArray hatsOffArray = mainData.getJSONArray("hatsoffs");
                                for (int i = 0; i < hatsOffArray.length(); i++) {
                                    JSONObject dataObj = hatsOffArray.getJSONObject(i);
                                    HatsOffModel hatsOffData = new HatsOffModel();
                                    hatsOffData.setUuid(dataObj.getString("uuid"));
                                    hatsOffData.setFirstName(dataObj.getString("firstname"));
                                    hatsOffData.setLastName(dataObj.getString("lastname"));
                                    hatsOffData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    mHatsOffList.add(hatsOffData);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "HatsOffActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "HatsOffActivity");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_HATSOFF = false;
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
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(HatsOffActivity.this, resId);
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
     * @param adapter HatsOffAdapter reference.
     */
    private void initLoadMoreListener(HatsOffAdapter adapter) {

        //Load more data listener
        adapter.setOnLoadMoreListener(new listener.OnHatsOffLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mHatsOffList.add(null);
                                               mAdapter.notifyItemInserted(mHatsOffList.size() - 1);
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

        mCompositeDisposable.add(getHatsOffObservableFromServer(BuildConfig.URL + "/hatsoff/load"
                , mEntityID
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
                        mHatsOffList.remove(mHatsOffList.size() - 1);
                        mAdapter.notifyItemRemoved(mHatsOffList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Hats off details list
                                JSONArray hatsOffArray = mainData.getJSONArray("hatsoffs");
                                for (int i = 0; i < hatsOffArray.length(); i++) {
                                    JSONObject dataObj = hatsOffArray.getJSONObject(i);
                                    HatsOffModel hatsOffData = new HatsOffModel();
                                    hatsOffData.setUuid(dataObj.getString("uuid"));
                                    hatsOffData.setFirstName(dataObj.getString("firstname"));
                                    hatsOffData.setLastName(dataObj.getString("lastname"));
                                    hatsOffData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    mHatsOffList.add(hatsOffData);
                                    //Notify changes
                                    mAdapter.notifyItemInserted(mHatsOffList.size() - 1);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "HatsOffActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mHatsOffList.remove(mHatsOffList.size() - 1);
                        mAdapter.notifyItemRemoved(mHatsOffList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "HatsOffActivity");
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
}
