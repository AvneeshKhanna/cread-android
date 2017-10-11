package com.thetestament.cread.helpers;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Helper class to show snack bar.
 */

public class ViewHelper {

    /**
     * Method to show snack bar.
     *
     * @param viewParent Parent view of the layout where snack bar to be shown.
     * @param message    Message to be displayed on the snack bar
     */
    public static void getSnackBar(View viewParent, String message) {
        Snackbar.make(viewParent
                , message
                , Snackbar.LENGTH_LONG)
                .show();
    }
}
