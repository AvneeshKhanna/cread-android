package com.thetestament.cread.models;

/**
 * Model class for ExploreCategory.
 */

public class ExploreCategoryModel {

    private String categoryText;

    public ExploreCategoryModel(String categoryText) {
        this.categoryText = categoryText;
    }

    public String getCategoryText() {
        return categoryText;
    }

    public void setCategoryText(String categoryText) {
        this.categoryText = categoryText;
    }

}
