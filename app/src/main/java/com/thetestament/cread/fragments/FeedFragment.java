package com.thetestament.cread.fragments;

import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.BottomNavigationActivity;
import com.thetestament.cread.activities.FindFBFriendsActivity;
import com.thetestament.cread.adapters.FeedAdapter;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_EXPLORE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FIND_FRIENDS;


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
    @BindView(R.id.findfbFriendsButton)
    TextView findfbFriendsButton;
    @BindView(R.id.explorePeopleButton)
    TextView explorePeopleButton;
    @BindView(R.id.view_no_posts)
    LinearLayout viewNoPosts;
    Unbinder unbinder;
    private Unbinder mUnbinder;
    private int mPageNumber = 0;
    private boolean mRequestMoreData;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getContext());
        View view = inflater
                .inflate(R.layout.fragment_feed
                        , container
                        , false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ButterKnife view binding
        mUnbinder = ButterKnife.bind(this, view);
        initScreen();

        //This screen opened for first time
        if (mHelper.isWelcomeFirstTime()) {
            //Show welcome dialog
            showWelcomeMessage();
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

    /**
     * Method to initialize swipe to refresh view.
     */
    private void initScreen() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new FeedAdapter(mFeedDataList, getActivity(), mHelper.getUUID());
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
                // hide no posts view
                viewNoPosts.setVisibility(View.GONE);
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
                    loadMoreData();
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
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));

                        } else if (mFeedDataList.size() == 0) {

                            viewNoPosts.setVisibility(View.VISIBLE);


                            //ViewHelper.getSnackBar(rootView, "Find friends functionality coming soon");
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

                                    mFeedDataList.add(feedData);
                                    //Notify item changes
                                    mAdapter.notifyItemInserted(mFeedDataList.size() - 1);
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
            jsonObject.put("entityid", feedData.getEntityID());
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

    /**
     * Method to show welcome Message when user land on this screen for the first time.
     */
    private void showWelcomeMessage() {
        //Todo change image and text
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_generic, false)
                .positiveText(getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        //update status
                        mHelper.updateWelcomeDialogStatus(false);
                    }
                }).show();

        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);

        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.img_welcome));
        //Set title text
        textTitle.setText("Welcome to Cread");
        //Set description text
        textDesc.setText("Cread is a Social platform where artists can collaborate and showcase their work to earn recognition, goodwill and revenues.");
    }


    @OnClick(R.id.findfbFriendsButton)
    public void onFindFBFriendsClicked() {
        startActivity(new Intent(getActivity(), FindFBFriendsActivity.class));
        //Log firebase event
        setAnalytics(FIREBASE_EVENT_FIND_FRIENDS);
    }

    @OnClick(R.id.explorePeopleButton)
    public void onExploreFriendsClicked() {
        ((BottomNavigationActivity) getActivity()).activateBottomNavigationItem(R.id.action_explore);
        ((BottomNavigationActivity) getActivity()).replaceFragment(new ExploreFragment(), Constant.TAG_EXPLORE_FRAGMENT);
        //Log firebase event
        setAnalytics(FIREBASE_EVENT_EXPLORE_CLICKED);
    }


    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent) {

        Bundle bundle = new Bundle();
        bundle.putString("uuid", mHelper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_FIND_FRIENDS)) {
            FirebaseAnalytics.getInstance(getActivity()).logEvent(FIREBASE_EVENT_FIND_FRIENDS, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_EXPLORE_CLICKED)) {
            FirebaseAnalytics.getInstance(getActivity()).logEvent(FIREBASE_EVENT_EXPLORE_CLICKED, bundle);
        }

    }
}
