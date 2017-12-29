package com.thetestament.cread.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.CollaborationDetailsActivity;
import com.thetestament.cread.activities.CommentsActivity;
import com.thetestament.cread.activities.FeedDescriptionActivity;
import com.thetestament.cread.activities.ShortActivity;
import com.thetestament.cread.helpers.FeedHelper;
import com.thetestament.cread.helpers.NetworkHelper;
import com.thetestament.cread.helpers.SharedPreferenceHelper;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnContentDeleteListener;
import com.thetestament.cread.listeners.listener.OnMeCaptureClickListener;
import com.thetestament.cread.listeners.listener.OnShareLinkClickedListener;
import com.thetestament.cread.listeners.listener.OnUserActivityHatsOffListener;
import com.thetestament.cread.listeners.listener.OnUserActivityLoadMoreListener;
import com.thetestament.cread.models.FeedModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.helpers.FeedHelper.collabOnCollab;
import static com.thetestament.cread.helpers.FeedHelper.getCollabCountText;
import static com.thetestament.cread.helpers.FeedHelper.getCreatorText;
import static com.thetestament.cread.helpers.FeedHelper.initializeShareDialog;
import static com.thetestament.cread.helpers.FeedHelper.initializeSpannableString;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_CAPTURE;
import static com.thetestament.cread.utils.Constant.CONTENT_TYPE_SHORT;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_CAPTURE_URL;
import static com.thetestament.cread.utils.Constant.EXTRA_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_ID;
import static com.thetestament.cread.utils.Constant.EXTRA_ENTITY_TYPE;
import static com.thetestament.cread.utils.Constant.EXTRA_FEED_DESCRIPTION_DATA;
import static com.thetestament.cread.utils.Constant.EXTRA_MERCHANTABLE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_CAPTURE_CLICKED;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_SHARED_FROM_PROFILE;
import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_WRITE_CLICKED;
import static com.thetestament.cread.utils.Constant.REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a Me RecyclerView.
 */
public class MeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private List<FeedModel> mUserContentList;
    private FragmentActivity mContext;
    private Fragment mMeFragment;
    private boolean mIsLoading;
    private String mUUID;
    private SharedPreferenceHelper mHelper;

    private OnUserActivityLoadMoreListener onLoadMore;
    private OnUserActivityHatsOffListener onHatsOffListener;
    private OnContentDeleteListener onContentDeleteListener;
    private OnMeCaptureClickListener onMeCaptureClickListener;
    private listener.OnShareListener onShareListener;
    private OnShareLinkClickedListener onShareLinkClickedListener;

    /**
     * Required constructor.
     *
     * @param mUserContentList List of feed data.
     * @param mContext         Context to be use.
     * @param mUUID            UUID of user.
     */
    public MeAdapter(List<FeedModel> mUserContentList, FragmentActivity mContext, String mUUID, Fragment mMeFragment) {
        this.mUserContentList = mUserContentList;
        this.mContext = mContext;
        this.mUUID = mUUID;
        this.mMeFragment = mMeFragment;

        mHelper = new SharedPreferenceHelper(mContext);
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setUserActivityLoadMoreListener(OnUserActivityLoadMoreListener onLoadMore) {
        this.onLoadMore = onLoadMore;
    }

    /**
     * Register a callback to be invoked when hats off is clicked.
     */
    public void setHatsOffListener(OnUserActivityHatsOffListener onHatsOffListener) {
        this.onHatsOffListener = onHatsOffListener;
    }

    /**
     * Register a callback to be invoked when user clicks on delete button.
     */
    public void setOnContentDeleteListener(OnContentDeleteListener onContentDeleteListener) {
        this.onContentDeleteListener = onContentDeleteListener;
    }

    /**
     * Register a callback to be invoked when user clicks on capture button.
     */
    public void setOnMeCaptureClickListener(OnMeCaptureClickListener onMeCaptureClickListener) {
        this.onMeCaptureClickListener = onMeCaptureClickListener;
    }

    /**
     * Register a callback to be invoked when user clicks on share button.
     */
    public void setOnShareListener(listener.OnShareListener onShareListener) {
        this.onShareListener = onShareListener;
    }

    /**
     * Register a callback to be invoked when user clicks on share link button.
     */
    public void setOnShareLinkClickedListener(OnShareLinkClickedListener onShareLinkClickedListener) {
        this.onShareLinkClickedListener = onShareLinkClickedListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mUserContentList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_me, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final FeedModel data = mUserContentList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Load creator profile picture
            loadCreatorPic(data.getCreatorImage(), itemViewHolder.imageCreator);

            //Load content image
            loadContentImage(data.getContentImage(), itemViewHolder.imageContent);

            // set text and click actions acc. to content type
            //performContentTypeSpecificOperations(itemViewHolder, data);
            FeedHelper.performContentTypeSpecificOperations(mContext
                    , data
                    , itemViewHolder.collabCount
                    , itemViewHolder.collabCount
                    , itemViewHolder.buttonCollaborate
                    , itemViewHolder.textCreatorName
                    , true);

            //Initialize context menu button
            initializeMenuButton(itemViewHolder.buttonMenu, data.getUUID());

            //open bottom sheet on clicking of 3 dots
            itemViewHolder.buttonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMenuActionsBottomSheet(position, data.getEntityID());
                }
            });

            //ItemView collabOnWritingClick functionality
            itemViewOnClick(itemViewHolder.itemView, data, position);

            //Check whether user has given hats off to this campaign or not
            checkHatsOffStatus(data.getHatsOffStatus(), itemViewHolder);
            //HatsOff collabOnWritingClick functionality
            hatsOffOnClick(itemViewHolder, data, position);
            //Comment click functionality
            commentOnClick(itemViewHolder.containerComment, data.getEntityID());
            //Share click functionality
            shareOnClick(itemViewHolder.containerShare, data.getContentImage(), data.getEntityID(), data.getCreatorName());
            //Collaboration count click functionality
            collaborationCountOnClick(itemViewHolder.collabCount, data.getEntityID(), data.getContentType());

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //Load more data initialization
        initializeLoadMore(position);
    }

    @Override
    public int getItemCount() {
        return mUserContentList == null ? 0 : mUserContentList.size();
    }

    /**
     * Method is set loading status to false..
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
     * Method to setVisibility on delete button and initialize delete button functionality.
     *
     * @param creatorID UUID of content creator.
     */
    private void initializeMenuButton(TextView menu, String creatorID) {
        if (mUUID.equals(creatorID)) {
            //Show delete button
            menu.setVisibility(View.VISIBLE);
        } else {
            //Hide delete button
            menu.setVisibility(View.GONE);
        }
    }


    /**
     * Method to show confirmation dialog before deletion.
     *
     * @param index    position of item in adapter.
     * @param entityID Entity id of content.
     */
    private void showDeleteConfirmationDialog(final int index, final String entityID) {
        new MaterialDialog.Builder(mContext)
                .content("Are you sure want to delete this?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        onContentDeleteListener.onDelete(entityID, index);
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


    /**
     * ItemView collabOnWritingClick functionality.
     *
     * @param view      View to be clicked.
     * @param feedModel Data set for current item.
     * @param position  Position of item
     */
    private void itemViewOnClick(View view, final FeedModel feedModel, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_FEED_DESCRIPTION_DATA, feedModel);
                bundle.putInt("position", position);

                Intent intent = new Intent(mContext, FeedDescriptionActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                mMeFragment.startActivityForResult(intent, REQUEST_CODE_FEED_DESCRIPTION_ACTIVITY);
            }
        });
    }

    /**
     * write collabOnWritingClick functionality.
     *
     * @param view       View to be clicked.
     * @param captureID  CaptureID of image.
     * @param captureURL Capture image url.
     */
    private void writeOnClick(View view, final String captureID, final String captureURL, final String entityID, final boolean merchantable) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mHelper.isCaptureIconTooltipFirstTime()) {
                    getShortOnClickDialog(captureID, captureURL, merchantable);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(EXTRA_CAPTURE_ID, captureID);
                    bundle.putString(EXTRA_CAPTURE_URL, captureURL);
                    bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);
                    Intent intent = new Intent(mContext, ShortActivity.class);
                    intent.putExtra(EXTRA_DATA, bundle);
                    mContext.startActivity(intent);
                }
                //Log Firebase event
                setAnalytics(FIREBASE_EVENT_WRITE_CLICKED, entityID);
            }
        });
    }

    /**
     * capture collabOnWritingClick functionality.
     *
     * @param view View to be clicked.
     */
    private void captureOnClick(View view, final String entityID, final String shoid) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mHelper.isWriteIconTooltipFirstTime()) {

                    // open dialog
                    getCaptureOnClickDialog(shoid);
                } else {
                    onMeCaptureClickListener.onClick(shoid);
                }
                //Log Firebase event
                setAnalytics(FIREBASE_EVENT_CAPTURE_CLICKED, entityID);
            }
        });
    }


    /**
     * Method to check hatsOff status and perform operation accordingly.
     *
     * @param hatsOffStatus  True if user has given hatsOff, false otherwise.
     * @param itemViewHolder ViewHolder object.
     */
    private void checkHatsOffStatus(boolean hatsOffStatus, ItemViewHolder itemViewHolder) {
        if (hatsOffStatus) {
            itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
            //Animation for hats off
            itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off_fast));
            itemViewHolder.mIsHatsOff = true;
        } else {
            itemViewHolder.imageHatsOff.setColorFilter(Color.TRANSPARENT);
            itemViewHolder.mIsHatsOff = false;
        }
    }


    /**
     * HatsOff collabOnWritingClick functionality.
     *
     * @param itemViewHolder ViewHolder for items.
     * @param data           Data for current item.
     */
    private void hatsOffOnClick(final ItemViewHolder itemViewHolder, final FeedModel data, final int itemPosition) {
        itemViewHolder.containerHatsOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check net status
                if (NetworkHelper.getNetConnectionStatus(mContext)) {
                    //User has already given the hats off
                    if (itemViewHolder.mIsHatsOff) {
                        //Animation for hats off
                        itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.reverse_rotate_animation_hats_off));
                        //Toggle hatsOff tint
                        itemViewHolder.imageHatsOff.setColorFilter(Color.TRANSPARENT);
                        //Update hats of count i.e decrease by one
                        data.setHatsOffCount(data.getHatsOffCount() - 1);
                    } else {
                        //Animation for hats off
                        itemViewHolder.imageHatsOff.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.rotate_animation_hats_off));
                        //Toggle hatsOff tint
                        itemViewHolder.imageHatsOff.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
                        //Change hatsOffCount i.e increase by one
                        data.setHatsOffCount(data.getHatsOffCount() + 1);
                    }
                    //Toggle hatsOff status
                    itemViewHolder.mIsHatsOff = !itemViewHolder.mIsHatsOff;
                    //Update hats off here
                    data.setHatsOffStatus(itemViewHolder.mIsHatsOff);
                    //Listener
                    onHatsOffListener.onHatsOffClick(data, itemPosition);
                } else {
                    ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_no_connection));
                }

            }
        });
    }

    /**
     * Compose collabOnWritingClick functionality.
     *
     * @param view     View to be clicked.
     * @param entityID Entity ID.
     */
    private void commentOnClick(View view, final String entityID) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra(EXTRA_ENTITY_ID, entityID);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * Share collabOnWritingClick functionality.
     *
     * @param view       View to be clicked.x
     * @param pictureUrl URL of the picture to be shared.
     * @param entityID   Entity id of content.
     */
    private void shareOnClick(View view, final String pictureUrl, final String entityID, final String creatorName) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShareDialogAdapter adapter = new ShareDialogAdapter(mContext, initializeShareDialog(mContext));
                final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                        .adapter(adapter, null)
                        .show();

                adapter.setShareDialogItemClickedListener(new listener.OnShareDialogItemClickedListener() {
                    @Override
                    public void onShareDialogItemClicked(int index) {

                        //dismiss dialog
                        dialog.dismiss();

                        switch (index) {
                            case 0:
                                // image sharing
                                //so load image
                                loadBitmapForSharing(pictureUrl, entityID);
                                break;
                            case 1:
                                // link sharing
                                // get deep link from server
                                onShareLinkClickedListener.onShareLinkClicked(entityID, pictureUrl, creatorName);
                                break;

                        }
                    }
                });
            }
        });
    }


    /**
     * Method to load bitmap image to be shared
     */
    private void loadBitmapForSharing(final String pictureUrl, final String entityID) {

        Picasso.with(mContext).load(pictureUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //Set listener
                onShareListener.onShareClick(bitmap);
                //Log firebase event
                setAnalytics(FIREBASE_EVENT_SHARED_FROM_PROFILE, entityID);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ViewHelper.getToast(mContext, mContext.getString(R.string.error_msg_internal));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    /**
     * Method that performs operations according to content type and collaboration functionality
     *
     * @param itemViewHolder item view holder
     * @param data           data
     */

    private void performContentTypeSpecificOperations(MeAdapter.ItemViewHolder itemViewHolder, FeedModel data) {

        // initialize text
        String text = getCreatorText(mContext, data.getContentType(), data.isAvailableForCollab(), data.getCreatorName(), data.getCollabWithName());


        //Check for content type
        switch (data.getContentType()) {
            case CONTENT_TYPE_CAPTURE:

                // set collab count text
                if (data.getCollabCount() != 0) {
                    itemViewHolder.collabCount.setText(getCollabCountText(mContext, data.getCollabCount(), data.getContentType(), data.isAvailableForCollab()));
                    itemViewHolder.collabCount.setVisibility(View.VISIBLE);
                    itemViewHolder.lineSepartor.setVisibility(View.VISIBLE);

                } else {
                    itemViewHolder.collabCount.setVisibility(View.GONE);
                    itemViewHolder.lineSepartor.setVisibility(View.GONE);
                }

                if (data.isAvailableForCollab()) {

                    // for stand alone capture

                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);
                    // set text
                    //itemViewHolder.buttonCollaborate.setText("Write");

                    //write click functionality on capture
                    writeOnClick(itemViewHolder.buttonCollaborate, data.getCaptureID(), data.getContentImage(), data.getEntityID(), data.isMerchantable());

                    //String text = data.getCreatorName() + " added a capture";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = -1;
                    int collabWithEndPos = -1;

                    // get clickable text;
                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());

                } else {

                    // showing collaborate button
                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);


                    collabOnCollab(itemViewHolder.buttonCollaborate, mContext, data.getShortID(), data.getCaptureID(), data.getContentImage(), data.isMerchantable());

                    //String text = data.getCreatorName() + " added a capture to " + data.getCollabWithName() + "'s short";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(data.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + data.getCollabWithName().length() + 2; // +2 for 's

                    // get clickable text
                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());


                }

                break;

            case CONTENT_TYPE_SHORT:

                // set collab count text
                if (data.getCollabCount() != 0) {
                    itemViewHolder.collabCount.setText(getCollabCountText(mContext, data.getCollabCount(), data.getContentType(), data.isAvailableForCollab()));
                    itemViewHolder.collabCount.setVisibility(View.VISIBLE);
                    itemViewHolder.lineSepartor.setVisibility(View.VISIBLE);

                } else {
                    itemViewHolder.collabCount.setVisibility(View.GONE);
                    itemViewHolder.lineSepartor.setVisibility(View.GONE);
                }

                // check if available for collab
                if (data.isAvailableForCollab()) {

                    // for stand alone short

                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);
                    // set text
                    //itemViewHolder.buttonCollaborate.setText("Capture");

                    // capture click functionality on short
                    captureOnClick(itemViewHolder.buttonCollaborate, data.getEntityID(), data.getShortID());

                    //String text = data.getCreatorName() + " wrote a short ";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = -1; // since no collabwith
                    int collabWithEndPos = -1; // since no collabwith

                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, false, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());


                } else {
                    // showing collaborate button
                    itemViewHolder.buttonCollaborate.setVisibility(View.VISIBLE);

                    collabOnCollab(itemViewHolder.buttonCollaborate, mContext, data.getShortID(), data.getCaptureID(), data.getContentImage(), data.isMerchantable());

                    //String text = data.getCreatorName() + " wrote a short on " + data.getCollabWithName() + "'s capture";

                    // get text indexes
                    int creatorStartPos = text.indexOf(data.getCreatorName());
                    int creatorEndPos = creatorStartPos + data.getCreatorName().length();
                    int collabWithStartPos = text.indexOf(data.getCollabWithName());
                    int collabWithEndPos = collabWithStartPos + data.getCollabWithName().length() + 2; // +2 to incorporate 's

                    // get clickable text
                    initializeSpannableString(mContext, itemViewHolder.textCreatorName, true, text, creatorStartPos, creatorEndPos, collabWithStartPos, collabWithEndPos, data.getUUID(), data.getCollabWithUUID());
                }

                break;
            default:
        }
    }


    /**
     * Method to update dataList and notify for changes.
     *
     * @param list Updated data list.
     */
    public void updateList(List<FeedModel> list) {
        mUserContentList = list;
        notifyDataSetChanged();
    }

    /**
     * Method to show bottomSheet dialog with 'write a short' and 'Upload a capture' option.
     */
    public void getMenuActionsBottomSheet(final int index, final String entityID) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
        View sheetView = mContext.getLayoutInflater()
                .inflate(R.layout.bottomsheet_dialog_content_actions, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        LinearLayout buttonDelete = sheetView.findViewById(R.id.buttonDelete);


        //Delete button functionality
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDeleteConfirmationDialog(index, entityID);

                //Dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }

    /**
     * Method to initialize load more listener.
     */
    private void initializeLoadMore(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mUserContentList.size() - 1 && !mIsLoading) {
            if (onLoadMore != null) {
                //Lode more data here
                onLoadMore.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    /**
     * Method to show intro dialog when user collaborated by clicking on capture
     *
     * @param shoid short ID on which user is collaborating
     */
    private void getCaptureOnClickDialog(final String shoid) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_generic, false)
                .positiveText(mContext.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open capture functionality

                        onMeCaptureClickListener.onClick(shoid);

                        dialog.dismiss();
                        //update status
                        mHelper.updateWriteIconToolTipStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_collab_intro));
        //Set title text
        textTitle.setText(mContext.getString(R.string.title_dialog_collab_capture));
        //Set description text
        textDesc.setText(mContext.getString(R.string.text_dialog_collab_capture));
    }

    /**
     * Method to show intro dialog when user collaborated by clicking on capture
     *
     * @param captureID    capture ID
     * @param captureURL   capture URl
     * @param merchantable merchantable true or false
     */
    private void getShortOnClickDialog(final String captureID, final String captureURL, final boolean merchantable) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.dialog_generic, false)
                .positiveText(mContext.getString(R.string.text_ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Open short functionality

                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_CAPTURE_ID, captureID);
                        bundle.putString(EXTRA_CAPTURE_URL, captureURL);
                        bundle.putBoolean(EXTRA_MERCHANTABLE, merchantable);
                        Intent intent = new Intent(mContext, ShortActivity.class);
                        intent.putExtra(EXTRA_DATA, bundle);
                        mContext.startActivity(intent);

                        dialog.dismiss();
                        //update status
                        mHelper.updateCaptureIconToolTipStatus(false);
                    }
                })
                .show();
        //Obtain views reference
        ImageView fillerImage = dialog.getCustomView().findViewById(R.id.viewFiller);
        TextView textTitle = dialog.getCustomView().findViewById(R.id.textTitle);
        TextView textDesc = dialog.getCustomView().findViewById(R.id.textDesc);


        //Set filler image
        fillerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_collab_intro));
        //Set title text
        textTitle.setText(mContext.getString(R.string.title_dialog_collab_short));
        //Set description text
        textDesc.setText(mContext.getString(R.string.text_dialog_collab_short));
    }

    /**
     * Collaboration count click functionality to launch collaborationDetailsActivity.
     *
     * @param textView   View to be clicked
     * @param entityID   Entity id of the content.
     * @param entityType Type of content i.e CAPTURE or SHORT
     */
    private void collaborationCountOnClick(TextView textView, final String entityID, final String entityType) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_ENTITY_ID, entityID);
                bundle.putString(EXTRA_ENTITY_TYPE, entityType);

                Intent intent = new Intent(mContext, CollaborationDetailsActivity.class);
                intent.putExtra(EXTRA_DATA, bundle);
                mContext.startActivity(intent);
            }
        });
    }


    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageCreator)
        CircleImageView imageCreator;
        @BindView(R.id.textCreatorName)
        TextView textCreatorName;
        @BindView(R.id.imageContent)
        ImageView imageContent;
        @BindView(R.id.imageHatsOff)
        ImageView imageHatsOff;
        @BindView(R.id.buttonCollaborate)
        TextView buttonCollaborate;
        @BindView(R.id.containerHatsOff)
        LinearLayout containerHatsOff;
        @BindView(R.id.containerComment)
        LinearLayout containerComment;
        @BindView(R.id.containerShare)
        LinearLayout containerShare;
        @BindView(R.id.buttonMenu)
        TextView buttonMenu;
        @BindView(R.id.collabCount)
        TextView collabCount;
        @BindView(R.id.lineSeparatorTop)
        View lineSepartor;

        //Variable to maintain hats off status
        private boolean mIsHatsOff = false;

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

    /**
     * Method to send analytics data on firebase server.
     *
     * @param firebaseEvent Event type.
     * @param entityID      Entity id of the content.
     */
    private void setAnalytics(String firebaseEvent, String entityID) {
        Bundle bundle = new Bundle();
        bundle.putString("uuid", mUUID);
        if (firebaseEvent.equals(FIREBASE_EVENT_WRITE_CLICKED)) {
            bundle.putString("class_name", "me_feed");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_WRITE_CLICKED, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_SHARED_FROM_PROFILE)) {
            bundle.putString("entity_id", entityID);
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_SHARED_FROM_PROFILE, bundle);
        } else if (firebaseEvent.equals(FIREBASE_EVENT_CAPTURE_CLICKED)) {
            bundle.putString("class_name", "me_feed");
            FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_CAPTURE_CLICKED, bundle);
        }
    }

}
