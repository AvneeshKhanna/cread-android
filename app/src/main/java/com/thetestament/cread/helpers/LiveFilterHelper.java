package com.thetestament.cread.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.thetestament.cread.R;

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


}
