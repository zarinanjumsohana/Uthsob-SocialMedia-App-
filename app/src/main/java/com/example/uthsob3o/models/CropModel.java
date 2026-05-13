package com.example.uthsob3o.models;

public class CropModel {
    private String cropId;
    private String farmerId;
    private String farmerName;
    private String farmerLocation;
    private boolean farmerVerified;
    private String cropName;
    private String scientificName;
    private String quantity;
    private String unit;
    private double basePrice;
    private double currentBid;
    private String imageUrl;
    private String cultivationDate;
    private String productionDate;
    private String expiryDate;
    private String auctionEndDate;
    private String status; // "active", "closed", "sold"
    private long timestamp;

    // Empty constructor for Firestore
    public CropModel() {}

    public CropModel(String cropId, String farmerId, String farmerName,
                     String farmerLocation, boolean farmerVerified,
                     String cropName, String quantity, String unit,
                     double basePrice, String auctionEndDate) {
        this.cropId = cropId;
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.farmerLocation = farmerLocation;
        this.farmerVerified = farmerVerified;
        this.cropName = cropName;
        this.quantity = quantity;
        this.unit = unit;
        this.basePrice = basePrice;
        this.currentBid = basePrice;
        this.auctionEndDate = auctionEndDate;
        this.status = "active";
        this.timestamp = System.currentTimeMillis();
        this.imageUrl = "";
    }

    // Getters
    public String getCropId() { return cropId; }
    public String getFarmerId() { return farmerId; }
    public String getFarmerName() { return farmerName; }
    public String getFarmerLocation() { return farmerLocation; }
    public boolean isFarmerVerified() { return farmerVerified; }
    public String getCropName() { return cropName; }
    public String getScientificName() { return scientificName; }
    public String getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getBasePrice() { return basePrice; }
    public double getCurrentBid() { return currentBid; }
    public String getImageUrl() { return imageUrl; }
    public String getCultivationDate() { return cultivationDate; }
    public String getProductionDate() { return productionDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getAuctionEndDate() { return auctionEndDate; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setCropId(String cropId) { this.cropId = cropId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public void setFarmerLocation(String farmerLocation) { this.farmerLocation = farmerLocation; }
    public void setFarmerVerified(boolean farmerVerified) { this.farmerVerified = farmerVerified; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public void setCurrentBid(double currentBid) { this.currentBid = currentBid; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCultivationDate(String cultivationDate) { this.cultivationDate = cultivationDate; }
    public void setProductionDate(String productionDate) { this.productionDate = productionDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setAuctionEndDate(String auctionEndDate) { this.auctionEndDate = auctionEndDate; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}