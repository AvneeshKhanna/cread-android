package com.thetestament.cread.models;

/**
 * Model class for chat details.
 */

public class ChatDetailsModel {
    String message;
    String chatUserType;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChatUserType() {
        return chatUserType;
    }

    public void setChatUserType(String chatUserType) {
        this.chatUserType = chatUserType;
    }
}
