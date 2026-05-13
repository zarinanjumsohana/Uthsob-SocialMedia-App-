package com.example.uthsob3o.models;

public class NotificationModel {
    private String notifId;
    private String type;
    private String title;
    private String message;
    private String time;
    private boolean read;
    private String relatedId; // cropId or bidId

    public NotificationModel() {}

    public NotificationModel(String notifId, String type, String title,
                             String message, String time, String relatedId) {
        this.notifId = notifId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.time = time;
        this.read = false;
        this.relatedId = relatedId;
    }

    public String getNotifId() { return notifId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getTitleEn() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public boolean isRead() { return read; }
    public String getRelatedId() { return relatedId; }

    public void setNotifId(String notifId) { this.notifId = notifId; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setTime(String time) { this.time = time; }
    public void setRead(boolean read) { this.read = read; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
}