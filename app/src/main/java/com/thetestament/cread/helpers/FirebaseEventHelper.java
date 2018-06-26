package com.thetestament.cread.helpers;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

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
}
