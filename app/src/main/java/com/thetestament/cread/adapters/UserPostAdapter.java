package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.models.UserPostModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a user post RecyclerView.
 */
public class UserPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UserPostModel> mDataList;
    private FragmentActivity mContext;

    /**
     * Required constructor.
     *
     * @param dataList List of User Post data.
     * @param context  Context to use.
     */
    public UserPostAdapter(List<UserPostModel> dataList, FragmentActivity context) {
        this.mDataList = dataList;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_user_posts, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserPostModel data = mDataList.get(position);

        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        //Load post image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getPostURL())
                , itemViewHolder.imageViewUserPost);

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgUserPost)
        SimpleDraweeView imageViewUserPost;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}