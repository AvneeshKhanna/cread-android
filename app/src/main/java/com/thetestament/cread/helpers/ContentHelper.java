package com.thetestament.cread.helpers;

import com.thetestament.cread.models.FeedModel;

import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;

/**
 * Helper class which provides utility methods related to content deletion and editing.
 */

public class ContentHelper {

    /**
     * Method to launch required screen fro content editing.
     */
    public static void lauchContentEditingScreen(FeedModel data) {

        switch (data.getContentType()) {
            case CONTENT_TYPE_CAPTURE:
                //Stand alone capture
                if (data.isAvailableForCollab()) {

                }
                //Capture on short
                else {

                }
                break;
            case CONTENT_TYPE_SHORT:
                //Stand alone short
                if (data.isAvailableForCollab()) {

                }
                //Short on capture
                else {

                }
                break;
            default:
        }
    }
}
