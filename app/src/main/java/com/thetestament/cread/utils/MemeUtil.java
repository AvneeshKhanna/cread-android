package com.thetestament.cread.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ViewHelper;

import java.io.File;

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
        String text;
        if (textView.getText().toString().equals(context.getString(R.string.hint_text_meme))) {
            text = "";
        } else {
            text = textView.getText().toString();
        }

        new MaterialDialog.Builder(context)
                .title("Place your text here")
                .autoDismiss(false)
                /*.inputRange(1, 80, ContextCompat.getColor(getActivity(), R.color.red))*/
                .inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(null, text, false, new MaterialDialog.InputCallback() {
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

    /**
     * Method to load image if its exist in device storage.
     *
     * @param filePath  Path of the file.
     * @param context   Context to use.
     * @param imageView ImageView where image to be loaded.
     */
    public static void setImageIfExist(String filePath, FragmentActivity context, AppCompatImageView imageView) {
        File file = new File(filePath);

        if (file.exists()) {
            //Set scale type
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            //Load image here
            Picasso.with(context)
                    .load(file)
                    .error(R.drawable.image_placeholder)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageView);
        }

    }
}
