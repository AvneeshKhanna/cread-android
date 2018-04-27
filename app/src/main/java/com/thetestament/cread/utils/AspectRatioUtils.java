package com.thetestament.cread.utils;


import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.Locale;

/**
 * Class to provide utility methods for aspect ration related operation.
 */
public class AspectRatioUtils {


    /**
     * Method to retrieve device screen width.
     *
     * @return Return device width.
     */
    public static int getScreenWidth() {
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
        String formattedAspectRatio = String.format(Locale.ENGLISH, "%.1f", valueAspectRatio);

        RelativeLayout.LayoutParams params;

        switch (formattedAspectRatio) {
            //1:1 aspect ratio
            case "1.0":
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , AspectRatioUtils.getScreenWidth());
                imageView.setLayoutParams(params);
                break;
            //3:2 aspect ratio
            case "1.5":
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((float) (AspectRatioUtils.getScreenWidth() / 1.5)));
                imageView.setLayoutParams(params);
                break;
            //4:5 aspect ratio
            case "0.8":
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((float) (AspectRatioUtils.getScreenWidth() / 0.8)));
                imageView.setLayoutParams(params);
                break;
            //4:3 aspect ratio
            case "1.3":
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((float) (AspectRatioUtils.getScreenWidth() / 1.3)));
                imageView.setLayoutParams(params);
                break;
            //16:9 aspect ratio
            case "1.8":
                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , Math.round((float) (AspectRatioUtils.getScreenWidth() / 1.8)));
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
                , "%.1f", valueAspectRatio);

        switch (formattedAspectRatio) {
            //1:1 aspect ratio
            case "1.0":
                //3:2 aspect ratio
            case "1.5":
                //4:5 aspect ratio
            case "0.8":
                //4:3 aspect ratio
            case "1.3":
                //16:9 aspect ratio
            case "1.8":
                return false;
            default:
                //Create square image with blurred background
                return true;
        }
    }
}
