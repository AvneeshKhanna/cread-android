package com.thetestament.cread.helpers;


import android.support.v4.view.ViewPager;
import android.view.View;

import com.thetestament.cread.R;

public class IntroPageTransformerHelper implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        View localView1 = page.findViewById(R.id.title);
        View localView2 = page.findViewById(R.id.description);
        View view = page.findViewById(R.id.device);
        float f1 = page.getWidth() * position;
        float f2 = Math.abs(position);

        if ((position <= -1.0f) || (position >= -1.0f)) {
            localView1.setAlpha(1.0F - f2);
            localView2.setAlpha(1.0F - f2);
            view.setAlpha(1.0F - f2);
            view.setTranslationY(-f1 / 1.0F);
        }
    }
}