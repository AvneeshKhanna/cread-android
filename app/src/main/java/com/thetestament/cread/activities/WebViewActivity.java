package com.thetestament.cread.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ViewHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_TITLE;
import static com.thetestament.cread.utils.Constant.EXTRA_WEB_VIEW_URL;

/**
 * Appcompat class to load read more data in webView from server.
 */

public class WebViewActivity extends BaseActivity {

    @BindView(R.id.rootView)
    CoordinatorLayout rootView;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.progressView)
    View progressView;

    @State
    String mWebPageUrl, mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        //Bind the view
        ButterKnife.bind(this);
        //Get data from intent
        retrieveIntentData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    /**
     * Method to retrieve intent data from server.
     */
    private void retrieveIntentData() {
        //Retrieve data from server
        mWebPageUrl = getIntent().getStringExtra(EXTRA_WEB_VIEW_URL);
        mTitle = getIntent().getStringExtra(EXTRA_WEB_VIEW_TITLE);
        //Set toolbar title
        getSupportActionBar().setTitle(mTitle);
        //initialize webView
        initWebView();
    }

    /**
     * To initialize webView settings and url.
     */
    private void initWebView() {
        //Configure WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        //Load web page
        webView.loadUrl(mWebPageUrl);
    }

    /**
     * To Override the WebViewClient default behaviour so we can implement progress indicator
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            //Hide progress view
            progressView.setVisibility(View.GONE);
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //Hide progress view
            progressView.setVisibility(View.GONE);
        }
    }

}
