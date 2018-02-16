package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ChatListAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.ChatListModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_LAST_MESSAGE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CHAT_DETAILS;

/**
 * Appcompat activity class to show user chat list.
 */

public class ChatListActivity extends BaseActivity {

    //region :Views binding with butter knife
    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.viewNoData)
    LinearLayout viewNoData;
    @BindView(R.id.progressView)
    View progressView;
    //endregion

    //region Fields and constant
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper mHelper;

    List<ChatListModel> mChatList = new ArrayList<>();
    ChatListAdapter mAdapter;

    FragmentActivity mContext;

    @State
    String mLastIndexKey = null;
    @State
    boolean mRequestMoreData;
    //endregion

    //region :Overridden Methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        //Bind with butterKnife
        ButterKnife.bind(this);
        //Method called
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHAT_DETAILS && resultCode == RESULT_OK) {
            //Retrieve data
            Bundle bundle = data.getBundleExtra(EXTRA_CHAT_DETAILS_DATA);
            //Set last message
            mChatList.get(bundle.getInt(EXTRA_CHAT_ITEM_POSITION))
                    .setLastMessage(bundle.getString(EXTRA_CHAT_LAST_MESSAGE));
            //Notify changes
            mAdapter.notifyItemChanged(bundle.getInt(EXTRA_CHAT_ITEM_POSITION));
        }
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
    //endregion

    //region :Private methods

    /**
     * Method to initialize views and retrieve data from intent.
     */
    private void initView() {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
        //Obtain context
        mContext = this;
        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Set adapter
        mAdapter = new ChatListAdapter(mChatList, mContext);
        recyclerView.setAdapter(mAdapter);

        //Load chat list data
        loadChatListData();
        //Initialize listener
        initLoadMoreListener(mAdapter);
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadChatListData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            //Show progress view
            progressView.setVisibility(View.VISIBLE);
            //Get data from server
            getChatListData();
        } else {
            //Hide progressView
            progressView.setVisibility(View.GONE);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }


    /**
     * RxJava2 implementation for retrieving chat list data from server.
     */
    private void getChatListData() {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/chat-list/list-all"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST)
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
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //chat list
                                JSONArray chatListArray = mainData.getJSONArray("chatlist");
                                for (int i = 0; i < chatListArray.length(); i++) {
                                    JSONObject dataObj = chatListArray.getJSONObject(i);
                                    ChatListModel chatListData = new ChatListModel();
                                    chatListData.setReadStatus(dataObj.getBoolean("unread"));
                                    chatListData.setUserUID(dataObj.getString("senderuuid"));
                                    chatListData.setLastMessage(dataObj.getString("lastmessage"));
                                    chatListData.setUserName(dataObj.getString("name"));
                                    chatListData.setProfileImgUrl(dataObj.getString("profilepicurl"));
                                    mChatList.add(chatListData);
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
                        //Hide progress view
                        progressView.setVisibility(View.GONE);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress view
                        progressView.setVisibility(View.GONE);
                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mChatList.size() == 0) {
                            //Show no data message
                            viewNoData.setVisibility(View.VISIBLE);
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
                            recyclerView.setLayoutAnimation(animation);
                            //Notify changes
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                })
        );
    }

    /**
     * Initialize load more listener.
     *
     * @param adapter ChatListAdapter reference.
     */
    private void initLoadMoreListener(ChatListAdapter adapter) {

        //Load more data listener
        adapter.setLoadMoreListener(new listener.OnChatListLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //if more data is available
                if (mRequestMoreData) {
                    new Handler().post(new Runnable() {
                                           @Override
                                           public void run() {
                                               mChatList.add(null);
                                               mAdapter.notifyItemInserted(mChatList.size() - 1);
                                           }
                                       }
                    );
                    //Load new set of data
                    loadMoreData();
                }
            }
        });
    }

    /**
     * Method to retrieve next set of data from server.
     */
    private void loadMoreData() {

        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/chat-list/list-all"
                , mHelper.getUUID()
                , mHelper.getAuthToken()
                , mLastIndexKey
                , CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        mChatList.remove(mChatList.size() - 1);
                        mAdapter.notifyItemRemoved(mChatList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //chat list
                                JSONArray chatListArray = mainData.getJSONArray("chatlist");
                                for (int i = 0; i < chatListArray.length(); i++) {
                                    JSONObject dataObj = chatListArray.getJSONObject(i);
                                    ChatListModel chatListData = new ChatListModel();
                                    chatListData.setUserUID(dataObj.getString("senderuuid"));
                                    chatListData.setReadStatus(dataObj.getBoolean("unread"));
                                    chatListData.setLastMessage(dataObj.getString("lastmessage"));
                                    chatListData.setUserName(dataObj.getString("name"));
                                    chatListData.setProfileImgUrl(dataObj.getString("profilepicurl"));
                                    mChatList.add(chatListData);
                                    //Notify changes
                                    mAdapter.notifyItemInserted(mChatList.size() - 1);
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
                        mChatList.remove(mChatList.size() - 1);
                        mAdapter.notifyItemRemoved(mChatList.size());
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
    //endregion

}
