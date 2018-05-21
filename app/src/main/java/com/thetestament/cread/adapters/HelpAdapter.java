package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.HelpModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Help RecyclerView.
 */
public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.ItemViewHolder> {

    private List<HelpModel> mHelpDataList;
    private FragmentActivity mContext;

    /**
     * Flag to maintain last selected item position.
     */
    private int mLastExpandedItem;


    private listener.OnFeedBackClickListener feedBackClickListener;

    /**
     * Required constructor.
     *
     * @param helpDataList List of help data.
     * @param mContext     Context to be use.
     */
    public HelpAdapter(List<HelpModel> helpDataList, FragmentActivity mContext) {
        this.mHelpDataList = helpDataList;
        this.mContext = mContext;
    }


    /**
     * Register a callback to be invoked when user uploads feedback.
     */
    public void setFeedBackClickListener(listener.OnFeedBackClickListener feedBackClickListener) {
        this.feedBackClickListener = feedBackClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_help, parent, false));
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        final HelpModel data = mHelpDataList.get(position);
        //Set views property
        holder.textTitle.setText(data.getTitleText());
        holder.textDesc.setText(data.getDescText());
        holder.btnSubmit.setText(data.getBtnText());

        if (data.isExpanded()) {
            holder.containerHelp.setVisibility(View.VISIBLE);
        } else {
            holder.containerHelp.setVisibility(View.GONE);
        }

        //Click functionality
        toggleItemView(holder.itemView, holder.containerHelp, data, position);
        toggleItemView(holder.textTitle, holder.containerHelp, data, position);

        //Submit btn click functionality
        holder.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set listener
                feedBackClickListener.onFeedBackUpdate(data.getHelpID());
            }
        });


    }

    @Override
    public int getItemCount() {
        return mHelpDataList.size();
    }

    //Item view holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textTitle)
        AppCompatTextView textTitle;
        @BindView(R.id.textDesc)
        AppCompatTextView textDesc;
        @BindView(R.id.btnSubmit)
        AppCompatTextView btnSubmit;
        @BindView(R.id.containerHelp)
        LinearLayout containerHelp;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * Method to toggle item visibility.
     *
     * @param view          View to be clicked.
     * @param containerHelp View to be hide/show
     * @param data          Data of selected item.
     * @param itemPosition  Position of item in list.
     */
    private void toggleItemView(View view, final LinearLayout containerHelp, final HelpModel data, final int itemPosition) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if item is expanded
                if (containerHelp.getVisibility() == View.VISIBLE) {
                    //Hide view
                    containerHelp.setVisibility(View.GONE);
                } else {
                    //Show view
                    containerHelp.setVisibility(View.VISIBLE);
                    //Update property
                    data.setExpanded(true);

                    //if lat selected item is not currently selected
                    if (itemPosition != mLastExpandedItem) {
                        mHelpDataList.get(mLastExpandedItem).setExpanded(false);
                        notifyItemChanged(mLastExpandedItem);
                    }
                    mLastExpandedItem = itemPosition;
                }
            }
        });
    }
}
