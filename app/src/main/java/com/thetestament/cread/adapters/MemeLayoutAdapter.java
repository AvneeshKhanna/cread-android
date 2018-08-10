package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;
import com.thetestament.cread.activities.MemeActivity;
import com.thetestament.cread.models.MemeLayoutModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Meme RecyclerView i.e.
 * {@link MemeActivity#recyclerViewMemeLayout}
 */
public class MemeLayoutAdapter extends RecyclerView.Adapter<MemeLayoutAdapter.ItemViewHolder> {

    //region Fields and constants
    private List<MemeLayoutModel> mDataList;
    private FragmentActivity mContext;
    private int mLayoutSelected = 0;
    //endregion

    //region :Required constructor

    /**
     * Required constructor.
     *
     * @param dataList             List of meme layout data.
     * @param mContext             Context to be use.
     * @param lastSelectedPosition position of last selected layout.
     */
    public MemeLayoutAdapter(List<MemeLayoutModel> dataList, FragmentActivity mContext, int lastSelectedPosition) {
        this.mDataList = dataList;
        this.mContext = mContext;
        mLayoutSelected = lastSelectedPosition;
    }
    //endregion

    //region :Listener
    private com.thetestament.cread.listeners.listener.OnMemeLayoutClickListener listener;

    /**
     * Register a callback to be invoked when user selects meme.
     */
    public void setListener(com.thetestament.cread.listeners.listener.OnMemeLayoutClickListener listener) {
        this.listener = listener;
    }
    //endregion

    //region :Overridden methods
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_meme_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        final MemeLayoutModel data = mDataList.get(position);
        //set views properties
        holder.imgMemeLayout.setImageResource(data.getDrawableID());
        //Method called
        toggleIndicatorVisibility(holder.getAdapterPosition(), holder.selectionIndicator);
        itemClickFunctionality(holder.itemView, data, holder.getAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
    //endregion

    //region :ItemView holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_meme_layout)
        AppCompatImageView imgMemeLayout;
        @BindView(R.id.selection_indicator)
        View selectionIndicator;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }
    //endregion

    //region private methods


    /**
     * Method to toggle visibility of item selection indicator view.
     *
     * @param position      Position of item in list.
     * @param indicatorView Selection indicator view.
     */
    private void toggleIndicatorVisibility(int position, View indicatorView) {
        //update layout selection indicator
        if (mLayoutSelected == position) {
            indicatorView.setVisibility(View.VISIBLE);
        } else {
            indicatorView.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * ItemView click functionality.
     *
     * @param view     View wto be clicked.
     * @param data     MemeLayoutModel data.
     * @param position Position of item in the list.
     */
    private void itemClickFunctionality(View view, final MemeLayoutModel data, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set Listener
                listener.onMemeLayoutClick(data, position);
                //update flag and notify changes
                mLayoutSelected = position;
                notifyDataSetChanged();
            }
        });
    }

//endregion
}