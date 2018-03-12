package com.thetestament.cread.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.activities.PreviewActivity;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.FeedModel;

import io.reactivex.disposables.CompositeDisposable;

import static android.app.Activity.RESULT_OK;
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
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_EDIT_POST;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM_EDIT_SHORT;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CAPTION_TEXT;


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
                bundleShort.putBoolean(EXTRA_MERCHANTABLE, data.isMerchantable());
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
            , final FeedModel data, final listener.OnContentDeleteListener onContentDeleteListener, boolean shouldShowCreatorOptions, final CompositeDisposable compositeDisposable, final Bundle resultBundle, final Intent resultIntent) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate this view
        View sheetView = context.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_content_actions
                        , null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        if(shouldShowCreatorOptions && data.isEligibleForDownvote())
        {
            //init options
            initContentCreatorOptions(bottomSheetDialog, sheetView, context, index, data, onContentDeleteListener);
            initDownvoteOption(sheetView, data, context, resultBundle, resultIntent, compositeDisposable);

        }

        else if(shouldShowCreatorOptions)
        {
            initContentCreatorOptions(bottomSheetDialog, sheetView, context, index, data, onContentDeleteListener);
        }


        else if(data.isEligibleForDownvote())
        {
            initDownvoteOption(sheetView, data, context, resultBundle, resultIntent, compositeDisposable);
        }
    }


    private static void initContentCreatorOptions(final BottomSheetDialog bottomSheetDialog, View sheetView, final FragmentActivity context, final int index, final FeedModel data, final listener.OnContentDeleteListener onContentDeleteListener)
    {
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

    private static void initDownvoteOption(View sheetView, final FeedModel data, final FragmentActivity context, final Bundle resultBundle, final Intent resultIntent, final CompositeDisposable compositeDisposable)
    {
        LinearLayout buttonDownvote = sheetView.findViewById(R.id.buttonDownvote);

        buttonDownvote.setVisibility(View.VISIBLE);

        final TextView textDownvote = sheetView.findViewById(R.id.textDownvote);
        final ImageView iconDownvote = sheetView.findViewById(R.id.imageDownvote);

        // set downvote text
        final DownvoteHelper downvoteHelper = new DownvoteHelper();
        downvoteHelper.updateDownvoteText(textDownvote, iconDownvote, data.isDownvoteStatus(), context);

        // click functionality
        buttonDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update text
                data.setDownvoteStatus(!data.isDownvoteStatus());
                downvoteHelper.updateDownvoteText(textDownvote, iconDownvote, data.isDownvoteStatus(), context);

                downvoteHelper.updateDownvoteStatus(context, compositeDisposable, data.isDownvoteStatus(), data.getEntityID(), new listener.OnDownvoteRequestedListener() {
                    @Override
                    public void onDownvoteSuccess() {

                        // do nothing
                        resultBundle.putBoolean("downvotestatus", data.isDownvoteStatus());
                        context.setResult(RESULT_OK, resultIntent);

                    }

                    @Override
                    public void onDownvoteFailiure(String errorMsg) {

                        ViewHelper.getToast(context, errorMsg);
                        // revert status
                        data.setDownvoteStatus(!data.isDownvoteStatus());
                        downvoteHelper.updateDownvoteText(textDownvote, iconDownvote, data.isDownvoteStatus(), context);

                    }
                });

            }
        });

    }
}
