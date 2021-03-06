package com.thetestament.cread.helpers;


import android.content.Context;

import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.VignetteSubfilter;

/**
 * Helper class to generate custom image filter.
 */
public class CustomFilters {

    /**
     * Method to generate Star Lit filter.
     */
    public static Filter getStarLitFilter() {
        Point[] rgbKnots;
        rgbKnots = new Point[8];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(34, 6);
        rgbKnots[2] = new Point(69, 23);
        rgbKnots[3] = new Point(100, 58);
        rgbKnots[4] = new Point(150, 154);
        rgbKnots[5] = new Point(176, 196);
        rgbKnots[6] = new Point(207, 233);
        rgbKnots[7] = new Point(255, 255);
        Filter filter = new Filter();

        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, null, null, null));
        return filter;
    }

    /**
     * Method to generate Blue Mess filter.
     */
    public static Filter getBlueMessFilter() {
        Point[] redKnots;
        redKnots = new Point[8];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(86, 34);
        redKnots[2] = new Point(117, 41);
        redKnots[3] = new Point(146, 80);
        redKnots[4] = new Point(170, 151);
        redKnots[5] = new Point(200, 214);
        redKnots[6] = new Point(225, 242);
        redKnots[7] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, null, null));
        filter.addSubFilter(new BrightnessSubfilter(30));
        filter.addSubFilter(new ContrastSubfilter(1f));
        return filter;
    }

    /**
     * Method to generate Awe Stuck Vibe filter.
     */
    public static Filter getAweStruckVibeFilter() {
        Point[] rgbKnots;
        Point[] redKnots;
        Point[] greenKnots;
        Point[] blueKnots;

        rgbKnots = new Point[5];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(80, 43);
        rgbKnots[2] = new Point(149, 102);
        rgbKnots[3] = new Point(201, 173);
        rgbKnots[4] = new Point(255, 255);

        redKnots = new Point[5];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(125, 147);
        redKnots[2] = new Point(177, 199);
        redKnots[3] = new Point(213, 228);
        redKnots[4] = new Point(255, 255);


        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(57, 76);
        greenKnots[2] = new Point(103, 130);
        greenKnots[3] = new Point(167, 192);
        greenKnots[4] = new Point(211, 229);
        greenKnots[5] = new Point(255, 255);


        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(38, 62);
        blueKnots[2] = new Point(75, 112);
        blueKnots[3] = new Point(116, 158);
        blueKnots[4] = new Point(171, 204);
        blueKnots[5] = new Point(212, 233);
        blueKnots[6] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, redKnots, greenKnots, blueKnots));
        return filter;
    }


    /**
     * Method to generate Lime Stutter filter.
     */
    public static Filter getLimeStutterFilter() {
        Point[] blueKnots;
        blueKnots = new Point[3];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(165, 114);
        blueKnots[2] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, null, null, blueKnots));
        return filter;
    }

    /**
     * Method to generate Night Whisper filter.
     */
    public static Filter getNightWhisperFilter() {
        Point[] rgbKnots;
        Point[] redKnots;
        Point[] greenKnots;
        Point[] blueKnots;

        rgbKnots = new Point[3];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(174, 109);
        rgbKnots[2] = new Point(255, 255);

        redKnots = new Point[4];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(70, 114);
        redKnots[2] = new Point(157, 145);
        redKnots[3] = new Point(255, 255);

        greenKnots = new Point[3];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(109, 138);
        greenKnots[2] = new Point(255, 255);

        blueKnots = new Point[3];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(113, 152);
        blueKnots[2] = new Point(255, 255);

        Filter filter = new Filter();

        filter.addSubFilter(new ContrastSubfilter(1.5f));
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, redKnots, greenKnots, blueKnots));
        return filter;
    }


    /**
     * Method to generate Black & white filter.
     */
    public static Filter getBlackAndWhiteFilter() {
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(-100f));
        return filter;
    }

    /**
     * Method to generate Sepia filter.
     */
    public static Filter getSepiaFilter() {
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(-100f));
        filter.addSubFilter(new ColorOverlaySubfilter(1, 102, 51, 0));
        return filter;
    }

    /**
     * Method to generate Amazon filter.
     */
    public static Filter getAmazonFilter() {
        Point[] blueKnots;
        blueKnots = new Point[6];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(11, 40);
        blueKnots[2] = new Point(36, 99);
        blueKnots[3] = new Point(86, 151);
        blueKnots[4] = new Point(167, 209);
        blueKnots[5] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(1.2f));
        filter.addSubFilter(new ToneCurveSubfilter(null, null, null, blueKnots));
        return filter;
    }

    /**
     * Method to generate Adele filter.
     */
    public static Filter getAdeleFilter() {
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(-100f));
        return filter;
    }

    /**
     * Method to generate Cruz filter.
     */
    public static Filter getCruzFilter() {
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(-100f));
        filter.addSubFilter(new ContrastSubfilter(1.3f));
        filter.addSubFilter(new BrightnessSubfilter(20));
        return filter;
    }

    /**
     * Method to generate Metropolis filter.
     */
    public static Filter getMetropolisFilter() {
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(-1f));
        filter.addSubFilter(new ContrastSubfilter(1.7f));
        filter.addSubFilter(new BrightnessSubfilter(70));
        return filter;
    }

    /**
     * Method to generate Audrey filter.
     */
    public static Filter getAudreyFilter() {
        Filter filter = new Filter();

        Point[] redKnots;
        redKnots = new Point[3];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(124, 138);
        redKnots[2] = new Point(255, 255);

        filter.addSubFilter(new SaturationSubfilter(-100f));
        filter.addSubFilter(new ContrastSubfilter(1.3f));
        filter.addSubFilter(new BrightnessSubfilter(20));
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, null, null));
        return filter;
    }

    /**
     * Method to generate Rise filter.
     */
    public static Filter getRiseFilter(Context context) {
        Point[] blueKnots;
        Point[] redKnots;

        blueKnots = new Point[4];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(39, 70);
        blueKnots[2] = new Point(150, 200);
        blueKnots[3] = new Point(255, 255);

        redKnots = new Point[4];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(45, 64);
        redKnots[2] = new Point(170, 190);
        redKnots[3] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(1.9f));
        filter.addSubFilter(new BrightnessSubfilter(60));
        filter.addSubFilter(new VignetteSubfilter(context, 200));
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, null, blueKnots));
        return filter;
    }

    /**
     * Method to generate Mars filter.
     */
    public static Filter getMarsFilter() {
        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(1.5f));
        filter.addSubFilter(new BrightnessSubfilter(10));
        return filter;
    }

    /**
     * Method to generate April filter.
     */
    public static Filter getAprilFilter(Context context) {
        Point[] blueKnots;
        Point[] redKnots;

        blueKnots = new Point[4];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(39, 70);
        blueKnots[2] = new Point(150, 200);
        blueKnots[3] = new Point(255, 255);

        redKnots = new Point[4];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(45, 64);
        redKnots[2] = new Point(170, 190);
        redKnots[3] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(1.5f));
        filter.addSubFilter(new BrightnessSubfilter(5));
        filter.addSubFilter(new VignetteSubfilter(context, 150));
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, null, blueKnots));
        return filter;
    }

    /**
     * Method to generate Han filter.
     */
    public static Filter getHanFilter(Context context) {
        Point[] greenKnots;
        greenKnots = new Point[3];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(113, 142);
        greenKnots[2] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(1.3f));
        filter.addSubFilter(new BrightnessSubfilter(60));
        filter.addSubFilter(new VignetteSubfilter(context, 200));
        filter.addSubFilter(new ToneCurveSubfilter(null, null, greenKnots, null));
        return filter;
    }

    /**
     * Method to generate Old Man filter.
     */
    public static Filter getOldManFilter(Context context) {
        Filter filter = new Filter();
        filter.addSubFilter(new BrightnessSubfilter(30));
        filter.addSubFilter(new SaturationSubfilter(0.8f));
        filter.addSubFilter(new ContrastSubfilter(1.3f));
        filter.addSubFilter(new VignetteSubfilter(context, 100));
        filter.addSubFilter(new ColorOverlaySubfilter(100, .2f, .2f, .1f));
        return filter;
    }

    /**
     * Method to generate Clarendon filter.
     */
    public static Filter getClarendonFilter() {
        Point[] redKnots;
        Point[] greenKnots;
        Point[] blueKnots;

        redKnots = new Point[4];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(56, 68);
        redKnots[2] = new Point(196, 206);
        redKnots[3] = new Point(255, 255);


        greenKnots = new Point[4];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(46, 77);
        greenKnots[2] = new Point(160, 200);
        greenKnots[3] = new Point(255, 255);


        blueKnots = new Point[4];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(33, 86);
        blueKnots[2] = new Point(126, 220);
        blueKnots[3] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ContrastSubfilter(1.5f));
        filter.addSubFilter(new BrightnessSubfilter(-10));
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));
        return filter;
    }

}
