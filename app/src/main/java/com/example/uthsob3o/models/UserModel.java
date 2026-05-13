package com.example.uthsob3o.models;

public class UserModel {
    private String uid;
    private String name;
    private String phone;
    private String location;
    private String role; // "farmer" or "businessman"
    private String photoUrl;
    private String krishokId;   // farmers only
    private String dinNumber;   // businessmen only
    private String nationalId;  // never shown publicly
    private boolean verified;
    private String status;      // "pending", "approved", "rejected"

    // Empty constructor required for Firestore
    public UserModel() {}

    public UserModel(String uid, String name, String phone,
                     String location, String role) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.location = location;
        this.role = role;
        this.verified = false;
        this.status = "pending";
        this.photoUrl = "";
    }

    // Getters
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getRole() { return role; }
    public String getPhotoUrl() { return photoUrl; }
    public String getKrishokId() { return krishokId; }
    public String getDinNumber() { return dinNumber; }
    public String getNationalId() { return nationalId; }
    public boolean isVerified() { return verified; }
    public String getStatus() { return status; }

    // Setters
    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLocation(String location) { this.location = location; }
    public void setRole(String role) { this.role = role; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setKrishokId(String krishokId) { this.krishokId = krishokId; }
    public void setDinNumber(String dinNumber) { this.dinNumber = dinNumber; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setStatus(String status) { this.status = status; }
}