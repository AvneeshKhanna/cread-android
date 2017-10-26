package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.thetestament.cread.R;

import butterknife.ButterKnife;

/**
 * Class to show preview of capture screen.
 */

public class CapturePreviewActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_preview);
        ButterKnife.bind(this);
    }
}
