package com.thetestament.cread.dialog;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

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

}
