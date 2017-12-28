package com.thetestament.cread.helpers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.adapters.ShareDialogAdapter;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnServerRequestedListener;
import com.thetestament.cread.listeners.listener.OnShareDialogItemClickedListener;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.ListItemsDialogModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.disposables.CompositeDisposable;

import static com.thetestament.cread.helpers.NetworkHelper.getDeepLinkObservable;
import static com.thetestament.cread.helpers.NetworkHelper.requestServer;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.URI_HASH_TAG_ACTIVITY;


/**
 * Helper class for feeds
 */
public class FeedHelper {

    OnShareDialogItemClickedListener onShareDialogItemClickedListener;


    /**
     *   Method to create text with 2 clicks for opening user profiles
     * @param context
     * @param text  Text to create as spannable
     * @param creatorStartPos  creator name starting index in text
     * @param creatorEndPos   creator name ending index in text
     * @param collabWithStartPos collaborate with user name start index in text
     * @param collabWithEndPos  collaborate with user name end index in text
     * @param creatorUUID   creator userid
     * @param collabWithUUID collaborate with uuid
     */
    public static void initializeSpannableString(final FragmentActivity context, TextView textView , boolean hasTwoClicks, String text, int creatorStartPos, int creatorEndPos, int collabWithStartPos, int collabWithEndPos, final String creatorUUID, final String collabWithUUID)
    {
        SpannableString ss = new SpannableString(text);
        ClickableSpan collaboratorSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                context.startActivity(new Intent(context, ProfileActivity.class).putExtra(EXTRA_PROFILE_UUID, creatorUUID));
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

        if(hasTwoClicks)
        {
            ClickableSpan collaboratedWithSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    context.startActivity(new Intent(context, ProfileActivity.class).putExtra(EXTRA_PROFILE_UUID, collabWithUUID));
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
     * @param context
     * @param contentType
     * @param isAvailableforCollab
     * @param creatorName
     * @param collaboratorName
     * @return
     */
    public static String getCreatorText(FragmentActivity context, String contentType, boolean isAvailableforCollab, String creatorName, String collaboratorName )
    {
        String text = null;

        switch (contentType)
        {
            case CONTENT_TYPE_CAPTURE :

                text = isAvailableforCollab ? creatorName + " " + context.getString(R.string.creator_text_standalone_capture)
                        : (creatorName + " " + context.getString(R.string.creator_text_collab_capture_part1) + " "
                        + collaboratorName + "'s " + context.getString(R.string.creator_text_collab_capture_part2));

                break;

            case CONTENT_TYPE_SHORT :

                text = isAvailableforCollab ? creatorName + " " + context.getString(R.string.creator_text_standalone_short)
                        : (creatorName + " " + context.getString(R.string.creator_text_collab_short_part1) + " "
                        + collaboratorName + "'s " + context.getString(R.string.creator_text_collab_short_part2));
                break;
        }

        return text;
    }

    /**
     * Method to get the collaboration count text according to type and count
     * @param context context
     * @param count collaboration count
     * @param contentType capture or short
     * @return
     */
    public static String getCollabCountText(FragmentActivity context, long count, String contentType, boolean isAvailableForCollab)
    {
        String text = null;

        switch (contentType)
        {
            case CONTENT_TYPE_CAPTURE :

                if (!isAvailableForCollab) {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_short_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_short_count_text_single);
                } else {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_single);
                }

                break;

            case CONTENT_TYPE_SHORT :

                if (!isAvailableForCollab) {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_capture_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_capture_count_text_single);
                } else {
                    text = isMultiple(count) ? String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_multiple) : String.valueOf(count) + " " + context.getString(R.string.collab_general_count_text_single);
                }

                break;
        }

        return text;
    }


    /**
     *
     * @param count the value of the count which to be checked
     * @return true if count is more than 1 else false
     *
     */

    public static boolean isMultiple(long count)
    {
        return count != 1;
    }

    /**
     * Method to share deep link via Intent
     *
     * @param context context
     * @param link    link to share
     */
    public static void shareDeepLink(FragmentActivity context, String link) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, link);
        context.startActivity(Intent.createChooser(intent, "Share Link"));
    }

    /**
     * Method to get the deep link from server
     *
     * @param context
     * @param compositeDisposable
     * @param rootView
     * @param uuid
     * @param authkey
     * @param entityID
     * @param entityUrl
     */
    public static void generateDeepLink(final FragmentActivity context, CompositeDisposable compositeDisposable, final View rootView, String uuid, String authkey, String entityID, String entityUrl, String creatorName) {
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(context.getString(R.string.generating_title))
                .content(context.getString(R.string.waiting_msg))
                .progress(true, 0)
                .show();

        requestServer(compositeDisposable, getDeepLinkObservable(BuildConfig.URL + "/entity-share-link/generate-dynamic-link",
                uuid,
                authkey,
                entityID,
                entityUrl,
                creatorName),
                context,
                new OnServerRequestedListener<JSONObject>() {
                    @Override
                    public void onDeviceOffline() {

                        dialog.dismiss();
                        ViewHelper.getSnackBar(rootView, context.getString(R.string.error_msg_no_connection));

                    }

                    @Override
                    public void onNextCalled(JSONObject jsonObject) {

                        dialog.dismiss();

                        try {
                            //Token status is not valid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {

                                ViewHelper.getSnackBar(rootView, context.getString(R.string.error_msg_invalid_token));
                            }
                            //Token is valid
                            else {
                                JSONObject mainData = jsonObject.getJSONObject("data");
                                String deepLink = mainData.getString("link");

                                // share link
                                shareDeepLink(context, deepLink);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            ViewHelper.getSnackBar(rootView, context.getString(R.string.error_msg_internal));
                        }
                    }

                    @Override
                    public void onErrorCalled(Throwable e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        dialog.dismiss();
                        ViewHelper.getSnackBar(rootView, context.getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onCompleteCalled() {
                        // do nothing
                    }
                });
    }

    /**
     * Parses the hash tags and creates them as links
     *
     * @param textView text view which contains the text which from which hash tags are parsed
     * @param context  context
     */
    public void setHashTags(TextView textView, FragmentActivity context) {
        textView.setLinkTextColor(ContextCompat.getColor(context, R.color.blue_dark));
        //Pattern to find if there's a hash tag in the message
        //i.e. any word starting with a # and containing letter or numbers or _
        Pattern tagMatcher = Pattern.compile("\\#\\w+", Pattern.CASE_INSENSITIVE);
        // attach linkify to text view for click action of hash tags
        Linkify.addLinks(textView, tagMatcher, URI_HASH_TAG_ACTIVITY);
        // to remove underlines from the hashtag links
        stripUnderlines(textView);
    }

    /**
     * Removes the underline of the spannable texts
     */
    private void stripUnderlines(TextView textView) {
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
        feedData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
        feedData.setCommentCount(dataObj.getLong("commentcount"));
        feedData.setContentImage(dataObj.getString("entityurl"));
        feedData.setCollabCount(dataObj.getLong("collabcount"));
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


        int[] drawables = {R.drawable.ic_image_24, R.drawable.ic_link_24};


        return initializeItemsDialog(titles, bylines, drawables);
    }


    public static void collabOnOneForm(View view, final FragmentActivity context) {
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

                        switch (index)

                        {
                            case 0:
                                // TODO listener
                                break;

                            case 1:
                                // TODO listener
                                break;
                        }
                    }
                });


            }
        });
    }



}
