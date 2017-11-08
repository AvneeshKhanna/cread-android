package com.thetestament.cread.models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for feed.
 */

public class FeedModel implements Parcelable {


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
    private String entityID, captureID;
    private String UUID, creatorName, creatorImage;
    private boolean hatsOffStatus, followStatus;
    private long hatsOffCount, commentCount;
    private String contentType;
    private String contentImage;


    //Required constructor
    public FeedModel() {
    }

    protected FeedModel(Parcel in) {
        entityID = in.readString();
        captureID = in.readString();
        UUID = in.readString();
        creatorName = in.readString();
        creatorImage = in.readString();
        hatsOffStatus = in.readByte() != 0;
        followStatus = in.readByte() != 0;
        hatsOffCount = in.readLong();
        commentCount = in.readLong();
        contentType = in.readString();
        contentImage = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(entityID);
        parcel.writeString(captureID);
        parcel.writeString(UUID);
        parcel.writeString(creatorName);
        parcel.writeString(creatorImage);
        parcel.writeByte((byte) (hatsOffStatus ? 1 : 0));
        parcel.writeByte((byte) (followStatus ? 1 : 0));
        parcel.writeLong(hatsOffCount);
        parcel.writeLong(commentCount);
        parcel.writeString(contentType);
        parcel.writeString(contentImage);
    }

}
