package com.thetestament.cread.adapters;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.listeners.listener.OnCommentDeleteListener;
import com.thetestament.cread.listeners.listener.OnCommentEditListener;
import com.thetestament.cread.listeners.listener.OnCommentsLoadMoreListener;
import com.thetestament.cread.models.CommentsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Comments RecyclerView.
 */

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private List<CommentsModel> mCommentList;
    private FragmentActivity mContext;
    private boolean mIsLoading;


    private OnCommentsLoadMoreListener onLoadMoreListener;
    private OnCommentDeleteListener onDeleteListener;
    private OnCommentEditListener onEditListener;


    /**
     * Required constructor.
     *
     * @param mCommentList List of Comment data.
     * @param mContext     Context to be use.
     */
    public CommentsAdapter(List<CommentsModel> mCommentList, FragmentActivity mContext) {
        this.mCommentList = mCommentList;
        this.mContext = mContext;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnLoadMoreListener(OnCommentsLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
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

    @Override
    public int getItemViewType(int position) {
        return mCommentList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final CommentsModel data = mCommentList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            //Typecast viewHolder
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //set user name
            itemViewHolder.textUserName.setText(data.getFirstName() + " " + data.getLastName());
            //Set comment
            itemViewHolder.textComment.setText(data.getComment());
            //Load profile picture
            loadProfilePic(data.getProfilePicUrl(), itemViewHolder.imageUser);

            //If comment is edited
            if (data.isEdited()) {
                itemViewHolder.textEdited.setVisibility(View.VISIBLE);
            } else {
                itemViewHolder.textEdited.setVisibility(View.INVISIBLE);
            }

            //Check for user comments
            if (data.getUuid().equals(data.getCommentId())) {
                itemViewHolder.buttonMore.setVisibility(View.VISIBLE);
                //Long click functionality i.e edit and delete
                itemViewHolder.buttonMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Show dialog with options
                        getBottomSheetDialog(position, data.getCommentId(), data.getComment(), data, holder.itemView);
                    }
                });
            } else {
                //Do nothing
                itemViewHolder.buttonMore.setVisibility(View.GONE);
            }

            /*
            * OnClick functionality to expand collapse comments.
            * */
            itemViewHolder.textComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextViewCompat.getMaxLines(itemViewHolder.textComment) == 5) {
                        //Expand comment
                        itemViewHolder.textComment.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        //Collapse comment
                        itemViewHolder.textComment.setMaxLines(5);
                    }
                }
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            //Typecast viewHolder
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            //Show progress view
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mCommentList.size() - 1 && !mIsLoading) {
            if (onLoadMoreListener != null) {
                //Lode more data here
                onLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    @Override
    public int getItemCount() {
        return mCommentList == null ? 0 : mCommentList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to load  profile picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadProfilePic(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to show bottomSheet dialog with edit and delete option.
     */
    private void getBottomSheetDialog(final int index, final String commentId, final String commentText, final CommentsModel commentsModel, final View item) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
        View sheetView = mContext.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_comment, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        LinearLayout buttonEdit = (LinearLayout) sheetView.findViewById(R.id.button_edit);
        LinearLayout buttonDelete = (LinearLayout) sheetView.findViewById(R.id.button_delete);

        //Edit campaign functionality
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                onEditListener.onEdit(index, commentId, commentText, commentsModel);

            }
        });
        //Delete button functionality
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                showDeleteCommentDialog(index, commentId);
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

    //ViewHolder class for item
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageUser)
        CircleImageView imageUser;
        @BindView(R.id.textUserName)
        TextView textUserName;
        @BindView(R.id.textComment)
        TextView textComment;
        @BindView(R.id.textEdited)
        TextView textEdited;
        @BindView(R.id.buttonMore)
        ImageView buttonMore;

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
