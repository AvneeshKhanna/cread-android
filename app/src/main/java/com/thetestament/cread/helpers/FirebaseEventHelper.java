package com.thetestament.cread.helpers;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_COLLAB_INVITE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_EXPLORE_TAB_SELECTED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FOLLOW_FROM_PROFILE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_POST_SAVED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_POST_UPLOADED;

/**
 * Helper class to log firebase event with appropriate data.
 */

public class FirebaseEventHelper {

    /**
     * Method to log post creation firebase event.
     *
     * @param context       Context to use.
     * @param firebaseEvent {@link com.thetestament.cread.utils.Constant#FIREBASE_EVENT_POST_SAVED},
     *                      {@link com.thetestament.cread.utils.Constant#FIREBASE_EVENT_POST_UPLOADED}
     */
    public static void logPostCreationEvent(FragmentActivity context, String firebaseEvent) {
        SharedPreferenceHelper helper = new SharedPreferenceHelper(context);
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString("uuid", helper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_POST_SAVED)) {
            firebaseAnalytics.logEvent(FIREBASE_EVENT_POST_SAVED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_POST_UPLOADED)) {
            firebaseAnalytics.logEvent(FIREBASE_EVENT_POST_UPLOADED, bundle);
        }
    }


    /**
     * Method to log Collab Invite firebase event {@link com.thetestament.cread.utils.Constant#FIREBASE_EVENT_COLLAB_INVITE_CLICKED}.
     *
     * @param context Context to use.
     */
    public static void logCollabInviteEvent(FragmentActivity context) {
        SharedPreferenceHelper helper = new SharedPreferenceHelper(context);
        Bundle bundle = new Bundle();
        bundle.putString("uuid", helper.getUUID());
        FirebaseAnalytics.getInstance(context)
                .logEvent(FIREBASE_EVENT_COLLAB_INVITE_CLICKED, bundle);
    }


    /**
     * Method to log explore tab selection firebase event {@link com.thetestament.cread.utils.Constant#FIREBASE_EVENT_EXPLORE_TAB_SELECTED}.
     *
     * @param context     Context to use.
     * @param selectedTab Selected tab name.
     */
    public static void logExploreTabSelectionEvent(FragmentActivity context, String selectedTab) {
        Bundle bundle = new Bundle();
        bundle.putString("tab_selected", selectedTab);
        FirebaseAnalytics.getInstance(context)
                .logEvent(FIREBASE_EVENT_EXPLORE_TAB_SELECTED, bundle);
    }

    /**
     * Method to log follow from profile firebase event {@link com.thetestament.cread.utils.Constant#FIREBASE_EVENT_FOLLOW_FROM_PROFILE}.
     *
     * @param context Context to use.
     * @param uuid    uuid of user to clicked follow button.
     */
    public static void logFollowFromProfileEvent(FragmentActivity context, String uuid) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", uuid);
        FirebaseAnalytics.getInstance(context).logEvent(FIREBASE_EVENT_FOLLOW_FROM_PROFILE, bundle);
    }
}
