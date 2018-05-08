package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.UserInterestsAdapter;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.UserInterestsHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.UserInterestsModel;
import com.thetestament.cread.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.NetworkHelper.getUserInterestsDataFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;

public class UserInterestsActivity extends BaseActivity {

    @BindView(R.id.recyclerViewUserInterests)
    RecyclerView recyclerViewUserInterests;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rootView)
    ConstraintLayout rootView;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    UserInterestsAdapter mAdapter;
    ArrayList<UserInterestsModel> mInterestsList = new ArrayList<>();
    FragmentActivity mContext;
    SharedPreferenceHelper mHelper;

    @State
    String mLastIndexKey = null;
    @State
    boolean mRequestMoreData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interests);
        ButterKnife.bind(this);

        // init context
        mContext = this;
        // init screen
        initScreen();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_user_interests, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_action_user_interests_done:

                if (getIntent().getStringExtra(Constant.EXTRA_USER_INTERESTS_CALLED_FROM).equals(Constant.USER_INTERESTS_CALLED_FROM_LOGIN)) {
                    Intent intent = new Intent(mContext, BottomNavigationActivity.class);
                    startActivity(intent);
                    finish();

                    // TODO open recommendations screen
                } else {
                    // TODO
                }

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
     * Initializes the screen
     */
    private void initScreen() {

        // init shared preferences
        mHelper = new SharedPreferenceHelper(mContext);
        // init swipe refresh
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));


        // init adapter
        mAdapter = new UserInterestsAdapter(mContext, mInterestsList);
        // set adapter to recycler view
        recyclerViewUserInterests.setAdapter(mAdapter);

        //init listeners
        initLoadMoreListener();
        initInterestClickedListener();


        // load data
        loadInterestsData();
    }

    /**
     * Initializes the load more listener
     */
    private void initLoadMoreListener() {

        mAdapter.setLoadMoreInterestsListener(new listener.OnInterestsLoadMoreListener() {
            @Override
            public void onLoadMore() {

                //if more data is available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mInterestsList.add(null);
                                               mAdapter.notifyItemInserted(mInterestsList.size() - 1);
                                           }
                                       }
                    );
                    //Load new set of data
                    loadMoreInterestsData();
                }

            }
        });

    }

    /**
     * Initializes interest click listener
     */
    private void initInterestClickedListener() {
        mAdapter.setUserInterestClickedListener(new listener.OnInterestClickedListener() {
            @Override
            public void onInterestClicked(final UserInterestsModel data, final int position) {

                UserInterestsHelper userInterestsHelper = new UserInterestsHelper();
                userInterestsHelper.updateUserInterests(mContext
                        , mCompositeDisposable, data.isUserInterested()
                        , data.getInterestId()
                        , new listener.OnUserInterestClickedListener() {
                            @Override
                            public void onInterestSuccess() {

                            }

                            @Override
                            public void onInterestFailure(String errorMsg) {
                                //set status to true if its false and vice versa
                                data.setUserInterested(!data.isUserInterested());
                                //notify changes
                                mAdapter.notifyItemChanged(position);
                                ViewHelper.getSnackBar(rootView, errorMsg);

                            }
                        });


            }
        });
    }

    /**
     * Loads the interests data from server
     */
    private void loadInterestsData() {

        final boolean tokenError[] = {false};
        final boolean connectionError[] = {false};

        NetworkHelper.requestServer(mCompositeDisposable
                , getUserInterestsDataFromServer(BuildConfig.URL + "/user-interests/load",
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        mLastIndexKey
                )
                , mContext
                , new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        // dismiss swipe refresh
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefreshLayout.setEnabled(false);
                        // show snackbar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {

                                // parse json and add data to data list
                                parseInterestsData(jsonObject, false);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }

                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        // dismiss swipe refresh
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefreshLayout.setEnabled(false);
                        // show snackbar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));


                    }

                    @Override
                    public void onCompleteCalled() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        swipeRefreshLayout.setEnabled(false);

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
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
                            recyclerViewUserInterests.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                        }

                    }
                });
    }


    /**
     * Loads next set of interests from server
     */
    private void loadMoreInterestsData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable,
                getUserInterestsDataFromServer(BuildConfig.URL + "/user-interests/load",
                        mHelper.getUUID(),
                        mHelper.getAuthToken(),
                        mLastIndexKey
                ),
                mContext,
                new listener.OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        //Remove loading item
                        mInterestsList.remove(mInterestsList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mInterestsList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                // add data to list
                                parseInterestsData(jsonObject, true);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        //Remove loading item
                        mInterestsList.remove(mInterestsList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mInterestsList.size());
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

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
                });
    }

    /**
     * Method to parse JSON data returned from the server
     *
     * @param jsonObject Json object to parse
     * @param isLoadMore true if called from load more else false
     * @throws JSONException
     */
    private void parseInterestsData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {

        JSONObject mainData = jsonObject.getJSONObject("data");
        mRequestMoreData = mainData.getBoolean("requestmore");
        mLastIndexKey = mainData.getString("lastindexkey");
        //ExploreArray list
        JSONArray interestArray = mainData.getJSONArray("interests");
        for (int i = 0; i < interestArray.length(); i++) {
            JSONObject dataObj = interestArray.getJSONObject(i);


            UserInterestsModel interestsData = new UserInterestsModel();
            interestsData.setInterestId(dataObj.getString("intid"));
            interestsData.setInterestName(dataObj.getString("intname"));
            interestsData.setInterestImageURL(dataObj.getString("intimgurl"));

            mInterestsList.add(interestsData);

            if (isLoadMore) {
                //Notify item changes
                mAdapter.notifyItemInserted(mInterestsList.size() - 1);
            }
        }
    }
}
