package com.thetestament.cread.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.thetestament.cread.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Here user writes short.
 */

public class ShortActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short);
        //ButterKnife view binding
        ButterKnife.bind(this);
    }

    @OnClick(R.id.buttonClick)
    void onCLick() {
        // startActivity(new Intent(this, InspirationActivity.class));
        startActivity(new Intent(this, FinaliseShortActivity.class));
    }
}
