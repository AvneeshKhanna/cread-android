package com.thetestament.cread.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.gaurav.gesto.OnGestureListener;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.HatsOffHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnCollaborationDetailsLoadMoreListener;
import com.thetestament.cread.listeners.listener.OnCollaborationItemClickedListener;
import com.thetestament.cread.models.CollaborationDetailsModel;
import com.thetestament.cread.utils.AspectRatioUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within collaboration details RecyclerView.
 */
public class CollaborationDetailsAdapter extends RecyclerView.Adapter<CollaborationDetailsAdapter.ItemViewHolder> {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

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


    public void setCollaborationItemClickedListener(OnCollaborationItemClickedListener onCollaborationItemClickedListener) {
        this.onCollaborationItemClickedListener = onCollaborationItemClickedListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.item_collaboration_details
                        , parent
                        , false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        CollaborationDetailsModel data = mCollaborationList.get(position);
        //Set creator name
        holder.textCreatorName.setText(data.getUserName());
        //Load creator image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getProfilePic()), holder.imageCreator);

        //Set image width and height
        AspectRatioUtils.setImageAspectRatio(data.getImgWidth()
                , data.getImgHeight()
                , holder.imageCollaboration
                , true);
        //Load content image
        ImageHelper.loadProgressiveImage(Uri.parse(data.getEntityUrl())
                , holder.imageCollaboration);
        //Click functionality to launch profile of creator
        openCreatorProfile(holder.containerCreator, data.getUuid());

        //Double tap and click functionality
        setDoubleTap(holder, holder.hatsOffView, data);

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
     * Method to open creator profile.
     *
     * @param view        View to be clicked.
     * @param creatorUUID UUID of the creator.
     */
    private void openCreatorProfile(View view, final String creatorUUID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Method called
                IntentHelper.openProfileActivity(mContext, creatorUUID);
            }
        });
    }


    /**
     * Method to set double tap listener.
     *
     * @param holder      View to double tapped.
     * @param hatsOffView ImageView to be updated.
     * @param data        FeedModel data.
     */
    private void setDoubleTap(final RecyclerView.ViewHolder holder, final AppCompatImageView hatsOffView, final CollaborationDetailsModel data) {
        holder.itemView.setOnTouchListener(new OnGestureListener(mContext) {
            @Override
            public void onDoubleClick() {
                //region :Code to update hatsOff status
                //if device is connected  to internet
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    HatsOffHelper helper = new HatsOffHelper(mContext);
                    helper.setOnHatsOffSuccessListener(new HatsOffHelper.OnHatsOffSuccessListener() {
                        @Override
                        public void onSuccess() {
                            //do nothing
                        }
                    });
                    helper.updateHatsOffStatus(data.getEntityID(), true);
                    helper.setOnHatsOffFailureListener(new HatsOffHelper.OnHatsOffFailureListener() {
                        @Override
                        public void onFailure(String errorMsg) {
                            //Do nothing
                        }
                    });
                } else {
                    ViewHelper.getShortToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }
                //endregion

                //region :Animation code starts here
                hatsOffView.setVisibility(View.VISIBLE);
                hatsOffView.setScaleY(0.1f);
                hatsOffView.setScaleX(0.1f);
                AnimatorSet animatorSet = new AnimatorSet();

                ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 0.1f, 1f);
                imgScaleUpYAnim.setDuration(300);
                imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 0.1f, 1f);
                imgScaleUpXAnim.setDuration(300);
                imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

                ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleY", 1f, 0f);
                imgScaleDownYAnim.setDuration(300);
                imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
                ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(hatsOffView, "scaleX", 1f, 0f);
                imgScaleDownXAnim.setDuration(300);
                imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                animatorSet.playTogether(imgScaleUpYAnim, imgScaleUpXAnim);
                animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hatsOffView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                //endregion animation code end here
            }

            @Override
            public void onClick() {
                //ItemView onClick functionality
                onCollaborationItemClickedListener.onItemClicked(data.getEntityID());
            }
        });
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container_creator)
        RelativeLayout containerCreator;
        @BindView(R.id.image_creator)
        SimpleDraweeView imageCreator;
        @BindView(R.id.text_creator_name)
        AppCompatTextView textCreatorName;
        @BindView(R.id.image_collaboration)
        SimpleDraweeView imageCollaboration;
        @BindView(R.id.image_container)
        FrameLayout imageContainer;
        @BindView(R.id.hats_off_view)
        AppCompatImageView hatsOffView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
