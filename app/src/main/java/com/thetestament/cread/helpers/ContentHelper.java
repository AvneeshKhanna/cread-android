package com.thetestament.cread.helpers;

import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;

import static com.thetestament.cread.helpers.DeletePostHelper.showDeleteConfirmationDialog;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;

/**
 * Helper class which provides utility methods related to content deletion and editing.
 */

public class ContentHelper {

    /**
     * Method to launch required screen fro content editing.
     */
    public static void launchContentEditingScreen(FeedModel data) {

        switch (data.getContentType()) {
            case CONTENT_TYPE_CAPTURE:
                //Stand alone capture
                if (data.isAvailableForCollab()) {

                }
                //Capture on short
                else {

                }
                break;
            case CONTENT_TYPE_SHORT:
                //Stand alone short
                if (data.isAvailableForCollab()) {

                }
                //Short on capture
                else {

                }
                break;
            default:
        }
    }


    /**
     * Method to show bottomSheet dialog with 'write a short' and 'Upload a capture' option.
     */
    public static void getMenuActionsBottomSheet(final FragmentActivity context, final int index, final FeedModel data, final listener.OnContentDeleteListener onContentDeleteListener) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = context.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_content_actions, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        // TODO add edit button

        LinearLayout buttonDelete = sheetView.findViewById(R.id.buttonDelete);


        //Delete button functionality
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDeleteConfirmationDialog(context, index, data.getEntityID(), onContentDeleteListener);

                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }
}
