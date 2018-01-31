package com.thetestament.cread.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;

import com.thetestament.cread.R;

/**
 * Helper class to provide utility method for font related operation.
 */

public class FontsHelper {

    //region Font types
    public static final String FONT_TYPE_AMATIC_SC_REGULAR = "amatic_sc_regular.ttf";
    public static final String FONT_TYPE_BARLOW_CONDENSED_REGULAR = "barlow_condensed_regular.ttf";
    public static final String FONT_TYPE_BOHEMIAN_TYPEWRITER = "bohemian_typewriter.ttf";
    public static final String FONT_TYPE_BROTHER_DELUXE = "brother_deluxe.ttf";
    public static final String FONT_TYPE_CABIN_SKETCH_REGULAR = "cabin_sketch_regular.ttf";
    public static final String FONT_TYPE_HELVETICA_NEUE_MEDIUM = "helvetica_neue_medium.ttf";
    public static final String FONT_TYPE_INDIE_FLOWER = "indie_flower.ttf";
    public static final String FONT_TYPE_MONTSERRAT_REGULAR = "montserrat_regular.ttf";
    public static final String FONT_TYPE_OSWALD_REGULAR = "oswald_regular.ttf";
    public static final String FONT_TYPE_PLAYFAIR_DISPLAY_REGULAR = "playfair_display_regular.ttf";
    public static final String FONT_TYPE_POIRET_ONE_REGULAR = "poiret_one_regular.ttf";
    public static final String FONT_TYPE_SHADOWS_INTO_LIGHT = "shadows_into_light.ttf";
    public static final String FONT_TYPE_SPECTRA_ISC_REGULAR = "spectra_isc_regular.ttf";
    public static final String FONT_TYPE_TITILLIUM_WEB_REGULAR = "titillium_web_regular.ttf";
    public static final String FONT_TYPE_UBUNTU_MEDIUM = "ubuntu_medium.ttf";
    public static final String FONT_TYPE_ITALIANNO_REGULAR = "italianno_regular.ttf";
    public static final String FONT_TYPE_LALEZAR_REGULAR = "lalezar_regular.ttf";
    public static final String FONT_TYPE_MARVEL_REGULAR = "marvel_regular.ttf";
    public static final String FONT_TYPE_TANGERINE_REGULAR = "tangerine_regular.ttf";
    public static final String FONT_TYPE_TENOR_SANS_REGULAR = "tenor_sans_regular.ttf";
    public static final String FONT_TYPE_TEXT_ME_ONE_REGULAR = "text_me_one_regular.ttf";
    public static final String FONT_TYPE_VIGA_REGULAR = "viga_regular.ttf";
    public static final String FONT_TYPE_WALTERTURNCOAT_REGULAR = "walterturncoat_regular.ttf";
    public static final String FONT_TYPE_RALEWAY_LIGHT = "raleway_light.ttf";
    //endregion

    //region :Font list
    public static String[] fontTypes = {
            FONT_TYPE_BOHEMIAN_TYPEWRITER
            , FONT_TYPE_BROTHER_DELUXE
            , FONT_TYPE_MONTSERRAT_REGULAR
            , FONT_TYPE_AMATIC_SC_REGULAR
            , FONT_TYPE_BARLOW_CONDENSED_REGULAR
            , FONT_TYPE_CABIN_SKETCH_REGULAR
            , FONT_TYPE_HELVETICA_NEUE_MEDIUM
            , FONT_TYPE_INDIE_FLOWER
            , FONT_TYPE_OSWALD_REGULAR
            , FONT_TYPE_PLAYFAIR_DISPLAY_REGULAR
            , FONT_TYPE_POIRET_ONE_REGULAR
            , FONT_TYPE_SHADOWS_INTO_LIGHT
            , FONT_TYPE_SPECTRA_ISC_REGULAR
            , FONT_TYPE_TITILLIUM_WEB_REGULAR
            , FONT_TYPE_UBUNTU_MEDIUM
            , FONT_TYPE_ITALIANNO_REGULAR
            , FONT_TYPE_LALEZAR_REGULAR
            , FONT_TYPE_MARVEL_REGULAR
            , FONT_TYPE_TANGERINE_REGULAR
            , FONT_TYPE_TENOR_SANS_REGULAR
            , FONT_TYPE_TEXT_ME_ONE_REGULAR
            , FONT_TYPE_VIGA_REGULAR
            , FONT_TYPE_WALTERTURNCOAT_REGULAR
            , FONT_TYPE_RALEWAY_LIGHT
    };
    //endregion

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
            case FONT_TYPE_BOHEMIAN_TYPEWRITER:
                return ResourcesCompat.getFont(context, R.font.bohemian_typewriter);
            case FONT_TYPE_BROTHER_DELUXE:
                return ResourcesCompat.getFont(context, R.font.brother_deluxe);
            case FONT_TYPE_CABIN_SKETCH_REGULAR:
                return ResourcesCompat.getFont(context, R.font.cabin_sketch_regular);
            case FONT_TYPE_HELVETICA_NEUE_MEDIUM:
                return ResourcesCompat.getFont(context, R.font.helvetica_neue_medium);
            case FONT_TYPE_INDIE_FLOWER:
                return ResourcesCompat.getFont(context, R.font.indie_flower);
            case FONT_TYPE_MONTSERRAT_REGULAR:
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
            case FONT_TYPE_ITALIANNO_REGULAR:
                return ResourcesCompat.getFont(context, R.font.italianno_regular);
            case FONT_TYPE_LALEZAR_REGULAR:
                return ResourcesCompat.getFont(context, R.font.lalezar_regular);
            case FONT_TYPE_MARVEL_REGULAR:
                return ResourcesCompat.getFont(context, R.font.marvel_regular);
            case FONT_TYPE_TANGERINE_REGULAR:
                return ResourcesCompat.getFont(context, R.font.tangerine_regular);
            case FONT_TYPE_TENOR_SANS_REGULAR:
                return ResourcesCompat.getFont(context, R.font.tenor_sans_regular);
            case FONT_TYPE_TEXT_ME_ONE_REGULAR:
                return ResourcesCompat.getFont(context, R.font.text_me_one_regular);
            case FONT_TYPE_VIGA_REGULAR:
                return ResourcesCompat.getFont(context, R.font.viga_regular);
            case FONT_TYPE_WALTERTURNCOAT_REGULAR:
                return ResourcesCompat.getFont(context, R.font.walterturncoat_regular);
            case FONT_TYPE_RALEWAY_LIGHT:
                return ResourcesCompat.getFont(context, R.font.raleway_light);
            default:
                return ResourcesCompat.getFont(context, R.font.ubuntu_medium);
        }
    }
}
