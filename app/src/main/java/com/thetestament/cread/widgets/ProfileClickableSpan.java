package com.thetestament.cread.widgets;

import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.IntentHelper;


public class ProfileClickableSpan extends ClickableSpan {


    private FragmentActivity mContext;
    private String mUUID;

    public ProfileClickableSpan(FragmentActivity mContext, String mUUID) {
        this.mContext = mContext;
        this.mUUID = mUUID;
    }


    @Override
    public void onClick(View view) {
        //Method called
        IntentHelper.openProfileActivity(mContext, mUUID);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        ds.setColor(ContextCompat.getColor(mContext, R.color.blue_dark));
    }
}
