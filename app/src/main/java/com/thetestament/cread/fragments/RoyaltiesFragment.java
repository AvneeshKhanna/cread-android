package com.thetestament.cread.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CashInActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.adapters.RoyaltiesAdapter;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnServerRequestedListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.RoyaltiesModel;

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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.helpers.FeedHelper.parseEntitySpecificJSON;
import static com.thetestament.cread.helpers.NetworkHelper.getEntitySpecificObservable;
import static com.thetestament.cread.helpers.NetworkHelper.getRoyaltiesObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CASH_IN_AMOUNT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_IS_PROFILE_EDITABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_MIN_CASH_AMT;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CASH_IN;


public class RoyaltiesFragment extends Fragment {


    List<RoyaltiesModel> mDataList = new ArrayList<>();
    RoyaltiesAdapter mAdapter;
    @BindView(R.id.royaltiesAmt)
    TextView royaltiesAmt;
    @BindView(R.id.containerRedeem)
    LinearLayout containerRedeem;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.viewNoData)
    LinearLayout viewNoData;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.buttonRedeem)
    TextView buttonRedeem;
    @BindView(R.id.royaltiesByLine)
    TextView royaltiesByLine;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.viewProgress)
    ProgressBar progressBar;

    Unbinder unbinder;
    @BindView(R.id.buttonCreate)
    TextView buttonCreate;

    private SharedPreferenceHelper mHelper;
    private String mLastIndexKey;
    private Unbinder mUnbinder;
    private boolean mRequestMoreData, mIsProfileEditable;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private boolean mRequestRoyaltiesData = true;
    private double minCashAmount;
    private double redeemAmount;

    FeedModel entitySpecificData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(getContext());

        View view = inflater
                .inflate(R.layout.fragment_royalties
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CODE_CASH_IN:
                if (resultCode == RESULT_OK) {   // refresh page
                    getActivity().recreate();
                }
                break;
        }
    }

    @OnClick(R.id.containerRedeem)
    public void onViewClicked() {

        Intent intent = new Intent(getActivity(), CashInActivity.class);
        intent.putExtra(EXTRA_MIN_CASH_AMT, minCashAmount);
        intent.putExtra(EXTRA_CASH_IN_AMOUNT, redeemAmount);
        // start cashin in activity
        startActivityForResult(intent, REQUEST_CODE_CASH_IN);
    }


    /**
     * Method to initialize swipe to refresh view.
     */
    private void initScreen() {

        // show dialog
        if(mHelper.isRoyaltyFirstTime())
        {
            showRoyaltiesDialog();
        }

        // set defualt values for texts
        setAmountTexts(0);

        // get data from Intent
        getIntentData();

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new RoyaltiesAdapter(mDataList, getActivity());
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // hide header
                appBarLayout.setVisibility(View.INVISIBLE);
                //Clear data
                mDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to null
                mLastIndexKey = null;
                //set load royalties data key to true
                mRequestRoyaltiesData = true;
                // hide no posts view
                viewNoData.setVisibility(View.GONE);
                //Load data here
                loadRoyaltiesData();
            }
        });

        //Initialize listeners
        initLoadMoreListener(mAdapter);
        initRoyaltyItemClickedListener();

        //Load data here
        loadRoyaltiesData();
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter FeedAdapter reference.
     */
    private void initLoadMoreListener(RoyaltiesAdapter adapter) {

        adapter.setOnRoyaltiesLoadMoreListener(new listener.OnRoyaltiesLoadMoreListener() {
            @Override
            public void onLoadMore() {
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
                    loadMoreRoyaltiesData();
                }
            }
        });
    }


    /**
     * Initialize item clicked listener.
     */
    private void initRoyaltyItemClickedListener() {

        mAdapter.setOnRoyaltyitemClicked(new listener.OnRoyaltyitemClickedListener() {
            @Override
            public void onRoyaltyItemClicked(String entityID) {

                getFeedDetails(entityID);
            }
        });
    }


    /**
     * Method to load data from server
     */
    private void loadRoyaltiesData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        swipeRefreshLayout.setRefreshing(true);

        requestServer(mCompositeDisposable,
                getRoyaltiesObservable
                        (BuildConfig.URL + "/sell/load",
                                mHelper.getUUID(),
                                mHelper.getAuthToken(),
                                mLastIndexKey,
                                mRequestRoyaltiesData),
                getActivity(),
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        swipeRefreshLayout.setRefreshing(false);
                        //No connection Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {

                                // add data to dat list
                                parseJSONData(jsonObject, false);

                                // show header
                                appBarLayout.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }

                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        swipeRefreshLayout.setRefreshing(false);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));

                        } else if (mDataList.size() == 0) {
                            if(mIsProfileEditable)
                            {
                                // show header
                                appBarLayout.setVisibility(View.VISIBLE);
                                viewNoData.setVisibility(View.VISIBLE);
                            }

                            ViewHelper.getSnackBar(rootView, "No orders yet");

                        } else {

                            //setting load royalties data to false
                            mRequestRoyaltiesData = false;
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerView.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );
    }

    /**
     * Method to retrieve to next set of data from server.
     */
    private void loadMoreRoyaltiesData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable,
                getRoyaltiesObservable
                        (BuildConfig.URL + "/sell/load",
                                mHelper.getUUID(),
                                mHelper.getAuthToken(),
                                mLastIndexKey,
                                mRequestRoyaltiesData),
                getActivity(),
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {
                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mDataList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                // add data to list
                                parseJSONData(jsonObject, true);
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
                        mDataList.remove(mDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mDataList.size());
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
                }
        );
    }

    /**
     * Method to parse JSON data returned from the server
     *
     * @param jsonObject Json object to parse
     * @param isLoadMore true if called from laod more else false
     * @throws JSONException
     */
    private void parseJSONData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {


        JSONObject mainData = jsonObject.getJSONObject("data");
        mRequestMoreData = mainData.getBoolean("requestmore");
        mLastIndexKey = mainData.getString("lastindexkey");
        //FeedArray list
        JSONArray itemsArray = mainData.getJSONArray("items");

        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject dataObj = itemsArray.getJSONObject(i);

            RoyaltiesModel royaltiesData = new RoyaltiesModel();

            royaltiesData.setEntityID(dataObj.getString("entityid"));
            royaltiesData.setProductType(dataObj.getString("producttype"));
            royaltiesData.setType(dataObj.getString("type"));
            royaltiesData.setRoyaltyDate(dataObj.getString("regdate"));
            royaltiesData.setQuantity(dataObj.getInt("qty"));
            royaltiesData.setUuid(dataObj.getString("uuid"));
            royaltiesData.setEntityUrl(dataObj.getString("entityurl"));
            royaltiesData.setName(dataObj.getString("name"));
            royaltiesData.setRoyaltyAmount(dataObj.getDouble("royalty_amount"));
            royaltiesData.setRedeemStatus(dataObj.getBoolean("redeemstatus"));

            mDataList.add(royaltiesData);

            if (isLoadMore) {
                //Notify item changes
                mAdapter.notifyItemInserted(mDataList.size() - 1);
            }
        }

        minCashAmount = mainData.getDouble("minimum_wallet_balance");

        if (mainData.getDouble("total_royalty") != -1 && mainData.getDouble("minimum_wallet_balance") != -1) {
            double totalRoyalty = mainData.getDouble("total_royalty");
            redeemAmount = mainData.getDouble("redeem_amount");
            // set texts
            setAmountTexts(totalRoyalty);
        }
    }

    /**
     * Method to update redeem amount and total royalty amount
     */
    private void setAmountTexts(double totalRoyalty) {
        buttonRedeem.setText("Redeem " + getActivity().getString(R.string.Rs) + " " + String.valueOf(redeemAmount));
        royaltiesAmt.setText(getActivity().getString(R.string.Rs) + " " + String.valueOf(totalRoyalty));
    }


    /**
     * RxJava2 implementation for retrieving feed details
     *
     * @param entityID
     */
    private void getFeedDetails(final String entityID) {
        progressBar.setVisibility(View.VISIBLE);

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(getActivity());

        requestServer(mCompositeDisposable,
                getEntitySpecificObservable(spHelper.getUUID(),
                        spHelper.getAuthToken(),
                        entityID),
                getActivity(),
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        progressBar.setVisibility(View.GONE);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        progressBar.setVisibility(View.GONE);

                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                entitySpecificData = parseEntitySpecificJSON(jsonObject, entityID);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        progressBar.setVisibility(View.GONE);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onCompleteCalled() {

                        GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = false;

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));

                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, entitySpecificData);
                            bundle.putInt("position", -1);

                            Intent intent = new Intent(getActivity(), FeedDescriptionActivity.class);
                            intent.putExtra(EXTRA_DATA, bundle);
                            getActivity().startActivity(intent);
                        }
                    }
                });
    }


    private void getIntentData()
    {
        mIsProfileEditable = getActivity().getIntent().getBooleanExtra(EXTRA_IS_PROFILE_EDITABLE, false);
    }

    @OnClick(R.id.buttonCreate)
    public void onCreateClicked() {
        getActivity().setResult(RESULT_OK);
        getActivity().finish();
    }

    /**
     * Method to show the royalties dialog
     */
    private void showRoyaltiesDialog()
    {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_generic, false)
                .positiveText("Okay")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@android.support.annotation.NonNull MaterialDialog dialog, @android.support.annotation.NonNull DialogAction which) {
                        dialog.dismiss();
                        mHelper.updateRoyaltyStatus(false);
                    }
                })
                .show();

        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.img_intro_royalty));
        //Set title text
        textTitle.setText(getActivity().getString(R.string.title_dialog_royalty));
        //Set description text
        textDesc.setText(getActivity().getString(R.string.text_dialog_royalty));

    }
}
