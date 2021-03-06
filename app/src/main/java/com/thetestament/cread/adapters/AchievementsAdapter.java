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
import com.thetestament.cread.activities.AchievementsActivity;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.AchievementsModels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Achievements RecyclerView.
 * {@link AchievementsActivity#recyclerView}
 */

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ItemVIewHolder> {

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
    public AchievementsAdapter(List<AchievementsModels> achievementsDataList, FragmentActivity context) {
        this.mAchievementsDataList = achievementsDataList;
        this.mContext = context;
    }
    //endregion

    //region :Overridden methods
    @Override
    public ItemVIewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemVIewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievements
                        , parent,
                        false));
    }

    @Override
    public void onBindViewHolder(ItemVIewHolder holder, int position) {
        AchievementsModels data = mAchievementsDataList.get(position);

        //if badge is unlocked
        if (data.isBadgeUnlock()) {
            //toggle view visibility
            holder.badgeOverlay.setVisibility(View.GONE);
            holder.textReveal.setVisibility(View.GONE);
            //set badge title
            holder.badgeTitle.setText(data.getBadgeTitle());
        } else {
            //toggle view visibility
            holder.badgeOverlay.setVisibility(View.VISIBLE);
            holder.textReveal.setVisibility(View.VISIBLE);
            //set badge title
            holder.badgeTitle.setText("Locked");
        }
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
                onBadgeClickListener.onBadgeClick(data);
            }
        });
    }
    //endregion

    //region :ViewHolders
    static class ItemVIewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.badge_image)
        SimpleDraweeView badgeImage;
        @BindView(R.id.badge_overlay)
        CircleImageView badgeOverlay;
        @BindView(R.id.text_reveal)
        AppCompatTextView textReveal;
        @BindView(R.id.badge_title)
        AppCompatTextView badgeTitle;


        public ItemVIewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
    //endregion
}
