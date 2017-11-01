package com.thetestament.cread.listeners;

import com.thetestament.cread.models.CommentsModel;
import com.thetestament.cread.models.FBFriendsModel;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.FollowModel;

public class listener {

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnFeedLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when a 'Hats off' is clicked.
     */
    public interface OnHatsOffListener {
        /**
         * Called when hats off has been clicked.
         */
        void onHatsOffClick(FeedModel shareData, int itemPosition);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnHatsOffLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnFriendsLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnExploreLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user clicks on follow button.
     */
    public interface OnExploreFollowListener {
        void onFollowClick(FeedModel exploreData, int itemPosition);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnCommentsLoadMoreListener {
        void onLoadMore();
    }


    /**
     * Interface definition for a callback to be invoked when user clicks on delete button.
     */
    public interface OnCommentDeleteListener {
        /**
         * @param index     index of the comment to be deleted.
         * @param commentID Unique id for the comment.
         */
        void onDelete(int index, String commentID);
    }

    /**
     * Interface definition for a callback to be invoked when user clicks on edit button.
     */
    public interface OnCommentEditListener {
        /**
         * @param index index of the comment to be edited.
         */
        void onEdit(int index, CommentsModel commentsModel);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnFollowLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user clicks on follow button.
     */
    public interface OnFollowListener {
        void onFollowClick(FollowModel data, boolean followStatus);
    }

    public interface OnFollowFriendsClickedListener {
        void onFollowClicked(int position, FBFriendsModel fbFriendsModel);
    }

    public interface OnBuyButtonClickedListener{

        void onBuyButtonClicked(String type, String size, String color, String quantity, String price, String productID);
    }


}
