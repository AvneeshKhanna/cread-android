package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.models.AchievementsModels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Achievements RecyclerView.
 */

public class OtherUserAchievementsAdapter extends RecyclerView.Adapter<OtherUserAchievementsAdapter.ItemVIewHolder> {

    //region :Field and constants
    List<AchievementsModels> mAchievementsDataList;
    FragmentActivity mContext;
    //endregion

    //region :Constructor

    /**
     * Required constructor.
     *
     * @param achievementsDataList List of achievement data.
     * @param context              Context to use.
     */
    public OtherUserAchievementsAdapter(List<AchievementsModels> achievementsDataList, FragmentActivity context) {
        this.mAchievementsDataList = achievementsDataList;
        this.mContext = context;
    }
    //endregion

    //region :Overridden methods
    @Override
    public ItemVIewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemVIewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_other_user_achievements
                        , parent,
                        false));
    }

    @Override
    public void onBindViewHolder(ItemVIewHolder holder, int position) {
        AchievementsModels data = mAchievementsDataList.get(position);
        //set badge title
        holder.badgeTitle.setText(data.getBadgeTitle());
        //load badge image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getBadgeImageUrl()), holder.badgeImage);
        //Method called
        initItemClick(holder.itemView, data);
    }

    @Override
    public int getItemCount() {
        return mAchievementsDataList.size();
    }

    //endregion

    //region :Private methods

    /**
     * ItemView click functionality.
     *
     * @param view view to be clicked.
     * @param data Data of item.
     */
    private void initItemClick(View view, final AchievementsModels data) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBadgeDetailsDialog(data);
            }
        });
    }


    /**
     * Method to show unlock badge details.
     *
     * @param data Achievement model data.
     */
    private void showBadgeDetailsDialog(AchievementsModels data) {
        // show detail dialog
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_unlocked_badge,
                        false)
                .show();

        //Obtain dialog views
        SimpleDraweeView badgeImage = dialog.getCustomView().findViewById(R.id.img_badge);
        AppCompatTextView badgeTitle = dialog.getCustomView().findViewById(R.id.badge_title);
        AppCompatTextView desc = dialog.getCustomView().findViewById(R.id.text_congratulation);
        AppCompatTextView btnShare = dialog.getCustomView().findViewById(R.id.btn_share);
        //Set share button text
        btnShare.setText("Ok");

        //Load badge image here
        ImageHelper.loadProgressiveImage(Uri.parse(data.getBadgeImageUrl()), badgeImage);
        //Set title and desc
        badgeTitle.setText(data.getBadgeTitle());
        desc.setText(data.getUnlockDescription());

        //Button click functionality
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dismiss dialog
                dialog.dismiss();
            }
        });

    }

    //endregion

    //region :ViewHolders
    static class ItemVIewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.badge_image)
        SimpleDraweeView badgeImage;
        @BindView(R.id.badge_title)
        AppCompatTextView badgeTitle;


        public ItemVIewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
    //endregion
}
