package com.thetestament.cread.helpers;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
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

public class ProfileMentionsHelper {

    public static final String BUCKET = "people-network";


    public static String convertToMentionsFormat(MentionsEditText mentionsEditText) {

        String originalText = mentionsEditText.getText().toString().trim();

        List<MentionSpan> mentionSpans = mentionsEditText.getMentionsText().getMentionSpans();

        // if mentions exist format them
        if (mentionSpans.size() != 0) {
            for (MentionSpan mentionSpan : mentionSpans) {
                // replacing mention with the custom mention format in the string
                originalText = originalText.replaceFirst(mentionSpan.getDisplayString(), setMentionFormat(mentionSpan));
            }
        }

        return originalText;
    }


    private static String setMentionFormat(MentionSpan mentionSpan) {
        String uuid = ((PersonMentionModel) mentionSpan.getMention()).getUserUUID();
        String name = mentionSpan.getDisplayString();

        // format @[(u:ac879s-ascui8-2489w+n:Avneesh  Khanna)]
        return "@[(u:" + uuid + "+n:" + name + ")]";

    }

    public static void setProfileMentionsForViewing(String mentionText, FragmentActivity context, TextView textView) {

        Pattern mentionPattern = Pattern.compile
                ("\\@\\[\\(u:\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}\\+n:([^\\x00-\\x7F]|\\w|\\s|\\n)+\\)\\]",
                        Pattern.CASE_INSENSITIVE);

        Pattern namePattern = Pattern.compile
                ("\\+n:([^\\x00-\\x7F]|\\w|\\s|\\n)+",
                        Pattern.CASE_INSENSITIVE);

        Pattern uuidPattern = Pattern.compile
                ("u:\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}",
                        Pattern.CASE_INSENSITIVE);

        Matcher matcher = mentionPattern.matcher(mentionText);

        ArrayList<Integer> startIndi = new ArrayList<>();
        ArrayList<Integer> endIndi = new ArrayList<>();
        ArrayList<String> uuids = new ArrayList<>();


        while (matcher.find()) {

            String matchedText = matcher.group();

            String improperName = null;

            Matcher nameMatcher = namePattern.matcher(matchedText);

            if (nameMatcher.find()) {
                improperName = nameMatcher.group();
            }

            String properName = improperName.split(":")[1];

            String tempName = "@&" + properName;


            mentionText = mentionText.replaceFirst(Pattern.quote(matchedText), tempName);

            int sIndex = mentionText.indexOf(tempName);
            startIndi.add(sIndex);
            endIndi.add(sIndex + tempName.length());

            String improperUUID = null;

            Matcher uuidMatcher = uuidPattern.matcher(matchedText);

            if (uuidMatcher.find()) {
                improperUUID = uuidMatcher.group();
            }

            String properUUID = improperUUID.split(":")[1];

            uuids.add(properUUID);

            mentionText = mentionText.replaceAll("@&", "");

        }


        SpannableString spannableString = new SpannableString(mentionText);

        int stInda = 0;
        int enInda = -2;
        int d = -2;

        for (int n = 0; n < uuids.size(); n++) {
            int stIndSubFactor = stInda + n * d;
            int enIndSubFactor = enInda + n * d;

            int startPos = startIndi.get(n) + /*stIndSubFactor*/0;
            int endPos = endIndi.get(n) + /*enIndSubFactor*/(-2);

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

    public static final WordTokenizerConfig tokenizerConfig = new WordTokenizerConfig
            .Builder()
            .setThreshold(Integer.MAX_VALUE)
            .build();


    public static MentionSpanConfig getMentionSpanConfig(FragmentActivity context) {
        return new MentionSpanConfig
                .Builder()
                .setMentionTextColor
                        (ContextCompat.getColor(context, R.color.blue_dark))
                .build();
    }


}
