package com.thetestament.cread.models;

/**
 * Model class for chat list
 */

public class ChatListModel {
    private String profileImgUrl, receiverName, lastMessage, receiverUUID, chatID;
    private boolean unreadStatus;
    private String status;
    private int itemType;

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getReceiverUUID() {
        return receiverUUID;
    }

    public void setReceiverUUID(String receiverUUID) {
        this.receiverUUID = receiverUUID;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public boolean getUnreadStatus() {
        return unreadStatus;
    }

    public void setUnreadStatus(boolean unreadStatus) {
        this.unreadStatus = unreadStatus;
    }

    public String getFollowStatus() {
        return status;
    }

    public void setFollowStatus(String status) {
        this.status = status;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
