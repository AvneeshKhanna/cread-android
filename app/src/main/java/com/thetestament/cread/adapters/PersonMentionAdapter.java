package com.thetestament.cread.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ImageHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.PersonMentionModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonMentionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<? extends Suggestible> mPeople = new ArrayList<>();
    private Context mContext;
    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private boolean mIsLoading;

    private listener.onSuggestionsLoadMore mSuggestionsLoadMore;
    private listener.OnPeopleSuggestionsClick mSuggestionsClick;

    public PersonMentionAdapter(List<? extends Suggestible> mPeople, Context mContext) {
        this.mPeople = mPeople;
        this.mContext = mContext;
    }

    public void setLoadMoreSuggestionsListener(listener.onSuggestionsLoadMore mSuggestionsLoadMore) {
        this.mSuggestionsLoadMore = mSuggestionsLoadMore;
    }

    public void setSuggestionsClickListener(listener.OnPeopleSuggestionsClick mSuggestionsClick) {
        this.mSuggestionsClick = mSuggestionsClick;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_ITEM) {
            return new ItemViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_profile_mention, parent, false));
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_load_more, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Suggestible suggestion = mPeople.get(position);
        if (!(suggestion instanceof PersonMentionModel)) {
            return;
        }

        final PersonMentionModel person = (PersonMentionModel) suggestion;

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            // load image
            ImageHelper.loadProgressiveImage(Uri.parse(person.getmPictureURL())
                    , itemViewHolder.imagePerson);


            // set name
            itemViewHolder.textName.setText(person.getmName());
            // init click listener
            initMentionsClick(itemViewHolder.itemView, person);


        } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressView.setVisibility(View.VISIBLE);
        }

        //If last item is visible to user and new set of data is to yet to be loaded
        initializeLoadMore(position);
    }

    @Override
    public int getItemViewType(int position) {

        return mPeople.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;

    }

    @Override
    public int getItemCount() {
        return mPeople.size();
    }


    /**
     * Method is toggle the loading status
     */
    public void setLoaded() {
        mIsLoading = false;
    }


    /**
     * Method to initialize load more listener.
     */
    private void initializeLoadMore(int position) {
        //If last item is visible to user and new set of data is to yet to be loaded
        if (position == mPeople.size() - 1 && !mIsLoading) {
            if (mSuggestionsLoadMore != null) {
                //Lode more data here
                mSuggestionsLoadMore.onLoadMore();
            }
            //toggle
            mIsLoading = true;
        }
    }


    private void initMentionsClick(View view, final PersonMentionModel person) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSuggestionsClick != null) {
                    mSuggestionsClick.onPeopleSuggestionsClick(person);
                }


            }
        });
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imagePerson)
        SimpleDraweeView imagePerson;
        @BindView(R.id.textName)
        TextView textName;

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
