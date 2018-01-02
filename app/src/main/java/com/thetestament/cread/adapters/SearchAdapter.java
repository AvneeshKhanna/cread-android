package com.thetestament.cread.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.activities.ProfileActivity;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.SearchModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.thetestament.cread.utils.Constant.EXTRA_PROFILE_UUID;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_HASHTAG;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_NO_RESULTS;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PEOPLE;
import static com.thetestament.cread.utils.Constant.SEARCH_TYPE_PROGRESS;
import static com.thetestament.cread.utils.Constant.URI_HASH_TAG_ACTIVITY;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a search RecyclerView.
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_PEOPLE = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_HASHTAG = 2;
    private final int VIEW_TYPE_NO_RESULTS = 3;
    private final int VIEW_TYPE_PROGRESS = 4;
    private List<SearchModel> mSearchDataList;
    private FragmentActivity mContext;
    private boolean mIsLoading;


    private listener.OnSearchLoadMoreListener onSearchLoadMoreListener;

    /**
     * Required constructor.
     *
     * @param searchDataList List of search data.
     * @param context        Context to use.
     */

    public SearchAdapter(List<SearchModel> searchDataList, FragmentActivity context) {
        this.mSearchDataList = searchDataList;
        this.mContext = context;
    }

    /**
     * Register a callback to be invoked when user scrolls for more data.
     */
    public void setOnSearchLoadMoreListener(listener.OnSearchLoadMoreListener onSearchLoadMoreListener) {
        this.onSearchLoadMoreListener = onSearchLoadMoreListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mSearchDataList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            if (mSearchDataList.get(position).getSearchType().equals(SEARCH_TYPE_PEOPLE)) {
                return VIEW_TYPE_PEOPLE;
            } else if (mSearchDataList.get(position).getSearchType().equals(SEARCH_TYPE_HASHTAG)) {
                return VIEW_TYPE_HASHTAG;
            } else if (mSearchDataList.get(position).getSearchType().equals(SEARCH_TYPE_NO_RESULTS)) {
                return VIEW_TYPE_NO_RESULTS;
            } else if (mSearchDataList.get(position).getSearchType().equals(SEARCH_TYPE_PROGRESS)) {
                return VIEW_TYPE_PROGRESS;
            }
        }
        //Default view to  be loaded
        return VIEW_TYPE_PEOPLE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PEOPLE) {
            return new PeopleViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_search_people, parent, false));
        } else if (viewType == VIEW_TYPE_HASHTAG) {
            return new HashTagViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_search_hashtag, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        } else if (viewType == VIEW_TYPE_NO_RESULTS) {
            return new NoResultViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_no_results, parent, false));
        } else if (viewType == VIEW_TYPE_PROGRESS) {
            return new ProgressViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_progress, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchModel data = mSearchDataList.get(position);
        //Type is people
        if (holder.getItemViewType() == VIEW_TYPE_PEOPLE) {
            PeopleViewHolder peopleViewHolder = (PeopleViewHolder) holder;
            //Set user name
            peopleViewHolder.textUserName.setText(data.getUserName());
            //Load profile picture
            loadProfilePicture(data.getProfilePicUrl(), peopleViewHolder.imageUser);
            //Click functionality
            peopleItemViewOnClick(peopleViewHolder.itemView, data.getUserUUID());
        }
        //Type is hashTag
        else if (holder.getItemViewType() == VIEW_TYPE_HASHTAG) {
            HashTagViewHolder hashTagViewHolder = (HashTagViewHolder) holder;
            //Set tag and count
            hashTagViewHolder.textHashTag.setText(data.getHashTagLabel());
            hashTagViewHolder.textHashTagCount.setText("  \u2022  " + String.valueOf(data.getHashTagCount() + " posts"));
            //Click functionality
            hashTagItemViewOnClick(hashTagViewHolder.itemView, data.getHashTagLabel());
        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        } else if (holder.getItemViewType() == VIEW_TYPE_NO_RESULTS) {
            NoResultViewHolder noResultViewHolder = (NoResultViewHolder) holder;
        } else if (holder.getItemViewType() == VIEW_TYPE_PROGRESS) {
            ProgressViewHolder progressViewHolder = (ProgressViewHolder) holder;
        }


        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mSearchDataList.size() - 1 && !mIsLoading) {
            if (onSearchLoadMoreListener != null) {
                //Lode more data here
                onSearchLoadMoreListener.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }

    @Override
    public int getItemCount() {
        return mSearchDataList == null ? 0 : mSearchDataList.size();
    }

    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }

    /**
     * Method to load profile picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadProfilePicture(String picUrl, CircleImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * ItemView collabOnWritingClick functionality.
     *
     * @param uuid unique ID of the person whose profile to be opened.
     */
    private void peopleItemViewOnClick(View view, final String uuid) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(EXTRA_PROFILE_UUID, uuid);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * ItemView collabOnWritingClick functionality.
     *
     * @param hashTag HashTag.
     */
    private void hashTagItemViewOnClick(View view, final String hashTag) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get uri from string
                Uri uri = Uri.parse(URI_HASH_TAG_ACTIVITY + "#" + hashTag);

                // open hash tag activity
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(intent);

            }
        });
    }

    //PeopleViewHolder class
    static class PeopleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageUser)
        CircleImageView imageUser;
        @BindView(R.id.textUserName)
        TextView textUserName;

        public PeopleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //HashTagViewHolder class
    static class HashTagViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textHashTag)
        TextView textHashTag;
        @BindView(R.id.countHashTag)
        TextView textHashTagCount;

        public HashTagViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    //NoResultViewHolder
    static class NoResultViewHolder extends RecyclerView.ViewHolder {
        public NoResultViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(View itemView) {
            super(itemView);
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
