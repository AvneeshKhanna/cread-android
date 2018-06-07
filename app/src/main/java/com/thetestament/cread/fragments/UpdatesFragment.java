package com.thetestament.cread.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.RoyaltiesActivity;
import com.thetestament.cread.adapters.UpdatesAdapter;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnServerRequestedListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.UpdatesModel;
import com.thetestament.cread.networkmanager.NotificationNetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC;
import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_UPDATES;
import static com.thetestament.cread.helpers.FeedHelper.parseEntitySpecificJSON;
import static com.thetestament.cread.helpers.NetworkHelper.getEntitySpecificObservable;
import static com.thetestament.cread.helpers.NetworkHelper.getUpdateUnreadObservable;
import static com.thetestament.cread.helpers.NetworkHelper.getUpdatesObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_FROM_UPDATES_COMMENT_MENTION;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FB_FRIEND;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_GENERAL;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_HATSOFF;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_TOP_POST;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_PROFILE_MENTION_POST;


/**
 * Fragment class which shows notification.
 */
public class UpdatesFragment extends Fragment {

    // Required empty public constructor
    public UpdatesFragment() {
    }

    //region Views binding with Butter knife
    @BindView(R.id.view_no_notifications)
    LinearLayout viewNoNotification;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.viewProgress)
    ProgressBar progressBar;
    @BindView(R.id.rootView)
    RelativeLayout rootView;
    //endregion

    //region :Fields and constants
    private Unbinder unbinder;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private SharedPreferenceHelper mHelper;

    private FeedModel feedData;
    List<UpdatesModel> mDataList = new ArrayList<>();
    UpdatesAdapter mAdapter;

    /**
     * Flag to maintain last index key value for pagination.
     */
    @State
    String mLastIndexKey;

    /**
     * Flag to maintain whether the next set of data is available or not.
     */
    @State
    boolean mRequestMoreData = false;
    //endregion

    //region :Overridden methods
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mHelper = new SharedPreferenceHelper(getActivity());
        //set indicator status
        mHelper.setNotifIndicatorStatus(false);
        //Inflate this view
        return inflater.inflate(R.layout.fragment_updates
                , container
                , false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        //load Data here
        initScreen();
        //Method called
        updateNotificationSeenStatus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mCompositeDisposable.dispose();
    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize view for this screen.
     */
    private void initScreen() {
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //Set adapter
        mAdapter = new UpdatesAdapter(mDataList, getActivity());
        recyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Clear data
                mDataList.clear();
                //Notify for changes
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
                //set last index key to null
                mLastIndexKey = null;
                // hide no posts view
                viewNoNotification.setVisibility(View.GONE);
                //For loading the notifications
                getUpdatesData();
            }
        });
        //For loading the notifications
        getUpdatesData();
        // initialize listeners
        initLoadMoreListener();
        initNotificationClickedListener();
    }

    /**
     * Initialize load more listener.
     */
    private void initLoadMoreListener() {
        mAdapter.setNotificationsLoadMoreListener(new listener.onNotificationsLoadMore() {
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
                    loadMoreNotificationsData();
                }

            }
        });
    }

    /**
     * Notification click functionality.
     */
    private void initNotificationClickedListener() {

        mAdapter.setNotificationItemClick(new listener.NotificationItemClick() {
            @Override
            public void onNotificationClick(UpdatesModel updatesModel, int position) {
                // Method called
                updateUnread(updatesModel, position);

                switch (updatesModel.getCategory()) {
                    case NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                        openProfileScreen(updatesModel);
                        break;
                    case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:
                        // gets feed details and opens details screen
                        getFeedDetails(updatesModel.getEntityID(), false);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_HATSOFF:
                        // gets feed details and opens details screen
                        getFeedDetails(updatesModel.getEntityID(), false);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_COMMENT:
                        // gets feed details and opens details screen
                        getFeedDetails(updatesModel.getEntityID(), false);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER:
                        // gets feed details and opens details screen
                        getFeedDetails(updatesModel.getEntityID(), false);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_TOP_POST:
                        // gets feed details and opens details screen
                        getFeedDetails(updatesModel.getEntityID(), false);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_BUY:
                        // open royalties screen
                        startActivity(new Intent(getActivity(), RoyaltiesActivity.class));
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                        // do nothing since it is not persistable and not present here
                        break;

                    case NOTIFICATION_CATEGORY_PROFILE_MENTION_POST:
                        // gets feed details and opens details screen
                        getFeedDetails(updatesModel.getEntityID(), false);
                        break;
                    case NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT:
                        // gets feed details and opens details screen
                        getFeedDetails(updatesModel.getEntityID(), true);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_FB_FRIEND:
                        // open profile screen
                        openProfileScreen(updatesModel);
                    default:
                        break;
                }
            }
        });
    }


    /**
     * Get updates data from server.
     */
    private void getUpdatesData() {

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable
                , getUpdatesObservable(mHelper.getUUID()
                        , mHelper.getAuthToken()
                        , mLastIndexKey)
                , getActivity()
                , new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        swipeRefreshLayout.setRefreshing(false);
                        //No connection Snack bar
                        showSnackBar(getString(R.string.error_msg_no_connection), false);
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
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UpdatesFragment");
                            connectionError[0] = true;
                        }

                    }


                    @Override
                    public void onErrorCalled(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "UpdatesFragment");
                        //Show Snack bar
                        showSnackBar(getString(R.string.error_msg_server), false);
                    }

                    @Override
                    public void onCompleteCalled() {
                        //Dismiss progress indicator
                        swipeRefreshLayout.setRefreshing(false);

                        GET_RESPONSE_FROM_NETWORK_UPDATES = false;

                        // Token status invalid
                        if (tokenError[0]) {
                            //Show Snack bar
                            showSnackBar(getString(R.string.error_msg_invalid_token), false);
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            //Show Snack bar
                            showSnackBar(getString(R.string.error_msg_internal), false);

                        } else if (mDataList.size() == 0) {
                            viewNoNotification.setVisibility(View.VISIBLE);
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
                            recyclerView.setLayoutAnimation(animation);
                            mAdapter.notifyDataSetChanged();
                        }

                    }
                });
    }

    /**
     * Methods to retrieve next set of data.
     */
    private void loadMoreNotificationsData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        requestServer(mCompositeDisposable
                , getUpdatesObservable(mHelper.getUUID()
                        , mHelper.getAuthToken()
                        , mLastIndexKey)
                , getActivity()
                , new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        //No connection Snack bar
                        showSnackBar(getString(R.string.error_msg_no_connection), true);
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
                                // add data to dat list
                                parseJSONData(jsonObject, true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UpdatesFragment");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        //Remove loading item
                        mDataList.remove(mDataList.size() - 1);
                        //Notify changes
                        mAdapter.notifyItemRemoved(mDataList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "UpdatesFragment");
                        //Server error Snack bar
                        showSnackBar(getString(R.string.error_msg_server), true);

                    }

                    @Override
                    public void onCompleteCalled() {
                        // Token status invalid
                        if (tokenError[0]) {
                            showSnackBar(getString(R.string.error_msg_invalid_token), true);
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            showSnackBar(getString(R.string.error_msg_internal), true);
                        } else {
                            //Notify changes
                            mAdapter.setLoaded();
                        }
                    }
                });
    }

    private void updateUnread(final UpdatesModel updatesModel, final int position) {
        requestServer(mCompositeDisposable
                , getUpdateUnreadObservable(mHelper.getUUID(), mHelper.getAuthToken(), updatesModel.getUpdateID())
                , getActivity()
                , new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {
                        updatesModel.setUnread(true);
                        mAdapter.notifyItemChanged(position);
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {

                                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));

                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");

                                if (mainData.getString("status").equals("done")) {

                                    // get reponse from network
                                    GET_RESPONSE_FROM_NETWORK_UPDATES = true;
                                }
                            }

                        } catch (JSONException e) {


                            updatesModel.setUnread(true);
                            mAdapter.notifyItemChanged(position);

                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UpdatesFragment");
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UpdatesFragment");
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        updatesModel.setUnread(true);
                        mAdapter.notifyItemChanged(position);

                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "UpdatesFragment");
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onCompleteCalled() {
                        // do nothing
                    }
                });
    }


    private void parseJSONData(JSONObject jsonObject, boolean isLoadMore) throws JSONException {

        JSONObject mainData = jsonObject.getJSONObject("data");
        mRequestMoreData = mainData.getBoolean("requestmore");
        mLastIndexKey = mainData.getString("lastindexkey");
        //FeedArray list
        JSONArray itemsArray = mainData.getJSONArray("items");

        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject dataObj = itemsArray.getJSONObject(i);

            {
                // process data only if category is valid
                if (isValidCategory(dataObj.getString("category"))) {
                    UpdatesModel updatesData = new UpdatesModel();


                    updatesData.setTimeStamp(dataObj.getString("regdate"));
                    updatesData.setUpdateID(dataObj.getString("updateid"));
                    updatesData.setUnread(dataObj.getBoolean("unread"));
                    updatesData.setCategory(dataObj.getString("category"));
                    updatesData.setActorID(dataObj.getString("actor_uuid"));
                    updatesData.setEntityID(dataObj.getString("entityid"));
                    updatesData.setActorImage(dataObj.getString("actor_profilepicurl"));
                    updatesData.setEntityImage(dataObj.getString("entityurl"));
                    updatesData.setOtherCollaborator(dataObj.getBoolean("other_collaborator"));
                    updatesData.setContentType(dataObj.getString("type"));
                    updatesData.setProductType(dataObj.getString("producttype"));
                    updatesData.setActorName(dataObj.getString("actorname"));


                    mDataList.add(updatesData);


                    if (isLoadMore) {
                        //Notify item changes
                        mAdapter.notifyItemInserted(mDataList.size() - 1);
                    }
                }
            }
        }
    }


    private boolean isValidCategory(String category) {
        boolean isValid = true;

        switch (category) {
            case NOTIFICATION_CATEGORY_CREAD_BUY:
                break;
            case NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                break;
            case NOTIFICATION_CATEGORY_CREAD_HATSOFF:
                break;
            case NOTIFICATION_CATEGORY_CREAD_COMMENT:
                break;
            case NOTIFICATION_CATEGORY_CREAD_COMMENT_OTHER:
                break;
            case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:
                break;
            case NOTIFICATION_CATEGORY_PROFILE_MENTION_COMMENT:
                break;
            case NOTIFICATION_CATEGORY_PROFILE_MENTION_POST:
                break;
            case NOTIFICATION_CATEGORY_CREAD_FB_FRIEND:
                break;
            default:
                isValid = false;
        }


        return isValid;
    }


    private void openProfileScreen(UpdatesModel updatesModel) {
        //Method called
        IntentHelper.openProfileActivity(getActivity()
                , updatesModel.getActorID());
    }


    /**
     * RxJava2 implementation for retrieving feed details
     *
     * @param entityID
     */
    private void getFeedDetails(final String entityID, boolean shouldScrollFeedDesc) {
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
                                feedData = parseEntitySpecificJSON(jsonObject, entityID);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "UpdatesFragment");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {

                        progressBar.setVisibility(View.GONE);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "UpdatesFragment");
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
                            bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, feedData);
                            bundle.putInt("position", -1);
                            bundle.putBoolean(EXTRA_FROM_UPDATES_COMMENT_MENTION, true);

                            Intent intent = new Intent(getActivity(), FeedDescriptionActivity.class);
                            intent.putExtra(EXTRA_DATA, bundle);
                            getActivity().startActivity(intent);
                        }
                    }
                });
    }


    /**
     * Method to update notification seen status on server for 'Updates Screen'.
     */
    private void updateNotificationSeenStatus() {
        NotificationNetworkManager.updateUpdatesSeenStatus(getActivity(), mCompositeDisposable
                , new NotificationNetworkManager.OnUpdatesSeenUpdateListener() {
                    @Override
                    public void onSuccess() {
                        //Update flag
                        mHelper.setNotifIndicatorStatus(false);
                    }

                    @Override
                    public void onFailure(String errorMsg) {

                    }
                });


    }


    /**
     * Method to show snack bar with reload option.
     */
    private void showSnackBar(String message, final boolean isLoadMore) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("Reload", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isLoadMore) {
                            loadMoreNotificationsData();
                        } else {
                            swipeRefreshLayout.setRefreshing(true);
                            getUpdatesData();
                        }
                    }
                })
                .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                .show();
    }
}
