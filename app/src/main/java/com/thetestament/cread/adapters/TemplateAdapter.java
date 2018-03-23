package com.thetestament.cread.adapters;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thetestament.cread.R;
import com.thetestament.cread.helpers.TemplateHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.models.TemplateModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a template RecyclerView.
 */
public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ItemViewHolder> {

    private List<TemplateModel> mTemplateList;
    private FragmentActivity mContext;
    private listener.OnTemplateClickListener templateClickListener;

    private int mTemplateSelected = 0;

    /**
     * Required constructor.
     *
     * @param templateList List of template data.
     * @param context      Context to be use.
     */
    public TemplateAdapter(List<TemplateModel> templateList, FragmentActivity context) {
        this.mTemplateList = templateList;
        this.mContext = context;
    }

    /**
     * Register a callback to be invoked when user selects template.
     */
    public void setOnTemplateClickListener(listener.OnTemplateClickListener templateClickListener) {
        this.templateClickListener = templateClickListener;
    }

    @Override
    public TemplateAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_template, parent, false));
    }

    @Override
    public void onBindViewHolder(TemplateAdapter.ItemViewHolder holder, final int position) {
        final TemplateModel data = mTemplateList.get(position);

        //Set template name
        holder.textTemplateName.setText(data.getTemplateName());
        //Set template image
        holder.imageView.setImageDrawable(TemplateHelper.getTemplateDrawable(data.getTemplateName(), mContext));


        //ItemView click functionality
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set Listener
                templateClickListener.onTemplateClick(data.getTemplateName(),position);
                //update flag and notify changes
                mTemplateSelected = position;
                notifyDataSetChanged();
            }
        });

        //update color
        if (mTemplateSelected == position) {
            holder.textTemplateName.setTextColor(Color.BLACK);
        } else {
            holder.textTemplateName.setTextColor(Color.GRAY);
        }

    }

    @Override
    public int getItemCount() {
        return mTemplateList.size();
    }

    //Item view holder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.templateName)
        TextView textTemplateName;
        @BindView(R.id.templateImage)
        AppCompatImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            //Bind view
            ButterKnife.bind(this, itemView);
        }
    }
}
