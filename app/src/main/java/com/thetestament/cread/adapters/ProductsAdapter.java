package com.thetestament.cread.adapters;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.thetestament.cread.R;
import com.thetestament.cread.helpers.ViewHelper;
import com.thetestament.cread.listeners.listener.OnBuyButtonClickedListener;
import com.thetestament.cread.models.ProductsModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.thetestament.cread.utils.Constant.FIREBASE_EVENT_BUY_CLICKED;

/**
 * Adapter class to provide a binding from data set to views that are displayed within a product RecyclerView.
 */
public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ItemViewHolder> {

    private List<ProductsModel> mProductsList;
    private FragmentActivity mContext;
    private String mUUID;
    private OnBuyButtonClickedListener buttonClickedListener;


    /**
     * Required constructor
     *
     * @param mContext      Context to use.
     * @param mProductsList Data list.
     * @param mUUID         User uuid
     */
    public ProductsAdapter(FragmentActivity mContext, List<ProductsModel> mProductsList, String mUUID) {
        this.mContext = mContext;
        this.mProductsList = mProductsList;
        this.mUUID = mUUID;
    }

    /**
     * Register a callback to be invoked when user clicks on buy button.
     */
    public void setBuyButtonClickedListener(OnBuyButtonClickedListener buttonClickedListener) {
        this.buttonClickedListener = buttonClickedListener;
    }

    @Override
    public ProductsAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_merchandizing_product, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductsAdapter.ItemViewHolder holder, int position) {
        final ProductsModel data = mProductsList.get(position);

        loadProductPicture(data.getProductUrl(), holder.productImage);
        loadEntityPicture(data.getEntityUrl(), processEntityImage(data.getType(), holder.artImg));

        // initializing price to a default value and it is changed according to the price
        holder.price = data.getPrice().get(0);

        holder.type = getProductName(data.getType());
        holder.productName.setText(holder.type);
        holder.priceText.setText(mContext.getString(R.string.Rs) + " " + holder.price);

        holder.productID = data.getProductID();

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, data.getSizes());
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.sizeSpinner.setAdapter(sizeAdapter);

        holder.sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // adjusting price according to the size
                holder.price = data.getPrice().get(position);
                holder.priceText.setText(mContext.getString(R.string.Rs) + " " + holder.price);

                holder.size = data.getSizes().get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> colorsAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, data.getColors());
        colorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.colorSpinner.setAdapter(colorsAdapter);
        holder.colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                holder.color = data.getColors().get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> quantityAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, data.getQuanity());
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.quantitySpinner.setAdapter(quantityAdapter);
        holder.quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                holder.quantity = data.getQuanity().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        holder.buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClickedListener.onBuyButtonClicked(holder.type, holder.size, holder.color, holder.quantity, holder.price, holder.productID, data.getDeliveryCharge());

                //Log firebase event
                Bundle bundle = new Bundle();
                bundle.putString("uuid", mUUID);
                FirebaseAnalytics.getInstance(mContext).logEvent(FIREBASE_EVENT_BUY_CLICKED, bundle);
            }
        });

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
    private void loadProductPicture(String picUrl, ImageView imageView) {
        Picasso.with(mContext)
                .load(picUrl)
                // TODO change error image
                .error(R.drawable.ic_account_circle_48)
                .into(imageView);
    }


    private String getProductName(String type) {
        String name = null;
        switch (type) {
            case "SHIRT":
                name = "T-Shirt";
                break;
            case "COFFEE_MUG":
                name = "Coffee Mug";
                break;
            case "FRAME":
                name = "Frame";
                break;
            case "POSTER":
                name = "Poster";
                break;
        }
        return name;
    }

    private ImageView processEntityImage(String type, ImageView imageView) {
        switch (type) {
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
                imageView.getLayoutParams().height = ViewHelper.convertToPx(mContext, 250);
                imageView.getLayoutParams().width = ViewHelper.convertToPx(mContext, 250);
                break;
            case "POSTER":
                imageView.setVisibility(View.VISIBLE);
                imageView.getLayoutParams().height = ViewHelper.convertToPx(mContext, 260);
                imageView.getLayoutParams().width = ViewHelper.convertToPx(mContext, 260);
                break;
        }
        return imageView;
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

}
