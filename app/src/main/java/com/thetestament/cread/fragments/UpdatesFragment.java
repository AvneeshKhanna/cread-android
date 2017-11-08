package com.thetestament.cread.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.thetestament.cread.R;
import com.thetestament.cread.adapters.UpdatesAdapter;
import com.thetestament.cread.database.NotificationsDBHelper;
import com.thetestament.cread.database.NotificationsDBSchema;
import com.thetestament.cread.models.UpdatesModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_ACTOR_USERID;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_CATEGORY;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_DATE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_ENTITY_ID;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_MESSAGE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_SEEN;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_TIME;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_USER_IMAGE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.TABLE_NAME;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_HATSOFF;



        /*Fragment class which shows notification.*/


public class UpdatesFragment extends Fragment {

    // Required empty public constructor
    public UpdatesFragment() {
    }

    @BindView(R.id.view_no_notifications)
    LinearLayout viewNoNotification;
    @BindView(R.id.swipeToRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;
    private UpdatesFragment mUpdatesFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mUpdatesFragment = this;
        //Inflate this view
        View view = inflater.inflate(R.layout.fragment_updates, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //load Data here
        initScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


     /*Method which execute GetUpdatesData AsyncTask.*/


    private void loadUpdates() {
        new getUpdatesData().execute();
    }


     /* AsyncTask which retrieve notifications data from Sqlite DB*/


    private class getUpdatesData extends AsyncTask<Void, Void, Void> {

        SQLiteDatabase sqLiteDatabase;
        List<UpdatesModel> updatesDataList = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... params) {

            NotificationsDBHelper notificationsDBHelper = new NotificationsDBHelper(getContext());
            // Gets the data repository in read mode
            sqLiteDatabase = notificationsDBHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    "*"
            };



            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    NotificationsDBSchema.NotificationDBEntry._ID + " DESC";

            Cursor cursor = sqLiteDatabase.query(
                    TABLE_NAME,                               // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );
            //  Since the cursor starts at position -1
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {

                String notifyDateTime = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DATE))
                        + " at "

                        + cursor.getString((cursor.getColumnIndex(COLUMN_NAME_TIME)));

                UpdatesModel updatesData = new UpdatesModel();

                updatesData.setMessage(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_MESSAGE)));
                updatesData.setTimeStamp(notifyDateTime);
                updatesData.set_ID(cursor.getInt(cursor.getColumnIndex(NotificationsDBSchema.NotificationDBEntry._ID)));
                updatesData.setSeen(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SEEN)));
                updatesData.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CATEGORY)));
                updatesData.setActorID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ACTOR_USERID)));
                updatesData.setEntityID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ENTITY_ID)));
                updatesData.setActorImage(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USER_IMAGE)));
                updatesDataList.add(updatesData);
                cursor.moveToNext();
            }
            //Close to release resources
            cursor.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mUpdatesFragment != null && mUpdatesFragment.isVisible()) {
                //if the fragment is visible, only then load the contents
                //If there is no notification
                if (updatesDataList.size() == 0) {
                    viewNoNotification.setVisibility(View.VISIBLE);
                }
                UpdatesAdapter updatesAdapter = new UpdatesAdapter(updatesDataList, getActivity());
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(updatesAdapter);
                swipeRefreshLayout.setRefreshing(false);

                /*updatesAdapter.setNotificationItemClick(new UpdatesAdapter.NotificationItemClick() {
                    @Override
                    public void onNotificationClick(String notificationType, String shareId) {
                        switch (notificationType) {
                            case NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                            case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:
                                Intent returnIntent = new Intent();
                                Bundle returnData = new Bundle();
                                returnData.putString("notificationCategory", notificationType);
                                returnIntent.putExtras(returnData);

                                getActivity().setResult(RESULT_OK, returnIntent);
                                //Finish this activity
                                getActivity().finish();
                                break;

                            case NOTIFICATION_CATEGORY_CREAD_HATSOFF:

                                Intent returnIntentShareStatus = new Intent();
                                Bundle returnDataShareStatus = new Bundle();
                                returnDataShareStatus.putString("notificationCategory", NOTIFICATION_CATEGORY_CREAD_SHARE_STATUS);
                                returnDataShareStatus.putString("shareID", shareId);
                                returnIntentShareStatus.putExtras(returnDataShareStatus);

                                getActivity().setResult(RESULT_OK, returnIntentShareStatus);
                                //Finish this activity
                                getActivity().finish();
                                break;

                            case NOTIFICATION_CATEGORY_CREAD_COMMENT:
                                Intent returnIntentGivers = new Intent();
                                Bundle returnDataGivers = new Bundle();
                                returnDataGivers.putString("notificationCategory", NOTIFICATION_CATEGORY_CREAD_TOP_GIVERS);
                                returnIntentGivers.putExtras(returnDataGivers);

                                getActivity().setResult(RESULT_OK, returnIntentGivers);
                                //Finish this activity
                                getActivity().finish();
                                break;

                            case NOTIFICATION_CATEGORY_CREAD_BUY:
                                Intent returnIntentGivers = new Intent();
                                Bundle returnDataGivers = new Bundle();
                                returnDataGivers.putString("notificationCategory", NOTIFICATION_CATEGORY_CREAD_TOP_GIVERS);
                                returnIntentGivers.putExtras(returnDataGivers);

                                getActivity().setResult(RESULT_OK, returnIntentGivers);
                                //Finish this activity
                                getActivity().finish();
                                break;

                            default:
                                break;
                        }
                    }
                });*/

            }
        }
    }

    //Method to initialize this screen.
    private void initScreen() {
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext()
                , R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUpdates();//For loading the notifications
            }
        });
        loadUpdates(); //For loading the notifications
    }
}
