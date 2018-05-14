package com.thetestament.cread.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.DeepLinkHelper;
import com.thetestament.cread.helpers.ViewHelper;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.ShareHelper.isAppInstalled;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_COLLAB_INVITE_CLICKED;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_FACEBOOK;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_INSTAGRAM;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_WHATSAPP;

public class DialogHelper {

    public static MaterialDialog getDeletePostDialog(FragmentActivity context) {
        //To show the progress dialog
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                /*.title("Deleting")*/
                .content("Deleting...")
                .autoDismiss(false)
                .cancelable(false)
                .progress(true, 0);
        final MaterialDialog dialog = builder.build();
        dialog.show();

        return dialog;
    }


    public static void showCollabInvitationDialog(final FragmentActivity context, final CompositeDisposable compositeDisposable, final View rootView, final String uuid, final String authkey, final String entityID, final String entityUrl, final String creatorName, final String contentType, final String caption) {

        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_share_post, false)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        //Obtain views reference
        AppCompatImageView imageWhatsapp = dialog.getCustomView().findViewById(R.id.logoWhatsapp);
        AppCompatImageView imageFacebook = dialog.getCustomView().findViewById(R.id.logoFacebook);
        AppCompatImageView imageInstagram = dialog.getCustomView().findViewById(R.id.logoInstagram);
        AppCompatImageView imageMoreOptions = dialog.getCustomView().findViewById(R.id.logoMore);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView buttonDismiss = dialog.getCustomView().findViewById(R.id.buttonCancel);

        // init whatsapp share
        imageWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

                if (isAppInstalled(context, "com.whatsapp")) {
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
                            , contentType
                            , SHARE_OPTION_WHATSAPP
                            , caption);
                } else {
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Whatsapp installed");
                    context.finish();
                }


            }
        });

        // init facebook share
        imageFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

                if (isAppInstalled(context, "com.facebook.katana")) {
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
                            , contentType
                            , SHARE_OPTION_FACEBOOK
                            , caption);
                } else {
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Facebook installed");
                    context.finish();
                }


            }
        });

        // init instagram share
        imageInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

                if (isAppInstalled(context, "com.instagram.android")) {
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
                            , contentType
                            , SHARE_OPTION_INSTAGRAM
                            , caption);
                } else {
                    ViewHelper.getToast(context, "Problem in sharing because you might not have Instagram installed");
                    context.finish();
                }


            }
        });

        // init other share options
        imageMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();

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
                        , contentType
                        , SHARE_OPTION_OTHER
                        , caption);

            }
        });


        buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // finish activity
                context.finish();
            }
        });


    }

}
