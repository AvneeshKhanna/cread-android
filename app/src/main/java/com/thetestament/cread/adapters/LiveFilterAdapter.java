package com.thetestament.cread.adapters;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.LiveFilterHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.LiveFilterModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a liveFilter RecyclerView.
 */
public class LiveFilterAdapter extends RecyclerView.Adapter<LiveFilterAdapter.ItemViewHolder> {

    private List<LiveFilterModel> mDataList;
    private FragmentActivity mContext;
    private listener.OnLiveFilterClickListener liveFilterClickListener;

    private int mTemplateSelected = 0;

    /**
     * Required constructor.
     *
     * @param dataList List of live filter data.
     * @param context  Context to be use.
     */
    public LiveFilterAdapter(List<LiveFilterModel> dataList, FragmentActivity context) {
        this.mDataList = dataList;
        this.mContext = context;
    }

    /**
     * Register a callback to be invoked when user selects live filter.
     */
    public void setOnLiveFilterClickListener(listener.OnLiveFilterClickListener liveFilterClickListener) {
        this.liveFilterClickListener = liveFilterClickListener;
    }

    @Override
    public LiveFilterAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_live_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(LiveFilterAdapter.ItemViewHolder holder, final int position) {
        final LiveFilterModel data = mDataList.get(position);

        //Set live filter name
        holder.nameLiveFilter.setText(data.getLiveFilterName());
        //Set image
        holder.imageLiveFilter.setImageDrawable(LiveFilterHelper.getLiveFilterDrawable(data.getLiveFilterName(), mContext));


        //ItemView click functionality
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set Listener
                liveFilterClickListener.onLiveFilterClick(data.getLiveFilterName().toLowerCase(), position);
                //update flag and notify changes
                mTemplateSelected = position;
                notifyDataSetChanged();
            }
        });

        //update color
        if (mTemplateSelected == position) {
            holder.nameLiveFilter.setTextColor(Color.BLACK);
        } else {
            holder.nameLiveFilter.setTextColor(Color.GRAY);
        }

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    //Item view holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name_live_filter)
        AppCompatTextView nameLiveFilter;
        @BindView(R.id.image_live_filter)
        AppCompatImageView imageLiveFilter;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }

}
