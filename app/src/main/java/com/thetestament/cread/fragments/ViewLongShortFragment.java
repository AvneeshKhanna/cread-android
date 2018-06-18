package com.thetestament.cread.fragments;

import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.github.glomadrian.grav.GravView;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.LongShortHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.models.ShortModel;
import com.thetestament.cread.utils.Constant;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;
import nl.dionsegijn.konfetti.KonfettiView;

import static com.thetestament.cread.helpers.FontsHelper.getFontType;

public class ViewLongShortFragment extends Fragment {

    //region:Butter knife injections
    @BindView(R.id.content_image)
    AppCompatImageView contentImage;
    @BindView(R.id.text_writing)
    AppCompatTextView textLongShort;
    @BindView(R.id.live_filter_bubble)
    GravView liveFilterBubble;
    @BindView(R.id.whether_view)
    WeatherView whetherView;
    @BindView(R.id.konfetti_view)
    KonfettiView konfettiView;
    //endregion

    //region :Fields and constants
    private Unbinder mUnbinder;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    MediaPlayer mediaPlayer = new MediaPlayer();
    @State
    ShortModel mShortData;
    //endregion

    //region :Overridden methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater
                .inflate(R.layout.fragment_view_long_short
                        , container
                        , false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        mShortData = getArguments().getParcelable("shortData");
        //Method called
        initLiveFilters(mShortData.getLiveFilterName());
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
        // play sound
        playSound(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(SettingsFragment.KEY_SETTINGS_LONGFORMSOUND, true));
    }
    //endregion

    //region :Private methods
    private void applyStyling() {
        // set IMAGE params
        String entityURL = mShortData.getImageURL();


        //If capture image is not present
        if (TextUtils.isEmpty(entityURL) || entityURL.equals("null")) {

            if (mShortData.getBgcolor().equals("FFFFFFFF")) {
                // set default bg
                contentImage.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.img_short_default_bg));
            } else {
                //Change backgroundColor
                contentImage.setBackground(null);
                contentImage.setBackgroundColor((int) Long.parseLong(mShortData.getBgcolor(), 16));
            }
        } else {
            ImageHelper.loadImageFromPicasso(getActivity(), contentImage, entityURL, R.drawable.img_short_default_bg);
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

                textLongShort.setTextSize(ViewHelper.pixelsToSp(getActivity(), (float) mShortData.getTextSize() * factor));
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
                    contentImage.setColorFilter(ContextCompat.getColor(getActivity(), R.color.transparent_30));
                    break;
                case "80000000":
                    //Apply tint
                    contentImage.setColorFilter(ContextCompat.getColor(getActivity(), R.color.transparent_50));
                    break;
                case "B3000000":
                    //Apply tint
                    contentImage.setColorFilter(ContextCompat.getColor(getActivity(), R.color.transparent_70));
                    break;
                case "99000000":
                    //Apply tint
                    contentImage.setColorFilter(ContextCompat.getColor(getActivity(), R.color.transparent_60));
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
            textLongShort.setTypeface(getFontType(fontType, getActivity()), Typeface.NORMAL);
        } else if (!isItalic && isBold) {
            //Set typeface to bold
            textLongShort.setTypeface(getFontType(fontType, getActivity()), Typeface.BOLD);
        } else if (isItalic && !isBold) {
            //Set typeface to italic
            textLongShort.setTypeface(getFontType(fontType, getActivity()), Typeface.ITALIC);
        } else if (isItalic && isBold) {
            //Set typeface to bold_italic
            textLongShort.setTypeface(getFontType(fontType, getActivity()), Typeface.BOLD_ITALIC);
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
                    , ContextCompat.getColor(getActivity(), R.color.color_grey_600));
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
            ViewHelper.getToast(getActivity(), "Reading mode enabled");

        } else {
            //show toast
            ViewHelper.getToast(getActivity(), "Reading mode disabled");
            // set original shadow
            applyTextShadow();
            // set original tint
            applyImageTint();
        }
    }

    public void toggleSoundMode(boolean isSoundEnabled) {

        // enable toggling only if sound is not none
        if (!mShortData.getBgSound().equals(LongShortHelper.LONG_FORM_SOUND_NONE)) {
            // if sound is enabled from settings or this screen
            if (isSoundEnabled) {
                // start
                mediaPlayer.start();
                // set looping
                mediaPlayer.setLooping(true);
            } else {
                // pause sound
                mediaPlayer.pause();
            }

        }
    }

    /**
     * Frees the media player instance
     */
    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            // reset to idle state
            mediaPlayer.reset();
            // release
            mediaPlayer.release();
            // set to null
            mediaPlayer = null;
        }

    }

    /**
     * Initializes the media player and plays sound if sound enabled is true
     *
     * @param isSoundEnabled
     */
    public void initMediaPlayer(boolean isSoundEnabled) {

        // create new instance only if null
        if (mediaPlayer == null) {
            // create instance
            mediaPlayer = new MediaPlayer();
            // play sound
            playSound(isSoundEnabled);
        }

    }

    /**
     * Sets the audio source and plays sound if sound enabled is true
     *
     * @param isSoundEnabled
     */
    public void playSound(boolean isSoundEnabled) {

        if (!mShortData.getBgSound().equals(LongShortHelper.LONG_FORM_SOUND_NONE)) {

            try {
                Uri uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/raw/" + mShortData.getBgSound());
                // set data source
                mediaPlayer.setDataSource(getActivity(), uri);
                // prepare media player
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();

            }
            // start only if enabled from settings or from this screen
            if (isSoundEnabled) {
                // start
                mediaPlayer.start();
                //set looping
                mediaPlayer.setLooping(true);

            }
        }

    }


    /**
     * Method to initialize live filter.
     *
     * @param filterName Name of filter to be applied.
     */
    private void initLiveFilters(String filterName) {
        switch (filterName) {
            case Constant.LIVE_FILTER_SNOW:
                whetherView.setWeatherData(PrecipType.SNOW);
                whetherView.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_RAIN:
                whetherView.setWeatherData(PrecipType.RAIN);
                whetherView.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_BUBBLE:
                liveFilterBubble.setVisibility(View.VISIBLE);
                break;
            case Constant.LIVE_FILTER_CONFETTI:
                konfettiView.setVisibility(View.VISIBLE);
                ViewHelper.showKonfetti(konfettiView);
                break;
            case Constant.LIVE_FILTER_NONE:
                //do nothing
                break;
            default:
                break;
        }
    }
    //endregion

}
