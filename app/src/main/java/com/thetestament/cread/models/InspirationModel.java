package com.thetestament.cread.models;

/**
 * Model for inspiration screen
 */

public class InspirationModel {

    private String entityID, captureID, UUID, creatorName, creatorProfilePic, capturePic;
    private boolean merchantable;

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getCaptureID() {
        return captureID;
    }

    public void setCaptureID(String captureID) {
        this.captureID = captureID;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorProfilePic() {
        return creatorProfilePic;
    }

    public void setCreatorProfilePic(String creatorProfilePic) {
        this.creatorProfilePic = creatorProfilePic;
    }

    public String getCapturePic() {
        return capturePic;
    }

    public void setCapturePic(String capturePic) {
        this.capturePic = capturePic;
    }

    public boolean isMerchantable() {
        return merchantable;
    }

    public void setMerchantable(boolean merchantable) {
        this.merchantable = merchantable;
    }
}
