package com.example.uthsob3o.models;

public class BidModel {
    private String bidderName;
    private String bidAmount;
    private String timeAgo;
    private int rank;

    public BidModel(int rank, String bidderName, String bidAmount, String timeAgo) {
        this.rank = rank;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.timeAgo = timeAgo;
    }

    public int getRank() { return rank; }
    public String getBidderName() { return bidderName; }
    public String getBidAmount() { return bidAmount; }
    public String getTimeAgo() { return timeAgo; }
}