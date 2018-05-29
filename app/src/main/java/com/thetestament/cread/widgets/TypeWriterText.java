package com.thetestament.cread.widgets;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Custom textView implementation for typewriter effect.
 */

public class TypeWriterText extends AppCompatTextView {
    private CharSequence mText;
    private int mIndex;
    private long mDelay = 150; // in ms
    private OnAnimationFinishListener animationListener;


    /**
     * Interface definition for a callback to be invoked typewriter animation finishes.
     */
    public interface OnAnimationFinishListener {
        void onFinish();
    }

    /**
     * Register a callback to be invoked when  typewriter animation finishes.
     */
    public void setOnAnimationFinishListener(OnAnimationFinishListener listener) {
        animationListener = listener;
    }

    //region :Constructor
    public TypeWriterText(Context context) {
        super(context);
    }

    public TypeWriterText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TypeWriterText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    //endregion

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            /*if (mIndex<mText.length()){
                setText(mText.subSequence(0, mIndex++));
                mHandler.postDelayed(this, mDelay);
            }*/
            setText(mText.subSequence(0, mIndex++));
            if (mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            } else {
                //Set listener if not null
                if (animationListener != null) {
                    animationListener.onFinish();
                }
            }

        }
    };

    public void animateText(CharSequence txt) {
        mText = txt;
        mIndex = 0;
        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    /**
     * Method to set delays b/w characters..
     *
     * @param m Delay in milliseconds.
     */
    public void setCharacterDelay(long m) {
        mDelay = m;
    }
}

