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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.crash.FirebaseCrash;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.thetestament.cread.BuildConfig;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.adapters.UpdatesAdapter;
import com.thetestament.cread.database.NotificationsDBHelper;
import com.thetestament.cread.database.NotificationsDBSchema;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.UpdatesModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_ACTOR_USERID;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_CATEGORY;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_DATE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_ENTITY_ID;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_MESSAGE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_SEEN;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_TIME;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.COLUMN_NAME_USER_IMAGE;
import static com.thetestament.cread.database.NotificationsDBSchema.NotificationDBEntry.TABLE_NAME;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_BUY;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COLLABORATE;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_COMMENT;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_FOLLOW;
import static com.thetestament.cread.utils.Constant.NOTIFICATION_CATEGORY_CREAD_GENERAL;
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
    @BindView(R.id.viewProgress)
    ProgressBar progressBar;
    @BindView(R.id.rootView)
    RelativeLayout rootView;

    private Unbinder unbinder;
    private UpdatesFragment mUpdatesFragment;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final String TAG = getClass().getSimpleName();

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
        mCompositeDisposable.dispose();
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

            NotificationsDBHelper notificationsDBHelper = new NotificationsDBHelper(getActivity());
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

            sqLiteDatabase.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


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
                public void onNotificationClick(String notificationType, String entityID) {
                    switch (notificationType) {
                        case NOTIFICATION_CATEGORY_CREAD_FOLLOW:
                            // handled in updates adapter
                            break;
                        case NOTIFICATION_CATEGORY_CREAD_COLLABORATE:

                            // gets feed details and opens details screen
                            getFeedDetails(entityID);

                            break;

                        case NOTIFICATION_CATEGORY_CREAD_HATSOFF:

                            // gets feed details and opens details screen
                            getFeedDetails(entityID);
                            break;

                        case NOTIFICATION_CATEGORY_CREAD_COMMENT:

                            // gets feed details and opens details screen
                            getFeedDetails(entityID);
                            break;

                        case NOTIFICATION_CATEGORY_CREAD_BUY:

                            // gets feed details and opens details screen
                            getFeedDetails(entityID);
                            break;

                        case NOTIFICATION_CATEGORY_CREAD_GENERAL:
                            // handled in updates adapter
                            break;

                        default:
                            break;
                    }
                }
            });

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


    /**
     * RxJava2 implementation for retrieving feed details
     *
     * @param entityID
     */
    private void getFeedDetails(final String entityID) {
        final boolean[] tokenError = {false};
        final boolean[] connectionError = {false};

        final FeedModel feedData = new FeedModel();

        progressBar.setVisibility(View.VISIBLE);

        SharedPreferenceHelper spHelper = new SharedPreferenceHelper(getActivity());

        JSONObject data = new JSONObject();
        try {
            data.put("uuid", spHelper.getUUID());
            data.put("authkey", spHelper.getAuthToken());
            data.put("entityid", entityID);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Rx2AndroidNetworking.post(BuildConfig.URL + "/entity-manage/load-specific")
                .addJSONObjectBody(data)
                .build()
                .getJSONObjectObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<JSONObject>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull JSONObject jsonObject) {

                        progressBar.setVisibility(View.GONE);

                        try {
                            //Token status is invalid
                            if (jsonObject.getString("tokenstatus").equals("invalid")) {
                                tokenError[0] = true;
                            } else {
                                JSONObject mainObject = jsonObject.getJSONObject("data");

                                JSONObject dataObj = mainObject.getJSONObject("entity");

                                feedData.setEntityID(entityID);
                                feedData.setCaptureID(dataObj.getString("captureid"));
                                feedData.setContentType(dataObj.getString("type"));
                                feedData.setUUID(dataObj.getString("uuid"));
                                feedData.setCreatorImage(dataObj.getString("profilepicurl"));
                                feedData.setCreatorName(dataObj.getString("creatorname"));
                                feedData.setHatsOffStatus(dataObj.getBoolean("hatsoffstatus"));
                                feedData.setMerchantable(dataObj.getBoolean("merchantable"));
                                feedData.setHatsOffCount(dataObj.getLong("hatsoffcount"));
                                feedData.setCommentCount(dataObj.getLong("commentcount"));
                                feedData.setContentImage(dataObj.getString("entityurl"));


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            FirebaseCrash.report(e);
                            connectionError[0] = true;

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                        progressBar.setVisibility(View.GONE);
                        FirebaseCrash.report(e);
                        //Server error Snack bar
                        ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_server));

                    }

                    @Override
                    public void onComplete() {

                        // Token status invalid
                        if (tokenError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_invalid_token));
                        }
                        //Error occurred
                        else if (connectionError[0]) {
                            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));

                        } else

                        {
                            Intent intent = new Intent(getActivity(), FeedDescriptionActivity.class);
                            intent.putExtra(EXTRA_FEED_DESCRIPTION_DATA, feedData);
                            getActivity().startActivity(intent);

                            getActivity().finish();
                        }
                    }
                });


    }
}
