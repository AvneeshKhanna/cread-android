package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.ListItemsDialogModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShareDialogAdapter extends RecyclerView.Adapter<ShareDialogAdapter.ViewHolder> {

    private FragmentActivity mContext;
    listener.OnShareDialogItemClickedListener onShareDialogItemClickedListener;
    private List<ListItemsDialogModel> mDataList;

    public ShareDialogAdapter(FragmentActivity mContext, List<ListItemsDialogModel> mDataList) {
        this.mContext = mContext;
        this.mDataList = mDataList;
    }

    public void setShareDialogItemClickedListener(listener.OnShareDialogItemClickedListener onShareDialogItemClickedListener) {
        this.onShareDialogItemClickedListener = onShareDialogItemClickedListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_share_dialog, parent, false)) {
        };
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        switch (position) {
            /*case 0:
                holder.imageIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_image_24));
                holder.itemTitle.setText("Share As Image");
                holder.itemText.setText("Your friends would only be able to view this post as an image");
                break;
            case 1:
                holder.imageIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_link_24));
                holder.itemTitle.setText("Share As Link");
                holder.itemText.setText("Your friends would be redirected to this post on the Cread app");
                break;*/


        }

        ListItemsDialogModel data = mDataList.get(position);

        holder.imageIcon.setImageDrawable(ContextCompat.getDrawable(mContext, data.getDrawableResource()));
        holder.itemTitle.setText(data.getTitle());
        holder.itemText.setText(data.getContent());

        initItemClick(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.itemIcon)
        ImageView imageIcon;
        @BindView(R.id.itemText)
        TextView itemText;
        @BindView(R.id.itemTitle)
        TextView itemTitle;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    private void initItemClick(View view, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareDialogItemClickedListener.onShareDialogItemClicked(position);
            }
        });
    }

}


