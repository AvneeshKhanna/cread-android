package com.thetestament.cread.models;

/**
 * Model class for SuggestedArtists.
 */

public class SuggestedArtistsModel {
    String artistName, artistUUID, artistProfilePic;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistUUID() {
        return artistUUID;
    }

    public void setArtistUUID(String artistUUID) {
        this.artistUUID = artistUUID;
    }

    public String getArtistProfilePic() {
        return artistProfilePic;
    }

    public void setArtistProfilePic(String artistProfilePic) {
        this.artistProfilePic = artistProfilePic;
    }
}
