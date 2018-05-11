package com.thetestament.cread.adapters;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.LabelsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Adapter class to provide a binding from data set to views that are displayed within a labels RecyclerView.
 */

public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.ItemViewHolder> {


    private List<LabelsModel> mLabelsList;
    private FragmentActivity mContext;


    private listener.OnLabelsSelectListener labelsSelectListener;

    /**
     * Required constructor.
     *
     * @param labelsList List of labels data.
     * @param context    Context to be use.
     */
    public LabelsAdapter(List<LabelsModel> labelsList, FragmentActivity context) {
        this.mLabelsList = labelsList;
        this.mContext = context;
    }


    /**
     * Register a callback to be invoked when user select label from list.
     */
    public void setLabelsSelectListener(listener.OnLabelsSelectListener labelsSelectListener) {
        this.labelsSelectListener = labelsSelectListener;
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_labels, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        LabelsModel data = mLabelsList.get(position);
        //Set label
        holder.textLabels.setText(data.getLabel());

        //Toggle label appearance
        toggleLabelAppearance(data.isSelected()
                , mContext
                , holder.textLabels);

        //Initialize itemClick
        itemViewOnClick(mContext, holder.itemView, data, holder.textLabels, holder.getAdapterPosition());

    }

    @Override
    public int getItemCount() {
        return mLabelsList.size();
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textLabels)
        AppCompatTextView textLabels;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * ItemView on click functionality .
     *
     * @param context      Context to use
     * @param view         View to be clicked
     * @param data         Data for the item.
     * @param viewLabel    Label view reference.
     * @param itemPosition Item position in list.
     */
    private void itemViewOnClick(final Context context, View view, final LabelsModel data, final AppCompatTextView viewLabel, final int itemPosition) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toggle data section status
                data.setSelected(!data.isSelected());

                //Toggle label appearance
                toggleLabelAppearance(data.isSelected()
                        , context
                        , viewLabel);

                if (labelsSelectListener != null) {
                    //set listener
                    labelsSelectListener.onLabelSelected(data, itemPosition);
                }
            }
        });

    }

    /**
     * Method to toggle label text color and background.
     *
     * @param isSelected true if selected false otherwise.
     * @param context    Context to use.
     * @param viewLabel  Label view
     */
    private void toggleLabelAppearance(boolean isSelected, Context context, AppCompatTextView viewLabel) {
        if (isSelected) {
            //Change background
            ViewCompat.setBackground(viewLabel
                    , ContextCompat.getDrawable(context
                            , R.drawable.chips_bg_filled));
            //Change text color
            viewLabel.setTextColor(ContextCompat.getColor(context
                    , R.color.white));
        } else {
            //Change background
            ViewCompat.setBackground(viewLabel
                    , ContextCompat.getDrawable(context
                            , R.drawable.chips_bg_outline));
            //Change text color
            viewLabel.setTextColor(ContextCompat.getColor(context
                    , R.color.grey_dark));
        }
    }

    /**
     * Method to update item selection status.
     *
     * @param itemPosition Position of item in the list.
     */
    public void updateItemSelection(int itemPosition) {
        notifyItemChanged(itemPosition);
    }
}


