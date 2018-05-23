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
import com.thetestament.cread.models.ExploreCategoryModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a ExploreCategory RecyclerView.
 */
public class ExploreCategoryAdapter extends RecyclerView.Adapter<ExploreCategoryAdapter.ItemViewHolder> {

    private List<ExploreCategoryModel> mDataList;
    private FragmentActivity mContext;

    /**
     * Flag to maintain category ID of currently selected item in the list.
     */
    private String mSelectedItemID ;


    private listener.OnCategorySelectListener categorySelectListener;

    /**
     * Required constructor.
     *
     * @param dataList List of category data.
     * @param context  Context to be use.
     */
    public ExploreCategoryAdapter(List<ExploreCategoryModel> dataList, FragmentActivity context, String selectedItemID) {
        this.mDataList = dataList;
        this.mContext = context;
        this.mSelectedItemID = selectedItemID;
    }

    /**
     * Register a callback to be invoked when user select label from list.
     */
    public void setCategorySelectListener(listener.OnCategorySelectListener categorySelectListener) {
        this.categorySelectListener = categorySelectListener;
    }


    @Override
    public ExploreCategoryAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExploreCategoryAdapter.ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_explore_category
                        , parent, false));
    }

    @Override
    public void onBindViewHolder(ExploreCategoryAdapter.ItemViewHolder holder, int position) {
        ExploreCategoryModel data = mDataList.get(position);

        //Set category text
        holder.textCategory.setText(data.getCategoryText());
        //Initialize itemClick
        itemViewOnClick(holder.itemView, data, holder.getAdapterPosition());
        //Toggle category appearance
        toggleCategoryAppearance(holder.getAdapterPosition()
                , mContext
                , holder.textCategory);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textCategory)
        AppCompatTextView textCategory;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * ItemView on click functionality .
     *
     * @param view         View to be clicked
     * @param data         Data for the item.
     * @param itemPosition Item position in list.
     */
    private void itemViewOnClick(View view, final ExploreCategoryModel data, final int itemPosition) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set listener if its not null
                if (categorySelectListener != null) {
                    //set listener
                    categorySelectListener.onCategorySelected(data, itemPosition);
                }
                //Update selection and notify changes
                mSelectedItemID = data.getCategoryID();
                notifyDataSetChanged();
            }
        });

    }

    /**
     * Method to toggle category text color and background.
     *
     * @param itemPosition Position of item.
     * @param context      Context to use.
     * @param textView     Category text view
     */
    private void toggleCategoryAppearance(int itemPosition, Context context, AppCompatTextView textView) {
        if (mSelectedItemID.equals(mDataList.get(itemPosition).getCategoryID())) {
            //Change background
            ViewCompat.setBackground(textView
                    , ContextCompat.getDrawable(context
                            , R.drawable.chips_bg_filled_blue));
            //Change text color
            textView.setTextColor(ContextCompat.getColor(context
                    , R.color.white));
        } else {
            //Change background
            ViewCompat.setBackground(textView
                    , ContextCompat.getDrawable(context
                            , R.drawable.chips_bg_outline_blue));
            //Change text color
            textView.setTextColor(ContextCompat.getColor(context
                    , R.color.grey_dark));
        }
    }


    /**
     * Method to return ID of selected item.
     */
    public String getSelectedItemID() {
        return mSelectedItemID;
        //return mDataList.get(mSelectedItemID).getCategoryID();
    }

    /**
     * Method to return ID of selected item.
     */
    public int getSelectedItemIDPosition() {
        return mDataList.indexOf(mSelectedItemID);
        //return mDataList.get(mSelectedItemID).getCategoryID();
    }
}
