package com.thetestament.cread.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry;

import org.reactivestreams.Subscriber;

import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.internal.operators.observable.ObservableFromCallable;

import static com.thetestament.cread.database.UserActionsDBSchema.*;


//Helper class for  notification system DB


public class NotificationsDBHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";

    public static final String COMMA_SEP = ",";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
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


    private static final String SQL_CREATE_USER_ACTIONS_TABLE =
            "CREATE TABLE " + UserActionsDBEntry.TABLE_NAME + " (" +
                    UserActionsDBEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    UserActionsDBEntry.COLUMN_NAME_ACTOR_ID + TEXT_TYPE + COMMA_SEP +
                    UserActionsDBEntry.COLUMN_NAME_ENTITY_ID + TEXT_TYPE + COMMA_SEP +
                    UserActionsDBEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    UserActionsDBEntry.COLUMN_NAME_ACTION_TYPE + TEXT_TYPE + COMMA_SEP +
                    " UNIQUE ( " + UserActionsDBEntry.COLUMN_NAME_ACTOR_ID + COMMA_SEP +
                    UserActionsDBEntry.COLUMN_NAME_ENTITY_ID + " ) ON CONFLICT REPLACE" +
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
        db.execSQL(SQL_CREATE_USER_ACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion == 1) {
            db.execSQL(SQL_ALTER_QUERY_V2_Q1);
            db.execSQL(SQL_ALTER_QUERY_V2_Q2);
        }

        db.execSQL(SQL_CREATE_USER_ACTIONS_TABLE);


    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
