package com.thetestament.cread.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.thetestament.cread.R;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

/**
 * Created by avnee on 27-06-2018.
 */

public class ConfettiViewUtils {

    private Handler handler;

    public void showKonfettiInstance(KonfettiView konfettiView, Context context) {
        konfettiView.build()
                .addColors(Color.YELLOW, ContextCompat.getColor(context, R.color.blue), ContextCompat.getColor(context, R.color.lt_orange), ContextCompat.getColor(context, R.color.color_green_500))
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(8, 5))
                .setPosition(0f, (float) AspectRatioUtils.getDeviceScreenWidth(), -50f, -50f)
                .streamFor(75, 5000L);

        konfettiView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                stopConfettiFilter((KonfettiView) v);
            }
        });

       
        showKonfettiRepeating(konfettiView, context);
    }

    private void showKonfettiRepeating(final KonfettiView konfettiView, final Context context){
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                konfettiView.build()
                        .addColors(Color.YELLOW, ContextCompat.getColor(context, R.color.blue), ContextCompat.getColor(context, R.color.lt_orange), ContextCompat.getColor(context, R.color.color_green_500))
                        .setDirection(0.0, 359.0)
                        .setSpeed(1f, 5f)
                        .setFadeOutEnabled(true)
                        .setTimeToLive(2000L)
                        .addShapes(Shape.RECT, Shape.CIRCLE)
                        .addSizes(new Size(8, 5))
                        .setPosition(0f, (float) AspectRatioUtils.getDeviceScreenWidth(), -50f, -50f)
                        .streamFor(75, 5000L);

                showKonfettiRepeating(konfettiView, context);
            }
        }, 8000);
    }

    private void stopConfettiFilter(KonfettiView konfettiView){
        konfettiView.getActiveSystems().clear();
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }

}
