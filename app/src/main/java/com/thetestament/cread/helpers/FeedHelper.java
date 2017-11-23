package com.thetestament.cread.helpers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.activities.ProfileActivity;

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;


/**
 * Helper class for feeds
 */
public class FeedHelper {


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




}
