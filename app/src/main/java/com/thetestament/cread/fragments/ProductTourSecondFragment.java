package com.thetestament.cread.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;

/**
 * Created by gaurav on 24/05/18.
 */

public class ProductTourSecondFragment extends Fragment {

    /**
     * Required constructor.
     */
    public ProductTourSecondFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_tour_second
                , container
                , false);
    }
}
