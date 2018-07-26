package com.thetestament.cread.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.PreviewActivity;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.utils.Constant;

import static com.thetestament.cread.helpers.DeletePostHelper.showDeleteConfirmationDialog;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_IMAGE_HEIGHT;
import static com.thetestament.cread.utils.Constant.EXTRA_IMAGE_WIDTH;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CAPTION_TEXT;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_CONTENT_IMAGE;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.PREVIEW_EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_POST;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM_EDIT_SHORT;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CAPTION_TEXT;


/**
 * Helper class which provides utility methods related to content deletion and editing.
 */

public class ContentHelper {

    /**
     * Method to show bottomSheet dialog with 'edit a post' and 'delete a post' option.
     *
     * @param context                 Context to use.
     * @param index                   Position of item in the list.
     * @param data                    FeedModel data.
     * @param onContentDeleteListener DeleteListener reference.
     */

    public static void getMenuActionsBottomSheet(final FragmentActivity context, final int index,
                                                 final FeedModel data
            , final listener.OnContentDeleteListener onContentDeleteListener) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate this view
        View sheetView = context.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_content_actions
                        , null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        // init views
        LinearLayout buttonDelete = sheetView.findViewById(R.id.buttonDelete);
        LinearLayout buttonEdit = sheetView.findViewById(R.id.buttonEdit);

        buttonDelete.setVisibility(View.VISIBLE);
        buttonEdit.setVisibility(View.VISIBLE);


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

    /**
     * Method to show bottomSheet dialog with 'edit a post' and 'delete a post' option.
     *
     * @param context                 Context to use.
     * @param index                   Position of item in the list.
     * @param data                    FeedModel data.
     * @param onReposttDeleteListener DeleteListener reference.
     */

    public static void getRepostMenuBottomSheet(final FragmentActivity context, final int index,
                                                final FeedModel data
            , final listener.OnRepostDeleteListener onReposttDeleteListener) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate this view
        View sheetView = context.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_content_actions
                        , null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        // init views
        LinearLayout buttonDelete = sheetView.findViewById(R.id.buttonDelete);
        LinearLayout buttonEdit = sheetView.findViewById(R.id.buttonEdit);

        buttonDelete.setVisibility(View.VISIBLE);
        buttonEdit.setVisibility(View.GONE);


        //Delete button functionality
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteRepostConfirmationDialog(context, index, data.getRepostID(), onReposttDeleteListener);
                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }


    /**
     * Method to show confirmation dialog before deletion.
     *
     * @param context                Context to use.
     * @param index                  position of item in adapter.
     * @param repostID               Repost id of content.
     * @param onRepostDeleteListener OnRepostDeleteListener
     */
    private static void showDeleteRepostConfirmationDialog(FragmentActivity context, final int index, final String repostID, final listener.OnRepostDeleteListener onRepostDeleteListener) {
        new MaterialDialog.Builder(context)
                .content("Are you sure want to remove this repost?")
                .positiveText("Remove")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        onRepostDeleteListener.onDelete(repostID, index);
                        materialDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .build()
                .show();
    }


    /**
     * Method to launch required screen for content editing.
     *
     * @param data    FeedModel reference.
     * @param context Context to use.
     */
    public static void launchContentEditingScreen(FeedModel data, FragmentActivity context) {

        switch (data.getContentType()) {
            case CONTENT_TYPE_CAPTURE:
                Intent intent = new Intent(context, PreviewActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(PREVIEW_EXTRA_ENTITY_ID, data.getEntityID());
                bundle.putString(PREVIEW_EXTRA_CAPTION_TEXT, data.getCaption());
                bundle.putString(PREVIEW_EXTRA_CONTENT_IMAGE, data.getContentImage());
                bundle.putInt(EXTRA_IMAGE_WIDTH, data.getImgWidth());
                bundle.putInt(EXTRA_IMAGE_HEIGHT, data.getImgHeight());
                bundle.putString(Constant.PREVIEW_EXTRA_LIVE_FILTER, data.getLiveFilterName());
                bundle.putString(PREVIEW_EXTRA_CALLED_FROM, PREVIEW_EXTRA_CALLED_FROM_EDIT_CAPTURE);
                intent.putExtra(PREVIEW_EXTRA_DATA, bundle);

                context.startActivityForResult(intent, REQUEST_CODE_EDIT_POST);

                break;
            case CONTENT_TYPE_SHORT:
                Intent intentShort = new Intent(context, ShortActivity.class);

                Bundle bundleShort = new Bundle();
                bundleShort.putString(EXTRA_ENTITY_ID, data.getEntityID());
                bundleShort.putString(EXTRA_ENTITY_TYPE, data.getContentType());
                bundleShort.putString(Constant.EXTRA_LIVE_FILTER, data.getLiveFilterName());
                bundleShort.putString(SHORT_EXTRA_CALLED_FROM, SHORT_EXTRA_CALLED_FROM_EDIT_SHORT);
                bundleShort.putBoolean(EXTRA_MERCHANTABLE, data.isMerchantable());
                bundleShort.putString(SHORT_EXTRA_CAPTION_TEXT, data.getCaption());
                intentShort.putExtra(EXTRA_DATA, bundleShort);

                context.startActivityForResult(intentShort, REQUEST_CODE_EDIT_POST);
                break;
            default:
        }
    }

}
