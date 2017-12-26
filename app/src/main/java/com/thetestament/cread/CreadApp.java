package com.thetestament.cread;

import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.androidnetworking.AndroidNetworking;

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


}
