package com.thetestament.cread.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linkedin.android.spyglass.suggestions.interfaces.Suggestible;
import com.thetestament.cread.R;
import com.thetestament.cread.models.PersonMentionModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PersonMentionAdapter extends RecyclerView.Adapter<PersonMentionAdapter.ViewHolder> {

    private List<? extends Suggestible> mPeople = new ArrayList<>();
    private Context mContext;

    public PersonMentionAdapter(List<? extends Suggestible> mPeople, Context mContext) {
        this.mPeople = mPeople;
        this.mContext = mContext;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new
                ViewHolder(
                LayoutInflater
                        .from(mContext)
                        .inflate(R.layout.item_profile_mention, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Suggestible suggestion = mPeople.get(position);
        if (!(suggestion instanceof PersonMentionModel)) {
            return;
        }

        final PersonMentionModel person = (PersonMentionModel) suggestion;
        holder.textName.setText(person.getmName());
    }

    @Override
    public int getItemCount() {
        return mPeople.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.imagePerson)
        ImageView imagePerson;
        @BindView(R.id.textName)
        TextView textName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
