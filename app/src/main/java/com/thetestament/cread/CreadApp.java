package com.thetestament.cread;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.androidnetworking.AndroidNetworking;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import java.net.URISyntaxException;

import io.fabric.sdk.android.Fabric;
import io.socket.client.IO;
import io.socket.client.Socket;
import pl.tajchert.nammu.Nammu;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class CreadApp extends MultiDexApplication {

    private static CreadApp singleTone;
    private static io.socket.client.Socket mSocket;
    private static boolean isChatDetailsVisible = false;

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
    public static boolean GET_RESPONSE_FROM_NETWORK_FEATURED_ARTISTS = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_VIEW_LONG_SHORT = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_RECOMMENDED_ARTISTS = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_MORE_POSTS = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_HASHTAG_OF_THE_DAY = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_HASHTAG_SUGGETION = false;
    public static boolean GET_RESPONSE_FROM_NETWORK_EXPLORE_CATEGORY = false;

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

        //Method called
        initCrashLytics();
        //For calligraphy
        initCalligraphy();
        //Networking library initialization
        AndroidNetworking.initialize(getApplicationContext());
        //Permission helper library initialization
        Nammu.init(getApplicationContext());
        //For vector drawable
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //For socket io
        initSocketIo(getApplicationContext());
        //initialize fresco
        Fresco.initialize(this);
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
     * Method to initialize  CrashLytics.
     */
    private void initCrashLytics() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();

        Fabric.with(this
                , new Crashlytics.Builder()
                        .core(crashlyticsCore)
                        .build());
    }


    /**
     * Method to initialize socket io connection for  global use
     */
    public static void initSocketIo(Context context) {
        //set query parameter
        IO.Options opts = new IO.Options();
        opts.forceNew = false;
        opts.query = "uuid=" + new SharedPreferenceHelper(context).getUUID();
        {
            try {
                mSocket = IO.socket(BuildConfig.URL, opts);
            } catch (URISyntaxException e) {
                Crashlytics.logException(e);
                Crashlytics.setString("className", "CreadApp");
                e.printStackTrace();
            }
        }
    }


    /**
     * Method to return Socket io instance
     */
    public static Socket getSocketIo() {
        return mSocket;
    }

    public static boolean isChatDetailsVisible() {
        return isChatDetailsVisible;
    }

    public static void setChatDetailsVisible(boolean chatDetailsVisible) {
        isChatDetailsVisible = chatDetailsVisible;
    }

}
