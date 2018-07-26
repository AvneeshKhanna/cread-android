package com.thetestament.cread.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.AchievementsAdapter;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.AchievementsModels;
import com.thetestament.cread.networkmanager.AchivementNetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;


/**
 * Achievement screen to show badge system for gamification.
 */
public class AchievementsActivity extends BaseActivity {

    //region :Views binding with Butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.progress_view)
    View loadingView;
    //endregion

    //region :Private fields and constant.
    /**
     * To store achievement data.
     */
    List<AchievementsModels> mAchievementDataList = new ArrayList<>();

    /**
     * To store reference of this screen.
     */
    FragmentActivity mContext;

    /**
     * CompositeDisposable for reactive programming.
     */
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @State
    String mRequestedUUID;
    //endregion

    //region :Overridden methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        ButterKnife.bind(this);
        mContext = this;
        //Method called
        initViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setBackButtonBehaviour();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        setBackButtonBehaviour();
        finish();
    }
    //endregion

    //region :Private methods

    /**
     * Methods is initialise views for this screen.
     */
    private void initViews() {
        //retrieve data from intent
        mRequestedUUID = getIntent().getStringExtra("requesteduuid");

        recyclerView.setHasFixedSize(true);
        //set layout manager
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        AchievementsAdapter adapter = new AchievementsAdapter(mAchievementDataList, mContext);
        //set adapter
        recyclerView.setAdapter(adapter);
        //Listener
        initBadgeClickListener(adapter);
        //Load data here
        loadAchievementsData(adapter);
    }

    /**
     * Load achievements data.
     */
    private void loadAchievementsData(final AchievementsAdapter adapter) {
        //Show loading view
        loadingView.setVisibility(View.VISIBLE);
        AchivementNetworkManager.getAchievementsData(mContext, mCompositeDisposable, mRequestedUUID, new AchivementNetworkManager.OnAchievementsLoadListener() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    JSONObject mainData = jsonObject.getJSONObject("data");
                    //Achievements list
                    JSONArray achievementArray = mainData.getJSONArray("items");
                    for (int i = 0; i < achievementArray.length(); i++) {
                        AchievementsModels data = new AchievementsModels();

                        JSONObject dataObj = achievementArray.getJSONObject(i);
                        data.setBadgeTitle(dataObj.getString("title"));
                        data.setBadgeImageUrl(dataObj.getString("imgurl"));
                        data.setBadgeUnlock(dataObj.getBoolean("unlocked"));
                        data.setUnlockDescription(dataObj.getString("description"));
                        mAchievementDataList.add(data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //notify changes
                adapter.notifyDataSetChanged();
                //Hide loading view
                loadingView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String errorMsg) {
                //Show error msg snack bar.
                ViewHelper.getSnackBar(rootView, errorMsg);
                //Hide loading view
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Method to initialize badge click listener.
     *
     * @param adapter Achievement adapter reference.
     */
    private void initBadgeClickListener(AchievementsAdapter adapter) {
        adapter.setOnBadgeClickListener(new listener.OnBadgeClickListener() {
            @Override
            public void onBadgeClick(AchievementsModels data) {
                //todo code here
                showBadgeDetailsDialog(data);
            }
        });
    }


    /**
     *
     * */
    private void showBadgeDetailsDialog(AchievementsModels data) {
        // show detail dialog
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_badge_details,
                        false)
                .show();

        //Obtain dialog views
        SimpleDraweeView badgeImage = dialog.getCustomView().findViewById(R.id.img_badge);
        AppCompatTextView badgeTitle = dialog.getCustomView().findViewById(R.id.badge_title);
        AppCompatTextView desc = dialog.getCustomView().findViewById(R.id.text_congratulation);
        AppCompatTextView btnShare = dialog.getCustomView().findViewById(R.id.btn_share);

        //Load badge image here
        ImageHelper.loadProgressiveImage(Uri.parse(data.getBadgeImageUrl()), badgeImage);
        //Set title and desc
        badgeTitle.setText(data.getBadgeTitle());
        desc.setText(data.getUnlockDescription());

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo sharing func.
                dialog.dismiss();
            }
        });

    }



    /**
     * Method to launch parent activity.
     */
    private void setBackButtonBehaviour() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }
    }


    //endregion
}
