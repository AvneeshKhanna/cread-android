package com.thetestament.cread.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.thetestament.cread.R;
import com.tooltip.Tooltip;

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

    /**
     * Method to show tooltip for short duration.
     *
     * @param context Context to use.
     * @param message Message to be displayed.
     */
    public static void getShortToast(Context context, String message) {
        Toast.makeText(context
                , message
                , Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Method to show tooltip.
     *
     * @param view    View where tooltip to be shown.
     * @param msg     Tooltip message.
     * @param context Context to use.
     */
    public static void getToolTip(View view, String msg, Context context) {
        Tooltip tooltip = new Tooltip.Builder(view)
                .setText(msg)
                .setCancelable(true)
                .setTextColor(ContextCompat.getColor(context, R.color.white))
                .setBackgroundColor(ContextCompat.getColor(context, R.color.black_dark))
                .setCornerRadius(R.dimen.tooltip_border_radius)
                .setTypeface(ResourcesCompat.getFont(context, R.font.helvetica_neue_medium))
                .setBackgroundColor(ContextCompat.getColor(context, R.color.grey_dark))
                .show();
    }


    /**
     * Method to scroll recycler view to next item if last selected item was last visible/first visible item.
     *
     * @param layoutManager LinearLayout manger.
     * @param recyclerView  RecyclerView recycler view reference.
     * @param itemPosition  Position of selected item in list.
     * @param listSize      Size of list which contains all the item.
     */
    public static void scrollToNextItemPosition(LinearLayoutManager layoutManager, RecyclerView recyclerView, int itemPosition, int listSize) {
        if ((layoutManager.findLastCompletelyVisibleItemPosition() == itemPosition
                || layoutManager.findLastVisibleItemPosition() == itemPosition)
                && (itemPosition != listSize - 1)) {
            recyclerView.scrollToPosition(itemPosition + 1);
        }

        if ((layoutManager.findFirstCompletelyVisibleItemPosition() == itemPosition
                || layoutManager.findFirstVisibleItemPosition() == itemPosition)
                && (itemPosition != 0)) {
            recyclerView.smoothScrollToPosition(itemPosition - 1);
        }
    }

    public static int convertToPx(Context context, int dp) {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return px;
    }


    public static float pixelsToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }


}
