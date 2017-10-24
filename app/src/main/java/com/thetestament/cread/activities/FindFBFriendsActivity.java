package com.thetestament.cread.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
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
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class FindFBFriendsActivity extends AppCompatActivity {

    @BindView(R.id.rootView)
    ConstraintLayout rootView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    //private int mPageNumber = 0;
    private String mNextUrl;
    private boolean mRequestMoreData;
    List<FBFriendsModel> mDataList = new ArrayList<>();
    FBFriendsAdapter mAdapter;
    private int mFriendCount;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fbfriends);
        ButterKnife.bind(this);

      /*  FBFriendsModel friendsModel = new FBFriendsModel();
        friendsModel.setFirstName("Prakhar");
        friendsModel.setLastName("Chandna");
        friendsModel.setUuid("abc");
        friendsModel.setProfilePicUrl("https://scontent.xx.fbcdn.net/v/t1.0-1/c41.41.512.512/s50x50/407817_2584380493226_1285693610_n.jpg?oh=18ac083ae4f5906f3576efa1f4f530cf&oe=5A82237B");
        mDataList.add(friendsModel);

        FBFriendsModel friendsModel1 = new FBFriendsModel();
        friendsModel1.setFirstName("Avneesh");
        friendsModel1.setLastName("Khanna");
        friendsModel1.setUuid("def");
        friendsModel1.setProfilePicUrl("https://scontent.xx.fbcdn.net/v/t1.0-1/c41.41.512.512/s50x50/407817_2584380493226_1285693610_n.jpg?oh=18ac083ae4f5906f3576efa1f4f530cf&oe=5A82237B");
        mDataList.add(friendsModel1);

        FBFriendsModel friendsModel2 = new FBFriendsModel();
        friendsModel2.setFirstName("Biswa");
        friendsModel2.setLastName("Kalyan Rath");
        friendsModel2.setUuid("pqr");
        friendsModel2.setProfilePicUrl("https://scontent.xx.fbcdn.net/v/t1.0-1/c41.41.512.512/s50x50/407817_2584380493226_1285693610_n.jpg?oh=18ac083ae4f5906f3576efa1f4f530cf&oe=5A82237B");
        mDataList.add(friendsModel2);*/



        //Set layout manager for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(FindFBFriendsActivity.this));
        //Set adapter
        mAdapter = new FBFriendsAdapter(mDataList, FindFBFriendsActivity.this, mFriendCount);


        initScreen();

        recyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
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
                //set page count to zero
                mNextUrl = null;
                //mPageNumber = 0;
                getFriendsData();
            }
        });
        //Initialize listener
        initLoadMoreListener(mAdapter);
        //Load more data

        askFBFriendsPermission();

        getFriendsData();
    }

    private void getFriendsData() {
        if (NetworkHelper.getNetConnectionStatus(FindFBFriendsActivity.this)) {

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

                                }*/ else {
                                    JSONObject mainData = jsonObject.getJSONObject("data");
                                    mRequestMoreData = mainData.getBoolean("requestmore");

                                    //Friends details list
                                    JSONArray friendsArray = mainData.getJSONArray("friends");
                                    for (int i = 0; i < friendsArray.length(); i++) {
                                        JSONObject dataObj = friendsArray.getJSONObject(i);
                                        FBFriendsModel friendsModel = new FBFriendsModel();
                                        friendsModel.setUuid(dataObj.getString("uuid"));
                                        friendsModel.setFirstName(dataObj.getString("firstname"));
                                        friendsModel.setLastName(dataObj.getString("lastname"));
                                        friendsModel.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                        friendsModel.setFollowStatus(dataObj.getString("followstatus"));
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
                                //Dismiss indicator
                                swipeRefreshLayout.setRefreshing(false);
                                //Apply 'Slide Up' animation
                                int resId = R.anim.layout_animation_from_bottom;
                                LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(FindFBFriendsActivity.this, resId);
                                recyclerView.setLayoutAnimation(animation);
                                //Notify changes
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    })
            );


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
                                           public void run() {// TODO check logic
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


    /**
     * Method to return data from the server
     *
     * @param serverURL URL of the server
     */
    private Observable<JSONObject> getObservableFromServer(String serverURL) {

        JSONObject jsonObject = new JSONObject();
        try {
            SharedPreferenceHelper spHelper = new SharedPreferenceHelper(FindFBFriendsActivity.this);

            jsonObject.put("uuid", spHelper.getUUID());
            jsonObject.put("authkey", spHelper.getAuthToken());
            jsonObject.put("fbaccesstoken", AccessToken.getCurrentAccessToken().getToken());
            jsonObject.put("fbid", AccessToken.getCurrentAccessToken().getUserId());
            jsonObject.put("nexturl", mNextUrl);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        return Rx2AndroidNetworking.post(serverURL)
                .addJSONObjectBody(jsonObject)
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
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            }
                            // TODO when fb access token has expired
                            /*else if () {

                            }*/ else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                //friends details list
                                JSONArray friendsArray = mainData.getJSONArray("friends");
                                for (int i = 0; i < friendsArray.length(); i++) {
                                    JSONObject dataObj = friendsArray.getJSONObject(i);
                                    FBFriendsModel friendsModel = new FBFriendsModel();
                                    friendsModel.setUuid(dataObj.getString("uuid"));
                                    friendsModel.setFirstName(dataObj.getString("firstname"));
                                    friendsModel.setLastName(dataObj.getString("lastname"));
                                    friendsModel.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    friendsModel.setFollowStatus(dataObj.getString("followstatus"));
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
                        //Remove loading item
                        // TODO check logic
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Remove loading item
                        // TODO check logic
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
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
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(FindFBFriendsActivity.this, resId);
                            recyclerView.setLayoutAnimation(animation);
                            //Notify changes
                            mAdapter.notifyDataSetChanged();
                            mAdapter.setLoaded();
                        }
                    }
                })
        );
    }

    private void askFBFriendsPermission()
    {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("user_friends"));
    }
}
