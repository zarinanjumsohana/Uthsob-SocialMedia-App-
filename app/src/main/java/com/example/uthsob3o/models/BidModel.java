package com.example.uthsob3o.models;

public class BidModel {
    private String bidId;
    private String cropId;
    private String businessmanId;
    private String businessmanName;
    private double amount;
    private String timeAgo;
    private long timestamp;
    private String status; // "pending", "accepted", "rejected"
    private int rank;

    // Empty constructor for Firestore
    public BidModel() {}

    public BidModel(String bidId, String cropId, String businessmanId,
                    String businessmanName, double amount) {
        this.bidId = bidId;
        this.cropId = cropId;
        this.businessmanId = businessmanId;
        this.businessmanName = businessmanName;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
        this.timeAgo = "Just now";
    }

    // Getters
    public String getBidId() { return bidId; }
    public String getCropId() { return cropId; }
    public String getBusinessmanId() { return businessmanId; }
    public String getBusinessmanName() { return businessmanName; }
    public double getAmount() { return amount; }
    public String getTimeAgo() { return timeAgo; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public int getRank() { return rank; }
    public String getBidAmount() { return String.valueOf((int)amount); }
    public String getBidderName() { return businessmanName; }

    // Setters
    public void setBidId(String bidId) { this.bidId = bidId; }
    public void setCropId(String cropId) { this.cropId = cropId; }
    public void setBusinessmanId(String businessmanId) { this.businessmanId = businessmanId; }
    public void setBusinessmanName(String businessmanName) { this.businessmanName = businessmanName; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }
    public void setRank(int rank) { this.rank = rank; }
}