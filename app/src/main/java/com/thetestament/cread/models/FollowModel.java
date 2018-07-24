package com.thetestament.cread.models;


public class FollowModel {

    private String uuid, firstName, lastName, profilePicUrl;
    private boolean topArtist;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public boolean isTopArtist() {
        return topArtist;
    }

    public void setTopArtist(boolean topArtist) {
        this.topArtist = topArtist;
    }
}
