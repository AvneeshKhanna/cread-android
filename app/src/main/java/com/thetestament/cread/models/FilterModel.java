package com.thetestament.cread.models;

import android.graphics.Bitmap;

import com.zomato.photofilters.imageprocessors.Filter;

/**
 * Model for filter data.
 */

public class FilterModel {
    public Bitmap image;
    public Filter filter;
    public String filterName;

    public FilterModel(String name, Bitmap image, Filter filter) {
        filterName = name;
        this.filter = filter;
        if (filter == null)
            this.image = image;
        else {
            this.image = filter.processFilter(Bitmap.createBitmap(image));
        }
    }
}
