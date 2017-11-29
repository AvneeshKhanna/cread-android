package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thetestament.cread.R;
import com.thetestament.cread.models.FontModel;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Font RecyclerView.
 */
public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ItemViewHolder> {

    private List<FontModel> mFontDataList;
    private FragmentActivity mContext;

    /**
     * Required constructor.
     *
     * @param mFontDataList List of font data.
     * @param mContext      Context to be use.
     */
    public FontAdapter(List<FontModel> mFontDataList, FragmentActivity mContext) {
        this.mFontDataList = mFontDataList;
        this.mContext = mContext;
    }

    /**
     * Required constructor.
     *
     * @param mContext Context to be use.
     */
    public FontAdapter(FragmentActivity mContext) {
        this.mContext = mContext;
    }

    @Override
    public FontAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_font, parent, false));
    }

    @Override
    public void onBindViewHolder(FontAdapter.ItemViewHolder holder, int position) {
        // FontModel data = mFontDataList.get(position);
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    //Item view holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }
}
