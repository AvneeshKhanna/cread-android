package com.thetestament.cread.models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for feed.
 */

public class FeedModel implements Parcelable {

    private String entityID, captureID, shortID;
    private String UUID, creatorName, creatorImage, collabWithUUID, collabWithName, caption;
    private boolean hatsOffStatus, followStatus, merchantable, isAvailableForCollab, downvoteStatus, isEligibleForDownvote, isLongForm;
    private long hatsOffCount, commentCount, collabCount;
    private String contentType;
    private String contentImage;
    private String collaboWithEntityID;


    //Required constructor
    public FeedModel() {
    }


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

    public String getShortID() {
        return shortID;
    }

    public void setShortID(String shortID) {
        this.shortID = shortID;
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

    public String getCreatorImage() {
        return creatorImage;
    }

    public void setCreatorImage(String creatorImage) {
        this.creatorImage = creatorImage;
    }

    public boolean getHatsOffStatus() {
        return hatsOffStatus;
    }

    public void setHatsOffStatus(boolean hatsOffStatus) {
        this.hatsOffStatus = hatsOffStatus;
    }

    public boolean getFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(boolean followStatus) {
        this.followStatus = followStatus;
    }

    public long getHatsOffCount() {
        return hatsOffCount;
    }

    public void setHatsOffCount(long hatsOffCount) {
        this.hatsOffCount = hatsOffCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentImage() {
        return contentImage;
    }

    public void setContentImage(String contentImage) {
        this.contentImage = contentImage;
    }

    public boolean isMerchantable() {
        return merchantable;
    }

    public void setMerchantable(boolean merchantable) {
        this.merchantable = merchantable;
    }

    public boolean isDownvoteStatus() {
        return downvoteStatus;
    }

    public void setDownvoteStatus(boolean downvoteStatus) {
        this.downvoteStatus = downvoteStatus;
    }

    public boolean isEligibleForDownvote() {
        return isEligibleForDownvote;
    }

    public void setEligibleForDownvote(boolean eligibleForDownvote) {
        isEligibleForDownvote = eligibleForDownvote;
    }

    public boolean isAvailableForCollab() {
        return isAvailableForCollab;
    }

    public void setAvailableForCollab(boolean availableForCollab) {
        isAvailableForCollab = availableForCollab;
    }

    public String getCollabWithUUID() {
        return collabWithUUID;
    }

    public void setCollabWithUUID(String collabWithUUID) {
        this.collabWithUUID = collabWithUUID;
    }

    public String getCollabWithName() {
        return collabWithName;
    }

    public void setCollabWithName(String collabWithName) {
        this.collabWithName = collabWithName;
    }

    public long getCollabCount() {
        return collabCount;
    }

    public void setCollabCount(long collabCount) {
        this.collabCount = collabCount;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCollaboWithEntityID() {
        return collaboWithEntityID;
    }

    public void setCollaboWithEntityID(String collaboWithEntityID) {
        this.collaboWithEntityID = collaboWithEntityID;
    }

    public boolean isLongForm() {
        return isLongForm;
    }

    public void setLongForm(boolean longForm) {
        isLongForm = longForm;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(entityID);
        parcel.writeString(captureID);
        parcel.writeString(shortID);
        parcel.writeString(UUID);
        parcel.writeString(creatorName);
        parcel.writeString(creatorImage);
        parcel.writeByte((byte) (hatsOffStatus ? 1 : 0));
        parcel.writeByte((byte) (followStatus ? 1 : 0));
        parcel.writeByte((byte) (merchantable ? 1 : 0));
        parcel.writeByte((byte) (downvoteStatus ? 1 : 0));
        parcel.writeByte((byte) (isEligibleForDownvote ? 1 : 0));
        parcel.writeLong(hatsOffCount);
        parcel.writeLong(commentCount);
        parcel.writeString(contentType);
        parcel.writeString(contentImage);
        parcel.writeByte((byte) (isAvailableForCollab ? 1 : 0));
        parcel.writeString(collabWithUUID);
        parcel.writeString(collabWithName);
        parcel.writeLong(getCollabCount());
        parcel.writeString(caption);
        parcel.writeString(collaboWithEntityID);
        parcel.writeByte((byte) (isLongForm ? 1 : 0));

    }


    public static final Creator<FeedModel> CREATOR = new Creator<FeedModel>() {
        @Override
        public FeedModel createFromParcel(Parcel in) {
            return new FeedModel(in);
        }

        @Override
        public FeedModel[] newArray(int size) {
            return new FeedModel[size];
        }
    };

    protected FeedModel(Parcel in) {
        entityID = in.readString();
        captureID = in.readString();
        shortID = in.readString();
        UUID = in.readString();
        creatorName = in.readString();
        creatorImage = in.readString();
        hatsOffStatus = in.readByte() != 0;
        followStatus = in.readByte() != 0;
        merchantable = in.readByte() != 0;
        downvoteStatus = in.readByte() != 0;
        isEligibleForDownvote = in.readByte() != 0;
        hatsOffCount = in.readLong();
        commentCount = in.readLong();
        contentType = in.readString();
        contentImage = in.readString();
        isAvailableForCollab = in.readByte() != 0;
        collabWithUUID = in.readString();
        collabWithName = in.readString();
        collabCount = in.readLong();
        caption = in.readString();
        collaboWithEntityID = in.readString();
        isLongForm = in.readByte() != 0;
        ;
    }

}
