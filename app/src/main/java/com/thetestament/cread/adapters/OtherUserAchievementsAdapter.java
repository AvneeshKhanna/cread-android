package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.listeners.listener;
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

    //region :Listeners
    listener.OnBadgeClickListener onBadgeClickListener;

    /**
     * Register a callback to be invoked when user clicks on badge item.
     */
    public void setOnBadgeClickListener(listener.OnBadgeClickListener onBadgeClickListener) {
        this.onBadgeClickListener = onBadgeClickListener;
    }
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
                //set click listener
                //onBadgeClickListener.onBadgeClick(data);
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
