package com.thetestament.cread.helpers;

import android.support.v4.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;

public class DialogHelper {

    public static MaterialDialog getDeletePostDialog(FragmentActivity context) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .title("Deleting")
                .content("Please wait...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        return dialog;
    }
}
