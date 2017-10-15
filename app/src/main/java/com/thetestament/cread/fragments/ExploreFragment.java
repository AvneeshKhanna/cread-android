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
import com.thetestament.cread.adapters.ExploreAdapter;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;


public class ExploreFragment extends Fragment {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    List<FeedModel> mExploreDataList = new ArrayList<>();
    ExploreAdapter mAdapter;
    private Unbinder mUnbinder;
    private int mPageNumber = 0;
    private boolean mRequestMoreData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater
                .inflate(R.layout.fragment_explore
                        , container
                        , false);
        //ButterKnife view binding
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initScreen();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mCompositeDisposable.dispose();
    }

    /**
     * Method to initialize swipe to refresh view.
     */
    private void initScreen() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new ExploreAdapter(mExploreDataList, getActivity());
        recyclerView.setAdapter(mAdapter);


        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mExploreDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set page count to zero
                mPageNumber = 0;
                //Load data here
                loadExploreData();
            }
        });

        //Initialize listener
        initLoadMoreListener(mAdapter);
        initFollowListener(mAdapter);
        //Load data here
        loadExploreData();
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadExploreData() {
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
     * RxJava2 implementation for retrieving explore data
     */
    private void getFeedData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(getActivity(), BuildConfig.URL + "/explore/load", mPageNumber)
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
                                JSONArray exploreArray = mainData.getJSONArray("feed");
                                for (int i = 0; i < exploreArray.length(); i++) {
                                    JSONObject dataObj = exploreArray.getJSONObject(i);
                                    FeedModel exploreData = new FeedModel();
                                    exploreData.setEntityID(dataObj.getString("entityid"));
                                    exploreData.setUuID(dataObj.getString("uuid"));
                                    exploreData.setCreatorImage(dataObj.getString("creatorimage"));
                                    exploreData.setCreatorName(dataObj.getString("creatorName"));
                                    exploreData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    exploreData.setFollowStatus(dataObj.getBoolean("followstatus"));
                                    exploreData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    exploreData.setCommentCount(dataObj.getLong("commnetcount"));
                                    exploreData.setContentType(dataObj.getString("contenttype"));
                                    exploreData.setImage(dataObj.getString("image"));

                                    if (dataObj.getString("contentType").equals(CONTENT_TYPE_SHORT)) {
                                        exploreData.setText(dataObj.getString("text"));
                                        exploreData.setTextSize(dataObj.getString("textsize"));
                                        exploreData.setTextColor(dataObj.getString("textcolor"));
                                        exploreData.setCoordinates(dataObj.getString("coordinates"));
                                    } else {
                                        //do nothing
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
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_invalid_token));
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
     * Initialize load more listener.
     *
     * @param adapter ExploreAdapter reference.
     */
    private void initLoadMoreListener(ExploreAdapter adapter) {

        adapter.setOnExploreLoadMoreListener(new listener.OnExploreLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (mRequestMoreData) {

                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mExploreDataList.add(null);
                                               mAdapter.notifyItemInserted(mExploreDataList.size() - 1);
                                           }
                                       }
                    );
                    //Increment page counter
                    mPageNumber += 1;
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    /**
     * Method to retrieve to next set of data from server.
     */
    private void loadMoreData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        mCompositeDisposable.add(getObservableFromServer(getActivity(), BuildConfig.URL + "/explore/load", mPageNumber)
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
                                JSONArray exploreArray = mainData.getJSONArray("feed");
                                for (int i = 0; i < exploreArray.length(); i++) {
                                    JSONObject dataObj = exploreArray.getJSONObject(i);
                                    FeedModel exploreData = new FeedModel();
                                    exploreData.setEntityID(dataObj.getString("entityid"));
                                    exploreData.setUuID(dataObj.getString("uuid"));
                                    exploreData.setCreatorImage(dataObj.getString("creatorimage"));
                                    exploreData.setCreatorName(dataObj.getString("creatorName"));
                                    exploreData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                    exploreData.setFollowStatus(dataObj.getBoolean("followstatus"));
                                    exploreData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                    exploreData.setCommentCount(dataObj.getLong("commnetcount"));
                                    exploreData.setContentType(dataObj.getString("contenttype"));
                                    exploreData.setImage(dataObj.getString("image"));

                                    if (dataObj.getString("contentType").equals(CONTENT_TYPE_SHORT)) {
                                        exploreData.setText(dataObj.getString("text"));
                                        exploreData.setTextSize(dataObj.getString("textsize"));
                                        exploreData.setTextColor(dataObj.getString("textcolor"));
                                        exploreData.setCoordinates(dataObj.getString("coordinates"));
                                    } else {
                                        //do nothing
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
                        //Remove loading item
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Remove loading item
                        mExploreDataList.remove(mExploreDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mExploreDataList.size());

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerView.setLayoutAnimation(animation);
                            //Notify changes
                            mAdapter.notifyDataSetChanged();
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
            public void onFollowClick(FeedModel exploreData, boolean followStatus) {
                updateFollowStatus(exploreData.getUuID(), followStatus);
            }
        });
    }

    /**
     * Method to update follow status.
     *
     * @param entityID   entity ID i.e String
     * @param isFollowed true if user is following , false otherwise.
     */
    private void updateFollowStatus(String entityID, boolean isFollowed) {
        final JSONObject jsonObject = new JSONObject();
        try {
            //Todo replace uuid an authkey
            jsonObject.put("uuid", "");
            jsonObject.put("authkey", "");
            jsonObject.put("enityid", entityID);
            jsonObject.put("register", isFollowed);
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
                                ViewHelper.getSnackBar(rootView
                                        , getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {
                                    //Do nothing
                                } else {
                                    ViewHelper.getSnackBar(rootView
                                            , getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        ViewHelper.getSnackBar(rootView
                                , getString(R.string.error_msg_server));
                    }
                });
    }

}
