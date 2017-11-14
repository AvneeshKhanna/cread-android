package com.thetestament.cread.adapters;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.listeners.listener.OnInspirationLoadMoreListener;
import com.thetestament.cread.models.InspirationModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a inspiration RecyclerView.
 */

public class InspirationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<InspirationModel> mInspirationList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnInspirationLoadMoreListener loadMoreListener;

    /**
     * Required constructor.
     *
     * @param mInspirationList List of inspiration data.
     * @param mContext         Context to be use.
     */
    public InspirationAdapter(List<InspirationModel> mInspirationList, FragmentActivity mContext) {
        this.mInspirationList = mInspirationList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(OnInspirationLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mInspirationList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_inspiration, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        InspirationModel data = mInspirationList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Load creator profile picture
            loadCreatorPic(data.getCreatorProfilePic(), itemViewHolder.imageCreator);
            //Set creator name
            itemViewHolder.textCreatorName.setText(data.getCreatorName());
            //Load inspiration image
            loadInspirationImage(data.getCapturePic(), itemViewHolder.imageInspiration);

            //Click functionality to launch profile of creator
            openCreatorProfile(itemViewHolder.containerCreator, data.getUUID());

            //ItemView onClick functionality
            itemViewOnClick(itemViewHolder.itemView, data);


        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }


        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mInspirationList.size() - 1 && !mIsLoading) {
            if (loadMoreListener != null) {
                //Lode more data here
                loadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }

    }

    @Override
    public int getItemCount() {
        return mInspirationList == null ? 0 : mInspirationList.size();
    }


    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to load creator profile picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadCreatorPic(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to load inspiration image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadInspirationImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);
    }

    /**
     * Method to open creator profile.
     *
     * @param view        View to be clicked.
     * @param creatorUUID UUID of the creator.
     */
    private void openCreatorProfile(View view, final String creatorUUID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(EXTRA_PROFILE_UUID, creatorUUID);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * ItemView click functionality.
     */
    private void itemViewOnClick(View view, final InspirationModel data) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_CAPTURE_ID, data.getCaptureID());
                bundle.putString(EXTRA_CAPTURE_URL, data.getCapturePic());
                bundle.putBoolean(EXTRA_MERCHANTABLE, data.isMerchantable());
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DATA, bundle);
                mContext.setResult(Activity.RESULT_OK, intent);
                //finis this activity
                mContext.finish();
            }
        });
    }


    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        CircleImageView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageInspiration)
        ImageView imageInspiration;

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

}
