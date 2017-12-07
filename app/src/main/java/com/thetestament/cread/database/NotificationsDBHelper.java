package com.thetestament.cread.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry;


//Helper class for  notification system DB


public class NotificationsDBHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Cread_Notifications.db";

    //Create table syntax
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NotificationDBEntry.TABLE_NAME + " (" +
                    NotificationDBEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    NotificationDBEntry.COLUMN_NAME_USER_ID + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_ACTOR_USERID + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_ENTITY_ID + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_USER_IMAGE + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_ENTITY_IMAGE + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_CATEGORY + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_UNREAD + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_SEEN + TEXT_TYPE + COMMA_SEP +
                    NotificationDBEntry.COLUMN_NAME_OTHER_COLLABORATOR + INTEGER_TYPE + " DEFAULT 0" +
                    " )";

    private static final String SQL_ALTER_QUERY_V2_Q1 =
            "ALTER TABLE " + NotificationDBEntry.TABLE_NAME + " ADD COLUMN " +
                    NotificationDBEntry.COLUMN_NAME_ENTITY_IMAGE + TEXT_TYPE;

    private static final String SQL_ALTER_QUERY_V2_Q2 = "ALTER TABLE " +
            NotificationDBEntry.TABLE_NAME + " ADD COLUMN " +
            NotificationDBEntry.COLUMN_NAME_OTHER_COLLABORATOR + INTEGER_TYPE + " DEFAULT 0";



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

        db.execSQL(SQL_ALTER_QUERY_V2_Q1);
        db.execSQL(SQL_ALTER_QUERY_V2_Q2);

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
