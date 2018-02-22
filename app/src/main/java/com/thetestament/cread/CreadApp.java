package com.thetestament.cread;

import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.androidnetworking.AndroidNetworking;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import java.util.List;

import io.smooch.core.CardSummary;
import io.smooch.core.Conversation;
import io.smooch.core.ConversationEvent;
import io.smooch.core.InitializationStatus;
import io.smooch.core.LoginResult;
import io.smooch.core.LogoutResult;
import io.smooch.core.Message;
import io.smooch.core.MessageAction;
import io.smooch.core.MessageUploadStatus;
import io.smooch.core.PaymentStatus;
import io.smooch.core.Settings;
import io.smooch.core.Smooch;
import io.smooch.core.SmoochCallback;
import io.smooch.core.SmoochConnectionStatus;
import pl.tajchert.nammu.Nammu;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class CreadApp extends MultiDexApplication {

    private static CreadApp singleTone;

    // to determine whether data for the following screens
    // is to be restored from cache or from network
    public static boolean GET_RESPONSE_FROM_NETWORK_MAIN = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_EXPLORE = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_ME = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_FIND_FRIENDS = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_FOLLOWING = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_HATSOFF = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_INSPIRATION = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_ENTITY_SPECIFIC = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_COLLABORATION_DETAILS = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_COMMENTS = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_UPDATES = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_CHAT_LIST = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_CHAT_REQUEST = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_CHAT_DETAILS = false;

    // for picasso image loading
    public static boolean IMAGE_LOAD_FROM_NETWORK_ME = false;
    public static boolean IMAGE_LOAD_FROM_NETWORK_FEED_DESCRIPTION = false;

    /**
     * Method to return singleton instance of this class.
     */
    public static CreadApp getSingleTone() {
        return singleTone;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleTone = this;
        //For calligraphy
        initCalligraphy();
        //Networking library initialization
        AndroidNetworking.initialize(getApplicationContext());
        //Permission helper library initialization
        Nammu.init(getApplicationContext());
        //For vector drawable
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //For smooch
        initSmooch();
    }

    /**
     * Method to configure/initialize Calligraphy library for global use.
     */
    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/HelveticaNeueMedium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    /**
     * Method to initialize smooch.
     */
    private void initSmooch() {
        Settings settings = new Settings("5a65b0084cc508004ba2eba7");

        settings.setFileProviderAuthorities(BuildConfig.APPLICATION_ID + ".provider");
        Smooch.init(this, settings, new SmoochCallback() {
            @Override
            public void run(Response response) {
            }
        });

        final SharedPreferenceHelper helper = new SharedPreferenceHelper(getApplicationContext());

        Conversation.Delegate delegate = new Conversation.Delegate() {
            @Override
            public void onMessagesReceived(Conversation conversation, List<Message> list) {

            }

            @Override
            public void onUnreadCountChanged(Conversation conversation, int i) {
                //if count is greater than zero
                if (i > 0) {
                    //Update status
                    helper.setChatMsgReadStatus(false);
                }
            }

            @Override
            public void onMessagesReset(Conversation conversation, List<Message> list) {

            }

            @Override
            public void onMessageSent(Message message, MessageUploadStatus messageUploadStatus) {

            }

            @Override
            public void onConversationEventReceived(ConversationEvent conversationEvent) {

            }

            @Override
            public void onInitializationStatusChanged(InitializationStatus initializationStatus) {

            }

            @Override
            public void onLoginComplete(LoginResult loginResult) {

            }

            @Override
            public void onLogoutComplete(LogoutResult logoutResult) {

            }

            @Override
            public void onPaymentProcessed(MessageAction messageAction, PaymentStatus paymentStatus) {

            }

            @Override
            public boolean shouldTriggerAction(MessageAction messageAction) {
                return false;
            }

            @Override
            public void onCardSummaryLoaded(CardSummary cardSummary) {

            }

            @Override
            public void onSmoochConnectionStatusChanged(SmoochConnectionStatus smoochConnectionStatus) {

            }

            @Override
            public void onSmoochShown() {
                //Update  status
                helper.setChatMsgReadStatus(true);
            }

            @Override
            public void onSmoochHidden() {

            }
        };
        //Set delegate
        Smooch.getConversation().setDelegate(delegate);
    }
}
