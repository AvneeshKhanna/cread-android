package com.thetestament.cread.models;

/**
 * Model class for help.
 */

public class HelpModel {
    String helpID, titleText, descText, btnText;
    boolean isExpanded;

    public String getHelpID() {
        return helpID;
    }

    public void setHelpID(String helpID) {
        this.helpID = helpID;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getDescText() {
        return descText;
    }

    public void setDescText(String descText) {
        this.descText = descText;
    }

    public String getBtnText() {
        return btnText;
    }

    public void setBtnText(String btnText) {
        this.btnText = btnText;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
