package com.thetestament.cread.models;

/**
 * Created by prakharchandna on 02/05/18.
 */

public class UserInterestsModel {


    private String interestName, interestImageURL, interestId;
    private boolean isUserInterested = false;


    public String getInterestName() {
        return interestName;
    }

    public void setInterestName(String interestName) {
        this.interestName = interestName;
    }

    public String getInterestImageURL() {
        return interestImageURL;
    }

    public void setInterestImageURL(String interestImageURL) {
        this.interestImageURL = interestImageURL;
    }

    public String getInterestId() {
        return interestId;
    }

    public void setInterestId(String interestId) {
        this.interestId = interestId;
    }

    public boolean isUserInterested() {
        return isUserInterested;
    }

    public void setUserInterested(boolean userInterested) {
        isUserInterested = userInterested;
    }
}
