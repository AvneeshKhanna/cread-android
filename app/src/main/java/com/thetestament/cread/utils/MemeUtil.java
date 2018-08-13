package com.thetestament.cread.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.thetestament.cread.helpers.ViewHelper;

/**
 * Class to provide utility methods for memes.
 */

public class MemeUtil {

    /**
     * Method to show input dialog where user enters text for meme.
     *
     * @param context  Context to use.
     * @param textView
     */
    public static void showMemeInputDialog(final FragmentActivity context, final AppCompatTextView textView) {
        new MaterialDialog.Builder(context)
                .title("Enter here your text")
                .autoDismiss(false)
                /*.inputRange(1, 80, ContextCompat.getColor(getActivity(), R.color.red))*/
                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(null, textView.getText(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String s = String.valueOf(input).trim();
                        if (s.length() < 1) {
                            ViewHelper.getToast(context, "This field can't be empty");
                        } else {
                            //Set text here
                            textView.setText(s);
                            //Dismiss
                            dialog.dismiss();
                        }
                    }
                })
                .build()
                .show();
    }

}
