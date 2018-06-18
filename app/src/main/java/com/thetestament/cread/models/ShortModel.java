package com.thetestament.cread.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by prakharchandna on 29/03/18.
 */

public class ShortModel implements Parcelable {


    String contentText, bgcolor, font, imgTintColor, textColor, textGravity, imageURL, bgSound;
    boolean bold, italic, textShadow;
    double textSize, imgWidth;
    String liveFilterName;

    // Required constructor
    public ShortModel() {

    }


    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getBgcolor() {
        return bgcolor;
    }

    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getImgTintColor() {
        return imgTintColor;
    }

    public void setImgTintColor(String imgTintColor) {
        this.imgTintColor = imgTintColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getTextGravity() {
        return textGravity;
    }

    public void setTextGravity(String textGravity) {
        this.textGravity = textGravity;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isTextShadow() {
        return textShadow;
    }

    public void setTextShadow(boolean textShadow) {
        this.textShadow = textShadow;
    }

    public double getTextSize() {
        return textSize;
    }

    public void setTextSize(double textSize) {
        this.textSize = textSize;
    }

    public double getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(double imgWidth) {
        this.imgWidth = imgWidth;
    }

    public String getBgSound() {
        return bgSound;
    }

    public void setBgSound(String bgSound) {
        this.bgSound = bgSound;
    }

    public String getLiveFilterName() {
        return liveFilterName;
    }

    public void setLiveFilterName(String liveFilterName) {
        this.liveFilterName = liveFilterName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(contentText);
        parcel.writeString(bgcolor);
        parcel.writeString(font);
        parcel.writeString(imgTintColor);
        parcel.writeString(textColor);
        parcel.writeString(textGravity);
        parcel.writeString(imageURL);
        parcel.writeByte((byte) (bold ? 1 : 0));
        parcel.writeByte((byte) (italic ? 1 : 0));
        parcel.writeByte((byte) (textShadow ? 1 : 0));
        parcel.writeDouble(textSize);
        parcel.writeDouble(imgWidth);
        parcel.writeString(bgSound);
        parcel.writeString(liveFilterName);
    }


    public static final Creator<ShortModel> CREATOR = new Creator<ShortModel>() {
        @Override
        public ShortModel createFromParcel(Parcel in) {
            return new ShortModel(in);
        }

        @Override
        public ShortModel[] newArray(int size) {
            return new ShortModel[size];
        }
    };


    protected ShortModel(Parcel in) {

        contentText = in.readString();
        bgcolor = in.readString();
        font = in.readString();
        imgTintColor = in.readString();
        textColor = in.readString();
        textGravity = in.readString();
        imageURL = in.readString();
        bold = in.readByte() != 0;
        italic = in.readByte() != 0;
        textShadow = in.readByte() != 0;
        textSize = in.readDouble();
        imgWidth = in.readDouble();
        bgSound = in.readString();
        liveFilterName = in.readString();
    }

    /**
     * Converts int to boolean
     *
     * @param value int value to convert
     * @return
     */
    public static boolean convertIntToBool(int value) {
        return value == 1;
    }

    /**
     * Converts string to boolean
     *
     * @param value String value to convert
     * @return
     */
    public static boolean convertStringToBool(String value) {
        return convertIntToBool(Integer.parseInt(value));
    }
}
