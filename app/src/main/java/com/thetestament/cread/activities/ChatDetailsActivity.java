package com.thetestament.cread.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.models.ChatDetailsModel;

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
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.adapters.ChatDetailsAdapter.VIEW_TYPE_MESSAGE_RECEIVED_VALUE;
import static com.thetestament.cread.adapters.ChatDetailsAdapter.VIEW_TYPE_MESSAGE_SENT_VALUE;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
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
            jsonObject.put("to", mBundle.getString(EXTRA_CHAT_UUID));
            jsonObject.put("from", mPreferenceHelper.getUUID());
            jsonObject.put("body", etWriteMessage.getText().toString());
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
        //fixme
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

        //Retrieve intent data
        mBundle = getIntent().getBundleExtra(EXTRA_CHAT_DETAILS_DATA);
        //Set toolbar title
        getSupportActionBar().setTitle(mBundle.getString(EXTRA_CHAT_USER_NAME));

        //set query parameter
        IO.Options opts = new IO.Options();
        opts.query = "uuid=" + mPreferenceHelper.getUUID();
        {
            try {
                mSocket = IO.socket(BuildConfig.SOCKET_URL, opts);
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
                } else {
                    //Change button tint to color primary
                    btnSend.setColorFilter(ContextCompat.getColor(ChatDetailsActivity.this, R.color.colorPrimary));
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
    //endregion
}
