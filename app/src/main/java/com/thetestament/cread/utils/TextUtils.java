package com.thetestament.cread.utils;

/**
 * Created by avnee on 14-06-2018.
 */

import android.text.SpannableString;

/**
 * Class to provide utility methods for texts and strings
 * */
public class TextUtils {

    /**
     * Applies the spannable properties to a String and returns the SpannableString
     * */
    public static SpannableString getSpannedString(String source,
                                                   Object what,
                                                   int start,
                                                   int end,
                                                   int flags){

        SpannableString sstring = new SpannableString(source);
        sstring.setSpan(what, start, end, flags);
        return sstring;
    }

}
