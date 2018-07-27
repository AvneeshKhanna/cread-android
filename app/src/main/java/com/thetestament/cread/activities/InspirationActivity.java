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
import com.thetestament.cread.adapters.InspirationAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.InspirationModel;
import com.thetestament.cread.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_INSPIRATION;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;

/**
 * Activity class to show images available for inspiration.
 */
public class InspirationActivity extends BaseActivity {

    //region :View binding with butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    //endregion

    //region :Fields and constants
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    List<InspirationModel> mInspirationDataList = new ArrayList<>();
    InspirationAdapter mAdapter;
    SharedPreferenceHelper mHelper;
    private String mLastIndexKey;
    private boolean mRequestMoreData;
    InspirationActivity mContext;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspiration);
        mContext = this;
        //ButterKnife view binding
        ButterKnife.bind(mContext);
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(mContext);
        initScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
     * Method to initialize swipe refresh dialogParentView.
     */
    private void initScreen() {
        //Set dialogParentView manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Set adapter
        mAdapter = new InspirationAdapter(mInspirationDataList, mContext, Constant.INSPIRATION_ITEM_TYPE_DETAIL);
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data list
                mInspirationDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to null
                mLastIndexKey = null;
                //Load data here
                loadInspirationData();
            }
        });

        //Initialize listener
        initLoadMoreListener(mAdapter);
        //Load data here
        loadInspirationData();
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter ExploreAdapter reference.
     */
    private void initLoadMoreListener(InspirationAdapter adapter) {
        adapter.setLoadMoreListener(new listener.OnInspirationLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //If next set of data available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mInspirationDataList.add(null);
                                               mAdapter.notifyItemInserted(mInspirationDataList.size() - 1);
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
    private void loadInspirationData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getInspirationData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving inspiration data
     */
    private void getInspirationData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/inspiration-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_INSPIRATION)
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
                                parsePostsData(jsonObject, false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "InspirationActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "InspirationActivity");
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // set to false
                        GET_RESPONSE_FROM_NETWORK_INSPIRATION = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                        //No data
                        else if (mInspirationDataList.size() == 0) {
                            ViewHelper.getSnackBar(rootView, "Nothing to show.");
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
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
        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/inspiration-feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , GET_RESPONSE_FROM_NETWORK_INSPIRATION)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mInspirationDataList.remove(mInspirationDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mInspirationDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                parsePostsData(jsonObject, true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "InspirationActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mInspirationDataList.remove(mInspirationDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mInspirationDataList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "InspirationActivity");
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
     * Method to parse Json.
     *
     * @param jsonObject JsonObject to be parsed
     * @param isLoadMore Whether called from load more or not.
     * @throws JSONException
     */
    private void parsePostsData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {
        JSONObject mainData = jsonObject.getJSONObject("data");
        mRequestMoreData = mainData.getBoolean("requestmore");
        mLastIndexKey = mainData.getString("lastindexkey");
        //Inspiration list
        JSONArray array = mainData.getJSONArray("items");
        for (int i = 0; i < array.length(); i++) {
            JSONObject dataObj = array.getJSONObject(i);
            InspirationModel data = new InspirationModel();
            data.setEntityID(dataObj.getString("entityid"));
            data.setCaptureID(dataObj.getString("captureid"));
            data.setUUID(dataObj.getString("uuid"));
            data.setCreatorProfilePic(dataObj.getString("profilepicurl"));
            data.setCreatorName(dataObj.getString("creatorname"));
            data.setCapturePic(dataObj.getString("captureurl"));
            data.setMerchantable(dataObj.getBoolean("merchantable"));
            data.setLiveFilterName(dataObj.getString("livefilter"));
            //if image width pr image height is null
            if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                data.setImgWidth(1);
                data.setImgHeight(1);
            } else {
                data.setImgWidth(dataObj.getInt("img_width"));
                data.setImgHeight(dataObj.getInt("img_height"));
            }
            mInspirationDataList.add(data);
            //Called from load more
            if (isLoadMore) {
                //Notify item insertion
                mAdapter.notifyItemInserted(mInspirationDataList.size() - 1);
            }
        }

    }
    //endregion
}



