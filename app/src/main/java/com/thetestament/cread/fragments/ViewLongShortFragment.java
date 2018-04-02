package com.thetestament.cread.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.models.ShortModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.FontsHelper.getFontType;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewLongShortFragment extends Fragment {


    //region: Butterknife injections
    @BindView(R.id.contentImage)
    ImageView contentImage;
    @BindView(R.id.textLongShort)
    TextView textLongShort;
    //endregion

    private Unbinder mUnbinder;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private FragmentActivity mContext;

    @State
    ShortModel mShortData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater
                .inflate(R.layout.fragment_view_long_short
                        , container
                        , false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mShortData = getArguments().getParcelable("shortData");
        mContext = getActivity();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mCompositeDisposable.dispose();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }


        // apply style
        applyStyling();
    }


    private void applyStyling() {
        // set IMAGE params
        String entityURL = mShortData.getImageURL();


        //If capture image is not present
        if (TextUtils.isEmpty(entityURL) || entityURL.equals("null")) {

            if (mShortData.getBgcolor().equals("FFFFFFFF")) {
                // set default bg
                contentImage.setBackground(ContextCompat.getDrawable(mContext, R.drawable.img_short_default_bg));
            } else {
                //Change backgroundColor
                contentImage.setBackground(null);
                contentImage.setBackgroundColor((int) Long.parseLong(mShortData.getBgcolor(), 16));
            }
        } else {
            ImageHelper.loadImageFromPicasso(mContext, contentImage, entityURL, R.drawable.img_short_default_bg);
        }

        // apply image tint
        applyImageTint();


        contentImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                contentImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float imgWidth = contentImage.getWidth();
                float factor = imgWidth
                        / (float) mShortData.getImgWidth();

                textLongShort.setTextSize(ViewHelper.pixelsToSp(mContext, (float) mShortData.getTextSize() * factor));
            }
        });

        // set TEXT params
        // set text
        textLongShort.setText(mShortData.getContentText());

        //set text color
        textLongShort.setTextColor((int) Long.parseLong(mShortData.getTextColor(), 16));
        //set bold and italic and font
        applyBoldItalic();
        // apply shadow
        applyTextShadow();
        //set text gravity
        applyTextGravity();
    }

    private void applyTextGravity() {
        //Update short text gravity
        switch (ShortActivity.TextGravity.valueOf(mShortData.getTextGravity())) {
            case Center:
                textLongShort.setGravity(Gravity.CENTER);
                break;
            case West:
                textLongShort.setGravity(Gravity.LEFT);
                break;
            case East:
                textLongShort.setGravity(Gravity.RIGHT);
                break;
        }
    }

    /**
     * Applies tint to the image
     */
    private void applyImageTint() {
        String imageTint = mShortData.getImgTintColor();
        //Update image tint
        if (!TextUtils.isEmpty(imageTint) && !imageTint.equals("null")) {
            switch (imageTint.toUpperCase()) {
                case "4D000000":
                    //Apply tint
                    contentImage.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_30));
                    break;
                case "80000000":
                    //Apply tint
                    contentImage.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_50));
                    break;
                case "B3000000":
                    //Apply tint
                    contentImage.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_70));
                    break;
                case "99000000":
                    //Apply tint
                    contentImage.setColorFilter(ContextCompat.getColor(mContext, R.color.transparent_60));
                    break;
                default:
                    break;
            }
        } else {   // remove tint
            contentImage.clearColorFilter();
        }

    }

    /**
     * Sets the font and bold and italic on the text
     */
    private void applyBoldItalic() {
        boolean isBold = mShortData.isBold();
        boolean isItalic = mShortData.isItalic();

        String fontType = mShortData.getFont();

        //Update short text typeface
        if (!isItalic && !isBold) {
            //Set typeface to normal
            textLongShort.setTypeface(getFontType(fontType, mContext), Typeface.NORMAL);
        } else if (!isItalic && isBold) {
            //Set typeface to bold
            textLongShort.setTypeface(getFontType(fontType, mContext), Typeface.BOLD);
        } else if (isItalic && !isBold) {
            //Set typeface to italic
            textLongShort.setTypeface(getFontType(fontType, mContext), Typeface.ITALIC);
        } else if (isItalic && isBold) {
            //Set typeface to bold_italic
            textLongShort.setTypeface(getFontType(fontType, mContext), Typeface.BOLD_ITALIC);
        }
    }

    /**
     * Sets the text shadow
     */
    private void applyTextShadow() {
        //if text shadow is present
        if (mShortData.isTextShadow()) {
            //Apply text shadow
            textLongShort.setShadowLayer(3, 3, 3
                    , ContextCompat.getColor(mContext, R.color.color_grey_600));
        } else {
            textLongShort.setShadowLayer(0, 0, 0, 0);
        }
    }




    /**
     * Initializes the reading mode
     */
    public void toggleReadingMode(boolean isReadingMode) {

        if (isReadingMode) {
            //Apply tint
            contentImage.setColorFilter(ContextCompat.getColor(getActivity(), R.color.transparent_50));
            //apply text shadow
            textLongShort.setShadowLayer(3, 3, 3, ContextCompat.getColor(getActivity(), R.color.color_grey_600));
            // show toast
            ViewHelper.getToast(mContext, "Reading mode enabled");

        } else {
            //show toast
            ViewHelper.getToast(mContext, "Reading mode disabled");
            // set original shadow
            applyTextShadow();
            // set original tint
            applyImageTint();
        }
    }


}
