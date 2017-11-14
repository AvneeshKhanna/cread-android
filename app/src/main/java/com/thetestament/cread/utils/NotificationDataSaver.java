package com.thetestament.cread.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import com.thetestament.cread.database.NotificationsDBHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_ACTOR_USERID;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_CATEGORY;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_DATE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_ENTITY_ID;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_MESSAGE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_SEEN;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_TIME;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_UNREAD;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_USER_ID;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_USER_IMAGE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.TABLE_NAME;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ACTOR_ID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_CATEGORY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_MESSAGE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_BUNDLE_DATA_PERSISTABLE;

/**
 * Helper class for saving notification is sqlite DB.
 */

public class NotificationDataSaver {

    private OnCompleteListener mListener;
    private Context context;

    public interface OnCompleteListener {
        void onComplete();
    }

    public void setOnCompleteListener(OnCompleteListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Method to save NotificationData in Sqlite DB.
     *
     * @param context Context: The context to use. Usually your Application or Activity object.
     * @param data    Bundle which contains notifications data.
     */
    public void save(Context context, Bundle data) {
        this.context = context;
        //Called AsyncTask
        new StoreTask().execute(data);
    }

    /**
     * Async class to save notifications data
     */
    private class StoreTask extends AsyncTask<Bundle, Void, Void> {

        private SQLiteDatabase db;

        @Override
        protected Void doInBackground(Bundle... data) {
            Bundle storeData = data[0];

            String message = storeData.getString(NOTIFICATION_BUNDLE_DATA_MESSAGE);
            String category = storeData.getString(NOTIFICATION_BUNDLE_DATA_CATEGORY);
            String actorID = storeData.getString(NOTIFICATION_BUNDLE_DATA_ACTOR_ID);
            String entityID = storeData.getString(NOTIFICATION_BUNDLE_DATA_ENTITY_ID);
            String actorImage = storeData.getString(NOTIFICATION_BUNDLE_DATA_ACTOR_IMAGE);
            String isPersistable = storeData.getString(NOTIFICATION_BUNDLE_DATA_PERSISTABLE);

            String timeStamp = new SimpleDateFormat("hh-mma").format(new Date());
            String dateStamp = new SimpleDateFormat("dd MMM").format(new Date());


            //If message is not persistable
            if (isPersistable.equals("No")) {

            }
            //Save  message to DB
            else {

                SharedPreferenceHelper spHelper = new SharedPreferenceHelper(context);

                // Create a new map of values, where column names are the keys
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_NAME_ACTOR_USERID, actorID);
                contentValues.put(COLUMN_NAME_MESSAGE, message);
                contentValues.put(COLUMN_NAME_ENTITY_ID, entityID);
                contentValues.put(COLUMN_NAME_CATEGORY, category);
                contentValues.put(COLUMN_NAME_USER_IMAGE, actorImage);
                contentValues.put(COLUMN_NAME_UNREAD, "true");
                contentValues.put(COLUMN_NAME_DATE, dateStamp);
                contentValues.put(COLUMN_NAME_TIME, timeStamp);
                contentValues.put(COLUMN_NAME_SEEN, "false");
                contentValues.put(COLUMN_NAME_USER_ID, spHelper.getUUID());
                //Getting reference
                NotificationsDBHelper notificationsDBHelper = new NotificationsDBHelper(context);
                // Gets the data repository in write mode
                db = notificationsDBHelper.getWritableDatabase();
                // Insert the new row, returning the primary key value of the new row
                db.insert(TABLE_NAME, null, contentValues);

                db.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListener.onComplete();
        }
    }
}
