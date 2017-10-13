package com.thetestament.cread.listeners;

import com.thetestament.cread.models.ExploreModel;
import com.thetestament.cread.models.FeedModel;

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
        void onHatsOffClick(FeedModel shareData, boolean hatsOffStatus);
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
    public interface OnExploreLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user clicks on follow button.
     */
    public interface OnExploreFollowListener {
        void onFollowClick(ExploreModel exploreData, boolean followStatus);
    }
}
