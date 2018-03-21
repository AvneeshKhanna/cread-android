package com.thetestament.cread.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.thetestament.cread.R;

/**
 * Helper class to provide utility method for template related operation.
 */


public class TemplateHelper {

    //Template names
    public static final String TEMPLATE_NONE = "none";
    public static final String TEMPLATE_ONE = "templateOne";
    public static final String TEMPLATE_TWO = "templateTwo";
    public static final String TEMPLATE_THREE = "templateThree";
    public static final String TEMPLATE_FOUR = "templateFour";
    public static final String TEMPLATE_FIVE = "templateFive";


    //Template list
    public static String[] templateList = {
            TEMPLATE_NONE
            , TEMPLATE_ONE
            , TEMPLATE_TWO
            , TEMPLATE_THREE
            , TEMPLATE_FOUR
            , TEMPLATE_FIVE
    };


    /**
     * Method to return drawable resource id for respective templates.
     *
     * @param context      Context to use.
     * @param templateName Name of template.
     */
    public static Drawable getTemplateDrawable(String templateName, Context context) {
        switch (templateName) {
            case TEMPLATE_NONE:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
            case TEMPLATE_ONE:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
            case TEMPLATE_TWO:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
            case TEMPLATE_THREE:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
            case TEMPLATE_FOUR:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
            case TEMPLATE_FIVE:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
            default:
                return ContextCompat.getDrawable(context, R.drawable.image_placeholder);
        }
    }

}
