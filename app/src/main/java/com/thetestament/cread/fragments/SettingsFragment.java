package com.thetestament.cread.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.AboutUsActivity;
import com.thetestament.cread.activities.FindFBFriendsActivity;
import com.thetestament.cread.helpers.DeepLinkHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.LogoutHelper.getLogOutDialog;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FIND_FRIENDS;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_RATE_US_CLICKED;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String KEY_SETTINGS_NOTIFICATIONS = "settings_notifications_key";
    public static final String KEY_SETTINGS_HATSOFFSOUND = "settings_hatsoffsound_key";
    public static final String KEY_SETTINGS_CHATSOUND = "settings_chatsound_key";
    public static final String KEY_SETTINGS_LONGFORMSOUND = "settings_longformsound_key";
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    SharedPreferenceHelper spHelper;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preference);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        spHelper = new SharedPreferenceHelper(getActivity());

        Preference about = findPreference("settings_about_key");
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), AboutUsActivity.class));
                return false;
            }
        });

        Preference findFriends = findPreference("settings_findFriends_key");
        findFriends.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), FindFBFriendsActivity.class));
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_FIND_FRIENDS);
                return false;
            }
        });

        final Preference inviteFriends = findPreference("settings_inviteFriends_key");
        inviteFriends.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DeepLinkHelper.generateUserSpecificDeepLink(getActivity()
                        , mCompositeDisposable
                        , spHelper.getUUID()
                        , spHelper.getAuthToken());
                return false;
            }
        });

        Preference logOutItem = findPreference("log_out");
        logOutItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getLogOutDialog(getActivity(), mCompositeDisposable);
                return false;
            }
        });

        Preference tosItem = findPreference("settings_tos_key");
        tosItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Method called
                IntentHelper.openWebViewActivity(getActivity()
                        , "file:///android_asset/" + "cread_tos.html"
                        , "Terms of Service");
                return false;
            }
        });


        Preference rateUsItem = findPreference("rate_us");
        rateUsItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Method called
                IntentHelper.openPlayStore(getActivity());
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_RATE_US_CLICKED);
                return false;
            }
        });

        //FAQ onClick functionality
        Preference faqItem = findPreference("faq");
        faqItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Start FAQ Screen
                IntentHelper.openWebViewActivity(getActivity()
                        , "http://cread.in/faq"
                        , "FAQ");
                return false;
            }
        });

        //Method called
        chatWithUsOnClick();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle("Settings");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCompositeDisposable.dispose();
    }


    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     */
    private void setAnalytics(String firebaseEvent) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", spHelper.getUUID());
        if (firebaseEvent.equals(FIREBASE_EVENT_RATE_US_CLICKED)) {
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_RATE_US_CLICKED, bundle);
        } else {
            mFirebaseAnalytics.logEvent(FIREBASE_EVENT_FIND_FRIENDS, bundle);
        }
    }

    /**
     * Chat with us click functionality to launch smooch chat system.
     */
    private void chatWithUsOnClick() {
        final Preference preference = findPreference("chatWithUs");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Open chat details screen
                IntentHelper.openChatWithCreadKalakaar(getActivity());
                return false;
            }
        });
    }
}
