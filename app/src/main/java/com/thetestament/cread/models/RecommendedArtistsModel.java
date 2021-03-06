package com.thetestament.cread.models;

import java.util.List;

/**
 * Models for RecommendedArtists.
 */

public class RecommendedArtistsModel {
    private String artistName, artistProfilePic, artistBio, artistUUID;
    private long postCount;
    private List<UserPostModel> userPostList;


    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistProfilePic() {
        return artistProfilePic;
    }

    public void setArtistProfilePic(String artistProfilePic) {
        this.artistProfilePic = artistProfilePic;
    }

    public String getArtistBio() {
        return artistBio;
    }

    public void setArtistBio(String artistBio) {
        this.artistBio = artistBio;
    }

    public String getArtistUUID() {
        return artistUUID;
    }

    public void setArtistUUID(String artistUUID) {
        this.artistUUID = artistUUID;
    }

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }

    public List<UserPostModel> getUserPostList() {
        return userPostList;
    }

    public void setUserPostList(List<UserPostModel> userPostList) {
        this.userPostList = userPostList;
    }
}
