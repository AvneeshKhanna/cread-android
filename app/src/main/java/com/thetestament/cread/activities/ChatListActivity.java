package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ChatListAdapter;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.ChatListModel;
import com.thetestament.cread.utils.NotificationUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.thetestament.cread.CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST;
import static com.thetestament.cread.helpers.NetworkHelper.getChatRequestCountObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getObservableFromServer;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_FOLLOW_STATUS;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_LAST_MESSAGE;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_LIST_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CHAT_DETAILS;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_CHAT_REQUEST;

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

    Socket mSocket;

    @State
    String mLastIndexKey = null;
    @State
    boolean mRequestMoreData;

    /**
     * Flag to maintain this activity foreground status
     */
    @State
    boolean mIsActivityInForeground = true;


    /**
     * Flag to maintain chat request count
     */
    @State
    int mChatRequestCount = 0;

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
        initSocketConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Update flag
        mIsActivityInForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Update flag
        mIsActivityInForeground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Dispose disposable
        mCompositeDisposable.dispose();
        //Remove incoming message listener
        mSocket.off("send-message", inComingListener);
        //Disconnect socket connection
        mSocket.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHAT_DETAILS && resultCode == RESULT_OK) {
            //Retrieve data
            Bundle bundle = data.getBundleExtra(EXTRA_CHAT_DETAILS_DATA);
            //if follow status is true
            if (bundle.getBoolean(EXTRA_CHAT_FOLLOW_STATUS)) {
                //Set last message
                mChatList.get(bundle.getInt(EXTRA_CHAT_ITEM_POSITION))
                        .setLastMessage(bundle.getString(EXTRA_CHAT_LAST_MESSAGE));
                //Notify changes
                mAdapter.notifyItemChanged(bundle.getInt(EXTRA_CHAT_ITEM_POSITION));
            } else {
                //Clear list
                mChatList.clear();
                //Notify changes
                mAdapter.notifyDataSetChanged();
                //Refresh last index key
                mLastIndexKey = null;
                //Method called
                //Notify changes
                mAdapter.setLoaded();
                getChatRequestCount();
            }
        } else if (requestCode == REQUEST_CODE_CHAT_REQUEST && resultCode == RESULT_OK) {
            // Remove data from list
            mChatList.clear();
            //Notify changes
            mAdapter.notifyDataSetChanged();
            //Refresh last index key
            mLastIndexKey = null;
            //Method called
            mAdapter.setLoaded();
            getChatRequestCount();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Navigate back to previous screen
                if (!getIntent().hasExtra(EXTRA_CHAT_LIST_CALLED_FROM)) {
                    NotificationUtil.getNotificationBackButtonBehaviour(ChatListActivity.this);
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //Navigate back to previous screen
        if (!getIntent().hasExtra(EXTRA_CHAT_LIST_CALLED_FROM)) {
            NotificationUtil.getNotificationBackButtonBehaviour(ChatListActivity.this);
        }
        finish();
    }

    //endregion

    //region :Private methods

    /**
     * Method to initialize views and retrieve data from intent.
     */
    private void initView() {
        //SharedPreference reference
        mHelper = new SharedPreferenceHelper(this);
        //Update flag in sharedPreference
        mHelper.setPersonalChatIndicatorStatus(false);
        //Obtain context
        mContext = this;
        //Set dialogParentView manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Set adapter
        mAdapter = new ChatListAdapter(mChatList, mContext);
        recyclerView.setAdapter(mAdapter);

        //Load chat request count
        getChatRequestCount();
        //Initialize load more listener
        initLoadMoreListener(mAdapter);
    }

    /**
     * Method to initialize socket for real time messaging.
     */
    private void initSocketConnection() {
        //Obtain socket reference
        //mSocket = ChatUtil.getSocket(mContext);
        mSocket = CreadApp.getSocketIo();

        //Set incoming message listener
        mSocket.on("send-message", inComingListener);

        if (getIntent().hasExtra(EXTRA_CHAT_LIST_CALLED_FROM)) {
            //Make socket connection
            if (!mSocket.connected()) {
                mSocket.connect();
            }
        }
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

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/chat-list/load"
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
                                    chatListData.setUnreadStatus(dataObj.getBoolean("unread"));
                                    chatListData.setReceiverUUID(dataObj.getString("receiveruuid"));
                                    chatListData.setLastMessage(dataObj.getString("lastmessage"));
                                    chatListData.setReceiverName(dataObj.getString("receivername"));
                                    chatListData.setProfileImgUrl(dataObj.getString("profilepicurl"));
                                    chatListData.setChatID(dataObj.getString("chatid"));
                                    chatListData.setItemType(ChatListAdapter.VIEW_TYPE_ITEM);
                                    mChatList.add(chatListData);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "ChatListActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Hide progress view
                        progressView.setVisibility(View.GONE);
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "ChatListActivity");
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

                            //Update flag
                            GET_RESPONSE_FROM_NETWORK_CHAT_LIST = false;

                            //hide no data message
                            viewNoData.setVisibility(View.INVISIBLE);
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

        mCompositeDisposable.add(getObservableFromServer(BuildConfig.URL + "/chat-list/load"
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
                                    chatListData.setUnreadStatus(dataObj.getBoolean("unread"));
                                    chatListData.setReceiverUUID(dataObj.getString("receiveruuid"));
                                    chatListData.setLastMessage(dataObj.getString("lastmessage"));
                                    chatListData.setReceiverName(dataObj.getString("receivername"));
                                    chatListData.setProfileImgUrl(dataObj.getString("profilepicurl"));
                                    chatListData.setChatID(dataObj.getString("chatid"));
                                    chatListData.setItemType(ChatListAdapter.VIEW_TYPE_ITEM);
                                    mChatList.add(chatListData);
                                    //Notify changes
                                    mAdapter.notifyItemInserted(mChatList.size() - 1);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "ChatListActivity");
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        //Remove loading item
                        mChatList.remove(mChatList.size() - 1);
                        mAdapter.notifyItemRemoved(mChatList.size());
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "ChatListActivity");
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
     * RxJava2 implementation for retrieving chat request count from server.
     */
    private void getChatRequestCount() {
        mCompositeDisposable.add(getChatRequestCountObservableFromServer(BuildConfig.URL + "/chat-list/requests-count"
                , mHelper.getUUID()
                , true)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            mChatRequestCount = jsonObject.getInt("requestcount");
                            //if request count is one
                            if (mChatRequestCount == 1) {
                                ChatListModel chatListData = new ChatListModel();
                                chatListData.setItemType(ChatListAdapter.VIEW_TYPE_HEADER);
                                chatListData.setLastMessage(mChatRequestCount + " chat request");
                                mChatList.add(0, chatListData);
                                mAdapter.notifyItemInserted(0);
                            }
                            //Request count is more than one
                            else if (mChatRequestCount > 1) {
                                ChatListModel chatListData = new ChatListModel();
                                chatListData.setItemType(ChatListAdapter.VIEW_TYPE_HEADER);
                                chatListData.setLastMessage(mChatRequestCount + " chat requests");
                                mChatList.add(0, chatListData);
                                mAdapter.notifyItemInserted(0);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "ChatListActivity");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "ChatListActivity");
                    }

                    @Override
                    public void onComplete() {
                        //Load chat list data
                        loadChatListData();
                    }
                })
        );
    }

    /**
     * Incoming message listener.
     */
    private Emitter.Listener inComingListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Method called
                        processIncomingMessage((JSONObject) args[0]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /**
     * Method to perform required operation on incoming message.
     */
    private void processIncomingMessage(JSONObject incomingData) throws JSONException {
        JSONObject data = incomingData;

        //Activity is in foreground
        if (mIsActivityInForeground) {
            for (int i = 0; i < mChatList.size(); i++) {
                if (mChatList.get(i).getItemType() == ChatListAdapter.VIEW_TYPE_ITEM) {
                    if (mChatList.get(i).getChatID().equals(data.getString("chatid"))) {
                        mChatList.get(i).setUnreadStatus(true);
                        mChatList.get(i).setLastMessage(data.getString("body"));

                        //if chat request count is zero
                        if (mChatRequestCount == 0) {
                            Collections.swap(mChatList, i, 0);
                            mAdapter.notifyItemMoved(i, 0);
                            mAdapter.notifyItemChanged(i);
                            mAdapter.notifyItemChanged(0);
                        } else {
                            Collections.swap(mChatList, i, 1);
                            mAdapter.notifyItemMoved(i, 1);
                            mAdapter.notifyItemChanged(i);
                            mAdapter.notifyItemChanged(1);
                        }
                        //Play new message sound
                        NotificationUtil.notifyNewMessage(mContext, mHelper);
                        return;
                    }
                }
            }

        }
        //Activity is not visible to user
        else {
            //if chat details is not visible
            if (!CreadApp.isChatDetailsVisible()) {
                NotificationUtil.buildNotificationForPersonalChat(mContext
                        , data.getString("from_uuid")
                        , data.getString("from_name")
                        , data.getString("chatid")
                        , data.getString("body")
                        , mHelper
                        , data.getString("from_profilepicurl"));
            }
        }
    }
    //endregion

}
