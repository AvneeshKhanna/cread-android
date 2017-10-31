package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookRequestError.Category;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.IntroViewPagerAdapter;
import com.thetestament.cread.helpers.IntroPageTransformerHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();
    //Array to store  sliders layout
    @BindView(R.id.dotsLayout)
    LinearLayout dotsLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.loginButton)
    LoginButton loginButton;
    @BindView(R.id.loginParent)
    RelativeLayout parentLayout;
    CallbackManager mCallbackManager;
    JSONObject graphObject;
    String phoneNo;
    MaterialDialog verifyDialog;
    private int[] mLayouts;
    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addDots(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For fullscreen display
        initFullScreen();
        //Layout for this screen
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //For sliders
        initSliders();
        // Add bottom dots
        addDots(0);
        //Initialize view pager
        initViewPager();

        mCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("email", "user_friends"));
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = loginResult.getAccessToken();
                Log.d(TAG, "onSuccess: token" + accessToken.getToken());

                Log.d(TAG, "onSuccess: user token id" + accessToken.getUserId());

                //phoneLogin();
                checkUserStatus(accessToken.getUserId());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Log In button onClick Listener
     */
    @OnClick(R.id.loginButton)
    public void logInOnClick() {

        if (!NetworkHelper.getNetConnectionStatus(MainActivity.this)) {
            ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_no_connection));
        }

    }

    /**
     * Functionality to launch TermsOfServiceActivity.
     */
    @OnClick(R.id.textTOS)
    public void showTos() {
        startActivity(new Intent(MainActivity.this
                , TermsOfServiceActivity.class));

    }

    /**
     * Method to add dots  and to change the color of dots
     *
     * @param currentPage value of current page i.e 0(zero)
     */
    private void addDots(int currentPage) {
        TextView[] dots = new TextView[mLayouts.length];
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active_main);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive_main);

        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("\u2022");   //\u2022 for dots
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    /**
     * Method to initialize sliders for the view pager.
     */
    private void initSliders() {
        mLayouts = new int[]{
                R.layout.intro_screen_one,
                R.layout.intro_screen_two,
                R.layout.intro_screen_three
        };
    }

    /**
     * Method to initialize view pager for intro tour.
     */
    private void initViewPager() {
        viewPager.setAdapter(new IntroViewPagerAdapter(mLayouts, getBaseContext()));
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setPageTransformer(false, new IntroPageTransformerHelper());
    }

    /**
     * To open this screen in full screen mode.
     */
    private void initFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    /**
     * Checks whether the user is a returning user or a new user
     *
     * @param userid
     */
    private void checkUserStatus(String userid) {
        JSONObject object = new JSONObject();

        try {

            object.put("fbid", userid);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        // check internet status
        if (NetworkHelper.getNetConnectionStatus(MainActivity.this)) {
            final MaterialDialog statusDialog = new MaterialDialog.Builder(MainActivity.this)
                    .title(getString(R.string.verif_title))
                    .content(getString(R.string.waiting_msg))
                    .progress(true, 0)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .show();


            // TODO Update url
            AndroidNetworking.post(BuildConfig.URL + "/user-access/sign-in")
                    .addJSONObjectBody(object)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                statusDialog.dismiss();

                                JSONObject dataObject = response.getJSONObject("data");

                                // user is already registered, so direct him to the main screen
                                if (dataObject.getString("status").equals("existing-user")) {


                                    SharedPreferenceHelper spHelper = new SharedPreferenceHelper(MainActivity.this);
                                    spHelper.setAuthToken(dataObject.getString("authkey"));
                                    spHelper.setUUID(dataObject.getString("uuid"));

                                    // open the main screen
                                    Intent startIntent = new Intent(MainActivity.this, MerchandizingProductsActivity.class);
                                    startActivity(startIntent);

                                    finish();
                                }

                                // new user so mobile verification is to be done
                                else if (dataObject.getString("status").equals("new-user")) {
                                    phoneLogin();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(e);

                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            statusDialog.dismiss();
                            AccessToken.setCurrentAccessToken(null);
                            ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_server));

                        }


                    });
        } else {
            ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_no_connection));
        }


    }


    /**
     * To initiate facebook register flow for mobile verification
     */
    private void phoneLogin() {

        final Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, Constant.REQUEST_CODE_FB_ACCOUNT_KIT);

    }

    /**
     * gets the user data using facebook graph API
     */
    private void getUserData() {

        verifyDialog = new MaterialDialog.Builder(MainActivity.this)
                .title(getString(R.string.verif_title))
                .content(getString(R.string.waiting_msg))
                .progress(true, 0)
                .show();


        // reading the user's data from graph API
        final GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.d(TAG, "onCompleted: " + object);


                        FacebookRequestError error = response.getError();

                        //no error
                        if (error == null) {

                            graphObject = object;
                            // retrieve the verified number and send the details to the server
                            getPhoneNo();
                        }
                        // error
                        else {



                            verifyDialog.dismiss();

                            Category errorCateg = error.getCategory();

                            switch (errorCateg) {
                                case LOGIN_RECOVERABLE:
                                    // error is authentication related
                                    LoginManager.getInstance().resolveError(MainActivity.this, response);
                                    break;
                                case TRANSIENT:
                                    // some temporary error occurred so try again
                                    //access token is not set to null because graph request is retried
                                    getUserData();
                                    break;
                                case OTHER:
                                    ViewHelper.getSnackBar(parentLayout, error.getErrorUserMessage());
                                    AccessToken.setCurrentAccessToken(null);
                                    break;
                            }
                        }


                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,age_range,link,gender,locale,picture,email");
        request.setParameters(parameters);
        request.executeAsync();

        // TODO error handling for graph api

    }

    /**
     * sends phone number and other user details to the server and takes action according to the responsse
     */
    private void setUserDetails() {

        JSONObject reqObject = new JSONObject();

        Log.d(TAG, "setUserDetails: phone" + phoneNo);

        try {

            graphObject.put("phone", phoneNo);
            // processing picture object obtained from graph api
            graphObject.put("picture",
                    graphObject.getJSONObject("picture").getJSONObject("data").getString("url"));
            reqObject.put("userdata", graphObject);

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
        // TODO Update url
        AndroidNetworking.post(BuildConfig.URL + "/user-access/sign-up")
                .addJSONObjectBody(reqObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        verifyDialog.dismiss();

                        try {

                            JSONObject dataObject = response.getJSONObject("data");

                            // phone number verified by account kit doesn't exist already
                            if (dataObject.getString("status").equals("done")) {

                                SharedPreferenceHelper spHelper = new SharedPreferenceHelper(MainActivity.this);
                                spHelper.setAuthToken(dataObject.getString("authkey"));
                                spHelper.setUUID(dataObject.getString("uuid"));

                                // open the main screen
                                Intent startIntent = new Intent(MainActivity.this, BottomNavigationActivity.class);
                                startActivity(startIntent);

                                finish();
                            }

                            //phone number already exists
                            else if (dataObject.getString("status").equals("phone-exists")) {
                                // TODO incomplete
                                AccessToken.setCurrentAccessToken(null);
                                ViewHelper.getSnackBar(parentLayout, "This number is already registered with us");
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        verifyDialog.dismiss();
                        AccessToken.setCurrentAccessToken(null);
                        ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_server));
                    }
                });

    }

    /**
     * method to get phone number from account kit
     */
    private void getPhoneNo() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {

                PhoneNumber number = account.getPhoneNumber();
                phoneNo = number.toString();

                setUserDetails();
            }

            @Override
            public void onError(AccountKitError accountKitError) {

                verifyDialog.dismiss();
                AccessToken.setCurrentAccessToken(null);
                ViewHelper.getSnackBar(parentLayout, accountKitError.getUserFacingMessage());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);


        // Handling the result of fb mobile verification
        if (requestCode == Constant.REQUEST_CODE_FB_ACCOUNT_KIT) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if (loginResult.getError() != null) {
                ViewHelper.getSnackBar(parentLayout, loginResult.getError().getUserFacingMessage());
                AccessToken.setCurrentAccessToken(null);

            } else if (loginResult.wasCancelled()) {

                AccessToken.setCurrentAccessToken(null);
                ViewHelper.getSnackBar(parentLayout, "Login cancelled");

            } else {
                getUserData();
            }

        }
    }
}


