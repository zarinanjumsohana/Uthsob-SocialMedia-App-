package com.example.uthsob3o.models;

public class StoryModel {
    private String storyId;
    private String userId;
    private String userName;
    private String userPhoto;
    private String imageUrl;
    private long timestamp;
    private long expiresAt; // 24 hours from creation

    public StoryModel() {}

    public StoryModel(String storyId, String userId,
                      String userName, String imageUrl) {
        this.storyId = storyId;
        this.userId = userId;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
        // Expires in 24 hours
        this.expiresAt = System.currentTimeMillis()
                + (24 * 60 * 60 * 1000);
    }

    // Getters
    public String getStoryId() { return storyId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserPhoto() { return userPhoto; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
    public long getExpiresAt() { return expiresAt; }

    // Check if story is still valid
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    // Setters
    public void setStoryId(String storyId) { this.storyId = storyId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserPhoto(String userPhoto) { this.userPhoto = userPhoto; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
}