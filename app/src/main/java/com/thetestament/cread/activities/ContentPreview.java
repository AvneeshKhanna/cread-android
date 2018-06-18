package com.thetestament.cread.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.AspectRatioUtils;
import com.thetestament.cread.utils.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.dionsegijn.konfetti.KonfettiView;

import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_IMAGE_HEIGHT;
import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_IMAGE_WIDTH;

/**
 * Appcompat activity to show content preview in fullscreen mode.
 */

public class ContentPreview extends BaseActivity {


    //region :Views binding with butter knife
    @BindView(R.id.root_view)
    RelativeLayout rootView;
    @BindView(R.id.image_content_preview)
    AppCompatImageView imageContentPreview;
    @BindView(R.id.live_filter_bubble)
    GravView liveFilterBubble;
    @BindView(R.id.whether_view)
    WeatherView whetherView;
    @BindView(R.id.konfetti_view)
    KonfettiView konfettiView;
    @BindView(R.id.textSignature)
    AppCompatTextView textSignature;
    //endregion

    //region :Fields and constants
    Context mContext;


    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_preview);
        ButterKnife.bind(this);
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

    //region :Private method

    /**
     * Initialize views for this screen.
     */
    private void initView() {
        //Obtain reference
        mContext = this;

        //Retrieve bundle data here
        Bundle bundle = getIntent().getBundleExtra(CONTENT_PREVIEW_EXTRA_DATA);

        //Set  aspect ration
        AspectRatioUtils.setImageAspectRatio(bundle.getInt(CONTENT_PREVIEW_EXTRA_IMAGE_WIDTH)
                , bundle.getInt(CONTENT_PREVIEW_EXTRA_IMAGE_HEIGHT)
                , imageContentPreview
        ,true);

        //Load image
        ImageHelper.loadImageFromPicasso(mContext
                , imageContentPreview
                , bundle.getString(Constant.CONTENT_PREVIEW_EXTRA_IMAGE_URL)
                , R.drawable.image_placeholder);

        //if signature text was not empty
        if (!TextUtils.isEmpty(bundle.getString(Constant.CONTENT_PREVIEW_EXTRA_SIGNATURE_TEXT))) {
            //Set text and its visibility
            textSignature.setVisibility(View.VISIBLE);
            textSignature.setText(bundle.getString(Constant.CONTENT_PREVIEW_EXTRA_SIGNATURE_TEXT));
        }

        initLiveFilters(bundle.getString(Constant.CONTENT_PREVIEW_EXTRA_LIVE_FILTER_NAME));
    }

    /**
     * Method to initialize live filter.
     *
     * @param filterName Name of filter to be applied.
     */
    private void initLiveFilters(String filterName) {
        switch (filterName) {
            case Constant.LIVE_FILTER_SNOW:
                whetherView.setWeatherData(PrecipType.SNOW);
                whetherView.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_RAIN:
                whetherView.setWeatherData(PrecipType.RAIN);
                whetherView.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_BUBBLE:
                liveFilterBubble.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_CONFETTI:
                konfettiView.setVisibility(View.VISIBLE);
                ViewHelper.showKonfetti(konfettiView);
                break;
            case Constant.LIVE_FILTER_NONE:
                //do nothing
                break;
        }
    }
    //endregion
}
