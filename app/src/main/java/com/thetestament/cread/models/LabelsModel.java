package com.thetestament.cread.models;

/**
 * Model class for labels.
 */

public class LabelsModel {
    String labelsID, label;
    boolean isSelected;

    public String getLabelsID() {
        return labelsID;
    }

    public void setLabelsID(String labelsID) {
        this.labelsID = labelsID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
