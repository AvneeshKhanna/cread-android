package com.thetestament.cread.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FeedAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;


public class FeedFragment extends Fragment {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    List<FeedModel> mFeedDataList = new ArrayList<>();
    FeedAdapter mAdapter;
    SharedPreferenceHelper mHelper;
    private Unbinder mUnbinder;
    private int mPageNumber = 0;
    private boolean mRequestMoreData;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getContext());
        return inflater
                .inflate(R.layout.fragment_feed
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

    /**
     * Method to initialize swipe to refresh view.
     */
    private void initScreen() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new FeedAdapter(mFeedDataList, getActivity());
        recyclerView.setAdapter(mAdapter);


        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mFeedDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set page count to zero
                mPageNumber = 0;
                //Load data here
                loadFeedData();
            }
        });

        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initHatsOffListener(mAdapter);
        //Load data here
        loadFeedData();
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter FeedAdapter reference.
     */
    private void initLoadMoreListener(FeedAdapter adapter) {

        adapter.setOnFeedLoadMoreListener(new listener.OnFeedLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (mRequestMoreData) {

                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mFeedDataList.add(null);
                                               mAdapter.notifyItemInserted(mFeedDataList.size() - 1);
                                           }
                                       }
                    );
                    //Increment page counter
                    mPageNumber += 1;
                    //Load new set of data
                    loadMoreData(mFeedDataList.size());
                }
            }
        });
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadFeedData() {
        // if user device is connected to net
        if (getNetConnectionStatus(getActivity())) {
            swipeRefreshLayout.setRefreshing(true);
            //Get data from server
            getFeedData();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving feed data
     */
    private void getFeedData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mPageNumber)
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
                                //FeedArray list
                                JSONArray feedArray = mainData.getJSONArray("feed");
                                for (int i = 0; i < feedArray.length(); i++) {
                                    JSONObject dataObj = feedArray.getJSONObject(i);
                                    FeedModel feedData = new FeedModel();
                                    feedData.setEntityID(dataObj.getString("entityid"));
                                    feedData.setUuID(dataObj.getString("uuid"));
                                    feedData.setCreatorImage(dataObj.getString("creatorimage"));
                                    feedData.setCreatorName(dataObj.getString("creatorName"));
                                    feedData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    feedData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    feedData.setCommentCount(dataObj.getLong("commnetcount"));
                                    feedData.setContentType(dataObj.getString("contenttype"));
                                    feedData.setImage(dataObj.getString("image"));

                                    mFeedDataList.add(feedData);
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
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            //Dismiss progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                            //Dismiss progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            //Dismiss progress indicator
                            swipeRefreshLayout.setRefreshing(false);
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
    private void loadMoreData(final int oldListSize) {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/feed/load"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mPageNumber)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mFeedDataList.remove(mFeedDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mFeedDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                //FeedArray list
                                JSONArray feedArray = mainData.getJSONArray("feed");
                                for (int i = 0; i < feedArray.length(); i++) {
                                    JSONObject dataObj = feedArray.getJSONObject(i);
                                    FeedModel feedData = new FeedModel();
                                    feedData.setEntityID(dataObj.getString("entityid"));
                                    feedData.setUuID(dataObj.getString("uuid"));
                                    feedData.setCreatorImage(dataObj.getString("creatorimage"));
                                    feedData.setCreatorName(dataObj.getString("creatorName"));
                                    feedData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    feedData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    feedData.setCommentCount(dataObj.getLong("commnetcount"));
                                    feedData.setContentType(dataObj.getString("contenttype"));
                                    feedData.setImage(dataObj.getString("image"));

                                    mFeedDataList.add(feedData);
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
                        mFeedDataList.remove(mFeedDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mFeedDataList.size());
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
                            mAdapter.notifyItemRangeInserted(oldListSize - 1, mFeedDataList.size() - oldListSize);
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }

    /**
     * Initialize hats off listener.
     *
     * @param adapter FeedAdapter reference.
     */
    private void initHatsOffListener(FeedAdapter adapter) {
        adapter.setHatsOffListener(new listener.OnHatsOffListener() {
            @Override
            public void onHatsOffClick(FeedModel feedData, int itemPosition) {
                updateHatsOffStatus(feedData, itemPosition);
            }
        });
    }


    /**
     * Method to update hats off status of campaign.
     *
     * @param feedData     Model of current item
     * @param itemPosition Position of current item i.e integer
     */
    private void updateHatsOffStatus(final FeedModel feedData, final int itemPosition) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", mHelper.getUUID());
            jsonObject.put("authkey", mHelper.getAuthToken());
            jsonObject.put("enityid", feedData.getEntityID());
            jsonObject.put("register", feedData.getHatsOffStatus());
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        AndroidNetworking.post(BuildConfig.URL + "/hatsoff/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Token status is not valid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                //set status to true if its false and vice versa
                                feedData.setHatsOffStatus(!feedData.getHatsOffStatus());
                                //notify changes
                                mAdapter.notifyItemChanged(itemPosition);
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Do nothing
                                } else {
                                    //set status to true if its false and vice versa
                                    feedData.setHatsOffStatus(!feedData.getHatsOffStatus());
                                    //notify changes
                                    mAdapter.notifyItemChanged(itemPosition);
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            //set status to true if its false and vice versa
                            feedData.setHatsOffStatus(!feedData.getHatsOffStatus());
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
                        feedData.setHatsOffStatus(!feedData.getHatsOffStatus());
                        //notify changes
                        mAdapter.notifyItemChanged(itemPosition);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }

}
