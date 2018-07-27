package com.thetestament.cread.activities;

import android.content.Intent;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.rx2androidnetworking.Rx2ANRequest;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.FBFriendsAdapter;
import com.thetestament.cread.helpers.DeepLinkHelper;
import com.thetestament.cread.helpers.FollowHelper;
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
    LinearLayout header;
    @BindView(R.id.containerInviteFriends)
    RelativeLayout containerInviteFriends;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Method to initialize views
     */
    private void initView() {

        //Set dialogParentView manger for recyclerView
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
                containerInviteFriends.setVisibility(View.GONE);

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
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            // checking validity of facebook access token
            if (AccessToken.getCurrentAccessToken() == null || AccessToken.getCurrentAccessToken().isExpired()) {
                swipeRefreshLayout.setRefreshing(false);


                showFbIntegrationDialog();


            } else

            {
                loadFriendsData();
            }


        } else {
            swipeRefreshLayout.setRefreshing(false);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * Shows fb integration dialog and handles the fb integration process
     */
    private void showFbIntegrationDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_fb_friends_integration, false)
                .show();


        final LoginButton buttonFbLogin = dialog.getCustomView().findViewById(R.id.buttonFBLogin);
        TextView buttonCustomFbLogin = dialog.getCustomView().findViewById(R.id.buttonCustomFbLogin);
        TextView buttonCancel = dialog.getCustomView().findViewById(R.id.buttonCanacel);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });

        buttonCustomFbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                buttonFbLogin.performClick();
            }
        });

        mCallbackManager = CallbackManager.Factory.create();


        buttonFbLogin.setReadPermissions(Arrays.asList("email", "user_friends"));
        buttonFbLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                if (loginResult.getRecentlyDeniedPermissions().contains("user_friends")) {
                    ViewHelper.getSnackBar(rootView, "You need to grant friends permission to continue");
                    AccessToken.setCurrentAccessToken(null);
                } else {
                    loadFriendsData();
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


    }

    private void loadFriendsData() {

        swipeRefreshLayout.setRefreshing(true);
        //Get data from server


        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};
        final boolean[] duplicateFbId = {false};


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

                                if (mainData.getBoolean("duplicate_fbid")) {
                                    duplicateFbId[0] = true;
                                } else

                                {
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

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "FindFBFriendsActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "FindFBFriendsActivity");

                        ANError error = (ANError) e;

                        if (error.getErrorCode() == 500) {
                            try {
                                JSONObject object = new JSONObject(error.getErrorBody());

                                ViewHelper.getSnackBar(rootView, object.getString("message"));

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "FindFBFriendsActivity");
                            }
                        }
                        // case when server is down
                        else {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        }
                    }

                    @Override
                    public void onComplete() {

                        // set to false
                        GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS = false;

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
                        } else if (duplicateFbId[0]) {
                            new MaterialDialog.Builder(FindFBFriendsActivity.this)
                                    .title(getString(R.string.title_dialog_fb_duplicate))
                                    .content(getString(R.string.text_dialog_fb_duplicate))
                                    .positiveText(getString(R.string.text_ok))
                                    .build()
                                    .show();
                            AccessToken.setCurrentAccessToken(null);
                            //Dismiss progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                        } else {

                            header.setVisibility(View.VISIBLE);
                            containerInviteFriends.setVisibility(View.VISIBLE);

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

        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(this,
                mCompositeDisposable,
                data.isFollowStatus(),
                new JSONArray().put(data.getUuid()),
                new listener.OnFollowRequestedListener() {
                    @Override
                    public void onFollowSuccess() {

                        data.setFollowStatus(mFollowStatus);
                        mAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onFollowFailure(String errorMsg) {

                        //set status to true if its false and vice versa
                        mFollowStatus = !mFollowStatus;
                        data.setFollowStatus(mFollowStatus);
                        mAdapter.notifyItemChanged(position);
                        ViewHelper.getSnackBar(rootView, errorMsg);

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


        Rx2ANRequest.GetRequestBuilder requestBuilder = Rx2AndroidNetworking.get(serverURL)
                .addHeaders(headers)
                .addQueryParameter(queryParam);

        if (GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS) {
            requestBuilder.getResponseOnlyFromNetwork();
        }

        return requestBuilder
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
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "FindFBFriendsActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {


                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        mAdapter.notifyItemRemoved(mDataList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "FindFBFriendsActivity");
                        //Server error Snack bar
                        ANError error = (ANError) e;

                        if (error.getErrorCode() == 500) {
                            try {
                                JSONObject object = new JSONObject(error.getErrorBody());

                                ViewHelper.getSnackBar(rootView, object.getString("message"));

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "FindFBFriendsActivity");
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

            if (AccessToken.getCurrentAccessToken() == null || AccessToken.getCurrentAccessToken().isExpired()) {

                showFbIntegrationDialog();
            } else {
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("uuid", spHelper.getUUID());
                    jsonObject.put("authkey", spHelper.getAuthToken());
                    jsonObject.put("fbid", AccessToken.getCurrentAccessToken().getUserId());
                    jsonObject.put("fbaccesstoken", AccessToken.getCurrentAccessToken().getToken());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Crashlytics.setString("className", "FindFBFriendsActivity");
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
                                    Crashlytics.logException(e);
                                    Crashlytics.setString("className", "FindFBFriendsActivity");
                                    ViewHelper.getSnackBar(rootView
                                            , getString(R.string.error_msg_internal));
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                anError.printStackTrace();
                                Crashlytics.logException(anError);
                                Crashlytics.setString("className", "FindFBFriendsActivity");

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
            }


        } else {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }

    }

    /**
     * Invite friends button click functionality
     */
    @OnClick({R.id.buttonInviteFriends, R.id.noDataInvite})
    public void onInviteClicked() {
        DeepLinkHelper.generateUserSpecificDeepLink(this
                , mCompositeDisposable
                , spHelper.getUUID()
                , spHelper.getAuthToken());
    }
}
