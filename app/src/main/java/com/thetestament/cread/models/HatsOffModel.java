package com.thetestament.cread.models;

/**
 * Model class for hats off.
 */

public class HatsOffModel {

    private String uuid, firstName, LastName, profilePicUrl;
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
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
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
