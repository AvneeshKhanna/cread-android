package com.thetestament.cread.utils;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by gaurav on 29/05/18.
 */

public class ReverseInterpolator implements Interpolator {

    private final Interpolator delegate;

    public ReverseInterpolator(Interpolator delegate){
        this.delegate = delegate;
    }

    public ReverseInterpolator(){
        this(new LinearInterpolator());
    }

    @Override
    public float getInterpolation(float input) {
        return 1 - delegate.getInterpolation(input);
    }
}