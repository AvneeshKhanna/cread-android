package com.thetestament.cread.models;

public class RoyaltiesModel {

    private String entityID, entityUrl, royaltyDate, productType, type, name, uuid;
    private double royaltyAmount;
    private int quantity;
    private boolean redeemStatus;

    public String getEntityUrl() {
        return entityUrl;
    }

    public void setEntityUrl(String entityUrl) {
        this.entityUrl = entityUrl;
    }

    public double getRoyaltyAmount() {
        return royaltyAmount;
    }

    public void setRoyaltyAmount(double royaltyAmount) {
        this.royaltyAmount = royaltyAmount;
    }

    public String getRoyaltyDate() {
        return royaltyDate;
    }

    public void setRoyaltyDate(String royaltyDate) {
        this.royaltyDate = royaltyDate;
    }


    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isRedeemStatus() {
        return redeemStatus;
    }

    public void setRedeemStatus(boolean redeemStatus) {
        this.redeemStatus = redeemStatus;
    }
}
