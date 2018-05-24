package com.thetestament.cread.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.adapters.IntroViewPagerAdapter;
import com.thetestament.cread.adapters.ProductTourViewPagerAdapter;
import com.thetestament.cread.helpers.IntroPageTransformerHelper;
import com.thetestament.cread.helpers.ViewHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * AppCompatActivity for product tour feature.
 */

public class ProductTourActivity extends BaseActivity {

    //region :View binding with Butter knife
    @BindView(R.id.rootView)
    RelativeLayout rootView;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.dotsLayout)
    LinearLayout dotsLayout;

    //endregion

    //region :Field and constants
    /**
     * Flag to maintain reference of this activity.
     */
    ProductTourActivity mContext;
    private int[] mLayouts;

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
        //initView();
        viewPager.setAdapter(new ProductTourViewPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        addDots(0);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    //endregion


    //region :Private methods

    /**
     * Method to initialize views for this screen.
     */
    private void initView() {
        //obtain reference of this screen.
        mContext = this;
        //For sliders
        initSliders();
        // Add bottom dots
        addDots(0);
        //Initialize view pager
        initViewPager();
    }


    /**
     * Method to add dots  and to change the color of dots
     *
     * @param currentPage mGravityFlag of current page i.e 0(zero)
     */
    private void addDots(int currentPage) {
        TextView[] dots = new TextView[3];
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

}
