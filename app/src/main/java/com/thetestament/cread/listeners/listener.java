package com.thetestament.cread.listeners;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.thetestament.cread.models.CommentsModel;
import com.thetestament.cread.models.FBFriendsModel;
import com.thetestament.cread.models.FeedModel;
import com.thetestament.cread.models.InspirationModel;
import com.thetestament.cread.models.PersonMentionModel;
import com.thetestament.cread.models.UpdatesModel;
import com.thetestament.cread.utils.Constant.GratitudeNumbers;

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
     * Interface definition for a callback to be invoked when user clicks on edit button.
     */
    public interface OnLoadMoreClickedListener {

        void onLoadMoreClicked();
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
    public interface OnFollowRequestedListener {
        void onFollowSuccess();

        void onFollowFailiure(String errorMsg);
    }

    /**
     * Interface definition for a callback to be invoked when user clicks on downvote button.
     */
    public interface OnDownvoteRequestedListener {
        void onDownvoteSuccess();

        void onDownvoteFailiure(String errorMsg);
    }


    /**
     * Interface definition for a callback to be invoked when user clicks on coolab invite button.
     */
    public interface OnDeepLinkRequestedListener {
        void onDeepLinkSuccess(String deepLink, MaterialDialog dialog);

        void onDeepLinkFailiure(String errorMsg, MaterialDialog dialog);
    }

    public interface OnDeleteRequestedListener {

        void onDeleteSuccess();

        void onDeleteFailure(String errorMsg);
    }

    public interface OnFollowFriendsClickedListener {
        void onFollowClicked(int position, FBFriendsModel fbFriendsModel);
    }

    public interface OnBuyButtonClickedListener {

        void onBuyButtonClicked(String type, String size, String color, String quantity, String price, String productID, String deliveryCharge);
    }


    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnUserActivityLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when a 'Hats off' is clicked.
     */
    public interface OnUserActivityHatsOffListener {
        /**
         * Called when hats off has been clicked.
         */
        void onHatsOffClick(FeedModel data, int itemPosition);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnInspirationLoadMoreListener {
        void onLoadMore();
    }


    /**
     * Interface definition for a callback to be invoked when user clicks on delete button of his/her content.
     */
    public interface OnContentDeleteListener {
        void onDelete(String entityID, int position);
    }

    /**
     * Interface definition for a callback to be invoked when user clicks on edit button of his/her content.
     */
    public interface OnContentEditListener {
        void onEdit(FeedModel data, int position);
    }


    /**
     * Interface definition for callback to be invoked when user clicks on capture button for collaboration(ExploreFragment).
     */
    public interface OnExploreCaptureClickListener {
        void onClick(String shortId);
    }


    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnCollaborationDetailsLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnRoyaltiesLoadMoreListener {
        void onLoadMore();
    }


    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface onSuggestionsLoadMore {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface onNotificationsLoadMore {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when a notification is clicked.
     */
    public interface NotificationItemClick {
        void onNotificationClick(UpdatesModel updatesModel, int position);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnServerRequestedListener<T> {
        void onDeviceOffline();

        void onNextCalled(T jsonObject);

        void onErrorCalled(Throwable e);

        void onCompleteCalled();
    }

    public interface OnRoyaltyitemClickedListener {
        void onRoyaltyItemClicked(String entityID);
    }

    public interface OnPeopleSuggestionsClick {
        void onPeopleSuggestionsClick(PersonMentionModel person);
    }


    /**
     * Interface definition for a callback to be invoked when user selects font from bottom sheet.
     */
    public interface OnFontClickListener {
        /**
         * @param typeface Selected typeface
         */
        void onFontClick(Typeface typeface, String fontType, int itemPosition);
    }

    /**
     * Interface definition for a callback to be invoked when user click on share button.
     */
    public interface OnShareListener {
        void onShareClick(Bitmap bitmap, FeedModel model);
    }

    public interface OnShareDialogItemClickedListener {
        void onShareDialogItemClicked(int index);
    }

    public interface OnShareLinkClickedListener {
        void onShareLinkClicked(String entityID, String entityURL, String creatorName);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnSearchLoadMoreListener {
        void onLoadMore();
    }

    public interface OnUserStatsClickedListener {
        void onUserStatsClicked(GratitudeNumbers gratitudeNumbers, LinearLayout view);
    }


    public interface OnCollaborationItemClickedListener {
        void onItemClicked(String entityID);
    }

    /**
     * Interface definition for callback to be invoked when user clicks on capture button for collaboration.
     */
    public interface OnCollaborationListener {
        void collaborationOnGraphic();

        /**
         * @param entityID   entity ID of content.
         * @param entityType entity type of content.
         */
        void collaborationOnWriting(String entityID, String entityType);
    }

    /**
     * Interface definition for a callback to be invoked when user selects color from bottom sheet.
     */
    public interface OnColorSelectListener {
        void onColorSelected(int selectedColor, int itemPosition);
    }

    /**
     * Interface definition for a callback to be invoked when user selects filter from bottom sheet.
     */
    public interface OnFilterSelectListener {
        void onFilterSelected(Bitmap bitmap, String filterName);
    }

    /**
     * Interface definition for a callback to be invoked when user selects image from inspiration list.
     */
    public interface OnInspirationSelectListener {
        void onInspireImageSelected(InspirationModel model , int itemPosition);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnChatListLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnChatDetailsLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when user clicks on delete button.
     */
    public interface OnChatDeleteListener {
        /**
         * @param index index of the comment to be deleted.
         */
        void onDelete(int index);
    }

    /**
     * Interface definition for a callback to be invoked when user scroll for more data.
     */
    public interface OnChatRequestLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Interface definition for a callback to be invoked when featured artist is clicked.
     */
    public interface OnFeatArtistClickedListener {
        void onFeatArtistClicked(int itemType, String uuid);
    }

    public interface OnDownvoteClickedListener {
        void onDownvoteClicked(FeedModel data, int position, ImageView imageDownvote);
    }

    /**
     * Interface definition for a callback to be invoked when user selects template from bottom sheet.
     */
    public interface OnTemplateClickListener {
        /**
         * @param templateName Name of template
         * @param itemPosition Position of item in list
         **/
        void onTemplateClick(String templateName, int itemPosition);
    }
}
