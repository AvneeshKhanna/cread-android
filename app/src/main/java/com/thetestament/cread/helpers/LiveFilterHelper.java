package com.thetestament.cread.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.thetestament.cread.R;
import com.thetestament.cread.utils.ConfettiViewUtils;
import com.thetestament.cread.utils.Constant;

import nl.dionsegijn.konfetti.KonfettiView;

/**
 * Helper class which provides utility methods for live filters.
 */
public class LiveFilterHelper {


    //region :Live Filter names
    public static final String LIVE_FILTER_NONE = "None";
    public static final String LIVE_FILTER_SNOW = "Snow";
    public static final String LIVE_FILTER_RAIN = "Rain";
    public static final String LIVE_FILTER_BUBBLE = "Bubble";
    public static final String LIVE_FILTER_CONFETTI = "Confetti";
    //endregion

    //region :LiveFilter list
    public static String[] liveFilterList = {
            LIVE_FILTER_NONE
            , LIVE_FILTER_SNOW
            , LIVE_FILTER_RAIN
            , LIVE_FILTER_BUBBLE
            , LIVE_FILTER_CONFETTI
    };
    //endregion


    /**
     * Method to return drawable resource id for respective live filters.
     *
     * @param context Context to use.
     * @param name    Name of live filter.
     */
    public static Drawable getLiveFilterDrawable(String name, Context context) {
        switch (name) {
            case LIVE_FILTER_NONE:
                return ContextCompat.getDrawable(context, R.drawable.img_none_preview);
            case LIVE_FILTER_SNOW:
                return ContextCompat.getDrawable(context, R.drawable.img_snow_fall_preview);
            case LIVE_FILTER_RAIN:
                return ContextCompat.getDrawable(context, R.drawable.img_rain_preview);
            case LIVE_FILTER_BUBBLE:
                return ContextCompat.getDrawable(context, R.drawable.img_bubble_preview);
            case LIVE_FILTER_CONFETTI:
                return ContextCompat.getDrawable(context, R.drawable.img_confetti_preview);
            default:
                return ContextCompat.getDrawable(context, R.drawable.img_none_preview);
        }
    }


    /**
     * Method to initialize live filter.
     *
     * @param filterName   Name of filter to be applied.
     * @param weatherView  WeatherView for snow and rain filters.
     * @param konfettiView KonfettiView view for confetti filter.
     * @param bubbleView   BubbleView for bubble filter.
     * @param context      Context to use.
     */
    public static void initLiveFilters(String filterName, WeatherView weatherView, KonfettiView konfettiView, GravView bubbleView, Context context) {
        switch (filterName) {
            case Constant.LIVE_FILTER_SNOW:
                weatherView.setWeatherData(PrecipType.SNOW);
                weatherView.setVisibility(View.VISIBLE);

                //Toggle view visibility
                konfettiView.setVisibility(View.GONE);
                bubbleView.setVisibility(View.INVISIBLE);
                break;
            case Constant.LIVE_FILTER_RAIN:
                weatherView.setWeatherData(PrecipType.RAIN);
                weatherView.setVisibility(View.VISIBLE);
                //Toggle view visibility
                konfettiView.setVisibility(View.GONE);
                bubbleView.setVisibility(View.INVISIBLE);
                break;
            case Constant.LIVE_FILTER_BUBBLE:
                bubbleView.setVisibility(View.VISIBLE);
                bubbleView.start();
                //Toggle view visibility
                konfettiView.setVisibility(View.GONE);
                weatherView.setVisibility(View.GONE);
                break;
            case Constant.LIVE_FILTER_CONFETTI:
                konfettiView.setVisibility(View.VISIBLE);
                new ConfettiViewUtils().showKonfettiInstance(konfettiView, context);
                //Toggle view visibility
                bubbleView.setVisibility(View.INVISIBLE);
                weatherView.setVisibility(View.GONE);
                break;
            case Constant.LIVE_FILTER_NONE:
                //Toggle view visibility
                konfettiView.setVisibility(View.GONE);
                bubbleView.setVisibility(View.INVISIBLE);
                weatherView.setVisibility(View.GONE);
                //do nothing
                break;
            default:
                //Toggle view visibility
                konfettiView.setVisibility(View.GONE);
                bubbleView.setVisibility(View.INVISIBLE);
                weatherView.setVisibility(View.GONE);
                break;
        }
    }

}
