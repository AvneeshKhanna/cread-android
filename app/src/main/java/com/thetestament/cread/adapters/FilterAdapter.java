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
import com.thetestament.cread.listeners.listener.OnFilterSelectListener;
import com.thetestament.cread.models.FilterModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a filter RecyclerView.
 */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ItemViewHolder> {

    private int mImageSelected = 0;
    private FragmentActivity mContext;
    private List<FilterModel> mFilterDataList;

    private OnFilterSelectListener onFilterSelectListener;


    /**
     * Constructor to create instance of this adapter class.
     *
     * @param filterDataList List of filter data.
     * @param context        Content to use.
     */
    public FilterAdapter(FragmentActivity context, List<FilterModel> filterDataList) {
        this.mContext = context;
        mFilterDataList = filterDataList;
    }

    /**
     * Register a callback to be invoked when user selects filter.
     */
    public void setOnFilterSelectListener(OnFilterSelectListener onFilterSelectListener) {
        this.onFilterSelectListener = onFilterSelectListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        //FilterModel data = new FilterModel();

        //Set filter name
        // holder.filterName.setText(data.getFilterName());
        //Set image
        // holder.filterImage.setImageBitmap(data.getImage());

        holder.filterName.setText(mFilterDataList.get(position).filterName);
        holder.filterImage.setImageBitmap(mFilterDataList.get(position).image);
        //Item Click functionality.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set listener
                onFilterSelectListener.onFilterSelected(mFilterDataList.get(position).image);
                //update flag and notify changes
                mImageSelected = position;
                notifyDataSetChanged();
            }
        });

        //update color
        if (mImageSelected == position) {
            holder.filterName.setTextColor(Color.BLACK);
        } else {
            holder.filterName.setTextColor(Color.GRAY);
        }

    }

    @Override
    public int getItemCount() {
        return mFilterDataList.size();
    }

    //Item view holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.filterName)
        AppCompatTextView filterName;
        @BindView(R.id.filterImage)
        AppCompatImageView filterImage;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }


}
