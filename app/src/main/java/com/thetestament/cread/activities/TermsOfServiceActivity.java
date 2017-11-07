package com.thetestament.cread.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.thetestament.cread.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity class to show the T&c foe this app.
 */

public class TermsOfServiceActivity extends BaseActivity {

    @BindView(R.id.webViewTOS)
    WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_services);
        //Bind the view
        ButterKnife.bind(this);
    }

}
