/*
package com.thetestament.cread.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import in.thetestament.marketrecruit.navigation.notificationpanel.database.NotificationsDBSchema.NotificationDBEntry;

*/
/**
 * Helper class for  notification system DB
 *//*

public class NotificationsDBHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";

    private static final String COMMA_SEP = ",";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "MarketRecruit_Notifications.db";

    //Create table syntax
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NotificationDBEntry.TABLE_NAME + " (" +
                    NotificationDBEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    NotificationDBEntry.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_JOB_ID + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_CAMPAIGN_ID + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_SHARE_ID + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_UNREAD + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_SEEN + TEXT_TYPE +
                    " )";

    //Drop table syntax
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NotificationDBEntry.TABLE_NAME;


    //Required Constructor
    public NotificationsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
*/
