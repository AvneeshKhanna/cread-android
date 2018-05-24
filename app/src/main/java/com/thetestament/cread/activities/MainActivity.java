package com.thetestament.cread.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.crashlytics.android.Crashlytics;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.CreadApp;
import com.thetestament.cread.R;
import com.thetestament.cread.dialog.CustomDialog;
import com.thetestament.cread.helpers.IntentHelper;
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

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_PIC_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_INTERESTS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.LOGIN_TYPE_FACEBOOK;
import static com.thetestament.cread.utils.Constant.LOGIN_TYPE_GOOGLE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_GOOGLE_SIGN_IN;
import static com.thetestament.cread.utils.Constant.USER_INTERESTS_CALLED_FROM_LOGIN;

public class MainActivity extends BaseActivity {

    //region :View binding with Butter knife
    @BindView(R.id.rootView)
    RelativeLayout rootView;
    @BindView(R.id.btnFBLogin)
    LoginButton btnFBLogin;
    //endregion

    //region :Fields and constant
    CallbackManager mCallbackManager;
    JSONObject graphObject;
    MaterialDialog verifyDialog;
    SharedPreferenceHelper spHelper;

    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount mGoogleSignInAccount;


    /**
     * Flag to store user phone number.
     */
    @State
    String phoneNo;
    /**
     * Flag to maintain  whether user is signing with FB or Google .
     */
    @State
    String mLoginType;

    /**
     * Flag to maintain reference of this activity.
     */
    MainActivity mContext;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For fullscreen display
        ViewHelper.initFullScreen(this);
        //Layout for this screen
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //Method called
        initView();
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
            AccountKitLoginResult loginResult = data
                    .getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if (loginResult.getError() != null) {
                //Show snack bar
                ViewHelper.getSnackBar(rootView
                        , loginResult.getError().getUserFacingMessage());
                //Update flags
                mGoogleSignInClient.signOut();
                AccessToken.setCurrentAccessToken(null);
            } else if (loginResult.wasCancelled()) {
                //Update flags
                mGoogleSignInClient.signOut();
                AccessToken.setCurrentAccessToken(null);
                //Show snack bar
                ViewHelper.getSnackBar(rootView, "Login cancelled");
            } else {
                //Device is connected to internet
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    verifyDialog = CustomDialog.getProgressDialog(mContext
                            , "Verifying...");
                    if (mLoginType.equals(LOGIN_TYPE_FACEBOOK)) {
                        getUserData();
                    } else if (mLoginType.equals(LOGIN_TYPE_GOOGLE)) {
                        retrieveUserPhoneNo();
                    }
                }
                //Device is not connected to internet.
                else {
                    ////Update flags
                    mGoogleSignInClient.signOut();
                    AccessToken.setCurrentAccessToken(null);
                    //Show snack bar
                    ViewHelper.getSnackBar(rootView
                            , getString(R.string.error_msg_no_connection));
                }
            }
        } else
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful
                    mGoogleSignInAccount = task.getResult(ApiException.class);
                    checkUserStatus(null, mGoogleSignInAccount.getIdToken(), mContext);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    Crashlytics.setString("className", "MainActivity");
                    ViewHelper.getSnackBar(rootView, "Google Sign in Failed");
                }
            }
    }
    //endregion

    //region :Click functionality

    /**
     * Log In button onClick Listener
     */
    @OnClick(R.id.btnFBLogin)
    public void logInOnClick() {
        if (!NetworkHelper.getNetConnectionStatus(mContext)) {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        } else {
            //Update flag
            mLoginType = LOGIN_TYPE_FACEBOOK;
        }
    }

    /**
     * Functionality to launch TermsOfServiceActivity.
     */
    @OnClick(R.id.textTOS)
    public void showTos() {
        IntentHelper.openWebViewActivity(mContext
                , "file:///android_asset/" + "cread_tos.html"
                , "Terms of Service");
    }

    /**
     * Google sign btn click functionality.
     */
    @OnClick(R.id.btnGoogleLogin)
    public void googleLoginOnClick() {
        if (!NetworkHelper.getNetConnectionStatus(mContext)) {
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_no_connection));
        } else {
            //Update flag
            mLoginType = LOGIN_TYPE_GOOGLE;
            //Launch sign in options
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
        }
    }
    //endregion

    //region :Private methods

    /**
     * Method to initialize views for this screen.
     */
    private void initView() {
        //obtain reference of this screen.
        mContext = this;
        // init shared prefs
        spHelper = new SharedPreferenceHelper(mContext);
        //initialize  sign in options
        initGoogleSignIn();
        initFBLogin();
    }

    /**
     * Method to initialize Google sign option.
     */
    private void initGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);
    }

    /**
     * Method to initialize FB login option.
     */
    private void initFBLogin() {
        mCallbackManager = CallbackManager.Factory.create();
        btnFBLogin.setReadPermissions(Arrays.asList("email", "user_friends"));
        btnFBLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (loginResult.getRecentlyDeniedPermissions().contains("user_friends")) {
                    //Show snack bar
                    ViewHelper.getSnackBar(rootView,
                            "You need to grant friends permission to continue");
                    AccessToken.setCurrentAccessToken(null);
                } else {
                    AccessToken accessToken = loginResult.getAccessToken();
                    checkUserStatus(accessToken.getUserId(), null, mContext);
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


    /**
     * Checks whether the user is a returning user or a new user.
     *
     * @param fbUserID      FB user ID.
     * @param googleIDToken Google token ID of user.
     * @param context       Context to use.
     */
    private void checkUserStatus(String fbUserID, String googleIDToken, final AppCompatActivity context) {
        JSONObject object = new JSONObject();
        try {
            object.put("fbid", fbUserID);
            object.put("google_access_token", googleIDToken);
            object.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            Crashlytics.setString("className", "MainActivity");
        }

        // check internet status
        if (NetworkHelper.getNetConnectionStatus(context)) {
            //Show progress dialog
            final MaterialDialog statusDialog = CustomDialog.getProgressDialog(context
                    , "Verifying...");

            AndroidNetworking.post(BuildConfig.URL + "/user-access/sign-in")
                    .addJSONObjectBody(object)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                statusDialog.dismiss();
                                JSONObject dataObject = response.getJSONObject("data");

                                //User is already registered, so direct him to the main screen
                                if (dataObject.getString("status").equals("existing-user")) {
                                    SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
                                    spHelper.setAuthToken(dataObject.getString("authkey"));
                                    spHelper.setUUID(dataObject.getString("uuid"));
                                    spHelper.setFirstName(dataObject.getString("firstname"));
                                    spHelper.setLastName(dataObject.getString("lastname"));

                                    //Initialize for socket IO
                                    CreadApp.initSocketIo(context);

                                    //Open BottomNavigationActivity screen
                                    IntentHelper.openBottomNavigationctivity(context);
                                    //Finish this screen
                                    finish();
                                }
                                //New user so mobile verification is to be done
                                else if (dataObject.getString("status").equals("new-user")) {
                                    //Method called
                                    phoneLogin(context);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                Crashlytics.setString("className", "MainActivity");
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            //Dismiss dialog
                            statusDialog.dismiss();
                            //Update flags
                            AccessToken.setCurrentAccessToken(null);
                            mGoogleSignInClient.signOut();
                            //Show snack bar
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                        }
                    });
        } else {
            //Show snack bar
            ViewHelper.getSnackBar(rootView
                    , getString(R.string.error_msg_no_connection));
        }


    }


    /**
     * To initiate facebook register flow for mobile verification.
     *
     * @param context Context to use.
     */
    public static void phoneLogin(AppCompatActivity context) {
        final Intent intent = new Intent(context, AccountKitActivity.class);

        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
                = new AccountKitConfiguration.AccountKitConfigurationBuilder(
                LoginType.PHONE,
                AccountKitActivity.ResponseType.TOKEN);

        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());

        context.startActivityForResult(intent, Constant.REQUEST_CODE_FB_ACCOUNT_KIT);
    }

    /**
     * Method to retrieve user data using facebook graph API.
     */
    private void getUserData() {
        // reading the user's data from graph API
        final GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        FacebookRequestError error = response.getError();
                        //No error
                        if (error == null) {
                            graphObject = object;
                            // retrieve the verified number and send the details to the server
                            retrieveUserPhoneNo();
                        }
                        // error
                        else {
                            //Dismiss dialog
                            verifyDialog.dismiss();

                            Category errorCategory = error.getCategory();

                            switch (errorCategory) {
                                case LOGIN_RECOVERABLE:
                                    // error is authentication related
                                    LoginManager.getInstance().resolveError(mContext, response);
                                    break;
                                case TRANSIENT:
                                    // some temporary error occurred so try again
                                    //access token is not set to null because graph request is retried
                                    getUserData();
                                    break;
                                case OTHER:
                                    ViewHelper.getSnackBar(rootView, error.getErrorUserMessage());
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
     * Method to retrieve user phone number from FB account kit.
     */
    private void retrieveUserPhoneNo() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                PhoneNumber number = account.getPhoneNumber();
                phoneNo = number.toString();
                saveUserDetails();
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                //Dismiss dialog
                verifyDialog.dismiss();
                //Update flags
                mGoogleSignInClient.signOut();
                AccessToken.setCurrentAccessToken(null);
                //Show snack bar
                ViewHelper.getSnackBar(rootView, accountKitError.getUserFacingMessage());
            }
        });
    }

    /**
     * Method to send phone number and other user details to the server and takes action according to the response.
     */
    private void saveUserDetails() {
        JSONObject reqObject = new JSONObject();
        try {
            //If FB login
            if (mLoginType.equals(LOGIN_TYPE_FACEBOOK)) {
                graphObject.put("phone", phoneNo);
                // processing picture object obtained from graph api
                graphObject.put("picture",
                        graphObject.getJSONObject("picture").getJSONObject("data").getString("url"));
                reqObject.put("userdata", graphObject);
            }
            //Google sign in
            else if (mLoginType.equals(LOGIN_TYPE_GOOGLE)) {
                // phone number
                reqObject.put("userdata", new JSONObject().put("phone", phoneNo));
                // put google id token
                reqObject.put("google_access_token", mGoogleSignInAccount.getIdToken());
            }
            //Put fcm token
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
            Crashlytics.logException(e);
            Crashlytics.setString("className", "MainActivity");
        }

        AndroidNetworking.post(BuildConfig.URL + "/user-access/sign-up")
                .addJSONObjectBody(reqObject)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject dataObject = response.getJSONObject("data");
                            // phone number verified by account kit doesn't exist already
                            if (dataObject.getString("status").equals("done")) {
                                spHelper.setAuthToken(dataObject.getString("authkey"));
                                spHelper.setUUID(dataObject.getString("uuid"));
                                String profilePicUrl = null;
                                //FB login
                                if (mLoginType.equals(LOGIN_TYPE_FACEBOOK)) {
                                    // getting first name and last name from graph object
                                    spHelper.setFirstName(graphObject.getString("first_name"));
                                    spHelper.setLastName(graphObject.getString("last_name"));
                                    // if not null set profile pic url
                                    if (graphObject.getString("picture") != null) {
                                        // get profile pic url
                                        profilePicUrl = graphObject.getString("picture");
                                    }
                                }
                                //Google login
                                else if (mLoginType.equals(LOGIN_TYPE_GOOGLE)) {
                                    spHelper.setFirstName(mGoogleSignInAccount.getGivenName());
                                    spHelper.setLastName(mGoogleSignInAccount.getFamilyName());
                                    if (mGoogleSignInAccount.getPhotoUrl().toString() != null) {
                                        profilePicUrl = mGoogleSignInAccount.getPhotoUrl().toString();
                                    }
                                }

                                //Initialize for  socket IO
                                CreadApp.initSocketIo(mContext);
                                //Dismiss dialog
                                verifyDialog.dismiss();
                                // open the user intro screen
                                Intent startIntent = new Intent(MainActivity.this, UserInterestIntroductionActivity.class);
                                startIntent.putExtra(EXTRA_USER_INTERESTS_CALLED_FROM, USER_INTERESTS_CALLED_FROM_LOGIN);
                                startIntent.putExtra(EXTRA_PROFILE_PIC_URL, profilePicUrl);
                                startActivity(startIntent);
                                finish();
                            }
                            //phone number already exists
                            else if (dataObject.getString("status").equals("phone-exists")) {
                                //Dismiss dialog
                                verifyDialog.dismiss();
                                mGoogleSignInClient.signOut();
                                AccessToken.setCurrentAccessToken(null);
                                ViewHelper.getSnackBar(rootView, "This number is already registered with us");
                            }

                        } catch (JSONException e) {
                            //Dismiss dialog
                            verifyDialog.dismiss();
                            //Invalidate token
                            mGoogleSignInClient.signOut();
                            AccessToken.setCurrentAccessToken(null);
                            //Report error to firebase
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "MainActivity");
                            //Show snack bar
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //Dismiss dialog
                        verifyDialog.dismiss();
                        //Update flags
                        mGoogleSignInClient.signOut();
                        AccessToken.setCurrentAccessToken(null);
                        //Show snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));
                    }
                });

    }

    //endregion

}


