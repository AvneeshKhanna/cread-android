/*
package com.thetestament.cread.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry;


*/
/**
 * Created by PRAKHAR 95 on 06-09-2016.
 *//*

public class NotificationsDBFunctions {

    private AppCompatActivity context;
    private SQLiteDatabase db;
    private String userId;

    //Required constructor
    public NotificationsDBFunctions(AppCompatActivity context) {
        this.context = context;
        userId = AccountManagerUtils.getUserID(context);
    }

    */
/**
     * Method to access  sqliteDB in writable mode
     *//*

    public void accessFormDatabase() {
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

    }
}
*/
