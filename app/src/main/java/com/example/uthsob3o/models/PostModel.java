package com.example.uthsob3o.models;

public class PostModel {
    private String postId;
    private String userId;
    private String userName;
    private String userPhoto;
    private String userRole; // "farmer" or "businessman"
    private String content;
    private String productNeeded;
    private String quantity;
    private String budget;
    private String imageUrl;
    private long timestamp;
    private int likesCount;
    private int commentsCount;
    private String status; // "active", "closed"

    public PostModel() {}

    public PostModel(String postId, String userId,
                     String userName, String userRole,
                     String content) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.likesCount = 0;
        this.commentsCount = 0;
        this.status = "active";
        this.imageUrl = "";
    }

    // Getters
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserPhoto() { return userPhoto; }
    public String getUserRole() { return userRole; }
    public String getContent() { return content; }
    public String getProductNeeded() { return productNeeded; }
    public String getQuantity() { return quantity; }
    public String getBudget() { return budget; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }
    public int getLikesCount() { return likesCount; }
    public int getCommentsCount() { return commentsCount; }
    public String getStatus() { return status; }

    // Setters
    public void setPostId(String postId) { this.postId = postId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserPhoto(String userPhoto) { this.userPhoto = userPhoto; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public void setContent(String content) { this.content = content; }
    public void setProductNeeded(String productNeeded) { this.productNeeded = productNeeded; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setBudget(String budget) { this.budget = budget; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
    public void setStatus(String status) { this.status = status; }
}