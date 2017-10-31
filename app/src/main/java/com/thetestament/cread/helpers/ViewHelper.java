package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

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

    /**
     * Method to show tooltip.
     *
     * @param context Context to use.
     * @param message Message to be displayed.
     */
    public static void getToast(Context context, String message) {
        Toast.makeText(context
                , message
                , Toast.LENGTH_LONG)
                .show();
    }

    public static int convertToPx(Context context, int dp)
    {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());

        return px;
    }
}
