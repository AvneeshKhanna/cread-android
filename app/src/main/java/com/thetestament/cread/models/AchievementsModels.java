package com.thetestament.cread.models;

/**
 * Models class for achievements screen data.
 */

public class AchievementsModels {
    private String badgeTitle, badgeImageUrl;
    private boolean badgeUnlock;
    private String unlockDescription;

    public String getBadgeTitle() {
        return badgeTitle;
    }

    public void setBadgeTitle(String badgeTitle) {
        this.badgeTitle = badgeTitle;
    }

    public String getBadgeImageUrl() {
        return badgeImageUrl;
    }

    public void setBadgeImageUrl(String badgeImageUrl) {
        this.badgeImageUrl = badgeImageUrl;
    }

    public boolean isBadgeUnlock() {
        return badgeUnlock;
    }

    public void setBadgeUnlock(boolean badgeUnlock) {
        this.badgeUnlock = badgeUnlock;
    }

    public String getUnlockDescription() {
        return unlockDescription;
    }

    public void setUnlockDescription(String unlockDescription) {
        this.unlockDescription = unlockDescription;
    }
}
