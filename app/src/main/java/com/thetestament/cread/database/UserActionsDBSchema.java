package com.thetestament.cread.database;

import android.provider.BaseColumns;

/**
 * Created by prakharchandna on 16/02/18.
 */

public class UserActionsDBSchema {

    // To prevent someone from accidentally instantiating this contract class,
    // give it an empty constructor.
    private UserActionsDBSchema() {
    }

    /* Inner class that defines the table contents */
    public static abstract class UserActionsDBEntry implements BaseColumns {

        public static final String TABLE_NAME = "userActions";

        public static final String COLUMN_NAME_ACTOR_ID = "actorid";
        public static final String COLUMN_NAME_ENTITY_ID = "entityid";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_ACTION_TYPE = "actiontype";

    }
}
