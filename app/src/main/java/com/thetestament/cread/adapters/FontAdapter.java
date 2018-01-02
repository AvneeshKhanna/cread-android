package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener.OnFontClickListener;
import com.thetestament.cread.models.FontModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.thetestament.cread.helpers.FontsHelper.getFontType;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Font RecyclerView.
 */
public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ItemViewHolder> {

    private List<FontModel> mFontDataList;
    private FragmentActivity mContext;
    private OnFontClickListener onFontClickListener;

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
     * Register a callback to be invoked when user selects font.
     */
    public void setOnFontClickListener(OnFontClickListener onFontClickListener) {
        this.onFontClickListener = onFontClickListener;
    }

    @Override
    public FontAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_font, parent, false));
    }

    @Override
    public void onBindViewHolder(FontAdapter.ItemViewHolder holder, int position) {
        final FontModel data = mFontDataList.get(position);
        //Set typeface
        holder.textFont.setTypeface(getFontType(data.getFontName(), mContext));

        //ItemView click functionality
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set Listener
                onFontClickListener.onFontClick(getFontType(data.getFontName(), mContext), data.getFontName());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFontDataList.size();
    }

    //Item view holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textFont)
        TextView textFont;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }
}