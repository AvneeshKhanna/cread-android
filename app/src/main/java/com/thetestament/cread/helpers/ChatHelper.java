package com.thetestament.cread.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.activities.ChatDetailsActivity;

import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_WITH_US;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_DETAILS_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_ITEM_POSITION;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_USER_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_CHAT_UUID;

/**
 * Class to provide utility methods for chat related operation
 */

public class ChatHelper {


    /**
     * Method to open chat details screen with Cread kalakasr.
     *
     * @param context Content to use
     */
    public static void openChatWithCreadKalakaar(FragmentActivity context) {
        //Open ChatDetailsActivity with open
        Intent intent = new Intent(context, ChatDetailsActivity.class);
        //Set bundle data
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CHAT_UUID, BuildConfig.CREAD_KALAKAAR_UUID);
        bundle.putString(EXTRA_CHAT_USER_NAME, "Cread Kalakaar");
        bundle.putString(EXTRA_CHAT_ID, "");
        bundle.putInt(EXTRA_CHAT_ITEM_POSITION, 0);
        bundle.putString(EXTRA_CHAT_DETAILS_CALLED_FROM, EXTRA_CHAT_DETAILS_CALLED_FROM_CHAT_WITH_US);

        intent.putExtra(EXTRA_CHAT_DETAILS_DATA, bundle);
        context.startActivity(intent);
    }

}
