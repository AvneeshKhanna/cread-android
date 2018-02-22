package com.thetestament.cread.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.Callable;

import static com.thetestament.cread.database.UserActionsDBSchema.UserActionsDBEntry;
import static com.thetestament.cread.database.UserActionsDBSchema.UserActionsDBEntry.COLUMN_NAME_ACTION_TYPE;
import static com.thetestament.cread.database.UserActionsDBSchema.UserActionsDBEntry.COLUMN_NAME_ACTOR_ID;
import static com.thetestament.cread.database.UserActionsDBSchema.UserActionsDBEntry.COLUMN_NAME_ENTITY_ID;
import static com.thetestament.cread.database.UserActionsDBSchema.UserActionsDBEntry.COLUMN_NAME_TIMESTAMP;
import static com.thetestament.cread.database.UserActionsDBSchema.UserActionsDBEntry.TABLE_NAME;



    /*Created by PRAKHAR 95on 06-09-2016*/


public class NotificationsDBFunctions {

    private Context context;
    private SQLiteDatabase db;
    private String userId;

    //Required constructor
    public NotificationsDBFunctions(Context context) {
        this.context = context;
        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);
        userId = spHelper.getUUID();
    }


      /*Method to access  sqliteDB in writable mode*/


    public void accessNotificationsDatabase() {
        NotificationsDBHelper notificationsDBHelper = new NotificationsDBHelper(context);
        db = notificationsDBHelper.getWritableDatabase();
    }

    public void setSeen(int _id) {
        //Check if the calling thread is main thread or not. If it is, throw an exception
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new NetworkOnMainThreadException();
        }

        // Which row to update, based on the title
        String selection = NotificationDBEntry._ID + " LIKE ?" + " AND "
                + NotificationDBEntry.COLUMN_NAME_USER_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(_id), userId};

        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationDBEntry.COLUMN_NAME_SEEN, "true");

        db.update(
                NotificationDBEntry.TABLE_NAME,
                contentValues,
                selection,
                selectionArgs);

        db.close();
    }

    public int getUnreadCount() {
        if (BuildConfig.DEBUG) {
            Log.d("db func", "getUnreadCount: " + Thread.currentThread().getName());
        }

        //Check if the calling thread is main thread or not. If it is, throw an exception
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new NetworkOnMainThreadException();
        }

        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {

                NotificationDBEntry.COLUMN_NAME_UNREAD
        };

// Filter results WHERE "title" = 'My Title'
        String selection = NotificationDBEntry.COLUMN_NAME_UNREAD + " = ?" + " AND "
                + NotificationDBEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(true), userId};

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                NotificationDBEntry._ID + " DESC";


        Cursor c = db.query(
                NotificationDBEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();
        int count = c.getCount();
        c.close();

        db.close();

        return count;
    }

    public void setRead() {

        //Check if the calling thread is main thread or not. If it is, throw an exception
        if (Looper.myLooper() == Looper.getMainLooper()) {

            throw new NetworkOnMainThreadException();
        }

        String selection = NotificationDBEntry.COLUMN_NAME_USER_ID + " LIKE ?";
        String[] selectionArgs = {userId};

        ContentValues contentValues = new ContentValues();
        contentValues.put(NotificationDBEntry.COLUMN_NAME_UNREAD, "false");

        db.update(
                NotificationDBEntry.TABLE_NAME,
                contentValues,
                selection,
                selectionArgs);

        db.close();

    }


    public JSONArray getUserActionsData(String userId) {

        JSONArray jsonArray = new JSONArray();

        String selection = UserActionsDBEntry.COLUMN_NAME_ACTOR_ID + " = ?";
        String[] selectionArgs = {userId};

        Cursor c = db.query(
                UserActionsDBEntry.TABLE_NAME,                     // The table to query
                null,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        try {


            while (c.moveToNext()) {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("actor_uuid", c.getString(c.getColumnIndex(UserActionsDBEntry.COLUMN_NAME_ACTOR_ID)));
                jsonObject.put("entityid", c.getString(c.getColumnIndex(UserActionsDBEntry.COLUMN_NAME_ENTITY_ID)));
                jsonObject.put("event_type", c.getString(c.getColumnIndex(UserActionsDBEntry.COLUMN_NAME_ACTION_TYPE)));
                jsonObject.put("regdate", c.getString(c.getColumnIndex(UserActionsDBEntry.COLUMN_NAME_TIMESTAMP)));

                jsonArray.put(jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrash.report(e);
        }

        c.close();
        db.close();

        return jsonArray;

    }

    public void deleteUserActionsData(String userId) {
        String selection = UserActionsDBEntry.COLUMN_NAME_ACTOR_ID + " = ?";
        String[] selectionArgs = {userId};

        db.delete(TABLE_NAME, selection, selectionArgs);

        db.close();
    }

    private void getDataFromDatabase(String entityid, String actionType) {

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

        Date date = new Date();
        String timestamp = TimeUtils.getISO8601StringForDate(date);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_ENTITY_ID, entityid);
        contentValues.put(COLUMN_NAME_ACTOR_ID, spHelper.getUUID());
        contentValues.put(COLUMN_NAME_ACTION_TYPE, actionType);
        contentValues.put(COLUMN_NAME_TIMESTAMP, timestamp);

        db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();

    }

    public Callable getData(final String entityid, final String actionType) {
        return new Callable<Void>() {
            public Void call() {
                getDataFromDatabase(entityid, actionType);
                return null;
            }
        };
    }
}
