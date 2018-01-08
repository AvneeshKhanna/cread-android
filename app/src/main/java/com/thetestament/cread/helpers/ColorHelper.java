package com.thetestament.cread.helpers;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.thetestament.cread.R;

/**
 * Helper class to provide utility method for color related operation.
 */


public class ColorHelper {

    //Color variant
    public static final String COlOR_BLACK = "colorBlack";
    public static final String COLOR_WHITE = "colorWhite";
    public static final String COLOR_GREY_500 = "colorGrey500";
    public static final String COLOR_GREY_600 = "colorGrey600";
    public static final String COLOR_BLUE_GREY_500 = "colorBlueGrey500";
    public static final String COLOR_BROWN_400 = "colorBrown400";
    public static final String COLOR_BROWN_500 = "colorBrown500";
    public static final String COLOR_DEEP_ORANGE_500 = "colorDeepOrange500";
    public static final String COLOR_ORANGE_500 = "colorOrange500";
    public static final String COLOR_AMBER_500 = "colorOAmber500";
    public static final String COLOR_YELLOW_500 = "colorYellow500";
    public static final String COLOR_LIME_500 = "colorLime500";
    public static final String COLOR_LIGHT_GREEN_500 = "colorLightGreen500";
    public static final String COLOR_GREEN_500 = "colorGreen500";
    public static final String COLOR_TEAL_500 = "colorTeal500";
    public static final String COLOR_CYAN_500 = "colorCyan500";
    public static final String COLOR_LIGHT_BLUE_500 = "colorLightBlue500";
    public static final String COLOR_BLUE_500 = "colorBlue500";
    public static final String COLOR_BLUE_700 = "colorBlue700";
    public static final String COLOR_INDIGO_500 = "colorIndigo500";
    public static final String COLOR_DEEP_PURPLE_500 = "colorDeepPurple500";
    public static final String COLOR_PURPLE_300 = "colorPurple300";
    public static final String COLOR_PURPLE_400 = "colorPurple400";
    public static final String COLOR_PURPLE_500 = "colorPurple500";
    public static final String COLOR_PINK_500 = "colorPink500";
    public static final String COLOR_RED_300 = "colorRed300";
    public static final String COLOR_RED_400 = "colorRed400";
    public static final String COLOR_RED_500 = "colorRed500";


    //color  list
    public static String[] colorList = {
            COlOR_BLACK
            , COLOR_WHITE
            , COLOR_GREY_500
            , COLOR_GREY_600
            , COLOR_BLUE_GREY_500
            , COLOR_BROWN_400
            , COLOR_BROWN_500
            , COLOR_DEEP_ORANGE_500
            , COLOR_ORANGE_500
            , COLOR_AMBER_500
            , COLOR_YELLOW_500
            , COLOR_LIME_500
            , COLOR_LIGHT_GREEN_500
            , COLOR_GREEN_500
            , COLOR_TEAL_500
            , COLOR_CYAN_500
            , COLOR_LIGHT_BLUE_500
            , COLOR_BLUE_500
            , COLOR_BLUE_700
            , COLOR_INDIGO_500
            , COLOR_DEEP_PURPLE_500
            , COLOR_PURPLE_300
            , COLOR_PURPLE_400
            , COLOR_PURPLE_500
            , COLOR_PINK_500
            , COLOR_RED_300
            , COLOR_RED_400
            , COLOR_RED_500
    };


    /**
     * Method to return color value.
     *
     * @param context    Context to use.
     * @param colorValue color name.
     */
    public static int getColorValue(String colorValue, Context context) {
        switch (colorValue) {
            case COlOR_BLACK:
                return ContextCompat.getColor(context, R.color.color_black);
            case COLOR_WHITE:
                return ContextCompat.getColor(context, R.color.color_white);
            case COLOR_GREY_500:
                return ContextCompat.getColor(context, R.color.color_grey_500);
            case COLOR_GREY_600:
                return ContextCompat.getColor(context, R.color.color_grey_600);
            case COLOR_BLUE_GREY_500:
                return ContextCompat.getColor(context, R.color.color_blue_grey_500);
            case COLOR_BROWN_400:
                return ContextCompat.getColor(context, R.color.color_brown_400);
            case COLOR_BROWN_500:
                return ContextCompat.getColor(context, R.color.color_brown_500);
            case COLOR_DEEP_ORANGE_500:
                return ContextCompat.getColor(context, R.color.color_deep_orange_500);
            case COLOR_ORANGE_500:
                return ContextCompat.getColor(context, R.color.color_orange_500);
            case COLOR_AMBER_500:
                return ContextCompat.getColor(context, R.color.color_amber_500);
            case COLOR_YELLOW_500:
                return ContextCompat.getColor(context, R.color.color_Yellow_500);
            case COLOR_LIME_500:
                return ContextCompat.getColor(context, R.color.color_lime_500);
            case COLOR_LIGHT_GREEN_500:
                return ContextCompat.getColor(context, R.color.color_light_green_500);
            case COLOR_GREEN_500:
                return ContextCompat.getColor(context, R.color.color_green_500);
            case COLOR_TEAL_500:
                return ContextCompat.getColor(context, R.color.color_teal_500);
            case COLOR_CYAN_500:
                return ContextCompat.getColor(context, R.color.color_cyan_500);
            case COLOR_LIGHT_BLUE_500:
                return ContextCompat.getColor(context, R.color.color_light_blue_500);
            case COLOR_BLUE_500:
                return ContextCompat.getColor(context, R.color.color_blue_500);
            case COLOR_BLUE_700:
                return ContextCompat.getColor(context, R.color.color_blue_700);
            case COLOR_INDIGO_500:
                return ContextCompat.getColor(context, R.color.color_indigo_500);
            case COLOR_DEEP_PURPLE_500:
                return ContextCompat.getColor(context, R.color.color_deep_purple_500);
            case COLOR_PURPLE_300:
                return ContextCompat.getColor(context, R.color.color_purple_300);
            case COLOR_PURPLE_400:
                return ContextCompat.getColor(context, R.color.color_purple_400);
            case COLOR_PURPLE_500:
                return ContextCompat.getColor(context, R.color.color_purple_500);
            case COLOR_PINK_500:
                return ContextCompat.getColor(context, R.color.color_pink_500);
            case COLOR_RED_300:
                return ContextCompat.getColor(context, R.color.color_red_300);
            case COLOR_RED_400:
                return ContextCompat.getColor(context, R.color.color_red_400);
            case COLOR_RED_500:
                return ContextCompat.getColor(context, R.color.color_red_500);
            default:
                return ContextCompat.getColor(context, R.color.color_white);
        }
    }

}
