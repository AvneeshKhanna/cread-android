package com.thetestament.cread.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.thetestament.cread.R;
import com.thetestament.cread.adapters.HatsOffAdapter;
import com.thetestament.cread.models.HatsOffModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;


/**
 * Class which show hats off details.
 */
public class HatsOffActivity extends AppCompatActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @State
    String mCampaignID;

    List<HatsOffModel> hatsOffList = new ArrayList<>();
    HatsOffAdapter mAdapter;
    private int mPageNumber = 0;
    private boolean mRequestMoreData;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_hats_off);
        ButterKnife.bind(this);


        //Retrieve data from intent
        mCampaignID = getIntent().getStringExtra("cmid");

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(HatsOffActivity.this));
        //Set adapter
        mAdapter = new HatsOffAdapter(hatsOffList, HatsOffActivity.this);
        recyclerView.setAdapter(mAdapter);

        //Load more data listener
        mAdapter.setOnLoadMoreListener(new HatsOffAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               hatsOffList.add(null);
                                               mAdapter.notifyItemInserted(hatsOffList.size() - 1);
                                           }
                                       }
                    );

                    //Increment page counter
                    mPageNumber += 1;
                    //Load new set of data
                    // loadMoreData();
                }
            }
        });
        //init screen
        initScreen();
    }

/*
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
                //Navigate away from  this screen
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //For calligraphy library
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    */
/**
 * This method loads data from server if user device is connected to internet.
 *//*

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

    */
/**
 * RxJava2 implementation for retrieving share details data
 *//*

    private void getHatsOffData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(this, BuildConfig.URL + "/campaign-manage/load-hatsoffs", mCampaignID, mPageNumber)
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
                                //Hats off details list
                                JSONArray hatsOffArray = mainData.getJSONArray("hatsoffs");
                                for (int i = 0; i < hatsOffArray.length(); i++) {
                                    JSONObject dataObj = hatsOffArray.getJSONObject(i);
                                    HatsOffModel hatsOffData = new HatsOffModel();
                                    hatsOffData.setUuid(dataObj.getString("uuid"));
                                    hatsOffData.setFirstName(dataObj.getString("firstname"));
                                    hatsOffData.setLastName(dataObj.getString("lastname"));
                                    hatsOffData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    hatsOffList.add(hatsOffData);
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
                        SnackBarHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        // Token status invalid
                        if (tokenError[0]) {
                            SnackBarHelper.getSnackBar(rootView
                                    , getString(R.string.error_msg_invalid_token));
                            //Hide progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            SnackBarHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                            //Hide progress indicator
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            //Hide indicator
                            swipeRefreshLayout.setRefreshing(false);
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(HatsOffActivity.this, resId);
                            recyclerView.setLayoutAnimation(animation);

                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }

    private void loadMoreData() {

        MyApplication myApplication = MyApplication.getSingleTone();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uuid", AccountManagerUtils.getUserID(this));
            jsonObject.put("authkey", myApplication.getAuthToken());
            jsonObject.put("cmid", mCampaignID);
            jsonObject.put("page", mPageNumber);
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
            SnackBarHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }
        AndroidNetworking.post(BuildConfig.URL + "/campaign-manage/load-hatsoffs")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        //Remove loading item
                        hatsOffList.remove(hatsOffList.size() - 1);
                        mAdapter.notifyItemRemoved(hatsOffList.size());
                        try {
                            //Token in not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                SnackBarHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                JSONArray hatsOffArray = mainData.getJSONArray("hatsoffs");
                                for (int i = 0; i < hatsOffArray.length(); i++) {

                                    JSONObject dataObj = hatsOffArray.getJSONObject(i);
                                    HatsOffModel hatsOffData = new HatsOffModel();
                                    hatsOffData.setUuid(dataObj.getString("uuid"));
                                    hatsOffData.setFirstName(dataObj.getString("firstname"));
                                    hatsOffData.setLastName(dataObj.getString("lastname"));
                                    hatsOffData.setProfilePicUrl(dataObj.getString("profilepicurl"));
                                    hatsOffList.add(hatsOffData);

                                    //Notify changes
                                    mAdapter.notifyDataSetChanged();
                                    mAdapter.setLoaded();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            SnackBarHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Remove loading item
                        hatsOffList.remove(hatsOffList.size() - 1);
                        mAdapter.notifyItemRemoved(hatsOffList.size());
                        FirebaseCrash.report(anError);
                        //Server error Snack bar
                        SnackBarHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });
    }
*/

    /**
     * Method to initialize swipe to refresh view
     */
    private void initScreen() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                hatsOffList.clear();
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set page count to zero
                mPageNumber = 0;
                // loadHatsOffData();
            }
        });
        //loadHatsOffData();
    }
}
