package com.thetestament.cread.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.RecommendedArtistsAdapter;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.RecommendedArtistsModel;
import com.thetestament.cread.models.UserPostModel;

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

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getRecommendedArtistObservableFromServer;

/**
 * AppCompat activity class to list of artists who are yet to be followed by user.
 */

public class RecommendedArtistsActivity extends BaseActivity {

    //region :Views binding with butter knife
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressView)
    View progressView;
    //endregion

    //region :Fields and constants
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    List<RecommendedArtistsModel> mDataList = new ArrayList<>();
    RecommendedArtistsAdapter mAdapter;

    @State
    String mLastIndexKey = null;
    @State
    boolean mRequestMoreData;
    RecommendedArtistsActivity mContext;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_artists);
        ButterKnife.bind(this);
        //Method called
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize views and retrieve data from intent.
     */
    private void initView() {
        //Obtain reference of this activity
        mContext = this;
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(mContext);

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Set adapter
        mAdapter = new RecommendedArtistsAdapter(mContext, mDataList);
        recyclerView.setAdapter(mAdapter);
        //initialize swipeRefreshLayout
        initSwipeRefreshLayout();
    }

    /**
     * Method to initialize swipe refresh view.
     */
    private void initSwipeRefreshLayout() {
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
                //Load data
                loadRecommendedArtistData();
            }
        });
        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initFollowListener(mAdapter);
        //Load data
        loadRecommendedArtistData();
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadRecommendedArtistData() {
        // if user device is connected to net
        if (getNetConnectionStatus(mContext)) {
            //Show progress indicator
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getRecommendedArtistData();
        } else {
            //Hide progress indicator
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView
                    , getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving recommended artists data.
     */
    private void getRecommendedArtistData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getRecommendedArtistObservableFromServer(BuildConfig.URL + "/recommend-users/details"
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
                                //Recommended artists list
                                JSONArray jsonArray = mainData.getJSONArray("users");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject dataObj = jsonArray.getJSONObject(i);
                                    RecommendedArtistsModel artistsModel = new RecommendedArtistsModel();
                                    //Set artists property
                                    artistsModel.setArtistUUID(dataObj.getString("uuid"));
                                    artistsModel.setArtistName(dataObj.getString("name"));
                                    artistsModel.setArtistProfilePic(dataObj.getString("profilepicurl"));
                                    artistsModel.setArtistBio(dataObj.getString("bio"));
                                    artistsModel.setPostCount(dataObj.getLong("postcount"));

                                    JSONArray contentArray = dataObj.getJSONArray("posts");
                                    List<UserPostModel> contentList = new ArrayList<>();
                                    for (int start = 0; start < contentArray.length(); start++) {
                                        UserPostModel postModel = new UserPostModel();
                                        postModel.setPostURL(contentArray.getString(start));
                                        contentList.add(postModel);
                                    }
                                    artistsModel.setUserPostList(contentList);
                                    mDataList.add(artistsModel);
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
                        //Set to false
                        GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS = false;
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                        //If data size is zero
                        else if (mDataList.size() == 0) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_data));
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
     * @param adapter RecommendedArtistsAdapter reference.
     */
    private void initLoadMoreListener(RecommendedArtistsAdapter adapter) {
        //Load more data listener
        adapter.setOnRecommendedArtistsLoadMoreListener(new listener.OnRecommendedArtistsLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
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
                    loadNextSetOfData();
                }
            }
        });
    }

    /**
     * Initialize follow listener.
     *
     * @param adapter dArtistsAdapter reference.
     */
    private void initFollowListener(RecommendedArtistsAdapter adapter) {
        adapter.setOnFollowClickListener(new listener.OnFollowClickListener() {
            @Override
            public void onFollowClick(String artistUUId, final int itemPosition) {
                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //Show progress view
                    progressView.setVisibility(View.VISIBLE);

                    FollowHelper followHelper = new FollowHelper();
                    followHelper.updateFollowStatus(mContext,
                            mCompositeDisposable,
                            true,
                            new JSONArray().put(artistUUId),
                            new listener.OnFollowRequestedListener() {
                                @Override
                                public void onFollowSuccess() {
                                    //Hide progress view
                                    progressView.setVisibility(View.GONE);
                                    //remove item from list and notify changes
                                    mAdapter.deleteItem(itemPosition);
                                    //Show snack bar
                                    ViewHelper.getSnackBar(rootView, "Followed successfully");
                                    //Set result ok
                                    setResult(RESULT_OK);
                                }

                                @Override
                                public void onFollowFailure(String errorMsg) {
                                    //Hide progress view
                                    progressView.setVisibility(View.GONE);
                                    //Show error message
                                    ViewHelper.getSnackBar(rootView, errorMsg);
                                }
                            });
                } else {
                    //Hide progress view
                    progressView.setVisibility(View.GONE);
                    //Show no connection view
                    ViewHelper.getSnackBar(rootView
                            , getString(R.string.error_msg_no_connection));
                }
            }
        });
    }

    /**
     * Method to retrieve next set of data from server.
     */
    private void loadNextSetOfData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getRecommendedArtistObservableFromServer(BuildConfig.URL + "/recommend-users/details"
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
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //Recommended artists list
                                JSONArray jsonArray = mainData.getJSONArray("users");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject dataObj = jsonArray.getJSONObject(i);
                                    RecommendedArtistsModel artistsModel = new RecommendedArtistsModel();
                                    //Set artists property
                                    artistsModel.setArtistUUID(dataObj.getString("uuid"));
                                    artistsModel.setArtistName(dataObj.getString("name"));
                                    artistsModel.setArtistProfilePic(dataObj.getString("profilepicurl"));
                                    artistsModel.setArtistBio(dataObj.getString("bio"));
                                    artistsModel.setPostCount(dataObj.getLong("postcount"));

                                    JSONArray contentArray = dataObj.getJSONArray("posts");
                                    List<UserPostModel> contentList = new ArrayList<>();
                                    for (int start = 0; start < contentArray.length(); start++) {
                                        UserPostModel postModel = new UserPostModel();
                                        postModel.setPostURL(contentArray.getString(start));
                                        contentList.add(postModel);
                                    }
                                    artistsModel.setUserPostList(contentList);
                                    mDataList.add(artistsModel);
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
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
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
    //endregion
}
