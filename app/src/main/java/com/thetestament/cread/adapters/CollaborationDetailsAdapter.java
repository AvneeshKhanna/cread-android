package com.thetestament.cread.adapters;

import android.content.Intent;
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
import com.thetestament.cread.listeners.listener.OnCollaborationDetailsLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnCollaborationItemClickedListener;
import com.thetestament.cread.models.CollaborationDetailsModel;
import com.thetestament.cread.widgets.SquareView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;

/**
 * Adapter class to provide a binding from data set to views that are displayed within collaboration details RecyclerView.
 */
public class CollaborationDetailsAdapter extends RecyclerView.Adapter<CollaborationDetailsAdapter.ItemViewHolder> {

    private List<CollaborationDetailsModel> mCollaborationList;
    private FragmentActivity mContext;
    private boolean mIsLoading;

    private OnCollaborationDetailsLoadMoreListener loadMoreListener;
    private OnCollaborationItemClickedListener onCollaborationItemClickedListener;

    /**
     * Required constructor.
     *
     * @param mCollaborationList List of collaboration details data.
     * @param mContext           Context to use.
     */

    public CollaborationDetailsAdapter(List<CollaborationDetailsModel> mCollaborationList, FragmentActivity mContext) {
        this.mCollaborationList = mCollaborationList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setLoadMoreListener(OnCollaborationDetailsLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }


    public void setCollaborationitemClickedListener(OnCollaborationItemClickedListener onCollaborationItemClickedListener) {
        this.onCollaborationItemClickedListener = onCollaborationItemClickedListener;
    }

    @Override
    public CollaborationDetailsAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_collaboration_details, parent, false));
    }

    @Override
    public void onBindViewHolder(CollaborationDetailsAdapter.ItemViewHolder holder, int position) {
        CollaborationDetailsModel data = mCollaborationList.get(position);
        //Set creator name
        holder.textCreatorName.setText(data.getUserName());
        //Load creator image
        loadCreatorPic(data.getProfilePic(), holder.imageCreator);
        //Load content image
        loadContentImage(data.getEntityUrl(), holder.imageCollaboration);
        //Click functionality to launch profile of creator
        openCreatorProfile(holder.containerCreator, data.getUuid());

        // init click listener
        initItemClick(holder.entityImage, data.getEntityID());

        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mCollaborationList.size() - 1 && !mIsLoading) {
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
        return mCollaborationList.size();
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
     * @param picUrl    Picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadCreatorPic(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to load content image.
     *
     * @param imageUrl  picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadContentImage(String imageUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(imageUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView);
    }


    private void initItemClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onCollaborationItemClickedListener.onItemClicked(entityID);
            }
        });
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

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.containerCreator)
        RelativeLayout containerCreator;
        @BindView(R.id.imageCreator)
        CircleImageView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.imageCollaboration)
        ImageView imageCollaboration;
        @BindView(R.id.imageContainer)
        SquareView entityImage;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
