package com.thetestament.cread.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FBFriendsAdapter;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FBFriendsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_EXPLORE;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_FOLLOWING;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_MAIN;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ME;

public class FindFBFriendsActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.header_ff)
    RelativeLayout header;
    @BindView(R.id.buttonFollowAll)
    TextView buttonFollowAll;
    @BindView(R.id.placeholder)
    TextView placeholder;
    @BindView(R.id.nestedScrollView)
    NestedScrollView scrollView;
    @BindView(R.id.noFriendsPlaceholder)
    LinearLayout noFriendsPlaceholder;


    private String mNextUrl;
    private boolean mRequestMoreData;
    List<FBFriendsModel> mDataList = new ArrayList<>();
    FBFriendsAdapter mAdapter;
    private CallbackManager mCallbackManager;
    private final String TAG = getClass().getSimpleName();
    boolean mFollowStatus;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper spHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fbfriends);
        ButterKnife.bind(this);

        //For smooth scrolling
        ViewCompat.setNestedScrollingEnabled(recyclerView, false);

        spHelper = new SharedPreferenceHelper(FindFBFriendsActivity.this);

        initView();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish this activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }


    /**
     * Method to initialize views
     */
    private void initView() {

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(FindFBFriendsActivity.this));
        //Set adapter
        mAdapter = new FBFriendsAdapter(mDataList, FindFBFriendsActivity.this);
        recyclerView.setAdapter(mAdapter);

        //initialize  recyclerView
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

                header.setVisibility(View.GONE);

                //Clear data
                mDataList.clear();
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set page count to zero
                mNextUrl = null;
                //mPageNumber = 0;
                getFriendsData();
            }
        });


        //Initialize listener
        initLoadMoreListener(mAdapter);
        //Load more data

        initFollowClickedListener(mAdapter);

        getFriendsData();
    }

    private void getFriendsData() {
        if (NetworkHelper.getNetConnectionStatus(FindFBFriendsActivity.this)) {

            // checking validity of facebook access token
            if (AccessToken.getCurrentAccessToken().isExpired()) {
                ViewHelper.getSnackBar(rootView, "Some problem occured. You'll have to login again.");
            } else

            {
                swipeRefreshLayout.setRefreshing(true);
                //Get data from server


                final boolean[] tokenError = {false};
                final boolean[] connectionError = {false};


                mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/user-profile/load-fb-friends")
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
                                    }
                                    // authtoken expiration handling redirect user to fb login
                               /* else if () {

                                }*/
                                    else {

                                        JSONObject mainData = jsonObject.getJSONObject("data");
                                        mRequestMoreData = mainData.getBoolean("requestmore");
                                        mNextUrl = mainData.getString("nexturl");
                                        //Friends details list
                                        JSONArray friendsArray = mainData.getJSONArray("friends");
                                        for (int i = 0; i < friendsArray.length(); i++) {
                                            JSONObject dataObj = friendsArray.getJSONObject(i);
                                            FBFriendsModel friendsModel = new FBFriendsModel();
                                            friendsModel.setUuid(dataObj.getString("uuid"));
                                            friendsModel.setFirstName(dataObj.getString("firstname"));
                                            friendsModel.setLastName(dataObj.getString("lastname"));
                                            friendsModel.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                            friendsModel.setFollowStatus(dataObj.getBoolean("followstatus"));
                                            mDataList.add(friendsModel);
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

                                ANError error = (ANError) e;

                                if (error.getErrorCode() == 500) {
                                    try {
                                        JSONObject object = new JSONObject(error.getErrorBody());

                                        ViewHelper.getSnackBar(rootView, object.getString("message"));

                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                        FirebaseCrash.report(e1);
                                    }
                                }
                                // case when server is down
                                else {
                                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                                }
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

                                    header.setVisibility(View.VISIBLE);

                                    if (mDataList.size() == 0) {
                                        scrollView.setVisibility(View.GONE);
                                        //placeholder.setVisibility(View.VISIBLE);
                                        noFriendsPlaceholder.setVisibility(View.VISIBLE);
                                    } else {
                                        scrollView.setVisibility(View.VISIBLE);
                                        //placeholder.setVisibility(View.INVISIBLE);
                                        noFriendsPlaceholder.setVisibility(View.INVISIBLE);
                                    }
                                    //Dismiss indicator
                                    swipeRefreshLayout.setRefreshing(false);
                                    //Apply 'Slide Up' animation
                                    int resId = R.anim.layout_animation_from_bottom;
                                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(FindFBFriendsActivity.this, resId);
                                    recyclerView.setLayoutAnimation(animation);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                );
            }


        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }


    /**
     * Initialize load more listener.
     *
     * @param adapter FBFriendsAdapter reference.
     */
    private void initLoadMoreListener(FBFriendsAdapter adapter) {

        //Load more data listener
        adapter.setOnLoadMoreListener(new listener.OnFriendsLoadMoreListener() {
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
                    //Increment page counter
                    //mPageNumber += 1;
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    private void initFollowClickedListener(FBFriendsAdapter adapter) {

        adapter.setFollowFriendsClickedListener(new listener.OnFollowFriendsClickedListener() {
            @Override
            public void onFollowClicked(int position, FBFriendsModel data) {

                if (NetworkHelper.getNetConnectionStatus(FindFBFriendsActivity.this)) {
                    updateFollowStatus(position, data);
                } else {
                    ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                }

            }
        });
    }


    /**
     * Method to update follow status.
     */
    private void updateFollowStatus(final int position, final FBFriendsModel data) {

        mFollowStatus = data.isFollowStatus();

        final JSONObject jsonObject = new JSONObject();
        try {
            JSONArray followees = new JSONArray();
            followees.put(data.getUuid());

            jsonObject.put("uuid", spHelper.getUUID());
            jsonObject.put("authkey", spHelper.getAuthToken());
            jsonObject.put("followees", followees);
            jsonObject.put("register", data.isFollowStatus());
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }


        AndroidNetworking.post(BuildConfig.URL + "/follow/on-click")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            //Token status is not valid
                            if (response.getString("tokenstatus").equals("invalid")) {
                                //set status to true if its false and vice versa
                                mFollowStatus = !mFollowStatus;
                                ViewHelper.getSnackBar(rootView
                                        , getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = response.getJSONObject("data");
                                if (mainData.getString("status").equals("done")) {

                                    // set feeds data to be loaded from network
                                    // instead of cached data
                                    GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                    GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                    GET_RESPONSE_FROM_NETWORK_ME = true;
                                    GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS = true;
                                    GET_RESPONSE_FROM_NETWORK_FOLLOWING = true;


                                } else {
                                    //set status to true if its false and vice versa
                                    mFollowStatus = !mFollowStatus;
                                    //toggle follow button
                                    ViewHelper.getSnackBar(rootView
                                            , getString(R.string.error_msg_internal));
                                }
                            }
                        } catch (JSONException e) {
                            //set status to true if its false and vice versa
                            mFollowStatus = !mFollowStatus;
                            //toggle follow button
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_internal));
                        }

                        data.setFollowStatus(mFollowStatus);
                        mAdapter.notifyItemChanged(position);

                    }

                    @Override
                    public void onError(ANError anError) {
                        //set status to true if its false and vice versa
                        mFollowStatus = !mFollowStatus;
                        //toggle follow button
                        anError.printStackTrace();
                        FirebaseCrash.report(anError);
                        ViewHelper.getSnackBar(rootView
                                , getString(R.string.error_msg_server));

                        data.setFollowStatus(mFollowStatus);
                        mAdapter.notifyItemChanged(position);
                    }
                });
    }


    /**
     * Method to return data from the server
     *
     * @param serverURL URL of the server
     */
    private Observable<JSONObject> getObservableFromServer(String serverURL) {

        Map<String, String> headers = new HashMap<>();
        headers.put("uuid", spHelper.getUUID());
        headers.put("authkey", spHelper.getAuthToken());

        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("fbaccesstoken", AccessToken.getCurrentAccessToken().getToken());
        queryParam.put("fbid", AccessToken.getCurrentAccessToken().getUserId());
        queryParam.put("nexturl", mNextUrl);


        return Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParam)
                .build()
                .getJSONObjectObservable();
    }


    /**
     * Method to retrieve next set of data from server.
     */
    private void loadMoreData() {

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};


        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/user-profile/load-fb-friends")
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
                                mNextUrl = mainData.getString("nexturl");
                                //friends details list
                                JSONArray friendsArray = mainData.getJSONArray("friends");
                                for (int i = 0; i < friendsArray.length(); i++) {
                                    JSONObject dataObj = friendsArray.getJSONObject(i);
                                    FBFriendsModel friendsModel = new FBFriendsModel();
                                    friendsModel.setUuid(dataObj.getString("uuid"));
                                    friendsModel.setFirstName(dataObj.getString("firstname"));
                                    friendsModel.setLastName(dataObj.getString("lastname"));
                                    friendsModel.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    friendsModel.setFollowStatus(dataObj.getBoolean("followstatus"));
                                    mDataList.add(friendsModel);
                                    //Notify item changes
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


                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ANError error = (ANError) e;

                        if (error.getErrorCode() == 500) {
                            try {
                                JSONObject object = new JSONObject(error.getErrorBody());

                                ViewHelper.getSnackBar(rootView, object.getString("message"));

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                FirebaseCrash.report(e1);
                            }
                        }
                        // case when server is down
                        else {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        }
                    }

                    @Override
                    public void onComplete() {
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else {
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }

    @OnClick(R.id.buttonFollowAll)
    public void onViewClicked() {

        if (NetworkHelper.getNetConnectionStatus(FindFBFriendsActivity.this)) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("uuid", spHelper.getUUID());
                jsonObject.put("authkey", spHelper.getAuthToken());
                jsonObject.put("fbid", AccessToken.getCurrentAccessToken().getUserId());
                jsonObject.put("fbaccesstoken", AccessToken.getCurrentAccessToken().getToken());
            } catch (JSONException e) {
                e.printStackTrace();
                FirebaseCrash.report(e);
            }


            AndroidNetworking.post(BuildConfig.URL + "/follow/fb-friends-all")
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

                                        int i = 0;
                                        while (i < mDataList.size()) {
                                            mDataList.get(i).setFollowStatus(true);
                                            i++;
                                        }
                                        mAdapter.notifyDataSetChanged();
                                        ViewHelper.getSnackBar(rootView, "All friends followed");

                                        // set feeds data to be loaded from network
                                        // instead of cached data
                                        GET_RESPONSE_FROM_NETWORK_MAIN = true;
                                        GET_RESPONSE_FROM_NETWORK_EXPLORE = true;
                                        GET_RESPONSE_FROM_NETWORK_ME = true;
                                        GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS = true;
                                        GET_RESPONSE_FROM_NETWORK_FOLLOWING = true;

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
                            anError.printStackTrace();
                            FirebaseCrash.report(anError);

                            if (anError.getErrorCode() == 500) {
                                try {
                                    JSONObject error = new JSONObject(anError.getErrorBody());
                                    ViewHelper.getSnackBar(rootView, error.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            // case when server is down
                            else {
                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                            }


                        }
                    });
        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }

    }
}
