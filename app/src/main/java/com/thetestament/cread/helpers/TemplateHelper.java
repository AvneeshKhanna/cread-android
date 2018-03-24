package com.thetestament.cread.helpers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.thetestament.cread.R;

/**
 * Helper class to provide utility method for template related operation.
 */

/**
 * Helper class which provides utility methods for template related operations.
 */
public class TemplateHelper {

    //region :Template shape name
    public static final String SHAPE_NAME_NONE = "shape_none";
    public static final String SHAPE_NAME_QUOTE = "shape_quote";
    public static final String SHAPE_NAME_SIDE_LINE = "shape_side_line";
    public static final String SHAPE_NAME_TOP_BOTTOM_LINE = "shape_top_bottom_line";
    public static final String SHAPE_NAME_CORNER_LINE = "shape_corner_line";
    //endregion
    public static final int FONT_SIZE_DEFAULT = 16;
    public static final int FONT_SIZE_SMALL = 18;
    public static final int FONT_SIZE_MEDIUM = 24;
    public static final int FONT_SIZE_LARGE = 30;

    //region :Template names
    public static final String TEMPLATE_NONE = "none";
    public static final String TEMPLATE_1 = "template1";
    public static final String TEMPLATE_2 = "template2";
    public static final String TEMPLATE_3 = "template3";
    public static final String TEMPLATE_4 = "template4";
    public static final String TEMPLATE_5 = "template5";
    public static final String TEMPLATE_6 = "template6";
    public static final String TEMPLATE_7 = "template7";
    public static final String TEMPLATE_8 = "template8";
    public static final String TEMPLATE_9 = "template9";
    public static final String TEMPLATE_10 = "template10";
    public static final String TEMPLATE_11 = "template11";
    public static final String TEMPLATE_12 = "template12";
    public static final String TEMPLATE_13 = "template13";
    public static final String TEMPLATE_14 = "template14";
    public static final String TEMPLATE_15 = "template15";
    public static final String TEMPLATE_16 = "template16";
    public static final String TEMPLATE_17 = "template17";
    //endregion

    //region :Template list
    public static String[] templateList = {
            TEMPLATE_NONE
            , TEMPLATE_1
            , TEMPLATE_2
            , TEMPLATE_3
            , TEMPLATE_4
            , TEMPLATE_5
            , TEMPLATE_6
            , TEMPLATE_7
            , TEMPLATE_8
            , TEMPLATE_9
            , TEMPLATE_10
            , TEMPLATE_11
            , TEMPLATE_12
            , TEMPLATE_13
            , TEMPLATE_14
            , TEMPLATE_15
            , TEMPLATE_16
            , TEMPLATE_17
    };
    //endregion

    /**
     * Method to return drawable resource id for respective templates.
     *
     * @param context      Context to use.
     * @param templateName Name of template.
     */
    public static Drawable getTemplateDrawable(String templateName, Context context) {
        switch (templateName) {
            case TEMPLATE_NONE:
                return ContextCompat.getDrawable(context, R.drawable.template_none);
            case TEMPLATE_1:
                return ContextCompat.getDrawable(context, R.drawable.template_amatic_noshape);
            case TEMPLATE_2:
                return ContextCompat.getDrawable(context, R.drawable.template_amatic_shape);
            case TEMPLATE_3:
                return ContextCompat.getDrawable(context, R.drawable.template_blackout_noshape);
            case TEMPLATE_4:
                return ContextCompat.getDrawable(context, R.drawable.template_fear_noshape);
            case TEMPLATE_5:
                return ContextCompat.getDrawable(context, R.drawable.template_komikaaxis_noshape);
            case TEMPLATE_6:
                return ContextCompat.getDrawable(context, R.drawable.template_komikaaxis_shape);
            case TEMPLATE_7:
                return ContextCompat.getDrawable(context, R.drawable.template_langdon_noshape);
            case TEMPLATE_8:
                return ContextCompat.getDrawable(context, R.drawable.template_love_thunder_noshape);
            case TEMPLATE_9:
                return ContextCompat.getDrawable(context, R.drawable.template_ostrich_noshape);
            case TEMPLATE_10:
                return ContextCompat.getDrawable(context, R.drawable.template_ostrich_shape);
            case TEMPLATE_11:
                return ContextCompat.getDrawable(context, R.drawable.template_pacifico_noshape);
            case TEMPLATE_12:
                return ContextCompat.getDrawable(context, R.drawable.template_pacifico_shape);
            case TEMPLATE_13:
                return ContextCompat.getDrawable(context, R.drawable.template_poiret_noshape);
            case TEMPLATE_14:
                return ContextCompat.getDrawable(context, R.drawable.template_poiret_shape);
            case TEMPLATE_15:
                return ContextCompat.getDrawable(context, R.drawable.template_twoan_noshape);
            case TEMPLATE_16:
                return ContextCompat.getDrawable(context, R.drawable.template_yanone_noshape);
            case TEMPLATE_17:
                return ContextCompat.getDrawable(context, R.drawable.template_yanone_shape);
            default:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
        }
    }

    /**
     * Method to return template name for respective templates.
     *
     * @param templateName Name of template.
     */
    public static String getTemplateName(String templateName) {
        switch (templateName) {
            case TEMPLATE_NONE:
                return "None";
            case TEMPLATE_1:
                return "Template 1";
            case TEMPLATE_2:
                return "Template 2";
            case TEMPLATE_3:
                return "Template 3";
            case TEMPLATE_4:
                return "Template 4";
            case TEMPLATE_5:
                return "Template 5";
            case TEMPLATE_6:
                return "Template 6";
            case TEMPLATE_7:
                return "Template 7";
            case TEMPLATE_8:
                return "Template 8";
            case TEMPLATE_9:
                return "Template 9";
            case TEMPLATE_10:
                return "Template 10";
            case TEMPLATE_11:
                return "Template 11";
            case TEMPLATE_12:
                return "Template 12";
            case TEMPLATE_13:
                return "Template 13";
            case TEMPLATE_14:
                return "Template 14";
            case TEMPLATE_15:
                return "Template 15";
            case TEMPLATE_16:
                return "Template 16";
            case TEMPLATE_17:
                return "Template 17";
            default:
                return "None";
        }
    }


    /**
     * Sets the color of the content shape
     *
     * @param selectedColor color to set on the shape
     * @param context       Context to use.
     * @param shapeName     Name of background shape.
     * @param textView      TextView/EditText object.
     */
    public static void setContentShapeColor(int selectedColor, String shapeName, TextView textView, Context context) {

        switch (shapeName) {
            case TemplateHelper.SHAPE_NAME_QUOTE:
            case TemplateHelper.SHAPE_NAME_CORNER_LINE:
                Drawable drawable = textView.getBackground();
                drawable.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN);
                break;
            case TemplateHelper.SHAPE_NAME_SIDE_LINE:
            case TemplateHelper.SHAPE_NAME_TOP_BOTTOM_LINE:
                LayerDrawable layerDrawable = (LayerDrawable) textView.getBackground();
                GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.contentStyleLines);
                gradientDrawable.setStroke(ViewHelper.convertToPx(context, 1), selectedColor);
                break;
        }
    }


    /**
     * Method to return template position in list.
     *
     * @param shapeName shapeName.
     * @param fontName  Recently applied font name.
     */
    public static int getTemplatePosition(String shapeName, String fontName) {
        switch (fontName) {
            case FontsHelper.FONT_TYPE_BOHEMIAN_TYPEWRITER:
                return 0;
            case FontsHelper.FONT_TYPE_AMATIC_SC_REGULAR:
                if (shapeName.equals(SHAPE_NAME_NONE)) {
                    return 1;
                } else {
                    return 2;
                }
            case FontsHelper.FONT_TYPE_BLACKOUT_SUNRISE:
                return 3;
            case FontsHelper.FONT_TYPE_FRESSH:
                return 4;

            case FontsHelper.FONT_TYPE_KOMIKAAXIS:
                if (shapeName.equals(SHAPE_NAME_NONE)) {
                    return 5;
                } else {
                    return 6;
                }
            case FontsHelper.FONT_TYPE_LANGDON:
                return 7;
            case FontsHelper.FONT_TYPE_A_LOVE_OF_THUNDER:
                return 8;
            case FontsHelper.FONT_TYPE_OSTRICH_ROUNDED:
                if (shapeName.equals(SHAPE_NAME_NONE)) {
                    return 9;
                } else {
                    return 10;
                }
            case FontsHelper.FONT_TYPE_PACIFICO:
                if (shapeName.equals(SHAPE_NAME_NONE)) {
                    return 11;
                } else {
                    return 12;
                }

            case FontsHelper.FONT_TYPE_POIRET_ONE_REGULAR:
                if (shapeName.equals(SHAPE_NAME_NONE)) {
                    return 13;
                } else {
                    return 14;
                }
            case FontsHelper.FONT_TYPE_BLACKOUT_TWOAM:
                return 15;
            case FontsHelper.FONT_TYPE_YANONE_KAFFEESATZ:
                if (shapeName.equals(SHAPE_NAME_NONE)) {
                    return 16;
                } else {
                    return 17;
                }
            default:
                return 0;
        }
    }
}
