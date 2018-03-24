package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ColorHelper;
import com.thetestament.cread.listeners.listener.OnColorSelectListener;
import com.thetestament.cread.models.ColorModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a color RecyclerView.
 */
public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ItemViewHolder> {

    private List<ColorModel> mColorDataList;
    private FragmentActivity mContext;
    private OnColorSelectListener listener;

    /**
     * Required constructor.
     *
     * @param colorDataList List of color data.
     * @param mContext      Context to be use.
     */
    public ColorAdapter(List<ColorModel> colorDataList, FragmentActivity mContext) {
        this.mColorDataList = colorDataList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user selects color.
     */
    public void setColorSelectListener(OnColorSelectListener listener) {
        this.listener = listener;
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_color, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        final ColorModel data = mColorDataList.get(position);
        //Set color
        holder.colorView.setColorFilter(ColorHelper.getColorValue(data.getColorValue(), mContext));
        //ItemView click functionality
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set Listener
                listener.onColorSelected(ColorHelper.getColorValue(data.getColorValue(), mContext), position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mColorDataList.size();
    }

    //Item view holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.view)
        CircleImageView colorView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }
}
