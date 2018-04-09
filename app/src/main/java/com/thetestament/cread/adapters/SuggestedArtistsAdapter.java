package com.thetestament.cread.adapters;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.models.SuggestedArtistsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_USER_PROFILE_FROM_FEED;


/**
 * Adapter class to provide a binding from data set to views that are displayed within a SuggestedArtists RecyclerView.
 */
public class SuggestedArtistsAdapter extends RecyclerView.Adapter<SuggestedArtistsAdapter.ItemViewHolder> {

    private List<SuggestedArtistsModel> mDataList;
    private FragmentActivity mContext;
    private Fragment mFeedFragment;
    private boolean mStartActivityForResult;

    /**
     * Required constructor.
     *
     * @param dataList List of SuggestedArtistModel
     * @param context  Context to use.
     */
    public SuggestedArtistsAdapter(List<SuggestedArtistsModel> dataList, FragmentActivity context, Fragment feedFragment, boolean startActivityResult) {
        this.mDataList = dataList;
        this.mContext = context;
        this.mFeedFragment = feedFragment;
        this.mStartActivityForResult = startActivityResult;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate this view
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_suggested_artists, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        SuggestedArtistsModel data = mDataList.get(position);
        //Set artist name
        holder.textArtistName.setText(data.getArtistName());
        //Load artist profile picture
        ImageHelper.loadImageFromPicasso(mContext
                , holder.imageArtist
                , data.getArtistProfilePic()
                , R.drawable.ic_account_circle_100);
        //Method called
        itemViewOnClick(holder.itemView, data.getArtistUUID());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageArtist)
        CircleImageView imageArtist;
        @BindView(R.id.textArtistName)
        TextView textArtistName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * ItemView  click functionality.
     *
     * @param itemView View to be clicked
     * @param UUID     UUID of artist.
     */
    private void itemViewOnClick(View itemView, final String UUID) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(EXTRA_PROFILE_UUID, UUID);
                if (mStartActivityForResult) {
                    mFeedFragment.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE_FROM_FEED);
                } else {
                    mContext.startActivity(intent);
                }

            }
        });
    }


}
