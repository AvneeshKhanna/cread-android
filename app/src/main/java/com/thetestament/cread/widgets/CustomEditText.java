package com.thetestament.cread.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Custom AppCompatEditText class.
 */

public class CustomEditText extends android.support.v7.widget.AppCompatEditText {


    private OnEditTextBackListener onEditTextBackListener;


    /**
     * Interface definition for a callback to be invoked when user clicks on back button.
     */
    public interface OnEditTextBackListener {
        void onBack();
    }

    /**
     * Register a callback to be invoked when user clicks on back button.
     */
    public void setOnEditTextBackListener(OnEditTextBackListener listener) {
        onEditTextBackListener = listener;
    }

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (onEditTextBackListener != null)
                onEditTextBackListener.onBack();
        }
        return super.dispatchKeyEvent(event);
    }

}
