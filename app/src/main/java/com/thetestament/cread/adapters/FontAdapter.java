package com.thetestament.cread.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
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

import static com.thetestament.cread.utils.Constant.FONT_TYPE_AMATIC_SC_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_BARLOW_CONDENSED_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_CABIN_SKETCH_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_HELVETICA_NEUE_MEDUIM;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_INDIE_FLOWER;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_MOSTSERRAT_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_OSWALD_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_PLAYFAIR_DISPLAY_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_POIRET_ONE_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_SHADOWS_INTO_LIGHT;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_SPECTRA_ISC_REGULAR;
import static com.thetestament.cread.utils.Constant.FONT_TYPE_TITILLIUM_WEB_REGULAR;

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
                onFontClickListener.onFontClick(getFontType(data.getFontName(), mContext));
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

    /**
     * Method to return typeFace
     *
     * @param context  Context to use.
     * @param fontType Font type.
     */
    private Typeface getFontType(String fontType, Context context) {
        switch (fontType) {
            case FONT_TYPE_AMATIC_SC_REGULAR:
                return ResourcesCompat.getFont(context, R.font.amatic_sc_regular);
            case FONT_TYPE_BARLOW_CONDENSED_REGULAR:
                return ResourcesCompat.getFont(context, R.font.barlow_condensed_regular);
            case FONT_TYPE_CABIN_SKETCH_REGULAR:
                return ResourcesCompat.getFont(context, R.font.cabin_sketch_regular);
            case FONT_TYPE_HELVETICA_NEUE_MEDUIM:
                return ResourcesCompat.getFont(context, R.font.helvetica_neue_medium);
            case FONT_TYPE_INDIE_FLOWER:
                return ResourcesCompat.getFont(context, R.font.indie_flower);
            case FONT_TYPE_MOSTSERRAT_REGULAR:
                return ResourcesCompat.getFont(context, R.font.montserrat_regular);
            case FONT_TYPE_OSWALD_REGULAR:
                return ResourcesCompat.getFont(context, R.font.oswald_regular);
            case FONT_TYPE_PLAYFAIR_DISPLAY_REGULAR:
                return ResourcesCompat.getFont(context, R.font.playfair_display_regular);
            case FONT_TYPE_POIRET_ONE_REGULAR:
                return ResourcesCompat.getFont(context, R.font.poiret_one_regular);
            case FONT_TYPE_SHADOWS_INTO_LIGHT:
                return ResourcesCompat.getFont(context, R.font.shadows_into_light);
            case FONT_TYPE_SPECTRA_ISC_REGULAR:
                return ResourcesCompat.getFont(context, R.font.spectra_isc_regular);
            case FONT_TYPE_TITILLIUM_WEB_REGULAR:
                return ResourcesCompat.getFont(context, R.font.titillium_web_regular);
            default:
                return ResourcesCompat.getFont(context, R.font.helvetica_neue_medium);
        }
    }
}
