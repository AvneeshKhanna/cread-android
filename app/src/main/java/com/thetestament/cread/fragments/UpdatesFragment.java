/*
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;


*/
/**
 * Fragment class which shows notification.
 *//*


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

    */
/**
     * Method which execute GetUpdatesData AsyncTask.
     *//*

    private void loadUpdates() {
        new getUpdatesData().execute();
    }

    */
/**
     * AsyncTask which retrieve notifications data from Sqlite DB
     *//*

    private class getUpdatesData extends AsyncTask<Void, Void, Void> {

        SQLiteDatabase sqLiteDatabase;
        List<UpdatesData> updatesDataList = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... params) {

            NotificationsDBHelper notificationsDBHelper = new NotificationsDBHelper(getContext());
            // Gets the data repository in read mod
            sqLiteDatabase = notificationsDBHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    "*"
            };

            // Filter results WHERE "type" = "2.0"
            String selection = COLUMN_NAME_TYPE + " = ?";
            String[] selectionArgs = {"2.0"};

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    NotificationsDBSchema.NotificationDBEntry._ID + " DESC";

            Cursor cursor = sqLiteDatabase.query(
                    TABLE_NAME,                               // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
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

                UpdatesData updatesData = new UpdatesData();

                updatesData.setMessage(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_MESSAGE)));
                updatesData.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE)));
                updatesData.setTimeStamp(notifyDateTime);
                updatesData.set_ID(cursor.getInt(cursor.getColumnIndex(NotificationsDBSchema.NotificationDBEntry._ID)));
                updatesData.setSeen(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SEEN)));
                updatesData.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CATEGORY)));
                updatesData.setCampaignID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CAMPAIGN_ID)));
                updatesData.setShareID(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_SHARE_ID)));
                // Selection of notification  icon depending  on category
                switch (updatesData.getCategory()) {
                    case NOTIFICATION_CATEGORY_CREAD_CAMPAIGN_SPECIFIC:
                        updatesData.setLogo(R.drawable.ic_cread_notification_camp_specific);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_SHARE_STATUS:
                        updatesData.setLogo(R.drawable.ic_cread_notification_share_status);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_CAMPAIGN_UNLOCKED:
                        updatesData.setLogo(R.drawable.ic_cread_notification_camp_unlocked);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                        updatesData.setLogo(R.drawable.ic_cread_notification_general);
                        break;

                    case NOTIFICATION_CATEGORY_CREAD_TOP_GIVERS:
                        updatesData.setLogo(R.drawable.ic_cread_notification_top_givers);
                        break;
                }

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

                updatesAdapter.setNotificationItemClick(new UpdatesAdapter.NotificationItemClick() {
                    @Override
                    public void onNotificationClick(String notificationType, String shareId) {
                        switch (notificationType) {
                            case NOTIFICATION_CATEGORY_CREAD_CAMPAIGN_UNLOCKED:
                            case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                                Intent returnIntent = new Intent();
                                Bundle returnData = new Bundle();
                                returnData.putString("notificationCategory", notificationType);
                                returnIntent.putExtras(returnData);

                                getActivity().setResult(RESULT_OK, returnIntent);
                                //Finish this activity
                                getActivity().finish();
                                break;

                            case NOTIFICATION_CATEGORY_CREAD_SHARE_STATUS:

                                Intent returnIntentShareStatus = new Intent();
                                Bundle returnDataShareStatus = new Bundle();
                                returnDataShareStatus.putString("notificationCategory", NOTIFICATION_CATEGORY_CREAD_SHARE_STATUS);
                                returnDataShareStatus.putString("shareID", shareId);
                                returnIntentShareStatus.putExtras(returnDataShareStatus);

                                getActivity().setResult(RESULT_OK, returnIntentShareStatus);
                                //Finish this activity
                                getActivity().finish();
                                break;

                            case NOTIFICATION_CATEGORY_CREAD_TOP_GIVERS:
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
                });

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
*/
