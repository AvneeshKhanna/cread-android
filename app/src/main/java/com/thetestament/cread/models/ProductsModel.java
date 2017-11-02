package com.thetestament.cread.models;

import java.util.ArrayList;
import java.util.List;

public class ProductsModel {

    private String type, productID, entityUrl, productUrl,deliveryCharge;
    private List<String> colors  = new ArrayList<>();
    private List<String> sizes = new ArrayList<>();
    private List<String> quanity = new ArrayList<>();
    private List<String> price = new ArrayList<>();




    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public String getEntityUrl() {
        return entityUrl;
    }

    public void setEntityUrl(String entityUrl) {
        this.entityUrl = entityUrl;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public List<String> getPrice() {
        return price;
    }

    public void setPrice(List<String> price) {
        this.price = price;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public List<String> getQuanity() {
        return quanity;
    }

    public void setQuanity(List<String> quanity) {
        this.quanity = quanity;
    }

    public String getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(String deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }
}
