package com.thetestament.cread.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.DeepLinkHelper;
import com.thetestament.cread.utils.Constant;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_COLLAB_INVITE_CLICKED;

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


    public static void showCollabInvitationDialog(final FragmentActivity context, final CompositeDisposable compositeDisposable, final View rootView, final String uuid, final String authkey, final String entityID, final String entityUrl, final String creatorName, final String contentType) {

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_generic, false)
                .negativeText(context.getString(R.string.text_dialog_button_later))
                .positiveText(context.getString(R.string.text_dialog_button_invite))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // finish activity
                        context.finish();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Bundle bundle = new Bundle();
                        bundle.putString("uuid", uuid);
                        FirebaseAnalytics.getInstance(context)
                                .logEvent(FIREBASE_EVENT_COLLAB_INVITE_CLICKED, bundle);

                        //generate deep link and open share dialog
                        DeepLinkHelper.generateDeepLinkForCollabInvite(context
                                , compositeDisposable
                                , rootView
                                , uuid
                                , authkey
                                , entityID
                                , entityUrl
                                , creatorName
                                , contentType);

                    }
                })
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_collab_intro));

        if (contentType.equals(Constant.CONTENT_TYPE_CAPTURE)) {
            //Set title text
            textTitle.setText(context.getString(R.string.text_title_dialog_capture_collab_invitation));
            //Set description text
            textDesc.setText(context.getString(R.string.text_desc_dialog_capture_collab_invitation));
        } else {
            //Set title text
            textTitle.setText(context.getString(R.string.text_title_dialog_short_collab_invitation));
            //Set description text
            textDesc.setText(context.getString(R.string.text_desc_dialog_short_collab_invitation));
        }

    }

}
