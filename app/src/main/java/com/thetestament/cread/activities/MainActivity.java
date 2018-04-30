package com.thetestament.cread.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
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
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_TITLE;
import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_URL;
import static com.thetestament.cread.utils.Constant.LOGIN_TYPE_FACEBOOK;
import static com.thetestament.cread.utils.Constant.LOGIN_TYPE_GOOGLE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_GOOGLE_SIGN_IN;

public class MainActivity extends BaseActivity {

    private final String TAG = getClass().getSimpleName();
    //Array to store  sliders layout
    @BindView(R.id.dotsLayout)
    LinearLayout dotsLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.loginButton)
    LoginButton loginButton;
    @BindView(R.id.buttonGoogleLogin)
    com.google.android.gms.common.SignInButton buttonGoogleLogin;
    @BindView(R.id.loginParent)
    RelativeLayout parentLayout;
    CallbackManager mCallbackManager;
    JSONObject graphObject;
    String phoneNo;
    MaterialDialog verifyDialog;
    private int[] mLayouts;
    SharedPreferenceHelper spHelper;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount mGoogleSignInAccount;

    @State
    String mLoginType;


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
        // init shared prefs
        spHelper = new SharedPreferenceHelper(MainActivity.this);

        //For sliders
        initSliders();
        // Add bottom dots
        addDots(0);
        //Initialize view pager
        initViewPager();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // set google login button text
        setGoogleLoginButtonText();

        mCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("email", "user_friends"));
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                if (loginResult.getRecentlyDeniedPermissions().contains("user_friends")) {
                    ViewHelper.getSnackBar(parentLayout, "You need to grant friends permission to continue");
                    AccessToken.setCurrentAccessToken(null);
                } else {
                    AccessToken accessToken = loginResult.getAccessToken();
                    checkUserStatus(accessToken.getUserId(), null);
                }
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
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);

        if (mGoogleSignInAccount == null) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            }
        }
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
                //fixme
                mGoogleSignInClient.signOut();
                AccessToken.setCurrentAccessToken(null);

            } else if (loginResult.wasCancelled()) {
                // fixme
                mGoogleSignInClient.signOut();
                AccessToken.setCurrentAccessToken(null);
                ViewHelper.getSnackBar(parentLayout, "Login cancelled");

            } else {

                if (NetworkHelper.getNetConnectionStatus(MainActivity.this)) {
                    verifyDialog = new MaterialDialog.Builder(MainActivity.this)
                            .title(getString(R.string.verif_title))
                            .content(getString(R.string.waiting_msg))
                            .progress(true, 0)
                            .show();

                    if (mLoginType.equals(LOGIN_TYPE_FACEBOOK)) {
                        getUserData();
                    } else if (mLoginType.equals(LOGIN_TYPE_GOOGLE)) {
                        getPhoneNo();
                    }

                } else {
                    mGoogleSignInClient.signOut();
                    AccessToken.setCurrentAccessToken(null);
                    ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_no_connection));
                }


            }

        } else  // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful
                    mGoogleSignInAccount = task.getResult(ApiException.class);

                    checkUserStatus(null, mGoogleSignInAccount.getIdToken());

                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    e.printStackTrace();
                    FirebaseCrash.report(e);
                    ViewHelper.getSnackBar(parentLayout, "Google Sign in Failed");
                }
            }
    }

    /**
     * Log In button onClick Listener
     */
    @OnClick(R.id.loginButton)
    public void logInOnClick() {

        if (!NetworkHelper.getNetConnectionStatus(MainActivity.this)) {
            ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_no_connection));
        } else {
            mLoginType = LOGIN_TYPE_FACEBOOK;
        }

    }

    /**
     * Functionality to launch TermsOfServiceActivity.
     */
    @OnClick(R.id.textTOS)
    public void showTos() {

        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra(EXTRA_WEB_VIEW_URL, "file:///android_asset/" + "cread_tos.html");
        intent.putExtra(EXTRA_WEB_VIEW_TITLE, "Terms of Service");
        startActivity(intent);
    }

    @OnClick(R.id.buttonGoogleLogin)
    public void googleLoginOnClick() {

        if (!NetworkHelper.getNetConnectionStatus(MainActivity.this)) {
            ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_no_connection));
        } else {
            mLoginType = LOGIN_TYPE_GOOGLE;
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
        }
    }

    /**
     * Method to add dots  and to change the color of dots
     *
     * @param currentPage mGravityFlag of current page i.e 0(zero)
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
     * @param fbUserID
     * @param googleIDToken
     */
    private void checkUserStatus(String fbUserID, String googleIDToken) {
        JSONObject object = new JSONObject();

        try {

            object.put("fbid", fbUserID);
            object.put("google_access_token", googleIDToken);
            object.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());

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
                                    spHelper.setFirstName(dataObject.getString("firstname"));
                                    spHelper.setLastName(dataObject.getString("lastname"));


                                    CreadApp.initSocketIo(MainActivity.this);


                                    // open the main screen
                                    Intent startIntent = new Intent(MainActivity.this, BottomNavigationActivity.class);
                                    startActivity(startIntent);

                                    finish();
                                }

                                // new user so mobile verification is to be done
                                else if (dataObject.getString("status").equals("new-user")) {
                                    phoneLogin(MainActivity.this);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                                FirebaseCrash.report(e);

                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            statusDialog.dismiss();
                            // fixme
                            AccessToken.setCurrentAccessToken(null);
                            mGoogleSignInClient.signOut();
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
    public static void phoneLogin(AppCompatActivity context) {

        final Intent intent = new Intent(context, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        context.startActivityForResult(intent, Constant.REQUEST_CODE_FB_ACCOUNT_KIT);

    }

    /**
     * gets the user data using facebook graph API
     */
    private void getUserData() {


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
        parameters.putString("fields", "id,first_name,last_name,age_range,link,gender,locale,picture.width(800).height(800),email");
        request.setParameters(parameters);
        request.executeAsync();
    }


    /**
     * sends phone number and other user details to the server and takes action according to the response
     */
    private void setUserDetails() {

        JSONObject reqObject = new JSONObject();

        Log.d(TAG, "setUserDetails: phone" + phoneNo);

        try {
            // if fb sign up
            if (mLoginType.equals(LOGIN_TYPE_FACEBOOK)) {
                graphObject.put("phone", phoneNo);
                // processing picture object obtained from graph api
                graphObject.put("picture",
                        graphObject.getJSONObject("picture").getJSONObject("data").getString("url"));
                reqObject.put("userdata", graphObject);
            }
            // google sign up
            else if (mLoginType.equals(LOGIN_TYPE_GOOGLE)) {
                // phone number
                reqObject.put("userdata", new JSONObject().put("phone", phoneNo));

                // put google id token
                reqObject.put("google_access_token", mGoogleSignInAccount.getIdToken());
            }
            // put fcm token
            reqObject.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());
            // if from deep link
            // insert data
            if (spHelper.getDeepLink() != null) {
                Uri deepLinkUri = Uri.parse(spHelper.getDeepLink());
                // parse referral code
                reqObject.put("referral_code", deepLinkUri.getQueryParameter("referral_code"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

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

                                spHelper.setAuthToken(dataObject.getString("authkey"));
                                spHelper.setUUID(dataObject.getString("uuid"));

                                if (mLoginType.equals(LOGIN_TYPE_FACEBOOK)) {
                                    // getting first name and last name from graph object
                                    spHelper.setFirstName(graphObject.getString("first_name"));
                                    spHelper.setLastName(graphObject.getString("last_name"));
                                } else if (mLoginType.equals(LOGIN_TYPE_GOOGLE)) {
                                    spHelper.setFirstName(mGoogleSignInAccount.getGivenName());
                                    spHelper.setLastName(mGoogleSignInAccount.getFamilyName());
                                }

                                CreadApp.initSocketIo(MainActivity.this);

                                // open the main screen
                                Intent startIntent = new Intent(MainActivity.this, BottomNavigationActivity.class);
                                startActivity(startIntent);

                                finish();
                            }

                            //phone number already exists
                            else if (dataObject.getString("status").equals("phone-exists")) {
                                // fixme
                                mGoogleSignInClient.signOut();
                                AccessToken.setCurrentAccessToken(null);
                                ViewHelper.getSnackBar(parentLayout, "This number is already registered with us");
                            }


                        } catch (JSONException e) {
                            //Invalidate token
                            // fixme
                            mGoogleSignInClient.signOut();
                            AccessToken.setCurrentAccessToken(null);
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(parentLayout, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        verifyDialog.dismiss();
                        // fixme
                        mGoogleSignInClient.signOut();
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
                // fixme
                mGoogleSignInClient.signOut();
                AccessToken.setCurrentAccessToken(null);
                ViewHelper.getSnackBar(parentLayout, accountKitError.getUserFacingMessage());
            }
        });
    }

    /**
     * Sets google login button text
     */
    private void setGoogleLoginButtonText() {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < buttonGoogleLogin.getChildCount(); i++) {
            View v = buttonGoogleLogin.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText("Sign In with Google   ");
                return;
            }
        }
    }


}


