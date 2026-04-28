package com.example.uthsob3o.models;

public class CropModel {
    private String id;
    private String cropName;
    private String farmerName;
    private String location;
    private String weight;
    private String basePrice;
    private String imageUrl;
    private boolean isVerified;

    // Constructor
    public CropModel(String id, String cropName, String farmerName,
                     String location, String weight, String basePrice,
                     String imageUrl, boolean isVerified) {
        this.id = id;
        this.cropName = cropName;
        this.farmerName = farmerName;
        this.location = location;
        this.weight = weight;
        this.basePrice = basePrice;
        this.imageUrl = imageUrl;
        this.isVerified = isVerified;
    }

    // Getters
    public String getId() { return id; }
    public String getCropName() { return cropName; }
    public String getFarmerName() { return farmerName; }
    public String getLocation() { return location; }
    public String getWeight() { return weight; }
    public String getBasePrice() { return basePrice; }
    public String getImageUrl() { return imageUrl; }
    public boolean isVerified() { return isVerified; }
}