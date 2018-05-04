package com.thetestament.cread.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.utils.AspectRatioUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_IMAGE_HEIGHT;
import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_IMAGE_URL;
import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_IMAGE_WIDTH;
import static com.thetestament.cread.utils.Constant.CONTENT_PREVIEW_EXTRA_SIGNATURE_TEXT;

/**
 * Appcompat activity to show content preview in fullscreen mode.
 */

public class ContentPreview extends BaseActivity {

    //region :Views binding with butter knife
    @BindView(R.id.imageContentPreview)
    AppCompatImageView imageContentPreview;
    @BindView(R.id.textSignature)
    AppCompatTextView textSignature;
    //endregion

    //region :Fields and constants
    Context mContext;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_preview);
        ButterKnife.bind(this);
        //Obtain reference
        mContext = this;

        //Retrieve bundle data here
        Bundle bundle = getIntent().getBundleExtra(CONTENT_PREVIEW_EXTRA_DATA);

        //Set  aspect ration
        AspectRatioUtils.setImageAspectRatio(bundle.getInt(CONTENT_PREVIEW_EXTRA_IMAGE_WIDTH)
                , bundle.getInt(CONTENT_PREVIEW_EXTRA_IMAGE_HEIGHT)
                , imageContentPreview);

        //Load image
        ImageHelper.loadImageFromPicasso(mContext
                , imageContentPreview
                , bundle.getString(CONTENT_PREVIEW_EXTRA_IMAGE_URL)
                , R.drawable.image_placeholder);

        //if signature text was not empty
        if (!TextUtils.isEmpty(bundle.getString(CONTENT_PREVIEW_EXTRA_SIGNATURE_TEXT))) {
            //Set text and its visibility
            textSignature.setVisibility(View.VISIBLE);
            textSignature.setText(bundle.getString(CONTENT_PREVIEW_EXTRA_SIGNATURE_TEXT));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    //endregion
}
