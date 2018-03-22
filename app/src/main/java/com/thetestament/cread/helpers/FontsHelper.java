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
    public static final String FONT_TYPE_POIRET_ONE_REGULAR = "poiret_one_regular.ttf";
    public static final String FONT_TYPE_WALTERTURNCOAT_REGULAR = "walterturncoat_regular.ttf";
    public static final String FONT_TYPE_RALEWAY_LIGHT = "raleway_light.ttf";
    public static final String FONT_TYPE_A_LOVE_OF_THUNDER = "a_love_of_thunder.ttf";
    public static final String FONT_TYPE_BABAS = "babas.ttf";
    public static final String FONT_TYPE_BELLE_ROSE = "bellerose.ttf";
    public static final String FONT_TYPE_BLACKOUT_SUNRISE = "blackout_sunrise.ttf";
    public static final String FONT_TYPE_BLACKOUT_TWOAM = "blackout_twoam.ttf";
    public static final String FONT_TYPE_BRAIN_FLOWER = "brain_flower.ttf";
    public static final String FONT_TYPE_COMFORTAA = "comfortaa.ttf";
    public static final String FONT_TYPE_FOLK_SOLID = "folk_solid.otf";
    public static final String FONT_TYPE_FRESSH = "fressh.ttf";
    public static final String FONT_TYPE_GARAGE_GOTHIC = "garage_gothic.otf";
    public static final String FONT_TYPE_GEO_SANS = "geosans.ttf";
    public static final String FONT_TYPE_GROTA_SANS_ROUNDED = "grota_sansrounded.otf";
    public static final String FONT_TYPE_HOMESTEAD = "homestead.ttf";
    public static final String FONT_TYPE_JACKALOPE = "jackalope.otf";
    public static final String FONT_TYPE_JOYFUL_JULIANA = "joyful_juliana.ttf";
    public static final String FONT_TYPE_JUICE = "juice.ttf";
    public static final String FONT_TYPE_KOMIKAAXIS = "komikaaxis.ttf";
    public static final String FONT_TYPE_LANGDON = "langdon.otf";
    public static final String FONT_TYPE_LUNCHBOX_BOLD = "lunchbox_bold.otf";
    public static final String FONT_TYPE_MAVEN = "maven.otf";
    public static final String FONT_TYPE_METROPOLIS_1920 = "metropolis_1920.otf";
    public static final String FONT_TYPE_MUSEO_SANS_500 = "museo_sans_500.otf";
    public static final String FONT_TYPE_NEO_RETRO_DRAW = "neo_retro_draw.ttf";
    public static final String FONT_TYPE_OSTRICH_ROUNDED = "ostrich_rounded.ttf";
    public static final String FONT_TYPE_PACIFICO = "pacifico.ttf";
    public static final String FONT_TYPE_QUESTA_GRANDE = "questa_grande.otf";
    public static final String FONT_TYPE_QUICKSAND = "quicksand.otf";
    public static final String FONT_TYPE_RIBBON = "ribbon.otf";
    public static final String FONT_TYPE_THUNDER_PANTS = "thunder_pants.otf";
    public static final String FONT_TYPE_TREND_HANDMADE = "trend_handmade.otf";
    public static final String FONT_TYPE_VENERA = "venera.otf";
    public static final String FONT_TYPE_VETKA = "vetka.otf";
    public static final String FONT_TYPE_VINDENCE = "vindence.otf";
    public static final String FONT_TYPE_YANONE_KAFFEESATZ = "yanone_kaffeesatz.otf";
    public static final String FONT_TYPE_YONDER = "yonder.otf";
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
            , FONT_TYPE_POIRET_ONE_REGULAR
            , FONT_TYPE_WALTERTURNCOAT_REGULAR
            , FONT_TYPE_RALEWAY_LIGHT
            , FONT_TYPE_A_LOVE_OF_THUNDER
            , FONT_TYPE_BABAS
            , FONT_TYPE_BELLE_ROSE
            , FONT_TYPE_BLACKOUT_SUNRISE
            , FONT_TYPE_BLACKOUT_TWOAM
            , FONT_TYPE_BRAIN_FLOWER
            , FONT_TYPE_COMFORTAA
            , FONT_TYPE_FOLK_SOLID
            , FONT_TYPE_FRESSH
            , FONT_TYPE_GARAGE_GOTHIC
            , FONT_TYPE_GEO_SANS
            , FONT_TYPE_GROTA_SANS_ROUNDED
            , FONT_TYPE_HOMESTEAD
            , FONT_TYPE_JACKALOPE
            , FONT_TYPE_JOYFUL_JULIANA
            , FONT_TYPE_JUICE
            , FONT_TYPE_KOMIKAAXIS
            , FONT_TYPE_LANGDON
            , FONT_TYPE_LUNCHBOX_BOLD
            , FONT_TYPE_MAVEN
            , FONT_TYPE_METROPOLIS_1920
            , FONT_TYPE_MUSEO_SANS_500
            , FONT_TYPE_NEO_RETRO_DRAW
            , FONT_TYPE_OSTRICH_ROUNDED
            , FONT_TYPE_PACIFICO
            , FONT_TYPE_QUESTA_GRANDE
            , FONT_TYPE_QUICKSAND
            , FONT_TYPE_RIBBON//
            , FONT_TYPE_THUNDER_PANTS
            , FONT_TYPE_TREND_HANDMADE
            , FONT_TYPE_VENERA
            , FONT_TYPE_VETKA
            , FONT_TYPE_VINDENCE
            , FONT_TYPE_YANONE_KAFFEESATZ
            , FONT_TYPE_YONDER

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
            case FONT_TYPE_POIRET_ONE_REGULAR:
                return ResourcesCompat.getFont(context, R.font.poiret_one_regular);
            case FONT_TYPE_WALTERTURNCOAT_REGULAR:
                return ResourcesCompat.getFont(context, R.font.walterturncoat_regular);
            case FONT_TYPE_RALEWAY_LIGHT:
                return ResourcesCompat.getFont(context, R.font.raleway_light);
            case FONT_TYPE_A_LOVE_OF_THUNDER:
                return ResourcesCompat.getFont(context, R.font.a_love_of_thunder);
            case FONT_TYPE_BABAS:
                return ResourcesCompat.getFont(context, R.font.bebas);
            case FONT_TYPE_BELLE_ROSE:
                return ResourcesCompat.getFont(context, R.font.bellerose);
            case FONT_TYPE_BLACKOUT_SUNRISE:
                return ResourcesCompat.getFont(context, R.font.blackout_sunrise);
            case FONT_TYPE_BLACKOUT_TWOAM:
                return ResourcesCompat.getFont(context, R.font.blackout_twoam);
            case FONT_TYPE_BRAIN_FLOWER:
                return ResourcesCompat.getFont(context, R.font.brain_flower);
            case FONT_TYPE_COMFORTAA:
                return ResourcesCompat.getFont(context, R.font.comfortaa);
            case FONT_TYPE_FOLK_SOLID:
                return ResourcesCompat.getFont(context, R.font.folk_solid);
            case FONT_TYPE_FRESSH:
                return ResourcesCompat.getFont(context, R.font.fressh);
            case FONT_TYPE_GARAGE_GOTHIC:
                return ResourcesCompat.getFont(context, R.font.garage_gothic);
            case FONT_TYPE_GEO_SANS:
                return ResourcesCompat.getFont(context, R.font.geosans);
            case FONT_TYPE_GROTA_SANS_ROUNDED:
                return ResourcesCompat.getFont(context, R.font.grota_sansrounded);
            case FONT_TYPE_HOMESTEAD:
                return ResourcesCompat.getFont(context, R.font.homestead);
            case FONT_TYPE_JACKALOPE:
                return ResourcesCompat.getFont(context, R.font.jackalope);
            case FONT_TYPE_JOYFUL_JULIANA:
                return ResourcesCompat.getFont(context, R.font.joyful_juliana);
            case FONT_TYPE_JUICE:
                return ResourcesCompat.getFont(context, R.font.juice);
            case FONT_TYPE_KOMIKAAXIS:
                return ResourcesCompat.getFont(context, R.font.komikaaxis);
            case FONT_TYPE_LANGDON:
                return ResourcesCompat.getFont(context, R.font.langdon);
            case FONT_TYPE_LUNCHBOX_BOLD:
                return ResourcesCompat.getFont(context, R.font.lunchbox_bold);
            case FONT_TYPE_MAVEN:
                return ResourcesCompat.getFont(context, R.font.maven);
            case FONT_TYPE_METROPOLIS_1920:
                return ResourcesCompat.getFont(context, R.font.metropolis_1920);
            case FONT_TYPE_MUSEO_SANS_500:
                return ResourcesCompat.getFont(context, R.font.museo_sans_500);
            case FONT_TYPE_NEO_RETRO_DRAW:
                return ResourcesCompat.getFont(context, R.font.neo_retro_draw);
            case FONT_TYPE_OSTRICH_ROUNDED:
                return ResourcesCompat.getFont(context, R.font.ostrich_rounded);
            case FONT_TYPE_PACIFICO:
                return ResourcesCompat.getFont(context, R.font.pacifico);
            case FONT_TYPE_QUESTA_GRANDE:
                return ResourcesCompat.getFont(context, R.font.questa_grande);
            case FONT_TYPE_QUICKSAND:
                return ResourcesCompat.getFont(context, R.font.quicksand);
            case FONT_TYPE_RIBBON:
                return ResourcesCompat.getFont(context, R.font.ribbon);
            case FONT_TYPE_THUNDER_PANTS:
                return ResourcesCompat.getFont(context, R.font.thunder_pants);
            case FONT_TYPE_TREND_HANDMADE:
                return ResourcesCompat.getFont(context, R.font.trend_handmade);
            case FONT_TYPE_VENERA:
                return ResourcesCompat.getFont(context, R.font.venera);
            case FONT_TYPE_VETKA:
                return ResourcesCompat.getFont(context, R.font.vetka);
            case FONT_TYPE_VINDENCE:
                return ResourcesCompat.getFont(context, R.font.vindence);
            case FONT_TYPE_YANONE_KAFFEESATZ:
                return ResourcesCompat.getFont(context, R.font.yanone_kaffeesatz);
            case FONT_TYPE_YONDER:
                return ResourcesCompat.getFont(context, R.font.yonder);
            default:
                return ResourcesCompat.getFont(context, R.font.thunder_pants);
        }
    }


    /**
     * Method to return font name.
     *
     * @param fontType Font type.
     */
    public static String getTypeFaceName(String fontType) {
        switch (fontType) {
            case FONT_TYPE_AMATIC_SC_REGULAR:
                return "Amatic";
            case FONT_TYPE_BARLOW_CONDENSED_REGULAR:
                return "Barlow";
            case FONT_TYPE_BOHEMIAN_TYPEWRITER:
                return "Bohemian";
            case FONT_TYPE_BROTHER_DELUXE:
                return "Brother Deluxe";
            case FONT_TYPE_CABIN_SKETCH_REGULAR:
                return "Cabin Sketch";
            case FONT_TYPE_HELVETICA_NEUE_MEDIUM:
                return "Helevetica";
            case FONT_TYPE_INDIE_FLOWER:
                return "Indie Flower";
            case FONT_TYPE_MONTSERRAT_REGULAR:
                return "Montserrat";
            case FONT_TYPE_POIRET_ONE_REGULAR:
                return "Poiret";
            case FONT_TYPE_WALTERTURNCOAT_REGULAR:
                return "Walter";
            case FONT_TYPE_RALEWAY_LIGHT:
                return "Raleway";
            case FONT_TYPE_A_LOVE_OF_THUNDER:
                return "Love Thunder";
            case FONT_TYPE_BABAS:
                return "Babes";
            case FONT_TYPE_BELLE_ROSE:
                return "Belle Rose";
            case FONT_TYPE_BLACKOUT_SUNRISE:
                return "Blackout";
            case FONT_TYPE_BLACKOUT_TWOAM:
                return "Twoan";
            case FONT_TYPE_BRAIN_FLOWER:
                return "Brain Flower";
            case FONT_TYPE_COMFORTAA:
                return "Comfortaa";
            case FONT_TYPE_FOLK_SOLID:
                return "Folk Solid";
            case FONT_TYPE_FRESSH:
                return "Fresh";
            case FONT_TYPE_GARAGE_GOTHIC:
                return "Garage Gothic";
            case FONT_TYPE_GEO_SANS:
                return "Geo Sans";
            case FONT_TYPE_GROTA_SANS_ROUNDED:
                return "Grota";
            case FONT_TYPE_HOMESTEAD:
                return "Homestead";
            case FONT_TYPE_JACKALOPE:
                return "Jackalope";
            case FONT_TYPE_JOYFUL_JULIANA:
                return "Joyful Juliana";
            case FONT_TYPE_JUICE:
                return "Juice";
            case FONT_TYPE_KOMIKAAXIS:
                return "Komikaaxis";
            case FONT_TYPE_LANGDON:
                return "Langdon";
            case FONT_TYPE_LUNCHBOX_BOLD:
                return "Lunchbox";
            case FONT_TYPE_MAVEN:
                return "Maven";
            case FONT_TYPE_METROPOLIS_1920:
                return "Metropolis";
            case FONT_TYPE_MUSEO_SANS_500:
                return "Museo";
            case FONT_TYPE_NEO_RETRO_DRAW:
                return "Neo Retro";
            case FONT_TYPE_OSTRICH_ROUNDED:
                return "Ostrich";
            case FONT_TYPE_PACIFICO:
                return "Pacifico";
            case FONT_TYPE_QUESTA_GRANDE:
                return "Questa Grande";
            case FONT_TYPE_QUICKSAND:
                return "Quicksand";
            case FONT_TYPE_RIBBON:
                return "Ribbon";
            case FONT_TYPE_THUNDER_PANTS:
                return "Thunder";
            case FONT_TYPE_TREND_HANDMADE:
                return "Trend";
            case FONT_TYPE_VENERA:
                return "Venera";
            case FONT_TYPE_VETKA:
                return "Vetks";
            case FONT_TYPE_VINDENCE:
                return "Vindence";
            case FONT_TYPE_YANONE_KAFFEESATZ:
                return "Yanone";
            case FONT_TYPE_YONDER:
                return "Yonder";
            default:
                return "Font";
        }
    }
}
