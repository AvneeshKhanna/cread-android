package com.thetestament.cread.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.RecommendedArtistsModel;
import com.thetestament.cread.widgets.SquareImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a RecommendedArtist RecyclerView.
 */
public class RecommendedArtistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private FragmentActivity mContext;
    private List<RecommendedArtistsModel> mDataList;
    private boolean mIsLoading;

    private listener.OnRecommendedArtistsLoadMoreListener loadMoreListener;

    /**
     * Required constructor.
     *
     * @param dataList List of RecommendedArtists model.
     * @param context  Context to use.
     */
    public RecommendedArtistsAdapter(FragmentActivity context, List<RecommendedArtistsModel> dataList) {
        this.mContext = context;
        this.mDataList = dataList;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnRecommendedArtistsLoadMoreListener(listener.OnRecommendedArtistsLoadMoreListener onRecommendedArtistsLoadMoreListener) {
        this.loadMoreListener = onRecommendedArtistsLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_recommended_artists, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecommendedArtistsModel data = mDataList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            //Set artist name
            itemViewHolder.textArtistName.setText(data.getArtistName());

            //Method called
            setPostCount(data.getPostCount(), itemViewHolder.textPostCount);
            setArtistBio(data.getArtistBio(), itemViewHolder.textArtistBio);

            //Load Artist profile image view
            loadImageFromUrl(mContext, itemViewHolder.imageArtist
                    , data.getArtistProfilePic(), R.drawable.ic_account_circle_100);
            //Method called
            setArtistContentImage(data, itemViewHolder.imageContainer);
            followOnClick(itemViewHolder.buttonFollow);
            itemViewOnClick(itemViewHolder.itemView, data.getArtistUUID());

        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }
        //Method called
        setupLoadMoreListener(position);
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    //ItemViewHolder class
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageArtist)
        CircleImageView imageArtist;
        @BindView(R.id.textArtistName)
        AppCompatTextView textArtistName;
        @BindView(R.id.textPostCount)
        AppCompatTextView textPostCount;
        @BindView(R.id.buttonFollow)
        AppCompatTextView buttonFollow;
        @BindView(R.id.textArtistBio)
        AppCompatTextView textArtistBio;
        @BindView(R.id.artistContentContainer)
        LinearLayout imageContainer;
        @BindView(R.id.imageArtFirst)
        SquareImageView imageArtFirst;
        @BindView(R.id.imageArtTwo)
        SquareImageView imageArtTwo;
        @BindView(R.id.imageArtThree)
        SquareImageView imageArtThree;
        @BindView(R.id.imageArtFour)
        SquareImageView imageArtFour;

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
     * Method to setup load more listener.
     *
     * @param position Item position in list.
     */
    private void setupLoadMoreListener(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mDataList.size() - 1 && !mIsLoading) {
            if (loadMoreListener != null) {
                //Lode more data here
                loadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    /**
     * Method to set artist bio.
     *
     * @param artistBio Bio of artist.
     * @param textBio   View where text to be updated.
     */
    private void setArtistBio(String artistBio, AppCompatTextView textBio) {
        //Check null and empty string
        if (TextUtils.isEmpty(artistBio)) {
            //Hide bio textView
            textBio.setVisibility(View.GONE);
        } else {
            //Set post count
            textBio.setText(artistBio);
            //Show bio textView
            textBio.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Method to set post count.
     *
     * @param postCount Post created by user.
     * @param textCount View where text to be updated.
     */
    private void setPostCount(long postCount, AppCompatTextView textCount) {
        //Check for post count
        if (postCount == 1) {
            //Set post count
            textCount.setText(postCount + "Post");
        } else {
            //Set post count
            textCount.setText(postCount + "Posts");
        }

    }

    /**
     * @param context     Context to use.
     * @param imageView   View where image to be loaded.
     * @param url         Url of image.
     * @param placeholder Resource ID of error placeholder drawable.
     */
    private void loadImageFromUrl(Context context, ImageView imageView, String url, int placeholder) {
        Picasso.with(context)
                .load(url)
                .error(placeholder)
                .into(imageView);
    }

    /**
     * @param data   Instance of RecommendedArtistsModel.
     * @param layout ParentView of imageViews.
     */
    private void setArtistContentImage(RecommendedArtistsModel data, LinearLayout layout) {
        int index = 0;
        for (String s : data.getImagesList()) {
            //Load Artist profile image view
            loadImageFromUrl(mContext, (AppCompatImageView) layout.getChildAt(index)
                    , s, R.drawable.image_placeholder);
            //Increment counter
            index++;
        }
    }

    /**
     * Follow button click functionality.
     *
     * @param buttonFollow View to be clicked
     */
    private void followOnClick(AppCompatTextView buttonFollow) {
        buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fixme click functionality
            }
        });
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
                mContext.startActivity(intent);
            }
        });
    }

}
