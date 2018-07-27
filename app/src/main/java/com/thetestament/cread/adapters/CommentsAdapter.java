package com.thetestament.cread.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.helpers.IntentHelper;
import com.thetestament.cread.helpers.ProfileMentionsHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnCommentDeleteListener;
import com.thetestament.cread.listeners.listener.OnCommentEditListener;
import com.thetestament.cread.listeners.listener.OnLoadMoreClickedListener;
import com.thetestament.cread.models.CommentsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Comments RecyclerView.
 */

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private List<CommentsModel> mCommentList;
    private FragmentActivity mContext;
    private String mUUID;
    private boolean mShowMoreOption;


    private OnCommentDeleteListener onDeleteListener;
    private OnCommentEditListener onEditListener;
    private OnLoadMoreClickedListener onLoadMoreClickedListener;


    /**
     * Required constructor.
     *
     * @param mCommentList   List of Comment data.
     * @param mContext       Context to be use.
     * @param mUUID          UUID of the user.
     * @param showMoreOption If true then show more option.
     */
    public CommentsAdapter(List<CommentsModel> mCommentList, FragmentActivity mContext, String mUUID, boolean showMoreOption) {
        this.mCommentList = mCommentList;
        this.mContext = mContext;
        this.mUUID = mUUID;
        mShowMoreOption = showMoreOption;

    }


    /**
     * Register a callback to be invoked when user clicks on delete button.
     */
    public void setOnDeleteListener(OnCommentDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    /**
     * Register a callback to be invoked when user clicks on edit button.
     */
    public void setOnEditListener(OnCommentEditListener onEditListener) {
        this.onEditListener = onEditListener;
    }

    public void setOnViewLoadMoreListener(OnLoadMoreClickedListener onLoadMoreClickedListener) {
        this.onLoadMoreClickedListener = onLoadMoreClickedListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false));
        } else if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.header_comments, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ItemViewHolder) {
            final CommentsModel data = getItem(position);

            //Typecast viewHolder
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Set user name
            itemViewHolder.textUserName.setText(data.getFirstName() + " " + data.getLastName());
            //Set comment
            itemViewHolder.textComment.setText(data.getComment());
            // set profile mentions
            ProfileMentionsHelper.setProfileMentionsForViewing(data.getComment(), mContext, itemViewHolder.textComment);

            // set hash tags on comment
            FeedHelper feedHelper = new FeedHelper();
            feedHelper.setHashTags(itemViewHolder.textComment, mContext, R.color.blue_dark, -1);

            //Load profile picture
            ImageHelper.loadProgressiveImage(Uri.parse(data.getProfilePicUrl()), itemViewHolder.imageUser);

            //If comment is edited
            if (data.isEdited()) {
                itemViewHolder.textEdited.setVisibility(View.VISIBLE);
            } else {
                itemViewHolder.textEdited.setVisibility(View.INVISIBLE);
            }

            //Check for user comments
            if (mShowMoreOption) {
                if (data.getUuid().equals(mUUID)) {
                    itemViewHolder.buttonMore.setVisibility(View.VISIBLE);
                    //Long click functionality i.e edit and delete
                    itemViewHolder.buttonMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Show dialog with options
                            getBottomSheetDialog(itemViewHolder.getAdapterPosition(), data);
                        }
                    });
                }
            } else {
                //Hide more option
                itemViewHolder.buttonMore.setVisibility(View.INVISIBLE);
            }


            // Expand and collapse comments.
            toggleComment(itemViewHolder.textComment);
            //Open creator profile
            openCreatorProfile(itemViewHolder.textUserName, data.getUuid(), mContext);
            openCreatorProfile(itemViewHolder.imageUser, data.getUuid(), mContext);

            //If artist is top artist
            if (data.isTopArtist()) {
                //toggle visibility
                itemViewHolder.viewTopArtist.setVisibility(View.VISIBLE);
            } else {
                //toggle visibility
                itemViewHolder.viewTopArtist.setVisibility(View.GONE);
            }
            topArtistOnClick(itemViewHolder.viewTopArtist);
        } else if (holder instanceof HeaderViewHolder) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            // init header click functionality
            initLoadMoreViewClicked(headerViewHolder.loadMoreView);
        }
    }

    @Override
    public int getItemCount() {
        return mCommentList == null ? 0 : mCommentList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {

        if (isPositionHeader(position))
            return VIEW_TYPE_HEADER;

        return VIEW_TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private CommentsModel getItem(int position) {
        return mCommentList.get(position - 1);
    }

    public void setLoadMoreViewVisibility(HeaderViewHolder holder, int visibility) {
        holder.loadMoreView.setVisibility(visibility);
    }

    public void setLoadingIconVisibility(HeaderViewHolder holder, int visibility) {
        holder.loadMoreViewProgress.setVisibility(visibility);
    }

    private void initLoadMoreViewClicked(LinearLayout view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onLoadMoreClickedListener.onLoadMoreClicked();
            }
        });

    }


    /**
     * Method to expand and collapse comment text.
     *
     * @param textComment Comment textView.
     */
    public static void toggleComment(final TextView textComment) {
        textComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextViewCompat.getMaxLines(textComment) == 5) {
                    //Expand comment
                    textComment.setMaxLines(Integer.MAX_VALUE);
                } else {
                    //Collapse comment
                    textComment.setMaxLines(5);
                }
            }
        });
    }

    /**
     * Method to open creator profile.
     *
     * @param view        View to be clicked.
     * @param creatorUUID UUID of the creator.
     */
    public static void openCreatorProfile(View view, final String creatorUUID, final FragmentActivity context) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Method called
                IntentHelper.openProfileActivity(context, creatorUUID);
            }
        });
    }

    /**
     * Method to show bottomSheet dialog with edit and delete option.
     */
    private void getBottomSheetDialog(final int index, final CommentsModel commentsModel) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
        View sheetView = mContext.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_comment, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        LinearLayout buttonEdit = sheetView.findViewById(R.id.button_edit);
        LinearLayout buttonDelete = sheetView.findViewById(R.id.button_delete);

        //Edit campaign functionality
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                onEditListener.onEdit(index, commentsModel);

            }
        });
        //Delete button functionality
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                showDeleteCommentDialog(index, commentsModel.getCommentId());
            }
        });
    }

    /**
     * Method to show confirmation dialog for comment deletion.
     *
     * @param index     position of comment in adapter.
     * @param commentId Comment id i.e String
     */
    private void showDeleteCommentDialog(final int index, final String commentId) {
        new MaterialDialog.Builder(mContext)
                .content("Are you sure want to delete this comment?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        onDeleteListener.onDelete(index, commentId);
                        materialDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    /**
     * Top artist click functionality.
     *
     * @param view View to be clicked.
     */
    private void topArtistOnClick(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show tooltip
                ViewHelper.getToolTip(view
                        , mContext.getString(R.string.text_top_artist)
                        , mContext);
            }
        });
    }

    //ViewHolder class for item
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageUser)
        SimpleDraweeView imageUser;
        @BindView(R.id.textUserName)
        TextView textUserName;
        @BindView(R.id.textComment)
        TextView textComment;
        @BindView(R.id.textEdited)
        TextView textEdited;
        @BindView(R.id.buttonMore)
        ImageView buttonMore;
        @BindView(R.id.view_top_artist)
        AppCompatTextView viewTopArtist;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //ViewHolder class for header
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.loadMoreView)
        LinearLayout loadMoreView;
        @BindView(R.id.loadMoreViewProgress)
        View loadMoreViewProgress;

        public HeaderViewHolder(View headerView) {
            super(headerView);
            ButterKnife.bind(this, headerView);

        }
    }

}
