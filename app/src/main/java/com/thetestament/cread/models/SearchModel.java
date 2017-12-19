package com.thetestament.cread.models;

/**
 * Model class for search system.
 */

public class SearchModel {

    private String profilePicUrl, userName, userUUID;
    private String searchType, hashTagLabel;
    private Long hashTagCount;


    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getHashTagLabel() {
        return hashTagLabel;
    }

    public void setHashTagLabel(String hashTagLabel) {
        this.hashTagLabel = hashTagLabel;
    }

    public Long getHashTagCount() {
        return hashTagCount;
    }

    public void setHashTagCount(Long hashTagCount) {
        this.hashTagCount = hashTagCount;
    }
}
