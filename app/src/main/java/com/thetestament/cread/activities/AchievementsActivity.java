package com.thetestament.cread.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.Manifest;
import com.thetestament.cread.R;
import com.thetestament.cread.adapters.AchievementsAdapter;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.AchievementsModels;
import com.thetestament.cread.networkmanager.AchivementNetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import io.reactivex.disposables.CompositeDisposable;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;


/**
 * Achievement screen to show badge system for gamification.
 */
public class AchievementsActivity extends BaseActivity {

    //region :Views binding with Butter knife
    @BindView(R.id.root_view)
    CoordinatorLayout rootView;
    @BindView(R.id.img_user)
    SimpleDraweeView imgUser;
    @BindView(R.id.text_achievement)
    AppCompatTextView textAchievement;
    @BindView(R.id.text_achievement_details)
    AppCompatTextView textAchievementDesc;
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

    /**
     * String to store UUID of user whose achievement to be loaded.
     */
    @State
    String mRequestedUUID;

    /**
     * Parent view of the data to be loaded.
     */
    LinearLayout dialogParentView;

    /**
     * FLag to maintain unlocked badge count.
     */
    @State
    int unlockedBadgeCount = 0;
    /**
     * FLag to maintain total badge count.
     */
    @State
    int totalBadgeCount = 0;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Required for permission manager library
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        //set dialogParentView manager
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
                    ImageHelper.loadProgressiveImage(Uri.parse(mainData.getString("profilepicurl")), imgUser);
                    //Achievements list
                    JSONArray achievementArray = mainData.getJSONArray("items");
                    //set total badge count
                    totalBadgeCount = achievementArray.length();
                    for (int i = 0; i < achievementArray.length(); i++) {
                        AchievementsModels data = new AchievementsModels();

                        JSONObject dataObj = achievementArray.getJSONObject(i);
                        data.setBadgeTitle(dataObj.getString("title"));
                        data.setBadgeImageUrl(dataObj.getString("imgurl"));
                        data.setBadgeUnlock(dataObj.getBoolean("unlocked"));
                        data.setUnlockDescription(dataObj.getString("description"));
                        mAchievementDataList.add(data);

                        if (dataObj.getBoolean("unlocked")) {
                            //increase unlocked badge count by one
                            unlockedBadgeCount++;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //set text
                textAchievement.setText(unlockedBadgeCount + "/" + totalBadgeCount + " badges unlocked");
                textAchievementDesc.setVisibility(View.VISIBLE);
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
                //If badge unlocked
                if (data.isBadgeUnlock()) {
                    showUnlockedBadgeDetailsDialog(data);
                }
                //badge is locked
                else {
                    showLockedBadgeDetailsDialog(data);
                }

            }
        });
    }


    /**
     * Method to show unlock badge details.
     *
     * @param data Achievement model data.
     */
    private void showUnlockedBadgeDetailsDialog(AchievementsModels data) {
        // show detail dialog
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_unlocked_badge,
                        false)
                .show();

        //Obtain dialog views
        final SimpleDraweeView badgeImage = dialog.getCustomView().findViewById(R.id.img_badge);
        AppCompatTextView badgeTitle = dialog.getCustomView().findViewById(R.id.badge_title);
        final AppCompatTextView desc = dialog.getCustomView().findViewById(R.id.text_congratulation);
        final AppCompatTextView btnShare = dialog.getCustomView().findViewById(R.id.btn_share);
        dialogParentView = dialog.getCustomView().findViewById(R.id.parent_view);
        final View dividerTop = dialog.getCustomView().findViewById(R.id.divider_top);
        final View dividerBottom = dialog.getCustomView().findViewById(R.id.divider_bottom);

        //Load badge image here
        ImageHelper.loadProgressiveImage(Uri.parse(data.getBadgeImageUrl()), badgeImage);
        //Set title and desc
        badgeTitle.setText(data.getBadgeTitle());
        desc.setText(data.getUnlockDescription());

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desc.setText("Congratulations, " + new SharedPreferenceHelper(mContext).getFirstName() + " for unlocking this badge");
                //Toggle views visibility
                dividerTop.setVisibility(View.GONE);
                dividerBottom.setVisibility(View.GONE);
                btnShare.setVisibility(View.GONE);
                //Method called
                performRuntimePermissionCheck();
                dialog.dismiss();
            }
        });

    }

    /**
     * Method to show lock badge details.
     *
     * @param data Achievement model data.
     */
    private void showLockedBadgeDetailsDialog(AchievementsModels data) {
        // show detail dialog
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_locked_badge,
                        false)
                .show();

        //Obtain dialog views
        AppCompatTextView badgeTitle = dialog.getCustomView().findViewById(R.id.badge_title);
        AppCompatTextView desc = dialog.getCustomView().findViewById(R.id.text_desc);
        AppCompatTextView btnOk = dialog.getCustomView().findViewById(R.id.btn_ok);
        //Set title and desc
        badgeTitle.setText("Locked");
        desc.setText(data.getUnlockDescription());

        //Ok button click functionality
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }


    /**
     * Method to create shareable badge image.
     *
     * @param layout ParentView of dialog view.
     */
    private void createShareableImage(LinearLayout layout) {
        //Enable drawing cache
        layout.setDrawingCacheEnabled(true);
        Bitmap bm = layout.getDrawingCache();

        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Cread/Share/badge_pic.png");
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.close();
            //Disable drawing cache
            layout.setDrawingCacheEnabled(false);

        } catch (IOException e) {
            e.printStackTrace();
            ViewHelper.getSnackBar(rootView, getString(R.string.error_msg_internal));
        }
        //Obtain uri of image to be shared
        Uri uri = FileProvider.getUriForFile(getApplicationContext()
                , getPackageName() + ".provider"
                , new File(Environment.getExternalStorageDirectory() + "/Cread/Share/badge_pic.png"));

        //Launch intent launcher for badge sharing
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share"));
    }


    /**
     * Method to check for runtime permission and perform required operation.
     */
    private void performRuntimePermissionCheck() {
        //Check for Write permission
        if (Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //We have permission do whatever you want to do
            createShareableImage(dialogParentView);
        } else {
            //We do not own this permission
            if (Nammu.shouldShowRequestPermissionRationale(mContext
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //User already refused to give us this permission or removed it
                ViewHelper.getToast(mContext
                        , getString(R.string.error_msg_share_permission_denied));
            } else {
                //First time asking for permission
                Nammu.askForPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE, shareWritePermission);
            }
        }
    }


    /**
     * Used to handle result of askForPermission for share
     */
    PermissionCallback shareWritePermission = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            createShareableImage(dialogParentView);
        }

        @Override
        public void permissionRefused() {
            //Show error message
            ViewHelper.getToast(mContext
                    , getString(R.string.error_msg_share_permission_denied));
        }
    };


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
