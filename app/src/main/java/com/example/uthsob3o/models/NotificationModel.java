package com.example.uthsob3o.models;

public class NotificationModel {
    private String title;
    private String titleEn;
    private String message;
    private String time;
    private String type; // "bid" or "auction"

    public NotificationModel(String title, String titleEn,
                             String message, String time, String type) {
        this.title = title;
        this.titleEn = titleEn;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getTitle() { return title; }
    public String getTitleEn() { return titleEn; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public String getType() { return type; }
}