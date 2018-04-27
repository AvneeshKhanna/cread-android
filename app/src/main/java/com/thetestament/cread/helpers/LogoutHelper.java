package com.thetestament.cread.helpers;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.razorpay.Checkout;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by prakharchandna on 07/03/18.
 */

public class LogoutHelper {


    /**
     * Show logout dialog
     */
    public static void getLogOutDialog(final FragmentActivity context, final CompositeDisposable compositeDisposable) {
        new MaterialDialog.Builder(context)
                .title("Logout")
                .content("Are you sure you want to logout from Cread?")
                .positiveText("Logout")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //LogOut Action
                        performLogOut(context, compositeDisposable);
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
    public static void performLogOut(final FragmentActivity context, final CompositeDisposable compositeDisposable) {

        // check net status
        if (NetworkHelper.getNetConnectionStatus(context)) {

            final SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

            //To show the progress dialog
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
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
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(@io.reactivex.annotations.NonNull JSONObject jsonObject) {

                            try {
                                JSONObject data = jsonObject.getJSONObject("data");

                                if (data.getString("status").equals("done")) {
                                    dialog.dismiss();
                                    //Resetting defaultSharedPreferences to reset settings_notifications_key
                                    SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                    SharedPreferences.Editor defaultPrefEditor = defaultPreferences.edit();
                                    defaultPrefEditor.clear();
                                    defaultPrefEditor.apply();

                                    // clear shared preferences of cread
                                    spHelper.clearSharedPreferences();

                                    // clear Razorpay user data
                                    Checkout.clearUserData(context);
                                    // clear facebook token
                                    AccessToken.setCurrentAccessToken(null);

                                    if (GoogleSignIn.getLastSignedInAccount(context) != null) {
                                        //sign out of google
                                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                                .requestEmail()
                                                .build();
                                        // Build a GoogleSignInClient with the options specified by gso.
                                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

                                        mGoogleSignInClient.signOut();

                                    }



                                    // to remove all notifications from drawer
                                    // so user can't open them after logging out
                                    NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(context.NOTIFICATION_SERVICE);
                                    notificationManager.cancelAll();

                                    // broadcast the logout action
                                    // base activity receives the broadcast and destroys all activities
                                    // directs the user to Main Activity
                                /*Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(ACTION_LOG_OUT);
                                getActivity().sendBroadcast(broadcastIntent);*/
                                    // to close all all activities
                                    context.finishAffinity();
                                    context.startActivity(new Intent(context, MainActivity.class));
                                    //To finish SettingsActivity
                                    context.finish();
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(context
                                            , context.getString(R.string.error_msg_server)
                                            , Toast.LENGTH_LONG)
                                            .show();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(e);

                                dialog.dismiss();
                                Toast.makeText(context
                                        , context.getString(R.string.error_msg_server)
                                        , Toast.LENGTH_LONG)
                                        .show();
                            }

                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                            dialog.dismiss();

                            e.printStackTrace();
                            FirebaseCrash.report(e);

                            ViewHelper.getToast(context, context.getString(R.string.error_msg_server));

                        }

                        @Override
                        public void onComplete() {

                            // do nothing
                        }
                    });
        } else {
            ViewHelper.getToast(context, context.getString(R.string.error_msg_no_connection));
        }
    }
}
