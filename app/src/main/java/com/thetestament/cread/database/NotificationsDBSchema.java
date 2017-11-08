package com.thetestament.cread.database;

import android.provider.BaseColumns;

/**
 * Class for Defining DBSchema for notifications
 */
public class NotificationsDBSchema {

    // To prevent someone from accidentally instantiating this contract class,
    // give it an empty constructor.
    public NotificationsDBSchema() {
    }

    /* Inner class that defines the table contents */
    public static abstract class NotificationDBEntry implements BaseColumns {
        public static final String TABLE_NAME = "Notifications";
        public static final String COLUMN_NAME_MESSAGE = "Message";
        public static final String COLUMN_NAME_CATEGORY = "Category";
        public static final String COLUMN_NAME_TIME = "Time";
        public static final String COLUMN_NAME_DATE = "Date";
        public static final String COLUMN_NAME_UNREAD = "Unread";
        public static final String COLUMN_NAME_SEEN = "Seen";
        public static final String COLUMN_NAME_USER_ID = "UserID";
        public static final String COLUMN_NAME_ENTITY_ID = "EntityID";
        public static final String COLUMN_NAME_ACTOR_USERID = "ActorUserID";
        public static final String COLUMN_NAME_USER_IMAGE = "UserImageURL";

    }
}
