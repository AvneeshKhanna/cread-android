package com.thetestament.cread.models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for explore feed.
 */

public class ExploreModel implements Parcelable {
    public static final Creator<ExploreModel> CREATOR = new Creator<ExploreModel>() {
        @Override
        public ExploreModel createFromParcel(Parcel in) {
            return new ExploreModel(in);
        }

        @Override
        public ExploreModel[] newArray(int size) {
            return new ExploreModel[size];
        }
    };
    String entityID;
    String uuID, creatorName, creatorImage;
    boolean hatsOffStatus, followStatus;
    long hatsOffCount, commentCount;
    String contentType;
    String image;
    String text, textSize, textColor;
    String coordinates;

    public ExploreModel() {
    }

    protected ExploreModel(Parcel in) {
        entityID = in.readString();
        uuID = in.readString();
        creatorName = in.readString();
        creatorImage = in.readString();
        hatsOffStatus = in.readByte() != 0;
        followStatus = in.readByte() != 0;
        hatsOffCount = in.readLong();
        commentCount = in.readLong();
        contentType = in.readString();
        image = in.readString();
        text = in.readString();
        textSize = in.readString();
        textColor = in.readString();
        coordinates = in.readString();
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getUuID() {
        return uuID;
    }

    public void setUuID(String uuID) {
        this.uuID = uuID;
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

    public boolean isHatsOffStatus() {
        return hatsOffStatus;
    }

    public void setHatsOffStatus(boolean hatsOffStatus) {
        this.hatsOffStatus = hatsOffStatus;
    }

    public boolean isFollowStatus() {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextSize() {
        return textSize;
    }

    public void setTextSize(String textSize) {
        this.textSize = textSize;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(entityID);
        parcel.writeString(uuID);
        parcel.writeString(creatorName);
        parcel.writeString(creatorImage);
        parcel.writeByte((byte) (hatsOffStatus ? 1 : 0));
        parcel.writeByte((byte) (followStatus ? 1 : 0));
        parcel.writeLong(hatsOffCount);
        parcel.writeLong(commentCount);
        parcel.writeString(contentType);
        parcel.writeString(image);
        parcel.writeString(text);
        parcel.writeString(textSize);
        parcel.writeString(textColor);
        parcel.writeString(coordinates);
    }
}