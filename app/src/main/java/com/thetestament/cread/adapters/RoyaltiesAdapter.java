package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnRoyaltiesLoadMoreListener;
import com.thetestament.cread.models.RoyaltiesModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.thetestament.cread.utils.TimeUtils.getCustomTime;

public class RoyaltiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<RoyaltiesModel> mFeedList;
    private FragmentActivity mContext;
    private SharedPreferenceHelper mHelper;
    private boolean mIsLoading;
    private OnRoyaltiesLoadMoreListener onRoyaltiesLoadMoreListener;
    private listener.OnRoyaltyitemClickedListener onRoyaltyitemClickedListener;

    /**
     * Required Constructor
     *
     * @param mFeedList data list
     * @param mContext  Context
     */
    public RoyaltiesAdapter(List<RoyaltiesModel> mFeedList, FragmentActivity mContext) {
        this.mFeedList = mFeedList;
        this.mContext = mContext;

        mHelper = new SharedPreferenceHelper(mContext);
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnRoyaltiesLoadMoreListener(OnRoyaltiesLoadMoreListener onRoyaltiesLoadMoreListener) {
        this.onRoyaltiesLoadMoreListener = onRoyaltiesLoadMoreListener;
    }

    /**
     * Register a callback to be invoked when user clicks on a item.
     */
    public void setOnRoyaltyitemClicked(listener.OnRoyaltyitemClickedListener onRoyaltyitemClickedListener)
    {
        this.onRoyaltyitemClickedListener = onRoyaltyitemClickedListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_royalty, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return mFeedList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final RoyaltiesModel data = mFeedList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            // set art type
            String artType = data.getType().equals("CAPTURE") ? "graphic art" : "writing";

            // set text
            itemViewHolder.textDesc.setText(data.getName()
                                            + " placed an order for a "
                                            + data.getProductType().toLowerCase()
                                            + " made using your "
                                            + artType
                                            + " (x"
                                            + String.valueOf(data.getQuantity())
                                            + ")"
                                            );
            // set visibility of redeemed text
            if(data.isRedeemStatus())
            {
                itemViewHolder.redeemStatus.setVisibility(View.VISIBLE);
            }

            else
            {
                itemViewHolder.redeemStatus.setVisibility(View.INVISIBLE);
            }

            // set On click
            itemViewOnClick(itemViewHolder.itemView, data.getEntityID());

            // load entity image
            loadEntityImage(data.getEntityUrl(), itemViewHolder.imageEntity);

            // parsing server date
            List<String> dateList = getCustomTime(data.getRoyaltyDate());
            String timeStamp = dateList.get(1) + "/" + dateList.get(0);

            //set amount and date
            itemViewHolder.textAmountAndTime.setText("Royalty : " + mContext.getString(R.string.Rs) + " " + String.valueOf(data.getRoyaltyAmount()) + " " + mContext.getString(R.string.bullet) + " " + timeStamp);
        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }


        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mFeedList.size() - 1 && !mIsLoading) {
            if (onRoyaltiesLoadMoreListener != null) {
                //Lode more data here
                onRoyaltiesLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    @Override
    public int getItemCount() {
        return mFeedList == null ? 0 : mFeedList.size();
    }


    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_description)
        TextView textDesc;
        @BindView(R.id.img_entity)
        ImageView imageEntity;
        @BindView(R.id.text_timestamp)
        TextView textAmountAndTime;
        @BindView(R.id.redeemStatus)
        TextView redeemStatus;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    //LoadingViewHolder class
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.viewProgress)
        View progressView;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * ItemView onClick functionality.
     * @param  view
     * @param entityID
     */
    private void itemViewOnClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onRoyaltyitemClickedListener.onRoyaltyItemClicked(entityID);
            }
        });
    }

    /**
     * Method to load entity image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadEntityImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);
    }
}
