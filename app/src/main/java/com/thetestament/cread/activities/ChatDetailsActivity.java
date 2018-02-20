package com.thetestament.cread.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ChatDetailsAdapter;
import com.thetestament.cread.adapters.ChatListAdapter;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.ChatDetailsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.adapters.ChatDetailsAdapter.VIEW_TYPE_MESSAGE_RECEIVED_VALUE;
import static com.thetestament.cread.adapters.ChatDetailsAdapter.VIEW_TYPE_MESSAGE_SENT_VALUE;
import static com.thetestament.cread.helpers.NetworkHelper.getChatDataObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_LAST_MESSAGE;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;

/**
 * AppCompat activity class to show details of  1-1 chat.
 */

public class ChatDetailsActivity extends BaseActivity {

    //region :Views binding with butter knife
    @BindView(R.id.rootView)
    RelativeLayout rootView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.btnSend)
    ImageView btnSend;
    @BindView(R.id.etWriteMessage)
    EditText etWriteMessage;
    @BindView(R.id.textRequestChat)
    TextView textRequestChat;
    @BindView(R.id.buttonFollow)
    TextView buttonFollow;
    @BindView(R.id.progressView)
    View progressView;
    //endregion

    //region :Fields and constants
    private Socket mSocket;
    private FragmentActivity mContext;
    private SharedPreferenceHelper mPreferenceHelper;

    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    List<ChatDetailsModel> mChatDetailsList = new ArrayList<>();
    ChatDetailsAdapter mAdapter;

    @State
    String mLastIndexKey = null;
    @State
    boolean mRequestMoreData;

    @State
    Bundle mBundle;

    /**
     * Flag to maintain last message update status true if last message has been updated ,false otherwise.
     */
    @State
    boolean mLastMessageUpdated = false;
    /**
     * Flag to maintain last message.
     */
    @State
    String mLastMessage = "";
    //endregion

    //region :Overridden Methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);
        //ButterKnife view binding
        ButterKnife.bind(this);
        //Method called
        initScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Remove compositeDisposable
        mCompositeDisposable.dispose();
        //Disconnect socket connection
        mSocket.disconnect();
        //Remove incoming message listener
        mSocket.off("send-message", inComingListener);
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
                //Method called
                navigateBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //Method called
        navigateBack();
    }

    //endregion

    //region :Click functionality
    @OnClick(R.id.btnSend)
    /**
     * Send button click functionality.
     * */
    void sendBtnOnClick() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to_uuid", mBundle.getString(EXTRA_CHAT_UUID));
            jsonObject.put("from_uuid", mPreferenceHelper.getUUID());
            jsonObject.put("body", etWriteMessage.getText().toString());
            jsonObject.put("chatid", mBundle.getString(EXTRA_CHAT_ID));
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
        //Send message
        mSocket.emit("send-message", jsonObject);

        // add the message to view
        addMessage(etWriteMessage.getText().toString(), VIEW_TYPE_MESSAGE_SENT_VALUE);
        //Clear edit text
        etWriteMessage.getText().clear();
        //fixme remove it before release
        notifyIncomingMessage();
    }

    /**
     * Follow button click functionality.
     */
    @OnClick(R.id.buttonFollow)
    public void onFollowClicked() {
        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(mContext
                , mCompositeDisposable
                , true
                , new JSONArray().put(mBundle.getString(EXTRA_CHAT_UUID))
                , new listener.OnFollowRequestedListener() {
                    @Override
                    public void onFollowSuccess() {
                        //Hide request text and follow button
                        textRequestChat.setVisibility(View.GONE);
                        buttonFollow.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFollowFailiure(String errorMsg) {
                        //Show error snackBar
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize this screen.
     */
    private void initScreen() {
        //Obtain context
        mContext = this;
        //Obtain shared preference reference
        mPreferenceHelper = new SharedPreferenceHelper(mContext);
        //Disable send button initially
        btnSend.setEnabled(false);
        //Retrieve intent data
        mBundle = getIntent().getBundleExtra(EXTRA_CHAT_DETAILS_DATA);
        //Set toolbar title
        getSupportActionBar().setTitle(mBundle.getString(EXTRA_CHAT_USER_NAME));

        //set query parameter
        IO.Options opts = new IO.Options();
        opts.query = "uuid=" + mPreferenceHelper.getUUID();
        {
            try {
                mSocket = IO.socket(BuildConfig.URL, opts);
            } catch (URISyntaxException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
        }

        //Set incoming message listener
        mSocket.on("send-message", inComingListener);
        //Make socket connection
        mSocket.connect();

        //Method called
        initTextWatcher(etWriteMessage);

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Set adapter
        mAdapter = new ChatDetailsAdapter(mChatDetailsList, mContext);
        recyclerView.setAdapter(mAdapter);

        //Load chat list data
        loadChatDetailData();
        //Initialize listener
        //initLoadMoreListener(mAdapter);
    }

    /**
     * Method to setup edit text textWatcher.
     *
     * @param editText EditText reference.
     */
    private void initTextWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                charSequence = charSequence.toString().trim();
                count = charSequence.length();
                //if count is zero
                if (count == 0) {
                    //Change button tint to grey
                    btnSend.setColorFilter(ContextCompat.getColor(ChatDetailsActivity.this, R.color.grey));
                    //disable button
                    btnSend.setEnabled(false);
                } else {
                    //Change button tint to color primary
                    btnSend.setColorFilter(ContextCompat.getColor(ChatDetailsActivity.this, R.color.colorPrimary));
                    //enable button
                    btnSend.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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

                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("body");
                    } catch (JSONException e) {
                        return;
                    }
                    //Method called
                    notifyIncomingMessage();
                    // add the message to view
                    addMessage(message, VIEW_TYPE_MESSAGE_RECEIVED_VALUE);
                }
            });
        }
    };

    /**
     * Add message to UI
     *
     * @param message     Message to be added.
     * @param messageType Type of message i.e incoming or outgoing
     */
    private void addMessage(String message, String messageType) {
        //set data in model
        ChatDetailsModel model = new ChatDetailsModel();
        model.setMessage(message);
        model.setChatUserType(messageType);
        //Add data to list
        mChatDetailsList.add(model);
        //Notify item insertion
        mAdapter.notifyItemInserted(mChatDetailsList.size() - 1);
        // scroll to last item in the recycler view
        recyclerView.smoothScrollToPosition(mChatDetailsList.size());

        //Update flags
        mLastMessageUpdated = true;
        mLastMessage = message;
    }

    /**
     * * Method to navigate back to previous screen
     */
    private void navigateBack() {
        //If last message has been updated
        if (mLastMessageUpdated) {
            Intent intent = getIntent();

            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_CHAT_ITEM_POSITION, mBundle.getInt(EXTRA_CHAT_ITEM_POSITION));
            bundle.putString(EXTRA_CHAT_LAST_MESSAGE, mLastMessage);
            intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);

            setResult(RESULT_OK, intent);
        }
        //Navigate back to previous screen
        finish();
    }

    /**
     * Method to play a sound when user receives a message.
     */
    private void notifyIncomingMessage() {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.track_one);
        //Listener for track completion
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
        //Play sound
        mediaPlayer.start();
    }


    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadChatDetailData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            //Show progress view
            progressView.setVisibility(View.VISIBLE);
            //Get data from server
            getChatDetailsData();
        } else {
            //Hide progressView
            progressView.setVisibility(View.GONE);
            //No connection Snack bar
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        }
    }

    /**
     * RxJava2 implementation for retrieving chat details data from server.
     */
    private void getChatDetailsData() {
        final boolean[] connectionError = {false};

        mCompositeDisposable.add(getChatDataObservableFromServer(BuildConfig.URL + "/chat-convo/load-messages"
                , mBundle.getString(EXTRA_CHAT_UUID)
                , mPreferenceHelper.getUUID()
                , mLastIndexKey)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            JSONObject mainData = jsonObject.getJSONObject("data");
                            mRequestMoreData = mainData.getBoolean("requestmore");
                            mLastIndexKey = mainData.getString("lastindexkey");
                            //chat details list
                            JSONArray chatDetailsArray = mainData.getJSONArray("messages");
                            for (int i = 0; i < chatDetailsArray.length(); i++) {
                                JSONObject dataObj = chatDetailsArray.getJSONObject(i);
                                ChatDetailsModel chatDetailsData = new ChatDetailsModel();
                                chatDetailsData.setMessage(dataObj.getString("body"));
                                chatDetailsData.setMessageID(dataObj.getString("messageid"));
                                chatDetailsData.setSenderUUID(dataObj.getString("from_uuid"));

                                if (dataObj.getString("from_uuid").equals(mPreferenceHelper.getUUID())) {
                                    chatDetailsData.setChatUserType(VIEW_TYPE_MESSAGE_SENT_VALUE);
                                } else {
                                    chatDetailsData.setChatUserType(VIEW_TYPE_MESSAGE_RECEIVED_VALUE);
                                }
                                mChatDetailsList.add(chatDetailsData);
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
                        //Error occurred
                        if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        } else if (mChatDetailsList.size() == 0) {
                            //Show no data message
                            // viewNoData.setVisibility(View.VISIBLE);
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
                            recyclerView.setLayoutAnimation(animation);
                            //Notify changes
                            mAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(mChatDetailsList.size() );
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
                                               mChatDetailsList.add(null);
                                               mAdapter.notifyItemInserted(mChatDetailsList.size() - 1);
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

        mCompositeDisposable.add(getChatDataObservableFromServer(BuildConfig.URL + "/chat-convo/load-messages"
                , mPreferenceHelper.getUUID()
                , mPreferenceHelper.getAuthToken()
                , mLastIndexKey)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Remove loading item
                        // mChatList.remove(mChatList.size() - 1);
                        //mAdapter.notifyItemRemoved(mChatList.size());
                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                mRequestMoreData = mainData.getBoolean("requestmore");
                                mLastIndexKey = mainData.getString("lastindexkey");
                                //chat details list
                                JSONArray chatDetailsArray = mainData.getJSONArray("messages");
                                for (int i = 0; i < chatDetailsArray.length(); i++) {
                                    JSONObject dataObj = chatDetailsArray.getJSONObject(i);
                                    ChatDetailsModel chatDetailsData = new ChatDetailsModel();
                                    chatDetailsData.setMessage(dataObj.getString("body"));
                                    chatDetailsData.setMessageID(dataObj.getString("messageid"));
                                    chatDetailsData.setSenderUUID(dataObj.getString("from_uuid"));

                                    if (dataObj.getString("from_uuid").equals(mPreferenceHelper.getUUID())) {
                                        chatDetailsData.setChatUserType(VIEW_TYPE_MESSAGE_SENT_VALUE);
                                    } else {
                                        chatDetailsData.setChatUserType(VIEW_TYPE_MESSAGE_RECEIVED_VALUE);
                                    }
                                     mChatDetailsList.add(chatDetailsData);
                                    //Notify changes
                                     mAdapter.notifyItemInserted(mChatDetailsList.size() - 1);
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
                        mChatDetailsList.remove(mChatDetailsList.size() - 1);
                        mAdapter.notifyItemRemoved(mChatDetailsList.size());
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
