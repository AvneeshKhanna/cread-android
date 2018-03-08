package com.thetestament.cread.fragments;

import android.content.Intent;
import android.net.Uri;
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
import com.thetestament.cread.activities.WebViewActivity;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import io.reactivex.disposables.CompositeDisposable;
import io.smooch.ui.ConversationActivity;

import static com.thetestament.cread.helpers.FeedHelper.inviteFriends;
import static com.thetestament.cread.helpers.LogoutHelper.getLogOutDialog;
import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_TITLE;
import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_URL;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_FIND_FRIENDS;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_RATE_US_CLICKED;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String KEY_SETTINGS_NOTIFICATIONS = "settings_notifications_key";
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
                inviteFriends(getActivity());
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
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(EXTRA_WEB_VIEW_URL, "file:///android_asset/" + "cread_tos.html");
                intent.putExtra(EXTRA_WEB_VIEW_TITLE, "Terms of Service");
                startActivity(intent);
                return false;
            }
        });


        Preference rateUsItem = findPreference("rate_us");
        rateUsItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                redirectToPlayStore();
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
                //Start   FAQ Screen
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(EXTRA_WEB_VIEW_URL, "http://cread.in/FAQ-users.php");
                intent.putExtra(EXTRA_WEB_VIEW_TITLE, "FAQ");
                startActivity(intent);
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
     * Method to redirect user to Cread app on google play store.
     */
    private void redirectToPlayStore() {
        //To get the package name
        String appPackageName = getContext().getPackageName();
        try {
            //To redirect to google play store
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            //if play store is not installed
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
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
                //Start smooch activity
                ConversationActivity.show(getActivity());
                return false;
            }
        });
    }
}
