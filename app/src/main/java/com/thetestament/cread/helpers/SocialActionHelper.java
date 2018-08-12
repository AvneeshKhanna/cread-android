package com.thetestament.cread.helpers;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.thetestament.cread.activities.CommentsActivity;

import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;

/**
 * A helper class to provide utility method for social actions
 */

public class SocialActionHelper {

    /**
     * Click functionality to open comment screen.
     *
     * @param view     View to be clicked.
     * @param context  Context to use.
     * @param entityID Entity ID of post
     */
    public static void navigateToComment(View view, final FragmentActivity context, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra(EXTRA_ENTITY_ID, entityID);
                context.startActivity(intent);
            }
        });
    }

    /**
     * Method to open ProfileActivity screen.
     *
     * @param view    View to be clicked.
     * @param context Context to use.
     * @param uuid    UUID of user whose profile to  be loaded.
     */
    public static void navigateToProfile(View view, final FragmentActivity context, final String uuid) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentHelper.openProfileActivity(context, uuid);
            }
        });
    }
}
