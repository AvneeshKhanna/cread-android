package com.thetestament.cread.dialog;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.thetestament.cread.R;

/**
 * Utility class for dialogs
 */

public class CustomDialog {

    /**
     * Method to show dialog prompt when user navigate back from the screen with yes and no options.
     *
     * @param context Context to use.
     * @param title
     **/
    public static void getBackNavigationDialog(final FragmentActivity context, String title, String content) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(R.string.text_yes)
                .negativeText(R.string.text_no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Navigate back to previous screen
                        context.finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss this dialog
                        dialog.dismiss();
                    }
                })
                .build().show();

    }

    /**
     * Method to show Generic dialog.
     *
     * @param context           Context to use.
     * @param positiveBtnText   Text for positive button.
     * @param dialogTitleText   Text for dialog title.
     * @param dialogContentText Text for dialog content.
     * @param fillerImageID     Drawable ID of image to be displayed as filler.
     */
    public static void getGenericDialog(FragmentActivity context, String positiveBtnText, String dialogTitleText
            , String dialogContentText, int fillerImageID) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_generic, false)
                .positiveText(positiveBtnText)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);

        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(context, fillerImageID));
        //Set title text
        textTitle.setText(dialogTitleText);
        //Set description text
        textDesc.setText(dialogContentText);
    }

    /**
     * Method to show Generic dialog with Spannable arguments.
     *
     * @param context           Context to use.
     * @param positiveBtnText   Text for positive button.
     * @param dialogTitleText   Text for dialog title.
     * @param dialogContentText Text for dialog content.
     * @param fillerImageID     Drawable ID of image to be displayed as filler.
     */
    public static void getGenericDialog(FragmentActivity context, String positiveBtnText, SpannableString dialogTitleText
            , SpannableString dialogContentText, int fillerImageID) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_generic, false)
                .positiveText(positiveBtnText)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Dismiss dialog
                        dialog.dismiss();
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);

        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(context, fillerImageID));
        //Set title text
        textTitle.setText(dialogTitleText);
        //Set description text
        textDesc.setText(dialogContentText);
    }

    /**
     * To return the indeterminate progress dialog.
     *
     * @param context     context to0 use.
     * @param contentText Text to be displayed.
     */

    public static MaterialDialog getProgressDialog(FragmentActivity context, String contentText) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .content(contentText)
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();
        return dialog;
    }


}
