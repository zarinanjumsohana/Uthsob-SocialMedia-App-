package com.example.uthsob3o.models;

public class UserModel {
    private String name;
    private String imageUrl;

    public UserModel(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}