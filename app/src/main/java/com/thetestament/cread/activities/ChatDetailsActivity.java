package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.ChatDetailsAdapter;
import com.thetestament.cread.helpers.FollowHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.ChatDetailsModel;
import com.thetestament.cread.utils.NotificationUtil;
import com.thetestament.cread.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
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

import static android.view.View.VISIBLE;
import static com.thetestament.cread.adapters.ChatDetailsAdapter.VIEW_TYPE_MESSAGE_RECEIVED_VALUE;
import static com.thetestament.cread.adapters.ChatDetailsAdapter.VIEW_TYPE_MESSAGE_SENT_VALUE;
import static com.thetestament.cread.helpers.NetworkHelper.getChatDataObservableFromServer;
import static com.thetestament.cread.helpers.NetworkHelper.getNetConnectionStatus;
import static com.thetestament.cread.helpers.NetworkHelper.getupdateChatReadStatusObservable;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_LIST;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_PROFILE;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_REQUEST;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_FOLLOW_STATUS;
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
    @BindView(R.id.chatRequestContainer)
    LinearLayout chatRequestContainer;
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

    Menu menu;

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
    @State
    String mChatId;

    /**
     * Flag to maintain whether this user is following the receiver or not. True if following false otherwise.
     */
    @State
    boolean mIsFollowingReceiver;
    //endregion

    //region :Overridden Methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);
        //ButterKnife view binding
        ButterKnife.bind(this);
        //Method called
        initScreen(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Method called
        initScreen(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Method call
        initSocketConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Disconnect socket connection
        mSocket.disconnect();
        //Remove incoming message listener
        mSocket.off("send-message", inComingListener);
        mSocket.off(Socket.EVENT_ERROR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Remove compositeDisposable
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
        //inflate this menu
        getMenuInflater().inflate(R.menu.menu_chat_details, menu);
        //Obtain reference of this menu
        this.menu = menu;
        //Method called
        updateMenuTitleText(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Method called
                navigateBack();
                return true;
            case R.id.action_follow_or_block:
                //User is already following user show dialog
                if (mIsFollowingReceiver) {
                    //Show unfollow confirmation dialog
                    showUnFollowConfirmationDialog();
                } else {
                    //Method called
                    updateFollowStatus();
                }
                return true;
            case R.id.action_toggle_chat_sound:
                if (mPreferenceHelper.isChatSoundEnabled()) {
                    //Update status
                    mPreferenceHelper.updateChatSoundEnableStatus(false);
                    //Update title text
                    item.setTitle("Enable chat sound");
                } else {
                    //Update status
                    mPreferenceHelper.updateChatSoundEnableStatus(true);
                    //Update title text
                    item.setTitle("Disable chat sound");
                }
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

    /**
     * Send button click functionality.
     */
    @OnClick(R.id.btnSend)
    void sendBtnOnClick() {
        //If user is following the receiver
        if (mIsFollowingReceiver) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("to_uuid", mBundle.getString(EXTRA_CHAT_UUID));
                jsonObject.put("from_uuid", mPreferenceHelper.getUUID());
                jsonObject.put("body", etWriteMessage.getText().toString().trim());
                jsonObject.put("chatid", mBundle.getString(EXTRA_CHAT_ID));
                jsonObject.put("from_name", mPreferenceHelper.getFirstName() + " " + mPreferenceHelper.getLastName());
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
            //Send message
            mSocket.emit("send-message", jsonObject);

            // add the message to view
            addMessage(etWriteMessage.getText().toString().trim(), VIEW_TYPE_MESSAGE_SENT_VALUE);
            //Clear edit text
            etWriteMessage.getText().clear();
            //Play sound track
            NotificationUtil.notifyNewMessage(mContext, mPreferenceHelper);
        } else {
            ViewHelper.getSnackBar(rootView
                    , "Follow " + mBundle.getString(EXTRA_CHAT_USER_NAME) + " to chat");
        }

    }

    /**
     * Follow button click functionality.
     */
    @OnClick(R.id.buttonFollow)
    void onFollowClicked() {
        //Method called
        updateFollowStatus();
    }
    //endregion

    //region :Private methods

    /**
     * Method to update menu title text depending upon the condition.
     *
     * @param menu Menu for this activity
     */
    private void updateMenuTitleText(Menu menu) {
        //if this user is following receiver
        if (mIsFollowingReceiver) {
            //Change menu title
            menu.findItem(R.id.action_follow_or_block).setTitle("Unfollow");
        } else {
            //Change menu title
            menu.findItem(R.id.action_follow_or_block).setTitle("Follow");
        }
        //if sound is enabled for chat
        if (mPreferenceHelper.isChatSoundEnabled()) {
            //Change title text
            menu.findItem(R.id.action_toggle_chat_sound).setTitle("Disable chat sound");
        } else {
            //Change title text
            menu.findItem(R.id.action_toggle_chat_sound).setTitle("Enable chat sound");
        }
    }

    /**
     * Method to initialize this screen.
     */
    private void initScreen(Intent newIntent) {
        //Obtain context
        mContext = this;
        //Obtain shared preference reference
        mPreferenceHelper = new SharedPreferenceHelper(mContext);
        //Disable send button initially
        btnSend.setEnabled(false);

        //Method called
        retrieveIntentData(newIntent);
        initTextWatcher(etWriteMessage);

        //Set layout manger for recyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //Set adapter
        mAdapter = new ChatDetailsAdapter(mChatDetailsList, mContext);
        recyclerView.setAdapter(mAdapter);

        //Load chat list data
        loadChatDetailData();
        //Initialize listener
        initLoadMoreListener(mAdapter);
    }

    /**
     * Method to retrieve intent data and perform required operation.
     */
    private void retrieveIntentData(Intent newIntent) {
        //Retrieve intent data
        mBundle = newIntent.getBundleExtra(EXTRA_CHAT_DETAILS_DATA);
        mChatId = mBundle.getString(EXTRA_CHAT_ID);
        //Set toolbar title
        getSupportActionBar().setTitle(mBundle.getString(EXTRA_CHAT_USER_NAME));
        //Update text
        textRequestChat.setText("Follow " + mBundle.getString(EXTRA_CHAT_USER_NAME) + " to chat ");

        //if user is not following the receiver
        if (mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_REQUEST)) {
            //Toggle visibility
            chatRequestContainer.setVisibility(VISIBLE);
            //update flag
            mIsFollowingReceiver = false;
        } else {
            //update flag
            mIsFollowingReceiver = true;
        }
        //if this is not called from EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_PROFILE screen
        if (!mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_PROFILE)) {
            //Method called
            updateChatReadStatus(mChatId, mPreferenceHelper.getUUID(), mPreferenceHelper.getAuthToken());
        }
        //if this screen called from EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION screen
        if (mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION)) {
            //Update flag in sharedPreference
            mPreferenceHelper.setPersonalChatIndicatorStatus(false);
        }
    }

    /**
     * Method to initialize socket for real time messaging.
     */
    private void initSocketConnection() {
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

        mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //Show error message
                ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
            }
        });
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
                    String chatID;
                    try {
                        message = data.getString("body");
                        chatID = data.getString("chatid");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        return;
                    }
                    if (mChatId.equals(chatID)) {
                        //Play new message sound
                        NotificationUtil.notifyNewMessage(mContext, mPreferenceHelper);
                        // add the message to view
                        addMessage(message, VIEW_TYPE_MESSAGE_RECEIVED_VALUE);
                    } else {
                        try {
                            NotificationUtil.buildNotificationForPersonalChat(mContext
                                    , data.getString("from_uuid")
                                    , data.getString("from_name")
                                    , chatID
                                    , message
                                    , mPreferenceHelper
                                    , data.getString("from_profilepicurl"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                        }
                    }
                    //Update flags
                    CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS = true;
                    CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST = true;
                }
            });
        }
    };


    /**
     * Method to show confirmation dialog when user tries to unfollow the receiver.
     */
    private void showUnFollowConfirmationDialog() {
        new MaterialDialog.Builder(mContext)
                .content("Do you want to unfollow " + mBundle.getString(EXTRA_CHAT_USER_NAME) + "? This will move the chat to the chat-requests section")
                .positiveText(R.string.text_unfollow)
                .negativeText(R.string.text_no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss this dialog
                        dialog.dismiss();
                        //Method called
                        updateFollowStatus();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss this dialog
                        dialog.dismiss();
                    }
                })
                .build().show();
    }

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
        model.setTimeStamp(TimeUtils.getISO8601StringForDate(new Date()));
        //Add data to list
        mChatDetailsList.add(model);
        //Notify item insertion
        mAdapter.notifyItemInserted(mChatDetailsList.size());
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
        if (mLastMessageUpdated && mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_LIST)) {
            Intent intent = getIntent();

            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_CHAT_ITEM_POSITION, mBundle.getInt(EXTRA_CHAT_ITEM_POSITION));
            bundle.putString(EXTRA_CHAT_LAST_MESSAGE, mLastMessage);
            bundle.putBoolean(EXTRA_CHAT_FOLLOW_STATUS, mIsFollowingReceiver);
            intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);

            setResult(RESULT_OK, intent);

            //Update flag
            CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST = true;
        } else if (!mIsFollowingReceiver && mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_LIST)) {
            Intent intent = getIntent();

            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_CHAT_ITEM_POSITION, mBundle.getInt(EXTRA_CHAT_ITEM_POSITION));
            bundle.putString(EXTRA_CHAT_LAST_MESSAGE, mLastMessage);
            bundle.putBoolean(EXTRA_CHAT_FOLLOW_STATUS, mIsFollowingReceiver);
            intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);

            setResult(RESULT_OK, intent);

            //Update flag
            CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST = true;
        }

        //if this screen was opened by click of notification
        if (mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_NOTIFICATION)) {
            //Method called
            NotificationUtil.getNotificationBackButtonBehaviour(ChatDetailsActivity.this);
        }
        //Navigate back to previous screen
        finish();
    }

    /**
     * Method to update chat read status on server.
     *
     * @param chatID  chatID of this conversation
     * @param authKey Authentication token of user.
     * @UUID UUID of the user.
     */
    private void updateChatReadStatus(String chatID, String UUID, String authKey) {
        mCompositeDisposable.add(getupdateChatReadStatusObservable(BuildConfig.URL + "/chat-list/mark-as-read"
                , UUID
                , authKey
                , chatID)
                //Run on a background thread
                .subscribeOn(Schedulers.io())
                //Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<JSONObject>() {
                    @Override
                    public void onNext(JSONObject jsonObject) {
                        //Do nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        FirebaseCrash.report(e);
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        //Do nothing
                    }
                })
        );
    }

    /**
     * Method to update follow status
     */
    private void updateFollowStatus() {
        FollowHelper followHelper = new FollowHelper();
        followHelper.updateFollowStatus(mContext
                , mCompositeDisposable
                , !mIsFollowingReceiver
                , new JSONArray().put(mBundle.getString(EXTRA_CHAT_UUID))
                , new listener.OnFollowRequestedListener() {
                    @Override
                    public void onFollowSuccess() {
                        //Update flag
                        mIsFollowingReceiver = !mIsFollowingReceiver;
                        //Hide request text and follow button
                        chatRequestContainer.setVisibility(View.GONE);
                        //Method called
                        updateMenuTitleText(menu);
                        //Method called
                        showFollowStatusSnackBar(mIsFollowingReceiver);
                        //Update flags
                        CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_REQUEST = true;
                        CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_LIST = true;

                        //This screen is opened from chat request screen
                        if (mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_REQUEST)) {
                            //Set result ok
                            setResult(RESULT_OK);
                        }
                        //This screen is opened from user profile
                        if (mBundle.getString(EXTRA_CHAT_DETAILS_CALLED_FROM)
                                .equals(EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_PROFILE)) {
                            Intent intent = getIntent();

                            Bundle bundle = new Bundle();
                            bundle.putBoolean(EXTRA_CHAT_FOLLOW_STATUS, mIsFollowingReceiver);
                            intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);
                            //Set result ok
                            setResult(RESULT_OK, intent);

                            //Update flags
                            CreadApp.GET_RESPONSE_FROM_NETWORK_ME = true;
                        }
                    }

                    @Override
                    public void onFollowFailiure(String errorMsg) {
                        //Show error snackBar
                        ViewHelper.getSnackBar(rootView, errorMsg);
                    }
                });
    }

    /**
     * Method to show snack bar when follow status changes.
     *
     * @param status True if followed false otherwise.
     */
    private void showFollowStatusSnackBar(boolean status) {
        if (status) {
            ViewHelper.getSnackBar(rootView, "Followed " + mBundle.getString(EXTRA_CHAT_USER_NAME));
        } else {
            ViewHelper.getSnackBar(rootView, "Unfollowed " + mBundle.getString(EXTRA_CHAT_USER_NAME));
        }
    }

    /**
     * This method loads data from server if user device is connected to internet.
     */
    private void loadChatDetailData() {
        // if user device is connected to net
        if (getNetConnectionStatus(this)) {
            //Show progress view
            progressView.setVisibility(VISIBLE);
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
                                chatDetailsData.setTimeStamp(dataObj.getString("regdate"));
                                mChatId = dataObj.getString("chatid");

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
                        e.printStackTrace();
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
                        } else {
                            //Apply 'Slide Up' animation
                            int resId = R.anim.layout_animation_from_bottom;
                            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, resId);
                            recyclerView.setLayoutAnimation(animation);

                            // show header
                            if (mRequestMoreData) {
                                new Handler().post(new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           mAdapter.setLoadMoreViewVisibility((ChatDetailsAdapter.HeaderViewHolder) recyclerView.
                                                                   findViewHolderForAdapterPosition(0), View.VISIBLE);
                                                       }
                                                   }
                                );
                            }
                            //Notify changes
                            mAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(mChatDetailsList.size());
                            //Update flag
                            CreadApp.GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS = false;

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
    private void initLoadMoreListener(ChatDetailsAdapter adapter) {

        //Load more data listener
        adapter.setLoadMoreListener(new listener.OnChatDetailsLoadMoreListener() {
            @Override
            public void onLoadMore() {
                // hide load more and show loading icon
                mAdapter.setLoadMoreViewVisibility((ChatDetailsAdapter.HeaderViewHolder) recyclerView.
                        findViewHolderForAdapterPosition(0), View.GONE);
                mAdapter.setLoadingIconVisibility((ChatDetailsAdapter.HeaderViewHolder) recyclerView.
                        findViewHolderForAdapterPosition(0), View.VISIBLE);
                //Load new set of data
                loadMoreData();
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
                        //Remove loading item
                        try {
                            JSONObject mainData = jsonObject.getJSONObject("data");
                            mRequestMoreData = mainData.getBoolean("requestmore");
                            mLastIndexKey = mainData.getString("lastindexkey");

                            // hide loading icon  and show load more image
                            if (mRequestMoreData) {
                                mAdapter.setLoadMoreViewVisibility((ChatDetailsAdapter.HeaderViewHolder) recyclerView.
                                        findViewHolderForAdapterPosition(0), View.VISIBLE);

                            }
                            mAdapter.setLoadingIconVisibility((ChatDetailsAdapter.HeaderViewHolder) recyclerView.
                                    findViewHolderForAdapterPosition(0), View.GONE);
                            //chat details list
                            JSONArray chatDetailsArray = mainData.getJSONArray("messages");
                            for (int i = 0; i < chatDetailsArray.length(); i++) {
                                JSONObject dataObj = chatDetailsArray.getJSONObject(i);
                                ChatDetailsModel chatDetailsData = new ChatDetailsModel();
                                chatDetailsData.setMessage(dataObj.getString("body"));
                                chatDetailsData.setMessageID(dataObj.getString("messageid"));
                                chatDetailsData.setSenderUUID(dataObj.getString("from_uuid"));
                                chatDetailsData.setTimeStamp(dataObj.getString("regdate"));

                                if (dataObj.getString("from_uuid").equals(mPreferenceHelper.getUUID())) {
                                    chatDetailsData.setChatUserType(VIEW_TYPE_MESSAGE_SENT_VALUE);
                                } else {
                                    chatDetailsData.setChatUserType(VIEW_TYPE_MESSAGE_RECEIVED_VALUE);
                                }

                                mChatDetailsList.add(0, chatDetailsData);
                                //Notify changes
                                mAdapter.notifyItemInserted(0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // hide loading icon  and show load more image
                        if (mRequestMoreData) {
                            mAdapter.setLoadMoreViewVisibility((ChatDetailsAdapter.HeaderViewHolder) recyclerView.
                                    findViewHolderForAdapterPosition(0), View.VISIBLE);

                        }
                        mAdapter.setLoadingIconVisibility((ChatDetailsAdapter.HeaderViewHolder) recyclerView.
                                findViewHolderForAdapterPosition(0), View.GONE);
                        //Remove loading item
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
                            //recyclerView.smoothScrollToPosition(0);
                        }
                    }
                })
        );
    }

    //endregion
}
