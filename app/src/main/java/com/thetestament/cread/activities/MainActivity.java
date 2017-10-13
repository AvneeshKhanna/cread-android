package com.thetestament.cread.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.adapters.IntroViewPagerAdapter;
import com.thetestament.cread.helpers.IntroPageTransformerHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    //Array to store  sliders layout
    @BindView(R.id.dotsLayout)
    LinearLayout dotsLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

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
      /*  startActivity(new Intent(MainActivity.this, LogInActivity.class));
        finish();*/
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
}