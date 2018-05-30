package com.thetestament.cread.activities;

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
import android.widget.RelativeLayout;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.widgets.TypeWriterText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

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
    @BindView(R.id.slide_in_image_fourth)
    AppCompatImageView slideInImageFourth;
    @BindView(R.id.slide_in_image_fifth)
    AppCompatImageView slideInImageFifth;
    @BindView(R.id.title_text)
    AppCompatTextView titleText;
    @BindView(R.id.desc_typewriter_text)
    TypeWriterText descTypewriterText;
    @BindView(R.id.collaboration_typewriter_text)
    AppCompatTextView collaborationTypewriterText;
    @BindView(R.id.text_got_it)
    AppCompatTextView textGotIt;
    @BindView(R.id.btn_replay_animation)
    AppCompatTextView btnReplayAnimation;
    @BindView(R.id.container_explore)
    RelativeLayout containerExplore;
    @BindView(R.id.img_explore_one)
    CircleImageView imgExploreOne;
    @BindView(R.id.img_explore_two)
    CircleImageView imgExploreTwo;
    @BindView(R.id.img_explore_three)
    CircleImageView imgExploreThree;
    @BindView(R.id.img_explore_four)
    CircleImageView imgExploreFour;
    @BindView(R.id.img_explore_five)
    CircleImageView imgExploreFive;
    @BindView(R.id.img_explore_six)
    CircleImageView imgExploreSix;
    @BindView(R.id.img_explore_seven)
    CircleImageView imgExploreSeven;

    //endregion

    //region :Field and constants
    /**
     * Flag to maintain reference of this activity.
     */
    ProductTourActivity mContext;

    Animation expandInOne, expandInTwo, expandInThree, expandInFour, expandInFive, expandInSix, expandInSeven;

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
        slideInImageFirst.setVisibility(View.GONE);
        slideInImageSecond.setVisibility(View.GONE);
        slideInImageThird.setVisibility(View.GONE);
        slideInImageFourth.setVisibility(View.GONE);
        slideInImageFifth.setVisibility(View.GONE);
        titleText.setVisibility(View.GONE);
        descTypewriterText.setVisibility(View.GONE);
        collaborationTypewriterText.setVisibility(View.GONE);
        btnReplayAnimation.setVisibility(View.GONE);
        containerExplore.setVisibility(View.GONE);
        imgExploreOne.setVisibility(View.GONE);
        imgExploreTwo.setVisibility(View.GONE);
        imgExploreThree.setVisibility(View.GONE);
        imgExploreFour.setVisibility(View.GONE);
        imgExploreFive.setVisibility(View.GONE);
        imgExploreSix.setVisibility(View.GONE);
        imgExploreSeven.setVisibility(View.GONE);


        //Set title text
        titleText.setText("Express yourself with captivating words");

        //Clear animation
        expandInOne.reset();
        expandInTwo.reset();
        expandInThree.reset();
        expandInFour.reset();
        expandInFive.reset();
        expandInSix.reset();
        expandInSeven.reset();
        imgExploreOne.clearAnimation();
        imgExploreTwo.clearAnimation();
        imgExploreThree.clearAnimation();
        imgExploreFour.clearAnimation();
        imgExploreFive.clearAnimation();
        imgExploreSix.clearAnimation();
        imgExploreSeven.clearAnimation();
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
        //Method called
        initWritingAnimation(titleText);


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
                        initGraphicAnimation();
                    }
                }, 2500);
            }
        });

    }


    /**
     * Method to initialize Writing animation.
     *
     * @param textView TextView to be animated.
     */
    private void initWritingAnimation(final AppCompatTextView textView) {
        //Toggle visibility
        textView.setVisibility(View.VISIBLE);
        //Obtain fade in reference
        Animation fadeInAnimTitleText = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        fadeInAnimTitleText.reset();
        textView.clearAnimation();
        textView.startAnimation(fadeInAnimTitleText);
        fadeInAnimTitleText.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Animate description text with typewriter animation with moving cursor.
                descTypewriterText.setVisibility(View.VISIBLE);
                descTypewriterText.requestFocus();
                descTypewriterText.setText("");
                descTypewriterText.setCharacterDelay(30);
                descTypewriterText.animateText("Fostering the silence of my mind. I duly learnt i'm an articulate kind.");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * Method to initialize animation for graphics.
     */
    private void initGraphicAnimation() {
        //Obtain fade out animation for description text
        Animation fadeInDescText = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
        descTypewriterText.clearAnimation();
        fadeInDescText.setStartOffset(1000);
        descTypewriterText.startAnimation(fadeInDescText);
        fadeInDescText.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Hide view and update properties
                descTypewriterText.setVisibility(View.GONE);
                descTypewriterText.setTypeface(ResourcesCompat.getFont(mContext, R.font.bohemian_typewriter), Typeface.NORMAL);
                descTypewriterText.setTextColor(ContextCompat.getColor(mContext, R.color.black_defined));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        //Obtain fade in animation for firstImage
        slideInImageFirst.setVisibility(View.VISIBLE);
        Animation fadeInImage = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        fadeInImage.setStartOffset(1000);
        fadeInImage.reset();
        slideInImageFirst.clearAnimation();
        slideInImageFirst.startAnimation(fadeInImage);
        fadeInImage.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Second image slide in animation
                Animation slideInSecondImage = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                slideInSecondImage.setStartOffset(1000);
                slideInImageSecond.setVisibility(View.VISIBLE);
                slideInImageSecond.startAnimation(slideInSecondImage);
                slideInSecondImage.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //Third image slide in animation
                        Animation slideInThirdImage = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                        slideInThirdImage.setStartOffset(1000);
                        slideInImageThird.setVisibility(View.VISIBLE);
                        slideInImageThird.startAnimation(slideInThirdImage);
                        slideInThirdImage.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                //Method called
                                initializeCollaborationAnimation();
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


        Handler  handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Change title text with fade in animation
                titleText.setText("Upload creative photographs");
                Animation fadeInTitleText = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                //fadeInTitleText.reset();
                //titleText.clearAnimation();
                titleText.startAnimation(fadeInTitleText);
            }
        },1000);

    }


    /**
     * Method to initialize collaboration animation.
     */
    private void initializeCollaborationAnimation() {
        //Toggle view visibility
        titleText.setVisibility(View.GONE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Toggle view visibility and set titleText with fade in animation
                titleText.setText("Place others' photos on your words or vice-versa to collaborate");
                titleText.setVisibility(View.VISIBLE);
                Animation fadeInTitleText = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                fadeInTitleText.reset();
                titleText.clearAnimation();
                titleText.startAnimation(fadeInTitleText);

                //Collaboration text with fade in animation
                collaborationTypewriterText.setVisibility(View.VISIBLE);
                Animation slideInFirst = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                collaborationTypewriterText.startAnimation(slideInFirst);
                slideInFirst.setFillAfter(false);
                slideInFirst.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        //Update text color and font after 1.5 seconds
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Update text color and font
                                collaborationTypewriterText.setTextColor(ContextCompat.getColor(mContext, R.color.blue));
                                collaborationTypewriterText.setTypeface(ResourcesCompat.getFont(mContext, R.font.montserrat_regular), Typeface.BOLD);
                            }
                        }, 1000);

                        //Update text color and font after 2.5 seconds
                        Handler handlerTextUpdate = new Handler();
                        handlerTextUpdate.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Fourth image slide in animation
                                Animation slideInFourth = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                                slideInImageFourth.setVisibility(View.VISIBLE);
                                slideInFourth.reset();
                                slideInImageFourth.clearAnimation();
                                slideInImageFourth.startAnimation(slideInFourth);
                                slideInFourth.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        //Update text color and font
                                        collaborationTypewriterText.setTextColor(ContextCompat.getColor(mContext, R.color.color_deep_orange_500));
                                        collaborationTypewriterText.setTypeface(ResourcesCompat.getFont(mContext, R.font.a_love_of_thunder), Typeface.NORMAL);
                                        //Fifth image slide in animation
                                        Animation slideInFifth = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
                                        slideInImageFifth.setVisibility(View.VISIBLE);
                                        slideInFifth.reset();
                                        slideInFifth.setStartOffset(500);
                                        slideInImageFifth.clearAnimation();
                                        slideInImageFifth.startAnimation(slideInFifth);
                                        slideInFifth.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                //Method called
                                                initExploreAnimation();
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
                        }, 2000);


                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

            }
        }, 1000);
    }

    /**
     * Method to initialize explore animation.
     */
    private void initExploreAnimation() {
        collaborationTypewriterText.setTypeface(ResourcesCompat.getFont(mContext, R.font.amatic_sc_regular), Typeface.NORMAL);
        collaborationTypewriterText.setTextColor(ContextCompat.getColor(mContext, R.color.black_defined));

        containerExplore.setVisibility(View.VISIBLE);
        Animation fadeInExplore = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
        fadeInExplore.setStartOffset(2000);
        fadeInExplore.reset();
        containerExplore.clearAnimation();
        containerExplore.startAnimation(fadeInExplore);
        fadeInExplore.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                expandInOne = AnimationUtils.loadAnimation(mContext, R.anim.pop_anim);
                imgExploreOne.setVisibility(View.VISIBLE);
                imgExploreOne.startAnimation(expandInOne);

                expandInTwo = AnimationUtils.loadAnimation(mContext, R.anim.pop_anim);
                expandInTwo.setStartOffset(300);
                imgExploreTwo.setVisibility(View.VISIBLE);
                imgExploreTwo.startAnimation(expandInTwo);

                expandInThree = AnimationUtils.loadAnimation(mContext, R.anim.pop_anim);
                expandInThree.setStartOffset(300);
                imgExploreThree.setVisibility(View.VISIBLE);
                imgExploreThree.startAnimation(expandInThree);

                expandInFour = AnimationUtils.loadAnimation(mContext, R.anim.pop_anim);
                expandInFour.setStartOffset(900);
                imgExploreFour.setVisibility(View.VISIBLE);
                imgExploreFour.startAnimation(expandInFour);

                expandInFive = AnimationUtils.loadAnimation(mContext, R.anim.pop_anim);
                expandInFive.setStartOffset(1200);
                imgExploreFive.setVisibility(View.VISIBLE);
                imgExploreFive.startAnimation(expandInFive);


                expandInSix = AnimationUtils.loadAnimation(mContext, R.anim.pop_anim);
                expandInSix.setStartOffset(1500);
                imgExploreSix.setVisibility(View.VISIBLE);
                imgExploreSix.startAnimation(expandInSix);


                expandInSeven = AnimationUtils.loadAnimation(mContext, R.anim.pop_anim);
                expandInSeven.setStartOffset(1800);
                imgExploreSeven.setVisibility(View.VISIBLE);
                imgExploreSeven.startAnimation(expandInSeven);
                expandInSeven.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //Toggle view visibility
                        btnReplayAnimation.setVisibility(View.VISIBLE);
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

    //endregion
}
