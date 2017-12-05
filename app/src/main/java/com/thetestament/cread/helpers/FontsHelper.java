package com.thetestament.cread.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;

import com.thetestament.cread.R;

/**
 * Helper class to provide utility method for font related operation.
 */

public class FontsHelper {
    //Font types
    public static final String FONT_TYPE_AMATIC_SC_REGULAR = "amatic_sc_regular.ttf";
    public static final String FONT_TYPE_BARLOW_CONDENSED_REGULAR = "barlow_condensed_regular.ttf";
    public static final String FONT_TYPE_CABIN_SKETCH_REGULAR = "cabin_sketch_regular.ttf";
    public static final String FONT_TYPE_HELVETICA_NEUE_MEDUIM = "helvetica_neue_medium.ttf";
    public static final String FONT_TYPE_INDIE_FLOWER = "indie_flower.ttf";
    public static final String FONT_TYPE_MOSTSERRAT_REGULAR = "montserrat_regular.ttf";
    public static final String FONT_TYPE_OSWALD_REGULAR = "oswald_regular.ttf";
    public static final String FONT_TYPE_PLAYFAIR_DISPLAY_REGULAR = "playfair_display_regular.ttf";
    public static final String FONT_TYPE_POIRET_ONE_REGULAR = "poiret_one_regular.ttf";
    public static final String FONT_TYPE_SHADOWS_INTO_LIGHT = "shadows_into_light.ttf";
    public static final String FONT_TYPE_SPECTRA_ISC_REGULAR = "spectra_isc_regular.ttf";
    public static final String FONT_TYPE_TITILLIUM_WEB_REGULAR = "titillium_web_regular.ttf";
    public static final String FONT_TYPE_UBUNTU_MEDIUM = "ubuntu_medium.ttf";


    //Font list
    public static String[] fontTypes = {
            FONT_TYPE_AMATIC_SC_REGULAR
            , FONT_TYPE_BARLOW_CONDENSED_REGULAR
            , FONT_TYPE_CABIN_SKETCH_REGULAR
            , FONT_TYPE_HELVETICA_NEUE_MEDUIM
            , FONT_TYPE_INDIE_FLOWER
            , FONT_TYPE_MOSTSERRAT_REGULAR
            , FONT_TYPE_OSWALD_REGULAR
            , FONT_TYPE_PLAYFAIR_DISPLAY_REGULAR
            , FONT_TYPE_POIRET_ONE_REGULAR
            , FONT_TYPE_SHADOWS_INTO_LIGHT
            , FONT_TYPE_SPECTRA_ISC_REGULAR
            , FONT_TYPE_TITILLIUM_WEB_REGULAR
            , FONT_TYPE_UBUNTU_MEDIUM
    };

    /**
     * Method to return typeFace
     *
     * @param context  Context to use.
     * @param fontType Font type.
     */
    public static Typeface getFontType(String fontType, Context context) {
        switch (fontType) {
            case FONT_TYPE_AMATIC_SC_REGULAR:
                return ResourcesCompat.getFont(context, R.font.amatic_sc_regular);
            case FONT_TYPE_BARLOW_CONDENSED_REGULAR:
                return ResourcesCompat.getFont(context, R.font.barlow_condensed_regular);
            case FONT_TYPE_CABIN_SKETCH_REGULAR:
                return ResourcesCompat.getFont(context, R.font.cabin_sketch_regular);
            case FONT_TYPE_HELVETICA_NEUE_MEDUIM:
                return ResourcesCompat.getFont(context, R.font.helvetica_neue_medium);
            case FONT_TYPE_INDIE_FLOWER:
                return ResourcesCompat.getFont(context, R.font.indie_flower);
            case FONT_TYPE_MOSTSERRAT_REGULAR:
                return ResourcesCompat.getFont(context, R.font.montserrat_regular);
            case FONT_TYPE_OSWALD_REGULAR:
                return ResourcesCompat.getFont(context, R.font.oswald_regular);
            case FONT_TYPE_PLAYFAIR_DISPLAY_REGULAR:
                return ResourcesCompat.getFont(context, R.font.playfair_display_regular);
            case FONT_TYPE_POIRET_ONE_REGULAR:
                return ResourcesCompat.getFont(context, R.font.poiret_one_regular);
            case FONT_TYPE_SHADOWS_INTO_LIGHT:
                return ResourcesCompat.getFont(context, R.font.shadows_into_light);
            case FONT_TYPE_SPECTRA_ISC_REGULAR:
                return ResourcesCompat.getFont(context, R.font.spectra_isc_regular);
            case FONT_TYPE_TITILLIUM_WEB_REGULAR:
                return ResourcesCompat.getFont(context, R.font.titillium_web_regular);
            case FONT_TYPE_UBUNTU_MEDIUM:
                return ResourcesCompat.getFont(context, R.font.ubuntu_medium);
            default:
                return ResourcesCompat.getFont(context, R.font.ubuntu_medium);
        }
    }
}
