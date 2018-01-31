package com.thetestament.cread.helpers;

import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.util.Linkify;
import android.widget.TextView;

import com.linkedin.android.spyglass.mentions.MentionSpan;
import com.linkedin.android.spyglass.mentions.MentionSpanConfig;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig;
import com.linkedin.android.spyglass.ui.MentionsEditText;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.PersonMentionAdapter;
import com.thetestament.cread.models.PersonMentionModel;

import java.util.List;
import java.util.regex.Pattern;

import static com.thetestament.cread.utils.Constant.URI_HASH_TAG_ACTIVITY;

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

    public static void setProfileMentions(TextView textView, FragmentActivity context) {
        textView.setLinkTextColor(ContextCompat.getColor(context, R.color.blue_dark));
        //Pattern to find if there's a hash tag in the message
        //i.e. any word starting with a # and containing letter or numbers or _
        Pattern tagMatcher = Pattern.compile("\\+n:([^\\x00-\\x7F]|\\w|\\s|\\n)+", Pattern.CASE_INSENSITIVE);
        // attach linkify to text view for click action of hash tags
        Linkify.addLinks(textView, tagMatcher, URI_HASH_TAG_ACTIVITY);
        // to remove underlines from the hashtag links
        new FeedHelper().stripUnderlines(textView);
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
