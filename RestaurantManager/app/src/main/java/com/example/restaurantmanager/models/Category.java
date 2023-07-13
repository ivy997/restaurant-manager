package com.example.restaurantmanager.models;

public class Category {
    private String categoryId;
    private String name;
    private String imageUrl;
    private String userId;

    public Category() {
    }

    public Category(String name, String imageUrl, String userId) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
