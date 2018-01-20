package com.thetestament.cread.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.thetestament.cread.R;
import com.thetestament.cread.activities.PreviewActivity;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;

import static com.thetestament.cread.helpers.DeletePostHelper.showDeleteConfirmationDialog;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTION_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CONTENT_IMAGE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_CAPTURE;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_SHORT;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM_EDIT_SHORT;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CAPTION_TEXT;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_POST;


/**
 * Helper class which provides utility methods related to content deletion and editing.
 */

public class ContentHelper {

    /**
     * Method to launch required screen for content editing.
     *
     * @param data FeedModel reference.
     */
    public static void launchContentEditingScreen(FeedModel data, FragmentActivity context) {

        switch (data.getContentType()) {
            case CONTENT_TYPE_CAPTURE:
                Intent intent = new Intent(context, PreviewActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(PREVIEW_EXTRA_ENTITY_ID, data.getEntityID());
                bundle.putString(PREVIEW_EXTRA_CAPTION_TEXT, data.getCaption());
                bundle.putString(PREVIEW_EXTRA_CONTENT_IMAGE, data.getContentImage());
                bundle.putString(PREVIEW_EXTRA_CALLED_FROM, PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE);
                intent.putExtra(PREVIEW_EXTRA_DATA, bundle);

                context.startActivityForResult(intent, REQUEST_CODE_EDIT_POST);

                break;
            case CONTENT_TYPE_SHORT:
                Intent intentShort = new Intent(context, ShortActivity.class);

                Bundle bundleShort = new Bundle();
                bundleShort.putString(EXTRA_ENTITY_ID, data.getEntityID());
                bundleShort.putString(EXTRA_ENTITY_TYPE, data.getContentType());
                bundleShort.putString(SHORT_EXTRA_CALLED_FROM, SHORT_EXTRA_CALLED_FROM_EDIT_SHORT);
                bundleShort.putString(EXTRA_MERCHANTABLE, String.valueOf(data.isMerchantable()));
                bundleShort.putString(SHORT_EXTRA_CAPTION_TEXT, data.getCaption());
                intentShort.putExtra(EXTRA_DATA, bundleShort);

                context.startActivityForResult(intentShort, REQUEST_CODE_EDIT_POST);
                break;
            default:
        }
    }


    /**
     * Method to show bottomSheet dialog with 'write a short' and 'Upload a capture' option.
     */

    public static void getMenuActionsBottomSheet(final FragmentActivity context, final int index
            , final FeedModel data, final listener.OnContentDeleteListener onContentDeleteListener) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate this view
        View sheetView = context.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_content_actions
                        , null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        LinearLayout buttonDelete = sheetView.findViewById(R.id.buttonDelete);
        LinearLayout buttonEdit = sheetView.findViewById(R.id.buttonEdit);

        //Delete button functionality
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(context
                        , index
                        , data.getEntityID()
                        , onContentDeleteListener);
                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Method called
                launchContentEditingScreen(data, context);
                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }
}
