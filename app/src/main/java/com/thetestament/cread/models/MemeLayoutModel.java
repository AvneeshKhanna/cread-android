package com.thetestament.cread.models;

/**
 * Model class for font.
 */

public class MemeLayoutModel {
    private int drawableID;
    private String layoutName;

    public MemeLayoutModel(int drawableID, String layoutName) {
        this.drawableID = drawableID;
        this.layoutName = layoutName;
    }

    public int getDrawableID() {
        return drawableID;
    }

    public void setDrawableID(int drawableID) {
        this.drawableID = drawableID;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }
}
