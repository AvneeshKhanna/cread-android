package com.thetestament.cread.helpers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.adapters.ShareDialogAdapter;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnShareDialogItemClickedListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.ListItemsDialogModel;
import com.thetestament.cread.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.helpers.ProfileMentionsHelper.setProfileMentionsForViewing;
import static com.thetestament.cread.helpers.ViewHelper.convertToPx;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_IMAGE_HEIGHT;
import static com.thetestament.cread.utils.Constant.EXTRA_IMAGE_WIDTH;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.SHORT_EXTRA_CALLED_FROM_COLLABORATION_SHORT;
import static com.thetestament.cread.utils.Constant.URI_HASH_TAG_ACTIVITY;
import static com.thetestament.cread.utils.TimeUtils.getCustomTime;


/**
 * Helper class for feeds.
 */
public class FeedHelper {

    static listener.OnCollaborationListener onCollaborationListener;


    public void setOnCaptureClickListener(listener.OnCollaborationListener onCollaborationListener) {
        this.onCollaborationListener = onCollaborationListener;
    }

    /**
     * Method to create text with 2 clicks for opening user profiles
     *
     * @param context
     * @param text               Text to create as spannable
     * @param creatorStartPos    creator name starting index in text
     * @param creatorEndPos      creator name ending index in text
     * @param collabWithStartPos collaborate with user name start index in text
     * @param collabWithEndPos   collaborate with user name end index in text
     * @param creatorUUID        creator userid
     * @param collabWithUUID     collaborate with uuid
     */
    public static void initializeSpannableString(final FragmentActivity context, TextView textView, boolean hasTwoClicks, String text, int creatorStartPos, int creatorEndPos, int collabWithStartPos, int collabWithEndPos, final String creatorUUID, final String collabWithUUID, boolean isCreatorCollaborator) {
        SpannableString ss = new SpannableString(text);
        ClickableSpan collaboratorSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                //Method called
                IntentHelper.openProfileActivity(context, creatorUUID);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                ds.setColor(ContextCompat.getColor(context, R.color.grey_dark));
            }
        };
        ss.setSpan(collaboratorSpan, creatorStartPos, creatorEndPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (hasTwoClicks && !isCreatorCollaborator) {
            ClickableSpan collaboratedWithSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    //Method called
                    IntentHelper.openProfileActivity(context, collabWithUUID);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    ds.setColor(ContextCompat.getColor(context, R.color.grey_dark));
                }
            };
            ss.setSpan(collaboratedWithSpan, collabWithStartPos, collabWithEndPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    /**
     * Method to set the creator text name depending on content type and collaboration status
     *
     * @param context
     * @param contentType
     * @param isAvailableforCollab
     * @param creatorName
     * @param collaboratorName
     * @return
     */
    public static String getCreatorText(FragmentActivity context, String contentType, boolean isAvailableforCollab, String creatorName, String collaboratorName, boolean isCreatorCollaborator) {
        String text = null;

        switch (contentType) {
            case CONTENT_TYPE_CAPTURE:

                if (isCreatorCollaborator) {
                    text = creatorName + " " + context.getString(R.string.creator_text_collab_capture_self);
                } else {
                    text = isAvailableforCollab ? creatorName + " " + context.getString(R.string.creator_text_standalone_capture)
                            : (creatorName + " " + context.getString(R.string.creator_text_collab_capture_part1) + " "
                            + collaboratorName + "'s " + context.getString(R.string.creator_text_collab_capture_part2));
                }

                break;

            case CONTENT_TYPE_SHORT:

                if (isCreatorCollaborator) {
                    text = creatorName + " " + context.getString(R.string.creator_text_collab_short_self);
                } else {

                    text = isAvailableforCollab ? creatorName + " " + context.getString(R.string.creator_text_standalone_short)
                            : (creatorName + " " + context.getString(R.string.creator_text_collab_short_part1) + " "
                            + collaboratorName + "'s " + context.getString(R.string.creator_text_collab_short_part2));

                }

                break;
        }

        return text;
    }

    /**
     * Method to get the collaboration count text according to type and count
     *
     * @param context     context
     * @param count       collaboration count
     * @param contentType capture or short
     * @return
     */
    public static String getCollabCountText(FragmentActivity context, long count, String contentType, boolean isAvailableForCollab) {
        String text = null;

        switch (contentType) {
            case CONTENT_TYPE_CAPTURE:

                if (isAvailableForCollab) {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_short_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_short_count_text_single);
                } else {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_single);
                }

                break;

            case CONTENT_TYPE_SHORT:

                if (isAvailableForCollab) {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_capture_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_capture_count_text_single);
                } else {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_single);
                }

                break;
        }

        return text;
    }


    /**
     * @param count the value of the count which to be checked
     * @return true if count is more than 1 else false
     */

    public static boolean isMultiple(long count) {
        return count != 1;
    }

    public static void inviteFriends(FragmentActivity context, String deepLink) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.text_invite_friends, deepLink));
        context.startActivity(Intent.createChooser(intent, "Invite Friends"));
    }


    /**
     * Parses the hash tags and creates them as links
     *
     * @param textView    text view which contains the text from which hash tags are parsed
     * @param context     context
     * @param textColorID Resource ID of color to be applied on hash tags.
     */
    public void setHashTags(TextView textView, FragmentActivity context, int textColorID, long count) {
        textView.setLinkTextColor(ContextCompat.getColor(context, textColorID));
        //Pattern to find if there's a hash tag in the message
        //i.e. any word starting with a # and containing letter or numbers or _
        Pattern tagMatcher = Pattern.compile("\\#\\w+", Pattern.CASE_INSENSITIVE);
        // attach linkify to text view for click action of hash tags
        // appended count for the case when hashtag feed is opened directly from hash tag of the day
        Linkify.addLinks(textView, tagMatcher, URI_HASH_TAG_ACTIVITY + String.valueOf(count) + ":");
        // to remove underlines from the hashtag links
        stripUnderlines(textView);
    }

    /**
     * Removes the underline of the spannable texts
     */
    public void stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }


    public static FeedModel parseEntitySpecificJSON(JSONObject jsonObject, String entityID) throws JSONException {
        //fixme this too
        FeedModel feedData = new FeedModel();

        JSONObject mainObject = jsonObject.getJSONObject("data");

        JSONObject dataObj = mainObject.getJSONObject("entity");
        String type = dataObj.getString("type");

        feedData.setEntityID(entityID);
        feedData.setCaptureID(dataObj.getString("captureid"));
        feedData.setContentType(dataObj.getString("type"));
        feedData.setUUID(dataObj.getString("uuid"));
        feedData.setCreatorImage(dataObj.getString("profilepicurl"));
        feedData.setCreatorName(dataObj.getString("creatorname"));
        feedData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
        feedData.setMerchantable(dataObj.getBoolean("merchantable"));
        feedData.setDownvoteStatus(dataObj.getBoolean("downvotestatus"));
        feedData.setEligibleForDownvote(mainObject.getBoolean("candownvote"));
        feedData.setPostTimeStamp(dataObj.getString("regdate"));
        feedData.setLongForm(dataObj.getBoolean("long_form"));
        feedData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
        feedData.setCommentCount(dataObj.getLong("commentcount"));
        feedData.setContentImage(dataObj.getString("entityurl"));
        feedData.setFollowStatus(dataObj.getBoolean("followstatus"));
        feedData.setCollabCount(dataObj.getLong("collabcount"));

        if (dataObj.has("img_width") || dataObj.has("img_height")) {
            //if image width pr image height is null
            if (dataObj.isNull("img_width") || dataObj.isNull("img_height")) {
                feedData.setImgWidth(1);
                feedData.setImgHeight(1);
            } else {
                feedData.setImgWidth(dataObj.getInt("img_width"));
                feedData.setImgHeight(dataObj.getInt("img_height"));
            }
        }

        if (dataObj.isNull("caption")) {
            feedData.setCaption(null);
        } else {
            feedData.setCaption(dataObj.getString("caption"));
        }

        if (type.equals(CONTENT_TYPE_CAPTURE)) {

            //Retrieve "CAPTURE_ID" if type is capture
            feedData.setCaptureID(dataObj.getString("captureid"));
            // if capture
            // then if key cpshort exists
            // not available for collaboration
            if (!dataObj.isNull("cpshort")) {
                JSONObject collabObject = dataObj.getJSONObject("cpshort");

                feedData.setAvailableForCollab(false);
                // set collaborator details
                feedData.setCollabWithUUID(collabObject.getString("uuid"));
                feedData.setCollabWithName(collabObject.getString("name"));
                feedData.setCollaboWithEntityID(collabObject.getString("entityid"));

            } else {
                feedData.setAvailableForCollab(true);
            }

        } else if (type.equals(CONTENT_TYPE_SHORT)) {

            //Retrieve "SHORT_ID" if type is short
            feedData.setShortID(dataObj.getString("shoid"));

            // if short
            // then if key shcapture exists
            // not available for collaboration
            if (!dataObj.isNull("shcapture")) {

                JSONObject collabObject = dataObj.getJSONObject("shcapture");

                feedData.setAvailableForCollab(false);
                // set collaborator details
                feedData.setCollabWithUUID(collabObject.getString("uuid"));
                feedData.setCollabWithName(collabObject.getString("name"));
                feedData.setCollaboWithEntityID(collabObject.getString("entityid"));
            } else {
                feedData.setAvailableForCollab(true);
            }
        }
        return feedData;
    }

    public static List<ListItemsDialogModel> initializeItemsDialog(String[] titles, String[] bylines, int[] drawables) {
        List<ListItemsDialogModel> list = new ArrayList<>();


        for (int i = 0; i < titles.length; i++) {
            ListItemsDialogModel model = new ListItemsDialogModel();

            model.setTitle(titles[i]);
            model.setContent(bylines[i]);
            model.setDrawableResource(drawables[i]);

            list.add(model);

        }

        return list;
    }

    public static List<ListItemsDialogModel> initializeShareDialog(FragmentActivity context) {
        String[] titles = context.getResources()
                .getStringArray
                        (R.array.share_dialog_titles);

        String[] bylines = context.getResources()
                .getStringArray
                        (R.array.share_dialog_texts);


        int[] drawables = {R.drawable.ic_image_24, R.drawable.ic_link_24};


        return initializeItemsDialog(titles, bylines, drawables);
    }

    public static List<ListItemsDialogModel> initializeCollaborateDialog(FragmentActivity context) {
        String[] titles = context.getResources()
                .getStringArray
                        (R.array.collaborate_dialog_titles);

        String[] bylines = context.getResources()
                .getStringArray
                        (R.array.collaborate_dialog_texts);


        int[] drawables = {R.drawable.ic_add_write, R.drawable.ic_add_photo};


        return initializeItemsDialog(titles, bylines, drawables);
    }


    /**
     * Method to show dialog with options when user click on collaboration button.
     *
     * @param view     View to be clicked.
     * @param context  Context to use.
     * @param entityId entity ID of writing.
     */
    public static void collabOnCollab(View view, final FragmentActivity context, final String entityId, final boolean merchantable, final String entityType, final String collaboWithEntityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareDialogAdapter adapter = new ShareDialogAdapter
                        (context, initializeCollaborateDialog(context));

                final MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .adapter(adapter, null)
                        .show();

                adapter.setShareDialogItemClickedListener(new OnShareDialogItemClickedListener() {
                    @Override
                    public void onShareDialogItemClicked(int index) {
                        switch (index) {
                            //Write on photo/graphics art
                            case 0:
                                if (entityType.equals(CONTENT_TYPE_CAPTURE)) {
                                    loadCollaborationData(context, entityId, merchantable);
                                } else {
                                    loadCollaborationData(context, collaboWithEntityID, merchantable);
                                }

                                //Dismiss dialog
                                dialog.dismiss();
                                break;
                            //Add photo/graphic art on writing
                            case 1:
                                //Set listener
                                onCollaborationListener.collaborationOnWriting(entityId, entityType);
                                //Dismiss dialog
                                dialog.dismiss();
                                break;
                        }
                    }
                });

            }
        });
    }


    public static void performContentTypeSpecificOperations(final FragmentActivity context, final FeedModel feedData, TextView textCollabCount, View containerCollabCount, TextView buttonCollaborate, TextView textCreatorName, boolean showCountAsText, boolean shouldToggleVisibility, @Nullable View view) {

        boolean isCreatorCollaborator = feedData.getUUID().equals(feedData.getCollabWithUUID());

        // initialize text
        String text = getCreatorText(context
                , feedData.getContentType()
                , feedData.isAvailableForCollab()
                , feedData.getCreatorName()
                , feedData.getCollabWithName()
                , isCreatorCollaborator);

        // set collaboration count text

        if (feedData.getCollabCount() != 0) {

            if (showCountAsText) {
                // showing count as text
                textCollabCount.setText(getCollabCountText(context, feedData.getCollabCount(), feedData.getContentType(), feedData.isAvailableForCollab()));
            } else {   // showing count as number
                textCollabCount.setText(String.valueOf(feedData.getCollabCount()));
            }

            // check if some view's visibility has to be toggled
            // true for me and main feed
            // the view concerned is the line separator
            if (shouldToggleVisibility) {
                view.setVisibility(View.VISIBLE);
            }

            containerCollabCount.setVisibility(View.VISIBLE);
        } else {
            containerCollabCount.setVisibility(View.GONE);

            // check if some view's visibility has to be toggled
            // true for me and main feed
            // the view concerned is the line separator
            if (shouldToggleVisibility) {
                view.setVisibility(View.GONE);
            }
        }

        //Check for content type
        switch (feedData.getContentType()) {
            case CONTENT_TYPE_CAPTURE:

                if (feedData.isAvailableForCollab()) {
                    // for stand alone capture
                    buttonCollaborate.setVisibility(View.VISIBLE);
                    //write click functionality on capture
                    // writeOnClick(buttonCollaborate, feedData.getCaptureID(), feedData.getContentImage(), feedData.isMerchantable());
                    buttonCollaborate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // onCollaborationListener.collaborationOnGraphic();

                            if (new SharedPreferenceHelper(context).isCaptureIconTooltipFirstTime()) {

                                getShortOnClickDialog(context, feedData.getCaptureID()
                                        , feedData.getContentImage(), feedData.isMerchantable()
                                        , feedData.getImgWidth(), feedData.getImgHeight());
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString(EXTRA_CAPTURE_ID, feedData.getCaptureID());
                                bundle.putString(EXTRA_CAPTURE_URL, feedData.getContentImage());
                                bundle.putBoolean(EXTRA_MERCHANTABLE, feedData.isMerchantable());
                                bundle.putInt(EXTRA_IMAGE_WIDTH, feedData.getImgWidth());
                                bundle.putInt(EXTRA_IMAGE_HEIGHT, feedData.getImgHeight());
                                bundle.putString(SHORT_EXTRA_CALLED_FROM, SHORT_EXTRA_CALLED_FROM_COLLABORATION_SHORT);
                                Intent intent = new Intent(context, ShortActivity.class);
                                intent.putExtra(EXTRA_DATA, bundle);
                                context.startActivity(intent);
                            }
                            //Log Firebase event
                            // setAnalytics(FIREBASE_EVENT_WRITE_CLICKED);
                        }
                    });
                    // get text indexes
                    int creatorStartPos = text.indexOf(feedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + feedData.getCreatorName().length();
                    int collabWithStartPos = -1;
                    int collabWithEndPos = -1;

                    // get clickable text;
                    initializeSpannableString(context, textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, feedData.getUUID(), feedData.getCollabWithUUID(), isCreatorCollaborator);
                } else {

                    // showing collaborate button
                    buttonCollaborate.setVisibility(View.VISIBLE);

                    collabOnCollab(buttonCollaborate, context, feedData.getEntityID(), feedData.isMerchantable(), feedData.getContentType(), feedData.getCollaboWithEntityID());

                    // get text indexes
                    int creatorStartPos = text.indexOf(feedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + feedData.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(feedData.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + feedData.getCollabWithName().length() + 2; // +2 for 's

                    // get clickable text
                    initializeSpannableString(context, textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, feedData.getUUID(), feedData.getCollabWithUUID(), isCreatorCollaborator);
                }

                break;

            case CONTENT_TYPE_SHORT:

                // check if available for collaboration
                if (feedData.isAvailableForCollab()) {

                    // for stand alone short

                    buttonCollaborate.setVisibility(View.VISIBLE);
                    // set text
                    //buttonCollaborate.setText("Capture");

                    // capture click functionality on short
                    //captureOnClick(buttonCollaborate);
                    buttonCollaborate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (new SharedPreferenceHelper(context).isWriteIconTooltipFirstTime()) {
                                // open dialog
                                getCaptureOnClickDialog(context, feedData.getEntityID(), feedData.getContentType());
                            } else {
                                onCollaborationListener.collaborationOnWriting(feedData.getEntityID(), feedData.getContentType());
                            }
                            //Log Firebase event
                            // setAnalytics(FIREBASE_EVENT_CAPTURE_CLICKED);

                        }
                    });

                    //String text = mFeedData.getCreatorName() + " wrote a short ";

                    // get text indexes
                    int creatorStartPos = text.indexOf(feedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + feedData.getCreatorName().length();
                    int collabWithStartPos = -1; // since no collabwith
                    int collabWithEndPos = -1; // since no collabwith

                    initializeSpannableString(context, textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, feedData.getUUID(), feedData.getCollabWithUUID(), isCreatorCollaborator);


                } else {
                    // showing collaborate button
                    buttonCollaborate.setVisibility(View.VISIBLE);


                    collabOnCollab(buttonCollaborate, context, feedData.getEntityID(), feedData.isMerchantable(), feedData.getContentType(), feedData.getCollaboWithEntityID());

                    //String text = mFeedData.getCreatorName() + " wrote a short on " + mFeedData.getCollabWithName() + "'s capture";

                    // get text indexes
                    int creatorStartPos = text.indexOf(feedData.getCreatorName());
                    int creatorEndPos = creatorStartPos + feedData.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(feedData.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + feedData.getCollabWithName().length() + 2; // +2 to incorporate 's

                    // get clickable text
                    initializeSpannableString(context, textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, feedData.getUUID(), feedData.getCollabWithUUID(), isCreatorCollaborator);

                }

                break;
            default:
        }

    }


    /**
     * Method to show intro dialog when user collaborated by clicking on capture
     *
     * @param captureID    capture ID
     * @param captureURL   capture URl
     * @param merchantable merchantable true or false
     */
    private static void getShortOnClickDialog(final FragmentActivity context, final String captureID
            , final String captureURL, final boolean merchantable, final int imageWidth, final int imageHeight) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_generic, false)
                .positiveText(context.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open short functionality

                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_CAPTURE_ID, captureID);
                        bundle.putString(EXTRA_CAPTURE_URL, captureURL);
                        bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);
                        bundle.putInt(EXTRA_IMAGE_WIDTH, imageWidth);
                        bundle.putInt(EXTRA_IMAGE_HEIGHT, imageHeight);
                        bundle.putString(SHORT_EXTRA_CALLED_FROM, SHORT_EXTRA_CALLED_FROM_COLLABORATION_SHORT);
                        Intent intent = new Intent(context, ShortActivity.class);
                        intent.putExtra(EXTRA_DATA, bundle);
                        context.startActivity(intent);

                        dialog.dismiss();
                        //update status
                        new SharedPreferenceHelper(context).updateCaptureIconToolTipStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_collab_intro));
        //Set title text
        textTitle.setText(context.getString(R.string.title_dialog_collab_short));
        //Set description text
        textDesc.setText(context.getString(R.string.text_dialog_collab_short));
    }


    /**
     * Method to show intro dialog when user collaborated by clicking on capture
     */
    private static void getCaptureOnClickDialog(final FragmentActivity context, final String entityID, final String entityType) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_generic, false)
                .positiveText(context.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open capture functionality
                        onCollaborationListener.collaborationOnWriting(entityID, entityType);

                        dialog.dismiss();
                        //update status
                        new SharedPreferenceHelper(context).updateWriteIconToolTipStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.img_collab_intro));
        //Set title text
        textTitle.setText(context.getString(R.string.title_dialog_collab_capture));
        //Set description text
        textDesc.setText(context.getString(R.string.text_dialog_collab_capture));
    }

    /**
     * Method to retrieve capture data from the server.
     *
     * @param context      FragmentActivity context.
     * @param entityID     Entity ID of content.
     * @param merchantable Merchantable status.
     */
    private static void loadCollaborationData(final FragmentActivity context, String entityID, final boolean merchantable) {
        final ProgressBar progressBar = new ProgressBar(context);
        Rx2AndroidNetworking.get(BuildConfig.URL + "/entity-manage/load-captureid")
                .addQueryParameter("entityid", entityID)
                .build()
                .getJSONObjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        try {
                            JSONObject responseObject = jsonObject.getJSONObject("data");
                            //Retrieve data from server response
                            Bundle bundle = new Bundle();
                            bundle.putString(EXTRA_CAPTURE_ID, responseObject.getString("capid"));
                            bundle.putString(EXTRA_CAPTURE_URL, responseObject.getString("entityurl"));
                            bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);

                            //if image width and image height is null
                            if (responseObject.isNull("img_width") || responseObject.isNull("img_height")) {
                                bundle.putInt(EXTRA_IMAGE_WIDTH, 1);
                                bundle.putInt(EXTRA_IMAGE_HEIGHT, 1);
                            } else {
                                bundle.putInt(EXTRA_IMAGE_WIDTH, responseObject.getInt("img_width"));
                                bundle.putInt(EXTRA_IMAGE_HEIGHT, responseObject.getInt("img_height"));
                            }

                            bundle.putString(SHORT_EXTRA_CALLED_FROM, SHORT_EXTRA_CALLED_FROM_COLLABORATION_SHORT);
                            Intent intent = new Intent(context, ShortActivity.class);
                            intent.putExtra(EXTRA_DATA, bundle);
                            context.startActivity(intent);

                        } catch (JSONException e) {
                            //Hide progress view
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            Crashlytics.setString("className", "FeedHelper");
                            ViewHelper.getToast(context, context.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        Crashlytics.setString("className", "FeedHelper");
                        ViewHelper.getToast(context, context.getString(R.string.error_msg_server));
                        //Hide progress view
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onComplete() {
                        //Hide progress view
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    /**
     * Method to set Margins for grid view items
     *
     * @param context  context
     * @param position position of the item
     * @param image    Image View
     */
    public static void setGridItemMargins(FragmentActivity context, int position, ImageView image) {
        FrameLayout.LayoutParams params = new
                FrameLayout
                        .LayoutParams(image
                .getLayoutParams());

        int px = convertToPx(context, 1);

        if (position % 2 == 0) {
            params.setMargins(0, px, px, px);
        } else {
            params.setMargins(px, px, 0, px);
        }

        image.setLayoutParams(params);
    }


    /**
     * Sets the caption and processes the hashtags and their click actions
     *
     * @param context
     * @param data
     * @param textView
     */
    public static void initCaption(FragmentActivity context, FeedModel data, TextView textView) {
        if (data.getCaption() != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(data.getCaption());

            //set profile mentions
            setProfileMentionsForViewing(data.getCaption(), context, textView);

            // set hash tags
            FeedHelper feedHelper = new FeedHelper();
            feedHelper.setHashTags(textView, context, R.color.blue_dark, -1);

        } else {
            textView.setVisibility(View.GONE);
        }
    }


    public static void initSocialActionsCount(FragmentActivity context,
                                              FeedModel data,
                                              LinearLayout hatsoffContainer,
                                              TextView hatsoffCount,
                                              LinearLayout commentContainer,
                                              TextView commentCount,
                                              TextView dotSeperator) {

        updateDotSeperatorVisibility(data, dotSeperator);

        //Check for hats of count
        if (data.getHatsOffCount() > 0) {
            //Set hatsOff count
            hatsoffContainer.setVisibility(View.VISIBLE);
            hatsoffCount.setText(String.valueOf(data.getHatsOffCount()));
        } else {
            //Hide hatsOff count textView
            hatsoffContainer.setVisibility(View.GONE);
        }

        //Check for comment count
        if (data.getCommentCount() > 0) {
            commentContainer.setVisibility(View.VISIBLE);
            //Set comment count
            commentCount.setText(String.valueOf(data.getCommentCount()));
        } else {
            commentContainer.setVisibility(View.GONE);
        }

    }


    /**
     * Updates the visibility of the dot separator
     * based on the values of hatsOff and comment count
     */
    public static void updateDotSeperatorVisibility(FeedModel data, TextView dotSeperator) {
        long hatsoffCount = data.getHatsOffCount();
        long commentCount = data.getCommentCount();

        // if one or both the counts are zero remove the dot
        if ((hatsoffCount == 0 && commentCount == 0)
                || (hatsoffCount != 0 && commentCount == 0)
                || (hatsoffCount == 0 && commentCount != 0)) {
            dotSeperator.setVisibility(View.GONE);
        }
        // both are non-zero so show the dot
        else if (hatsoffCount != 0 && commentCount != 0) {
            dotSeperator.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Updates the visibility of the dot separator
     * based on the values of collab count
     */
    public static void updateDownvoteAndSeperatorVisibility(FeedModel data, TextView dotSeperator, ImageView imageDownvote) {
        long collabCount = data.getCollabCount();
        boolean canDownvote = data.isEligibleForDownvote();

        // if count is zero remove the dot
        if ((collabCount == 0 && !canDownvote)
                || (collabCount != 0 && !canDownvote)
                || (collabCount == 0 && canDownvote)) {
            dotSeperator.setVisibility(View.GONE);
        }
        // non-zero so show the dot
        else if (collabCount != 0 && canDownvote) {
            dotSeperator.setVisibility(View.VISIBLE);
        }

        // toggle downvote visibility
        if (canDownvote) {
            imageDownvote.setVisibility(View.VISIBLE);
        } else {
            imageDownvote.setVisibility(View.GONE);
        }
    }


    /**
     * Method to update follow status for each item occurrence of the followed user
     *
     * @param exploreData
     * @param list
     */
    public static void updateFollowForAll(FeedModel exploreData, List<FeedModel> list) {
        for (FeedModel f : list) {
            if (f.getUUID() != null && f.getUUID().equals(exploreData.getUUID())) {
                f.setFollowStatus(exploreData.getFollowStatus());
            }
        }
    }

    /**
     * Initializes timestamp of the post
     *
     * @param viewTimeStamp view where timestamp is to be updated
     * @param data          Data
     */
    public static void updatePostTimestamp(TextView viewTimeStamp, FeedModel data) {
        // get curremt date
        Date date = new Date();
        // convert it to ISO
        String currentISO = TimeUtils.getISO8601StringForDate(date);
        // get individual units
        List<String> currentDateList = getCustomTime(currentISO);
        // parsing server date
        List<String> dateList = getCustomTime(data.getPostTimeStamp());
        String timeStamp = "";

        // if date and month are same include 'today' in the timestamp
        if (currentDateList.get(0).equals(dateList.get(0)) && currentDateList.get(1).equals(dateList.get(1))) {
            timeStamp = "Today" + " at " + dateList.get(3);
        } else {
            timeStamp = dateList.get(1) + " " + dateList.get(0) + " at " + dateList.get(3);
        }

        // set timestamp
        viewTimeStamp.setText(timeStamp);
    }

}
