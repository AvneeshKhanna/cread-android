package com.thetestament.cread.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.razorpay.Checkout;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.AboutUsActivity;
import com.thetestament.cread.activities.FindFBFriendsActivity;
import com.thetestament.cread.activities.MainActivity;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.utils.Constant.ACTION_LOG_OUT;
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

        Preference logOutItem = findPreference("log_out");
        logOutItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getLogOutDialog();
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
                //startActivity(new Intent(getActivity(), FaqActivity.class));
                return false;
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle("Settings");
    }

    /**
     * Show logout dialog
     */
    private void getLogOutDialog() {
        new MaterialDialog.Builder(getActivity())
                .title("Logout")
                .content("Are you sure you want to logout from Cread?")
                .positiveText("Logout")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //LogOut Action
                        performLogOut();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).build()
                .show();
    }

    /**
     * Method to perform log out task.
     */
    private void performLogOut() {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title("Log out in progress")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        //Retrieving fcmToken
        String fcmToken = FirebaseInstanceId.getInstance().getToken();
        //Send request to server
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("uuid", spHelper.getUUID());
            jsonObject.put("fcmtoken", fcmToken);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Rx2AndroidNetworking.post(BuildConfig.URL + "/user-access/sign-out")
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {

                        try {
                            JSONObject data = jsonObject.getJSONObject("data");

                            if (data.getString("status").equals("done")) {
                                dialog.dismiss();
                                //Resetting defaultSharedPreferences to reset settings_notifications_key
                                SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                SharedPreferences.Editor defaultPrefEditor = defaultPreferences.edit();
                                defaultPrefEditor.clear();
                                defaultPrefEditor.apply();

                                // clear shared preferences of cread
                                spHelper.clearSharedPreferences();

                                // clear Razorpay user data
                                Checkout.clearUserData(getActivity());
                                // clear facebook token
                                AccessToken.setCurrentAccessToken(null);


                                // broadcast the logout action
                                // base activity receives the broadcast and destroys all activities
                                // directs the user to Main Activity
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(ACTION_LOG_OUT);
                                getActivity().sendBroadcast(broadcastIntent);

                                startActivity(new Intent(getActivity(), MainActivity.class));
                                //To finish SettingsActivity
                                getActivity().finish();
                            } else {
                                dialog.dismiss();
                                Toast.makeText(getActivity()
                                        , getString(R.string.error_msg_server)
                                        , Toast.LENGTH_LONG)
                                        .show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);

                            dialog.dismiss();
                            Toast.makeText(getActivity()
                                    , getString(R.string.error_msg_server)
                                    , Toast.LENGTH_LONG)
                                    .show();
                        }

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                        dialog.dismiss();

                        e.printStackTrace();
                        FirebaseCrash.report(e);

                        ViewHelper.getToast(getActivity(), getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onComplete() {

                        // do nothing
                    }
                });
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
}
