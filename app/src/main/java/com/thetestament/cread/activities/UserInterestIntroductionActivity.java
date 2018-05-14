package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import icepick.State;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

import static com.thetestament.cread.fragments.MeFragment.loadUserPicture;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_PIC_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_IMAGE_PATH;
import static com.thetestament.cread.utils.Constant.EXTRA_USER_INTERESTS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_UPDATE_PROFILE_PIC;
import static com.thetestament.cread.utils.Constant.USER_INTERESTS_CALLED_FROM_PROFILE;

public class UserInterestIntroductionActivity extends BaseActivity {


    @BindView(R.id.buttonOpenUserInterests)
    TextView buttonOpenUserInterests;
    @BindView(R.id.textWelcomeLine1)
    TextView textWelcomeLine1;
    @BindView(R.id.textWelcomeLine2)
    TextView textWelcomeLine2;
    @BindView(R.id.textWelcomeLine3)
    TextView textWelcomeLine3;
    @BindView(R.id.imageUser)
    CircleImageView imageUser;
    @BindView(R.id.buttonEdit)
    TextView buttonEdit;

    @State
    String mProfilePicURL, mCalledFrom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init full screen
        initFullScreen();

        setContentView(R.layout.activity_user_interest_introduction);
        ButterKnife.bind(this);

        // init called from
        mCalledFrom = getIntent().getStringExtra(EXTRA_USER_INTERESTS_CALLED_FROM);
        // init profile pic url
        mProfilePicURL = getIntent().getStringExtra(EXTRA_PROFILE_PIC_URL);
        // init shared preferences
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(this);

        // load profile pic
        if (mProfilePicURL != null) {
            loadUserPicture(mProfilePicURL, imageUser, this);
        }

        // set text
        textWelcomeLine1.setText(Html.fromHtml("Hey" + "<font color=black>" + " " + spHelper.getFirstName() + "!" + "</font>"));
        // if called from profile set diff text
        if (mCalledFrom.equals(USER_INTERESTS_CALLED_FROM_PROFILE)) {
            // TODO update text
            textWelcomeLine2.setText("As you know Cread is a community of artists");
        }

        // set the animations
        initAnimatedText(textWelcomeLine1, 0);
        initAnimatedText(textWelcomeLine2, 1000);
        initAnimatedText(textWelcomeLine3, 2000);
        initAnimatedText(buttonOpenUserInterests, 3000);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Required for permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_UPDATE_PROFILE_PIC:
                if (resultCode == RESULT_OK) {
                    mProfilePicURL = data.getExtras().getString(EXTRA_USER_IMAGE_PATH);
                    //load user profile
                    loadUserPicture(mProfilePicURL, imageUser, this);
                }
                break;
        }
    }

    @OnClick(R.id.buttonOpenUserInterests)
    void onChooseClick() {
        Intent intent = new Intent(this, UserInterestsActivity.class);
        intent.putExtra(EXTRA_USER_INTERESTS_CALLED_FROM, mCalledFrom);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

    @OnClick({R.id.imageUser, R.id.buttonEdit})
    void profilePicEditOnClick() {
        getRuntimePermission();
    }

    /**
     * To open this screen in full screen mode.
     */
    private void initFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initAnimatedText(final TextView textView, int delay) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.VISIBLE);
                Animation a = AnimationUtils.loadAnimation(UserInterestIntroductionActivity.this, R.anim.scale);
                a.reset();
                textView.clearAnimation();
                textView.startAnimation(a);

            }
        }, delay);

    }


    /**
     * Used to handle result of askForPermission for Profile pic.
     */
    PermissionCallback profilePicWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            openUpdateImageScreen();
        }

        @Override
        public void permissionRefused() {
            ViewHelper.getToast(UserInterestIntroductionActivity.this
                    , "Please grant storage permission from settings to edit your profile picture.");
        }
    };


    /**
     * Method to get WRITE_EXTERNAL_STORAGE permission and perform specified operation.
     */
    private void getRuntimePermission() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            openUpdateImageScreen();
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(this
                        , "Please grant storage permission from settings to edit your profile picture.");
            } else {
                Nammu.askForPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, profilePicWritePermission);
            }
        }
    }

    /**
     * Open UpdateProfileImageActivity screen.
     */
    private void openUpdateImageScreen() {

        Intent intent = new Intent(this, UpdateProfileImageActivity.class);
        intent.putExtra(EXTRA_USER_IMAGE_PATH, mProfilePicURL);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_PROFILE_PIC);
    }


}
