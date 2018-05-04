package com.thetestament.cread.utils;


import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Locale;

import static com.thetestament.cread.utils.Constant.ASPECT_RATIO_FIVE_TO_FOUR_VALUE;
import static com.thetestament.cread.utils.Constant.ASPECT_RATIO_FOUR_TO_FIVE_VALUE;
import static com.thetestament.cread.utils.Constant.ASPECT_RATIO_FOUR_TO_THREE_VALUE;
import static com.thetestament.cread.utils.Constant.ASPECT_RATIO_ONE_TO_ONE;
import static com.thetestament.cread.utils.Constant.ASPECT_RATIO_ONE_TO_ONE_VALUE;
import static com.thetestament.cread.utils.Constant.ASPECT_RATIO_THREE_TO_FOUR_VALUE;

/**
 * Class to provide utility methods for aspect ration related operation.
 */
public class AspectRatioUtils {


    /**
     * Method to retrieve device screen width.
     *
     * @return Return device width.
     */
    public static int getDeviceScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    /**
     * Method to set appropriate height and width of imageView to keep its aspect ratio intact.
     *
     * @param imgHeight Height of image.
     * @param imgWidth  Width of image.
     * @param imageView ImageView where image to be displayed.
     */
    public static void setImageAspectRatio(int imgWidth, int imgHeight, View imageView) {
        float valueAspectRatio = (float) imgWidth / imgHeight;
        String formattedAspectRatio = String.format(Locale.ENGLISH, "%.2f", valueAspectRatio);

        RelativeLayout.LayoutParams params;

        switch (formattedAspectRatio) {
            //1:1 aspect ratio
            case ASPECT_RATIO_ONE_TO_ONE:
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , AspectRatioUtils.getDeviceScreenWidth());
                imageView.setLayoutParams(params);
                break;

            //4:5 aspect ratio
            case Constant.ASPECT_RATIO_FOUR_TO_FIVE:
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((AspectRatioUtils.getDeviceScreenWidth() / ASPECT_RATIO_FOUR_TO_FIVE_VALUE)));
                imageView.setLayoutParams(params);
                break;

            //5:4 aspect ratio
            case Constant.ASPECT_RATIO_FIVE_TO_FOUR:
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((AspectRatioUtils.getDeviceScreenWidth() / ASPECT_RATIO_FIVE_TO_FOUR_VALUE)));
                imageView.setLayoutParams(params);
                break;

            //4:3 aspect ratio
            case Constant.ASPECT_RATIO_FOUR_TO_THREE:
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((AspectRatioUtils.getDeviceScreenWidth() / ASPECT_RATIO_FOUR_TO_THREE_VALUE)));
                imageView.setLayoutParams(params);
                break;
            //3:4 aspect ratio
            case Constant.ASPECT_RATIO_THREE_TO_FOUR:
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((AspectRatioUtils.getDeviceScreenWidth() / ASPECT_RATIO_THREE_TO_FOUR_VALUE)));
                imageView.setLayoutParams(params);
                break;
            //For original and other views
            default:
                break;
        }
    }

    /**
     * Method to return true and false depending upon whether Square image manipulation required or not.
     *
     * @param imgHeight Height of image.
     * @param imgWidth  Width of image.
     * @return True if image manipulation required false otherwise.
     */
    public static boolean getSquareImageManipulation(float imgWidth, float imgHeight) {
        float valueAspectRatio = imgWidth / imgHeight;
        String formattedAspectRatio = String.format(Locale.ENGLISH
                , "%.2f", valueAspectRatio);

        switch (formattedAspectRatio) {
            //1:1 aspect ratio
            case ASPECT_RATIO_ONE_TO_ONE:
                //4:5 aspect ratio
            case Constant.ASPECT_RATIO_FOUR_TO_FIVE:
                //5:4 aspect ratio
            case Constant.ASPECT_RATIO_FIVE_TO_FOUR:
                //4:3 aspect ratio
            case Constant.ASPECT_RATIO_FOUR_TO_THREE:
                //3:4 aspect ratio
            case Constant.ASPECT_RATIO_THREE_TO_FOUR:
                return false;
            default:
                //Create square image with blurred background
                return true;
        }
    }


    /**
     * Method to return division factor to obtain required image height.
     *
     * @param imgHeight Height of image.
     * @param imgWidth  Width of image.
     */
    public static float getImageAspectRatioFactor(int imgWidth, int imgHeight) {
        float valueAspectRatio = (float) imgWidth / imgHeight;
        String formattedAspectRatio = String.format(Locale.ENGLISH, "%.2f", valueAspectRatio);

        switch (formattedAspectRatio) {
            //1:1 aspect ratio
            case ASPECT_RATIO_ONE_TO_ONE:
                return ASPECT_RATIO_ONE_TO_ONE_VALUE;
            //4:5 aspect ratio
            case Constant.ASPECT_RATIO_FOUR_TO_FIVE:
                return ASPECT_RATIO_FOUR_TO_FIVE_VALUE;
            //5:4 aspect ratio
            case Constant.ASPECT_RATIO_FIVE_TO_FOUR:
                return ASPECT_RATIO_FIVE_TO_FOUR_VALUE;
            //4:3 aspect ratio
            case Constant.ASPECT_RATIO_FOUR_TO_THREE:
                return ASPECT_RATIO_FOUR_TO_THREE_VALUE;
            //3:4 aspect ratio
            case Constant.ASPECT_RATIO_THREE_TO_FOUR:
                return ASPECT_RATIO_THREE_TO_FOUR_VALUE;
            //For original and other views
            default:
                return (float) 1.00;
        }
    }


}
