package com.thetestament.cread.helpers;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.linkedin.android.spyglass.mentions.MentionSpan;
import com.linkedin.android.spyglass.mentions.MentionSpanConfig;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig;
import com.linkedin.android.spyglass.ui.MentionsEditText;
import com.thetestament.cread.R;
import com.thetestament.cread.models.PersonMentionModel;
import com.thetestament.cread.widgets.ProfileClickableSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Blog Reference: https://engineering.linkedin.com/android/open-sourcing-spyglass-flexible-library-implementing-mentions-android
 * <p>
 * Github : https://github.com/linkedin/Spyglass
 * <p>
 * Sample App : https://github.com/linkedin/Spyglass/blob/master/spyglass-sample/README.md
 */
public class ProfileMentionsHelper {
    // static string to be passed to Linkedin library
    public static final String BUCKET = "people-network";

    // initialize word tokenizer (linkedin class)
    public static final WordTokenizerConfig tokenizerConfig = new WordTokenizerConfig
            .Builder()
            // Number of characters required in a word before returning a mention suggestion starting with the word
            .setThreshold(Integer.MAX_VALUE)
            .build();

    // custom config for mention color
    public static MentionSpanConfig getMentionSpanConfig(FragmentActivity context) {
        return new MentionSpanConfig
                .Builder()
                .setMentionTextColor
                        (ContextCompat.getColor(context, R.color.blue_dark))
                .build();
    }


    // Regex for mention pattern
    private static Pattern mentionPattern = Pattern.compile
            ("\\@\\[\\(u:[\\w\\-]+\\+n:([^\\x00-\\x7F]|\\w|\\s|\\n)+\\)\\]",
                    Pattern.CASE_INSENSITIVE);

    // Regex for name part of the pattern
    private static Pattern namePattern = Pattern.compile
            ("\\+n:([^\\x00-\\x7F]|\\w|\\s|\\n)+",
                    Pattern.CASE_INSENSITIVE);

    // Regex for uuid part of the pattern
    private static Pattern uuidPattern = Pattern.compile
            ("u:[\\w\\-]+",
                    Pattern.CASE_INSENSITIVE);


    /**
     * Retrieves the mentions text from MentionsEditText and converts it into our custom format
     * @param mentionsEditText
     * @return Custom Mentions formatted string
     */
    public static String convertToMentionsFormat(MentionsEditText mentionsEditText) {

        String originalText = mentionsEditText.getText().toString().trim();

        StringBuilder stringBuilder = new StringBuilder(originalText);

        List<MentionSpan> mentionSpans = mentionsEditText.getMentionsText().getMentionSpans();

        // if mentions exist then
        // extract each mention span from the mentions text and replace it with our custom format
        if (mentionSpans.size() != 0) {
            for (int i = mentionSpans.size() - 1; i >= 0; i--) {

                int start = mentionsEditText.getMentionsText().getSpanStart(mentionSpans.get(i));
                int end = mentionsEditText.getMentionsText().getSpanEnd(mentionSpans.get(i));

                // get custom mention formatted string from a mention
                String customMention = setMentionFormat(mentionSpans.get(i));

                // replacing mention with the custom mention format in the string
                stringBuilder.replace(start, end, customMention);

            }
        }

        return stringBuilder.toString();
    }

    /**
     * Converts a mention span into our custom mention formatted string
     * @param mentionSpan
     * @return
     */
    private static String setMentionFormat(MentionSpan mentionSpan) {
        String uuid = ((PersonMentionModel) mentionSpan.getMention()).getUserUUID();
        String name = mentionSpan.getDisplayString();

        // format @[(u:ac879s-ascui8-2489w+n:Avneesh  Khanna)]
        return "@[(u:" + uuid + "+n:" + name + ")]";

    }

    /**
     * Parses the entire text by replacing the custom formatted mentions string with clickable spans and sets the resulting string in the text view
     *
     * Used when Profile Mention has to be set for viewing purpose of user
     * @param mentionText
     * @param context
     * @param textView
     */
    public static void setProfileMentionsForViewing(String mentionText, FragmentActivity context, TextView textView) {


        if (!TextUtils.isEmpty(mentionText)) {
            Matcher matcher = mentionPattern.matcher(mentionText);

            ArrayList<Integer> startIndi = new ArrayList<>();
            ArrayList<Integer> endIndi = new ArrayList<>();
            ArrayList<String> uuids = new ArrayList<>();


            while (matcher.find()) {

                String matchedText = matcher.group();

                // process name part
                String improperName = null;
                Matcher nameMatcher = namePattern.matcher(matchedText);

                if (nameMatcher.find()) {
                    improperName = nameMatcher.group();
                }
                String properName = improperName.split(":")[1];
                String tempName = "@&" + properName;

                // replace mention part with temp name
                mentionText = mentionText.replaceFirst(Pattern.quote(matchedText), tempName);

                // get indexes
                int sIndex = mentionText.indexOf(tempName);
                startIndi.add(sIndex);
                endIndi.add(sIndex + tempName.length());

                // process uuid part
                String improperUUID = null;
                Matcher uuidMatcher = uuidPattern.matcher(matchedText);
                if (uuidMatcher.find()) {
                    improperUUID = uuidMatcher.group();
                }
                String properUUID = improperUUID.split(":")[1];
                uuids.add(properUUID);

                // replace all @& to get the text
                mentionText = mentionText.replaceAll("@&", "");

            }

            // Now convert the mentions into spans
            SpannableString spannableString = new SpannableString(mentionText);

            for (int n = 0; n < uuids.size(); n++) {

                int startPos = startIndi.get(n);
                int endPos = endIndi.get(n) + (-2);

                spannableString.setSpan(new ProfileClickableSpan(context
                                , uuids.get(n))
                        , (startPos)
                        , (endPos)
                        , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spannableString);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setHighlightColor(Color.TRANSPARENT);
        }


    }


    /**
     * Parses the entire text by replacing the custom formatted mentions string with the Linkedin Library Mentionables and sets the resulting text in Mentions edit text
     *
     * Used when Profile Mention has to be set for editing purpose of user
     * @param context
     * @param mentionText
     * @param mentionsEditText
     */
    public static void setProfileMentionsForEditing(FragmentActivity context, String mentionText, MentionsEditText mentionsEditText) {


        if (!TextUtils.isEmpty(mentionText)) {
            //set config
            mentionsEditText.setMentionSpanConfig(getMentionSpanConfig(context));

            ArrayList<Integer> startIndi = new ArrayList<>();
            ArrayList<Integer> endIndi = new ArrayList<>();
            ArrayList<PersonMentionModel> mentions = new ArrayList<>();

            Matcher matcher = mentionPattern.matcher(mentionText);

            while (matcher.find()) {

                String matchedText = matcher.group();

                // process name part
                String improperName = null;
                Matcher nameMatcher = namePattern.matcher(matchedText);

                if (nameMatcher.find()) {
                    improperName = nameMatcher.group();
                }
                String properName = improperName.split(":")[1];
                String tempName = "@&" + properName;

                // replace mention part with temp name
                mentionText = mentionText.replaceFirst(Pattern.quote(matchedText), tempName);

                // get indexes
                int sIndex = mentionText.indexOf(tempName);
                startIndi.add(sIndex);
                endIndi.add(sIndex + tempName.length());

                // process uuid part
                String improperUUID = null;
                Matcher uuidMatcher = uuidPattern.matcher(matchedText);
                if (uuidMatcher.find()) {
                    improperUUID = uuidMatcher.group();
                }
                String properUUID = improperUUID.split(":")[1];


                // init mentionable
                PersonMentionModel person = new PersonMentionModel();
                person.setmName(properName);
                person.setUserUUID(properUUID);
                mentions.add(person);

                // replace all @& to get the text
                mentionText = mentionText.replaceAll("@&", "");

            }

            mentionsEditText.setText(mentionText);
            // Now convert the mentions into Linkedin library format
            for (int n = 0; n < mentions.size(); n++) {

                int startPos = startIndi.get(n) + 0;
                int endPos = endIndi.get(n) + (-2);

                mentionsEditText.getMentionsText().setSpan(
                        new MentionSpan(mentions.get(n))
                        , startPos
                        , endPos
                        , 0);
            }

            // set cursor next to the last char
            mentionsEditText.setSelection(mentionsEditText.getText().length());
        }
    }

}
