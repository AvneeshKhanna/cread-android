package com.thetestament.cread.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.ReverseInterpolator;
import com.thetestament.cread.widgets.TypeWriterText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * AppCompatActivity for product tour feature.
 */

public class ProductTourActivity extends AppCompatActivity {

    //region :View binding with Butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.slide_in_image_first)
    AppCompatImageView slideInImageFirst;
    @BindView(R.id.slide_in_image_second)
    AppCompatImageView slideInImageSecond;
    @BindView(R.id.slide_in_image_third)
    AppCompatImageView slideInImageThird;
    @BindView(R.id.title_text)
    AppCompatTextView titleText;
    @BindView(R.id.desc_typewriter_text)
    TypeWriterText descTypewriterText;
    @BindView(R.id.collaboration_typewriter_text)
    TypeWriterText collaborationTypewriterText;
    @BindView(R.id.text_got_it)
    AppCompatTextView textGotIt;
    @BindView(R.id.btn_replay_animation)
    AppCompatImageView btnReplayAnimation;
    //endregion

    //region :Field and constants
    /**
     * Flag to maintain reference of this activity.
     */
    ProductTourActivity mContext;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //For fullscreen display
        ViewHelper.initFullScreen(this);
        //Layout for this screen
        setContentView(R.layout.activity_product_tour);
        ButterKnife.bind(this);
        //Method called
        initView();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    //endregion

    //region :Click functionality

    /**
     * Got it button click functionality to Open MainActivity screen.
     */
    @OnClick(R.id.text_got_it)
    void gotItOnClick() {
        //Open MainActivity
        IntentHelper.openMainActivity(mContext);
        //Close parent activity
        finish();
    }


    /**
     * Click functionality to restart animation.
     */
    @OnClick(R.id.btn_replay_animation)
    void onReplayClick() {
        //toggle views visibility
        textGotIt.setVisibility(View.GONE);
        btnReplayAnimation.setVisibility(View.GONE);

        slideInImageFirst.setVisibility(View.GONE);
        slideInImageSecond.setVisibility(View.GONE);
        slideInImageThird.setVisibility(View.GONE);

        descTypewriterText.setVisibility(View.GONE);
        collaborationTypewriterText.setVisibility(View.GONE);
        titleText.setVisibility(View.GONE);
        titleText.setText("Express yourself with captivating words");
        //Method called
        initView();
    }

    //endregion

    //region :Private methods

    /**
     * Method to initialize views for this screen.
     */
    private void initView() {
        //obtain reference of this screen.
        mContext = this;
        // set fade in animations on title text
        initFadeInAnimation(titleText);
        //set animation end listener
        descTypewriterText.setOnAnimationFinishListener(new TypeWriterText.OnAnimationFinishListener() {
            @Override
            public void onFinish() {
                //Update text color and font after 1.5 seconds
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Update text color and font
                        descTypewriterText.setTextColor(ContextCompat.getColor(mContext, R.color.blue));
                        descTypewriterText.setTypeface(ResourcesCompat.getFont(mContext, R.font.montserrat_regular), Typeface.BOLD);
                    }
                }, 1500);

                //Update text color and font after 2.5 seconds
                Handler handlerTextUpdate = new Handler();
                handlerTextUpdate.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Update text color and font
                        descTypewriterText.setTextColor(ContextCompat.getColor(mContext, R.color.color_pink_500));
                        descTypewriterText.setTypeface(ResourcesCompat.getFont(mContext, R.font.thunder_pants), Typeface.NORMAL);
                        //Method called
                        initSlideInAnimation(slideInImageFirst);
                    }
                }, 2500);
            }
        });

    }


    /**
     * Method to initialize fade in animation on title text.
     *
     * @param textView TextView to be animated.
     */
    private void initFadeInAnimation(final AppCompatTextView textView) {
        textView.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.scale);
        fadeIn.reset();
        textView.clearAnimation();
        textView.startAnimation(fadeIn);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Animate description text with typewriter animation.
                descTypewriterText.setVisibility(View.VISIBLE);
                descTypewriterText.setText("");
                descTypewriterText.setCharacterDelay(30);
                descTypewriterText.animateText("Fostering the silence of my hand. I duly learnt i'm an articulate kind.");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * Method to initialize slide in animation on first image.
     *
     * @param image Image to be animated.
     */
    private void initSlideInAnimation(final AppCompatImageView image) {
        //Animate image with slide in right animation
        Animation slideInFirst = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
        image.setVisibility(View.VISIBLE);
        image.startAnimation(slideInFirst);
        slideInFirst.setStartOffset(1000);
        slideInFirst.setFillAfter(false);
        slideInFirst.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //Apply slide in animation on typewriter text
                textSlideAnimation();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Change text with fade in animation
                titleText.setText("Upload creative photographs");
                titleText.setVisibility(View.VISIBLE);
                Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.scale);
                fadeIn.reset();
                titleText.clearAnimation();
                titleText.startAnimation(fadeIn);
                //Second image slide in animation
                Animation slideInSecond = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                slideInSecond.setStartOffset(1000);
                slideInImageSecond.setVisibility(View.VISIBLE);
                slideInImageSecond.startAnimation(slideInSecond);
                slideInSecond.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //Third image slide in animation
                        Animation slideIn = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                        slideIn.setStartOffset(1000);
                        slideInImageThird.setVisibility(View.VISIBLE);
                        slideInImageThird.startAnimation(slideIn);
                        slideIn.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                titleText.setVisibility(View.GONE);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        titleText.setText("Place others' photos on your words or vice-versa to collaborate");
                                        titleText.setVisibility(View.VISIBLE);
                                        Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.scale);
                                        fadeIn.reset();
                                        titleText.clearAnimation();
                                        titleText.startAnimation(fadeIn);

                                        collaborationTypewriterText.setVisibility(View.VISIBLE);

                                        Animation slideInFirst = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                                        collaborationTypewriterText.setVisibility(View.VISIBLE);
                                        collaborationTypewriterText.startAnimation(slideInFirst);
                                        slideInFirst.setStartOffset(1000);
                                        slideInFirst.setFillAfter(false);
                                        slideInFirst.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                //Toggle views visibility
                                                btnReplayAnimation.setVisibility(View.VISIBLE);
                                                textGotIt.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });

                                    }
                                }, 1000);

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * Method to move text with slide out animation.
     */
    private void textSlideAnimation() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator moveAnim = ObjectAnimator.ofFloat(descTypewriterText, "translationX"
                        , -AspectRatioUtils.getDeviceScreenWidth());
                moveAnim.setDuration(800);
                moveAnim.start();
                moveAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        //Toggle view visibility
                        descTypewriterText.setVisibility(View.GONE);
                        descTypewriterText.setTypeface(ResourcesCompat.getFont(mContext, R.font.bohemian_typewriter), Typeface.NORMAL);
                        descTypewriterText.setTextColor(ContextCompat.getColor(mContext, R.color.black_defined));
                        animation.removeListener(this);
                        animation.setDuration(0);
                        animation.setInterpolator(new ReverseInterpolator());
                        animation.start();
                    }
                });
            }
        }, 1000);

    }


    //endregion
}
