package com.thetestament.cread.dialog;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.DeepLinkHelper;
import com.thetestament.cread.helpers.FirebaseEventHelper;
import com.thetestament.cread.helpers.GifHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.utils.Constant;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.ShareHelper.isAppInstalled;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_FACEBOOK;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_INSTAGRAM;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_OTHER;
import static com.thetestament.cread.utils.Constant.SHARE_OPTION_WHATSAPP;

public class DialogHelper {

    public static void showCollabInvitationDialog(final FragmentActivity context, final CompositeDisposable compositeDisposable
            , final View rootView, final String liveFilter, final Bitmap bitmap, final FrameLayout frameLayout
            , final String uuid, final String authKey, final String entityID, final String entityUrl
            , final String creatorName, final String contentType, final String caption, final RelativeLayout watermarkView) {

        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_share_post, false)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        //Obtain view reference
        AppCompatImageView imageWhatsApp = dialog.getCustomView().findViewById(R.id.logoWhatsapp);
        AppCompatImageView imageFacebook = dialog.getCustomView().findViewById(R.id.logoFacebook);
        AppCompatImageView imageInstagram = dialog.getCustomView().findViewById(R.id.logoInstagram);
        AppCompatImageView imageMoreOptions = dialog.getCustomView().findViewById(R.id.logoMore);
        AppCompatTextView buttonDismiss = dialog.getCustomView().findViewById(R.id.btn_dismiss);

        //WhatsApp share
        imageWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dismiss dialog
                dialog.dismiss();
                if (isAppInstalled(context, Constant.PACKAGE_NAME_WHATSAPP)) {
                    //Log event
                    FirebaseEventHelper.logCollabInviteEvent(context);
                    //Live filter is present
                    if (GifHelper.hasLiveFilter(liveFilter)) {
                        new GifHelper(context, bitmap, frameLayout, SHARE_OPTION_WHATSAPP, true, watermarkView)
                                .startHandlerTask(new Handler(), 0);
                    } else {
                        //generate deep link and open share dialog
                        DeepLinkHelper.generateDeepLinkForCollabInvite(context
                                , compositeDisposable
                                , rootView
                                , uuid
                                , authKey
                                , entityID
                                , entityUrl
                                , creatorName
                                , contentType
                                , SHARE_OPTION_WHATSAPP
                                , caption);
                    }
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
                if (isAppInstalled(context, Constant.PACKAGE_NAME_FACEBOOK)) {
                    //Log event
                    FirebaseEventHelper.logCollabInviteEvent(context);
                    //Live filter is present
                    if (GifHelper.hasLiveFilter(liveFilter)) {
                        new GifHelper(context, bitmap, frameLayout, SHARE_OPTION_FACEBOOK, true,watermarkView)
                                .startHandlerTask(new Handler(), 0);
                    } else {
                        //generate deep link and open share dialog
                        DeepLinkHelper.generateDeepLinkForCollabInvite(context
                                , compositeDisposable
                                , rootView
                                , uuid
                                , authKey
                                , entityID
                                , entityUrl
                                , creatorName
                                , contentType
                                , SHARE_OPTION_FACEBOOK
                                , caption);
                    }
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
                if (isAppInstalled(context, Constant.PACKAGE_NAME_INSTAGRAM)) {
                    //Log event
                    FirebaseEventHelper.logCollabInviteEvent(context);
                    //Live filter is present
                    if (GifHelper.hasLiveFilter(liveFilter)) {
                        new GifHelper(context, bitmap, frameLayout, SHARE_OPTION_INSTAGRAM, true, watermarkView)
                                .startHandlerTask(new Handler(), 0);
                    } else {
                        //generate deep link and open share dialog
                        DeepLinkHelper.generateDeepLinkForCollabInvite(context
                                , compositeDisposable
                                , rootView
                                , uuid
                                , authKey
                                , entityID
                                , entityUrl
                                , creatorName
                                , contentType
                                , SHARE_OPTION_INSTAGRAM
                                , caption);
                    }
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
                //Log event
                FirebaseEventHelper.logCollabInviteEvent(context);
                //Live filter is present
                if (GifHelper.hasLiveFilter(liveFilter)) {
                    new GifHelper(context, bitmap, frameLayout, SHARE_OPTION_OTHER, true, watermarkView)
                            .startHandlerTask(new Handler(), 0);
                } else {
                    //generate deep link and open share dialog
                    DeepLinkHelper.generateDeepLinkForCollabInvite(context
                            , compositeDisposable
                            , rootView
                            , uuid
                            , authKey
                            , entityID
                            , entityUrl
                            , creatorName
                            , contentType
                            , SHARE_OPTION_OTHER
                            , caption);
                }
            }
        });


        //Dismiss btn click functionality
        buttonDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hide dialog
                dialog.dismiss();
                // finish activity
                context.finish();
            }
        });


    }

}
