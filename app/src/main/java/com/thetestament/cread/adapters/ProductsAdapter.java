package com.thetestament.cread.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener;
import com.thetestament.cread.listeners.listener.OnBuyButtonClickedListener;
import com.thetestament.cread.models.ProductsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    private List<ProductsModel> mProductsList;
    private FragmentActivity mContext;
    private OnBuyButtonClickedListener buttonClickedListener;


    public ProductsAdapter(FragmentActivity mContext, List<ProductsModel> mProductsList) {
        this.mContext = mContext;
        this.mProductsList = mProductsList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_merchandizing_product, parent, false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        final ProductsModel data = mProductsList.get(position);

        final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

        itemViewHolder.price = data.getPrice().get(0);

        loadproductPicture(data.getProductUrl(),itemViewHolder.productImage);

        loadEntityPicture(data.getEntityUrl(),processEntityImage(data.getType(),itemViewHolder.artImg));

        itemViewHolder.type = getProductName(data.getType());
        itemViewHolder.productName.setText(itemViewHolder.type);
        itemViewHolder.priceText.setText(mContext.getString(R.string.Rs)+ " " + itemViewHolder.price);

        itemViewHolder.productID = data.getProductID();

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, data.getSizes());
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemViewHolder.sizeSpinner.setAdapter(sizeAdapter);

        itemViewHolder.sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                itemViewHolder.price = data.getPrice().get(position);
                itemViewHolder.priceText.setText(mContext.getString(R.string.Rs)+ " " + itemViewHolder.price);

                itemViewHolder.size = data.getSizes().get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> colorsAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, data.getColors());
        colorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemViewHolder.colorSpinner.setAdapter(colorsAdapter);
        itemViewHolder.colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                itemViewHolder.color = data.getColors().get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> quantityAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, data.getQuanity());
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemViewHolder.quantitySpinner.setAdapter(quantityAdapter);
        itemViewHolder.quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                itemViewHolder.quantity = data.getQuanity().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        itemViewHolder.buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonClickedListener.onBuyButtonClicked(itemViewHolder.type,itemViewHolder.size,itemViewHolder.color,itemViewHolder.quantity,itemViewHolder.price, itemViewHolder.productID);

            }
        });

    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.productImage)
        ImageView productImage;
        @BindView(R.id.artImg)
        ImageView artImg;
        @BindView(R.id.merchProduct)
        RelativeLayout merchProduct;
        @BindView(R.id.productName)
        TextView productName;
        @BindView(R.id.sizeImg)
        ImageView sizeImg;
        @BindView(R.id.sizeText)
        TextView sizeText;
        @BindView(R.id.sizeSpinner)
        AppCompatSpinner sizeSpinner;
        @BindView(R.id.colorText)
        TextView colorText;
        @BindView(R.id.colorSpinner)
        AppCompatSpinner colorSpinner;
        @BindView(R.id.quantityText)
        TextView quantityText;
        @BindView(R.id.quantitySpinner)
        AppCompatSpinner quantitySpinner;
        @BindView(R.id.price)
        TextView priceText;
        @BindView(R.id.buyButton)
        TextView buyButton;

        String type, size, color, quantity, price, productID;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public int getItemCount() {
        return mProductsList.size();
    }


    /**
     * Method to load entity picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadEntityPicture(String picUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                // TODO change error image
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }

    /**
     * Method to load product picture.
     *
     * @param picUrl    picture URL.
     * @param imageView View where image to be loaded.
     */
    private void loadproductPicture(String picUrl, ImageView imageView) {



        Picasso.with(mContext)
                .load(picUrl)
                // TODO change error image
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }


    private String getProductName(String type)
    {
        String name = null;

        switch (type)
        {
            case "SHIRT":
                name = "T-Shirt";
                break;
            case "COFFEE_MUG":
                name = "Coffee Mug";
                break;
            case "FRAME":
                name= "Frame";
                break;
            case "POSTER":
                name = "Poster";
                break;

        }

        return name;
    }

    private ImageView processEntityImage(String type,ImageView imageView)
    {
        switch (type)
        {
            case "SHIRT":
                imageView.getLayoutParams().height = ViewHelper.convertToPx(mContext, 100);
                imageView.getLayoutParams().width = ViewHelper.convertToPx(mContext, 100);
                imageView.setVisibility(View.VISIBLE);
                break;
            case "COFFEE_MUG":
                imageView.setVisibility(View.GONE);
                break;
            case "FRAME":
                imageView.setVisibility(View.VISIBLE);
                imageView.getLayoutParams().height = ViewHelper.convertToPx(mContext,250);
                imageView.getLayoutParams().width = ViewHelper.convertToPx(mContext,250);
                break;
            case "POSTER":
                imageView.setVisibility(View.VISIBLE);
                imageView.getLayoutParams().height = ViewHelper.convertToPx(mContext,260);
                imageView.getLayoutParams().width = ViewHelper.convertToPx(mContext,260);
                break;

        }

        return imageView;
    }

    public void setBuyButtonClickedListener(OnBuyButtonClickedListener buttonClickedListener)
    {
        this.buttonClickedListener = buttonClickedListener;
    }

}
