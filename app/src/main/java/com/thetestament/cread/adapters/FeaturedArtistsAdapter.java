package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.listeners.listener.OnFeatArtistClickedListener;
import com.thetestament.cread.models.FeaturedArtistsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by prakharchandna on 26/02/18.
 */

public class FeaturedArtistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private FragmentActivity mContext;
    private List<FeaturedArtistsModel> mArtistList;
    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    private OnFeatArtistClickedListener mClickListener;

    public FeaturedArtistsAdapter(FragmentActivity mContext, List<FeaturedArtistsModel> mArtistList) {
        this.mContext = mContext;
        this.mArtistList = mArtistList;
    }

    public void setFeatArtistClickListener(OnFeatArtistClickedListener mClickListener) {
        this.mClickListener = mClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_featured_artists, parent, false));
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.header_featured_artists, parent, false));
        }

        return null;

    }


    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {   // get data
            FeaturedArtistsModel data = mArtistList.get(position - 1);

            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            //load artist image
            ImageHelper.loadProgressiveImage(Uri.parse(data.getImageUrl())
                    , itemViewHolder.imageArtist);
            // set name
            itemViewHolder.textArtistName.setText(data.getName());
            // init click
            initItemClick(holder, data.getUuid());
        } else if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            HeaderViewHolder header = (HeaderViewHolder) holder;
            // init shared prefs
            SharedPreferenceHelper spHelper = new SharedPreferenceHelper(mContext);
            // set text
            header.textArtistName.setText("Me");
            // load user pic
            ImageHelper.loadProgressiveImage(Uri.parse(ImageHelper.getAWSS3ProfilePicUrl(spHelper.getUUID()))
                    , header.imageArtist);
            // init click
            initItemClick(holder, null);
        }


    }

    @Override
    public int getItemCount() {
        return mArtistList.size() + 1;
    }




    private void initItemClick(final RecyclerView.ViewHolder holder, final String uuid) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mClickListener.onFeatArtistClicked(holder.getItemViewType(), uuid);
            }
        });
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageArtist)
        SimpleDraweeView imageArtist;
        @BindView(R.id.textArtistName)
        TextView textArtistName;

        public ItemViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageArtist)
        SimpleDraweeView imageArtist;
        @BindView(R.id.textArtistName)
        TextView textArtistName;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }


}
